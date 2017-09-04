package com.eteks.renovations3d;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by phil on 5/7/2017.
 */

public class ImageAcquireManager
{
	public enum Destination {IMPORT_BACKGROUND, IMPORT_TEXTURE};

	private static final int REQUEST_CODE_GET_IMAGE = 4736;
	private static final int REQUEST_CODE_TAKE_IMAGE = 4737;
	private ImageReceiver imageReceiver = null;
	private Destination imageDestination = null;
	private String imagedReceivedFile = null;// used by the dialog itself to ask for a pending image

	private String mCurrentPhotoPath = null;
	private Renovations3DActivity activity;

	public ImageAcquireManager(Renovations3DActivity activity)
	{
		this.activity = activity;
	}

	public void onDestroy()
	{

	}

	/**
	 * Call whenever no intents can reasonably return anymore (e.g. on pressing the new button)
	 */
	public void clear()
	{
		imageReceiver = null;
		imageDestination = null;
		imagedReceivedFile = null;
		mCurrentPhotoPath = null;
	}

	public String requestPendingChosenImageFile(Destination destination)
	{
		if(destination == imageDestination)
		{
			// return the value then blank it out so it doesn't get used twices
			String ret = imagedReceivedFile;
			clear();
			return ret;
		}
		else
		{
			return null;
		}
	}

	public void pickImage(ImageReceiver imageReceiver, Destination destination)
	{
		clear();
		this.imageReceiver = imageReceiver;
		this.imageDestination = destination;
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		// we will get teh file path back in teh return but we might still be destroyed so we need to record the destination
		SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("mCurrentPhotoPath", "");
		editor.putString("imageDestination", imageDestination.name());
		editor.apply();

		// in case this is a destroy version
		System.out.println("startActivityForResult - auto saving");
		activity.doAutoSave();
		activity.startActivityForResult(intent, REQUEST_CODE_GET_IMAGE);
	}

