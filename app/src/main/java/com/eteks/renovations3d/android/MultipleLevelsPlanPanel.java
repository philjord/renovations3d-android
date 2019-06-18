/*
 * MultipleLevelsPlanPanel.java 23 oct. 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.Tutorial;
import com.eteks.renovations3d.android.utils.ToolSpinnerControl;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.JComponent;
import com.eteks.renovations3d.android.utils.LevelSpinnerControl;
import com.eteks.renovations3d.android.utils.WelcomeDialog;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanController.EditableProperty;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.mindblowing.renovations3d.R;

import org.jogamp.vecmath.Point2f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.Insets;
import javaawt.geom.AffineTransform;
import javaawt.geom.Rectangle2D;
import javaawt.print.PageFormat;
import javaawt.print.PrinterException;

/**
 * A panel for multiple levels plans where users can select the displayed level.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class MultipleLevelsPlanPanel extends JComponent implements PlanView {
	public static final String WELCOME_SCREEN_UNWANTED = "PLAN_WELCOME_SCREEN_UNWANTED";
	public static final int TOOLS_WIDE_MIN_DP = 550;
	public static final int LEVELS_WIDE_MIN_DP = 350;

	// if we are not initialized then ignore onCreateViews
	private boolean initialized = false;

	private DrawableView drawableView;

	private Menu mOptionsMenu;

	private Spinner toolSpinner;
	private Spinner levelsSpinner;

	private View rootView;// recorded to prevent double view creates from fragment manager
	private boolean resetToSelectTool = false;



	//PJPJP will be needed later I guess
	// use a class from the jar!
	//private static final ImageIcon sameElevationIcon = SwingTools.getScaledImageIcon(this.class.getResource("swing/resources/sameElevation.png"));

	private PlanComponent planComponent;
	private LevelSpinnerControl levelSpinnerControl;
	private ToolSpinnerControl toolSpinnerControl;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		// TODO: find out why a new home gets a double onViewCreate
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.multiple_level_plan_panel, container, false);

			if (initialized) {
				this.setHasOptionsMenu(true);

				drawableView = (DrawableView) rootView.findViewById(R.id.drawableView);
				drawableView.setDrawer(this);

				planComponent.setDrawableView(drawableView);

				this.levelSpinnerControl = new LevelSpinnerControl(this.getContext());
				this.toolSpinnerControl = new ToolSpinnerControl(this.getContext());

				// from the constructor but placed here now so views are set
				createComponents(home, preferences, planController);
				layoutComponents();
				updateSelectedTab(home);
			}



			// make the left and right swipers work
			ImageButton planLeftSwiper = (ImageButton) rootView.findViewById(R.id.planLeftSwiper);
			planLeftSwiper.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Renovations3DActivity) getActivity()).getViewPager().setCurrentItem(0, true);
				}
			});
			ImageButton planRightSwiper = (ImageButton) rootView.findViewById(R.id.planRightSwiper);
			planRightSwiper.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Renovations3DActivity) getActivity()).getViewPager().setCurrentItem(2, true);
				}
			});

			// make the zoom buttons zoom at center
			ImageButton planZoomIn = (ImageButton) rootView.findViewById(R.id.planZoomIn);
			planZoomIn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					zoomAtCenter(1.5f);
				}
			});

			ImageButton planZoomOut = (ImageButton) rootView.findViewById(R.id.planZoomOut);
			planZoomOut.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					zoomAtCenter(1.0f / 1.5f);
				}
			});

		}
		return rootView;
	}

	private void zoomAtCenter(float scaleChangeFactor) {
		// keep the center central
		float centerX = planComponent.getWidth()/2.0f; //TODO: what's center??
		float centerY = planComponent.getHeight()/2.0f;
		float priorCenterX = convertXPixelToModel((int)centerX);
		float priorCenterY = convertYPixelToModel((int)centerY);

		float newScale = getScale() * scaleChangeFactor;
		// Don't let the object get too small or too large.
		newScale = Math.max(0.1f, Math.min(newScale, 10.0f));
		//controller.zoom(newScale); //don't want the mouse move calls
		newScale = Math.max(planController.getMinimumScale(), Math.min(newScale, planController.getMaximumScale()));
		if(newScale != getScale()) {
			planController.getView().setScale(newScale);
			home.setProperty("com.eteks.sweethome3d.SweetHome3D.PlanScale", String.valueOf(newScale));
		}

		float modelDiffX = priorCenterX - convertXPixelToModel((int)centerX);
		float modelDiffY = priorCenterY - convertYPixelToModel((int)centerY);
		planComponent.moveScrolledXY(modelDiffX * newScale,modelDiffY * newScale);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser) {
			// this gets called heaps of time, wait until we have an activity
			if (getActivity() != null) {
				WelcomeDialog.possiblyShowWelcomeScreen((Renovations3DActivity) getActivity(), WELCOME_SCREEN_UNWANTED, R.string.welcometext_planview, preferences);
			}

			resetToSelectTool = true;


			if (rootView != null) {
				if (Renovations3DActivity.SHOW_PAGER_BUTTONS) {
					rootView.findViewById(R.id.planLeftSwiper).setVisibility(View.VISIBLE);
					rootView.findViewById(R.id.planRightSwiper).setVisibility(View.VISIBLE);
				} else {
					rootView.findViewById(R.id.planLeftSwiper).setVisibility(View.INVISIBLE);
					rootView.findViewById(R.id.planRightSwiper).setVisibility(View.INVISIBLE);
				}
				if (Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS) {
					rootView.findViewById(R.id.planZoomIn).setVisibility(View.VISIBLE);
					rootView.findViewById(R.id.planZoomOut).setVisibility(View.VISIBLE);
				} else {
					rootView.findViewById(R.id.planZoomIn).setVisibility(View.INVISIBLE);
					rootView.findViewById(R.id.planZoomOut).setVisibility(View.INVISIBLE);
				}
			}

			repaint();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onDestroy() {
		PlanComponent.PieceOfFurnitureModelIcon.destroyUniverse();
		super.onDestroy();
	}

	AdapterView.OnItemSelectedListener planToolSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (getActivity() != null) {
				Tutorial tutorial = ((Renovations3DActivity) getActivity()).getTutorial();
				switch (position) {
					case 0://planSelect:
						finishCurrentMode();
						setMode(PlanController.Mode.SELECTION);
						break;
					case 1://createWalls:
						finishCurrentMode();
						if (!tutorial.isEnabled() && getActivity().isFinishing())
							Toast.makeText(getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
						setMode(PlanController.Mode.WALL_CREATION);
						tutorial.actionComplete(Tutorial.TutorialAction.CREATE_WALL_TOOL_SELECTED);
						break;
					case 2://createRooms:
						finishCurrentMode();
						if (!tutorial.isEnabled() && getActivity().isFinishing())
							Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
						setMode(PlanController.Mode.ROOM_CREATION);
						tutorial.actionComplete(Tutorial.TutorialAction.CREATE_ROOM_TOOL_SELECTED);
						break;
					case 3://createPolyLines:
						finishCurrentMode();
						if (!tutorial.isEnabled() && getActivity().isFinishing())
							Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
						planController.setMode(PlanController.Mode.POLYLINE_CREATION);
						break;
					case 4://createDimensions:
						finishCurrentMode();
						if (!tutorial.isEnabled() && getActivity().isFinishing())
							Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
						setMode(PlanController.Mode.DIMENSION_LINE_CREATION);
						break;
					case 5://createText:
						finishCurrentMode();
						// note single tap works for this one
						setMode(PlanController.Mode.LABEL_CREATION);
						break;
					case 6://planPan:
						finishCurrentMode();
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

	private void finishCurrentMode() {
		PlanController.Mode currentMode = planController.getMode();

		if (currentMode == PlanController.Mode.DIMENSION_LINE_CREATION
				|| currentMode == PlanController.Mode.WALL_CREATION
				|| currentMode == PlanController.Mode.ROOM_CREATION
				|| currentMode == PlanController.Mode.POLYLINE_CREATION) {
			// need to simulate a press on the last dragged position to make it stick
			Point2f lastDrag = planComponent.getLastDragLocation();
			if (lastDrag.x > 0 && lastDrag.y > 0) {
				planController.pressMouse(lastDrag.x, lastDrag.y, 2, false, false, false, false);
			}
		}

		// pan, selection and labels do nothing
	}

	/**
	 * This return the state of the control key and resets it to off, so it is a one time use button (an auto popper)
	 *
	 * @return
	 */
	public boolean getIsControlKeyOn() {
		// not sure why, something to do with restore lifecycle?
		if (mOptionsMenu != null) {
			MenuItem cntlKey = mOptionsMenu.findItem(R.id.controlKeyOneTimer);
			// might be missing if we are reloading a home
			if (cntlKey != null) {
				boolean isChecked = cntlKey.isChecked();
				cntlKey.setChecked(false);
				return isChecked;
			}
		}

		return false;
	}


	//copied from HomeController as we can't touch the EDT thread like they do
	public void setMode(PlanController.Mode mode) {
		if (planController.getMode() != mode) {
			final String actionKey;
			if (mode == PlanController.Mode.WALL_CREATION) {
				actionKey = HomeView.ActionType.CREATE_WALLS.name();
			} else if (mode == PlanController.Mode.ROOM_CREATION) {
				actionKey = HomeView.ActionType.CREATE_ROOMS.name();
			} else if (mode == PlanController.Mode.POLYLINE_CREATION) {
				actionKey = HomeView.ActionType.CREATE_POLYLINES.name();
			} else if (mode == PlanController.Mode.DIMENSION_LINE_CREATION) {
				actionKey = HomeView.ActionType.CREATE_DIMENSION_LINES.name();
			} else if (mode == PlanController.Mode.LABEL_CREATION) {
				actionKey = HomeView.ActionType.CREATE_LABELS.name();
			} else {
				actionKey = null;
			}

			if (actionKey != null && !this.preferences.isActionTipIgnored(actionKey)) {
				Thread t = new Thread(new Runnable()
				{
					public void run()
					{
						if(getActivity() != null) {
							if (((Renovations3DActivity) getActivity()).getHomeController() != null &&
									((Renovations3DActivity) getActivity()).getHomeController().getView().showActionTipMessage(actionKey)) {
								preferences.setActionTipIgnored(actionKey);
							}
						}
					}
				});
				t.start();
			}

			planController.setMode(mode);
		}
	}


	private int[] toolIcon = new int[] {
					R.drawable.plan_select,
					R.drawable.plan_create_walls,
					R.drawable.plan_create_rooms,
					R.drawable.plan_create_polylines,
					R.drawable.plan_create_dimension_lines,
					R.drawable.plan_create_labels,
					R.drawable.plan_pan
	};
	private PlanController.Mode[] modesInSpinnerOrder = new PlanController.Mode[] {
					PlanController.Mode.SELECTION,
					PlanController.Mode.WALL_CREATION,
					PlanController.Mode.ROOM_CREATION,
					PlanController.Mode.POLYLINE_CREATION,
					PlanController.Mode.DIMENSION_LINE_CREATION,
					PlanController.Mode.LABEL_CREATION,
					PlanController.Mode.PANNING
	};
	private String[] toolNames;
	private void updateToolNames() {
		toolNames = new String[] {
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "SELECT.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_WALLS.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_ROOMS.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_POLYLINES.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_DIMENSION_LINES.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_LABELS.Name"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "PAN.Name")
		};
	}
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mOptionsMenu = menu;
		inflater.inflate(R.menu.plan_component_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		menu.findItem(R.id.editUndo).setEnabled(false);// nothing to undo at first

		// titles are all set on prepare

		String subMenuTile = preferences.getLocalizedString(com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController.class, "wizard.title");
		//TODO: localize this properly not this madness
		// let's try ripping out everything after last space from the wizard title
		if (subMenuTile.lastIndexOf(" ") > 0) {
			subMenuTile = subMenuTile.substring(0, subMenuTile.lastIndexOf(" "));
		}
		subMenuTile += "...";
		menu.findItem(R.id.bgImageMenu).setTitle(subMenuTile);

		sortOutBackgroundMenu(menu);

		menu.findItem(R.id.delete).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE.Name"));

		updateToolNames();
		toolSpinner = (Spinner) menu.findItem(R.id.planToolSelectSpinner).getActionView();
		toolSpinner.setPadding(toolSpinner.getPaddingLeft(), 0, toolSpinner.getPaddingRight(), toolSpinner.getPaddingBottom());
		// possibly on a double onCreateView call this gets called and the levelSpinnerControl has not yet been created so ignore the call this time round
		if (toolSpinnerControl != null)
			toolSpinnerControl.setSpinner(toolSpinner, toolNames, toolIcon);

		levelsSpinner = (Spinner) menu.findItem(R.id.planLevelsSpinner).getActionView();
		// no icons do not need to shift up levelsSpinner.setPadding(levelsSpinner.getPaddingLeft(), 0, levelsSpinner.getPaddingRight(), levelsSpinner.getPaddingBottom());
		// possibly on a double onCreateView call this gets called and the levelSpinnerControl has not yet been created so ignore the call this time round
		if (levelSpinnerControl != null)
			levelSpinnerControl.setSpinner(levelsSpinner);

		menu.findItem(R.id.planLevelsSpinner).setVisible(home.getLevels().size() > 0);
	}


	private BackgroundImage currentBackgroundImage() {
		Level selectedLevel = this.home.getSelectedLevel();
		Level backgroundImageLevel = null;
		if (selectedLevel != null) {
			// Search the first level at same elevation with a background image
			List<Level> levels = this.home.getLevels();
			for (int i = levels.size() - 1; i >= 0; i--) {
				Level level = levels.get(i);
				if (level.getElevation() == selectedLevel.getElevation()
						&& level.getElevationIndex() <= selectedLevel.getElevationIndex()
						&& level.isViewable()
						&& level.getBackgroundImage() != null) {//PJ this is taken from plancomp paint, but visible check removed
					backgroundImageLevel = level;
					break;
				}
			}
		}
		return backgroundImageLevel == null ? this.home.getBackgroundImage() : backgroundImageLevel.getBackgroundImage();
	}

	private void sortOutBackgroundMenu(Menu menu) {
		final BackgroundImage backgroundImage = currentBackgroundImage();

		String importModifyString = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_BACKGROUND_IMAGE.Name");
		if (backgroundImage != null)
			importModifyString = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_BACKGROUND_IMAGE.Name");
		menu.findItem(R.id.bgImageImportModify).setTitle(importModifyString);

		String showHideVis = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "HIDE_BACKGROUND_IMAGE.Name");
		if (backgroundImage != null && !backgroundImage.isVisible())
			showHideVis = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "SHOW_BACKGROUND_IMAGE.Name");
		menu.findItem(R.id.bgImageToggleVis).setTitle(showHideVis);

		menu.findItem(R.id.bgImageDelete).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE_BACKGROUND_IMAGE.Name"));

		menu.findItem(R.id.bgImageToggleVis).setEnabled(backgroundImage != null);
		menu.findItem(R.id.bgImageDelete).setEnabled(backgroundImage != null);
	}

	private void resetToolSpinnerToMode() {
		PlanController.Mode mode = planController.getMode();
		toolSpinner.setOnItemSelectedListener(null);
		int position = Arrays.asList(modesInSpinnerOrder).indexOf(mode);
		toolSpinner.setSelection(position);
		toolSpinner.setOnItemSelectedListener(planToolSpinnerListener);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		mOptionsMenu = menu;

		MenuItem cntlMI = menu.findItem(R.id.controlKeyOneTimer);
		if (cntlMI != null) {
			String cntlText = getActivity().getResources().getString(android.R.string.copy);
			Renovations3DActivity.setIconizedMenuTitle(cntlMI, cntlText, R.drawable.edit_copy, getContext());
			cntlMI.setEnabled(planController.getMode() == PlanController.Mode.SELECTION);
		}

		if (resetToSelectTool && planController.getMode() != PlanController.Mode.PANNING) {
			setMode(PlanController.Mode.SELECTION);
		}
		resetToSelectTool = false;

		resetToolSpinnerToMode();

		menu.findItem(R.id.planLevelsSpinner).setVisible(home.getLevels().size() > 0);

		// undo doesn't get text updated as it is just an icon
		menu.findItem(R.id.editUndo).setEnabled(undoEnabled);

		MenuItem redo = menu.findItem(R.id.editRedo);
		String redoStr = redoText == null ? preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "REDO.Name") : redoText;
		Renovations3DActivity.setIconizedMenuTitle(redo, redoStr, R.drawable.edit_redo, getContext());
		redo.setEnabled(redoEnabled);


		menu.findItem(R.id.planModifyWallsMenu).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_WALL.Name"));
		menu.findItem(R.id.planModifyWalls).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "wall.title").replace("...", ""));
		menu.findItem(R.id.planModifyWalls).setEnabled(!Home.getWallsSubList(this.home.getSelectedItems()).isEmpty());
		menu.findItem(R.id.planJoinWalls).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "JOIN_WALLS.Name"));
		menu.findItem(R.id.planJoinWalls).setEnabled(joinWallsEnabled);// enabled via the setEnabled call
		menu.findItem(R.id.planReverseWalls).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "REVERSE_WALL_DIRECTION.Name"));
		menu.findItem(R.id.planReverseWalls).setEnabled(!Home.getWallsSubList(this.home.getSelectedItems()).isEmpty());
		menu.findItem(R.id.planSplitWall).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "SPLIT_WALL.Name"));
		menu.findItem(R.id.planSplitWall).setEnabled(Home.getWallsSubList(this.home.getSelectedItems()).size() == 1);

		menu.findItem(R.id.planModifyMenu).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_FURNITURE.Name"));
		String modifyFurniture = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_FURNITURE.Name").replace("...", "") + " "
				+ preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "FURNITURE_MENU.Name") + "...";
		menu.findItem(R.id.planModifyFurniture).setTitle(modifyFurniture);
		menu.findItem(R.id.planModifyFurniture).setEnabled(!Home.getFurnitureSubList(this.home.getSelectedItems()).isEmpty());
		menu.findItem(R.id.planModifyPolylines).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_POLYLINE.Name"));
		menu.findItem(R.id.planModifyPolylines).setEnabled(!Home.getPolylinesSubList(this.home.getSelectedItems()).isEmpty());
		menu.findItem(R.id.planModifyText).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_LABEL.Name"));
		menu.findItem(R.id.planModifyText).setEnabled(!Home.getLabelsSubList(this.home.getSelectedItems()).isEmpty());
		menu.findItem(R.id.planModifyCompass).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_COMPASS.Name"));
		menu.findItem(R.id.planModifyRooms).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_ROOM.Name"));
		menu.findItem(R.id.planModifyRooms).setEnabled(!Home.getRoomsSubList(this.home.getSelectedItems()).isEmpty());
		menu.findItem(R.id.planAddRoomPoint).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_ROOM_POINT.Name"));
		menu.findItem(R.id.planAddRoomPoint).setEnabled(this.addRoomPointEnabled);// enabled via the setEnabled call
		menu.findItem(R.id.planAddRoomPoint).setChecked(this.addRoomPointEnabled && this.planComponent.addRoomPointActivated);
		menu.findItem(R.id.planDeleteRoomPoint).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE_ROOM_POINT.Name"));
		menu.findItem(R.id.planDeleteRoomPoint).setEnabled(this.deleteRoomPointEnabled);// enabled via the setEnabled call
		menu.findItem(R.id.planDeleteRoomPoint).setChecked(this.deleteRoomPointEnabled && this.planComponent.deleteRoomPointActivated);

		MenuItem itemLocked = menu.findItem(R.id.lockBasePlanCheck);
		itemLocked.setChecked(home.isBasePlanLocked());

		int iconId = itemLocked.isChecked() ? R.drawable.plan_locked : R.drawable.plan_unlocked;
		String actionName = itemLocked.isChecked() ? "UNLOCK_BASE_PLAN.Name" : "LOCK_BASE_PLAN.Name";
		String lockedText = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, actionName);
		Renovations3DActivity.setIconizedMenuTitle(itemLocked, lockedText, iconId, getContext() );

		// use the new default name without the number format
		String planeLevelMenuName = preferences.getLocalizedString(com.eteks.sweethome3d.viewcontroller.PlanController.class, "levelName").replace(" %d", "") + "...";
		menu.findItem(R.id.planLevelMenu).setTitle(planeLevelMenuName);
		menu.findItem(R.id.planAddLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_LEVEL.Name"));
		menu.findItem(R.id.planAddLevelAtSame).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_LEVEL_AT_SAME_ELEVATION.Name"));
		menu.findItem(R.id.planModifyLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_LEVEL.Name"));
		boolean canModLevel = ((Renovations3DActivity) getActivity()).getHome() != null && ((Renovations3DActivity) getActivity()).getHome().getSelectedLevel() != null;
		menu.findItem(R.id.planModifyLevel).setEnabled(canModLevel);
		menu.findItem(R.id.planDeleteLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE_LEVEL.Name"));
		menu.findItem(R.id.planModifyCompass).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_COMPASS.Name"));

		menu.findItem(R.id.planEdit).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "EDIT_MENU.Name") + "...");
		menu.findItem(R.id.planSelectLasso).setChecked(this.planComponent.selectLasso);
		menu.findItem(R.id.planSelectMultiple).setChecked(this.planComponent.selectMultiple);

		menu.findItem(R.id.planAlignment).setChecked(this.planComponent.alignmentActivated);
		menu.findItem(R.id.planMagnetism).setChecked(preferences.isMagnetismEnabled());

		sortOutBackgroundMenu(menu);

		super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Renovations3DActivity renovations3DActivity = ((Renovations3DActivity) getActivity());
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.editUndo:
				try {
					if (renovations3DActivity.getHomeController() != null)
						renovations3DActivity.getHomeController().undo();
				} catch (Exception e) {
					//ignored, as the button should only be enabled when one undo is available (see HomeView addUndoSupportListener)
					// I'm getting NPE in a call to addLevel https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/crash?reportTypes=REPORT_TYPE_UNSPECIFIED&duration=2592000000
					e.printStackTrace();
				}
				return true;
			case R.id.editRedo:
				try {
					if (renovations3DActivity.getHomeController() != null)
						renovations3DActivity.getHomeController().redo();
				} catch (Exception e) {//ignored, as the button should only be enabled when one undo is available (see HomeView addUndoSupportListener)
					e.printStackTrace();
				}
				return true;
			case R.id.delete:
				planController.deleteSelection();
				return true;
			case R.id.planGoto3D:
				renovations3DActivity.getViewPager().setCurrentItem(3, true);
				return true;
			case R.id.planModifyWalls:
				planController.modifySelectedWalls();
				return true;
			case R.id.planJoinWalls:
				planController.joinSelectedWalls();
				return true;
			case R.id.planReverseWalls:
				planController.reverseSelectedWallsDirection();
				return true;
			case R.id.planSplitWall:
				planController.splitSelectedWall();
				return true;
			case R.id.planModifyFurniture:
				planController.modifySelectedFurniture();
				return true;
			case R.id.planModifyPolylines:
				planController.modifySelectedPolylines();
				return true;
			case R.id.planModifyText:
				planController.modifySelectedLabels();
				return true;
			case R.id.planModifyCompass:
				planController.modifyCompass();
				return true;
			case R.id.planModifyRooms:
				planController.modifySelectedRooms();
				return true;
			case R.id.planAddRoomPoint:
				item.setChecked(!item.isChecked());
				this.planComponent.addRoomPointActivated = item.isChecked();
				this.planComponent.deleteRoomPointActivated = false; //radios!
				return true;
			case R.id.planDeleteRoomPoint:
				item.setChecked(!item.isChecked());
				this.planComponent.deleteRoomPointActivated = item.isChecked();
				this.planComponent.addRoomPointActivated = false; //radios!
				return true;
			case R.id.planAddLevel:
				planController.addLevel();
				return true;
			case R.id.planAddLevelAtSame:
				planController.addLevelAtSameElevation();
				return true;
			case R.id.planModifyLevel:
				planController.modifySelectedLevel();
				return true;
			case R.id.planDeleteLevel:
				planController.deleteSelectedLevel();
				return true;

			case R.id.lockBasePlanCheck:
				item.setChecked(!item.isChecked());
				//menu closes now, on reopen it will recalc display

				if (item.isChecked())
					planController.lockBasePlan();
				else
					planController.unlockBasePlan();
				return true;

			case R.id.bgImageImportModify:
				//PJ the import appears to modify if it's already there??
				if (renovations3DActivity.getHomeController() != null)
					renovations3DActivity.getHomeController().importBackgroundImage();
				return true;
			case R.id.bgImageToggleVis:
				if (renovations3DActivity.getHomeController() != null) {
					final BackgroundImage backgroundImage = currentBackgroundImage();
					if (backgroundImage != null) {
						if (backgroundImage.isVisible())
							renovations3DActivity.getHomeController().hideBackgroundImage();
						else
							renovations3DActivity.getHomeController().showBackgroundImage();
					}
				}
				return true;
			case R.id.bgImageDelete:
				if (renovations3DActivity.getHomeController() != null)
					renovations3DActivity.getHomeController().deleteBackgroundImage();
				return true;

			case R.id.controlKeyOneTimer:
				item.setChecked(!item.isChecked());
				return true;
			case R.id.planSelectLasso:
				item.setChecked(!item.isChecked());
				this.planComponent.selectLasso = item.isChecked();
				return true;
			case R.id.planSelectMultiple:
				item.setChecked(!item.isChecked());
				this.planComponent.selectMultiple = item.isChecked();
				return true;
			case R.id.planAlignment:
				item.setChecked(!item.isChecked());
				this.planComponent.alignmentActivated = item.isChecked();
				return true;
			case R.id.planMagnetism:
				item.setChecked(!item.isChecked());
				preferences.setMagnetismEnabled(item.isChecked());
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	private Home home;
	private UserPreferences preferences;
	private PlanController planController;

	private boolean rulersVisible = true;

	public void init(Home home, UserPreferences preferences2, PlanController planController) {
		initialized = true;
		this.home = home;
		this.preferences = preferences2;
		this.planController = planController;

		// taken from createComponents
		this.planComponent = createPlanComponent(home, preferences, planController);
		//moved to onCreateView
		//createComponents(home, preferences, planController);
		//layoutComponents();
		//updateSelectedTab(home);

		// taken from HomePane
		rulersVisible = preferences.isRulersVisible();
		preferences.addPropertyChangeListener(UserPreferences.Property.RULERS_VISIBLE,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						rulersVisible = preferences.isRulersVisible();
						planComponent.repaint();
					}
				});

		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent event)
					{
						//TODO: this should set the names of the menu options just once now
					}
				});
	}

	/**
	 * Called by our drawableView when onDraw called for it
	 *
	 * @param g
	 */
	public void paintComponent(Graphics g) {
		// don't bother painting if we are on the 3d view of a table view or something else
		if (this.getUserVisibleHint()) {
			AffineTransform previousTransform = ((Graphics2D) g).getTransform();
			planComponent.paintComponent(g);
			((Graphics2D) g).setTransform(previousTransform);

			if (rulersVisible) {
				AffineTransform previousTransform2 = ((Graphics2D) g).getTransform();
				//PJPJ rendering rulers moved from jscrollpane to here (after the plan in order to overwrite)
				JComponent hRuler = (JComponent) this.getHorizontalRuler();
				hRuler.setWidth(planComponent.getWidth());
				hRuler.setHeight(30);
				hRuler.paintComponent(g);

				JComponent vRuler = (JComponent) this.getVerticalRuler();
				vRuler.setWidth(30);
				vRuler.setHeight(planComponent.getHeight());
				vRuler.paintComponent(g);
				((Graphics2D) g).setTransform(previousTransform2);
			}
		}
	}

	/**
	 * Creates components displayed by this panel.
	 */
	private void createComponents(final Home home,
								  final UserPreferences preferences, final PlanController controller) {
		// moved to init
		//this.planComponent = createPlanComponent(home, preferences, controller);

		List<Level> levels = home.getLevels();

		createTabs(home, preferences);
		final ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				LevelLabel selectedComponent = levelSpinnerControl.getSelectedComponent();
				controller.setSelectedLevel(selectedComponent.getLevel());
			}
		};
		this.levelSpinnerControl.addChangeListener(changeListener);
		this.levelSpinnerControl.addOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				LevelLabel selectedComponent = levelSpinnerControl.getSelectedComponent();
				if (selectedComponent != null) {
					controller.setSelectedLevel(selectedComponent.getLevel());
					controller.modifySelectedLevel();
				}
				return false;
			}
		});

		// Add listeners to levels to maintain tabs name and order
		final PropertyChangeListener levelChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				if (Level.Property.NAME.name().equals(ev.getPropertyName())) {
					int index = home.getLevels().indexOf(ev.getSource());
					levelSpinnerControl.setTitleAt(index, (String) ev.getNewValue());
					updateTabComponent(home, index);
				} else if (Level.Property.VIEWABLE.name().equals(ev.getPropertyName())) {
					updateTabComponent(home, home.getLevels().indexOf(ev.getSource()));
				} else if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())
						|| Level.Property.ELEVATION_INDEX.name().equals(ev.getPropertyName())) {
					levelSpinnerControl.removeChangeListener(changeListener);
					levelSpinnerControl.removeAll();
					createTabs(home, preferences);
					updateSelectedTab(home);
					levelSpinnerControl.addChangeListener(changeListener);
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						getActivity().invalidateOptionsMenu();
					}
				});
			}
		};
		for (Level level : levels) {
			level.addPropertyChangeListener(levelChangeListener);
		}
		home.addLevelsListener(new CollectionListener<Level>() {
			public void collectionChanged(CollectionEvent<Level> ev) {
				levelSpinnerControl.removeChangeListener(changeListener);
				switch (ev.getType()) {
					case ADD:
						levelSpinnerControl.insertTab(ev.getItem().getName(), null, new LevelLabel(ev.getItem()), ev.getIndex());
						Renovations3DActivity.logFireBaseLevelUp("AddLevel", ev.getItem().getName());
						updateTabComponent(home, ev.getIndex());
						ev.getItem().addPropertyChangeListener(levelChangeListener);
						break;
					case DELETE:
						ev.getItem().removePropertyChangeListener(levelChangeListener);
						levelSpinnerControl.remove(ev.getIndex());
						Renovations3DActivity.logFireBaseLevelUp("DeleteLevel", "Idx: " + ev.getIndex());
						break;
				}
				updateLayout(home);
				levelSpinnerControl.addChangeListener(changeListener);

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run()
					{
						getActivity().invalidateOptionsMenu();
					}
				});
			}
		});

		home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				levelSpinnerControl.removeChangeListener(changeListener);
				updateSelectedTab(home);
				levelSpinnerControl.addChangeListener(changeListener);
			}
		});

		home.addPropertyChangeListener(Home.Property.ALL_LEVELS_SELECTION, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				planComponent.repaint();
			}
		});

		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new LanguageChangeListener(this));

		preferences.addPropertyChangeListener(UserPreferences.Property.UPDATES_MINIMUM_DATE,new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ev) {
				if (rootView != null) {
					if (Renovations3DActivity.SHOW_PAGER_BUTTONS) {
						rootView.findViewById(R.id.planLeftSwiper).setVisibility(View.VISIBLE);
						rootView.findViewById(R.id.planRightSwiper).setVisibility(View.VISIBLE);
					} else {
						rootView.findViewById(R.id.planLeftSwiper).setVisibility(View.INVISIBLE);
						rootView.findViewById(R.id.planRightSwiper).setVisibility(View.INVISIBLE);
					}
					if (Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS) {
						rootView.findViewById(R.id.planZoomIn).setVisibility(View.VISIBLE);
						rootView.findViewById(R.id.planZoomOut).setVisibility(View.VISIBLE);
					} else {
						rootView.findViewById(R.id.planZoomIn).setVisibility(View.INVISIBLE);
						rootView.findViewById(R.id.planZoomOut).setVisibility(View.INVISIBLE);
					}
				}
			}
		});


		// auto reset for label creates
		home.addLabelsListener(new CollectionListener<Label>() {
			@Override
			public void collectionChanged(CollectionEvent<Label> collectionEvent) {
				if (planController.getMode() == PlanController.Mode.LABEL_CREATION) {
					// run this code after everything has finished in the controller (invoke later)
					Handler h = new Handler();
					h.post(new Runnable() {
						@Override
						public void run() {
							setMode(PlanController.Mode.SELECTION);
							resetToolSpinnerToMode();
						}
					});
				}
			}
		});

		// auto reset for room creates
		planController.addPropertyChangeListener(PlanController.Property.MODIFICATION_STATE, new  PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (planController.getMode() == PlanController.Mode.ROOM_CREATION &&
						((Boolean) evt.getOldValue()).booleanValue() && !((Boolean) evt.getNewValue()).booleanValue()) {
					// run this code after everything has finished in the controller (invoke later)
					Handler h = new Handler();
					h.post(new Runnable()
					{
						@Override
						public void run()
						{
							setMode(PlanController.Mode.SELECTION);
							resetToolSpinnerToMode();
						}
					});
				}
			}
		});


		//tutorial listener
		planController.addPropertyChangeListener(PlanController.Property.MODIFICATION_STATE, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (planController.getMode() == PlanController.Mode.WALL_CREATION &&
						((Boolean) evt.getOldValue()).booleanValue() && !((Boolean) evt.getNewValue()).booleanValue()) {
					((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.WALL_CREATION_FINISHED);
				}
			}
		});

		//tutorial listener
		home.addWallsListener(new CollectionListener<Wall>()
		{
			@Override
			public void collectionChanged(CollectionEvent<Wall> collectionEvent)
			{
				if (collectionEvent.getType() == CollectionEvent.Type.ADD) {
					 ((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.WALL_PLACED);
				}
			}
		});

		//tutorial listener
		home.addRoomsListener(new CollectionListener<Room>()
		{
			@Override
			public void collectionChanged(CollectionEvent<Room> collectionEvent)
			{
				if (collectionEvent.getType() == CollectionEvent.Type.ADD) {
					((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.CREATE_ROOM_FINISHED);
				}
			}
		});

		//tutorial listener
		home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture>()
		{
			@Override
			public void collectionChanged(CollectionEvent<HomePieceOfFurniture> collectionEvent)
			{
				if (collectionEvent.getType() == CollectionEvent.Type.ADD) {
					if(collectionEvent.getItem() instanceof HomeDoorOrWindow) {
						((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.DOOR_ADDED);
					} else {
						((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.FURNITURE_ADDED);
					}
				}
			}
		});
	}


	/**
	 * Creates and returns the main plan component displayed and layout by this component.
	 */
	protected PlanComponent createPlanComponent(final Home home, final UserPreferences preferences,
												final PlanController controller) {
		PlanComponent pc = new PlanComponent();
		pc.init(home, preferences, controller, this);
		return pc;
	}

  /**
   * Updates tab component with a label that will display tab text outlined by selection color
   * when all objects are selected at all levels.
   */
	private void updateTabComponent(final Home home, int i) {
		//TODO: this should in fact ensure the tab/level is selected in the drop down and also on screen
	}


	private boolean redoEnabled = false;
	private boolean undoEnabled = false;
	private String redoText = null;
	private String undoText = null;
	private boolean joinWallsEnabled = false;
	private boolean addRoomPointEnabled = false;
	private boolean deleteRoomPointEnabled = false;

	public void setEnabled(HomeView.ActionType actionType, boolean enabled) {
		if (actionType == HomeView.ActionType.UNDO) {
			undoEnabled = enabled;
			if (mOptionsMenu != null) {
				MenuItem undoItem = mOptionsMenu.findItem(R.id.editUndo);
				if (undoItem != null)
					undoItem.setEnabled(enabled);
			}
		} else if (actionType == HomeView.ActionType.REDO) {
			redoEnabled = enabled;
			if (mOptionsMenu != null) {
				MenuItem redoItem = mOptionsMenu.findItem(R.id.editRedo);
				if (redoItem != null)
					redoItem.setEnabled(enabled);
			}
		} else if (actionType == HomeView.ActionType.JOIN_WALLS) {
			joinWallsEnabled = enabled;
		}	else if (actionType == HomeView.ActionType.ADD_ROOM_POINT) {
			addRoomPointEnabled = enabled;
		}	else if (actionType == HomeView.ActionType.DELETE_ROOM_POINT) {
			deleteRoomPointEnabled = enabled;
		}

	}

	public void setNameAndShortDescription(HomeView.ActionType actionType, String text)
	{
		if (actionType == HomeView.ActionType.REDO) {
			redoText = text != null ? text.replace("redoText ", "") : null;
			if (mOptionsMenu != null && text != null) {
				MenuItem redoItem = mOptionsMenu.findItem(R.id.editRedo);
				if (redoItem != null) {
					Renovations3DActivity.setIconizedMenuTitle(redoItem, redoText, R.drawable.edit_redo, getContext());
				}
			}
		} else {
			//System.out.println("Action text updated " + actionType + " " + text );
		}
	}

	/**
	 * Preferences property listener bound to this component with a weak reference to avoid
	 * strong link between preferences and this component.
	 */
	private static class LanguageChangeListener implements PropertyChangeListener {
		private WeakReference<MultipleLevelsPlanPanel> planPanel;

		public LanguageChangeListener(MultipleLevelsPlanPanel planPanel) {
			this.planPanel = new WeakReference<MultipleLevelsPlanPanel>(planPanel);
		}

		public void propertyChange(PropertyChangeEvent ev) {
			// If help pane was garbage collected, remove this listener from preferences
			MultipleLevelsPlanPanel planPanel = this.planPanel.get();
			UserPreferences preferences = (UserPreferences) ev.getSource();
			if (planPanel == null) {
				preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
			} else {
				// Update create level tooltip in new locale
				//String createNewLevelTooltip = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.ShortDescription");
				//planPanel.multipleLevelsTabbedPane.setToolTipTextAt(planPanel.multipleLevelsTabbedPane.getTabCount() - 1, createNewLevelTooltip);

				planPanel.updateToolNames();
			}
		}
	}

	/**
	 * Creates the tabs from <code>home</code> levels.
	 */
	private void createTabs(Home home, UserPreferences preferences) {
		List<Level> levels = home.getLevels();
		for (int i = 0; i < levels.size(); i++) {
			Level level = levels.get(i);
			this.levelSpinnerControl.addTab(level.getName(), new LevelLabel(level));
			updateTabComponent(home, i);
		}
	}

	/**
	 * Selects the tab matching the selected level in <code>home</code>.
	 */
	private void updateSelectedTab(Home home) {
		List<Level> levels = home.getLevels();
		Level selectedLevel = home.getSelectedLevel();
		if (levels.size() >= 2 && selectedLevel != null) {
			this.levelSpinnerControl.setSelectedIndex(levels.indexOf(selectedLevel));
			displayPlanComponentAtSelectedIndex(home);
		}
		updateLayout(home);
	}

	/**
	 * Display the plan component at the selected tab index.
	 */
	private void displayPlanComponentAtSelectedIndex(Home home) {
		//FIXME: this should select the drop down and load the right plan
	}

	/**
	 * Switches between a simple plan component view and a tabbed pane for multiple levels.
	 */
	private void updateLayout(Home home) {
		//PJPJPJ card layout just dropped as there is only the multi system now
	}

	/**
	 * Layouts the components displayed by this panel.
	 */
	private void layoutComponents() {
		//PJPJPJ I only use the multiple system now
	}

/*	@Override
	public void setTransferHandler(TransferHandler newHandler) {
		this.planComponent.setTransferHandler(newHandler);
	}

	@Override
	public void setComponentPopupMenu(JPopupMenu tooltippopup) {
		this.planComponent.setComponentPopupMenu(tooltippopup);
	}

	@Override
	public void addMouseMotionListener(final MouseMotionListener l) {
		this.planComponent.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent ev) {
				l.mouseMoved(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseDragged(MouseEvent ev) {
				l.mouseDragged(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}
		});
	}

	@Override
	public void addMouseListener(final MouseListener l) {
		this.planComponent.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent ev) {
				l.mouseReleased(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mousePressed(MouseEvent ev) {
				l.mousePressed(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseExited(MouseEvent ev) {
				l.mouseExited(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseEntered(MouseEvent ev) {
				l.mouseEntered(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseClicked(MouseEvent ev) {
				l.mouseClicked(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}
		});
	}

	@Override
	public void addFocusListener(final FocusListener l) {
		FocusListener componentFocusListener = new FocusListener() {
			public void focusGained(FocusEvent ev) {
				l.focusGained(new FocusEvent(MultipleLevelsPlanPanel.this, FocusEvent.FOCUS_GAINED, ev.isTemporary(), ev.getOppositeComponent()));
			}

			public void focusLost(FocusEvent ev) {
				l.focusLost(new FocusEvent(MultipleLevelsPlanPanel.this, FocusEvent.FOCUS_LOST, ev.isTemporary(), ev.getOppositeComponent()));
			}
		};
		this.planComponent.addFocusListener(componentFocusListener);
		this.multipleLevelsTabbedPane.addFocusListener(componentFocusListener);
	}*/

	/**
	 * Returns an image of the plan for transfer purpose.
	 */
	public Object createTransferData(DataType dataType) {
		return this.planComponent.createTransferData(dataType);
	}

	/**
	 * Returns <code>true</code> if the plan component supports the given format type.
	 */
	public boolean isFormatTypeSupported(FormatType formatType) {
		return this.planComponent.isFormatTypeSupported(formatType);
	}

	/**
	 * Writes the plan in the given output stream at SVG (Scalable Vector Graphics) format if this is the requested format.
	 */
	public void exportData(OutputStream out, FormatType formatType, Properties settings) throws IOException {
		this.planComponent.exportData(out, formatType, settings);
	}

	/**
	 * Sets rectangle selection feedback coordinates.
	 */
	public void setRectangleFeedback(float x0, float y0, float x1, float y1) {
		this.planComponent.setRectangleFeedback(x0, y0, x1, y1);
	}

	/**
	 * Ensures selected items are visible in the plan displayed by this component and moves
	 * its scroll bars if needed.
	 */
	public void makeSelectionVisible() {
		this.planComponent.makeSelectionVisible();
	}

	/**
	 * Ensures the point at (<code>x</code>, <code>y</code>) is visible in the plan displayed by this component,
	 * moving its scroll bars if needed.
	 */
	public void makePointVisible(float x, float y) {
		this.planComponent.makePointVisible(x, y);
	}

	/**
	 * Returns the scale used to display the plan displayed by this component.
	 */
	public float getScale() {
		return this.planComponent.getScale();
	}

	/**
	 * Sets the scale used to display the plan displayed by this component.
	 */
	public void setScale(float scale) {
		this.planComponent.setScale(scale);
	}

	/**
	 * Moves the plan displayed by this component from (dx, dy) unit in the scrolling zone it belongs to.
	 */
	public void moveView(float dx, float dy) {
		this.planComponent.moveView(dx, dy);
	}

	/**
	 * Returns <code>x</code> converted in model coordinates space.
	 */
	public float convertXPixelToModel(int x) {
		return this.planComponent.convertXPixelToModel(SwingTools.convertPoint(this, x, 0, this.planComponent).x);
	}

	/**
	 * Returns <code>y</code> converted in model coordinates space.
	 */
	public float convertYPixelToModel(int y) {
		return this.planComponent.convertYPixelToModel(SwingTools.convertPoint(this, 0, y, this.planComponent).y);
	}

	/**
	 * Returns <code>x</code> converted in screen coordinates space.
	 */
	public int convertXModelToScreen(float x) {
		return this.planComponent.convertXModelToScreen(x);
	}

	/**
	 * Returns <code>y</code> converted in screen coordinates space.
	 */
	public int convertYModelToScreen(float y) {
		return this.planComponent.convertYModelToScreen(y);
	}

	/**
	 * Returns the length in centimeters of a pixel with the current scale.
	 */
	public float getPixelLength() {
		return this.planComponent.getPixelLength();
	}

	/**
	 * Returns the coordinates of the bounding rectangle of the <code>text</code> displayed at
	 * the point (<code>x</code>,<code>y</code>).
	 */
	public float[][] getTextBounds(String text, TextStyle style, float x, float y, float angle) {
		return this.planComponent.getTextBounds(text, style, x, y, angle);
	}

	/**
	 * Sets the cursor of this component.
	 */
	public void setCursor(CursorType cursorType) {
		this.planComponent.setCursor(cursorType);
	}

	/**
	 * Sets the cursor of this component.
	 */
/*	@Override
	public void setCursor(Cursor cursor) {
		this.planComponent.setCursor(cursor);
	}*/

	/**
	 * Returns the cursor of this component.
	 */
/*	@Override
	public Cursor getCursor() {
		return this.planComponent.getCursor();
	}*/

	/**
	 * Sets tool tip text displayed as feedback.
	 */
	public void setToolTipFeedback(String toolTipFeedback, float x, float y) {
		this.planComponent.setToolTipFeedback(toolTipFeedback, x, y);
	}

	/**
	 * Set properties edited in tool tip.
	 */
	public void setToolTipEditedProperties(EditableProperty[] toolTipEditedProperties, Object[] toolTipPropertyValues,
										   float x, float y) {
		this.planComponent.setToolTipEditedProperties(toolTipEditedProperties, toolTipPropertyValues, x, y);
	}

	/**
	 * Deletes tool tip text from screen.
	 */
	public void deleteToolTipFeedback() {
		this.planComponent.deleteToolTipFeedback();
	}

	/**
	 * Sets whether the resize indicator of selected wall or piece of furniture
	 * should be visible or not.
	 */
	public void setResizeIndicatorVisible(boolean visible) {
		this.planComponent.setResizeIndicatorVisible(visible);
	}

	/**
	 * Sets the location point for alignment feedback.
	 */
	public void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass, Selectable alignedObject, float x,
									 float y, boolean showPoint) {
		this.planComponent.setAlignmentFeedback(alignedObjectClass, alignedObject, x, y, showPoint);
	}

	/**
	 * Sets the points used to draw an angle in the plan displayed by this component.
	 */
	public void setAngleFeedback(float xCenter, float yCenter, float x1, float y1, float x2, float y2) {
		this.planComponent.setAngleFeedback(xCenter, yCenter, x1, y1, x2, y2);
	}

	/**
	 * Sets the feedback of dragged items drawn during a drag and drop operation,
	 * initiated from outside of the plan displayed by this component.
	 */
	public void setDraggedItemsFeedback(List<Selectable> draggedItems) {
		this.planComponent.setDraggedItemsFeedback(draggedItems);
	}

	/**
	 * Sets the given dimension lines to be drawn as feedback.
	 */
	public void setDimensionLinesFeedback(List<DimensionLine> dimensionLines) {
		this.planComponent.setDimensionLinesFeedback(dimensionLines);
	}

	/**
	 * Deletes all elements shown as feedback.
	 */
	public void deleteFeedback() {
		this.planComponent.deleteFeedback();
	}

	/**
	 * Returns <code>true</code> if the given coordinates belong to the plan displayed by this component.
	 */
	public boolean canImportDraggedItems(List<Selectable> items, int x, int y) {
		//	JViewport viewport = this.planScrollPane.getViewport();
		//	Point point = SwingUtilities.convertPoint(this, x, y, viewport);
		//	return viewport.contains(point);
		return false;
	}

	/**
	 * Returns the size of the given piece of furniture in the horizontal plan.
	 */
	public float [] getPieceOfFurnitureSizeInPlan(HomePieceOfFurniture piece) {
		return this.planComponent.getPieceOfFurnitureSizeInPlan(piece);
	}

	/**
	 * Returns <code>true</code> if this component is able to compute the size of horizontally rotated furniture.
	 */
	public boolean isFurnitureSizeInPlanSupported() {
		return this.planComponent.isFurnitureSizeInPlanSupported();
	}

	/**
	 * Returns the component used as an horizontal ruler for the plan displayed by this component.
	 */
	public com.eteks.sweethome3d.viewcontroller.View getHorizontalRuler() {
		return this.planComponent.getHorizontalRuler();
	}

	/**
	 * Returns the component used as a vertical ruler for the plan displayed by this component.
	 */
	public com.eteks.sweethome3d.viewcontroller.View getVerticalRuler() {
		return this.planComponent.getVerticalRuler();
	}

	/**
	 * Prints the plan component.
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		return this.planComponent.print(graphics, pageFormat, pageIndex);
	}

	/**
	 * Returns the preferred scale to print the plan component.
	 */
	public float getPrintPreferredScale(Graphics graphics, PageFormat pageFormat) {
		return this.planComponent.getPrintPreferredScale(graphics, pageFormat);
	}

	/**
	 * Returns the preferred scale to ensure it can be fully printed on the given print zone.
	 */
	public float getPrintPreferredScale(float preferredWidth, float preferredHeight) {
		return ((PlanView)this.planComponent).getPrintPreferredScale(preferredWidth, preferredHeight);
	}

	/**
	 * A dummy label used to track tabs matching levels.
	 */
	public static class LevelLabel  {
		private final Level level;

		public LevelLabel(Level level) {
			this.level = level;
		}

		public Level getLevel() {
			return this.level;
		}
	}
}
