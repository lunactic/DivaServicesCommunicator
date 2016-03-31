package ch.unifr.diva.request;

import java.awt.image.BufferedImage;
import java.util.List;
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

    public DivaServicesRequest(List<BufferedImage> images){
        this.images = Optional.of(images);
        collection = Optional.empty();
    }

    public DivaServicesRequest(String collection){
        this.collection = Optional.of(collection);
        this.images = Optional.empty();
    }

    public Optional<List<BufferedImage>> getImages(){
        return images;
    }
    public Optional<String> getCollection(){
        return collection;
    }

}
