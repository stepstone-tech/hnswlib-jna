package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.OnceIndexIsClearedItCannotBeReusedException;
import com.stepstone.search.hnswlib.jna.exception.UnexpectedNativeException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
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
		assertTrue(Arrays.equals(vector, data.get()));
		assertFalse(index.hasId(1));
		assertFalse(index.getData(1).isPresent());
	}
}
