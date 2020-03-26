<a href="https://github.com/nmslib/hnswlib/releases"><img src="https://img.shields.io/static/v1?label=hnswlib&message=v0.3.4&color=blue"/></a>
<a href="https://travis-ci.org/stepstone-tech/hnswlib-jna"><img src="https://api.travis-ci.org/stepstone-tech/hnswlib-jna.svg?branch=master"/></a>

# __Hnswlib with JNA (Java Native Access)__

This project contains a [JNA](https://github.com/java-native-access/jna) (Java Native Access) implementation built on top of the native [Hnswlib](https://github.com/nmslib/hnswlib) (Hierarchical Navigable Small World Graph) which offers a fast approximate nearest neighbor search. It includes some modifications and simplifications in order to provide Hnswlib features with native like performance to applications written in Java. Differently from the original Python implementation, the multi-thread support is not included in the bindings itself but it can be easily implemented on the Java side.

## __Using in Your Project__

Add the following dependency in your `pom.xml`:
```
<dependency>
    <groupId>com.stepstone.search.hnswlib.jna</groupId>
    <artifactId>hnswlib-jna</artifactId>
    <version>1.1.2-SNAPSHOT</version>
</dependency>
```

`hnswlib-jna` works in collaboration with a __shared library__ which contains the native code. For more information, please check the sections below.

## __Pre-Generated Shared Library__

 In order to decrease the complexity and tooling necessary to generate the shared library, we provide within the [jar](#) file some pre-generated libraries for _Windows_, _Debian Linux_ and _MacOS_ which should allow a transparent integration for the user. In case of operating system issues, a runtime exception will be thrown and the manual setup will be advised. 

__On Windows, the [Build Tools for Visual Studio 2019](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2019) is required__.

## __Compiling the Shared Library__

To generate the shared library required by this project, `binding.cpp` needs to be compiled using a C compiler (e.g., `clang` or `gcc`) with C++11 support, at least. The library can be generated with `clang` via:
```
clang++ -O3 -shared bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.dylib
```
__Note:__ The shared library's name must be: **libhnswlib-jna** (`.dylib` is the extension for MacOS, for windows use `.dll`, and linux `.so`).

### Instructions for Windows

#### Using Visual Studio Build Tools

1. Download and install [LLVM](https://releases.llvm.org/9.0.0/LLVM-9.0.0-win64.exe);
2. Download and install [Build Tools for Visual Studio 2019 (or higher)](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2019);
3. Compile the bindings using `clang`:
```
clang++ -O3 -shared bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.dll
```
This procedure will generate the 3 necessary files: `libhnswlib-jna.dll`, `libhnswlib-jna.exp` and `libhnswlib-jna.lib`.

#### Using MinGW64

1. Download and install [LLVM](https://releases.llvm.org/9.0.0/LLVM-9.0.0-win64.exe);
2. Make sure that LLVM's bin folder is in your PATH;
3. Download [MinGW-w64 with Headers for Clang](https://sourceforge.net/projects/mingw-w64/files/Toolchains%20targetting%20Win64/Personal%20Builds/mingw-builds/8.1.0/threads-posix/seh/);
4. Unpack the archive and include MinGW64's bin folder into your PATH as well;
5. Compile the bindings using `clang`:
```
clang++ -O3 -target x86_64-pc-windows-gnu -shared bindings.cpp -I hnswlib -o <project_folder>lib/libhnswlib-jna.dll -lpthread
```
This procedure will generate `libhnswlib-jna.dll`. 

### Instructions for Linux

1. Download and install `clang` (older versions might trigger compilation issues, so it is better use a recent version);
2. Compile the bindings using `clang`:
```
clang++ -O3 -fPIC -shared -std=c++11 bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.so
```
This procedure will generate `libhnswlib-jna.so`. 

## __Reading the Shared Library in Your Project__

Once the shared library is available, it is necessary to tell the `JVM` and `JNA` where it is located. This can be done by setting the property `jna.library.path` via JVM parameters or system properties.

### Via JVM parameters
```
-Djna.library.path=<project_folder>/lib
```
### Programmatically via System Class
```
System.setProperty("jna.library.path", "<project_folder>/lib");
```
For more information and implementation details, please check [hnswlib-jna-example](./hnswlib-jna-example/).
