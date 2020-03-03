package com.stepstone.search.hnswlib.jna;

public class NormalPerformanceTest extends PerformanceTest {

	@Override
	protected Index createIndexInstance(SpaceName spaceName, int dimensions) {
		return new Index(spaceName, dimensions);
	}
}
