/*
 * Home3DAttributesPanel.java 25 juin 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSlider;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.Home3DAttributesController;

/**
 * Home 3D attributes editing panel.
 * @author Emmanuel Puybaret
 */
public class Home3DAttributesPanel extends AndroidDialogView implements DialogView {
  private final Home3DAttributesController controller;
  private JRadioButton  groundColorRadioButton;
  private ColorButton   groundColorButton;
  private JRadioButton  groundTextureRadioButton;
  private JButton groundTextureComponent;
  private JRadioButton  skyColorRadioButton;
  private ColorButton   skyColorButton;
  private JRadioButton skyTextureRadioButton;
  private JButton    skyTextureComponent;
  private JLabel brightnessLabel;
  private JSlider       brightnessSlider;
  private JLabel        darkBrightnessLabel;
  private JLabel        brightBrightnessLabel;
  private JLabel        wallsTransparencyLabel;
  private JLabel        opaqueWallsTransparencyLabel;
  private JLabel        invisibleWallsTransparencyLabel;
  private JSlider wallsTransparencySlider;
  private String        dialogTitle;

  /**
   * Creates a panel that displays home 3D attributes data.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public Home3DAttributesPanel(UserPreferences preferences,
                               Home3DAttributesController controller, Activity activity) {
    //super(new GridBagLayout());
	  super(preferences, activity);
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final Home3DAttributesController controller) {
    // Ground color and texture buttons bound to ground controller properties
    this.groundColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "groundColorRadioButton.text"));
    this.groundColorRadioButton.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
			if (groundColorRadioButton.isSelected()) {
				controller.setGroundPaint(Home3DAttributesController.EnvironmentPaint.COLORED);
			}
		}
	});
    controller.addPropertyChangeListener(Home3DAttributesController.Property.GROUND_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateGroundRadioButtons(controller);
          }
        });
  
    this.groundColorButton = new ColorButton(activity, preferences);
    this.groundColorButton.setColorDialogTitle(preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "groundColorDialog.title"));
    this.groundColorButton.setColor(controller.getGroundColor());
    this.groundColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setGroundColor(groundColorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(Home3DAttributesController.Property.GROUND_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            groundColorButton.setColor(controller.getGroundColor());
          }
        });

    this.groundTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "groundTextureRadioButton.text"));
    this.groundTextureRadioButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ev) {
        if (groundTextureRadioButton.isSelected()) {
          controller.setGroundPaint(Home3DAttributesController.EnvironmentPaint.TEXTURED);
        }
      }
    });
    
    this.groundTextureComponent = (JButton)controller.getGroundTextureController().getView();

    ButtonGroup groundGroup = new ButtonGroup();
    groundGroup.add(this.groundColorRadioButton);
    groundGroup.add(this.groundTextureRadioButton);
    updateGroundRadioButtons(controller);
    
    // Sky color and texture buttons bound to sky controller properties
    this.skyColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "skyColorRadioButton.text"));
    this.skyColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (skyColorRadioButton.isSelected()) {
            controller.setSkyPaint(Home3DAttributesController.EnvironmentPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(Home3DAttributesController.Property.SKY_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateSkyRadioButtons(controller);
          }
        });
  
    this.skyColorButton = new ColorButton(activity, preferences);
    this.skyColorButton.setColorDialogTitle(preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "skyColorDialog.title"));
    this.skyColorButton.setColor(controller.getSkyColor());
    this.skyColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setSkyColor(skyColorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(Home3DAttributesController.Property.SKY_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            skyColorButton.setColor(controller.getSkyColor());
          }
        });

    this.skyTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "skyTextureRadioButton.text"));
    this.skyTextureRadioButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ev) {
        if (skyTextureRadioButton.isSelected()) {
          controller.setSkyPaint(Home3DAttributesController.EnvironmentPaint.TEXTURED);
        }
      }
    });
    
    this.skyTextureComponent = (JButton)controller.getSkyTextureController().getView();

    ButtonGroup skyGroup = new ButtonGroup();
    skyGroup.add(this.skyColorRadioButton);
    skyGroup.add(this.skyTextureRadioButton);
    updateSkyRadioButtons(controller);
    
    // Brightness label and slider bound to LIGHT_COLOR controller property
    this.brightnessLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "brightnessLabel.text"));
    this.brightnessSlider = new JSlider(activity, 0, 255);
    this.darkBrightnessLabel = new JLabel(activity, preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "darkLabel.text"));
    this.brightBrightnessLabel = new JLabel(activity, preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "brightLabel.text"));
    this.brightnessSlider.setPaintTicks(true);
    this.brightnessSlider.setMajorTickSpacing(17);
    this.brightnessSlider.setValue(controller.getLightColor() & 0xFF);
    this.brightnessSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          int brightness = brightnessSlider.getValue();
          controller.setLightColor((brightness << 16) + (brightness << 8) + brightness);
        }
      });
    controller.addPropertyChangeListener(Home3DAttributesController.Property.LIGHT_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            brightnessSlider.setValue(controller.getLightColor() & 0xFF);
          }
        });
    
    // Walls transparency label and slider bound to WALLS_ALPHA controller property
    this.wallsTransparencyLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "wallsTransparencyLabel.text"));
    this.wallsTransparencySlider = new JSlider(activity, 0, 255);
    this.opaqueWallsTransparencyLabel = new JLabel(activity, preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "opaqueLabel.text"));
    this.invisibleWallsTransparencyLabel = new JLabel(activity, preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "invisibleLabel.text"));
    this.wallsTransparencySlider.setPaintTicks(true);
    this.wallsTransparencySlider.setMajorTickSpacing(17);
    this.wallsTransparencySlider.setValue((int)(controller.getWallsAlpha() * 255));
    this.wallsTransparencySlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setWallsAlpha(wallsTransparencySlider.getValue() / 255f);
        }
      });
    controller.addPropertyChangeListener(Home3DAttributesController.Property.WALLS_ALPHA, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            wallsTransparencySlider.setValue((int)(controller.getWallsAlpha() * 255));
          }
        });
    
    this.dialogTitle = preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "home3DAttributes.title");

  }

  /**
   * Updates ground radio buttons. 
   */
  private void updateGroundRadioButtons(Home3DAttributesController controller) {
    if (controller.getGroundPaint() == Home3DAttributesController.EnvironmentPaint.COLORED) {
      this.groundColorRadioButton.setSelected(true);
    } else {
      this.groundTextureRadioButton.setSelected(true);
    } 
  }

