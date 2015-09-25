package ch.unifr.diva;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Unit test for simple DivaServicesCommunicator.
 */
public class DivaServicesCommunicatorTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DivaServicesCommunicatorTest(String testName)
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DivaServicesCommunicatorTest.class );
    }


    public void testSeamCarving() throws IOException {
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        Rectangle rect = new Rectangle(141,331,1208,404);
        java.util.List<Polygon> response = DivaServicesCommunicator.runSeamCarvingTextlineExtraction(image, rect);
        System.out.println("nr of polygons:" + response.size());
    }

    public void testSauvolaBinarization() throws IOException{
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        BufferedImage resImage = DivaServicesCommunicator.runSauvolaBinarization(image);
        System.out.println("image size height: " + resImage.getHeight() + " - image size width: " + resImage.getWidth());
    }

    public void testHistogramTextLineExtraction() throws IOException{
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        Rectangle rect = new Rectangle(141,331,1208,404);
        java.util.List<Rectangle> response = DivaServicesCommunicator.runHistogramTextLineExtraction(image, rect);
        System.out.println("nr of rectangles:" + response.size());
    }

    public void testMultiScaleInterestPointDetection() throws IOException{
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        java.util.List<Point> interestPoints = DivaServicesCommunicator.runMultiScaleInterestPointDetection(image, "Harris", 1.0f, 5, 3, 0.000001f, 2);
        System.out.println("nr of points: " + interestPoints.size());
    }

    public void testOtsuBinarization() throws IOException{
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        BufferedImage resImage = DivaServicesCommunicator.runOtsuBinarization(image);
        System.out.println("image size height: " + resImage.getHeight() + " - image size width: " + resImage.getWidth());
    }

    public void testUploadImage() throws IOException{
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        String sourceMd5 = ImageEncoding.encodeToMd5(image);
        String targetMd5 = DivaServicesCommunicator.uploadImage(image);
        assertEquals(sourceMd5,targetMd5);
    }

    public void testHistogramTextLineExtractionMdt() throws IOException{
        BufferedImage image = ImageIO.read(new File("/home/lunactic/Downloads/csg562-005.png"));
        String sourceMd5 = ImageEncoding.encodeToMd5(image);
    }

}
