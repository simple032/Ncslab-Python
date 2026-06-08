# cuSOLVER GPU pseudoinverse roadmap (Phase B)

Today: **GSL** `gsl_linalg_SV_decomp` + column scaling, then **`W*U^T`** via **cuBLAS** tail `dgemm` when enabled.

Goal: optional **full-GPU** path for `ncs_invert_square_svd_pinv` (same Moore–Penrose semantics and tolerance as GSL), to remove CPU SVD when profiling shows **`gsl_linalg_SV_decomp`** dominates **T_run**.

## Hook in the repo

- **`NCSLAB_CUDA_SVD=1`** — attempt GPU SVD path first; on failure (or unset), use existing GSL implementation.
- **`ncslab_cuda_svd_pinv_colmajor`** in `ncslab_cuda_cusolver_stub.cpp` — currently always returns **nonzero** (fallback). Replace implementation with dynamic **libcusolver** + **libcublas** (or reuse existing cuBLAS handle module).
- **`util.cpp`** — `ncs_invert_square_svd_pinv` calls the hook **before** allocating GSL workspaces when `NCSLAB_CUDA_SVD=1`.

## Implementation sketch

1. **Dynamic load** `cusolver64_*.dll` / `libcusolver.so.*` (same pattern as `ncslab_cuda_cublas.cpp`).
2. **Device buffers** for `A`, `U`, `V`, `S`, work; **`gesvd`** or **`gesvdj`** for `n` up to model max (e.g. 32 / 128).
3. **Apply `Sigma^+`** on device or CPU (small `n`) to form **`W`**, then **`C = W U^T`** on GPU (existing tail path or fused).
4. **Tolerance** — mirror GSL `smax * 1e-14 * nscale` pinv cutoff; expose **`NCSLAB_CUDA_SVD_RTOL`** if needed.
5. **Fallback** — any CUDA error, wrong dims, or `NCSLAB_CUDA_DISABLE=1` ⇒ GSL.

## Validation

- Golden **CPU** run vs **`NCSLAB_CUDA_SVD=1`** on identical model: **`inv` / scope** within documented tolerance.
- **T_run** and **`nsys`** CPU Top before/after to confirm SVD time moved to GPU.

## Build

- Generated `makefile` must compile **`ncslab_cuda_cusolver_stub.o`** (or renamed `.cpp` once implemented). **Regenerate** model C files after pulling templates.
