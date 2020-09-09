package com.stepstone.search.hnswlib.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Interface that implements JNA (Java Native Access) to Hnswlib
 * a fast approximate nearest neighbor search library available
 * on (https://github.com/nmslib/hnswlib). This implementation was created
 * in order to provide a high performance also in Java (not only in Python or C++).
 *
 * This implementation relies also in a dynamic library generated
 * from the sources available in bindings.cpp.
 */
public interface Hnswlib extends Library {

	/**
	 * Allocates memory for the index in the native context and
	 * stores the address in a JNA Pointer variable.
	 *
	 * @param spaceName - use: l2, ip or cosine strings only;
	 * @param dimension - length of the vectors used for indexation.
	 *
	 * @return the index reference pointer.
	 */
	Pointer createNewIndex(String spaceName, int dimension);

	/**
	 * Initialize the index with information needed for the indexation.
	 *
	 * @param index - JNA pointer reference of the index;
	 * @param maxNumberOfElements - max number of elements in the index;
	 * @param m - the value of M;
	 * @param efConstruction - ef parameter;
	 * @param randomSeed - a random seed specified by the user.
	 *
	 * @return a result code.
	 */
	int initNewIndex(Pointer index, int maxNumberOfElements, int m, int efConstruction, int randomSeed);

	/**
	 * Add an item to the index.
	 *
	 * @param item - array containing the input to be inserted into the index;
	 * @param normalized - is the item normalized? if not and if required, it will be performed at the native level;
	 * @param label - an identifier to be used for this entry;
	 * @param index - JNA pointer reference of the index.
	 *
	 * @return a result code.
	 */
	int addItemToIndex(float[] item, boolean normalized, int label, Pointer index);

	/**
	 * Retrieve the number of elements already inserted into the index.
	 *
	 * @param index - JNA pointer reference of the index.
	 *
	 * @return number of items in the index.
	 */
	int getIndexLength(Pointer index);

	/**
	 * Save the content of an index into a file (using native implementation).
	 *
	 * @param index - JNA pointer reference of the index.
	 * @param path - path where the index will be stored.
	 *
	 * @return a result code.
	 */
	int saveIndexToPath(Pointer index, String path);

	/**
	 * Restore the content of an index saved into a file (using native implementation).
	 *
	 * @param index - JNA pointer reference of the index;
	 * @param maxNumberOfElements - max number of items to be inserted into the index;
	 * @param path - path where the index will be stored.
	 *
	 * @return a result code.
	 */
	int loadIndexFromPath(Pointer index, int maxNumberOfElements, String path);

	/**
	 * This function invokes the knnQuery available in the hnswlib native library.
	 *
	 * @param index - JNA pointer reference of the index;
	 * @param input - input used for the query;
	 * @param normalized - is the input normalized? if not and if required, it will be performed at the native level;
	 * @param k - dimension used for the query;
	 * @param indices [output] retrieves the indices returned by the query;
	 * @param coefficients [output] retrieves the coefficients returned by the query.
	 *
	 * @return a result code.
	 */
	int knnQuery(Pointer index, float[] input, boolean normalized, int k, int[] indices, float[] coefficients);

	/**
	 * Clear the index from the memory.
	 *
	 * @param index - JNA pointer reference of the index.
	 *
	 * @return a result code.
	 */
	int clearIndex(Pointer index);

	/**
	 * Sets the query time accuracy / speed trade-off value.
	 *
	 * @param index - JNA pointer reference of the index;
	 * @param ef value.
	 *
	 * @return a result code.
	 */
	int setEf(Pointer index, int ef);

	/**
	 * Populate vector with data for given id
	 * @param index index
	 * @param id id
	 * @param vector vector
	 * @param dim dimension
	 * @return result code
	 */
	int getData(Pointer index, int id, float[] vector, int dim);

	/**
	 * Determine whether the index contains data for given id
	 * @param index index
	 * @param id id
	 * @return result_code
	 */
	int hasId(Pointer index, int id);

	/**
	 * Compute similarity between two vectors
	 * @param index index
	 * @param vector1 vector1
	 * @param vector2 vector2
	 * @return similarity score between vectors
	 */
	float computeSimilarity(Pointer index, float[] vector1, float[] vector2);

}