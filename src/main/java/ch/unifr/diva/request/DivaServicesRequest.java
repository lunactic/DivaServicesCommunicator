package ch.unifr.diva.request;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 30.03.2016.
 */
public class DivaServicesRequest {
    private Optional<List<BufferedImage>> images;
    private Optional<String> collection;
    private Map<String, String> data;

    public DivaServicesRequest(){
        data = new HashMap<>();
    }

    public DivaServicesRequest(List<BufferedImage> images) {
        this.images = Optional.of(images);
        collection = Optional.empty();
        data = new HashMap<>();
    }

    public DivaServicesRequest(String collection) {
        this.collection = Optional.of(collection);
        this.images = Optional.empty();
        data = new HashMap<>();
    }

    public Optional<List<BufferedImage>> getImages() {
        return images;
    }

    public Optional<String> getCollection() {
        return collection;
    }

    public void addDataValue(String key, String value){
        data.put(key,value);
    }

    public Map<String, String> getData(){
        return data;
    }
}
