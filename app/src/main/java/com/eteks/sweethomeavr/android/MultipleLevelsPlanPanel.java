package com.eteks.sweethomeavr.android;


import android.os.Bundle;

import android.view.ContextMenu;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eteks.sweethome3d.HomeFrameController;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanController.EditableProperty;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.VCView;
import com.eteks.sweethomeavr.SweetHomeAVRActivity;
import com.eteks.sweethomeavr.android.swingish.ButtonGroup;
import com.eteks.sweethomeavr.android.swingish.JComponent;
import com.eteks.sweethomeavr.android.swingish.JTabbedPane;
import com.mindblowing.sweethomeavr.R;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import java.util.List;


import javaawt.Graphics;

import javaawt.Graphics2D;
import javaawt.geom.AffineTransform;
import javaawt.print.PageFormat;
import javaawt.print.PrinterException;
import javaxswing.ImageIcon;
import javaxswing.undo.CannotRedoException;
import javaxswing.undo.CannotUndoException;

import static android.R.id.message;

/**
 * Created by phil on 11/22/2016.
 * This guy is the tabbed panel itself
 * <p>
 * why is there only one plan component? are the tabs not really used and just flip the level for the plan???
 * yep definitely only one plan and a simple non tabbed layout
 * <p>
 * yep and change listeners state updated calls the plancontroller telling it to set the level number
 * which comes through as a LEVEL_CHANGED event to plancomp, which just dumps all cache and calls repaint
 * so presumably a paintComponent will check level to draw
 * <p>
 * So possibly this guy just needs a bunch of level buttons, probably capped at 6 say for now
 * I wonder if I should shim up tab pane and organise myself like data?
 */

public class MultipleLevelsPlanPanel extends JComponent implements PlanView
{

	//menu item options
	public static boolean alignmentActivated = false;
	public static boolean magnetismToggled = false;// careful toggle != checked!
	public static boolean duplicationActivated = false;
	public static float dpiMinSpanForZoom = 1.0f;
	public static float dpiIndicatorTouchSize = 0.25f;
	private DrawableView drawableView;

	private ButtonGroup selectionGroup = new ButtonGroup();


	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		this.setHasOptionsMenu(true);

		View rootView = inflater.inflate(
				R.layout.multiple_level_plan_panel, container, false);

		//now get the drawableView from it
		drawableView = (DrawableView) rootView.findViewById(R.id.drawableView);
		drawableView.setDrawer(this);

		planComponent.setDrawableView(drawableView);

		RadioGroup rg = (RadioGroup) rootView.findViewById(R.id.levelsRadioGroup);
		this.multipleLevelsTabbedPane = new JTabbedPane(this.getContext(), rg);

		// from the constructor but placed here now so views are set
		createComponents(home, preferences, planController);
		layoutComponents();
		updateSelectedTab(home);


		//controller.addLevel();
		// select new level then...
		// controller.modifySelectedLevel();
		// but possibly the new jtabbedpane gear sorts this out?


