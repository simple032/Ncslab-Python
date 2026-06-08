# CUDA simulation deployment and QA

## Runtime requirements

- **NVIDIA GPU** with a driver version compatible with the installed CUDA runtime DLLs / `libcudart` used by the probe (`cudart64_12.dll`, `libcudart.so.12`, etc.).
- For the **tail `dgemm` path**: matching **cuBLAS** shared libraries must be discoverable (`cublas64_12.dll` / `libcublas.so.12`, with fallbacks for 11.x / 10.x as listed in `ncslab_cuda_cublas.cpp`). Same major version family as the driver stack is recommended.
- **CUDA-capable machine** for the host that runs `ncslab.exe` (same machine as Tomcat in typical setups).

## Environment variables

| Variable | Set by | Meaning |
|----------|--------|---------|
| `NCSLAB_SIM_CUDA=1` | Java `ProcessBuilder` when `SimulationBackendContext.preferCuda()` is true | Enables `ncslab_cuda_runtime_probe()` and optional **runtime-loaded cuBLAS** tail `dgemm` in `util.cpp` (see [VALIDATION_DGEMM.md](VALIDATION_DGEMM.md)). |
| `NCSLAB_CUDA_DISABLE` | Manual QA | If `1`, keeps probe behavior but forces **CPU** for the optional GPU `dgemm`. |
| `NCSLAB_CUDA_DGEMM_MIN_N` | unset（默认 **`32`** in code） | Minimum matrix order `n` before attempting GPU multiply. Set higher (e.g. `64`) to keep mid-size partitions on CPU; was `96` before 2026-05 repo tuning. |
| `NCSLAB_CUDA_GPU_AFTER_CALLS` | unset (default `0`) | Skip GPU for the first *k* pseudoinverse tail multiplies (short-run / warm-up control). |
| `NCSLAB_CUDA_CUBLAS_WARMUP` | unset（默认 **on**：未设为 `0` 则在一次 **`ncslab_cuda_cublas_warmup()`** 中触发尾乘路径） | Set `0` to skip startup **cuBLAS** warm-up (saves a few ms startup; first real pseudoinverse may pay **`cuLibraryLoadData` / Lt heuristic**). See [PROFILE_COMPILE_AND_RUN.md](PROFILE_COMPILE_AND_RUN.md). |
| `NCSLAB_CUDA_SVD` | Java `ProcessBuilder` default **`1`** for CUDA backend | Set `1` to try **GPU full SVD** hook first (`ncslab_cuda_svd_pinv_colmajor`) for matrices at or above `NCSLAB_CUDA_SVD_MIN_N`; failed CUDA calls fall back to the CPU path with a one-time stderr reason. |
| `USE_CUDA` | `make` environment from `CodeStructC.makeExeFile()` when client requested CUDA build | On Windows, `1` compiles **`ncslab_cuda_device.cu`** with `nvcc` and links `cudart.lib`; `0` compiles a C++ stub. The cuBLAS/cuSOLVER pseudoinverse path still uses runtime-loaded CUDA libraries so it can report detailed fallback reasons. |
| `NCSLAB_CUDA_SOLVE_DGEMV_MIN_N` | Java `ProcessBuilder` default **`128`** for CUDA backend | For dense inverse fallback solves, uploads each cached inverse matrix once and uses cuBLAS `dgemv` for repeated `solveStoreGAA` calls when `n` is large enough. |
| `NCSLAB_CUDA_SOLVE_DGEMV_FORCE` | Java default **`0`** | Set `1` only for profiling to try GPU cached `dgemv` before KLU/LU solves. |

## Compile-time logging

Each `make` invocation from `CodeStructC.runMakeOnce` prints **`[CodeStructC] make wall=... ms USE_CUDA=... exit=...`** on **stderr** (Locale US). Use it to split **T_compile** from **T_run** and to spot **`USE_CUDA=1` failure + `USE_CUDA=0` retry** doubling build time.

- **Stderr** from `ncslab` is redirected to `ncslab_stderr.log` in the model directory (Windows simulation path; Linux PC simulation aligned).
- With `NCSLAB_SIM_CUDA=1` and a working driver, expect a line such as:  
  `[ncslab_cuda] probe OK (sum=...); GPU path exercised when NCSLAB_SIM_CUDA=1`  
  When **cuBLAS warm-up** runs (default), also: **`[ncslab_cuda] cublas warm-up OK (n=...)`** before the simulation loop; the first **real** pseudoinverse tail on GPU may still log **`tail dgemm on GPU`** once.
- With `USE_CUDA=1`, a linked kernel probe should log **`[ncslab_cuda] linked CUDA kernel OK ... USE_CUDA=1 nvcc path active`**.
- For full pseudoinverse offload, expect **`[ncslab_cuda] cuSOLVER SVD pseudoinverse on GPU ...`**; buffers and handles are reused across calls for the same matrix order to avoid repeated `cudaMalloc/cudaFree`.
- For repeated dense solve fallback, expect **`[ncslab_cuda] cached inverse matrix on GPU for repeated dgemv solves ...`** once per process when the first large cached inverse is uploaded.
- If cudart cannot load:  
  `[ncslab_cuda] NCSLAB_SIM_CUDA=1 but could not load cudart ...`
- **`nvidia-smi`**: during startup, GPU memory may spike when the probe runs. When large switch-table inversions run with `NCSLAB_SIM_CUDA=1`, expect additional short bursts from the **cuBLAS tail `dgemm`**.

## CPU fallback

- If **`make`** fails with `USE_CUDA=1`, the build is **automatically retried** with `USE_CUDA=0` and `cudaSimulationRequested` is cleared on the model so the user still gets a CPU binary.
- If **`NCSLAB_SIM_CUDA`** is unset or not `1`, the probe is a no-op (no GPU calls) and the tail `dgemm` path is skipped (GSL only).
- The **tail `dgemm`** falls back to **GSL** if cuBLAS/cudart cannot load, if `n` is below **`NCSLAB_CUDA_DGEMM_MIN_N`**, during the first **`NCSLAB_CUDA_GPU_AFTER_CALLS`** invocations, or if **`NCSLAB_CUDA_DISABLE=1`**.

## Numerical regression (when real kernels exist)

- Compare scope/results against a **CPU gold** run for the same model, step, and tolerances.
- Document acceptable **absolute/relative** error for floating-point order differences on GPU.
- Track **wall time** and **GPU memory** for regression dashboards.

## Toolchain note (Windows)

Current generated Windows `ncslab.exe` uses **MSVC `cl.exe`** for C++ objects. When `USE_CUDA=1`, the makefile additionally compiles a linked CUDA device probe with **`nvcc`** and links **`cudart.lib`**; the cuBLAS/cuSOLVER numerical hooks remain runtime-loaded so they can fall back independently.

Mixing **nvcc** with MinGW for future device code still requires a consistent host compiler (`-ccbin`); for production `.cu` objects, document a **single supported toolchain** (e.g. MSVC + nvcc or Linux build agents). See [HOTSPOT_TABLE.md](HOTSPOT_TABLE.md) for profiling-driven follow-ons.
