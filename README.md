# Hnswlib - JNA (Java Native Access) 

This is a work in progress project which contains a JNA (Java Native Access) implementation of the Hnswlib (fast approximate nearest neighbor search) created based on the python bindings (https://github.com/nmslib/hnswlib). It includes some modifications and simplifications with the purpose of providing with a native like performance to applications written in Java. Differently from the python implementation, the multi-thread support is not included in the bindings itself but it can be easily implemented on the Java side.

### Compiling the dynamic library

Before using **hnswlib-jna.jar** in your project, it is necessary to generate  a dynamic library from the _binding.cpp_ file provided, it will be later on added to your project. This can be done via:

> clang++ -O3 -shared -std=c++11 bindings.cpp -I hnswlib -o <project_folder>/lib/libhnswlib-jna.dylib

Note: The dynamic library's name must be: **libhnswlib-jna**.<extension> (i.e., _dylib_ is the dynamic library extension for MacOS).

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