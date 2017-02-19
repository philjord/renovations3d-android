package org.jogamp.java3d;

import javaawt.image.BufferedImage;


public class ImageComponent2DBitmap extends ImageComponent2D
{
	/**
	 * Note there is no by ref and yup as this is only for offscreen!
	 * @param format
	 * @param image
	 */
	public ImageComponent2DBitmap(int format, BufferedImage image) {
		super(format,image);
	}

	void createRetained() {
		this.retained = new ImageComponent2DBitmapRetained();
		this.retained.setSource(this);
	}
}