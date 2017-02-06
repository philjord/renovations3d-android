package com.eteks.renovations3d;


import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.eteks.renovations3d.android.swingish.JFileChooser;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;
import com.mindblowing.renovations3d.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import static com.mindblowing.renovations3d.R.id.pager;

/**
 * Created by phil on 11/20/2016.
 */
public class SweetHomeAVRActivity extends FragmentActivity
{

	public static final String PREFS_NAME = "SweetHomeAVRActivityDefault";

	public static FirebaseAnalytics mFirebaseAnalytics;

	private SweetHomeAVRPagerAdapter mSweetHomeAVRPagerAdapter;
	private ViewPager mViewPager;

	private File chooserStartFolder;

	public static HashSet<String> welcomScreensShownThisSession = new HashSet<String>();

	public static SweetHomeAVR sweetHomeAVR; // for plan undo redo, now for import statements too

	private boolean fileSystemAccessGranted = false;

	// Description of original
	// SweetHomeAVR|HomeApplication
	// this has public void main
	// this is a singleton
	// designed to allow many HomeFrameControllers (with HomeController) to be openable
	// system opens a blank HomeFrameController in the start method
	// and then leaves it or uses it to open a new HomeController (and it's home model)
	// which when listener is called back will close the blank HomeFrameController
	// and display the opened one


	// so the HomeFrameController is the entry point to each of the multiple home editors

	// A HomeFrameController owns the HomeController and hands out to sub parts for construction
	// it also has the Home home data model for the controller, but just constructs it with it (no handing out)
	// It's view is a HomeFramePane

	// The HomeFramePane is mildly odd
	// It's a JContentPane, but put s JContentPane as is sole child (a HomePane|HomeView)
	// It owns it's parent JFrame and sets it up
	// most work it just get the Frame sexy

	// HomePane is constructed from Factory on request out of HomeController
	// it is placed into HomeFrameController and is the main view of the 4 panes
	// mostly just right click and main_menu actions, heaps of that in fact
	// listeners are only to get the actions displaying right
	// it has 3 interesting things
	// createMainPane -> first horizontal split
	// createCatalogFurniturePane left side vert split
	// createPlanView3DPane right side vert split
	// using the HomeController->getController*->getView x 4

	// Each of the 4 interesting controllers under HomeController holds a lazy single view from the factory


	// What I want to do!!!!

	// Activity is entry point, so it's a bit like the SweetHomeAVR is that regard
	// ActivityPagerAdpater needs to hand out views, so it needs the HomeController loaded before it's talked to at all
	// Activity is the main JFrame and ContentPane gear and will dictagte the display
	// in the longer run it needs to rejig the display for device sizes, not major
	// I still must recognise the Canvas3D in a view issue before load up of the
	// 3D gear of Component3D
	// I would like to teh viewcontroller versions of Component3DController etc
	// to come from teh base project, so I need to get that right
	// notice that the Factory can happily hand out the Fragment bits
	// so that should be possible?

	//Note the HomeApplication parent of SweetHomeAVR is a multi-home interface, possibly dump for good measure?

	// Question then, can I rely on a HomeController from base project to look aside at factory point
	// seems possible.
	//
	//
	// What is HomeControllers life cycle, no the dummy controller open is not important
	// my rewrite suing the homerecorder is fine!

	// key point in inti method Application is listening for the open guy to change teh collection
	// and open the frame from it
	//public void collectionChanged(CollectionEvent<Home> ev) {
	//	switch (ev.getType()) {
	// case ADD:
	// Home home = ev.getItem();
	// HomeFrameController controller = createHomeFrameController(home);
	// controller.displayView();
	// if (!this.firstApplicationHomeAdded) {
	// 	this.firstApplicationHomeAdded = true;
	// 	addNewHomeCloseListener(home, controller.getHomeController());
	// }
	// homeFrameControllers.put(home, controller);

	// this gets called because HomeController.open says open the HomeApplciation.addHome triggers the callback
	// so HomeController public void open(final String homeName)
	// need the open lines swapped and the addHome made a call to sweethomeAVR directly, no probs
	// notice open uses ThreadedTaskController for executing open task, so it can borrow the empty
	// HomePane view for a loading bar in a dialog etc, not major
	// I suspect the ThreadTeaskView gear can probably be ported to android views without much pain

	// This guy represents HomeFrameController, HomeFramePane and basically HomPane
	// but SweetHomeAVR has one pointer to a HomeController, instead of a map of HomeFrameController
	// and once running this tells SweetHomeAVR to load a single controller


	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		MobileAds.initialize(getApplicationContext(), "ca-app-pub-7177705441403385~4026888158");
		setContentView(R.layout.main);

