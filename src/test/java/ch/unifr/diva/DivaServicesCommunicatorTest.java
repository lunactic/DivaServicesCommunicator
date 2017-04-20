package ch.unifr.diva;

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

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for simple DivaServicesCommunicator.
 */
public class DivaServicesCommunicatorTest{
    private static DivaServicesCommunicator divaServicesCommunicator;

    @BeforeClass
    public static void beforeClass(){
        divaServicesCommunicator = new DivaServicesCommunicator("http://divaservices.unifr.ch/api/v1");
    }

    @Before
    public void beforeTest(){
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
        DivaServicesResponse response = divaServicesCommunicator.runSeamCarvingTextlineExtraction(image,rect,0.0003f,3.0f,4,true);
        System.out.println("nr of polygons:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testSauvolaBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        //BufferedImage resImage = divaServicesCommunicator.runSauvolaBinarization(image);
        DivaServicesResponse response = divaServicesCommunicator.runSauvolaBinarization(image,true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testGraphExtraction() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\data\\images\\176ddc62c420952e3e082b2134e75f5c\\crop_cpg330_0025_double.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runGraphExtraction(image, "Harris", true);
        System.out.println(response.getOutput().get("url"));
    }


    @Test
    public void testHistogramTextLineExtraction() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        //java.util.List<Rectangle> response = divaServicesCommunicator.runHistogramTextLineExtraction(image, rect);
        DivaServicesResponse response = divaServicesCommunicator.runHistogramTextLineExtraction(image,rect);
        System.out.println("nr of rectangles:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testMultiScaleInterestPointDetection() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        //java.util.List<Point> interestPoints = divaServicesCommunicator.runMultiScaleInterestPointDetection(image, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        DivaServicesResponse response = divaServicesCommunicator.runMultiScaleInterestPointDetection(image, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        System.out.println("nr of points: " + response.getHighlighter().getData().size());
    }

    @Test
    public void testOtsuBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        //BufferedImage resImage = divaServicesCommunicator.runOtsuBinarization(image);
        DivaServicesResponse response = divaServicesCommunicator.runOtsuBinarization(image,true);
        ImageIO.write(response.getImage(), "png", new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008-out.png"));
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testUploadImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        String sourceMd5 = ImageEncoding.encodeToMd5(image);
        String targetMd5 = divaServicesCommunicator.uploadImage(image);
        assertEquals(sourceMd5, targetMd5);
    }

    @Test
    public void testOcropyPageSegmentation() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\binary.png"));
        DivaServicesResponse response = divaServicesCommunicator.runOcropyPageSegmentation(image,true);
        System.out.println("number of segmented text lines: " + response.getOutput().size());
    }

    @Test
    public void testCannyEdgeDetection() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runCannyEdgeDetection(image,true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramEnhancement() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runHistogramEnhancement(image,true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }
    @Test
    public void testLaplacianSharpening() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runLaplacianSharpening(image,4,true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyBinarization() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runOcropyBinarization(image,true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }
    @Test
    public void testDecolorizing() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runDecolorization(image,0.5f,0.001f,true);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());

    }

    @Test
    public void testTranscriptionWorkflow() throws IOException{
        BufferedImage inputImage = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\Whiting_diary001.jpg"));
        //Binarize the image
        DivaServicesResponse binarizationResult = divaServicesCommunicator.runOtsuBinarization(inputImage,true);
        //Run ocropy page segmentation
        DivaServicesResponse pageSegResult = divaServicesCommunicator.runOcropyPageSegmentation(binarizationResult.getImage(),true);
        //run text extraction for one textline
        for(String textline : pageSegResult.getOutput().keySet()){
            DivaServicesResponse textExtraction = divaServicesCommunicator.runOcropyTextExtraction((String)pageSegResult.getOutput().get(textline));
            System.out.println("transcription of " + textline + ": " + textExtraction.getOutput().get("recognition"));
        }

    }

}
