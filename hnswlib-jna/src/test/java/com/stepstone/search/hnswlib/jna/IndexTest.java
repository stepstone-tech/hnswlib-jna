package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.IndexAlreadyInitializedException;
import com.stepstone.search.hnswlib.jna.exception.ItemCannotBeInsertedIntoTheVectorSpace;
import com.stepstone.search.hnswlib.jna.exception.QueryCannotReturnResultsException;
import com.stepstone.search.hnswlib.jna.exception.UnexpectedNativeException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IndexTest {

	@BeforeClass
	public static void setUpOnce() {
		File libFolder = new File("lib");
		System.setProperty("jna.library.path", libFolder.toPath().toAbsolutePath().toString());
	}

	@Test
	public void testSingleIndexInstantiation(){
		Index i1 = new Index(SpaceName.IP, 30);
		assertNotNull(i1);
		i1.clear();
	}

	@Test
	public void testMultipleIndexInstantiation(){
		Index i1 = new Index(SpaceName.IP, 30);
		assertNotNull(i1);
		Index i2 = new Index(SpaceName.COSINE, 30);
		assertNotNull(i2);
		Index i3 = new Index(SpaceName.L2, 30);
		assertNotNull(i3);
		i1.clear();
		i2.clear();
		i3.clear();
	}

	@Test
	public void testIndexInitialization() throws UnexpectedNativeException {
		Index i1 = new Index(SpaceName.COSINE, 50);
		i1.initialize(500_000, 16, 200, 100);
		assertEquals(0, i1.getLength());
		i1.clear();
	}

	@Test(expected = IndexAlreadyInitializedException.class)
	public void testIndexMultipleInitialization() throws UnexpectedNativeException {
		Index i1 = new Index(SpaceName.COSINE, 50);
		i1.initialize(500_000, 16, 200, 100);
		i1.initialize(500_000, 16, 200, 100);
	}

	@Test
	public void testIndexAddItem() throws UnexpectedNativeException {
		Index i1 = new Index(SpaceName.COSINE, 3);
		i1.initialize(1);
		i1.addItem(new float[] { 1.3f, 1.2f, 1.5f }, 3);
		assertEquals(1, i1.getLength());
		i1.clear();
	}

	@Test
	public void testIndexAddItemIndependence() throws UnexpectedNativeException {
		testIndexAddItem();
		Index i2 = new Index(SpaceName.IP, 4);
		i2.initialize(3);
		assertEquals(0, i2.getLength());
		i2.clear();
	}

	@Test
	public void testIndexSaveAndLoad() throws UnexpectedNativeException, IOException {
		File tempFile = File.createTempFile("index", "sm");
		Path tempFilePath = Paths.get(tempFile.getAbsolutePath());

		Index i1 = new Index(SpaceName.COSINE, 3);
		i1.initialize(1);
		i1.addItem(new float[] { 1.3f, 1.2f, 1.5f }, 3);
		i1.save(tempFilePath);
		i1.clear();

		Index i2 = new Index(SpaceName.COSINE, 3);
		assertEquals(0, i2.getLength());
		i2.load(tempFilePath,1);
		assertEquals(1, i2.getLength());
		i2.clear();

		assertTrue(tempFile.delete());
	}

	@Test
	public void testParallelAddItemsInMultipleIndexes() throws InterruptedException, UnexpectedNativeException {
		int cpus = Runtime.getRuntime().availableProcessors();
		ExecutorService executorService = Executors.newFixedThreadPool(cpus);

		Index i1 = new Index(SpaceName.L2, 50);
		i1.initialize(1_050);

		Index i2 = new Index(SpaceName.COSINE, 50);
		i2.initialize(1_050);

		Runnable addItemIndex1 = () -> {
			try {
				i1.addItem(HnswlibTestUtils.getRandomFloatArray(50));
			} catch (UnexpectedNativeException e) {
				e.printStackTrace();
			}
		};
		Runnable addItemIndex2 = () -> {
			try {
				i2.addItem(HnswlibTestUtils.getRandomFloatArray(50));
			} catch (UnexpectedNativeException e) {
				e.printStackTrace();
			}
		};

		for(int i = 0; i < 1_000; i++) {
			executorService.submit(addItemIndex1);
			executorService.submit(addItemIndex2);
		}

		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.MINUTES);

		assertEquals(1_000, i1.getLength());
		assertEquals(1_000, i2.getLength());

		i1.clear(); i2.clear();
	}

	@Test
	public void testConcurrentInsertQuery() throws InterruptedException, UnexpectedNativeException {
		ExecutorService executorService = Executors.newFixedThreadPool(50);

		Index i1 = new Index(SpaceName.L2, 50);
		i1.initialize(1_050);

		float[] randomFloatArray = HnswlibTestUtils.getRandomFloatArray(50);

		Runnable addItemIndex1 = () -> {
			try {
				i1.addItem(randomFloatArray);
			} catch (UnexpectedNativeException e) {
				e.printStackTrace();
			}
		};

		Runnable queryItemIndex1 = () -> {
			QueryTuple queryTuple;
			try {
				queryTuple = i1.knnQuery(randomFloatArray, 1);
				assertEquals(50, queryTuple.getIndices().length);
				assertEquals(50, queryTuple.getCoefficients().length);
			} catch (UnexpectedNativeException e) {
				e.printStackTrace();
			}
		};

		for(int i = 0; i < 1_000; i++) {
			executorService.submit(addItemIndex1);
			executorService.submit(queryItemIndex1);
		}

		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.MINUTES);

		assertEquals(1_000, i1.getLength());
		i1.clear();
	}

	@Test(expected = QueryCannotReturnResultsException.class)
	public void testQueryEmptyException() throws UnexpectedNativeException {
		Index idx = new Index(SpaceName.COSINE, 3);
		idx.initialize(300);
		QueryTuple queryTuple = idx.knnQuery(new float[] {1.3f, 1.4f, 1.5f}, 3);
		assertNull(queryTuple);
	}

	@Test
	public void testOverwritingAnItemInTheModel() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.COSINE, 4);
		index.initialize(5);

		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 1.0f}, 1);
		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 0.95f}, 2);
		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 0.9f}, 3);
		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 0.85f}, 4);

		QueryTuple queryTuple = index.knnQuery(new float[] {1.0f, 1.0f, 1.0f, 1.0f}, 3);
		assertEquals(1, queryTuple.indices[0]);
		assertEquals(2, queryTuple.indices[1]);
		assertEquals(3, queryTuple.indices[2]);

		index.addItem(new float[] { 0.0f, 0.0f, 0.0f, 0.0f}, 2);
		queryTuple = index.knnQuery(new float[] {1.0f, 1.0f, 1.0f, 1.0f}, 3);
		assertEquals(1, queryTuple.indices[0]);
		assertEquals(3, queryTuple.indices[1]);
		assertEquals(4, queryTuple.indices[2]);

		index.clear();
	}

	@Test(expected = ItemCannotBeInsertedIntoTheVectorSpace.class)
	public void testIncludingMoreItemsThanPossible() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.L2, 4);
		index.initialize(2);

		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 1.0f}, 1);
		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 0.95f}, 2);
		index.addItem(new float[] { 1.0f, 1.0f, 1.0f, 0.9f}, 3);
	}

	@Test
	public void testNativeArrayNormalization() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.COSINE, 7);
		index.initialize(20);

		float[] item1 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
		index.addItem(item1); /* COSINE requires a normalized item. So, this input will be normalized (and modified) before being added to the index. */
		assertArrayEquals(new float[] {0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f}, item1, 0.000001f);

		float[] item2 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
		index.addNormalizedItem(item1); /* since we are using a add normalized method, nothing should happen here. */
		assertArrayEquals(new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, item2,0.000001f);

		QueryTuple queryTuple = index.knnQuery(new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, 1);
		assertEquals(-2.3841858E-7f, queryTuple.getCoefficients()[0], 0.00001);

		queryTuple = index.knnNormalizedQuery(item2, 1);
		assertEquals(-1.645751476f, queryTuple.getCoefficients()[0], 0.00001);
	}

	@Test
	public void testHostNormalization() {
		float[] item1 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
		Index.normalize(item1);
		assertArrayEquals(new float[] {0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f, 0.3779645f}, item1, 0.000001f);
	}

	@Test
	public void testIndexCosineEqualsToIPWhenNormalized() throws UnexpectedNativeException {
		float[] i1 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
		Index.normalize(i1);
		float[] i2 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.95f};
		Index.normalize(i2);
		float[] i3 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f};
		Index.normalize(i3);

		Index indexCosine = new Index(SpaceName.COSINE, 7);
		indexCosine.initialize(3);
		indexCosine.addNormalizedItem(i1, 1_111_111);
		indexCosine.addNormalizedItem(i2, 1_222_222);
		indexCosine.addNormalizedItem(i3, 1_333_333);

		Index indexIP = new Index(SpaceName.IP, 7);
		indexIP.initialize(3);
		indexIP.addNormalizedItem(i1, 1_111_111);
		indexIP.addNormalizedItem(i2, 1_222_222);
		indexIP.addNormalizedItem(i3, 1_333_333);

		float[] input = new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
		Index.normalize(input);

		QueryTuple cosineQT = indexCosine.knnNormalizedQuery(input, 3);
		QueryTuple ipQT = indexCosine.knnNormalizedQuery(input, 3);

		assertArrayEquals(cosineQT.getCoefficients(), ipQT.getCoefficients(), 0.000001f);
		assertArrayEquals(cosineQT.getIndices(), ipQT.getIndices());

		indexIP.clear();
		indexCosine.clear();
	}

	@Test
	public void testSimpleQueryOf5ElementsAndDimension7IP() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.IP, 7);
		index.initialize(7);

		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f }, 5);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.95f }, 6);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f }, 7);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.85f }, 8);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f },9);

		float[] input = new float[] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
		QueryTuple ipQT = index.knnQuery(input, 4);

		assertArrayEquals(new int[] {5, 6, 7, 8}, ipQT.getIndices());
		assertArrayEquals(new float[] {-6.0f, -5.95f, -5.9f, -5.85f}, ipQT.getCoefficients(), 0.000001f);
		index.clear();
	}

	@Test
	public void testSimpleQueryOf5ElementsAndDimension7Cosine() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.COSINE, 7);
		index.initialize(7);

		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f }, 14);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.95f }, 13);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f }, 12);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.85f }, 11);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f },10);

		float[] input = new float[] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
		QueryTuple ipQT = index.knnQuery(input, 4);

		assertArrayEquals(new int[] {14, 13, 12, 11}, ipQT.getIndices());
		assertArrayEquals(new float[] {-2.3841858E-7f, 1.552105E-4f, 6.2948465E-4f, 0.001435399f}, ipQT.getCoefficients(), 0.000001f);
		index.clear();
	}

	@Test
	public void testSimpleQueryOf5ElementsAndDimension7L2() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.L2, 7);
		index.initialize(7);

		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f }, 48);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.85f }, 10);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.95f }, 35);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f },1);
		index.addItem(new float [] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f }, 33);

		float[] input = new float[] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
		QueryTuple ipQT = index.knnQuery(input, 4);

		assertArrayEquals(new int[] {33, 35, 48, 10}, ipQT.getIndices());
		assertArrayEquals(new float[] { 0.0f, 0.002500001f, 0.010000004f, 0.022499993f}, ipQT.getCoefficients(), 0.000001f);
		index.clear();
	}

	@Ignore
	@Test
	public void testToBeValidatedAgainstPython() throws UnexpectedNativeException {
		Index indexIP = new Index(SpaceName.IP, 1);
		indexIP.initialize(7);

		indexIP.addItem(new float [] { 1.0f }, 1);
		indexIP.addItem(new float [] { 2.0f }, 2);
		indexIP.addItem(new float [] { 3.0f }, 3);
		indexIP.addItem(new float [] { 4.0f }, 4);
		indexIP.addItem(new float [] { 5.0f }, 5);
		indexIP.addItem(new float [] { 6.0f }, 6);

		float[] input = new float[] { 1.0f };

		QueryTuple ipQT = indexIP.knnQuery(input, 3);

		System.out.println("Inner Product Index - Query Results: ");
		System.out.println(Arrays.toString(ipQT.getCoefficients()));
		System.out.println(Arrays.toString(ipQT.getIndices()));

		indexIP.clear();
	}

}
