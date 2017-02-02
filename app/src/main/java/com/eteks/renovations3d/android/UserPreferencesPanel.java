/*
 * UserPreferencesPanel.java 18 sept. 2006
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JCheckBox;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.VCView;
import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.DefaultComboBoxModel;
import com.eteks.renovations3d.android.swingish.JComboBox;
import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;

import javaawt.Color;
import javaawt.Graphics2D;
import javaawt.VMGraphics2D;
import javaawt.image.BufferedImage;
import javaawt.image.VMBufferedImage;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends Dialog implements DialogView {
  private final UserPreferencesController controller;
	private JLabel           languageLabel;
	private JComboBox        languageComboBox;
	private JButton          languageLibraryImportButton;
	private JLabel           unitLabel;
	private JComboBox        unitComboBox;
	private JLabel           furnitureCatalogViewLabel;
	private JRadioButton     treeRadioButton;
	private JRadioButton     listRadioButton;
	private JLabel           navigationPanelLabel;
	private JCheckBox        navigationPanelCheckBox;
	private JLabel           aerialViewCenteredOnSelectionLabel;
	private JCheckBox        aerialViewCenteredOnSelectionCheckBox;
	private JLabel           magnetismLabel;
	private JCheckBox        magnetismCheckBox;
	private JLabel           rulersLabel;
	private JCheckBox rulersCheckBox;
	private JLabel           gridLabel;
	private JCheckBox        gridCheckBox;
	private JLabel           defaultFontNameLabel;
	//private FontNameComboBox defaultFontNameComboBox;
	private JComboBox defaultFontNameComboBox;
	private JLabel           furnitureIconLabel;
	private JRadioButton catalogIconRadioButton;
	private JRadioButton     topViewRadioButton;
	private JLabel           roomRenderingLabel;
	private JRadioButton     monochromeRadioButton;
	private JRadioButton     floorColorOrTextureRadioButton;
	private JLabel           wallPatternLabel;
	private JComboBox        wallPatternComboBox;
	private JLabel           newWallPatternLabel;
	private JComboBox        newWallPatternComboBox;
	private JLabel           newWallThicknessLabel;
	private JSpinner newWallThicknessSpinner;
	private JLabel           newWallHeightLabel;
	private JSpinner         newWallHeightSpinner;
	private JLabel           newFloorThicknessLabel;
	private JSpinner         newFloorThicknessSpinner;
	//private JCheckBox        checkUpdatesCheckBox;
	//private JButton checkUpdatesNowButton;
	private JCheckBox        autoSaveDelayForRecoveryCheckBox;
	private JSpinner         autoSaveDelayForRecoverySpinner;
	private JLabel           autoSaveDelayForRecoveryUnitLabel;
	private JButton          resetDisplayedActionTipsButton;
	private String           dialogTitle;


	private Activity activity;
	private LinearLayout rootView;
	private Button closeButton;
  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>. 
   */
  public UserPreferencesPanel(UserPreferences preferences,
                              UserPreferencesController controller, Activity activity) {
    //super(new GridBagLayout());
	  super(activity);
    this.controller = controller;
	  this.activity = activity;
	  this.rootView = new LinearLayout(activity);
	  rootView.setOrientation(LinearLayout.VERTICAL);
	  ScrollView sv = new ScrollView(activity);
	  sv.addView(rootView);
	  this.setContentView(sv);
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final UserPreferencesController controller) {
    if (controller.isPropertyEditable(UserPreferencesController.Property.LANGUAGE)) {
      // Create language label and combo box bound to controller LANGUAGE property
      this.languageLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "languageLabel.text"));
      this.languageComboBox = new JComboBox(activity, new DefaultComboBoxModel(preferences.getSupportedLanguages()));
		languageComboBox.setAdapter(new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1,preferences.getSupportedLanguages())
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				return getDropDownView(position, convertView, parent);
			}
			@Override
			public View getDropDownView (int position, View convertView, ViewGroup parent)
			{
				TextView ret = new TextView(activity);
				String language = (String)languageComboBox.getItemAtPosition(position);
				Locale locale;
				int underscoreIndex = language.indexOf("_");
				if (underscoreIndex != -1) {
				  locale = new Locale(language.substring(0, underscoreIndex),
					  language.substring(underscoreIndex + 1));
				} else {
				  locale = new Locale(language);
				}
				String displayedValue = locale.getDisplayLanguage(locale);
				displayedValue = Character.toUpperCase(displayedValue.charAt(0)) + displayedValue.substring(1);
				if (underscoreIndex != -1) {
				  displayedValue += " - " + locale.getDisplayCountry(locale);
				}
				ret.setText(displayedValue);
            	return ret;
          	}
        });
      this.languageComboBox.setMaximumRowCount(Integer.MAX_VALUE);
      this.languageComboBox.setSelectedItem(controller.getLanguage());
      this.languageComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		  public void onNothingSelected(AdapterView<?> parent) {}
			  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            controller.setLanguage((String)languageComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.LANGUAGE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
				languageComboBox.setSelectedItem(controller.getLanguage());
            }
          });
      preferences.addPropertyChangeListener(UserPreferences.Property.SUPPORTED_LANGUAGES, 
          new SupportedLanguagesChangeListener(this));
    }
    
    /*if (controller.mayImportLanguageLibrary()) {
      this.languageLibraryImportButton = new Button(activity);languageLibraryImportButton.setText("Import lanaguage library not enabled");
		//TODO: note this has an icon, not a text
				 new ResourceAction(
          preferences, com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "IMPORT_LANGUAGE_LIBRARY", true) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.importLanguageLibrary();
            }
          });
     // this.languageLibraryImportButton.setToolTipText(preferences.getLocalizedString(
      //    com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "IMPORT_LANGUAGE_LIBRARY.tooltip"));
    }*/
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.UNIT)) {
      // Create unit label and combo box bound to controller UNIT property
      this.unitLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitLabel.text"));
      this.unitComboBox = new JComboBox(activity, new DefaultComboBoxModel(LengthUnit.values()));
      final Map<LengthUnit, String> comboBoxTexts = new HashMap<LengthUnit, String>();
      comboBoxTexts.put(LengthUnit.MILLIMETER, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitComboBox.millimeter.text"));
      comboBoxTexts.put(LengthUnit.CENTIMETER, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitComboBox.centimeter.text"));
      comboBoxTexts.put(LengthUnit.METER, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitComboBox.meter.text"));
      comboBoxTexts.put(LengthUnit.INCH, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitComboBox.inch.text"));
      comboBoxTexts.put(LengthUnit.INCH_DECIMALS, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitComboBox.inchDecimals.text"));
		unitComboBox.setAdapter(new ArrayAdapter<LengthUnit>(activity,android.R.layout.simple_list_item_1,LengthUnit.values())
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				return getDropDownView(position, convertView, parent);
			}
			@Override
			public View getDropDownView (int position,	View convertView, ViewGroup parent)
			{
				TextView ret = new TextView(activity);
				ret.setText(comboBoxTexts.get(unitComboBox.getItemAtPosition(position)));
				return ret;
			}
		});
      /*this.unitComboBox.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                        boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, comboBoxTexts.get(value), index, isSelected, cellHasFocus);
          }
        });*/
      this.unitComboBox.setSelectedItem(controller.getUnit());
      this.unitComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		  public void onNothingSelected(AdapterView<?> parent) {}
		  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            controller.setUnit((LengthUnit)unitComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              unitComboBox.setSelectedItem(controller.getUnit());
            }
          });
    }
    
/*    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE)) {
      // Create furniture catalog label and radio buttons bound to controller FURNITURE_CATALOG_VIEWED_IN_TREE property
      this.furnitureCatalogViewLabel = new TextView(activity);furnitureCatalogViewLabel.setText(preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "furnitureCatalogViewLabel.text"));
      this.treeRadioButton = new RadioButton(activity);treeRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "treeRadioButton.text"));treeRadioButton.setChecked(
          controller.isFurnitureCatalogViewedInTree());
      this.listRadioButton = new RadioButton(activity);listRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "listRadioButton.text"));listRadioButton.setChecked(
          !controller.isFurnitureCatalogViewedInTree());
      ButtonGroup furnitureCatalogViewButtonGroup = new ButtonGroup(activity);
      furnitureCatalogViewButtonGroup.add(this.treeRadioButton);
      furnitureCatalogViewButtonGroup.add(this.listRadioButton);
  
      ItemListener furnitureCatalogViewChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setFurnitureCatalogViewedInTree(treeRadioButton.isSelected());
          }
        };
      this.treeRadioButton.addItemListener(furnitureCatalogViewChangeListener);
      this.listRadioButton.addItemListener(furnitureCatalogViewChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              treeRadioButton.setSelected(controller.isFurnitureCatalogViewedInTree());
            }
          });
    }*/

/*    boolean no3D;
    try {
      no3D = Boolean.getBoolean("com.eteks.sweethome3d.no3D");
    } catch (AccessControlException ex) {
      // If com.eteks.sweethome3d.no3D property can't be read, 
      // security manager won't allow to access to Java 3D DLLs required by 3D view too
      no3D = true;
    }
    if (controller.isPropertyEditable(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE)
        && !no3D) {
      // Create navigation panel label and check box bound to controller NAVIGATION_PANEL_VISIBLE property
      this.navigationPanelLabel = new TextView(activity);navigationPanelLabel.setText(preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "navigationPanelLabel.text"));
      this.navigationPanelCheckBox = new CheckBox(activity);navigationPanelCheckBox.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "navigationPanelCheckBox.text"));
      if (!OperatingSystem.isMacOSX()
          || OperatingSystem.isMacOSXLeopardOrSuperior()) {
        this.navigationPanelCheckBox.setSelected(controller.isNavigationPanelVisible());
        this.navigationPanelCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
              controller.setNavigationPanelVisible(navigationPanelCheckBox.isSelected());
            }
          });
        controller.addPropertyChangeListener(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE, 
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                navigationPanelCheckBox.setSelected(controller.isNavigationPanelVisible());
              }
            });
      } else {
        // No support for navigation panel under Mac OS X Tiger (too unstable)
        this.navigationPanelCheckBox.setEnabled(false);
      }
    }*/

    if (controller.isPropertyEditable(UserPreferencesController.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED)) {
      // Create aerialViewCenteredOnSelection label and check box bound to controller AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED property
      this.aerialViewCenteredOnSelectionLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "aerialViewCenteredOnSelectionLabel.text"));
      this.aerialViewCenteredOnSelectionCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "aerialViewCenteredOnSelectionCheckBox.text"), controller.isAerialViewCenteredOnSelectionEnabled());
      this.aerialViewCenteredOnSelectionCheckBox.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setAerialViewCenteredOnSelectionEnabled(aerialViewCenteredOnSelectionCheckBox.isChecked());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              aerialViewCenteredOnSelectionCheckBox.setSelected(controller.isAerialViewCenteredOnSelectionEnabled());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.MAGNETISM_ENABLED)) {
      // Create magnetism label and check box bound to controller MAGNETISM_ENABLED property
      this.magnetismLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "magnetismLabel.text"));
      this.magnetismCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "magnetismCheckBox.text"), controller.isMagnetismEnabled());
      this.magnetismCheckBox.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setMagnetismEnabled(magnetismCheckBox.isChecked());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.MAGNETISM_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              magnetismCheckBox.setSelected(controller.isMagnetismEnabled());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.RULERS_VISIBLE)) {
      // Create rulers label and check box bound to controller RULERS_VISIBLE property
      this.rulersLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "rulersLabel.text"));
      this.rulersCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "rulersCheckBox.text"), controller.isRulersVisible());
      this.rulersCheckBox.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setRulersVisible(rulersCheckBox.isChecked());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.RULERS_VISIBLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              rulersCheckBox.setSelected(controller.isRulersVisible());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.GRID_VISIBLE)) {
      // Create grid label and check box bound to controller GRID_VISIBLE property
      this.gridLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "gridLabel.text"));
      this.gridCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "gridCheckBox.text"), controller.isGridVisible());
      this.gridCheckBox.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setGridVisible(gridCheckBox.isChecked());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.GRID_VISIBLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              gridCheckBox.setSelected(controller.isGridVisible());
            }
          });
    }
    
