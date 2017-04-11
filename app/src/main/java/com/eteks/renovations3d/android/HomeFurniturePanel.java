/*
 * HomeFurniturePanel.java 16 mai 07
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.security.AccessControlException;

import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.ItemListener;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JCheckBox;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSpinnerJogDial;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.renovations3d.android.swingish.JTextField;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;

import com.mindblowing.renovations3d.R;

/**
 * Home furniture editing panel.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanel extends AndroidDialogView implements DialogView {
  private final HomeFurnitureController controller;
  private JLabel nameLabel;
  private JTextField nameTextField;
  private JLabel                  descriptionLabel;
  private JTextField              descriptionTextField;
  private NullableCheckBox nameVisibleCheckBox;
  private JLabel                  priceLabel;
  private JSpinner priceSpinner;
  private JLabel                  xLabel;
  private JSpinner xSpinner;
  private JLabel                  yLabel;
  private JSpinner ySpinner;
  private JLabel                  elevationLabel;
  private JSpinner elevationSpinner;
  private JLabel                  angleLabel;
  private JSpinnerJogDial angleSpinner;
  private NullableCheckBox        basePlanItemCheckBox;
  private JLabel                  widthLabel;
  private JSpinner widthSpinner;
  private JLabel                  depthLabel;
  private JSpinner depthSpinner;
  private JLabel                  heightLabel;
  private JSpinner heightSpinner;
  private JCheckBox keepProportionsCheckBox;
  private NullableCheckBox        mirroredModelCheckBox;
  private JRadioButton defaultColorAndTextureRadioButton;
  private JRadioButton            colorRadioButton;
  private ColorButton             colorButton;
  private JRadioButton            textureRadioButton;
  private JButton textureComponent;
  private JRadioButton            modelMaterialsRadioButton;
  private JButton              modelMaterialsComponent;
  private JRadioButton            defaultShininessRadioButton;
  private JRadioButton            mattRadioButton;
  private JRadioButton            shinyRadioButton;
  private NullableCheckBox        visibleCheckBox;
  private JLabel                  lightPowerLabel;
  private JSpinnerJogDial lightPowerSpinner;
  private String                  dialogTitle;

  /**
   * Creates a panel that displays home furniture data according to the units 
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public HomeFurniturePanel(UserPreferences preferences,
                            HomeFurnitureController controller,
							Activity activity) {
	  //super(new GridBagLayout());
	  super(preferences, activity, R.layout.dialog_homefurniturepanel);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final HomeFurnitureController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    if (controller.isPropertyEditable(HomeFurnitureController.Property.NAME)) {
      // Create name label and its text field bound to NAME controller property
      this.nameLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "nameLabel.text"));
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

		nameTextField.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
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
/*      this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
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
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "nameVisibleCheckBox.text"));
      this.nameVisibleCheckBox.setNullable(controller.getNameVisible() == null);
      this.nameVisibleCheckBox.setValue(controller.getNameVisible());
      final PropertyChangeListener nameVisibleChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameVisibleCheckBox.setNullable(ev.getNewValue() == null);
            nameVisibleCheckBox.setValue((Boolean)ev.getNewValue());
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
      this.descriptionLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "descriptionLabel.text"));
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

		nameTextField.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
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
 /*     this.descriptionTextField.getDocument().addDocumentListener(new DocumentListener() {
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
        
    if (controller.isPropertyEditable(HomeFurnitureController.Property.PRICE)) {
      // Create Price label and its spinner bound to PRICE controller property
      this.priceLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "priceLabel.text"));
      final NullableSpinnerNumberModel priceSpinnerModel =
          new NullableSpinnerNumberModel(0, 0, 10000, 1f);
      this.priceSpinner = new NullableSpinner(activity, priceSpinnerModel);
      BigDecimal price = controller.getPrice();
      priceSpinnerModel.setNullable(price == null);
      priceSpinnerModel.setValue(price == null  ? null  : price.floatValue());
      final PropertyChangeListener priceChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            priceSpinnerModel.setNullable(ev.getNewValue() == null);
            priceSpinnerModel.setValue(((Number)ev.getNewValue()).floatValue());
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
    }

    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    
    if (controller.isPropertyEditable(HomeFurnitureController.Property.X)) {
      // Create X label and its spinner bound to X controller property
      this.xLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "xLabel.text", unitName));
      final NullableSpinnerNumberModel.NullableSpinnerLengthModel xSpinnerModel =
          new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
      this.xSpinner = new NullableSpinner(activity, xSpinnerModel);
      xSpinnerModel.setNullable(controller.getX() == null);
      xSpinnerModel.setLength(controller.getX());
      final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            xSpinnerModel.setNullable(ev.getNewValue() == null);
            xSpinnerModel.setLength((Float)ev.getNewValue());
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
      this.yLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "yLabel.text",
          unitName));
      final NullableSpinnerNumberModel.NullableSpinnerLengthModel ySpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
          preferences, -maximumLength, maximumLength);
      this.ySpinner = new NullableSpinner(activity, ySpinnerModel);
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
      this.elevationLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "elevationLabel.text", unitName));
      final NullableSpinnerNumberModel.NullableSpinnerLengthModel elevationSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
          preferences, 0f, preferences.getLengthUnit().getMaximumElevation());
      this.elevationSpinner = new NullableSpinner(activity, elevationSpinnerModel);
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
    
    if (controller.isPropertyEditable(HomeFurnitureController.Property.ANGLE_IN_DEGREES)) {
      // Create angle label and its spinner bound to ANGLE_IN_DEGREES controller property
      this.angleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "angleLabel.text"));
      final NullableSpinnerNumberModel angleSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerModuloNumberModel(
          0, 0, 360, 1);
      this.angleSpinner = new NullableSpinnerJogDial(activity, angleSpinnerModel);
      Integer angle = controller.getAngleInDegrees();
      angleSpinnerModel.setNullable(angle == null);
      angleSpinnerModel.setValue(angle);
      final PropertyChangeListener angleChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Integer newAngle = (Integer)ev.getNewValue();
            angleSpinnerModel.setNullable(newAngle == null);
            angleSpinnerModel.setValue(newAngle);
          }
        };
      controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE_IN_DEGREES, angleChangeListener);
      angleSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(HomeFurnitureController.Property.ANGLE_IN_DEGREES,
                angleChangeListener);
            Number value = (Number)angleSpinnerModel.getValue();
            if (value == null) {
              controller.setAngleInDegrees(null);
            } else {
              controller.setAngleInDegrees(value.intValue());
            }
            controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE_IN_DEGREES, angleChangeListener);
          }
        });
    }
    
    if (controller.isPropertyEditable(HomeFurnitureController.Property.BASE_PLAN_ITEM)) {
      // Create base plan item check box bound to BASE_PLAN_ITEM controller property
      this.basePlanItemCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "basePlanItemCheckBox.text"));
      String basePlanItemToolTip = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
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
      this.widthLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "widthLabel.text", unitName));
      final NullableSpinnerNumberModel.NullableSpinnerLengthModel widthSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
          preferences, minimumLength, maximumLength);
      this.widthSpinner = new NullableSpinner(activity, widthSpinnerModel);
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
      this.depthLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "depthLabel.text", unitName));
      final NullableSpinnerNumberModel.NullableSpinnerLengthModel depthSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
          preferences, minimumLength, maximumLength);
      this.depthSpinner = new NullableSpinner(activity, depthSpinnerModel);
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
      this.heightLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "heightLabel.text", unitName));
      final NullableSpinnerNumberModel.NullableSpinnerLengthModel heightSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
          preferences, minimumLength, maximumLength);
      this.heightSpinner = new NullableSpinner(activity, heightSpinnerModel);
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
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "keepProportionsCheckBox.text"));
      this.keepProportionsCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setProportional(keepProportionsCheckBox.isSelected());
        }
      });
      this.keepProportionsCheckBox.setSelected(controller.isProportional());
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
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "mirroredModelCheckBox.text"));
      String mirroredModelToolTip = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
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
      controller
          .addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED, mirroredModelChangeListener);
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
    
    if (controller.isPropertyEditable(HomeFurnitureController.Property.PAINT)) {
      ButtonGroup buttonGroup = new ButtonGroup();
      // Create radio buttons bound to COLOR and TEXTURE controller properties
      this.defaultColorAndTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "defaultColorAndTextureRadioButton.text"));
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
      
      this.colorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
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
          .getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "colorDialog.title"));
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
            com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "textureRadioButton.text"));
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
              com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "modelMaterialsRadioButton.text"));
          this.modelMaterialsRadioButton.addChangeListener(new ChangeListener() {
              public void stateChanged(ChangeEvent ev) {
                if (modelMaterialsRadioButton.isSelected()) {
                  controller.setPaint(HomeFurnitureController.FurniturePaint.MODEL_MATERIALS);
                }
              }
            });
          //this.modelMaterialsComponent = (JButton)modelMaterialsController.getView();
          //if (OperatingSystem.isMacOSX()) {
          //  this.modelMaterialsComponent.putClientProperty("JButton.buttonType", "segmented");
          //  this.modelMaterialsComponent.putClientProperty("JButton.segmentPosition", "only");
          //}
          buttonGroup.add(this.modelMaterialsRadioButton);
          boolean uniqueModel = modelMaterialsController.getModel() != null;
          this.modelMaterialsRadioButton.setEnabled(uniqueModel);
          //this.modelMaterialsComponent.setEnabled(uniqueModel);
        }
      } catch (AccessControlException ex) {
        // com.eteks.sweethome3d.no3D property can't be read
      }
    }
    
    if (controller.isPropertyEditable(HomeFurnitureController.Property.SHININESS)) {
      // Create radio buttons bound to SHININESS controller properties
      this.defaultShininessRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "defaultShininessRadioButton.text"));
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
      this.mattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "mattRadioButton.text"));
      this.mattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (mattRadioButton.isSelected()) {
              controller.setShininess(HomeFurnitureController.FurnitureShininess.MATT);
            }
          }
        });
      this.shinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
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
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "visibleCheckBox.text"));
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
      this.lightPowerLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomeFurniturePanel.class,
          "lightPowerLabel.text", unitName));
      final NullableSpinnerNumberModel lightPowerSpinnerModel = new NullableSpinnerNumberModel(
          0, 0, 100, 5);
      this.lightPowerSpinner = new NullableSpinnerJogDial(activity, lightPowerSpinnerModel);
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
            controller
                .addPropertyChangeListener(HomeFurnitureController.Property.LIGHT_POWER, lightPowerChangeListener);
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
    
    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "homeFurniture.title");

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
          this.modelMaterialsRadioButton.setSelected(true);
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
    this.keepProportionsCheckBox.setEnabled(editableSize && controller.isDeformable());
    this.mirroredModelCheckBox.setEnabled(editableSize);
  }
  



  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final HomeFurnitureController controller) {
	  boolean priceDisplayed = this.priceLabel != null;

	  JLabel namePanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, priceDisplayed ? "nameAndPricePanel.title" : "namePanel.title"));
	  swapOut(namePanel, R.id.furniture_panel_namePanel);

	  if (this.nameLabel != null)
	  {
		  swapOut(this.nameLabel, R.id.furniture_panel_nameLabel);
		  swapOut(this.nameTextField, R.id.furniture_panel_nameTextField);
		  if (this.nameTextField.getText().toString() != null && this.nameTextField.getText().toString().length() > 0)
			  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_nameLabel);
		  removeView(R.id.furniture_panel_nameTextField);
	  }
	  if (this.nameVisibleCheckBox != null)
	  {
		  swapOut(this.nameVisibleCheckBox, R.id.furniture_panel_nameVisibleCheckBox);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_nameVisibleCheckBox);
	  }
	  if (this.descriptionLabel != null)
	  {
		  swapOut(this.descriptionLabel, R.id.furniture_panel_descriptionLabel);
		  swapOut(this.descriptionTextField, R.id.furniture_panel_descriptionTextField);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_descriptionLabel);
		  removeView(R.id.furniture_panel_descriptionTextField);
	  }
	  if (priceDisplayed)
	  {
		  swapOut(this.priceLabel, R.id.furniture_panel_priceLabel);
		  swapOut(this.priceSpinner, R.id.furniture_panel_priceSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_priceLabel);
		  removeView(R.id.furniture_panel_priceSpinner);
	  }

	  JLabel locationPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "locationPanel.title"));
	  swapOut(locationPanel, R.id.furniture_panel_locationPanel);
	  if (this.xLabel != null)
	  {
		  swapOut(this.xLabel, R.id.furniture_panel_xLabel);
		  swapOut(this.xSpinner, R.id.furniture_panel_xSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_xLabel);
		  removeView(R.id.furniture_panel_xSpinner);
	  }

	  if (this.yLabel != null)
	  {
		  swapOut(this.yLabel, R.id.furniture_panel_yLabel);
		  swapOut(this.ySpinner, R.id.furniture_panel_ySpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_yLabel);
		  removeView(R.id.furniture_panel_ySpinner);
	  }

	  if (this.elevationLabel != null)
	  {
		  swapOut(this.elevationLabel, R.id.furniture_panel_elevationLabel);
		  swapOut(this.elevationSpinner, R.id.furniture_panel_elevationSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_elevationLabel);
		  removeView(R.id.furniture_panel_elevationSpinner);
	  }

	  if (this.angleLabel != null)
	  {
		  swapOut(this.angleLabel, R.id.furniture_panel_angleLabel);
		  swapOut(this.angleSpinner, R.id.furniture_panel_angleSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_angleLabel);
		  removeView(R.id.furniture_panel_angleSpinner);
	  }

	  if (this.basePlanItemCheckBox != null)
	  {
		  swapOut(this.basePlanItemCheckBox, R.id.furniture_panel_basePlanItemCheckBox);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_basePlanItemCheckBox);
	  }

	  JLabel sizePanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "sizePanel.title"));
	  swapOut(sizePanel, R.id.furniture_panel_sizePanel);
	  if (this.widthLabel != null)
	  {
		  swapOut(this.widthLabel, R.id.furniture_panel_widthLabel);
		  swapOut(this.widthSpinner, R.id.furniture_panel_widthSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_widthLabel);
		  removeView(R.id.furniture_panel_widthSpinner);
	  }

	  if (this.depthLabel != null)
	  {
		  swapOut(this.depthLabel, R.id.furniture_panel_depthLabel);
		  swapOut(this.depthSpinner, R.id.furniture_panel_depthSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_depthLabel);
		  removeView(R.id.furniture_panel_depthSpinner);
	  }

	  if (this.heightLabel != null)
	  {
		  swapOut(this.heightLabel, R.id.furniture_panel_heightLabel);
		  swapOut(this.heightSpinner, R.id.furniture_panel_heightSpinner);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_heightLabel);
		  removeView(R.id.furniture_panel_heightSpinner);
	  }

	  if (this.keepProportionsCheckBox != null)
	  {
		  swapOut(this.keepProportionsCheckBox, R.id.furniture_panel_keepProportionsCheckBox);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_keepProportionsCheckBox);
	  }

	  if (this.mirroredModelCheckBox != null)
	  {
		  swapOut(this.mirroredModelCheckBox, R.id.furniture_panel_mirroredModelCheckBox);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_mirroredModelCheckBox);
	  }

	  JLabel paintPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "colorAndTexturePanel.title"));
	  swapOut(paintPanel, R.id.furniture_panel_paintPanel);
	  if (this.defaultColorAndTextureRadioButton != null)
	  {
		  swapOut(this.defaultColorAndTextureRadioButton, R.id.furniture_panel_defaultColorAndTextureRadioButton);
		  swapOut(this.colorRadioButton, R.id.furniture_panel_colorRadioButton);
		  swapOut(this.colorButton, R.id.furniture_panel_colorButton);
		  if (this.textureComponent != null)
		  {
			  swapOut(this.textureRadioButton, R.id.furniture_panel_textureRadioButton);
			  swapOut(this.textureComponent, R.id.furniture_panel_textureButton);
		  }
		  else
		  {
			  removeView(R.id.furniture_panel_textureRadioButton);
			  removeView(R.id.furniture_panel_textureButton);
		  }

		  if (this.modelMaterialsComponent != null)
		  {
			  swapOut(this.modelMaterialsRadioButton, R.id.furniture_panel_modelMaterialsRadioButton);
			  //TODO:  swapOut(this.modelMaterialsComponent, R.id.furniture_panel_modelMaterialsButton);
		  }
		  else
		  {
			  removeView(R.id.furniture_panel_modelMaterialsRadioButton);
			  //TODO:  removeView(R.id.furniture_panel_modelMaterialsButton);
		  }

		  controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE,
				  new PropertyChangeListener()
				  {
					  public void propertyChange(PropertyChangeEvent ev)
					  {

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
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_defaultColorAndTextureRadioButton);
		  removeView(R.id.furniture_panel_colorRadioButton);
		  removeView(R.id.furniture_panel_colorButton);
		  removeView(R.id.furniture_panel_textureRadioButton);
		  removeView(R.id.furniture_panel_textureButton);
		  removeView(R.id.furniture_panel_modelMaterialsRadioButton);
		  //TODO:  removeView(R.id.furniture_panel_modelMaterialsButton);
	  }

	  if (this.defaultShininessRadioButton != null)
	  {
		  JLabel shininessPanel = new JLabel(activity, preferences.getLocalizedString(
				  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "shininessPanel.title"));
		  swapOut(shininessPanel, R.id.furniture_panel_shininessPanel);
		  swapOut(this.defaultShininessRadioButton, R.id.furniture_panel_defaultShininessRadioButton);
		  swapOut(this.mattRadioButton, R.id.furniture_panel_mattRadioButton);
		  swapOut(this.shinyRadioButton, R.id.furniture_panel_shinyRadioButton);

		  controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE,
				  new PropertyChangeListener()
				  {
					  public void propertyChange(PropertyChangeEvent ev)
					  {
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
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_defaultShininessRadioButton);
		  removeView(R.id.furniture_panel_mattRadioButton);
		  removeView(R.id.furniture_panel_shinyRadioButton);
	  }

	  if (this.visibleCheckBox != null)
	  {
		  swapOut(this.visibleCheckBox, R.id.furniture_panel_visibleCheckBox);
	  }
	  else
	  {
		  removeView(R.id.furniture_panel_visibleCheckBox);
	  }

	  if (this.lightPowerLabel != null)
	  {
		  swapOut(this.lightPowerLabel, R.id.furniture_panel_lightPowerLabel);
		  swapOut(this.lightPowerSpinner, R.id.furniture_panel_lightPowerSpinner);
	  }
	  else
	  {
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
	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modifyFurniture();
		  }
	  });
	  this.show();
  }
}
