package ch.unifr.diva;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for simple DivaServicesCommunicator.
 */
public class DivaServicesCommunicatorTest {
    private static DivaServicesCommunicator divaServicesCommunicator;

    @BeforeClass
    public static void beforeClass() {
        divaServicesCommunicator = new DivaServicesCommunicator("http://192.168.56.101:8080",5);
        //divaServicesCommunicator = new DivaServicesCommunicator("http://divaservices.unifr.ch");
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
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runSeamCarvingTextlineExtraction(request, rect, 0.0003f, 3.0f, 4, true);
        System.out.println("nr of polygons:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testSauvolaBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runSauvolaBinarization(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramTextLineExtraction() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        //java.util.List<Rectangle> response = divaServicesCommunicator.runHistogramTextLineExtraction(image, rect);
        DivaServicesResponse response = divaServicesCommunicator.runHistogramTextLineExtraction(request, rect);
        System.out.println("nr of rectangles:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testMultiScaleInterestPointDetection() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        //java.util.List<Point> interestPoints = divaServicesCommunicator.runMultiScaleInterestPointDetection(image, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runMultiScaleInterestPointDetection(request, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        System.out.println("nr of points: " + response.getHighlighter().getData().size());
    }

    @Test
    public void testOtsuBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runOtsuBinarization(request, true);
        ImageIO.write(response.getImage(), "png", new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008-out.png"));
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testUploadImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        String sourceMd5 = ImageEncoding.encodeToMd5(image);
        String targetMd5 = divaServicesCommunicator.uploadImage(request);
        assertEquals(sourceMd5, targetMd5);
    }


    @Test
    public void testOcropyPageSegmentation() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\binary.png"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runOcropyPageSegmentation(request, true);
        System.out.println("number of segmented text lines: " + response.getOutput().size());
    }

    @Test
    public void testCannyEdgeDetection() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runCannyEdgeDetection(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramEnhancement() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runHistogramEnhancement(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testLaplacianSharpening() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runLaplacianSharpening(request, 4, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        DivaServicesRequest request = new DivaServicesRequest(images);
        DivaServicesResponse response = divaServicesCommunicator.runOcropyBinarization(request, true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testTranscriptionWorkflow() throws IOException {
        BufferedImage inputImage = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\Whiting_diary001.jpg"));
        //Binarize the image
        List<BufferedImage> images = new LinkedList<>();
        images.add(inputImage);
        DivaServicesRequest binarizationRequest = new DivaServicesRequest(images);
        DivaServicesResponse binarizationResult = divaServicesCommunicator.runOcropyBinarization(binarizationRequest, true);
        //Run ocropy page segmentation
        List<BufferedImage> binarizedImages = new LinkedList<>();
        binarizedImages.add(binarizationResult.getImage());
        DivaServicesRequest pageSegRequest = new DivaServicesRequest(images);
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
        DivaServicesRequest transcriptionRequest = new DivaServicesRequest(textLines);
        DivaServicesResponse response = divaServicesCommunicator.runOcropyTextExtraction(transcriptionRequest);
        List<Map> transcriptionOutput = response.getOutput();
        for(Map map : transcriptionOutput){
            String transcription = (String)map.get("recognition");
            System.out.println(transcription);
        }

    }

    @Test
    public void testUploadZipAndCollectionComputation() throws IOException{
        String collection = divaServicesCommunicator.uploadZip("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\Gmail.zip");
        DivaServicesRequest request = new DivaServicesRequest(collection);
        DivaServicesResponse response = divaServicesCommunicator.runSauvolaBinarization(request,true);
    }

    @Test
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
    }
}
