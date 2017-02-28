package com.eteks.renovations3d;

import android.os.Bundle;
import android.widget.Toast;

import com.eteks.renovations3d.android.HomePane;
import com.eteks.sweethome3d.io.AutoRecoveryManager;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DamagedHomeRecorderException;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.renovations3d.android.AndroidViewFactory;
import com.eteks.renovations3d.android.FileContentManager;
import com.eteks.renovations3d.j3d.Component3DManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindblowing.renovations3d.BuildConfig;


import org.jogamp.java3d.JoglesPipeline;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javaawt.EventQueue;
import javaawt.VMEventQueue;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;


public class SweetHomeAVR extends HomeApplication
{
	private static final String PREFERENCES_FOLDER = "com.eteks.sweethome3d.preferencesFolder";
	private static final String APPLICATION_FOLDERS = "com.eteks.sweethome3d.applicationFolders";
	private static final String APPLICATION_PLUGINS_SUB_FOLDER = "plugins";

	private HomeRecorder homeRecorder;
	private HomeRecorder compressedHomeRecorder;
	private UserPreferences userPreferences;
	private ContentManager contentManager;
	private ViewFactory viewFactory;
	private PluginManager pluginManager;
	private boolean pluginManagerInitialized;
	private AutoRecoveryManager autoRecoveryManager;

	// replaced by singleton
	//private final Map<Home, HomeFrameController> homeFrameControllers;
	// new magic singleton,
	private HomeController homeController;
	private Home home;

	private SweetHomeAVRActivity parentActivity;

	/**
	 * Creates a home application instance. Recorders, user preferences, content
	 * manager, view factory and plug-in manager handled by this application are
	 * lazily instantiated to let subclasses override their creation.
	 */
	public SweetHomeAVR(SweetHomeAVRActivity parentActivity)
	{
		this.parentActivity = parentActivity;

		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");
		System.setProperty("j3d.usePbuffer", "true"); // some phones have no fbo under offscreen
		System.setProperty("j3d.noDestroyContext", "true");// don't clean up as the preserve/restore will handle it

		JoglesPipeline.LATE_RELEASE_CONTEXT = false;// so the world can clean up, otherwise the plan renderer holds onto it

		//PJPJPJ
		javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
		javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);
		javaawt.EventQueue.installEventQueueImpl(VMEventQueue.class);
		VMEventQueue.activity = parentActivity;
		SimpleShaderAppearance.setVersionES100();

		init();

