package org.jogamp.java3d.utils.image;

import android.graphics.Bitmap;

import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.NioImageBuffer;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Texture2D;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javaawt.image.BufferedImage;


// Copied from java3d-utils-and project to add mBuffer access attempt
public class TextureLoader {

	/*
	 * Private declaration for BufferedImage allocation
	 */

	private Texture2D tex = null;
	private BufferedImage bufferedImage = null;
	private int textureFormat = Texture.RGBA;
	private int imageComponentFormat = ImageComponent.FORMAT_RGBA;
	private boolean byRef = false;
	private boolean yUp = false;

	/**
	 * Contructs a TextureLoader object using the specified BufferedImage,
	 * format and option flags
	 * @param bImage The BufferedImage used for loading the texture
	 * @param format The format specifies which channels to use
	 * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
	 *
	 * @exception NullPointerException if bImage is null
	 */
	public TextureLoader(BufferedImage bImage)
	{
		//TODO: surely y up is much better less copying involved? but images  are definately upside down
		this(bImage, false);
	}
	public TextureLoader(BufferedImage bImage, boolean yUp)
	{
		if (bImage == null)
		{
			throw new NullPointerException();
		}

		bufferedImage = bImage;
		chooseFormat(bufferedImage);
		byRef = true;
		this.yUp = yUp;
	}

	/**
	 * Returns the associated Texture object.
	 *
	 * @return The associated Texture object
	 */
	public Texture getTexture()
	{
		ImageComponent2D[] scaledImageComponents = null;
		BufferedImage[] scaledBufferedImages = null;

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		scaledImageComponents = new ImageComponent2D[1];
		scaledBufferedImages = new BufferedImage[1];

		scaledBufferedImages[0] = bufferedImage;

		if (tex == null)
		{
			if (bufferedImage != null)
			{
				// TODO: The below is taken from ObjLoader, and is good for old devices
				// however I want it in all cases on old devices, but PlanComponent uses in 2 places getImage() from texture which
				// crashes if the texture has a nioimagebuffer instead of a bufferedimage reference (which has it's own getNioImage call in texture)

				//this part diverges from original, to allow NioImageBuffer use on older devices
			/*	Bitmap delegate = (Bitmap) bufferedImage.getDelegate();
				// this field is only availible on older OS,(e.g.5.1.1)
				try
				{
					Field fieldmBuffer = delegate.getClass().getDeclaredField("mBuffer");
					fieldmBuffer.setAccessible(true);
					byte[] mBuffer = (byte[]) fieldmBuffer.get(delegate); //IllegalAccessException

					int textureFormat = Texture.RGBA;
					int imageComponentFormat = ImageComponent.FORMAT_RGBA;
					byRef = true;
					yUp = true;

					ByteBuffer buffer = ByteBuffer.wrap(mBuffer).order(ByteOrder.nativeOrder());
					NioImageBuffer nioImageBuffer = new NioImageBuffer(width, height, NioImageBuffer.ImageType.TYPE_4BYTE_RGBA, buffer);
					// Create texture from image
					scaledImageComponents[0] = new ImageComponent2D(imageComponentFormat, nioImageBuffer, byRef, yUp);
					tex = new Texture2D(Texture.BASE_LEVEL, textureFormat, width, height);
					tex.setImage(0, scaledImageComponents[0]);
				}
				catch (NoSuchFieldException e)
				{
					//e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					//e.printStackTrace();
				}*/

				//If the mBuffer path isn't availible use a normal texture loader
				if (tex == null)
				{
					// Create texture from image
					scaledImageComponents[0] = new ImageComponent2D(imageComponentFormat, scaledBufferedImages[0], byRef, yUp);
					tex = new Texture2D(Texture.BASE_LEVEL, textureFormat, width, height);
					tex.setImage(0, scaledImageComponents[0]);
				}
				tex.setMinFilter(Texture.NICEST);// will cause mip maps to be used if auto generation enabled on device
				tex.setMagFilter(Texture.NICEST);
			}
		}
		return tex;
	}

	/**
	 * Choose the correct ImageComponent and Texture format for the given
	 * image
	 */
	private void chooseFormat(BufferedImage image)
	{
		switch (image.getType())
		{
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_INT_ARGB:
				imageComponentFormat = ImageComponent.FORMAT_RGBA;
				textureFormat = Texture.RGBA;
				break;
			case BufferedImage.TYPE_3BYTE_BGR:
			case BufferedImage.TYPE_INT_BGR:
			case BufferedImage.TYPE_INT_RGB:
				imageComponentFormat = ImageComponent.FORMAT_RGB;
				textureFormat = Texture.RGB;
				break;
			case BufferedImage.TYPE_CUSTOM:
				throw new UnsupportedOperationException("BufferedImage.TYPE_CUSTOM!");

			default:
				// System.err.println("Unoptimized Image Type "+image.getType());
				imageComponentFormat = ImageComponent.FORMAT_RGBA;
				textureFormat = Texture.RGBA;
				break;
		}
	}


}
