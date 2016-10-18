package ch.unifr.diva.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 30.03.2016.
 */
public class DivaServicesRequest {
    private Optional<DivaCollection> collection;
    private Map<String, String> data;

    public DivaServicesRequest() {
        data = new HashMap<>();
    }

    public DivaServicesRequest(DivaCollection collection) {
        this.collection = Optional.of(collection);
        data = new HashMap<>();
    }


    public Optional<DivaCollection> getCollection() {
        return collection;
    }

    public void addDataValue(String key, String value) {
        data.put(key, value);
    }

    public Map<String, String> getData() {
        return data;
    }
}
