/*
 * HomeFurniturePanel.java 16 mai 07
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
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
import android.os.UserManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.ObjectProperty;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.View;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ButtonGroup;
import com.mindblowing.swingish.DefaultComboBoxModel;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JPanel;
import com.mindblowing.swingish.JRadioButton;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.JTextField;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.swingish.ChangeListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.mindblowing.renovations3d.R;
import org.jogamp.java3d.BranchGroup;

/**
 * Home furniture editing panel.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanel extends AndroidDialogView implements DialogView {
  private final HomeFurnitureController controller;
  private JLabel 				  nameLabel;
  private JTextField 			  nameTextField;
  private NullableCheckBox        nameVisibleCheckBox;
  private JLabel                  descriptionLabel;
  private JTextField              descriptionTextField;
  private JButton                 additionalPropertiesButton;
  private JLabel                  priceLabel;
  private JSpinner 				  priceSpinner;
  private JLabel                  valueAddedTaxPercentageLabel;
  private JSpinner                valueAddedTaxPercentageSpinner;
  private JLabel                  xLabel;
  private JSpinner 				  xSpinner;
  private JLabel                  yLabel;
  private JSpinner 				  ySpinner;
  private JLabel                  elevationLabel;
  private JSpinner 				  elevationSpinner;
  private NullableCheckBox        basePlanItemCheckBox;
  private JLabel                  angleLabel;
  private JSpinner 		  		  angleSpinner;
  private JRadioButton            rollRadioButton;
  private JSpinner			      rollSpinner;
  private JRadioButton            pitchRadioButton;
  private JSpinner         		  pitchSpinner;
  private JLabel                  widthLabel;
  private JSpinner 				  widthSpinner;
  private JLabel                  depthLabel;
  private JSpinner 				  depthSpinner;
  private JLabel                  heightLabel;
  private JSpinner 				  heightSpinner;
  private JCheckBox 		 	  keepProportionsCheckBox;
  private NullableCheckBox        mirroredModelCheckBox;
  private JButton                 modelTransformationsButton;
  private JRadioButton 			  defaultColorAndTextureRadioButton;
  private JRadioButton            colorRadioButton;
  private ColorButton             colorButton;
  private JRadioButton            textureRadioButton;
  private JButton 				  textureComponent;
  private JRadioButton            modelMaterialsRadioButton;
  private JButton              	  modelMaterialsComponent;
  private JRadioButton            defaultShininessRadioButton;
  private JRadioButton            mattRadioButton;
  private JRadioButton            shinyRadioButton;
  private NullableCheckBox        visibleCheckBox;
  private JLabel                  lightPowerLabel;
  private JSpinner 		  		  lightPowerSpinner;
  private String                  dialogTitle;

  /**
   * Creates a panel that displays home furniture data according to the units 
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public HomeFurniturePanel(UserPreferences preferences,
                            HomeFurnitureController controller, Activity activity) {
	  //super(new GridBagLayout());
	  super(preferences, activity, R.layout.dialog_homefurniturepanel);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(final UserPreferences preferences,
                                final HomeFurnitureController controller) {
	  // Get unit name matching current unit
	  String unitName = preferences.getLengthUnit().getName();

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.NAME)) {
		  // Create name label and its text field bound to NAME controller property
		  this.nameLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "nameLabel.text"));
		  this.nameTextField = new AutoCompleteTextField(activity, controller.getName(), 15, preferences.getAutoCompletionStrings("HomePieceOfFurnitureName"));
		  // if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
		  //   SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
		  // }
		  final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  nameTextField.setText(controller.getName());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
		  nameTextField.addTextChangedListener(new TextWatcher() {
			  public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			  }

			  public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			  }

			  public void afterTextChanged(Editable arg0) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
				  String name = nameTextField.getText().toString();
				  if (name == null || name.trim().length() == 0) {
					  controller.setName(null);
				  } else {
					  controller.setName(name);
				  }
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
			  }
		  });
/*
		this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
            String name = nameTextField.getText(); 
            if (name == null || name.trim().length() == 0) {
              controller.setName(null);
            } else {
              controller.setName(name);
            }
            controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
          }
    
          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
    
          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });*/
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.NAME_VISIBLE)) {
		  // Create name visible check box bound to NAME_VISIBLE controller property
		  this.nameVisibleCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "nameVisibleCheckBox.text"));
		  this.nameVisibleCheckBox.setNullable(controller.getNameVisible() == null);
		  this.nameVisibleCheckBox.setValue(controller.getNameVisible());
		  final PropertyChangeListener nameVisibleChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  nameVisibleCheckBox.setNullable(ev.getNewValue() == null);
				  nameVisibleCheckBox.setValue((Boolean) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
		  this.nameVisibleCheckBox.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
				  controller.setNameVisible(nameVisibleCheckBox.getValue());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.DESCRIPTION)) {
		  // Create description label and its text field bound to DESCRIPTION controller property
		  this.descriptionLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "descriptionLabel.text"));
		  this.descriptionTextField = new AutoCompleteTextField(activity, controller.getDescription(), 15, preferences.getAutoCompletionStrings("HomePieceOfFurnitureDescription"));
		  // if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
		  //   SwingTools.addAutoSelectionOnFocusGain(this.descriptionTextField);
		  // }
		  final PropertyChangeListener descriptionChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  descriptionTextField.setText(controller.getDescription());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
		  nameTextField.addTextChangedListener(new TextWatcher() {
			  public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			  }

			  public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			  }

			  public void afterTextChanged(Editable arg0) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
				  String description = descriptionTextField.getText().toString();
				  if (description == null || description.trim().length() == 0) {
					  controller.setDescription(null);
				  } else {
					  controller.setDescription(description);
				  }
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
			  }
		  });
 /*
 		this.descriptionTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
            String description = descriptionTextField.getText(); 
            if (description == null || description.trim().length() == 0) {
              controller.setDescription(null);
            } else {
              controller.setDescription(description);
            }
            controller.addPropertyChangeListener(HomeFurnitureController.Property.DESCRIPTION, descriptionChangeListener);
          }
    
          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
    
          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });*/
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.ADDITIONAL_PROPERTIES)) {
		  this.additionalPropertiesButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "additionalPropertiesButton.text"));
		  //if (OperatingSystem.isMacOSX()) {
			//  this.additionalPropertiesButton.putClientProperty("JButton.buttonType", "segmented");
			//  this.additionalPropertiesButton.putClientProperty("JButton.segmentPosition", "only");
		  //}
		  this.additionalPropertiesButton.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent ev) {
				  displayAdditionalPropertiesView(preferences, controller);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.PRICE)) {
		  // Create Price label and its spinner bound to PRICE controller property
		  this.priceLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "priceLabel.text"));
		  final NullableSpinnerNumberModel priceSpinnerModel =
				  new NullableSpinnerNumberModel(0, 0, 10000, 1f);
		  this.priceSpinner = new NullableSpinner(activity, priceSpinnerModel, true);
		  BigDecimal price = controller.getPrice();
      	  priceSpinnerModel.setNullable(true);
		  priceSpinnerModel.setValue(price);
		  final PropertyChangeListener priceChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  priceSpinnerModel.setNullable(ev.getNewValue() == null);
				  priceSpinnerModel.setValue(ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.PRICE, priceChangeListener);
		  priceSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.PRICE, priceChangeListener);
				  controller.setPrice(new BigDecimal(priceSpinnerModel.getNumber().doubleValue()));
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.PRICE, priceChangeListener);
			  }
		  });

      if (controller.isPropertyEditable(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE)) {
        this.valueAddedTaxPercentageLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
								com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "valueAddedTaxPercentageLabel.text"));
				final BigDecimal hundred = new BigDecimal("100");
        final NullableSpinnerNumberModel valueAddedTaxPercentageSpinnerModel = new NullableSpinnerNumberModel(
            0, 0, hundred.floatValue(), 0.5f);
        this.valueAddedTaxPercentageSpinner = new NullableSpinner(activity, valueAddedTaxPercentageSpinnerModel);
        BigDecimal valueAddedTaxPercentage = controller.getValueAddedTaxPercentage();
        valueAddedTaxPercentageSpinnerModel.setNullable(true);
        if (valueAddedTaxPercentage != null) {
          valueAddedTaxPercentageSpinnerModel.setValue(valueAddedTaxPercentage.multiply(hundred));
        } else {
          valueAddedTaxPercentageSpinnerModel.setValue(null);
        }
        final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              priceSpinnerModel.setNullable(ev.getNewValue() == null);
              if (ev.getNewValue() != null) {
                valueAddedTaxPercentageSpinnerModel.setValue(((BigDecimal)ev.getNewValue()).multiply(hundred));
              } else {
                valueAddedTaxPercentageSpinnerModel.setValue(null);
              }
            }
          };
        valueAddedTaxPercentageSpinnerModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
              // Remove listener on controller to avoid being recalled since actual value is divided by 100
              controller.removePropertyChangeListener(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE, propertyChangeListener);
              controller.setValueAddedTaxPercentage(valueAddedTaxPercentageSpinnerModel.getValue() != null
                  ? new BigDecimal(((Number)valueAddedTaxPercentageSpinnerModel.getValue()).doubleValue()/(hundred.doubleValue()))
                  : null);
              controller.addPropertyChangeListener(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE, propertyChangeListener);
            }
          });
        controller.addPropertyChangeListener(HomeFurnitureController.Property.VALUE_ADDED_TAX_PERCENTAGE,
            propertyChangeListener);
      }
	  }

	  final float maximumLength = preferences.getLengthUnit().getMaximumLength();

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.X)) {
		  // Create X label and its spinner bound to X controller property
		  this.xLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "xLabel.text", unitName));
		  final NullableSpinnerNumberModel.NullableSpinnerLengthModel xSpinnerModel =
				  new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
		  this.xSpinner = new NullableSpinner(activity, xSpinnerModel, true);
		  xSpinnerModel.setNullable(controller.getX() == null);
		  xSpinnerModel.setLength(controller.getX());
		  final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  xSpinnerModel.setNullable(ev.getNewValue() == null);
				  xSpinnerModel.setLength((Float) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
		  xSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
				  controller.setX(xSpinnerModel.getLength());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.Y)) {
		  // Create Y label and its spinner bound to Y controller property
		  this.yLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "yLabel.text",
				  unitName));
		  final NullableSpinnerNumberModel.NullableSpinnerLengthModel ySpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
				  preferences, -maximumLength, maximumLength);
		  this.ySpinner = new NullableSpinner(activity, ySpinnerModel, true);
		  ySpinnerModel.setNullable(controller.getY() == null);
		  ySpinnerModel.setLength(controller.getY());
		  final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  ySpinnerModel.setNullable(ev.getNewValue() == null);
				  ySpinnerModel.setLength((Float) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
		  ySpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
				  controller.setY(ySpinnerModel.getLength());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.ELEVATION)) {
		  // Create elevation label and its spinner bound to ELEVATION controller property
		  this.elevationLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "elevationLabel.text", unitName));
		  final NullableSpinnerNumberModel.NullableSpinnerLengthModel elevationSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
				  preferences, 0f, preferences.getLengthUnit().getMaximumElevation());
		  this.elevationSpinner = new NullableSpinner(activity, elevationSpinnerModel, true);
		  elevationSpinnerModel.setNullable(controller.getElevation() == null);
		  elevationSpinnerModel.setLength(controller.getElevation());
		  final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  elevationSpinnerModel.setNullable(ev.getNewValue() == null);
				  elevationSpinnerModel.setLength((Float) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.ELEVATION, elevationChangeListener);
		  elevationSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.ELEVATION, elevationChangeListener);
				  controller.setElevation(elevationSpinnerModel.getLength());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.ELEVATION, elevationChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.ANGLE_IN_DEGREES)
			  || controller.isPropertyEditable(HomeFurnitureController.Property.ANGLE)) {
		  // Create angle label and its spinner bound to ANGLE controller property
		  this.angleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "angleLabel.text"));
		  final NullableSpinnerNumberModel angleSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerModuloNumberModel(
				  0, 0, 360, 1);
		  this.angleSpinner = new NullableSpinner(activity, angleSpinnerModel, true);
		  Float angle = controller.getAngle();
		  angleSpinnerModel.setNullable(angle == null);
		  angleSpinnerModel.setValue(angle != null ? new Float((float) Math.toDegrees(angle)) : null);
		  final PropertyChangeListener angleChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  Float newAngle = (Float) ev.getNewValue();
				  angleSpinnerModel.setNullable(newAngle == null);
				  angleSpinnerModel.setValue(newAngle != null ? new Float((float) Math.toDegrees(newAngle)) : null);
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE, angleChangeListener);
		  angleSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.ANGLE,
						  angleChangeListener);
				  Number value = (Number) angleSpinnerModel.getValue();
				  if (value == null) {
					  controller.setAngle(null);
				  } else {
					  controller.setAngle((float) Math.toRadians(value.doubleValue()));
				  }
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE, angleChangeListener);
			  }
		  });
	  }

	  if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
		  if (controller.isPropertyEditable(HomeFurnitureController.Property.ROLL)) {
			  // Create roll label and its spinner bound to ROLL_IN_DEGREES controller property
			  this.rollRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
					  "rollRadioButton.text"));
			  this.rollRadioButton.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent ev) {
					  if (rollRadioButton.isSelected()) {
						  controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.ROLL);
					  }
				  }
			  });

			  final NullableSpinnerNumberModel rollSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerModuloNumberModel(
					  0, 0, 360, 1);
			  this.rollSpinner = new NullableSpinner(activity, rollSpinnerModel, true);
			  Float roll = controller.getRoll();
			  rollSpinnerModel.setNullable(roll == null);
			  rollSpinnerModel.setValue(roll != null ? new Float((float) Math.toDegrees(roll)) : null);
			  final PropertyChangeListener rollChangeListener = new PropertyChangeListener() {
				  public void propertyChange(PropertyChangeEvent ev) {
					  Float newRoll = (Float) ev.getNewValue();
					  rollSpinnerModel.setNullable(newRoll == null);
					  rollSpinnerModel.setValue(newRoll != null ? new Float((float) Math.toDegrees(newRoll)) : null);
				  }
			  };
			  controller.addPropertyChangeListener(HomeFurnitureController.Property.ROLL, rollChangeListener);
			  rollSpinnerModel.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent ev) {
					  controller.removePropertyChangeListener(HomeFurnitureController.Property.ROLL,
							  rollChangeListener);
					  Number value = (Number) rollSpinnerModel.getValue();
					  if (value == null) {
						  controller.setRoll(null);
					  } else {
						  controller.setRoll((float) Math.toRadians(value.floatValue()));
					  }
					  controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.ROLL);
					  controller.addPropertyChangeListener(HomeFurnitureController.Property.ROLL, rollChangeListener);
				  }
			  });
		  }

		  if (controller.isPropertyEditable(HomeFurnitureController.Property.PITCH)) {
			  // Create pitch label and its spinner bound to PITCH_IN_DEGREES controller property
			  this.pitchRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
					  "pitchRadioButton.text"));
			  this.pitchRadioButton.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent ev) {
					  if (pitchRadioButton.isSelected()) {
						  controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.PITCH);
					  }
				  }
			  });

			  final NullableSpinnerNumberModel pitchSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerModuloNumberModel(
					  0, 0, 360, 1);
			  this.pitchSpinner = new NullableSpinner(activity, pitchSpinnerModel, true);
			  Float pitch = controller.getPitch();
			  pitchSpinnerModel.setNullable(pitch == null);
			  pitchSpinnerModel.setValue(pitch != null ? new Float((float) Math.toDegrees(pitch)) : null);
			  final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
				  public void propertyChange(PropertyChangeEvent ev) {
					  Float newPitch = (Float) ev.getNewValue();
					  pitchSpinnerModel.setNullable(newPitch == null);
					  pitchSpinnerModel.setValue(newPitch != null ? new Float((float) Math.toDegrees(newPitch)) : null);
				  }
			  };
			  controller.addPropertyChangeListener(HomeFurnitureController.Property.PITCH, pitchChangeListener);
			  pitchSpinnerModel.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent ev) {
					  controller.removePropertyChangeListener(HomeFurnitureController.Property.PITCH,
							  pitchChangeListener);
					  Number value = (Number) pitchSpinnerModel.getValue();
					  if (value == null) {
						  controller.setPitch(null);
					  } else {
						  controller.setPitch((float) Math.toRadians(value.floatValue()));
					  }
					  controller.setHorizontalAxis(HomeFurnitureController.FurnitureHorizontalAxis.PITCH);
					  controller.addPropertyChangeListener(HomeFurnitureController.Property.PITCH, pitchChangeListener);
				  }
			  });
		  }

		  if (this.rollRadioButton != null && this.pitchRadioButton != null) {
			  ButtonGroup group = new ButtonGroup();
			  group.add(this.rollRadioButton);
			  group.add(this.pitchRadioButton);
			  updateHorizontalAxisRadioButtons(controller);
			  controller.addPropertyChangeListener(HomeFurnitureController.Property.HORIZONTAL_AXIS, new PropertyChangeListener() {
				  public void propertyChange(PropertyChangeEvent ev) {
					  updateHorizontalAxisRadioButtons(controller);
				  }
			  });
		  }
	  }


	  if (controller.isPropertyEditable(HomeFurnitureController.Property.BASE_PLAN_ITEM)) {
		  // Create base plan item check box bound to BASE_PLAN_ITEM controller property
		  this.basePlanItemCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "basePlanItemCheckBox.text"));
		  String basePlanItemToolTip = preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "basePlanItemCheckBox.tooltip");
		  if (basePlanItemToolTip.length() > 0) {
			  this.basePlanItemCheckBox.setToolTipText(basePlanItemToolTip);
		  }
		  this.basePlanItemCheckBox.setNullable(controller.getBasePlanItem() == null);
		  this.basePlanItemCheckBox.setValue(controller.getBasePlanItem());
		  final PropertyChangeListener basePlanItemModelChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  basePlanItemCheckBox.setNullable(ev.getNewValue() == null);
				  basePlanItemCheckBox.setValue((Boolean) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.BASE_PLAN_ITEM,
				  basePlanItemModelChangeListener);
		  this.basePlanItemCheckBox.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.BASE_PLAN_ITEM,
						  basePlanItemModelChangeListener);
				  controller.setBasePlanItem(basePlanItemCheckBox.getValue());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.BASE_PLAN_ITEM,
						  basePlanItemModelChangeListener);
			  }
		  });
		  this.basePlanItemCheckBox.setEnabled(controller.isBasePlanItemEnabled());
	  }

	  final float minimumLength = preferences.getLengthUnit().getMinimumLength();

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.WIDTH)) {
		  // Create width label and its spinner bound to WIDTH controller property
		  this.widthLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "widthLabel.text", unitName));
		  final NullableSpinnerNumberModel.NullableSpinnerLengthModel widthSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
				  preferences, minimumLength, maximumLength);
		  this.widthSpinner = new NullableSpinner(activity, widthSpinnerModel, true);
		  final PropertyChangeListener widthChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  Float width = controller.getWidth();
				  widthSpinnerModel.setNullable(width == null);
				  widthSpinnerModel.setLength(width);
				  if (width != null) {
					  widthSpinnerModel.setMinimumLength(Math.min(width, minimumLength));
				  }
			  }
		  };
		  widthChangeListener.propertyChange(null);
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
		  widthSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
				  controller.setWidth(widthSpinnerModel.getLength());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.DEPTH)) {
		  // Create depth label and its spinner bound to DEPTH controller property
		  this.depthLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "depthLabel.text", unitName));
		  final NullableSpinnerNumberModel.NullableSpinnerLengthModel depthSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
				  preferences, minimumLength, maximumLength);
		  this.depthSpinner = new NullableSpinner(activity, depthSpinnerModel, true);
		  final PropertyChangeListener depthChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  Float depth = controller.getDepth();
				  depthSpinnerModel.setNullable(depth == null);
				  depthSpinnerModel.setLength(depth);
				  if (depth != null) {
					  depthSpinnerModel.setMinimumLength(Math.min(depth, minimumLength));
				  }
			  }
		  };
		  depthChangeListener.propertyChange(null);
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
		  depthSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
				  controller.setDepth(depthSpinnerModel.getLength());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.HEIGHT)) {
		  // Create height label and its spinner bound to HEIGHT controller property
		  this.heightLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "heightLabel.text", unitName));
		  final NullableSpinnerNumberModel.NullableSpinnerLengthModel heightSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
				  preferences, minimumLength, maximumLength);
		  this.heightSpinner = new NullableSpinner(activity, heightSpinnerModel, true);
		  final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  Float height = controller.getHeight();
				  heightSpinnerModel.setNullable(height == null);
				  heightSpinnerModel.setLength(height);
				  if (height != null) {
					  heightSpinnerModel.setMinimumLength(Math.min(height, minimumLength));
				  }
			  }
		  };
		  heightChangeListener.propertyChange(null);
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
		  heightSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
				  controller.setHeight(heightSpinnerModel.getLength());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.PROPORTIONAL)) {
		  // Create keep proportions check box bound to PROPORTIONAL controller property
		  this.keepProportionsCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.ImportedFurnitureWizardStepsPanel.class, "keepProportionsCheckBox.text"));
		  this.keepProportionsCheckBox.addItemListener(new ItemListener() {
			  public void itemStateChanged(ItemEvent ev) {
				  controller.setProportional(keepProportionsCheckBox.isSelected());
			  }
		  });
		  this.keepProportionsCheckBox.setSelected(controller.isProportional()
				  // Force proportional if selection is rotated around horizontal axis when no 3D is available
				  || Boolean.getBoolean("com.eteks.sweethome3d.no3D")
				  && (controller.getRoll() == null
				  || controller.getRoll() != 0
				  || controller.getPitch() == null
				  || controller.getPitch() != 0));
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.PROPORTIONAL, new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  // If proportional property changes update keep proportions check box
				  keepProportionsCheckBox.setSelected(controller.isProportional());
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.MODEL_MIRRORED)) {
		  // Create mirror check box bound to MODEL_MIRRORED controller property
		  this.mirroredModelCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "mirroredModelCheckBox.text"));
		  String mirroredModelToolTip = preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "mirroredModelCheckBox.tooltip");
		  if (mirroredModelToolTip.length() > 0) {
			  this.mirroredModelCheckBox.setToolTipText(mirroredModelToolTip);
		  }
		  this.mirroredModelCheckBox.setNullable(controller.getModelMirrored() == null);
		  this.mirroredModelCheckBox.setValue(controller.getModelMirrored());
		  final PropertyChangeListener mirroredModelChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  mirroredModelCheckBox.setNullable(ev.getNewValue() == null);
				  mirroredModelCheckBox.setValue((Boolean) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED, mirroredModelChangeListener);
		  this.mirroredModelCheckBox.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED,
						  mirroredModelChangeListener);
				  controller.setModelMirrored(mirroredModelCheckBox.getValue());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED,
						  mirroredModelChangeListener);
			  }
		  });
	  }

    if (controller.isPropertyEditable(HomeFurnitureController.Property.MODEL_TRANSFORMATIONS)) {
      try {
        if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
          this.modelTransformationsButton = new JButton(activity, "");
          this.modelTransformationsButton.setEnabled(false);
          //if (OperatingSystem.isMacOSX()) {
          //  this.modelTransformationsButton.putClientProperty("JButton.buttonType", "segmented");
          //  this.modelTransformationsButton.putClientProperty("JButton.segmentPosition", "only");
          //}
          ModelMaterialsController modelMaterialsController = controller.getModelMaterialsController();
          if (modelMaterialsController != null && modelMaterialsController.getModel() != null) {
            ModelManager.getInstance().loadModel(modelMaterialsController.getModel(),
                new ModelManager.ModelObserver() {
                  public void modelUpdated(BranchGroup modelRoot) {
                    ModelManager modelManager = ModelManager.getInstance();
                    if (modelManager.containsDeformableNode(modelRoot)) {
                      // Make button visible only if model contains some deformable nodes
                      modelTransformationsButton.setText(SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
                          modelManager.containsNode(modelRoot, ModelManager.MANNEQUIN_ABDOMEN_PREFIX)
                              ? "mannequinTransformationsButton.text"
                              : "modelTransformationsButton.text"));
                      modelTransformationsButton.setEnabled(true);
                      modelTransformationsButton.addActionListener(new ActionListener() {
                          public void actionPerformed(ActionEvent ev) {
                            displayModelTransformationsView(preferences, controller);
                          }
                        });
                    }
                  }

                  public void modelError(Exception ex) {
                    // Ignore missing models
                  }
                });
          }
        }
      } catch (AccessControlException ex) {
        // com.eteks.sweethome3d.no3D property can't be read
      }
    }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.PAINT)) {
		  ButtonGroup buttonGroup = new ButtonGroup();
		  // Create radio buttons bound to COLOR and TEXTURE controller properties
		  this.defaultColorAndTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "defaultColorAndTextureRadioButton.text"));
		  buttonGroup.add(this.defaultColorAndTextureRadioButton);
		  this.defaultColorAndTextureRadioButton.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  if (defaultColorAndTextureRadioButton.isSelected()) {
					  controller.setPaint(HomeFurnitureController.FurniturePaint.DEFAULT);
				  }
			  }
		  });
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.PAINT, new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  updatePaintRadioButtons(controller);
			  }
		  });

		  this.colorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "colorRadioButton.text"));
		  buttonGroup.add(this.colorRadioButton);
		  this.colorRadioButton.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  if (colorRadioButton.isSelected()) {
					  controller.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
				  }
			  }
		  });

		  this.colorButton = new ColorButton(activity, preferences);
		  //if (OperatingSystem.isMacOSX()) {
		  //  this.colorButton.putClientProperty("JButton.buttonType", "segmented");
		  //  this.colorButton.putClientProperty("JButton.segmentPosition", "only");
		  //}
		  this.colorButton.setColorDialogTitle(preferences
				  .getLocalizedString(com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "colorDialog.title"));
		  this.colorButton.setColor(controller.getColor());
		  this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  controller.setColor(colorButton.getColor());
				  controller.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
			  }
		  });
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.COLOR, new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  colorButton.setColor(controller.getColor());
			  }
		  });

		  TextureChoiceController textureController = controller.getTextureController();
		  if (textureController != null) {
			  this.textureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
					  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "textureRadioButton.text"));
			  this.textureRadioButton.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent ev) {
					  if (textureRadioButton.isSelected()) {
						  controller.setPaint(HomeFurnitureController.FurniturePaint.TEXTURED);
					  }
				  }
			  });
			  this.textureComponent = (JButton) textureController.getView();
			  //if (OperatingSystem.isMacOSX()) {
			  //  this.textureComponent.putClientProperty("JButton.buttonType", "segmented");
			  //  this.textureComponent.putClientProperty("JButton.segmentPosition", "only");
			  //}
			  buttonGroup.add(this.textureRadioButton);
		  }

		  try {
			  ModelMaterialsController modelMaterialsController = controller.getModelMaterialsController();
			  if (modelMaterialsController != null
					  && !Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
				  this.modelMaterialsRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
						  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "modelMaterialsRadioButton.text"));
				  this.modelMaterialsRadioButton.addChangeListener(new ChangeListener() {
					  public void stateChanged(ChangeEvent ev) {
						  if (modelMaterialsRadioButton.isSelected()) {
							  controller.setPaint(HomeFurnitureController.FurniturePaint.MODEL_MATERIALS);
						  }
					  }
				  });
				  this.modelMaterialsComponent = (JButton)modelMaterialsController.getView();
				  //if (OperatingSystem.isMacOSX()) {
				  //  this.modelMaterialsComponent.putClientProperty("JButton.buttonType", "segmented");
				  //  this.modelMaterialsComponent.putClientProperty("JButton.segmentPosition", "only");
				  //}
				  buttonGroup.add(this.modelMaterialsRadioButton);
				  boolean uniqueModel = modelMaterialsController.getModel() != null;
				  this.modelMaterialsRadioButton.setEnabled(uniqueModel);
				  this.modelMaterialsComponent.setEnabled(uniqueModel);
			  }
		  } catch (AccessControlException ex) {
			  // com.eteks.sweethome3d.no3D property can't be read
		  }
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.SHININESS)) {
		  // Create radio buttons bound to SHININESS controller properties
		  this.defaultShininessRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "defaultShininessRadioButton.text"));
		  this.defaultShininessRadioButton.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  if (defaultShininessRadioButton.isSelected()) {
					  controller.setShininess(HomeFurnitureController.FurnitureShininess.DEFAULT);
				  }
			  }
		  });
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.SHININESS, new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  updateShininessRadioButtons(controller);
			  }
		  });
		  this.mattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "mattRadioButton.text"));
		  this.mattRadioButton.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  if (mattRadioButton.isSelected()) {
					  controller.setShininess(HomeFurnitureController.FurnitureShininess.MATT);
				  }
			  }
		  });
		  this.shinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "shinyRadioButton.text"));
		  this.shinyRadioButton.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  if (shinyRadioButton.isSelected()) {
					  controller.setShininess(HomeFurnitureController.FurnitureShininess.SHINY);
				  }
			  }
		  });
		  ButtonGroup buttonGroup = new ButtonGroup();
		  buttonGroup.add(this.defaultShininessRadioButton);
		  buttonGroup.add(this.mattRadioButton);
		  buttonGroup.add(this.shinyRadioButton);
		  updateShininessRadioButtons(controller);
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.PAINT)) {
		  updatePaintRadioButtons(controller);
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.VISIBLE)) {
		  // Create visible check box bound to VISIBLE controller property
		  this.visibleCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "visibleCheckBox.text"));
		  this.visibleCheckBox.setNullable(controller.getVisible() == null);
		  this.visibleCheckBox.setValue(controller.getVisible());
		  final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  visibleCheckBox.setNullable(ev.getNewValue() == null);
				  visibleCheckBox.setValue((Boolean) ev.getNewValue());
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
		  this.visibleCheckBox.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
				  controller.setVisible(visibleCheckBox.getValue());
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
			  }
		  });
	  }

	  if (controller.isPropertyEditable(HomeFurnitureController.Property.LIGHT_POWER)) {
		  // Create power label and its spinner bound to POWER controller property
		  this.lightPowerLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.HomeFurniturePanel.class,
				  "lightPowerLabel.text", unitName));
		  final NullableSpinnerNumberModel lightPowerSpinnerModel = new NullableSpinnerNumberModel(
				  0, 0, 100, 5);
		  this.lightPowerSpinner = new NullableSpinner(activity, lightPowerSpinnerModel, true);
		  lightPowerSpinnerModel.setNullable(controller.getLightPower() == null);
		  lightPowerSpinnerModel.setValue(controller.getLightPower() != null
				  ? Math.round(controller.getLightPower() * 100)
				  : null);
		  final PropertyChangeListener lightPowerChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  Float lightPower = (Float) ev.getNewValue();
				  lightPowerSpinnerModel.setNullable(lightPower == null);
				  lightPowerSpinnerModel.setValue(lightPower != null
						  ? Math.round((Float) ev.getNewValue() * 100)
						  : null);
			  }
		  };
		  controller.addPropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER, lightPowerChangeListener);
		  lightPowerSpinnerModel.addChangeListener(new ChangeListener() {
			  public void stateChanged(ChangeEvent ev) {
				  controller.removePropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER,
						  lightPowerChangeListener);
				  controller.setLightPower(((Number) lightPowerSpinnerModel.getValue()).floatValue() / 100f);
				  controller.addPropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER, lightPowerChangeListener);
			  }
		  });
	  }

	  updateSizeComponents(controller);
	  // Add a listener that enables / disables size fields depending on furniture resizable and deformable
	  PropertyChangeListener sizeListener = new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent ev) {
			  updateSizeComponents(controller);
		  }
	  };
	  controller.addPropertyChangeListener(HomeFurnitureController.Property.RESIZABLE, sizeListener);
	  controller.addPropertyChangeListener(HomeFurnitureController.Property.DEFORMABLE, sizeListener);

	  this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "homeFurniture.title");
  }

  /**
   * Updates roll and pitch radio buttons.
   */
  private void updateHorizontalAxisRadioButtons(HomeFurnitureController controller) {
    if (controller.getHorizontalAxis() == null) {
      SwingTools.deselectAllRadioButtons(this.rollRadioButton, this.pitchRadioButton);
    } else {
      switch (controller.getHorizontalAxis()) {
        case ROLL :
          this.rollRadioButton.setSelected(true);
          break;
        case PITCH :
          this.pitchRadioButton.setSelected(true);
          break;
      }
    }
  }
  
  /**
   * Updates color, texture and materials radio buttons. 
   */
  private void updatePaintRadioButtons(HomeFurnitureController controller) {
    if (controller.getPaint() == null) {
      SwingTools.deselectAllRadioButtons(this.defaultColorAndTextureRadioButton, 
          this.colorRadioButton, this.textureRadioButton, this.modelMaterialsRadioButton);
    } else {
      switch (controller.getPaint()) {
        case DEFAULT :
          this.defaultColorAndTextureRadioButton.setSelected(true);
          break;
        case COLORED :
          this.colorRadioButton.setSelected(true);
          break;
        case TEXTURED :
          this.textureRadioButton.setSelected(true);
          break;
        case MODEL_MATERIALS :
          if (this.modelMaterialsRadioButton != null) {
          	this.modelMaterialsRadioButton.setSelected(true);
          }
          break;
      } 
      updateShininessRadioButtons(controller);
    }
  }

  /**
   * Updates shininess radio buttons. 
   */
  private void updateShininessRadioButtons(HomeFurnitureController controller) {
    if (controller.isPropertyEditable(HomeFurnitureController.Property.SHININESS)) {
      if (controller.getShininess() == HomeFurnitureController.FurnitureShininess.DEFAULT) {
        this.defaultShininessRadioButton.setSelected(true);
      } else if (controller.getShininess() == HomeFurnitureController.FurnitureShininess.MATT) {
        this.mattRadioButton.setSelected(true);
      } else if (controller.getShininess() == HomeFurnitureController.FurnitureShininess.SHINY) {
        this.shinyRadioButton.setSelected(true);
      } else { // null
        SwingTools.deselectAllRadioButtons(this.defaultShininessRadioButton, this.mattRadioButton, this.shinyRadioButton);
      }
      boolean shininessEnabled = controller.getPaint() != HomeFurnitureController.FurniturePaint.MODEL_MATERIALS;
      this.defaultShininessRadioButton.setEnabled(shininessEnabled);
      this.mattRadioButton.setEnabled(shininessEnabled);
      this.shinyRadioButton.setEnabled(shininessEnabled);
      if (!shininessEnabled) {
        SwingTools.deselectAllRadioButtons(this.defaultShininessRadioButton, this.mattRadioButton, this.shinyRadioButton);
      }
    }
  }

  /**
   * Updates size components depending on the fact that furniture is resizable or not.
   */
  private void updateSizeComponents(final HomeFurnitureController controller) {
    boolean editableSize = controller.isResizable();
    this.widthLabel.setEnabled(editableSize);
    this.widthSpinner.setEnabled(editableSize);
    this.depthLabel.setEnabled(editableSize);
    this.depthSpinner.setEnabled(editableSize);
    this.heightLabel.setEnabled(editableSize);
    this.heightSpinner.setEnabled(editableSize);
    this.keepProportionsCheckBox.setEnabled(editableSize && controller.isDeformable()
        // Disable proportional if selection is rotated around horizontal axis when no 3D is available
        && (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")
            || controller.getRoll() != null
                && controller.getRoll() == 0
                && controller.getPitch() != null
                && controller.getPitch() == 0));
    this.mirroredModelCheckBox.setEnabled(editableSize);
  }

	/**
	 * Sets components mnemonics and label / component associations.
	 */
	private void setMnemonics(UserPreferences preferences) {
	}

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final HomeFurnitureController controller) {
	  // First row
	  boolean priceDisplayed = this.priceLabel != null;
	  boolean orientationPanelDisplayed = this.angleLabel != null
			  && (this.rollRadioButton != null || this.pitchRadioButton != null);

	  //JLabel namePanel = new JLabel(activity, preferences.getLocalizedString(
	//		  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, priceDisplayed ? "nameAndPricePanel.title" : "namePanel.title"));
	  //swapOut(namePanel, R.id.furniture_panel_namePanel);
		//PJ removed as pointless
	  removeView(R.id.furniture_panel_namePanel);

	  if (this.nameLabel != null) {
		  swapOut(this.nameLabel, R.id.furniture_panel_nameLabel);
		  swapOut(this.nameTextField, R.id.furniture_panel_nameTextField);
		  if (this.nameTextField.getText().toString() != null && this.nameTextField.getText().toString().length() > 0)
			  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  } else {
		  removeView(R.id.furniture_panel_nameLabel);
		  removeView(R.id.furniture_panel_nameTextField);
	  }
	  if (this.nameVisibleCheckBox != null) {
		  swapOut(this.nameVisibleCheckBox, R.id.furniture_panel_nameVisibleCheckBox);
	  } else {
		  removeView(R.id.furniture_panel_nameVisibleCheckBox);
	  }
	  if (this.descriptionLabel != null) {
		  swapOut(this.descriptionLabel, R.id.furniture_panel_descriptionLabel);
		  swapOut(this.descriptionTextField, R.id.furniture_panel_descriptionTextField);
	  } else {
		  removeView(R.id.furniture_panel_descriptionLabel);
		  removeView(R.id.furniture_panel_descriptionTextField);
	  }
	  if (priceDisplayed) {
		  swapOut(this.priceLabel, R.id.furniture_panel_priceLabel);
		  swapOut(this.priceSpinner, R.id.furniture_panel_priceSpinner);
			if (this.valueAddedTaxPercentageLabel != null) {
				swapOut(this.valueAddedTaxPercentageLabel, R.id.furniture_panel_valueAddedTaxPercentageLabel);
				swapOut(this.valueAddedTaxPercentageSpinner, R.id.furniture_panel_valueAddedTaxPercentageSpinner);
			} else {
				removeView(R.id.furniture_panel_valueAddedTaxPercentageLabel);
				removeView(R.id.furniture_panel_valueAddedTaxPercentageSpinner);
			}
	  } else {
		  removeView(R.id.furniture_panel_priceLabel);
		  removeView(R.id.furniture_panel_priceSpinner);
		  removeView(R.id.furniture_panel_valueAddedTaxPercentageLabel);
		  removeView(R.id.furniture_panel_valueAddedTaxPercentageSpinner);
	  }
	  // Location panel
	  JLabel locationPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "locationPanel.title"));
	  swapOut(locationPanel, R.id.furniture_panel_locationPanel);
	  if (this.xLabel != null) {
		  swapOut(this.xLabel, R.id.furniture_panel_xLabel);
		  swapOut(this.xSpinner, R.id.furniture_panel_xSpinner);
	  } else {
		  removeView(R.id.furniture_panel_xLabel);
		  removeView(R.id.furniture_panel_xSpinner);
	  }
	  if (this.yLabel != null) {
		  swapOut(this.yLabel, R.id.furniture_panel_yLabel);
		  swapOut(this.ySpinner, R.id.furniture_panel_ySpinner);
	  } else {
		  removeView(R.id.furniture_panel_yLabel);
		  removeView(R.id.furniture_panel_ySpinner);
	  }
	  if (this.elevationLabel != null) {
		  swapOut(this.elevationLabel, R.id.furniture_panel_elevationLabel);
		  swapOut(this.elevationSpinner, R.id.furniture_panel_elevationSpinner);
	  } else {
		  removeView(R.id.furniture_panel_elevationLabel);
		  removeView(R.id.furniture_panel_elevationSpinner);
	  }
	  if (this.angleLabel != null
			  && !orientationPanelDisplayed) {
		  swapOut(this.angleLabel, R.id.furniture_panel_angleLabel);
		  swapOut(this.angleSpinner, R.id.furniture_panel_angleSpinner);
	  } else if(!orientationPanelDisplayed) { // can't double remove
		  removeView(R.id.furniture_panel_angleLabel);
		  removeView(R.id.furniture_panel_angleSpinner);
	  }
		if (this.mirroredModelCheckBox != null) {
			swapOut(this.mirroredModelCheckBox, R.id.furniture_panel_mirroredModelCheckBox);
		} else {
			removeView(R.id.furniture_panel_mirroredModelCheckBox);
		}
	  if (this.basePlanItemCheckBox != null) {
		  swapOut(this.basePlanItemCheckBox, R.id.furniture_panel_basePlanItemCheckBox);
	  } else {
		  removeView(R.id.furniture_panel_basePlanItemCheckBox);
	  }
	  if (orientationPanelDisplayed) {
		  // Orientation panel
		  JLabel orientationPanel = new JLabel(activity, preferences.getLocalizedString(
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "orientationPanel.title"));
		  swapOut(orientationPanel, R.id.furniture_panel_orientationPanel);
		  JLabel verticalRotationLabel = new JLabel(activity, preferences.getLocalizedString(
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "verticalRotationLabel.text"));
		  JLabel horizontalRotationLabel = new JLabel(activity, preferences.getLocalizedString(
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "horizontalRotationLabel.text"));
		  //JLabel orientationLabel = new JLabel(activity, preferences.getLocalizedString(
			//	  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "orientationLabel.text"));
		  swapOut(verticalRotationLabel, R.id.furniture_panel_verticalRotationLabel);
		  swapOut(horizontalRotationLabel, R.id.furniture_panel_horizontalRotationLabel);
		 	// swapOut(orientationLabel, R.id.furniture_panel_orientationLabel);
		  // There are two possible layout depending whether horizontal and vertical rotation label are defined or not
		  //boolean layoutWithHorizontalVerticalLabels = verticalRotationLabel.getText().length() > 0
			//	  && horizontalRotationLabel.getText().length() > 0;
		  if (this.angleLabel != null) {
			  // Row 0 may contain verticalRotationLabel
			  swapOut(this.angleLabel, R.id.furniture_panel_angleLabel);
			  swapOut(this.angleSpinner, R.id.furniture_panel_angleSpinner);
		  } else {
			  removeView(R.id.furniture_panel_angleLabel);
			  removeView(R.id.furniture_panel_angleSpinner);
		  }
		  if (this.pitchRadioButton != null) {
			  // Row 2 may contain horizontalRotationLabel
			  swapOut(this.pitchRadioButton, R.id.furniture_panel_pitchRadioButton);
			  swapOut(this.pitchSpinner, R.id.furniture_panel_pitchSpinner);
		  } else {
			  removeView(R.id.furniture_panel_pitchRadioButton);
			  removeView(R.id.furniture_panel_pitchSpinner);
		  }
		  if (this.rollRadioButton != null) {
			  swapOut(this.rollRadioButton, R.id.furniture_panel_rollRadioButton);
			  swapOut(this.rollSpinner, R.id.furniture_panel_rollSpinner);
		  } else {
			  removeView(R.id.furniture_panel_rollRadioButton);
			  removeView(R.id.furniture_panel_rollSpinner);
		  }
		  if (this.rollRadioButton != null
				  && this.pitchRadioButton != null) {
		  	//left in to make code comparison better
		  }
	  } else {
		  //removeView(R.id.furniture_panel_orientationPanel);
		  removeView(R.id.furniture_panel_angleLabelSpacer);
		  removeView(R.id.furniture_panel_verticalRotationLabel);
		  removeView(R.id.furniture_panel_horizontalRotationLabel);
		  removeView(R.id.furniture_panel_pitchRadioButton);
		  removeView(R.id.furniture_panel_pitchSpinner);
		  removeView(R.id.furniture_panel_rollRadioButton);
		  removeView(R.id.furniture_panel_rollSpinner);
	  }

	  // Size panel
	  JLabel sizePanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "sizePanel.title"));
	  swapOut(sizePanel, R.id.furniture_panel_sizePanel);
	  if (this.widthLabel != null) {
		  swapOut(this.widthLabel, R.id.furniture_panel_widthLabel);
		  swapOut(this.widthSpinner, R.id.furniture_panel_widthSpinner);
	  } else {
		  removeView(R.id.furniture_panel_widthLabel);
		  removeView(R.id.furniture_panel_widthSpinner);
	  }
	  if (this.depthLabel != null) {
		  swapOut(this.depthLabel, R.id.furniture_panel_depthLabel);
		  swapOut(this.depthSpinner, R.id.furniture_panel_depthSpinner);
	  } else {
		  removeView(R.id.furniture_panel_depthLabel);
		  removeView(R.id.furniture_panel_depthSpinner);
	  }
	  if (this.heightLabel != null) {
		  swapOut(this.heightLabel, R.id.furniture_panel_heightLabel);
		  swapOut(this.heightSpinner, R.id.furniture_panel_heightSpinner);
	  } else {
		  removeView(R.id.furniture_panel_heightLabel);
		  removeView(R.id.furniture_panel_heightSpinner);
	  }
	  if (this.keepProportionsCheckBox != null) {
		  swapOut(this.keepProportionsCheckBox, R.id.furniture_panel_keepProportionsCheckBox);
	  } else {
		  removeView(R.id.furniture_panel_keepProportionsCheckBox);
	  }
	  if (this.modelTransformationsButton != null && this.modelTransformationsButton.isEnabled()) {
		  swapOut(this.modelTransformationsButton, R.id.furniture_panel_modelTransformationsButton);
	  } else {
		  removeView(R.id.furniture_panel_modelTransformationsButton);
	  }
	  JLabel paintPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "colorAndTexturePanel.title"));
	  if (this.defaultColorAndTextureRadioButton != null) {
		  /*int buttonPadY;
		  int buttonsBottomInset;
		  if (OperatingSystem.isMacOSXLeopardOrSuperior()
				  && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
			  // Ensure the top and bottom of segmented buttons are correctly drawn
			  buttonPadY = 4;
			  buttonsBottomInset = -4;
		  } else {
			  buttonPadY = 0;
			  buttonsBottomInset = 0;
		  }*/
		  // Color and Texture panel
		  swapOut(this.defaultColorAndTextureRadioButton, R.id.furniture_panel_defaultColorAndTextureRadioButton);
		  swapOut(this.colorRadioButton, R.id.furniture_panel_colorRadioButton);
		  swapOut(this.colorButton, R.id.furniture_panel_colorButton);
		  if (this.textureComponent != null) {
			  swapOut(this.textureRadioButton, R.id.furniture_panel_textureRadioButton);
			  swapOut(this.textureComponent, R.id.furniture_panel_textureButton);
		  } else {
			  removeView(R.id.furniture_panel_textureRadioButton);
			  removeView(R.id.furniture_panel_textureButton);
		  }
		  if (this.modelMaterialsComponent != null) {
			  swapOut(this.modelMaterialsRadioButton, R.id.furniture_panel_modelMaterialsRadioButton);
			  swapOut(this.modelMaterialsComponent, R.id.furniture_panel_modelMaterialsButton);
		  } else {
			  removeView(R.id.furniture_panel_modelMaterialsRadioButton);
			  removeView(R.id.furniture_panel_modelMaterialsButton);
		  }
		  swapOut(paintPanel, R.id.furniture_panel_paintPanel);

		  controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE,
				  new PropertyChangeListener() {
					  public void propertyChange(PropertyChangeEvent ev) {
						  //paintPanel.setVisible(controller.isTexturable());
						  defaultColorAndTextureRadioButton.setEnabled(controller.isTexturable());
						  colorRadioButton.setEnabled(controller.isTexturable());
						  colorButton.setEnabled(controller.isTexturable());
						  textureRadioButton.setEnabled(controller.isTexturable());
						  modelMaterialsRadioButton.setEnabled(controller.isTexturable());
					  }
				  });
		  //paintPanel.setVisible(controller.isTexturable());
		  this.defaultColorAndTextureRadioButton.setEnabled(controller.isTexturable());
		  this.colorRadioButton.setEnabled(controller.isTexturable());
		  this.colorButton.setEnabled(controller.isTexturable());
		  this.textureRadioButton.setEnabled(controller.isTexturable());
		  this.modelMaterialsRadioButton.setEnabled(controller.isTexturable());
	  } else {
		  removeView(R.id.furniture_panel_defaultColorAndTextureRadioButton);
		  removeView(R.id.furniture_panel_colorRadioButton);
		  removeView(R.id.furniture_panel_colorButton);
		  removeView(R.id.furniture_panel_textureRadioButton);
		  removeView(R.id.furniture_panel_textureButton);
		  removeView(R.id.furniture_panel_modelMaterialsRadioButton);
		  removeView(R.id.furniture_panel_modelMaterialsButton);
	  }
	  if (this.defaultShininessRadioButton != null) {
		  JLabel shininessPanel = new JLabel(activity, preferences.getLocalizedString(
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "shininessPanel.title"));
		  swapOut(shininessPanel, R.id.furniture_panel_shininessPanel);
		  swapOut(this.defaultShininessRadioButton, R.id.furniture_panel_defaultShininessRadioButton);
		  swapOut(this.mattRadioButton, R.id.furniture_panel_mattRadioButton);
		  swapOut(this.shinyRadioButton, R.id.furniture_panel_shinyRadioButton);

		  controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE,
				  new PropertyChangeListener() {
					  public void propertyChange(PropertyChangeEvent ev) {
						  //shininessPanel.setVisible(controller.isTexturable());
						  defaultShininessRadioButton.setEnabled(controller.isTexturable());
						  mattRadioButton.setEnabled(controller.isTexturable());
						  shinyRadioButton.setEnabled(controller.isTexturable());
					  }
				  });
		  //shininessPanel.setVisible(controller.isTexturable());
		  this.defaultShininessRadioButton.setEnabled(controller.isTexturable());
		  this.mattRadioButton.setEnabled(controller.isTexturable());
		  this.shinyRadioButton.setEnabled(controller.isTexturable());
	  } else {
		  removeView(R.id.furniture_panel_defaultShininessRadioButton);
		  removeView(R.id.furniture_panel_mattRadioButton);
		  removeView(R.id.furniture_panel_shinyRadioButton);
	  }
	  // Last row
	  if (this.visibleCheckBox != null) {
		  swapOut(this.visibleCheckBox, R.id.furniture_panel_visibleCheckBox);
	  } else {
		  removeView(R.id.furniture_panel_visibleCheckBox);
	  }
	  if (this.lightPowerLabel != null) {
		  swapOut(this.lightPowerLabel, R.id.furniture_panel_lightPowerLabel);
		  swapOut(this.lightPowerSpinner, R.id.furniture_panel_lightPowerSpinner);
	  } else {
		  // no empty tables, remove it
		  removeView(R.id.furniture_panel_lightPowerTable);
	  }

	  this.setTitle(dialogTitle);
	  swapOut(this.closeButton, R.id.furniture_panel_closeButton);
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
    /*if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyFurniture();
    }*/
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  this.setOnDismissListener(new OnDismissListener() {
		  @Override
		  public void onDismiss(DialogInterface dialog) {
			  controller.modifyFurniture();
		  }
	  });
	  this.show();
  }

	/**
	 * Displays a panel which lets the user modify the additional properties of the edited piece of furniture.
	 */
	private void displayAdditionalPropertiesView(UserPreferences preferences, HomeFurnitureController controller) {
//		new AdditionalPropertiesPanel(preferences, controller).displayView(this);
	}
