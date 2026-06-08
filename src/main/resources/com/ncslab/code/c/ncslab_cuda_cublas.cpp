/**
 * Optional GPU path for dense tail dgemm (W * U^T) used after CPU SVD in util.cpp.
 * Loads cudart + cuBLAS at runtime (no -lcublas / -lcudart at link time).
 * Resident device buffers + cublas handle are reused across calls when possible.
 */

#include <cstdio>
#include <cstdlib>
#include <cstring>

#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#else
#include <dlfcn.h>
#endif

typedef int cudaError_t;
#ifndef cudaSuccess
#define cudaSuccess 0
#endif
#ifndef cudaMemcpyHostToDevice
#define cudaMemcpyHostToDevice 1
#endif
#ifndef cudaMemcpyDeviceToHost
#define cudaMemcpyDeviceToHost 2
#endif

typedef int cublasStatus_t;
#ifndef CUBLAS_STATUS_SUCCESS
#define CUBLAS_STATUS_SUCCESS 0
#endif
#ifndef CUBLAS_OP_N
#define CUBLAS_OP_N 0
#endif
#ifndef CUBLAS_OP_T
#define CUBLAS_OP_T 1
#endif

typedef cudaError_t (*p_cudaSetDevice)(int);
typedef cudaError_t (*p_cudaMalloc)(void**, size_t);
typedef cudaError_t (*p_cudaFree)(void*);
typedef cudaError_t (*p_cudaMemcpy)(void*, const void*, size_t, int);
typedef cudaError_t (*p_cudaDeviceSynchronize)(void);

typedef cublasStatus_t (*p_cublasCreate)(void** handle);
typedef cublasStatus_t (*p_cublasDestroy)(void* handle);
typedef cublasStatus_t (*p_cublasDgemm)(void* handle, int transa, int transb, int m, int n, int k,
    const double* alpha, const double* A, int lda, const double* B, int ldb, const double* beta,
    double* C, int ldc);
typedef cublasStatus_t (*p_cublasDgemv)(void* handle, int trans, int m, int n, const double* alpha,
    const double* A, int lda, const double* x, int incx, const double* beta, double* y, int incy);

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

static int parse_env_int(const char* name, int def_val) {
    const char* s = getenv(name);
    if (s == NULL || s[0] == '\0') {
        return def_val;
    }
    return (int)strtol(s, NULL, 10);
}

static int sim_cuda_enabled(void) {
    const char* f = getenv("NCSLAB_SIM_CUDA");
    return (f != NULL && f[0] == '1');
}

struct GpuGemmState {
    void* lib_cudart;
    void* lib_cublas;
    p_cudaSetDevice fn_set;
    p_cudaMalloc fn_malloc;
    p_cudaFree fn_free;
    p_cudaMemcpy fn_memcpy;
    p_cudaDeviceSynchronize fn_sync;
    p_cublasCreate fn_create;
    p_cublasDestroy fn_destroy;
    p_cublasDgemm fn_dgemm;
    p_cublasDgemv fn_dgemv;
    void* handle;
    void* d_w;
    void* d_u;
    void* d_c;
    void* d_x;
    void* d_y;
    int cap_n;
    int cap_vec_n;
    int init_failed;
};

static GpuGemmState g_state;

