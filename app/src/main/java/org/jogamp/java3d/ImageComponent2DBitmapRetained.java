package org.jogamp.java3d;

import android.graphics.Bitmap;

import javaawt.image.RenderedImage;
import javaawt.image.VMBufferedImage;

/**
 * Created by phil on 12/29/2016.
 */
public class ImageComponent2DBitmapRetained extends ImageComponent2DRetained
{

	RenderedImage getImage()
	{
		//return (RenderedImage) (this.isByReference() ? (RenderedImage) this.getRefImage(0) : (this.imageData != null ? this.imageData.createBufferedImage(0) : null));
		//so at this point we have all our lovely data is in the ImageData class in the int[] and we want to smash out a bitmap/bufferedimage from it


		if (!this.isByReference())
		{
			int[] srcdata = imageData.getAsIntArray();

			//WAIT! the copyByLine business swaps things to y down or whatever that weirdness is!!!

			int[] upsidedowndata = new int[srcdata.length];
			int w = this.getWidth();
			int lineCount = this.getHeight();
			for (int l = 0; l < lineCount; l++)
				System.arraycopy(srcdata, l * w, upsidedowndata, srcdata.length - ((l * w) + w), w);


			Bitmap bm = Bitmap.createBitmap(upsidedowndata, this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
			return new VMBufferedImage(bm);
		}
		else
		{
			throw new UnsupportedOperationException("Bitmap image component is for offscreen buffer only and must not be by ref");
		}

	}
}
