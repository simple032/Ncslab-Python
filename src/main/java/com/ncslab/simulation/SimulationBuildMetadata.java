package com.ncslab.simulation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;

final class SimulationBuildMetadata {
    private static final String FILE_NAME = "simulation_build_metadata.json";

    private SimulationBuildMetadata() {
    }

    static void write(
            File codeDir,
            String host,
            SimulationBackend backend,
            ModelFingerprint.Fingerprint fingerprint,
            boolean compileCacheHit,
            boolean sourceCacheHit,
            long prepareWallMs) throws IOException {
        String json = "{\n"
                + "  \"createdAt\": \"" + escape(Instant.now().toString()) + "\",\n"
                + "  \"host\": \"" + escape(host) + "\",\n"
                + "  \"backend\": \"" + escape(backend.id()) + "\",\n"
                + "  \"backendDisplayName\": \"" + escape(backend.displayName()) + "\",\n"
                + "  \"cudaBuild\": " + backend.useCudaBuild() + ",\n"
                + "  \"structureHash\": \"" + escape(fingerprint.getStructureHash()) + "\",\n"
                + "  \"parameterHash\": \"" + escape(fingerprint.getParameterHash()) + "\",\n"
                + "  \"structurePayloadLength\": " + fingerprint.getStructurePayloadLength() + ",\n"
                + "  \"parameterPayloadLength\": " + fingerprint.getParameterPayloadLength() + ",\n"
                + "  \"compileCacheHit\": " + compileCacheHit + ",\n"
                + "  \"sourceCacheHit\": " + sourceCacheHit + ",\n"
                + "  \"prepareWallMs\": " + prepareWallMs + "\n"
                + "}\n";
        Files.writeString(new File(codeDir, FILE_NAME).toPath(), json, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
