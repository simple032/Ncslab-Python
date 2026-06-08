package com.ncslab.simulation;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.c.windows.simulation.CodeModelCWindowsSimulation;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.model.MdlDataDto;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.util.JsonUtils;
import com.ncslab.util.UserContext;

public final class SimulationService {
    private static final Logger logger = Logger.getLogger(SimulationService.class.getName());

    public interface ProgressSink {
        void status(String status) throws IOException;

        void simulating(double endTime) throws IOException;
    }

    public static final class PreparedSimulation {
        private final CodeModelC model;
        private final SimulationBackend backend;

        PreparedSimulation(CodeModelC model, SimulationBackend backend) {
            this.model = model;
            this.backend = backend;
        }

        public CodeModelC getModel() {
            return model;
        }

        public SimulationBackend getBackend() {
            return backend;
        }
    }

    public PreparedSimulation prepareSimulation(
            MdlDataDto mdlData,
            boolean requestCuda,
            ProgressSink progress,
            String logTag) throws Exception {
        if (mdlData == null) {
            throw new ModelException("No mdlData found in WebSocket message");
        }

        SimulationBackend backend = SimulationBackend.select(mdlData, requestCuda, logTag);
        setUserContext(mdlData, logTag);

        String jsonDataString = mdlData.getJsonDataString();
        if (jsonDataString == null) {
            throw new ModelException("No jsonData found in mdlData");
        }

        progress.status("generating");

        String host = detectHost();
        System.out.println("Running on " + host + " with DTO-enhanced simulation service (" + logTag + ")");

        ModelDto modelDto = parseModelDto(jsonDataString);
        ModelFingerprint.Fingerprint fingerprint = ModelFingerprint.from(modelDto);
        System.out.println(logTag + " Model fingerprint structure="
                + fingerprint.getStructureHash().substring(0, 12)
                + " params=" + fingerprint.getParameterHash().substring(0, 12)
                + " structurePayload=" + fingerprint.getStructurePayloadLength()
                + " paramPayload=" + fingerprint.getParameterPayloadLength());

        CodeModelC model = createModel(modelDto, host);
        if (model == null) {
            throw new ModelException("Failed to create WebSocket simulation model from DTO");
        }

        System.out.println("WebSocket model created successfully: " + model.getModelName()
                + " on " + host + " with " + model.getBlockList().size() + " blocks");

        prepareBuildArtifacts(modelDto, model, fingerprint, backend, host, progress);
        progress.simulating(model.getConfig().getStopTime());
        return new PreparedSimulation(model, backend);
    }

    private void setUserContext(MdlDataDto mdlData, String logTag) {
        Integer userId = mdlData.getUserId();
        if (userId != null) {
            UserContext.setUserId(userId);
            System.out.println(logTag + " Set user context to user ID: " + userId);
        } else {
            System.out.println("Warning: No user ID in mdlData, expression parsing will use default user ID");
        }
    }

    private ModelDto parseModelDto(String jsonDataString) throws ModelException {
        String validationError = JsonUtils.validateJsonStructure(jsonDataString);
        if (validationError != null) {
            throw new ModelException("JSON validation failed: " + validationError);
        }

        ModelDto modelDto;
        try {
            modelDto = JsonUtils.getObjectMapper().readValue(jsonDataString, ModelDto.class);
        } catch (JsonProcessingException e) {
            logger.severe("Failed to parse JSON to ModelDto: " + e.getMessage());
            throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
        }

        if (!modelDto.isValid()) {
            throw new ModelException("Invalid ModelDto DTO structure");
        }
        System.out.println("Using DTO-based WebSocket model creation for: " + modelDto.getModelName());
        return modelDto;
    }

    private CodeModelC createModel(ModelDto modelDto, String host) throws ModelException {
        if (Objects.equals(host, "Windows")) {
            return CodeModelCWindowsSimulation.createFromDto(modelDto, ModelMode.Simulation);
        }
        return CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Simulation);
    }

    private void prepareBuildArtifacts(
            ModelDto modelDto,
            CodeModelC model,
            ModelFingerprint.Fingerprint fingerprint,
            SimulationBackend backend,
            String host,
            ProgressSink progress) throws IOException, ModelException {
        long t0 = System.nanoTime();
        String codePath = model.prepareGeneratedCodePath();
        File codeDir = new File(codePath);
        int runtimeParamCount = ModelFingerprint.writeRuntimeParams(modelDto, codeDir);
        System.out.println("[ModelFingerprint] wrote " + runtimeParamCount
                + " runtime params to " + new File(codeDir, ModelFingerprint.RUNTIME_PARAM_FILE).getAbsolutePath());

        String cacheKey = SimulationBuildCache.compileKey(fingerprint.getStructureHash(), backend.useCudaBuild(), host);
        model.setCudaSimulationRequested(backend.useCudaBuild());
        if (SimulationBuildCache.isCompileHit(codeDir, cacheKey)) {
            System.out.println("[SimulationService] semantic compile cache hit: " + codeDir.getAbsolutePath());
            progress.status("generated");
            progress.status("compiled");
            writeBuildMetadata(codeDir, host, backend, fingerprint, true, true, t0);
            return;
        }

        model.generate();
        progress.status("generated");

        if (!model.getErrorList().isEmpty()) {
            StringBuilder errorMsgs = new StringBuilder();
            for (ErrorMessage em : model.getErrorList()) {
                errorMsgs.append(em.getMessage());
            }
            throw new ModelException(errorMsgs.toString());
        }

        String sourceKey = SimulationBuildCache.sourceKey(codeDir, backend.useCudaBuild(), host);
        boolean sourceCacheHit = SimulationBuildCache.isSourceHit(codeDir, sourceKey);
        if (sourceCacheHit) {
            System.out.println("[SimulationService] native source cache hit: " + codeDir.getAbsolutePath());
        } else {
            progress.status("compiling");
            if (!model.makeExeFile()) {
                throw new ModelException("Can not make exe file!");
            }
            SimulationBuildCache.writeSourceMarker(codeDir, sourceKey);
        }

        SimulationBuildCache.writeCompileMarker(codeDir, cacheKey);
        writeBuildMetadata(codeDir, host, backend, fingerprint, false, sourceCacheHit, t0);
        progress.status("compiled");
    }

    private void writeBuildMetadata(
            File codeDir,
            String host,
            SimulationBackend backend,
            ModelFingerprint.Fingerprint fingerprint,
            boolean compileCacheHit,
            boolean sourceCacheHit,
            long startNanoTime) {
        try {
            long wallMs = (System.nanoTime() - startNanoTime) / 1_000_000L;
            SimulationBuildMetadata.write(
                    codeDir, host, backend, fingerprint, compileCacheHit, sourceCacheHit, wallMs);
        } catch (IOException e) {
            logger.warning("Unable to write simulation build metadata: " + e.getMessage());
        }
    }

    static String detectHost() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "Windows";
        }
        if (osName.contains("mac")) {
            return "Mac";
        }
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return "Linux";
        }
        return "Unknown";
    }
}
