cmake_minimum_required(VERSION 3.18)
project(circuit_cuda LANGUAGES C)

option(CIRCUIT_CUDA_ENABLE_GPU "Build with CUDA device backend" OFF)

add_library(circuit_cuda STATIC
    src/circuit_cuda.c
    src/circuit_cuda_cpu.c
)
target_include_directories(circuit_cuda PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_include_directories(circuit_cuda PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src
)
target_compile_definitions(circuit_cuda PRIVATE CIRCUIT_CUDA_BUILDING_LIB)

if(CIRCUIT_CUDA_ENABLE_GPU)
    enable_language(CUDA)
    if(NOT DEFINED CMAKE_CUDA_ARCHITECTURES)
        set(CMAKE_CUDA_ARCHITECTURES native)
    endif()
    target_sources(circuit_cuda PRIVATE src/circuit_cuda_gpu.cu)
    find_package(CUDAToolkit REQUIRED)
    target_link_libraries(circuit_cuda PUBLIC CUDA::cudart)
else()
    target_sources(circuit_cuda PRIVATE src/circuit_cuda_gpu_stub.c)
endif()

# Note: generated ncslab (Java build) can load cuBLAS at runtime for util.cpp tail dgemm
# (resources .../ncslab_cuda_cublas.cpp). That path does not link this CMake library.

add_executable(circuit_cuda_bench tools/bench.c)
target_link_libraries(circuit_cuda_bench PRIVATE circuit_cuda)
