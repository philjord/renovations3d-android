/*
 * ObserverCameraPanel.java 09 mars 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.view.WindowManager;

import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JSpinnerJogDial;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.SpinnerNumberModel;
import com.eteks.renovations3d.android.utils.AndroidDialogView;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.ObserverCameraController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Observer camera editing panel.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class ObserverCameraPanel extends AndroidDialogView implements DialogView {
  private final ObserverCameraController controller;
  private JLabel 					xLabel;
  private JSpinner 				xSpinner;
  private JLabel        	yLabel;
  private JSpinner 				ySpinner;
  private JLabel        	elevationLabel;
  private JSpinner 				elevationSpinner;
  private JLabel        	yawLabel;
  private JSpinnerJogDial yawSpinner;
  private JLabel        	pitchLabel;
  private JSpinnerJogDial pitchSpinner;
  private JLabel        	fieldOfViewLabel;
  private JSpinnerJogDial fieldOfViewSpinner;
  private JCheckBox 			adjustObserverCameraElevationCheckBox;
  private String        	dialogTitle;

  /**
   * Creates a panel that displays observer camera attributes data according to the units 
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public ObserverCameraPanel(UserPreferences preferences,
                             ObserverCameraController controller, Activity activity) {
	  super(preferences, activity, R.layout.dialog_observercamera);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents(preferences);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final ObserverCameraController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    // Create X label and its spinner bound to X controller property
    this.xLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "xLabel.text", unitName));
    final float maximumLength = 5E5f;
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel xSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xSpinner = new NullableSpinner(activity, xSpinnerModel, true);
    xSpinnerModel.setLength(controller.getX());
    final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(ObserverCameraController.Property.X, xChangeListener);
    xSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setX(xSpinnerModel.getLength());
        }
      });
    
    // Create Y label and its spinner bound to Y controller property
    this.yLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "yLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel ySpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.ySpinner = new NullableSpinner(activity, ySpinnerModel, true);
    ySpinnerModel.setLength(controller.getY());
    final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ySpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(ObserverCameraController.Property.Y, yChangeListener);
    ySpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setY(ySpinnerModel.getLength());
        }
      });

    // Create camera elevation label and spinner bound to ELEVATION controller property
    this.elevationLabel = new JLabel(activity, String.format(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "elevationLabel.text"), unitName));
    float maximumElevation = preferences.getLengthUnit().getMaximumElevation();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel elevationSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, controller.getMinimumElevation(), maximumElevation);
    this.elevationSpinner = new NullableSpinner(activity, elevationSpinnerModel, true);
    elevationSpinnerModel.setLength(controller.getElevation());
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setElevation(elevationSpinnerModel.getLength());
        }
      });
    PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationSpinnerModel.setLength(controller.getElevation());
        }
      };
    controller.addPropertyChangeListener(ObserverCameraController.Property.ELEVATION, elevationChangeListener);
    
    // Create yaw label and spinner bound to YAW_IN_DEGREES controller property
    this.yawLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "yawLabel.text"));
    final SpinnerNumberModel yawSpinnerModel = new SpinnerNumberModel(0, -360, 720, 5);
    this.yawSpinner = new AutoCommitSpinnerJogDial(activity, yawSpinnerModel);
    yawSpinnerModel.setValue(controller.getYawInDegrees() % 360);//PJPJP added % 360
    yawSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setYawInDegrees(((Number)yawSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.YAW_IN_DEGREES,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            yawSpinnerModel.setValue(controller.getYawInDegrees() % 360);
          }
        });
    
    // Create pitch label and spinner bound to SPIN_IN_DEGREES controller property
    this.pitchLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "pitchLabel.text"));
    final SpinnerNumberModel pitchSpinnerModel = new SpinnerNumberModel(0, -90, 90, 5);
    this.pitchSpinner = new AutoCommitSpinnerJogDial(activity, pitchSpinnerModel);
    pitchSpinnerModel.setValue(controller.getPitchInDegrees());
    pitchSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setPitchInDegrees(((Number)pitchSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.PITCH_IN_DEGREES, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            pitchSpinnerModel.setValue(controller.getPitchInDegrees());
          }
        });
    
    // Create field of view label and spinner bound to FIELD_OF_VIEW_IN_DEGREES controller property
    this.fieldOfViewLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "fieldOfViewLabel.text"));
    final SpinnerNumberModel fieldOfViewSpinnerModel = new SpinnerNumberModel(10, 10, 120, 1);
    this.fieldOfViewSpinner = new AutoCommitSpinnerJogDial(activity, fieldOfViewSpinnerModel);
    fieldOfViewSpinnerModel.setValue(controller.getFieldOfViewInDegrees());
    fieldOfViewSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setFieldOfViewInDegrees(((Number)fieldOfViewSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.FIELD_OF_VIEW_IN_DEGREES, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            fieldOfViewSpinnerModel.setValue(controller.getFieldOfViewInDegrees());
          }
        });
    
    this.adjustObserverCameraElevationCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "adjustObserverCameraElevationCheckBox.text"), controller.isElevationAdjusted());
    this.adjustObserverCameraElevationCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setElevationAdjusted(adjustObserverCameraElevationCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.OBSERVER_CAMERA_ELEVATION_ADJUSTED,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            adjustObserverCameraElevationCheckBox.setSelected(controller.isElevationAdjusted());
          }
        });

    controller.addPropertyChangeListener(ObserverCameraController.Property.MINIMUM_ELEVATION, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (elevationSpinnerModel.getLength() != null) {
              elevationSpinnerModel.setLength(Math.max(elevationSpinnerModel.getLength(), controller.getMinimumElevation()));
            }
            elevationSpinnerModel.setMinimum(controller.getMinimumElevation());
          }
        });

    this.dialogTitle = preferences.getLocalizedString(
			com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "observerCamera.title");
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
	  JLabel rendering3DPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "locationPanel.title"));
	  swapOut(rendering3DPanel, R.id.observercamera_rendering3DPanel);

	  swapOut(this.xLabel, R.id.observercamera_xLabel);
	  swapOut(this.xSpinner, R.id.observercamera_xSpinner);
	  swapOut(this.yLabel, R.id.observercamera_yLabel);
	  swapOut(this.ySpinner, R.id.observercamera_ySpinner);
	  swapOut(this.elevationLabel, R.id.observercamera_elevationLabel);
	  swapOut(this.elevationSpinner, R.id.observercamera_elevationSpinner);

	  JLabel anglesPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.swing.ObserverCameraPanel.class, "anglesPanel.title"));
	  swapOut(anglesPanel, R.id.observercamera_anglesPanel);

	  swapOut(this.yawLabel, R.id.observercamera_yawLabel);
	  swapOut(this.yawSpinner, R.id.observercamera_yawSpinner);
	  swapOut(this.pitchLabel, R.id.observercamera_pitchLabel);
	  swapOut(this.pitchSpinner, R.id.observercamera_pitchSpinner);
	  swapOut(this.fieldOfViewLabel, R.id.observercamera_fieldOfViewLabel);
	  swapOut(this.fieldOfViewSpinner, R.id.observercamera_fieldOfViewSpinner);

	  if (controller.isObserverCameraElevationAdjustedEditable()) {
		  swapOut(this.adjustObserverCameraElevationCheckBox, R.id.observercamera_adjustObserverCameraElevationCheckBox);
	  } else {
		  removeView(R.id.observercamera_adjustObserverCameraElevationCheckBox);
	  }

	  this.setTitle(dialogTitle);
	  swapOut(closeButton, R.id.observercamera_closeButton);
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
   /* JFormattedTextField elevationSpinnerTextField =
        ((JSpinner.DefaultEditor)this.elevationSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView, this, this.dialogTitle,
            elevationSpinnerTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyObserverCamera();
    }*/
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  this.setOnDismissListener(new OnDismissListener() {
		  @Override
		  public void onDismiss(DialogInterface dialog) {
			  if (controller != null) {
				  controller.modifyObserverCamera();
			  }
		  }
	  });
	  this.show();
  }
}
