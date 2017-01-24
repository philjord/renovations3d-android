package org.jogamp.java3d;

import javaawt.image.BufferedImage;

public class ImageComponent2DBitmap extends ImageComponent2D
{
	public ImageComponent2DBitmap(int format, BufferedImage image) {
		super(format,image);
	}

	void createRetained() {
		this.retained = new ImageComponent2DBitmapRetained();
		this.retained.setSource(this);
	}
}