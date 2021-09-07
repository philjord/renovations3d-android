package com.eteks.renovations3d;


import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eteks.renovations3d.android.SwingTools;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.io.HomeStreamRecorder;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.mindblowing.swingish.JFileChooser;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.utils.SopInterceptor;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

import org.jogamp.java3d.JoglesPipeline;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.activity.result.ActivityResult;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import javaawt.EventQueue;
import javaawt.VMEventQueue;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import me.drakeet.support.toast.ToastCompat;

import static android.os.Build.VERSION_CODES.M;


/**
 * Created by phil on 11/20/2016.
 */
public class Renovations3DActivity extends FragmentActivity {
	public static final String PREFS_NAME = "SweetHomeAVRActivityDefault";// can't touch as current users use this!

	public static final String CURRENT_WORK_FILENAME = "currentWork.sh3d";
	public static final String LANGUAGE_SET_ON_FIRST_USE = "LANGUAGE_SET_ON_FIRST_USE";
	public static final String SHOW_PAGER_BUTTONS_PREF = "SHOW_PAGER_BUTTONS_PREF";
	public static final String SHOW_PLAN_ZOOM_BUTTONS_PREF = "SHOW_PLAN_ZOOM_BUTTONS_PREF";
	public static final String DOUBLE_TAP_EDIT_2D_PREF = "DOUBLE_TAP_EDIT_2D_PREF";
	public static final String DOUBLE_TAP_EDIT_3D_PREF = "DOUBLE_TAP_EDIT_3D_PREF";

	public static boolean SHOW_PAGER_BUTTONS = true;
	public static boolean SHOW_PLAN_ZOOM_BUTTONS = false;
	public static boolean DOUBLE_TAP_EDIT_2D = false;
	public static boolean DOUBLE_TAP_EDIT_3D = false;

	private static String STATE_TEMP_HOME_REAL_NAME = "STATE_TEMP_HOME_REAL_NAME";
	private static String STATE_TEMP_HOME_REAL_MODIFIED = "STATE_TEMP_HOME_REAL_MODIFIED";

	// used as a modal mouse click blocker
	public AndroidDialogView currentDialog = null;

	private static FirebaseAnalytics mFirebaseAnalytics;

	private Renovations3DPagerAdapter mRenovations3DPagerAdapter;

	private ViewPager mViewPager; // public to allow fragment to move around by button

	private static HashSet<String> welcomeScreensShownThisSession = new HashSet<String>();

	//public static boolean writeExternalStorageGranted = false;
	//public static File downloadsLocation;

	private final Timer autoSaveTimer = new Timer("autosave", true);

	private BillingManager billingManager;
	private ImageAcquireManager imageAcquireManager;
	private ImportManager importManager;
	private AdMobManager adMobManager;
	private Tutorial tutorial;
	public final FileActivityResult<Intent, ActivityResult> fileActivityLauncher = FileActivityResult.registerActivityForResult(this);

	private Renovations3D renovations3D; // for plan undo redo, now for import statements too


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
	public static void logFireBaseContent(String id, String value) {
		logFireBase(FirebaseAnalytics.Event.SELECT_CONTENT, id, value);
	}

	/**
	 * For generally doing day to day things
	 *
	 * @param id short id (method name?)
	 */
	public static void logFireBaseContent(String id) {
		logFireBase(FirebaseAnalytics.Event.SELECT_CONTENT, id, null);
	}

	/**
	 * For more unusual event that represent exploring/exploiting the system
	 *
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBaseLevelUp(String id, String value) {
		logFireBase(FirebaseAnalytics.Event.LEVEL_UP, id, value);
	}

	/**
	 * For more unusual event that represent exploring/exploiting the system
	 *
	 * @param id short id (method name?)
	 */
	public static void logFireBaseLevelUp(String id) {
		logFireBase(FirebaseAnalytics.Event.LEVEL_UP, id, null);
	}

	/**
	 * @param event Event MUST be FirebaseAnalytics.Event.*
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBase(String event, String id, String value) {
		System.out.println("logFireBase : " + id + (value != null ? (" [" + value + "]") : ""));
		if (mFirebaseAnalytics != null && !BuildConfig.DEBUG) {
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
			if (value != null && value.length() > 0) {
				bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, value);
			}
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
		storagePermission();
	}*/

	public void onCreate(Bundle savedInstanceState) {
		//System.setProperty("jogl.verbose", "true");
		//System.setProperty("jogl.debug", "true");

		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");
		System.setProperty("j3d.usePbuffer", "true"); // some phones have no fbo under offscreen
		System.setProperty("j3d.noDestroyContext", "true");// don't clean up as the preserve/restore will handle it

		JoglesPipeline.LATE_RELEASE_CONTEXT = false;// so the world can clean up, otherwise the plan renderer holds onto it

		javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
		javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);
		javaawt.EventQueue.installEventQueueImpl(VMEventQueue.class);
		VMEventQueue.activity = this;
		SimpleShaderAppearance.setVersionES100();

		super.onCreate(savedInstanceState);

		// Obtain the FirebaseAnalytics instance.
		if (!BuildConfig.DEBUG) {
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		}

		setContentView(R.layout.main);

		this.imageAcquireManager = new ImageAcquireManager(this);
		// set up billing manager will call back adsmanager once connected
		this.billingManager = new BillingManager(this);
		this.importManager = new ImportManager(this);
		this.adMobManager = new AdMobManager(this);
		this.tutorial = new Tutorial(this, (ViewGroup) this.findViewById(R.id.tutorial));

		SwingTools.setResolutionScale(this);

		ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		OperatingSystem.applicationInfoDataDir = getApplicationInfo().dataDir;

		// if we have any fragments in the manager then we are doing a restore with bundle style,
		// so all frags will get onCreate() but not the init() and fail, let's chuck em away now
		if (getSupportFragmentManager().getFragments() != null) {
			Fragment[] frags = getSupportFragmentManager().getFragments().toArray(new Fragment[0]);
			for (int i = 0; i < frags.length; i++) {
				Fragment fragment = frags[i];
				if (fragment != null)
					getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			}
		}

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SHOW_PAGER_BUTTONS = settings.getBoolean(SHOW_PAGER_BUTTONS_PREF, true);
		SHOW_PLAN_ZOOM_BUTTONS = settings.getBoolean(SHOW_PLAN_ZOOM_BUTTONS_PREF, false);
		DOUBLE_TAP_EDIT_2D = settings.getBoolean(DOUBLE_TAP_EDIT_2D_PREF, false);
		DOUBLE_TAP_EDIT_3D = settings.getBoolean(DOUBLE_TAP_EDIT_3D_PREF, false);