		this.viewFactory = getViewFactory();
		this.contentManager = getContentManager();
		this.pluginManager = getPluginManager();

	}

	public void newHome()
	{
		if (!BuildConfig.DEBUG)
		{
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "newHome");
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "newHome");
			SweetHomeAVRActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
		}

		home = new Home();
		home.setName(null);// ensures save does a save as

		homeController = new HomeController(home, SweetHomeAVR.this, SweetHomeAVR.this.viewFactory, SweetHomeAVR.this.contentManager);

		parentActivity.setUpViews();
		parentActivity.invalidateOptionsMenu();
	}

	/**
	 * this is a butchery of HomeController.open(String)
	 */
	public void loadHome(final File homeFile, final boolean clearName)
	{
		if(!BuildConfig.DEBUG)
		{
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "loadHome");
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "loadHome");
			bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "loadHome " + homeFile);
			SweetHomeAVRActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
		}

		final String homeName = homeFile.getAbsolutePath();
		// this guy is stolen from the HomeController.open method which does fancy stuff
		// Read home in a threaded task
		Callable<Void> openTask = new Callable<Void>()
		{
			public Void call() throws RecorderException
			{
				// Read home with application recorder
				home = getHomeRecorder().readHome(homeName);
				home.setName(clearName ? null : homeName);// Notice this is used as the save name
				homeController = new HomeController(home, SweetHomeAVR.this, viewFactory, contentManager);
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						parentActivity.setUpViews();
						parentActivity.invalidateOptionsMenu();
					}
				});
				return null;
			}
		};
		ThreadedTaskController.ExceptionHandler exceptionHandler =
				new ThreadedTaskController.ExceptionHandler()
				{
					public void handleException(Exception ex)
					{
						if (!(ex instanceof InterruptedRecorderException))
						{
							//if (ex instanceof DamagedHomeRecorderException) {
							//	DamagedHomeRecorderException ex2 = (DamagedHomeRecorderException)ex;
							//	openDamagedHome(homeName, ex2.getDamagedHome(), ex2.getInvalidContent());
							//} else {
							ex.printStackTrace();
							if (ex instanceof RecorderException)
							{
								String message = userPreferences.getLocalizedString(HomeController.class, "openError", homeName);
								new HomePane(null, userPreferences, null, parentActivity).showError(message);
							}
							//}
						}
					}
				};
		new ThreadedTaskController(openTask,
				this.userPreferences.getLocalizedString(HomeController.class, "openMessage"), exceptionHandler,
				this.userPreferences, this.viewFactory).executeTask(new View()
		{
		});

	}

	//new singleton hand outerer
	public HomeController getHomeController()
	{
		return homeController;
	}

	//new singleton hand outerer
	public Home getHome()
	{
		return home;
	}

	/**
	 * Returns a recorder able to write and read homes in files.
	 */
	@Override
	public HomeRecorder getHomeRecorder()
	{
		// Initialize homeRecorder lazily
		if (this.homeRecorder == null)
		{
			this.homeRecorder = new HomeFileRecorder(0, false, getUserPreferences(), false, true);
		}
		return this.homeRecorder;
	}

	@Override
	public HomeRecorder getHomeRecorder(HomeRecorder.Type type)
	{
		if (type == HomeRecorder.Type.COMPRESSED)
		{
			// Initialize compressedHomeRecorder lazily
			if (this.compressedHomeRecorder == null)
			{
				this.compressedHomeRecorder = new HomeFileRecorder(9, false, getUserPreferences(), false, true);
			}
			return this.compressedHomeRecorder;
		}
		else
		{
			return super.getHomeRecorder(type);
		}
	}

	/**
	 * Returns user preferences stored in resources and local file system.
	 */
	@Override
	public UserPreferences getUserPreferences()
	{
		// Initialize userPreferences lazily
		if (this.userPreferences == null)
		{
			// Retrieve preferences and application folders
			String preferencesFolderProperty = System.getProperty(PREFERENCES_FOLDER, null);
			String applicationFoldersProperty = System.getProperty(APPLICATION_FOLDERS, null);
			File preferencesFolder = preferencesFolderProperty != null
					? new File(preferencesFolderProperty)
					: null;
			File[] applicationFolders;
			if (applicationFoldersProperty != null)
			{
				String[] applicationFoldersProperties = applicationFoldersProperty.split(File.pathSeparator);
				applicationFolders = new File[applicationFoldersProperties.length];
				for (int i = 0; i < applicationFolders.length; i++)
				{
					applicationFolders[i] = new File(applicationFoldersProperties[i]);
				}
			}
			else
			{
				applicationFolders = null;
			}
			Executor eventQueueExecutor = new Executor()
			{
				@Override
				public void execute(Runnable command)
				{
					EventQueue.invokeLater(command);
				}
			};
			this.userPreferences = new FileUserPreferences(preferencesFolder, applicationFolders, eventQueueExecutor)
			{
				@Override
				public List<Library> getLibraries()
				{
					if (userPreferences != null // Don't go further if preferences are not ready
							&& getPluginManager() != null)
					{
						List<Library> pluginLibraries = getPluginManager().getPluginLibraries();
						if (!pluginLibraries.isEmpty())
						{
							// Add plug-ins to the list returned by user preferences
							ArrayList<Library> libraries = new ArrayList<Library>(super.getLibraries());
							libraries.addAll(pluginLibraries);
							return Collections.unmodifiableList(libraries);
						}
					}
					return super.getLibraries();
				}

				@Override
				public void deleteLibraries(List<Library> libraries) throws RecorderException
				{
					if (userPreferences != null // Don't go further if preferences are not ready
							&& getPluginManager() != null)
					{
						super.deleteLibraries(libraries);
						List<Library> plugins = new ArrayList<Library>();
						for (Library library : libraries)
						{
							if (PluginManager.PLUGIN_LIBRARY_TYPE.equals(library.getType()))
							{
								plugins.add(library);
							}
						}
						getPluginManager().deletePlugins(plugins);
					}
				}
			};
		}
		return this.userPreferences;
	}

	/**
	 * Returns a content manager able to handle files.
	 */
	protected ContentManager getContentManager()
	{
		if (this.contentManager == null)
		{//PJ had to hand in the activity for dialog construction
			this.contentManager = new FileContentManagerWithRecordedLastDirectories(getUserPreferences(), getClass(), parentActivity);
		}
		return this.contentManager;
	}

	/**
	 * Returns a Swing view factory.
	 */
	protected ViewFactory getViewFactory()
	{
		if (this.viewFactory == null)
		{
			//PJ had to hand in the activity for dialog construction
			this.viewFactory = new AndroidViewFactory(parentActivity);
		}
		return this.viewFactory;
	}

	/**
	 * Returns the plugin manager of this application.
	 */
	protected PluginManager getPluginManager()
	{
		if (!this.pluginManagerInitialized)
		{
			try
			{
				UserPreferences userPreferences = getUserPreferences();
				if (userPreferences instanceof FileUserPreferences)
				{
					File[] applicationPluginsFolders = ((FileUserPreferences) userPreferences)
							.getApplicationSubfolders(APPLICATION_PLUGINS_SUB_FOLDER);
					// Create the plug-in manager that will search plug-in files in plugins folders
					this.pluginManager = new PluginManager(applicationPluginsFolders);
				}
			}
			catch (IOException ex)
			{
			}
			this.pluginManagerInitialized = true;
		}
		return this.pluginManager;
	}

	/**
	 * Returns Sweet Home 3D application read from resources.
	 */
	@Override
	public String getId()
	{
		String applicationId = System.getProperty("com.eteks.sweethome3d.applicationId");
		if (applicationId != null && applicationId.length() > 0)
		{
			return applicationId;
		}
		else
		{
			try
			{
				return getUserPreferences().getLocalizedString(com.eteks.sweethome3d.SweetHomeAVR.class, "applicationId");
			}
			catch (IllegalArgumentException ex)
			{
				return super.getId();
			}
		}
	}

	/**
	 * Returns the name of this application read from resources.
	 */
	@Override
	public String getName()
	{
		return getUserPreferences().getLocalizedString(com.eteks.sweethome3d.SweetHomeAVR.class, "applicationName");
	}

	/**
	 * Returns information about the version of this application.
	 */
	@Override
	public String getVersion()
	{
		String applicationVersion = System.getProperty("com.eteks.sweethome3d.applicationVersion");
		if (applicationVersion != null)
		{
			return applicationVersion;
		}
		else
		{
			// note use a class from the jar, not this class, but matching simple name (so the right property is pulled form teh resource once loaded
			return getUserPreferences().getLocalizedString(com.eteks.sweethome3d.SweetHomeAVR.class, "applicationVersion");
		}
	}


	/**
	 * Inits application instance.
	 */
	protected void init()
	{
		initSystemProperties();

		//SwingTools.showSplashScreenWindow(SweetHomeAVR.class.getResource("resources/splashScreen.jpg"));

		// Add a listener that opens a frame when a home is added to application
		addHomesListener(new CollectionListener<Home>()
		{
			private boolean firstApplicationHomeAdded;

			@Override
			public void collectionChanged(CollectionEvent<Home> ev)
			{
				//PJPJPJPJ collection no longer listened to, the loadHome calls controller.displayView(); directly!
				/*		switch (ev.getType())
						{
							 case ADD:
							Home home = ev.getItem();
							try {
							  HomeFrameController controller = createHomeFrameController(home);
							  controller.displayView();
							  if (!this.firstApplicationHomeAdded) {
								this.firstApplicationHomeAdded = true;
								addNewHomeCloseListener(home, controller.getHomeController());
							  }

							  homeFrameControllers.put(home, controller);
							} catch (IllegalStateException ex) {
							  // Check exception by class name to avoid a mandatory bind to Java 3D
							  if ("org.jogamp.java3d.IllegalRenderingStateException".equals(ex.getClass().getName())) {
								ex.printStackTrace();
								// In case of a problem in Java 3D, simply exit with a message.
								exitAfter3DError();
							  } else {
								throw ex;
							  }
							}
							break;
						  case DELETE:
							homeFrameControllers.remove(ev.getItem());

							// If application has no more home
							if (getHomes().isEmpty()
									) {
							   // Exit once current events are managed (under Mac OS X, exit is managed by MacOSXConfiguration)
							  EventQueue.invokeLater(new Runnable() {
								  @Override
								public void run() {
									System.exit(0);
								  }
								});
							}
							break;
						}*/
			}

		});

//		addComponent3DRenderingErrorObserver();

		getUserPreferences();
		try
		{
			// Set User Agent to follow statistics on used operating systems
//			System.setProperty("http.agent", getId() + "/" + getVersion()
//					+ " (" + System.getProperty("os.name") + " " + System.getProperty("os.version") + "; " + System.getProperty("os.arch") + "; " + Locale.getDefault() + ")");
		}
		catch (AccessControlException ex)
		{
			// Ignore User Agent change
		}
		// Init look and feel afterwards to ensure that Swing takes into account
		// default locale change
		//initLookAndFeel();


		//PJPJPJ the AutoRecoveryManager has a timer that holds a reference to this SweetHomeAVR once it's been released by everythign else on a change
		/*try
		{
			this.autoRecoveryManager = new AutoRecoveryManager(this);
		}
		catch (RecorderException ex)
		{
			// Too bad we can't retrieve homes to recover
			ex.printStackTrace();
		}*/


	}


	/**
	 * Sets various <code>System</code> properties.
	 */
	private static void initSystemProperties()
	{
		// Request to use system proxies to access to the Internet
/*		if (System.getProperty("java.net.useSystemProxies") == null)
		{
			System.setProperty("java.net.useSystemProxies", "true");
		}*/
	}


	/**
	 * Adds a listener to new home to close it if an other one is opened.
	 */
	private void addNewHomeCloseListener(final Home home, final HomeController controller)
	{
		if (home.getName() == null)
		{
			final CollectionListener<Home> newHomeListener = new CollectionListener<Home>()
			{
				@Override
				public void collectionChanged(CollectionEvent<Home> ev)
				{
					// Close new home for any named home added to application
					if (ev.getType() == CollectionEvent.Type.ADD)
					{
						if (ev.getItem().getName() != null
								&& home.getName() == null
								&& !home.isRecovered())
						{

							controller.close();

						}
						removeHomesListener(this);
					}
					else if (ev.getItem() == home && ev.getType() == CollectionEvent.Type.DELETE)
					{
						removeHomesListener(this);
					}
				}
			};
			addHomesListener(newHomeListener);
			// Disable this listener at first home change
			home.addPropertyChangeListener(Home.Property.MODIFIED, new PropertyChangeListener()
			{
				@Override
				public void propertyChange(PropertyChangeEvent ev)
				{
					removeHomesListener(newHomeListener);
					home.removePropertyChangeListener(Home.Property.MODIFIED, this);
				}
			});
		}
	}

	/**
	 * Sets the rendering error listener bound to Java 3D to avoid default System
	 * exit in case of error during 3D rendering.
	 */
	private void addComponent3DRenderingErrorObserver()
	{
		if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D"))
		{
			// Add a RenderingErrorObserver to Component3DManager, because offscreen
			// rendering needs to check rendering errors with its own RenderingErrorListener
			Component3DManager.getInstance().setRenderingErrorObserver(new Component3DManager.RenderingErrorObserver()
			{
				@Override
				public void errorOccured(int errorCode, String errorMessage)
				{
					System.err.print("Error in Java 3D : " + errorCode + " " + errorMessage);
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							exitAfter3DError();
						}
					});
				}
			});
		}
	}

	/**
	 * Displays a message to user about a 3D error, saves modified homes and
	 * forces exit.
	 */
	private void exitAfter3DError()
	{
		// Check if there are modified homes
		boolean modifiedHomes = false;
		for (Home home : getHomes())
		{
			if (home.isModified())
			{
				modifiedHomes = true;
				break;
			}
		}

		if (!modifiedHomes)
		{
			// Show 3D error message
			show3DError();
		}
		else if (confirmSaveAfter3DError())
		{
			// Delete all homes after saving modified ones
			for (Home home : getHomes())
			{
				if (home.isModified())
				{
					String homeName = home.getName();
					if (homeName == null)
					{
						//TODO: some sort of toast message here I wager
						//JFrame homeFrame = getHomeFrame(home);
						//homeFrame.toFront();
						//homeName = contentManager.showSaveDialog((View) homeFrame.getRootPane(), null,
						//		ContentManager.ContentType.SWEET_HOME_3D, null);
					}
					if (homeName != null)
					{
						try
						{
							// Write home with application recorder
							getHomeRecorder().writeHome(home, homeName);
						}
						catch (RecorderException ex)
						{
							// As this is an emergency exit, don't report error
							ex.printStackTrace();
						}
					}
					deleteHome(home);
				}
			}
		}
		// Close homes
		for (Home home : getHomes())
		{
			deleteHome(home);
		}
		// Force exit if program didn't exit by itself
		System.exit(0);
	}

	/**
	 * Displays in a 3D error message.
	 */
	private void show3DError()
	{
		UserPreferences userPreferences = getUserPreferences();
		String message = userPreferences.getLocalizedString(SweetHomeAVR.class, "3DError.message");
		String title = userPreferences.getLocalizedString(SweetHomeAVR.class, "3DError.title");

		//TODO: definitely a toast here too
		//JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), message,
		//		title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a dialog that let user choose whether he wants to save modified
	 * homes after an error in 3D rendering system.
	 *
	 * @return <code>true</code> if user confirmed to save.
	 */
	private boolean confirmSaveAfter3DError()
	{
		UserPreferences userPreferences = getUserPreferences();
		String message = userPreferences.getLocalizedString(SweetHomeAVR.class, "confirmSaveAfter3DError.message");
		String title = userPreferences.getLocalizedString(SweetHomeAVR.class, "confirmSaveAfter3DError.title");
		String save = userPreferences.getLocalizedString(SweetHomeAVR.class, "confirmSaveAfter3DError.save");
		String doNotSave = userPreferences.getLocalizedString(SweetHomeAVR.class, "confirmSaveAfter3DError.doNotSave");


		//TODO: More toast questions
		//return JOptionPane.showOptionDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(),
		//		message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{save, doNotSave},
		//		save) == JOptionPane.YES_OPTION;
		return false;
	}


/**
 * A file content manager that records the last directories for each content
 * in Java preferences.
 */
private static class FileContentManagerWithRecordedLastDirectories extends FileContentManager
{
	private static final String LAST_DIRECTORY = "lastDirectory#";
	private static final String LAST_DEFAULT_DIRECTORY = "lastDefaultDirectory";

	private final Class<? extends SweetHomeAVR> mainClass;

	public FileContentManagerWithRecordedLastDirectories(UserPreferences preferences,
														 Class<? extends SweetHomeAVR> mainClass,
														 SweetHomeAVRActivity activity)
	{
		super(preferences, activity);
		this.mainClass = mainClass;
	}

	@Override
	protected File getLastDirectory(ContentType contentType)
	{
		Preferences preferences = Preferences.userNodeForPackage(this.mainClass);
		String directoryPath = null;
		if (contentType != null)
		{
			directoryPath = preferences.get(LAST_DIRECTORY + contentType, null);
		}
		if (directoryPath == null)
		{
			directoryPath = preferences.get(LAST_DEFAULT_DIRECTORY, null);
		}
		if (directoryPath != null)
		{
			File directory = new File(directoryPath);
			if (directory.isDirectory())
			{
				return directory;
			}
		}
		return null;
	}

	@Override
	protected void setLastDirectory(ContentType contentType, File directory)
	{
		// Last directories are not recorded in user preferences since there's no need of portability
		// from a computer to an other
		Preferences preferences = Preferences.userNodeForPackage(this.mainClass);
		if (directory == null)
		{
			preferences.remove(LAST_DIRECTORY + contentType);
		}
		else
		{
			String directoryPath = directory.getAbsolutePath();
			if (contentType != null)
			{
				preferences.put(LAST_DIRECTORY + contentType, directoryPath);
			}
			if (directoryPath != null)
			{
				preferences.put(LAST_DEFAULT_DIRECTORY, directoryPath);
			}
		}
		try
		{
			preferences.flush();
		}
		catch (BackingStoreException ex)
		{
			// Ignore exception, Sweet Home 3D will work without recorded directories
		}
	}
}


}



