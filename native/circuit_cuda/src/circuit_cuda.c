#include "circuit_cuda.h"
#include "circuit_cuda_internal.h"

#include <stdlib.h>

struct circuit_cuda_context {
    int backend_cuda;
    void *gpu_state;
};

int circuit_cuda_device_available(void) {
    return circuit_cuda_impl_device_count() > 0 ? 1 : 0;
}

circuit_cuda_context_t *circuit_cuda_create(int use_gpu_if_available) {
    circuit_cuda_context_t *ctx = (circuit_cuda_context_t *)calloc(1, sizeof(circuit_cuda_context_t));
    if (ctx == NULL) {
        return NULL;
    }
    ctx->backend_cuda = 0;
    ctx->gpu_state = NULL;
    if (use_gpu_if_available && circuit_cuda_impl_device_count() > 0) {
        void *st = NULL;
        if (circuit_cuda_impl_init(&st) == 0) {
            ctx->backend_cuda = 1;
            ctx->gpu_state = st;
        }
    }
    return ctx;
}

void circuit_cuda_destroy(circuit_cuda_context_t *ctx) {
    if (ctx == NULL) {
        return;
    }
    if (ctx->gpu_state != NULL) {
        circuit_cuda_impl_shutdown(ctx->gpu_state);
        ctx->gpu_state = NULL;
    }
    free(ctx);
}

const char *circuit_cuda_backend_name(const circuit_cuda_context_t *ctx) {
    if (ctx == NULL) {
        return "null";
    }
    return ctx->backend_cuda ? "cuda" : "cpu";
}

circuit_cuda_status_t circuit_cuda_batch_saxpy(
    circuit_cuda_context_t *ctx,
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements) {
    if (ctx == NULL) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    if (ctx->backend_cuda && x_stride_elements == (size_t)dim && y_stride_elements == (size_t)dim) {
        circuit_cuda_status_t st = circuit_cuda_impl_gpu_batch_saxpy(
            ctx->gpu_state,
            batch_count,
            dim,
            x_host,
            x_stride_elements,
            scale,
            bias,
            y_host,
            y_stride_elements);
        if (st == CIRCUIT_CUDA_OK) {
            return CIRCUIT_CUDA_OK;
        }
        /* Fall back to CPU if GPU path declines (e.g. runtime error). */
    }
    return circuit_cuda_cpu_batch_saxpy(
        batch_count,
        dim,
        x_host,
        x_stride_elements,
        scale,
        bias,
        y_host,
        y_stride_elements);
}
