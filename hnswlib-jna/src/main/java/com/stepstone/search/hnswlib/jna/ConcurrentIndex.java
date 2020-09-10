package com.stepstone.search.hnswlib.jna;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class offers a thread-safe alternative for a small-world Index.
 * It allows concurrent item insertion and querying which is not supported
 * by the native Hnswlib implementation.
 *
 * Note: this class relies on a ReadWriteLock with fairness enabled. So,
 * when multi-thread insertions are serialized. To take advantage of parallel
 * insertion, please create a Index instance and then retrieve a ConcurrentIndex
 * one via Index.synchronizedIndex() method call.
 */
public class ConcurrentIndex extends Index {

	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
	private Lock readLock = readWriteLock.readLock();
	private Lock writeLock = readWriteLock.writeLock();

	public ConcurrentIndex(SpaceName spaceName, int dimensions) {
		super(spaceName, dimensions);
	}

	/**
	 * Thread-safe method which adds an item without ID to the index.
	 * Internally, an incremental ID (starting from 1) will be given to this item.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	@Override
	public void addItem(float[] item) {
		this.writeLock.lock();
		try {
			super.addItem(item, NO_ID);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method which adds an item with ID to the index.
	 * It won't apply any extra normalization unless it is required
	 * by the Vector Space (e.g., COSINE).
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param id - an identifier used by the native library.
	 */
	@Override
	public void addItem(float[] item, int id) {
		this.writeLock.lock();
		try {
			super.addItem(item, id);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method which adds a normalized item without ID to the index.
	 * Internally, an incremental ID (starting from 0) will be given to this item.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	@Override
	public void addNormalizedItem(float[] item) {
		this.writeLock.lock();
		try {
			super.addNormalizedItem(item, Index.NO_ID);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method which adds a normalized item with ID to the index.
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param id - an identifier used by the native library.
	 */
	@Override
	public void addNormalizedItem(float[] item, int id) {
		this.writeLock.lock();
		try {
			super.addNormalizedItem(item, id);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method which returns the number of elements
	 * already inserted in the index.
	 *
	 * @return elements count.
	 */
	@Override
	public int getLength(){
		this.readLock.lock();
		try {
			return super.getLength();
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Thread-safe method which performs a knn query in the index instance.
	 * In case the vector space requires the input to be normalized, it will
	 * normalize at the native level.
	 *
	 * @param input - float array;
	 * @param k - number of results expected.
	 *
	 * @return a query tuple instance that contain the indices and coefficients.
	 */
	@Override
	public QueryTuple knnQuery(float[] input, int k) {
		this.readLock.lock();
		QueryTuple queryTuple;
		try {
			queryTuple = super.knnQuery(input, k);
		} finally {
			this.readLock.unlock();
		}
		return queryTuple;
	}

	/**
	 * Thread-safe method which performs a knn query in the index instance
	 * using an normalized input. It will not normalize the vector again.
	 *
	 * @param input - a normalized float array;
	 * @param k - number of results expected.
	 *
	 * @return a query tuple instance that contain the indices and coefficients.
	 */
	@Override
	public QueryTuple knnNormalizedQuery(float[] input, int k) {
		this.readLock.lock();
		QueryTuple queryTuple;
		try {
			queryTuple = super.knnNormalizedQuery(input, k);
		} finally {
			this.readLock.unlock();
		}
		return queryTuple;
	}

	/**
	 * Thread-safe method which stores the content of the index into a file.
	 * This method relies on the native implementation.
	 *
	 * @param path - destination path.
	 */
	@Override
	public void save(Path path) {
		this.readLock.lock();
		try {
			super.save(path);
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * Thread-safe method which loads the content stored in a file path onto the index.
	 *
	 * Note: if the index was previously initialized, the old
	 * content will be erased.
	 *
	 * @param path - path to the index file;
	 * @param maxNumberOfElements - max number of elements in the index.
	 */
	@Override
	public void load(Path path, int maxNumberOfElements) {
		this.writeLock.lock();
		try {
			super.load(path, maxNumberOfElements);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method which frees the memory allocated for this index in the native context.
	 *
	 * NOTE: Once the index is cleared, it cannot be initialized or used again.
	 */
	@Override
	public void clear() {
		this.writeLock.lock();
		try {
			super.clear();
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method which sets the query time accuracy / speed trade-off value.
	 *
	 * @param ef value.
	 */
	@Override
	public void setEf(int ef) {
		this.writeLock.lock();
		try {
			super.setEf(ef);
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * Thread-safe method that checks whether there is an item with the specified identifier in the index.
	 *
	 * @param id - identifier.
	 *
	 * @return true or false.
	 */
	public boolean hasId(int id) {
		this.readLock.lock();
		boolean hasId;
		try {
			hasId = super.hasId(id);
		} finally {
			this.readLock.unlock();
		}
		return hasId;
	}

	/**
	 * Thread-safe method that gets the data from a specific identifier in the index.
	 *
	 * @param id - identifier.
	 *
	 * @return an optional containing or not the
	 */
	public Optional<float[]> getData(int id) {
		this.readLock.lock();
		Optional data;
		try {
			data = super.getData(id);
		} finally {
			this.readLock.unlock();
		}
		return data;
	}

}