static int ensure_libs(GpuGemmState* g) {
    if (g->init_failed) {
        return -1;
    }
    if (g->lib_cudart != NULL && g->lib_cublas != NULL && g->handle != NULL) {
        return 0;
    }

#ifdef _WIN32
    static const char* crt_names[] = {"cudart64_13.dll", "cudart64_12.dll", "cudart64_110.dll", "cudart64_11.dll", "cudart64_10.dll"};
    static const char* blas_names[] = {"cublas64_13.dll", "cublas64_12.dll", "cublas64_110.dll", "cublas64_11.dll", "cublas64_10.dll"};
#else
    static const char* crt_names[] = {"libcudart.so.12", "libcudart.so.11.0", "libcudart.so.10.2", "libcudart.so"};
    static const char* blas_names[] = {"libcublas.so.12", "libcublas.so.11", "libcublas.so.10", "libcublas.so"};
#endif

    g->lib_cudart = load_lib(crt_names, sizeof(crt_names) / sizeof(crt_names[0]));
    if (g->lib_cudart == NULL) {
        g->init_failed = 1;
        return -1;
    }
    g->fn_set = (p_cudaSetDevice)sym(g->lib_cudart, "cudaSetDevice");
    g->fn_malloc = (p_cudaMalloc)sym(g->lib_cudart, "cudaMalloc");
    g->fn_free = (p_cudaFree)sym(g->lib_cudart, "cudaFree");
    g->fn_memcpy = (p_cudaMemcpy)sym(g->lib_cudart, "cudaMemcpy");
    g->fn_sync = (p_cudaDeviceSynchronize)sym(g->lib_cudart, "cudaDeviceSynchronize");
    if (!g->fn_set || !g->fn_malloc || !g->fn_free || !g->fn_memcpy || !g->fn_sync) {
        g->init_failed = 1;
        return -1;
    }

    g->lib_cublas = load_lib(blas_names, sizeof(blas_names) / sizeof(blas_names[0]));
    if (g->lib_cublas == NULL) {
        g->init_failed = 1;
        return -1;
    }
    g->fn_create = (p_cublasCreate)sym(g->lib_cublas, "cublasCreate_v2");
    g->fn_destroy = (p_cublasDestroy)sym(g->lib_cublas, "cublasDestroy_v2");
    g->fn_dgemm = (p_cublasDgemm)sym(g->lib_cublas, "cublasDgemm_v2");
    g->fn_dgemv = (p_cublasDgemv)sym(g->lib_cublas, "cublasDgemv_v2");
    if (!g->fn_create || !g->fn_destroy || !g->fn_dgemm || !g->fn_dgemv) {
        g->init_failed = 1;
        return -1;
    }

    cudaError_t st = g->fn_set(0);
    if (st != cudaSuccess) {
        g->init_failed = 1;
        return -1;
    }

    void* h = NULL;
    cublasStatus_t cb = g->fn_create(&h);
    if (cb != CUBLAS_STATUS_SUCCESS || h == NULL) {
        g->init_failed = 1;
        return -1;
    }
    g->handle = h;
    g->d_w = g->d_u = g->d_c = NULL;
    g->cap_n = 0;
    return 0;
}

static void free_device_buffers(GpuGemmState* g) {
    if (g->fn_free) {
        if (g->d_w) {
            g->fn_free(g->d_w);
            g->d_w = NULL;
        }
        if (g->d_u) {
            g->fn_free(g->d_u);
            g->d_u = NULL;
        }
        if (g->d_c) {
            g->fn_free(g->d_c);
            g->d_c = NULL;
        }
        if (g->d_x) {
            g->fn_free(g->d_x);
            g->d_x = NULL;
        }
        if (g->d_y) {
            g->fn_free(g->d_y);
            g->d_y = NULL;
        }
    }
    g->cap_n = 0;
    g->cap_vec_n = 0;
}

static int ensure_buffers(GpuGemmState* g, int n) {
    const size_t nbytes = (size_t)n * (size_t)n * sizeof(double);
    if (n <= 0) {
        return -1;
    }
    /* Reuse only for the same n so device layout stays lda==n (column-major). */
    if (g->cap_n == n && g->d_w && g->d_u && g->d_c) {
        return 0;
    }
    free_device_buffers(g);
    cudaError_t st = g->fn_malloc(&g->d_w, nbytes);
    if (st != cudaSuccess || g->d_w == NULL) {
        return -1;
    }
    st = g->fn_malloc(&g->d_u, nbytes);
    if (st != cudaSuccess || g->d_u == NULL) {
        g->fn_free(g->d_w);
        g->d_w = NULL;
        return -1;
    }
    st = g->fn_malloc(&g->d_c, nbytes);
    if (st != cudaSuccess || g->d_c == NULL) {
        g->fn_free(g->d_w);
        g->fn_free(g->d_u);
        g->d_w = g->d_u = NULL;
        return -1;
    }
    g->cap_n = n;
    return 0;
}

