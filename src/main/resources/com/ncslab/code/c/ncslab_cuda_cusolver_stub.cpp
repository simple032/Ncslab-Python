/**
 * Optional full-GPU pseudoinverse path (cuSOLVER SVD + cuBLAS multiply).
 *
 * This file loads cudart, cuBLAS, and cuSOLVER dynamically so ncslab.exe does
 * not need link-time CUDA libraries. Enable with:
 *   NCSLAB_SIM_CUDA=1
 *   NCSLAB_CUDA_SVD=1
 *
 * Nonzero return means the caller should fall back to the CPU GSL path.
 */

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <vector>

#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#else
#include <dlfcn.h>
#endif

typedef int cudaError_t;
typedef int cublasStatus_t;
typedef int cusolverStatus_t;

#ifndef cudaSuccess
#define cudaSuccess 0
#endif
#ifndef cudaMemcpyHostToDevice
#define cudaMemcpyHostToDevice 1
#endif
#ifndef cudaMemcpyDeviceToHost
#define cudaMemcpyDeviceToHost 2
#endif

#ifndef CUBLAS_STATUS_SUCCESS
#define CUBLAS_STATUS_SUCCESS 0
#endif
#ifndef CUSOLVER_STATUS_SUCCESS
#define CUSOLVER_STATUS_SUCCESS 0
#endif
#ifndef CUBLAS_OP_T
#define CUBLAS_OP_T 1
#endif
#ifndef CUBLAS_SIDE_LEFT
#define CUBLAS_SIDE_LEFT 0
#endif

typedef cudaError_t (*p_cudaSetDevice)(int);
typedef cudaError_t (*p_cudaMalloc)(void**, size_t);
typedef cudaError_t (*p_cudaFree)(void*);
typedef cudaError_t (*p_cudaMemcpy)(void*, const void*, size_t, int);
typedef cudaError_t (*p_cudaDeviceSynchronize)(void);

typedef cublasStatus_t (*p_cublasCreate)(void**);
typedef cublasStatus_t (*p_cublasDestroy)(void*);
typedef cublasStatus_t (*p_cublasDgemm)(void*, int, int, int, int, int, const double*,
    const double*, int, const double*, int, const double*, double*, int);
typedef cublasStatus_t (*p_cublasDdgmm)(void*, int, int, int, const double*, int,
    const double*, int, double*, int);

typedef cusolverStatus_t (*p_cusolverDnCreate)(void**);
typedef cusolverStatus_t (*p_cusolverDnDestroy)(void*);
typedef cusolverStatus_t (*p_cusolverDnDgesvd_bufferSize)(void*, int, int, int*);
typedef cusolverStatus_t (*p_cusolverDnDgesvd)(void*, signed char, signed char, int, int,
    double*, int, double*, double*, int, double*, int, double*, int, double*, int*);

#ifdef _WIN32
static void* load_lib(const char* const* names, size_t nnames) {
    for (size_t i = 0; i < nnames; ++i) {
        HMODULE h = LoadLibraryA(names[i]);
        if (h != NULL) {
            return (void*)h;
        }
        const char* roots[] = {
            getenv("NCSLAB_CUDA_PATH"),
            getenv("CUDA_PATH"),
            getenv("CUDA_PATH_V13_2"),
            getenv("CUDA_PATH_V13_1"),
            getenv("CUDA_PATH_V13_0"),
            getenv("CUDA_PATH_V12_0"),
            "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v13.2",
            "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v13.1",
            "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v13.0",
            "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v12.0"
        };
        for (size_t r = 0; r < sizeof(roots) / sizeof(roots[0]); ++r) {
            if (roots[r] == NULL || roots[r][0] == '\0') {
                continue;
            }
            char path[MAX_PATH * 2];
            snprintf(path, sizeof(path), "%s\\bin\\x64\\%s", roots[r], names[i]);
            h = LoadLibraryA(path);
            if (h != NULL) {
                return (void*)h;
            }
            snprintf(path, sizeof(path), "%s\\bin\\%s", roots[r], names[i]);
            h = LoadLibraryA(path);
            if (h != NULL) {
                return (void*)h;
            }
        }
    }
    return NULL;
}
static void* sym(void* lib, const char* name) {
    return (void*)GetProcAddress((HMODULE)lib, name);
}
#else
static void* load_lib(const char* const* names, size_t nnames) {
    for (size_t i = 0; i < nnames; ++i) {
        void* h = dlopen(names[i], RTLD_LAZY);
        if (h != NULL) {
            return h;
        }
    }
    return NULL;
}
static void* sym(void* lib, const char* name) {
    return dlsym(lib, name);
}
#endif

static int env_enabled(const char* name) {
    const char* f = std::getenv(name);
    return (f != NULL && f[0] == '1');
}

