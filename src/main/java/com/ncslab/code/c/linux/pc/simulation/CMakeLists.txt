cmake_minimum_required(VERSION 3.18 FATAL_ERROR)

# set project name and language
project(
    ncslablink
    VERSION 1.0
    LANGUAGES CXX
)


if (APPLE)
    # change this to the real path
    set(CMAKE_PREFIX_PATH "/Users/square/Documents/Code/libtorch")
elseif(UNIX)
    set(CMAKE_PREFIX_PATH "/home/square/ncslablink/libtorch")
endif()

find_package(nlohmann_json REQUIRED)


# list header files for IDEs (not necessary)
file(GLOB ncslab_src CONFIGURE_DEPENDS "*.h" "*.hpp" "*.c" "*.cpp")

add_executable(${PROJECT_NAME} ${ncslab_src})

target_compile_features(${PROJECT_NAME} PUBLIC cxx_std_17)

target_include_directories(${PROJECT_NAME}
    PRIVATE 
        ${PROJECT_SOURCE_DIR}      
)

target_link_directories(${PROJECT_NAME} 
    PRIVATE
    /usr/lib/python3.10/config-3.10-x86_64-linux-gnu
    /usr/lib/x86_64-linux-gnu
)

target_link_libraries(${PROJECT_NAME}
    PRIVATE
        ${JSONCPP_LIBRARIES}
        nlohmann_json::nlohmann_json
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

# The following code block is suggested to be used on Windows.
if (MSVC)
  file(GLOB TORCH_DLLS "${TORCH_INSTALL_PREFIX}/lib/*.dll")
  add_custom_command(TARGET ${PROJECT_NAME}
                     POST_BUILD
                     COMMAND ${CMAKE_COMMAND} -E copy_if_different
                     ${TORCH_DLLS}
                     $<TARGET_FILE_DIR:ncslab>)
endif (MSVC)
