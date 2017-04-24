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
import android.graphics.Region;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.eteks.renovations3d.android.swingish.JFileChooser;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.utils.SopInterceptor;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javaawt.EventQueue;


/**
 * Created by phil on 11/20/2016.
 */
public class Renovations3DActivity extends FragmentActivity
{
	public static final String PREFS_NAME = "SweetHomeAVRActivityDefault";// can't touch as current users use this!
	private static final String APP_OPENED_COUNT = "APP_OPENED_COUNT";
	private static String STATE_TEMP_HOME_REAL_NAME = "STATE_TEMP_HOME_REAL_NAME";
	private static String STATE_TEMP_HOME_REAL_MODIFIED = "STATE_TEMP_HOME_REAL_MODIFIED";
	private static String STATE_CURRENT_HOME_NAME = "STATE_CURRENT_HOME_NAME";
	private static String EXAMPLE_DOWNLOAD_COUNT = "EXAMPLE_DOWNLOAD_COUNT";


	// used as a modal mouse click blocker
	public AndroidDialogView currentDialog = null;

	private static FirebaseAnalytics mFirebaseAnalytics;

	private Renovations3DPagerAdapter mRenovations3DPagerAdapter;
	public ViewPager mViewPager; // public to allow fragment to move around by button

	private File chooserStartFolder;

	public static HashSet<String> welcomeScreensShownThisSession = new HashSet<String>();

	public Renovations3D renovations3D; // for plan undo redo, now for import statements too

	private boolean fileSystemAccessGranted = false;

	private final Timer autoSaveTimer = new Timer("autosave", true);

	// Description of original
	// Renovations3D|HomeApplication
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
	// createCatalogFurniturePane left side vertical split
	// createPlanView3DPane right side vertical split
	// using the HomeController->getController*->getView x 4

	// Each of the 4 interesting controllers under HomeController holds a lazy single view from the factory

	// What I want to do!!!!

	// Activity is entry point, so it's a bit like the Renovations3D is that regard
	// ActivityPagerAdapter needs to hand out views, so it needs the HomeController loaded before it's talked to at all
	// Activity is the main JFrame and ContentPane gear and will dictagte the display
	// in the longer run it needs to rejig the display for device sizes, not major
	// I still must recognise the Canvas3D in a view issue before load up of the
	// 3D gear of Component3D
	// I would like to the viewController versions of Component3DController etc
	// to come from the base project, so I need to get that right
	// notice that the Factory can happily hand out the Fragment bits
	// so that should be possible?

	//Note the HomeApplication parent of Renovations3D is a multi-home interface, possibly dump for good measure?

	// key point in init method Application is listening for the open guy to change the collection
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
	// need the open lines swapped and the addHome made a call to Renovations3D directly, no probs
	// notice open uses ThreadedTaskController for executing open task, so it can borrow the empty
	// HomePane view for a loading bar in a dialog etc, not major
	// I suspect the ThreadTeaskView gear can probably be ported to android views without much pain

	// This guy represents HomeFrameController, HomeFramePane and basically HomPane
	// but Renovations3D has one pointer to a HomeController, instead of a map of HomeFrameController
	// and once running this tells Renovations3D to load a single controller


	/**
	 * For generally doing day to day things
	 *
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBaseContent(String id, String value)
	{
		logFireBase(FirebaseAnalytics.Event.SELECT_CONTENT, id, value);
	}

	/**
	 * For generally doing day to day things
	 *
	 * @param id short id (method name?)
	 */
	public static void logFireBaseContent(String id)
	{
		logFireBase(FirebaseAnalytics.Event.SELECT_CONTENT, id, null);
	}

	/**
	 * For more unusual event that represent exploring/exploiting the system
	 *
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBaseLevelUp(String id, String value)
	{
		logFireBase(FirebaseAnalytics.Event.LEVEL_UP, id, value);
	}

	/**
	 * For more unusual event that represent exploring/exploiting the system
	 *
	 * @param id short id (method name?)
	 */
	public static void logFireBaseLevelUp(String id)
	{
		logFireBase(FirebaseAnalytics.Event.LEVEL_UP, id, null);
	}

