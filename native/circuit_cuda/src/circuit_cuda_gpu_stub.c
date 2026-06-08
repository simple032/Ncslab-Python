#include "circuit_cuda_internal.h"

int circuit_cuda_impl_device_count(void) {
    return 0;
}

int circuit_cuda_impl_init(void **state) {
    (void)state;
    return -1;
}

void circuit_cuda_impl_shutdown(void *state) {
    (void)state;
}

circuit_cuda_status_t circuit_cuda_impl_gpu_batch_saxpy(
    void *state,
    int batch_count,
    int dim,
    const double *x_host,
    size_t x_stride_elements,
    double scale,
    double bias,
    double *y_host,
    size_t y_stride_elements) {
    (void)state;
    (void)batch_count;
    (void)dim;
    (void)x_host;
    (void)x_stride_elements;
    (void)scale;
    (void)bias;
    (void)y_host;
    (void)y_stride_elements;
    return CIRCUIT_CUDA_NO_DEVICE;
}
