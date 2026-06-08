# Validation — GPU tail `dgemm` in SVD pseudoinverse (`ncslab_cuda_cublas`)

## What is accelerated

When `NCSLAB_SIM_CUDA=1`, `ncs_invert_square_svd_pinv` may compute **`C = W * U^T`** (column-major `n×n`) on the device via **dynamically loaded cuBLAS**. The **SVD and pseudoinverse scaling remain on the CPU (GSL)** so numerical differences should be limited to floating-point reordering in the final multiply.

## Environment variables

| Variable | Default | Meaning |
|----------|---------|---------|
| `NCSLAB_SIM_CUDA` | unset | Must be `1` for any GPU path (including probe and optional `dgemm`). |
| `NCSLAB_CUDA_DISABLE` | unset | If set to `1`, forces CPU `dgemm` (GSL) even when `NCSLAB_SIM_CUDA=1`. |
| `NCSLAB_CUDA_DGEMM_MIN_N` | `32`（代码内建默认；可用环境变量覆盖） | Use GPU only if `n >=` this value (avoids tiny-matrix launch overhead). |
| `NCSLAB_CUDA_GPU_AFTER_CALLS` | `0` | Skip GPU for the first *k* calls to the tail `dgemm` path (warm-up / very short runs). |
| `NCSLAB_CUDA_CUBLAS_WARMUP` | on unless `0` | After probe, runs one tail GEMM to load **cuBLAS Lt** before simulation; set `0` to disable. |
| `NCSLAB_CUDA_SVD` | unset | `1` = try GPU SVD hook (currently always falls back to GSL); see [CUSOLVER_ROADMAP.md](CUSOLVER_ROADMAP.md). |

## Golden vs GPU

1. Build `ncslab` as usual (no link-time CUDA libs required). **Regenerate** the model after upgrading templates so **`ncslab_cuda_cusolver_stub.cpp`** and updated **`makefile`** are present.
2. Run the **same model** twice to completion or for a fixed horizon:
   - **CPU gold**: unset `NCSLAB_SIM_CUDA` or set `NCSLAB_CUDA_DISABLE=1`.
   - **GPU path**: `NCSLAB_SIM_CUDA=1`, optional `NCSLAB_CUDA_DGEMM_MIN_N` tuned so the model actually hits the GPU branch (`n` large enough).
3. Compare **logged trajectories / scope results** or regression CSVs your workflow already produces. Expect **small relative drift** on states that depend on the pseudoinverse; document `max |Δ|` / relative tolerance per model class.

## Wall time

Use the same inputs and fixed stop time; compare wall clock (PowerShell `Measure-Command`, `time`, or Java-side timers). GPU wins only when the tail `dgemm` dominates **and** PCIe + launch cost is amortized.

## End-to-end wall (compile + run + Java bridge)

Split measurements so tuning targets the right layer:

| Segment | What to record | How |
|---------|----------------|-----|
| **T_compile** | `make` duration | **`[CodeStructC] make wall=...`** lines on stderr; note `USE_CUDA` and exit code for each pass. |
| **T_run** | `ncslab.exe` CPU/GPU time | Nsight **Attach** during stepping; see [PROFILE_COMPILE_AND_RUN.md](PROFILE_COMPILE_AND_RUN.md). |
| **Java wait** | `preambleRead` vs `handle` | `[NativeCodeBridgeProfiler]` when `-Dncslab.sim.profile=true`. |

Report **CPU gold vs GPU** for **results/tolerances** separately from **end-to-end** stopwatch so a faster `ncslab` is not hidden by a slow compile.

## `nvidia-smi`

During a run with `NCSLAB_SIM_CUDA=1` and large enough `n`, you should see **non-zero** utilization or memory when the tail multiply executes (in addition to the existing startup probe if present).

## Optional micro-check

For a model that triggers `caclulateInv` / switch tables with large `n`, temporarily log or breakpoint-count calls; correlate with Nsight timeline to confirm the new kernel is on the critical path.
