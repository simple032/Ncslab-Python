package com.ncslab.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.model.ConfigDto;
import com.ncslab.dto.model.LineDto;
import com.ncslab.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Separates model structure from runtime-tunable parameters so repeated
 * simulations can reuse a compiled executable when only safe scalar values
 * changed.
 */
public final class ModelFingerprint {
    public static final String RUNTIME_PARAM_FILE = "runtime_params.tsv";

    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapper().copy()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    private static final Set<String> STRUCTURAL_PARAMETER_NAMES = Set.of(
            "inputs", "number", "numberofinputs", "outputs", "numberofoutputs",
            "no", "width", "height", "dimension", "dimensions", "sampletime",
            "fixedstep", "step", "solver", "starttime", "maxtimestep", "mintimestep",
            "maxstep", "minstep", "initialstep", "outdatatypestr",
            "saturateonintegeroverflow", "multiplication", "tag", "gototag",
            "tagvisibility", "blocktype", "seriesType".toLowerCase(Locale.ROOT));

    private ModelFingerprint() {
    }

    public static Fingerprint from(ModelDto model) throws IOException {
        String structurePayload = canonicalJson(structurePayload(model));
        String parameterPayload = canonicalJson(parameterPayload(model));
        return new Fingerprint(
                sha256(structurePayload),
                sha256(parameterPayload),
                structurePayload.length(),
                parameterPayload.length());
    }

    public static int writeRuntimeParams(ModelDto model, File codeDir) throws IOException {
        List<String> lines = runtimeParamLines(model);
        File target = new File(codeDir, RUNTIME_PARAM_FILE);
        Files.write(target.toPath(), lines, StandardCharsets.UTF_8);
        return Math.max(0, lines.size() - 1);
    }

