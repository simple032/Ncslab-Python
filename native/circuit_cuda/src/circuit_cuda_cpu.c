#include "circuit_cuda_internal.h"

#include <stddef.h>

circuit_cuda_status_t circuit_cuda_cpu_batch_saxpy(
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements) {
    int b;
    int j;
    if (batch_count < 0 || dim < 0) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    if (x_stride_elements < (size_t)dim || y_stride_elements < (size_t)dim) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    if (x_host == NULL || y_host == NULL) {
        return CIRCUIT_CUDA_INVALID_ARGUMENT;
    }
    for (b = 0; b < batch_count; b++) {
        const double *xs = x_host + (size_t)b * x_stride_elements;
        double *ys = y_host + (size_t)b * y_stride_elements;
        for (j = 0; j < dim; j++) {
            ys[j] = scale * xs[j] + bias;
        }
    }
    return CIRCUIT_CUDA_OK;
}
