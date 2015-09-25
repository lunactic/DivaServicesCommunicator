package ch.unifr.diva;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

public class DivaServicesCommunicator {


    public static String uploadImage(BufferedImage image){
        String base64Image = ImageEncoding.encodeToBase64(image);
        Map<String, Object> highlighter = new HashMap();

        JSONObject request = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        request.put("highlighter", high);
        request.put("inputs", inputs);
        request.put("image", base64Image);
        JSONObject result = executePost("http://127.0.0.1:8080/upload", request);
        return result.getString("md5");

    }

    /**
     *
     * @param image
     * @return
     */
    public static BufferedImage runOtsuBinarization(BufferedImage image){
        String base64Image = ImageEncoding.encodeToBase64(image);
        Map<String, Object> highlighter = new HashMap();

        JSONObject request = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        request.put("highlighter", high);
        request.put("inputs", inputs);
        request.put("image", base64Image);
        JSONObject result = executePost("http://divaservices.unifr.ch/imageanalysis/binarization/otsu", request);
        String resImage = (String)result.get("image");
        return ImageEncoding.decodeBas64(resImage);

    }

    /**
     * Extracts interest points from an image
     * @param image the image to extract points from
     * @param detector the detector to use
     * @param blurSigma the amount of blur applied
     * @param numScales the number of scales
     * @param numOctaves the number of octaves
     * @param threshold the threshold
     * @param maxFeaturesPerScale the maximal number of features to use
     * @return A list of points, representing the interest points
     */
    public static List<Point> runMultiScaleInterestPointDetection(BufferedImage image, String detector, float blurSigma, int numScales, int numOctaves, float threshold, int maxFeaturesPerScale){
        String base64Image = ImageEncoding.encodeToBase64(image);
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(10); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

        JSONObject request = new JSONObject();
        JSONObject high = new JSONObject();
        JSONObject inputs = new JSONObject();
        inputs.put("detector",detector);
        inputs.put("blurSigma",blurSigma);
        inputs.put("numScales",numScales);
        inputs.put("numOctaves",numOctaves);
        inputs.put("threshold",df.format(threshold));
        inputs.put("maxFeaturesPerScale",maxFeaturesPerScale);

        request.put("highlighter", high);
        request.put("inputs", inputs);
        request.put("image", base64Image);

        //System.out.println(request.toString());

        JSONObject result = executePost("http://divaservices.unifr.ch/ipd/multiscale",request);

        return extractPoints(result);
    }

    /**
     *
     * @param image
     * @param rectangle
     * @return A list of polygons, each representing a text line
     */
    public static List<Rectangle> runHistogramTextLineExtraction(BufferedImage image, Rectangle rectangle){
        String base64Image = ImageEncoding.encodeToBase64(image);
        Map<String, Object> highlighter = new HashMap();
        highlighter.put("segments", prepareRectangle(rectangle));
        highlighter.put("closed", true);
        highlighter.put("type", "rectangle");

        JSONObject request = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        request.put("highlighter", high);
        request.put("inputs", inputs);
        request.put("image", base64Image);

        JSONObject result = executePost("http://divaservices.unifr.ch/segmentation/textline/hist", request);
        // now you have the string representation of the HTML request

        return extractRectangles(result);
    }

    /**
     *
     * @param image
     * @return
     */
    public static BufferedImage runSauvolaBinarization(BufferedImage image) {
        String base64Image = ImageEncoding.encodeToBase64(image);
        Map<String, Object> highlighter = new HashMap();

        JSONObject request = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        request.put("highlighter", high);
        request.put("inputs", inputs);
        request.put("image", base64Image);
        JSONObject result = executePost("http://divaservices.unifr.ch/imageanalysis/binarization/sauvola", request);
        String resImage = (String)result.get("image");
        return ImageEncoding.decodeBas64(resImage);
    }

