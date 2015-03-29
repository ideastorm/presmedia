package com.ideastormsoftware.presmedia;

import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Phillip
 */
public final class ImageUtils {

    public static BufferedImage copy(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        copy.getGraphics().drawImage(img, 0, 0, null);
        return copy;
    }

    public static BufferedImage convertToImage(Mat mat) {
        int bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            Mat rgbMat = new Mat();
            Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_BGR2RGB);
            bufferedImageType = BufferedImage.TYPE_3BYTE_BGR;
            mat = rgbMat;
        }
        byte[] b = new byte[mat.channels() * mat.cols() * mat.rows()];
        mat.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), bufferedImageType);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), b);
        return image;
    }

    public static Size aspectScaledSize(int sourceWidth, int sourceHeight, int destWidth, int destHeight) {
        double sourceRatio = 1.0 * sourceHeight / sourceWidth;
        double destRatio = 1.0 * destHeight / destWidth;
        if (destRatio > sourceRatio) //dest is narrower than source
        {
            return new Size(destWidth, destWidth * sourceRatio);
        } else {
            return new Size(destHeight / sourceRatio, destHeight);
        }
    }

    public static BufferedImage copyScaled(BufferedImage img, int width, int height) {
        BufferedImage copy = new BufferedImage(width, height, img.getType());
        copy.getGraphics().drawImage(img, 0, 0, width, height, null);
        return copy;
    }

    public static BufferedImage emptyImage() {
        return new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
    }

    private ImageUtils() {
    }

}
