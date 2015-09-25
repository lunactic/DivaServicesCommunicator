package ch.unifr.diva;

import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by lunactic on 22.09.15.
 */
public class ImageEncoding {

    /**
     * Creates a Base64 encoding of an image
     *
     * @param image image
     * @return the image as base64 encoded string
     */
    public static String encodeToBase64(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeBase64String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeToMd5(BufferedImage image){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
            byte[] data = outputStream.toByteArray();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            byte[] hash = md.digest();
            System.out.println(Arrays.toString(hash));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static BufferedImage decodeBas64(String base64) {
        BASE64Decoder bd = new BASE64Decoder();
        try {
            byte[] buffer = bd.decodeBuffer(base64);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