    /**
     * @param image
     * @param rectangle
     * @return
     */
    public static List<Polygon> runSeamCarvingTextlineExtraction(BufferedImage image, Rectangle rectangle) {
        String base64Image = ImageEncoding.encodeToBase64(image);
        Map<String, Object> highlighter = new HashMap();
        highlighter.put("segments", prepareRectangle(rectangle));
        highlighter.put("closed", true);
        highlighter.put("type", "rectangle");

        JSONObject request = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        request.put("highlighter", high);
        request.put("inputs", inputs);
        request.put("image", base64Image);

        JSONObject result = executePost("http://divaservices.unifr.ch/segmentation/textline/seam", request);
        // now you have the string representation of the HTML request

        return extractPolygons(result);
    }

    /**
     * Executes a post request and returns the body json object
     *
     * @param url
     * @param payload
     * @return
     */
    private static JSONObject executePost(String url, JSONObject payload) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        try {
            StringEntity se = new StringEntity(payload.toString());
            se.setContentType("application/json");
            post.setEntity(se);
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                JSONObject result = new JSONObject(convertStreamToString(instream));
                instream.close();
                return result;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * converts a Rectangle into a List<int[]> for sending it as JSON
     * @param rectangle the rectangle
     * @return a List<int[]>
     */
    private static List<int[]> prepareRectangle(Rectangle rectangle){
        List<int[]> points = new ArrayList<>();
        points.add(new int[]{rectangle.x, rectangle.y});
        points.add(new int[]{rectangle.x + rectangle.width, rectangle.y});
        points.add(new int[]{rectangle.x + rectangle.width, rectangle.y + rectangle.height});
        points.add(new int[]{rectangle.x, rectangle.y + rectangle.height});
        return points;

    }

    /**
     * Creates polygons from polygons as "lines" in DivaServices
     * @param result the JSON result object from DIVAServices
     * @return a list of polygons
     */
    private static List<Polygon> extractPolygons(JSONObject result){
        JSONArray highlighters = result.getJSONArray("highlighters");
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < highlighters.length(); i++) {
            JSONObject line = highlighters.getJSONObject(i).getJSONObject("line");
            JSONArray segments = line.getJSONArray("segments");
            Polygon polygon = new Polygon();
            for (int j = 0; j < segments.length(); j++) {
                JSONArray coordinates = segments.getJSONArray(j);
                polygon.addPoint(coordinates.getInt(0), coordinates.getInt(1));
            }
            polygons.add(polygon);
        }
        return polygons;
    }

    /**
     * Creates a list of rectangles from "rectangles" returned from DIVAServices
     * @param result the JSONObject returned from DivaServices
     * @return A list of rectangles
     */
    private static List<Rectangle> extractRectangles(JSONObject result){
        JSONArray highlighters = result.getJSONArray("highlighters");
        List<Rectangle> rectangles = new ArrayList<>();
        for (int i = 0; i < highlighters.length(); i++) {
            JSONObject line = highlighters.getJSONObject(i).getJSONObject("rectangle");
            JSONArray segments = line.getJSONArray("segments");
            //get top left point
            JSONArray topLeft = segments.getJSONArray(0);
            JSONArray bottomRight = segments.getJSONArray(2);
            Rectangle rectangle = new Rectangle(topLeft.getInt(0),topLeft.getInt(1),bottomRight.getInt(0)-topLeft.getInt(0),bottomRight.getInt(1) - topLeft.getInt(1));
            rectangles.add(rectangle);
        }
        return rectangles;
    }

    private static List<Point> extractPoints(JSONObject result){
        JSONArray highlighters = result.getJSONArray("highlighters");
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < highlighters.length(); i++) {
            JSONObject line = highlighters.getJSONObject(i).getJSONObject("point");
            JSONArray position = line.getJSONArray("position");
            //get top left point
            points.add(new Point(position.getInt(0),position.getInt(1)));
        }
        return points;
    }

    /**
     * Converst an input stream to a json string
     *
     * @param is
     * @return
     */
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
