package com.eteks.renovations3d.android;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.swingish.JComponent;
import com.eteks.renovations3d.android.swingish.JOptionPane;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.renovations3d.android.utils.DrawableView;
import com.eteks.renovations3d.android.utils.LevelSpinnerControl;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanController.EditableProperty;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.mindblowing.renovations3d.R;

import org.jogamp.vecmath.Point2f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import javaawt.EventQueue;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.geom.AffineTransform;
import javaawt.print.PageFormat;
import javaawt.print.PrinterException;
import javaxswing.undo.CannotRedoException;
import javaxswing.undo.CannotUndoException;


public class MultipleLevelsPlanPanel extends JComponent implements PlanView
{
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

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: find out why a new home gets a double onViewCreate
		if (rootView == null)
		{
			rootView = inflater.inflate(R.layout.multiple_level_plan_panel, container, false);

			if (initialized)
			{
				this.setHasOptionsMenu(true);

				drawableView = (DrawableView) rootView.findViewById(R.id.drawableView);
				drawableView.setDrawer(this);

				planComponent.setDrawableView(drawableView);

				this.levelSpinnerControl = new LevelSpinnerControl(this.getContext());

				// from the constructor but placed here now so views are set
				createComponents(home, preferences, planController);
				layoutComponents();
				updateSelectedTab(home);
			}


			// make the left and right swipers work
			Button planLeftSwiper = (Button)rootView.findViewById(R.id.planLeftSwiper);
			planLeftSwiper.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					((Renovations3DActivity)getActivity()).mViewPager.setCurrentItem(0, true);
				}
			});
			Button planRightSwiper = (Button)rootView.findViewById(R.id.planRightSwiper);
			planRightSwiper.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					((Renovations3DActivity)getActivity()).mViewPager.setCurrentItem(2, true);
				}
			});
		}
		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser)
		{
			// this gets called heaps of time, wait until we have an activity
			if (getActivity() != null)
			{
				JOptionPane.possiblyShowWelcomeScreen(getActivity(), WELCOME_SCREEN_UNWANTED, R.string.planview_welcometext, preferences);
			}

			resetToSelectTool = true;

			repaint();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onDestroy()
	{
		PlanComponent.PieceOfFurnitureModelIcon.destroyUniverse();
		super.onDestroy();
	}



	AdapterView.OnItemSelectedListener planToolSpinnerListener = new AdapterView.OnItemSelectedListener()
	{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			switch (position)
			{
				case 0://R.id.planSelect:
					finishCurrentMode();
					setMode(PlanController.Mode.SELECTION);
					break;
				case 1://R.id.createWalls:
					finishCurrentMode();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
					setMode(PlanController.Mode.WALL_CREATION);
					break;
				case 2://R.id.createRooms:
					finishCurrentMode();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
					setMode(PlanController.Mode.ROOM_CREATION);
					break;
				case 3://R.id.createPolyLines:
					finishCurrentMode();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
					planController.setMode(PlanController.Mode.POLYLINE_CREATION);
					break;
				case 4://R.id.createDimensions:
					finishCurrentMode();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), R.string.double_tap_finish, Toast.LENGTH_SHORT).show();
					setMode(PlanController.Mode.DIMENSION_LINE_CREATION);
					break;
				case 5://R.id.createText:
					finishCurrentMode();
					// note single tap works for this one
					setMode(PlanController.Mode.LABEL_CREATION);
					break;
				case 6://R.id.planPan:
					finishCurrentMode();
					setMode(PlanController.Mode.PANNING);
					break;

			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{
			// this should never happen, no real plan here
		}
	};

	private void finishCurrentMode()
	{
		PlanController.Mode currentMode = planController.getMode();

		if (currentMode == PlanController.Mode.DIMENSION_LINE_CREATION
			|| currentMode == PlanController.Mode.WALL_CREATION
			|| currentMode == PlanController.Mode.ROOM_CREATION
			|| currentMode == PlanController.Mode.POLYLINE_CREATION)
		{
			// need to simulate a press on the last dragged position to make it stick
			Point2f lastDrag = planComponent.getLastDragLocation();
			if (lastDrag.x > 0 && lastDrag.y > 0)
			{
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
	public boolean getIsControlKeyOn()
	{
		// not sure why, something to do with restore lifecycle?
		if (mOptionsMenu != null)
		{
			MenuItem cntlKey = mOptionsMenu.findItem(R.id.controlKeyOneTimer);
			// might be missing if we are reloading a home
			if (cntlKey != null)
			{
				boolean isChecked = cntlKey.isChecked();
				cntlKey.setChecked(false);
				return isChecked;
			}
		}

		return false;
	}

	//copied from HomeController as we can't touch the EDT thread like they do
	public void setMode(PlanController.Mode mode)
	{
		if (planController.getMode() != mode)
		{
			final String actionKey;
			if (mode == PlanController.Mode.WALL_CREATION)
			{
				actionKey = HomeView.ActionType.CREATE_WALLS.name();
			}
			else if (mode == PlanController.Mode.ROOM_CREATION)
			{
				actionKey = HomeView.ActionType.CREATE_ROOMS.name();
			}
			else if (mode == PlanController.Mode.POLYLINE_CREATION)
			{
				actionKey = HomeView.ActionType.CREATE_POLYLINES.name();
			}
			else if (mode == PlanController.Mode.DIMENSION_LINE_CREATION)
			{
				actionKey = HomeView.ActionType.CREATE_DIMENSION_LINES.name();
			}
			else if (mode == PlanController.Mode.LABEL_CREATION)
			{
				actionKey = HomeView.ActionType.CREATE_LABELS.name();
			}
			else
			{
				actionKey = null;
			}

			if (actionKey != null && !this.preferences.isActionTipIgnored(actionKey))
			{
				Thread t = new Thread(new Runnable()
				{
					public void run()
					{
						if (((Renovations3DActivity) getActivity()).renovations3D.getHomeController() != null &&
								((Renovations3DActivity) getActivity()).renovations3D.getHomeController().getView().showActionTipMessage(actionKey))
						{
							preferences.setActionTipIgnored(actionKey);
						}
					}
				});
				t.start();
			}

			planController.setMode(mode);
		}
	}



	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		mOptionsMenu = menu;
		inflater.inflate(R.menu.plan_component_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		menu.findItem(R.id.editUndo).setEnabled(false);// nothing to undo at first


		// use the new default name without teh number format
		String planeLevelMenuName = preferences.getLocalizedString(com.eteks.sweethome3d.viewcontroller.PlanController.class, "levelName").replace(" %d", "") + "...";
		menu.findItem(R.id.planLevelMenu).setTitle(planeLevelMenuName);
		menu.findItem(R.id.planAddLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_LEVEL.Name"));
		menu.findItem(R.id.planAddLevelAtSame).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_LEVEL_AT_SAME_ELEVATION.Name"));
		menu.findItem(R.id.planModifyLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_LEVEL.Name"));
		menu.findItem(R.id.planDeleteLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE_LEVEL.Name"));


		String subMenuTile = preferences.getLocalizedString(com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController.class, "wizard.title");
		//TODO: localize this properly not this madness
		//subMenuTile = subMenuTile.replace(" wizard", "...");// just en only
		// let's try rip out every ting after last space the append
		if (subMenuTile.lastIndexOf(" ") > 0)
		{
			subMenuTile = subMenuTile.substring(0, subMenuTile.lastIndexOf(" "));
		}
		subMenuTile += "...";
		menu.findItem(R.id.bgImageMenu).setTitle(subMenuTile);

		sortOutBackgroundMenu(menu);


		String redoName = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "REDO.Name");
		SpannableStringBuilder builder2 = new SpannableStringBuilder("* " + redoName);// it will replace "*" with icon
		builder2.setSpan(new ImageSpan(getActivity(), R.drawable.edit_redo), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		menu.findItem(R.id.editRedo).setTitle(builder2);
		menu.findItem(R.id.editRedo).setTitleCondensed(redoName);
		menu.findItem(R.id.editRedo).setEnabled(false);// nothing to redo at first

		String copy = getActivity().getResources().getString(android.R.string.copy);
		SpannableStringBuilder builder = new SpannableStringBuilder("* " + copy);// it will replace "*" with icon
		builder.setSpan(new ImageSpan(getActivity(), R.drawable.edit_copy), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		menu.findItem(R.id.controlKeyOneTimer).setTitle(builder);
		menu.findItem(R.id.controlKeyOneTimer).setTitleCondensed(copy);

		menu.findItem(R.id.delete).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE.Name"));

		updateToolNames();

		toolSpinner = (Spinner) MenuItemCompat.getActionView(menu.findItem(R.id.toolSelectSpinner));
		toolSpinner.setPadding(toolSpinner.getPaddingLeft(), 0, toolSpinner.getPaddingRight(), toolSpinner.getPaddingBottom());
		toolSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, toolNames)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				 // why can this be null?
				if (getActivity() != null)
				{
					Configuration configuration = getActivity().getResources().getConfiguration();
					int screenWidthDp = configuration.screenWidthDp;
					boolean toolsWide = screenWidthDp > TOOLS_WIDE_MIN_DP;

					View view = getTextView(position, toolsWide);
					view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);

					return view;
				}
				else
				{
					return convertView;
				}
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
				return getTextView(position, true);
			}

			public View getTextView(int position, boolean withText)
			{
				TextView ret = new TextView(getActivity());
				ret.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
				String spanText = "* " + (withText ? toolNames[position] : "");
				int drawRes = toolIcon[position];
				SpannableStringBuilder builder = new SpannableStringBuilder(spanText);// it will replace "*" with icon
				builder.setSpan(new ImageSpan(getActivity(), drawRes), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ret.setText(builder);

				return ret;
			}
		}); // set the adapter to provide layout of rows and content
		toolSpinner.setOnItemSelectedListener(planToolSpinnerListener);

		levelsSpinner = (Spinner) MenuItemCompat.getActionView(menu.findItem(R.id.levelsSpinner));
		// PJ no icons no need to shift up levelsSpinner.setPadding(levelsSpinner.getPaddingLeft(), 0, levelsSpinner.getPaddingRight(), levelsSpinner.getPaddingBottom());

		// possibly on a double onCreateView call this gets called and the levelSpinnerControl has not yet been created so ignore the call this time round
		if(levelSpinnerControl != null)
			levelSpinnerControl.setSpinner(levelsSpinner);


		menu.findItem(R.id.levelsSpinner).setVisible(home.getLevels().size() > 0);

	}


	private BackgroundImage currentBackgroundImage()
	{
		Level selectedLevel = this.home.getSelectedLevel();
		Level backgroundImageLevel = null;
		if (selectedLevel != null)
		{
			// Search the first level at same elevation with a background image
			List<Level> levels = this.home.getLevels();
			for (int i = levels.size() - 1; i >= 0; i--)
			{
				Level level = levels.get(i);
				if (level.getElevation() == selectedLevel.getElevation()
						&& level.getElevationIndex() <= selectedLevel.getElevationIndex()
						&& level.isViewable()
						&& level.getBackgroundImage() != null) //PJ this is taken from plancomp paint, but visible check removed
				{
					backgroundImageLevel = level;
					break;
				}
			}
		}
		return backgroundImageLevel == null ? this.home.getBackgroundImage() : backgroundImageLevel.getBackgroundImage();
	}

	private void sortOutBackgroundMenu(Menu menu)
	{
		final BackgroundImage backgroundImage = currentBackgroundImage();

		String importModifyString  = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_BACKGROUND_IMAGE.Name");
		if(backgroundImage != null)
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


	private void updateToolNames()
	{
		toolNames = new String[]{
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "SELECT.Name"),
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_WALLS.Name"),
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_ROOMS.Name"),
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_POLYLINES.Name"),
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_DIMENSION_LINES.Name"),
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "CREATE_LABELS.Name"),
				preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "PAN.Name")
		};
	}

	private String[] toolNames;
	private int[] toolIcon = new int[]{
			R.drawable.plan_select,
			R.drawable.plan_create_walls,
			R.drawable.plan_create_rooms,
			R.drawable.plan_create_polylines,
			R.drawable.plan_create_dimension_lines,
			R.drawable.plan_create_labels,
			R.drawable.plan_pan
	};
	private PlanController.Mode[] modesInSpinnerOrder = new PlanController.Mode[]{
			PlanController.Mode.SELECTION,
			PlanController.Mode.WALL_CREATION,
			PlanController.Mode.ROOM_CREATION,
			PlanController.Mode.POLYLINE_CREATION,
			PlanController.Mode.DIMENSION_LINE_CREATION,
			PlanController.Mode.LABEL_CREATION,
			PlanController.Mode.PANNING
	};

	private void resetToolSpinnerToMode()
	{
		PlanController.Mode mode = planController.getMode();
		toolSpinner.setOnItemSelectedListener(null);
		int position = Arrays.asList(modesInSpinnerOrder).indexOf(mode);
		toolSpinner.setSelection(position);
		toolSpinner.setOnItemSelectedListener(planToolSpinnerListener);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		mOptionsMenu = menu;

		MenuItem itemLocked = menu.findItem(R.id.lockCheck);
		itemLocked.setChecked(home.isBasePlanLocked());

		int iconId = itemLocked.isChecked() ? R.drawable.plan_locked : R.drawable.plan_unlocked;
		String actionName = itemLocked.isChecked() ? "UNLOCK_BASE_PLAN.Name" : "LOCK_BASE_PLAN.Name";
		String lockedText = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, actionName);
		SpannableStringBuilder builder = new SpannableStringBuilder("* " + lockedText);// it will replace "*" with icon
		builder.setSpan(new ImageSpan(getActivity(), iconId), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		itemLocked.setTitle(builder);
		itemLocked.setTitleCondensed(lockedText);
		itemLocked.setIcon(iconId);

		sortOutBackgroundMenu(menu);


		MenuItem cntlMI = menu.findItem(R.id.controlKeyOneTimer);
		if(planController.getMode() == PlanController.Mode.WALL_CREATION || planController.getMode() == PlanController.Mode.POLYLINE_CREATION)
		{
			String arcText = SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.WallPanel.class, "arcExtentLabel.text");
			//TODO: make up an icon for bend walls
			cntlMI.setTitle(arcText);
			cntlMI.setTitleCondensed(arcText);
			cntlMI.setEnabled(true);
		}
		else if(planController.getMode() == PlanController.Mode.SELECTION)
		{
			String cntlText = getActivity().getResources().getString(android.R.string.copy);
			int cntlRes = R.drawable.edit_copy;
			SpannableStringBuilder builder2 = new SpannableStringBuilder("* " + cntlText);// it will replace "*" with icon
			builder2.setSpan(new ImageSpan(getActivity(), cntlRes), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			cntlMI.setTitle(builder2);
			cntlMI.setTitleCondensed(cntlText);
			cntlMI.setEnabled(true);
		}
		else
		{
			cntlMI.setTitle("");
			cntlMI.setTitleCondensed("");
			cntlMI.setEnabled(false);
		}

		menu.findItem(R.id.planSelectLasso).setChecked(this.planComponent.selectLasso);
		menu.findItem(R.id.planSelectMultiple).setChecked(this.planComponent.selectMultiple);

		menu.findItem(R.id.alignment).setChecked(this.planComponent.alignmentActivated);
		menu.findItem(R.id.magnetism).setEnabled(preferences.isMagnetismEnabled());
		menu.findItem(R.id.magnetism).setChecked(preferences.isMagnetismEnabled() && !this.planComponent.magnetismToggled);

		if (resetToSelectTool && planController.getMode() != PlanController.Mode.PANNING)
		{
			setMode(PlanController.Mode.SELECTION);
		}
		resetToSelectTool = false;

		resetToolSpinnerToMode();

		menu.findItem(R.id.levelsSpinner).setVisible(home.getLevels().size() > 0);

		// undo doesn't get text updated as it is just an icon
		menu.findItem(R.id.editUndo).setEnabled(undoEnabled);

		// use the new default name without the number format
		String planeLevelMenuName = preferences.getLocalizedString(com.eteks.sweethome3d.viewcontroller.PlanController.class, "levelName").replace(" %d", "") + "...";
		menu.findItem(R.id.planLevelMenu).setTitle(planeLevelMenuName);
		menu.findItem(R.id.planAddLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_LEVEL.Name"));
		menu.findItem(R.id.planAddLevelAtSame).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "ADD_LEVEL_AT_SAME_ELEVATION.Name"));
		menu.findItem(R.id.planModifyLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "MODIFY_LEVEL.Name"));
		boolean canModLevel = ((Renovations3DActivity) getActivity()).renovations3D.getHome() != null && ((Renovations3DActivity) getActivity()).renovations3D.getHome().getSelectedLevel() != null;
		menu.findItem(R.id.planModifyLevel).setEnabled(canModLevel);
		menu.findItem(R.id.planDeleteLevel).setTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "DELETE_LEVEL.Name"));

		String redoName = redoText == null ? preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "REDO.Name") : redoText;
		SpannableStringBuilder builder3 = new SpannableStringBuilder("* " + redoName);// it will replace "*" with icon
		builder3.setSpan(new ImageSpan(getActivity(), R.drawable.edit_redo), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		menu.findItem(R.id.editRedo).setTitle(builder3);
		menu.findItem(R.id.editRedo).setTitleCondensed(redoName);
		menu.findItem(R.id.editRedo).setEnabled(redoEnabled);

		super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Renovations3DActivity renovations3DActivity = ((Renovations3DActivity) getActivity());
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.editUndo:
				try
				{

					if (renovations3DActivity.renovations3D.getHomeController() != null)
						renovations3DActivity.renovations3D.getHomeController().undo();
				}
				catch (Exception e)
				{
					//ignored, as the button should only be enabled when one undo is available (see HomeView addUndoSupportListener)
					// I'm getting NPE in a call to addLevel https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/crash?reportTypes=REPORT_TYPE_UNSPECIFIED&duration=2592000000
					e.printStackTrace();
				}
				return true;
			case R.id.editRedo:
				try
				{
					if (renovations3DActivity.renovations3D.getHomeController() != null)
						renovations3DActivity.renovations3D.getHomeController().redo();
				}
				catch (Exception e)
				{//ignored, as the button should only be enabled when one undo is available (see HomeView addUndoSupportListener)
					e.printStackTrace();
				}
				return true;
			case R.id.delete:
				planController.deleteSelection();
				return true;
			case R.id.planGoto3D:
				renovations3DActivity.mViewPager.setCurrentItem(3, true);
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
			case R.id.controlKeyOneTimer:
				//TODO: this guy needs to reflect the control option on anything, so duplication for select, but curve wall for create
				item.setChecked(!item.isChecked());
				return true;
			case R.id.lockCheck:
				item.setChecked(!item.isChecked());

				// this crash
				//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/aa60d8ac?duration=2592000000&appVersions=192					// is caused by this
				//http://stackoverflow.com/questions/7658725/android-java-lang-illegalargumentexception-invalid-payload-item-type
				//hence menuItem.setTitleCondensed(rawTitle);

				int iconId = item.isChecked() ? R.drawable.plan_locked : R.drawable.plan_unlocked;
				String actionName = item.isChecked() ? "UNLOCK_BASE_PLAN.Name" : "LOCK_BASE_PLAN.Name";
				String lockedText = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, actionName);
				SpannableStringBuilder builder = new SpannableStringBuilder("* " + lockedText);// it will replace "*" with icon
				builder.setSpan(new ImageSpan(getActivity(), iconId), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				item.setTitle(builder);
				item.setTitleCondensed(lockedText);
				item.setIcon(iconId);

				if (item.isChecked())
					planController.lockBasePlan();
				else
					planController.unlockBasePlan();
				return true;

			case R.id.bgImageImportModify:
				//PJ the import appears to modify if it's already there??
				if (renovations3DActivity.renovations3D.getHomeController() != null)
					renovations3DActivity.renovations3D.getHomeController().importBackgroundImage();
				return true;
			case R.id.bgImageToggleVis:
				if (renovations3DActivity.renovations3D.getHomeController() != null)
				{
					final BackgroundImage backgroundImage = currentBackgroundImage();
					if(backgroundImage != null)
					{
						if(backgroundImage.isVisible())
							renovations3DActivity.renovations3D.getHomeController().hideBackgroundImage();
						else
							renovations3DActivity.renovations3D.getHomeController().showBackgroundImage();
					}
				}
				return true;
			case R.id.bgImageDelete:
				if (renovations3DActivity.renovations3D.getHomeController() != null)
					renovations3DActivity.renovations3D.getHomeController().deleteBackgroundImage();
				return true;

			case R.id.alignment:
				item.setChecked(!item.isChecked());
				this.planComponent.alignmentActivated = item.isChecked();
				return true;
			case R.id.magnetism:
				item.setChecked(!item.isChecked());
				this.planComponent.magnetismToggled = !item.isChecked();// careful toggle != checked!
				return true;
			case R.id.planSelectLasso:
				item.setChecked(!item.isChecked());
				this.planComponent.selectLasso = item.isChecked();
				return true;
			case R.id.planSelectMultiple:
				item.setChecked(!item.isChecked());
				this.planComponent.selectMultiple = item.isChecked();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	private Home home;
	private UserPreferences preferences;
	private PlanController planController;

	private boolean rulersVisible = true;

	public void init(Home home, UserPreferences preferences2, PlanController planController)
	{
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
	public void paintComponent(Graphics g)
	{
		// don't bother painting if we are on the 3d view of a table view or something else
		if (this.getUserVisibleHint())
		{
			AffineTransform previousTransform = ((Graphics2D) g).getTransform();
			planComponent.paintComponent(g);
			((Graphics2D) g).setTransform(previousTransform);

			if (rulersVisible)
			{
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
								  final UserPreferences preferences, final PlanController controller)
	{
		// moved to init
		//this.planComponent = createPlanComponent(home, preferences, controller);

		List<Level> levels = home.getLevels();

		createTabs(home, preferences);

		final ChangeListener changeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent ev)
			{
				LevelLabel selectedComponent = levelSpinnerControl.getSelectedComponent();
				controller.setSelectedLevel(selectedComponent.getLevel());
			}
		};
		this.levelSpinnerControl.addChangeListener(changeListener);
		this.levelSpinnerControl.addOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				LevelLabel selectedComponent = levelSpinnerControl.getSelectedComponent();
				if(selectedComponent != null)
				{
					controller.setSelectedLevel(selectedComponent.getLevel());
					controller.modifySelectedLevel();
				}
				return false;
			}
		});

		// Add listeners to levels to maintain tabs name and order
		final PropertyChangeListener levelChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				if (Level.Property.NAME.name().equals(ev.getPropertyName()))
				{
					int index = home.getLevels().indexOf(ev.getSource());
					levelSpinnerControl.setTitleAt(index, (String) ev.getNewValue());
					updateTabComponent(home, index);
				}
				else if (Level.Property.VIEWABLE.name().equals(ev.getPropertyName()))
				{
					updateTabComponent(home, home.getLevels().indexOf(ev.getSource()));
				}
				else if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())
						|| Level.Property.ELEVATION_INDEX.name().equals(ev.getPropertyName()))
				{
					levelSpinnerControl.removeChangeListener(changeListener);
					levelSpinnerControl.removeAll();
					createTabs(home, preferences);
					updateSelectedTab(home);
					levelSpinnerControl.addChangeListener(changeListener);
				}
				getActivity().invalidateOptionsMenu();
			}
		};
		for (Level level : levels)
		{
			level.addPropertyChangeListener(levelChangeListener);
		}
		home.addLevelsListener(new CollectionListener<Level>()
		{
			public void collectionChanged(CollectionEvent<Level> ev)
			{
				levelSpinnerControl.removeChangeListener(changeListener);
				switch (ev.getType())
				{
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

				getActivity().invalidateOptionsMenu();
			}
		});

		home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				levelSpinnerControl.removeChangeListener(changeListener);
				updateSelectedTab(home);
				levelSpinnerControl.addChangeListener(changeListener);
			}
		});

		// PJ this.oneLevelPanel not used at all, nor is scrollpane
		//	this.oneLevelPanel = new JPanel(new BorderLayout());


		//PJ is this always true or always false?
		//if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6"))
		{
			home.addPropertyChangeListener(Home.Property.ALL_LEVELS_SELECTION, new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent ev)
				{
					planComponent.repaint();
				}
			});
		}

		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new LanguageChangeListener(this));
	}

	/**
	 * Creates and returns the main plan component displayed and layout by this component.
	 */
	protected PlanComponent createPlanComponent(final Home home, final UserPreferences preferences,
												final PlanController controller)
	{
		PlanComponent pc = new PlanComponent();
		pc.init(home, preferences, controller, this);
		return pc;
	}

	/**
	 * Updates tab component with a label that will display tab text outlined by selection color
	 * when all objects are selected at all levels.
	 */
	private void updateTabComponent(final Home home, int i)
	{

		//PJPJPJ what does less than 1.6 do?
	/*	if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6"))
		{
			JLabel tabLabel = new JLabel(this.multipleLevelsTabbedPane.getTitleAt(i))
			{
				@Override
				protected void paintComponent(Graphics g)
				{
					if (home.isAllLevelsSelection() && isEnabled())
					{
						Graphics2D g2D = (Graphics2D) g;
						// Draw text outline with half transparent selection color when all tabs are selected
						g2D.setPaint(planComponent.getSelectionColor());
						Composite oldComposite = g2D.getComposite();
						g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
						Font font = getFont();
						FontMetrics fontMetrics = getFontMetrics(font);
						float strokeWidth = fontMetrics.getHeight() * 0.125f;
						g2D.setStroke(new BasicStroke(strokeWidth));
						FontRenderContext fontRenderContext = g2D.getFontRenderContext();
						TextLayout textLayout = new TextLayout(getText(), font, fontRenderContext);
						AffineTransform oldTransform = g2D.getTransform();
						if (getIcon() != null)
						{
							g2D.translate(getIcon().getIconWidth() + getIconTextGap(), 0);
						}
						g2D.draw(textLayout.getOutline(AffineTransform.getTranslateInstance(-strokeWidth / 5,
								(getHeight() - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent() - strokeWidth / 5)));
						g2D.setComposite(oldComposite);
						g2D.setTransform(oldTransform);
					}
					super.paintComponent(g);
				}
			};
			List<Level> levels = home.getLevels();
			tabLabel.setEnabled(levels.get(i).isViewable());
			if (i > 0
					&& levels.get(i - 1).getElevation() == levels.get(i).getElevation())
			{
				tabLabel.setIcon(sameElevationIcon);
			}

			try
			{
				// Invoke dynamically Java 6 setTabComponentAt method
				this.multipleLevelsTabbedPane.getClass().getMethod("setTabComponentAt", int.class, Component.class)
						.invoke(this.multipleLevelsTabbedPane, i, tabLabel);
			}
			catch (InvocationTargetException ex)
			{
				throw new RuntimeException(ex);
			}
			catch (IllegalAccessException ex)
			{
				throw new IllegalAccessError(ex.getMessage());
			}
			catch (NoSuchMethodException ex)
			{
				throw new NoSuchMethodError(ex.getMessage());
			}
		}*/
	}


	private boolean redoEnabled = false;
	private boolean undoEnabled = false;
	private String redoText = null;
	private String undoText = null;

	public void setEnabled(HomeView.ActionType actionType, boolean enabled)
	{
		if (actionType == HomeView.ActionType.UNDO)
		{
			undoEnabled = enabled;
			if (mOptionsMenu != null)
			{
				MenuItem undoItem = mOptionsMenu.findItem(R.id.editUndo);
				if (undoItem != null)
					undoItem.setEnabled(enabled);
			}
		}
		else if (actionType == HomeView.ActionType.REDO)
		{

			redoEnabled = enabled;
			if (mOptionsMenu != null)
			{
				MenuItem redoItem = mOptionsMenu.findItem(R.id.editRedo);
				if (redoItem != null)
					redoItem.setEnabled(enabled);
			}
		}
	}

	private boolean resettingToSelect = false;
	public void setNameAndShortDescription(HomeView.ActionType actionType, String text)
	{
		// Each time an undoable name is undated then if we are on the room,line,dim or label tools we should go back to select
		if (actionType == HomeView.ActionType.UNDO)
		{
			if (!resettingToSelect &&
					(planController.getMode() == PlanController.Mode.ROOM_CREATION ||
					planController.getMode() == PlanController.Mode.DIMENSION_LINE_CREATION ||
					planController.getMode() == PlanController.Mode.POLYLINE_CREATION ||
					planController.getMode() == PlanController.Mode.LABEL_CREATION))
			{
				// get off this thread
				resettingToSelect = true;
				EventQueue.invokeLater(new Runnable(){public void run(){
					setMode(PlanController.Mode.SELECTION);
					resetToolSpinnerToMode();
					resettingToSelect = false;}});
			}
		}

		if (actionType == HomeView.ActionType.REDO)
		{
			redoText =  text != null ? text.replace("redoText ", "") : null;
			if (mOptionsMenu != null && text != null)
			{
				MenuItem redoItem = mOptionsMenu.findItem(R.id.editRedo);
				if(redoItem != null)
				{
					SpannableStringBuilder builder2 = new SpannableStringBuilder("* " + redoText);// it will replace "*" with icon
					builder2.setSpan(new ImageSpan(getActivity(), R.drawable.edit_redo), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					redoItem.setTitle(builder2);
					redoItem.setTitleCondensed(text);
				}
			}
		}
		else
		{
			System.out.println("Action text updated " + actionType + " " + text );
		}
	}

	/**
	 * Preferences property listener bound to this component with a weak reference to avoid
	 * strong link between preferences and this component.
	 */
	private static class LanguageChangeListener implements PropertyChangeListener
	{
		private WeakReference<MultipleLevelsPlanPanel> planPanel;

		public LanguageChangeListener(MultipleLevelsPlanPanel planPanel)
		{
			this.planPanel = new WeakReference<MultipleLevelsPlanPanel>(planPanel);
		}

		public void propertyChange(PropertyChangeEvent ev)
		{
			// If help pane was garbage collected, remove this listener from preferences
			MultipleLevelsPlanPanel planPanel = this.planPanel.get();
			UserPreferences preferences = (UserPreferences) ev.getSource();
			if (planPanel == null)
			{
				preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
			}
			else
			{
				// Update create level tooltip in new locale
				//PJPJPJ tooltips removed
				//String createNewLevelTooltip = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.ShortDescription");
				//planPanel.multipleLevelsTabbedPane.setToolTipTextAt(planPanel.multipleLevelsTabbedPane.getTabCount() - 1, createNewLevelTooltip);

				//PJ
				planPanel.updateToolNames();
			}
		}
	}

	/**
	 * Creates the tabs from <code>home</code> levels.
	 */
	private void createTabs(Home home, UserPreferences preferences)
	{
		List<Level> levels = home.getLevels();
		for (int i = 0; i < levels.size(); i++)
		{
			Level level = levels.get(i);
			this.levelSpinnerControl.addTab(level.getName(), new LevelLabel(level));
			updateTabComponent(home, i);
		}

		//PJPJPJ this is just a button on the JTabbedPane now
	/*	String createNewLevelIcon = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.SmallIcon");
		String createNewLevelTooltip = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.ShortDescription");

		ImageIcon newLevelIcon = SwingTools.getScaledImageIcon(MultipleLevelsPlanPanel.class.getResource(createNewLevelIcon));
		this.multipleLevelsTabbedPane.addTab("", newLevelIcon, new JLabel(), createNewLevelTooltip);
		// Disable last tab to avoid user stops on it
		this.multipleLevelsTabbedPane.setEnabledAt(this.multipleLevelsTabbedPane.getTabCount() - 1, false);
		this.multipleLevelsTabbedPane.setDisabledIconAt(this.multipleLevelsTabbedPane.getTabCount() - 1, newLevelIcon);*/
	}

	/**
	 * Selects the tab matching the selected level in <code>home</code>.
	 */
	private void updateSelectedTab(Home home)
	{
		List<Level> levels = home.getLevels();
		Level selectedLevel = home.getSelectedLevel();
		if (levels.size() >= 2 && selectedLevel != null)
		{
			this.levelSpinnerControl.setSelectedIndex(levels.indexOf(selectedLevel));
			displayPlanComponentAtSelectedIndex(home);
		}
		updateLayout(home);
	}

	/**
	 * Display the plan component at the selected tab index.
	 */
	private void displayPlanComponentAtSelectedIndex(Home home)
	{
		//PJPJPJ this is not needed by me, as I don't in fact put the single plan into the tabs
		/*int planIndex = this.multipleLevelsTabbedPane.indexOfComponent(this.planScrollPane);
		if (planIndex != -1)
		{
			// Replace plan component by a dummy label to avoid losing tab
			this.multipleLevelsTabbedPane.setComponentAt(planIndex, new LevelLabel(home.getLevels().get(planIndex)));
		}
		this.multipleLevelsTabbedPane.setComponentAt(this.multipleLevelsTabbedPane.getSelectedIndex(), this.planScrollPane);*/
	}

	/**
	 * Switches between a simple plan component view and a tabbed pane for multiple levels.
	 */
	private void updateLayout(Home home)
	{
		//PJPJPJ card layout just dropped as there is only the multi system now
		/*CardLayout layout = (CardLayout) getLayout();
		List<Level> levels = home.getLevels();
		boolean focus = this.planComponent.hasFocus();
		if (levels.size() < 2 || home.getSelectedLevel() == null)
		{
			int planIndex = this.multipleLevelsTabbedPane.indexOfComponent(this.planScrollPane);
			if (planIndex != -1)
			{
				// Replace plan component by a dummy label to avoid losing tab
				this.multipleLevelsTabbedPane.setComponentAt(planIndex, new LevelLabel(home.getLevels().get(planIndex)));
			}
			this.oneLevelPanel.add(this.planScrollPane);
			layout.show(this, ONE_LEVEL_PANEL_NAME);
		}
		else
		{
			layout.show(this, MULTIPLE_LEVELS_PANEL_NAME);
		}
		if (focus)
		{
			this.planComponent.requestFocusInWindow();
		}*/
	}

	/**
	 * Layouts the components displayed by this panel.
	 */
	private void layoutComponents()
	{
		//PJPJPJ I only use the ulti system now
		//add(this.multipleLevelsTabbedPane, MULTIPLE_LEVELS_PANEL_NAME);
		//add(this.oneLevelPanel, ONE_LEVEL_PANEL_NAME);

		//PJPJ dropped probably pointless
		/*SwingTools.installFocusBorder(this.planComponent);
		setFocusTraversalPolicyProvider(false);
		setMinimumSize(new Dimension());*/
	}

/*	@Override
	public void setTransferHandler(TransferHandler newHandler)
	{
		this.planComponent.setTransferHandler(newHandler);
	}*/

/*	@Override
	public void setComponentPopupMenu(JPopupMenu popup)
	{
		this.planComponent.setComponentPopupMenu(popup);
	}*/

/*	@Override
	public void addMouseMotionListener(final MouseMotionListener l)
	{
		this.planComponent.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseMoved(MouseEvent ev)
			{
				l.mouseMoved(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseDragged(MouseEvent ev)
			{
				l.mouseDragged(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}
		});
	}*/

/*	@Override
	public void addMouseListener(final MouseListener l)
	{
		this.planComponent.addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent ev)
			{
				l.mouseReleased(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mousePressed(MouseEvent ev)
			{
				l.mousePressed(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseExited(MouseEvent ev)
			{
				l.mouseExited(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseEntered(MouseEvent ev)
			{
				l.mouseEntered(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}

			public void mouseClicked(MouseEvent ev)
			{
				l.mouseClicked(SwingUtilities.convertMouseEvent(planComponent, ev, MultipleLevelsPlanPanel.this));
			}
		});
	}*/

/*	@Override
	public void addFocusListener(final FocusListener l)
	{
		FocusListener componentFocusListener = new FocusListener()
		{
			public void focusGained(FocusEvent ev)
			{
				l.focusGained(new FocusEvent(MultipleLevelsPlanPanel.this, FocusEvent.FOCUS_GAINED, ev.isTemporary(), ev.getOppositeComponent()));
			}

			public void focusLost(FocusEvent ev)
			{
				l.focusLost(new FocusEvent(MultipleLevelsPlanPanel.this, FocusEvent.FOCUS_LOST, ev.isTemporary(), ev.getOppositeComponent()));
			}
		};
		this.planComponent.addFocusListener(componentFocusListener);
		this.multipleLevelsTabbedPane.addFocusListener(componentFocusListener);
	}*/

	/**
	 * Sets rectangle selection feedback coordinates.
	 */
	public void setRectangleFeedback(float x0, float y0, float x1, float y1)
	{
		this.planComponent.setRectangleFeedback(x0, y0, x1, y1);
	}

	/**
	 * Ensures selected items are visible in the plan displayed by this component and moves
	 * its scroll bars if needed.
	 */
	public void makeSelectionVisible()
	{
		this.planComponent.makeSelectionVisible();
	}

	/**
	 * Ensures the point at (<code>x</code>, <code>y</code>) is visible in the plan displayed by this component,
	 * moving its scroll bars if needed.
	 */
	public void makePointVisible(float x, float y)
	{
		this.planComponent.makePointVisible(x, y);
	}

	/**
	 * Returns the scale used to display the plan displayed by this component.
	 */
	public float getScale()
	{
		return this.planComponent.getScale();
	}

	/**
	 * Sets the scale used to display the plan displayed by this component.
	 */
	public void setScale(float scale)
	{
		this.planComponent.setScale(scale);
	}

	/**
	 * Moves the plan displayed by this component from (dx, dy) unit in the scrolling zone it belongs to.
	 */
	public void moveView(float dx, float dy)
	{
		this.planComponent.moveView(dx, dy);
	}

	/**
	 * Returns <code>x</code> converted in model coordinates space.
	 */
	public float convertXPixelToModel(int x)
	{
		return this.planComponent.convertXPixelToModel(SwingTools.convertPoint(this, x, 0, this.planComponent).x);
	}

	/**
	 * Returns <code>y</code> converted in model coordinates space.
	 */
	public float convertYPixelToModel(int y)
	{
		return this.planComponent.convertYPixelToModel(SwingTools.convertPoint(this, 0, y, this.planComponent).y);
	}

	/**
	 * Returns <code>x</code> converted in screen coordinates space.
	 */
	public int convertXModelToScreen(float x)
	{
		return this.planComponent.convertXModelToScreen(x);
	}

	/**
	 * Returns <code>y</code> converted in screen coordinates space.
	 */
	public int convertYModelToScreen(float y)
	{
		return this.planComponent.convertYModelToScreen(y);
	}

	/**
	 * Returns the length in centimeters of a pixel with the current scale.
	 */
	public float getPixelLength()
	{
		return this.planComponent.getPixelLength();
	}

	/**
	 * Returns the coordinates of the bounding rectangle of the <code>text</code> displayed at
	 * the point (<code>x</code>,<code>y</code>).
	 */
	public float[][] getTextBounds(String text, TextStyle style, float x, float y, float angle)
	{
		return this.planComponent.getTextBounds(text, style, x, y, angle);
	}

	/**
	 * Sets the cursor of this component.
	 */
	public void setCursor(CursorType cursorType)
	{
		this.planComponent.setCursor(cursorType);
	}

	/**
	 * Sets the cursor of this component.
	 */
/*	@Override
	public void setCursor(Cursor cursor)
	{
		this.planComponent.setCursor(cursor);
	}*/

	/**
	 * Returns the cursor of this component.
	 */
/*	@Override
	public Cursor getCursor()
	{
		return this.planComponent.getCursor();
	}*/

	/**
	 * Sets tool tip text displayed as feedback.
	 */
	public void setToolTipFeedback(String toolTipFeedback, float x, float y)
	{
		this.planComponent.setToolTipFeedback(toolTipFeedback, x, y);
	}

	/**
	 * Set properties edited in tool tip.
	 */
	public void setToolTipEditedProperties(EditableProperty[] toolTipEditedProperties, Object[] toolTipPropertyValues,
										   float x, float y)
	{
		this.planComponent.setToolTipEditedProperties(toolTipEditedProperties, toolTipPropertyValues, x, y);
	}

	/**
	 * Deletes tool tip text from screen.
	 */
	public void deleteToolTipFeedback()
	{
		this.planComponent.deleteToolTipFeedback();
	}

	/**
	 * Sets whether the resize indicator of selected wall or piece of furniture
	 * should be visible or not.
	 */
	public void setResizeIndicatorVisible(boolean visible)
	{
		this.planComponent.setResizeIndicatorVisible(visible);
	}

	/**
	 * Sets the location point for alignment feedback.
	 */
	public void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass, Selectable alignedObject, float x,
									 float y, boolean showPoint)
	{
		this.planComponent.setAlignmentFeedback(alignedObjectClass, alignedObject, x, y, showPoint);
	}

	/**
	 * Sets the points used to draw an angle in the plan displayed by this component.
	 */
	public void setAngleFeedback(float xCenter, float yCenter, float x1, float y1, float x2, float y2)
	{
		this.planComponent.setAngleFeedback(xCenter, yCenter, x1, y1, x2, y2);
	}

	/**
	 * Sets the feedback of dragged items drawn during a drag and drop operation,
	 * initiated from outside of the plan displayed by this component.
	 */
	public void setDraggedItemsFeedback(List<Selectable> draggedItems)
	{
		this.planComponent.setDraggedItemsFeedback(draggedItems);
	}

	/**
	 * Sets the given dimension lines to be drawn as feedback.
	 */
	public void setDimensionLinesFeedback(List<DimensionLine> dimensionLines)
	{
		this.planComponent.setDimensionLinesFeedback(dimensionLines);
	}

	/**
	 * Deletes all elements shown as feedback.
	 */
	public void deleteFeedback()
	{
		this.planComponent.deleteFeedback();
	}

	/**
	 * Returns <code>true</code> if the given coordinates belong to the plan displayed by this component.
	 */
	public boolean canImportDraggedItems(List<Selectable> items, int x, int y)
	{
		//	JViewport viewport = this.planScrollPane.getViewport();
		//	Point point = SwingUtilities.convertPoint(this, x, y, viewport);
		//	return viewport.contains(point);
		return false;
	}

	/**
	 * Returns the component used as an horizontal ruler for the plan displayed by this component.
	 */
	public com.eteks.sweethome3d.viewcontroller.View getHorizontalRuler()
	{
		return this.planComponent.getHorizontalRuler();
	}

	/**
	 * Returns the component used as a vertical ruler for the plan displayed by this component.
	 */
	public com.eteks.sweethome3d.viewcontroller.View getVerticalRuler()
	{
		return this.planComponent.getVerticalRuler();
	}

	/**
	 * Prints the plan component.
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
	{
		return this.planComponent.print(graphics, pageFormat, pageIndex);
	}

	/**
	 * Returns the preferred scale to print the plan component.
	 */
	public float getPrintPreferredScale(Graphics graphics, PageFormat pageFormat)
	{
		return this.planComponent.getPrintPreferredScale(graphics, pageFormat);
	}

	/**
	 * A dummy label used to track tabs matching levels.
	 */
	public static class LevelLabel //extends JLabel
	{
		private final Level level;

		public LevelLabel(Level level)
		{
			this.level = level;

		}

		public Level getLevel()
		{
			return this.level;
		}

	}


}
