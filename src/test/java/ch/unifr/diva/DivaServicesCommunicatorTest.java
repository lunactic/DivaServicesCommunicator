package ch.unifr.diva;

import org.junit.Before;
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

    @Before
    public void beforTest(){
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
        java.util.List<Polygon> response = DivaServicesCommunicator.runSeamCarvingTextlineExtraction(image, rect);
        System.out.println("nr of polygons:" + response.size());
    }

    @Test
    public void testSauvolaBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        BufferedImage resImage = DivaServicesCommunicator.runSauvolaBinarization(image);
        System.out.println("image size height: " + resImage.getHeight() + " - image size width: " + resImage.getWidth());
    }

    @Test
    public void testHistogramTextLineExtraction() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        Rectangle rect = new Rectangle(141, 331, 1208, 404);
        java.util.List<Rectangle> response = DivaServicesCommunicator.runHistogramTextLineExtraction(image, rect);
        System.out.println("nr of rectangles:" + response.size());
    }

    @Test
    public void testMultiScaleInterestPointDetection() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        java.util.List<Point> interestPoints = DivaServicesCommunicator.runMultiScaleInterestPointDetection(image, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        System.out.println("nr of points: " + interestPoints.size());
    }

    @Test
    public void testOtsuBinarization() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        BufferedImage resImage = DivaServicesCommunicator.runOtsuBinarization(image);
        ImageIO.write(resImage, "png", new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008-out.png"));
        System.out.println("image size height: " + resImage.getHeight() + " - image size width: " + resImage.getWidth());
    }

    @Test
    public void testUploadImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        String sourceMd5 = ImageEncoding.encodeToMd5(image);
        String targetMd5 = DivaServicesCommunicator.uploadImage(image);
        assertEquals(sourceMd5, targetMd5);
    }

    @Test
    public void testHistogramTextLineExtractionMdt() throws IOException {
        BufferedImage image = ImageIO.read(new File("D:\\DEV\\UniFr\\DivaServicesCommunicator\\data\\d-008.jpg"));
        String sourceMd5 = ImageEncoding.encodeToMd5(image);
    }

}