		// If we were given locations permission at some point then use it here now for ads, but don't ask for it (the compass does that)
		if (Build.VERSION.SDK_INT >= M) {
			locationPermission(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
		} else {
			locationPermission(true);
		}

		billingManager.initialize();
		invalidateOptionsMenu();

		// set up the auto save system now as various things below call return
		final TimerTask autoSaveTask = new TimerTask() {
			public void run() {
				System.out.println("Possibly auto save");
				doAutoSave();
			}
		};
		autoSaveTimer.purge();// in case of restart we don't want 2 tasks running
		//TODO: in fact use the users preference for teh prefs dialog, this is 1 minute in ms
		autoSaveTimer.scheduleAtFixedRate(autoSaveTask, 1 * 60 * 1000, 1 * 60 * 1000);

		loadUpContent();

	}



	public ViewPager getViewPager() {
		return mViewPager;
	}

	public HashSet<String> getWelcomeScreensShownThisSession() {
		return welcomeScreensShownThisSession;
	}

	public Tutorial getTutorial() {
		return tutorial;
	}

	/**
	 * This must be called eventually by Renovations3D
	 */
	public void setUpViews() {
		mViewPager = (ViewPager) findViewById(R.id.pager);
		//see https://github.com/umano/AndroidSlidingUpPanel/issues/590
		// I got IndexOutOfBound when dragging (in a double call case or perhaps a very slow start up time)
		//https://console.firebase.google.com/u/0/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/9db15004?duration=2592000000

		//FIXME:!! this solution stops changes happening at all! need a better answer
		//if(mViewPager.getAdapter() == null){
			mViewPager.setAdapter(mRenovations3DPagerAdapter);
			mViewPager.setCurrentItem(1);
			mViewPager.setOffscreenPageLimit(4);
		//} else {
		//  mRenovations3DPagerAdapter.notifyChangeInPosition(4);
		//	mRenovations3DPagerAdapter.notifyDataSetChanged();
		//}

		invalidateOptionsMenu();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// these three always on, but save location modified
		menu.findItem(R.id.menu_load).setEnabled(true);

		boolean homeLoaded = renovations3D != null && renovations3D.getHomeController() != null;
		menu.findItem(R.id.menu_save).setEnabled(homeLoaded);
		menu.findItem(R.id.menu_saveas).setEnabled(homeLoaded);


		// should this be on the menu at all?
		if (this.getBillingManager().ownsBasicAdFree()) {
			menu.removeItem(R.id.basic_remove_ads);
		} else {
			MenuItem removeAdsMI = menu.findItem(R.id.basic_remove_ads);
			String removeAdsStr = this.getString(R.string.basic_remove_ads);
			setIconizedMenuTitle(removeAdsMI, removeAdsStr, R.drawable.ic_shopping_cart_black_24dp, this);
		}

		if (renovations3D != null) {
			MenuItem fileMI = menu.findItem(R.id.menu_file);
			String fileStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "FILE_MENU.Name");
			fileMI.setTitle(fileStr + "...");
			fileMI.setTitleCondensed(fileStr);

			MenuItem newMI = menu.findItem(R.id.menu_new);
			String newStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "NEW_HOME.Name");
			setIconizedMenuTitle(newMI, newStr, android.R.drawable.ic_input_add, this);

