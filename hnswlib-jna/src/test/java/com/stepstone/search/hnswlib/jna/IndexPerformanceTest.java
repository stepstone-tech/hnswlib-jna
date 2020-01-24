package com.stepstone.search.hnswlib.jna;

import com.stepstone.search.hnswlib.jna.exception.UnexpectedNativeException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class IndexPerformanceTest {

	@BeforeClass
	public static void setUpOnce() {
		File libFolder = new File("lib");
		System.setProperty("jna.library.path", libFolder.toPath().toAbsolutePath().toString());
	}

	@Test
	public void testPerformanceSingleThreadInsertionOf600kItems() throws UnexpectedNativeException {
		Index index = new Index(SpaceName.COSINE, 50);
		index.initialize(600_000);
		long begin = Instant.now().getEpochSecond();
		for (int i = 0; i < 600_000; i++) {
			index.addItem(HnswlibTestUtils.getRandomFloatArray(50));
		}
		long end = Instant.now().getEpochSecond();
		assertTrue((end - begin) < 600); /* +/- 8min for 1 CPU (on 20/01/2020) */
		index.clear();
	}

	@Test
	public void testPerformanceMultiThreadedInsertionOf600kItems() throws UnexpectedNativeException, InterruptedException {
		int cpus = Runtime.getRuntime().availableProcessors();
		ExecutorService executorService = Executors.newFixedThreadPool(cpus);

		Index index = new Index(SpaceName.COSINE, 50);
		index.initialize(600_000);

		Runnable addItemIndex = () -> {
			try {
				index.addItem(HnswlibTestUtils.getRandomFloatArray(50));
			} catch (UnexpectedNativeException e) {
				e.printStackTrace();
			}
		};

		long begin = Instant.now().getEpochSecond();
		for (int i = 0; i < 600_000; i++) {
			executorService.submit(addItemIndex);
		}
		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.MINUTES);
		long end = Instant.now().getEpochSecond();
		assertTrue((end - begin) < 150); /* 102s on my laptop (on 20/01/2020) */
		index.clear();
	}

}
