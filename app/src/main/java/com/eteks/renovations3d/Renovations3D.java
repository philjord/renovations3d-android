/*
 *
 * Renovations3D, Copyright (c) 2016 Philip Jordan <philjord@ihug.co.nz>
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.renovations3d;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.format.Formatter;
import android.widget.Toast;

import androidx.core.os.ConfigurationCompat;

import com.eteks.renovations3d.android.AndroidViewFactory;
import com.eteks.renovations3d.android.FileContentManager;
import com.eteks.renovations3d.android.FurnitureCatalogListPanel;
import com.eteks.renovations3d.android.HomePane;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.io.HomeStreamRecorder;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.mindblowing.swingish.JOptionPane;
import com.eteks.renovations3d.j3d.Component3DManager;
import com.eteks.sweethome3d.io.AutoRecoveryManager;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javaawt.EventQueue;
import me.drakeet.support.toast.ToastCompat;


public class Renovations3D extends HomeApplication {
	protected static final String PREFERENCES_FOLDER = "com.eteks.sweethome3d.preferencesFolder";
	protected static final String APPLICATION_FOLDERS = "com.eteks.sweethome3d.applicationFolders";
	private static final String APPLICATION_PLUGINS_SUB_FOLDER = "plugins";

	public static final float LARGE_HOME_MIN_BYTES_RATIO = 0.05f;//512 max = 25.5mb is large, for 256 max = 12.75mb is large

	private HomeRecorder homeRecorder;
	private HomeRecorder homeStreamRecorder;
	private HomeRecorder compressedHomeRecorder;
	private HomeRecorder compressedHomeStreamRecorder;
	//private UserPreferences userPreferences;	// moved to Renovations3DActivity to make a singleton
	private ContentManager contentManager;
	private ViewFactory viewFactory;
	private PluginManager pluginManager;
	private boolean pluginManagerInitialized;
	//private boolean                 checkUpdatesNeeded;//never needed
	private AutoRecoveryManager autoRecoveryManager;
	//private final Map<Home, HomeFrameController> homeFrameControllers;	// replaced by singleton

	// new singletons
	private HomeController homeController;
	private Home home;

	private Renovations3DActivity parentActivity;
	public boolean currentHomeReduceVisibleModels = false;

	//this is a new listener collection
	private LinkedHashSet<OnHomeLoadedListener> onHomeLoadedListeners = new LinkedHashSet<OnHomeLoadedListener>();
	public void addOnHomeLoadedListener(OnHomeLoadedListener onHomeLoadedListener) {
		onHomeLoadedListeners.add(onHomeLoadedListener);
	}
	public void removeOnHomeLoadedListener(OnHomeLoadedListener onHomeLoadedListener) {
		onHomeLoadedListeners.remove(onHomeLoadedListener);
	}
	public interface OnHomeLoadedListener {
		void onHomeLoaded(Home home, HomeController homeController);
	}

	/**
	 * Creates a home application instance. Recorders, user preferences, content
	 * manager, view factory and plug-in manager handled by this application are
	 * lazily instantiated to let subclasses override their creation.
	 */
	public Renovations3D(Renovations3DActivity parentActivity) {
		this.parentActivity = parentActivity;

		init();

		this.viewFactory = getViewFactory();
		this.contentManager = getContentManager();
		this.pluginManager = getPluginManager();

		languageSetOnFirstUse();
	}

	private void languageSetOnFirstUse() {
		SharedPreferences settings = parentActivity.getSharedPreferences(Renovations3DActivity.PREFS_NAME, 0);
		boolean languageSetOnFirstUse = settings.getBoolean(Renovations3DActivity.LANGUAGE_SET_ON_FIRST_USE, false);

		if (!languageSetOnFirstUse) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(Renovations3DActivity.LANGUAGE_SET_ON_FIRST_USE, true);
			editor.apply();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					//see https://stackoverflow.com/questions/4212320/get-the-current-language-in-device
					Locale defaultLocale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
					String language = defaultLocale.getLanguage();
					String country = defaultLocale.getCountry();
					String[] langNames = new String[]{language + "_" + country, language};

					for( String lang : langNames) {
						List<String> supportedLanguages = Arrays.asList(getUserPreferences().getSupportedLanguages());
						if (supportedLanguages.contains(lang)) {
							getUserPreferences().setLanguage(lang);
							ToastCompat.makeText(parentActivity, "Language set to " + lang, Toast.LENGTH_SHORT).show();
							return;
						}

						// let's see if there exists as a downloadable language file
						for (ImportManager.ImportInfo imp : ImportManager.importInfos) {
							if (imp.type == ImportManager.ImportType.LANGUAGE && imp.id.equals(lang)) {
								parentActivity.getImportManager().importLibrary(imp, null);
								ToastCompat.makeText(parentActivity, "Downloading language " + lang, Toast.LENGTH_SHORT).show();
								return;
							}
						}
					}


				}
			});
		}
	}


	public void newHome() {
		Renovations3DActivity.logFireBaseContent("newHome");

		// force dump old pref property change support
		this.getUserPreferences().clearPropertyChangeListeners();

		currentHomeReduceVisibleModels = false;
		home = new Home();
		home.setName(null);// ensures save does a save as

		homeController = new HomeController(home, Renovations3D.this, Renovations3D.this.viewFactory, Renovations3D.this.contentManager);
		homeController.getView();// this must be called in order to add the edit listeners so isModified is set correctly.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				parentActivity.setUpViews();
				parentActivity.invalidateOptionsMenu();
			}
		});

		for(OnHomeLoadedListener onHomeLoadedListener : onHomeLoadedListeners) {
			onHomeLoadedListener.onHomeLoaded(home, homeController);
		}
	}

	// this is a butchery of HomeController.newHomeFromExample() like the loadHome below
	public void loadHomeFromExample(String exampleName) {

		// force dump old pref property change support
		this.getUserPreferences().clearPropertyChangeListeners();

		final String homeName = exampleName;

		Callable<Void> openTask = new Callable<Void>() {
			public Void call() throws RecorderException {
				home = getHomeRecorder().readHome(homeName);
				home.setName((String) null);

				homeController = new HomeController(home, Renovations3D.this, viewFactory, contentManager);

				homeController.getView();// this must be called in order to add the edit listeners so isModified is set correctly.
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						parentActivity.setUpViews();
						parentActivity.invalidateOptionsMenu();
					}
				});

				Map<String, String> furnitureNames = homeController.getCatalogFurnitureNames(getUserPreferences().getFurnitureCatalog());
				String groupName = getUserPreferences().getLocalizedString(HomeController.class, "defaultGroupName", new Object[0]);
				Iterator furniture = home.getFurniture().iterator();

				while (furniture.hasNext()) {
					HomePieceOfFurniture piece = (HomePieceOfFurniture) furniture.next();
					homeController.renameToCatalogName(piece, furnitureNames, groupName);
				}

				home.setName((String) null);//belt and braces not sure if required
				for(OnHomeLoadedListener onHomeLoadedListener : onHomeLoadedListeners) {
					onHomeLoadedListener.onHomeLoaded(home, homeController);
				}
				return null;
			}
		};
		ThreadedTaskController.ExceptionHandler exceptionHandler = new ThreadedTaskController.ExceptionHandler() {
			public void handleException(Exception ex) {
				if (!(ex instanceof InterruptedRecorderException)) {
					ex.printStackTrace();
					if (ex instanceof RecorderException) {
						String message = getUserPreferences().getLocalizedString(HomeController.class, "openError", new Object[]{homeName});
						new HomePane(null, getUserPreferences(), null, parentActivity).showError(message);
					}
				}

			}
		};
		(new ThreadedTaskController(openTask,
						getUserPreferences().getLocalizedString(HomeController.class, "openMessage"), exceptionHandler,
						getUserPreferences(), this.viewFactory)).executeTask(new View(){});


	}


	// this is a butchery of HomeController.open(String)
	// homeFile must be either File or InputStream
	public void loadHome(final Object homeIn, final String overrideName, final boolean isModifiedOverrideValue, final boolean loadedFromTemp) {

		if( homeIn instanceof File) {
			File homeFile = (File)homeIn;
			Renovations3DActivity.logFireBaseContent("loadHomeFile", homeFile.getName());

			// force dump old pref property change support
			this.getUserPreferences().clearPropertyChangeListeners();

			final String homeName = homeFile.getAbsolutePath();
			// this guy is stolen from the HomeController.open method which does fancy stuff
			// Read home in a threaded task
			Callable<Void> openTask = new Callable<Void>() {
				public Void call() throws RecorderException {

					// Read home with application recorder
					home = getHomeRecorder().readHome(homeName);
					if (overrideName != null) {
						home.setName(overrideName.length() == 0 ? null : overrideName);// Notice this is used as the save name
						home.setModified(isModifiedOverrideValue);
					} else {
						home.setName(homeName);
					}
					homeController = new HomeController(home, Renovations3D.this, viewFactory, contentManager);

					currentHomeReduceVisibleModels = false;
					//TODO: More correctly I want to know the zipped size, but home getHomeRecorder().readHome(homeName); above works out zip-ness but does not record that fact
					// So instead I'm just working with modelsizes which is all I can play with anyway
					long maxMem = Runtime.getRuntime().maxMemory();
					long largeModelsLimitSize = (long) (LARGE_HOME_MIN_BYTES_RATIO * maxMem);
					//512 max meme * 0.05 = 25.5Mb is large home, large model = 25.5Mb / 10 = 2.5Mb
					long largeModelSize = (long) (largeModelsLimitSize * 0.1f);

					//temp saves already have the reduce visibility choices in them inherently
					if (!loadedFromTemp) {
						boolean hasModelToMakeInvisible = false;
						long totalModelSize = 0;
						for (HomePieceOfFurniture f : home.getFurniture()) {
							long modelSize = (f.getModelSize() == null ? 0 : f.getModelSize());// can be a null Long oddly
							totalModelSize += modelSize;
							if (modelSize > largeModelSize) {
								hasModelToMakeInvisible = true;
							}
						}
						if (totalModelSize > largeModelsLimitSize) {
							if (hasModelToMakeInvisible) {
								String warningMessageHtml = parentActivity.getString(R.string.large_home_question);
								String size = Formatter.formatShortFileSize(parentActivity, homeFile.length());
								String messageHtml = warningMessageHtml.replace("%1", size);

								int result = JOptionPane.showConfirmDialog(parentActivity, messageHtml, parentActivity.getString(R.string.large_home_question_title),
										JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

								currentHomeReduceVisibleModels = result == JOptionPane.OK_OPTION;
							}
						}
					}

					if (currentHomeReduceVisibleModels == true) {
						for (HomePieceOfFurniture f : home.getFurniture()) {
							long modelSize = (f.getModelSize() == null ? 0 : f.getModelSize());// can be a null Long oddly
							if (modelSize > largeModelSize) {
								f.setVisible(false);
							}
						}
					}

					homeController.getView();// this must be called in order to add the edit listeners so isModified is set correctly.
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							parentActivity.setUpViews();
							parentActivity.invalidateOptionsMenu();
						}
					});

					for (OnHomeLoadedListener onHomeLoadedListener : onHomeLoadedListeners) {
						onHomeLoadedListener.onHomeLoaded(home, homeController);
					}
					return null;
				}
			};
			ThreadedTaskController.ExceptionHandler exceptionHandler =
					new ThreadedTaskController.ExceptionHandler() {
						public void handleException(Exception ex) {
							if (!(ex instanceof InterruptedRecorderException)) {
								//if (ex instanceof DamagedHomeRecorderException) {
								//	DamagedHomeRecorderException ex2 = (DamagedHomeRecorderException)ex;
								//	openDamagedHome(homeName, ex2.getDamagedHome(), ex2.getInvalidContent());
								//} else {
								ex.printStackTrace();
								if (ex instanceof RecorderException) {
									String message = getUserPreferences().getLocalizedString(HomeController.class, "openError", homeName);
									new HomePane(null, getUserPreferences(), null, parentActivity).showError(message);
								}
								//}
							}
						}
					};
			(new ThreadedTaskController(openTask,
					getUserPreferences().getLocalizedString(HomeController.class, "openMessage"), exceptionHandler,
					getUserPreferences(), this.viewFactory)).executeTask(new View() {
			});
		} else if (homeIn instanceof InputStream) {
			InputStream homeInputStream = (InputStream)homeIn;

			// force dump old pref property change support
			this.getUserPreferences().clearPropertyChangeListeners();


			// this guy is stolen from the HomeController.open method which does fancy stuff
			// Read home in a threaded task
			Callable<Void> openTask = new Callable<Void>() {
				public Void call() throws RecorderException {

					// Read home with application recorder
					home = ((HomeStreamRecorder)getHomeStreamRecorder()).readHome(homeInputStream);
					if (overrideName != null) {
						home.setName(overrideName.length() == 0 ? null : overrideName);// Notice this is used as the save name
						home.setModified(isModifiedOverrideValue);
					} else {
						home.setName("");
					}
					homeController = new HomeController(home, Renovations3D.this, viewFactory, contentManager);

					homeController.getView();// this must be called in order to add the edit listeners so isModified is set correctly.
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							parentActivity.setUpViews();
							parentActivity.invalidateOptionsMenu();
						}
					});

					for (OnHomeLoadedListener onHomeLoadedListener : onHomeLoadedListeners) {
						onHomeLoadedListener.onHomeLoaded(home, homeController);
					}
					return null;
				}
			};
			ThreadedTaskController.ExceptionHandler exceptionHandler =
					new ThreadedTaskController.ExceptionHandler() {
						public void handleException(Exception ex) {
							if (!(ex instanceof InterruptedRecorderException)) {
								//if (ex instanceof DamagedHomeRecorderException) {
								//	DamagedHomeRecorderException ex2 = (DamagedHomeRecorderException)ex;
								//	openDamagedHome(homeName, ex2.getDamagedHome(), ex2.getInvalidContent());
								//} else {
								ex.printStackTrace();
								if (ex instanceof RecorderException) {
									String message = getUserPreferences().getLocalizedString(HomeController.class, "openError", "");
									new HomePane(null, getUserPreferences(), null, parentActivity).showError(message);
								}
								//}
							}
						}
					};
			(new ThreadedTaskController(openTask,
					getUserPreferences().getLocalizedString(HomeController.class, "openMessage"), exceptionHandler,
					getUserPreferences(), this.viewFactory)).executeTask(new View() {
			});
		}
	}


	//new singleton hand outerer
	public HomeController getHomeController() {
		return homeController;
	}

	//new singleton hand outerer
	public Home getHome() {
		return home;
	}

	/**
	 * Returns a recorder able to write and read homes in files.
	 */
	@Override
	public HomeRecorder getHomeRecorder() {
		// Initialize homeRecorder lazily
		if (this.homeRecorder == null) {
			this.homeRecorder = new HomeFileRecorder(0, false, getUserPreferences(), false, true, true);
		}
		return this.homeRecorder;
	}
	public HomeRecorder getHomeStreamRecorder() {
		// Initialize homeRecorder lazily
		if (this.homeStreamRecorder == null) {
			this.homeStreamRecorder = new HomeStreamRecorder(0, false, getUserPreferences(), false, true, true);
		}
		return this.homeStreamRecorder;
	}

	@Override
	public HomeRecorder getHomeRecorder(HomeRecorder.Type type) {
		if (type == HomeRecorder.Type.COMPRESSED) {
			// Initialize compressedHomeRecorder lazily
			if (this.compressedHomeRecorder == null) {
				this.compressedHomeRecorder = new HomeFileRecorder(9, false, getUserPreferences(), false, true, true);
			}
			return this.compressedHomeRecorder;
		} else {
			return super.getHomeRecorder(type);
		}
	}
	public HomeRecorder getHomeStreamRecorder(HomeRecorder.Type type) {
		if (type == HomeRecorder.Type.COMPRESSED) {
			// Initialize compressedHomeRecorder lazily
			if (this.compressedHomeStreamRecorder == null) {
				this.compressedHomeStreamRecorder = new HomeStreamRecorder(9, false, getUserPreferences(), false, true, true);
			}
			return this.compressedHomeStreamRecorder;
		} else {
			return super.getHomeRecorder(type);
		}
	}

	/**
	 * Returns user preferences stored in resources and local file system.
	 */
	@Override
	public UserPreferences getUserPreferences() {
		//Renovations3DActivity intializes this just once, to avoid heavy file loading work repeats
		return parentActivity.getUserPreferences();
	}

	/**
	 * Returns a content manager able to handle files.
	 */
	protected ContentManager getContentManager() {
		if (this.contentManager == null) {
			this.contentManager = new FileContentManagerWithRecordedLastDirectories(getUserPreferences(), getClass(), parentActivity);
		}
		return this.contentManager;
	}

	/**
	 * Returns a Swing view factory.
	 */
	protected ViewFactory getViewFactory() {
		if (this.viewFactory == null) {
			this.viewFactory = new AndroidViewFactory(parentActivity);
		}
		return this.viewFactory;
	}

	/**
	 * Returns the plugin manager of this application.
	 */
	protected PluginManager getPluginManager() {
		if (!this.pluginManagerInitialized) {
			try {
				UserPreferences userPreferences = getUserPreferences();
				if (userPreferences instanceof FileUserPreferences) {
					File[] applicationPluginsFolders = ((FileUserPreferences) userPreferences)
							.getApplicationSubfolders(APPLICATION_PLUGINS_SUB_FOLDER);
					// Create the plug-in manager that will search plug-in files in plugins folders
					this.pluginManager = new PluginManager(applicationPluginsFolders);
				}
			} catch (IOException ex) {
			}
			this.pluginManagerInitialized = true;
		}
		return this.pluginManager;
	}

	/**
	 * Returns Sweet Home 3D application read from resources.
	 */
	@Override
	public String getId() {
		String applicationId = System.getProperty("com.eteks.sweethome3d.applicationId");
		if (applicationId != null && applicationId.length() > 0) {
			return applicationId;
		} else {
			try {
				return getUserPreferences().getLocalizedString(com.eteks.sweethome3d.Renovations3D_props.class, "applicationId");
			} catch (IllegalArgumentException ex) {
				return super.getId();
			}
		}
	}

	/**
	 * Returns the name of this application read from resources.
	 */
	@Override
	public String getName() {
		return getUserPreferences().getLocalizedString(com.eteks.sweethome3d.Renovations3D_props.class, "applicationName");
	}

	/**
	 * Returns information about the version of this application.
	 */
	@Override
	public String getVersion() {
		String applicationVersion = System.getProperty("com.eteks.sweethome3d.applicationVersion");
		if (applicationVersion != null) {
			return applicationVersion;
		} else {
			return getUserPreferences().getLocalizedString(com.eteks.sweethome3d.Renovations3D_props.class, "applicationVersion");
		}
	}

	/**
	 * Returns the frame that displays the given <code>home</code>.
	 */

	/**
	 * Returns the controller of the given <code>home</code>.
	 */

	/**
	 * Shows and brings to front <code>home</code> frame.
	 */

	/**
	 * Inits application instance.
	 */
	protected void init() {
		initSystemProperties();

		//SwingTools.showSplashScreenWindow(Renovations3D.class.getResource("resources/splashScreen.jpg"));

		// Add a listener that opens a frame when a home is added to application
		addHomesListener(new CollectionListener<Home>() {
			private boolean firstApplicationHomeAdded;

			@Override
			public void collectionChanged(CollectionEvent<Home> ev) {
				// collection no longer listened to, the loadHome calls controller.displayView(); directly!
			}
		});

//		addComponent3DRenderingErrorObserver();

		getUserPreferences();
	}


	/**
	 * Sets various <code>System</code> properties.
	 */
	private static void initSystemProperties() {
		// Request to use system proxies to access to the Internet
/*		if (System.getProperty("java.net.useSystemProxies") == null) {
			System.setProperty("java.net.useSystemProxies", "true");
		}*/
	}


	/**
	 * Adds a listener to new home to close it if an other one is opened.
	 */
	private void addNewHomeCloseListener(final Home home, final HomeController controller) {
		if (home.getName() == null) {
			final CollectionListener<Home> newHomeListener = new CollectionListener<Home>() {
				public void collectionChanged(CollectionEvent<Home> ev) {
					// Close new home for any named home added to application
					if (ev.getType() == CollectionEvent.Type.ADD) {
						if (ev.getItem().getName() != null
								&& home.getName() == null
								&& !home.isRecovered()) {
							controller.close();
						}
						removeHomesListener(this);
					} else if (ev.getItem() == home && ev.getType() == CollectionEvent.Type.DELETE) {
						removeHomesListener(this);
					}
				}
			};
			addHomesListener(newHomeListener);
			// Disable this listener at first home change
			home.addPropertyChangeListener(Home.Property.MODIFIED, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent ev) {
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
	private void addComponent3DRenderingErrorObserver() {
		if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
			// Add a RenderingErrorObserver to Component3DManager, because offscreen
			// rendering needs to check rendering errors with its own RenderingErrorListener
			Component3DManager.getInstance().setRenderingErrorObserver(new Component3DManager.RenderingErrorObserver() {
				public void errorOccured(int errorCode, String errorMessage) {
					System.err.print("Error in Java 3D : " + errorCode + " " + errorMessage);
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "Error in Java 3D", "" + errorCode + " " + errorMessage);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
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
	private void exitAfter3DError() {
		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT , "exitAfter3DError", null);
		// Check if there are modified homes
		boolean modifiedHomes = false;
		for (Home home : getHomes()) {
			if (home.isModified()) {
				modifiedHomes = true;
				break;
			}
		}

		if (!modifiedHomes) {
			// Show 3D error message
			show3DError();
		} else if (confirmSaveAfter3DError()) {
			// Delete all homes after saving modified ones
			for (Home home : getHomes()) {
				if (home.isModified()) {
					String homeName = home.getName();
					if (homeName == null) {
						//nothing can be done this is an exit path I assume never occurs
						//JFrame homeFrame = getHomeFrame(home);
						//homeFrame.toFront();
						//homeName = contentManager.showSaveDialog((View) homeFrame.getRootPane(), null,
						//		ContentManager.ContentType.SWEET_HOME_3D, null);
					}
					if (homeName != null) {
						try {
							// Write home with application recorder
							getHomeRecorder().writeHome(home, homeName);
						} catch (RecorderException ex) {
							// As this is an emergency exit, don't report error
							ex.printStackTrace();
						}
					}
					deleteHome(home);
				}
			}
		}
		// Close homes
		for (Home home : getHomes()) {
			deleteHome(home);
		}
		// Force exit if program didn't exit by itself
		System.exit(0);
	}

	/**
	 * Displays in a 3D error message.
	 */
	private void show3DError() {
		UserPreferences userPreferences = getUserPreferences();
		String message = userPreferences.getLocalizedString(Renovations3D.class, "3DError.message");
		String title = userPreferences.getLocalizedString(Renovations3D.class, "3DError.title");
		ToastCompat.makeText(this.parentActivity, message, Toast.LENGTH_LONG).show();

		//JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), message,
		//		title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a dialog that let user choose whether he wants to save modified
	 * homes after an error in 3D rendering system.
	 *
	 * @return <code>true</code> if user confirmed to save.
	 */
	private boolean confirmSaveAfter3DError() {
		UserPreferences userPreferences = getUserPreferences();
		String message = userPreferences.getLocalizedString(Renovations3D.class, "confirmSaveAfter3DError.message");
		String title = userPreferences.getLocalizedString(Renovations3D.class, "confirmSaveAfter3DError.title");
		String save = userPreferences.getLocalizedString(Renovations3D.class, "confirmSaveAfter3DError.save");
		String doNotSave = userPreferences.getLocalizedString(Renovations3D.class, "confirmSaveAfter3DError.doNotSave");

		ToastCompat.makeText(this.parentActivity, message, Toast.LENGTH_LONG).show();
		//return JOptionPane.showOptionDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(),
		//		message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{save, doNotSave},
		//		save) == JOptionPane.YES_OPTION;
		return false;
	}

	/**
	 * Starts application once initialized and opens home passed in arguments.
	 * This method is executed from Event Dispatch Thread.
	 */

	/**
	 * Shows a home frame, either a new one when no home is opened, or the last created home frame.
	 */

	/**
	 * Check updates if needed.
	 */

/**
 * A file content manager that records the last directories for each content
 * in Java preferences.
 */
private static class FileContentManagerWithRecordedLastDirectories extends FileContentManager {
	private static final String LAST_DIRECTORY = "lastDirectory#";
	private static final String LAST_DEFAULT_DIRECTORY = "lastDefaultDirectory";

	private final Class<? extends Renovations3D> mainClass;

	public FileContentManagerWithRecordedLastDirectories(UserPreferences preferences,
																											 Class<? extends Renovations3D> mainClass,
																											 Renovations3DActivity activity) {
		super(preferences, activity);
		this.mainClass = mainClass;
	}

	@Override
	protected File getLastDirectory(ContentType contentType) {
		Preferences preferences = Preferences.userNodeForPackage(this.mainClass);
		String directoryPath = null;
		if (contentType != null) {
			directoryPath = preferences.get(LAST_DIRECTORY + contentType, null);
		}
		if (directoryPath == null) {
			directoryPath = preferences.get(LAST_DEFAULT_DIRECTORY, null);
		}
		if (directoryPath != null) {
			File directory = new File(directoryPath);
			if (directory.isDirectory()) {
				return directory;
			}
		}
		return null;
	}

	@Override
	protected void setLastDirectory(ContentType contentType, File directory) {
		// Last directories are not recorded in user preferences since there's no need of portability
		// from a computer to an other
		Preferences preferences = Preferences.userNodeForPackage(this.mainClass);
		if (directory == null) {
			preferences.remove(LAST_DIRECTORY + contentType);
		} else {
			String directoryPath = directory.getAbsolutePath();
			if (contentType != null) {
				preferences.put(LAST_DIRECTORY + contentType, directoryPath);
			}
			if (directoryPath != null) {
				preferences.put(LAST_DEFAULT_DIRECTORY, directoryPath);
			}
		}
		try {
			preferences.flush();
		} catch (BackingStoreException ex) {
			// Ignore exception, Sweet Home 3D will work without recorded directories
		}
	}
}

}
