import ast
import math


_SAFE_BINARY_OPERATORS = {
    ast.Add: lambda left, right: left + right,
    ast.Sub: lambda left, right: left - right,
    ast.Mult: lambda left, right: left * right,
    ast.Div: lambda left, right: left / right,
    ast.Pow: lambda left, right: left ** right,
    ast.Mod: lambda left, right: left % right,
}

_SAFE_UNARY_OPERATORS = {
    ast.UAdd: lambda value: value,
    ast.USub: lambda value: -value,
}


def evaluate_numeric_expression(value):
    if isinstance(value, (int, float)):
        return float(value)

    if isinstance(value, str):
        expression = value.strip()
        if not expression:
            return 0.0
        try:
            node = ast.parse(expression, mode="eval")
            return float(_evaluate_ast_node(node.body))
        except Exception:
            return 0.0

    try:
        return float(value)
    except (TypeError, ValueError):
        return 0.0


def _evaluate_ast_node(node):
    if isinstance(node, ast.Constant):
        if isinstance(node.value, (int, float)):
            return float(node.value)
        raise ValueError("Unsupported constant")

    if hasattr(ast, "Num") and isinstance(node, ast.Num):
        return float(node.n)

    if isinstance(node, ast.BinOp) and type(node.op) in _SAFE_BINARY_OPERATORS:
        left = _evaluate_ast_node(node.left)
        right = _evaluate_ast_node(node.right)
        return _SAFE_BINARY_OPERATORS[type(node.op)](left, right)

    if isinstance(node, ast.UnaryOp) and type(node.op) in _SAFE_UNARY_OPERATORS:
        return _SAFE_UNARY_OPERATORS[type(node.op)](_evaluate_ast_node(node.operand))

    if isinstance(node, ast.Name):
        if node.id == "pi":
            return math.pi
        if node.id == "e":
            return math.e
        if node.id.lower() in {"inf", "infinity"}:
            return math.inf
        raise ValueError(f"Unsupported name: {node.id}")

    if isinstance(node, ast.Attribute) and isinstance(node.value, ast.Name) and node.value.id == "math":
        if node.attr == "pi":
            return math.pi
        if node.attr == "e":
            return math.e
        if node.attr in {"inf", "infinity"}:
            return math.inf
        raise ValueError(f"Unsupported math attribute: {node.attr}")

    raise ValueError(f"Unsupported expression: {ast.dump(node)}")


def _to_scalar(value):
    return evaluate_numeric_expression(value)


def normalize_signal_value(value):
    if isinstance(value, dict) and "value" in value:
        value = value["value"]

    if hasattr(value, "tolist") and not isinstance(value, (str, bytes, list, tuple)):
        value = value.tolist()

    if isinstance(value, tuple):
        value = list(value)

    if isinstance(value, list):
        return [normalize_signal_value(item) for item in value]

    if isinstance(value, str):
        stripped = value.strip()
        if not stripped:
            return 0.0
        if stripped.startswith("[") and stripped.endswith("]"):
            return parse_signal_parameter(stripped)
        return _to_scalar(stripped)

    return _to_scalar(value)


def parse_signal_parameter(value):
    if isinstance(value, dict) and "value" in value:
        value = value["value"]

    if hasattr(value, "tolist") and not isinstance(value, (str, bytes, list, tuple)):
        value = value.tolist()

    if isinstance(value, tuple):
        value = list(value)

    if isinstance(value, list):
        return [parse_signal_parameter(item) for item in value]

    if isinstance(value, str):
        stripped = value.strip()
        if not stripped:
            return 0.0
        if stripped.startswith("[") and stripped.endswith("]"):
            inner = stripped[1:-1].strip()
            if not inner:
                return 0.0
            rows = [row.strip() for row in inner.split(";")]
            parsed_rows = []
            for row in rows:
                parts = [part for part in row.replace(",", " ").split() if part]
                parsed_rows.append([_to_scalar(part) for part in parts])
            if len(parsed_rows) == 1:
                if len(parsed_rows[0]) == 1:
                    return parsed_rows[0][0]
                return parsed_rows
            return parsed_rows
        return _to_scalar(stripped)

    return _to_scalar(value)


def flatten_signal_elements(value):
    normalized = normalize_signal_value(value)

    if isinstance(normalized, list):
        if not normalized:
            return [0.0]
        if any(isinstance(item, list) for item in normalized):
            flat = []
            for row in normalized:
                if isinstance(row, list):
                    flat.extend(flatten_signal_elements(row))
                else:
                    flat.append(_to_scalar(row))
            return flat if flat else [0.0]
        return [_to_scalar(item) for item in normalized]

    return [_to_scalar(normalized)]


