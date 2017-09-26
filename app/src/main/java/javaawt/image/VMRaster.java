package javaawt.image;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

import android.support.v4.graphics.BitmapCompat;

public class VMRaster implements Raster
{
	protected android.graphics.Bitmap delegate = null;

	public VMRaster(android.graphics.Bitmap delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public android.graphics.Bitmap getDelegate()
	{
		return delegate;
	}

	@Override
	public void getDataElements(int w, int h, Object pixel)
	{
		throw new UnsupportedOperationException();//delegate.getDataElements(w, h, pixel);
	}

	@Override
	public int getNumDataElements()
	{
		throw new UnsupportedOperationException();//	return delegate.getNumDataElements();
	}

	@Override
	public int getTransferType()
	{
		throw new UnsupportedOperationException();//	return delegate.getTransferType();
	}

	@Override
	public DataBuffer getDataBuffer()
	{		

		IntBuffer buffer1 = IntBuffer.allocate(BitmapCompat.getAllocationByteCount(delegate) / 4);
		delegate.copyPixelsToBuffer(buffer1);

		VMDataBufferInt ret = new VMDataBufferInt(buffer1);
		return ret;

	}
}
