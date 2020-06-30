package com.stepstone.search.hnswlib.jna;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Factory for the hnswlib JNA (4.x.x) implementation.
 */
public final class HnswlibFactory {

	private static final String LIBRARY_NAME = "hnswlib-jna";
	private static final String JNA_LIBRARY_PATH_PROPERTY = "jna.library.path";

	private static Hnswlib instance;

	private HnswlibFactory() {
	}

	/**
	 * Return a single instance of the loaded library.
	 *
	 * @return hnswlib JNA instance.
	 */
	static synchronized Hnswlib getInstance() {
		if (instance == null) {
			try {
				checkIfLibraryProvidedNeedsToBeLoadedIntoSO();
				instance = (Hnswlib) Native.loadLibrary(LIBRARY_NAME, Hnswlib.class);
			} catch (UnsatisfiedLinkError | IOException e){
				throw new UnsatisfiedLinkError("It's not possible to use the pre-generated dynamic libraries on your system. "
						+ "Please compile it yourself (if not done yet) and set the \"" + JNA_LIBRARY_PATH_PROPERTY + "\" property "
						+ "with correct path to where \"" + getLibraryFileName() + "\" is located.");
			}
		}
		return instance;
	}

	private static String getLibraryFileName(){
		String library;
		if (Platform.isLinux()) {
			library = "libhnswlib-jna.so";
		} else if (Platform.isWindows()) {
			library = "libhnswlib-jna.dll";
		} else {
			library = "libhnswlib-jna.dylib";
		}
		return library;
	}

	private static void copyPreGeneratedLibraryFiles(Path folder, String fileName) throws IOException {
		InputStream libraryStream = HnswlibFactory.class.getResourceAsStream("/" + fileName);
		Files.copy(libraryStream, folder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
	}

	private static void checkIfLibraryProvidedNeedsToBeLoadedIntoSO() throws IOException {
		String property = System.getProperty(JNA_LIBRARY_PATH_PROPERTY);
		if (property == null) {
			Path libraryFolder = Files.createTempDirectory(LIBRARY_NAME);
			copyPreGeneratedLibraryFiles(libraryFolder, getLibraryFileName());
			if (Platform.isWindows()) {
				copyPreGeneratedLibraryFiles(libraryFolder, "libhnswlib-jna.exp");
				copyPreGeneratedLibraryFiles(libraryFolder, "libhnswlib-jna.lib");
			}
			System.setProperty(JNA_LIBRARY_PATH_PROPERTY, libraryFolder.toString());
			libraryFolder.toFile().deleteOnExit();
		}
	}
}