			MenuItem newFromExampleMI = menu.findItem(R.id.menu_new_from_example);
			String newFromExampleStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "NEW_HOME_FROM_EXAMPLE.Name");
			setIconizedMenuTitle(newFromExampleMI, newFromExampleStr, android.R.drawable.ic_input_add, this);

			MenuItem loadMI = menu.findItem(R.id.menu_load);
			String loadStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "OPEN.Name");
			setIconizedMenuTitle(loadMI, loadStr, R.drawable.ic_open_in_new_black_24dp, this);

			MenuItem  saveMI = menu.findItem(R.id.menu_save);
			String  saveStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "SAVE.Name");
			setIconizedMenuTitle(saveMI, saveStr, android.R.drawable.ic_menu_save, this);

			MenuItem saveAsMI = menu.findItem(R.id.menu_saveas);
			String saveAsStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "SAVE_AS.Name");
			setIconizedMenuTitle(saveAsMI, saveAsStr, R.drawable.ic_menu_save_as, this);

			MenuItem shareMI = menu.findItem(R.id.menu_share);
			String shareStr = getResources().getString(R.string.share);
			setIconizedMenuTitle(shareMI, shareStr, R.drawable.ic_send_black_24dp, this);

			MenuItem prefsMI = menu.findItem(R.id.menu_preferences);
			String prefsStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "PREFERENCES.Name");
			setIconizedMenuTitle(prefsMI, prefsStr, android.R.drawable.ic_menu_preferences, this);

			MenuItem helpMI = menu.findItem(R.id.menu_help);
			String helpStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "HELP_MENU.Name");
			setIconizedMenuTitle(helpMI, helpStr, android.R.drawable.ic_menu_help, this);

			MenuItem onlineHelpMI = menu.findItem(R.id.menu_online_help);
			String onlineHelpStr = getResources().getString(R.string.menu_help_online);
			setIconizedMenuTitle(onlineHelpMI, onlineHelpStr, android.R.drawable.ic_menu_help, this);

			MenuItem aboutMI = menu.findItem(R.id.menu_about);
			String aboutStr = renovations3D.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "ABOUT.Name");
			setIconizedMenuTitle(aboutMI, aboutStr, android.R.drawable.ic_menu_info_details, this);

		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.basic_remove_ads:
				this.billingManager.buyBasicAdFree();
				return true;
			case R.id.menu_new:
				newHome();
				getAdMobManager().eventTriggered(AdMobManager.InterstitialEventType.NEW_HOME);
				return true;
			case R.id.menu_new_from_example:
				newHomeFromExample();
				return true;
			case R.id.menu_load:
				loadSh3dFile();
				return true;
			case R.id.menu_save:
				saveSh3dFile();
				return true;
			case R.id.menu_saveas:
				saveAsSh3dFile();
				getAdMobManager().eventTriggered(AdMobManager.InterstitialEventType.HOME_SAVE_AS);
				return true;
			case R.id.menu_share:
				share();
				getAdMobManager().eventTriggered(AdMobManager.InterstitialEventType.HOME_SHARE);
				return true;
			case R.id.menu_help_tutorial:
				tutorial.setEnable(true);
				return true;
			case R.id.menu_online_help:
				showHelp();
				return true;
			case R.id.menu_about:
				Renovations3DActivity.logFireBaseLevelUp("menu_about");
				if (renovations3D != null && renovations3D.getHomeController() != null)
					renovations3D.getHomeController().about();
				return true;

			case R.id.menu_privacy:
				Renovations3DActivity.logFireBaseLevelUp("menu_privacy");
				//TODO: localize the page and make this like menu_help
				String urlStr = "https://sites.google.com/view/renovations3d";
				Uri webpage = Uri.parse(urlStr);
				Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				}
				return true;
			case R.id.menu_preferences:
				Renovations3DActivity.logFireBaseLevelUp("menu_preferences");
				if (renovations3D != null && renovations3D.getHomeController() != null)
					renovations3D.getHomeController().editPreferences();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}



	private void showHelp() {
		//if (renovations3D.getHomeController() != null)
		//	renovations3D.getHomeController().help();

		Renovations3DActivity.logFireBaseLevelUp("menu_help");
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(getString(R.string.helpRedirectNotice));
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				// build a localized version if possible
				String language = Locale.getDefault().getLanguage();

				// note in order
				String[] localizedHelp = {
						"bg",
						"de",
						"es",
						"fr",
						"it",
						"pt",
						"ru",
						"th",
						"zh-cn",
						"zh-tw"
				};

				String urlStr = "http://www.sweethome3d.com";
				if (Arrays.binarySearch(localizedHelp, language) >= 0) {
					// http://www.sweethome3d.com/fr/userGuide.jsp#drawingWalls
					urlStr += "/" + language;
				}

				urlStr += "/userGuide.jsp#drawingWalls";
				Uri webpage = Uri.parse(urlStr);


				Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				}
			}
		});
		dialog.create().show();
	}


	@Override
	public void onPause() {
		//This is where we do an extra auto save to ensure content always can be loaded back up
		System.out.println("onPause - auto saving");
		doAutoSave();

		super.onPause();

		if(tutorial != null)
			tutorial.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.imageAcquireManager.onDestroy();
		this.billingManager.onDestroy();
	}

	@Override
	public void onResume() {
		System.out.println("onResume");
		super.onResume();

		if(tutorial != null)
			tutorial.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void recordHomeStateInPrefs(String tempHomeRealName, boolean tempHomeRealModified) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(STATE_TEMP_HOME_REAL_NAME, tempHomeRealName);
		editor.putBoolean(STATE_TEMP_HOME_REAL_MODIFIED, tempHomeRealModified);
		editor.apply();
	}


	/**
	 * NOTE! you must check for the often null return!
	 *
	 * @return
	 */
	public HomeController getHomeController() {
		if (renovations3D != null)
			return renovations3D.getHomeController();
		else
			return null;
	}

	/**
	 * NOTE! you must check for the often null return!
	 *
	 * @return
	 */
	public Home getHome() {
		if (renovations3D != null)
			return renovations3D.getHome();
		else
			return null;
	}

	private void saveSh3dFile() {
		//I must get off the EDT as it may ask the question in a blocking manner
		Thread t = new Thread() {
			public void run() {
				final Home home = getHome();
				if (getHomeController() != null && home != null) {
					String homeName = home.getName();
					Renovations3DActivity.logFireBaseContent("menu_save", homeName);

					//Last minute desperate attempt to ensure save is never over the temp file, null will trigger save as
					//this may not be truly needed but better to be safe
					if (homeName != null && (homeName.contains(CURRENT_WORK_FILENAME) ||  homeName.length() == 0)) {
						renovations3D.getHome().setName(null);
						homeName = null;
					}

					if (homeName != null && !home.isRepaired()) {
						// update prefs to base a check of a ral file save
						recordHomeStateInPrefs(homeName, false);
					} else {
						// no current name will get a modified homes change
						// record the name into prefs after it is changed
						PropertyChangeListener newNameListener = new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if(getHome() != null) {
									recordHomeStateInPrefs(getHome().getName(), false);
								}
								// only once
								if (getUserPreferences() != null)
									getUserPreferences().removePropertyChangeListener(UserPreferences.Property.RECENT_HOMES, this);
							}
						};
						if (getUserPreferences() != null)
							getUserPreferences().addPropertyChangeListener(UserPreferences.Property.RECENT_HOMES, newNameListener);
					}

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
						String saveHomeName = new File(homeName).getName() + (homeName.endsWith(".sh3d") ? "" : ".sh3d"); // strip path from filename and add ext if required
						HomeController homeController = renovations3D.getHomeController();
						final Home savedHome;
						try {
							savedHome = home.clone();
						} catch (RuntimeException var7) {
							homeController.getView().showError(
									getUserPreferences().getLocalizedString(HomeController.class, "saveError", new Object[]{saveHomeName, var7}));
							throw var7;
						}

						Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);

						String[] projection = new String[] {
								MediaStore.Downloads._ID,
								MediaStore.Downloads.DISPLAY_NAME
						};
						String selection = MediaStore.Downloads.DISPLAY_NAME +
								" = ?";
						String[] selectionArgs = new String[] {saveHomeName};

						String sortOrder = MediaStore.Downloads.DISPLAY_NAME + " ASC";

						Cursor cursor = null;
						try {
							cursor = getApplicationContext().getContentResolver().query(
								collection,
								projection,
								selection,
								selectionArgs,
								sortOrder);

							// Cache column indices.
							int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID);
							int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME);

							// I hope there is only one!
							if (cursor.moveToNext()) {
								// Get values of columns for a given video.
								long id = cursor.getLong(idColumn);
								String name = cursor.getString(nameColumn);

								Uri outputFileUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);

								ContentResolver resolver = getApplicationContext().getContentResolver();
								try {
									OutputStream out = resolver.openOutputStream(outputFileUri);
									((HomeStreamRecorder)Renovations3DActivity.this.renovations3D.getHomeStreamRecorder(HomeRecorder.Type.COMPRESSED)).writeHome(savedHome, out);

									cursor = getContentResolver().query(outputFileUri, new String[]{MediaStore.Downloads.DISPLAY_NAME},null,null,null);
									cursor.moveToNext();

									//As the file name is unknown to the user show it in a toast
									runOnUiThread(()->{ToastCompat.makeText(Renovations3DActivity.this, "Saved as " + name, Toast.LENGTH_SHORT).show();});
								} catch (IOException e) {
									// https://developer.android.com/training/data-storage/shared/media
									//If your app tries to access a file using the File API and it doesn't have the necessary permissions, a FileNotFoundException occurs.
									// give it a new file name by way of saveas
									saveAsSh3dFile();
								}
								catch (RecorderException e) {
									homeController.getView().showError(
											getUserPreferences().getLocalizedString(HomeController.class, "saveError", new Object[]{saveHomeName, e}));
								}
							} else {
								// no pre saved file of this name that is also owned by this app, so go saveas
								saveAsSh3dFile();
							}

						} finally {
							cursor.close();
						}
					} else {
						// on android < 11 I need to see if the homename is a valid file now otherwise open the saveas dialog
						// rather than simply failing with a file not found error
						File checkOutput = new File(home.getName());
						if(!checkOutput.exists() || checkOutput.canWrite()) {
							getHomeController().saveAndCompress();
						} else {
							getHomeController().saveAsAndCompress();
						}
					}
					getAdMobManager().interstitialDisplayPoint();
				}
			}
		};
		t.start();
	}

	private void saveAsSh3dFile() {
		//I must get off the EDT and ask the question in a blocking manner
		Thread t2 = new Thread() {
			public void run() {
				Home home = getHome();
				if (getHomeController() != null && home != null) {
					String homeName = home.getName(); //this may include path
					Renovations3DActivity.logFireBaseContent("menu_saveas", homeName);

					// in android 11 onwards all homes are saved to the download loads, FileContentManager is not used for saves
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
						// this wants files, only
						//getHomeController().saveAsAndCompress();
						String saveHomeName = new File(homeName).getName() + (homeName.endsWith(".sh3d") ? "" : ".sh3d"); // strip path from filename and add ext if required

						HomeController homeController = renovations3D.getHomeController();
						final Home savedHome;
						try {
							savedHome = home.clone();
						} catch (RuntimeException var7) {
							homeController.getView().showError(
									getUserPreferences().getLocalizedString(HomeController.class, "saveError", new Object[]{saveHomeName, var7}));
							throw var7;
						}

						// just let the media store know about it and save it to our local image file location
						ContentValues values = new ContentValues();
						values.put(MediaStore.Downloads.TITLE, saveHomeName);
						values.put(MediaStore.Downloads.DISPLAY_NAME, saveHomeName);
						values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");// MediaStore.Downloads.CONTENT_TYPE);// "application/sh3d");
						values.put(MediaStore.Downloads.DATE_ADDED, System.currentTimeMillis() / 1000);
						values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS );

						// notice this is not pending as we need the new file name now
						Uri outputFileUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

						Cursor cursor = getContentResolver().query(outputFileUri, new String[]{MediaStore.Downloads.DISPLAY_NAME},null,null,null);
						//TODO: what does false mean??
						if (cursor.moveToNext()) {

							// to get the new name, if a (x) has been added
							String newHomeName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME));

							//change the savedhome name to record it as matching the file
							savedHome.setName(newHomeName);

							// make it pending
							values = new ContentValues();
							values.put(MediaStore.Downloads.IS_PENDING, true);
							getContentResolver().update(outputFileUri, values, null, null);

							try {

								// actually write the home out to the stream
								OutputStream out = getContentResolver().openOutputStream(outputFileUri);
								((HomeStreamRecorder) Renovations3DActivity.this.renovations3D.getHomeStreamRecorder(HomeRecorder.Type.COMPRESSED)).writeHome(savedHome, out);

								// set pending to false now its complete
								values = new ContentValues();
								values.put(MediaStore.Downloads.IS_PENDING, false);
								getContentResolver().update(outputFileUri, values, null, null);

								// notice this may have been alters to (x) version
								home.setName(newHomeName);
								home.setModified(false);
								home.setRecovered(false);
								home.setRepaired(false);
								home.setVersion(savedHome.getVersion());

								// remember the name of the home file, for reload and autosave etc
								recordHomeStateInPrefs(newHomeName, false);

								//As the file name may be unknown to the user, show it in a toast
								String toastMessage = "Saved as " + newHomeName;
								runOnUiThread(() -> {ToastCompat.makeText(Renovations3DActivity.this, toastMessage, Toast.LENGTH_SHORT).show();});

							} catch (RecorderException | FileNotFoundException e) {
								homeController.getView().showError(
										getUserPreferences().getLocalizedString(HomeController.class, "saveError", new Object[]{saveHomeName, e}));
							}
						}
					} else {

						// record the name into prefs after it is changed
						PropertyChangeListener newNameListener = new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								recordHomeStateInPrefs(getHome().getName(), false);
								// only once
								if (getUserPreferences() != null)
									getUserPreferences().removePropertyChangeListener(UserPreferences.Property.RECENT_HOMES, this);
							}
						};

						if (getUserPreferences() != null)
							getUserPreferences().addPropertyChangeListener(UserPreferences.Property.RECENT_HOMES, newNameListener);

						getHomeController().saveAsAndCompress();
					}

					getAdMobManager().interstitialDisplayPoint();
				}
			}
		};
		t2.start();

	}

	private void share() {
		//https://developer.android.com/training/basics/intents/sending.html

		Home home = getHome();
		if(home != null) {
			final Home autoSaveHome = home.clone();
			Thread t = new Thread(new Runnable() {
				public void run() {
					//name will be null for a new home and "" for some corrupted autosaves
					// to share with a name the user needs to do a save as
					String homeName = autoSaveHome.getName();
					homeName = (homeName == null || homeName.length() == 0) ? "Home.sh3d" : homeName;
					String saveHomeName = new File(homeName).getName() + (homeName.endsWith(".sh3d") ? "" : ".sh3d"); // strip path from filename and add ext if required


					Renovations3DActivity.logFireBaseContent("shareemail_start", "homeName: " +  saveHomeName);
					final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("text/*");

					String subjectText = getResources().getString(R.string.app_name) + " " + saveHomeName;
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subjectText);
					//emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Attached");

					//let's do a temp save, can't use GetCacheDir as the getUriForFile below fails
					File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
					File homeFile = new File(storageDir, saveHomeName);

					try {
						//NOTE compressed slower
						renovations3D.getHomeRecorder(HomeRecorder.Type.COMPRESSED).writeHome(autoSaveHome, homeFile.getAbsolutePath());

						Uri outputFileUri = null;
						if (Build.VERSION.SDK_INT > M) {
							outputFileUri = FileProvider.getUriForFile(Renovations3DActivity.this, getApplicationContext().getPackageName() + ".provider", homeFile);
						} else {
							outputFileUri = Uri.fromFile(homeFile);
						}
						emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						emailIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);

						Renovations3DActivity.this.runOnUiThread(new Runnable(){public void run(){
							// Always use string resources for UI text.
							// This says something like "Share this photo with"
							String title = getResources().getString(R.string.share);
							// Create intent to show chooser
							Intent chooser = Intent.createChooser(emailIntent, title);

							// Verify the intent will resolve to at least one activity
							if (emailIntent.resolveActivity(getPackageManager()) != null) {
								startActivity(chooser);
							}
							Renovations3DActivity.logFireBaseContent("shareemail_end", "homeName: " + saveHomeName);}});
					} catch (RecorderException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}

	private void loadSh3dFile() {
		// get off EDT for controller.getView().showOpenDialog() call
		Thread t2 = new Thread() {
			public void run() {
				if (getHomeController() != null) {
					HomeController controller = getHomeController();
					String openFileName = controller.getView().showOpenDialog();
					if (openFileName != null) {
						loadHome(new File(openFileName));
					}
				}
			}
		};
		t2.start();
	}

	private void newHomeFromExample() {

		//!!copy of the loadHome code below
		// get off EDT for showOpenDialog call
		Thread t2 = new Thread() {
			public void run() {
				if (getHomeController() != null) {
					HomeController controller = getHomeController();
					final String exampleName = controller.getView().showNewHomeFromExampleDialog();
					if (exampleName != null) {
						loadHomeFromExample(exampleName);
					}
				}
			}
		};
		t2.start();
	}

	private void loadHomeFromExample(String exampleName) {

		// must get off the EDT thread as the close may ask for a save
		Thread t2 = new Thread() {
			public void run() {
				//trigger save of current
				if (getHomeController() != null)
					getHomeController().close();

				// to be safe get this back onto the ui thread
				Renovations3DActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Thread t = new Thread(new Runnable() {
							public void run() {

								//NOTE! the loadHome below has this exact lines of code, but in this call it causes crases
								// with some odd fragment manager issue I don't understand
								/*renovations3D = new Renovations3D(Renovations3DActivity.this);

								if (mRenovations3DPagerAdapter != null) {
									// discard the old view first
									mRenovations3DPagerAdapter.notifyChangeInPosition(4);
									mRenovations3DPagerAdapter.notifyDataSetChanged();
								}

								// must recreate this each time TEST that this is not holding onto views and causing a memory leak on reload of homes
								mRenovations3DPagerAdapter = new Renovations3DPagerAdapter(getSupportFragmentManager());

								mRenovations3DPagerAdapter.setRenovations3D(renovations3D);*/

								renovations3D.addOnHomeLoadedListener(new Renovations3D.OnHomeLoadedListener() {
									@Override
									public void onHomeLoaded(Home home, final HomeController homeController) {
										Renovations3DActivity.this.runOnUiThread(new Runnable() {
											public void run() {
												mRenovations3DPagerAdapter.notifyChangeInPosition(4);
												mRenovations3DPagerAdapter.notifyDataSetChanged();
											}
										});
									}
								});
								renovations3D.loadHomeFromExample(exampleName);
								recordHomeStateInPrefs("", false);
							}
						}
						);
						t.start();
					}
				});
			}
		};
		t2.start();
	}
	/**
	 * Only call when not on EDT as blocking save question may arise
	 */
	private void newHome() {
		loadHome(null);
	}

	/**
	 * Only call when not on EDT as blocking save question may arise
	 *
	 * @param homeFile
	 */
	public void loadHome(final Object homeFile) {
		loadHome(homeFile, null, false, false);
	}

	/**
	 * Only call when not on EDT as blocking save question may arise
	 *
	 * @param homeFile must be a file or an inputstream
	 * @param loadedFromTemp
	 */
	public void loadHome(final Object homeFile, final String overrideName, final boolean isModifiedOverrideValue, final boolean loadedFromTemp) {
		// must get off the EDT thread as the close may ask for a save
		Thread t2 = new Thread() {
			public void run() {
				//trigger save of current
				if (getHomeController() != null)
					getHomeController().close();

				// to be safe get this back onto the ui thread
				Renovations3DActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						renovations3D = new Renovations3D(Renovations3DActivity.this);

						if (mRenovations3DPagerAdapter != null) {
							// discard the old view first
							mRenovations3DPagerAdapter.notifyChangeInPosition(4);
							mRenovations3DPagerAdapter.notifyDataSetChanged();
						}

						// must recreate this each time TEST that this is not holding onto views and causing a memory leak on reload of homes
						mRenovations3DPagerAdapter = new Renovations3DPagerAdapter(getSupportFragmentManager(), Renovations3DActivity.this);

						mRenovations3DPagerAdapter.setRenovations3D(renovations3D);
						//see http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android/26944013#26944013 for ensuring new fragments


						//after the home is loaded check for an import wizard wanted and show interstitials
						renovations3D.addOnHomeLoadedListener(new Renovations3D.OnHomeLoadedListener() {
							@Override
							public void onHomeLoaded(Home home, final HomeController homeController) {
								if(importDestination != null) {
									switch (importDestination) {
										case IMPORT_TEXTURE:
											Handler handler = new Handler(Looper.getMainLooper());
											handler.post(new Runnable() {
												public void run() {
													homeController.importTexture();
												}
											});
											break;
										case IMPORT_BACKGROUND:
											Handler handler2 = new Handler(Looper.getMainLooper());
											handler2.post(new Runnable() {
												public void run() {
													homeController.importBackgroundImage();
												}
											});
											break;
									}
									importDestination = null;
								}

								getAdMobManager().interstitialDisplayPoint();
							}
						});


						// load home and trigger the new views
						if (homeFile != null) {
							if(homeFile instanceof File) {
								renovations3D.loadHome((File)homeFile, overrideName, isModifiedOverrideValue, loadedFromTemp);
								recordHomeStateInPrefs(overrideName == null ? "" : overrideName, isModifiedOverrideValue);
							} else if(homeFile instanceof InputStream) {
								renovations3D.loadHome((InputStream)homeFile, overrideName, isModifiedOverrideValue, loadedFromTemp);
								recordHomeStateInPrefs(overrideName == null ? "" : overrideName, isModifiedOverrideValue);
							}
						} else {
							renovations3D.newHome();
							recordHomeStateInPrefs("", false);
						}

						mRenovations3DPagerAdapter.notifyChangeInPosition(4);
						mRenovations3DPagerAdapter.notifyDataSetChanged();
					}
				});
			}
		};
		t2.start();

	}


	public BroadcastReceiver onCompleteHTTPIntent = new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent intent) {
			//don't call unregisterReceiver(this); in case multiple downloads are set up
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

			// This is one of our downloads.
			final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(id);
			Cursor cursor = downloadManager.query(query);

			if (cursor.moveToFirst()) {
				int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
				int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
				int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

				int status = cursor.getInt(statusIndex);

				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					String localUri = cursor.getString(localUriIndex);
					try {
						File file = new File(new URI(localUri).getPath());
						Renovations3DActivity.logFireBaseLevelUp("onCompleteHTTPIntent.OnReceive", file.getAbsolutePath());
						loadFile(file);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				} else if (status == DownloadManager.STATUS_FAILED) {
					int reason = cursor.getInt(reasonIndex);

					String message;
					switch (reason) {
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

					ToastCompat.makeText(Renovations3DActivity.this, "DownloadFailedWithErrorMessage: " + message, Toast.LENGTH_SHORT).show();
					Renovations3DActivity.logFireBaseLevelUp("onCompleteHTTPIntent.OnReceiveError", message);
				}
			}
		}
	};

	/**
	 * Either extracts and loads a file or simply extracts the file and returns the temp part
	 * @param inFile
	 * @return
	 */

	public void loadFile(final File inFile) {
		if (inFile.getName().toLowerCase().endsWith(".sh3d")) {
			loadHome(inFile);
		} else {
			// if no home loaded must load a new home and on complete import libraries, notice NOT a call to newHome()
			if (getHomeController() == null) {
				// must always recreate otherwise the pager hand out an IllegalStateException
				renovations3D = new Renovations3D(Renovations3DActivity.this);

				if (mRenovations3DPagerAdapter != null) {
					// discard the old view first
					mRenovations3DPagerAdapter.notifyChangeInPosition(4);
					mRenovations3DPagerAdapter.notifyDataSetChanged();
				}

				mRenovations3DPagerAdapter = new Renovations3DPagerAdapter(getSupportFragmentManager(), Renovations3DActivity.this);

				mRenovations3DPagerAdapter.setRenovations3D(renovations3D);

				// create new home and trigger the new views
				renovations3D.newHome();
				recordHomeStateInPrefs("", false);

				mRenovations3DPagerAdapter.notifyChangeInPosition(4);
				mRenovations3DPagerAdapter.notifyDataSetChanged();
			}


			if (getHomeController() != null) {
				// original or extracted
				File extractedFile = inFile;
				// try the zip wrapped option
				if (inFile.getName().toLowerCase().endsWith(".zip")) {
					int BUFFER = 1024;
					ZipInputStream zipIn = null;
					try {
						zipIn = new ZipInputStream(new FileInputStream(inFile));

						ZipEntry entry;
						while ((entry = zipIn.getNextEntry()) != null) {
							String entryName = entry.getName();

							if (entryName.toLowerCase().endsWith(".sh3f")
									|| entryName.toLowerCase().endsWith(".sh3t")
									|| entryName.toLowerCase().endsWith(".sh3l")) {
								//unzip to a cache file of entry name
								int count;
								byte data[] = new byte[1024];
								// Write the files to the disk
								extractedFile = new File(Renovations3DActivity.this.getCacheDir(), entryName);
								FileOutputStream fos = new FileOutputStream(extractedFile);
								BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
								while ((count = zipIn.read(data, 0, BUFFER)) != -1) {
									dest.write(data, 0, count);
								}
								dest.flush();
								dest.close();
								zipIn.closeEntry();


							}
						}
					} catch (IOException e) {
						//System.out.println(e);
						e.printStackTrace();
					} finally {
						if (zipIn != null) {
							try {
								zipIn.close();
							} catch (IOException e) {
								;
							}
						}
					}
				}

				// point at it and fall through
				File finalFile = extractedFile;

				if (finalFile.getName().toLowerCase().endsWith(".sh3f")
						|| finalFile.getName().toLowerCase().endsWith(".sh3t")
						|| finalFile.getName().toLowerCase().endsWith(".sh3l")) {
					//get off EDT so blocking questions can be asked
					Thread t = new Thread() {
						public void run() {
							// after optionally unzipping above try and load it
							if (finalFile.getName().toLowerCase().endsWith(".sh3f")) {
								getHomeController().importFurnitureLibrary(finalFile.getAbsolutePath());
							} else if (finalFile.getName().toLowerCase().endsWith(".sh3t")) {
								getHomeController().importTexturesLibrary(finalFile.getAbsolutePath());
							} else if (finalFile.getName().toLowerCase().endsWith(".sh3l")) {
								// like pref panel listen for the update and change language to the one loaded below
								getUserPreferences().addPropertyChangeListener(UserPreferences.Property.SUPPORTED_LANGUAGES,
										new PropertyChangeListener() {
											@Override
											public void propertyChange(PropertyChangeEvent ev) {
												//remove the listener
												((UserPreferences)ev.getSource()).removePropertyChangeListener(
														UserPreferences.Property.SUPPORTED_LANGUAGES, this);
												List<String> oldSupportedLanguages = Arrays.asList((String [])ev.getOldValue());
												String [] supportedLanguages = (String [])ev.getNewValue();
												// on a new thread otherwise stack overflow in FurnitureTable, weird
												runOnUiThread(new Runnable(){
													public void run() {
														// Select the first language added to supported languages
														for (final String language : supportedLanguages) {
															if (!oldSupportedLanguages.contains(language)) {
																getUserPreferences().setLanguage(language);
																break;
															}
														}
													}
												});

											}
										}
								);
								getHomeController().importLanguageLibrary(finalFile.getAbsolutePath());
							}
						}
					};
					t.start();
				} else {
					// do something else?
				}
			}
		}
	}





	private void loadUpContent() {


		//TODO: see eclipse Renovations3D.protected void start(String[] args) for exactly this setup, but better
		Intent intent = getIntent();
		if (intent != null) {
			String action = intent.getAction();

			if (action.compareTo(Intent.ACTION_VIEW) == 0) {
				String scheme = intent.getScheme();
				//ContentResolver resolver = getContentResolver();
				if(scheme != null) {
					if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0) {
						//Uri uri = intent.getData();
						//String name = getContentName(resolver, uri);
						//Log.v("tag", "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
						//ToastCompat.makeText(Renovations3DActivity.this, "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name, Toast.LENGTH_LONG).show();
						//InputStream input = resolver.openInputStream(uri);
						//TODO: clicking an http content fires off the http version and this one??
						//inputStreamToFile(input, Environment.DIRECTORY_DOWNLOADS + "/" + name);
						//loadHome(new File(Environment.DIRECTORY_DOWNLOADS + "/" + name));

						setIntent(null);
						return;
					} else if (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) {
						Uri uri = intent.getData();
						String name = uri.getLastPathSegment();

						Log.v("tag", "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
						ToastCompat.makeText(Renovations3DActivity.this, "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name, Toast.LENGTH_LONG).show();

						File inFile = new File(uri.getPath());
						loadFile(inFile);

						setIntent(null);
						Renovations3DActivity.logFireBaseLevelUp("ImportFromFile", intent.getDataString());
						return;
					} else if (scheme.compareTo("http") == 0 || scheme.compareTo("https") == 0) {
						ToastCompat.makeText(Renovations3DActivity.this, "http: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : ", Toast.LENGTH_LONG).show();

						Uri uri = intent.getData();
						final String fileName = uri.getLastPathSegment();

						Log.v("tag", "Http intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + fileName);
						ToastCompat.makeText(Renovations3DActivity.this, "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + fileName, Toast.LENGTH_LONG).show();
						DownloadManager.Request request = new DownloadManager.Request(uri);
						request.setDescription(fileName + "_download");
						request.setTitle(fileName);

						request.allowScanningByMediaScanner();
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

						try {
							// oddly see https://stackoverflow.com/questions/16749845/android-download-manager-setdestinationinexternalfilesdir
							//request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, fileName);
							request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

							registerReceiver(onCompleteHTTPIntent, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
							setIntent(null);

							// get download service and enqueue file
							DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
							manager.enqueue(request);

							ToastCompat.makeText(Renovations3DActivity.this, "Download started, please wait...", Toast.LENGTH_LONG).show();
							Renovations3DActivity.logFireBaseLevelUp("ImportFromHttp.enqueue", intent.getDataString());
						} catch (IllegalStateException e) {
							Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "IllegalStateException during http intent", intent.getDataString());
						}
						return;
					} else if (scheme.compareTo("ftp") == 0) {
						Renovations3DActivity.logFireBaseLevelUp("ImportFromFtp.enqueue", intent.getDataString());
						// TODO Import from FTP!
						ToastCompat.makeText(Renovations3DActivity.this, "Import from ftp not supported: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : ", Toast.LENGTH_LONG).show();
						setIntent(null);
						return;
					}
				}
			}
		}

		// figure out how many times we've been opened
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		//This is where we open the temp file that was last used, but wait for any writes to finish
		String tempWorkingFileRealName = settings.getString(STATE_TEMP_HOME_REAL_NAME, null);
		boolean isModifiedOverrideValue = settings.getBoolean(STATE_TEMP_HOME_REAL_MODIFIED, false);
		File outputDir = getCacheDir();
		File homeName = new File(outputDir, CURRENT_WORK_FILENAME);

		if (homeName.exists()) {
			Renovations3DActivity.logFireBaseContent("loadUpContentCurrentWork", "temp: " + homeName.getName() + " original: " + tempWorkingFileRealName
					+ " isModifiedOverrideValue: " + isModifiedOverrideValue);

			loadHome(homeName, tempWorkingFileRealName, isModifiedOverrideValue, true);
		} else {
			Renovations3DActivity.logFireBaseContent("loadUpContentCurrentWorkNoFile", "original: " + tempWorkingFileRealName);
			// or just fire up a lovely clear new home
			newHome();
		}
	}

	//TODO: the load has had this removed as it was only possible from onCreate so this is redundant
	//NOTE static as this is reused by the load to wait for auto save,
	// static will stay around as long as exiting activity is alive saving
	private static final Semaphore autoSaveSemaphore = new Semaphore(1, true);

	public void doAutoSave() {
		if (getHome() != null) {
			// get off the caller in case this is an onPause
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						// all synchro-meshed and sexy
						autoSaveSemaphore.acquire();
					} catch (InterruptedException e) {
					}
					Home home = getHome();
					//Notice isModified ignored, we always always save current work, and reload, we don't try to load the orginal file
					if (home != null) {
						try {
							// give it a default name if required
							if(home.getName() == null || home.getName().trim().length() == 0) {
								home.setName(SwingTools.getLocalizedLabelText(getUserPreferences(),
										com.eteks.sweethome3d.swing.HomePane.class, "NEW_HOME.Name"));
							}
							//clone so original modified flag is not changed
							Home autoSaveHome = home.clone();
							boolean isModifiedOverrideValue = home.isModified();
							String originalName = autoSaveHome.getName();
							File outputDir = getCacheDir();
							File homeName = new File(outputDir, CURRENT_WORK_FILENAME);
							// not using getHomeRecorder(HomeRecorder.Type.COMPRESSED) as it is 25 seconds for terrase
							// not compressed = 8 seconds
							System.out.println("doAutoSave start");
							renovations3D.getHomeRecorder().writeHome(autoSaveHome, homeName.getAbsolutePath());
							recordHomeStateInPrefs(originalName, isModifiedOverrideValue);

							Renovations3DActivity.logFireBaseContent("doAutoSave", "temp: " + homeName.getName() + " original: " + originalName
									+ " isModifiedOverrideValue: " + isModifiedOverrideValue);

						} catch (RecorderException e) {
							e.printStackTrace();
							Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "RecorderException during autosave", home.getName());
						}
					}
					autoSaveSemaphore.release();
				}
			});
			t.start();
		}
	}

	private UserPreferences userPreferences;
	/**
	 * PJ moved here from Renovations3D to make it a singleton again
	 * Returns user preferences stored in resources and local file system.
	 */

	public UserPreferences getUserPreferences() {
		// Initialize userPreferences lazily
		if (this.userPreferences == null) {
			// Retrieve preferences and application folders
			String preferencesFolderProperty = System.getProperty(Renovations3D.PREFERENCES_FOLDER, null);
			String applicationFoldersProperty = System.getProperty(Renovations3D.APPLICATION_FOLDERS, null);
			File preferencesFolder = preferencesFolderProperty != null
					? new File(preferencesFolderProperty)
					: null;
			File[] applicationFolders;
			if (applicationFoldersProperty != null) {
				String[] applicationFoldersProperties = applicationFoldersProperty.split(File.pathSeparator);
				applicationFolders = new File[applicationFoldersProperties.length];
				for (int i = 0; i < applicationFolders.length; i++) {
					applicationFolders[i] = new File(applicationFoldersProperties[i]);
				}
			} else {
				applicationFolders = null;
			}
			Executor eventQueueExecutor = new Executor() {
				@Override
				public void execute(Runnable command) {
					EventQueue.invokeLater(command);
				}
			};
			this.userPreferences = new FileUserPreferences(preferencesFolder, applicationFolders, eventQueueExecutor);
		}
		return this.userPreferences;
	}


	private ImageAcquireManager.Destination importDestination = null;

	public void showImportTextureWizard() {
		if (getHomeController() != null) {
			getHomeController().importTexture();
			importDestination = null;
		} else {
			importDestination = ImageAcquireManager.Destination.IMPORT_TEXTURE;
		}
	}

	public void showBackGroundImportWizard() {
		if (getHomeController() != null) {
			getHomeController().importBackgroundImage();
			importDestination = null;
		} else {
			importDestination = ImageAcquireManager.Destination.IMPORT_BACKGROUND;
		}
	}

	public static void setIconizedMenuTitle(MenuItem menuItem, String title, int iconId, Context context) {
		// NOTE use of setTitleCondensed as well as setTitle
		//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/aa60d8ac?duration=2592000000&appVersions=192					// is caused by this
		//http://stackoverflow.com/questions/7658725/android-java-lang-illegalargumentexception-invalid-payload-item-type

		if (menuItem != null) {
			SpannableStringBuilder builder = new SpannableStringBuilder("* " + title);
			builder.setSpan(new ImageSpan(context, iconId), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			menuItem.setTitle(builder);
			menuItem.setTitleCondensed(title);
			//menuItem.setIcon(iconId);// sub menus do show this, so don't do this for sub menus! otherwise there 2 icons
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		this.imageAcquireManager.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}


	public AdMobManager getAdMobManager() {
		return adMobManager;
	}

	public ImageAcquireManager getImageAcquireManager() {
		return imageAcquireManager;
	}

	public BillingManager getBillingManager() {
		return billingManager;
	}

	public ImportManager getImportManager() {
		return importManager;
	}

	final public int REQUEST_CODE_ASK_PERMISSION_STORAGE = 123;//just has to match from request to response below
	final public int REQUEST_CODE_ASK_PERMISSION_LOCATION = 124;

	// called by Compass
	public void getLocationPermission(LocationPermissionRequestor locationPermissionRequestor) {
		this.locationPermissionRequestor = locationPermissionRequestor;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSION_LOCATION);
			} else {
				locationPermission(true);
			}
		} else {
			locationPermission(true);
		}
	}

	private LocationPermissionRequestor locationPermissionRequestor = null;
	private void locationPermission(boolean granted) {
		this.adMobManager.locationPermission(granted);
		if (locationPermissionRequestor != null) {
			locationPermissionRequestor.permission(granted);
			locationPermissionRequestor = null;
		}
	}
	public void getDownloadsLocation(DownloadsLocationRequestor downloadsLocationRequestor) {
		this.downloadsLocationRequestor = downloadsLocationRequestor;
		//android 11, R, 30 onwards has a different storage model, it never use the downloads callback system
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
			throw new UnsupportedOperationException();
		} else {
			//below android 11
			if (Build.VERSION.SDK_INT >= M) {
				if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSION_STORAGE);
				} else {
					storagePermission(true);
				}
			} else {
				storagePermission(true);
			}
		}
	}
	private DownloadsLocationRequestor	downloadsLocationRequestor = null;
	private void storagePermission(boolean granted) {
		// set the no permission default
		File downloadsLocation = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

		if(granted) {
			downloadsLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}

		// give the jfilechooser a sanity checker to try to reduce trouble with bad file locations
		final File reviewFileLocation = downloadsLocation;
		JFileChooser.setFolderReviewer(new JFileChooser.FolderReviewer() {
			@Override
			public File reviewFolder(File folder) {
				if (folder == null) {
					folder = reviewFileLocation;
				} else if (folder.getAbsolutePath().contains(CURRENT_WORK_FILENAME)) {
					// extra care in case the caller is trying to use a app cache file location, which is invalid
					folder = reviewFileLocation;
				}
				return folder;
			}
		});

		//possibly the ext is not mounted
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "getExternalStorageState not MEDIA_MOUNTED", Environment.getExternalStorageState());
		}

		if (downloadsLocationRequestor != null) {
			downloadsLocationRequestor.location(downloadsLocation);
			downloadsLocationRequestor = null;
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_ASK_PERMISSION_STORAGE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission Granted
					Renovations3DActivity.logFireBaseContent("REQUEST_CODE_ASK_PERMISSIONS Granted");
					storagePermission(true);
				} else {
					// Permission Denied
					Renovations3DActivity.logFireBaseContent("REQUEST_CODE_ASK_PERMISSIONS Denied");

					// use that web site example dialogs
					ToastCompat.makeText(Renovations3DActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT).show();

					storagePermission(false);
				}
				break;
			case REQUEST_CODE_ASK_PERMISSION_LOCATION:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Renovations3DActivity.logFireBaseContent("location permission Granted");
					locationPermission(true);
				} else {
					Renovations3DActivity.logFireBaseContent("location permission Denied");
					locationPermission(false);
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}




	public interface LocationPermissionRequestor {
		void permission(boolean granted);
	}

	public interface DownloadsLocationRequestor {
		void location(File downloadLocation);
	}
}