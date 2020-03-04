package com.stepstone.search.hnswlib.jna;

import org.junit.Ignore;

@Ignore
public class NormalIndexPerformanceTest extends PerformanceTest {

	@Override
	protected Index createIndexInstance(SpaceName spaceName, int dimensions) {
		return new Index(spaceName, dimensions);
	}
}