/*    if (controller.isPropertyEditable(UserPreferencesController.Property.DEFAULT_FONT_NAME)) {
      // Create font name label and combo box bound to controller DEFAULT_FONT_NAME property
      this.defaultFontNameLabel = new TextView(activity);defaultFontNameLabel.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "defaultFontNameLabel.text"));
      this.defaultFontNameComboBox = new FontNameComboBox(preferences);
      this.defaultFontNameComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		  public void onNothingSelected(AdapterView<?> parent) {}
		  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selectedItem = (String)defaultFontNameComboBox.getSelectedItem();
            controller.setDefaultFontName(selectedItem == FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME 
                ? null : selectedItem);
          }
        });
      PropertyChangeListener fontNameChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            String defaultFontName = controller.getDefaultFontName();
            defaultFontNameComboBox.setSelectedItem(defaultFontName == null 
                ? FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME : defaultFontName);
          }
        };
      controller.addPropertyChangeListener(UserPreferencesController.Property.DEFAULT_FONT_NAME, fontNameChangeListener);
      fontNameChangeListener.propertyChange(null);
    }*/
    
/*    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP)) {
      // Create furniture appearance label and radio buttons bound to controller FURNITURE_VIEWED_FROM_TOP property
      this.furnitureIconLabel = new TextView(activity);furnitureIconLabel.setText(preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "furnitureIconLabel.text"));
      this.catalogIconRadioButton = new RadioButton(activity);catalogIconRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "catalogIconRadioButton.text"), 
          !controller.isFurnitureViewedFromTop());
      this.topViewRadioButton = new RadioButton(activity);topViewRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "topViewRadioButton.text"), 
          controller.isFurnitureViewedFromTop());
      if (no3D) {
        this.catalogIconRadioButton.setEnabled(false);
        this.topViewRadioButton.setEnabled(false);
      } else { 
        if (Component3DManager.getInstance().isOffScreenImageSupported()) {
          ButtonGroup furnitureAppearanceButtonGroup = new ButtonGroup(activity);
          furnitureAppearanceButtonGroup.add(this.catalogIconRadioButton);
          furnitureAppearanceButtonGroup.add(this.topViewRadioButton);
      
          ItemListener furnitureAppearanceChangeListener = new ItemListener() {
              public void itemStateChanged(ItemEvent ev) {
                controller.setFurnitureViewedFromTop(topViewRadioButton.isChecked());
              }
            };
          this.catalogIconRadioButton.addItemListener(furnitureAppearanceChangeListener);
          this.topViewRadioButton.addItemListener(furnitureAppearanceChangeListener);
          controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP, 
              new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                  topViewRadioButton.setSelected(controller.isFurnitureViewedFromTop());
                }
              });
        } else {
          this.catalogIconRadioButton.setEnabled(false);
          this.topViewRadioButton.setEnabled(false);
        }
      }
    }*/

    if (controller.isPropertyEditable(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED)) {
      // Create room rendering label and radio buttons bound to controller ROOM_FLOOR_COLORED_OR_TEXTURED property
      this.roomRenderingLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "roomRenderingLabel.text"));
      this.monochromeRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "monochromeRadioButton.text"),
          !controller.isRoomFloorColoredOrTextured());
      this.floorColorOrTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "floorColorOrTextureRadioButton.text"),
				controller.isRoomFloorColoredOrTextured());
      ButtonGroup roomRenderingButtonGroup = new ButtonGroup();
      roomRenderingButtonGroup.add(this.monochromeRadioButton);
      roomRenderingButtonGroup.add(this.floorColorOrTextureRadioButton);
		View.OnClickListener roomRenderingChangeListener = new View.OnClickListener() {
		  public void onClick(View v) {
            controller.setRoomFloorColoredOrTextured(floorColorOrTextureRadioButton.isChecked());
          }
        };

      this.monochromeRadioButton.setOnClickListener(roomRenderingChangeListener);
      this.floorColorOrTextureRadioButton.setOnClickListener(roomRenderingChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              floorColorOrTextureRadioButton.setSelected(controller.isRoomFloorColoredOrTextured());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_PATTERN)) {
      // Create new wall pattern label and combo box bound to controller NEW_WALL_PATTERN property
      this.newWallPatternLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newWallPatternLabel.text"));    
      List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
      this.newWallPatternComboBox = new JComboBox(activity, new DefaultComboBoxModel(patterns.toArray()));
      this.newWallPatternComboBox.setAdapter( new PatternRenderer(patterns.toArray()));
      TextureImage newWallPattern = controller.getNewWallPattern();
      this.newWallPatternComboBox.setSelectedItem(newWallPattern != null 
          ? newWallPattern  
          : controller.getWallPattern());
      this.newWallPatternComboBox.addItemListener( new JComboBox.ItemListener() {
		  public void itemStateChanged(JComboBox.ItemEvent ev)
		  {
			  controller.setNewWallPattern((TextureImage)newWallPatternComboBox.getSelectedItem());
		  }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_PATTERN, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallPatternComboBox.setSelectedItem(controller.getNewWallPattern());
            }
          });
    } else if (controller.isPropertyEditable(UserPreferencesController.Property.WALL_PATTERN)) {
      // Create wall pattern label and combo box bound to controller WALL_PATTERN property
      this.wallPatternLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "wallPatternLabel.text"));    
      List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
      this.wallPatternComboBox = new JComboBox(activity, new DefaultComboBoxModel(patterns.toArray()));
      this.wallPatternComboBox.setAdapter( new PatternRenderer(patterns.toArray()) );
      this.wallPatternComboBox.setSelectedItem(controller.getWallPattern());
      this.wallPatternComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		  public void onNothingSelected(AdapterView<?> parent) {}
		  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            controller.setWallPattern((TextureImage)wallPatternComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.WALL_PATTERN, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              wallPatternComboBox.setSelectedItem(controller.getWallPattern());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_THICKNESS)) {
      // Create wall thickness label and spinner bound to controller NEW_WALL_THICKNESS property
      this.newWallThicknessLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newWallThicknessLabel.text"));
      final SpinnerLengthModel newWallThicknessSpinnerModel = new SpinnerLengthModel(0.5f, 0.125f, controller);
      this.newWallThicknessSpinner = new AutoCommitLengthSpinner(newWallThicknessSpinnerModel, controller);
      newWallThicknessSpinnerModel.setValue(controller.getNewWallThickness());
      newWallThicknessSpinnerModel.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setNewWallThickness((float)newWallThicknessSpinnerModel.getValue());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_THICKNESS, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallThicknessSpinnerModel.setValue(controller.getNewWallThickness());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_HEIGHT)) {
      // Create wall height label and spinner bound to controller NEW_WALL_HEIGHT property
      this.newWallHeightLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newWallHeightLabel.text"));
      final SpinnerLengthModel newWallHeightSpinnerModel = new SpinnerLengthModel(10f, 2f, controller);
      this.newWallHeightSpinner = new AutoCommitLengthSpinner(newWallHeightSpinnerModel, controller);
      newWallHeightSpinnerModel.setValue(controller.getNewWallHeight());
      newWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setNewWallHeight((float)newWallHeightSpinnerModel.getValue());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_HEIGHT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallHeightSpinnerModel.setValue(controller.getNewWallHeight());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_FLOOR_THICKNESS)) {
      // Create wall thickness label and spinner bound to controller NEW_FLOOR_THICKNESS property
      this.newFloorThicknessLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newFloorThicknessLabel.text"));
      final SpinnerLengthModel newFloorThicknessSpinnerModel = new SpinnerLengthModel(0.5f, 0.125f, controller);
      this.newFloorThicknessSpinner = new AutoCommitLengthSpinner(newFloorThicknessSpinnerModel, controller);
      newFloorThicknessSpinnerModel.setValue(controller.getNewFloorThickness());
      newFloorThicknessSpinnerModel.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setNewFloorThickness((float)newFloorThicknessSpinnerModel.getValue());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_FLOOR_THICKNESS, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newFloorThicknessSpinnerModel.setValue(controller.getNewFloorThickness());
            }
          });
    }
    
