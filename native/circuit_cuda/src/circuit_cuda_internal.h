#ifndef CIRCUIT_CUDA_INTERNAL_H
#define CIRCUIT_CUDA_INTERNAL_H

#include "circuit_cuda.h"

circuit_cuda_status_t circuit_cuda_cpu_batch_saxpy(
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements);

int circuit_cuda_impl_device_count(void);
int circuit_cuda_impl_init(void **state);
void circuit_cuda_impl_shutdown(void *state);

circuit_cuda_status_t circuit_cuda_impl_gpu_batch_saxpy(
    void *state,
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements);

#endif
