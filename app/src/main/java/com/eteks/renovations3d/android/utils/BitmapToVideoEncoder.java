package com.eteks.renovations3d.android.utils;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

//https://stackoverflow.com/questions/17096726/how-to-encode-bitmaps-into-a-video-using-mediacodec
public class BitmapToVideoEncoder {
	private static final String TAG = BitmapToVideoEncoder.class.getSimpleName();

	private IBitmapToVideoEncoderCallback mCallback;
	private File mOutputFile;
	private Queue<Bitmap> mEncodeQueue = new ConcurrentLinkedQueue();
	private MediaCodec mediaCodec;
	private MediaMuxer mediaMuxer;

	private Object mFrameSync = new Object();
	private CountDownLatch mNewFrameLatch;

	private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
	private static int mWidth;
	private static int mHeight;
	private static int mFrameRate;
	private static final int BIT_RATE = 16000000;
	//private static final int FRAME_RATE = 30; // Frames per second

	private static final int I_FRAME_INTERVAL = 1;

	private int mGenerateIndex = 0;
	private int mTrackIndex;
	private boolean mNoMoreFrames = false;
	private boolean mAbort = false;

	public interface IBitmapToVideoEncoderCallback {
		void onEncodingComplete(File outputFile);
	}

	public BitmapToVideoEncoder(IBitmapToVideoEncoderCallback callback) {
		mCallback = callback;
	}

	public boolean isEncodingStarted() {
		return (mediaCodec != null) && (mediaMuxer != null) && !mNoMoreFrames && !mAbort;
	}

	public int getActiveBitmaps() {
		return mEncodeQueue.size();
	}

	public void startEncoding(int width, int height, int frameRate, File outputFile) {
		mWidth = width;
		mHeight = height;
		mFrameRate = frameRate;
		mOutputFile = outputFile;

		String outputFileString;
		try {
			outputFileString = outputFile.getCanonicalPath();
		} catch (IOException e) {
			Log.e(TAG, "Unable to get path for " + outputFile);
			return;
		}

		MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
		if (codecInfo == null) {
			Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
			return;
		}
		Log.d(TAG, "found codec: " + codecInfo.getName());
		int colorFormat;
		try {
			colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
		} catch (Exception e) {
			colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
		}

		try {
			mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
		} catch (IOException e) {
			Log.e(TAG, "Unable to create MediaCodec " + e.getMessage());
			return;
		}

		MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
		mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mediaCodec.start();
		try {
			mediaMuxer = new MediaMuxer(outputFileString, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (IOException e) {
			Log.e(TAG,"MediaMuxer creation failed. " + e.getMessage());
			return;
		}

		Log.d(TAG, "Initialization complete. Starting encoder...");

		//Completable.fromAction(() -> encode())
		//				.subscribeOn(Schedulers.io())
		//				.observeOn(AndroidSchedulers.mainThread())
		//				.subscribe();

		Thread thread = new Thread(new Runnable() {
			public void run() {
				encode();
			}});
		thread.start();
	}

	public void stopEncoding() {
		if (mediaCodec == null || mediaMuxer == null) {
			Log.d(TAG, "Failed to stop encoding since it never started");
			return;
		}
		Log.d(TAG, "Stopping encoding");

		mNoMoreFrames = true;

		synchronized (mFrameSync) {
			if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
				mNewFrameLatch.countDown();
			}
		}
	}

	public void abortEncoding() {
		if (mediaCodec == null || mediaMuxer == null) {
			Log.d(TAG, "Failed to abort encoding since it never started");
			return;
		}
		Log.d(TAG, "Aborting encoding");

		mNoMoreFrames = true;
		mAbort = true;
		mEncodeQueue = new ConcurrentLinkedQueue(); // Drop all frames

		synchronized (mFrameSync) {
			if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
				mNewFrameLatch.countDown();
			}
		}
	}

