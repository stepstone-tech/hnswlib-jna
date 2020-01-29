package com.stepstone.search.hnswlib.jna;

/**
 * Query Tuple that represents the results of a knn query.
 * It contains two arrays: labels and coefficients.
 */
public class QueryTuple {

	int[] labels;
	float[] coefficients;

	QueryTuple (int k) {
		labels = new int[k];
		coefficients = new float[k];
	}

	public float[] getCoefficients() {
		return coefficients;
	}

	public int[] getLabels() {
		return labels;
	}
}
