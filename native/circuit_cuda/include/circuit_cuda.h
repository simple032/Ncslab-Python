#ifndef CIRCUIT_CUDA_H
#define CIRCUIT_CUDA_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct circuit_cuda_context circuit_cuda_context_t;

typedef enum {
    CIRCUIT_CUDA_OK = 0,
    CIRCUIT_CUDA_NO_DEVICE = 1,
    CIRCUIT_CUDA_INVALID_ARGUMENT = 2,
    CIRCUIT_CUDA_RUNTIME_ERROR = 3
} circuit_cuda_status_t;

/**
 * Returns 1 if a CUDA device is visible to the runtime (may still be in use by another process).
 * Always safe to call; does not require a context.
 */
int circuit_cuda_device_available(void);

/**
 * Create execution context. If use_gpu_if_available is non-zero and a CUDA device exists,
 * the implementation may select the GPU backend; otherwise the CPU backend is used.
 */
circuit_cuda_context_t *circuit_cuda_create(int use_gpu_if_available);

void circuit_cuda_destroy(circuit_cuda_context_t *ctx);

/** Human-readable backend: "cpu" or "cuda". */
const char *circuit_cuda_backend_name(const circuit_cuda_context_t *ctx);

/**
 * POC batched element-wise map: for each batch index b in [0, batch_count),
 *   y[b * y_stride_elements + j] = scale * x[b * x_stride_elements + j] + bias,  j in [0, dim).
 * Strides must be >= dim. GPU fast path may require strides == dim (contiguous batches).
 */
circuit_cuda_status_t circuit_cuda_batch_saxpy(
    circuit_cuda_context_t *ctx,
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements);

#ifdef __cplusplus
}
#endif

#endif /* CIRCUIT_CUDA_H */