	public void takeImage(ImageReceiver imageReceiver, Destination destination)
	{
		clear();
		this.imageReceiver = imageReceiver;
		this.imageDestination = destination;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		try
		{
			//http://stackoverflow.com/questions/1910608/android-action-image-capture-intent
			Uri outputFileUri = null;

			//https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
			if (Build.VERSION.SDK_INT > M) {
				outputFileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", createImageFile());
			} else {
				outputFileUri = Uri.fromFile(createImageFile());
			}
			intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

			// I don't seem to get the save instance state or onPause calls, so if I have a photo file path then I assume this might destroy
			// to get the camera running, so I need to save the destination of the photo for a restart
			SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("mCurrentPhotoPath", mCurrentPhotoPath != null ? mCurrentPhotoPath : "");
			editor.putString("imageDestination", imageDestination.name());
			editor.apply();

			// in case this is a destroy version
			System.out.println("startActivityForResult - auto saving");
			activity.doAutoSave();
			activity.startActivityForResult(intent, REQUEST_CODE_TAKE_IMAGE);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "IOException - takeImage", null);
		}
	}


	private File createImageFile() throws IOException
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_" + ".jpg";
		File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		// I think internal only ever is a fine thing
	/*	if( Renovations3DActivity.writeExternalStorageGranted)
		{
			storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		}*/
	/*	File image = File.createTempFile(imageFileName, // prefix
				".jpg", // suffix
				storageDir // directory
		);*/
		// can't use temp as we might get destroyeded
		File image = new File(storageDir, imageFileName);
		image.createNewFile();

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = Uri.fromFile(image).toString();
		return image;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE_GET_IMAGE || requestCode == REQUEST_CODE_TAKE_IMAGE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				String fileName = null;
				try
				{
					if (data == null)
					{
						SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
						// first case camera returns no data, but we have the file pointer in a member field so use that
						if (mCurrentPhotoPath != null)
						{
							// we just have to access the temp file we gave it and assume it's been filled
							//http://stackoverflow.com/questions/6982184/camera-activity-returning-null-android
							fileName = new File(Uri.parse(mCurrentPhotoPath).getPath()).getAbsolutePath();
						}
						else
						{
							// this case no data and no mCurrentPhotoPath means we got destroyed during the call to get the photo
							// from the camera , cos the device is very small, let's see if onPause saved the file location for us
							mCurrentPhotoPath = settings.getString("mCurrentPhotoPath", null);
							if (mCurrentPhotoPath != null && mCurrentPhotoPath.length() > 0)
							{
								fileName = new File(Uri.parse(mCurrentPhotoPath).getPath()).getAbsolutePath();
							}
						}

						// just to be sure clear it all out
						mCurrentPhotoPath = null;
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("mCurrentPhotoPath", "");
						editor.apply();
					}
					else
					{
						fileName = getFilePath(activity, data.getData());
					}
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "URISyntaxException - onActivityResult", null);
				}

				// are we looking at some sort of google docs or something?
				if (fileName == null)
				{
					if (data != null)
					{
						Uri imageUri = data.getData();
						try
						{
							File tempFile = File.createTempFile("tempFileImageIntentReturn", ".png", activity.getCacheDir());

							Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
							FileOutputStream out = new FileOutputStream(tempFile);
							bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
							fileName = tempFile.getAbsolutePath();
						}
						catch (IOException e)
						{
							e.printStackTrace();
							Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "IOException - onActivityResult", null);
						}
					}
				}


				if (fileName != null)
				{
					if (imageReceiver != null)
					{
						imageReceiver.receivedImage(fileName);
						// all done clear up
						clear();
					}
					else
					{
						// record it ready for pick up once the dialog is up
						imagedReceivedFile = fileName;
						// note no clear obviously as we need destination etc ready


						if( imageDestination == null )
						{
							SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
							try
							{
								imageDestination = Destination.valueOf(settings.getString("imageDestination", ""));
							}
							catch( Exception e)
							{
								e.printStackTrace();
								//NPE or illegalarguement, just ignore as the null will carry through to no action
							}
							// just to be sure clear it all out
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("mCurrentPhotoPath", "");
							editor.apply();

						}

						if(imageDestination !=null )
						{
							switch(imageDestination)
							{
								case IMPORT_BACKGROUND:
									// let's presume this has come from a destroy call after the get file intent
									// so I need to open the background import dialog now and it will check back with the activity to see if a
									// a file is waiting to be received
									activity.showBackGroundImportWizard();
									break;
								case IMPORT_TEXTURE:
									activity.showImportTextureWizard();
									break;
							}
						}


					}

				}
				else
				{
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "fileName == null - onActivityResult", null);
				}
			}
		}
	}


	public interface ImageReceiver
	{
		void receivedImage(String imageName);
	}


	public static String getFilePath(Context context, Uri uri) throws URISyntaxException
	{
		String selection = null;
		String[] selectionArgs = null;
		// Uri is different in versions after KITKAT (Android 4.4), we need to

		if (Build.VERSION.SDK_INT >= KITKAT && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri))
		{
			if (isExternalStorageDocument(uri))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				return Environment.getExternalStorageDirectory() + "/" + split[1];
			}
			else if (isDownloadsDocument(uri))
			{
				final String id = DocumentsContract.getDocumentId(uri);
				uri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
			}
			else if (isMediaDocument(uri))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("image".equals(type))
				{
					uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("video".equals(type))
				{
					uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("audio".equals(type))
				{
					uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				selection = "_id=?";
				selectionArgs = new String[]{
						split[1]
				};
			}
		}
		if ("content".equalsIgnoreCase(uri.getScheme()))
		{
			String[] projection = {
					MediaStore.Images.Media.DATA
			};
			Cursor cursor = null;
			try
			{
				cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				if (cursor.moveToFirst())
				{
					return cursor.getString(column_index);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if ("file".equalsIgnoreCase(uri.getScheme()))
		{
			return uri.getPath();
		}
		return null;
	}

	public static boolean isExternalStorageDocument(Uri uri)
	{
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri)
	{
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri)
	{
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
