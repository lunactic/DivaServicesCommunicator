package ch.unifr.diva;

import ch.unifr.diva.exceptions.CollectionException;
import ch.unifr.diva.exceptions.MethodNotAvailableException;
import ch.unifr.diva.request.DivaCollection;
import ch.unifr.diva.request.DivaImage;
import ch.unifr.diva.request.DivaServicesRequest;
import ch.unifr.diva.returnTypes.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

@SuppressWarnings({"unchecked", "WeakerAccess", "JavaDoc"})
public class DivaServicesCommunicator {


    private DivaServicesConnection connection;
    private Properties properties;

    /**
     * Initialize a new DivaServicesCommunicator class
     *
     * @param connection The connection parameters to use
     */
    public DivaServicesCommunicator(DivaServicesConnection connection) {
        this.connection = connection;
        properties = new Properties();
        try {
            String resourceName = "services.properties";
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream resourceStream = loader.getResourceAsStream(resourceName);
            properties.load(resourceStream);
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            request.put("parameters", inputs);
            request.put("zip", base64);
            JSONObject result = HttpRequest.executePost(connection.getServerUrl() + "/upload", request);
            return result.getString("collection");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public DivaServicesResponse<Object> runOcropyTextExtraction(DivaServicesRequest request) throws MethodNotAvailableException {
        Map<String, Object> highlighter = new HashMap();
        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        inputs.put("languageModel", "English");
        jsonRequest.put("highlighter", high);
        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ocropy/recognize", jsonRequest);
        List<List<Map>> outputs = new LinkedList<>();
        for (int i = 0; i < postResult.getJSONArray("results").length(); i++) {
            List<JSONObject> results = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
            for (JSONObject result : results) {
                List<Map> output = extractOutput(result.getJSONArray("output"));
                outputs.add(output);
            }
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
    public DivaServicesResponse<Point> runMultiScaleInterestPointDetection(DivaServicesRequest request, String detector, float blurSigma, int numScales, int numOctaves, float threshold, int maxFeaturesPerScale) throws MethodNotAvailableException {
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

        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        //System.out.println(request.toString());
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("multiScaleIpd"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        return new DivaServicesResponse<>(null, null, extractPoints(result));
    }

    /**
     * @param request the request containing image/collection information
     * @return
     */
    public DivaServicesResponse<Object> runOtsuBinarization(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("otsuBinarization"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /**
     * @param request   the request containing image/collection information
     * @param rectangle
     * @return A list of polygons, each representing a text line
     */
    public DivaServicesResponse<Rectangle> runHistogramTextLineExtraction(DivaServicesRequest request, Rectangle rectangle) throws MethodNotAvailableException {
        Map<String, Object> highlighter = new HashMap();
        highlighter.put("segments", prepareRectangle(rectangle));
        highlighter.put("closed", true);
        highlighter.put("type", "rectangle");

        JSONObject jsonRequest = new JSONObject();
        JSONObject high = new JSONObject(highlighter);
        JSONObject inputs = new JSONObject();
        inputs.put("highlighter", high);
        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("histogramTextLineExtraction"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        return new DivaServicesResponse<>(null, null, extractRectangles(result));
    }

    /**
     * @param request the request containing image/collection information
     * @return
     */
    public DivaServicesResponse<Object> runSauvolaBinarization(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("sauvolaBinarization"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /*
    public DivaServicesResponse<Object> trainOcrLanguageModel(DivaServicesRequest request, String modelName, int trainingIterations, int saveIteration, boolean requireOutputImage) throws MethodNotAvailableException {
        JSONObject inputs = new JSONObject();
        inputs.put("modelName", modelName);
        inputs.put("trainingIterations", trainingIterations);
        inputs.put("saveIteration", saveIteration);
        JSONObject jsonRequest = new JSONObject();
        addDataToRequest(request.getData(), jsonRequest);
        jsonRequest.put("parameters", inputs);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + "/ocropy/train", jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        return new DivaServicesResponse<>(null, extractOutput(result.get(0).getJSONArray("output")), null);
    }
    */

    public DivaServicesResponse<Object> runImageInverting(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("imageInversion"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /**
     * @param request   the request containing image/collection information
     * @param rectangle
     * @return
     */
    public DivaServicesResponse<Polygon> runSeamCarvingTextlineExtraction(DivaServicesRequest request, Rectangle rectangle, float smooth, float sigma, int slices) throws MethodNotAvailableException {
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
        inputs.put("highlighter", high);
        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("seamCarvingTextLineExtraction"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<AbstractHighlighter<Polygon>> polygons = extractPolygons(result);
        return new DivaServicesResponse<>(null, null, polygons);
    }

    /**
     * run a canny edge detection algorithm
     *
     * @param request the request containing image/collection information
     * @return result image
     */
    public DivaServicesResponse<Object> runCannyEdgeDetection(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("cannyEdgeDetection"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /**
     * run simple histogram enhancement
     *
     * @param request the request containing image/collection information
     * @return output image
     */
    public DivaServicesResponse<Object> runHistogramEnhancement(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("histogramEnhancement"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /**
     * Image enhancement using Laplacian Sharpening
     *
     * @param request    the request containing image/collection information
     * @param sharpLevel sharpening Level (4 or 8)
     * @return output image
     */
    public DivaServicesResponse<Object> runLaplacianSharpening(DivaServicesRequest request, int sharpLevel) throws MethodNotAvailableException {
        JSONObject jsonRequest = new JSONObject();
        JSONObject inputs = new JSONObject();
        inputs.put("sharpLevel", sharpLevel);
        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("laplacianSharpening"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /**
     * runs the binarization algorithm of ocropy (https://github.com/tmbdev/ocropy)
     *
     * @param request the request containing image/collection information
     * @return the binarized output image
     */
    public DivaServicesResponse<Object> runOcropyBinarization(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("ocroBinarization"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);

    }

    public DivaServicesResponse<Object> runKrakenBinarization(DivaServicesRequest request) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        logJsonObject(jsonRequest);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("krakenBinarization"), jsonRequest);
        logJsonObject(postResult);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        for (JSONObject resultObject : result) {
            logJsonObject(resultObject);
        }

        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    public DivaServicesResponse<Object> runDecolorization(DivaServicesRequest request, float effect, float noise) throws MethodNotAvailableException {
        JSONObject jsonRequest = new JSONObject();
        JSONObject inputs = new JSONObject();
        inputs.put("effect", effect);
        inputs.put("noise", noise);
        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("decolorization"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
    }

    /**
     * run the page segmentation algorithm of ocropy (https://github.com/tmbdev/ocropy)
     *
     * @param request the request containing image / collection info
     */
    public DivaServicesResponse<Object> runOcropyPageSegmentation(DivaServicesRequest request, boolean requireOutputImage) throws MethodNotAvailableException {
        JSONObject jsonRequest = createImagesOnlyRequest(request);
        JSONObject postResult = HttpRequest.executePost(connection.getServerUrl() + properties.getProperty("ocroPageSeg"), jsonRequest);
        List<JSONObject> result = HttpRequest.getResult(postResult, connection.getCheckInterval(), request);
        //extract output
        List<Map> output = extractOutput(result.get(0).getJSONArray("output"));
        //extract image
        List<String> imageUrls = extractVisualizationImages(result);
        List<BufferedImage> outputImages = new LinkedList<>();
        for (String imageUrl : imageUrls) {
            outputImages.add(ImageEncoding.getImageFromUrl(imageUrl));
        }
        return new DivaServicesResponse<>(outputImages, null, null);
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

    public DivaImage createImage(BufferedImage image) {
        return new DivaImage(image);
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
     * @param results the JSON result object from DIVAServices
     * @return a list of polygons
     */
    private List<AbstractHighlighter<Polygon>> extractPolygons(List<JSONObject> results) {
        List<AbstractHighlighter<Polygon>> highlighters = new LinkedList<>();
        for (JSONObject result : results) {
            JSONArray outputs = result.getJSONArray("output");
            List<Polygon> polygons = new ArrayList<>();
            for (int i = 0; i < outputs.length(); i++) {
                if (outputs.getJSONObject(i).has("array")) {
                    JSONArray values = outputs.getJSONObject(i).getJSONObject("array").getJSONArray("values");
                    Polygon polygon = new Polygon();
                    for (int j = 0; j < values.length(); j++) {
                        JSONArray coordinates = values.getJSONArray(j);
                        polygon.addPoint(coordinates.getInt(0), coordinates.getInt(1));
                    }
                    polygons.add(polygon);
                }
            }
            highlighters.add(new PolygonHighlighter(polygons));
        }
        return highlighters;
    }

    /**
     * Creates a list of rectangles from "rectangles" returned from DIVAServices
     *
     * @param results the JSONObjects returned from DivaServices
     * @return A list of rectangles
     */
    private List<AbstractHighlighter<Rectangle>> extractRectangles(List<JSONObject> results) {
        List<AbstractHighlighter<Rectangle>> extractedRectangles = new LinkedList<>();
        for (JSONObject result : results) {
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
            extractedRectangles.add(new RectangleHighlighter(rectangles));
        }
        return extractedRectangles;
    }

    private List<AbstractHighlighter<Point>> extractPoints(List<JSONObject> results) {
        List<AbstractHighlighter<Point>> extractedPoints = new LinkedList<>();
        for (JSONObject result : results) {
            JSONArray output = result.getJSONArray("output");
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < output.length(); i++) {
                JSONObject jsonObject = output.getJSONObject(i);
                if (jsonObject.has("highlighter")) {
                    JSONArray highlighter = output.getJSONObject(i).getJSONArray("highlighter");
                    for (int j = 0; j < highlighter.length(); j++) {
                        JSONArray position = highlighter.getJSONObject(i).getJSONObject("point").getJSONArray("position");
                        points.add(new Point(position.getInt(0), position.getInt(1)));
                    }
                }
            }
            extractedPoints.add(new PointHighlighter(points));
        }
        return extractedPoints;
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

    private void addImageToRequest(String md5Hash, JSONObject jsonRequest) {
        JSONObject imageObject = new JSONObject();
        imageObject.put("type", "image");
        imageObject.put("value", md5Hash);
        JSONArray images = new JSONArray();
        JSONObject jsonImage = new JSONObject();
        for (String key : imageObject.keySet()) {
            jsonImage.put(key, imageObject.getString(key));
        }
        images.put(jsonImage);
        jsonRequest.put("images", images);
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
        jsonRequest.put("data", images);
    }

    private void addDataToRequest(Map<String, String> data, JSONObject request) {
        JSONObject dataObj = new JSONObject();
        for (String key : data.keySet()) {
            dataObj.put(key, data.get(key));
        }
        request.put("data", dataObj);
    }

    private List<String> extractVisualizationImages(List<JSONObject> results) {
        List<String> imageUrl = new LinkedList<>();
        for (JSONObject result : results) {
            for (int i = 0; i < result.getJSONArray("output").length(); i++) {
                JSONObject jsonObject = result.getJSONArray("output").getJSONObject(i);
                if (jsonObject.has("file") && jsonObject.getJSONObject("file").has("options") && jsonObject.getJSONObject("file").getJSONObject("options").getBoolean("visualization")) {
                    imageUrl.add(jsonObject.getJSONObject("file").getString("url"));
                }
            }
        }
        return imageUrl;
    }

    /**
     * Creates the JSON payload for a request that requires only an image and no parameters
     *
     * @param request the request containing image or collection info
     * @return the JSON object to be sent to the server
     */
    private JSONObject createImagesOnlyRequest(DivaServicesRequest request) {
        Map<String, Object> highlighter = new HashMap();
        JSONObject jsonRequest = new JSONObject();
        JSONObject inputs = new JSONObject();
        jsonRequest.put("parameters", inputs);
        JSONArray data = new JSONArray();
        JSONObject dataObject = new JSONObject();
        dataObject.put("inputImage", request.getCollection().get().getName() + "/*");
        data.put(dataObject);
        jsonRequest.put("data", data);
        return jsonRequest;
    }


    private void logJsonObject(JSONObject object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(object.toString());
        System.out.println(gson.toJson(je));
    }


}