/*    if (controller.isPropertyEditable(UserPreferencesController.Property.CHECK_UPDATES_ENABLED)) {
      // Create check box bound to controller CHECK_UPDATES_ENABLED property
      this.checkUpdatesCheckBox = new CheckBox(activity);checkUpdatesCheckBox.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "checkUpdatesCheckBox.text"), controller.isCheckUpdatesEnabled());
      this.checkUpdatesCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setCheckUpdatesEnabled(checkUpdatesCheckBox.isChecked());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.CHECK_UPDATES_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              checkUpdatesCheckBox.setSelected(controller.isCheckUpdatesEnabled());
            }
          });
      
      this.checkUpdatesNowButton = new Button(activity);poo.setText(new ResourceAction.ButtonAction(
          new ResourceAction(preferences, com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "CHECK_UPDATES_NOW", true) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.checkUpdates();
            }
          }));
    }*/
    

    if (controller.isPropertyEditable(UserPreferencesController.Property.AUTO_SAVE_DELAY_FOR_RECOVERY)) {
      this.autoSaveDelayForRecoveryCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "autoSaveDelayForRecoveryCheckBox.text"));
		// PJ min number altered to 5 cos my spinner needs exact maths!
      final SpinnerNumberModel autoSaveDelayForRecoverySpinnerModel = new SpinnerNumberModel(10, 5, 60, 5);/* {
          @Override
          public Object getNextValue() {
            if (((Number)getValue()).intValue() == ((Number)getMinimum()).intValue()) {
              return getStepSize();
            } else {
              return super.getNextValue();
            }
          }
          
          @Override
          public Object getPreviousValue() {
            if (((Number)getValue()).intValue() - ((Number)getStepSize()).intValue() < ((Number)getMinimum()).intValue()) {
              return super.getMinimum();
            } else {
              return super.getPreviousValue();
            }
          }
        };*/
      this.autoSaveDelayForRecoverySpinner = new AutoCommitSpinner(activity, autoSaveDelayForRecoverySpinnerModel);
      this.autoSaveDelayForRecoveryUnitLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "autoSaveDelayForRecoveryUnitLabel.text"));
      updateAutoSaveDelayForRecoveryComponents(controller);
      this.autoSaveDelayForRecoveryCheckBox.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setAutoSaveForRecoveryEnabled(autoSaveDelayForRecoveryCheckBox.isChecked());
          }
        });
      autoSaveDelayForRecoverySpinnerModel.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setAutoSaveDelayForRecovery((int)(autoSaveDelayForRecoverySpinnerModel.getValue() * 60000));
          }
        });
      PropertyChangeListener listener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateAutoSaveDelayForRecoveryComponents(controller);
          }
        };
      controller.addPropertyChangeListener(UserPreferencesController.Property.AUTO_SAVE_DELAY_FOR_RECOVERY, listener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.AUTO_SAVE_FOR_RECOVERY_ENABLED, listener);
    }
    
    this.resetDisplayedActionTipsButton = new JButton(activity,
			  SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "RESET_DISPLAYED_ACTION_TIPS.Name"));
	  resetDisplayedActionTipsButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View view)
		  {
			  controller.resetDisplayedActionTips();
          }
        });
    
    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "preferences.title");

	  this.closeButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.HomePane.class, "CLOSE.Name"));
	  closeButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View view)
		  {
			  activity.invalidateOptionsMenu();
			  UserPreferencesPanel.this.dismiss();
		  }
	  });
  }

  /**
   * Returns a renderer for patterns combo box.
   */
  private class PatternRenderer extends ArrayAdapter {
	  public  PatternRenderer(Object[] images)
	  {
			super(activity, android.R.layout.simple_list_item_1, images);
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent)
	  {
		  return getAllViews(position, convertView, parent);
	  }
	  public View getDropDownView (int position, View convertView, ViewGroup parent)
	  {
		  return getAllViews(position, convertView, parent);
	  }

	  private View getAllViews(int position, View convertView, ViewGroup parent)
	  {
		  TextureImage wallPattern = (TextureImage) this.getItem(position);
		  final BufferedImage patternImage = SwingTools.getPatternImage(wallPattern, Color.WHITE, Color.BLACK);

		  ImageView imageView;
		  if (convertView == null)
		  {
			  // if it's not recycled, initialize some attributes
			  imageView = new WideImageView(activity);
			  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			  imageView.setPadding(15, 15, 15, 15);
			  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
		  }
		  else
		  {
			  imageView = (ImageView) convertView;
		  }

		  imageView.setImageBitmap(((Bitmap) patternImage.getDelegate()));

		  return imageView;
	  }

	  private class WideImageView extends ImageView
	  {
		  private VMBufferedImage patternImage;

		  public WideImageView(Context context)
		  {
			  super(context);
		  }

		  public void setImageBitmap(Bitmap bm)
		  {
			  super.setImageBitmap(bm);
			  patternImage = new VMBufferedImage(bm);
		  }

		  public void onDraw(Canvas canvas)
		  {
			  Graphics2D g2D = new VMGraphics2D(canvas);
			  for (int i = 0; i < 4; i++)
			  {
				  g2D.drawImage(patternImage, i * patternImage.getWidth(), 1, null);
			  }
			  //g2D.setColor(getForegroundColor());
			  g2D.drawRect(0, 0, getIconWidth() - 2, getIconHeight() - 1);
		  }

		  public int getIconWidth()
		  {
			  return patternImage.getWidth() * 4 + 1;
		  }

		  public int getIconHeight()
		  {
			  return patternImage.getHeight() + 2;
		  }
	  }
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private class SupportedLanguagesChangeListener implements PropertyChangeListener {
    private WeakReference<UserPreferencesPanel> userPreferencesPanel;

    public SupportedLanguagesChangeListener(UserPreferencesPanel userPreferencesPanel) {
      this.userPreferencesPanel = new WeakReference<UserPreferencesPanel>(userPreferencesPanel);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If panel was garbage collected, remove this listener from preferences
      UserPreferencesPanel userPreferencesPanel = this.userPreferencesPanel.get();
      if (userPreferencesPanel == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.SUPPORTED_LANGUAGES, this);
      } else {
		  JComboBox languageComboBox = userPreferencesPanel.languageComboBox;
        List<String> oldSupportedLanguages = Arrays.asList((String [])ev.getOldValue());
        String [] supportedLanguages = (String [])ev.getNewValue();
        languageComboBox.setModel(new DefaultComboBoxModel(supportedLanguages));
        // Select the first language added to supported languages
        for (String language : supportedLanguages) {
          if (!oldSupportedLanguages.contains(language)) {
            languageComboBox.setSelectedItem(language);
            return;
          }
        }
        languageComboBox.setSelectedItem(userPreferencesPanel.controller.getLanguage());
      }
    }
  }

  private void updateAutoSaveDelayForRecoveryComponents(UserPreferencesController controller) {
    int autoSaveDelayForRecoveryInMinutes = controller.getAutoSaveDelayForRecovery() / 60000;
    boolean autoSaveForRecoveryEnabled = controller.isAutoSaveForRecoveryEnabled();
    this.autoSaveDelayForRecoverySpinner.setEnabled(autoSaveForRecoveryEnabled);
    this.autoSaveDelayForRecoveryCheckBox.setSelected(autoSaveForRecoveryEnabled);
    if (autoSaveForRecoveryEnabled) {
      this.autoSaveDelayForRecoverySpinner.setValue(autoSaveDelayForRecoveryInMinutes);
    }
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
   /* if (!OperatingSystem.isMacOSX()) {
      if (this.languageLabel != null) {
        this.languageLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "languageLabel.mnemonic")).getKeyCode());
        this.languageLabel.setLabelFor(this.languageComboBox);
      }
      if (this.unitLabel != null) {
        this.unitLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "unitLabel.mnemonic")).getKeyCode());
        this.unitLabel.setLabelFor(this.unitComboBox);
      }
      if (this.furnitureCatalogViewLabel != null) {
        this.treeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "treeRadioButton.mnemonic")).getKeyCode());
        this.listRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "listRadioButton.mnemonic")).getKeyCode());
      }
      if (this.navigationPanelLabel != null) {
        this.navigationPanelCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "navigationPanelCheckBox.mnemonic")).getKeyCode());
      }
      if (this.magnetismLabel != null) {
        this.magnetismCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "magnetismCheckBox.mnemonic")).getKeyCode());
      }
      if (this.aerialViewCenteredOnSelectionLabel != null) {
        this.aerialViewCenteredOnSelectionCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "aerialViewCenteredOnSelectionCheckBox.mnemonic")).getKeyCode());
      }
      if (this.rulersLabel != null) {
        this.rulersCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "rulersCheckBox.mnemonic")).getKeyCode());
      }
      if (this.gridLabel != null) {
        this.gridCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "gridCheckBox.mnemonic")).getKeyCode());
      }
      if (this.defaultFontNameLabel != null) {
        this.defaultFontNameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "defaultFontNameLabel.mnemonic")).getKeyCode());
        this.defaultFontNameLabel.setLabelFor(this.defaultFontNameComboBox);
      }
      if (this.furnitureIconLabel != null) {
        this.catalogIconRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "catalogIconRadioButton.mnemonic")).getKeyCode());
        this.topViewRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "topViewRadioButton.mnemonic")).getKeyCode());
      }
      if (this.roomRenderingLabel != null) {
        this.monochromeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "monochromeRadioButton.mnemonic")).getKeyCode());
        this.floorColorOrTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "floorColorOrTextureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.newWallPatternLabel != null) {
        this.newWallPatternLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newWallPatternLabel.mnemonic")).getKeyCode());
        this.newWallPatternLabel.setLabelFor(this.newWallPatternComboBox);
      } else if (this.wallPatternLabel != null) {
        this.wallPatternLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "wallPatternLabel.mnemonic")).getKeyCode());
        this.wallPatternLabel.setLabelFor(this.wallPatternComboBox);
      } 
      if (this.newWallThicknessLabel != null) {
        this.newWallThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newWallThicknessLabel.mnemonic")).getKeyCode());
        this.newWallThicknessLabel.setLabelFor(this.newWallThicknessSpinner);
      }
      if (this.newWallHeightLabel != null) {
        this.newWallHeightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newWallHeightLabel.mnemonic")).getKeyCode());
        this.newWallHeightLabel.setLabelFor(this.newWallHeightSpinner);
      }      
      if (this.newFloorThicknessLabel != null) {
        this.newFloorThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "newFloorThicknessLabel.mnemonic")).getKeyCode());
        this.newFloorThicknessLabel.setLabelFor(this.newFloorThicknessSpinner);
      }
      if (this.checkUpdatesCheckBox != null) {
        this.checkUpdatesCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "checkUpdatesCheckBox.mnemonic")).getKeyCode());
      }      
      if (this.autoSaveDelayForRecoveryCheckBox != null) {
        this.autoSaveDelayForRecoveryCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.UserPreferencesPanel.class, "autoSaveDelayForRecoveryCheckBox.mnemonic")).getKeyCode());
      }      
    }*/
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {


   /* int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    Insets labelInsets = new Insets(0, 0, 5, 5);
    Insets labelInsetsWithSpace = new Insets(0, 0, 10, 5);
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    Insets rightComponentInsetsWithSpace = new Insets(0, 0, 10, 0);*/
	  Resources r = activity.getResources();
	  int px5dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
	  int px10dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
	  int px15dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());
	  int px20dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

	  rootView.setPadding(px10dp,px10dp,px10dp,px10dp);

	  LinearLayout.LayoutParams  labelInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  labelInsets.setMargins(px10dp,px10dp,px15dp,px15dp);
	  LinearLayout.LayoutParams  labelInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  labelInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px15dp);
	  LinearLayout.LayoutParams  rightComponentInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rightComponentInsets.setMargins(px10dp,px10dp,px15dp,px10dp);
	  LinearLayout.LayoutParams  rightComponentInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rightComponentInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px10dp);


    if (this.languageLabel != null) {
      // First row
      rootView.addView(this.languageLabel, labelInsets);//, params);//, new GridBagConstraints(
          //0, 0, 1, 1, 0, 0, labelAlignment,
          //GridBagConstraints.NONE, labelInsets, 0, 0));
		this.rootView.addView(this.languageComboBox, rightComponentInsets);//, params);//, new GridBagConstraints(
        //  1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
         // GridBagConstraints.HORIZONTAL, new Insets(OperatingSystem.isMacOSX() ? 1 : 0, 0, 5, 0), 0, 0));
      if (this.languageLibraryImportButton != null) {
        rootView.addView(this.languageLibraryImportButton, labelInsets);//, params);//, new GridBagConstraints(
        //    2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        //    GridBagConstraints.NONE, new Insets(0, 5, 5, 0), 0, 0));
      }
    }
    if (this.unitLabel != null) {
      // Second row
      rootView.addView(this.unitLabel, labelInsets);//, new GridBagConstraints(
       //   0, 1, 1, 1, 0, 0, labelAlignment, 
       //   GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.unitComboBox, rightComponentInsets);//, new GridBagConstraints(
       //   1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
       //   GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
      // Keep third row empty (used to contain unit radio buttons)
    }
    if (this.furnitureCatalogViewLabel != null) {
      // Fourth row
      rootView.addView(this.furnitureCatalogViewLabel, labelInsets);//, new GridBagConstraints(
      //    0, 3, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.treeRadioButton, labelInsets);//, new GridBagConstraints(
      //    1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.listRadioButton, rightComponentInsets);//, new GridBagConstraints(
      //    2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.navigationPanelLabel != null) {
      // Fifth row
      rootView.addView(this.navigationPanelLabel, labelInsets);//, new GridBagConstraints(
      //    0, 4, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.navigationPanelCheckBox, rightComponentInsets);//, new GridBagConstraints(
       //   1, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
       //   GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.aerialViewCenteredOnSelectionLabel != null) {
      // Sixth row
      rootView.addView(this.aerialViewCenteredOnSelectionLabel, labelInsetsWithSpace);//, new GridBagConstraints(
      //    0, 5, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsetsWithSpace, 0, 0));
      rootView.addView(this.aerialViewCenteredOnSelectionCheckBox, rightComponentInsetsWithSpace);//, new GridBagConstraints(
      //    1, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsetsWithSpace, 0, 0));
    }
    if (this.magnetismLabel != null) {
      // Seventh row
      rootView.addView(this.magnetismLabel, labelInsets);//, new GridBagConstraints(
       //   0, 6, 1, 1, 0, 0, labelAlignment, 
       //   GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.magnetismCheckBox, rightComponentInsets);//, new GridBagConstraints(
      //    1, 6, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.rulersLabel != null) {
      // Eighth row
      rootView.addView(this.rulersLabel, labelInsets);//, new GridBagConstraints(
      //    0, 7, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.rulersCheckBox, rightComponentInsets);//, new GridBagConstraints(
      //    1, 7, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.gridLabel != null) {
      // Ninth row
      rootView.addView(this.gridLabel, labelInsets);//, new GridBagConstraints(
       //   0, 8, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.gridCheckBox, rightComponentInsets);//, new GridBagConstraints(
      //    1, 8, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.defaultFontNameLabel != null) {
      // Tenth row
      rootView.addView(this.defaultFontNameLabel, labelInsets);//, new GridBagConstraints(
      //    0, 9, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
     /* Dimension preferredSize = this.defaultFontNameComboBox.getPreferredSize();
      if (this.unitComboBox != null 
          && this.floorColorOrTextureRadioButton != null) {
        preferredSize.width = Math.min(preferredSize.width, 
            this.unitComboBox.getPreferredSize().width + 5 + this.floorColorOrTextureRadioButton.getPreferredSize().width);
      } else {
        preferredSize.width = Math.min(preferredSize.width, 250); 
      }
      this.defaultFontNameComboBox.setPreferredSize(preferredSize);*/
      rootView.addView(this.defaultFontNameComboBox, rightComponentInsets);//, new GridBagConstraints(
       //   1, 9, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
       //   GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.furnitureIconLabel != null) {
      // Eleventh row
      rootView.addView(this.furnitureIconLabel, labelInsets);//, new GridBagConstraints(
      //    0, 10, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.catalogIconRadioButton, labelInsets);//, new GridBagConstraints(
      //    1, 10, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.topViewRadioButton, rightComponentInsets);//, new GridBagConstraints(
      //    2, 10, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.roomRenderingLabel != null) {
      // Twelfth row
      rootView.addView(this.roomRenderingLabel, labelInsets);//, new GridBagConstraints(
      //    0, 11, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.monochromeRadioButton, labelInsets);//, new GridBagConstraints(
      //    1, 11, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.floorColorOrTextureRadioButton, rightComponentInsets);//, new GridBagConstraints(
      //    2, 11, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.newWallPatternLabel != null) {
      // Thirteenth row
      rootView.addView(this.newWallPatternLabel, labelInsets);//, new GridBagConstraints(
       //   0, 12, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.newWallPatternComboBox, rightComponentInsets);//, new GridBagConstraints(
      //    1, 12, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    } else if (this.wallPatternLabel != null) {
      rootView.addView(this.wallPatternLabel, labelInsets);//, new GridBagConstraints(
      //    0, 12, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.wallPatternComboBox, rightComponentInsets);//, new GridBagConstraints(
      //    1, 12, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    } 
    if (this.newWallThicknessLabel != null) {
      // Fourteenth row
      rootView.addView(this.newWallThicknessLabel, labelInsets);//, new GridBagConstraints(
      //    0, 13, 1, 1, 0, 0, labelAlignment, 
      //    GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.newWallThicknessSpinner, rightComponentInsets);//, new GridBagConstraints(
      //    1, 13, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
      //    GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.newWallHeightLabel != null) {
      // Fifteenth row
      rootView.addView(this.newWallHeightLabel, labelInsets);//, new GridBagConstraints(
       //   0, 14, 1, 1, 0, 0, labelAlignment, 
       //   GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.newWallHeightSpinner, rightComponentInsets);//, new GridBagConstraints(
       //   1, 14, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
       //   GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.newFloorThicknessLabel != null) {
      // Sixteenth row
      rootView.addView(this.newFloorThicknessLabel, labelInsets);//, new GridBagConstraints(
       //   0, 15, 1, 1, 0, 0, labelAlignment, 
       //   GridBagConstraints.NONE, labelInsets, 0, 0));
      rootView.addView(this.newFloorThicknessSpinner, rightComponentInsets);//, new GridBagConstraints(
       //   1, 15, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
       //   GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
/*    if (this.checkUpdatesCheckBox != null
        || this.autoSaveDelayForRecoveryCheckBox != null) {
      // Seventeenth row
      JPanel updatesAndAutoSaveDelayForRecoveryPanel = new JPanel(new GridBagLayout());
      if (this.checkUpdatesCheckBox != null) {
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.checkUpdatesCheckBox,
            new GridBagConstraints(
             //   0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
             //   GridBagConstraints.NONE, labelInsets, 0, 0));
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.checkUpdatesNowButton,
            new GridBagConstraints(
             //   1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
             //   GridBagConstraints.NONE, rightComponentInsets, 0, 0));
      }
      if (this.autoSaveDelayForRecoveryCheckBox != null) {
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.autoSaveDelayForRecoveryCheckBox,
            new GridBagConstraints(
            //    0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            //    GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.autoSaveDelayForRecoverySpinner,
            new GridBagConstraints(
             //   1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
             //   GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        updatesAndAutoSaveDelayForRecoveryPanel.add(this.autoSaveDelayForRecoveryUnitLabel,
            new GridBagConstraints(
             //   2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
             //   GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      }
      add(updatesAndAutoSaveDelayForRecoveryPanel, params);//, new GridBagConstraints(
        //  0, 16, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        //  GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }*/
    // Last row
    if (this.resetDisplayedActionTipsButton.getText() != null
        && this.resetDisplayedActionTipsButton.getText().length() > 0) {
      // Display reset button only if its text isn't empty 
		rootView.addView(this.resetDisplayedActionTipsButton, labelInsets);//, new GridBagConstraints(
        //  0, 17, 3, 1, 0, 0, GridBagConstraints.CENTER, 
        //  GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }

	  this.setTitle(dialogTitle);
	  rootView.addView(closeButton, labelInsets);
  }

  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(VCView parentView) {
   /* if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.languageComboBox) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyUserPreferences();
    }*/

	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modifyUserPreferences();
		  }
	  });
	  this.show();
  }

  private static class SpinnerLengthModel extends SpinnerNumberModel {
    public SpinnerLengthModel(final float centimeterStepSize,
                              final float inchStepSize,
                              final UserPreferencesController controller) {
      // Invoke constructor that take objects in parameter to avoid any ambiguity
      super(new Float(1f), new Float(0f), new Float(400f), new Float(centimeterStepSize));
      // Add a listener to update step when unit changes
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            updateStepsAndLength(centimeterStepSize, inchStepSize, controller);
          }
        });
      updateStepsAndLength(centimeterStepSize, inchStepSize, controller);
    }

    private void updateStepsAndLength(float centimeterStepSize,
                                      float inchStepSize,
                                      UserPreferencesController controller) {
      if (controller.getUnit() == LengthUnit.INCH
          || controller.getUnit() == LengthUnit.INCH_DECIMALS) {
        setStepSize(LengthUnit.inchToCentimeter(inchStepSize));
      } else {
        setStepSize(centimeterStepSize);
      }
      fireStateChanged();
    }
  }

  private class AutoCommitLengthSpinner extends AutoCommitSpinner {
    public AutoCommitLengthSpinner(SpinnerNumberModel model,
                                   final UserPreferencesController controller) {
      super(activity, model, controller.getUnit().getFormat());
      // Add a listener to update format when unit changes 
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setFormat((DecimalFormat)controller.getUnit().getFormat());
          }
        });
    }
  }


}
