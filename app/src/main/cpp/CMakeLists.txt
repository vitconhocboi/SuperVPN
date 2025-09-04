cmake_minimum_required(VERSION 3.4.1)

add_definitions(-DHAVE_CONFIG_H)
add_definitions(-DFEATURE_GRACEFUL_TERMINATION)
#add_definitions(-DFEATURE_HTTPS_INSPECTION)
#add_definitions(-DFEATURE_HTTPS_INSPECTION_OPENSSL)


# Include directories for your code and Privoxy
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre
    ${CMAKE_CURRENT_SOURCE_DIR}/privoxy
)
file(GLOB PCRE_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/chartables.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/get.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/maketables.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/pcre.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/pcregrep.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/pcreposix.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcre/study.c"
)
# Add Privoxy source code
file(GLOB PRIVOXY_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/actions.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/client-tags.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/cgi.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/cgiedit.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/cgisimple.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/deanimate.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/encode.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/errlog.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/filters.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/gateway.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/jbsockets.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/jcc.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/loadcfg.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/loaders.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/miscutil.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/parsers.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/pcrs.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/list.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/radix.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/ssl.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/ssl_common.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/ssplit.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/privoxy/urlmatch.c"
)

# Define our library targets
add_library(
    share-pr
    SHARED
    ${PCRE_SOURCES}
    ${PRIVOXY_SOURCES}
    privoxy-jni.cpp
)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library(z-lib z)

# Link libraries with our target
target_link_libraries(
    share-pr
    -ljnigraphics
    android
    ${log-lib}
    ${z-lib}
)

#target_compile_options(
#        share-pr
#        PRIVATE
#        -O3
#        -funroll-loops
#        -fvisibility=hidden
#        "SHELL:-mllvm -bcf -mllvm -fla -mllvm -icall  -mllvm -igv -mllvm -ibr -mllvm -sobf"
#)
#
target_link_options(
        share-pr
        PRIVATE
        "-Wl,--strip-all"
        "-Wl,--exclude-libs,ALL"
        "-Wl,-z,max-page-size=16384"
)