//PJPJ TODO: need an example of additional properties to work from
	/**
	 * A panel which displays additional properties of the edited piece of furniture.
	 */
/*	private static class AdditionalPropertiesPanel extends JPanel implements View {
		private HomeFurnitureController controller;
		private JLabel                  additionalPropertiesLabel;
		private JTable                  additionalPropertiesTable;
		private String                  dialogTitle;

		public AdditionalPropertiesPanel(UserPreferences preferences,
										 HomeFurnitureController controller) {
			super(new GridBagLayout());
			this.controller = controller;
			createComponents(preferences, controller);
			layoutComponents();
		}

		/**
		 * Creates and initializes components.
		 */
/*		private void createComponents(final UserPreferences preferences,
									  final HomeFurnitureController controller) {
			this.additionalPropertiesLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
					com.eteks.sweethome3d.swing.AdditionalPropertiesPanel.class, "additionalPropertiesLabel.text"));

			final JTextField editorTextField = new JTextField();
			// Manage tab
			class TabAction extends AbstractAction {
				private int direction;

				TabAction(int direction) {
					this.direction = direction;
				}

				public void actionPerformed(ActionEvent ev) {
					int row = (additionalPropertiesTable.getEditingRow() + additionalPropertiesTable.getRowCount() + direction) % additionalPropertiesTable.getRowCount();
					for ( ; (!additionalPropertiesTable.isCellEditable(row, 1)
							|| ((ObjectProperty)additionalPropertiesTable.getValueAt(row, 0)).getType() == ObjectProperty.Type.CONTENT
							|| ((ObjectProperty)additionalPropertiesTable.getValueAt(row, 0)).getType() == ObjectProperty.Type.BOOLEAN)
							&& row != additionalPropertiesTable.getEditingRow();
						  row = (row + additionalPropertiesTable.getRowCount() + direction) % additionalPropertiesTable.getRowCount()) {
					}
					if (row != additionalPropertiesTable.getEditingRow()) {
						additionalPropertiesTable.setRowSelectionInterval(row, row);
						additionalPropertiesTable.scrollRectToVisible(additionalPropertiesTable.getCellRect(row, 1, true));
						additionalPropertiesTable.editCellAt(row, 1);
						additionalPropertiesTable.getEditorComponent().requestFocusInWindow();
					}
				}
			}
			Object tabId = UUID.randomUUID();
			editorTextField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed TAB"), tabId);
			editorTextField.getActionMap().put(tabId, new TabAction(1));
			Object shiftTabId = UUID.randomUUID();
			editorTextField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift pressed TAB"), shiftTabId);
			editorTextField.getActionMap().put(shiftTabId, new TabAction(-1));
			Object enterId = UUID.randomUUID();
			editorTextField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), enterId);
			editorTextField.getActionMap().put(enterId, new AbstractAction() {
				public void actionPerformed(ActionEvent ev) {
					JOptionPane optionPane = (JOptionPane)SwingUtilities.getAncestorOfClass(JOptionPane.class, editorTextField);
					if (optionPane != null) {
						optionPane.setValue(JOptionPane.OK_OPTION);
					}
				}
			});

			final JButton modifyContentEditorButton = new JButton(activity, SwingTools.getLocalizedLabelText(
					preferences, com.eteks.sweethome3d.swing.AdditionalPropertiesPanel.class, "modifyContentButton.text"));
			final JPanel modifyContentEditorPanel = new JPanel(new GridBagLayout());
			modifyContentEditorPanel.setBackground(UIManager.getColor("Table.background"));
			modifyContentEditorPanel.add(modifyContentEditorButton,new GridBagConstraints(
					0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			final DefaultCellEditor propertyCellEditor = new DefaultCellEditor(editorTextField) {
				private DocumentListener documentListener;
				private Object oldValue;

				@Override
				public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
					final ObjectProperty property = (ObjectProperty)table.getValueAt(row, 0);
					if (property.getType() == ObjectProperty.Type.CONTENT
							|| value instanceof Content) {
						// Manage button click
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								String image = controller.getContentManager().showOpenDialog(AdditionalPropertiesPanel.this,
										preferences.getLocalizedString(AdditionalPropertiesPanel.class, "selectContent.title"),
										ContentManager.ContentType.IMAGE);
								table.editingCanceled(null);
								if (image != null) {
									try {
										TemporaryURLContent imageContent = TemporaryURLContent.copyToTemporaryURLContent(new URLContent(new File(image).toURI().toURL()));
										table.setValueAt(imageContent, row, column);
									} catch (IOException ex) {
										ex.printStackTrace();
									}
								}
							}
						});
						return modifyContentEditorPanel;
					} else {
						this.oldValue = value;
						// Add a listener to commit the editor value while user types a text
						this.documentListener = new DocumentListener() {
							public void changedUpdate(final DocumentEvent ev) {
								String text = editorTextField.getText() != null && editorTextField.getText().length() > 0
										? editorTextField.getText() : null;
								if (text != null) {
									ParsePosition position = new ParsePosition(0);
									Object value = null;
									if (property.getType() != null) {
										text = text.trim();
										switch (property.getType()) {
											case LENGTH :
												value = preferences.getLengthUnit().getFormat().parseObject(text, position);
												break;
											case DATE :
												value = DateFormat.getDateInstance(DateFormat.SHORT).parse(text, position);
												break;
											case INTEGER :
												value = NumberFormat.getIntegerInstance().parse(text, position);
												break;
											case PRICE :
											case NUMBER :
												value = NumberFormat.getNumberInstance().parse(text, position);
												break;
											case PERCENTAGE :
												// Reformat space + % which may be different from a Java version / locale to the other
												NumberFormat percentFormat = NumberFormat.getPercentInstance();
												String percentSign = String.valueOf(new DecimalFormatSymbols(Locale.getDefault()).getPercent());
												String zeroPercent = percentFormat.format(0);
												String percentageSuffix = zeroPercent.substring(1);
												if (!text.endsWith(percentageSuffix)) {
													if (!text.endsWith(percentSign)) {
														text += percentageSuffix;
													} else {
														text = text.substring(0, text.length() - 1).trim() + percentageSuffix;
													}
												}
												value = percentFormat.parse(text, position);
												break;
											default :
												table.setValueAt(text, row, column);
												return;
										}
										if (position.getIndex() == text.length()) {
											table.setValueAt(value instanceof Date
													? new SimpleDateFormat("yyyy-MM-dd").format((Date)value)
													: value.toString(), row, column);
											editorTextField.setForeground(UIManager.getColor("FormattedTextField.foreground"));
										} else {
											editorTextField.setForeground(Color.RED);
										}
									} else {
										table.setValueAt(text, row, column);
									}
								}
							}

							public void removeUpdate(DocumentEvent ev) {
								changedUpdate(ev);
							}

							public void insertUpdate(DocumentEvent ev) {
								changedUpdate(ev);
							}
						};
						editorTextField.getDocument().addDocumentListener(this.documentListener);
						editorTextField.setForeground(UIManager.getColor("FormattedTextField.foreground"));
						value = ((JLabel)table.getCellRenderer(row, column).getTableCellRendererComponent(
								table, value, isSelected, isSelected, row, column)).getText();
						return super.getTableCellEditorComponent(table, value, isSelected, row, column);
					}
				}

				@Override
				public boolean stopCellEditing() {
					if (editorTextField.getForeground() == Color.RED) {
						editorTextField.setText((String)this.oldValue);
					}
					editorTextField.getDocument().removeDocumentListener(this.documentListener);
					return true;
				}

				@Override
				public void cancelCellEditing() {
					editorTextField.getDocument().removeDocumentListener(this.documentListener);
					super.cancelCellEditing();
				}
			};
			propertyCellEditor.setClickCountToStart(1);

			final PropertiesTableModel propertiesTableModel = new PropertiesTableModel(controller.getAdditionalProperties(), preferences);
			this.additionalPropertiesTable = new JTable(propertiesTableModel) {
				@Override
				public TableCellEditor getCellEditor(int row, int column) {
					ObjectProperty property = (ObjectProperty)propertiesTableModel.getValueAt(row, 0);
					if (property.getType() == ObjectProperty.Type.BOOLEAN) {
						return getDefaultEditor(Boolean.class);
					} else {
						return propertyCellEditor;
					}
				}
			};
			this.additionalPropertiesTable.getTableHeader().setReorderingAllowed(false);
			this.additionalPropertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			float resolutionScale = SwingTools.getResolutionScale();
			if (resolutionScale != 1) {
				// Adapt row height to specified resolution scale
				this.additionalPropertiesTable.setRowHeight(Math.round(this.additionalPropertiesTable.getRowHeight() * resolutionScale));
			}
			// Set column widths
			this.additionalPropertiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			TableColumnModel columnModel = this.additionalPropertiesTable.getColumnModel();
			int [] columnMinWidths = {100, 200};
			Font defaultFont = new DefaultTableCellRenderer().getFont();
			int charWidth;
			if (defaultFont != null) {
				charWidth = getFontMetrics(defaultFont).getWidths() ['A'];
			} else {
				charWidth = 10;
			}
			for (int i = 0; i < columnMinWidths.length; i++) {
				columnModel.getColumn(i).setPreferredWidth(columnMinWidths [i] * charWidth);
			}

			columnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table,
															   Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					JComponent label = (JComponent)super.getTableCellRendererComponent(
							table, ((ObjectProperty)value).getDisplayedName(), isSelected, hasFocus, row, column);
					label.setEnabled(table.isCellEditable(row, 1));
					return label;
				}
			});

			final TableCellRenderer textRenderer = this.additionalPropertiesTable.getDefaultRenderer(String.class);
			final TableCellRenderer booleanRenderer = this.additionalPropertiesTable.getDefaultRenderer(Boolean.class);
			final JButton modifyContentRendererButton = new JButton(activity, SwingTools.getLocalizedLabelText(
					preferences, com.eteks.sweethome3d.swing.AdditionalPropertiesPanel.class, "modifyContentButton.text"));
			modifyContentRendererButton.setPreferredSize(new Dimension(
					modifyContentRendererButton.getPreferredSize().width + this.additionalPropertiesTable.getRowHeight(), this.additionalPropertiesTable.getRowHeight() - 2));
			final JPanel modifyContentRendererPanel = new JPanel(new GridBagLayout());
			modifyContentRendererPanel.setBackground(UIManager.getColor("Table.background"));
			modifyContentRendererPanel.add(modifyContentRendererButton, new GridBagConstraints(
					0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			TableCellRenderer valueCellRenderer = new TableCellRenderer() {
				public Component getTableCellRendererComponent(JTable table,
															   Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					ObjectProperty property = (ObjectProperty)table.getValueAt(row, 0);
					if (property.getType() == ObjectProperty.Type.CONTENT
							|| value instanceof Content) {
						if (value != null) {
							modifyContentRendererButton.setIcon(IconManager.getInstance().getIcon((Content)value,
									additionalPropertiesTable.getRowHeight() - 4, additionalPropertiesTable));
						} else {
							modifyContentRendererButton.setIcon(null);
						}
						return modifyContentRendererPanel;
					} else if (property.getType() == ObjectProperty.Type.BOOLEAN) {
						return booleanRenderer.getTableCellRendererComponent(
								table, Boolean.valueOf((String)value), isSelected, hasFocus, row, column);
					} else if (property.getType() != null
							&& value != null) {
						try {
							switch (property.getType()) {
								case LENGTH :
									value = preferences.getLengthUnit().getFormat().format(Float.parseFloat((String)value));
									break;
								case DATE :
									value = DateFormat.getDateInstance(DateFormat.SHORT).format(
											new SimpleDateFormat("yyyy-MM-dd").parse((String)value));
									break;
								case PRICE :
								case NUMBER :
									value = NumberFormat.getNumberInstance().format(new BigDecimal((String)value));
									break;
								case PERCENTAGE :
									NumberFormat format = NumberFormat.getPercentInstance();
									format.setMaximumFractionDigits(2);
									value = format.format(Float.parseFloat((String)value));
									break;
							}
						} catch (ParseException ex) {
							// value unchanged
						} catch (NumberFormatException ex) {
							// value unchanged
						}
						return (JComponent)textRenderer.getTableCellRendererComponent(
								table, value, isSelected, hasFocus, row, column);
					} else {
						return (JComponent)textRenderer.getTableCellRendererComponent(
								table, value, isSelected, hasFocus, row, column);
					}
				}
			};
			columnModel.getColumn(1).setCellRenderer(valueCellRenderer);

			this.dialogTitle = preferences.getLocalizedString(AdditionalPropertiesPanel.class, "additionalProperties.title");
		}

		/**
		 * Layouts components in panel with their labels.
		 */
