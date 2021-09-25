/*
 * HomeComponent3D.java 24 ao?t 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.renovations3d.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.eteks.renovations3d.NavigationPanel;
import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.ToolTipManager;
import com.eteks.renovations3d.Tutorial;
import com.eteks.renovations3d.android.utils.LevelSpinnerControl;
import com.eteks.renovations3d.android.utils.ToolSpinnerControl;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.google.firebase.components.Component;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.GLException;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.j3d.utils.InfoText3D;
import com.mindblowing.j3d.utils.JoglStatusActivity;
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
import com.mindblowing.j3d.utils.AndyFPSCounter;
import com.mindblowing.j3d.utils.Canvas3D2D;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
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
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TexCoordGeneration;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureAttributes;
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
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.Vector4f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import javaawt.Color;
import javaawt.EventQueue;
import javaawt.GraphicsConfiguration;
import javaawt.geom.Area;
import javaawt.geom.GeneralPath;
import javaawt.geom.PathIterator;
import javaawt.geom.Rectangle2D;
import javaawt.image.BufferedImage;
import jogamp.newt.WindowImpl;
import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.WindowDriver;

import static com.eteks.renovations3d.Renovations3DActivity.PREFS_NAME;
import static com.eteks.renovations3d.android.utils.WelcomeDialog.possiblyShowWelcomeScreen;

/**
 * A component that displays home walls, rooms and furniture with Java 3D.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class HomeComponent3D extends NewtBaseFragment implements com.eteks.sweethome3d.viewcontroller.View {

	private static final String RUN_UPDATES = "RUN_UPDATES";
	private boolean fullRoomUpdateRequired = false;
	private boolean fullWallUpdateRequired = false;

	public static boolean ENABLE_HUD = false;
	public static final String WELCOME_SCREEN_UNWANTED = "COMPONENT_3D_WELCOME_SCREEN_UNWANTED";

	private static final String DEOPTOMIZE = "DEOPTOMIZE";

	// if we are not initialized then ignore onCreateViews
	private boolean initialized = false;

	private AndyFPSCounter fpsCounter;
	private InfoText3D onscreenInfo;// not used currently

	private NavigationPanel navigationPanel;

	private HomeComponent3DMouseHandler homeComponent3DMouseHandler;
	private ScaleGestureDetector mScaleDetector;

	private Menu mOptionsMenu;
	private ToolSpinnerControl toolSpinnerControl;
	private LevelSpinnerControl levelSpinnerControl;
	private Spinner levelsSpinner;
	private Spinner toolSpinner;
	private String[] toolNames;
	private int[] toolIcon = new int[]{
					R.drawable.plan_select,
					R.drawable.plan_pan
	};

	private GLCapabilities caps;
	private GLWindow gl_window;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
		boolean deoptomize = settings.getBoolean(DEOPTOMIZE, false);
		setDeoptomize(deoptomize);

		try {
			caps = new GLCapabilities(null);
		} catch (GLException e) {
			//E.g. Profile GL_DEFAULT is not available on EGLGraphicsDevice[type .egl, v1.4.0, connection decon, unitID 0, handle 0x1, owner true, NullToolkitLock[obj 0x163357fd]], but: []

			// possibly we struggle on, maybe this is a double create and so we can ignore this life cycle
		}
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
			wd.setNativeWindowExceptionListener( new WindowImpl.NativeWindowExceptionListener() {
				/**
				 * Attempt to tell the user the 3D resources are low and they should save and resart
				 * @param nwp
				 * @return
				 */
				public boolean handleException(NativeWindowException nwp) {
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NativeWindowException", null );
					String message = getActivity().getString(R.string.insufficient3dResourcesMessage);
					String title = getActivity().getString(R.string.insufficient3dResourcesTitle);
					JOptionPane.showMessageDialog(getActivity(), message, title, JOptionPane.ERROR_MESSAGE);
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
				public boolean handleRuntimeException(RuntimeException e) {
					// just ignore the failed lock and hope it is acquired during later processing
					if(e.getMessage().contains("Waited 5000ms"))
						return true;

					return false;
				}
			});
		}


		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "GLWindow.create(caps)", null);
		// equal to addAncestor listeners but that's too late by far
		gl_window.addGLEventListener(glWindowInitListener);
	}

	GLEventListener glWindowInitListener = new GLEventListener() {
		@Override
		public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable) {
		}

		@Override
		public void reshape(final GLAutoDrawable drawable, final int x, final int y,
												final int w, final int h) {
		}

		@Override
		public void display(final GLAutoDrawable drawable) {
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start display", null );

			// old createComponent3D calls addMouseListener which attaches this listener to the view
			// that view is being destroyed and I'm getting back here so I have to re-add the mouse listener now
			// so instead I just always add the mouse listener here now

			getView().setOnTouchListener(new TouchyListener());

			if (canvas3D != null) {
				// must call this as onPause has called removeNotify
				try {
					canvas3D.addNotify();
					//wait for onscreen hint
					if (!HomeComponent3D.this.getUserVisibleHint()) {
						canvas3D.stopRenderer();
					} else {
						canvas3D.startRenderer();
					}
				} catch(NullPointerException e) {
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "1canvas3D2D.addNotify() null 0", null );
					if(gl_window != null) {
						Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "1canvas3D2D.addNotify() null 1", "ChosenGLCapabilities(): " + gl_window.getChosenGLCapabilities() );
					}
					// let's see if other failures happen or is this just a race condition that can be ignored
					//throw e;
				}
			} else {
				// taken from ancestor listener originally so get back onto EDT thread
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// Create component 3D only once the graphics configuration of its parent is known
						if (canvas3D == null) {
							createComponent3D(null, preferences, controller);
							System.out.println("createComponent3D");
							try {
								// called here not in createComponent, just for life cycle clarity
								canvas3D.addNotify();
							} catch(NullPointerException e) {
								Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "2canvas3D2D.addNotify() null 0", null);
								if(gl_window != null) {
									Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "2canvas3D2D.addNotify() null 1", "ChosenGLCapabilities(): " + gl_window.getChosenGLCapabilities() );
								}

								// let's see if other failures happen or is this just a race condition that can be ignored
								//throw e;
							}

							//wait for onscreen hint as this component is create whilst off screen
							if (!HomeComponent3D.this.getUserVisibleHint()) {
								canvas3D.stopRenderer();
							}

						}

						if (onscreenUniverse == null) {
							onscreenUniverse = createUniverse(displayShadowOnFloor, true, false);
							onscreenUniverse.getViewer().getView().addCanvas3D(canvas3D);
							if(BuildConfig.DEBUG && ENABLE_HUD && canvas3D instanceof Canvas3D2D) {
								fpsCounter = new AndyFPSCounter();
								onscreenUniverse.addBranchGraph(fpsCounter.getBehaviorBranchGroup());
								fpsCounter.addToCanvas((Canvas3D2D) canvas3D);
								onscreenInfo = new InfoText3D() {
									@Override
									protected String getText() {
										return "";
									}
								};
								onscreenUniverse.addBranchGraph(onscreenInfo.getBehaviorBranchGroup());
								onscreenInfo.addToCanvas((Canvas3D2D) canvas3D);
							}

							// I have no idea how getActivity() can return null here, possibly we are being destroyed right now
							// but I think it happens after the canvas.addnotify failure above anyway
							if(getActivity() != null) {
								// mouse interaction with picking
								homeComponent3DMouseHandler = new HomeComponent3DMouseHandler(home, preferences, controller, (Renovations3DActivity) getActivity());
								homeComponent3DMouseHandler.setConfig(canvas3D, onscreenUniverse.getLocale());
							}
						}
					}
				});
			}
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end display", null );
		}

		@Override
		public void dispose(final GLAutoDrawable drawable) {
			if(canvas3D != null) {
				canvas3D.stopRenderer();
				canvas3D.removeNotify();
			}

			PlanComponent.PieceOfFurnitureModelIcon.pauseOffScreenRendering();
			if(onscreenUniverse != null) {
				onscreenUniverse.cleanup();
				onscreenUniverse = null;
			}
			PlanComponent.PieceOfFurnitureModelIcon.unpauseOffScreenRendering();

			// taken from ancestor listener originally so get back onto EDT thread
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (onscreenUniverse != null) {
						removeHomeListeners();
					}
				}
			});
		}

	};

	@Override
	public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(initialized) {
			this.setHasOptionsMenu(true);
			this.toolSpinnerControl = new ToolSpinnerControl(this.getContext());
			this.levelSpinnerControl = new LevelSpinnerControl(this.getContext());
		}
		//make sure we aren't being destroyed by onPause
		if(gl_window.getDelegatedWindow() != null) {
			android.view.View rootView = getContentView(this.getWindow(), gl_window);
			return rootView;
		}
		return null;
	}

	public void onStart() {
		super.onStart();
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onStart", null );
	}

	public void onResume() {
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onResume", null );
		super.onResume();

		// ok at this point either
		// A/ we've just started up onStart was called and now onResume
		// B/ we've onPaused (possible onStop too) and GLStateKeeper saved state
		// 		now we've (possible onStart then) onResume and GLStateKeeper is working in the background on restoring state
		// 		a display is going to come through soon to make things show after state is happy
		// in both cases display callback is gonna get called, he needs to addNotify in all cases
		if(canvas3D != null) {
			//PJ I add this entire conditional to try to restart after a stop it appears ok, but be suspicious of it
			if(!canvas3D.getGLWindow().isNativeValid()) {
				gl_window = GLWindow.create(caps);
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "GLWindow.create(caps) recreate", null );
				// equal to addAncestor listeners but that's too late by far
				gl_window.addGLEventListener(glWindowInitListener);
			} else {
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onResume pre addNotify", null );
				canvas3D.addNotify();
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onResume post addNotify", null );
			}

			if (HomeComponent3D.this.getUserVisibleHint()) {
				canvas3D.startRenderer();
			}
		}
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onResume", null );
	}

	@Override
	public void onPause() {
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onPause", null );

		// so this is part of the exit so we need to call removeNotify in all cases, all re-entries will arrive back at display eventually
		// and display will always call addNotify
		if(canvas3D != null) {
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onPause pre removeNotify", null );
			canvas3D.stopRenderer();
			canvas3D.removeNotify();
			setNavigationPanelVisible(false);
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onPause post removeNotify", null );
		}

		PlanComponent.PieceOfFurnitureModelIcon.pauseOffScreenRendering();
		// note super onPause does NOT save state but marks to preserve ready for another lifecycle change like onStop
		super.onPause();

		PlanComponent.PieceOfFurnitureModelIcon.unpauseOffScreenRendering();
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onPause", null );
	}

	@Override
	public void onStop() {
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onStop", null );
		// MUST output GLStatePreserved on console, or it won't restart
		super.onStop();
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onStop", null );
	}

	@Override
	public void onDestroy() {
		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "start onDestroy", null );
		// now we want to dump the universe as this fragment is being garbage collected shortly
		if(canvas3D != null) {
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onDestroy pre removeNotify", null );
			canvas3D.stopRenderer();
			canvas3D.removeNotify();
			setNavigationPanelVisible(false);
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onDestroy post removeNotify", null );
		}

		PlanComponent.PieceOfFurnitureModelIcon.destroyUniverse();
		if(onscreenUniverse != null) {
			//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onscreenUniverse.cleanup();", null );
			try {
				onscreenUniverse.cleanup();
			}catch(Exception e) {
				// in production I don't care about exceptions now, but I do care about crashing.
				e.printStackTrace();
			}
			onscreenUniverse = null;
		}
		PlanComponent.PieceOfFurnitureModelIcon.pauseOffScreenRendering();
		try {
			super.onDestroy();
		} catch(Exception e) {
			//ignore as we are done
			e.printStackTrace();
		}
		PlanComponent.PieceOfFurnitureModelIcon.unpauseOffScreenRendering();

		//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "end onDestroy", null );
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// tell walls to update now
		if (isVisibleToUser && wallChangeListener != null) {
			wallChangeListener.propertyChange(new PropertyChangeEvent(this, RUN_UPDATES, null, null));
		}

		if(isVisibleToUser && getContext() != null) {
			possiblyShowWelcomeScreen((Renovations3DActivity) getContext(), WELCOME_SCREEN_UNWANTED, R.string.welcometext_component3dview, preferences);

			// tell the tutorial we've been shown
			((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.VIEW_SHOWN_3D);
		}

		if( navigationPanel != null) {
			setNavigationPanelVisible(isVisibleToUser);
		}

		if (canvas3D != null) {
			if (isVisibleToUser) {
				canvas3D.startRenderer();
			} else {
				canvas3D.stopRenderer();
			}
		}



		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mOptionsMenu = menu;// for later use
		inflater.inflate(R.menu.home_component3d_menu, menu);
		menu.findItem(R.id.virtual_visit).setChecked(home.getCamera() == home.getObserverCamera());
		boolean allLevelsVisible = home.getEnvironment().isAllLevelsVisible();
		menu.findItem(R.id.viewalllevels).setChecked(allLevelsVisible);

		createGoToPointOfViewMenu(home, menu.findItem(R.id.go_to_camera_position));

		SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
		boolean deoptomize = settings.getBoolean(DEOPTOMIZE, false);
		menu.findItem(R.id.deoptomize).setChecked(deoptomize);

		toolSpinner = (Spinner) menu.findItem(R.id.home3dToolSelectSpinner).getActionView();
		toolSpinner.setPadding(toolSpinner.getPaddingLeft(), 0, toolSpinner.getPaddingRight(), toolSpinner.getPaddingBottom());
		// possibly on a double onCreateView call this gets called and the levelSpinnerControl has not yet been created so ignore the call this time round
		updateToolNames();
		if (toolSpinnerControl != null)
			toolSpinnerControl.setSpinner(toolSpinner, toolNames, toolIcon);

		this.levelSpinnerControl.removeAll();
		List<Level> levels = home.getLevels();
		for (int i = 0; i < levels.size(); i++) {
			Level level = levels.get(i);
			this.levelSpinnerControl.addTab(level.getName(), new MultipleLevelsPlanPanel.LevelLabel(level));
		}

		levelsSpinner = (Spinner) menu.findItem(R.id.home3dLevelsSpinner).getActionView();
		levelSpinnerControl.setSpinner(levelsSpinner);
		// now set the correctly selected item
		Level selectedLevel = home.getSelectedLevel();
		if (levels.size() >= 2 && selectedLevel != null) {
			this.levelSpinnerControl.setSelectedIndex(levels.indexOf(selectedLevel));
		}

		final ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				MultipleLevelsPlanPanel.LevelLabel selectedComponent = levelSpinnerControl.getSelectedComponent();
				//also checks for nullness in the umexpected case of a null activity
				if (getActivity() instanceof Renovations3DActivity) {
					HomeController homeController = ((Renovations3DActivity) getActivity()).getHomeController();
					if (homeController != null) {
						homeController.getPlanController().setSelectedLevel(selectedComponent.getLevel());
					}
				}
			}
		};
		this.levelSpinnerControl.addChangeListener(changeListener);

		menu.findItem(R.id.home3dLevelsSpinner).setVisible(home.getLevels().size() > 0);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void updateToolNames() {
		toolNames = new String[]{
						preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "SELECT.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "PAN.Name")
		};
	}




	private void setMode(PlanController.Mode mode) {
		if(homeComponent3DMouseHandler != null)
			homeComponent3DMouseHandler.setEnabled(mode == PlanController.Mode.SELECTION);

		HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
		if(homeController != null) {
			PlanController planController = homeController.getPlanController();
			planController.setMode(mode);
			toolSpinner.setOnItemSelectedListener(null);
			// only select or panning, non selection defaults to panning
			int position = mode == PlanController.Mode.SELECTION ? 0 : 1;
			toolSpinner.setSelection(position);
			toolSpinner.setOnItemSelectedListener(planToolSpinnerListener);
		}
	}
	AdapterView.OnItemSelectedListener planToolSpinnerListener = new AdapterView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id)
		{
			if (getActivity() != null) {
				switch (position) {
					case 0:
						setMode(PlanController.Mode.SELECTION);
						break;
					case 1:
						setMode(PlanController.Mode.PANNING);
						break;
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// this should never happen, no real plan here
		}
	};

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem virtualVisit = menu.findItem(R.id.virtual_visit);
		virtualVisit.setTitle(preferences.getLocalizedString(
						com.eteks.sweethome3d.swing.HomePane.class, "VIEW_FROM_OBSERVER.Name"));
		setIconFromSelector(virtualVisit, R.drawable.virtualvist_selector);

		//both on action bar
		menu.findItem(R.id.go_to_camera_position).setTitle(R.string.goToCameraPosition);

		// tools on action bar now
		HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
		if(homeController != null) {
			setMode(homeController.getPlanController().getMode());
		}

		menu.findItem(R.id.home3dLevelsSpinner).setVisible(home.getLevels().size() > 0);

		MenuItem deletePov = menu.findItem(R.id.delete_camera_position);
		String deletePovStr =  getActivity().getString(R.string.deleteCameraPosition);
		Renovations3DActivity.setIconizedMenuTitle(deletePov, deletePovStr, R.drawable.ic_videocam_off_black_24dp, getContext());

		MenuItem cameraMenu = menu.findItem(R.id.cameraMenu);
		String cameraMenuStr = "...";
		Renovations3DActivity.setIconizedMenuTitle(cameraMenu, cameraMenuStr, R.drawable.ic_videocam_black_24dp, getContext());

		MenuItem modVV = menu.findItem(R.id.modify_virtual_visitor);
		String modVVStr =  preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "MODIFY_OBSERVER.Name");
		Renovations3DActivity.setIconizedMenuTitle(modVV, modVVStr, R.drawable.view3d_view_from_observer, getContext());

		menu.findItem(R.id.viewalllevels).setTitle(preferences.getLocalizedString(
						com.eteks.sweethome3d.swing.HomePane.class, "DISPLAY_ALL_LEVELS.Name"));

		menu.findItem(R.id.modify3dview).setTitle(preferences.getLocalizedString(
						com.eteks.sweethome3d.swing.HomePane.class, "MODIFY_3D_ATTRIBUTES.Name"));

		MenuItem createPhoto = menu.findItem(R.id.createPhoto);
		String createPhotoStr =  preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "CREATE_PHOTO.Name");
		Renovations3DActivity.setIconizedMenuTitle(createPhoto, createPhotoStr, R.drawable.ic_add_a_photo_black_24dp, getContext());

		MenuItem createVideo = menu.findItem(R.id.createVideo);
		String createVideoStr =  preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "CREATE_VIDEO.Name");
		Renovations3DActivity.setIconizedMenuTitle(createVideo, createVideoStr, R.drawable.ic_video_call_black_24dp, getContext());

		menu.findItem(R.id.exportToObj).setTitle(preferences.getLocalizedString(
						com.eteks.sweethome3d.swing.HomePane.class, "EXPORT_TO_OBJ.Name"));

		updateGoToPointOfViewMenu(menu.findItem(R.id.go_to_camera_position), home);

		super.onPrepareOptionsMenu(menu);
	}

	// Taken from HomePane and adapted
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
	// Taken from HomePane and adapted
	/**
	 * Updates Go to point of view menu items from the cameras stored in home.
	 */
	private void updateGoToPointOfViewMenu(MenuItem goToPointOfViewMenu,
										   Home home) {
		List<Camera> storedCameras = new ArrayList<Camera>(home.getStoredCameras());
		// sort them so this list is consistent
		Collections.sort(storedCameras, new Comparator<Camera>()
		{
			@Override
			public int compare(Camera o1, Camera o2)
			{
				if(o1==null||o2==null)
						return 0;
				return o1.getName().compareTo(o2.getName());
			}
		});

		goToPointOfViewMenu.getSubMenu().clear();
		// put back the add item
		MenuItem item = goToPointOfViewMenu.getSubMenu().add(Menu.NONE, R.id.store_camera_position,0, R.string.addCameraPosition);
		item.setIcon(R.drawable.ic_video_call_black_24dp);

		if (storedCameras.isEmpty()) {
			goToPointOfViewMenu.setEnabled(false);
			String name = SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomePane.class, "NoStoredPointOfView");
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
				//				controller.goToCamera(camera);
				//			}
				//		});
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle camera selections first
		if(item.getGroupId() == MENU_STORED_CAMERAS) {
			String cameraName = item.getTitle().toString();
			List<Camera> storedCameras = home.getStoredCameras();
			for (final Camera camera : storedCameras) {
				if(cameraName.equals(camera.getName())) {
					if(controller != null) {
						controller.goToCamera(camera);
						// update the check box item nicely
						MenuItem vv = mOptionsMenu.findItem(R.id.virtual_visit);
						vv.setChecked(home.getCamera() == home.getObserverCamera());
						setIconFromSelector(vv, R.drawable.virtualvist_selector);
						return true;
					}
				}
			}
		} else {
			// Handle item selection
			switch (item.getItemId()) {
				case R.id.goto_2D_view:
					((Renovations3DActivity)getActivity()).getViewPager().setCurrentItem(1, false);// true cause no render! god knows why
					break;
				case R.id.virtual_visit:
					item.setChecked(!item.isChecked());
					setIconFromSelector(item, R.drawable.virtualvist_selector);
					if (item.isChecked()) {
						controller.viewFromObserver();
						// tell the tutorial
						((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.VIRTUAL_VISIT_STARTED);
					} else {
						controller.viewFromTop();
					}
					break;
				case R.id.go_to_camera_position:
					// do nothing it just nicely opens the list for us
					break;
				case R.id.modify_virtual_visitor:
					HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
					if(homeController != null) {
						homeController.getPlanController().modifyObserverCamera();
					}
					break;
				case R.id.store_camera_position:
					//I must get off the EDT and ask the question in a blocking manner
					Thread t2 = new Thread() {
						public void run() {
							HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
							if(homeController != null) {
								homeController.storeCamera();
							}
						}
					};
					t2.start();
					break;
				case R.id.delete_camera_position:
					Thread t3 = new Thread() {
						public void run() {
							HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
							if(homeController != null) {
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
						if(homeController2 != null) {
							homeController2.createPhoto();
						}
					break;
				case R.id.createVideo:
					HomeController homeController3 = ((Renovations3DActivity)getActivity()).getHomeController();
					if(homeController3 != null) {
						homeController3.createVideo();
					}
					break;
				case R.id.exportToObj:
					Thread t4 = new Thread() {
						public void run() {
							HomeController homeController = ((Renovations3DActivity)getActivity()).getHomeController();
							if(homeController != null) {
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
					if(!getActivity().isFinishing())
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

	private void setIconFromSelector(MenuItem item, int resId) {
		StateListDrawable stateListDrawable = (StateListDrawable) ContextCompat.getDrawable(getContext(), resId);
		int[] state = {item.isChecked() ? android.R.attr.state_checked : android.R.attr.state_empty};
		stateListDrawable.setState(state);
		item.setIcon(stateListDrawable.getCurrent());
	}

	public void setDeoptomize(boolean deoptomize) {
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

	private enum ActionType {MOVE_CAMERA_FORWARD, MOVE_CAMERA_FAST_FORWARD, MOVE_CAMERA_BACKWARD, MOVE_CAMERA_FAST_BACKWARD,
		MOVE_CAMERA_LEFT, MOVE_CAMERA_FAST_LEFT, MOVE_CAMERA_RIGHT, MOVE_CAMERA_FAST_RIGHT,
		ROTATE_CAMERA_YAW_LEFT, ROTATE_CAMERA_YAW_FAST_LEFT, ROTATE_CAMERA_YAW_RIGHT, ROTATE_CAMERA_YAW_FAST_RIGHT,
		ROTATE_CAMERA_PITCH_UP, ROTATE_CAMERA_PITCH_FAST_UP, ROTATE_CAMERA_PITCH_DOWN, ROTATE_CAMERA_PITCH_FAST_DOWN,
		ELEVATE_CAMERA_UP, ELEVATE_CAMERA_FAST_UP, ELEVATE_CAMERA_DOWN, ELEVATE_CAMERA_FAST_DOWN}

	//private static final boolean JAVA3D_1_5 = VirtualUniverse.getProperties().get("j3d.version") != null
	//		&& ((String)VirtualUniverse.getProperties().get("j3d.version")).startsWith("1.5");

	private Home home;
	private boolean displayShadowOnFloor;
	private Object3DFactory object3dFactory;
	private final Map<Selectable, Object3DBranch> homeObjects = new HashMap<Selectable, Object3DBranch>();
  	private Light [] sceneLights;
	private Collection<Selectable> homeObjectsToUpdate;
	private Collection<Selectable> lightScopeObjectsToUpdate;
	//private Component component3D;
	private Canvas3D canvas3D;// component3D swapped to direct Canvas3D2D
	private SimpleUniverse onscreenUniverse;
	private Camera camera;
	// Listeners bound to home that updates 3D scene objects
	private PropertyChangeListener cameraChangeListener;
	private PropertyChangeListener homeCameraListener;
	private PropertyChangeListener backgroundChangeListener;
	private PropertyChangeListener groundChangeListener;
	private PropertyChangeListener backgroundLightColorListener;
	private PropertyChangeListener lightColorListener;
	private PropertyChangeListener subpartSizeListener;
	private PropertyChangeListener  elevationChangeListener;
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
  private CollectionListener<Polyline>             polylineListener;
  private PropertyChangeListener                   polylineChangeListener;
	private CollectionListener<Label> labelListener;
	private PropertyChangeListener labelChangeListener;
	private SelectionListener selectionOutliningListener;
	// Offscreen printed image cache
	// Creating an offscreen buffer is a quite lengthy operation so we keep the last printed image in this field
	// This image should be set to null each time the 3D view changes
	//private BufferedImage printedImageCache;
	private BoundingBox approximateHomeBoundsCache;
	private SimpleUniverse offscreenUniverse;

	//private JComponent navigationPanel;
	//private ComponentListener navigationPanelListener;
	//private BufferedImage navigationPanelImage;
	private Area lightScopeOutsideWallsAreaCache;


	// record from the init call to gl window init call
	private UserPreferences preferences;
	private HomeController3D controller;

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture,
	 * with no controller.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home)	{
		init(home, null);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home, HomeController3D controller) {
		init(home, null, controller);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture,
	 * with shadows on the floor.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home,
									 UserPreferences preferences,
									 boolean displayShadowOnFloor) {
		init(home, preferences, new Object3DBranchFactory(preferences), displayShadowOnFloor, null);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home,
									 UserPreferences preferences,
									 HomeController3D controller) {
		init(home, preferences, new Object3DBranchFactory(preferences), false, controller);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 * @param home            the home to display in this component
	 * @param preferences     user preferences
	 * @param object3dFactory a factory able to create 3D objects from <code>home</code> items.
	 *                        The {@link Object3DFactory#createObject3D(Home, Selectable, boolean) createObject3D} of
	 *                        this factory is expected to return an instance of {@link Object3DBranch} in current implementation.
	 * @param controller      the controller that manages modifications in <code>home</code>.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home,
									 UserPreferences preferences,
									 Object3DFactory object3dFactory,
									 HomeController3D controller) {
		init(home, preferences, object3dFactory, false, controller);
	}

	/**
	 * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
	 * @throws IllegalStateException if the 3D component couldn't be created.
	 */
	public void init(Home home,
									 UserPreferences preferences,
									 Object3DFactory object3dFactory,
									 boolean displayShadowOnFloor,
									 HomeController3D controller) {
		initialized = true;
		//record for init
		this.preferences = preferences;
		this.controller = controller;

		this.home = home;
		this.displayShadowOnFloor = displayShadowOnFloor;
    	this.object3dFactory = object3dFactory != null
        	? object3dFactory
        	: new Object3DBranchFactory(preferences);

		if (controller != null) {
			createActions(controller);
			installKeyboardActions();
			// Let this component manage focus
			// setFocusable(true);
			// SwingTools.installFocusBorder(this);
		}

		// deferred to gl window init above
		/*GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (graphicsEnvironment.getScreenDevices().length == 1)	{
			// If only one screen device is available, create canvas 3D immediately,
			// otherwise create it once the screen device of the parent is known
			createComponent3D(null//graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration()
			, preferences, controller);
		}*/

		// Add an ancestor listener to create canvas 3D and its universe once this component is made visible
		// and clean up universe once its parent frame is disposed
		// put into gl window listener above
		//addAncestorListener(preferences, controller, displayShadowOnFloor);

		// for outlining
		selectionOutliningListener = new SelectionOutliningListener();
		home.addSelectionListener(selectionOutliningListener);


	}


	private class SelectionOutliningListener implements SelectionListener {
		@Override
		public void selectionChanged(SelectionEvent selectionEvent) {
			for(Selectable sel : homeObjects.keySet()) {
				boolean isSelected = selectionEvent.getSelectedItems().contains(sel);
				Object3DBranch obj3D = homeObjects.get(sel);
				if (obj3D.isShowOutline() != isSelected)
					obj3D.showOutline(isSelected);
			}
		}
	}

  /**
   * Adds an ancestor listener to this component to manage the creation of the canvas and its universe
   * and clean up the universe.
   */
	private void addAncestorListener(final UserPreferences preferences,
																	 final HomeController3D controller,
																	 final boolean displayShadowOnFloor) {
		//the base calls here are moved into the gl window listener at onCreate so the display is not lost
	}

	/**
	 * Creates the 3D component associated with the given <code>configuration</code> device.
	 */
	private void createComponent3D(GraphicsConfiguration configuration,
																 UserPreferences preferences,
																 HomeController3D controller) {
		// construction is very different
		canvas3D = Component3DManager.getInstance().getOnscreenCanvas3D(gl_window,
				new Component3DManager.RenderingObserver() {
			//private Shape3D dummyShape;

			public void canvas3DSwapped(Canvas3D canvas3D) {
			}

			public void canvas3DPreRendered(Canvas3D canvas3D) {
			}

			public void canvas3DPostRendered(Canvas3D canvas3D) {
			}
		});

		// start isn the stopped state, so initial load up isn't laboured
		canvas3D.stopRenderer();

		//canvasPanel.add(this.component3D);
		//setLayout(new GridLayout());
		//add(canvasPanel);
		//replaced with canvas3D.addNotify() but also moved into gl_window display call for life cycle clarity
		if (controller != null)	{
			addMouseListeners(controller, this.canvas3D);
			if (preferences != null) {
				setNavigationPanelVisible(preferences.isNavigationPanelVisible() && isVisible() && this.getUserVisibleHint());
			}
			//createActions(controller);
			//installKeyboardActions();
			// Let this component manage focus
			//setFocusable(true);
			//SwingTools.installFocusBorder(this);
		}
	}

	/**
	 * A <code>JCanvas</code> canvas that displays the navigation panel of a home component 3D upon it.
	 */

/*	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (this.component3D != null) {
			this.component3D.setVisible(visible);

			//PJPJPJPJ do something interesting here
			this.canvas3D.setVisible(visible);
		}
	}*/


	/**
	 * Preferences property listener bound to this component with a weak reference to avoid
	 * strong link between preferences and this component.
	 */
	private static class NavigationPanelChangeListener implements PropertyChangeListener {
		private final WeakReference<HomeComponent3D> homeComponent3D;

		public NavigationPanelChangeListener(HomeComponent3D homeComponent3D) {
			this.homeComponent3D = new WeakReference<HomeComponent3D>(homeComponent3D);
		}

		public void propertyChange(PropertyChangeEvent ev) {

			// If home pane was garbage collected, remove this listener from preferences
			HomeComponent3D homeComponent3D = this.homeComponent3D.get();

			if (homeComponent3D == null) {
				((UserPreferences)ev.getSource()).removePropertyChangeListener(
								UserPreferences.Property.NAVIGATION_PANEL_VISIBLE, this);
			} else {

				homeComponent3D.setNavigationPanelVisible((Boolean) ev.getNewValue() && homeComponent3D.isVisible() && homeComponent3D.getUserVisibleHint());
			}
		}
	}

	/**
	 * Returns the component displayed as navigation panel by this 3D view.
	 */
  private NavigationPanel createNavigationPanel( UserPreferences preferences,
																					 HomeController3D controller) {

		NavigationPanel navigationPanel = new NavigationPanel(getContext(), getView());
		new NavigationButton(0, -(float) Math.PI / 36, 0, "TURN_LEFT", preferences, controller, navigationPanel.getLeftButton());
		new NavigationButton(12.5f, 0, 0, "GO_FORWARD", preferences, controller, navigationPanel.getForwardButton());
		new NavigationButton(0, (float) Math.PI / 36, 0, "TURN_RIGHT", preferences, controller, navigationPanel.getRightButton());
		new NavigationButton(-12.5f, 0, 0, "GO_BACKWARD", preferences, controller, navigationPanel.getBackButton());
		new NavigationButton(0, 0, -(float) Math.PI / 100, "TURN_UP", preferences, controller, navigationPanel.getUpButton());
		new NavigationButton(0, 0, (float) Math.PI / 100, "TURN_DOWN", preferences, controller, navigationPanel.getDownButton());
		return navigationPanel;
	}

  /**
   * An icon button that changes camera location and angles when pressed.
   */
  private static class NavigationButton  {
    private boolean shiftDown;

    public NavigationButton(final float moveDelta,
                            final float yawDelta,
                            final float pitchDelta,
                            String actionName,
                            UserPreferences preferences,
                            final HomeController3D controller,
														android.view.View button) {


			button.setOnKeyListener(new android.view.View.OnKeyListener() {
					@Override
					public boolean onKey(android.view.View v, int keyCode, KeyEvent event) {
						if(event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_RIGHT) {
							shiftDown = event.getAction() == KeyEvent.ACTION_DOWN;
						}
						return false;
					}
				});
      // Update camera when button is armed
			button.setOnTouchListener(new android.view.View.OnTouchListener() {
				// Create a timer that will update camera angles and location
				Timer timer;
				@Override
				public boolean onTouch(android.view.View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
							if(timer != null){
								timer.cancel();
							}
							timer = new Timer("NavButtonTimer",true);
              timer.schedule(new TimerTask() {
              	int repeatCount = 0;
								@Override
								public void run() {
									// increase speed after 20*50 = 1 sec
									// and a bit more after 4 secs
									repeatCount++;
									float speedDivisor = (repeatCount > 20 ?  repeatCount > 80 ? 0.5f : 1.5f : 5);
									controller.moveCamera(shiftDown ? moveDelta : moveDelta / speedDivisor);
									controller.rotateCameraYaw(shiftDown ? yawDelta : yawDelta / speedDivisor);
									controller.rotateCameraPitch(pitchDelta);
								}
							},50, 50 );
            } else if (event.getAction() == MotionEvent.ACTION_UP && timer != null) {
              timer.cancel();
            }
						return true;
          }

        });
    }
  }

	/**
	 * Sets the component that will be drawn upon the heavyweight 3D component shown by this component.
	 * Mouse events will targeted to the navigation panel when needed.
	 * Supports transparent components.
	 */
  private void setNavigationPanelVisible(boolean visible) {

		if(getContext() != null && this.navigationPanel == null && preferences != null) {
				this.navigationPanel = createNavigationPanel(preferences, controller);
				preferences.addPropertyChangeListener(UserPreferences.Property.NAVIGATION_PANEL_VISIBLE,
								new NavigationPanelChangeListener(this));
		}

		if(navigationPanel != null) {
			if (preferences != null && preferences.isNavigationPanelVisible() && visible && canvas3D != null) {
				navigationPanel.showTooltip();
			} else {
				navigationPanel.hideTooltip();
			}
		}
  }

	/**
	 * Updates the image of the components that may overlap canvas 3D
	 * (with a Z order smaller than the one of the canvas 3D).
	 */

	/**
	 * Returns a new 3D universe that displays <code>home</code> objects.
	 */
	private SimpleUniverse createUniverse(boolean displayShadowOnFloor,
										  boolean listenToHomeUpdates,
										  boolean waitForLoading) {
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
    	updateView(view, this.home.getCamera());

		// Update point of view from current camera
		updateViewPlatformTransform(viewPlatformTransform, this.home.getCamera(), false);

		// Add camera listeners to update later point of view from camera
    	if (listenToHomeUpdates) {
			addCameraListeners(view, viewPlatformTransform);
		}

		// Link scene matching home to universe
		universe.addBranchGraph(createSceneTree(
				displayShadowOnFloor, listenToHomeUpdates, waitForLoading));

		return universe;
	}

	/**
	 * Remove all listeners bound to home that updates 3D scene objects.
	 */
	private void removeHomeListeners() {
		this.home.removePropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
		HomeEnvironment homeEnvironment = this.home.getEnvironment();
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_COLOR, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_TEXTURE, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.backgroundChangeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.groundChangeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.groundChangeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.BACKGROUND_IMAGE_VISIBLE_ON_GROUND_3D, this.groundChangeListener);
    this.home.removePropertyChangeListener(Home.Property.BACKGROUND_IMAGE, this.groundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.backgroundLightColorListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
		homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SUBPART_SIZE_UNDER_LIGHT, this.subpartSizeListener);
		this.home.getCamera().removePropertyChangeListener(this.cameraChangeListener);
    this.home.removePropertyChangeListener(Home.Property.CAMERA, this.elevationChangeListener);
    this.home.getCamera().removePropertyChangeListener(this.elevationChangeListener);
		this.home.removeLevelsListener(this.levelListener);
    for (Level level : this.home.getLevels()) {
			level.removePropertyChangeListener(this.levelChangeListener);
		}
		this.home.removeWallsListener(this.wallListener);
    for (Wall wall : this.home.getWalls()) {
			wall.removePropertyChangeListener(this.wallChangeListener);
		}
		this.home.removeFurnitureListener(this.furnitureListener);
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
			removePropertyChangeListener(piece, this.furnitureChangeListener);
		}
		this.home.removeRoomsListener(this.roomListener);
		for (Room room : this.home.getRooms()) {
			room.removePropertyChangeListener(this.roomChangeListener);
		}
		this.home.removePolylinesListener(this.polylineListener);
    for (Polyline polyline : this.home.getPolylines()) {
      polyline.removePropertyChangeListener(this.polylineChangeListener);
    }
		this.home.removeLabelsListener(this.labelListener);
		for (Label label : this.home.getLabels()) {
			label.removePropertyChangeListener(this.labelChangeListener);
		}
	}

	/**
	 * Prints this component to make it fill <code>pageFormat</code> imageable size.
	 */
	/*public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex == 0) {
      // Compute printed image size to render 3D view in 150 dpi
      double printSize = Math.min(pageFormat.getImageableWidth(),
          pageFormat.getImageableHeight());
      int printedImageSize = (int)(printSize / 72 * 150);
      if (this.printedImageCache == null
          || this.printedImageCache.getWidth() != printedImageSize) {
        try {
          this.printedImageCache = getOffScreenImage(printedImageSize, printedImageSize);
        } catch (IllegalRenderingStateException ex) {
          // If off screen canvas failed, consider that 3D view page doesn't exist
          return NO_SUCH_PAGE;
        }
      }

      Graphics2D g2D = (Graphics2D)g.create();
      // Center the 3D view in component
      g2D.translate(pageFormat.getImageableX() + (pageFormat.getImageableWidth() - printSize) / 2,
          pageFormat.getImageableY() + (pageFormat.getImageableHeight() - printSize) / 2);
      double scale = printSize / printedImageSize;
      g2D.scale(scale, scale);
      g2D.drawImage(this.printedImageCache, 0, 0, this);
      g2D.dispose();

      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
	}*/

	/**
	 * Optimizes this component for the creation of a sequence of multiple off screen images.
	 * Once off screen images are generated with {@link #getOffScreenImage(int, int) getOffScreenImage},
	 * call {@link #endOffscreenImagesCreation() endOffscreenImagesCreation} method to free resources.
	 */
	public void startOffscreenImagesCreation() {
		if (this.offscreenUniverse == null) {
			if (this.onscreenUniverse != null) {
				throw new IllegalStateException("Can't listen to home changes offscreen and onscreen at the same time");
			}
			this.offscreenUniverse = createUniverse(this.displayShadowOnFloor, true, true);
			// Replace textures by clones because Java 3D doesn't accept all the time
			// to share textures between offscreen and onscreen environments
			// this does not appear necessary on android
			/*Map<Texture, Texture> replacedTextures = new HashMap<Texture, Texture>();
			for (Iterator<BranchGroup> it = this.offscreenUniverse.getLocale().getAllBranchGraphs(); it.hasNext(); ) {
				cloneTexture((Node) it.next(), replacedTextures);
			}*/
		}
	}

	/**
	 * Returns an image of the home viewed by this component at the given size.
	 */
	public BufferedImage getOffScreenImage(int width, int height) {
		List<Selectable> selectedItems = this.home.getSelectedItems();
		SimpleUniverse offScreenImageUniverse = null;
		try {
			View view;
			if (this.offscreenUniverse == null) {
				offScreenImageUniverse = createUniverse(this.displayShadowOnFloor, false, true);
				view = offScreenImageUniverse.getViewer().getView();
				// Replace textures by clones because Java 3D doesn't accept all the time
				// to share textures between offscreen and onscreen environments

				// this does not appear necessary on android
				/*Map<Texture, Texture> replacedTextures = new HashMap<Texture, Texture>();
				for (Iterator<BranchGroup> it = offScreenImageUniverse.getLocale().getAllBranchGraphs(); it.hasNext(); ) {
					cloneTexture(it.next(), replacedTextures);
				}*/
			} else {
				view = this.offscreenUniverse.getViewer().getView();
			}

			updateView(view, this.home.getCamera(), width, height);

			// Empty temporarily selection to create the off screen image
			List<Selectable> emptySelection = Collections.emptyList();
			this.home.setSelectedItems(emptySelection);
			return Component3DManager.getInstance().getOffScreenImage(view, width, height);
		} finally {
			// Restore selection
			this.home.setSelectedItems(selectedItems);
			if (offScreenImageUniverse != null) {
				try {
					offScreenImageUniverse.cleanup();
				} catch(Exception e) {
					// in production I don't care about exceptions now, but I do care about crashing.
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Replace the textures set on node shapes by clones.
	 */
	private void cloneTexture(Node node, Map<Texture, Texture> replacedTextures) {
		if (node instanceof Group) {
			// Enumerate children
			Iterator<Node> enumeration = ((Group) node).getAllChildren();
			while (enumeration.hasNext()) {
        cloneTexture((Node)enumeration.next(), replacedTextures);
			}
		} else if (node instanceof Link) {
			cloneTexture(((Link) node).getSharedGroup(), replacedTextures);
		} else if (node instanceof Shape3D) {
			Appearance appearance = ((Shape3D) node).getAppearance();
			if (appearance != null) {
				Texture texture = appearance.getTexture();
				if (texture != null) {
					Texture replacedTexture = replacedTextures.get(texture);
					if (replacedTexture == null) {
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
	public void endOffscreenImagesCreation() {
		if (this.offscreenUniverse != null) {
			this.offscreenUniverse.cleanup();
			removeHomeListeners();
			this.offscreenUniverse = null;
		}
	}

	/**
	 * Adds listeners to home to update point of view from current camera.
	 */
	private void addCameraListeners(final View view,
									final TransformGroup viewPlatformTransform) {
		this.cameraChangeListener = new PropertyChangeListener() {
			private Runnable updater;
			public void propertyChange(PropertyChangeEvent ev) {
				if (this.updater == null) {
					// Update view transform later to avoid flickering in case of multiple camera changes
					EventQueue.invokeLater(this.updater = new Runnable () {
						public void run() {
							updateView(view, home.getCamera());
							updateViewPlatformTransform(viewPlatformTransform, home.getCamera(), true);
							updater = null;
						}
					});
				}
			}
		};
		this.home.getCamera().addPropertyChangeListener(this.cameraChangeListener);
		this.homeCameraListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				updateView(view, home.getCamera());
				updateViewPlatformTransform(viewPlatformTransform, home.getCamera(), false);
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
	private void updateView(View view, Camera camera) {
		if (this.canvas3D != null) {
				updateView(view, camera, this.canvas3D.getWidth(), this.canvas3D.getHeight());
		} else {
				updateView(view, camera, 0, 0);
		}
	}

  private void updateView(View view, Camera camera, int width, int height) {
		float fieldOfView = camera.getFieldOfView();
		if (fieldOfView == 0) {
			fieldOfView = (float) (Math.PI * 63 / 180);
		}
		view.setFieldOfView(fieldOfView);
		double frontClipDistance = 2.5f;
    	float frontBackDistanceRatio = 500000; // More than 10 km for a 2.5 cm front distance
		if (Component3DManager.getInstance().getDepthSize() <= 16) {
		  // It's recommended to keep ratio between back and front clip distances under 3000 for a 16 bit Z-buffer
		  frontBackDistanceRatio = 3000;
			BoundingBox approximateHomeBounds = getApproximateHomeBounds();
			// If camera is out of home bounds, adjust the front clip distance to the distance to home bounds
			if (approximateHomeBounds != null
					&& !approximateHomeBounds.intersect(new Point3d(camera.getX(), camera.getY(), camera.getZ()))) {
				float distanceToClosestBoxSide = getDistanceToBox(camera.getX(), camera.getY(), camera.getZ(), approximateHomeBounds);
				if (!Float.isNaN(distanceToClosestBoxSide)) {
					frontClipDistance = Math.max(frontClipDistance, 0.1f * distanceToClosestBoxSide);
				}
			}
		}
		if (camera.getZ() > 0 && width != 0 && height != 0) {
			float halfVerticalFieldOfView = (float)Math.atan(Math.tan(fieldOfView / 2) * height / width);
			float fieldOfViewBottomAngle = camera.getPitch() + halfVerticalFieldOfView;
			// If the horizon is above the frustrum bottom, take into account the distance to the ground
			if (fieldOfViewBottomAngle > 0) {
				float distanceToGroundAtFieldOfViewBottomAngle = (float)(camera.getZ() / Math.sin(fieldOfViewBottomAngle));
				frontClipDistance = Math.min(frontClipDistance, 0.35f * distanceToGroundAtFieldOfViewBottomAngle);
				if (frontClipDistance * frontBackDistanceRatio < distanceToGroundAtFieldOfViewBottomAngle) {
						// Ensure the ground is always visible at the back clip distance
						frontClipDistance = distanceToGroundAtFieldOfViewBottomAngle / frontBackDistanceRatio;
				}
			}
		}
		// Update front and back clip distance
		view.setFrontClipDistance(frontClipDistance);
    view.setBackClipDistance(frontClipDistance * frontBackDistanceRatio);
		clearPrintedImageCache();
	}

	/**
	 * Returns quickly computed bounds of the objects in home.
	 */
  private BoundingBox getApproximateHomeBounds() {
  	if (this.approximateHomeBoundsCache == null) {
			BoundingBox approximateHomeBounds = null;
			for (HomePieceOfFurniture piece : this.home.getFurniture()) {
				if (piece.isVisible()
						&& (piece.getLevel() == null
								|| piece.getLevel().isViewable())) {
					float halfMaxDimension = Math.max(piece.getWidthInPlan(), piece.getDepthInPlan()) / 2;
					float elevation = piece.getGroundElevation();
					Point3d pieceLocation = new Point3d(
						piece.getX() - halfMaxDimension, piece.getY() - halfMaxDimension, elevation);
					if (approximateHomeBounds == null) {
						approximateHomeBounds = new BoundingBox(pieceLocation, pieceLocation);
					} else {
						approximateHomeBounds.combine(pieceLocation);
					}
					approximateHomeBounds.combine(new Point3d(
						piece.getX() + halfMaxDimension, piece.getY() + halfMaxDimension, elevation + piece.getHeightInPlan()));
				}
			}
			for (Wall wall : this.home.getWalls()) {
				if (wall.getLevel() == null
						|| wall.getLevel().isViewable()) {
					Point3d startPoint = new Point3d(wall.getXStart(), wall.getYStart(),
						wall.getLevel() != null ? wall.getLevel().getElevation() : 0);
							if (approximateHomeBounds == null) {
					approximateHomeBounds = new BoundingBox(startPoint, startPoint);
							} else {
					approximateHomeBounds.combine(startPoint);
							}
					approximateHomeBounds.combine(new Point3d(wall.getXEnd(), wall.getYEnd(),
						startPoint.z + (wall.getHeight() != null ? wall.getHeight() : this.home.getWallHeight())));
				}
			}
			for (Room room : this.home.getRooms()) {
				if (room.getLevel() == null
						|| room.getLevel().isViewable()) {
					Point3d center = new Point3d(room.getXCenter(), room.getYCenter(),
						room.getLevel() != null ? room.getLevel().getElevation() : 0);
					if (approximateHomeBounds == null) {
						approximateHomeBounds = new BoundingBox(center, center);
					} else {
						approximateHomeBounds.combine(center);
					}
				}
			}
			for (Label label : this.home.getLabels()) {
				if ((label.getLevel() == null
						|| label.getLevel().isViewable())
						&& label.getPitch() != null) {
					Point3d center = new Point3d(label.getX(), label.getY(), label.getGroundElevation());
					if (approximateHomeBounds == null) {
						approximateHomeBounds = new BoundingBox(center, center);
					} else {
						approximateHomeBounds.combine(center);
					}
				}
			}
			this.approximateHomeBoundsCache = approximateHomeBounds;
		}
	  return this.approximateHomeBoundsCache;
	}

  /**
   * Returns the distance between the point at the given coordinates (x,y,z) and the closest side of <code>box</code>.
   */
  private float getDistanceToBox(float x, float y, float z, BoundingBox box) {
    Point3f point = new Point3f(x, y, z);
    Point3d lower = new Point3d();
    box.getLower(lower);
    Point3d upper = new Point3d();
    box.getUpper(upper);
    Point3f [] boxVertices = {
      new Point3f((float)lower.x, (float)lower.y, (float)lower.z),
      new Point3f((float)upper.x, (float)lower.y, (float)lower.z),
      new Point3f((float)lower.x, (float)upper.y, (float)lower.z),
      new Point3f((float)upper.x, (float)upper.y, (float)lower.z),
      new Point3f((float)lower.x, (float)lower.y, (float)upper.z),
      new Point3f((float)upper.x, (float)lower.y, (float)upper.z),
      new Point3f((float)lower.x, (float)upper.y, (float)upper.z),
      new Point3f((float)upper.x, (float)upper.y, (float)upper.z)};
    float [] distancesToVertex = new float [boxVertices.length];
    for (int i = 0; i < distancesToVertex.length; i++) {
      distancesToVertex [i] = point.distanceSquared(boxVertices [i]);
    }
    float [] distancesToSide = {
        getDistanceToSide(point, boxVertices, distancesToVertex, 0, 1, 3, 2, 2),
        getDistanceToSide(point, boxVertices, distancesToVertex, 0, 1, 5, 4, 1),
        getDistanceToSide(point, boxVertices, distancesToVertex, 0, 2, 6, 4, 0),
        getDistanceToSide(point, boxVertices, distancesToVertex, 4, 5, 7, 6, 2),
        getDistanceToSide(point, boxVertices, distancesToVertex, 2, 3, 7, 6, 1),
        getDistanceToSide(point, boxVertices, distancesToVertex, 1, 3, 7, 5, 0)};
    float distance = distancesToSide [0];
    for (int i = 1; i < distancesToSide.length; i++) {
      distance = Math.min(distance, distancesToSide [i]);
    }
    return distance;
  }

  /**
   * Returns the distance between the given <code>point</code> and the plane defined by four vertices.
   */
  private float getDistanceToSide(Point3f point, Point3f [] boxVertices, float [] distancesSquaredToVertex,
                                  int index1, int index2, int index3, int index4, int axis) {
    switch (axis) {
      case 0 : // Normal along x axis
        if (point.y <= boxVertices [index1].y) {
          if (point.z <= boxVertices [index1].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index1]);
          } else if (point.z >= boxVertices [index4].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index4]);
          } else {
            return getDistanceToLine(point, boxVertices [index1], boxVertices [index4]);
          }
        } else if (point.y >= boxVertices [index2].y) {
          if (point.z <= boxVertices [index2].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index2]);
          } else if (point.z >= boxVertices [index3].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index3]);
          } else {
            return getDistanceToLine(point, boxVertices [index2], boxVertices [index3]);
          }
        } else if (point.z <= boxVertices [index1].z) {
          return getDistanceToLine(point, boxVertices [index1], boxVertices [index2]);
        } else if (point.z >= boxVertices [index4].z) {
          return getDistanceToLine(point, boxVertices [index3], boxVertices [index4]);
        }
        break;
      case 1 : // Normal along y axis
        if (point.x <= boxVertices [index1].x) {
          if (point.z <= boxVertices [index1].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index1]);
          } else if (point.z >= boxVertices [index4].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index4]);
          } else {
            return getDistanceToLine(point, boxVertices [index1], boxVertices [index4]);
          }
        } else if (point.x >= boxVertices [index2].x) {
          if (point.z <= boxVertices [index2].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index2]);
          } else if (point.z >= boxVertices [index3].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index3]);
          } else {
            return getDistanceToLine(point, boxVertices [index2], boxVertices [index3]);
          }
        } else if (point.z <= boxVertices [index1].z) {
          return getDistanceToLine(point, boxVertices [index1], boxVertices [index2]);
        } else if (point.z >= boxVertices [index4].z) {
          return getDistanceToLine(point, boxVertices [index3], boxVertices [index4]);
        }
        break;
      case 2 : // Normal along z axis
        if (point.x <= boxVertices [index1].x) {
          if (point.y <= boxVertices [index1].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index1]);
          } else if (point.y >= boxVertices [index4].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index4]);
          } else {
            return getDistanceToLine(point, boxVertices [index1], boxVertices [index4]);
          }
        } else if (point.x >= boxVertices [index2].x) {
          if (point.y <= boxVertices [index2].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index2]);
          } else if (point.y >= boxVertices [index3].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index3]);
          } else {
            return getDistanceToLine(point, boxVertices [index2], boxVertices [index3]);
          }
        } else if (point.y <= boxVertices [index1].y) {
          return getDistanceToLine(point, boxVertices [index1], boxVertices [index2]);
        } else if (point.y >= boxVertices [index4].y) {
          return getDistanceToLine(point, boxVertices [index3], boxVertices [index4]);
        }
        break;
    }

    // Return distance to plane
    // from https://fr.wikipedia.org/wiki/Distance_d%27un_point_�_un_plan
    Vector3f vector1 = new Vector3f(boxVertices [index2].x - boxVertices [index1].x,
        boxVertices [index2].y - boxVertices [index1].y,
        boxVertices [index2].z - boxVertices [index1].z);
    Vector3f vector2 = new Vector3f(boxVertices [index3].x - boxVertices [index1].x,
        boxVertices [index3].y - boxVertices [index1].y,
        boxVertices [index3].z - boxVertices [index1].z);
    Vector3f normal = new Vector3f();
    normal.cross(vector1, vector2);
    return Math.abs(normal.dot(new Vector3f(boxVertices [index1].x - point.x, boxVertices [index1].y - point.y, boxVertices [index1].z - point.z))) /
        normal.length();
  }

  /**
   * Returns the distance between the given <code>point</code> and the line defined by two points.
   */
  private float getDistanceToLine(Point3f point, Point3f point1, Point3f point2) {
    // From https://fr.wikipedia.org/wiki/Distance_d%27un_point_�_une_droite#Dans_l.27espace
    Vector3f lineDirection = new Vector3f(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z);
    Vector3f vector = new Vector3f(point.x - point1.x, point.y - point1.y, point.z - point1.z);
    Vector3f crossProduct = new Vector3f();
    crossProduct.cross(lineDirection, vector);
    return crossProduct.length() / lineDirection.length();
  }

	/**
	 * Frees printed image kept in cache.
	 */
	private void clearPrintedImageCache() {
		//this.printedImageCache = null;
	}

	/**
	 * Updates <code>viewPlatformTransform</code> transform from <code>camera</code> angles and location.
	 */
	private void updateViewPlatformTransform(TransformGroup viewPlatformTransform,
																					 Camera camera, boolean updateWithAnimation) {
		// Get the camera interpolator
		CameraInterpolator cameraInterpolator =
						(CameraInterpolator) viewPlatformTransform.getChild(viewPlatformTransform.numChildren() - 1);
		if (updateWithAnimation) {
			cameraInterpolator.moveCamera(camera);
		} else {
			cameraInterpolator.stop();
			Transform3D transform = new Transform3D();
			updateViewPlatformTransform(transform, camera.getX(), camera.getY(),
					camera.getZ(), camera.getYaw(), camera.getPitch());
			viewPlatformTransform.setTransform(transform);
		}
		clearPrintedImageCache();
	}

	/**
	 * An interpolator that computes smooth camera moves.
	 */
	private class CameraInterpolator extends TransformInterpolator {
		private final ScheduledExecutorService scheduledExecutor;
		private Camera initialCamera;
		private Camera finalCamera;

		public static final float onepi = (float)Math.PI * 1f;
		public static final float twopi = (float)Math.PI * 2f;
		public static final float fivepi = (float)Math.PI * 5f;

		public CameraInterpolator(TransformGroup transformGroup) {
			this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			setTarget(transformGroup);
		}

		/**
		 * Moves the camera to a new location.
		 */
		public void moveCamera(Camera finalCamera) {
			if (this.finalCamera == null
					|| this.finalCamera.getX() != finalCamera.getX()
					|| this.finalCamera.getY() != finalCamera.getY()
					|| this.finalCamera.getZ() != finalCamera.getZ()
					|| this.finalCamera.getYaw() != finalCamera.getYaw()
					|| this.finalCamera.getPitch() != finalCamera.getPitch()) {
				synchronized (this) {
					//PJ note modulo twopi below to stop crazy spins as camera values are not wrapped
					Alpha alpha = getAlpha();
					if (alpha == null || alpha.finished()) {
						this.initialCamera = new Camera(camera.getX(), camera.getY(), camera.getZ(),
								camera.getYaw() % twopi, camera.getPitch(), camera.getFieldOfView());
					} else if (alpha.value() < 0.1) { // dropped from 0.3 to 0.1 to reduce final pop to resting place
						Transform3D finalTransformation = new Transform3D();
						// Jump directly to final location
						updateViewPlatformTransform(finalTransformation, this.finalCamera.getX(), this.finalCamera.getY(),
								this.finalCamera.getZ(), this.finalCamera.getYaw() % twopi, this.finalCamera.getPitch());
						getTarget().setTransform(finalTransformation);
						this.initialCamera = this.finalCamera;
					} else {
						// Compute initial location from current alpha value
						float shortest_angle = ((((this.finalCamera.getYaw() - this.initialCamera.getYaw()) % twopi) + fivepi) % twopi) - onepi;
						this.initialCamera = new Camera(this.initialCamera.getX() + (this.finalCamera.getX() - this.initialCamera.getX()) * alpha.value(),
								this.initialCamera.getY() + (this.finalCamera.getY() - this.initialCamera.getY()) * alpha.value(),
								this.initialCamera.getZ() + (this.finalCamera.getZ() - this.initialCamera.getZ()) * alpha.value(),
								this.initialCamera.getYaw() % twopi + (shortest_angle * alpha.value()),
								this.initialCamera.getPitch() + (this.finalCamera.getPitch() - this.initialCamera.getPitch()) * alpha.value(),
								finalCamera.getFieldOfView());
					}
					this.finalCamera = new Camera(finalCamera.getX(), finalCamera.getY(), finalCamera.getZ(),
							finalCamera.getYaw() % twopi, finalCamera.getPitch(), finalCamera.getFieldOfView());

					// Create an animation that will interpolate camera location
					// between initial camera and final camera in 150 ms
					if (alpha == null) {
            alpha = new Alpha(1, 150);
						setAlpha(alpha);
					}
					// Start animation now
					alpha.setStartTime(System.currentTimeMillis());
					// In case system is overloaded computeTransform won't be called
					// ensure final location will always be set after 150 ms
					this.scheduledExecutor.schedule(new Runnable() {
						public void run() {
							if (getAlpha().value() == 1) {
								Transform3D transform = new Transform3D();
								computeTransform(1, transform);
								getTarget().setTransform(transform);
							}
						}
						}, 150, TimeUnit.MILLISECONDS);
				}
			}
		}

		@Override
		public synchronized void computeTransform(float alpha, Transform3D transform) {
			float shortest_angle = ((((this.finalCamera.getYaw() - this.initialCamera.getYaw()) % twopi) + fivepi) % twopi) - onepi;
			updateViewPlatformTransform(transform,
					this.initialCamera.getX() + (this.finalCamera.getX() - this.initialCamera.getX()) * alpha,
					this.initialCamera.getY() + (this.finalCamera.getY() - this.initialCamera.getY()) * alpha,
					this.initialCamera.getZ() + (this.finalCamera.getZ() - this.initialCamera.getZ()) * alpha,
					this.initialCamera.getYaw() % ((float)Math.PI * 2f) + shortest_angle * alpha,
					this.initialCamera.getPitch() + (this.finalCamera.getPitch() - this.initialCamera.getPitch()) * alpha);
		}

		public synchronized void stop() {
			setAlpha(null);
			this.finalCamera = null;
		}
	}

	/**
	 * Updates <code>viewPlatformTransform</code> transform from camera angles and location.
	 */
	private void updateViewPlatformTransform(Transform3D transform,
											 float cameraX, float cameraY, float cameraZ,
											 float cameraYaw, float cameraPitch) {
		Transform3D yawRotation = new Transform3D();
		yawRotation.rotY(-cameraYaw + Math.PI);

		// reset if not affine
		if((yawRotation.getType() & Transform3D.AFFINE) == 0)
			yawRotation.setIdentity();

		Transform3D pitchRotation = new Transform3D();
		pitchRotation.rotX(-cameraPitch);
		// only multiply if affine
		if((pitchRotation.getType() & Transform3D.AFFINE) != 0)
			yawRotation.mul(pitchRotation);

		transform.setIdentity();
		transform.setTranslation(new Vector3f(cameraX, cameraZ, cameraY));

		//perhaps the translation is not affine now
		if((transform.getType() & Transform3D.AFFINE) == 0)
			transform.setIdentity();

		// add yawPitch which will be affine as checked above
		transform.mul(yawRotation);

		this.camera = new Camera(cameraX, cameraY, cameraZ, cameraYaw, cameraPitch, 0);
	}

	/**
	 * Adds AWT mouse listeners to <code>component3D</code> that calls back <code>controller</code> methods.
	 */
	private void addMouseListeners(final HomeController3D controller, final Canvas3D component3D) {
		// This has been moved to the GLWindow display method as the HomeComponent3D doesn't get a recreate
		// call on a resume, but needs these listeners re-added
		//this.getView().setOnTouchListener(new TouchyListener());

		if(getContext() != null){
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable(){
				public void run(){
					// possibly if an exit is called and a loadHome is still happening we can get here just as the universe collapses
					if(getContext() != null){
						mScaleDetector = new ScaleGestureDetector(HomeComponent3D.this.getContext(), new ScaleListener());
					}
				}
			});
		}
	}



	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		private final float mDPI;
		public ScaleListener() {
			mDPI = getView().getResources().getDisplayMetrics().densityDpi;
		}

		/**
		 * @param detector
		 * @return
		 */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			if(getView() != null) {
				///RIGHT RIGHT! is to allow 2 finger drags to work I see
				// so this is not about how far the scale move, just about how close the fingers are
				// when deciding if this is a 2 drag or not
				float measurement = mScaleDetector.getCurrentSpan() / mDPI;
				return measurement > PlanComponent.dpiMinSpanForZoom;
			}
			return false;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float yd = (mScaleDetector.getCurrentSpan() - mScaleDetector.getPreviousSpan());
			controller.moveCamera(yd);
			return true;
		}
	}

	private class TouchyListener implements android.view.View.OnTouchListener {
		private static final int INVALID_POINTER_ID = -1;

		// The ‘active pointer’ is the one currently moving our object.
		private int mActivePointerId = INVALID_POINTER_ID;
		// for single finger moves
		private float xLastMouseMove = -1;
		private float yLastMouseMove = -1;

		// for 2 fingers
		private float xLastMouseMove2 = -1;
		private float yLastMouseMove2 = -1;

		@Override
		public boolean onTouch(android.view.View v, MotionEvent ev) {




			// Let the ScaleGestureDetector inspect all events, it will do move camera if it likes
			if(mScaleDetector != null ) {
				mScaleDetector.onTouchEvent(ev);
				if (mScaleDetector.isInProgress())
					return true;
			}

			// let the selection handler have a go first, if it's working then stop there
			// note it has a tiny waver factor, it returns false on any down ever though it will use it later
			if(homeComponent3DMouseHandler != null && homeComponent3DMouseHandler.onTouch(v,  ev))
				return true;

			// for long hold I will only get a mouse down and nothing after it, so on a mouse down (before even the tapper)
			// I need to start a timer waiting for a mouse up, and after waiting for 600ms say
			// it should just start firing off move events

			// otherwise a single finger up/down left right are pitch/pan
			// a single finger long hold is forward
			// a 2+ finger drag is forward/backward straf left/right

			final int action = ev.getActionMasked();

			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					if( ev.getPointerCount() == 1 ) {
						this.xLastMouseMove = ev.getX();
						this.yLastMouseMove = ev.getY();
					}
					this.xLastMouseMove2 = -1;
					this.yLastMouseMove2 = -1;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;
				case MotionEvent.ACTION_MOVE:
					if (ev.getPointerCount() == 1) {
						if(this.xLastMouseMove != -1 && this.yLastMouseMove != -1) {
							final float ANGLE_FACTOR = 0.0025f;
							// Mouse move along X axis changes camera yaw
							float yawDelta = ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove);

							// inside camera is a slower turn rate
							float factor = home.getCamera() == home.getObserverCamera() ? -0.5f : 1f;
							yawDelta *= factor;
							controller.rotateCameraYaw(yawDelta);

							// Mouse move along Y axis changes camera pitch
							float pitchDelta = ANGLE_FACTOR * (ev.getY() - this.yLastMouseMove);
							final float PITCH_REDUCTION = 0.4f; // pitch is across 180 only, and is less "desirable"
							pitchDelta *= PITCH_REDUCTION;
							pitchDelta *= factor;
							controller.rotateCameraPitch(pitchDelta);

							// tell the tutorial
							Point2f data = new Point2f(yawDelta, pitchDelta);
							((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.CAMERA_MOVED_3D, data);
						}
						this.xLastMouseMove = ev.getX();
						this.yLastMouseMove = ev.getY();
					} else if (ev.getPointerCount() > 1) {
						if(this.xLastMouseMove2 != -1 && this.yLastMouseMove2 != -1) {
							final float STRAF_REDUCTION = 0.5f; // stafing is less "wanted"
							final float FACTOR = 0.5f;
							float xd = FACTOR * (ev.getX() - this.xLastMouseMove2);
							float yd = FACTOR * (ev.getY() - this.yLastMouseMove2);
							controller.moveCamera(yd);
							controller.moveCameraSideways(-xd*STRAF_REDUCTION);//note does nothing in overhead view
						}

						this.xLastMouseMove2 = ev.getX();
						this.yLastMouseMove2 = ev.getY();
					}
					break;
				case MotionEvent.ACTION_UP: // fall through!
				case MotionEvent.ACTION_CANCEL:
					mActivePointerId = INVALID_POINTER_ID;

					this.xLastMouseMove = -1;
					this.yLastMouseMove = -1;

					this.xLastMouseMove2 = -1;
					this.yLastMouseMove2 = -1;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					//second finger has been released
					final int pointerIndex = ev.getActionIndex();
					final int pointerId = ev.getPointerId(pointerIndex);

					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						this.xLastMouseMove2 = -1;
						this.yLastMouseMove2 = -1;
						mActivePointerId = ev.getPointerId(newPointerIndex);
					}

					// reduce the jitter when leaving 2 finger mode
					this.xLastMouseMove = -1;
					this.yLastMouseMove = -1;

					break;
			}

			return true;
		}
	}

	//PJ retargetMouseEventToNavigationPanelChildren removed, but comment left in to assit code comparision
	/**
	 * Retargets to the first component of navigation panel able to manage the given event
	 * and returns <code>true</code> if a component consumed the event
	 * or needs to be repainted (meaning its state changed).
	 * This implementation doesn't cover all the possible cases (mouseEntered and mouseExited
	 * events are managed only during mouseDragged event).
	 */

	/**
	 * Installs keys bound to actions.
	 */
	private void installKeyboardActions() {
	}

	/**
	 * Creates actions that calls back <code>controller</code> methods.
	 */
	private void createActions(final HomeController3D controller) {
	}

	/**
	 * Returns the closest {@link Selectable} object at component coordinates (x, y),
	 * or <code>null</code> if not found.
	 */
	public Selectable getClosestItemAt(int x, int y) {
   /* if (this.component3D != null) {
      Canvas3D canvas;
      if (this.component3D instanceof JCanvas3D) {
        canvas = ((JCanvas3D)this.component3D).getOffscreenCanvas3D();
      } else {
        canvas = (Canvas3D)this.component3D;
      }
      PickCanvas pickCanvas = new PickCanvas(canvas, this.onscreenUniverse.getLocale());
      pickCanvas.setMode(PickCanvas.GEOMETRY);
      Point canvasPoint = SwingUtilities.convertPoint(this, x, y, this.component3D);
      pickCanvas.setShapeLocation(canvasPoint.x, canvasPoint.y);
      PickResult result = pickCanvas.pickClosest();
      if (result != null) {
        Node pickedNode = result.getNode(PickResult.SHAPE3D);
        while (!this.homeObjects.containsValue(pickedNode)
               && pickedNode.getParent() != null) {
          pickedNode = pickedNode.getParent();
        }
        if (pickedNode != null) {
          for (Map.Entry<Selectable, Object3DBranch> entry : this.homeObjects.entrySet()) {
            if (entry.getValue() == pickedNode) {
              return entry.getKey();
            }
          }
        }
      }
    }*/
		return null;
	}

	/**
	 * Returns a new scene tree root.
	 */
	private BranchGroup createSceneTree(boolean displayShadowOnFloor,
																			boolean listenToHomeUpdates,
																			boolean waitForLoading) {
		BranchGroup root = new BranchGroup();
		root.setName("Universe Root");
		root.setPickable(true);

		// Build scene tree
		root.addChild(createHomeTree(displayShadowOnFloor, listenToHomeUpdates, waitForLoading));
		Node backgroundNode = createBackgroundNode(listenToHomeUpdates, waitForLoading);
		root.addChild(backgroundNode);
		//PJ a 100km seems big enough and reduces rounding issues on small devices
		Node groundNode = createGroundNode(-0.5E5f, -0.5E5f, 1E5f, 1E5f, listenToHomeUpdates, waitForLoading);
		root.addChild(groundNode);

    this.sceneLights = createLights(groundNode, listenToHomeUpdates);
    for (Light light : this.sceneLights) {
			root.addChild(light);
		}
		//PJ called compile manually, and left a debug output call ready
		//root.outputTraversal();
		root.compile();
		return root;
	}

	/**
	 * Returns a new background node.
	 */
	private Node createBackgroundNode(boolean listenToHomeUpdates, final boolean waitForLoading) {
		final SimpleShaderAppearance skyBackgroundAppearance = new SimpleShaderAppearance();
		ColoringAttributes skyBackgroundColoringAttributes = new ColoringAttributes();
		skyBackgroundAppearance.setColoringAttributes(skyBackgroundColoringAttributes);
		TextureAttributes skyBackgroundTextureAttributes = new TextureAttributes();
		skyBackgroundAppearance.setTextureAttributes(skyBackgroundTextureAttributes);
		// Allow sky color and texture to change
		skyBackgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		skyBackgroundAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		skyBackgroundColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		skyBackgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
		skyBackgroundTextureAttributes.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);
		skyBackgroundAppearance.setUpdatableCapabilities();//PJ allow updatable shader building
		Geometry topHalfSphereGeometry = createHalfSphereGeometry(true);
		final Shape3D topHalfSphere = new Shape3D(topHalfSphereGeometry, skyBackgroundAppearance);
		BranchGroup backgroundBranch = new BranchGroup();
		backgroundBranch.addChild(topHalfSphere);

		final SimpleShaderAppearance bottomAppearance = new SimpleShaderAppearance();
		final RenderingAttributes bottomRenderingAttributes = new RenderingAttributes();
		bottomRenderingAttributes.setVisible(false);
		bottomAppearance.setRenderingAttributes(bottomRenderingAttributes);
		bottomRenderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		bottomAppearance.setUpdatableCapabilities();//PJ allow updatable shader building
		Shape3D bottomHalfSphere = new Shape3D(createHalfSphereGeometry(false), bottomAppearance);
		backgroundBranch.addChild(bottomHalfSphere);

		// Add two planes at ground level to complete landscape at the horizon when camera is above horizon
		// (one at y = -0.01 to fill the horizon and a lower one to fill the lower part of the scene)
		final SimpleShaderAppearance groundBackgroundAppearance = new SimpleShaderAppearance();
		TextureAttributes groundBackgroundTextureAttributes = new TextureAttributes();
		groundBackgroundTextureAttributes.setTextureMode(TextureAttributes.MODULATE);
		groundBackgroundAppearance.setTextureAttributes(groundBackgroundTextureAttributes);
		groundBackgroundAppearance.setTexCoordGeneration(
			new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2,
				new Vector4f(1E5f, 0, 0, 0), new Vector4f(0, 0, 1E5f, 0)));
		final RenderingAttributes groundRenderingAttributes = new RenderingAttributes();
		groundBackgroundAppearance.setRenderingAttributes(groundRenderingAttributes);
		// Allow ground color and texture to change
		groundBackgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		groundBackgroundAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		groundRenderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		groundBackgroundAppearance.setUpdatableCapabilities();//PJ allow updatable shader building

    GeometryInfo geometryInfo = new GeometryInfo (GeometryInfo.QUAD_ARRAY);
		geometryInfo.setCoordinates(new Point3f [] {
						new Point3f(-1f, -0.01f, -1f),
						new Point3f(-1f, -0.01f, 1f),
						new Point3f(1f, -0.01f, 1f),
						new Point3f(1f, -0.01f, -1f),
						new Point3f(-1f, -0.1f, -1f),
						new Point3f(-1f, -0.1f, 1f),
						new Point3f(1f, -0.1f, 1f),
						new Point3f(1f, -0.1f, -1f)});
		geometryInfo.setCoordinateIndices(new int [] {0, 1, 2, 3, 4, 5, 6, 7});
		geometryInfo.setNormals(new Vector3f [] {new Vector3f(0, 1, 0)});
		geometryInfo.setNormalIndices(new int [] {0, 0, 0, 0, 0, 0, 0, 0});

		//PJ quads not supported and a better getgeometry call
		geometryInfo.convertToIndexedTriangles();
		Shape3D groundBackground = new Shape3D(geometryInfo.getIndexedGeometryArray(true,true,true,true,true), groundBackgroundAppearance);
		backgroundBranch.addChild(groundBackground);

		// Add its own lights to background to ensure they have an effect
		for (Light light : createBackgroundLights(listenToHomeUpdates)) {
		  backgroundBranch.addChild(light);
		}

		final Background background = new Background(backgroundBranch);
    	updateBackgroundColorAndTexture(skyBackgroundAppearance, groundBackgroundAppearance, this.home, waitForLoading);
		background.setImageScaleMode(Background.SCALE_FIT_ALL);
		//PJPJ used an isInfinite version
		background.setApplicationBounds(new BoundingSphere(new Point3d(0,0,0), Double.POSITIVE_INFINITY));

		if (listenToHomeUpdates) {
			// Add a listener on sky color and texture properties change
		  this.backgroundChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				updateBackgroundColorAndTexture(skyBackgroundAppearance, groundBackgroundAppearance, home, waitForLoading);
			  }
			};
		  this.home.getEnvironment().addPropertyChangeListener(
			  HomeEnvironment.Property.SKY_COLOR, this.backgroundChangeListener);
		  this.home.getEnvironment().addPropertyChangeListener(
			  HomeEnvironment.Property.SKY_TEXTURE, this.backgroundChangeListener);
		  this.home.getEnvironment().addPropertyChangeListener(
			  HomeEnvironment.Property.GROUND_COLOR, this.backgroundChangeListener);
		  this.home.getEnvironment().addPropertyChangeListener(
			  HomeEnvironment.Property.GROUND_TEXTURE, this.backgroundChangeListener);
		  // Make groundBackground invisible and bottom half sphere visible if camera is below the ground
		  this.elevationChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				if (ev.getSource() == home) {
				  // Move listener to the new camera
				  ((Camera)ev.getOldValue()).removePropertyChangeListener(this);
				  home.getCamera().addPropertyChangeListener(this);
				}
				if (ev.getSource() == home
					|| Camera.Property.Z.name().equals(ev.getPropertyName())) {
				  groundRenderingAttributes.setVisible(home.getCamera().getZ() >= 0);
				  bottomRenderingAttributes.setVisible(home.getCamera().getZ() < 0);
				}
					}
				};
		  this.home.getCamera().addPropertyChangeListener(this.elevationChangeListener);
		  this.home.addPropertyChangeListener(Home.Property.CAMERA, this.elevationChangeListener);
		}
		return background;
	}

	/**
	 * Returns a half sphere oriented inward and with texture ordinates
	 * that spread along an hemisphere.
	 */
	private Geometry createHalfSphereGeometry(boolean top) {
		final int divisionCount = 48;
		Point3f[] coords = new Point3f[divisionCount * divisionCount];
		TexCoord2f[] textureCoords = top ? new TexCoord2f[divisionCount * divisionCount] : null;
		Color3f[] colors = top ? null : new Color3f[divisionCount * divisionCount];
		for (int i = 0, k = 0; i < divisionCount; i++) {
			double alpha = i * 2 * Math.PI / divisionCount;
			float cosAlpha = (float) Math.cos(alpha);
			float sinAlpha = (float) Math.sin(alpha);
			double nextAlpha = (i + 1) * 2 * Math.PI / divisionCount;
			float cosNextAlpha = (float) Math.cos(nextAlpha);
			float sinNextAlpha = (float) Math.sin(nextAlpha);
      for (int j = 0, max = divisionCount / 4; j < max; j++) {
				double beta = 2 * j * Math.PI / divisionCount;
				float cosBeta = (float) Math.cos(beta);
				float sinBeta = (float) Math.sin(beta);
				// Correct the bottom of the hemisphere to avoid seeing a bottom hemisphere at the horizon
				float y = j != 0 ? (top ? sinBeta : -sinBeta) : -0.01f;
				double nextBeta = 2 * (j + 1) * Math.PI / divisionCount;
				if (!top) {
					nextBeta = -nextBeta;
				}
				float cosNextBeta = (float) Math.cos(nextBeta);
				float sinNextBeta = (float) Math.sin(nextBeta);
				if (top) {
					coords[k] = new Point3f(cosAlpha * cosBeta, y, sinAlpha * cosBeta);
          textureCoords [k++] = new TexCoord2f((float)i / divisionCount, (float)j / max);

					coords[k] = new Point3f(cosNextAlpha * cosBeta, y, sinNextAlpha * cosBeta);
          textureCoords [k++] = new TexCoord2f((float)(i + 1) / divisionCount, (float)j / max);

					coords[k] = new Point3f(cosNextAlpha * cosNextBeta, sinNextBeta, sinNextAlpha * cosNextBeta);
          textureCoords [k++] = new TexCoord2f((float)(i + 1) / divisionCount, (float)(j + 1) / max);

					coords[k] = new Point3f(cosAlpha * cosNextBeta, sinNextBeta, sinAlpha * cosNextBeta);
          textureCoords [k++] = new TexCoord2f((float)i / divisionCount, (float)(j + 1) / max);
				} else {
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
		if (textureCoords != null) {
			geometryInfo.setTextureCoordinateParams(1, 2);
			geometryInfo.setTextureCoordinates(0, textureCoords);
		}
		if (colors != null) {
			geometryInfo.setColors(colors);
		}

		//PJPJPJPJ quads not supported, better get call
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
  private void updateBackgroundColorAndTexture(final Appearance skyBackgroundAppearance,
                                               final Appearance groundBackgroundAppearance,
																							 Home home,
																							 boolean waitForLoading) {
		Color c = new Color(home.getEnvironment().getSkyColor());
		Color3f skyColor = new Color3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
	  	skyBackgroundAppearance.getColoringAttributes().setColor(skyColor);
		HomeTexture skyTexture = home.getEnvironment().getSkyTexture();
		if (skyTexture != null) {
      final Transform3D transform = new Transform3D();
      transform.setTranslation(new Vector3f(-skyTexture.getXOffset(), 0, 0));
			TextureManager textureManager = TextureManager.getInstance();
			if (waitForLoading) {
				// Don't share the background texture otherwise if might not be rendered correctly
				skyBackgroundAppearance.setTexture(textureManager.loadTexture(skyTexture.getImage()));
        skyBackgroundAppearance.getTextureAttributes().setTextureTransform(transform);
			} else {
				textureManager.loadTexture(skyTexture.getImage(), waitForLoading,
				  new TextureManager.TextureObserver() {
					  public void textureUpdated(Texture texture) {
						  // Use a copy of the texture in case it's used in an other universe
						  skyBackgroundAppearance.setTexture((Texture) texture.cloneNodeComponent(false));
              skyBackgroundAppearance.getTextureAttributes().setTextureTransform(transform);
					  }
				  });
			}
		} else {
			skyBackgroundAppearance.setTexture(null);
		}

		HomeTexture groundTexture = home.getEnvironment().getGroundTexture();
		if (groundTexture != null) {
		  groundBackgroundAppearance.setMaterial(new Material(
			  new Color3f(1, 1, 1), new Color3f(), new Color3f(1, 1, 1), new Color3f(0, 0, 0), 1));
		  TextureManager textureManager = TextureManager.getInstance();
		  if (waitForLoading) {
			groundBackgroundAppearance.setTexture(textureManager.loadTexture(groundTexture.getImage()));
		  } else {
			textureManager.loadTexture(groundTexture.getImage(), waitForLoading,
				new TextureManager.TextureObserver() {
					public void textureUpdated(Texture texture) {
					  // Use a copy of the texture in case it's used in an other universe
					  groundBackgroundAppearance.setTexture((Texture)texture.cloneNodeComponent(false));
					}
				});
		  }
		} else {
		  int groundColor = home.getEnvironment().getGroundColor();
		  Color3f color = new Color3f(((groundColor >>> 16) & 0xFF) / 255.f,
									  ((groundColor >>> 8) & 0xFF) / 255.f,
									   (groundColor & 0xFF) / 255.f);
		  groundBackgroundAppearance.setMaterial(new Material(color, new Color3f(), color, new Color3f(0, 0, 0), 1));
		  groundBackgroundAppearance.setTexture(null);
		}

		clearPrintedImageCache();
	}

	/**
	 * Returns a new ground node.
	 */
	private Node createGroundNode(final float groundOriginX,
																final float groundOriginY,
																final float groundWidth,
																final float groundDepth,
																boolean listenToHomeUpdates,
																boolean waitForLoading) {
		final Ground3D ground3D = new Ground3D(this.home,
				groundOriginX, groundOriginY, groundWidth, groundDepth, waitForLoading);
		Transform3D translation = new Transform3D();
		translation.setTranslation(new Vector3f(0, -0.2f, 0));
		TransformGroup transformGroup = new TransformGroup(translation);
		transformGroup.addChild(ground3D);

		if (listenToHomeUpdates) {
			// Add a listener on ground color and texture properties change
			this.groundChangeListener = new PropertyChangeListener() {
				private Runnable updater;
				public void propertyChange(PropertyChangeEvent ev) {
					if (this.updater == null) {
						// Group updates
						EventQueue.invokeLater(this.updater = new Runnable() {
							public void run() {
								ground3D.update();
								updater = null;
							}
						});
					}
					clearPrintedImageCache();
				}
			};
			HomeEnvironment homeEnvironment = this.home.getEnvironment();
			homeEnvironment.addPropertyChangeListener(
					HomeEnvironment.Property.GROUND_COLOR, this.groundChangeListener);
			homeEnvironment.addPropertyChangeListener(
					HomeEnvironment.Property.GROUND_TEXTURE, this.groundChangeListener);
      homeEnvironment.addPropertyChangeListener(
          HomeEnvironment.Property.BACKGROUND_IMAGE_VISIBLE_ON_GROUND_3D, this.groundChangeListener);
      this.home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, this.groundChangeListener);
		}

		return transformGroup;
	}

  /**
   * Returns the lights used for the background.
   */
  private Light [] createBackgroundLights(boolean listenToHomeUpdates) {
    final Light [] lights = {
        // Use just one direct light for background because only one horizontal plane is under light
        new DirectionalLight(new Color3f(1.435f, 1.435f, 1.435f), new Vector3f(0f, -1f, 0f)),
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
    for (int i = 0; i < lights.length - 1; i++) {
      // Allow directional lights color and influencing bounds to change
      lights [i].setCapability(DirectionalLight.ALLOW_COLOR_WRITE);
      // Store default color in user data
      Color3f defaultColor = new Color3f();
      lights [i].getColor(defaultColor);
      lights [i].setUserData(defaultColor);
      updateLightColor(lights [i]);
    }

    final Bounds defaultInfluencingBounds = new BoundingSphere(new Point3d(), 2);
    for (Light light : lights) {
      light.setInfluencingBounds(defaultInfluencingBounds);
    }

    if (listenToHomeUpdates) {
      // Add a listener on light color property change to home
      this.backgroundLightColorListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateLightColor(lights [0]);
          }
        };
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.LIGHT_COLOR, this.backgroundLightColorListener);
    }

    return lights;
  }

	/**
	 * Returns the lights of the scene.
	 */
	private Light[] createLights(final Node groundNode, boolean listenToHomeUpdates) {
		final Light[] lights = {
				new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(1.5f, -0.8f, -1)),
				new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(-1.5f, -0.8f, -1)),
				new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(0, -0.8f, 1)),
				new DirectionalLight(new Color3f(0.7f, 0.7f, 0.7f), new Vector3f(0, 1f, 0)),
				new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
		for (int i = 0; i < lights.length - 1; i++) {
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
		for (Light light : lights) {
			light.setInfluencingBounds(defaultInfluencingBounds);
		}

		if (listenToHomeUpdates) {
			// Add a listener on light color property change to home
			this.lightColorListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent ev) {
					for (int i = 0; i < lights.length - 1; i++) {
						updateLightColor(lights[i]);
					}
					updateObjects(getHomeObjects(HomeLight.class));
				}
			};
			this.home.getEnvironment().addPropertyChangeListener(
					HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);

			// Add a listener on subpart size property change to home
			this.subpartSizeListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent ev) {
					if (ev != null) {
						// Update 3D objects if not at initialization
						Collection<Selectable> homeItems = new ArrayList<Selectable>(home.getWalls());
						homeItems.addAll(home.getRooms());
						homeItems.addAll(getHomeObjects(HomeLight.class));
						updateObjects(homeItems);
						clearPrintedImageCache();
					}

					// Update default lights scope
					List<Group> scope = null;
					if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
						Area lightScopeOutsideWallsArea = getLightScopeOutsideWallsArea();
						scope = new ArrayList<Group>();
						for (Wall wall : home.getWalls()) {
							Object3DBranch wall3D = homeObjects.get(wall);
							if (wall3D instanceof Wall3D) {
								// Add left and/or right side of the wall to scope
								float[][] points = wall.getPoints();
								if (!lightScopeOutsideWallsArea.contains(points[0][0], points[0][1])) {
									scope.add((Group) wall3D.getChild(1));
								}
								if (!lightScopeOutsideWallsArea.contains(points[points.length - 1][0], points[points.length - 1][1])) {
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
						for (Selectable item : otherItems) {
							// Add item to scope if one of its points don't belong to lightScopeWallsArea
							for (float[] point : item.getPoints()) {
								if (!lightScopeOutsideWallsArea.contains(point[0], point[1])) {
									Group object3D = homeObjects.get(item);
									if (object3D instanceof HomePieceOfFurniture3D) {
										// Add the direct parent of the shape that will be added once loaded
										// otherwise scope won't be updated automatically
										object3D = (Group) object3D.getChild(0);
									}
									scope.add(object3D);
									break;
								}
							}
						}
					} else {
						lightScopeOutsideWallsAreaCache = null;
					}

					for (Light light : lights) {
						if (light instanceof DirectionalLight) {
							light.removeAllScopes();
							if (scope != null) {
								light.addScope((Group) groundNode);
								for (Group group : scope) {
									light.addScope(group);
								}
							}
						}
					}
				}
			};

			this.home.getEnvironment().addPropertyChangeListener(
					HomeEnvironment.Property.SUBPART_SIZE_UNDER_LIGHT,this.subpartSizeListener);
			this.subpartSizeListener.propertyChange(null);
		}

		return lights;
	}

	/**
	 * Returns the home objects displayed by this component of the given class.
	 */
	private <T> List<T> getHomeObjects(Class<T> objectClass) {
		return Home.getSubList(new ArrayList<Selectable>(homeObjects.keySet()), objectClass);
	}

	/**
	 * Updates<code>light</code> color from <code>home</code> light color.
	 */
	private void updateLightColor(Light light) {
		Color3f defaultColor = (Color3f) light.getUserData();
		int lightColor = this.home.getEnvironment().getLightColor();
		light.setColor(new Color3f(((lightColor >>> 16) & 0xFF) / 255f * defaultColor.x,
									((lightColor >>> 8) & 0xFF) / 255f * defaultColor.y,
											(lightColor & 0xFF) / 255f * defaultColor.z));
		clearPrintedImageCache();
	}

	/**
	 * Returns walls area used for light scope outside.
	 */
	private Area getLightScopeOutsideWallsArea() {
		if (this.lightScopeOutsideWallsAreaCache == null) {
			// Compute a smaller area surrounding all walls at all levels
			Area wallsPath = new Area();
      for (Wall wall : this.home.getWalls()) {
				Wall thinnerWall = wall.clone();
				thinnerWall.setThickness(Math.max(thinnerWall.getThickness() - 0.1f, 0.08f));
				wallsPath.add(new Area(getShape(thinnerWall.getPoints())));
			}
			Area lightScopeOutsideWallsArea = new Area();
			List<float[]> points = new ArrayList<float[]>();
			for (PathIterator it = wallsPath.getPathIterator(null, 1); !it.isDone(); it.next()) {
				float[] point = new float[2];
				switch (it.currentSegment(point)) {
					case PathIterator.SEG_MOVETO:
					case PathIterator.SEG_LINETO:
						points.add(point);
						break;
					case PathIterator.SEG_CLOSE:
						if (points.size() > 2) {
							float[][] pointsArray = points.toArray(new float[points.size()][]);
							if (new Room(pointsArray).isClockwise()) {
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
	private Node createHomeTree(boolean displayShadowOnFloor,
								boolean listenToHomeUpdates,
								boolean waitForLoading) {
		Group homeRoot = createHomeRoot();
		// Add walls, pieces, rooms, polylines and labels already available
		for (Label label : this.home.getLabels()) {
			addObject(homeRoot, label, listenToHomeUpdates, waitForLoading);
		}
		for (Polyline polyline : this.home.getPolylines()) {
			addObject(homeRoot, polyline, listenToHomeUpdates, waitForLoading);
		}
		for (Room room : this.home.getRooms()) {
			addObject(homeRoot, room, listenToHomeUpdates, waitForLoading);
		}
		for (Wall wall : this.home.getWalls()) {
			addObject(homeRoot, wall, listenToHomeUpdates, waitForLoading);
		}
		Map<HomePieceOfFurniture, Node> pieces3D = new HashMap<HomePieceOfFurniture, Node>();
		for (HomePieceOfFurniture piece : this.home.getFurniture()) {
			if (piece instanceof HomeFurnitureGroup) {
				for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup) piece).getAllFurniture()) {
					if (!(childPiece instanceof HomeFurnitureGroup)) {
						pieces3D.put(childPiece, addObject(homeRoot, childPiece, listenToHomeUpdates, waitForLoading));
					}
				}
			} else {
				pieces3D.put(piece, addObject(homeRoot, piece, listenToHomeUpdates, waitForLoading));
			}
		}

		if (displayShadowOnFloor) {
			addShadowOnFloor(homeRoot, pieces3D);
		}

		if (listenToHomeUpdates) {
			// Add level, wall, furniture, room listeners to home for further update
			addLevelListener(homeRoot);
			addWallListener(homeRoot);
			addFurnitureListener(homeRoot);
			addRoomListener(homeRoot);
			addPolylineListener(homeRoot);
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
	private Group createHomeRoot() {
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
	private void addLevelListener(final Group group) {
		this.levelChangeListener = new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent ev) {
        	if (Level.Property.VISIBLE.name().equals(ev.getPropertyName())
						|| Level.Property.VIEWABLE.name().equals(ev.getPropertyName())) {
          	Set<Selectable> objects = homeObjects.keySet();
          	ArrayList<Selectable> updatedItems = new ArrayList<Selectable>(objects.size());
          	for (Selectable item : objects) {
            	if (item instanceof Room // 3D rooms depend on rooms at other levels
                  || !(item instanceof Elevatable)
                  || ((Elevatable)item).isAtLevel((Level)ev.getSource())) {
                	updatedItems.add(item);
            	}
          	}
          	updateObjects(updatedItems);
          	groundChangeListener.propertyChange(null);
        	} else if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())) {
				updateObjects(homeObjects.keySet());
				groundChangeListener.propertyChange(null);
        	} else if (Level.Property.BACKGROUND_IMAGE.name().equals(ev.getPropertyName())) {
          		groundChangeListener.propertyChange(null);
			} else if (Level.Property.FLOOR_THICKNESS.name().equals(ev.getPropertyName())) {
        		updateObjects(home.getWalls());
        		updateObjects(home.getRooms());
        	} else if (Level.Property.HEIGHT.name().equals(ev.getPropertyName())) {
        		updateObjects(home.getRooms());
        	}
		  }
		};
		for (Level level : this.home.getLevels()) {
			level.addPropertyChangeListener(this.levelChangeListener);
		}
		this.levelListener = new CollectionListener<Level>() {
			public void collectionChanged(CollectionEvent<Level> ev) {
				Level level = ev.getItem();
				switch (ev.getType()) {
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
	private void addWallListener(final Group group) {

		// I want need a single
		//updateObjects(home.getRooms());
		// and
		//updateObjects(home.getWalls());
		// fired on 3d made visible!
		this.wallChangeListener = new PropertyChangeListener() {
			private ArrayList<Wall> wallsNeedingUpdate = new ArrayList<Wall>();
			public void propertyChange(PropertyChangeEvent ev) {
				String propertyName = ev.getPropertyName();
				if( RUN_UPDATES.equals(propertyName)) {
					for( Wall updatedWall: wallsNeedingUpdate ) {
						updateWall(updatedWall);
					}
					wallsNeedingUpdate.clear();

					// all uses of this removed and called one time here
					if(fullRoomUpdateRequired) {
						updateObjects(home.getRooms());
						fullRoomUpdateRequired = false;

					}
					if(fullWallUpdateRequired) {
						updateObjects(home.getWalls());
						fullRoomUpdateRequired = false;
					}
				} else if (!Wall.Property.PATTERN.name().equals(propertyName)) {
					Wall updatedWall = (Wall) ev.getSource();
					//PJ updating walls is crazy expensive, lots of geometry create and normal creates (too many some might say)
					// so if we aren't displaying 3d then defer update until then and just take the last one for each wall
					if(HomeComponent3D.this.getUserVisibleHint()) {
						updateWall(updatedWall);
					} else {
						if(!wallsNeedingUpdate.contains(updatedWall))
							wallsNeedingUpdate.add(updatedWall);
					}

					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					if (updatedWall.getLevel() != null && updatedWall.getLevel().getElevation() < 0) {
						groundChangeListener.propertyChange(null);
					}
					if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
						if (Wall.Property.X_START.name().equals(propertyName)
								|| Wall.Property.Y_START.name().equals(propertyName)
								|| Wall.Property.X_END.name().equals(propertyName)
								|| Wall.Property.Y_END.name().equals(propertyName)
								|| Wall.Property.ARC_EXTENT.name().equals(propertyName)
								|| Wall.Property.THICKNESS.name().equals(propertyName)) {
							lightScopeOutsideWallsAreaCache = null;
							updateObjectsLightScope(null);
						}
					}
				}
			}
		};
		for (Wall wall : this.home.getWalls()) {
			wall.addPropertyChangeListener(this.wallChangeListener);
		}
		this.wallListener = new CollectionListener<Wall>() {
			public void collectionChanged(CollectionEvent<Wall> ev) {
				Wall wall = ev.getItem();
				switch (ev.getType()) {
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
	private void addFurnitureListener(final Group group) {
		this.furnitureChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				HomePieceOfFurniture updatedPiece = (HomePieceOfFurniture) ev.getSource();

				//tell the tutorial about updates
				((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.FURNITURE_UPDATED, updatedPiece);

				String propertyName = ev.getPropertyName();
				if (HomePieceOfFurniture.Property.X.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.Y.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.ANGLE.name().equals(propertyName)
            			|| HomePieceOfFurniture.Property.ROLL.name().equals(propertyName)
            			|| HomePieceOfFurniture.Property.PITCH.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.WIDTH.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.DEPTH.name().equals(propertyName)) {
					updatePieceOfFurnitureGeometry(updatedPiece, propertyName, (Float)ev.getOldValue());
					updateObjectsLightScope(Arrays.asList(new HomePieceOfFurniture[]{updatedPiece}));
				} else if (HomePieceOfFurniture.Property.HEIGHT.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.ELEVATION.name().equals(propertyName)
              			|| HomePieceOfFurniture.Property.MODEL.name().equals(propertyName)
              			|| HomePieceOfFurniture.Property.MODEL_ROTATION.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.MODEL_MIRRORED.name().equals(propertyName)
              			|| HomePieceOfFurniture.Property.BACK_FACE_SHOWN.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.MODEL_TRANSFORMATIONS.name().equals(propertyName)
             			|| HomePieceOfFurniture.Property.STAIRCASE_CUT_OUT_SHAPE.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.VISIBLE.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.LEVEL.name().equals(propertyName)) {
          			updatePieceOfFurnitureGeometry(updatedPiece, null, null);
				  } else if (HomeDoorOrWindow.Property.CUT_OUT_SHAPE.name().equals(propertyName)
					  || HomeDoorOrWindow.Property.WALL_CUT_OUT_ON_BOTH_SIDES.name().equals(propertyName)
					  || HomeDoorOrWindow.Property.WALL_WIDTH.name().equals(propertyName)
					  || HomeDoorOrWindow.Property.WALL_LEFT.name().equals(propertyName)
					  || HomeDoorOrWindow.Property.WALL_HEIGHT.name().equals(propertyName)
					  || HomeDoorOrWindow.Property.WALL_TOP.name().equals(propertyName)) {
					if (containsDoorsAndWindows(updatedPiece)) {
						// deferred to visible see RUN_UPDATES updateIntersectingWalls(updatedPiece);
						fullWallUpdateRequired = true;
					}
				} else if (HomePieceOfFurniture.Property.COLOR.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.TEXTURE.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.MODEL_MATERIALS.name().equals(propertyName)
						|| HomePieceOfFurniture.Property.SHININESS.name().equals(propertyName)
						|| (HomeLight.Property.POWER.name().equals(propertyName)
						&& home.getEnvironment().getSubpartSizeUnderLight() > 0)) {
					updateObjects(Arrays.asList(new HomePieceOfFurniture[]{updatedPiece}));
				}
			}

			private void updatePieceOfFurnitureGeometry(HomePieceOfFurniture piece, String propertyName, Float oldValue) {
				updateObjects(Arrays.asList(new HomePieceOfFurniture[]{piece}));
				if (containsDoorsAndWindows(piece)) {
					// deferred to visible see RUN_UPDATES updateObjects(home.getWalls());
					fullWallUpdateRequired = true;
				} else if (containsStaircases(piece)) {
					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					fullRoomUpdateRequired = true;
				}
				if (piece.getLevel() != null && piece.getLevel().getElevation() < 0) {
					groundChangeListener.propertyChange(null);
				}
			}
		};
		for (HomePieceOfFurniture piece : this.home.getFurniture()) {
			addPropertyChangeListener(piece, this.furnitureChangeListener);
		}
		this.furnitureListener = new CollectionListener<HomePieceOfFurniture>() {
			public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
				HomePieceOfFurniture piece = (HomePieceOfFurniture) ev.getItem();
				switch (ev.getType()) {
					case ADD:
						addPieceOfFurniture(group, piece, true, false);
						addPropertyChangeListener(piece, furnitureChangeListener);
						break;
					case DELETE:
						deletePieceOfFurniture(piece);
						removePropertyChangeListener(piece, furnitureChangeListener);
						break;
				}
				// If piece is or contains a door or a window, update walls that intersect with piece
				if (containsDoorsAndWindows(piece)) {
					// deferred to visible see RUN_UPDATES updateObjects(home.getWalls());
					fullWallUpdateRequired = true;
				} else if (containsStaircases(piece)) {
					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					fullRoomUpdateRequired = true;
				} else {
					approximateHomeBoundsCache = null;
				}
				groundChangeListener.propertyChange(null);
				updateObjectsLightScope(Arrays.asList(new HomePieceOfFurniture[]{piece}));
			}
		};
		this.home.addFurnitureListener(this.furnitureListener);
	}

	/**
	 * Adds the given <code>listener</code> to <code>piece</code> and its children.
	 */
	private void addPropertyChangeListener(HomePieceOfFurniture piece, PropertyChangeListener listener) {
		if (piece instanceof HomeFurnitureGroup) {
			for (HomePieceOfFurniture child : ((HomeFurnitureGroup)piece).getFurniture()) {
				addPropertyChangeListener(child, listener);
			}
		} else {
			piece.addPropertyChangeListener(listener);
		}
	}

	/**
	 * Removes the given <code>listener</code> from <code>piece</code> and its children.
	 */
	private void removePropertyChangeListener(HomePieceOfFurniture piece, PropertyChangeListener listener) {
		if (piece instanceof HomeFurnitureGroup) {
			for (HomePieceOfFurniture child : ((HomeFurnitureGroup)piece).getFurniture()) {
				removePropertyChangeListener(child, listener);
			}
		} else {
			piece.removePropertyChangeListener(listener);
		}
	}

	/**
	 * Returns <code>true</code> if the given <code>piece</code> is or contains a door or window.
	 */
	private boolean containsDoorsAndWindows(HomePieceOfFurniture piece) {
		if (piece instanceof HomeFurnitureGroup) {
			for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup) piece).getFurniture()) {
				if (containsDoorsAndWindows(groupPiece)) {
					return true;
				}
			}
			return false;
		} else {
			return piece.isDoorOrWindow();
		}
	}

	/**
	 * Returns <code>true</code> if the given <code>piece</code> is or contains a staircase
	 * with a top cut out shape.
	 */
	private boolean containsStaircases(HomePieceOfFurniture piece) {
		if (piece instanceof HomeFurnitureGroup) {
			for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup) piece).getFurniture()) {
				if (containsStaircases(groupPiece)) {
					return true;
				}
			}
			return false;
		} else {
			return piece.getStaircaseCutOutShape() != null;
		}
	}

	/**
	 * Adds a room listener to home rooms that updates the children of the given
	 * <code>group</code>, each time a room is added, updated or deleted.
	 */
	private void addRoomListener(final Group group) {
		this.roomChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				Room updatedRoom = (Room) ev.getSource();
				String propertyName = ev.getPropertyName();
				if (Room.Property.FLOOR_COLOR.name().equals(propertyName)
						|| Room.Property.FLOOR_TEXTURE.name().equals(propertyName)
						|| Room.Property.FLOOR_SHININESS.name().equals(propertyName)
						|| Room.Property.CEILING_COLOR.name().equals(propertyName)
						|| Room.Property.CEILING_TEXTURE.name().equals(propertyName)
						|| Room.Property.CEILING_SHININESS.name().equals(propertyName)) {
					updateObjects(Arrays.asList(new Room[]{updatedRoom}));
				} else if (Room.Property.FLOOR_VISIBLE.name().equals(propertyName)
						|| Room.Property.CEILING_VISIBLE.name().equals(propertyName)
						|| Room.Property.LEVEL.name().equals(propertyName)) {
					// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
					fullRoomUpdateRequired = true;
					groundChangeListener.propertyChange(null);
				} else if (Room.Property.POINTS.name().equals(propertyName)) {
					if (homeObjectsToUpdate != null) {
						// Don't try to optimize if more than one room to update
						// deferred to visible see RUN_UPDATES updateObjects(home.getRooms());
						fullRoomUpdateRequired = true;
					} else {
						updateObjects(Arrays.asList(new Room[]{updatedRoom}));
						updateObjects(getHomeObjects(HomeLight.class));
						// Search the rooms that overlap the updated one
						Area oldArea = new Area(getShape((float[][]) ev.getOldValue()));
						Area newArea = new Area(getShape((float[][]) ev.getNewValue()));
						Level updatedRoomLevel = updatedRoom.getLevel();
						for (Room room : home.getRooms()) {
							Level roomLevel = room.getLevel();
							//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/656a58a5?duration=2592000000
							// added || updatedRoomLevel == null
							if (room != updatedRoom
									&& (roomLevel == null
									|| updatedRoomLevel == null
									|| Math.abs(updatedRoomLevel.getElevation() + updatedRoomLevel.getHeight() - (roomLevel.getElevation() + roomLevel.getHeight())) < 1E-5
									|| Math.abs(updatedRoomLevel.getElevation() + updatedRoomLevel.getHeight() - (roomLevel.getElevation() - roomLevel.getFloorThickness())) < 1E-5)) {
								Area roomAreaIntersectionWithOldArea = new Area(getShape(room.getPoints()));
								Area roomAreaIntersectionWithNewArea = new Area(roomAreaIntersectionWithOldArea);
								roomAreaIntersectionWithNewArea.intersect(newArea);
								if (!roomAreaIntersectionWithNewArea.isEmpty()) {
									updateObjects(Arrays.asList(new Room[]{room}));
								} else {
									roomAreaIntersectionWithOldArea.intersect(oldArea);
									if (!roomAreaIntersectionWithOldArea.isEmpty()) {
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
		for (Room room : this.home.getRooms()) {
			room.addPropertyChangeListener(this.roomChangeListener);
		}
		this.roomListener = new CollectionListener<Room>() {
			public void collectionChanged(CollectionEvent<Room> ev) {
				Room room = ev.getItem();
				switch (ev.getType()) {
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
	private GeneralPath getShape(float[][] points) {
		GeneralPath path = new GeneralPath();
		path.moveTo(points[0][0], points[0][1]);
		for (int i = 1; i < points.length; i++) {
			path.lineTo(points[i][0], points[i][1]);
		}
		path.closePath();
		return path;
	}

  /**
   * Adds a polyline listener to home polylines that updates the children of the given
   * <code>group</code>, each time a polyline is added, updated or deleted.
   */
  private void addPolylineListener(final Group group) {
    this.polylineChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Polyline polyline = (Polyline)ev.getSource();
          updateObjects(Arrays.asList(new Polyline [] {polyline}));
        }
      };
    for (Polyline polyline : this.home.getPolylines()) {
      polyline.addPropertyChangeListener(this.polylineChangeListener);
    }
    this.polylineListener = new CollectionListener<Polyline>() {
        public void collectionChanged(CollectionEvent<Polyline> ev) {
          Polyline polyline = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addObject(group, polyline, true, false);
              polyline.addPropertyChangeListener(polylineChangeListener);
              break;
            case DELETE :
              deleteObject(polyline);
              polyline.removePropertyChangeListener(polylineChangeListener);
              break;
          }
        }
      };
    this.home.addPolylinesListener(this.polylineListener);
  }

	/**
	 * Adds a label listener to home labels that updates the children of the given
	 * <code>group</code>, each time a label is added, updated or deleted.
	 */
	private void addLabelListener(final Group group) {
		this.labelChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				Label label = (Label) ev.getSource();
				updateObjects(Arrays.asList(new Label[]{label}));
			}
		};
		for (Label label : this.home.getLabels()) {
			label.addPropertyChangeListener(this.labelChangeListener);
		}
		this.labelListener = new CollectionListener<Label>() {
			public void collectionChanged(CollectionEvent<Label> ev) {
				Label label = ev.getItem();
				switch (ev.getType()) {
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
	private void addEnvironmentListeners() {
		this.wallsAlphaListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				updateObjects(home.getWalls());
				updateObjects(home.getRooms());
			}
		};
		this.home.getEnvironment().addPropertyChangeListener(
				HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
		this.drawingModeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				updateObjects(home.getWalls());
				updateObjects(home.getRooms());
				updateObjects(getHomeObjects(HomePieceOfFurniture.class));
			}
		};
		this.home.getEnvironment().addPropertyChangeListener(
				HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
	}

	/**
	 * Adds to <code>group</code> a branch matching <code>homeObject</code>.
	 */
	private Node addObject(Group group, Selectable homeObject, boolean listenToHomeUpdates, boolean waitForLoading) {
		return addObject(group, homeObject, -1, listenToHomeUpdates, waitForLoading);
	}

	/**
	 * Adds to <code>group</code> a branch matching <code>homeObject</code> at a given <code>index</code>.
	 * If <code>index</code> is equal to -1, <code>homeObject</code> will be added at the end of the group.
	 */
	private Node addObject(Group group, Selectable homeObject, int index,
						   boolean listenToHomeUpdates, boolean waitForLoading) {
		Object3DBranch object3D = createObject3D(homeObject, waitForLoading);
		if (listenToHomeUpdates) {
			this.homeObjects.put(homeObject, object3D);
		}
		if (index == -1) {
			group.addChild(object3D);
		} else {
			group.insertChild(object3D, index);
		}
		clearPrintedImageCache();
		return object3D;
	}

	/**
	 * Adds to <code>group</code> a branch matching <code>homeObject</code> or its children if the piece is a group of furniture.
	 */
	private void addPieceOfFurniture(Group group, HomePieceOfFurniture piece, boolean listenToHomeUpdates, boolean waitForLoading) {
		if (piece instanceof HomeFurnitureGroup) {
			for (HomePieceOfFurniture child : ((HomeFurnitureGroup)piece).getFurniture()) {
				addPieceOfFurniture(group, child, listenToHomeUpdates, waitForLoading);
			}
		} else {
			addObject(group, piece, listenToHomeUpdates, waitForLoading);
		}
	}

	/**
	 * Returns the 3D object matching the given home object. If <code>waitForLoading</code>
	 * is <code>true</code> the resources used by the returned 3D object should be ready to be displayed.
	 * @deprecated Subclasses which used to override this method must be updated to create an instance of
	 * a {@link Object3DFactory factory} and give it as parameter to the constructor of this class.
	 */
	private Object3DBranch createObject3D(Selectable homeObject,
										  boolean waitForLoading) {
		return (Object3DBranch) this.object3dFactory.createObject3D(this.home, homeObject, waitForLoading);
	}

	/**
	 * Detaches from the scene the branch matching <code>homeObject</code>.
	 */
	private void deleteObject(Selectable homeObject) {
		this.homeObjects.get(homeObject).detach();
		this.homeObjects.remove(homeObject);
		if (this.homeObjectsToUpdate != null
			&& this.homeObjectsToUpdate.contains(homeObject)) {
		  this.homeObjectsToUpdate.remove(homeObject);
		}
		clearPrintedImageCache();
	}

	/**
	 * Detaches from the scene the branches matching <code>piece</code> or its children if it's a group.
	 */
	private void deletePieceOfFurniture(HomePieceOfFurniture piece) {
		if (piece instanceof HomeFurnitureGroup) {
			for (HomePieceOfFurniture child : ((HomeFurnitureGroup)piece).getFurniture()) {
				deletePieceOfFurniture(child);
			}
		} else {
			deleteObject(piece);
		}
	}

	/**
   * Updates 3D <code>objects</code> later. Should be invoked from Event Dispatch Thread.
	 */
	private void updateObjects(Collection<? extends Selectable> objects) {
		if (this.homeObjectsToUpdate != null) {
			this.homeObjectsToUpdate.addAll(objects);
		} else {
			this.homeObjectsToUpdate = new HashSet<Selectable>(objects);
			// Invoke later the update of objects of homeObjectsToUpdate
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					for (Selectable object : homeObjectsToUpdate) {
						Object3DBranch objectBranch = homeObjects.get(object);
						// Check object wasn't deleted since updateObjects call
						if (objectBranch != null) {
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
	 * PJ NOTE! this method is never called now, all objects that touch walls in have wall updates deferred to RUN_UPDATES, which redoes all walls
	 * Updates walls that may intersect from the given doors or window.
	 */
  private void updateIntersectingWalls(HomePieceOfFurniture ... doorOrWindows) {
    Collection<Wall> walls = this.home.getWalls();
    int wallCount = 0;
    if (this.homeObjectsToUpdate != null) {
      for (Selectable object : this.homeObjectsToUpdate) {
        if (object instanceof Wall) {
          wallCount++;
        }
      }
    }
    // Check if some more walls may require an update
    if (wallCount != walls.size()) {
      List<Wall> updatedWalls = new ArrayList<Wall>();
      Rectangle2D doorOrWindowBounds = null;
      // Compute the approximate bounds of the doors and windows
      for (HomePieceOfFurniture doorOrWindow : doorOrWindows) {
        float [][] points = doorOrWindow.getPoints();
        if (doorOrWindowBounds == null) {
          doorOrWindowBounds = new Rectangle2D.Float(points [0][0], points [0][1], 0, 0);
        } else {
          doorOrWindowBounds.add(points [0][0], points [0][1]);
        }
        for (int i = 1; i < points.length; i++) {
          doorOrWindowBounds.add(points [i][0], points [i][1]);
        }
      }
      // Search walls that intersect the bounds
      for (Wall wall : walls) {
        if (wall.intersectsRectangle((float)doorOrWindowBounds.getX(), (float)doorOrWindowBounds.getY(),
            (float)doorOrWindowBounds.getX() + (float)doorOrWindowBounds.getWidth(),
            (float)doorOrWindowBounds.getY() + (float)doorOrWindowBounds.getHeight())) {
          updatedWalls.add(wall);
        }
      }
      updateObjects(updatedWalls);
    }
  }

  /**
   * Updates <code>wall</code> geometry and the walls at its end or start.
	 */
	private void updateWall(Wall wall) {
		Collection<Wall> wallsToUpdate = new ArrayList<Wall>(3);
		wallsToUpdate.add(wall);
		if (wall.getWallAtStart() != null) {
			wallsToUpdate.add(wall.getWallAtStart());
		}
		if (wall.getWallAtEnd() != null) {
			wallsToUpdate.add(wall.getWallAtEnd());
		}
		updateObjects(wallsToUpdate);
	}

	/**
	 * Updates the <code>object</code> scope under light later. Should be invoked from Event Dispatch Thread.
	 */
	private void updateObjectsLightScope(Collection<? extends Selectable> objects) {
		if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
			if (this.lightScopeObjectsToUpdate != null) {
				if (objects == null) {
					this.lightScopeObjectsToUpdate.clear();
					this.lightScopeObjectsToUpdate.add(null);
				} else if (!this.lightScopeObjectsToUpdate.contains(null)) {
					this.lightScopeObjectsToUpdate.addAll(objects);
				}
			} else {
				this.lightScopeObjectsToUpdate = new HashSet<Selectable>();
				if (objects == null) {
					this.lightScopeObjectsToUpdate.add(null);
				} else {
					this.lightScopeObjectsToUpdate.addAll(objects);
				}
				// Invoke later the update of objects of lightScopeObjectsToUpdate
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (lightScopeObjectsToUpdate.contains(null)) {
							subpartSizeListener.propertyChange(null);
						} else if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
							Area lightScopeOutsideWallsArea = getLightScopeOutsideWallsArea();
							for (Selectable object : lightScopeObjectsToUpdate) {
								Group object3D = homeObjects.get(object);
								if (object3D instanceof HomePieceOfFurniture3D) {
									// Add the direct parent of the shape that will be added once loaded
									// otherwise scope won't be updated automatically
									object3D = (Group) object3D.getChild(0);
								}
								// Check object wasn't deleted since updateObjects call
								if (object3D != null) {
									// Add item to scope if one of its points don't belong to lightScopeOutsideWallsArea
									boolean objectInOutsideLightScope = false;
									for (float[] point : object.getPoints()) {
										if (!lightScopeOutsideWallsArea.contains(point[0], point[1])) {
											objectInOutsideLightScope = true;
											break;
										}
									}
									for (Light light : sceneLights){
										if (light instanceof DirectionalLight){
											if (objectInOutsideLightScope && light.indexOfScope(object3D) == -1){
												light.addScope(object3D);
											} else if (!objectInOutsideLightScope && light.indexOfScope(object3D) != -1) {
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
	private void addShadowOnFloor(Group homeRoot, Map<HomePieceOfFurniture, Node> pieces3D) {
		Comparator<Level> levelComparator = new Comparator<Level>() {
			public int compare(Level level1, Level level2) {
				return Float.compare(level1.getElevation(), level2.getElevation());
			}
		};
		Map<Level, Area> areasOnLevel = new TreeMap<Level, Area>(levelComparator);
		// Compute union of the areas of pieces at ground level that are not lights, doors or windows
		for (Map.Entry<HomePieceOfFurniture, Node> object3DEntry : pieces3D.entrySet()) {
			if (object3DEntry.getKey() instanceof HomePieceOfFurniture) {
				HomePieceOfFurniture piece = object3DEntry.getKey();
				// This operation can be lengthy, so give up if thread is interrupted
				if (Thread.currentThread().isInterrupted()) {
					return;
				}
				if (piece.getElevation() == 0
						&& !piece.isDoorOrWindow()
						&& !(piece instanceof com.eteks.sweethome3d.model.Light)) {
					Area pieceAreaOnFloor = ModelManager.getInstance().getAreaOnFloor(object3DEntry.getValue());
					Level level = piece.getLevel();
					if (piece.getLevel() == null) {
						level = new Level("Dummy", 0, 0, 0);
					}
					if (level.isViewableAndVisible()) {
						Area areaOnLevel = areasOnLevel.get(level);
						if (areaOnLevel == null) {
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
		for (Map.Entry<Level, Area> levelArea : areasOnLevel.entrySet()) {
			List<Point3f> coords = new ArrayList<Point3f>();
			List<Integer> stripCounts = new ArrayList<Integer>();
			int pointsCount = 0;
			float[] modelPoint = new float[2];
			for (PathIterator it = levelArea.getValue().getPathIterator(null); !it.isDone(); ) {
				if (it.currentSegment(modelPoint) == PathIterator.SEG_CLOSE) {
					stripCounts.add(pointsCount);
					pointsCount = 0;
				} else {
					coords.add(new Point3f(modelPoint[0], levelArea.getKey().getElevation() + 0.49f, modelPoint[1]));
					pointsCount++;
				}
				it.next();
			}

			if (coords.size() > 0) {
				GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
				geometryInfo.setCoordinates(coords.toArray(new Point3f[coords.size()]));
				int[] stripCountsArray = new int[stripCounts.size()];
				for (int i = 0; i < stripCountsArray.length; i++) {
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
