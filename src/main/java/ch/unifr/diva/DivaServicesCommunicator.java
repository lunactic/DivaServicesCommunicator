package ch.unifr.diva;

import ch.unifr.diva.exceptions.CollectionException;
import ch.unifr.diva.request.DivaCollection;
import ch.unifr.diva.request.DivaServicesRequest;
import ch.unifr.diva.returnTypes.DivaServicesResponse;
import ch.unifr.diva.returnTypes.PointHighlighter;
import ch.unifr.diva.returnTypes.PolygonHighlighter;
import ch.unifr.diva.returnTypes.RectangleHighlighter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

@SuppressWarnings({"unchecked", "WeakerAccess", "JavaDoc"})
public class DivaServicesCommunicator {


    private DivaServicesConnection connection;

    /**
     * Initialize a new DivaServicesCommunicator class
     *
     * @param connection The connection parameters to use
     */
    public DivaServicesCommunicator(DivaServicesConnection connection) {
        this.connection = connection;
    }

    public String uploadZip(String path) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                baos.write(buf, 0, readNum);
            }
            byte[] bytes = baos.toByteArray();
            String base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
            Map<String, Object> highlighter = new HashMap();

            JSONObject request = new JSONObject();
            JSONObject high = new JSONObject(highlighter);
            JSONObject inputs = new JSONObject();
            request.put("highlighter", high);
            request.put("inputs", inputs);
            request.put("zip", base64);
            JSONObject result = HttpRequest.executePost(connection.getServerUrl() + "/upload", request);
            return result.getString("collection");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public DivaServicesResponse<Object> runOcropyTextExtraction(DivaServicesRequest request) {
        Map<String, Object> highlighter = new HashMap();
        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        inputs.put("languageModel", "English");
        jsonRequest.put("highlighter", high);
        jsonRequest.put("inputs", inputs);
        processDivaRequest(request, jsonRequest);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ocropy/recognize", jsonRequest);
        List<Map> outputs = new LinkedList<>();
        for (int i = 0; i < postResult.getJSONArray("results").length(); i++) {
            JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), i);
            List<Map> output = extractOutput(result.getJSONArray("output"));
            outputs.add(output.get(0));
        }
        return new DivaServicesResponse<>(null, outputs, null);
    }


    /**
     * Extracts interest points from an image
     *
     * @param request             the request containing image/collection information
     * @param detector            the detector to use
     * @param blurSigma           the amount of blur applied
     * @param numScales           the number of scales
     * @param numOctaves          the number of octaves
     * @param threshold           the threshold
     * @param maxFeaturesPerScale the maximal number of features to use
     * @return A list of points, representing the interest points
     */
    public DivaServicesResponse<Point> runMultiScaleInterestPointDetection(DivaServicesRequest request, String detector, float blurSigma, int numScales, int numOctaves, float threshold, int maxFeaturesPerScale) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(10);

        JSONObject jsonRequest = new JSONObject();
        JSONObject inputs = new JSONObject();
        inputs.put("detector", detector);
        inputs.put("blurSigma", blurSigma);
        inputs.put("numScales", numScales);
        inputs.put("numOctaves", numOctaves);
        inputs.put("threshold", df.format(threshold));
        inputs.put("maxFeaturesPerScale", maxFeaturesPerScale);

        jsonRequest.put("inputs", inputs);
        processDivaRequest(request, jsonRequest);
        //System.out.println(request.toString());
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ipd/multiscaleinterestpointdetector/2", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        return new DivaServicesResponse<>(null, null, extractPoints(result));
    }

    /**
     * @param request the request containing image/collection information
     * @return
     */
    public DivaServicesResponse<Object> runOtsuBinarization(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/binarization/otsubinarization/1", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        String imageUrl = extractVisualizationImage(result);
        if(imageUrl != null) {
            BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);
            return new DivaServicesResponse<>(outputImage, null, null);
        }else{
            return new DivaServicesResponse<>(null, null,null);
        }
    }

    /**
     * @param request   the request containing image/collection information
     * @param rectangle
     * @return A list of polygons, each representing a text line
     */
    public DivaServicesResponse<Rectangle> runHistogramTextLineExtraction(DivaServicesRequest request, Rectangle rectangle) {
        Map<String, Object> highlighter = new HashMap();
        highlighter.put("segments", prepareRectangle(rectangle));
        highlighter.put("closed", true);
        highlighter.put("type", "rectangle");

        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        inputs.put("highlighter", high);
        jsonRequest.put("inputs", inputs);

        processDivaRequest(request, jsonRequest);

        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/textline/histogramtextlinesegmentation/1", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        return new DivaServicesResponse<>(null, null, extractRectangles(result));
    }

    /**
     * @param request the request containing image/collection information
     * @return
     */
    public DivaServicesResponse<Object> runSauvolaBinarization(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/binarization/sauvolabinarization/1", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        String imageUrl = extractVisualizationImage(result);

        if (imageUrl != null) {
            BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);
            return new DivaServicesResponse<>(outputImage, null, null);
        } else {
            return new DivaServicesResponse<>(null, null, null);
        }
    }

    public DivaServicesResponse<Object> trainOcrLanguageModel(DivaServicesRequest request, String modelName, int trainingIterations, int saveIteration, boolean requireOutputImage) {
        JSONObject inputs = new JSONObject();
        inputs.put("modelName", modelName);
        inputs.put("trainingIterations", trainingIterations);
        inputs.put("saveIteration", saveIteration);
        JSONObject jsonRequest = new JSONObject();
        addDataToRequest(request.getData(), jsonRequest);
        jsonRequest.put("inputs", inputs);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ocropy/train", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);

        return new DivaServicesResponse<>(null, extractOutput(result.getJSONArray("output")), null);

    }

    public DivaServicesResponse<Object> runImageInverting(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject result = HttpRequest.executePost(connection.getServerUrl() + "/imageanalysis/binarization/invert", jsonRequest);
        String resImage = (String) (result != null ? result.get("image") : null);
        return new DivaServicesResponse<>(ImageEncoding.decodeBase64(resImage), null, null);
    }

    /**
     * @param request   the request containing image/collection information
     * @param rectangle
     * @return
     */
    public DivaServicesResponse<Polygon> runSeamCarvingTextlineExtraction(DivaServicesRequest request, Rectangle rectangle, float smooth, float sigma, int slices, boolean requireOutputImage) {
        Map<String, Object> highlighter = new HashMap();
        highlighter.put("segments", prepareRectangle(rectangle));
        highlighter.put("closed", true);
        highlighter.put("type", "rectangle");

        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        inputs.put("smooth", smooth);
        inputs.put("slices", slices);
        inputs.put("sigma", sigma);
        jsonRequest.put("highlighter", high);
        jsonRequest.put("inputs", inputs);
        jsonRequest.put("requireOutputImage", requireOutputImage);
        processDivaRequest(request, jsonRequest);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/segmentation/textline/seam", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        PolygonHighlighter polygons = extractPolygons(result);
        return new DivaServicesResponse<>(null, null, polygons);
    }

    /**
     * run a canny edge detection algorithm
     *
     * @param request the request containing image/collection information
     * @return result image
     */
    public DivaServicesResponse<Object> runCannyEdgeDetection(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/edges/cannyedgedetection/1", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        String imageUrl = extractVisualizationImage(result);
        if(imageUrl != null) {
            BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);
            return new DivaServicesResponse<>(outputImage, null, null);
        }else
        {
            return new DivaServicesResponse<>(null,null,null);
        }
    }

    /**
     * run simple histogram enhancement
     *
     * @param request the request containing image/collection information
     * @return output image
     */
    public DivaServicesResponse<Object> runHistogramEnhancement(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/imageanalysis/enhancement/histogram", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        String imageUrl = (String) result.get("outputImage");
        BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);
        return new DivaServicesResponse<>(outputImage, null, null);
    }

    /**
     * Image enhancement using Laplacian Sharpening
     *
     * @param request    the request containing image/collection information
     * @param sharpLevel sharpening Level (4 or 8)
     * @return output image
     */
    public DivaServicesResponse<Object> runLaplacianSharpening(DivaServicesRequest request, int sharpLevel, boolean requireOutputImage) {
        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject();
        JSONObject inputs = new JSONObject();
        inputs.put("sharpLevel", sharpLevel);
        jsonRequest.put("highlighter", high);
        jsonRequest.put("inputs", inputs);
        jsonRequest.put("requireOutputImage", requireOutputImage);
        processDivaRequest(request, jsonRequest);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/imageanalysis/enhancement/sharpen", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        String imageUrl = (String) result.get("outputImage");
        BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);
        return new DivaServicesResponse<>(outputImage, null, null);
    }

    /**
     * runs the binarization algorithm of ocropy (https://github.com/tmbdev/ocropy)
     *
     * @param request the request containing image/collection information
     * @return the binarized output image
     */
    public DivaServicesResponse<Object> runOcropyBinarization(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ocropy/binarization", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        String imageUrl = (String) result.get("outputImage");
        BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);
        return new DivaServicesResponse<>(outputImage, null, null);
    }

    /**
     * run the page segmentation algorithm of ocropy (https://github.com/tmbdev/ocropy)
     *
     * @param request the request containing image / collection info
     */
    public DivaServicesResponse<Object> runOcropyPageSegmentation(DivaServicesRequest request, boolean requireOutputImage) {
        JSONObject jsonRequest = createImagesOnlyRequest(request, requireOutputImage);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ocropy/pageseg", jsonRequest);
        JSONObject result = HttpRequest.getResult(postResult, connection.getCheckInterval(), 0);
        //extract output
        List<Map> output = extractOutput(result.getJSONArray("output"));
        //extract image
        String imageUrl = (String) result.get("outputImage");
        BufferedImage outputImage = ImageEncoding.getImageFromUrl(imageUrl);

        return new DivaServicesResponse(outputImage, output, null);
    }

    public BufferedImage downloadImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new collection. This method will block until all the images are transferred and stored on the server.
     *
     * @param images
     * @return The name of the created collection
     */

    public DivaCollection createCollection(List<BufferedImage> images) {
        DivaCollection collection = DivaCollection.createCollectionWithImages(images, connection);
        return collection;
    }

    public DivaCollection createCollection(String name) throws CollectionException {
        DivaCollection collection = DivaCollection.createCollectionByName(name, connection);
        return collection;
    }


    /**
     * extracts variable output from the "output" field into a Map for easier processing
     *
     * @param output
     * @return
     */
    private List<Map> extractOutput(JSONArray output) {
        Gson gson = new Gson();
        List<Map> list = new LinkedList<>();
        for (int i = 0; i < output.length(); i++) {
            JSONObject object = output.getJSONObject(i);
            String type = (String) object.keySet().toArray()[0];
            JSONObject content = object.getJSONObject(type);
            Type t = new TypeToken<TreeMap<String, Object>>() {
            }.getType();
            Map myMap = gson.fromJson(content.toString(), t);
            myMap.put("type", type);
            list.add(myMap);
        }


        return list;
    }

    private void processDivaRequest(DivaServicesRequest request, JSONObject jsonRequest) {
        if (request.getCollection().isPresent()) {
            addCollectionToRequest(request.getCollection().get().getName(), jsonRequest);
        }
        //TODO: Add error handling
    }

    /**
     * Creates the JSON payload for a request that requires only an image and no parameters
     *
     * @param request the request containing image or collection info
     * @return the JSON object to be sent to the server
     */
    private JSONObject createImagesOnlyRequest(DivaServicesRequest request, boolean requireOutputImage) {
        Map<String, Object> highlighter = new HashMap();
        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        jsonRequest.put("highlighter", high);
        jsonRequest.put("inputs", inputs);
        jsonRequest.put("requireOutputImage", requireOutputImage);
        processDivaRequest(request, jsonRequest);
        return jsonRequest;
    }

    /**
     * converts a Rectangle into a List<int[]> for sending it as JSON
     *
     * @param rectangle the rectangle
     * @return a List<int[]>
     */
    private List<int[]> prepareRectangle(Rectangle rectangle) {
        List<int[]> points = new ArrayList<>();
        //top left
        points.add(new int[]{rectangle.x, rectangle.y});
        //bottom left
        points.add(new int[]{rectangle.x, rectangle.y + rectangle.height});
        //bottom right
        points.add(new int[]{rectangle.x + rectangle.width, rectangle.y + rectangle.height});
        //top right
        points.add(new int[]{rectangle.x + rectangle.width, rectangle.y});
        return points;

    }

    /**
     * Creates polygons from polygons as "lines" in DivaServices
     *
     * @param result the JSON result object from DIVAServices
     * @return a list of polygons
     */
    private PolygonHighlighter extractPolygons(JSONObject result) {
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
        return new PolygonHighlighter(polygons);
    }

    /**
     * Creates a list of rectangles from "rectangles" returned from DIVAServices
     *
     * @param result the JSONObject returned from DivaServices
     * @return A list of rectangles
     */
    private RectangleHighlighter extractRectangles(JSONObject result) {
        JSONArray highlighters = result.getJSONArray("output");
        List<Rectangle> rectangles = new ArrayList<>();

        for (int i = 0; i < highlighters.length(); i++) {
            if (highlighters.getJSONObject(i).has("array")) {
                JSONArray values = highlighters.getJSONObject(i).getJSONObject("array").getJSONArray("values");
                JSONArray topLeft = values.getJSONArray(0);
                JSONArray bottomRight = values.getJSONArray(2);
                Rectangle rectangle = new Rectangle(topLeft.getInt(0), topLeft.getInt(1), bottomRight.getInt(0) - topLeft.getInt(0), bottomRight.getInt(1) - topLeft.getInt(1));
                rectangles.add(rectangle);

            }
        }

        return new RectangleHighlighter(rectangles);
    }

    private PointHighlighter extractPoints(JSONObject result) {
        JSONArray output = result.getJSONArray("output");
        List<Point> points = new ArrayList<>();
        for(int i = 0; i< output.length(); i++){
            JSONObject jsonObject = output.getJSONObject(i);
            if(jsonObject.has("highlighter")){
                JSONArray highlighter = output.getJSONObject(i).getJSONArray("highlighter");
                for(int j = 0; j < highlighter.length(); j++){
                    JSONArray position = highlighter.getJSONObject(i).getJSONObject("point").getJSONArray("position");
                    points.add(new Point(position.getInt(0), position.getInt(1)));
                }
            }
        }
        return new PointHighlighter(points);
    }

    /**
     * <p>
     * Checks if an image is available on the server
     *
     * @param image The image
     * @return True if the image is already saved on the server, false otherwise
     */
    private boolean checkImageOnServer(BufferedImage image) {
        String md5 = ImageEncoding.encodeToMd5(image);
        return checkImageOnServer(md5);
    }

    /**
     * Checks if an image is available on the server based on an md5 String
     *
     * @param md5 The md5 representation of the image
     * @return True if the image is already saved on the server, false otherwise
     */
    private boolean checkImageOnServer(String md5) {
        String url = connection.getServerUrl() + "/image/check/" + md5;
        JSONObject response = HttpRequest.executeGet(url);
        return response.getBoolean("imageAvailable");
    }

    private void addCollectionToRequest(String collection, JSONObject jsonRequest) {
        JSONObject collectionObj = new JSONObject();
        collectionObj.put("type", "collection");
        collectionObj.put("value", collection);
        JSONArray images = new JSONArray();
        JSONObject jsonCollection = new JSONObject();
        for (String key : collectionObj.keySet()) {
            jsonCollection.put(key, collectionObj.getString(key));
        }
        images.put(jsonCollection);
        jsonRequest.put("images", images);
    }

    private void addDataToRequest(Map<String, String> data, JSONObject request) {
        JSONObject dataObj = new JSONObject();
        for (String key : data.keySet()) {
            dataObj.put(key, data.get(key));
        }
        request.put("data", dataObj);
    }

    private String extractVisualizationImage(JSONObject result) {
        String imageUrl = null;
        for (int i = 0; i < result.getJSONArray("output").length(); i++) {
            JSONObject jsonObject = result.getJSONArray("output").getJSONObject(i);
            if (jsonObject.has("file") && jsonObject.getJSONObject("file").has("options") && jsonObject.getJSONObject("file").getJSONObject("options").getBoolean("visualization")) {
                imageUrl = jsonObject.getJSONObject("file").getString("url");
            }
        }
        return imageUrl;
    }

    private void logJsonObject(JSONObject object) {
        System.out.println(object.toString());
    }


}
