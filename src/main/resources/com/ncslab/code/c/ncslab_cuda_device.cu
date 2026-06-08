#include <cuda_runtime.h>

#include <stddef.h>

__global__ void ncslab_cuda_probe_kernel(const double* in, double* out, size_t n) {
    size_t i = (size_t)blockIdx.x * (size_t)blockDim.x + (size_t)threadIdx.x;
    if (i < n) {
        out[i] = 2.0 * in[i];
    }
}

extern "C" int ncslab_cuda_linked_device_probe(double* out_sum) {
    if (out_sum == NULL) {
        return -1;
    }

    const int n = 256;
    double host[n];
    double host_out[n];
    for (int i = 0; i < n; ++i) {
        host[i] = 1.0 + 0.001 * (double)i;
        host_out[i] = 0.0;
    }

    if (cudaSetDevice(0) != cudaSuccess) {
        return -2;
    }

    double* dev_in = NULL;
    double* dev_out = NULL;
    if (cudaMalloc((void**)&dev_in, sizeof(host)) != cudaSuccess) {
        return -3;
    }
    if (cudaMalloc((void**)&dev_out, sizeof(host_out)) != cudaSuccess) {
        cudaFree(dev_in);
        return -4;
    }
    if (cudaMemcpy(dev_in, host, sizeof(host), cudaMemcpyHostToDevice) != cudaSuccess) {
        cudaFree(dev_out);
        cudaFree(dev_in);
        return -5;
    }

    ncslab_cuda_probe_kernel<<<1, 256>>>(dev_in, dev_out, (size_t)n);
    if (cudaGetLastError() != cudaSuccess) {
        cudaFree(dev_out);
        cudaFree(dev_in);
        return -6;
    }
    if (cudaDeviceSynchronize() != cudaSuccess) {
        cudaFree(dev_out);
        cudaFree(dev_in);
        return -7;
    }
    if (cudaMemcpy(host_out, dev_out, sizeof(host_out), cudaMemcpyDeviceToHost) != cudaSuccess) {
        cudaFree(dev_out);
        cudaFree(dev_in);
        return -8;
    }

    cudaFree(dev_out);
    cudaFree(dev_in);

    double sum = 0.0;
    for (int i = 0; i < n; ++i) {
        sum += host_out[i];
    }
    *out_sum = sum;
    return 0;
}
