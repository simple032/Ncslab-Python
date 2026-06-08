package com.ncslab.simulation;

/**
 * Per-simulation-thread hint that the client requested a CUDA-oriented run. This is set when either:
 * <ul>
 *   <li>The client connected to {@code /websocketsimulatecuda}, or</li>
 *   <li>The client used {@code /websocketsimulate} and sent
 *       {@code preferCudaSimulation: true} on the {@code start} message (same WebSocket URL as normal
 *       simulation — no extra nginx {@code location} required).</li>
 * </ul>
 * Native code and generated simulation processes read {@link #preferCuda()} to select GPU-capable paths.
 */
public final class SimulationBackendContext {

    private static final ThreadLocal<Boolean> PREFER_CUDA = new ThreadLocal<>();
    private static final String DEFAULT_CUDA_MODEL_IDS = "*";

    private SimulationBackendContext() {
    }

    public static void setPreferCuda(boolean prefer) {
        if (prefer) {
            PREFER_CUDA.set(Boolean.TRUE);
        } else {
            PREFER_CUDA.remove();
        }
    }

    public static boolean preferCuda() {
        return Boolean.TRUE.equals(PREFER_CUDA.get());
    }

    public static boolean shouldPreferCudaForModel(Integer modelId) {
        String configured = System.getProperty("ncslab.cuda.model.ids");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("NCSLAB_CUDA_MODEL_IDS");
        }
        if (configured == null || configured.trim().isEmpty()) {
            configured = DEFAULT_CUDA_MODEL_IDS;
        }

        for (String token : configured.split("[,;\\s]+")) {
            String id = token.trim();
            if (id.isEmpty()) {
                continue;
            }
            if ("*".equals(id) || "all".equalsIgnoreCase(id)) {
                return true;
            }
            try {
                if (modelId != null && Integer.parseInt(id) == modelId) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed tokens so one typo does not disable the list.
            }
        }
        return false;
    }

    public static void clear() {
        PREFER_CUDA.remove();
    }
}
