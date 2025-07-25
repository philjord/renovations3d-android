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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JImageButton;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JRadioButton;
import com.mindblowing.swingish.JSpinner;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.swingish.ChangeListener;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.mindblowing.swingish.ButtonGroup;
import com.mindblowing.swingish.DefaultComboBoxModel;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.SpinnerNumberModel;
import com.mindblowing.renovations3d.R;

import javaawt.Color;
import javaawt.EventQueue;
import javaawt.Graphics2D;
import javaawt.VMGraphics2D;
import javaawt.image.BufferedImage;
import javaawt.image.VMBufferedImage;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class UserPreferencesPanel extends AndroidDialogView implements DialogView {
  	private final UserPreferencesController controller;
	private JLabel           languageLabel;
	private JComboBox        languageComboBox;
	private JImageButton languageLibraryImportButton;
	private JLabel           unitLabel;
	private JComboBox        unitComboBox;
	private JLabel           currencyLabel;
	private JComboBox        currencyComboBox;
	private JCheckBox        valueAddedTaxCheckBox;
	//private JLabel           furnitureCatalogViewLabel;
	//private JRadioButton     treeRadioButton;
	//private JRadioButton     listRadioButton;
	private JLabel           navigationPanelLabel;
	private JCheckBox        navigationPanelCheckBox;
	private JLabel           aerialViewCenteredOnSelectionLabel;
	private JCheckBox        aerialViewCenteredOnSelectionCheckBox;
  	private JLabel           observerCameraSelectedAtChangeLabel;
  	private JCheckBox        observerCameraSelectedAtChangeCheckBox;
	private JLabel           magnetismLabel;
	private JCheckBox        magnetismCheckBox;
	private JLabel           rulersLabel;
	private JCheckBox 			 rulersCheckBox;
	private JLabel           gridLabel;
	private JCheckBox        gridCheckBox;
	//private JLabel           defaultFontNameLabel;
	//private FontNameComboBox defaultFontNameComboBox;
	//private JLabel           furnitureIconLabel;
	//private JRadioButton 		 catalogIconRadioButton;
	//private JRadioButton     topViewRadioButton;
	private JLabel           iconSizeLabel;
	private JComboBox        iconSizeComboBox;
	private JLabel           roomRenderingLabel;
	private JRadioButton     monochromeRadioButton;
	private JRadioButton     floorColorOrTextureRadioButton;
	private JLabel           wallPatternLabel;
	private JComboBox        wallPatternComboBox;
	private JLabel           newWallPatternLabel;
	private JComboBox        newWallPatternComboBox;
	private JLabel           newWallThicknessLabel;
	private JSpinner 				 newWallThicknessSpinner;
	private JLabel           newWallHeightLabel;
	private JSpinner 				 newWallHeightSpinner;
	private JLabel           newFloorThicknessLabel;
	private JSpinner 				 newFloorThicknessSpinner;
	//private JCheckBox        checkUpdatesCheckBox;
	//private JButton 				 checkUpdatesNowButton;
	//private JCheckBox        autoSaveDelayForRecoveryCheckBox;
	//private JSpinner 				 autoSaveDelayForRecoverySpinner;
	//private JLabel           autoSaveDelayForRecoveryUnitLabel;
	private JButton          resetDisplayedActionTipsButton;

	private JCheckBox        showPagerButtons;
	private JCheckBox        showPlanZoomButtons;
	private JCheckBox        doubleTapEdit2D;
	private JCheckBox        doubleTapEdit3D;
	private String           dialogTitle;

  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>. 
   */
  public UserPreferencesPanel(UserPreferences preferences,
                              UserPreferencesController controller, Activity activity) {
  	super(preferences, activity, R.layout.dialog_preferences);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents();
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(final UserPreferences preferences,
								final UserPreferencesController controller) {
		if (controller.isPropertyEditable(UserPreferencesController.Property.LANGUAGE)) {
			// Create language label and combo box bound to controller LANGUAGE property
			this.languageLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "languageLabel.text"));
			this.languageComboBox = new JComboBox(activity, new DefaultComboBoxModel(preferences.getSupportedLanguages()));
			languageComboBox.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, preferences.getSupportedLanguages()) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					return getDropDownView(position, convertView, parent);
				}

				@Override
				public View getDropDownView(int position, View convertView, ViewGroup parent) {
					TextView ret = new TextView(getContext());
					String language = (String) languageComboBox.getItemAtPosition(position);
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
			this.languageComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setLanguage((String) languageComboBox.getSelectedItem());
					Renovations3DActivity.logFireBaseLevelUp("setLanguage", (String) languageComboBox.getSelectedItem());
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

		if (controller.mayImportLanguageLibrary()) {
			this.languageLibraryImportButton = new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(
					com.eteks.sweethome3d.swing.UserPreferencesPanel.class.getResource("resources/actions/language-import.png")).getImage().getDelegate());
 	  		languageLibraryImportButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view) {
					//We can't use this as it gets onto the the EDT and cause much trouble, so we just copy out
					//controller.importLanguageLibrary();
					Thread t3 = new Thread() {
						public void run() {
							HomeController controller = ((Renovations3DActivity) activity).getHomeController();
							if (controller != null) {
								String languageLibraryName = controller.getView().showImportLanguageLibraryDialog();
								if (languageLibraryName != null) {
									controller.importLanguageLibrary(languageLibraryName);
									Renovations3DActivity.logFireBaseLevelUp("languageLibraryName", languageLibraryName);
								}
							}}};
					t3.start();;
				}
			  });
		}

		if (controller.isPropertyEditable(UserPreferencesController.Property.UNIT)) {
			// Create unit label and combo box bound to controller UNIT property
			this.unitLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "unitLabel.text"));
			this.unitComboBox = new JComboBox(activity, new DefaultComboBoxModel(LengthUnit.values()));
			final Map<LengthUnit, String> comboBoxTexts = new HashMap<LengthUnit, String>();
			comboBoxTexts.put(LengthUnit.MILLIMETER, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "unitComboBox.millimeter.text"));
			comboBoxTexts.put(LengthUnit.CENTIMETER, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "unitComboBox.centimeter.text"));
			comboBoxTexts.put(LengthUnit.METER, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "unitComboBox.meter.text"));
			comboBoxTexts.put(LengthUnit.INCH, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "unitComboBox.inch.text"));
			comboBoxTexts.put(LengthUnit.INCH_DECIMALS, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "unitComboBox.inchDecimals.text"));
			unitComboBox.setAdapter(new ArrayAdapter<LengthUnit>(activity, android.R.layout.simple_list_item_1, LengthUnit.values()) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					return getDropDownView(position, convertView, parent);
				}

				@Override
				public View getDropDownView(int position, View convertView, ViewGroup parent) {
					TextView ret = new TextView(getContext());
					ret.setText(comboBoxTexts.get(unitComboBox.getItemAtPosition(position)));
					return ret;
				}
			});
			this.unitComboBox.setSelectedItem(controller.getUnit());
			this.unitComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setUnit((LengthUnit) unitComboBox.getSelectedItem());
				}
			});
			controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
							new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent ev) {
									unitComboBox.setSelectedItem(controller.getUnit());
								}
							});
		}

		if (controller.isPropertyEditable(UserPreferencesController.Property.CURRENCY)) {
			// Create currency label and combo box bound to controller CURRENCY property
			this.currencyLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "currencyLabel.text"));

			final Map<String, String> currencyMap = new HashMap<String, String>();
			for (Locale locale : Locale.getAvailableLocales()) {
				try {
					Currency currency = Currency.getInstance(locale);
					if (currencyMap.get(currency.getCurrencyCode()) == null
									|| currencyMap.get(currency.getCurrencyCode()).length() > currency.getSymbol(locale).length()) {
						currencyMap.put(currency.getCurrencyCode(), currency.getSymbol(locale));
					}
				} catch (IllegalArgumentException ex) {
					// Currency in Locale not found
				}
			}
			ArrayList<String> currencies = new ArrayList<String>(currencyMap.keySet());
			Collections.sort(currencies);
			Locale defaultLocale = Locale.getDefault();
			Currency defaultLocaleCurrency = defaultLocale.getCountry() != null && defaultLocale.getCountry().length() == 2
							? Currency.getInstance(defaultLocale)
							: null;
			if (defaultLocaleCurrency != null) {
				currencies.add(0, defaultLocaleCurrency.getCurrencyCode()); // Add also the currency of the current locale
			}
			currencies.add(0, null);
			this.currencyComboBox = new JComboBox(activity, currencies);
			final String noCurrencyText = preferences.getLocalizedString(com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "currencyComboBox.noCurrency.text");
			this.currencyComboBox.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, currencies) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					return getDropDownView(position, convertView, parent);
				}

				@Override
				public View getDropDownView(int position, View convertView, ViewGroup parent) {
					TextView ret = new TextView(getContext());
					Object value = currencyComboBox.getItemAtPosition(position);
					if (value == null) {
						ret.setText(noCurrencyText);
					} else {
						ret.setText(value + " " + currencyMap.get(value));
					}
					return ret;
				}
			});
			this.currencyComboBox.setSelectedItem(controller.getCurrency());
			this.currencyComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setCurrency((String)currencyComboBox.getSelectedItem());
				}
			});
			controller.addPropertyChangeListener(UserPreferencesController.Property.CURRENCY,
							new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent ev) {
									currencyComboBox.setSelectedItem(controller.getCurrency());
									if (valueAddedTaxCheckBox != null) {
										valueAddedTaxCheckBox.setEnabled(controller.getCurrency() != null);
									}
								}
							});

			if (controller.isPropertyEditable(UserPreferencesController.Property.VALUE_ADDED_TAX_ENABLED)) {
				this.valueAddedTaxCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
								com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "valueAddedTaxCheckBox.text"));
				this.valueAddedTaxCheckBox.setEnabled(controller.getCurrency() != null);
				this.valueAddedTaxCheckBox.setSelected(controller.isValueAddedTaxEnabled());
				this.valueAddedTaxCheckBox.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent ev) {
						controller.setValueAddedTaxEnabled(valueAddedTaxCheckBox.isSelected());
					}
				});
				controller.addPropertyChangeListener(UserPreferencesController.Property.VALUE_ADDED_TAX_ENABLED,
								new PropertyChangeListener() {
									public void propertyChange(PropertyChangeEvent ev) {
										valueAddedTaxCheckBox.setEnabled(controller.getCurrency() != null);
										valueAddedTaxCheckBox.setSelected(controller.isValueAddedTaxEnabled());
									}
								});
			}
		}

