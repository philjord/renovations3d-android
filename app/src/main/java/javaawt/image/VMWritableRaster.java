package javaawt.image;

import java.nio.IntBuffer;

import android.support.v4.graphics.BitmapCompat;

import javaawt.image.VMRaster;
import javaawt.image.WritableRaster;

public class VMWritableRaster extends VMRaster implements WritableRaster
{	
	public VMWritableRaster(android.graphics.Bitmap delegate)
	{
		super(delegate);

	}

	@Override
	public android.graphics.Bitmap getDelegate()
	{
		return delegate;
	}

	@Override
	public int[] getDataElements(int i, int j, int width, int height, Object object)
	{
		IntBuffer buffer1 = IntBuffer.allocate(BitmapCompat.getAllocationByteCount(delegate) / 4);
		delegate.copyPixelsToBuffer(buffer1);
		int[] data = buffer1.array();			
		return data;
	}

}