static int parse_env_int(const char* name, int def_val) {
    const char* s = std::getenv(name);
    if (s == NULL || s[0] == '\0') {
        return def_val;
    }
    return (int)std::strtol(s, NULL, 10);
}

struct GpuSvdState {
    void* lib_cudart;
    void* lib_cublas;
    void* lib_cusolver;
    p_cudaSetDevice fn_set;
    p_cudaMalloc fn_malloc;
    p_cudaFree fn_free;
    p_cudaMemcpy fn_memcpy;
    p_cudaDeviceSynchronize fn_sync;
    p_cublasCreate fn_cublas_create;
    p_cublasDestroy fn_cublas_destroy;
    p_cublasDgemm fn_dgemm;
    p_cublasDdgmm fn_ddgmm;
    p_cusolverDnCreate fn_solver_create;
    p_cusolverDnDestroy fn_solver_destroy;
    p_cusolverDnDgesvd_bufferSize fn_gesvd_buffer;
    p_cusolverDnDgesvd fn_gesvd;
    void* cublas;
    void* solver;
    void* d_a;
    void* d_s;
    void* d_u;
    void* d_vt;
    void* d_inv_s;
    void* d_scaled_vt;
    void* d_out;
    void* d_work;
    void* d_info;
    int cap_n;
    int cap_lwork;
    int init_failed;
    std::vector<double> h_s;
    std::vector<double> h_inv_s;
};

static GpuSvdState g_svd;

static int ensure_libs(GpuSvdState* g) {
    if (g->init_failed) {
        return -1;
    }
    if (g->cublas != NULL && g->solver != NULL) {
        return 0;
    }

#ifdef _WIN32
    static const char* crt_names[] = {"cudart64_13.dll", "cudart64_12.dll", "cudart64_110.dll", "cudart64_11.dll", "cudart64_10.dll"};
    static const char* blas_names[] = {"cublas64_13.dll", "cublas64_12.dll", "cublas64_110.dll", "cublas64_11.dll", "cublas64_10.dll"};
    static const char* solver_names[] = {"cusolver64_13.dll", "cusolver64_12.dll", "cusolver64_11.dll", "cusolver64_10.dll"};
#else
    static const char* crt_names[] = {"libcudart.so.12", "libcudart.so.11.0", "libcudart.so.10.2", "libcudart.so"};
    static const char* blas_names[] = {"libcublas.so.12", "libcublas.so.11", "libcublas.so.10", "libcublas.so"};
    static const char* solver_names[] = {"libcusolver.so.12", "libcusolver.so.11", "libcusolver.so.10", "libcusolver.so"};
#endif

    g->lib_cudart = load_lib(crt_names, sizeof(crt_names) / sizeof(crt_names[0]));
    g->lib_cublas = load_lib(blas_names, sizeof(blas_names) / sizeof(blas_names[0]));
    g->lib_cusolver = load_lib(solver_names, sizeof(solver_names) / sizeof(solver_names[0]));
    if (!g->lib_cudart || !g->lib_cublas || !g->lib_cusolver) {
        g->init_failed = 1;
        return -1;
    }

    g->fn_set = (p_cudaSetDevice)sym(g->lib_cudart, "cudaSetDevice");
    g->fn_malloc = (p_cudaMalloc)sym(g->lib_cudart, "cudaMalloc");
    g->fn_free = (p_cudaFree)sym(g->lib_cudart, "cudaFree");
    g->fn_memcpy = (p_cudaMemcpy)sym(g->lib_cudart, "cudaMemcpy");
    g->fn_sync = (p_cudaDeviceSynchronize)sym(g->lib_cudart, "cudaDeviceSynchronize");

    g->fn_cublas_create = (p_cublasCreate)sym(g->lib_cublas, "cublasCreate_v2");
    g->fn_cublas_destroy = (p_cublasDestroy)sym(g->lib_cublas, "cublasDestroy_v2");
    g->fn_dgemm = (p_cublasDgemm)sym(g->lib_cublas, "cublasDgemm_v2");
    g->fn_ddgmm = (p_cublasDdgmm)sym(g->lib_cublas, "cublasDdgmm_v2");
    if (!g->fn_ddgmm) {
        g->fn_ddgmm = (p_cublasDdgmm)sym(g->lib_cublas, "cublasDdgmm");
    }

    g->fn_solver_create = (p_cusolverDnCreate)sym(g->lib_cusolver, "cusolverDnCreate");
    g->fn_solver_destroy = (p_cusolverDnDestroy)sym(g->lib_cusolver, "cusolverDnDestroy");
    g->fn_gesvd_buffer = (p_cusolverDnDgesvd_bufferSize)sym(g->lib_cusolver, "cusolverDnDgesvd_bufferSize");
    g->fn_gesvd = (p_cusolverDnDgesvd)sym(g->lib_cusolver, "cusolverDnDgesvd");

    if (!g->fn_set || !g->fn_malloc || !g->fn_free || !g->fn_memcpy || !g->fn_sync ||
        !g->fn_cublas_create || !g->fn_cublas_destroy || !g->fn_dgemm || !g->fn_ddgmm ||
        !g->fn_solver_create || !g->fn_solver_destroy || !g->fn_gesvd_buffer || !g->fn_gesvd) {
        g->init_failed = 1;
        return -1;
    }

    if (g->fn_set(0) != cudaSuccess) {
        g->init_failed = 1;
        return -1;
    }
    if (g->fn_cublas_create(&g->cublas) != CUBLAS_STATUS_SUCCESS || g->cublas == NULL) {
        g->init_failed = 1;
        return -1;
    }
    if (g->fn_solver_create(&g->solver) != CUSOLVER_STATUS_SUCCESS || g->solver == NULL) {
        g->fn_cublas_destroy(g->cublas);
        g->cublas = NULL;
        g->init_failed = 1;
        return -1;
    }
    return 0;
}

