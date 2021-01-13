package sig.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class ImageUtils {
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toCompatibleImage(BufferedImage image)
	{
	    return image; 
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
	public static BufferedImage copyBufferedImage(BufferedImage img)
	{
	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	public static BufferedImage cropImage(BufferedImage img,Rectangle offset)
	{
	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(offset.width, offset.height, BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, -offset.x, -offset.y, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	public static BufferedImage copyBufferedImages(BufferedImage...img)
	{
		int widthSum=0;
		int maxHeight=0;
		
		for (int i=0;i<img.length;i++) {
			widthSum+=img[i].getWidth();
			maxHeight=Math.max(maxHeight, img[i].getHeight());
		}
		
	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(widthSum, maxHeight, BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    int X=0;
	    for (int i=0;i<img.length;i++) {
	    	bGr.drawImage(img[i], X, 0, null);
	    	X+=img[i].getWidth();
	    }
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	public static double distanceToColor(Color p2, Color p1) {
		return Math.sqrt(Math.pow(p2.getRed()-p1.getRed(), 2)+Math.pow(p2.getGreen()-p1.getGreen(), 2)+Math.pow(p2.getBlue()-p1.getBlue(), 2));
	}
}
