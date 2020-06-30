package com.stepstone.search.hnswlib.jna.example;

import com.stepstone.search.hnswlib.jna.Index;
import com.stepstone.search.hnswlib.jna.QueryTuple;
import com.stepstone.search.hnswlib.jna.SpaceName;

import java.io.File;
import java.util.Arrays;

public class App {

    private static void exampleOfACosineIndex() {
        float[] i1 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        Index.normalize(i1);
        float[] i2 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.95f};
        Index.normalize(i2);
        float[] i3 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f};
        Index.normalize(i3); /* For cosine, if the normalization is not explicitly done, it will be done on the native side */
                             /* when you call index.addItem(). When explicitly done (in the Java code), use addNormalizedItem()
                                to avoid double normalization. */

        Index indexCosine = new Index(SpaceName.COSINE, 7);
        indexCosine.initialize(3);
        indexCosine.addNormalizedItem(i1, 1_111_111); /* 1_111_111 is a label */
        indexCosine.addNormalizedItem(i2, 2_222_222);
        indexCosine.addNormalizedItem(i3); /* if not defined, an incremental label will be automatically assigned */

        float[] input = new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        Index.normalize(input);

        QueryTuple cosineQT = indexCosine.knnNormalizedQuery(input, 3);

        System.out.println("Cosine Index - Query Results: ");
        System.out.println(Arrays.toString(cosineQT.getCoefficients()));
        System.out.println(Arrays.toString(cosineQT.getLabels()));
        indexCosine.clear();
    }

    private static void exampleOfAInnerProductIndex() {
        float[] i1 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        float[] i2 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.95f};
        float[] i3 = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f};

        Index indexIP = new Index(SpaceName.IP, 7);
        indexIP.initialize(3, 16, 100, 200);
        indexIP.addItem(i1, 1_111_111); /* 1_111_111 is a label */
        indexIP.addItem(i2, 0xCAFECAFE);
        indexIP.addItem(i3); /* if not defined, an incremental label will be automatically assigned */

        float[] input = new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};

        QueryTuple ipQT = indexIP.knnQuery(input, 3);

        System.out.println("Inner Product Index - Query Results: ");
        System.out.println(Arrays.toString(ipQT.getCoefficients()));
        System.out.println(Arrays.toString(ipQT.getLabels()));
        indexIP.clear();
    }

    /**
     * This is an example of how manually specify the location of the
     * dynamic libraries for hnswlib-jna. This step is required when
     * the pre-compiled ones (provided within the jars) are not sufficient
     * due to operating system dependencies or version of others libraries.
     */
    private static void setupHnswlibJnaDLLManually(){
        File projectFolder = new File("hnswlib-jna-example/lib");
        System.setProperty("jna.library.path", projectFolder.getAbsolutePath());
    }

    public static void main( String[] args ) {
        //setupHnswlibJnaDLLManually();
        exampleOfACosineIndex();
        exampleOfAInnerProductIndex();
    }
}
