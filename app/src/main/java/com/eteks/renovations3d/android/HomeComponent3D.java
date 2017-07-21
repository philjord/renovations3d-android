package com.eteks.renovations3d.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.swingish.JOptionPane;
import com.eteks.renovations3d.utils.InfoText3D;
import com.eteks.renovations3d.utils.JoglStatusActivity;
import com.eteks.sweethome3d.j3d.Ground3D;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.Object3DBranch;
import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.j3d.Wall3D;
import com.eteks.renovations3d.j3d.mouseover.HomeComponent3DMouseHandler;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;
import com.eteks.renovations3d.j3d.Component3DManager;
import com.eteks.renovations3d.utils.AndyFPSCounter;
import com.eteks.renovations3d.utils.Canvas3D2D;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.NativeWindow;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.FPSCounter;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingBox;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.JoglesPipeline;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransformInterpolator;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.View;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.Viewer;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Vector3f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javaawt.Color;
import javaawt.EventQueue;
import javaawt.GraphicsConfiguration;
import javaawt.geom.Area;
import javaawt.geom.GeneralPath;
import javaawt.geom.PathIterator;
import javaawt.image.BufferedImage;
import jogamp.newt.WindowImpl;
import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.WindowDriver;

import static com.eteks.renovations3d.Renovations3DActivity.PREFS_NAME;
import static com.eteks.renovations3d.android.swingish.JOptionPane.possiblyShowWelcomeScreen;



/**
 * Created by phil on 11/22/2016.
 */

public class HomeComponent3D extends NewtBaseFragment implements com.eteks.sweethome3d.viewcontroller.View
{

	private static final String RUN_UPDATES = "RUN_UPDATES";
	private boolean fullRoomUpdateRequired = false;
	private boolean fullWallUpdateRequired = false;

	public static boolean ENABLE_HUD = true;
	public static final String WELCOME_SCREEN_UNWANTED = "COMPONENT_3D_WELCOME_SCREEN_UNWANTED";

	private static final String DEOPTOMIZE = "DEOPTOMIZE";

	// if we are not initialized then ignore onCreateViews
	private boolean initialized = false;

	private AndyFPSCounter fpsCounter;
	private InfoText3D onscreenInfo;
	private int fingerCount = 0;
	private boolean dragging = false;
	private String extraInfo ="";

	private HomeComponent3DMouseHandler homeComponent3DMouseHandler;
	private ScaleGestureDetector mScaleDetector;
	private LongHoldHandler longHoldHandler;

	private Menu mOptionsMenu;

	private GLCapabilities caps;
	private GLWindow gl_window;



	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
		boolean deoptomize = settings.getBoolean(DEOPTOMIZE, false);
		setDeoptomize(deoptomize);

		caps = new GLCapabilities(null);

		caps.setDoubleBuffered(true);
		caps.setDepthBits(16);
		caps.setStencilBits(8);
		caps.setHardwareAccelerated(true);
		caps.setBackgroundOpaque(true);

		//TODO: see if this works
		//caps.setBackgroundOpaque(false);
		//caps.setSampleBuffers(true);//death! no touch! //TODO: this works in morrowind now?
		//caps.setNumSamples(2);

		gl_window = GLWindow.create(caps);

