cmake_minimum_required(VERSION 3.18 FATAL_ERROR)

# set project name and language
project(
    ncslab
    VERSION 1.0
    LANGUAGES CXX
)

if (APPLE)
    # change this to the real path
    set(CMAKE_TOOLCHAIN_FILE "/Users/square/ncslablink/vcpkg/scripts/buildsystems/vcpkg.cmake")
    set(CMAKE_OSX_ARCHITECTURES "arm64")
elseif(UNIX)
    set(CMAKE_TOOLCHAIN_FILE "/home/square/ncslablink/vcpkg/scripts/buildsystems/vcpkg.cmake")
endif()

include(${CMAKE_TOOLCHAIN_FILE})

find_package(Eigen3 REQUIRED NO_MODULE)
find_package(nlohmann_json REQUIRED)

# you can also manually list source files with "set(SOURCES ...)" command
# list header files for IDEs (not necessary)
file(GLOB ncslab_src CONFIGURE_DEPENDS "*.h" "*.hpp" "*.c" "*.cpp")

add_executable(${PROJECT_NAME} ${ncslab_src})

target_compile_features(${PROJECT_NAME} PUBLIC cxx_std_17)

target_include_directories(${PROJECT_NAME}
    PRIVATE 
        ${PROJECT_SOURCE_DIR} 
        /usr/include/python3.10     
)

target_link_directories(${PROJECT_NAME} 
    PRIVATE
        /usr/lib/python3.10/config-3.10-x86_64-linux-gnu
        /usr/lib/x86_64-linux-gnu
)

target_link_libraries(${PROJECT_NAME}
    PRIVATE
        nlohmann_json::nlohmann_json
        Eigen3::Eigen
        # python support
        crypt
        dl
        m
        python3.10
)

target_compile_options(${PROJECT_NAME}
    PRIVATE
        -D_SIMU
        -fPIC
        -g
        -O2
        -Wformat
)

target_link_options(${PROJECT_NAME}
    PRIVATE
        -rdynamic
        -fPIC
)

