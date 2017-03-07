/*
 * BaseboardChoiceComponent.java 24 mai 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.renovations3d.R;

/**
 * Baseboard editing panel.
 * @author Emmanuel Puybaret
 */
public class BaseboardChoiceComponent extends LinearLayout implements View {
  private NullableCheckBox      visibleCheckBox;
  private JRadioButton sameColorAsWallRadioButton;
  private JRadioButton          colorRadioButton;
  private ColorButton           colorButton;
  private JRadioButton          textureRadioButton;
  private JButton textureComponent;
  private JLabel heightLabel;
  private JSpinner heightSpinner;
  private JLabel                thicknessLabel;
  private JSpinner              thicknessSpinner;


	private final UserPreferences preferences;
	private Activity activity;

	protected ViewGroup rootView;

  /**
   * Creates a panel that displays baseboard data.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public BaseboardChoiceComponent(UserPreferences preferences,
                                  BaseboardChoiceController controller,
								  final Activity activity) {
	  super(activity);
	  this.activity = activity;
	  this.preferences = preferences;

	  rootView = (ViewGroup)activity.getLayoutInflater().inflate(R.layout.baseboardview, null);
	  this.addView(rootView);

    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);



  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final BaseboardChoiceController controller) {
    // Baseboard visible check box bound to VISIBLE controller property
    this.visibleCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "visibleCheckBox.text"));
    this.visibleCheckBox.setNullable(controller.getVisible() == null);
    this.visibleCheckBox.setValue(controller.getVisible());
    this.visibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setVisible(visibleCheckBox.getValue());
        }
      });
    PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Boolean visible = controller.getVisible();
          visibleCheckBox.setValue(visible);
          boolean componentsEnabled = visible != Boolean.FALSE;
          sameColorAsWallRadioButton.setEnabled(componentsEnabled);
          colorRadioButton.setEnabled(componentsEnabled);
          textureRadioButton.setEnabled(componentsEnabled);
          colorButton.setEnabled(componentsEnabled);
          ((JButton)controller.getTextureController().getView()).setEnabled(componentsEnabled);
          heightSpinner.setEnabled(componentsEnabled);
          thicknessSpinner.setEnabled(componentsEnabled);
        }
      };
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.VISIBLE, 
        visibleChangeListener);

    this.sameColorAsWallRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "sameColorAsWallRadioButton.text"));
    this.sameColorAsWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (sameColorAsWallRadioButton.isSelected()) {
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.DEFAULT);
          }
        }
      });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateColorRadioButtons(controller);
          }
        });

    // Baseboard color and texture buttons bound to baseboard controller properties
    this.colorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "colorRadioButton.text"));
    this.colorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (colorRadioButton.isSelected()) {
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateColorRadioButtons(controller);
          }
        });
    
    this.colorButton = new ColorButton(activity, preferences);
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });

    this.textureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "textureRadioButton.text"));
    this.textureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (textureRadioButton.isSelected()) {
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.TEXTURED);
          }
        }
      });
    
    this.textureComponent = (JButton)controller.getTextureController().getView();

    ButtonGroup colorButtonGroup = new ButtonGroup();
    colorButtonGroup.add(this.colorRadioButton);
    colorButtonGroup.add(this.textureRadioButton);
    colorButtonGroup.add(this.sameColorAsWallRadioButton);
    updateColorRadioButtons(controller);    

    // Create baseboard height label and its spinner bound to HEIGHT controller property
    String unitName = preferences.getLengthUnit().getName();
    float minimumLength = preferences.getLengthUnit().getMinimumLength();
    this.heightLabel = new JLabel(activity,SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "heightLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 
            controller.getMaxHeight() == null
                ? preferences.getLengthUnit().getMaximumLength() / 10
                : controller.getMaxHeight());
    this.heightSpinner = new NullableSpinner(activity, heightSpinnerModel);
    heightSpinnerModel.setNullable(controller.getHeight() == null);
    final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          heightSpinnerModel.setNullable(ev.getNewValue() == null);
          heightSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.HEIGHT, 
        heightChangeListener);
    if (controller.getHeight() != null && controller.getMaxHeight() != null) {
      heightSpinnerModel.setLength(Math.min(controller.getHeight(), controller.getMaxHeight()));
    } else {
      heightSpinnerModel.setLength(controller.getHeight());
    }
    heightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(BaseboardChoiceController.Property.HEIGHT, 
              heightChangeListener);
          controller.setHeight(heightSpinnerModel.getLength());
          controller.addPropertyChangeListener(BaseboardChoiceController.Property.HEIGHT, 
              heightChangeListener);
        }
      });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.MAX_HEIGHT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getOldValue() == null
                || controller.getMaxHeight() != null
                   && ((Number)heightSpinnerModel.getMaximum()).floatValue() < controller.getMaxHeight()) {
              // Change max only if larger value to avoid taking into account intermediate max values  
              // that may be fired by auto commit spinners while entering a value
              heightSpinnerModel.setMaximum(controller.getMaxHeight());
            }
          }
        });
    
    // Create baseboard thickness label and its spinner bound to THICKNESS controller property
    this.thicknessLabel = new JLabel(activity,SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.BaseboardChoiceComponent.class, "thicknessLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel thicknessSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 2);
    this.thicknessSpinner = new NullableSpinner(activity, thicknessSpinnerModel);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    final PropertyChangeListener thicknessChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          thicknessSpinnerModel.setNullable(ev.getNewValue() == null);
          thicknessSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.THICKNESS, 
        thicknessChangeListener);
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(BaseboardChoiceController.Property.THICKNESS, 
              thicknessChangeListener);
          controller.setThickness(thicknessSpinnerModel.getLength());
          controller.addPropertyChangeListener(BaseboardChoiceController.Property.THICKNESS, 
              thicknessChangeListener);
        }
      });
    
    visibleChangeListener.propertyChange(null);
  }

  /**
   * Updates baseboard color radio buttons. 
   */
  private void updateColorRadioButtons(BaseboardChoiceController controller) {
    if (controller.getPaint() == BaseboardChoiceController.BaseboardPaint.COLORED) {
      this.colorRadioButton.setSelected(true);
    } else if (controller.getPaint() == BaseboardChoiceController.BaseboardPaint.TEXTURED) {
      this.textureRadioButton.setSelected(true);
    } else if (controller.getPaint() == BaseboardChoiceController.BaseboardPaint.DEFAULT) {
      this.sameColorAsWallRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.colorRadioButton, this.textureRadioButton, 
          this.sameColorAsWallRadioButton);
    }
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {
  	  LinearLayout.LayoutParams rowInsets;
	  rowInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  Resources r = activity.getResources();
	  int px10dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
	  rowInsets.setMargins(px10dp,px10dp,px10dp,px10dp);

	  swapOut(this.visibleCheckBox, R.id.baseboardview_visibleCheckBox);
	  swapOut(this.sameColorAsWallRadioButton, R.id.baseboardview_sameColorAsWallRadioButton);
	  swapOut(this.colorRadioButton, R.id.baseboardview_colorRadioButton);
	  swapOut(this.colorButton, R.id.baseboardview_colorButton);
	  swapOut(this.textureRadioButton, R.id.baseboardview_textureRadioButton);
	  swapOut(this.textureComponent, R.id.baseboardview_textureComponent);
	  swapOut(this.heightLabel, R.id.baseboardview_heightLabel);
	  swapOut(this.heightSpinner, R.id.baseboardview_heightSpinner);
	  swapOut(this.thicknessLabel, R.id.baseboardview_thicknessLabel);
	  swapOut(this.thicknessSpinner, R.id.baseboardview_thicknessSpinner);
  }

	protected void swapOut(android.view.View newView, int placeHolderId)
	{
		android.view.View placeHolder = rootView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		AndroidDialogView.replaceView(placeHolder, newView);
	}
}
