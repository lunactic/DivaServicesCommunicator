package ch.unifr.diva.request;

import ch.unifr.diva.ImageEncoding;

import java.awt.image.BufferedImage;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 22.11.2016.
 */
public class DivaImage {
    private BufferedImage image;

    public DivaImage(BufferedImage image){
        this.image = image;
    }

    public String getMd5Hash(){
        return ImageEncoding.encodeToMd5(image);
    }

}
