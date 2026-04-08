from dataclasses import dataclass


def _normalize_block_type(value):
    text = str(value or "").strip().lower()
    for token in (" ", "-", "_", "\t"):
        text = text.replace(token, "")
    return text


def _is_electrical_port(port_name):
    port_text = str(port_name or "")
    lower = port_text.lower()
    return lower.startswith("e") or "conn" in lower


def _terminal_key(port_name):
    text = str(port_name or "").lower()
    if "lconn" in text or text.endswith("+") or "plus" in text:
        return "p"
    if "rconn" in text or text.endswith("-") or "minus" in text:
        return "n"
    if text in {"p", "pos", "positive"}:
        return "p"
    if text in {"n", "neg", "negative"}:
        return "n"
    return None


@dataclass
class CircuitElement:
    block_name: str
    block_type: str
    block_uuid: str
    p_node: int
    n_node: int
    params: dict
    port_nodes: dict


class UnionFind:
    def __init__(self):
        self.parent = {}

    def find(self, value):
        if value not in self.parent:
            self.parent[value] = value
            return value
        root = value
        while self.parent[root] != root:
            root = self.parent[root]
        while self.parent[value] != value:
            next_value = self.parent[value]
            self.parent[value] = root
            value = next_value
        return root

    def union(self, left, right):
        left_root = self.find(left)
        right_root = self.find(right)
        if left_root != right_root:
            self.parent[right_root] = left_root


class CircuitNetlistBuilder:
    def __init__(self, blocks_data, graph_data):
        self.blocks_data = blocks_data or []
        self.graph_data = graph_data or {}

    def build(self):
        block_by_uuid = {}
        terminals = {}
        for block in self.blocks_data:
            block_uuid = block.get("blockUUID") or ""
            if not block_uuid:
                continue
            block_by_uuid[block_uuid] = block
            terminals[block_uuid] = {"p": None, "n": None, "extra": []}

        links = []
        for cell in self.graph_data.get("cells", []):
            if cell.get("type") != "standard.Link":
                continue
            source = cell.get("source", {})
            target = cell.get("target", {})
            source_id = source.get("id")
            target_id = target.get("id")
            source_port = source.get("port")
            target_port = target.get("port")
            if not source_id or not target_id:
                continue
            if not _is_electrical_port(source_port) or not _is_electrical_port(target_port):
                continue
            links.append((source_id, source_port, target_id, target_port))

        if not links:
            return []

        uf = UnionFind()
        endpoint_keys = []
        for source_id, source_port, target_id, target_port in links:
            left_key = f"{source_id}:{source_port}"
            right_key = f"{target_id}:{target_port}"
            endpoint_keys.append((source_id, source_port))
            endpoint_keys.append((target_id, target_port))
            uf.union(left_key, right_key)

        node_index_map = {}
        next_node = 0
        endpoint_node = {}
        for block_uuid, port_name in endpoint_keys:
            key = f"{block_uuid}:{port_name}"
            root = uf.find(key)
            if root not in node_index_map:
                node_index_map[root] = next_node
                next_node += 1
            endpoint_node[key] = node_index_map[root]

        for block_uuid, port_name in endpoint_keys:
            key = f"{block_uuid}:{port_name}"
            node = endpoint_node[key]
            terminal = terminals.get(block_uuid)
            if terminal is None:
                continue
            t_key = _terminal_key(port_name)
            if t_key == "p" and terminal["p"] is None:
                terminal["p"] = node
            elif t_key == "n" and terminal["n"] is None:
                terminal["n"] = node
            else:
                terminal["extra"].append(node)

        elements = []
        for block_uuid, block in block_by_uuid.items():
            block_type = block.get("blockType", "")
            normalized = _normalize_block_type(block_type)
            if normalized not in {
                "resistor",
                "capacitor",
                "inductor",
                "variableresistor",
                "variableinductor",
                "variablecapacitor",
                "seriesrlcbranch",
                "dcvoltagesource",
                "acvoltagesource",
                "dccurrentsource",
                "accurrentsource",
                "controlledvoltagesource",
                "controlledcurrentsource",
                "voltagesensor",
                "currentsensor",
                "voltmeter",
                "ammeter",
                "circuitswitch",
                "diode",
                "opamp",
                "igbt",
                "mosfet",
                "ground",
                "electricalreference",
            }:
                continue

            terminal = terminals.get(block_uuid, {})
            port_nodes = {}
            for node in [terminal.get("p"), terminal.get("n")]:
                if node is not None:
                    port_nodes[str(node)] = node
            for cell in self.graph_data.get("cells", []):
                if cell.get("type") != "standard.Link":
                    continue
                source = cell.get("source", {})
                target = cell.get("target", {})
                if source.get("id") == block_uuid and _is_electrical_port(source.get("port")):
                    key = f"{block_uuid}:{source.get('port')}"
                    if key in endpoint_node:
                        port_nodes[str(source.get("port")).lower()] = endpoint_node[key]
                if target.get("id") == block_uuid and _is_electrical_port(target.get("port")):
                    key = f"{block_uuid}:{target.get('port')}"
                    if key in endpoint_node:
                        port_nodes[str(target.get("port")).lower()] = endpoint_node[key]

            node_candidates = [node for node in [terminal.get("p"), terminal.get("n")] if node is not None]
            node_candidates.extend(terminal.get("extra", []))
            if len(node_candidates) == 0:
                continue
            if len(node_candidates) == 1:
                p_node = node_candidates[0]
                n_node = node_candidates[0]
            else:
                p_node = node_candidates[0]
                n_node = node_candidates[1]

            elements.append(
                CircuitElement(
                    block_name=block.get("blockName", ""),
                    block_type=block_type,
                    block_uuid=block_uuid,
                    p_node=p_node,
                    n_node=n_node,
                    params=block.get("paramValues", {}) or {},
                    port_nodes=port_nodes,
                )
            )

        # Simscape-like behavior: all Electrical Reference/Ground symbols define
        # the same global reference potential even if not explicitly wired.
        reference_nodes = set()
        for element in elements:
            normalized = _normalize_block_type(element.block_type)
            if normalized in {"ground", "electricalreference"}:
                reference_nodes.add(element.p_node)
                reference_nodes.add(element.n_node)
        if reference_nodes:
            global_ref = min(reference_nodes)
            for element in elements:
                if element.p_node in reference_nodes:
                    element.p_node = global_ref
                if element.n_node in reference_nodes:
                    element.n_node = global_ref
                if element.port_nodes:
                    remapped = {}
                    for key, value in element.port_nodes.items():
                        remapped[key] = global_ref if value in reference_nodes else value
                    element.port_nodes = remapped

        return elements
