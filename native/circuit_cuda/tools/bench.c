#include "circuit_cuda.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _WIN32
#include <windows.h>
#else
#include <time.h>
#endif

static double wall_seconds(void) {
#ifdef _WIN32
    LARGE_INTEGER f;
    LARGE_INTEGER c;
    QueryPerformanceFrequency(&f);
    QueryPerformanceCounter(&c);
    return (double)c.QuadPart / (double)f.QuadPart;
#else
    struct timespec ts;
    if (clock_gettime(CLOCK_MONOTONIC, &ts) != 0) {
        return 0.0;
    }
    return (double)ts.tv_sec + 1e-9 * (double)ts.tv_nsec;
#endif
}

static int verify_sample(
    int batch_count,
    int dim,
    const double *x,
    size_t x_stride,
    const double *y,
    size_t y_stride,
    double scale,
    double bias) {
    int b;
    int j;
    for (b = 0; b < batch_count; b++) {
        for (j = 0; j < dim; j++) {
            double xv = x[(size_t)b * x_stride + (size_t)j];
            double ev = scale * xv + bias;
            double yv = y[(size_t)b * y_stride + (size_t)j];
            if (fabs(yv - ev) > 1e-9 * (1.0 + fabs(ev))) {
                return 0;
            }
        }
    }
    return 1;
}

static double run_case(
    circuit_cuda_context_t *ctx,
    int batch_count,
    int dim,
    double *x,
    double *y,
    int iters,
    double scale,
    double bias) {
    int k;
    double t0 = wall_seconds();
    size_t stride = (size_t)dim;
    for (k = 0; k < iters; k++) {
        circuit_cuda_status_t st = circuit_cuda_batch_saxpy(
            ctx,
            batch_count,
            dim,
            x,
            stride,
            scale,
            bias,
            y,
            stride);
        if (st != CIRCUIT_CUDA_OK) {
            fprintf(stderr, "batch_saxpy failed: %d\n", (int)st);
            exit(1);
        }
    }
    return wall_seconds() - t0;
}

int main(int argc, char **argv) {
    int batch_count = 512;
    int dim = 2048;
    int iters = 30;
    int i;

    for (i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--batch") == 0 && i + 1 < argc) {
            batch_count = atoi(argv[++i]);
        } else if (strcmp(argv[i], "--dim") == 0 && i + 1 < argc) {
            dim = atoi(argv[++i]);
        } else if (strcmp(argv[i], "--iters") == 0 && i + 1 < argc) {
            iters = atoi(argv[++i]);
        }
    }

    size_t n = (size_t)batch_count * (size_t)dim;
    double *x = (double *)malloc(n * sizeof(double));
    double *y = (double *)malloc(n * sizeof(double));
    if (x == NULL || y == NULL) {
        fprintf(stderr, "allocation failed\n");
        return 1;
    }
    size_t idx;
    for (idx = 0; idx < n; idx++) {
        x[idx] = sin(0.01 * (double)idx);
    }

    printf("circuit_cuda_bench batch=%d dim=%d elements=%zu iters=%d\n", batch_count, dim, n, iters);
    printf("CUDA devices visible: %d\n", circuit_cuda_device_available());

    {
        circuit_cuda_context_t *ctx = circuit_cuda_create(0);
        double sec = run_case(ctx, batch_count, dim, x, y, iters, 1.25, -0.5);
        printf("backend=%s total=%.6f s per_iter=%.6f ms  throughput=%.3f GE/s (effective)\n",
            circuit_cuda_backend_name(ctx),
            sec,
            1000.0 * sec / (double)iters,
            (double)iters * (double)n * 2.0 / sec / 1e9);
        if (!verify_sample(batch_count, dim, x, (size_t)dim, y, (size_t)dim, 1.25, -0.5)) {
            fprintf(stderr, "CPU path verification failed\n");
            return 1;
        }
        circuit_cuda_destroy(ctx);
    }

    {
        circuit_cuda_context_t *ctx = circuit_cuda_create(1);
        double sec = run_case(ctx, batch_count, dim, x, y, iters, 1.25, -0.5);
        printf("backend=%s total=%.6f s per_iter=%.6f ms  throughput=%.3f GE/s (effective)\n",
            circuit_cuda_backend_name(ctx),
            sec,
            1000.0 * sec / (double)iters,
            (double)iters * (double)n * 2.0 / sec / 1e9);
        if (!verify_sample(batch_count, dim, x, (size_t)dim, y, (size_t)dim, 1.25, -0.5)) {
            fprintf(stderr, "GPU/selected path verification failed\n");
            return 1;
        }
        circuit_cuda_destroy(ctx);
    }

    free(x);
    free(y);
    return 0;
}
