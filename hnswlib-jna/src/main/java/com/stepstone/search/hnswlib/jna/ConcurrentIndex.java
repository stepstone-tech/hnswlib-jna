package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.*;

import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentIndex extends Index {

	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
	private Lock readLock = readWriteLock.readLock();
	private Lock writeLock = readWriteLock.writeLock();

	public ConcurrentIndex(SpaceName spaceName, int dimensions) {
		super(spaceName, dimensions);
	}

	/**
	 * @see {link #initialize(int, int, int, int)}
	 */
	public void initialize(int maxNumberOfElements) throws UnexpectedNativeException {
		super.initialize(maxNumberOfElements, 16, 200, 100);
	}

	/**
	 * Initialize the index to be used.
	 *
	 * @param maxNumberOfElements;
	 * @param m;
	 * @param efConstruction;
	 * @param randomSeed .
	 *
	 * @throws IndexAlreadyInitializedException when a index reference was initialized before.
	 * @throws UnexpectedNativeException when something unexpected happened in the native side.
	 */
	public void initialize(int maxNumberOfElements, int m, int efConstruction, int randomSeed) throws UnexpectedNativeException {
		super.initialize(maxNumberOfElements, m, efConstruction, randomSeed);
	}

	/**
	 * Add an item without label to the index. Internally, an incremental
	 * label (starting from 1) will be given to this item.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	public void addItem(float[] item) throws UnexpectedNativeException {
		this.writeLock.lock();
		try {
			super.addItem(item, Index.NO_LABEL);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Add an item with ID to the index. It won't apply any extra normalization
	 * unless it is required by the Vector Space (e.g., COSINE).
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param label - an identifier used by the native library.
	 */
	public void addItem(float[] item, int label) throws UnexpectedNativeException {
		this.writeLock.lock();
		try {
			super.addItem(item, label);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Add a normalized item without label to the index. Internally, an incremental
	 * label (starting from 0) will be given to this item.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	public void addNormalizedItem(float[] item) throws UnexpectedNativeException {
		this.writeLock.lock();
		try {
			super.addNormalizedItem(item, Index.NO_LABEL);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Add a normalized item with ID to the index.
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param label - an identifier used by the native library.
	 */
	public void addNormalizedItem(float[] item, int label) throws UnexpectedNativeException {
		this.writeLock.lock();
		try {
			super.addNormalizedItem(item, label);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Return the number of elements already inserted in
	 * the index.
	 *
	 * @return elements count.
	 */
	public int getLength(){
		this.readLock.lock();
		try {
			return super.getLength();
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Performs a knn query in the index instance. In case the vector space requires
	 * the input to be normalized, it will normalize at the native level.
	 *
	 * @param input - float array;
	 * @param k - number of results expected.
	 *
	 * @return a query tuple instance that contain the indices and coefficients.
	 */
	public QueryTuple knnQuery(float[] input, int k) throws UnexpectedNativeException {
		this.readLock.lock();
		QueryTuple queryTuple = new QueryTuple(k);
		try {
			queryTuple = super.knnQuery(input, k);
		} finally {
			this.readLock.unlock();
		}
		return queryTuple;
	}

	/**
	 * Performs a knn query in the index instance using an normalized input.
	 * It will not normalize the vector again.
	 *
	 * @param input - a normalized float array;
	 * @param k - number of results expected.
	 *
	 * @return a query tuple instance that contain the indices and coefficients.
	 */
	public QueryTuple knnNormalizedQuery(float[] input, int k) throws UnexpectedNativeException {
		this.readLock.lock();
		QueryTuple queryTuple = new QueryTuple(k);
		try {
			queryTuple = super.knnNormalizedQuery(input, k);
		} finally {
			this.readLock.unlock();
		}
		return queryTuple;
	}

	/**
	 * Stores the content of the index into a file.
	 * This method relies on the native implementation.
	 *
	 * @param path - destination path.
	 */
	public void save(Path path) throws UnexpectedNativeException {
		this.readLock.lock();
		try {
			super.save(path);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * This method loads the content stored in a file path onto the index.
	 *
	 * Note: if the index was previously initialized, the old
	 * content will be erased.
	 *
	 * @param path - path to the index file;
	 * @param maxNumberOfElements - max number of elements in the index.
	 */
	public void load(Path path, int maxNumberOfElements) throws UnexpectedNativeException {
		this.writeLock.lock();
		try {
			super.load(path, maxNumberOfElements);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Free the memory allocated for this index in the native context.
	 *
	 * NOTE: Once the index is cleared, it cannot be initialized or used again.
	 */
	public void clear() throws UnexpectedNativeException {
		this.writeLock.lock();
		try {
			super.clear();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Cleanup the area allocated by the index in the native side.
	 *
	 * @throws Throwable when anything weird happened. :)
	 */
	@Override
	protected void finalize() throws Throwable {
		this.writeLock.lock();
		try {
			super.finalize();
		} finally {
			this.writeLock.unlock();
		}
	}

}
