package com.ncslab.simulation;

import com.ncslab.dto.model.MdlDataDto;

public enum SimulationBackend {
    CPU("cpu", "CPU", "Std-Simulation-", "simulation_backend_cpu", false),
    CUDA("cuda", "CUDA", "Cuda-Simulation-", "simulation_backend_cuda", true);

    private final String id;
    private final String displayName;
    private final String threadPrefix;
    private final String statusMessage;
    private final boolean cudaBuild;

    SimulationBackend(String id, String displayName, String threadPrefix, String statusMessage, boolean cudaBuild) {
        this.id = id;
        this.displayName = displayName;
        this.threadPrefix = threadPrefix;
        this.statusMessage = statusMessage;
        this.cudaBuild = cudaBuild;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String threadPrefix() {
        return threadPrefix;
    }

    public String statusMessage() {
        return statusMessage;
    }

    public boolean useCudaBuild() {
        return cudaBuild;
    }

    public void beforeRun() {
        if (cudaBuild) {
            SimulationBackendContext.setPreferCuda(true);
        }
    }

    public void afterRun() {
        if (cudaBuild) {
            SimulationBackendContext.clear();
        }
    }

    public static SimulationBackend select(MdlDataDto mdlData, boolean requestCuda, String logTag) {
        if (requestCuda) {
            return CUDA;
        }
        if (SimulationBackendContext.shouldPreferCudaForModel(mdlData.getModelId())) {
            System.out.println(logTag + " CUDA enabled for modelId=" + mdlData.getModelId());
            return CUDA;
        }
        return CPU;
    }
}