static void free_if(void* p, GpuSvdState* g) {
    if (p != NULL && g->fn_free != NULL) {
        g->fn_free(p);
    }
}

static void free_svd_buffers(GpuSvdState* g) {
    if (g == NULL || g->fn_free == NULL) {
        return;
    }
    free_if(g->d_work, g);
    free_if(g->d_info, g);
    free_if(g->d_out, g);
    free_if(g->d_scaled_vt, g);
    free_if(g->d_inv_s, g);
    free_if(g->d_vt, g);
    free_if(g->d_u, g);
    free_if(g->d_s, g);
    free_if(g->d_a, g);
    g->d_a = g->d_s = g->d_u = g->d_vt = NULL;
    g->d_inv_s = g->d_scaled_vt = g->d_out = NULL;
    g->d_work = g->d_info = NULL;
    g->cap_n = 0;
    g->cap_lwork = 0;
}

static int ensure_svd_buffers(GpuSvdState* g, int n, int lwork) {
    if (g == NULL || n <= 0 || lwork <= 0 || g->fn_malloc == NULL) {
        return -1;
    }
    if (g->cap_n == n && g->cap_lwork >= lwork && g->d_a && g->d_s && g->d_u && g->d_vt &&
        g->d_inv_s && g->d_scaled_vt && g->d_out && g->d_work && g->d_info) {
        return 0;
    }

    free_svd_buffers(g);

    const size_t matrix_bytes = (size_t)n * (size_t)n * sizeof(double);
    if (g->fn_malloc(&g->d_a, matrix_bytes) != cudaSuccess ||
        g->fn_malloc(&g->d_s, (size_t)n * sizeof(double)) != cudaSuccess ||
        g->fn_malloc(&g->d_u, matrix_bytes) != cudaSuccess ||
        g->fn_malloc(&g->d_vt, matrix_bytes) != cudaSuccess ||
        g->fn_malloc(&g->d_inv_s, (size_t)n * sizeof(double)) != cudaSuccess ||
        g->fn_malloc(&g->d_scaled_vt, matrix_bytes) != cudaSuccess ||
        g->fn_malloc(&g->d_out, matrix_bytes) != cudaSuccess ||
        g->fn_malloc(&g->d_work, (size_t)lwork * sizeof(double)) != cudaSuccess ||
        g->fn_malloc(&g->d_info, sizeof(int)) != cudaSuccess) {
        free_svd_buffers(g);
        return -1;
    }
    g->cap_n = n;
    g->cap_lwork = lwork;
    g->h_s.resize((size_t)n);
    g->h_inv_s.resize((size_t)n);
    return 0;
}

static void log_svd_fallback_once(const char* reason, int code) {
    static int s_logged;
    if (!s_logged) {
        fprintf(stderr, "[ncslab_cuda] cuSOLVER SVD fallback: %s (%d)\n", reason, code);
        s_logged = 1;
    }
}