static int ensure_vector_buffers(GpuGemmState* g, int n) {
    if (n <= 0) {
        return -1;
    }
    if (g->cap_vec_n >= n && g->d_x && g->d_y) {
        return 0;
    }
    if (g->fn_free) {
        if (g->d_x) {
            g->fn_free(g->d_x);
            g->d_x = NULL;
        }
        if (g->d_y) {
            g->fn_free(g->d_y);
            g->d_y = NULL;
        }
    }
    if (g->fn_malloc(&g->d_x, (size_t)n * sizeof(double)) != cudaSuccess ||
        g->fn_malloc(&g->d_y, (size_t)n * sizeof(double)) != cudaSuccess) {
        if (g->d_x) {
            g->fn_free(g->d_x);
            g->d_x = NULL;
        }
        if (g->d_y) {
            g->fn_free(g->d_y);
            g->d_y = NULL;
        }
        g->cap_vec_n = 0;
        return -1;
    }
    g->cap_vec_n = n;
    return 0;
}

/**
 * Run one tail GEMM; @param log_first_tail 1 = print "[ncslab_cuda] tail dgemm..." once on success.
 */
static int gemm_run_tail(GpuGemmState* g, int n, const double* W_col, const double* U_col, double* C_col,
    int log_first_tail) {
    if (ensure_buffers(g, n) != 0) {
        return 3;
    }

    const size_t nbytes = (size_t)n * (size_t)n * sizeof(double);
    cudaError_t st = g->fn_memcpy(g->d_w, W_col, nbytes, cudaMemcpyHostToDevice);
    if (st != cudaSuccess) {
        return 3;
    }
    st = g->fn_memcpy(g->d_u, U_col, nbytes, cudaMemcpyHostToDevice);
    if (st != cudaSuccess) {
        return 3;
    }

    const double alpha = 1.0;
    const double beta = 0.0;
    cublasStatus_t cb = g->fn_dgemm(g->handle, CUBLAS_OP_N, CUBLAS_OP_T, n, n, n, &alpha,
        (const double*)g->d_w, n, (const double*)g->d_u, n, &beta, (double*)g->d_c, n);
    if (cb != CUBLAS_STATUS_SUCCESS) {
        return 3;
    }

    st = g->fn_memcpy(C_col, g->d_c, nbytes, cudaMemcpyDeviceToHost);
    if (st != cudaSuccess) {
        return 3;
    }
    st = g->fn_sync();
    if (st != cudaSuccess) {
        return 3;
    }
    static int s_logged_tail_gpu;
    if (log_first_tail && !s_logged_tail_gpu) {
        const int min_n = parse_env_int("NCSLAB_CUDA_DGEMM_MIN_N", 32);
        fprintf(stderr, "[ncslab_cuda] tail dgemm on GPU (n=%d, min_n=%d)\n", n, min_n);
        s_logged_tail_gpu = 1;
    }
    return 0;
}

/**
 * Compute C = W * U^T with column-major storage (matches GSL / cuBLAS).
 * @return 0 on success (C_col filled); nonzero => caller should fall back to CPU.
 */
extern "C" int ncslab_cuda_gemm_nt_colmajor_nxn(int n, const double* W_col, const double* U_col, double* C_col) {
    if (n <= 0 || W_col == NULL || U_col == NULL || C_col == NULL) {
        return -1;
    }
    if (!sim_cuda_enabled()) {
        return 1;
    }
    const char* dis = getenv("NCSLAB_CUDA_DISABLE");
    if (dis != NULL && dis[0] == '1') {
        return 1;
    }

    /* Default 32: typical partitioned circuit Msize=32 uses GPU tail; Msize=2 stays CPU. */
    const int min_n = parse_env_int("NCSLAB_CUDA_DGEMM_MIN_N", 32);
    if (n < min_n) {
        return 1;
    }

    static long long s_call_idx = 0;
    ++s_call_idx;
    const int after = parse_env_int("NCSLAB_CUDA_GPU_AFTER_CALLS", 0);
    if (after > 0 && s_call_idx <= (long long)after) {
        return 1;
    }

    GpuGemmState* g = &g_state;
    if (ensure_libs(g) != 0) {
        return 2;
    }
    return gemm_run_tail(g, n, W_col, U_col, C_col, 1);
}

/**
 * Compute y = A*x for row-major A using cuBLAS. A is uploaded once per StoreGAA
 * through device_cache; each solve copies only x and y.
 */