  /**
   * Updates sky radio buttons. 
   */
  private void updateSkyRadioButtons(Home3DAttributesController controller) {
    if (controller.getSkyPaint() == Home3DAttributesController.EnvironmentPaint.COLORED) {
      this.skyColorRadioButton.setSelected(true);
    } else {
      this.skyTextureRadioButton.setSelected(true);
    } 
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
/*    if (!OperatingSystem.isMacOSX()) {
      this.groundColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class,"groundColorRadioButton.mnemonic")).getKeyCode());
      this.groundTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class,"groundTextureRadioButton.mnemonic")).getKeyCode());
      this.skyColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class,"skyColorRadioButton.mnemonic")).getKeyCode());
      this.skyTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class,"skyTextureRadioButton.mnemonic")).getKeyCode());
      this.brightnessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class,"brightnessLabel.mnemonic")).getKeyCode());
      this.brightnessLabel.setLabelFor(this.brightnessSlider);
      this.wallsTransparencyLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class,"wallsTransparencyLabel.mnemonic")).getKeyCode());
      this.wallsTransparencyLabel.setLabelFor(this.wallsTransparencySlider);
    }*/
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {
	  JLabel groundPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "groundPanel.title"));
	  rootView.addView(groundPanel, labelInsets);

	  rootView.addView(this.groundColorRadioButton, labelInsets);
	rootView.addView(this.groundColorButton, labelInsets);
	rootView.addView(this.groundTextureRadioButton, labelInsets);
	rootView.addView(this.groundTextureComponent, labelInsets);

	  JLabel skyPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "skyPanel.title"));
	  rootView.addView(skyPanel, labelInsets);

	rootView.addView(this.skyColorRadioButton, labelInsets);

	rootView.addView(this.skyColorButton, labelInsets);
	  rootView.addView(this.skyTextureRadioButton, labelInsets);
	  rootView.addView(this.skyTextureComponent, labelInsets);

	  JLabel renderingPanel = new JLabel(activity, preferences.getLocalizedString(
			        com.eteks.sweethome3d.android_props.Home3DAttributesPanel.class, "renderingPanel.title"));
	  rootView.addView(renderingPanel, labelInsets);

	rootView.addView(this.brightnessLabel, labelInsets);

	  LinearLayout ll = new LinearLayout(activity);
	  ll.setOrientation(LinearLayout.HORIZONTAL);

	  ll.addView(this.darkBrightnessLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	  final float scale = activity.getResources().getDisplayMetrics().density;
	  int widthPx = (int) (200 * scale + 0.5f);
	  ll.addView(this.brightnessSlider, new LinearLayout.LayoutParams(widthPx, LinearLayout.LayoutParams.WRAP_CONTENT));

	  ll.addView(this.brightBrightnessLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	  rootView.addView(ll, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

	rootView.addView(this.wallsTransparencyLabel, labelInsets);

	  LinearLayout ll2 = new LinearLayout(activity);
	  ll2.setOrientation(LinearLayout.HORIZONTAL);

	  ll2.addView(this.opaqueWallsTransparencyLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	  ll2.addView(this.wallsTransparencySlider, new LinearLayout.LayoutParams(widthPx, LinearLayout.LayoutParams.WRAP_CONTENT));

	  ll2.addView(this.invisibleWallsTransparencyLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	  rootView.addView(ll2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

	  this.setTitle(dialogTitle);
	  rootView.addView(closeButton, labelInsets);
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
    //if (SwingTools.showConfirmDialog((JComponent)parentView,
    //        this, this.dialogTitle, this.wallsTransparencySlider) == JOptionPane.OK_OPTION
    //    && this.controller != null) {
   //   this.controller.modify3DAttributes();
   // }

	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modify3DAttributes();
		  }
	  });
	  this.show();
  }
}
