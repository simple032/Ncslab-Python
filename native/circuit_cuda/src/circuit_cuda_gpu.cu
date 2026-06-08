#include "circuit_cuda_internal.h"

#include <cuda_runtime.h>

#include <stdlib.h>

typedef struct {
    size_t cap;
    double *dx;
    double *dy;
} GpuState;

__global__ void circuit_cuda_saxpy_kernel(const double *x, double *y, size_t n, double scale, double bias) {
    size_t i = (size_t)blockIdx.x * (size_t)blockDim.x + (size_t)threadIdx.x;
    if (i < n) {
        y[i] = scale * x[i] + bias;
    }
}

static int ensure_capacity(GpuState *s, size_t n) {
    if (s->cap >= n) {
        return 0;
    }
    if (s->dx != NULL) {
        cudaFree(s->dx);
    }
    if (s->dy != NULL) {
        cudaFree(s->dy);
    }
    s->dx = NULL;
    s->dy = NULL;
    s->cap = 0;
    if (cudaMalloc((void **)&s->dx, n * sizeof(double)) != cudaSuccess) {
        return -1;
    }
    if (cudaMalloc((void **)&s->dy, n * sizeof(double)) != cudaSuccess) {
        cudaFree(s->dx);
        s->dx = NULL;
        return -1;
    }
    s->cap = n;
    return 0;
}

extern "C" int circuit_cuda_impl_device_count(void) {
    int n = 0;
    cudaError_t e = cudaGetDeviceCount(&n);
    if (e != cudaSuccess) {
        return 0;
    }
    return n;
}

extern "C" int circuit_cuda_impl_init(void **state) {
    GpuState *s = (GpuState *)calloc(1, sizeof(GpuState));
    if (s == NULL) {
        return -1;
    }
    cudaError_t e = cudaSetDevice(0);
    if (e != cudaSuccess) {
        free(s);
        return -1;
    }
    *state = s;
    return 0;
}

extern "C" void circuit_cuda_impl_shutdown(void *state) {
    GpuState *s = (GpuState *)state;
    if (s == NULL) {
        return;
    }
    if (s->dx != NULL) {
        cudaFree(s->dx);
    }
    if (s->dy != NULL) {
        cudaFree(s->dy);
    }
    free(s);
}

extern "C" circuit_cuda_status_t circuit_cuda_impl_gpu_batch_saxpy(
    void *state,
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements) {
    GpuState *s = (GpuState *)state;
    size_t n;
    unsigned int threads;
    unsigned int blocks;

    if (s == NULL || batch_count < 0 || dim < 0) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    if (x_stride_elements != (size_t)dim || y_stride_elements != (size_t)dim) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    n = (size_t)batch_count * (size_t)dim;
    if (n == 0U) {
        return CIRCUIT_CUDA_OK;
    }
    if (x_host == NULL || y_host == NULL) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    if (ensure_capacity(s, n) != 0) {
        return CIRCUIT_CUDA_RUNTIME_ERROR;
    }
    if (cudaMemcpy(s->dx, x_host, n * sizeof(double), cudaMemcpyHostToDevice) != cudaSuccess) {
        return CIRCUIT_CUDA_RUNTIME_ERROR;
    }
    threads = 256U;
    blocks = (unsigned int)((n + (size_t)threads - 1U) / (size_t)threads);
    circuit_cuda_saxpy_kernel<<<blocks, threads>>>(s->dx, s->dy, n, scale, bias);
    if (cudaGetLastError() != cudaSuccess) {
        return CIRCUIT_CUDA_RUNTIME_ERROR;
    }
    if (cudaDeviceSynchronize() != cudaSuccess) {
        return CIRCUIT_CUDA_RUNTIME_ERROR;
    }
    if (cudaMemcpy(y_host, s->dy, n * sizeof(double), cudaMemcpyDeviceToHost) != cudaSuccess) {
        return CIRCUIT_CUDA_RUNTIME_ERROR;
    }
    return CIRCUIT_CUDA_OK;
}