/*    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE)) {
      // Create furniture catalog label and radio buttons bound to controller FURNITURE_CATALOG_VIEWED_IN_TREE property
      this.furnitureCatalogViewLabel = new TextView(activity);furnitureCatalogViewLabel.setText(preferences.getLocalizedString(
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "furnitureCatalogViewLabel.text"));
      this.treeRadioButton = new RadioButton(activity);treeRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "treeRadioButton.text"));treeRadioButton.setChecked(
          controller.isFurnitureCatalogViewedInTree());
      this.listRadioButton = new RadioButton(activity);listRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "listRadioButton.text"));listRadioButton.setChecked(
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

    boolean no3D = false;
    /*try {
      no3D = Boolean.getBoolean("com.eteks.sweethome3d.no3D");
    } catch (AccessControlException ex) {
      // If com.eteks.sweethome3d.no3D property can't be read, 
      // security manager won't allow to access to Java 3D DLLs required by 3D view too
      no3D = true;
    }*/
    if (controller.isPropertyEditable(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE)
        && !no3D) {
      // Create navigation panel label and check box bound to controller NAVIGATION_PANEL_VISIBLE property
      this.navigationPanelLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "navigationPanelLabel.text"));
      this.navigationPanelCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "navigationPanelCheckBox.text"));
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
    }

		if (controller.isPropertyEditable(UserPreferencesController.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED)) {
			// Create aerialViewCenteredOnSelection label and check box bound to controller AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED property
			this.aerialViewCenteredOnSelectionLabel = new JLabel(activity, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "aerialViewCenteredOnSelectionLabel.text"));
			this.aerialViewCenteredOnSelectionCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "aerialViewCenteredOnSelectionCheckBox.text"), controller.isAerialViewCenteredOnSelectionEnabled());
			this.aerialViewCenteredOnSelectionCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setAerialViewCenteredOnSelectionEnabled(aerialViewCenteredOnSelectionCheckBox.isSelected());
				}
			});
			controller.addPropertyChangeListener(UserPreferencesController.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED,
							new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent ev) {
									aerialViewCenteredOnSelectionCheckBox.setSelected(controller.isAerialViewCenteredOnSelectionEnabled());
								}
							});
		}
		//NOTICE disabled to avoid observer selection removing 3d selection
		if (false && controller.isPropertyEditable(UserPreferencesController.Property.OBSERVER_CAMERA_SELECTED_AT_CHANGE)) {
			// Create observerCameraSelectedAtChangeLabel label and check box bound to controller OBSERVER_CAMERA_SELECTED_AT_CHANGE property
			this.observerCameraSelectedAtChangeLabel = new JLabel(activity, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "observerCameraSelectedAtChangeLabel.text"));
			this.observerCameraSelectedAtChangeCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "observerCameraSelectedAtChangeCheckBox.text"), controller.isObserverCameraSelectedAtChange());
			this.observerCameraSelectedAtChangeCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setObserverCameraSelectedAtChange(observerCameraSelectedAtChangeCheckBox.isSelected());
				}
			});
			controller.addPropertyChangeListener(UserPreferencesController.Property.OBSERVER_CAMERA_SELECTED_AT_CHANGE,
							new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent ev) {
									observerCameraSelectedAtChangeCheckBox.setSelected(controller.isObserverCameraSelectedAtChange());
								}
							});
		}

		if (controller.isPropertyEditable(UserPreferencesController.Property.MAGNETISM_ENABLED)) {
			// Create magnetism label and check box bound to controller MAGNETISM_ENABLED property
			this.magnetismLabel = new JLabel(activity, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "magnetismLabel.text"));
			this.magnetismCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "magnetismCheckBox.text"), controller.isMagnetismEnabled());
			this.magnetismCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setMagnetismEnabled(magnetismCheckBox.isSelected());
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
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "rulersLabel.text"));
			this.rulersCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "rulersCheckBox.text"), controller.isRulersVisible());
			this.rulersCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setRulersVisible(rulersCheckBox.isSelected());
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
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "gridLabel.text"));
			this.gridCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "gridCheckBox.text"), controller.isGridVisible());
			this.gridCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setGridVisible(gridCheckBox.isSelected());
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
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "defaultFontNameLabel.text"));
      this.defaultFontNameComboBox = new FontNameComboBox(preferences);
      this.defaultFontNameComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
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
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "furnitureIconLabel.text"));
      this.catalogIconRadioButton = new RadioButton(activity);catalogIconRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "catalogIconRadioButton.text"),
          !controller.isFurnitureViewedFromTop());
      this.topViewRadioButton = new RadioButton(activity);topViewRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "topViewRadioButton.text"),
          controller.isFurnitureViewedFromTop());
      if (!no3D) {
        this.iconSizeLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            UserPreferencesPanel.class, "iconSizeLabel.text"));
        Set<Integer> iconSizes = new TreeSet<Integer>(Arrays.asList(128, 256, 512 ,1024));
        iconSizes.add(controller.getFurnitureModelIconSize());
        this.iconSizeComboBox = new JComboBox(iconSizes.toArray());
        this.iconSizeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
              return super.getListCellRendererComponent(list, value + "\u00d7" + value, index, isSelected, cellHasFocus);
            }
          });
        this.iconSizeComboBox.setSelectedItem(controller.getFurnitureModelIconSize());
        this.iconSizeComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
              controller.setFurnitureModelIconSize((Integer)iconSizeComboBox.getSelectedItem());
            }
          });
        controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_MODEL_ICON_SIZE,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                iconSizeComboBox.setSelectedItem(controller.getFurnitureModelIconSize());
              }
            });
      }

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
                controller.setFurnitureViewedFromTop(topViewRadioButton.isSelected());
              }
            };
          this.catalogIconRadioButton.addItemListener(furnitureAppearanceChangeListener);
          this.topViewRadioButton.addItemListener(furnitureAppearanceChangeListener);
          controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP, 
              new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                  topViewRadioButton.setSelected(controller.isFurnitureViewedFromTop());
                  if (iconSizeComboBox != null) {
                  	iconSizeComboBox.setEnabled(controller.isFurnitureViewedFromTop());
                	}
                }
              });
          if (this.iconSizeComboBox != null) {
          	this.iconSizeComboBox.setEnabled(controller.isFurnitureViewedFromTop());
          }
        } else {
          this.catalogIconRadioButton.setEnabled(false);
          this.topViewRadioButton.setEnabled(false);
        }
      }
    }*/

		if (controller.isPropertyEditable(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED)) {
			// Create room rendering label and radio buttons bound to controller ROOM_FLOOR_COLORED_OR_TEXTURED property
			this.roomRenderingLabel = new JLabel(activity, preferences.getLocalizedString(
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "roomRenderingLabel.text"));
			this.monochromeRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "monochromeRadioButton.text"),
							!controller.isRoomFloorColoredOrTextured());
			this.floorColorOrTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "floorColorOrTextureRadioButton.text"),
							controller.isRoomFloorColoredOrTextured());
			ButtonGroup roomRenderingButtonGroup = new ButtonGroup();
			roomRenderingButtonGroup.add(this.monochromeRadioButton);
			roomRenderingButtonGroup.add(this.floorColorOrTextureRadioButton);
			View.OnClickListener roomRenderingChangeListener = new View.OnClickListener() {
				public void onClick(View v) {
					controller.setRoomFloorColoredOrTextured(floorColorOrTextureRadioButton.isSelected());
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
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "newWallPatternLabel.text"));
			List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
			this.newWallPatternComboBox = new JComboBox(activity, new DefaultComboBoxModel(patterns.toArray()));
			this.newWallPatternComboBox.setAdapter(new PatternRenderer(activity, patterns.toArray()));
			TextureImage newWallPattern = controller.getNewWallPattern();
			this.newWallPatternComboBox.setSelectedItem(newWallPattern != null
							? newWallPattern
							: controller.getWallPattern());
			this.newWallPatternComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setNewWallPattern((TextureImage) newWallPatternComboBox.getSelectedItem());
				}
			});
			controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_PATTERN,
							new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent ev) {
									newWallPatternComboBox.setSelectedItem(controller.getNewWallPattern());
								}
							});
		}
		else if (controller.isPropertyEditable(UserPreferencesController.Property.WALL_PATTERN)) {
			// Create wall pattern label and combo box bound to controller WALL_PATTERN property
			this.wallPatternLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "wallPatternLabel.text"));
			List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
			this.wallPatternComboBox = new JComboBox(activity, new DefaultComboBoxModel(patterns.toArray()));
			this.wallPatternComboBox.setAdapter(new PatternRenderer(activity, patterns.toArray()));
			this.wallPatternComboBox.setSelectedItem(controller.getWallPattern());
			this.wallPatternComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					controller.setWallPattern((TextureImage) wallPatternComboBox.getSelectedItem());
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
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "newWallThicknessLabel.text"));
			final SpinnerLengthModel newWallThicknessSpinnerModel = new SpinnerLengthModel(0.5f, 0.125f, controller);
			this.newWallThicknessSpinner = new AutoCommitLengthSpinner(newWallThicknessSpinnerModel, controller);
			newWallThicknessSpinnerModel.setValue(controller.getNewWallThickness());
			newWallThicknessSpinnerModel.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ev) {
					controller.setNewWallThickness(((Number) newWallThicknessSpinnerModel.getValue()).floatValue());
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
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "newWallHeightLabel.text"));
			final SpinnerLengthModel newWallHeightSpinnerModel = new SpinnerLengthModel(10f, 2f, controller);
			this.newWallHeightSpinner = new AutoCommitLengthSpinner(newWallHeightSpinnerModel, controller);
			newWallHeightSpinnerModel.setValue(controller.getNewWallHeight());
			newWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ev) {
					controller.setNewWallHeight(((Number) newWallHeightSpinnerModel.getValue()).floatValue());
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
							com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "newFloorThicknessLabel.text"));
			final SpinnerLengthModel newFloorThicknessSpinnerModel = new SpinnerLengthModel(0.5f, 0.125f, controller);
			this.newFloorThicknessSpinner = new AutoCommitLengthSpinner(newFloorThicknessSpinnerModel, controller);
			newFloorThicknessSpinnerModel.setValue(controller.getNewFloorThickness());
			newFloorThicknessSpinnerModel.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ev) {
					controller.setNewFloorThickness(((Number) newFloorThicknessSpinnerModel.getValue()).floatValue());
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
      this.checkUpdatesCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "checkUpdatesCheckBox.text"), controller.isCheckUpdatesEnabled());
      this.checkUpdatesCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setCheckUpdatesEnabled(checkUpdatesCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.CHECK_UPDATES_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              checkUpdatesCheckBox.setSelected(controller.isCheckUpdatesEnabled());
            }
          });
      
      this.checkUpdatesNowButton = new JButton(activity, new ResourceAction.ButtonAction(
          new ResourceAction(preferences, com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "CHECK_UPDATES_NOW", true) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.checkUpdates();
            }
          }));
    }*/

