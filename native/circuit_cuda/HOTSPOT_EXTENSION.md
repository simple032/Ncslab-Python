# Where to extend for real circuit / model GPU speedup (after probe)

The `ncslab_cuda_runtime_probe` in generated `ncslab` only proves the driver and runtime are callable.

**Shipped first kernel (Phase 1):** when `NCSLAB_SIM_CUDA=1` and `n` is above a threshold, the final **`dgemm`** in `ncs_invert_square_svd_pinv` (`util.cpp`) can run on **cuBLAS** via runtime-loaded `ncslab_cuda_cublas.cpp` (CPU GSL SVD + scaling unchanged). Default threshold **`NCSLAB_CUDA_DGEMM_MIN_N=32`** (since 2026-05) targets typical **`Msize=32`** partitions; stderr logs **`[ncslab_cuda] tail dgemm on GPU (n=…)`** the first time the GPU path succeeds. See [HOTSPOT_TABLE.md](HOTSPOT_TABLE.md) and [VALIDATION_DGEMM.md](VALIDATION_DGEMM.md).

For **meaningful** further acceleration, migrate additional **dominant cost** from profiling into GPU kernels or libraries (cuSOLVER SVD, cuSPARSE / custom). **Roadmap:** [CUSOLVER_ROADMAP.md](CUSOLVER_ROADMAP.md). **Profiling checklist (T_compile + T_run):** [PROFILE_COMPILE_AND_RUN.md](PROFILE_COMPILE_AND_RUN.md).

## Suggested profiling order

1. **Java path** (models that never launch `ncslab`): enable `-Dncslab.sim.profile=true` and inspect `[SimulationStepProfiler]` (integrate vs `circuitModel` vs outputs). See [PROFILING.txt](PROFILING.txt).
2. **`ncslab.exe`**: Windows — Very Sleepy / Visual Studio on `ncslab`; Linux — `perf record`. NVIDIA — Nsight Systems on the `ncslab` process.
3. **Bridge**: `[NativeCodeBridgeProfiler]` if time is in TCP/WebSocket handling.

## Likely extension points in this repo (by symptom)

| Symptom | First files to inspect |
|-----------|-------------------------|
| Time in fixed-step **integrate** | [SimulationModel.java](../../src/main/java/com/ncslab/ncslablink/SimulationModel.java), Apache Commons Math integrator callbacks |
| Time in **CircuitModel** / loops | [CircuitModel.java](../../src/main/java/com/ncslab/circuit/CircuitModel.java), [LoopSolver](../../src/main/java/com/ncslab/circuit/loop/) |
| Time in **generated C** step | Resource `onestep*.cpp` templates under [src/main/resources/com/ncslab/code/c](../../src/main/resources/com/ncslab/code/c), `mainccode.cpp`, [util.cpp](../../src/main/resources/com/ncslab/code/c/util.cpp) (GSL linear algebra) |
| **Monte Carlo / many runs** | Parallelize outer runs; each run can stay CPU until inner dimension is large enough for GPU |

## Native library already in tree

[circuit_cuda](.) — C API and optional `circuit_cuda_gpu.cu`; link from `ncslab` or JNI when a hotspot is identified.

## JNI alternative

If the hotspot stays in the JVM (ODE + circuit), load a JNI wrapper around `circuit_cuda` in the same process as the integrator so `NCSLAB_SIM_CUDA` is not required for that path.
