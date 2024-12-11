cmake_minimum_required(VERSION 3.30 FATAL_ERROR)

# Change this to the real path for vcpkg on Windows
set(CMAKE_TOOLCHAIN_FILE "D:/menv/vcpkg/scripts/buildsystems/vcpkg.cmake")

# set project name and language
project(ncslab VERSION 1.0 LANGUAGES CXX)

# Include vcpkg toolchain
include(${CMAKE_TOOLCHAIN_FILE})

# Find required packages
find_package(Eigen3 REQUIRED NO_MODULE)
find_package(nlohmann_json REQUIRED)

# Add source files
file(GLOB ncslab_src CONFIGURE_DEPENDS "*.cpp" "*.hpp")

# Define executable
add_executable(${PROJECT_NAME} ${ncslab_src})

# Set C++ standard
target_compile_features(${PROJECT_NAME} PUBLIC cxx_std_17)

# Include directories
target_include_directories(${PROJECT_NAME}
    PRIVATE
    ${PROJECT_SOURCE_DIR}
    C:/ProgramData/anaconda3/include
)

# Link directories
target_link_directories(${PROJECT_NAME}
    PRIVATE
    C:/ProgramData/anaconda3/libs
)

# Link libraries
target_link_libraries(${PROJECT_NAME}
    PRIVATE
    nlohmann_json::nlohmann_json
    Eigen3::Eigen
    python311  # Adjust according to your Python version
)

# Conditional Link options
if(UNIX)
    target_link_options(${PROJECT_NAME}
        PRIVATE
        -rdynamic
    )
endif()

# Compile options
target_compile_options(${PROJECT_NAME}
    PRIVATE
    -D_SIMU
    -g
    -O2
    -Wformat
)
