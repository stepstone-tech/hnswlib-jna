package com.stepstone.search.hnswlib.jna;

import com.sun.jna.Native;

/**
 * Factory for the hnswlib JNA implementation.
 */
public final class HnswlibFactory {

	private static final String LIBRARY_NAME = "hnswlib-jna";

	private static Hnswlib instance;

	private HnswlibFactory() {
	}

	/**
	 * Return a single instance of the loaded library.
	 *
	 * @return hnswlib JNA instance.
	 */
	public static Hnswlib getInstance() {
		if (instance == null) {
			instance = Native.load(LIBRARY_NAME, Hnswlib.class);
		}
		return instance;
	}
}