		// in case of a create or runtime exception set up a listener to nicely report it to the user
		final Window delegateWindow = gl_window.getDelegatedWindow();
		if(delegateWindow instanceof WindowDriver) {
			WindowDriver wd = (WindowDriver)delegateWindow;
			wd.setNativeWindowExceptionListener( new WindowImpl.NativeWindowExceptionListener()
			{
				public boolean handleException(NativeWindowException nwp)
				{
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NativeWindowException", null );
					String message = getActivity().getString(R.string.insufficient3dResourcesMessage);
					String title = getActivity().getString(R.string.insufficient3dResourcesTitle);
					JOptionPane.showMessageDialog(getActivity(), message, title, JOptionPane.ERROR_MESSAGE);



				/* I'd like to do something like this, but obviously as it is it's a recursive mess
					try
					{
						Thread.sleep(2000);

						//try again manually
						for(int i=0; i<newtWindows.size(); i++)
						{
							final Window win = newtWindows.get(i);
							win.setVisible(true);
						}
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}*/

					//hopefully not throwing the exception will allow the user to save work
					return true;
				}

				/**
				 * Currently called when the WindowImpl has an exception during event processing
				 * So far only failed to lock messages are dealt with here
				 * @param e
				 * @return
				 */
				@Override
				public boolean handleRuntimeException(RuntimeException e)
				{
					// just ignore the failed lock and hope it is acquired during later processing
					if(e.getMessage().contains("Waited 5000ms"))
						return true;

					return false;
				}
			});
		}


		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "GLWindow.create(caps)", null);
		// equal to addAncestor listeners but that's too late by far
		gl_window.addGLEventListener(glWindowInitListener);

	}

	GLEventListener glWindowInitListener = new GLEventListener()
	{
		@Override
		public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable)
		{
		}

		@Override
		public void reshape(final GLAutoDrawable drawable, final int x, final int y,
		final int w, final int h)
		{
		}

		@Override
		public void display(final GLAutoDrawable drawable)
		{
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start display", null );

			//odd createComponent3D calls addMouseListener which attaches this listener to the view
			// that view is being destroyed and I'm getting back here so I have to re-add the mouse listener now
			// so instead I just always add the mouse listener here now

			getView().setOnTouchListener(new TouchyListener());


			if (canvas3D2D != null)
			{
				// must call this as onPause has called removeNotify
				try
				{
					canvas3D2D.addNotify();
					//wait for onscreen hint
					if (!HomeComponent3D.this.getUserVisibleHint())
					{
						canvas3D2D.stopRenderer();
					}
					else
					{
						canvas3D2D.startRenderer();
					}
				}
				catch(NullPointerException e)
				{
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "1canvas3D2D.addNotify() null 0", null );
					if(gl_window != null)
					{
						Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "1canvas3D2D.addNotify() null 1", "ChosenGLCapabilities(): " + gl_window.getChosenGLCapabilities() );
					}

					// let's see if other failures happen or is this just a race condition
					//throw e;
				}
			}
			else
			{
				// taken from ancestor listener originally so get back onto EDT thread
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
					// Create component 3D only once the graphics configuration of its parent is known
					if (canvas3D2D == null)
					{
						createComponent3D(null, preferences, controller);

						try
						{
							// called here not in createComponent, just for life cycle clarity
							canvas3D2D.addNotify();
						}
						catch(NullPointerException e)
						{
							Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "2canvas3D2D.addNotify() null 0", null);
							if(gl_window != null)
							{
								Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "2canvas3D2D.addNotify() null 1", "ChosenGLCapabilities(): " + gl_window.getChosenGLCapabilities() );
							}

							// let's see if other failures happen or is this just a race condition
							//throw e;
						}

						//wait for onscreen hint as this component is create whilst off screen
						if (!HomeComponent3D.this.getUserVisibleHint())
							canvas3D2D.stopRenderer();
					}


					if (onscreenUniverse == null)
					{
						onscreenUniverse = createUniverse(displayShadowOnFloor, true, false);

						onscreenUniverse.getViewer().getView().addCanvas3D(canvas3D2D);
						if(BuildConfig.DEBUG && ENABLE_HUD)
						{
							fpsCounter = new AndyFPSCounter();
							onscreenUniverse.addBranchGraph(fpsCounter.getBehaviorBranchGroup());
							fpsCounter.addToCanvas(canvas3D2D);
							onscreenInfo = new InfoText3D()
							{
								@Override
								protected String getText()
								{
									return "F: " + fingerCount + " D: " + dragging + " " + extraInfo;
								}
							};
							onscreenUniverse.addBranchGraph(onscreenInfo.getBehaviorBranchGroup());
							onscreenInfo.addToCanvas(canvas3D2D);
						}

						// I have no idea how 	getActivity() can return null here, possibly we are being destroyed right now
						// but I think it happens after the canvas.addnotify failure above anyway
						if(getActivity() != null)
						{
							// mouse interaction with picking
							homeComponent3DMouseHandler = new HomeComponent3DMouseHandler(home, preferences, controller, (Renovations3DActivity) getActivity());
							homeComponent3DMouseHandler.setConfig(canvas3D2D, onscreenUniverse.getLocale());
						}
					}
					}
				});
			}
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end display", null );
		}

		@Override
		public void dispose(final GLAutoDrawable drawable)
		{
			if(canvas3D2D != null)
			{
				canvas3D2D.stopRenderer();
				canvas3D2D.removeNotify();
			}

			PlanComponent.PieceOfFurnitureModelIcon.pauseOffScreenRendering();
			if(onscreenUniverse != null)
			{
				onscreenUniverse.cleanup();
				onscreenUniverse = null;
			}
			PlanComponent.PieceOfFurnitureModelIcon.unpauseOffScreenRendering();

			// taken from ancestor listener originally so get back onto EDT thread
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					if (onscreenUniverse != null)
					{
						removeHomeListeners();
					}
				}
			});
		}

	};


	@Override
	public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(initialized)
		{
			this.setHasOptionsMenu(true);
		}
		android.view.View rootView = getContentView(this.getWindow(), gl_window);
		return rootView;
	}
	public void onStart()
	{
		super.onStart();
		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onStart", null );
	}
	public void onResume()
	{
		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onResume", null );
		super.onResume();

		// ok at this point either
		// A/ we've just started up onStart was called and now onResume
		// B/ we've onPaused (possible onStop too) and GLStateKeeper saved state
		// 		now we've (possible onStart then) onResume and GLStateKeeper is working in the background on restoring state
		// 		a display is going to come through soon to make things show after state is happy
		// in both cases display callback is gonna get called, he needs to addNotify in all cases
		if(canvas3D2D != null)
		{
			//PJ I add this entire conditional to try to restart after a stop it appears ok, but be suspicious of it
			if(!canvas3D2D.getGLWindow().isNativeValid())
			{
				gl_window = GLWindow.create(caps);
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "GLWindow.create(caps) recreate", null );
				// equal to addAncestor listeners but that's too late by far
				gl_window.addGLEventListener(glWindowInitListener);
			}
			else
			{
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onResume pre addNotify", null );
				canvas3D2D.addNotify();
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onResume post addNotify", null );
			}

			if (HomeComponent3D.this.getUserVisibleHint())
			{
				canvas3D2D.startRenderer();
			}
		}
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onResume", null );
	}

	@Override
	public void onPause()
	{
		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onPause", null );

		// so this is part of the exit so we need to call removeNotify in all cases, all re-entries will arrive back at display eventually
		// and display will always call addNotify
		if(canvas3D2D != null)
		{
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onPause pre removeNotify", null );
			canvas3D2D.stopRenderer();
			canvas3D2D.removeNotify();
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onPause post removeNotify", null );
		}

		PlanComponent.PieceOfFurnitureModelIcon.pauseOffScreenRendering();
		// note super onPause does NOT save state but marks to preserve ready for another lifecycle change like onStop
		super.onPause();

		PlanComponent.PieceOfFurnitureModelIcon.unpauseOffScreenRendering();
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onPause", null );
	}

	@Override
	public void onStop()
	{
		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onStop", null );
		// MUST output GLStatePreserved on console, or it won't restart
		super.onStop();
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onStop", null );
	}

	@Override
	public void onDestroy()
	{
		Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onDestroy", null );
		// now we want to dump the universe as this fragment is being garbage collected shortly
		if(canvas3D2D != null)
		{
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onDestroy pre removeNotify", null );
			canvas3D2D.stopRenderer();
			canvas3D2D.removeNotify();
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onDestroy post removeNotify", null );
		}

		PlanComponent.PieceOfFurnitureModelIcon.destroyUniverse();
		if(onscreenUniverse != null)
		{
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onscreenUniverse.cleanup();", null );
			try
			{
				onscreenUniverse.cleanup();
			}catch(Exception e)
			{
				// in production I don't care about exceptions now, but I do care about crashing.
				e.printStackTrace();
			}
			onscreenUniverse = null;
		}
		PlanComponent.PieceOfFurnitureModelIcon.pauseOffScreenRendering();
		super.onDestroy();
		PlanComponent.PieceOfFurnitureModelIcon.unpauseOffScreenRendering();

		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onDestroy", null );
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {

		// tell walls to update now
		if (isVisibleToUser && wallChangeListener != null)
		{
			wallChangeListener.propertyChange(new PropertyChangeEvent(this, RUN_UPDATES, null, null));
		}


		// we don't want the camera listening to update while non visible (it's expensive)
		// so exit and enter current based on visibility
		if( home != null )
		{
			if(isVisibleToUser)
			{
				if(cameraPriorToUpdatePause != null)
				{
					home.setCamera(cameraPriorToUpdatePause);
					cameraPriorToUpdatePause = null;
				}
			}
			else
			{
				// we may already be paused so we don't want to grab the -1 fov camera
				if(cameraPriorToUpdatePause == null)
				{
					cameraPriorToUpdatePause = home.getCamera();
					Camera noupdate = cameraPriorToUpdatePause.clone();
					noupdate.setFieldOfView(-1);
					home.setCamera(noupdate);
				}
			}
		}

		if(isVisibleToUser && getContext() != null)
			possiblyShowWelcomeScreen(getContext(), WELCOME_SCREEN_UNWANTED, R.string.component3dview_welcometext, preferences);



		if (canvas3D2D != null)
		{
			if (isVisibleToUser)
			{
				canvas3D2D.startRenderer();
			}
			else
			{
				canvas3D2D.stopRenderer();
			}
		}

		super.setUserVisibleHint(isVisibleToUser);
	}

	private Camera cameraPriorToUpdatePause = null;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		mOptionsMenu = menu;// for later use
		inflater.inflate(R.menu.home_component3d_menu, menu);
		menu.findItem(R.id.virtualvisit).setChecked(home.getCamera() == home.getObserverCamera());
		boolean allLevelsVisible = home.getEnvironment().isAllLevelsVisible();
		menu.findItem(R.id.viewalllevels).setChecked(allLevelsVisible);

		createGoToPointOfViewMenu(home, menu.findItem(R.id.gotopov));

		SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
		boolean deoptomize = settings.getBoolean(DEOPTOMIZE, false);
		menu.findItem(R.id.deoptomize).setChecked(deoptomize);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.virtualvisit).setTitle(preferences.getLocalizedString(
					com.eteks.sweethome3d.android_props.HomePane.class, "VIEW_FROM_OBSERVER.Name"));
		setIconFromSelector(menu.findItem(R.id.virtualvisit), R.drawable.virtualvist_selector);
		menu.findItem(R.id.gotopov).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "GO_TO_POINT_OF_VIEW.Name"));

		menu.findItem(R.id.modifyvirtualvisitor).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_OBSERVER.Name"));
		menu.findItem(R.id.storepov).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "STORE_POINT_OF_VIEW.Name"));
		menu.findItem(R.id.deletepov).setTitle(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.HomePane.class, "DELETE_POINTS_OF_VIEW.Name"));
		menu.findItem(R.id.viewalllevels).setTitle(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.HomePane.class, "DISPLAY_ALL_LEVELS.Name"));
		menu.findItem(R.id.modify3dview).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_3D_ATTRIBUTES.Name"));
		menu.findItem(R.id.createPhoto).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_PHOTO.Name"));
		menu.findItem(R.id.exportToObj).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "EXPORT_TO_OBJ.Name"));

		updateGoToPointOfViewMenu(menu.findItem(R.id.gotopov), home);

		super.onPrepareOptionsMenu(menu);
	}

	//PJPJPJ Taken from HomePane and adapted
	private void createGoToPointOfViewMenu(final Home home,
										   final MenuItem goToPointOfViewMenu) {
		updateGoToPointOfViewMenu(goToPointOfViewMenu, home);
		home.addPropertyChangeListener(Home.Property.STORED_CAMERAS,
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent ev) {
						// need to do this on the EDT
						Handler mainHandler = new Handler(getContext().getMainLooper());
						Runnable myRunnable = new Runnable() {
							@Override
							public void run() {
								updateGoToPointOfViewMenu(goToPointOfViewMenu, home);
							}
						};
						mainHandler.post(myRunnable);
					}
				});
	}

	private static int NoViewMenuId = -1;
	private static int MENU_STORED_CAMERAS = 10;
	//PJPJPJ Taken from HomePane and adapted
	/**
	 * Updates Go to point of view menu items from the cameras stored in home.
	 */
	private void updateGoToPointOfViewMenu(MenuItem goToPointOfViewMenu,
										   Home home) {
		List<Camera> storedCameras = home.getStoredCameras();
		//TODO: very much sort this list so its n the same order ech time
		goToPointOfViewMenu.getSubMenu().clear();

		if (storedCameras.isEmpty()) {
			goToPointOfViewMenu.setEnabled(false);
			String name = SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomePane.class, "NoStoredPointOfView");
			goToPointOfViewMenu.getSubMenu().add(MENU_STORED_CAMERAS, NoViewMenuId, Menu.NONE, name);
			//goToPointOfViewMenu.add(new ResourceAction(preferences, HomePane.class, "NoStoredPointOfView", false));
		} else {
			goToPointOfViewMenu.setEnabled(true);
			int menuId = Menu.FIRST;
			for (final Camera camera : storedCameras) {
				goToPointOfViewMenu.getSubMenu().add(MENU_STORED_CAMERAS, menuId++, Menu.NONE, camera.getName());

				// this is below in the onOptionsItemSelected
				//goToPointOfViewMenu.add(new AbstractAction(camera.getName()) {
				//			public void actionPerformed(ActionEvent e) {
				//				controller.getHomeController3D().goToCamera(camera);
				//			}
				//		});
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle camera selections first
		if(item.getGroupId() == MENU_STORED_CAMERAS)
		{
			String cameraName = item.getTitle().toString();
			List<Camera> storedCameras = home.getStoredCameras();
			for (final Camera camera : storedCameras)
			{
				if(cameraName.equals(camera.getName()))
				{
					HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
					if(homeController != null)
					{
						homeController.getHomeController3D().goToCamera(camera);
						// update the check box item nicely
						MenuItem vv = mOptionsMenu.findItem(R.id.virtualvisit);
						vv.setChecked(home.getCamera() == home.getObserverCamera());
						setIconFromSelector(vv, R.drawable.virtualvist_selector);
						return true;
					}
				}
			}
		}
		else
		{
			// Handle item selection
			switch (item.getItemId())
			{
				case R.id.goto2Dview:
					((Renovations3DActivity)getActivity()).mViewPager.setCurrentItem(1, false);// true cause no render! god knows why
					break;
				case R.id.virtualvisit:
					item.setChecked(!item.isChecked());
					setIconFromSelector(item, R.drawable.virtualvist_selector);
					if (item.isChecked())
						controller.viewFromObserver();
					else
						controller.viewFromTop();
					break;
				case R.id.gotopov:
					// do nothing it just nicely opens the list for us
					break;
				case R.id.modifyvirtualvisitor:
					HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
					if(homeController != null)
					{
						homeController.getPlanController().modifyObserverCamera();
					}
					break;
				case R.id.storepov:
					//I must get off the EDT and ask the question in a blocking manner
					Thread t2 = new Thread()
					{
						public void run()
						{
							HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
							if(homeController != null)
							{
								homeController.storeCamera();
							}
						}
					};
					t2.start();
					break;
				case R.id.deletepov:
					Thread t3 = new Thread()
					{
						public void run()
						{
							HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
							if(homeController != null)
							{
								homeController.deleteCameras();
							}
						}
					};
					t3.start();
					break;
				case R.id.viewalllevels:
					item.setChecked(!item.isChecked());
					if (item.isChecked())
						controller.displayAllLevels();
					else
						controller.displaySelectedLevel();
					break;
				case R.id.modify3dview:
					controller.modifyAttributes();
					break;
				case R.id.createPhoto:
						HomeController homeController2 = ((Renovations3DActivity)getActivity()).getHomeController();
						if(homeController2 != null)
						{
							homeController2.createPhoto();
						}
					break;
				case R.id.exportToObj:
					Thread t4 = new Thread()
					{
						public void run(){
							HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
							if(homeController != null)
							{
								homeController.exportToOBJ();
							}
						}
					};
					t4.start();
				break;
				case R.id.deoptomize:
					item.setChecked(!item.isChecked());
					setDeoptomize(item.isChecked());
					Renovations3DActivity.logFireBaseLevelUp("setDeoptomizeMenu", "deoptomize " + item.isChecked());
					Toast.makeText(getContext(), "This requires a reload of your home to take effect.", Toast.LENGTH_LONG).show();
					break;
				case R.id.show_jogl_status:
					Intent myIntent = new Intent(this.getContext(), JoglStatusActivity.class);
					this.startActivity(myIntent);
					break;
				default:
					return super.onOptionsItemSelected(item);
			}
		}
		return true;
	}

	private void setIconFromSelector(MenuItem item, int resId)
	{
		StateListDrawable stateListDrawable = (StateListDrawable) ContextCompat.getDrawable(getContext(), resId);
		int[] state = {item.isChecked() ? android.R.attr.state_checked : android.R.attr.state_empty};
		stateListDrawable.setState(state);
		item.setIcon(stateListDrawable.getCurrent());
	}

	public void setDeoptomize(boolean deoptomize)
	{
		//DEBUG to fix Nexus 5, Redmi Note 3 Pro, possibly OnePlus3 (OnePlus3)
		JoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = !deoptomize;
		JoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = !deoptomize;
		//JoglesPipeline.LATE_RELEASE_CONTEXT = !deoptomize;
		JoglesPipeline.MINIMISE_NATIVE_CALLS_TRANSPARENCY = !deoptomize;
		JoglesPipeline.MINIMISE_NATIVE_CALLS_TEXTURE = !deoptomize;

		// PJ not needed now the shaders are per vert SimpleShaderAppearance.setDISABLE_LIGHTS(deoptomize);

		SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(DEOPTOMIZE, deoptomize);
		editor.apply();

	}

	private enum ActionType
	{
		MOVE_CAMERA_FORWARD, MOVE_CAMERA_FAST_FORWARD, MOVE_CAMERA_BACKWARD, MOVE_CAMERA_FAST_BACKWARD, MOVE_CAMERA_LEFT, MOVE_CAMERA_FAST_LEFT, MOVE_CAMERA_RIGHT, MOVE_CAMERA_FAST_RIGHT, ROTATE_CAMERA_YAW_LEFT, ROTATE_CAMERA_YAW_FAST_LEFT, ROTATE_CAMERA_YAW_RIGHT, ROTATE_CAMERA_YAW_FAST_RIGHT, ROTATE_CAMERA_PITCH_UP, ROTATE_CAMERA_PITCH_FAST_UP, ROTATE_CAMERA_PITCH_DOWN, ROTATE_CAMERA_PITCH_FAST_DOWN, ELEVATE_CAMERA_UP, ELEVATE_CAMERA_FAST_UP, ELEVATE_CAMERA_DOWN, ELEVATE_CAMERA_FAST_DOWN
	}

	//private static final boolean JAVA3D_1_5 = VirtualUniverse.getProperties().get("j3d.version") != null
	//		&& ((String) VirtualUniverse.getProperties().get("j3d.version")).startsWith("1.5");

	private Home home;
	private boolean displayShadowOnFloor;
	private Object3DFactory object3dFactory;
	private final Map<Selectable, Object3DBranch> homeObjects = new HashMap<Selectable, Object3DBranch>();
	private Light[] defaultLights;
	private Collection<Selectable> homeObjectsToUpdate;
	private Collection<Selectable> lightScopeObjectsToUpdate;
	//private Component component3D;
	//PJPJPJPJ
	private Canvas3D2D canvas3D2D;
	private SimpleUniverse onscreenUniverse;
	private Camera camera;
	// Listeners bound to home that updates 3D scene objects
	private PropertyChangeListener cameraChangeListener;
	private PropertyChangeListener homeCameraListener;
	private PropertyChangeListener skyColorListener;
	private PropertyChangeListener groundChangeListener;
	private PropertyChangeListener lightColorListener;
	private PropertyChangeListener subpartSizeListener;
	private PropertyChangeListener wallsAlphaListener;
	private PropertyChangeListener drawingModeListener;
	private CollectionListener<Level> levelListener;
	private PropertyChangeListener levelChangeListener;
	private CollectionListener<Wall> wallListener;
	private PropertyChangeListener wallChangeListener;
	private CollectionListener<HomePieceOfFurniture> furnitureListener;
	private PropertyChangeListener furnitureChangeListener;
	private CollectionListener<Room> roomListener;
	private PropertyChangeListener roomChangeListener;
	private CollectionListener<Label> labelListener;
	private PropertyChangeListener labelChangeListener;

	private SelectionListener selectionOutliningListener;

	// Offscreen printed image cache
	// Creating an offscreen buffer is a quite lengthy operation so we keep the last printed image in this field
	// This image should be set to null each time the 3D view changes
	private BufferedImage printedImageCache;
	private BoundingBox approximateHomeBoundsCache;
	private SimpleUniverse offscreenUniverse;

	//PJ private JComponent navigationPanel;
	//PJ private ComponentListener navigationPanelListener;
	//PJ private BufferedImage navigationPanelImage;
	private Area lightScopeOutsideWallsAreaCache;


	//PJPJPJ record from the init call to gl window init call
	private UserPreferences preferences;
	private HomeController3D controller;


	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture,
	 * with no controller.
	 *
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home)
	{
		init(home, null);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 *
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home, HomeController3D controller)
	{
		init(home, null, controller);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture,
	 * with shadows on the floor.
	 *
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home, UserPreferences preferences, boolean displayShadowOnFloor)
	{
		init(home, preferences, new Object3DBranchFactory(), displayShadowOnFloor, null);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 *
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home, UserPreferences preferences, HomeController3D controller)
	{
		init(home, preferences, new Object3DBranchFactory(), false, controller);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 *
	 * @param home            the home to display in this component
	 * @param preferences     user preferences
	 * @param object3dFactory a factory able to create 3D objects from <code>home</code> items.
	 *                        The {@link Object3DFactory#createObject3D(Home, Selectable, boolean) createObject3D} of
	 *                        this factory is expected to return an instance of {@link Object3DBranch} in current implementation.
	 * @param controller      the controller that manages modifications in <code>home</code>.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home, UserPreferences preferences, Object3DFactory object3dFactory, HomeController3D controller)
	{
		init(home, preferences, object3dFactory, false, controller);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 *
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home, UserPreferences preferences, Object3DFactory object3dFactory, boolean displayShadowOnFloor,
					 HomeController3D controller)
	{
		initialized = true;
		//record for init
		this.preferences = preferences;
		this.controller = controller;


		this.home = home;
		this.displayShadowOnFloor = displayShadowOnFloor;
		this.object3dFactory = object3dFactory != null ? object3dFactory : new Object3DBranchFactory();

		if (controller != null)
		{
			createActions(controller);
			installKeyboardActions();
			// Let this component manage focus
			//PJ setFocusable(true);
			//PJ SwingTools.installFocusBorder(this);
		}

		//GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//if (graphicsEnvironment.getScreenDevices().length == 1)
		{
			// If only one screen device is available, create canvas 3D immediately,
			// otherwise create it once the screen device of the parent is known

			//PJPJ defferred to gl window init
			//createComponent3D(null//graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration()
			//		, preferences, controller);
		}

		// Add an ancestor listener to create canvas 3D and its universe once this component is made visible
		// and clean up universe once its parent frame is disposed
		//PJPJP put into gl window listener
		//addAncestorListener(preferences, controller, displayShadowOnFloor);


		//PJPJPJ for outlining
		selectionOutliningListener = new SelectionOutliningListener();
		home.addSelectionListener(selectionOutliningListener);
	}


	private class SelectionOutliningListener implements SelectionListener
	{
		@Override
		public void selectionChanged(SelectionEvent selectionEvent)
		{
			for(Selectable sel : homeObjects.keySet())
			{
				boolean isSelected = selectionEvent.getSelectedItems().contains(sel);
				Object3DBranch obj3D = homeObjects.get(sel);
				if(obj3D.isShowOutline()!=isSelected)
					obj3D.showOutline(isSelected);
			}
		}
	}

	private void addAncestorListener(final UserPreferences preferences, final HomeController3D controller,
									 final boolean displayShadowOnFloor)
	{
		//the base calls here are moved into the gl window listener at onCreate so the display is not lost


	}

	/**
	 * Creates the 3D component associated with the given <code>configuration</code> device.
	 */
	private void createComponent3D(GraphicsConfiguration configuration, UserPreferences preferences, HomeController3D controller)
	{
		//PJPJPJPJ
		canvas3D2D = Component3DManager.getInstance().getOnscreenCanvas3D(gl_window, new Component3DManager.RenderingObserver()
		{
			//PJPJPJ
			//private Shape3D dummyShape;

			public void canvas3DSwapped(Canvas3D canvas3D)
			{
				//System.out.println("canvas3DSwapped");
			}

			public void canvas3DPreRendered(Canvas3D canvas3D)
			{
				//System.out.println("canvas3DPreRendered");
			}

			public void canvas3DPostRendered(Canvas3D canvas3D)
			{
				//System.out.println("canvas3DPostRendered");
			}
		});

		//moved into gl_window display call for life cycle clarity
		//canvas3D2D.addNotify();


		if (controller != null)
		{
			addMouseListeners(controller, this.canvas3D2D);

			//createActions(controller);
			//installKeyboardActions();
			// Let this component manage focus
			//setFocusable(true);
			//SwingTools.installFocusBorder(this);
		}
	}



//	@Override
/*	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (this.component3D != null)
		{
			this.component3D.setVisible(visible);

			//PJPJPJPJ do something interesting here
			this.canvas3D.setVisible(visible);
		}
	}*/



	/**
	 * Returns a new 3D universe that displays <code>home</code> objects.
	 */
	private SimpleUniverse createUniverse(boolean displayShadowOnFloor, boolean listenToHomeUpdates, boolean waitForLoading)
	{
		// Create a universe bound to no canvas 3D
		ViewingPlatform viewingPlatform = new ViewingPlatform();
		// Add an interpolator to view transform to get smooth transition
		TransformGroup viewPlatformTransform = viewingPlatform.getViewPlatformTransform();
		CameraInterpolator cameraInterpolator = new CameraInterpolator(viewPlatformTransform);
		cameraInterpolator.setSchedulingBounds(new BoundingSphere(new Point3d(), 1E7));
		viewPlatformTransform.addChild(cameraInterpolator);
		viewPlatformTransform.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

		Viewer viewer = new Viewer(new Canvas3D[0]);
		SimpleUniverse universe = new SimpleUniverse(viewingPlatform, viewer);

		View view = viewer.getView();
		view.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);

		// Update field of view from current camera
		updateView(view, this.home.getCamera(), this.home.getTopCamera() == this.home.getCamera());

		// Update point of view from current camera
		updateViewPlatformTransform(viewPlatformTransform, this.home.getCamera(), false);

		// Add camera listeners to update later point of view from camera
		if (listenToHomeUpdates)
		{
			addCameraListeners(view, viewPlatformTransform);
		}

		// Link scene matching home to universe
		universe.addBranchGraph(createSceneTree(displayShadowOnFloor, listenToHomeUpdates, waitForLoading));

		return universe;
	}

	/**
	 * Remove all listeners bound to home that updates 3D scene objects.
	 */
	private void removeHomeListeners()
	{
		this.home.removePropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
		HomeEnvironment homeEnvironment = this.home.getEnvironment();
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_COLOR, this.skyColorListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_TEXTURE, this.skyColorListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.groundChangeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.groundChangeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SUBPART_SIZE_UNDER_LIGHT, this.subpartSizeListener);
		this.home.getCamera().removePropertyChangeListener(this.cameraChangeListener);
		this.home.removeLevelsListener(this.levelListener);
		for (Level level : this.home.getLevels())
		{
			level.removePropertyChangeListener(this.levelChangeListener);
		}
		this.home.removeWallsListener(this.wallListener);
		for (Wall wall : this.home.getWalls())
		{
			wall.removePropertyChangeListener(this.wallChangeListener);
		}
		this.home.removeFurnitureListener(this.furnitureListener);
		for (HomePieceOfFurniture piece : this.home.getFurniture())
		{
			piece.removePropertyChangeListener(this.furnitureChangeListener);
			if (piece instanceof HomeFurnitureGroup)
			{
				for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup) piece).getAllFurniture())
				{
					childPiece.removePropertyChangeListener(this.furnitureChangeListener);
				}
			}
		}
		this.home.removeRoomsListener(this.roomListener);
		for (Room room : this.home.getRooms())
		{
			room.removePropertyChangeListener(this.roomChangeListener);
		}
		this.home.removeLabelsListener(this.labelListener);
		for (Label label : this.home.getLabels())
		{
			label.removePropertyChangeListener(this.labelChangeListener);
		}
	}

	/**
	 * Prints this component to make it fill <code>pageFormat</code> imageable size.
	 */
	/*public int print(Graphics g, PageFormat pageFormat, int pageIndex)
	{
		if (pageIndex == 0)
		{
			// Compute printed image size to render 3D view in 150 dpi
			double printSize = Math.min(pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
			int printedImageSize = (int) (printSize / 72 * 150);
			if (this.printedImageCache == null || this.printedImageCache.getWidth() != printedImageSize)
			{
				try
				{
					this.printedImageCache = getOffScreenImage(printedImageSize, printedImageSize);
				}
				catch (IllegalRenderingStateException ex)
				{
					// If off screen canvas failed, consider that 3D view page doesn't exist
					return NO_SUCH_PAGE;
				}
			}

			Graphics2D g2D = (Graphics2D) g.create();
			// Center the 3D view in component
			g2D.translate(pageFormat.getImageableX() + (pageFormat.getImageableWidth() - printSize) / 2,
					pageFormat.getImageableY() + (pageFormat.getImageableHeight() - printSize) / 2);
			double scale = printSize / printedImageSize;
			g2D.scale(scale, scale);
			g2D.drawImage(this.printedImageCache, 0, 0, this);
			g2D.dispose();

			return PAGE_EXISTS;
		}
		else
		{
			return NO_SUCH_PAGE;
		}
	}*/

	/**
	 * Optimizes this component for the creation of a sequence of multiple off screen images.
	 * Once off screen images are generated with {@link #getOffScreenImage(int, int) getOffScreenImage},
	 * call {@link #endOffscreenImagesCreation() endOffscreenImagesCreation} method to free resources.
	 */
	public void startOffscreenImagesCreation()
	{
		if (this.offscreenUniverse == null)
		{
			if (this.onscreenUniverse != null)
			{
				throw new IllegalStateException("Can't listen to home changes offscreen and onscreen at the same time");
			}
			this.offscreenUniverse = createUniverse(this.displayShadowOnFloor, true, true);
			// Replace textures by clones because Java 3D doesn't accept all the time
			// to share textures between offscreen and onscreen environments
			Map<Texture, Texture> replacedTextures = new HashMap<Texture, Texture>();
			for (Iterator<BranchGroup> it = this.offscreenUniverse.getLocale().getAllBranchGraphs(); it.hasNext(); )
			{
				cloneTexture((Node) it.next(), replacedTextures);
			}
		}
	}

	/**
	 * Returns an image of the home viewed by this component at the given size.
	 */
	public BufferedImage getOffScreenImage(int width, int height)
	{
		List<Selectable> selectedItems = this.home.getSelectedItems();
		SimpleUniverse offScreenImageUniverse = null;
		try
		{
			View view;
			if (this.offscreenUniverse == null)
			{
				offScreenImageUniverse = createUniverse(this.displayShadowOnFloor, false, true);
				view = offScreenImageUniverse.getViewer().getView();
				// Replace textures by clones because Java 3D doesn't accept all the time
				// to share textures between offscreen and onscreen environments

				//PJPJPJ this does not appear necessary on android

				/*Map<Texture, Texture> replacedTextures = new HashMap<Texture, Texture>();
				for (Iterator<BranchGroup> it = offScreenImageUniverse.getLocale().getAllBranchGraphs(); it.hasNext(); )
				{
					cloneTexture(it.next(), replacedTextures);
				}*/
			}
			else
			{
				view = this.offscreenUniverse.getViewer().getView();
			}

			// Empty temporarily selection to create the off screen image
			List<Selectable> emptySelection = Collections.emptyList();
			this.home.setSelectedItems(emptySelection);
			//PJPJPJ
			return Component3DManager.getInstance().getOffScreenImage(view, width, height);
		}
		finally
		{
			// Restore selection
			this.home.setSelectedItems(selectedItems);
			if (offScreenImageUniverse != null)
			{
				try
				{
					offScreenImageUniverse.cleanup();
				}catch(Exception e)
				{
					// in production I don't care about exceptions now, but I do care about crashing.
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Replace the textures set on node shapes by clones.
	 */
	private void cloneTexture(Node node, Map<Texture, Texture> replacedTextures)
	{
		if (node instanceof Group)
		{
			// Enumerate children
			Iterator<Node> enumeration = ((Group) node).getAllChildren();
			while (enumeration.hasNext())
			{
				cloneTexture( enumeration.next(), replacedTextures);
			}
		}
		else if (node instanceof Link)
		{
			cloneTexture(((Link) node).getSharedGroup(), replacedTextures);
		}
		else if (node instanceof Shape3D)
		{
			Appearance appearance = ((Shape3D) node).getAppearance();
			if (appearance != null)
			{
				Texture texture = appearance.getTexture();
				if (texture != null)
				{
					Texture replacedTexture = replacedTextures.get(texture);
					if (replacedTexture == null)
					{
						replacedTexture = (Texture) texture.cloneNodeComponent(false);
						replacedTextures.put(texture, replacedTexture);
					}
					appearance.setTexture(replacedTexture);
				}
			}
		}
	}

	/**
	 * Frees unnecessary resources after the creation of a sequence of multiple offscreen images.
	 */
	public void endOffscreenImagesCreation()
	{
		if (this.offscreenUniverse != null)
		{
			this.offscreenUniverse.cleanup();
			removeHomeListeners();
			this.offscreenUniverse = null;
		}
	}

	/**
	 * Adds listeners to home to update point of view from current camera.
	 */
	private void addCameraListeners(final View view, final TransformGroup viewPlatformTransform)
	{
		this.cameraChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				// Update view transform later to avoid flickering in case of multiple camera changes
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						updateView(view, home.getCamera(), home.getTopCamera() == home.getCamera());
						updateViewPlatformTransform(viewPlatformTransform, home.getCamera(), false);//PJPJ treu made fale, which removed a jitter effect
					}
				});
			}
		};
		this.home.getCamera().addPropertyChangeListener(this.cameraChangeListener);
		this.homeCameraListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				updateView(view, home.getCamera(), home.getTopCamera() == home.getCamera());
				//PJPJPJ false change to true and animate lengthed for cool effect
				updateViewPlatformTransform(viewPlatformTransform, home.getCamera(), true, 750);
				// Add camera change listener to new active camera
				((Camera) ev.getOldValue()).removePropertyChangeListener(cameraChangeListener);
				home.getCamera().addPropertyChangeListener(cameraChangeListener);
			}
		};
		this.home.addPropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
	}

	/**
	 * Updates <code>view</code> from <code>camera</code> field of view.
	 */
	private void updateView(View view, Camera camera, boolean topCamera)
	{
		float fieldOfView = camera.getFieldOfView();
		if (fieldOfView == 0)
		{
			fieldOfView = (float) (Math.PI * 63 / 180);
		}
		view.setFieldOfView(fieldOfView);
		double frontClipDistance;
		double backClipDistance;
		if (topCamera)
		{
			BoundingBox approximateHomeBounds = getApproximateHomeBoundsCache();
			if (approximateHomeBounds == null)
			{
				frontClipDistance = 5;
			}
			else
			{
				Point3d lower = new Point3d();
				approximateHomeBounds.getLower(lower);
				Point3d upper = new Point3d();
				approximateHomeBounds.getUpper(upper);
				// Use a variable front clip distance for top camera depending on the distance to home objects center
				frontClipDistance = 1 + Math.sqrt(Math.pow((lower.x + upper.x) / 2 - camera.getX(), 2)
						+ Math.pow((lower.y + upper.y) / 2 - camera.getY(), 2) + Math.pow((lower.z + upper.z) / 2 - camera.getZ(), 2))
						/ 100;
			}
			// It's recommended to keep ratio between back and front clip distances under 3000
			backClipDistance = frontClipDistance * 3000;
		}
		else
		{
			// Use a variable front clip distance for observer camera depending on the elevation
			// Caution: check that a white zone doesn't appear at the horizon in off screen images
			// when camera is at an intermediate elevation

			// Under 125 cm keep a front clip distance equal to 2.5 cm
			frontClipDistance = 2.5;
			backClipDistance = frontClipDistance * 5000;
			final float minElevation = 125;
			if (camera.getZ() > minElevation)
			{
				final float intermediateGrowFactor = 1 / 250f;
				BoundingBox approximateHomeBounds = getApproximateHomeBoundsCache();
				float highestPoint = 0;
				if (approximateHomeBounds != null)
				{
					Point3d upper = new Point3d();
					approximateHomeBounds.getUpper(upper);
					highestPoint = Math.min((float) upper.z, 10000f);
				}
				if (camera.getZ() < highestPoint + minElevation)
				{
					// Between 200 cm and the highest point, make front clip distance grow slowly and increase front/back ratio
					frontClipDistance += (camera.getZ() - minElevation) * intermediateGrowFactor;
					backClipDistance += (frontClipDistance - 2.5) * 25000;
				}
				else
				{
					// Above, make front clip distance grow faster
					frontClipDistance += highestPoint * intermediateGrowFactor + (camera.getZ() - highestPoint - minElevation) / 50;
					backClipDistance += +(highestPoint * intermediateGrowFactor) * 25000
							+ (frontClipDistance - highestPoint * intermediateGrowFactor - 2.5) * 5000;
				}
			}
		}

		// Update front and back clip distance
		view.setFrontClipDistance(frontClipDistance);
		view.setBackClipDistance(backClipDistance);
		clearPrintedImageCache();
	}

	/**
	 * Returns quickly computed bounds of the objects in home.
	 */
	private BoundingBox getApproximateHomeBoundsCache()
	{
		if (this.approximateHomeBoundsCache == null)
		{
			BoundingBox approximateHomeBounds = null;
			for (HomePieceOfFurniture piece : this.home.getFurniture())
			{
				if (piece.isVisible() && (piece.getLevel() == null || piece.getLevel().isViewable()))
				{
					Point3d pieceLocation = new Point3d(piece.getX(), piece.getY(), piece.getGroundElevation());
					if (approximateHomeBounds == null)
					{
						approximateHomeBounds = new BoundingBox(pieceLocation, pieceLocation);
					}
					else
					{
						approximateHomeBounds.combine(pieceLocation);
					}
				}
			}
			for (Wall wall : this.home.getWalls())
			{
				if (wall.getLevel() == null || wall.getLevel().isViewable())
				{
					Point3d startPoint = new Point3d(wall.getXStart(), wall.getYStart(),
							wall.getLevel() != null ? wall.getLevel().getElevation() : 0);
					if (approximateHomeBounds == null)
					{
						approximateHomeBounds = new BoundingBox(startPoint, startPoint);
					}
					else
					{
						approximateHomeBounds.combine(startPoint);
					}
					approximateHomeBounds.combine(new Point3d(wall.getXEnd(), wall.getYEnd(),
							startPoint.z + (wall.getHeight() != null ? wall.getHeight() : this.home.getWallHeight())));
				}
			}
			for (Room room : this.home.getRooms())
			{
				if (room.getLevel() == null || room.getLevel().isViewable())
				{
					Point3d center = new Point3d(room.getXCenter(), room.getYCenter(),
							room.getLevel() != null ? room.getLevel().getElevation() : 0);
					if (approximateHomeBounds == null)
					{
						approximateHomeBounds = new BoundingBox(center, center);
					}
					else
					{
						approximateHomeBounds.combine(center);
					}
				}
			}
			for (Label label : this.home.getLabels())
			{
				if ((label.getLevel() == null || label.getLevel().isViewable()) && label.getPitch() != null)
				{
					Point3d center = new Point3d(label.getX(), label.getY(), label.getGroundElevation());
					if (approximateHomeBounds == null)
					{
						approximateHomeBounds = new BoundingBox(center, center);
					}
					else
					{
						approximateHomeBounds.combine(center);
					}
				}
			}
			this.approximateHomeBoundsCache = approximateHomeBounds;
		}
		return this.approximateHomeBoundsCache;
	}

	/**
	 * Frees printed image kept in cache.
	 */
	private void clearPrintedImageCache()
	{
		this.printedImageCache = null;
	}

	/**
	 * Updates <code>viewPlatformTransform</code> transform from <code>camera</code> angles and location.
	 */
	private void updateViewPlatformTransform(TransformGroup viewPlatformTransform, Camera camera, boolean updateWithAnimation)
	{
		updateViewPlatformTransform(viewPlatformTransform,  camera,  updateWithAnimation, CameraInterpolator.DEFAULT_ANIMATE_LEN);
	}

	private void updateViewPlatformTransform(TransformGroup viewPlatformTransform, Camera camera, boolean updateWithAnimation, long animateTime)
	{
		if (updateWithAnimation)
		{
			// Get the camera interpolator
			CameraInterpolator cameraInterpolator = (CameraInterpolator) viewPlatformTransform
					.getChild(viewPlatformTransform.numChildren() - 1);
			cameraInterpolator.setLenAnimationMS(animateTime);
			cameraInterpolator.moveCamera(camera);
		}
		else
		{
			Transform3D transform = new Transform3D();
			updateViewPlatformTransform(transform, camera.getX(), camera.getY(), camera.getZ(), camera.getYaw(), camera.getPitch());
			viewPlatformTransform.setTransform(transform);
		}
		clearPrintedImageCache();
	}

	/**
	 * An interpolator that computes smooth camera moves.
	 */
	private class CameraInterpolator extends TransformInterpolator
	{
		public static final long DEFAULT_ANIMATE_LEN = 150;
		private final ScheduledExecutorService scheduledExecutor;
		private Camera initialCamera;
		private Camera finalCamera;

		private long lenAnimationMS = 150;

		public static final float onepi = (float)Math.PI * 1f;
		public static final float twopi = (float)Math.PI * 2f;
		public static final float fivepi = (float)Math.PI * 5f;


		public CameraInterpolator(TransformGroup transformGroup)
		{
			this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			setTarget(transformGroup);
		}
		public void setLenAnimationMS(long lenAnimationMS)
		{
			this.lenAnimationMS = lenAnimationMS;
		}


		/**
		 * Moves the camera to a new location.
		 */
		public void moveCamera(Camera finalCamera)
		{
			if (this.finalCamera == null || this.finalCamera.getX() != finalCamera.getX() || this.finalCamera.getY() != finalCamera.getY()
					|| this.finalCamera.getZ() != finalCamera.getZ() || this.finalCamera.getYaw() != finalCamera.getYaw()
					|| this.finalCamera.getPitch() != finalCamera.getPitch())
			{
				synchronized (this)
				{
					//PJ note mod 2pi added to stopp crazy spins as camera values are not wrapped
					Alpha alpha = getAlpha();
					if (alpha == null || alpha.finished())
					{
						this.initialCamera = new Camera(camera.getX(), camera.getY(), camera.getZ(), camera.getYaw() % twopi, camera.getPitch(),
								camera.getFieldOfView());
					}
					else if (alpha.value() < 0.1)
					{
						Transform3D finalTransformation = new Transform3D();
						// Jump directly to final location
						updateViewPlatformTransform(finalTransformation, this.finalCamera.getX(), this.finalCamera.getY(),
								this.finalCamera.getZ(), this.finalCamera.getYaw() % twopi, this.finalCamera.getPitch());
						getTarget().setTransform(finalTransformation);
						this.initialCamera = this.finalCamera;
					}
					else
					{
						// Compute initial location from current alpha value
						float shortest_angle=((((this.finalCamera.getYaw() - this.initialCamera.getYaw()) % twopi) + fivepi) % twopi) - onepi;
						this.initialCamera = new Camera(
								this.initialCamera.getX() + (this.finalCamera.getX() - this.initialCamera.getX()) * alpha.value(),
								this.initialCamera.getY() + (this.finalCamera.getY() - this.initialCamera.getY()) * alpha.value(),
								this.initialCamera.getZ() + (this.finalCamera.getZ() - this.initialCamera.getZ()) * alpha.value(),
								this.initialCamera.getYaw() % twopi + (shortest_angle * alpha.value()),
								this.initialCamera.getPitch()
										+ (this.finalCamera.getPitch() - this.initialCamera.getPitch()) * alpha.value(),
								finalCamera.getFieldOfView());
					}
					this.finalCamera = new Camera(finalCamera.getX(), finalCamera.getY(), finalCamera.getZ(), finalCamera.getYaw() % twopi,
							finalCamera.getPitch(), finalCamera.getFieldOfView());

					// Create an animation that will interpolate camera location
					// between initial camera and final camera in 150 ms
					if (alpha == null)
					{
						alpha = new Alpha(1, lenAnimationMS);
						setAlpha(alpha);
					}
					// Start animation now
					alpha.setStartTime(System.currentTimeMillis());
					// In case system is overloaded computeTransform won't be called
					// ensure final location will always be set after 150 ms
					this.scheduledExecutor.schedule(new Runnable()
					{
						public void run()
						{
							if (getAlpha().value() == 1)
							{
								Transform3D transform = new Transform3D();
								computeTransform(1, transform);
								getTarget().setTransform(transform);
							}
						}
					}, lenAnimationMS, TimeUnit.MILLISECONDS);
				}
			}
		}

		@Override
		public synchronized void computeTransform(float alpha, Transform3D transform)
		{

			float shortest_angle=((((this.finalCamera.getYaw() - this.initialCamera.getYaw()) % twopi) + fivepi) % twopi) - onepi;
			updateViewPlatformTransform(transform,
					this.initialCamera.getX() + (this.finalCamera.getX() - this.initialCamera.getX()) * alpha,
					this.initialCamera.getY() + (this.finalCamera.getY() - this.initialCamera.getY()) * alpha,
					this.initialCamera.getZ() + (this.finalCamera.getZ() - this.initialCamera.getZ()) * alpha,
					this.initialCamera.getYaw() % ((float)Math.PI * 2f) + shortest_angle * alpha,
					this.initialCamera.getPitch() + (this.finalCamera.getPitch() - this.initialCamera.getPitch()) * alpha);
		}
	}

	/**
	 * Updates <code>viewPlatformTransform</code> transform from camera angles and location.
	 */
	private void updateViewPlatformTransform(Transform3D transform, float cameraX, float cameraY, float cameraZ, float cameraYaw,
											 float cameraPitch)
	{
		Transform3D yawRotation = new Transform3D();
		yawRotation.rotY(-cameraYaw + Math.PI);

		Transform3D pitchRotation = new Transform3D();
		pitchRotation.rotX(-cameraPitch);
		yawRotation.mul(pitchRotation);

		transform.setIdentity();
		transform.setTranslation(new Vector3f(cameraX, cameraZ, cameraY));
		transform.mul(yawRotation);

		this.camera = new Camera(cameraX, cameraY, cameraZ, cameraYaw, cameraPitch, 0);
	}

	/**
	 * Adds AWT mouse listeners to <code>component3D</code> that calls back <code>controller</code> methods.
	 */
	private void addMouseListeners(final HomeController3D controller, final Canvas3D component3D)
	{
		// This has been moved to the GLWindow display method as the HomeComponent3D doesn't get a recreate
		// call on a resume, but needs these listeners re-added
		//this.getView().setOnTouchListener(new TouchyListener());

		//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/c11efe44?duration=2592000000&appVersions=192
		if(getContext() != null)
		{
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable()
			{
				public void run()
				{
					// possibly if an exit is called and a loadHome is still happening we can get here just as the universe collapses
					if(getContext() != null)
					{
						mScaleDetector = new ScaleGestureDetector(HomeComponent3D.this.getContext(), new ScaleListener());
					}
				}
			});
		}
		if(getView() != null)
		{
			longHoldHandler = new LongHoldHandler(getView().getResources().getDisplayMetrics());
		}
	}



	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		/**
		 * @param detector
		 * @return
		 */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector)
		{
			if(getView() != null)
			{
				DisplayMetrics mDisplayMetrics = getView().getResources().getDisplayMetrics();
				float mDPI = (float) mDisplayMetrics.densityDpi;
				float measurement = mScaleDetector.getCurrentSpan() / mDPI;
				return measurement > PlanComponent.dpiMinSpanForZoom;
			}
			return false;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			float yd = (mScaleDetector.getCurrentSpan() - mScaleDetector.getPreviousSpan());
			controller.moveCamera(yd);
			return true;
		}
	}
	private class LongHoldHandler implements android.view.View.OnTouchListener
	{
		private int JITTER_DP = 10;
		private int maxJitter = 10;
		private Handler handler = new Handler();
		public boolean pushingDown = false;
		private float xFirstMouseDown = -1;
		private float yFirstMouseDown = -1;

		public LongHoldHandler(DisplayMetrics mDisplayMetrics)
		{
			final float scale = mDisplayMetrics.density;
			maxJitter = (int) (JITTER_DP * scale + 0.5f);
		}
		private Runnable repeater = new Runnable() {
			@Override
			public void run() {
				// make sure the long hold is still running
				if (pushingDown) {
					controller.moveCamera(10);
					handler.postDelayed(this, 100);
				}
			}
		};
		@Override
		public boolean onTouch(android.view.View v, MotionEvent ev)
		{
			final int action = MotionEventCompat.getActionMasked(ev);

			switch (action & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
				{
					if (ev.getPointerCount() == 1)
					{
						if(!pushingDown)
						{
							pushingDown = true;
							xFirstMouseDown = ev.getX();
							yFirstMouseDown = ev.getY();
							handler.postDelayed(repeater, 750);// real long to get out of single and double tap times

							// don't consume the event as taps etc need it as well.
							return false;
						}
					}
				}
				case MotionEvent.ACTION_MOVE:
				{
					// we want to consume jitters as we are running
					if (ev.getPointerCount() == 1)
					{
						if(pushingDown)
						{
							if( Math.abs(xFirstMouseDown - ev.getX()) < maxJitter
								&& Math.abs(yFirstMouseDown - ev.getY()) < maxJitter)
							return true;

							// other wise fall through to stop teh long hold repeats
						}
					}
				}
			}
			pushingDown = false;
			return false;
		}
	}
	private class TouchyListener implements android.view.View.OnTouchListener
	{
		private static final int INVALID_POINTER_ID = -1;

		// The ‘active pointer’ is the one currently moving our object.
		private int mActivePointerId = INVALID_POINTER_ID;
		private float xLastMouseMove1 = -1;
		private float yLastMouseMove1 = -1;

		private float xLastMouseMove2 = -1;
		private float yLastMouseMove2 = -1;

		@Override
		public boolean onTouch(android.view.View v, MotionEvent ev)
		{
			// Let the ScaleGestureDetector inspect all events, it will do move camera if it likes
			if(mScaleDetector != null )
			{
				mScaleDetector.onTouchEvent(ev);
				if (mScaleDetector.isInProgress())
					return true;
			}


			//allow the long down hold to inspect it, it might consume jitters
			if(longHoldHandler!=null && longHoldHandler.onTouch(v,  ev))
				return true;

			// let the selection handler have a go first, if it's working then stop there
			// note it has a tiny waver factor, it returns false on any down ever though it will use it later
			if(homeComponent3DMouseHandler != null && homeComponent3DMouseHandler.onTouch(v,  ev))
				return true;

			// for long hold I will only get a mouse down and nothing after it, so on a mouse donw (before even teh tapper)
			// I need to start a timer waiting for a mouse up, and after waiting for 600ms say
			// it should just start firing off move events

			// otherwise a single finger up/down left right are pitch/pan
			// a single finger long hold is forward
			// a 2+ finger drag is forward/backward straf left/right

			dragging = false;
			extraInfo = " pc = "+ev.getPointerCount();
			final int action = MotionEventCompat.getActionMasked(ev);

			switch (action & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
				{
					if( ev.getPointerCount() == 1 )
					{
						fingerCount = 1;
						this.xLastMouseMove1 = ev.getX();
						this.yLastMouseMove1 = ev.getY();

						this.xLastMouseMove2 = -1;
						this.yLastMouseMove2 = -1;
					}
					else if( ev.getPointerCount() > 1 )
					{
						fingerCount = ev.getPointerCount();
						this.xLastMouseMove2 = -1;
						this.yLastMouseMove2 = -1;
					}

					break;
				}

				case MotionEvent.ACTION_POINTER_DOWN:
				{
					fingerCount = 2;
					dragging = false;
					break;
				}

				case MotionEvent.ACTION_MOVE:
				{
					dragging = true;
					if (ev.getPointerCount() == 1)
					{
						fingerCount = 1;
						if(this.xLastMouseMove1 != -1 && this.yLastMouseMove1 != -1)
						{
							final float PITCH_REDUCTION = 0.4f; // pitch is across 180 only, and is less "wanted"
							final float ANGLE_FACTOR = 0.0025f;
							// Mouse move along X axis changes camera yaw
							float yawDelta = ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove1);

							// inside is made into a slower drag
							float factor = home.getCamera() == home.getObserverCamera() ? -0.5f : 1f;

							controller.rotateCameraYaw(factor * yawDelta);

							// Mouse move along Y axis changes camera pitch
							float pitchDelta = ANGLE_FACTOR * (ev.getY() - this.yLastMouseMove1) * PITCH_REDUCTION;
							controller.rotateCameraPitch(factor * pitchDelta);
						}
						this.xLastMouseMove1 = ev.getX();
						this.yLastMouseMove1 = ev.getY();
					}
					else if (ev.getPointerCount() > 1)
					{
						fingerCount = ev.getPointerCount();

						extraInfo = " x " +this.xLastMouseMove2 + " y " + this.yLastMouseMove2;

						if(this.xLastMouseMove2 != -1 && this.yLastMouseMove2 != -1)
						{
							final float STRAF_REDUCTION = 0.5f; // stafing is less "wanted"
							final float FACTOR = 0.5f;
							float xd = FACTOR * (ev.getX() - this.xLastMouseMove2);
							float yd = FACTOR * (ev.getY() - this.yLastMouseMove2);
							extraInfo += " yd "+yd;
							controller.moveCamera(yd);
							controller.moveCameraSideways(-xd*STRAF_REDUCTION);//note does nothing in overhead view
						}

						this.xLastMouseMove2 = ev.getX();
						this.yLastMouseMove2 = ev.getY();


					}
					break;
				}

				case MotionEvent.ACTION_UP:
				{
					fingerCount = 0;
					dragging = false;
					mActivePointerId = INVALID_POINTER_ID;
					// make sure this isn't the exit of a double touch too
					//if (ev.getPointerCount() == 1 && !mScaleDetector.isInProgress() && fingers == 1)
					{
						this.xLastMouseMove1 = -1;
						this.yLastMouseMove1 = -1;

						this.xLastMouseMove2 = -1;
						this.yLastMouseMove2 = -1;
					}
					break;
				}

				case MotionEvent.ACTION_CANCEL:
				{
					fingerCount = 0;
					dragging = false;

					mActivePointerId = INVALID_POINTER_ID;

					this.xLastMouseMove1 = -1;
					this.yLastMouseMove1 = -1;

					this.xLastMouseMove2 = -1;
					this.yLastMouseMove2 = -1;
					break;
				}

				case MotionEvent.ACTION_POINTER_UP:
				{
					dragging = false;

					//second finger has been released
					final int pointerIndex = MotionEventCompat.getActionIndex(ev);
					final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

					if (pointerId == mActivePointerId)
					{
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						this.xLastMouseMove2 = -1;
						this.yLastMouseMove2 = -1;
						mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
					}

					// reduce the jitter when leaving 2 finger mode
					this.xLastMouseMove1 = -1;
					this.yLastMouseMove1 = -1;

					fingerCount = ev.getPointerCount() - 1;
					break;
				}
			}

			return true;
		}
	}


	/**
	 * Installs keys bound to actions.
	 */
	private void installKeyboardActions()
	{
		/*InputMap inputMap = getInputMap(WHEN_FOCUSED);
		// Tolerate alt modifier for forward and backward moves with UP and DOWN keys to avoid
		// the user to release the alt key when he wants to alternate forward/backward and sideways moves
		inputMap.put(KeyStroke.getKeyStroke("shift UP"), ActionType.MOVE_CAMERA_FAST_FORWARD);
		inputMap.put(KeyStroke.getKeyStroke("shift alt UP"), ActionType.MOVE_CAMERA_FAST_FORWARD);
		inputMap.put(KeyStroke.getKeyStroke("shift W"), ActionType.MOVE_CAMERA_FAST_FORWARD);
		inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.MOVE_CAMERA_FORWARD);
		inputMap.put(KeyStroke.getKeyStroke("alt UP"), ActionType.MOVE_CAMERA_FORWARD);
		inputMap.put(KeyStroke.getKeyStroke("W"), ActionType.MOVE_CAMERA_FORWARD);
		inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
		inputMap.put(KeyStroke.getKeyStroke("shift alt DOWN"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
		inputMap.put(KeyStroke.getKeyStroke("shift S"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
		inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.MOVE_CAMERA_BACKWARD);
		inputMap.put(KeyStroke.getKeyStroke("alt DOWN"), ActionType.MOVE_CAMERA_BACKWARD);
		inputMap.put(KeyStroke.getKeyStroke("S"), ActionType.MOVE_CAMERA_BACKWARD);
		inputMap.put(KeyStroke.getKeyStroke("shift alt LEFT"), ActionType.MOVE_CAMERA_FAST_LEFT);
		inputMap.put(KeyStroke.getKeyStroke("alt LEFT"), ActionType.MOVE_CAMERA_LEFT);
		inputMap.put(KeyStroke.getKeyStroke("shift alt RIGHT"), ActionType.MOVE_CAMERA_FAST_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke("alt RIGHT"), ActionType.MOVE_CAMERA_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), ActionType.ROTATE_CAMERA_YAW_FAST_LEFT);
		inputMap.put(KeyStroke.getKeyStroke("shift A"), ActionType.ROTATE_CAMERA_YAW_FAST_LEFT);
		inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.ROTATE_CAMERA_YAW_LEFT);
		inputMap.put(KeyStroke.getKeyStroke("A"), ActionType.ROTATE_CAMERA_YAW_LEFT);
		inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke("shift D"), ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.ROTATE_CAMERA_YAW_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke("D"), ActionType.ROTATE_CAMERA_YAW_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke("shift PAGE_UP"), ActionType.ROTATE_CAMERA_PITCH_FAST_UP);
		inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), ActionType.ROTATE_CAMERA_PITCH_UP);
		inputMap.put(KeyStroke.getKeyStroke("shift PAGE_DOWN"), ActionType.ROTATE_CAMERA_PITCH_FAST_DOWN);
		inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), ActionType.ROTATE_CAMERA_PITCH_DOWN);
		inputMap.put(KeyStroke.getKeyStroke("shift HOME"), ActionType.ELEVATE_CAMERA_FAST_UP);
		inputMap.put(KeyStroke.getKeyStroke("HOME"), ActionType.ELEVATE_CAMERA_UP);
		inputMap.put(KeyStroke.getKeyStroke("shift END"), ActionType.ELEVATE_CAMERA_FAST_DOWN);
		inputMap.put(KeyStroke.getKeyStroke("END"), ActionType.ELEVATE_CAMERA_DOWN);*/
	}

	/**
	 * Creates actions that calls back <code>controller</code> methods.
	 */
	private void createActions(final HomeController3D controller)
	{
	}

	/**
	 * Returns a new scene tree root.
	 */
	private BranchGroup createSceneTree(boolean displayShadowOnFloor, boolean listenToHomeUpdates, boolean waitForLoading)
	{
		BranchGroup root = new BranchGroup();
		root.setName("Universe Root");
		root.setPickable(true);

		// Build scene tree
		root.addChild(createHomeTree(displayShadowOnFloor, listenToHomeUpdates, waitForLoading));
		root.addChild(createBackgroundNode(listenToHomeUpdates, waitForLoading));
		//PJPJPJ a 100km seems big enough and reduces rounding issues on small devices
		Node groundNode = createGroundNode(-0.5E5f, -0.5E5f, 1E5f, 1E5f, listenToHomeUpdates, waitForLoading);
		root.addChild(groundNode);

		this.defaultLights = createLights(groundNode, listenToHomeUpdates);
		for (Light light : this.defaultLights)
		{
			root.addChild(light);
		}
		//PJPJPJ called compile manually
		//root.outputTraversal();
		root.compile();
		return root;
	}

	/**
	 * Returns a new background node.
	 */
	private Node createBackgroundNode(boolean listenToHomeUpdates, final boolean waitForLoading)
	{
		final SimpleShaderAppearance backgroundAppearance = new SimpleShaderAppearance();
		ColoringAttributes backgroundColoringAttributes = new ColoringAttributes();
		backgroundAppearance.setColoringAttributes(backgroundColoringAttributes);
		// Allow background color and texture to change
		backgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		backgroundAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		backgroundColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);

		//PJPJPJPJ allow updatable shader building
		backgroundAppearance.setUpdatableCapabilities();

		Geometry halfSphereGeometry = createHalfSphereGeometry(true);
		final Shape3D halfSphere = new Shape3D(halfSphereGeometry, backgroundAppearance);
		BranchGroup backgroundBranch = new BranchGroup();
		backgroundBranch.addChild(halfSphere);
		//PJPJP what the hell was this no appearance shape doing exactly?
		//backgroundBranch.addChild(new Shape3D(createHalfSphereGeometry(false)));

		final Background background = new Background(backgroundBranch);
		updateBackgroundColorAndTexture(backgroundAppearance, this.home, waitForLoading);
		background.setImageScaleMode(Background.SCALE_FIT_ALL);
		//PJPJ used an isInfinite version
		background.setApplicationBounds(new BoundingSphere(new Point3d(0,0,0), Double.POSITIVE_INFINITY));

		if (listenToHomeUpdates)
		{
			// Add a listener on sky color and texture properties change
			this.skyColorListener = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent ev)
				{
					updateBackgroundColorAndTexture(backgroundAppearance, home, waitForLoading);
				}
			};
			this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.SKY_COLOR, this.skyColorListener);
			this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.SKY_TEXTURE, this.skyColorListener);
		}
		return background;
	}

	/**
	 * Returns a half sphere oriented inward and with texture ordinates
	 * that spread along an hemisphere.
	 */
	private Geometry createHalfSphereGeometry(boolean top)
	{
		final int divisionCount = 48;
		Point3f[] coords = new Point3f[divisionCount * divisionCount];
		TexCoord2f[] textureCoords = top ? new TexCoord2f[divisionCount * divisionCount] : null;
		Color3f[] colors = top ? null : new Color3f[divisionCount * divisionCount];
		for (int i = 0, k = 0; i < divisionCount; i++)
		{
			double alpha = i * 2 * Math.PI / divisionCount;
			float cosAlpha = (float) Math.cos(alpha);
			float sinAlpha = (float) Math.sin(alpha);
			double nextAlpha = (i + 1) * 2 * Math.PI / divisionCount;
			float cosNextAlpha = (float) Math.cos(nextAlpha);
			float sinNextAlpha = (float) Math.sin(nextAlpha);
			for (int j = 0; j < divisionCount / 4; j++)
			{
				double beta = 2 * j * Math.PI / divisionCount;
				float cosBeta = (float) Math.cos(beta);
				float sinBeta = (float) Math.sin(beta);
				// Correct the bottom of the hemisphere to avoid seeing a bottom hemisphere at the horizon
				float y = j != 0 ? (top ? sinBeta : -sinBeta) : -0.01f;
				double nextBeta = 2 * (j + 1) * Math.PI / divisionCount;
				if (!top)
				{
					nextBeta = -nextBeta;
				}
				float cosNextBeta = (float) Math.cos(nextBeta);
				float sinNextBeta = (float) Math.sin(nextBeta);
				if (top)
				{
					coords[k] = new Point3f(cosAlpha * cosBeta, y, sinAlpha * cosBeta);
					textureCoords[k++] = new TexCoord2f((float) i / divisionCount, sinBeta);

					coords[k] = new Point3f(cosNextAlpha * cosBeta, y, sinNextAlpha * cosBeta);
					textureCoords[k++] = new TexCoord2f((float) (i + 1) / divisionCount, sinBeta);

					coords[k] = new Point3f(cosNextAlpha * cosNextBeta, sinNextBeta, sinNextAlpha * cosNextBeta);
					textureCoords[k++] = new TexCoord2f((float) (i + 1) / divisionCount, sinNextBeta);

					coords[k] = new Point3f(cosAlpha * cosNextBeta, sinNextBeta, sinAlpha * cosNextBeta);
					textureCoords[k++] = new TexCoord2f((float) i / divisionCount, sinNextBeta);
				}
				else
				{
					coords[k] = new Point3f(cosAlpha * cosBeta, y, sinAlpha * cosBeta);
					float color1 = .9f + y * .5f;
					colors[k++] = new Color3f(color1, color1, color1);

					coords[k] = new Point3f(cosAlpha * cosNextBeta, sinNextBeta, sinAlpha * cosNextBeta);
					float color2 = .9f + sinNextBeta * .5f;
					colors[k++] = new Color3f(color2, color2, color2);

					coords[k] = new Point3f(cosNextAlpha * cosNextBeta, sinNextBeta, sinNextAlpha * cosNextBeta);
					colors[k++] = new Color3f(color2, color2, color2);

					coords[k] = new Point3f(cosNextAlpha * cosBeta, y, sinNextAlpha * cosBeta);
					colors[k++] = new Color3f(color1, color1, color1);
				}
			}
		}

		GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
		geometryInfo.setCoordinates(coords);
		if (textureCoords != null)
		{
			geometryInfo.setTextureCoordinateParams(1, 2);
			geometryInfo.setTextureCoordinates(0, textureCoords);
		}
		if (colors != null)
		{
			geometryInfo.setColors(colors);
		}

		//PJPJPJPJ
		geometryInfo.convertToIndexedTriangles();

		//geometryInfo.indexify();
		//geometryInfo.compact();
		//new Stripifier().stripify(geometryInfo);
		Geometry halfSphereGeometry = geometryInfo.getIndexedGeometryArray(true,true,true,true,true);
		halfSphereGeometry.setName("Sky");
		return halfSphereGeometry;
	}

	/**
	 * Updates <code>backgroundAppearance</code> color and texture from <code>home</code> sky color and texture.
	 */
	private void updateBackgroundColorAndTexture(final Appearance backgroundAppearance, Home home, boolean waitForLoading)
	{
		Color c = new Color(home.getEnvironment().getSkyColor());
		Color3f skyColor = new Color3f(c.getRed() /255f, c.getGreen() / 255f, c.getBlue() /  255f);
		backgroundAppearance.getColoringAttributes().setColor(skyColor);
		HomeTexture skyTexture = home.getEnvironment().getSkyTexture();
		if (skyTexture != null)
		{
			TextureManager textureManager = TextureManager.getInstance();
			if (waitForLoading)
			{
				// Don't share the background texture otherwise if might not be rendered correctly
				backgroundAppearance.setTexture(textureManager.loadTexture(skyTexture.getImage()));
			}
			else
			{
				textureManager.loadTexture(skyTexture.getImage(), waitForLoading, new TextureManager.TextureObserver()
				{
					public void textureUpdated(Texture texture)
					{
						// Use a copy of the texture in case it's used in an other universe
						backgroundAppearance.setTexture((Texture) texture.cloneNodeComponent(false));
					}
				});
			}
		}
		else
		{
			backgroundAppearance.setTexture(null);
		}

		clearPrintedImageCache();
	}

	/**
	 * Returns a new ground node.
	 */
	private Node createGroundNode(final float groundOriginX, final float groundOriginY, final float groundWidth, final float groundDepth,
								  boolean listenToHomeUpdates, boolean waitForLoading)
	{
		final Ground3D ground3D = new Ground3D(this.home, groundOriginX, groundOriginY, groundWidth, groundDepth, waitForLoading);
		Transform3D translation = new Transform3D();
		translation.setTranslation(new Vector3f(0, -0.2f, 0));
		TransformGroup transformGroup = new TransformGroup(translation);
		transformGroup.addChild(ground3D);

		if (listenToHomeUpdates)
		{
			// Add a listener on ground color and texture properties change
			this.groundChangeListener = new PropertyChangeListener()
			{
				private Runnable updater;

				public void propertyChange(PropertyChangeEvent ev)
				{
					if (this.updater == null)
					{
						// Group updates
						EventQueue.invokeLater(this.updater = new Runnable()
						{
							public void run()
							{
								ground3D.update();
								updater = null;
							}
						});
					}
					clearPrintedImageCache();
				}
			};
			HomeEnvironment homeEnvironment = this.home.getEnvironment();
			homeEnvironment.addPropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.groundChangeListener);
			homeEnvironment.addPropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.groundChangeListener);
		}

		return transformGroup;
	}

	/**
	 * Returns the lights of the scene.
	 */
	private Light[] createLights(final Node groundNode, boolean listenToHomeUpdates)
	{
		final Light[] lights = {new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(1.5f, -0.8f, -1)),
				new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(-1.5f, -0.8f, -1)),
				new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(0, -0.8f, 1)),
				new DirectionalLight(new Color3f(0.7f, 0.7f, 0.7f), new Vector3f(0, 1f, 0)),
				new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
		for (int i = 0; i < lights.length - 1; i++)
		{
			// Allow directional lights color and influencing bounds to change
			lights[i].setCapability(DirectionalLight.ALLOW_COLOR_WRITE);
			lights[i].setCapability(DirectionalLight.ALLOW_SCOPE_WRITE);
			// Store default color in user data
			Color3f defaultColor = new Color3f();
			lights[i].getColor(defaultColor);
			lights[i].setUserData(defaultColor);
			updateLightColor(lights[i]);
		}

		final Bounds defaultInfluencingBounds = new BoundingSphere(new Point3d(), 1E7);
		for (Light light : lights)
		{
			light.setInfluencingBounds(defaultInfluencingBounds);
		}

		if (listenToHomeUpdates)
		{
			// Add a listener on light color property change to home
			this.lightColorListener = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent ev)
				{
					for (int i = 0; i < lights.length - 1; i++)
					{
						updateLightColor(lights[i]);
					}
					updateObjects(getHomeObjects(HomeLight.class));
				}
			};
			this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);

			// Add a listener on subpart size property change to home
			this.subpartSizeListener = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent ev)
				{
					if (ev != null)
					{
						// Update 3D objects if not at initialization
						Collection<Selectable> homeItems = new ArrayList<Selectable>(home.getWalls());
						homeItems.addAll(home.getRooms());
						homeItems.addAll(getHomeObjects(HomeLight.class));
						updateObjects(homeItems);
						clearPrintedImageCache();
					}

					// Update default lights scope
					List<Group> scope = null;
					if (home.getEnvironment().getSubpartSizeUnderLight() > 0)
					{
						Area lightScopeOutsideWallsArea = getLightScopeOutsideWallsArea();
						scope = new ArrayList<Group>();
						for (Wall wall : home.getWalls())
						{
							Object3DBranch wall3D = homeObjects.get(wall);
							if (wall3D instanceof Wall3D)
							{
								// Add left and/or right side of the wall to scope
								float[][] points = wall.getPoints();
								if (!lightScopeOutsideWallsArea.contains(points[0][0], points[0][1]))
								{
									scope.add((Group) wall3D.getChild(1));
								}
								if (!lightScopeOutsideWallsArea.contains(points[points.length - 1][0], points[points.length - 1][1]))
								{
									scope.add((Group) wall3D.getChild(4));
								}
							}
							// Add wall top and bottom groups to scope
							scope.add((Group) wall3D.getChild(0));
							scope.add((Group) wall3D.getChild(2));
							scope.add((Group) wall3D.getChild(3));
							scope.add((Group) wall3D.getChild(5));
						}
						List<Selectable> otherItems = new ArrayList<Selectable>(home.getRooms());
						otherItems.addAll(getHomeObjects(HomePieceOfFurniture.class));
						for (Selectable item : otherItems)
						{
							// Add item to scope if one of its points don't belong to lightScopeWallsArea
							for (float[] point : item.getPoints())
							{
								if (!lightScopeOutsideWallsArea.contains(point[0], point[1]))
								{
									Group object3D = homeObjects.get(item);
									if (object3D instanceof HomePieceOfFurniture3D)
									{
										// Add the direct parent of the shape that will be added once loaded
										// otherwise scope won't be updated automatically
										object3D = (Group) object3D.getChild(0);
									}
									scope.add(object3D);
									break;
								}
							}
						}
					}
					else
					{
						lightScopeOutsideWallsAreaCache = null;
					}

					for (Light light : lights)
					{
						if (light instanceof DirectionalLight)
						{
							light.removeAllScopes();
							if (scope != null)
							{
								light.addScope((Group) groundNode);
								for (Group group : scope)
								{
									light.addScope(group);
								}
							}
						}
					}
				}
			};
			this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.SUBPART_SIZE_UNDER_LIGHT,
					this.subpartSizeListener);
			this.subpartSizeListener.propertyChange(null);
		}

		return lights;
	}

	/**
	 * Returns the home objects displayed by this component of the given class.
	 */
	private <T> List<T> getHomeObjects(Class<T> objectClass)
	{
		return Home.getSubList(new ArrayList<Selectable>(homeObjects.keySet()), objectClass);
	}

	/**
	 * Updates<code>light</code> color from <code>home</code> light color.
	 */
	private void updateLightColor(Light light)
	{
		Color3f defaultColor = (Color3f) light.getUserData();
		int lightColor = this.home.getEnvironment().getLightColor();
		light.setColor(new Color3f(((lightColor >>> 16) & 0xFF) / 255f * defaultColor.x,
				((lightColor >>> 8) & 0xFF) / 255f * defaultColor.y, (lightColor & 0xFF) / 255f * defaultColor.z));
		clearPrintedImageCache();
	}

	/**
	 * Returns walls area used for light scope outside.
	 */
	private Area getLightScopeOutsideWallsArea()
	{
		if (this.lightScopeOutsideWallsAreaCache == null)
		{
			// Compute a smaller area surrounding all walls at all levels
			Area wallsPath = new Area();
			for (Wall wall : home.getWalls())
			{
				Wall thinnerWall = wall.clone();
				thinnerWall.setThickness(Math.max(thinnerWall.getThickness() - 0.1f, 0.08f));
				wallsPath.add(new Area(getShape(thinnerWall.getPoints())));
			}
			Area lightScopeOutsideWallsArea = new Area();
			List<float[]> points = new ArrayList<float[]>();
			for (PathIterator it = wallsPath.getPathIterator(null, 1); !it.isDone(); it.next())
			{
				float[] point = new float[2];
				switch (it.currentSegment(point))
				{
					case PathIterator.SEG_MOVETO:
					case PathIterator.SEG_LINETO:
						points.add(point);
						break;
					case PathIterator.SEG_CLOSE:
						if (points.size() > 2)
						{
							float[][] pointsArray = points.toArray(new float[points.size()][]);
							if (new Room(pointsArray).isClockwise())
							{
								lightScopeOutsideWallsArea.add(new Area(getShape(pointsArray)));
							}
						}
						points.clear();
						break;
				}
			}
			this.lightScopeOutsideWallsAreaCache = lightScopeOutsideWallsArea;
		}
		return this.lightScopeOutsideWallsAreaCache;
	}

	/**
	 * Returns a <code>home</code> new tree node, with branches for each wall
	 * and piece of furniture of <code>home</code>.
	 */
	private Node createHomeTree(boolean displayShadowOnFloor, boolean listenToHomeUpdates, boolean waitForLoading)
	{
		Group homeRoot = createHomeRoot();
		// Add walls, pieces, rooms and labels already available
		for (Label label : this.home.getLabels())
		{
			addObject(homeRoot, label, listenToHomeUpdates, waitForLoading);
		}
		for (Room room : this.home.getRooms())
		{
			addObject(homeRoot, room, listenToHomeUpdates, waitForLoading);
		}
		for (Wall wall : this.home.getWalls())
		{
			addObject(homeRoot, wall, listenToHomeUpdates, waitForLoading);
		}
		Map<HomePieceOfFurniture, Node> pieces3D = new HashMap<HomePieceOfFurniture, Node>();
		for (HomePieceOfFurniture piece : this.home.getFurniture())
		{
			if (piece instanceof HomeFurnitureGroup)
			{
				for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup) piece).getAllFurniture())
				{
					if (!(childPiece instanceof HomeFurnitureGroup))
					{
						pieces3D.put(childPiece, addObject(homeRoot, childPiece, listenToHomeUpdates, waitForLoading));
					}
				}
			}
			else
			{
				pieces3D.put(piece, addObject(homeRoot, piece, listenToHomeUpdates, waitForLoading));
			}
		}

		if (displayShadowOnFloor)
		{
			addShadowOnFloor(homeRoot, pieces3D);
		}

		if (listenToHomeUpdates)
		{
			// Add level, wall, furniture, room listeners to home for further update
			addLevelListener(homeRoot);
			addWallListener(homeRoot);
			addFurnitureListener(homeRoot);
			addRoomListener(homeRoot);
			addLabelListener(homeRoot);
			// Add environment listeners
			addEnvironmentListeners();
			// Should update shadow on floor too but in the facts
			// User Interface doesn't propose to modify the furniture of a home
			// that displays shadow on floor yet
		}
		return homeRoot;
	}

	/**
	 * Returns a new group at home subtree root.
	 */
	private Group createHomeRoot()
	{
		Group homeGroup = new Group();
		homeGroup.setName("Home Root");
		//  Allow group to have new children
		homeGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		homeGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		homeGroup.setPickable(true);
		return homeGroup;
	}

	/**
	 * Adds a level listener to home levels that updates the children of the given
	 * <code>group</code>, each time a level is added, updated or deleted.
	 */
	private void addLevelListener(final Group group)
	{
		this.levelChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())
						|| Level.Property.VISIBLE.name().equals(ev.getPropertyName())
						|| Level.Property.VIEWABLE.name().equals(ev.getPropertyName()))
				{
					updateObjects(homeObjects.keySet());
					groundChangeListener.propertyChange(null);
				}
				else if (Level.Property.FLOOR_THICKNESS.name().equals(ev.getPropertyName()))
				{
					updateObjects(home.getWalls());
					updateObjects(home.getRooms());
				}
				else if (Level.Property.HEIGHT.name().equals(ev.getPropertyName()))
				{
					updateObjects(home.getRooms());
				}
			}
		};
		for (Level level : this.home.getLevels())
		{
			level.addPropertyChangeListener(this.levelChangeListener);
		}
		this.levelListener = new CollectionListener<Level>()
		{
			public void collectionChanged(CollectionEvent<Level> ev)
			{
				Level level = ev.getItem();
				switch (ev.getType())
				{
					case ADD:
						level.addPropertyChangeListener(levelChangeListener);
						break;
					case DELETE:
						level.removePropertyChangeListener(levelChangeListener);
						break;
				}
				updateObjects(home.getRooms());
			}
		};
		this.home.addLevelsListener(this.levelListener);
	}


	/**
	 * Adds a wall listener to home walls that updates the children of the given
	 * <code>group</code>, each time a wall is added, updated or deleted.
	 */
	private void addWallListener(final Group group)
	{

		//Wait no, I just need a single
		//updateObjects(home.getRooms());
		// and
		//updateObjects(home.getWalls());
		// fired on 3d made visible!
		this.wallChangeListener = new PropertyChangeListener()
		{
			private ArrayList<Wall> wallsNeedingUpdate = new ArrayList<Wall>();
			public void propertyChange(PropertyChangeEvent ev)
			{
				String propertyName = ev.getPropertyName();
				if( RUN_UPDATES.equals(propertyName))
				{
					for( Wall updatedWall: wallsNeedingUpdate )
					{
						updateWall(updatedWall);
					}
					wallsNeedingUpdate.clear();

					// all uses of this removed and called one time here
					if(fullRoomUpdateRequired)
					{
						updateObjects(home.getRooms());
						fullRoomUpdateRequired = false;

					}
					if(fullWallUpdateRequired)
					{
						updateObjects(home.getWalls());
						fullRoomUpdateRequired = false;
					}
				}
				else if (!Wall.Property.PATTERN.name().equals(propertyName))
				{
					Wall updatedWall = (Wall) ev.getSource();
					//PJ updating walls is crazy expensive, lots of geometry create and normal creates (too many some might say)
					// so if we aren't displaying 3d then defer update until then and just take the last one for each wall
					if(HomeComponent3D.this.getUserVisibleHint())
					{
						updateWall(updatedWall);
					}
					else
					{
						if(!wallsNeedingUpdate.contains(updatedWall))
							wallsNeedingUpdate.add(updatedWall);
					}

					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					if (updatedWall.getLevel() != null && updatedWall.getLevel().getElevation() < 0)
					{
						groundChangeListener.propertyChange(null);
					}
					if (home.getEnvironment().getSubpartSizeUnderLight() > 0)
					{
						if (Wall.Property.X_START.name().equals(propertyName) || Wall.Property.Y_START.name().equals(propertyName)
								|| Wall.Property.X_END.name().equals(propertyName) || Wall.Property.Y_END.name().equals(propertyName)
								|| Wall.Property.ARC_EXTENT.name().equals(propertyName)
								|| Wall.Property.THICKNESS.name().equals(propertyName))
						{
							lightScopeOutsideWallsAreaCache = null;
							updateObjectsLightScope(null);
						}
					}
				}

			}
		};
		for (Wall wall : this.home.getWalls())
		{
			wall.addPropertyChangeListener(this.wallChangeListener);
		}
		this.wallListener = new CollectionListener<Wall>()
		{
			public void collectionChanged(CollectionEvent<Wall> ev)
			{
				Wall wall = ev.getItem();
				switch (ev.getType())
				{
					case ADD:
						addObject(group, wall, true, false);
						wall.addPropertyChangeListener(wallChangeListener);
						break;
					case DELETE:
						deleteObject(wall);
						wall.removePropertyChangeListener(wallChangeListener);
						break;
				}
				lightScopeOutsideWallsAreaCache = null;
				// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
				fullRoomUpdateRequired = true;
				groundChangeListener.propertyChange(null);
				updateObjectsLightScope(null);
			}
		};
		this.home.addWallsListener(this.wallListener);
	}

	/**
	 * Adds a furniture listener to home that updates the children of the given <code>group</code>,
	 * each time a piece of furniture is added, updated or deleted.
	 */
	private void addFurnitureListener(final Group group)
	{
		this.furnitureChangeListener = new PropertyChangeListener()
		{
			private ArrayList<HomePieceOfFurniture> furnitureNeedingUpdate = new ArrayList<HomePieceOfFurniture>();
			public void propertyChange(PropertyChangeEvent ev)
			{
				HomePieceOfFurniture updatedPiece = (HomePieceOfFurniture) ev.getSource();
				String propertyName = ev.getPropertyName();
				if (HomePieceOfFurniture.Property.X.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.Y.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.ANGLE.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.WIDTH.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.DEPTH.name().equals(propertyName))
				{
					updatePieceOfFurnitureGeometry(updatedPiece);
					updateObjectsLightScope(Arrays.asList(new HomePieceOfFurniture[]{updatedPiece}));
				}
				else if (HomePieceOfFurniture.Property.HEIGHT.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.ELEVATION.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.MODEL_MIRRORED.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.VISIBLE.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.LEVEL.name().equals(propertyName))
				{
					updatePieceOfFurnitureGeometry(updatedPiece);
				}
				else if (HomePieceOfFurniture.Property.COLOR.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.TEXTURE.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.MODEL_MATERIALS.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.SHININESS.name().equals(propertyName)
						|| (HomeLight.Property.POWER.name().equals(propertyName) && home.getEnvironment().getSubpartSizeUnderLight() > 0))
				{
					updateObjects(Arrays.asList(new HomePieceOfFurniture[]{updatedPiece}));
				}
			}

			private void updatePieceOfFurnitureGeometry(HomePieceOfFurniture piece)
			{
				updateObjects(Arrays.asList(new HomePieceOfFurniture[]{piece}));
				// If piece is or contains a door or a window, update walls that intersect with piece
				if (containsDoorsAndWindows(piece))
				{
					// deferred to visible see RUN_UPDATES updateObjects(home.getWalls());
					fullWallUpdateRequired = true;
				}
				else if (containsStaircases(piece))
				{
					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					fullRoomUpdateRequired = true;
				}
				if (piece.getLevel() != null && piece.getLevel().getElevation() < 0)
				{
					groundChangeListener.propertyChange(null);
				}
			}
		};
		for (HomePieceOfFurniture piece : this.home.getFurniture())
		{
			if (piece instanceof HomeFurnitureGroup)
			{
				for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup) piece).getAllFurniture())
				{
					childPiece.addPropertyChangeListener(this.furnitureChangeListener);
				}
			}
			else
			{
				piece.addPropertyChangeListener(this.furnitureChangeListener);
			}
		}
		this.furnitureListener = new CollectionListener<HomePieceOfFurniture>()
		{
			public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev)
			{
				HomePieceOfFurniture piece = (HomePieceOfFurniture) ev.getItem();
				switch (ev.getType())
				{
					case ADD:
						if (piece instanceof HomeFurnitureGroup)
						{
							for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup) piece).getAllFurniture())
							{
								if (!(childPiece instanceof HomeFurnitureGroup))
								{
									addObject(group, childPiece, true, false);
									childPiece.addPropertyChangeListener(furnitureChangeListener);
								}
							}
						}
						else
						{
							addObject(group, piece, true, false);
							piece.addPropertyChangeListener(furnitureChangeListener);
						}
						break;
					case DELETE:
						if (piece instanceof HomeFurnitureGroup)
						{
							for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup) piece).getAllFurniture())
							{
								if (!(childPiece instanceof HomeFurnitureGroup))
								{
									deleteObject(childPiece);
									childPiece.removePropertyChangeListener(furnitureChangeListener);
								}
							}
						}
						else
						{
							deleteObject(piece);
							piece.removePropertyChangeListener(furnitureChangeListener);
						}
						break;
				}
				// If piece is or contains a door or a window, update walls that intersect with piece
				if (containsDoorsAndWindows(piece))
				{
					// deferred to visible see RUN_UPDATES updateObjects(home.getWalls());
					fullWallUpdateRequired = true;
				}
				else if (containsStaircases(piece))
				{
					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					fullRoomUpdateRequired = true;
				}
				groundChangeListener.propertyChange(null);
				updateObjectsLightScope(Arrays.asList(new HomePieceOfFurniture[]{piece}));
			}
		};
		this.home.addFurnitureListener(this.furnitureListener);
	}

	/**
	 * Returns <code>true</code> if the given <code>piece</code> is or contains a door or window.
	 */
	private boolean containsDoorsAndWindows(HomePieceOfFurniture piece)
	{
		if (piece instanceof HomeFurnitureGroup)
		{
			for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup) piece).getFurniture())
			{
				if (containsDoorsAndWindows(groupPiece))
				{
					return true;
				}
			}
			return false;
		}
		else
		{
			return piece.isDoorOrWindow();
		}
	}

	/**
	 * Returns <code>true</code> if the given <code>piece</code> is or contains a staircase
	 * with a top cut out shape.
	 */
	private boolean containsStaircases(HomePieceOfFurniture piece)
	{
		if (piece instanceof HomeFurnitureGroup)
		{
			for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup) piece).getFurniture())
			{
				if (containsStaircases(groupPiece))
				{
					return true;
				}
			}
			return false;
		}
		else
		{
			return piece.getStaircaseCutOutShape() != null;
		}
	}

	/**
	 * Adds a room listener to home rooms that updates the children of the given
	 * <code>group</code>, each time a room is added, updated or deleted.
	 */
	private void addRoomListener(final Group group)
	{
		this.roomChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				Room updatedRoom = (Room) ev.getSource();
				String propertyName = ev.getPropertyName();
				if (Room.Property.FLOOR_COLOR.name().equals(propertyName) || Room.Property.FLOOR_TEXTURE.name().equals(propertyName)
						|| Room.Property.FLOOR_SHININESS.name().equals(propertyName)
						|| Room.Property.CEILING_COLOR.name().equals(propertyName)
						|| Room.Property.CEILING_TEXTURE.name().equals(propertyName)
						|| Room.Property.CEILING_SHININESS.name().equals(propertyName))
				{
					updateObjects(Arrays.asList(new Room[]{updatedRoom}));
				}
				else if (Room.Property.FLOOR_VISIBLE.name().equals(propertyName)
						|| Room.Property.CEILING_VISIBLE.name().equals(propertyName) || Room.Property.LEVEL.name().equals(propertyName))
				{
					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					fullRoomUpdateRequired = true;
					groundChangeListener.propertyChange(null);
				}
				else if (Room.Property.POINTS.name().equals(propertyName))
				{
					if (homeObjectsToUpdate != null)
					{
						// Don't try to optimize if more than one room to update
						// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
						fullRoomUpdateRequired = true;
					}
					else
					{
						updateObjects(Arrays.asList(new Room[]{updatedRoom}));
						updateObjects(getHomeObjects(HomeLight.class));
						// Search the rooms that overlap the updated one
						Area oldArea = new Area(getShape((float[][]) ev.getOldValue()));
						Area newArea = new Area(getShape((float[][]) ev.getNewValue()));
						Level updatedRoomLevel = updatedRoom.getLevel();
						for (Room room : home.getRooms())
						{
							Level roomLevel = room.getLevel();
							//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/656a58a5?duration=2592000000
							// added || updatedRoomLevel == null
							if (room != updatedRoom && (roomLevel == null || updatedRoomLevel == null
									|| Math.abs(updatedRoomLevel.getElevation() + updatedRoomLevel.getHeight()
									- (roomLevel.getElevation() + roomLevel.getHeight())) < 1E-5
									|| Math.abs(updatedRoomLevel.getElevation() + updatedRoomLevel.getHeight()
									- (roomLevel.getElevation() - roomLevel.getFloorThickness())) < 1E-5))
							{
								Area roomAreaIntersectionWithOldArea = new Area(getShape(room.getPoints()));
								Area roomAreaIntersectionWithNewArea = new Area(roomAreaIntersectionWithOldArea);
								roomAreaIntersectionWithNewArea.intersect(newArea);
								if (!roomAreaIntersectionWithNewArea.isEmpty())
								{
									updateObjects(Arrays.asList(new Room[]{room}));
								}
								else
								{
									roomAreaIntersectionWithOldArea.intersect(oldArea);
									if (!roomAreaIntersectionWithOldArea.isEmpty())
									{
										updateObjects(Arrays.asList(new Room[]{room}));
									}
								}
							}
						}
					}
					groundChangeListener.propertyChange(null);
					updateObjectsLightScope(Arrays.asList(new Room[]{updatedRoom}));
					updateObjectsLightScope(getHomeObjects(HomeLight.class));
				}
			}
		};
		for (Room room : this.home.getRooms())
		{
			room.addPropertyChangeListener(this.roomChangeListener);
		}
		this.roomListener = new CollectionListener<Room>()
		{
			public void collectionChanged(CollectionEvent<Room> ev)
			{
				Room room = ev.getItem();
				switch (ev.getType())
				{
					case ADD:
						// Add room to its group at the index indicated by the event
						// to ensure the 3D rooms are drawn in the same order as in the plan
						addObject(group, room, ev.getIndex(), true, false);
						room.addPropertyChangeListener(roomChangeListener);
						break;
					case DELETE:
						deleteObject(room);
						room.removePropertyChangeListener(roomChangeListener);
						break;
				}
				// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
				fullRoomUpdateRequired = true;
				groundChangeListener.propertyChange(null);
				updateObjectsLightScope(Arrays.asList(new Room[]{room}));
				updateObjectsLightScope(getHomeObjects(HomeLight.class));
			}
		};
		this.home.addRoomsListener(this.roomListener);
	}

	/**
	 * Returns the path matching points.
	 */
	private GeneralPath getShape(float[][] points)
	{
		GeneralPath path = new GeneralPath();
		path.moveTo(points[0][0], points[0][1]);
		for (int i = 1; i < points.length; i++)
		{
			path.lineTo(points[i][0], points[i][1]);
		}
		path.closePath();
		return path;
	}

	/**
	 * Adds a label listener to home labels that updates the children of the given
	 * <code>group</code>, each time a label is added, updated or deleted.
	 */
	private void addLabelListener(final Group group)
	{
		this.labelChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				Label label = (Label) ev.getSource();
				updateObjects(Arrays.asList(new Label[]{label}));
			}
		};
		for (Label label : this.home.getLabels())
		{
			label.addPropertyChangeListener(this.labelChangeListener);
		}
		this.labelListener = new CollectionListener<Label>()
		{
			public void collectionChanged(CollectionEvent<Label> ev)
			{
				Label label = ev.getItem();
				switch (ev.getType())
				{
					case ADD:
						addObject(group, label, true, false);
						label.addPropertyChangeListener(labelChangeListener);
						break;
					case DELETE:
						deleteObject(label);
						label.removePropertyChangeListener(labelChangeListener);
						break;
				}
			}
		};
		this.home.addLabelsListener(this.labelListener);
	}

	/**
	 * Adds a walls alpha change listener and drawing mode change listener to home
	 * environment that updates the home scene objects appearance.
	 */
	private void addEnvironmentListeners()
	{
		this.wallsAlphaListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				updateObjects(home.getWalls());
				updateObjects(home.getRooms());
			}
		};
		this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
		this.drawingModeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				updateObjects(home.getWalls());
				updateObjects(getHomeObjects(HomePieceOfFurniture.class));
			}
		};
		this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
	}

	/**
	 * Adds to <code>group</code> a branch matching <code>homeObject</code>.
	 */
	private Node addObject(Group group, Selectable homeObject, boolean listenToHomeUpdates, boolean waitForLoading)
	{
		return addObject(group, homeObject, -1, listenToHomeUpdates, waitForLoading);
	}

	/**
	 * Adds to <code>group</code> a branch matching <code>homeObject</code> at a given <code>index</code>.
	 * If <code>index</code> is equal to -1, <code>homeObject</code> will be added at the end of the group.
	 */
	private Node addObject(Group group, Selectable homeObject, int index, boolean listenToHomeUpdates, boolean waitForLoading)
	{
		Object3DBranch object3D = createObject3D(homeObject, waitForLoading);
		if (listenToHomeUpdates)
		{
			this.homeObjects.put(homeObject, object3D);
		}
		if (index == -1)
		{
			group.addChild(object3D);
		}
		else
		{
			group.insertChild(object3D, index);
		}
		clearPrintedImageCache();
		return object3D;
	}

	/**
	 * Returns the 3D object matching the given home object. If <code>waitForLoading</code>
	 * is <code>true</code> the resources used by the returned 3D object should be ready to be displayed.
	 *
	 * @deprecated Subclasses which used to override this method must be updated to create an instance of
	 * a {@link Object3DFactory factory} and give it as parameter to the constructor of this class.
	 */
	private Object3DBranch createObject3D(Selectable homeObject, boolean waitForLoading)
	{
		return (Object3DBranch) this.object3dFactory.createObject3D(this.home, homeObject, waitForLoading);
	}

	/**
	 * Detaches from the scene the branch matching <code>homeObject</code>.
	 */
	private void deleteObject(Selectable homeObject)
	{
		this.homeObjects.get(homeObject).detach();
		this.homeObjects.remove(homeObject);
		clearPrintedImageCache();
	}

	/**
	 * Updates <code>objects</code> later. Should be invoked from Event Dispatch Thread.
	 */
	private void updateObjects(Collection<? extends Selectable> objects)
	{
		if (this.homeObjectsToUpdate != null)
		{
			this.homeObjectsToUpdate.addAll(objects);
		}
		else
		{
			this.homeObjectsToUpdate = new HashSet<Selectable>(objects);
			// Invoke later the update of objects of homeObjectsToUpdate
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					for (Selectable object : homeObjectsToUpdate)
					{
						Object3DBranch objectBranch = homeObjects.get(object);
						// Check object wasn't deleted since updateObjects call
						if (objectBranch != null)
						{
							objectBranch.update();
						}
					}
					homeObjectsToUpdate = null;
				}
			});
		}
		clearPrintedImageCache();
		this.approximateHomeBoundsCache = null;
	}

	/**
	 * Updates <code>wall</code> geometry,
	 * and the walls at its end or start.
	 */
	private void updateWall(Wall wall)
	{
		Collection<Wall> wallsToUpdate = new ArrayList<Wall>(3);
		wallsToUpdate.add(wall);
		if (wall.getWallAtStart() != null)
		{
			wallsToUpdate.add(wall.getWallAtStart());
		}
		if (wall.getWallAtEnd() != null)
		{
			wallsToUpdate.add(wall.getWallAtEnd());
		}
		updateObjects(wallsToUpdate);
	}

	/**
	 * Updates the <code>object</code> scope under light later. Should be invoked from Event Dispatch Thread.
	 */
	private void updateObjectsLightScope(Collection<? extends Selectable> objects)
	{
		if (home.getEnvironment().getSubpartSizeUnderLight() > 0)
		{
			if (this.lightScopeObjectsToUpdate != null)
			{
				if (objects == null)
				{
					this.lightScopeObjectsToUpdate.clear();
					this.lightScopeObjectsToUpdate.add(null);
				}
				else if (!this.lightScopeObjectsToUpdate.contains(null))
				{
					this.lightScopeObjectsToUpdate.addAll(objects);
				}
			}
			else
			{
				this.lightScopeObjectsToUpdate = new HashSet<Selectable>();
				if (objects == null)
				{
					this.lightScopeObjectsToUpdate.add(null);
				}
				else
				{
					this.lightScopeObjectsToUpdate.addAll(objects);
				}
				// Invoke later the update of objects of lightScopeObjectsToUpdate
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						if (lightScopeObjectsToUpdate.contains(null))
						{
							subpartSizeListener.propertyChange(null);
						}
						else if (home.getEnvironment().getSubpartSizeUnderLight() > 0)
						{
							Area lightScopeOutsideWallsArea = getLightScopeOutsideWallsArea();
							for (Selectable object : lightScopeObjectsToUpdate)
							{
								Group object3D = homeObjects.get(object);
								if (object3D instanceof HomePieceOfFurniture3D)
								{
									// Add the direct parent of the shape that will be added once loaded
									// otherwise scope won't be updated automatically
									object3D = (Group) object3D.getChild(0);
								}
								// Check object wasn't deleted since updateObjects call
								if (object3D != null)
								{
									// Add item to scope if one of its points don't belong to lightScopeOutsideWallsArea
									boolean objectInOutsideLightScope = false;
									for (float[] point : object.getPoints())
									{
										if (!lightScopeOutsideWallsArea.contains(point[0], point[1]))
										{
											objectInOutsideLightScope = true;
											break;
										}
									}
									for (Light light : defaultLights)
									{
										if (light instanceof DirectionalLight)
										{
											if (objectInOutsideLightScope && light.indexOfScope(object3D) == -1)
											{
												light.addScope(object3D);
											}
											else if (!objectInOutsideLightScope && light.indexOfScope(object3D) != -1)
											{
												light.removeScope(object3D);
											}
										}
									}
								}
							}
						}
						lightScopeObjectsToUpdate = null;
					}
				});
			}
		}
	}

	/**
	 * Adds to <code>homeRoot</code> shapes matching the shadow of furniture at their level.
	 */
	private void addShadowOnFloor(Group homeRoot, Map<HomePieceOfFurniture, Node> pieces3D)
	{
		Comparator<Level> levelComparator = new Comparator<Level>()
		{
			public int compare(Level level1, Level level2)
			{
				return Float.compare(level1.getElevation(), level2.getElevation());
			}
		};
		Map<Level, Area> areasOnLevel = new TreeMap<Level, Area>(levelComparator);
		// Compute union of the areas of pieces at ground level that are not lights, doors or windows
		for (Map.Entry<HomePieceOfFurniture, Node> object3DEntry : pieces3D.entrySet())
		{
			if (object3DEntry.getKey() instanceof HomePieceOfFurniture)
			{
				HomePieceOfFurniture piece = object3DEntry.getKey();
				// This operation can be lengthy, so give up if thread is interrupted
				if (Thread.currentThread().isInterrupted())
				{
					return;
				}
				if (piece.getElevation() == 0 && !piece.isDoorOrWindow() && !(piece instanceof com.eteks.sweethome3d.model.Light))
				{
					Area pieceAreaOnFloor = ModelManager.getInstance().getAreaOnFloor(object3DEntry.getValue());
					Level level = piece.getLevel();
					if (piece.getLevel() == null)
					{
						level = new Level("Dummy", 0, 0, 0);
					}
					if (level.isViewableAndVisible())
					{
						Area areaOnLevel = areasOnLevel.get(level);
						if (areaOnLevel == null)
						{
							areaOnLevel = new Area();
							areasOnLevel.put(level, areaOnLevel);
						}
						areaOnLevel.add(pieceAreaOnFloor);
					}
				}
			}
		}

		// Create the 3D shape matching computed areas
		Shape3D shadow = new Shape3D();
		for (Map.Entry<Level, Area> levelArea : areasOnLevel.entrySet())
		{
			List<Point3f> coords = new ArrayList<Point3f>();
			List<Integer> stripCounts = new ArrayList<Integer>();
			int pointsCount = 0;
			float[] modelPoint = new float[2];
			for (PathIterator it = levelArea.getValue().getPathIterator(null); !it.isDone(); )
			{
				if (it.currentSegment(modelPoint) == PathIterator.SEG_CLOSE)
				{
					stripCounts.add(pointsCount);
					pointsCount = 0;
				}
				else
				{
					coords.add(new Point3f(modelPoint[0], levelArea.getKey().getElevation() + 0.49f, modelPoint[1]));
					pointsCount++;
				}
				it.next();
			}

			if (coords.size() > 0)
			{
				GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
				geometryInfo.setCoordinates(coords.toArray(new Point3f[coords.size()]));
				int[] stripCountsArray = new int[stripCounts.size()];
				for (int i = 0; i < stripCountsArray.length; i++)
				{
					stripCountsArray[i] = stripCounts.get(i);
				}
				geometryInfo.setStripCounts(stripCountsArray);
				shadow.addGeometry(geometryInfo.getIndexedGeometryArray());
			}
		}

		Appearance shadowAppearance = new SimpleShaderAppearance();
		shadowAppearance.setColoringAttributes(new ColoringAttributes(new Color3f(), ColoringAttributes.SHADE_FLAT));
		shadowAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.7f));
		shadow.setAppearance(shadowAppearance);
		homeRoot.addChild(shadow);
	}
}