		View sp = rootView.findViewById(R.id.levelPlusButton);
		sp.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				planController.addLevel();
			}
		});

		// now to programmatically register teh call back for the damn buttons!
		rootView.findViewById(R.id.planSelect).setOnClickListener(planActionListener);
		selectionGroup.add((CompoundButton) rootView.findViewById(R.id.planSelect));
		rootView.findViewById(R.id.createWalls).setOnClickListener(planActionListener);
		selectionGroup.add((CompoundButton) rootView.findViewById(R.id.createWalls));
		rootView.findViewById(R.id.createRooms).setOnClickListener(planActionListener);
		selectionGroup.add((CompoundButton) rootView.findViewById(R.id.createRooms));
		rootView.findViewById(R.id.createPolyLines).setOnClickListener(planActionListener);
		selectionGroup.add((CompoundButton) rootView.findViewById(R.id.createPolyLines));
		rootView.findViewById(R.id.createDimensions).setOnClickListener(planActionListener);
		selectionGroup.add((CompoundButton) rootView.findViewById(R.id.createDimensions));
		rootView.findViewById(R.id.createText).setOnClickListener(planActionListener);
		selectionGroup.add((CompoundButton) rootView.findViewById(R.id.createText));

		rootView.findViewById(R.id.lockCheck).setOnClickListener(planActionListener);

		rootView.findViewById(R.id.editUndo).setOnClickListener(planActionListener);
		rootView.findViewById(R.id.editRedo).setOnClickListener(planActionListener);
		rootView.findViewById(R.id.controlKeyToggle).setOnClickListener(planActionListener);
		rootView.findViewById(R.id.delete).setOnClickListener(planActionListener);
		rootView.findViewById(R.id.furnitureModify).setOnClickListener(planActionListener);


		return rootView;
	}

	View.OnClickListener planActionListener = new View.OnClickListener() {
		public void onClick(View view) {

			if( view.getParent() instanceof RadioGroup)
				((RadioGroup) view.getParent()).check(view.getId());

			// now set the action to the action
			switch (view.getId())
			{
				case R.id.planSelect:
					planController.escape();// in case we are doing a create now
					planController.setMode(PlanController.Mode.SELECTION);
					break;
				case R.id.createWalls:
					planController.escape();// in case we are doing a create now
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), String.format("Double tap to finish", message), Toast.LENGTH_SHORT).show();
					planController.setMode(PlanController.Mode.WALL_CREATION);
					break;
				case R.id.createRooms:
					planController.escape();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), String.format("Double tap to finish", message), Toast.LENGTH_SHORT).show();
					planController.setMode(PlanController.Mode.ROOM_CREATION);
					break;
				case R.id.createPolyLines:
					planController.escape();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), String.format("Double tap to finish", message), Toast.LENGTH_SHORT).show();
					planController.setMode(PlanController.Mode.POLYLINE_CREATION);
					break;
				case R.id.createDimensions:
					planController.escape();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), String.format("Double tap to finish", message), Toast.LENGTH_SHORT).show();
					planController.setMode(PlanController.Mode.DIMENSION_LINE_CREATION);
					break;
				case R.id.createText:
					planController.escape();
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), String.format("Double tap to finish (not working yet)", message), Toast.LENGTH_SHORT).show();
					planController.setMode(PlanController.Mode.LABEL_CREATION);
					break;
				case R.id.lockCheck:
					ToggleButton lockButton = (ToggleButton)view;
					if(lockButton.isChecked())
						planController.lockBasePlan();
					else
						planController.unlockBasePlan();
					break;

				case R.id.editUndo:
					try
					{
						((SweetHomeAVRActivity) MultipleLevelsPlanPanel.this.getActivity()).sweetHomeAVR.getHomeController().undo();
					}catch (CannotUndoException e)
					{//ignored, as teh button should only be enabled when one undo is available (see HomeView addUndoSupportListener)
					}
					break;
				case R.id.editRedo:
					try
					{((SweetHomeAVRActivity)MultipleLevelsPlanPanel.this.getActivity()).sweetHomeAVR.getHomeController().redo();
					}catch (CannotRedoException e)
					{//ignored, as teh button should only be enabled when one undo is available (see HomeView addUndoSupportListener)
					}
					break;
				case R.id.controlKeyToggle:
					//TODO: this guy needs to reflec tthe cntrl option on anything, so duplication for select, but curve wall for create
					ToggleButton controlKeyToggle = (ToggleButton)view;
					duplicationActivated = controlKeyToggle.isChecked();
					break;
				case R.id.delete:
					planController.deleteSelection();
					break;
				case R.id.furnitureModify:
					Toast.makeText(MultipleLevelsPlanPanel.this.getActivity(), "Not done, very swing", Toast.LENGTH_SHORT).show();
					break;

			}
		}
	};



	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.plan_component_menu, menu);

		menu.findItem(R.id.alignment).setChecked(alignmentActivated);

		//TODO: should also be done after prefs undated too
		menu.findItem(R.id.magnetism).setEnabled(preferences.isMagnetismEnabled());
		menu.findItem(R.id.magnetism).setChecked(preferences.isMagnetismEnabled() && !magnetismToggled);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.alignment:
				//CheckBox checkBox1 = (CheckBox) item.getActionView();
				item.setChecked(!item.isChecked());
				this.alignmentActivated = item.isChecked();
				return true;
			case R.id.magnetism:
				//CheckBox checkBox2= (CheckBox) item.getActionView();
				item.setChecked(!item.isChecked());
				this.magnetismToggled = !item.isChecked();// careful toggle != checked!
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.popup_plan_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
			case R.id.one:
				Toast.makeText(this.getActivity(), "One ", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.two:
				return true;
			case R.id.three:
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}


	private Home home;
	private UserPreferences preferences;
	private PlanController planController;

	private boolean rulersVisible = true;

	public void init(Home home, UserPreferences preferences2, PlanController planController)
	{
		this.home = home;
		this.preferences = preferences2;
		this.planController = planController;
		// Card layout relates to the odd single/multiple options
		//super(new CardLayout());


		// taken from createComponents
		this.planComponent = createPlanComponent(home, preferences, planController);
		//moved to onCreateView
		//createComponents(home, preferences, planController);
		//layoutComponents();
		//updateSelectedTab(home);


		// taken from HomePane
		rulersVisible = preferences.isRulersVisible();
		preferences.addPropertyChangeListener(UserPreferences.Property.RULERS_VISIBLE,
				new PropertyChangeListener(){
					public void propertyChange(PropertyChangeEvent event)
					{
						rulersVisible = preferences.isRulersVisible();
						multipleLevelsTabbedPane.repaint();
						planComponent.repaint();
					}
				});
	}


	//private static final String ONE_LEVEL_PANEL_NAME = "oneLevelPanel";
	//private static final String MULTIPLE_LEVELS_PANEL_NAME = "multipleLevelsPanel";

	// use a class from the jar
	private static final ImageIcon sameElevationIcon = SwingTools.getScaledImageIcon(HomeFrameController.class.getResource("swing/resources/sameElevation.png"));
	//private static final ImageIcon sameElevationIcon = SwingTools.getScaledImageIcon(FurnitureTable.class.getResource("resources/sameElevation.png"));


	private PlanComponent planComponent;
	//	private JScrollPane planScrollPane;
	private JTabbedPane multipleLevelsTabbedPane;
