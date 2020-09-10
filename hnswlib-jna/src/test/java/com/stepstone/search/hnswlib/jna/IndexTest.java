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
	public void testGetData() {
		Index index = createIndexInstance(SpaceName.COSINE, 3);
		index.initialize();
		float[] vector = {1F, 2F, 3F};
		index.addItem(vector);
		assertTrue(index.hasId(0));
		Optional<float[]> data = index.getData(0);
		assertTrue(data.isPresent());
		assertArrayEquals(vector, data.get(), 0.0f);
		assertFalse(index.hasId(1));
		assertFalse(index.getData(1).isPresent());

		float[] vector2 = {1F, 2F, 3F};
		index.addItem(vector2, 1230);
		assertTrue(index.hasId(1230));
		assertFalse(index.hasId(1231));

		index.clear();
		assertFalse(index.hasId(1230));
		assertFalse(index.hasId(1231));
	}

	@Test
	public void testGetDataWhenIndexCleared() {
		Index index = createIndexInstance(SpaceName.COSINE, 3);
		index.initialize();
		index.clear();
		assertFalse(index.hasId(1202));
		Index index2 = createIndexInstance(SpaceName.COSINE, 3);
		assertFalse(index2.hasId(1202));
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