//PJ cut out as misleading
/*
    if (controller.isPropertyEditable(UserPreferencesController.Property.AUTO_SAVE_DELAY_FOR_RECOVERY)) {
      this.autoSaveDelayForRecoveryCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "autoSaveDelayForRecoveryCheckBox.text"));
		// PJ min number altered to 5 cos my spinner needs exact maths!
      final SpinnerNumberModel autoSaveDelayForRecoverySpinnerModel = new SpinnerNumberModel(3, 1, 20, 1);/* {
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
/*      this.autoSaveDelayForRecoverySpinner = new AutoCommitSpinner(activity, autoSaveDelayForRecoverySpinnerModel);
      this.autoSaveDelayForRecoveryUnitLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "autoSaveDelayForRecoveryUnitLabel.text"));
      updateAutoSaveDelayForRecoveryComponents(controller);
      this.autoSaveDelayForRecoveryCheckBox.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setAutoSaveForRecoveryEnabled(autoSaveDelayForRecoveryCheckBox.isSelected());
          }
        });
      autoSaveDelayForRecoverySpinnerModel.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
            controller.setAutoSaveDelayForRecovery(((Number)autoSaveDelayForRecoverySpinnerModel.getValue()).intValue() * 60000);
          }
        });
      PropertyChangeListener listener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateAutoSaveDelayForRecoveryComponents(controller);
          }
        };
      controller.addPropertyChangeListener(UserPreferencesController.Property.AUTO_SAVE_DELAY_FOR_RECOVERY, listener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.AUTO_SAVE_FOR_RECOVERY_ENABLED, listener);
    }*/

	  final SharedPreferences settings = getContext().getSharedPreferences(Renovations3DActivity.PREFS_NAME, 0);

      this.resetDisplayedActionTipsButton = new JButton(activity,
			  SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "RESET_DISPLAYED_ACTION_TIPS.Name"));
	  resetDisplayedActionTipsButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					controller.resetDisplayedActionTips();

					//PJPJPJPJ
					// remind again for the welcome screens
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(MultipleLevelsPlanPanel.WELCOME_SCREEN_UNWANTED, false);
					editor.putBoolean(HomeComponent3D.WELCOME_SCREEN_UNWANTED, false);
					editor.putBoolean(FurnitureCatalogListPanel.WELCOME_SCREEN_UNWANTED, false);
					editor.putBoolean(FurnitureTable.WELCOME_SCREEN_UNWANTED, false);
					editor.apply();
					((Renovations3DActivity)activity).getWelcomeScreensShownThisSession().clear();
				}
    	});

	  //PJ----------------------------new Prefs for Renovations, using the local prefs storage system
	  boolean SHOW_PAGER_BUTTONS_PREF = settings.getBoolean(Renovations3DActivity.SHOW_PAGER_BUTTONS_PREF, Renovations3DActivity.SHOW_PAGER_BUTTONS);

	  this.showPagerButtons = new JCheckBox(activity, getContext().getString(R.string.showPagerButtons), SHOW_PAGER_BUTTONS_PREF);
	  this.showPagerButtons.addChangeListener(new ChangeListener() {
		  public void stateChanged(ChangeEvent ev) {
			  SharedPreferences.Editor editor = settings.edit();
			  editor.putBoolean(Renovations3DActivity.SHOW_PAGER_BUTTONS_PREF, showPagerButtons.isSelected());
			  editor.apply();
			  Renovations3DActivity.SHOW_PAGER_BUTTONS = showPagerButtons.isSelected();
				preferences.firePropertyChange(UserPreferences.Property.UPDATES_MINIMUM_DATE, !Renovations3DActivity.SHOW_PAGER_BUTTONS, Renovations3DActivity.SHOW_PAGER_BUTTONS);
		  }
	  });

		boolean SHOW_PLAN_ZOOM_BUTTONS_PREF = settings.getBoolean(Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS_PREF, Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS);

		this.showPlanZoomButtons = new JCheckBox(activity, getContext().getString(R.string.showPlanZoomButtons), SHOW_PLAN_ZOOM_BUTTONS_PREF);
		this.showPlanZoomButtons.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS_PREF, showPlanZoomButtons.isSelected());
				editor.apply();
				Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS = showPlanZoomButtons.isSelected();
				preferences.firePropertyChange(UserPreferences.Property.UPDATES_MINIMUM_DATE, !Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS, Renovations3DActivity.SHOW_PLAN_ZOOM_BUTTONS);
			}
		});

		boolean DOUBLE_TAP_EDIT_2D_PREF = settings.getBoolean(Renovations3DActivity.DOUBLE_TAP_EDIT_2D_PREF, Renovations3DActivity.DOUBLE_TAP_EDIT_2D);

		this.doubleTapEdit2D = new JCheckBox(activity, getContext().getString(R.string.doubleTapEdit2D), DOUBLE_TAP_EDIT_2D_PREF);
		this.doubleTapEdit2D.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(Renovations3DActivity.DOUBLE_TAP_EDIT_2D_PREF, doubleTapEdit2D.isSelected());
				editor.apply();
				Renovations3DActivity.DOUBLE_TAP_EDIT_2D = doubleTapEdit2D.isSelected();
				preferences.firePropertyChange(UserPreferences.Property.UPDATES_MINIMUM_DATE, !Renovations3DActivity.DOUBLE_TAP_EDIT_2D, Renovations3DActivity.DOUBLE_TAP_EDIT_2D);
			}
		});

		boolean DOUBLE_TAP_EDIT_3D_PREF = settings.getBoolean(Renovations3DActivity.DOUBLE_TAP_EDIT_3D_PREF, Renovations3DActivity.DOUBLE_TAP_EDIT_3D);

		this.doubleTapEdit3D = new JCheckBox(activity, getContext().getString(R.string.doubleTapEdit3D), DOUBLE_TAP_EDIT_3D_PREF);
		this.doubleTapEdit3D.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(Renovations3DActivity.DOUBLE_TAP_EDIT_3D_PREF, doubleTapEdit3D.isSelected());
				editor.apply();
				Renovations3DActivity.DOUBLE_TAP_EDIT_3D = doubleTapEdit3D.isSelected();
				preferences.firePropertyChange(UserPreferences.Property.UPDATES_MINIMUM_DATE, !Renovations3DActivity.DOUBLE_TAP_EDIT_3D, Renovations3DActivity.DOUBLE_TAP_EDIT_3D);
			}
		});

	  this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.swing.UserPreferencesPanel.class, "preferences.title");
  }

  /**
   * Returns a renderer for patterns combo box.
   */
  public static class PatternRenderer extends ArrayAdapter {

	  private Activity activity;
	  public  PatternRenderer(Activity activity, Object[] images) {
			super(activity, android.R.layout.simple_list_item_1, images);
		  this.activity = activity;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		  return getAllViews(position, convertView, parent);
	  }
	  public View getDropDownView (int position, View convertView, ViewGroup parent) {
		  return getAllViews(position, convertView, parent);
	  }

	  private View getAllViews(int position, View convertView, ViewGroup parent) {
		  TextureImage wallPattern = (TextureImage) this.getItem(position);
		  final BufferedImage patternImage = SwingTools.getPatternImage(wallPattern, Color.WHITE, Color.BLACK);

		  ImageView imageView;
		  if (convertView == null) {
			  // if it's not recycled, initialize some attributes
			  imageView = new WideImageView(activity);
			  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			  imageView.setPadding(15, 15, 15, 15);
			  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
		  } else {
			  imageView = (ImageView) convertView;
		  }

		  imageView.setImageBitmap(((Bitmap) patternImage.getDelegate()));

		  return imageView;
	  }

	  private class WideImageView extends ImageView {
		  private VMBufferedImage patternImage;

		  public WideImageView(Context context) {
			  super(context);
		  }

		  public void setImageBitmap(Bitmap bm) {
			  super.setImageBitmap(bm);
			  patternImage = new VMBufferedImage(bm);
		  }

		  public void onDraw(Canvas canvas) {
			  final float scale = activity.getResources().getDisplayMetrics().density * 2;//*2 cos it just needs to be a bit bigger
			  Graphics2D g2D = new VMGraphics2D(canvas);
			  for (int i = 0; i < 4; i++) {
				  g2D.drawImage(patternImage, (int)(i * patternImage.getWidth() * scale),  1,  (int)(patternImage.getWidth() * scale),  (int)(patternImage.getHeight() * scale), null);
			  }
			  //g2D.setColor(getForegroundColor());
			  g2D.drawRect(0, 0, (int)((getIconWidth() - 2) * scale), (int)((getIconHeight() - 1) * scale));
		  }

		  public int getIconWidth() {
			  return patternImage.getWidth() * 4 + 1;
		  }

		  public int getIconHeight() {
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
    
    public void propertyChange(final PropertyChangeEvent ev) {
      // If panel was garbage collected, remove this listener from preferences
      final UserPreferencesPanel userPreferencesPanel = this.userPreferencesPanel.get();
      if (userPreferencesPanel == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.SUPPORTED_LANGUAGES, this);
      } else {
				EventQueue.invokeLater(new Runnable(){
					public void run(){
						final JComboBox languageComboBox = userPreferencesPanel.languageComboBox;
						List<String> oldSupportedLanguages = Arrays.asList((String [])ev.getOldValue());
						String [] supportedLanguages = (String [])ev.getNewValue();
						languageComboBox.setModel(new DefaultComboBoxModel(supportedLanguages));
						languageComboBox.setSelectedItem(userPreferencesPanel.controller.getLanguage());
						// Select the first language added to supported languages
						for (final String language : supportedLanguages) {
							if (!oldSupportedLanguages.contains(language)) {
								languageComboBox.setSelectedItem(language);
								break;
							}
						}
					}
				});
      }
    }
  }

/*  private void updateAutoSaveDelayForRecoveryComponents(UserPreferencesController controller) {
    int autoSaveDelayForRecoveryInMinutes = controller.getAutoSaveDelayForRecovery() / 60000;
    boolean autoSaveForRecoveryEnabled = controller.isAutoSaveForRecoveryEnabled();
    this.autoSaveDelayForRecoverySpinner.setEnabled(autoSaveForRecoveryEnabled);
    this.autoSaveDelayForRecoveryCheckBox.setSelected(autoSaveForRecoveryEnabled);
    if (autoSaveForRecoveryEnabled) {
      this.autoSaveDelayForRecoverySpinner.setValue(autoSaveDelayForRecoveryInMinutes);
    }
  }*/

	/**
	 * Sets components mnemonics and label / component associations.
	 */
	private void setMnemonics(UserPreferences preferences) {
	}

	/**
	 * Layouts panel components in panel with their labels.
	 */
	private void layoutComponents() {
		if (this.languageLabel != null) {
			// First row
			swapOut(this.languageLabel, R.id.prefs_languageLabel);
			swapOut(this.languageComboBox, R.id.prefs_languageSpinner);

			if (this.languageLibraryImportButton != null) {
				swapOut(this.languageLibraryImportButton, R.id.prefs_languageLibraryImportButton);
			} else {
				removeView(R.id.prefs_languageLibraryImportButton);
			}
		} else {
			removeView(R.id.prefs_languageLabel);
			removeView(R.id.prefs_languageSpinner);
		}

		if (this.unitLabel != null) {
			// Second row
			swapOut(this.unitLabel, R.id.prefs_unitLabel);
			swapOut(this.unitComboBox, R.id.prefs_unitSpinner);
		} else {
			removeView(R.id.prefs_unitLabel);
			removeView(R.id.prefs_unitSpinner);
			// Keep third row empty (used to contain unit radio buttons)
		}
		if (this.currencyLabel != null) {
			// Fourth row
			swapOut(this.currencyLabel, R.id.prefs_currencyLabel);
			swapOut(this.currencyComboBox, R.id.prefs_currencySpinner);
			if (this.valueAddedTaxCheckBox != null) {
				swapOut(this.valueAddedTaxCheckBox, R.id.valueAddedTaxCheckBox);
			} else {
				removeView(R.id.valueAddedTaxCheckBox);
			}
		} else {
			removeView(R.id.prefs_currencyLabel);
			removeView(R.id.prefs_currencySpinner);
			removeView(R.id.valueAddedTaxCheckBox);
			removeView(R.id.prefs_currencyRow);
		}
		/* if (this.furnitureCatalogViewLabel != null) {
			// Fifth row
		  rootView.addView(this.furnitureCatalogViewLabel, labelInsets);
		  rootView.addView(this.treeRadioButton, labelInsets);
		  rootView.addView(this.listRadioButton, rightComponentInsets);
		}*/
		if (this.navigationPanelLabel != null) {
	   	// Sixth row
			swapOut(this.navigationPanelLabel, R.id.prefs_navigationPanelLabel);
			swapOut(this.navigationPanelCheckBox, R.id.prefs_navigationPanelCheckBox);
		} else {
			removeView(R.id.prefs_aerialViewCenteredOnSelectionLabel);
			removeView(R.id.prefs_aerialViewCenteredOnSelectionCheckBox);
		}
		if (this.aerialViewCenteredOnSelectionLabel != null) {
			// Seventh row
			swapOut(this.aerialViewCenteredOnSelectionLabel, R.id.prefs_aerialViewCenteredOnSelectionLabel);
			swapOut(this.aerialViewCenteredOnSelectionCheckBox, R.id.prefs_aerialViewCenteredOnSelectionCheckBox);
		} else {
			removeView(R.id.prefs_aerialViewCenteredOnSelectionLabel);
			removeView(R.id.prefs_aerialViewCenteredOnSelectionCheckBox);
		}
		//NOTICE disabled in create above to avoid observer selection removing 3d selection
		if (this.observerCameraSelectedAtChangeLabel != null) {
			// Eighth row
			swapOut(this.observerCameraSelectedAtChangeLabel, R.id.prefs_observerCameraSelectedAtChangeLabel);
			swapOut(this.observerCameraSelectedAtChangeCheckBox, R.id.prefs_observerCameraSelectedAtChangeCheckBox);
		} else {
			// note can't leave table rows empty
			swapOut(new TextView(activity), R.id.prefs_observerCameraSelectedAtChangeLabel);
			removeView(R.id.prefs_observerCameraSelectedAtChangeCheckBox);
		}
		if (this.magnetismLabel != null) {
			// Ninth row
			swapOut(this.magnetismLabel, R.id.prefs_magnetismLabel);
			swapOut(this.magnetismCheckBox, R.id.prefs_magnetismRadioButton);
		} else {
			removeView(R.id.prefs_magnetismLabel);
			removeView(R.id.prefs_magnetismRadioButton);
		}
		if (this.rulersLabel != null) {
			// Tenth row
			swapOut(this.rulersLabel, R.id.prefs_rulersLabel);
			swapOut(this.rulersCheckBox, R.id.prefs_rulersRadioButton);
		} else {
			removeView(R.id.prefs_rulersLabel);
			removeView(R.id.prefs_rulersRadioButton);
		}
		if (this.gridLabel != null) {
			// Eleventh row
			swapOut(this.gridLabel, R.id.prefs_gridLabel);
			swapOut(this.gridCheckBox, R.id.prefs_gridRadioButton);
		} else {
			removeView(R.id.prefs_gridLabel);
			removeView(R.id.prefs_gridRadioButton);
		}
		/*if (this.defaultFontNameLabel != null) {
			// Twelfth row
		  rootView.addView(this.defaultFontNameLabel, labelInsets);
		  Dimension preferredSize = this.defaultFontNameComboBox.getPreferredSize();
		  if (this.unitComboBox != null
			  && this.floorColorOrTextureRadioButton != null) {
				preferredSize.width = Math.min(preferredSize.width,
				this.unitComboBox.getPreferredSize().width + 5 + this.floorColorOrTextureRadioButton.getPreferredSize().width);
		  } else {
				preferredSize.width = Math.min(preferredSize.width, 250);
		  }
		  this.defaultFontNameComboBox.setPreferredSize(preferredSize);
		  rootView.addView(this.defaultFontNameComboBox, rightComponentInsets);
		}*/
		/*if (this.furnitureIconLabel != null) {
			// Thirteenth and fourteenth row
		  rootView.addView(this.furnitureIconLabel, labelInsets);
		  rootView.addView(this.catalogIconRadioButton, labelInsets);
		  rootView.addView(this.topViewRadioButton, rightComponentInsets);
		}*/
		if (this.roomRenderingLabel != null) {
			// Fifteenth row
			swapOut(this.roomRenderingLabel, R.id.prefs_roomRenderLabel);
			swapOut(this.monochromeRadioButton, R.id.prefs_roomRenderMonoRadioButton);
			swapOut(this.floorColorOrTextureRadioButton, R.id.prefs_roomRenderColorRadioButton);
		} else {
			removeView(R.id.prefs_roomRenderLabel);
			removeView(R.id.prefs_roomRenderMonoRadioButton);
			removeView(R.id.prefs_roomRenderColorRadioButton);
		}
		if (this.newWallPatternLabel != null) {
			// Sixteenth row
			swapOut(this.newWallPatternLabel, R.id.prefs_newWallsTextureLabel);
			swapOut(this.newWallPatternComboBox, R.id.prefs_newWallsTextureSpinner);
		} else if (this.wallPatternLabel != null) {
			swapOut(this.wallPatternLabel, R.id.prefs_newWallsTextureLabel);
			swapOut(this.wallPatternComboBox, R.id.prefs_newWallsTextureSpinner);
		} else {
			removeView(R.id.prefs_newWallsTextureLabel);
			removeView(R.id.prefs_newWallsTextureSpinner);
		}
		if (this.newWallThicknessLabel != null) {
			// Seventeenth row
			swapOut(this.newWallThicknessLabel, R.id.prefs_newWallsThicknessLabel);
			swapOut(this.newWallThicknessSpinner, R.id.prefs_newWallsThicknessSpinner);
		} else {
			removeView(R.id.prefs_newWallsThicknessLabel);
			removeView(R.id.prefs_newWallsThicknessSpinner);
		}
		if (this.newWallHeightLabel != null) {
			// Eighteenth row
			swapOut(this.newWallHeightLabel, R.id.prefs_newWallsHeightLabel);
			swapOut(this.newWallHeightSpinner, R.id.prefs_newWallsHeightSpinner);
		} else {
			removeView(R.id.prefs_newWallsHeightLabel);
			removeView(R.id.prefs_newWallsHeightSpinner);
		}
		if (this.newFloorThicknessLabel != null) {
			// Nineteenth row
			swapOut(this.newFloorThicknessLabel, R.id.prefs_newLevelFloorThicknessLabel);
			swapOut(this.newFloorThicknessSpinner, R.id.prefs_newLevelFloorThicknessSpinner);
		} else {
			removeView(R.id.prefs_newLevelFloorThicknessLabel);
			removeView(R.id.prefs_newLevelFloorThicknessSpinner);
		}
		/*if (this.checkUpdatesCheckBox != null
			|| this.autoSaveDelayForRecoveryCheckBox != null) {
      // Nineteenth row
			  JPanel updatesAndAutoSaveDelayForRecoveryPanel = new JPanel(new GridBagLayout());
			  if (this.checkUpdatesCheckBox != null) {
					updatesAndAutoSaveDelayForRecoveryPanel.add(this.checkUpdatesCheckBox,
					updatesAndAutoSaveDelayForRecoveryPanel.add(this.checkUpdatesNowButton,
		  }*/
		/*if (this.autoSaveDelayForRecoveryCheckBox != null) {
			swapOut(this.autoSaveDelayForRecoveryCheckBox, R.id.prefs_autoSaveRadioButton);
			swapOut(this.autoSaveDelayForRecoverySpinner, R.id.prefs_autoSaveSpinner);
			swapOut(this.autoSaveDelayForRecoveryUnitLabel, R.id.prefs_autoSaveUnitLabel);
		} else {
			removeView(R.id.prefs_autoSaveRadioButton);
			removeView(R.id.prefs_autoSaveSpinner);
			removeView(R.id.prefs_autoSaveUnitLabel);
			}*/

		// Last row
		if (this.resetDisplayedActionTipsButton.getText() != null
				&& this.resetDisplayedActionTipsButton.getText().length() > 0) {
			// Display reset button only if its text isn't empty
			swapOut(this.resetDisplayedActionTipsButton, R.id.prefs_resetTipsButton);
		} else {
			removeView(R.id.prefs_resetTipsButton);
		}

		swapOut(this.showPagerButtons, R.id.prefs_showPagerButtons);
		swapOut(this.showPlanZoomButtons, R.id.prefs_showPlanZoomButtons);
		swapOut(this.doubleTapEdit2D, R.id.prefs_doubleTapEdit2D);
		swapOut(this.doubleTapEdit3D, R.id.prefs_doubleTapEdit3D);

		this.setTitle(dialogTitle);
		swapOut(closeButton, R.id.prefs_closeButton);
	}

  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
	  this.setOnDismissListener(new OnDismissListener() {
		  @Override
		  public void onDismiss(DialogInterface dialog) {
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
