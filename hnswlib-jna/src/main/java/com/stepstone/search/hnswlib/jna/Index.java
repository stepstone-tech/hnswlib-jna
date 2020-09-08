package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.IndexAlreadyInitializedException;
import com.stepstone.search.hnswlib.jna.exception.ItemCannotBeInsertedIntoTheVectorSpaceException;
import com.stepstone.search.hnswlib.jna.exception.OnceIndexIsClearedItCannotBeReusedException;
import com.stepstone.search.hnswlib.jna.exception.QueryCannotReturnResultsException;
import com.stepstone.search.hnswlib.jna.exception.UnableToCreateNewIndexInstanceException;
import com.stepstone.search.hnswlib.jna.exception.UnexpectedNativeException;
import com.sun.jna.Pointer;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a small world index in the java context.
 * This class includes some abstraction to make the integration
 * with the native library a bit more java like and relies on the
 * JNA implementation.
 *
 * Each instance of index has a different memory context and should
 * work independently.
 */
public class Index {

	protected static final int NO_LABEL = -1;
	private static final int RESULT_SUCCESSFUL = 0;
	private static final int RESULT_QUERY_NO_RESULTS = 3;
	private static final int RESULT_ITEM_CANNOT_BE_INSERTED_INTO_THE_VECTOR_SPACE = 4;
	private static final int RESULT_ONCE_INDEX_IS_CLEARED_IT_CANNOT_BE_REUSED = 5;
	private static final int RESULT_GET_DATA_FAILED = 6;

	private static Hnswlib hnswlib = HnswlibFactory.getInstance();

	private Pointer reference;
	private boolean initialized;
	private boolean cleared;
	private SpaceName spaceName;
	private int dimension;
	private boolean referenceReused;

	public Index(SpaceName spaceName, int dimension) {
		this.spaceName = spaceName;
		this.dimension = dimension;
		reference = hnswlib.createNewIndex(spaceName.toString(), dimension);
		if (reference == null) {
			throw new UnableToCreateNewIndexInstanceException();
		}
	}

	/**
	 * This method initializes the index with the default values
	 * for the parameters m, efConstruction, randomSeed and sets
	 * the maxNumberOfElements to 1_000_000.
	 *
	 * Note: not setting the maxNumberOfElements might lead to out of memory
	 * issues and unpredictable behaviours in your application. Thus, use this
	 * method wisely and combine it with monitoring.
	 *
	 * For more information, please @see {link #initialize(int, int, int, int)}.
	 */
	public void initialize() {
		initialize(1_000_000);
	}

	/**
	 * For more information, please @see {link #initialize(int, int, int, int)}.
	 *
	 * @param maxNumberOfElements allowed in the index.
	 */
	public void initialize(int maxNumberOfElements) {
		initialize(maxNumberOfElements, 16, 200, 100);
	}

	/**
	 * Initialize the index to be used.
	 *
	 * @param maxNumberOfElements ;
	 * @param m ;
	 * @param efConstruction ;
	 * @param randomSeed .
	 *
	 * @throws IndexAlreadyInitializedException when a index reference was initialized before.
	 * @throws UnexpectedNativeException when something unexpected happened in the native side.
	 */
	public void initialize(int maxNumberOfElements, int m, int efConstruction, int randomSeed) {
		if (initialized) {
			throw new IndexAlreadyInitializedException();
		} else {
			checkResultCode(hnswlib.initNewIndex(reference, maxNumberOfElements, m, efConstruction, randomSeed));
			initialized = true;
		}
	}

	/**
	 * Sets the query time accuracy / speed trade-off value.
	 *
	 * @param ef value.
	 */
	public void setEf(int ef) {
		checkResultCode(hnswlib.setEf(reference, ef));
	}

	/**
	 * Add an item without label to the index. Internally, an incremental
	 * label (starting from 1) will be given to this item.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	public void addItem(float[] item) {
		addItem(item, NO_LABEL);
	}

	/**
	 * Add an item with ID to the index. It won't apply any extra normalization
	 * unless it is required by the Vector Space (e.g., COSINE).
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param label - an identifier used by the native library.
	 */
	public void addItem(float[] item, int label) {
		checkResultCode(hnswlib.addItemToIndex(item, false, label, reference));
	}

	/**
	 * Add a normalized item without label to the index. Internally, an incremental
	 * label (starting from 0) will be given to this item.
	 *
	 * @param item - float array with the length expected by the index (dimension).
	 */
	public void addNormalizedItem(float[] item) {
		addNormalizedItem(item, NO_LABEL);
	}

