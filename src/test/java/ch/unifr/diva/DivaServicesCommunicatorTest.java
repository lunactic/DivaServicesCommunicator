package ch.unifr.diva;

import ch.unifr.diva.exceptions.CollectionException;
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
        testCollection = divaServicesCommunicator.createCollection("lightgreenperfumedcockatiel");
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
        DivaServicesResponse response = divaServicesCommunicator.runSeamCarvingTextlineExtraction(request, rect, 0.0003f, 3.0f, 4, true);
        System.out.println("nr of polygons:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testSauvolaBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runSauvolaBinarization(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramTextLineExtraction() throws IOException {
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runHistogramTextLineExtraction(request, rect);
        System.out.println("nr of rectangles:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testMultiScaleInterestPointDetection() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runMultiScaleInterestPointDetection(request, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        System.out.println("nr of points: " + response.getHighlighter().getData().size());
    }

    @Test
    public void testOtsuBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runOtsuBinarization(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyPageSegmentation() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\binary.png"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaCollection collection = divaServicesCommunicator.createCollection(images);
        DivaServicesRequest request = new DivaServicesRequest(collection);
        DivaServicesResponse response = divaServicesCommunicator.runOcropyPageSegmentation(request, true);
        System.out.println("number of segmented text lines: " + response.getOutput().size());
    }

    @Test
    public void testCannyEdgeDetection() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runCannyEdgeDetection(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramEnhancement() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runHistogramEnhancement(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testLaplacianSharpening() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runLaplacianSharpening(request, 4, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyBinarization() throws IOException {
        DivaServicesRequest request = new DivaServicesRequest(testCollection);
        DivaServicesResponse response = divaServicesCommunicator.runOcropyBinarization(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testTranscriptionWorkflow() throws IOException {
        BufferedImage inputImage = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\Whiting_diary001.jpg"));
        //Binarize the image
        List<BufferedImage> images = new LinkedList<>();
        images.add(inputImage);
        DivaCollection collection = divaServicesCommunicator.createCollection(images);

        DivaServicesRequest binarizationRequest = new DivaServicesRequest(collection);
        DivaServicesResponse binarizationResult = divaServicesCommunicator.runOcropyBinarization(binarizationRequest, true);
        //Run ocropy page segmentation
        List<BufferedImage> binarizedImages = new LinkedList<>();
        binarizedImages.add(binarizationResult.getImage());
        DivaServicesRequest pageSegRequest = new DivaServicesRequest(collection);
        DivaServicesResponse pageSegResult = divaServicesCommunicator.runOcropyPageSegmentation(pageSegRequest, true);
        //run text extraction for one textline
        List<Map> output = pageSegResult.getOutput();
        int i = 0;
        List<BufferedImage> textLines = new LinkedList<>();
        for (Map entry : output) {
            String md5 = (String) entry.get("md5");
            String url = (String) entry.get("url");
            textLines.add(divaServicesCommunicator.downloadImage(url));
        }
        DivaCollection textLinesCollection = divaServicesCommunicator.createCollection(textLines);
        DivaServicesRequest transcriptionRequest = new DivaServicesRequest(textLinesCollection);
        DivaServicesResponse response = divaServicesCommunicator.runOcropyTextExtraction(transcriptionRequest);
        List<Map> transcriptionOutput = response.getOutput();
        for(Map map : transcriptionOutput){
            String transcription = (String)map.get("recognition");
            System.out.println(transcription);
        }

    }

    /*@Test
    public void testLanguageModelTraining() throws IOException{
        DivaServicesRequest request = new DivaServicesRequest();
        request.addDataValue("url","http://192.168.56.101:8080/static/training.zip");
        DivaServicesResponse response = divaServicesCommunicator.trainOcrLanguageModel(request,"greek",1000,100,false);
        List<Map> outputs = response.getOutput();
        for(Map value : outputs){
            if(value.get("type").equals("model")){
                System.out.println("Computed a model with a minimal Error of: " + value.get("minmalError"));
                System.out.println("Model can be downloaded at: " + value.get("bestModel"));
                System.out.println("Training data used can be downloaded at: " + value.get("trainingData"));
            }
        }
    }*/
}
