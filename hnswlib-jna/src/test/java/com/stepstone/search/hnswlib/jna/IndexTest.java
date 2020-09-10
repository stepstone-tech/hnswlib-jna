package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.OnceIndexIsClearedItCannotBeReusedException;
import com.stepstone.search.hnswlib.jna.exception.UnexpectedNativeException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IndexTest extends AbstractIndexTest {

	@Override
	protected Index createIndexInstance(SpaceName spaceName, int dimensions) {
		return new Index(spaceName, dimensions);
	}

	@Test
	public void testSynchronisedIndex() throws UnexpectedNativeException {
		Index i1 = createIndexInstance(SpaceName.COSINE, 50);
		i1.initialize(500_000, 16, 200, 100);
		Index syncIndex = Index.synchronizedIndex(i1);
		assertEquals(syncIndex.getLength(), i1.getLength());
		assertThat(syncIndex, instanceOf(ConcurrentIndex.class));
		syncIndex.clear();
	}

	@Test(expected = OnceIndexIsClearedItCannotBeReusedException.class)
	public void testSynchronisedIndexFailAfterReferenceClear() throws UnexpectedNativeException {
		Index i1 = createIndexInstance(SpaceName.COSINE, 50);
		i1.initialize(500_000, 16, 200, 100);
		Index syncIndex = Index.synchronizedIndex(i1);
		syncIndex.clear();
		//has to fail as i1 was cleared through syncIndex
		i1.addItem(HnswlibTestUtils.getRandomFloatArray(50));
	}

	@Test
	public void testComputeSimilarity() {
		Index index = createIndexInstance(SpaceName.COSINE, 2);
		index.initialize();
		float similarityClose = index.computeSimilarity(
				new float[]{1F, 2F},
				new float[]{1F, 3F}
		);
		float similarityFar = index.computeSimilarity(
				new float[] {1F, 100F},
				new float[] {50F, 450F}
		);
		// both values are minus, so the closer one should be closer to zero than the farther one
		assertEquals(Float.compare(similarityClose, similarityFar), 1);
	}
}