	/**
	 * Add a normalized item with ID to the index.
	 *
	 * @param item - float array with the length expected by the index (dimension);
	 * @param label - an identifier used by the native library.
	 */
	public void addNormalizedItem(float[] item, int label) {
		checkResultCode(hnswlib.addItemToIndex(item, true, label, reference));
	}

	/**
	 * Return the number of elements already inserted in
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
	public QueryTuple knnQuery(float[] input, int k) {
		QueryTuple queryTuple = new QueryTuple(k);
		checkResultCode(hnswlib.knnQuery(reference, input, false, k, queryTuple.labels, queryTuple.coefficients));
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
	public QueryTuple knnNormalizedQuery(float[] input, int k) {
		QueryTuple queryTuple = new QueryTuple(k);
		checkResultCode(hnswlib.knnQuery(reference, input, true, k, queryTuple.labels, queryTuple.coefficients));
		return queryTuple;
	}

	/**
	 * Stores the content of the index into a file.
	 * This method relies on the native implementation.
	 *
	 * @param path - destination path.
	 */
	public void save(Path path) {
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
	public void load(Path path, int maxNumberOfElements) {
		checkResultCode(hnswlib.loadIndexFromPath(reference, maxNumberOfElements, path.toAbsolutePath().toString()));
	}

	/**
	 * Free the memory allocated for this index in the native context.
	 *
	 * NOTE: Once the index is cleared, it cannot be initialized or used again.
	 */
	public void clear() {
		checkResultCode(hnswlib.clearIndex(reference));
		cleared = true;
	}

	/**
	 * Cleanup the area allocated by the index in the native side.
	 *
	 * @throws Throwable when anything weird happened. :)
	 */
	@Override
	protected void finalize() throws Throwable {
		if (!cleared && !referenceReused) {
			this.clear();
		}
		super.finalize();
	}

	/**
	 * This method checks the result code coming from the
	 * native execution is correct otherwise throws an exception.
	 *
	 * @throws UnexpectedNativeException when something went out of control in the native side.
	 */
	private void checkResultCode(int resultCode) {
		switch (resultCode) {
			case RESULT_SUCCESSFUL:
				break;
			case RESULT_QUERY_NO_RESULTS:
				throw new QueryCannotReturnResultsException();
			case RESULT_ITEM_CANNOT_BE_INSERTED_INTO_THE_VECTOR_SPACE:
				throw new ItemCannotBeInsertedIntoTheVectorSpaceException();
			case RESULT_ONCE_INDEX_IS_CLEARED_IT_CANNOT_BE_REUSED:
				throw new OnceIndexIsClearedItCannotBeReusedException();
			default:
				throw new UnexpectedNativeException();
		}
	}

	public boolean hasId(int id) {
		return hnswlib.hasId(reference, id);
	}

	public Optional<float[]> getData(int id) {
		float[] vector = new float[dimension];
		int success = hnswlib.getData(reference, id, vector, dimension);
		if (success != RESULT_GET_DATA_FAILED) {
			return Optional.of(vector);
		}
		return Optional.empty();
	}

	public float computeSimilarity(float[] vector1, float[] vector2) {
		return hnswlib.computeSimilarity(reference, vector1, vector2);
	}

	/**
	 * Util function that normalizes an array.
	 *
	 * @param array input.
	 */
	public static strictfp void normalize(float [] array){
		int n = array.length;
		float norm = 0;
		for (float v : array) {
			norm += v * v;
		}
		norm = (float) (1.0f / (Math.sqrt(norm) + 1e-30f));
		for (int i = 0; i < n; i++) {
			array[i] = array[i] * norm;
		}
	}

	/**
	 * This method returns a ConcurrentIndex instance which contains
	 * the same items present in a Index object. The indexes will share
	 * the same native pointer, so there will be no memory duplication.
	 *
	 * It is important to say that the Index class allows adding items is in
	 * parallel, so the building time can be much slower. On the other hand,
	 * ConcurrentIndex offers thread-safe methods for adding and querying, which
	 * can be interesting for multi-threaded environment with online
	 * updates. Via this method, you can get the best of the two worlds.
	 *
	 * @param index with the items added
	 * @return a thread-safe index
	 */
	public static Index synchronizedIndex(Index index) {
		Index concurrentIndex = new ConcurrentIndex(index.spaceName, index.dimension);
		concurrentIndex.reference = index.reference;
		concurrentIndex.cleared = index.cleared;
		concurrentIndex.initialized = index.initialized;
		index.referenceReused = true;
		return concurrentIndex;
	}
}
