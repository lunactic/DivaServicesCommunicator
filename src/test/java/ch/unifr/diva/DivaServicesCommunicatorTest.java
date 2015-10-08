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

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for simple DivaServicesCommunicator.
 */
public class DivaServicesCommunicatorTest{
    private static DivaServicesCommunicator divaServicesCommunicator;

    @BeforeClass
    public static void beforeClass(){
        divaServicesCommunicator = new DivaServicesCommunicator("http://divaservices.unifr.ch");
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
        //java.util.List<Polygon> response = divaServicesCommunicator.runSeamCarvingTextlineExtraction(image, rect);
        DivaServicesResponse response = divaServicesCommunicator.runSeamCarvingTextlineExtraction(image,rect);
        System.out.println("nr of polygons:" + response.getHighlighter().getData().size());
    }

    @Test
    public void testSauvolaBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        //BufferedImage resImage = divaServicesCommunicator.runSauvolaBinarization(image);
        DivaServicesResponse response = divaServicesCommunicator.runSauvolaBinarization(image);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
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
        DivaServicesResponse response = divaServicesCommunicator.runOtsuBinarization(image);
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
        DivaServicesResponse response = divaServicesCommunicator.runOcropyPageSegmentation(image);
        System.out.println("number of segmented text lines: " + response.getOutput().size());
    }

    @Test
    public void testCannyEdgeDetection() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runCannyEdgeDetection(image);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testHistogramEnhancement() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runHistogramEnhancement(image);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }
    @Test
    public void testLaplacianSharpening() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runLaplacianSharpening(image,4);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

    @Test
    public void testOcropyBinarization() throws IOException{
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        DivaServicesResponse response = divaServicesCommunicator.runOcropyBinarization(image);
        System.out.println("image size height: " + response.getImage().getHeight() + " - image size width: " + response.getImage().getWidth());
    }

}