	/**
	 * @param event Event MUST be FirebaseAnalytics.Event.*
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBase(String event, String id, String value)
	{
		System.out.println("logFireBase : " + id + (value != null ? (" [" + value + "]") : ""));
		if (mFirebaseAnalytics != null && !BuildConfig.DEBUG)
		{
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
			if (value != null && value.length() > 0)
				bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, value);
			Renovations3DActivity.mFirebaseAnalytics.logEvent(event, bundle);
		}
	}

	//TODO: why is this now being called in the wild?
	//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/crash?reportTypes=REPORT_TYPE_UNSPECIFIED&duration=2592000000&appVersions=193
	/*public void onNewIntent(Intent intent)
	{
		OperatingSystem.applicationInfoDataDir = getApplicationInfo().dataDir;

		renovations3D = new Renovations3D(this);

		// if we have any fragments in the manager then we are doing a restore with bundle style,
		// so all frags will get onCreate() but not the init() and fail, let's chuck em away now
		if (getSupportFragmentManager().getFragments() != null)
		{
			Fragment[] frags = getSupportFragmentManager().getFragments().toArray(new Fragment[0]);
			for (int i = 0; i < frags.length; i++)
			{
				Fragment fragment = frags[i];
				if (fragment != null)
					getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			}
		}

		this.setIntent(intent);
		mRenovations3DPagerAdapter = new Renovations3DPagerAdapter(getSupportFragmentManager(), renovations3D);
		permissionGranted();
	}*/

	public void onCreate(Bundle savedInstanceState)
	{

		if(renovations3D !=null )
		{
			System.out.println("renovations3D ! = null!!");
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		if (!BuildConfig.DEBUG)
		{
			MobileAds.initialize(getApplicationContext(), "ca-app-pub-7177705441403385~4026888158");
			AdView mAdView = (AdView) findViewById(R.id.lowerBannerAdView);
			AdRequest.Builder builder = new AdRequest.Builder();
			builder.addTestDevice("56ACE73C453B9562B288E8C2075BDA73");
			AdRequest adRequest = builder.build();
			mAdView.loadAd(adRequest);
		}

		if (!BuildConfig.DEBUG)
		{
			// Obtain the FirebaseAnalytics instance.
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		}

		ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		OperatingSystem.applicationInfoDataDir = getApplicationInfo().dataDir;

		renovations3D = new Renovations3D(this);

		// if we have any fragments in the manager then we are doing a restore with bundle style,
		// so all frags will get onCreate() but not the init() and fail, let's chuck em away now
		if (getSupportFragmentManager().getFragments() != null)
		{
			Fragment[] frags = getSupportFragmentManager().getFragments().toArray(new Fragment[0]);
			for (int i = 0; i < frags.length; i++)
			{
				Fragment fragment = frags[i];
				if (fragment != null)
					getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			}
		}

		mRenovations3DPagerAdapter = new Renovations3DPagerAdapter(getSupportFragmentManager(), renovations3D);

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
	 * This must be called eventually by Renovations3D
	 */
	public void setUpViews()
	{
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mRenovations3DPagerAdapter);
		mViewPager.setCurrentItem(1);
		mViewPager.setOffscreenPageLimit(4);

		invalidateOptionsMenu();
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

			boolean homeLoaded = renovations3D.getHomeController() != null;
			menu.findItem(R.id.menu_save).setEnabled(homeLoaded);
			menu.findItem(R.id.menu_saveas).setEnabled(homeLoaded);
		}

		MenuItem newMI = menu.findItem(R.id.menu_new);
		String newStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "NEW_HOME.Name");
		SpannableStringBuilder builderNew = new SpannableStringBuilder("* " + newStr);
		builderNew.setSpan(new ImageSpan(this, android.R.drawable.ic_input_add), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		newMI.setTitle(builderNew);
		newMI.setTitleCondensed(newStr);

		//TODO: find a good load icon
		MenuItem loadMI = menu.findItem(R.id.menu_load);
		String loadStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "OPEN.Name");
		//SpannableStringBuilder builderLoad = new SpannableStringBuilder("* " + loadStr);
		//builderLoad.setSpan(new ImageSpan(this, android.R.drawable.ic_menu_add), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		//loadMI.setTitle(builderLoad);
		loadMI.setTitle(loadStr);
		loadMI.setTitleCondensed(loadStr);

