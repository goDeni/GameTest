package com.company;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageManager {
    public static Image biggerImage(Image image, int size){
        return biggerImage(image, image.getWidth(null)*size, image.getHeight(null)*size);
    }
    public static Image biggerImage(Image image, int width, int height){
        return toBufferedImage(image).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
