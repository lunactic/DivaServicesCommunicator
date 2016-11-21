package ch.unifr.diva;

import ch.unifr.diva.exceptions.CollectionException;
import ch.unifr.diva.exceptions.MethodNotAvailableException;
import ch.unifr.diva.request.DivaCollection;
import ch.unifr.diva.request.DivaServicesRequest;
import ch.unifr.diva.returnTypes.DivaServicesResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for simple DivaServicesCommunicator.
 */
public class DivaServicesCommunicatorTest {
    private static DivaServicesCommunicator divaServicesCommunicator;
    private static DivaCollection testCollection;
    @BeforeClass
    public static void beforeClass() throws IOException, CollectionException {

        divaServicesCommunicator = new DivaServicesCommunicator(new DivaServicesConnection("http://localhost:8080",5));
        //divaServicesCommunicator = new DivaServicesCommunicator("http://divaservices.unifr.ch");
        List<BufferedImage> images = new ArrayList<>();
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        images.add(image);
        //testCollection = divaServicesCommunicator.createCollection(images);
        testCollection = divaServicesCommunicator.createCollection("dimyummywhelp");
    }

    @Before
    public void beforeTest() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSeamCarving() throws IOException {
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runSeamCarvingTextlineExtraction(request, rect, 0.0003f, 3.0f, 4, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("nr of polygons:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testSauvolaBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runSauvolaBinarization(request, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramTextLineExtraction() throws IOException {
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runHistogramTextLineExtraction(request, rect);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("nr of rectangles:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testMultiScaleInterestPointDetection() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runMultiScaleInterestPointDetection(request, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("nr of points: " + response.getHighlighter().getData().size());
    }

    @Test
    public void testOtsuBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runOtsuBinarization(request, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyPageSegmentation() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\binary.png"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaCollection collection = divaServicesCommunicator.createCollection(images);
        DivaServicesRequest request = new DivaServicesRequest(collection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runOcropyPageSegmentation(request, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("number of segmented text lines: " + response.getOutput().size());
    }

    @Test
    public void testCannyEdgeDetection() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runCannyEdgeDetection(request, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramEnhancement() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runHistogramEnhancement(request, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    //@Test
    public void testLaplacianSharpening() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runLaplacianSharpening(request, 4, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try {
            response = divaServicesCommunicator.runOcropyBinarization(request, true);
        } catch (MethodNotAvailableException e) {
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testKrakenBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try{
            response = divaServicesCommunicator.runKrakenBinarization(request, true);
        } catch(MethodNotAvailableException e){
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testDecolorization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = null;
        try{
            response = divaServicesCommunicator.runDecolorization(request,0.5f, 0.001f,  true);
        } catch(MethodNotAvailableException e){
            e.printStackTrace();
        }
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }
}
