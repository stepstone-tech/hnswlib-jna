package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.IndexAlreadyInitializedException;
import com.stepstone.search.hnswlib.jna.exception.ItemCannotBeInsertedIntoTheVectorSpace;
import com.stepstone.search.hnswlib.jna.exception.QueryCannotReturnResultsException;
import com.stepstone.search.hnswlib.jna.exception.UnexpectedNativeException;
import com.sun.jna.Pointer;

import java.nio.file.Path;

/**
 * Represents a small world index in the java context.
 * This class includes some abstraction to make the integration
 * with the native library a bit more java like and relies on the
 * JNA implementation.
 *
 * Each instance of index has a different memory context and should
 * work independently.
 */
public final class Index {

	private static final int NO_ID = -1;
	private static final int RESULT_SUCCESSFUL = 0;
	private static final int RESULT_QUERY_NO_RESULTS = 3;
	private static final int RESULT_ITEM_CANNOT_BE_INSERTED_INTO_THE_VECTOR_SPACE = 4;

	private static Hnswlib hnswlib = HnswlibFactory.getInstance();

	private Pointer reference;
	private boolean initialized;

	public Index(SpaceName spaceName, int dimension) {
		reference = hnswlib.createNewIndex(spaceName.toString(), dimension);
	}

	/**
	 * @see {link #initialize(int, int, int, int)}
	 */
	public void initialize(int maxNumberOfElements) throws UnexpectedNativeException {
		initialize(maxNumberOfElements, 16, 200, 100);
	}

	/**
	 * Initialize the index to be used.
	 *
	 * @param maxNumberOfElements;
	 * @param m;
	 * @param efConstruction;
	 * @param randomSeed .
	 *
	 * @throws IndexAlreadyInitializedException
	 * @throws UnexpectedNativeException
	 */
	public void initialize(int maxNumberOfElements, int m, int efConstruction, int randomSeed) throws UnexpectedNativeException {
		if (initialized) {
			throw new IndexAlreadyInitializedException();
		} else {
			checkResultCode(hnswlib.initNewIndex(reference, maxNumberOfElements, m, efConstruction, randomSeed));
			initialized = true;
		}
	}

	/**
	 * Add an item without ID to the index.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	public void addItem(float[] item) throws UnexpectedNativeException {
		addItem(item, NO_ID);
	}

	/**
	 * Add an item with ID to the index. It won't apply any extra normalization
	 * unless it is required by the Vector Space (e.g., COSINE).
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param id - an identifier used by the native library.
	 */
	public void addItem(float[] item, int id) throws UnexpectedNativeException {
		checkResultCode(hnswlib.addItemToIndex(item, false, id, reference));
	}

	/**
	 * Add a normalized item without ID to the index.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	public void addNormalizedItem(float[] item) throws UnexpectedNativeException {
		addNormalizedItem(item, NO_ID);
	}

	/**
	 * Add a normalized item with ID to the index.
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param id - an identifier used by the native library.
	 */
	public void addNormalizedItem(float[] item, int id) throws UnexpectedNativeException {
		checkResultCode(hnswlib.addItemToIndex(item, true, id, reference));
	}

	/**
	 * Return tne number of elements already inserted in
	 * the index.
	 *
	 * @return elements count.
	 */
	public int getLength(){
		return hnswlib.getIndexLength(reference);
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
		QueryTuple queryTuple = new QueryTuple(k);
		checkResultCode(hnswlib.knnQuery(reference, input, false, k, queryTuple.indices, queryTuple.coefficients));
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
		QueryTuple queryTuple = new QueryTuple(k);
		checkResultCode(hnswlib.knnQuery(reference, input, true, k, queryTuple.indices, queryTuple.coefficients));
		return queryTuple;
	}

	/**
	 * Stores the content of the index into a file.
	 * This method relies on the native implementation.
	 *
	 * @param path - destination path
	 */
	public void save(Path path) throws UnexpectedNativeException {
		checkResultCode(hnswlib.saveIndexToPath(reference, path.toAbsolutePath().toString()));
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
		checkResultCode(hnswlib.loadIndexFromPath(reference, maxNumberOfElements, path.toAbsolutePath().toString()));
	}

	/**
	 * Free the memory allocated for this index in the native context.
	 *
	 * Note: Once the index is cleared it cannot be initialized again. FIXME
	 */
	public void clear(){
		hnswlib.clearIndex(reference);
	}

	/**
	 * This method checks the result code coming from the
	 * native execution is correct otherwise throws an exception.
	 *
	 * @throws UnexpectedNativeException
	 */
	private void checkResultCode(int resultCode) throws UnexpectedNativeException {
		switch (resultCode) {
			case RESULT_SUCCESSFUL:
				break;
			case RESULT_QUERY_NO_RESULTS:
				throw new QueryCannotReturnResultsException();
			case RESULT_ITEM_CANNOT_BE_INSERTED_INTO_THE_VECTOR_SPACE:
				throw new ItemCannotBeInsertedIntoTheVectorSpace();
			default:
				throw new UnexpectedNativeException();
		}
	}

	/**
	 * Util function that normalizes an array.
	 *
	 * @param array input.
	 */
	public static void normalize(float [] array){
		int n = array.length;
		double norm = 0;
		for (int i = 0; i < n; i++) {
			norm += array[i] * array[i];
		}
		norm = 1.0f / (Math.sqrt(norm) + 1e-30f);
		for (int i = 0; i < n; i++) {
			array[i] = array[i] * ((float) norm);
		}
	}
}