extern "C" int ncslab_cuda_dgemv_rowmajor_cached_nxn(int n, const double* A_row, const double* x, double* y, void** device_cache) {
    if (n <= 0 || A_row == NULL || x == NULL || y == NULL || device_cache == NULL) {
        return -1;
    }
    if (!sim_cuda_enabled()) {
        return 1;
    }
    const char* dis = getenv("NCSLAB_CUDA_DISABLE");
    if (dis != NULL && dis[0] == '1') {
        return 1;
    }
    const int min_n = parse_env_int("NCSLAB_CUDA_SOLVE_DGEMV_MIN_N", 128);
    if (n < min_n) {
        return 1;
    }

    GpuGemmState* g = &g_state;
    if (ensure_libs(g) != 0 || ensure_vector_buffers(g, n) != 0) {
        return 2;
    }

    const size_t matrix_bytes = (size_t)n * (size_t)n * sizeof(double);
    if (*device_cache == NULL) {
        void* d_a = NULL;
        if (g->fn_malloc(&d_a, matrix_bytes) != cudaSuccess || d_a == NULL) {
            return 3;
        }
        if (g->fn_memcpy(d_a, A_row, matrix_bytes, cudaMemcpyHostToDevice) != cudaSuccess) {
            g->fn_free(d_a);
            return 3;
        }
        *device_cache = d_a;
        static int s_logged_upload;
        if (!s_logged_upload) {
            fprintf(stderr, "[ncslab_cuda] cached inverse matrix on GPU for repeated dgemv solves (n=%d, min_n=%d)\n",
                n, min_n);
            s_logged_upload = 1;
        }
    }

    if (g->fn_memcpy(g->d_x, x, (size_t)n * sizeof(double), cudaMemcpyHostToDevice) != cudaSuccess) {
        return 3;
    }
    const double alpha = 1.0;
    const double beta = 0.0;
    cublasStatus_t cb = g->fn_dgemv(g->handle, CUBLAS_OP_T, n, n, &alpha,
        (const double*)(*device_cache), n, (const double*)g->d_x, 1, &beta, (double*)g->d_y, 1);
    if (cb != CUBLAS_STATUS_SUCCESS) {
        return 3;
    }
    if (g->fn_memcpy(y, g->d_y, (size_t)n * sizeof(double), cudaMemcpyDeviceToHost) != cudaSuccess) {
        return 3;
    }
    return 0;
}

extern "C" void ncslab_cuda_free_device_cache(void** device_cache) {
    if (device_cache == NULL || *device_cache == NULL) {
        return;
    }
    GpuGemmState* g = &g_state;
    if (ensure_libs(g) == 0 && g->fn_free != NULL) {
        g->fn_free(*device_cache);
    }
    *device_cache = NULL;
}

/**
 * Optional: run one small tail GEMM at startup so cuBLAS Lt library load / heuristic is not charged
 * to the first real pseudoinverse on the simulation critical path.
 * Suppressed when NCSLAB_CUDA_CUBLAS_WARMUP=0 or NCSLAB_CUDA_DISABLE=1.
 */
extern "C" void ncslab_cuda_cublas_warmup(void) {
    if (!sim_cuda_enabled()) {
        return;
    }
    const char* dis = getenv("NCSLAB_CUDA_DISABLE");
    if (dis != NULL && dis[0] == '1') {
        return;
    }
    const char* wu = getenv("NCSLAB_CUDA_CUBLAS_WARMUP");
    if (wu != NULL && wu[0] == '0') {
        return;
    }

    const int min_n = parse_env_int("NCSLAB_CUDA_DGEMM_MIN_N", 32);
    int n = min_n;
    if (n < 2) {
        n = 2;
    }
    if (n > 4096) {
        n = 4096;
    }

    const size_t nelem = (size_t)n * (size_t)n;
    double* W = (double*)calloc(nelem, sizeof(double));
    double* U = (double*)calloc(nelem, sizeof(double));
    double* C = (double*)calloc(nelem, sizeof(double));
    if (W == NULL || U == NULL || C == NULL) {
        free(W);
        free(U);
        free(C);
        return;
    }

    GpuGemmState* g = &g_state;
    if (ensure_libs(g) != 0) {
        free(W);
        free(U);
        free(C);
        return;
    }

    const int rc = gemm_run_tail(g, n, W, U, C, 0);
    free(W);
    free(U);
    free(C);

    if (rc == 0) {
        fprintf(stderr, "[ncslab_cuda] cublas warm-up OK (n=%d)\n", n);
    }
}