/*		private void layoutComponents() {
			int gap = Math.round(5 * SwingTools.getResolutionScale());
			add(this.additionalPropertiesLabel, new GridBagConstraints(
					0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, new Insets(0, 0, gap, gap), 0, 0));
			JScrollPane propertiesTableScrollPane = SwingTools.createScrollPane(this.additionalPropertiesTable);
			propertiesTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			propertiesTableScrollPane.setPreferredSize(new Dimension(
					Math.round(250 * SwingTools.getResolutionScale()),
					this.additionalPropertiesTable.getTableHeader().getPreferredSize().height + 6
							+ this.additionalPropertiesTable.getRowHeight() * Math.min(6, this.additionalPropertiesTable.getRowCount())));
			add(propertiesTableScrollPane, new GridBagConstraints(
					0, 11, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		/**
		 * Displays this panel in a modal dialog box.
		 */
/*		public void displayView(View parent) {
			JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parent);
			if (SwingTools.showConfirmDialog(parentComponent, this, this.dialogTitle, this.additionalPropertiesTable) == JOptionPane.OK_OPTION) {
				controller.setAdditionalProperties(((PropertiesTableModel)this.additionalPropertiesTable.getModel()).getAdditionalProperties());
			}
		}
	}

	/**
	 * Table model showing the name and value of additional properties.
	 */
