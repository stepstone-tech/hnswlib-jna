package com.stepstone.search.hnswlib.jna;

public class ConcurrentPerformanceTest extends PerformanceTest {

	@Override
	protected Index createIndexInstance(SpaceName spaceName, int dimensions) {
		return new ConcurrentIndex(spaceName, dimensions);
	}
}
