/**
 * Runtime CUDA probe: loads cudart dynamically (no link-time -lcudart required).
 * When environment variable NCSLAB_SIM_CUDA is set to "1", allocates a tiny device
 * buffer to produce visible GPU activity (nvidia-smi) for validation.
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

typedef cudaError_t (*p_cudaSetDevice)(int);
typedef cudaError_t (*p_cudaMalloc)(void**, size_t);
typedef cudaError_t (*p_cudaFree)(void*);
typedef cudaError_t (*p_cudaMemcpy)(void*, const void*, size_t, int);
typedef cudaError_t (*p_cudaDeviceSynchronize)(void);

extern "C" int ncslab_cuda_linked_device_probe(double* out_sum);

#ifdef _WIN32
static void* cuda_open_library(void) {
    static const char* names[] = {
        "cudart64_13.dll",
        "cudart64_12.dll",
        "cudart64_110.dll",
        "cudart64_11.dll",
        "cudart64_10.dll",
    };
    for (size_t i = 0; i < sizeof(names) / sizeof(names[0]); ++i) {
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

static void* cuda_sym(void* lib, const char* name) {
    return (void*)GetProcAddress((HMODULE)lib, name);
}

static void cuda_close_library(void* lib) {
    if (lib != NULL) {
        FreeLibrary((HMODULE)lib);
    }
}
#else
static void* cuda_open_library(void) {
    static const char* names[] = {
        "libcudart.so.12",
        "libcudart.so.11.0",
        "libcudart.so.10.2",
        "libcudart.so",
    };
    for (size_t i = 0; i < sizeof(names) / sizeof(names[0]); ++i) {
        void* h = dlopen(names[i], RTLD_LAZY);
        if (h != NULL) {
            return h;
        }
    }
    return NULL;
}

static void* cuda_sym(void* lib, const char* name) {
    return dlsym(lib, name);
}

static void cuda_close_library(void* lib) {
    if (lib != NULL) {
        dlclose(lib);
    }
}
#endif

extern "C" void ncslab_cuda_runtime_probe(void) {
    const char* flag = getenv("NCSLAB_SIM_CUDA");
    if (flag == NULL || flag[0] != '1') {
        return;
    }

    double linked_sum = 0.0;
    int linked_rc = ncslab_cuda_linked_device_probe(&linked_sum);
    if (linked_rc == 0) {
        fprintf(stderr, "[ncslab_cuda] linked CUDA kernel OK (sum=%.6f); USE_CUDA=1 nvcc path active\n",
            linked_sum);
        return;
    }
    if (linked_rc < 0) {
        fprintf(stderr, "[ncslab_cuda] linked CUDA kernel failed rc=%d; falling back to runtime cudart probe\n",
            linked_rc);
    }

    void* lib = cuda_open_library();
    if (lib == NULL) {
        fprintf(stderr, "[ncslab_cuda] NCSLAB_SIM_CUDA=1 but could not load cudart (install CUDA toolkit / driver).\n");
        return;
    }

    p_cudaSetDevice fn_set = (p_cudaSetDevice)cuda_sym(lib, "cudaSetDevice");
    p_cudaMalloc fn_malloc = (p_cudaMalloc)cuda_sym(lib, "cudaMalloc");
    p_cudaFree fn_free = (p_cudaFree)cuda_sym(lib, "cudaFree");
    p_cudaMemcpy fn_memcpy = (p_cudaMemcpy)cuda_sym(lib, "cudaMemcpy");
    p_cudaDeviceSynchronize fn_sync = (p_cudaDeviceSynchronize)cuda_sym(lib, "cudaDeviceSynchronize");

    if (!fn_set || !fn_malloc || !fn_free || !fn_memcpy || !fn_sync) {
        fprintf(stderr, "[ncslab_cuda] Could not resolve required cuda runtime exports.\n");
        cuda_close_library(lib);
        return;
    }

    cudaError_t st = fn_set(0);
    if (st != cudaSuccess) {
        fprintf(stderr, "[ncslab_cuda] cudaSetDevice failed: %d\n", (int)st);
        cuda_close_library(lib);
        return;
    }

    const int n = 256;
    double host[n];
    double host_out[n];
    for (int i = 0; i < n; ++i) {
        host[i] = 1.0 + 0.001 * (double)i;
    }
    memset(host_out, 0, sizeof(host_out));

    void* dev = NULL;
    st = fn_malloc(&dev, sizeof(host));
    if (st != cudaSuccess || dev == NULL) {
        fprintf(stderr, "[ncslab_cuda] cudaMalloc failed: %d\n", (int)st);
        cuda_close_library(lib);
        return;
    }

    st = fn_memcpy(dev, host, sizeof(host), cudaMemcpyHostToDevice);
    if (st != cudaSuccess) {
        fprintf(stderr, "[ncslab_cuda] cudaMemcpy H2D failed: %d\n", (int)st);
        fn_free(dev);
        cuda_close_library(lib);
        return;
    }

    st = fn_memcpy(host_out, dev, sizeof(host), cudaMemcpyDeviceToHost);
    if (st != cudaSuccess) {
        fprintf(stderr, "[ncslab_cuda] cudaMemcpy D2H failed: %d\n", (int)st);
        fn_free(dev);
        cuda_close_library(lib);
        return;
    }

    st = fn_sync();
    if (st != cudaSuccess) {
        fprintf(stderr, "[ncslab_cuda] cudaDeviceSynchronize failed: %d\n", (int)st);
    }

    fn_free(dev);
    cuda_close_library(lib);

    double s = 0;
    for (int i = 0; i < n; ++i) {
        s += host_out[i];
    }
    fprintf(stderr, "[ncslab_cuda] probe OK (sum=%.6f); GPU path exercised when NCSLAB_SIM_CUDA=1\n", s);
}
