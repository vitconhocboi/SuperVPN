# PCRE library CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)

# Define PCRE source files (add the actual PCRE source files you have)
set(PCRE_SOURCES
    pcre_compile.c
    pcre_config.c
    pcre_dfa_exec.c
    pcre_exec.c
    pcre_fullinfo.c
    pcre_get.c
    pcre_globals.c
    pcre_info.c
    pcre_maketables.c
    pcre_newline.c
    pcre_ord2utf8.c
    pcre_refcount.c
    pcre_study.c
    pcre_tables.c
    pcre_try_flipped.c
    pcre_ucd.c
    pcre_valid_utf8.c
    pcre_version.c
    pcre_xclass.c
)

# Create static library
add_library(pcre STATIC ${PCRE_SOURCES})

# Add compiler definitions
target_compile_definitions(pcre PRIVATE
    HAVE_CONFIG_H
    PCRE_STATIC
    SUPPORT_UTF8
    SUPPORT_UCP
)

# Set include directories
target_include_directories(pcre PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Set compiler flags
target_compile_options(pcre PRIVATE
    -Wall
    -fPIC
    -DNDEBUG
) 