extern "C" int ncslab_cuda_svd_pinv_colmajor(int n, const double* A_col_major, double* inv_col_major) {
    if (n <= 0 || A_col_major == NULL || inv_col_major == NULL) {
        return -1;
    }
    if (!env_enabled("NCSLAB_SIM_CUDA") || !env_enabled("NCSLAB_CUDA_SVD")) {
        return 1;
    }
    const char* dis = std::getenv("NCSLAB_CUDA_DISABLE");
    if (dis != NULL && dis[0] == '1') {
        return 1;
    }
    const int min_n = parse_env_int("NCSLAB_CUDA_SVD_MIN_N", 32);
    if (n < min_n) {
        return 1;
    }

    GpuSvdState* g = &g_svd;
    if (ensure_libs(g) != 0) {
        log_svd_fallback_once("CUDA/cuBLAS/cuSOLVER library initialization failed", 2);
        return 2;
    }

    const size_t matrix_bytes = (size_t)n * (size_t)n * sizeof(double);
    int rc = 3;
    int lwork = 0;
    int info = -1;
    double smax = 0.0;
    double nscale = 0.0;
    double tol = 0.0;
    const double alpha = 1.0;
    const double beta = 0.0;

    if (g->fn_gesvd_buffer(g->solver, n, n, &lwork) != CUSOLVER_STATUS_SUCCESS || lwork <= 0) {
        log_svd_fallback_once("cusolverDnDgesvd_bufferSize failed", lwork);
        goto cleanup;
    }
    if (ensure_svd_buffers(g, n, lwork) != 0) {
        log_svd_fallback_once("reusable device buffer allocation failed", lwork);
        goto cleanup;
    }

    if (g->fn_memcpy(g->d_a, A_col_major, matrix_bytes, cudaMemcpyHostToDevice) != cudaSuccess) {
        log_svd_fallback_once("cudaMemcpy A host-to-device failed", n);
        goto cleanup;
    }

    if (g->fn_gesvd(g->solver, 'A', 'A', n, n, (double*)g->d_a, n, (double*)g->d_s,
            (double*)g->d_u, n, (double*)g->d_vt, n, (double*)g->d_work, lwork, NULL, (int*)g->d_info) != CUSOLVER_STATUS_SUCCESS) {
        log_svd_fallback_once("cusolverDnDgesvd failed", n);
        goto cleanup;
    }

    if (g->fn_memcpy(&info, g->d_info, sizeof(int), cudaMemcpyDeviceToHost) != cudaSuccess || info != 0) {
        log_svd_fallback_once("cusolverDnDgesvd returned nonzero info", info);
        goto cleanup;
    }

    if (g->fn_memcpy(&g->h_s[0], g->d_s, (size_t)n * sizeof(double), cudaMemcpyDeviceToHost) != cudaSuccess) {
        log_svd_fallback_once("cudaMemcpy singular values device-to-host failed", n);
        goto cleanup;
    }

    for (int i = 0; i < n; ++i) {
        if (g->h_s[(size_t)i] > smax) {
            smax = g->h_s[(size_t)i];
        }
    }
    nscale = (double)((n < 2) ? 1 : n);
    tol = (smax > 0.0) ? (smax * 1.0e-14 * nscale) : 0.0;
    for (int i = 0; i < n; ++i) {
        g->h_inv_s[(size_t)i] = (g->h_s[(size_t)i] > tol) ? (1.0 / g->h_s[(size_t)i]) : 0.0;
    }
    if (g->fn_memcpy(g->d_inv_s, &g->h_inv_s[0], (size_t)n * sizeof(double), cudaMemcpyHostToDevice) != cudaSuccess) {
        log_svd_fallback_once("cudaMemcpy inverse singular values host-to-device failed", n);
        goto cleanup;
    }

    /* scaled_vt = diag(inv_s) * VT, then out = scaled_vt^T * U^T = V * Sigma^+ * U^T. */
    if (g->fn_ddgmm(g->cublas, CUBLAS_SIDE_LEFT, n, n, (const double*)g->d_vt, n,
            (const double*)g->d_inv_s, 1, (double*)g->d_scaled_vt, n) != CUBLAS_STATUS_SUCCESS) {
        log_svd_fallback_once("cublasDdgmm failed", n);
        goto cleanup;
    }

    if (g->fn_dgemm(g->cublas, CUBLAS_OP_T, CUBLAS_OP_T, n, n, n, &alpha,
            (const double*)g->d_scaled_vt, n, (const double*)g->d_u, n, &beta, (double*)g->d_out, n) != CUBLAS_STATUS_SUCCESS) {
        log_svd_fallback_once("cublasDgemm failed", n);
        goto cleanup;
    }
    if (g->fn_memcpy(inv_col_major, g->d_out, matrix_bytes, cudaMemcpyDeviceToHost) != cudaSuccess) {
        log_svd_fallback_once("cudaMemcpy pseudoinverse device-to-host failed", n);
        goto cleanup;
    }
    if (g->fn_sync() != cudaSuccess) {
        log_svd_fallback_once("cudaDeviceSynchronize failed", n);
        goto cleanup;
    }

    {
        static int s_logged;
        if (!s_logged) {
            fprintf(stderr, "[ncslab_cuda] cuSOLVER SVD pseudoinverse on GPU (n=%d, min_n=%d)\n", n, min_n);
            s_logged = 1;
        }
    }
    rc = 0;

cleanup:
    return rc;
}