		MenuItem saveMI = menu.findItem(R.id.menu_save);
		String saveStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "SAVE.Name");
		SpannableStringBuilder builderSave = new SpannableStringBuilder("* " + saveStr);
		builderSave.setSpan(new ImageSpan(this, android.R.drawable.ic_menu_save), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		saveMI.setTitle(builderSave);
		saveMI.setTitleCondensed(saveStr);

		MenuItem saveasMI = menu.findItem(R.id.menu_saveas);
		String saveasStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "SAVE_AS.Name");
		SpannableStringBuilder builderSaveas = new SpannableStringBuilder("* " + saveasStr);
		builderSaveas.setSpan(new ImageSpan(this, R.drawable.ic_menu_save_as), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		saveasMI.setTitle(builderSaveas);
		saveasMI.setTitleCondensed(saveasStr);

		MenuItem prefsMI = menu.findItem(R.id.menu_preferences);
		String prefsStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "PREFERENCES.Name");
		SpannableStringBuilder builderPref = new SpannableStringBuilder("* " + prefsStr);
		builderPref.setSpan(new ImageSpan(this, android.R.drawable.ic_menu_preferences), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		prefsMI.setTitle(builderPref);
		prefsMI.setTitleCondensed(prefsStr);

		MenuItem helpMI = menu.findItem(R.id.menu_help);
		String helpStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "HELP_MENU.Name");
		SpannableStringBuilder builderHelp = new SpannableStringBuilder("* " + helpStr);
		builderHelp.setSpan(new ImageSpan(this, android.R.drawable.ic_menu_help), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		helpMI.setTitle(builderHelp);
		helpMI.setTitleCondensed(helpStr);

		MenuItem aboutMI = menu.findItem(R.id.menu_about);
		String aboutStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ABOUT.Name");
		SpannableStringBuilder builderAbout = new SpannableStringBuilder("* " + aboutStr);
		builderAbout.setSpan(new ImageSpan(this, android.R.drawable.ic_menu_info_details), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		aboutMI.setTitle(builderAbout);
		aboutMI.setTitleCondensed(aboutStr);

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
				saveSh3dFile();
				return true;
			case R.id.menu_saveas:
				saveAsSh3dFile();

				return true;
			case R.id.menu_help:
				//if (renovations3D.getHomeController() != null)
				//	renovations3D.getHomeController().help();

				Renovations3DActivity.logFireBaseLevelUp("menu_help");
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				//TODO: externalize strings
				dialog.setTitle("Notice");
				dialog.setMessage(Html.fromHtml("This is the Sweet Home 3D desktop application's help system, it will not match exactly with Renovations 3D user interface"));
				dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						//TODO: this is a short term fix, I need to make a Renovations 3D branded version of this site
						Uri webpage = Uri.parse("http://www.sweethome3d.com/userGuide.jsp#drawingWalls");
						//localize like so
						// http://www.sweethome3d.com/fr/userGuide.jsp#drawingWalls
						// String language = Locale.getDefault().getLanguage();
						// fr etc

						Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
						if (intent.resolveActivity(getPackageManager()) != null)
						{
							startActivity(intent);
						}
					}
				});
				dialog.create().show();

				return true;
			case R.id.menu_about:
				Renovations3DActivity.logFireBaseLevelUp("menu_about");
				if (renovations3D.getHomeController() != null)
					renovations3D.getHomeController().about();
				return true;
			case R.id.menu_preferences:
				Renovations3DActivity.logFireBaseLevelUp("menu_preferences");
				if (renovations3D.getHomeController() != null)
					renovations3D.getHomeController().editPreferences();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onPause()
	{
		//This is where we do an extra auto save to ensure content always can be loaded back up
		System.out.println("onPause - auto saving");
		doAutoSave();
		super.onPause();
	}

	private void saveSh3dFile()
	{
		//I must get off the EDT as it may ask the question in a blocking manner
		Thread t = new Thread()
		{
			public void run()
			{
				if (renovations3D.getHomeController() != null)
				{
					Renovations3DActivity.logFireBaseContent("menu_save", renovations3D.getHome().getName());

					//Last minute desperate attempt to ensure save is never over the temp file, null will trigger save as
					//this may not be truly needed but better to be safe
					if (renovations3D.getHome().getName() != null && renovations3D.getHome().getName().contains("currentWork.sh3d"))
						renovations3D.getHome().setName(null);


					if(renovations3D.getHome().getName() != null && !renovations3D.getHome().isRepaired())
					{
						// we have a name already so record it
						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(STATE_TEMP_HOME_REAL_NAME, "");
						editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
						editor.putString(STATE_CURRENT_HOME_NAME, renovations3D.getHome().getName());
						editor.apply();
					}
					else
					{
						// no current name will get a modified homes change
						// record the name into prefs after it is changed
						PropertyChangeListener newNameListener = new PropertyChangeListener()
						{
							@Override
							public void propertyChange(PropertyChangeEvent evt)
							{
								SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
								SharedPreferences.Editor editor = settings.edit();
								editor.putString(STATE_TEMP_HOME_REAL_NAME, "");
								editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
								editor.putString(STATE_CURRENT_HOME_NAME, renovations3D.getHome().getName());
								editor.apply();
								// only once
								renovations3D.getUserPreferences().removePropertyChangeListener(UserPreferences.Property.RECENT_HOMES, this);
							}
						};

						renovations3D.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.RECENT_HOMES, newNameListener);
					}

					renovations3D.getHomeController().saveAndCompress();
				}
			}
		};
		t.start();
	}

	private void saveAsSh3dFile()
	{
		//I must get off the EDT and ask the question in a blocking manner
		Thread t2 = new Thread()
		{
			public void run()
			{
				if (renovations3D.getHomeController() != null)
				{
					Renovations3DActivity.logFireBaseContent("menu_saveas", renovations3D.getHome().getName());

					// record the name into prefs after it is changed
					PropertyChangeListener newNameListener = new PropertyChangeListener(){
						@Override
						public void propertyChange(PropertyChangeEvent evt)
						{
							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(STATE_TEMP_HOME_REAL_NAME, "");
							editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
							editor.putString(STATE_CURRENT_HOME_NAME, renovations3D.getHome().getName());
							editor.apply();

							// only once
							renovations3D.getUserPreferences().removePropertyChangeListener(UserPreferences.Property.RECENT_HOMES, this);
						}
					};

					renovations3D.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.RECENT_HOMES, newNameListener );
					renovations3D.getHomeController().saveAsAndCompress();
				}
			}
		};
		t2.start();

	}

	private void loadSh3dFile()
	{
		if (renovations3D.getHomeController() != null)
		{
			Thread t2 = new Thread()
			{
				public void run()
				{
					HomeController controller = renovations3D.getHomeController();
					if (controller == null)
						newHome();
					String openFileName = controller.getView().showOpenDialog();
					if (openFileName != null)
					{
						loadHome(new File(openFileName));
					}
				}
			};
			t2.start();
		}
		else
		{
			// above this is the proper way to do it, but if I don't have a home control...
			chooserStartFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

			final JFileChooser fileChooser = new JFileChooser(Renovations3DActivity.this, chooserStartFolder);
			fileChooser.setExtension("sh3d");
			fileChooser.setFileListener(new JFileChooser.FileSelectedListener()
			{
				@Override
				public void fileSelected(final File file)
				{
					chooserStartFolder = file;
					Thread t2 = new Thread()
					{
						public void run()
						{
							loadHome(file);
						}
					};
					t2.start();
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
		}
	}

	/**
	 * Only call when not on EDT as blocking save question may arise
	 */
	private void newHome()
	{
		// must get off the EDT thread as the close may ask for a save
		Thread t2 = new Thread()
		{
			public void run()
			{
				if (renovations3D.getHomeController() != null)
					renovations3D.getHomeController().close();

				// to be safe get this back onto the ui thread
				Renovations3DActivity.this.runOnUiThread(new Runnable()
				{
					public void run()
					{
						// must always recreate otherwise the pager hand out an IllegalStateException
						renovations3D = new Renovations3D(Renovations3DActivity.this);

						mRenovations3DPagerAdapter.setRenovations3D(renovations3D);
						//see http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android/26944013#26944013 for ensuring new fragments

						// discard the old view first
						mRenovations3DPagerAdapter.notifyChangeInPosition(1);
						mRenovations3DPagerAdapter.notifyDataSetChanged();

						// create new home and trigger the new views
						renovations3D.newHome();
						mRenovations3DPagerAdapter.notifyChangeInPosition(1);
						mRenovations3DPagerAdapter.notifyDataSetChanged();

						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(STATE_TEMP_HOME_REAL_NAME, "");
						editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
						editor.putString(STATE_CURRENT_HOME_NAME, "");
						editor.apply();
					}
				});
			}
		};
		t2.start();


	}

	/**
	 * Only call when not on EDT as blocking save question may arise
	 *
	 * @param homeFile
	 */
	public void loadHome(final File homeFile)
	{
		loadHome(homeFile, null, false, false);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(STATE_TEMP_HOME_REAL_NAME, "");
		editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
		editor.putString(STATE_CURRENT_HOME_NAME, homeFile.getAbsolutePath());
		editor.apply();
	}

	/**
	 * Only call when not on EDT as blocking save question may arise
	 *
	 * @param homeFile
	 * @param loadedFromTemp
	 */
	public void loadHome(final File homeFile, final String overrideName, final boolean isModifiedOverrideValue, final boolean loadedFromTemp)
	{
		if (homeFile != null)
		{
			if (renovations3D.getHomeController() != null)
				renovations3D.getHomeController().close();

			// to be safe get this back onto the ui thread
			Renovations3DActivity.this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					boolean prevHomeLoaded = renovations3D.getHomeController() != null;
					renovations3D = new Renovations3D(Renovations3DActivity.this);
					mRenovations3DPagerAdapter.setRenovations3D(renovations3D);
					//see http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android/26944013#26944013 for ensuring new fragments

					// discard the old view first
					mRenovations3DPagerAdapter.notifyChangeInPosition(1);
					mRenovations3DPagerAdapter.notifyDataSetChanged();

					renovations3D.loadHome(homeFile, overrideName, isModifiedOverrideValue, loadedFromTemp);

					// force the frags to load up now
					if (prevHomeLoaded)
					{
						mRenovations3DPagerAdapter.notifyChangeInPosition(1);
						mRenovations3DPagerAdapter.notifyDataSetChanged();
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
						File file = new File(new URI(localUri).getPath());
						Renovations3DActivity.logFireBaseLevelUp("onCompleteHTTPIntent.OnReceive", file.getAbsolutePath());
						loadFile(file);
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

					Toast.makeText(Renovations3DActivity.this, "DownloadFailedWithErrorMessage: " + message, Toast.LENGTH_SHORT).show();
					Renovations3DActivity.logFireBaseLevelUp("onCompleteHTTPIntent.OnReceiveError", message);
				}
			}
		}
	};


	private void loadFile(File inFile)
	{
		/*
		 I think I need to clean up any existing loading file now? don't I, coming in from a tap on teh internet
		 seems to leave teh old home in place some how? wooooahh hold on

		 If I come to a home form the internet I'm not reusing my current Activity at all!!!
		 I've now got 2 activities running at the same time!!
		 */
		if (inFile.getName().toLowerCase().endsWith(".sh3d"))
		{
			loadHome(inFile);
		}
		else if (inFile.getName().toLowerCase().endsWith(".sh3f"))
		{
			//TODO: not sure if this is needed? might start like this?
			newHome();
			HomeController controller = renovations3D.getHomeController();
			if (controller != null)
			{
				controller.importFurnitureLibrary(inFile.getAbsolutePath());
			}
		}
		else if (inFile.getName().toLowerCase().endsWith(".sh3t"))
		{
			newHome();
			HomeController controller2 = renovations3D.getHomeController();
			if (controller2 != null)
			{
				controller2.importTexturesLibrary(inFile.getAbsolutePath());
			}
		}
	}

	private void permissionGranted()
	{
		fileSystemAccessGranted = true;
		invalidateOptionsMenu();
		loadUpContent();
	}

	private void loadUpContent()
	{
		// set up the auto save system now as various things below call return
		final TimerTask autoSaveTask = new TimerTask()
		{
			public void run()
			{
				System.out.println("Possibly auto save");
				doAutoSave();
			}
		};
		autoSaveTimer.purge();// in case of restart we don't want 2 tasks running
		autoSaveTimer.scheduleAtFixedRate(autoSaveTask, 3 * 60 * 1000, 3 * 60 * 1000);

		//TODO: see eclipse Renovations3D.protected void start(String[] args) for exactly this setup, but better
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
					//Toast.makeText(Renovations3DActivity.this, "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name, Toast.LENGTH_LONG).show();
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
					Toast.makeText(Renovations3DActivity.this, "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name, Toast.LENGTH_LONG).show();

					File inFile = new File(uri.getPath());
					loadFile(inFile);

					setIntent(null);
					Renovations3DActivity.logFireBaseLevelUp("ImportFromFile", intent.getDataString());
					return;
				}
				else if (scheme.compareTo("http") == 0)
				{
					Toast.makeText(Renovations3DActivity.this, "http: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : ", Toast.LENGTH_LONG).show();

					Uri uri = intent.getData();
					final String fileName = uri.getLastPathSegment();

					Log.v("tag", "Http intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + fileName);
					Toast.makeText(Renovations3DActivity.this, "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + fileName, Toast.LENGTH_LONG).show();
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
					Toast.makeText(Renovations3DActivity.this, "Download started, please wait...", Toast.LENGTH_LONG).show();
					Renovations3DActivity.logFireBaseLevelUp("ImportFromHttp.enqueue", intent.getDataString());
					return;
				}
				else if (scheme.compareTo("ftp") == 0)
				{
					Renovations3DActivity.logFireBaseLevelUp("ImportFromFtp.enqueue", intent.getDataString());
					// TODO Import from FTP!
					Toast.makeText(Renovations3DActivity.this, "Import from ftp not supported: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : ", Toast.LENGTH_LONG).show();
					setIntent(null);
					return;
				}

			}
		}


		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int appOpenedCount = settings.getInt(APP_OPENED_COUNT, 0);
		appOpenedCount++;
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(APP_OPENED_COUNT, appOpenedCount);
		editor.apply();

		boolean firstOpening = appOpenedCount <= 1;
		if (firstOpening)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					String language = Locale.getDefault().getLanguage();
					List<String> supportedLanguages = Arrays.asList(renovations3D.getUserPreferences().getSupportedLanguages());
					if (supportedLanguages.contains(language))
					{
						renovations3D.getUserPreferences().setLanguage(language);
					}
				}
			});
		}


		// download managers only appeared in ginger bread
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			// now download the files from drop box
			// folder	String url = "https://www.dropbox.com/sh/m1291ug4cooafn9/AADthFyjnndOhjTvr2iiMJzWa?dl=0";

			//https://www.dropbox.com/s/ejfdbfopcat3c26/SweetHome3DExample2.sh3d?dl=0
			//https://www.dropbox.com/s/ird6ysp4cai1wej/SweetHome3DExample3.sh3d?dl=0

			// notice the files actual url not the sexy page offering the file
			String[] urls = new String[]{
					"https://dl.dropboxusercontent.com/s/ejfdbfopcat3c26/SweetHome3DExample2.sh3d",
					"https://dl.dropboxusercontent.com/s/ird6ysp4cai1wej/SweetHome3DExample3.sh3d"};
			String[] fileNames = new String[]{
					"SweetHome3DExample2.sh3d",
					"SweetHome3DExample3.sh3d"};

			// only download attempt 3 times, after that consider it impossible or the user sick of it
			int downloadAttempts = settings.getInt(EXAMPLE_DOWNLOAD_COUNT, 0);
			if (!(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNames[0]).exists()))
			{
				downloadAttempts++;
				editor.putInt(EXAMPLE_DOWNLOAD_COUNT, downloadAttempts);
				editor.apply();
			}

			if (downloadAttempts < 4)
			{
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
						Renovations3DActivity.logFireBaseContent("DownloadManager.enqueue", "fileName: " + fileName);

						// if this is first ever opening then we should open the SweetHome3DExample2.sh3d file
						if (firstOpening && fileName.equals("SweetHome3DExample2.sh3d"))
						{
							registerReceiver(onCompleteHTTPIntent, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
							return;
						}
					}
				}
			}
		}

		if (!firstOpening)
		{
			// do we have a temp home we were working on
			String unmodifiedFileName = settings.getString(STATE_CURRENT_HOME_NAME, "");
			if (unmodifiedFileName.length() > 0)
			{
				File homeName = new File(unmodifiedFileName);
				Renovations3DActivity.logFireBaseContent("loadUpContentSTATE_CURRENT_HOME_NAME", "homeName: " + homeName.getName());
				loadHome(homeName);
			}
			else
			{
				Region r;
				//This is where we open the temp file that was last used
				String tempWorkingFileRealName = settings.getString(STATE_TEMP_HOME_REAL_NAME, null);
				boolean isModifiedOverrideValue = settings.getBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
				File outputDir = getCacheDir();
				File homeName = new File(outputDir, "currentWork.sh3d");
				if (homeName.exists())
				{
					Renovations3DActivity.logFireBaseContent("loadUpContentCurrentWork", "temp: " + homeName.getName() + " original: " + tempWorkingFileRealName
							+ " isModifiedOverrideValue: " + isModifiedOverrideValue);

					loadHome(homeName, tempWorkingFileRealName, isModifiedOverrideValue, true);
				}
				else
				{
					Renovations3DActivity.logFireBaseContent("loadUpContentCurrentWorkNoFile", "original: " + tempWorkingFileRealName);
					// or just fire up a lovely clear new home
					newHome();
				}
			}
		}
		else
		{
			String autoOpenFirstOpen = "SweetHome3DExample2.sh3d";
			File autoOpenFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), autoOpenFirstOpen);
			if (autoOpenFile.exists())
			{
				Renovations3DActivity.logFireBaseContent("loadUpContentFirstOpenAutoFile", "autoOpenFile: " + autoOpenFile.getName());
				loadHome(autoOpenFile);
			}
			else
			{
				Renovations3DActivity.logFireBaseContent("loadUpContentFirstOpenNoAutoFile");
				newHome();
			}
		}


	}

	final Semaphore dialogSemaphore = new Semaphore(1, true);

	private void doAutoSave()
	{
		if (renovations3D != null && renovations3D.getHome() != null)
		{
			// get off the caller in case this is an onPause
			Thread t = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						// all synchro-meshed and sexy
						dialogSemaphore.acquire();
					}
					catch (InterruptedException e)
					{
					}

					if (renovations3D.getHome() != null && renovations3D.getHome().isModified())
					{
						try
						{
							//clone so original modified flag is not changed
							Home autoSaveHome = renovations3D.getHome().clone();
							boolean isModifiedOverrideValue = renovations3D.getHome().isModified();
							String originalName = autoSaveHome.getName();
							File outputDir = getCacheDir();
							File homeName = new File(outputDir, "currentWork.sh3d");
							// not using HomeRecorder.Type.COMPRESSED I feel it is super slow, compare a normal save press
							renovations3D.getHomeRecorder().writeHome(autoSaveHome, homeName.getAbsolutePath());
							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(STATE_TEMP_HOME_REAL_NAME, originalName);
							editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, isModifiedOverrideValue);

							// clear out the indicator of a cleanly opened unaltered file
							editor.putString(STATE_CURRENT_HOME_NAME, "");
							editor.apply();

							Renovations3DActivity.logFireBaseContent("doAutoSave", "temp: " + homeName.getName() + " original: " + originalName
									+ " isModifiedOverrideValue: " + isModifiedOverrideValue);

						}
						catch (RecorderException e)
						{
							e.printStackTrace();
						}
					}
					dialogSemaphore.release();
				}
			});
			t.start();

		}
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
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					// Permission Granted
					permissionGranted();
				}
				else
				{
					// Permission Denied
					Toast.makeText(Renovations3DActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}


}