		AdView mAdView = (AdView) findViewById(R.id.lowerBannerAdView);
		AdRequest.Builder builder = new AdRequest.Builder();
		builder.addTestDevice("56ACE73C453B9562B288E8C2075BDA73");
		AdRequest adRequest = builder.build();
		mAdView.loadAd(adRequest);

		// Obtain the FirebaseAnalytics instance.
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

		ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);


		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		OperatingSystem.activity = this;

		sweetHomeAVR = new SweetHomeAVR(this);
		mSweetHomeAVRPagerAdapter = new SweetHomeAVRPagerAdapter(getSupportFragmentManager(), sweetHomeAVR);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			int hasWriteExternalStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED)
			{
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						REQUEST_CODE_ASK_PERMISSIONS);
			}
			else
			{
				permissionGranted();
			}
		}
		else
		{
			permissionGranted();
		}
	}

	/**
	 * This must be called eventually by SweetHomeAVR
	 */
	public void setUpViews()
	{
		mViewPager = (ViewPager) findViewById(pager);
		mViewPager.setAdapter(mSweetHomeAVRPagerAdapter);
		mViewPager.setCurrentItem(1);
		mViewPager.setOffscreenPageLimit(4);

		invalidateOptionsMenu();
	}



	private void permissionGranted()
	{
		fileSystemAccessGranted = true;
		invalidateOptionsMenu();
		loadUpContent();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (fileSystemAccessGranted)
		{
			menu.findItem(R.id.menu_load).setEnabled(true);

			boolean homeLoaded = sweetHomeAVR.getHomeController() != null;
			menu.findItem(R.id.menu_save).setEnabled(homeLoaded);
			menu.findItem(R.id.menu_saveas).setEnabled(homeLoaded);
		}

		menu.findItem(R.id.menu_new).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "NEW_HOME.Name"));
		menu.findItem(R.id.menu_load).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "OPEN.Name"));
		menu.findItem(R.id.menu_save).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "SAVE.Name"));
		menu.findItem(R.id.menu_saveas).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "SAVE_AS.Name"));
		menu.findItem(R.id.menu_help).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "HELP_MENU.Name"));
		menu.findItem(R.id.menu_about).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "ABOUT.Name"));
		menu.findItem(R.id.menu_preferences).setTitle(sweetHomeAVR.getUserPreferences().getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "PREFERENCES.Name"));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.menu_new:
				newHome();
				return true;
			case R.id.menu_load:
				loadSh3dFile();
				return true;
			case R.id.menu_save:
				//I must get off the EDT as it may ask the question in a blocking manner
				Thread t = new Thread()
				{
					public void run()
					{
						if (sweetHomeAVR.getHomeController() != null)
							sweetHomeAVR.getHomeController().saveAndCompress();
					}
				};
				t.start();
				return true;
			case R.id.menu_saveas:
				//I must get off the EDT and ask the question in a blocking manner
				Thread t2 = new Thread()
				{
					public void run()
					{
						if (sweetHomeAVR.getHomeController() != null)
							sweetHomeAVR.getHomeController().saveAsAndCompress();
					}
				};
				t2.start();
				return true;
			case R.id.menu_help:
				if (sweetHomeAVR.getHomeController() != null)
					sweetHomeAVR.getHomeController().help();
				return true;
			case R.id.menu_about:
				if (sweetHomeAVR.getHomeController() != null)
					sweetHomeAVR.getHomeController().about();
				return true;
			case R.id.menu_preferences:
				if (sweetHomeAVR.getHomeController() != null)
					sweetHomeAVR.getHomeController().editPreferences();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void loadSh3dFile()
	{
		chooserStartFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

		final JFileChooser fileChooser = new JFileChooser(SweetHomeAVRActivity.this, chooserStartFolder);
		fileChooser.setExtension("sh3d");
		//I can't get hold of the content manager from here
		// I want to use the real filefilters so sh3x will load
		//fileChooser.setFileFilter(sweetHomeAVR.getHomeController().getContentManager().getFileFilter(ContentManager.ContentType.SWEET_HOME_3D )[0]);
		fileChooser.setFileListener(new JFileChooser.FileSelectedListener()
		{
			@Override
			public void fileSelected(final File file)
			{
				chooserStartFolder = file;
				loadHome(file);
			}
		});
		// get on the EDT
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				fileChooser.showDialog();
			}
		});
		// this is the proper way to do it, but I don't have a home control here
		/*Thread t2 = new Thread(){public void run(){
			HomeController controller = sweetHomeAVR.getHomeController();
			if(controller == null)
					newHome();
				String openFileName = controller.getView().showOpenDialog();
				if(openFileName != null) {
					loadHome(new File(openFileName));
				}
			}};
		t2.start();*/

	}

	private void newHome()
	{
		if (sweetHomeAVR.getHomeController() != null)
			sweetHomeAVR.getHomeController().close();

		// must always recreate otherwise the pager hand out an IllegalStateException
		sweetHomeAVR = new SweetHomeAVR(this);

		mSweetHomeAVRPagerAdapter.setSweetHomeAVR(sweetHomeAVR);
		//see http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android/26944013#26944013 for ensuring new fragments

		// discard the old view first
		mSweetHomeAVRPagerAdapter.notifyChangeInPosition(1);
		mSweetHomeAVRPagerAdapter.notifyDataSetChanged();

		// create new home and trigger the new views
		sweetHomeAVR.newHome();
		mSweetHomeAVRPagerAdapter.notifyChangeInPosition(1);
		mSweetHomeAVRPagerAdapter.notifyDataSetChanged();
	}

	public void loadHome(final File homeFile)
	{
		if (homeFile != null)
		{
			if (sweetHomeAVR.getHomeController() != null)
				sweetHomeAVR.getHomeController().close();

			// to be safe get this back onto the ui thread
			SweetHomeAVRActivity.this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					boolean prevHomeLoaded = sweetHomeAVR.getHomeController() != null;
					sweetHomeAVR = new SweetHomeAVR(SweetHomeAVRActivity.this);
					mSweetHomeAVRPagerAdapter.setSweetHomeAVR(sweetHomeAVR);
					//see http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android/26944013#26944013 for ensuring new fragments

					// discard the old view first
					mSweetHomeAVRPagerAdapter.notifyChangeInPosition(1);
					mSweetHomeAVRPagerAdapter.notifyDataSetChanged();

					sweetHomeAVR.loadHome(homeFile);

					// force teh frags to load up now
					if(prevHomeLoaded)
					{
						mSweetHomeAVRPagerAdapter.notifyChangeInPosition(1);
						mSweetHomeAVRPagerAdapter.notifyDataSetChanged();
					}
				}
			});
		}
		else
		{
			newHome();
		}
	}


	BroadcastReceiver onCompleteHTTPIntent = new BroadcastReceiver()
	{
		public void onReceive(Context ctxt, Intent intent)
		{
		unregisterReceiver(this);
		long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

		// This is one of our downloads.
		final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(id);
		Cursor cursor = downloadManager.query(query);

		if (cursor.moveToFirst())
		{
			int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
			int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
			int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

			int status = cursor.getInt(statusIndex);

			if (status == DownloadManager.STATUS_SUCCESSFUL)
			{
				String localUri = cursor.getString(localUriIndex);
				try
				{
					loadFile(new File(new URI(localUri).getPath()));
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
			else if (status == DownloadManager.STATUS_FAILED)
			{
				int reason = cursor.getInt(reasonIndex);

				String message;
				switch (reason)
				{
					case DownloadManager.ERROR_FILE_ERROR:
					case DownloadManager.ERROR_DEVICE_NOT_FOUND:
					case DownloadManager.ERROR_INSUFFICIENT_SPACE:
						message = "DownloadErrorDisk";
						break;
					case DownloadManager.ERROR_HTTP_DATA_ERROR:
					case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
						message = "DownloadErrorHttp";
						break;
					case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
						message = "DownloadErrorRedirection";
						break;
					default:
						message = "DownloadErrorUnknown";
						break;
				}

				Toast.makeText(SweetHomeAVRActivity.this, String.format("DownloadFailedWithErrorMessage", message), Toast.LENGTH_SHORT).show();

			}
		}
		}
	};

	private void loadFile(File inFile)
	{
		if (inFile.getName().toLowerCase().endsWith(".sh3d"))
		{
			loadHome(inFile);
		}
		else if (inFile.getName().toLowerCase().endsWith(".sh3f"))
		{
			//TODO: not sure if this is needed? might start like this?
			newHome();
			HomeController controller = SweetHomeAVRActivity.sweetHomeAVR.getHomeController();
			if (controller != null)
			{
				controller.importFurnitureLibrary(inFile.getAbsolutePath());
			}
		}
		else if (inFile.getName().toLowerCase().endsWith(".sh3t"))
		{
			newHome();
			HomeController controller2 = SweetHomeAVRActivity.sweetHomeAVR.getHomeController();
			if (controller2 != null)
			{
				controller2.importTexturesLibrary(inFile.getAbsolutePath());
			}
		}
	}

	private void loadUpContent()
	{
		//TODO: see eclipse SweetHomeAVR.protected void start(String[] args) for exactly this setup, but better
		Intent intent = getIntent();
		if (intent != null)
		{
			String action = intent.getAction();

			if (action.compareTo(Intent.ACTION_VIEW) == 0)
			{
				String scheme = intent.getScheme();
				ContentResolver resolver = getContentResolver();

				if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0)
				{
					//Uri uri = intent.getData();
					//String name = getContentName(resolver, uri);
					//Log.v("tag", "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
					//Toast.makeText(SweetHomeAVRActivity.this, "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name, Toast.LENGTH_LONG).show();
					//InputStream input = resolver.openInputStream(uri);
					//TODO: clicking an http content fires off the http version and this one??
					//inputStreamToFile(input, Environment.DIRECTORY_DOWNLOADS + "/" + name);
					//loadHome(new File(Environment.DIRECTORY_DOWNLOADS + "/" + name));

					setIntent(null);
					return;
				}
				else if (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0)
				{
					Uri uri = intent.getData();
					String name = uri.getLastPathSegment();

					Log.v("tag", "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
					Toast.makeText(SweetHomeAVRActivity.this, "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name, Toast.LENGTH_LONG).show();

					File inFile = new File(uri.getPath());
					loadFile(inFile);

					setIntent(null);
					return;
				}
				else if (scheme.compareTo("http") == 0)
				{
					Toast.makeText(SweetHomeAVRActivity.this, "http: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : ", Toast.LENGTH_LONG).show();

					Uri uri = intent.getData();
					final String fileName = uri.getLastPathSegment();

					Log.v("tag", "Http intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + fileName);
					Toast.makeText(SweetHomeAVRActivity.this, "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + fileName, Toast.LENGTH_LONG).show();
					DownloadManager.Request request = new DownloadManager.Request(uri);
					request.setDescription(fileName + "_download");
					request.setTitle(fileName);
					// in order for this if to run, you must use the android 3.2 to compile your app
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					{
						request.allowScanningByMediaScanner();
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					}
					request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

					// get download service and enqueue file
					DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
					manager.enqueue(request);
					registerReceiver(onCompleteHTTPIntent, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
					setIntent(null);
					Toast.makeText(SweetHomeAVRActivity.this, "Download started, please wait...", Toast.LENGTH_LONG).show();
					return;
				}
				else if (scheme.compareTo("ftp") == 0)
				{
					// TODO Import from FTP!
					Toast.makeText(SweetHomeAVRActivity.this, "Import from ftp not supported: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : ", Toast.LENGTH_LONG).show();
					setIntent(null);
					return;
				}

			}
		}


		// download managers only appeared in ginger bread
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			// now download the files from drop box
			// folder	String url = "https://www.dropbox.com/sh/m1291ug4cooafn9/AADthFyjnndOhjTvr2iiMJzWa?dl=0";
			// notice the files actual url not the sexy page offering the file
			String[] urls = new String[]{
					"https://dl.dropboxusercontent.com/s/0jgkj1wrsvsk1ix/SweetHome3DExample10-FlatWithMezzanine.sh3d",
					"https://dl.dropboxusercontent.com/s/axkjuhmi03pbhxu/verysimple.sh3d",
					"https://dl.dropboxusercontent.com/s/a7jcxe0xdtkhw9b/LucaPresidente.sh3f"};
			String[] fileNames = new String[]{
					"SweetHome3DExample10-FlatWithMezzanine.sh3d",
					"verysimple.sh3d",
					"LucaPresidente.sh3f"};

			for (int i = 0; i < urls.length; i++)
			{
				if (!(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNames[i]).exists()))
				{
					String url = urls[i];
					String fileName = fileNames[i];
					DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
					request.setDescription(fileName + " download");
					request.setTitle(fileName);
					// in order for this if to run, you must use the android 3.2 to compile your app
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					{
						request.allowScanningByMediaScanner();
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					}
					request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

					// get download service and enqueue file
					DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
					manager.enqueue(request);
				}
			}
		}

		// now just fire up a lovely clear new home
		newHome();
	}
	private String getContentName(ContentResolver resolver, Uri uri)
	{
		Cursor cursor = resolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
		if (nameIndex >= 0)
		{
			return cursor.getString(nameIndex);
		}
		else
		{
			return null;
		}
	}

	private void inputStreamToFile(InputStream in, String file)
	{
		try
		{
			OutputStream out = new FileOutputStream(new File(file));

			int size = 0;
			byte[] buffer = new byte[1024];

			while ((size = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, size);
			}

			out.close();
		}
		catch (Exception e)
		{
			Log.e("MainActivity", "InputStreamToFile exception: " + e.getMessage());
		}
	}

	final private int REQUEST_CODE_ASK_PERMISSIONS = 123;//just has to match from request to response below

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_ASK_PERMISSIONS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					// Permission Granted
					permissionGranted();
				}
				else
				{
					// Permission Denied
					Toast.makeText(SweetHomeAVRActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}




}