    private static Map<String, Object> structurePayload(ModelDto model) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("modelName", norm(model.getModelName()));
        root.put("modelRealName", norm(model.getModelRealName()));
        root.put("templateName", norm(model.getTemplateName()));
        root.put("config", structureConfig(model.getConfig()));
        root.put("blocks", structureBlocks(model.getBlocks()));
        root.put("lines", structureLines(model.getLines()));
        return root;
    }

    private static Map<String, Object> parameterPayload(ModelDto model) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> config = new LinkedHashMap<>();
        ConfigDto cfg = model.getConfig();
        if (cfg != null) {
            config.put("StopTime", value(cfg.getStopTime()));
        }
        root.put("config", config);
        root.put("runtimeParams", runtimeParamObjects(model));
        return root;
    }

    private static Map<String, Object> structureConfig(ConfigDto cfg) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (cfg == null) {
            return out;
        }
        out.put("Step", value(cfg.getStep()));
        out.put("FixedStep", value(cfg.getFixedStep()));
        out.put("Solver", value(cfg.getSolver()));
        out.put("StartTime", value(cfg.getStartTime()));
        out.put("MaxDataPoints", value(cfg.getMaxDataPoints()));
        out.put("MaxStep", value(cfg.getMaxStep()));
        out.put("MinStep", value(cfg.getMinStep()));
        out.put("InitialStep", value(cfg.getInitialStep()));
        out.put("RelTol", value(cfg.getRelTol()));
        out.put("AbsTol", value(cfg.getAbsTol()));
        out.put("TemplateMakefile", value(cfg.getTemplateMakefile()));
        out.put("SystemTargetFile", value(cfg.getSystemTargetFile()));
        return out;
    }

    private static List<Map<String, Object>> structureBlocks(List<BlockDto> blocks) {
        if (blocks == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (BlockDto block : blocks) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("uuid", blockKey(block));
            item.put("type", norm(block.getBlockType()));
            item.put("name", norm(block.getBlockName()));
            item.put("path", norm(block.getBlockPath()));
            item.put("srcBlock", norm(block.getSrcBlock()));
            item.put("params", nonRuntimeParams(block));
            out.add(item);
        }
        out.sort(Comparator.comparing(ModelFingerprint::sortKey));
        return out;
    }

    private static List<Map<String, Object>> structureLines(List<LineDto> lines) {
        if (lines == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (LineDto line : lines) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fromUuid", norm(line.getFromBlockUUID()));
            item.put("fromName", norm(line.getFromBlockName()));
            item.put("fromPort", value(line.getFromPortNo()));
            item.put("toUuid", norm(line.getToBlockUUID()));
            item.put("toName", norm(line.getToBlockName()));
            item.put("toPort", value(line.getToPortNo()));
            item.put("path", norm(line.getLinePath()));
            item.put("seriesType", norm(line.getSeriesType()));
            out.add(item);
        }
        out.sort(Comparator.comparing(ModelFingerprint::sortKey));
        return out;
    }

    private static Map<String, Object> nonRuntimeParams(BlockDto block) {
        Map<String, Object> params = block.getParamValues();
        if (params == null || params.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> out = new TreeMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (isRuntimeParam(block, entry.getKey(), entry.getValue())) {
                MatrixValue matrix = parseMatrix(entry.getValue());
                if (matrix != null) {
                    Map<String, Object> shape = new LinkedHashMap<>();
                    shape.put("rows", matrix.rows());
                    shape.put("cols", matrix.cols());
                    out.put(entry.getKey(), shape);
                }
                continue;
            }
            out.put(entry.getKey(), value(entry.getValue()));
        }
        return out;
    }

    private static List<Map<String, Object>> runtimeParamObjects(ModelDto model) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (model.getBlocks() == null) {
            return out;
        }
        for (BlockDto block : model.getBlocks()) {
            Map<String, Object> params = block.getParamValues();
            if (params == null) {
                continue;
            }
            for (String param : runtimeParamNames(block)) {
                Object raw = params.get(param);
                Double numeric = parseScalar(raw);
                MatrixValue matrix = parseMatrix(raw);
                if (numeric == null && matrix == null) {
                    continue;
                }
                if (numeric != null) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("key", blockKey(block) + "." + param);
                    item.put("value", numeric);
                    out.add(item);
                } else {
                    for (int i = 0; i < matrix.rows(); i++) {
                        for (int j = 0; j < matrix.cols(); j++) {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("key", blockKey(block) + "." + param + "[" + i + "," + j + "]");
                            item.put("value", matrix.values[i][j]);
                            out.add(item);
                        }
                    }
                }
            }
        }
        out.sort(Comparator.comparing(ModelFingerprint::sortKey));
        return out;
    }

    private static List<String> runtimeParamLines(ModelDto model) {
        List<String> lines = new ArrayList<>();
        lines.add("# key\tvalue");
        if (model.getBlocks() == null) {
            return lines;
        }
        for (BlockDto block : model.getBlocks()) {
            Map<String, Object> params = block.getParamValues();
            if (params == null) {
                continue;
            }
            for (String param : runtimeParamNames(block)) {
                Double numeric = parseScalar(params.get(param));
                MatrixValue matrix = parseMatrix(params.get(param));
                if (numeric == null && matrix == null) {
                    continue;
                }
                String primaryKey = blockKey(block) + "." + param;
                if (numeric != null) {
                    addRuntimeLine(lines, primaryKey, numeric);
                    for (String aliasParam : runtimeParamAliases(param)) {
                        addRuntimeLine(lines, blockKey(block) + "." + aliasParam, numeric);
                    }
                    addRuntimeLine(lines, pathKey(block) + "." + param, numeric);
                    for (String aliasParam : runtimeParamAliases(param)) {
                        addRuntimeLine(lines, pathKey(block) + "." + aliasParam, numeric);
                    }
                } else {
                    for (int i = 0; i < matrix.rows(); i++) {
                        for (int j = 0; j < matrix.cols(); j++) {
                            String suffix = "[" + i + "," + j + "]";
                            addRuntimeLine(lines, primaryKey + suffix, matrix.values[i][j]);
                            for (String aliasParam : runtimeParamAliases(param)) {
                                addRuntimeLine(lines, blockKey(block) + "." + aliasParam + suffix, matrix.values[i][j]);
                            }
                            addRuntimeLine(lines, pathKey(block) + "." + param + suffix, matrix.values[i][j]);
                            for (String aliasParam : runtimeParamAliases(param)) {
                                addRuntimeLine(lines, pathKey(block) + "." + aliasParam + suffix, matrix.values[i][j]);
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(lines.subList(1, lines.size()));
        return lines;
    }

    private static void addRuntimeLine(List<String> lines, String key, double value) {
        String line = key + "\t" + formatDouble(value);
        if (!lines.contains(line)) {
            lines.add(line);
        }
    }

    private static List<String> runtimeParamNames(BlockDto block) {
        List<String> names = new ArrayList<>();
        Map<String, Object> params = block.getParamValues();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (isRuntimeParam(block, entry.getKey(), entry.getValue())) {
                    names.add(entry.getKey());
                }
            }
        }
        String type = norm(block.getBlockType()).toLowerCase(Locale.ROOT);
        addIfPresent(names, block, "constant".equals(type), "Value");
        addIfPresent(names, block, "gain".equals(type), "Gain");
        addIfPresent(names, block, type.contains("resistor"), "R");
        addIfPresent(names, block, type.contains("capacitor"), "c");
        addIfPresent(names, block, type.contains("capacitor"), "C");
        addIfPresent(names, block, type.contains("inductor"), "l");
        addIfPresent(names, block, type.contains("inductor"), "L");
        Collections.sort(names);
        return names;
    }

    private static void addIfPresent(List<String> names, BlockDto block, boolean enabled, String paramName) {
        if (!enabled || names.contains(paramName)) {
            return;
        }
        Map<String, Object> params = block.getParamValues();
        if (params != null && params.containsKey(paramName)
                && (parseScalar(params.get(paramName)) != null || parseMatrix(params.get(paramName)) != null)) {
            names.add(paramName);
        }
    }

    private static List<String> runtimeParamAliases(String param) {
        if ("c".equals(param)) {
            return Collections.singletonList("C");
        }
        if ("C".equals(param)) {
            return Collections.singletonList("c");
        }
        if ("l".equals(param)) {
            return Collections.singletonList("L");
        }
        if ("L".equals(param)) {
            return Collections.singletonList("l");
        }
        return Collections.emptyList();
    }

    private static boolean isRuntimeParam(BlockDto block, String paramName, Object rawValue) {
        if (paramName == null || (parseScalar(rawValue) == null && parseMatrix(rawValue) == null)) {
            return false;
        }
        String lower = paramName.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        if (STRUCTURAL_PARAMETER_NAMES.contains(lower)) {
            return false;
        }
        return true;
    }

    private static String blockKey(BlockDto block) {
        String uuid = norm(block.getBlockUUID());
        if (!uuid.isEmpty() && !"null".equalsIgnoreCase(uuid)) {
            return uuid;
        }
        return pathKey(block);
    }

    private static String pathKey(BlockDto block) {
        return norm(block.getBlockPath()) + "/" + norm(block.getBlockName());
    }

    private static String sortKey(Map<String, Object> map) {
        return map.toString();
    }

    private static Object value(Object raw) {
        if (raw == null) {
            return "";
        }
        if (raw instanceof Number || raw instanceof Boolean) {
            return raw;
        }
        return raw.toString();
    }

    private static String norm(String value) {
        return value == null ? "" : value.trim();
    }

    private static Double parseScalar(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }
        String text = raw.toString().trim();
        if (text.isEmpty() || text.startsWith("[") || text.contains(" ")) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static MatrixValue parseMatrix(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof List<?>) {
            return parseListMatrix((List<?>) raw);
        }
        String text = raw.toString().trim();
        if (!text.startsWith("[") || !text.endsWith("]")) {
            return null;
        }
        String body = text.substring(1, text.length() - 1).trim();
        if (body.isEmpty()) {
            return null;
        }
        String[] rowTexts = body.split(";");
        List<double[]> rows = new ArrayList<>();
        int cols = -1;
        for (String rowText : rowTexts) {
            String normalized = rowText.trim().replace(",", " ");
            if (normalized.isEmpty()) {
                return null;
            }
            String[] tokens = normalized.split("\\s+");
            if (cols < 0) {
                cols = tokens.length;
            } else if (cols != tokens.length) {
                return null;
            }
            double[] row = new double[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                try {
                    row[i] = Double.parseDouble(tokens[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            rows.add(row);
        }
        return MatrixValue.fromRows(rows);
    }

    private static MatrixValue parseListMatrix(List<?> raw) {
        if (raw.isEmpty()) {
            return null;
        }
        if (!(raw.get(0) instanceof List<?>)) {
            double[][] values = new double[1][raw.size()];
            for (int i = 0; i < raw.size(); i++) {
                Double value = parseScalar(raw.get(i));
                if (value == null) {
                    return null;
                }
                values[0][i] = value;
            }
            return new MatrixValue(values);
        }
        List<double[]> rows = new ArrayList<>();
        int cols = -1;
        for (Object rowObj : raw) {
            if (!(rowObj instanceof List<?>)) {
                return null;
            }
            List<?> rowList = (List<?>) rowObj;
            if (cols < 0) {
                cols = rowList.size();
            } else if (cols != rowList.size()) {
                return null;
            }
            double[] row = new double[rowList.size()];
            for (int i = 0; i < rowList.size(); i++) {
                Double value = parseScalar(rowList.get(i));
                if (value == null) {
                    return null;
                }
                row[i] = value;
            }
            rows.add(row);
        }
        return MatrixValue.fromRows(rows);
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.17g", value);
    }

    private static String canonicalJson(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }

    private static String sha256(String value) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not available", e);
        }
    }

    public static final class Fingerprint {
        private final String structureHash;
        private final String parameterHash;
        private final int structurePayloadLength;
        private final int parameterPayloadLength;

        private Fingerprint(String structureHash, String parameterHash,
                            int structurePayloadLength, int parameterPayloadLength) {
            this.structureHash = Objects.requireNonNull(structureHash);
            this.parameterHash = Objects.requireNonNull(parameterHash);
            this.structurePayloadLength = structurePayloadLength;
            this.parameterPayloadLength = parameterPayloadLength;
        }

        public String getStructureHash() {
            return structureHash;
        }

        public String getParameterHash() {
            return parameterHash;
        }

        public int getStructurePayloadLength() {
            return structurePayloadLength;
        }

        public int getParameterPayloadLength() {
            return parameterPayloadLength;
        }
    }

    private static final class MatrixValue {
        private final double[][] values;

        private MatrixValue(double[][] values) {
            this.values = values;
        }

        private static MatrixValue fromRows(List<double[]> rows) {
            double[][] values = new double[rows.size()][];
            for (int i = 0; i < rows.size(); i++) {
                values[i] = rows.get(i);
            }
            return new MatrixValue(values);
        }

        private int rows() {
            return values.length;
        }

        private int cols() {
            return values.length == 0 ? 0 : values[0].length;
        }
    }
}
