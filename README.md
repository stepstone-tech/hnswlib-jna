# Hnswlib - JNA (Java Native Access) 

This is a work in progress project which contains a JNA (Java Native Access) implementation of the Hnswlib (fast approximate nearest neighbor search) created based on the python bindings (https://github.com/nmslib/hnswlib). It includes some modifications and simplifications with the purpose of providing with a native like performance to applications written in Java. Differently from the python implementation, the multi-thread support is not included in the bindings itself but it can be easily implemented on the Java side.

### Compiling the dynamic library

Before using **hnswlib-jna.jar** in your project, it is necessary to generate  a dynamic library from the _binding.cpp_ file provided, it will be later on added to your project. This can be done via:

> clang++ -O3 -shared -std=c++11 bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.dylib

Note: The dynamic library's name must be: **libhnswlib-jna**.<extension> (i.e., _dylib_ is the dynamic library extension for MacOS).

__Instructions for Windows:__

1. __Using Visual Studio build tools__

	* Download and install LLVM: https://releases.llvm.org/9.0.0/LLVM-9.0.0-win64.exe
	* Download and install Build Tools for Visual Studio 2019 (or higher):   
	    * https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2019
	* Then run the _clang++_ command above, replacing the _.dylib_ extension by _.dll_.
	    * Specifying C++11 can cause version-related issues, if that is the case it can be simply:
		  `clang++ -O3 -shared bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.dll`
	* This will generate the 3 necessary files: _libhnswlib-jna.dll_, _libhnswlib-jna.exp_ and _libhnswlib-jna.lib_.

2. __Using MinGW64 compiler__

	* Download and install LLVM: https://releases.llvm.org/9.0.0/LLVM-9.0.0-win64.exe
	* Make sure LLVM's bin folder is in your PATH variable
	* Download MinGW-w64 with headers for clang: 
		* https://sourceforge.net/projects/mingw-w64/files/Toolchains%20targetting%20Win64/Personal%20Builds/mingw-builds/8.1.0/threads-posix/seh/
	* Unpack the archive and include mingw64's bin folder into your PATH as well
	* Run:
	`clang++ -O3 -target x86_64-pc-windows-gnu -shared bindings.cpp -I hnswlib -o lib/libhnswlib-jna.dll -lpthread`
	* This will generate the 3 necessary files: _libhnswlib-jna.dll_.


### Reading the dynamic Library

Once the dynamic library is generated, JNA needs to know where it is located. One way is adding the library to the common library paths of the operational system. Another is adding a folder to the JNA path which can be done using a JVM parameter of via system properties.

__Using a JVM parameter:__

> -Djna.library.path=<project_folder>/lib

__Setting a system property:__

> System.setProperty("jna.library.path", "<project_folder>/lib");

### Using hnswlib-jna with Maven:

```
<dependency>
    <groupId>com.stepstone.search.hnswlib.jna</groupId>
    <artifactId>hnswlib-jna</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```