/*	private static class PropertiesTableModel extends AbstractTableModel {
		private Map<ObjectProperty, Object> additionalProperties;
		private List<ObjectProperty>        keys;
		private String []                   columnNames;

		private PropertiesTableModel(Map<ObjectProperty, Object> additionalProperties,
									 UserPreferences preferences) {
			this.additionalProperties = new LinkedHashMap<ObjectProperty, Object>(additionalProperties);
			this.keys = new ArrayList<ObjectProperty>(additionalProperties.keySet());
			this.columnNames = new String [] {
					preferences.getLocalizedString(AdditionalPropertiesPanel.class, "additionalProperties.nameColumn"),
					preferences.getLocalizedString(AdditionalPropertiesPanel.class, "additionalProperties.valueColumn")};
		}

		public int getRowCount() {
			return this.additionalProperties.size();
		}

		public int getColumnCount() {
			return this.columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return this.columnNames [column];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ObjectProperty property = this.keys.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return property;
				case 1:
					return this.additionalProperties.get(property);
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			ObjectProperty property = this.keys.get(rowIndex);
			if (columnIndex == 1) {
				this.additionalProperties.put(property, value instanceof Boolean
						? String.valueOf(value) // BooleanEditor uses Boolean value
						: value);
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		public Map<ObjectProperty, Object> getAdditionalProperties() {
			return new LinkedHashMap<ObjectProperty, Object>(this.additionalProperties);
		}
	}*/

	/**
	 * Displays a panel which lets the user modify the transformations applied to the edited model.
	 */
	private void displayModelTransformationsView(UserPreferences preferences, HomeFurnitureController controller) {
		new ModelTransformationsPanel(preferences, controller, activity).displayView(this);
	}

	/**
	 * A panel that displays a preview of a model to let the user change transformations applied on it.
	 */
	private static class ModelTransformationsPanel extends AndroidDialogView implements DialogView {
		private HomeFurnitureController controller;
		private ModelPreviewComponent   previewComponent;
		private JLabel                  transformationsLabel;
		private JButton                 resetTransformationsButton;
		private JButton                 viewFromFrontButton;
		private JButton                 viewFromSideButton;
		private JButton                 viewFromTopButton;
		private JLabel                  presetTransformationsLabel;
    	private JComboBox 				presetTransformationsComboBox;
		private String                  dialogTitle;

		public ModelTransformationsPanel(UserPreferences preferences,
																		 HomeFurnitureController controller,
																		 Activity activity) {
			//super(new GridBagLayout());
			super(preferences, activity, R.layout.dialog_modeltransformationspanel);
			this.controller = controller;
			createComponents(preferences, controller);
			setMnemonics(preferences);
			layoutComponents();
		}

		/**
		 * Creates and initializes components.
		 */
		private void createComponents(final UserPreferences preferences,
																	final HomeFurnitureController controller) {
			ModelMaterialsController modelMaterialsController = controller.getModelMaterialsController();
			this.previewComponent = new ModelPreviewComponent(true, true, true, true, activity);
			this.previewComponent.setFocusable(false);
			float resolutionScale = SwingTools.getResolutionScale();
			//this.previewComponent.setPreferredSize(new Dimension((int)(400 * resolutionScale), (int)(400 * resolutionScale)));
			this.previewComponent.setModel(modelMaterialsController.getModel(), modelMaterialsController.isBackFaceShown(), modelMaterialsController.getModelRotation(),
							modelMaterialsController.getModelWidth(), modelMaterialsController.getModelDepth(), modelMaterialsController.getModelHeight());
			this.previewComponent.setModelMaterials(modelMaterialsController.getMaterials());
			this.previewComponent.setModelTransformations(controller.getModelTransformations());
			this.previewComponent.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent ev) {
					updateComponents(controller);
				}
			});

			String messageLessStyle = SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "transformationsLabel.text").replaceAll("<style([\\s\\S]+?)</style>", "");
			messageLessStyle = messageLessStyle.replace("<br>", " ");
			this.transformationsLabel = new JLabel(activity, Html.fromHtml(messageLessStyle, null, new JOptionPane.ListTagHandler()));

			this.resetTransformationsButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "resetTransformationsButton.text"));
			resetTransformationsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					previewComponent.resetModelTransformations();
					updateComponents(controller);
				}
			});
			updateComponents(controller);

			this.presetTransformationsLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "presetTransformationsLabel.text"));
			ArrayList<Object> presetTransformationsModelList = new ArrayList<Object>();
			presetTransformationsModelList.add(preferences.getLocalizedString(com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "presetTransformationsComboBox.chooseTransformations.text"));
			final List<String> modelPresetTransformationsNames = controller.getModelPresetTransformationsNames();
			for (int i = 0; i < modelPresetTransformationsNames.size(); i++) {
				// Store transformations index to allow duplicated names
				presetTransformationsModelList.add(new Integer(i));
			}
			DefaultComboBoxModel presetTransformationsModel = new DefaultComboBoxModel(presetTransformationsModelList);

			this.presetTransformationsComboBox = new JComboBox(activity, presetTransformationsModel);
			this.presetTransformationsComboBox.setAdapter(new ArrayAdapter<Object>(activity, android.R.layout.simple_list_item_1, presetTransformationsModelList.toArray()) {
				@Override
				public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
					return getDropDownView(position, convertView, parent);
				}

				@Override
				public android.view.View getDropDownView(int position, android.view.View convertView, ViewGroup parent) {
					TextView ret = new TextView(getContext());
					Object value = (Object) presetTransformationsComboBox.getItemAtPosition(position);
					String displayedValue = value instanceof Integer ? modelPresetTransformationsNames.get((Integer)value) : (String)value;
					ret.setText(displayedValue);
					return ret;
				}
			});
			this.presetTransformationsComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					if (presetTransformationsComboBox.getSelectedItemPosition() > 0) {
						Object value = presetTransformationsComboBox.getSelectedItem();
						if (value instanceof Integer) {
							previewComponent.setPresetModelTransformations(
									controller.getModelPresetTransformations((Integer)value));
							updateComponents(controller);
						}
					}
				}
			});
			this.presetTransformationsComboBox.setMaximumRowCount(Math.max(presetTransformationsModelList.size(), 10));

			this.viewFromFrontButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "viewFromFrontButton.text"));
			viewFromFrontButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					previewComponent.setViewYaw(0);
					previewComponent.setViewPitch(0);
				}
			});
			this.viewFromSideButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "viewFromSideButton.text"));
			viewFromSideButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					previewComponent.setViewYaw((float)(Math.PI / 2));
					previewComponent.setViewPitch(0);
				}
			});
			this.viewFromTopButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "viewFromTopButton.text"));
			viewFromTopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					previewComponent.setViewYaw(0);
					previewComponent.setViewPitch(-(float)(Math.PI / 2));
				}
			});

			this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.swing.ModelTransformationsPanel.class, "modelTransformations.title");
		}

		private void updateComponents(HomeFurnitureController controller) {
			this.resetTransformationsButton.setEnabled(this.previewComponent.getModelTransformations() != null);
		}

		/**
		 * Sets components mnemonics and label / component associations.
		 */
		private void setMnemonics(UserPreferences preferences) {
		}

		/**
		 * Layouts components in panel with their labels.
		 */
		private void layoutComponents() {
  		// Preview
			swapOut(this.transformationsLabel, R.id.modeltransformations_panel_transformationsLabel);
			swapOut(this.previewComponent, R.id.modeltransformations_panel_previewComponent);
			swapOut(this.resetTransformationsButton, R.id.modeltransformations_panel_resetTransformationsButton);
			swapOut(this.presetTransformationsLabel, R.id.modeltransformations_panel_presetTransformationsLabel);
			swapOut(this.presetTransformationsComboBox, R.id.modeltransformations_panel_presetTransformationsComboBox);
			swapOut(this.viewFromFrontButton, R.id.modeltransformations_panel_viewFromFrontButton);
			swapOut(this.viewFromSideButton, R.id.modeltransformations_panel_viewFromSideButton);
			swapOut(this.viewFromTopButton, R.id.modeltransformations_panel_viewFromTopButton);

			this.setTitle(dialogTitle);
			swapOut(this.closeButton, R.id.modeltransformations_panel_closeButton);
		}

		private void updateLocationAndSize() {
      		float modelX = this.controller.getModelMirrored()
          		? -this.previewComponent.getModelX()
          		: this.previewComponent.getModelX();
			float modelY = this.previewComponent.getModelY();
			float pieceX = (float)(this.controller.getX()
							+ modelX * Math.cos(this.controller.getAngle()) - modelY * Math.sin(this.controller.getAngle()));
			float pieceY = (float)(this.controller.getY()
							+ modelX * Math.sin(this.controller.getAngle()) + modelY * Math.cos(this.controller.getAngle()));
			float pieceElevation = this.controller.getElevation()
							+ this.previewComponent.getModelElevation() + this.controller.getHeight() / 2;
			Transformation[] modelTransformations = this.previewComponent.getModelTransformations();
			this.controller.setModelTransformations(modelTransformations != null ? modelTransformations : new Transformation [0],
							pieceX, pieceY, pieceElevation,
							this.previewComponent.getModelWidth(),
							this.previewComponent.getModelDepth(),
							this.previewComponent.getModelHeight());
		}

		/**
		 * Displays this panel in a modal dialog box.
		 */
		public void displayView(View parent) {
			//JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parent);
			//if (SwingTools.showConfirmDialog(parentComponent, this, this.dialogTitle, null) == JOptionPane.OK_OPTION) {
			//	updateLocationAndSize();
			//}
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			this.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					updateLocationAndSize();
				}
			});
			this.show();
		}
  }
}
