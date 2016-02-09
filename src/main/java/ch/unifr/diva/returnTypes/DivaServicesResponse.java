package ch.unifr.diva.returnTypes;

import java.awt.image.BufferedImage;
import java.util.Map;

import static javafx.scene.input.KeyCode.H;
import static javafx.scene.input.KeyCode.M;
import static javafx.scene.input.KeyCode.T;

/**
 * Class encapsulating an image and output information from DivaServices
 *
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public class DivaServicesResponse<H> {

    /**
     * extracted image
     */
    private BufferedImage image;
    /**
     * extracted outputs
     */
    private Map<String, Object> output;
    /**
     * extracted highlighters
     */
    private AbstractHighlighter<H> highlighter;
    /**
     *
     * @param image the result image
     * @param output the contents of "output"
     * @param highlighter the extracted highlighter information
     */
    public DivaServicesResponse(BufferedImage image, Map<String, Object> output, AbstractHighlighter<H> highlighter){
        this.image = image;
        this.output = output;
        this.highlighter = highlighter;
    }

    public BufferedImage getImage(){
        return image;
    }

    public Map<String,Object> getOutput(){
        return output;
    }

    public AbstractHighlighter<H> getHighlighter(){
        return highlighter;
    }
}
