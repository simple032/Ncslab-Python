package com.ncslab.simulation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Logger;

import com.ncslab.ncslablink.ModelException;

final class SimulationBuildCache {
    private static final Logger logger = Logger.getLogger(SimulationBuildCache.class.getName());

    private static final String CACHE_VERSION = "semantic-runtime-param-cache-v2";
    private static final String COMPILE_CACHE_FILE = "ncslab_compile.sha256";
    private static final String SOURCE_CACHE_FILE = "ncslab_sources.sha256";

    private SimulationBuildCache() {
    }

    static String compileKey(String structureHash, boolean cudaMode, String host) throws ModelException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(CACHE_VERSION.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(host.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(Boolean.toString(cudaMode).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(structureHash.getBytes(StandardCharsets.UTF_8));
            return toHex(digest.digest());
        } catch (Exception e) {
            throw new ModelException("Unable to calculate compile cache key: " + e.getMessage());
        }
    }

    static boolean isCompileHit(File codeDir, String cacheKey) {
        File exe = new File(codeDir, "ncslab.exe");
        File marker = new File(codeDir, COMPILE_CACHE_FILE);
        return exe.isFile() && markerMatches(marker, cacheKey);
    }

    static void writeCompileMarker(File codeDir, String cacheKey) {
        writeMarker(new File(codeDir, COMPILE_CACHE_FILE), cacheKey, "compile");
    }

    static String sourceKey(File codeDir, boolean cudaMode, String host) throws ModelException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(CACHE_VERSION.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(host.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
            digest.update(Boolean.toString(cudaMode).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');

            File[] files = codeDir.listFiles(file -> file.isFile() && isNativeBuildInput(file.getName()));
            if (files != null) {
                Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                for (File file : files) {
                    digest.update(file.getName().getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    digest.update(Files.readAllBytes(file.toPath()));
                    digest.update((byte) '\n');
                }
            }
            return toHex(digest.digest());
        } catch (Exception e) {
            throw new ModelException("Unable to calculate native source cache key: " + e.getMessage());
        }
    }

    static boolean isSourceHit(File codeDir, String sourceKey) {
        File exe = new File(codeDir, "ncslab.exe");
        File marker = new File(codeDir, SOURCE_CACHE_FILE);
        return exe.isFile() && markerMatches(marker, sourceKey);
    }

    static void writeSourceMarker(File codeDir, String sourceKey) {
        writeMarker(new File(codeDir, SOURCE_CACHE_FILE), sourceKey, "native source");
    }

    private static boolean isNativeBuildInput(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h")
                || lower.endsWith(".hpp") || lower.equals("makefile");
    }

    private static boolean markerMatches(File marker, String expected) {
        if (!marker.isFile()) {
            return false;
        }
        try {
            String saved = Files.readString(marker.toPath(), StandardCharsets.UTF_8).trim();
            return expected.equals(saved);
        } catch (IOException e) {
            return false;
        }
    }

    private static void writeMarker(File marker, String value, String label) {
        try {
            Files.writeString(marker.toPath(), value, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warning("Unable to write " + label + " cache marker: " + e.getMessage());
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
