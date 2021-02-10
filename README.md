<a href="https://github.com/nmslib/hnswlib/releases"><img src="https://img.shields.io/static/v1?label=hnswlib&message=v0.4.0&color=blue"/></a>
<a href="https://travis-ci.org/stepstone-tech/hnswlib-jna"><img src="https://api.travis-ci.org/stepstone-tech/hnswlib-jna.svg"/></a>

# __Hnswlib with JNA (Java Native Access)__

This project contains a [JNA](https://github.com/java-native-access/jna) (Java Native Access) implementation built on top of the native [Hnswlib](https://github.com/nmslib/hnswlib) (Hierarchical Navigable Small World Graph) which offers a fast approximate nearest neighbor search. It includes some modifications and simplifications in order to provide Hnswlib features with native like performance to applications written in Java. Differently from the original Python implementation, the multi-thread support is not included in the bindings itself but it can be easily implemented on the Java side. `Hnswlib-jna` works in collaboration with a __shared library__ which contains the native code. For more information, please check the sections below.

## __Dependencies__

### __Pre-Generated Shared Library__

The jar file includes some pre-generated libraries for _Windows_, _Debian Linux_ and _MacOS_ (x86-64) which should allow an easy integration and abstract all complexity related to compilation. An extra library for Debian Linux (aarch64) is also available for tests with AWS Graviton 2. In the case of operating system issues, a runtime exception will be thrown and the manual compilation will be advised. 

__On Windows, the [Build Tools for Visual Studio 2019 (C++ build tools)](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2019) is required__.

## __Using in Your Project__

Add the following dependency in your `pom.xml`:
```
<dependency>
    <groupId>com.stepstone.search.hnswlib.jna</groupId>
    <artifactId>hnswlib-jna</artifactId>
    <version>1.4.2</version>
</dependency>
```

For more information and implementation details, please check [hnswlib-jna-example](./hnswlib-jna-example/).

## __Manual Compilation (Whenever it is advised)__

This section includes more information about how to compile the shared libraries on Windows, Linux and Mac for different architectures (e.g., `x86-64`, `aarch64`). __If you were able to run the example project on your PC, this section can be ignored.__

### __Compiling the Shared Library__

To generate the shared library required by this project, `binding.cpp` needs to be compiled using a C compiler (e.g., `clang` or `gcc`) with C++11 support, at least. The library can be generated with `clang` via:
```
clang++ -O3 -shared bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna-x86-64.dylib
```
__Note:__ The shared library's name must be: __libhnswlib-jna-ARCH.EXT__ where `ARCH` is the canonical architecture name (e.g., `x86-64` for AMD64, or `aarch64` for ARM64) and `EXT` is `dylib` for MacOS, for windows use `dll`, and linux `so`.

#### Instructions for Windows

##### Using Visual Studio Build Tools

1. Download and install [LLVM](https://releases.llvm.org/9.0.0/LLVM-9.0.0-win64.exe);
2. Download and install [Build Tools for Visual Studio 2019 (or higher)](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2019);
3. Compile the bindings using `clang`:
```
clang++ -O3 -shared bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna-x86-64.dll
```
This procedure will generate the 3 necessary files: `libhnswlib-jna-x86-64.dll`, `libhnswlib-jna-x86-64.exp` and `libhnswlib-jna-x86-64.lib`.

##### Using MinGW64

1. Download and install [LLVM](https://releases.llvm.org/9.0.0/LLVM-9.0.0-win64.exe);
2. Make sure that LLVM's bin folder is in your PATH;
3. Download [MinGW-w64 with Headers for Clang](https://sourceforge.net/projects/mingw-w64/files/Toolchains%20targetting%20Win64/Personal%20Builds/mingw-builds/8.1.0/threads-posix/seh/);
4. Unpack the archive and include MinGW64's bin folder into your PATH as well;
5. Compile the bindings using `clang`:
```
clang++ -O3 -target x86_64-pc-windows-gnu -shared bindings.cpp -I hnswlib -o <project_folder>lib/libhnswlib-jna-x86-64.dll -lpthread
```
This procedure will generate `libhnswlib-jna-x86-64.dll`. 

#### Instructions for Linux

1. Download and install `clang` (older versions might trigger compilation issues, so it is better use a recent version);
2. Compile the bindings using `clang`:
```
clang++ -O3 -fPIC -shared -std=c++11 bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna-x86-64.so
```
This procedure will generate `libhnswlib-jna-x86-64.so`. 

### __Reading the Shared Library in Your Project__

Once the shared library is available, it is necessary to tell the `JVM` and `JNA` where it is located. This can be done by setting the property `jna.library.path` via JVM parameters or system properties.

#### Via JVM parameters
```
-Djna.library.path=<project_folder>/lib
```
#### Programmatically via System Class
```
System.setProperty("jna.library.path", "<project_folder>/lib");
```
For more information and implementation details, please check [hnswlib-jna-example](./hnswlib-jna-example/).

## License
Copyright 2020 StepStone Services
    
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
&nbsp;&nbsp;&nbsp;&nbsp;[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