//	private JPanel oneLevelPanel; //PJPJP this is not used now at all, nor the card layout thing

	/**
	 * Called by our drawableView when onDraw called for it
	 *
	 * @param g
	 */
	public void paintComponent(Graphics g)
	{
		//TODO: lock icon need drawing about now
		AffineTransform previousTransform = ((Graphics2D) g).getTransform();
		planComponent.paintComponent(g);
		((Graphics2D) g).setTransform(previousTransform);

		if(rulersVisible)
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

	/**
	 * Creates components displayed by this panel.
	 */
	private void createComponents(final Home home,
								  final UserPreferences preferences, final PlanController controller)
	{
		// moved to init
		//this.planComponent = createPlanComponent(home, preferences, controller);

	/*	UIManager.getDefaults().put("TabbedPane.contentBorderInsets", OperatingSystem.isMacOSX()
				? new Insets(2, 2, 2, 2)
				: new Insets(-1, 0, 2, 2));*/
		//this.multipleLevelsTabbedPane = new JTabbedPane(); // defined in the layout xml in fact
/*		if (OperatingSystem.isMacOSX())
		{
			this.multipleLevelsTabbedPane.setBorder(new EmptyBorder(-2, -6, -7, -6));
		}*/
		List<Level> levels = home.getLevels();
		//	this.planScrollPane = new JScrollPane(this.planComponent);
		//	this.planScrollPane.setMinimumSize(new Dimension());
/*		if (OperatingSystem.isMacOSX())
		{
			this.planScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			this.planScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}*/

		createTabs(home, preferences);

		final ChangeListener changeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent ev)
			{
				LevelLabel selectedComponent = multipleLevelsTabbedPane.getSelectedComponent();
				if (selectedComponent instanceof LevelLabel)
				{
					controller.setSelectedLevel(((LevelLabel) selectedComponent).getLevel());
				}
			}
		};
		this.multipleLevelsTabbedPane.addChangeListener(changeListener);
		// Add a mouse listener that will give focus to plan component only if a change in tabbed pane comes from the mouse
		// and will add a level only if user clicks on the last tab
	/*	this.multipleLevelsTabbedPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent ev)
			{
				int indexAtLocation = multipleLevelsTabbedPane.indexAtLocation(ev.getX(), ev.getY());
				if (ev.getClickCount() == 1)
				{
					if (indexAtLocation == multipleLevelsTabbedPane.getTabCount() - 1)
					{
						controller.addLevel();
					}
					final Level oldSelectedLevel = home.getSelectedLevel();
					EventQueue.invokeLater(new Runnable()
					{
						public void run()
						{
							if (oldSelectedLevel == home.getSelectedLevel())
							{
								planComponent.requestFocusInWindow();
							}
						}
					});
				}
				else if (indexAtLocation != -1)
				{
					if (multipleLevelsTabbedPane.getSelectedIndex() == multipleLevelsTabbedPane.getTabCount() - 1)
					{
						// May happen with a row of tabs is full
						multipleLevelsTabbedPane.setSelectedIndex(multipleLevelsTabbedPane.getTabCount() - 2);
					}
					controller.modifySelectedLevel();
				}
			}
		});*/

		// Add listeners to levels to maintain tabs name and order
		final PropertyChangeListener levelChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				if (Level.Property.NAME.name().equals(ev.getPropertyName()))
				{
					int index = home.getLevels().indexOf(ev.getSource());
					multipleLevelsTabbedPane.setTitleAt(index, (String) ev.getNewValue());
					updateTabComponent(home, index);
				}
				else if (Level.Property.VIEWABLE.name().equals(ev.getPropertyName()))
				{
					updateTabComponent(home, home.getLevels().indexOf(ev.getSource()));
				}
				else if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())
						|| Level.Property.ELEVATION_INDEX.name().equals(ev.getPropertyName()))
				{
					multipleLevelsTabbedPane.removeChangeListener(changeListener);
					multipleLevelsTabbedPane.removeAll();
					createTabs(home, preferences);
					updateSelectedTab(home);
					multipleLevelsTabbedPane.addChangeListener(changeListener);
				}
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
				multipleLevelsTabbedPane.removeChangeListener(changeListener);
				switch (ev.getType())
				{
					case ADD:
						multipleLevelsTabbedPane.insertTab(ev.getItem().getName(), null, new LevelLabel(ev.getItem()), null, ev.getIndex());
						updateTabComponent(home, ev.getIndex());
						ev.getItem().addPropertyChangeListener(levelChangeListener);
						break;
					case DELETE:
						ev.getItem().removePropertyChangeListener(levelChangeListener);
						multipleLevelsTabbedPane.remove(ev.getIndex());
						break;
				}
				updateLayout(home);
				multipleLevelsTabbedPane.addChangeListener(changeListener);
			}
		});

		home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				multipleLevelsTabbedPane.removeChangeListener(changeListener);
				updateSelectedTab(home);
				multipleLevelsTabbedPane.addChangeListener(changeListener);
			}
		});

		// PJ this.oneLevelPanel not used at all, nor is scrollpane
		//	this.oneLevelPanel = new JPanel(new BorderLayout());


		if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6"))
		{
			home.addPropertyChangeListener(Home.Property.ALL_LEVELS_SELECTION, new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent ev)
				{
					multipleLevelsTabbedPane.repaint();
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
		pc.init(home, preferences, controller);
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
			this.multipleLevelsTabbedPane.addTab(level.getName(), new LevelLabel(level));
			updateTabComponent(home, i);
		}


		//TODO: the add level tab might just be a simple + button at the end??
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
			this.multipleLevelsTabbedPane.setSelectedIndex(levels.indexOf(selectedLevel));
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
		//tODO: I need this working I guess at aaomse point
		return false;
	}

	/**
	 * Returns the component used as an horizontal ruler for the plan displayed by this component.
	 */
	public VCView getHorizontalRuler()
	{
		return this.planComponent.getHorizontalRuler();
	}

	/**
	 * Returns the component used as a vertical ruler for the plan displayed by this component.
	 */
	public VCView getVerticalRuler()
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
