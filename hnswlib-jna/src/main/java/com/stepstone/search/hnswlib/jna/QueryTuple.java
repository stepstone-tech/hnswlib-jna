package com.stepstone.search.hnswlib.jna;

/**
 * Query Tuple that represents the results of a knn query.
 * It contains two arrays: indices and coefficients.
 */
public class QueryTuple {

	int[] indices;
	float[] coefficients;

	QueryTuple (int k) {
		indices = new int[k];
		coefficients = new float[k];
	}

	public float[] getCoefficients() {
		return coefficients;
	}

	public int[] getIndices() {
		return indices;
	}
}
