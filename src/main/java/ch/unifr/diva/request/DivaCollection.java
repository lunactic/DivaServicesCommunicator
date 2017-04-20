package ch.unifr.diva.request;

import ch.unifr.diva.DivaServicesConnection;
import ch.unifr.diva.HttpRequest;
import ch.unifr.diva.ImageEncoding;
import ch.unifr.diva.exceptions.CollectionException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 18.10.2016.
 */
public class DivaCollection {
    private String name;

    private DivaCollection(String name) {
        this.name = name;
    }


    public static DivaCollection createCollectionWithImages(List<BufferedImage> images, DivaServicesConnection connection) {
        JSONObject request = new JSONObject();
        JSONArray jsonImages = new JSONArray();
        int i = 0;
        for (BufferedImage image : images) {
            JSONObject jsonImage = new JSONObject();
            jsonImage.put("type", "image");
            jsonImage.put("value", ImageEncoding.encodeToBase64(image));
            jsonImage.put("name", String.valueOf(i));
            jsonImages.put(jsonImage);
            i++;
        }
        request.put("files", jsonImages);
        JSONObject response = HttpRequest.executePost(connection.getServerUrl() + "/upload", request);
        String collection = response.getString("collection");
        String url = connection.getServerUrl() + "/collections/" + collection;
        JSONObject getResponse = HttpRequest.executeGet(url);
        while (!(getResponse.getInt("percentage") == 100)) {
            try {
                Thread.sleep(connection.getCheckInterval() * 1000);
                getResponse = HttpRequest.executeGet(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new DivaCollection(collection);
    }

    public static DivaCollection createCollectionByName(String name, DivaServicesConnection connection) throws CollectionException {
        JSONObject response = HttpRequest.executeGet(connection.getServerUrl() + "/collections/" + name);
        if (response.getInt("statusCode") == 200) {
            return new DivaCollection(name);
        } else {
            throw new CollectionException("Collection: " + name + " does not exists on the remote system");
        }
    }

    public String getName() {
        return name;
    }
}
