# __Hnswlib with JNA (Java Native Access)__

This project contains a [JNA](https://github.com/java-native-access/jna) (Java Native Access) implementation built on top of the native [Hnswlib](https://github.com/nmslib/hnswlib) (fast approximate nearest neighbor search) and based on its python bindings. It includes some modifications and simplifications with the purpose of providing Hnswlib features with native like performance to applications written in Java. Differently from the python implementation, the multi-thread support is not included in the bindings itself but it can be easily implemented on the Java side.

&nbsp;
## __Compiling the Dynamic Library__

Before using `hnswlib-jna.jar` in your project, it is necessary to generate a dynamic library from the `binding.cpp` file which will be later on added and used by your project. This library requires at least C++11 to be compiled and it can be generated using `clang` via:

    clang++ -O3 -shared bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.dylib

__Note:__ The dynamic library's name must be: **libhnswlib-jna** (`.dylib` is the extension for MacOS, for windows use `.dll`, and linux `.so`).

&nbsp;
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

&nbsp;
### Instructions for Linux

1. Download and install CLang++. Older versions might give problems, so better use a recent version.
2. Compile the bindings using `clang`:
```
clang++ -O3 -fPIC -shared -std=c++11 bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.so
```
This procedure will generate `libhnswlib-jna.so`. 

&nbsp;
## __Using _hnswlib-jna_ in Your Project__

After generating the dynamic library, you are ready to move on and plug it into your project. Currently, the Java binding is built using `Maven`. In order to make a `jar` file available for local usage use `mvn install` and then add the following dependency in your `pom.xml`:
```
<dependency>
    <groupId>com.stepstone.search.hnswlib.jna</groupId>
    <artifactId>hnswlib-jna</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

&nbsp;
### Reading the Dynamic Library 

The last step is telling JNA where the dynamic library is located. This can be done setting `jna.library.path` via JVM parameters or system properties.

#### Via JVM parameters

	-Djna.library.path=<project_folder>/lib

#### Programmatically via System Class

	System.setProperty("jna.library.path", "<project_folder>/lib");

For more information and implementation details, please check [hnswlib-jna-example](./hnswlib-jna-example/).