	public void queueFrame(Bitmap bitmap) {
		if (mediaCodec == null || mediaMuxer == null) {
			Log.d(TAG, "Failed to queue frame. Encoding not started");
			return;
		}

		Log.d(TAG, "Queueing frame");
		mEncodeQueue.add(bitmap);

		synchronized (mFrameSync) {
			if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
				mNewFrameLatch.countDown();
			}
		}
	}

	private void encode() {

		Log.d(TAG, "Encoder started");

		while(true) {
			if (mNoMoreFrames && (mEncodeQueue.size() ==  0)) break;

			Bitmap bitmap = mEncodeQueue.poll();
			if (bitmap ==  null) {
				synchronized (mFrameSync) {
					mNewFrameLatch = new CountDownLatch(1);
				}

				try {
					mNewFrameLatch.await();
				} catch (InterruptedException e) {}

				bitmap = mEncodeQueue.poll();
			}

			if (bitmap == null) continue;

			long TIMEOUT_USEC = 500000;
			int inputBufIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
			long ptsUsec = computePresentationTime(mGenerateIndex, mFrameRate);
			if (inputBufIndex >= 0) {
				int colorFormat = mediaCodec.getInputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT);
				//NOTE I always get 19 back, but if I select any format other than semi planar the output is rubbish
				if(colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) {
					//COLOR_FormatYUV420Flexible is more modern but a bit more complex
					Image inputImage = mediaCodec.getInputImage(inputBufIndex);
					this.fillImage(inputImage, bitmap.getWidth(), bitmap.getHeight(), bitmap);
					int len = 0;
					for(Image.Plane p :	inputImage.getPlanes() )
						len += p.getBuffer().capacity();
					mediaCodec.queueInputBuffer(inputBufIndex, 0, len, ptsUsec, 0);
				} else {
					final ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufIndex);
					inputBuffer.clear();
					byte[] byteConvertFrame = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
					inputBuffer.put(byteConvertFrame);
					mediaCodec.queueInputBuffer(inputBufIndex, 0, byteConvertFrame.length, ptsUsec, 0);
				}
				mGenerateIndex++;
			}
			MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
			int encoderStatus = mediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
			if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				// no output available yet
				Log.e(TAG, "No output from encoder available");
			} else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				// not expected for an encoder
				MediaFormat newFormat = mediaCodec.getOutputFormat();
				mTrackIndex = mediaMuxer.addTrack(newFormat);
				mediaMuxer.start();
			} else if (encoderStatus < 0) {
				Log.e(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
			} else if (mBufferInfo.size != 0) {
				ByteBuffer encodedData = mediaCodec.getOutputBuffer(encoderStatus);
				if (encodedData == null) {
					Log.e(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
				} else {
					encodedData.position(mBufferInfo.offset);
					encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
					mediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
					mediaCodec.releaseOutputBuffer(encoderStatus, false);
				}
			}
		}

		release();

		if (mAbort) {
			mOutputFile.delete();
		} else {
			mCallback.onEncodingComplete(mOutputFile);
		}
	}

	private void release() {
		if (mediaCodec != null) {
			try {
				mediaCodec.stop();
				mediaCodec.release();
			} catch(IllegalStateException e){
				//ignore
			}
			mediaCodec = null;
			Log.d(TAG,"RELEASE CODEC");
		}
		if (mediaMuxer != null) {
			try {
				mediaMuxer.stop();
				mediaMuxer.release();
			} catch(IllegalStateException e){
				//ignore
			}
			mediaMuxer = null;
			Log.d(TAG,"RELEASE MUXER");
		}
	}

	private static MediaCodecInfo selectCodec(String mimeType) {
		for (MediaCodecInfo codecInfo : new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()) {
			if (!codecInfo.isEncoder()) {
				continue;
			}
			String[] types = codecInfo.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}

	private static int selectColorFormat(MediaCodecInfo codecInfo,
																			 String mimeType) {
		MediaCodecInfo.CodecCapabilities capabilities = codecInfo
						.getCapabilitiesForType(mimeType);
		for (int i = 0; i < capabilities.colorFormats.length; i++) {
			int colorFormat = capabilities.colorFormats[i];
			if (isRecognizedFormat(colorFormat)) {
				return colorFormat;
			}
		}
		return 0; // not reached
	}

	private static boolean isRecognizedFormat(int colorFormat) {
		switch (colorFormat) {
			// these are the formats we know how to handle for
			// to support flexible, I have some code but it's untested
			//https://stackoverflow.com/questions/38421564/what-is-color-formatyuv420flexible
			//case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
			//case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			//case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			//case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			//case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
				return true;
			default:
				return false;
		}
	}

	private byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

		int[] argb = new int[inputWidth * inputHeight];

		scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

		byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
		encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

		scaled.recycle();

		return yuv;
	}
	private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
		final int frameSize = width * height;

		int yIndex = 0;
		int uvIndex = frameSize;

		int a, R, G, B, Y, U, V;
		int index = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
				R = (argb[index] & 0xff0000) >> 16;
				G = (argb[index] & 0xff00) >> 8;
				B = (argb[index] & 0xff) >> 0;

				// well known RGB to YUV algorithm
				Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
				U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
				V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

				// NV21 has a plane of Y and interleaved planes of UV each sampled by a factor of 2
				//    meaning for every 4 Y pixels there are 1 U and 1 V.  Note the sampling is every other
				//    pixel AND every other scanline.
				yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
				if (j % 2 == 0 && index % 2 == 0) {
					yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
					yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
				}
				index++;
			}
		}
	}

	private void fillImage(Image image, int inputWidth, int inputHeight, Bitmap scaled) {
		int[] argb = new int[inputWidth * inputHeight];
		scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
		fillImagePlanes(image, argb, inputWidth, inputHeight);
		scaled.recycle();
	}

	private void fillImagePlanes(Image image, int[] argb, int width, int height) {
		final int frameSize = width * height;
		int format = image.getFormat();
		// but infact get pixelstride etc etc from format
		// get plane buffer like this depending on format
		ByteBuffer yBuff = image.getPlanes()[0].getBuffer();
		ByteBuffer uBuff = image.getPlanes()[0].getBuffer();
		ByteBuffer vBuff = image.getPlanes()[0].getBuffer();

		int a, R, G, B, Y, U, V;
		int index = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
				R = (argb[index] & 0xff0000) >> 16;
				G = (argb[index] & 0xff00) >> 8;
				B = (argb[index] & 0xff) >> 0;

				// well known RGB to YUV algorithm
				Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
				U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
				V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

				yBuff.put((byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y)));
				if (j % 2 == 0 && index % 2 == 0) {
					vBuff.put((byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V)));
					uBuff.put((byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U)));
				}
				index++;
			}
		}
	}



	private long computePresentationTime(long frameIndex, int framerate) {
		return 132 + frameIndex * 1000000 / framerate;
	}
}