def infer_signal_shape(value):
    normalized = normalize_signal_value(value)

    if isinstance(normalized, list):
        if not normalized:
            return 1, 1, [0.0]

        if any(isinstance(item, list) for item in normalized):
            rows = []
            for row in normalized:
                if isinstance(row, list):
                    rows.append([_to_scalar(item) for item in row])
                else:
                    rows.append([_to_scalar(row)])

            height = len(rows)
            width = max((len(row) for row in rows), default=1)
            flat = []
            for row in rows:
                padded = row + [0.0] * (width - len(row))
                flat.extend(padded)
            return max(height, 1), max(width, 1), flat if flat else [0.0]

        flat = [_to_scalar(item) for item in normalized]
        return max(len(flat), 1), 1, flat if flat else [0.0]

    return 1, 1, [_to_scalar(normalized)]


def scalar_of(value):
    _, _, flat = infer_signal_shape(value)
    return flat[0] if flat else 0.0


def reshape_signal(height, width, flat_values):
    total = max(1, height * width)
    flat = [_to_scalar(item) for item in flat_values[:total]]
    if len(flat) < total:
        flat.extend([0.0] * (total - len(flat)))

    if total == 1:
        return flat[0]
    if width == 1:
        return flat[:height]
    if height == 1:
        return [flat[:width]]

    rows = []
    for row_index in range(height):
        start = row_index * width
        rows.append(flat[start:start + width])
    return rows


def _broadcast_flat(flat_values, source_height, source_width, target_height, target_width):
    source_total = max(1, source_height * source_width)
    target_total = max(1, target_height * target_width)
    flat = [_to_scalar(item) for item in flat_values[:source_total]]
    if len(flat) < source_total:
        flat.extend([0.0] * (source_total - len(flat)))

    if source_total == target_total:
        return flat[:target_total]
    if source_total == 1:
        return [flat[0]] * target_total

    raise ValueError(
        f"Incompatible signal sizes: {source_height}x{source_width} vs {target_height}x{target_width}"
    )


def _resolve_common_shape(left, right):
    left_height, left_width, left_flat = infer_signal_shape(left)
    right_height, right_width, right_flat = infer_signal_shape(right)

    if left_height == right_height and left_width == right_width:
        return left_height, left_width, left_flat, right_flat

    left_total = left_height * left_width
    right_total = right_height * right_width

    if left_total == 1:
        return right_height, right_width, left_flat, right_flat
    if right_total == 1:
        return left_height, left_width, left_flat, right_flat
    if left_total == right_total:
        return left_height, left_width, left_flat, right_flat

    raise ValueError(
        f"Incompatible signal sizes: {left_height}x{left_width} vs {right_height}x{right_width}"
    )


def elementwise_unary_op(value, func):
    height, width, flat = infer_signal_shape(value)
    result = [func(item) for item in flat]
    return reshape_signal(height, width, result)


def elementwise_binary_op(left, right, func):
    target_height, target_width, left_flat, right_flat = _resolve_common_shape(left, right)
    left_height, left_width, _ = infer_signal_shape(left)
    right_height, right_width, _ = infer_signal_shape(right)
    left_values = _broadcast_flat(left_flat, left_height, left_width, target_height, target_width)
    right_values = _broadcast_flat(right_flat, right_height, right_width, target_height, target_width)
    result = [func(lval, rval) for lval, rval in zip(left_values, right_values)]
    return reshape_signal(target_height, target_width, result)


def elementwise_reduce(values, func):
    if not values:
        return 0.0
    result = values[0]
    for value in values[1:]:
        result = elementwise_binary_op(result, value, func)
    return result


def signal_add(left, right):
    return elementwise_binary_op(left, right, lambda a, b: a + b)


def signal_subtract(left, right):
    return elementwise_binary_op(left, right, lambda a, b: a - b)


def signal_multiply(left, right):
    return elementwise_binary_op(left, right, lambda a, b: a * b)


def signal_divide(left, right):
    return elementwise_binary_op(left, right, lambda a, b: a / b if b != 0 else 0.0)


def signal_min(left, right):
    return elementwise_binary_op(left, right, min)


def signal_max(left, right):
    return elementwise_binary_op(left, right, max)


def signal_compare(left, right, predicate):
    return elementwise_binary_op(left, right, lambda a, b: 1.0 if predicate(a, b) else 0.0)


def signal_matrix_multiply(left, right):
    left_height, left_width, left_flat = infer_signal_shape(left)
    right_height, right_width, right_flat = infer_signal_shape(right)

    left_total = left_height * left_width
    right_total = right_height * right_width

    if left_total == 1 or right_total == 1:
        return signal_multiply(left, right)

    if left_width != right_height:
        if left_total == right_total:
            return signal_multiply(left, right)
        raise ValueError(
            f"Incompatible matrix shapes: {left_height}x{left_width} vs {right_height}x{right_width}"
        )

    result = []
    for row in range(left_height):
        for col in range(right_width):
            total = 0.0
            for idx in range(left_width):
                total += left_flat[row * left_width + idx] * right_flat[idx * right_width + col]
            result.append(total)

    return reshape_signal(left_height, right_width, result)
