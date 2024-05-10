cmake_minimum_required(VERSION 3.18 FATAL_ERROR)

# set project name and language
project(
    ncslab
    VERSION 1.0
    LANGUAGES CXX
)

set(CMAKE_PREFIX_PATH "/home/square/ncslablink/libtorch")

find_package(Torch REQUIRED)
find_package(PkgConfig REQUIRED)
pkg_check_modules(JSONCPP jsoncpp)

message("${JSONCPP_INCLUDE_DIRS}")
message("${JSONCPP_LIBRARIES}")

# you can also manually list source files with "set(SOURCES ...)" command
# list header files for IDEs (not necessary)
file(GLOB ncslab_src CONFIGURE_DEPENDS "*.h" "*.hpp" "*.c" "*.cpp")

add_executable(${PROJECT_NAME} ${ncslab_src})

target_compile_features(${PROJECT_NAME} PUBLIC cxx_std_17)

target_include_directories(${PROJECT_NAME}
    PRIVATE 
        ${PROJECT_SOURCE_DIR}
        ${JSONCPP_INCLUDE_DIRS}
        /home/square/ncslablink/WiringPi-2.61-1/wiringPi
        /usr/include/python3.10
)

target_link_directories(${PROJECT_NAME} PRIVATE
    ${TORCH_LIBRARIES}
    /usr/lib/python3.10/config-3.10-x86_64-linux-gnu
    /usr/lib/x86_64-linux-gnu
)

target_link_libraries(${PROJECT_NAME}
    PRIVATE
        ${JSONCPP_LIBRARIES}
        ${TORCH_LIBRARIES}
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
        ${TORCH_CXX_FLAGS}
)

target_link_options(${PROJECT_NAME}
    PRIVATE
        -rdynamic
        -fPIC
)

# The following code block is suggested to be used on Windows.
# According to https://github.com/pytorch/pytorch/issues/25457,
# the DLLs need to be copied to avoid memory errors.
if (MSVC)
  file(GLOB TORCH_DLLS "${TORCH_INSTALL_PREFIX}/lib/*.dll")
  add_custom_command(TARGET ${PROJECT_NAME}
                     POST_BUILD
                     COMMAND ${CMAKE_COMMAND} -E copy_if_different
                     ${TORCH_DLLS}
                     $<TARGET_FILE_DIR:ncslab>)
endif (MSVC)
