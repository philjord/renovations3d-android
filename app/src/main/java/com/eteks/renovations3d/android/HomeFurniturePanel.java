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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.security.AccessControlException;


import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JCheckBox;
import com.eteks.renovations3d.android.swingish.JComponent;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.renovations3d.android.swingish.JTextField;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.android.utils.ChangeListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.VCView;

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
  private JSpinner                xSpinner;
  private JLabel                  yLabel;
  private JSpinner                ySpinner;
  private JLabel                  elevationLabel;
  private JSpinner                elevationSpinner;
  private JLabel                  angleLabel;
  private JSpinner                angleSpinner;
  private NullableCheckBox        basePlanItemCheckBox;
  private JLabel                  widthLabel;
  private JSpinner                widthSpinner;
  private JLabel                  depthLabel;
  private JSpinner                depthSpinner;
  private JLabel                  heightLabel;
  private JSpinner                heightSpinner;
  private JCheckBox keepProportionsCheckBox;
  private NullableCheckBox        mirroredModelCheckBox;
  private JRadioButton defaultColorAndTextureRadioButton;
  private JRadioButton            colorRadioButton;
  private ColorButton             colorButton;
  private JRadioButton            textureRadioButton;
  private JComponent textureComponent;
  private JRadioButton            modelMaterialsRadioButton;
  private JComponent              modelMaterialsComponent;
  private JRadioButton            defaultShininessRadioButton;
  private JRadioButton            mattRadioButton;
  private JRadioButton            shinyRadioButton;
  private NullableCheckBox        visibleCheckBox;
  private JLabel                  lightPowerLabel;
  private JSpinner                lightPowerSpinner;
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
	  super(preferences, activity);
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
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
      final NullableSpinner.NullableSpinnerNumberModel priceSpinnerModel = 
          new NullableSpinner.NullableSpinnerNumberModel(0, 0, 10000, 1f);
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
      final NullableSpinner.NullableSpinnerLengthModel xSpinnerModel =
          new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
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
      final NullableSpinner.NullableSpinnerLengthModel ySpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
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
      final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
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
      final NullableSpinner.NullableSpinnerNumberModel angleSpinnerModel = new NullableSpinner.NullableSpinnerModuloNumberModel(
          0, 0, 360, 1);
      this.angleSpinner = new NullableSpinner(activity, angleSpinnerModel);
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
      final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
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
      final NullableSpinner.NullableSpinnerLengthModel depthSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
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
      final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
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
      this.keepProportionsCheckBox.addItemListener(new JCheckBox.ItemListener() {
        public void itemStateChanged(JCheckBox.ItemEvent ev) {
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
//        this.textureComponent = (JComponent) textureController.getView();
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
//          this.modelMaterialsComponent = (JComponent)modelMaterialsController.getView();
          //if (OperatingSystem.isMacOSX()) {
          //  this.modelMaterialsComponent.putClientProperty("JButton.buttonType", "segmented");
          //  this.modelMaterialsComponent.putClientProperty("JButton.segmentPosition", "only");
          //}
          buttonGroup.add(this.modelMaterialsRadioButton);
          boolean uniqueModel = modelMaterialsController.getModel() != null;
          this.modelMaterialsRadioButton.setEnabled(uniqueModel);
//          this.modelMaterialsComponent.setEnabled(uniqueModel);
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
      final NullableSpinner.NullableSpinnerNumberModel lightPowerSpinnerModel = new NullableSpinner.NullableSpinnerNumberModel(
          0, 0, 100, 5);
      this.lightPowerSpinner = new NullableSpinner(activity, lightPowerSpinnerModel);
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
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
  /*  if (!OperatingSystem.isMacOSX()) {
      if (this.nameLabel != null) {
        this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "nameLabel.mnemonic")).getKeyCode());
        this.nameLabel.setLabelFor(this.nameTextField);
      }
      if (this.nameVisibleCheckBox != null) {
        this.nameVisibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "nameVisibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.descriptionLabel != null) {
        this.descriptionLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "descriptionLabel.mnemonic")).getKeyCode());
        this.descriptionLabel.setLabelFor(this.descriptionTextField);
      }
      if (this.priceLabel != null) {
        this.priceLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "priceLabel.mnemonic")).getKeyCode());
        this.priceLabel.setLabelFor(this.priceSpinner);
      }
      if (this.xLabel != null) {
        this.xLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "xLabel.mnemonic")).getKeyCode());
        this.xLabel.setLabelFor(this.xSpinner);
      }
      if (this.yLabel != null) {
        this.yLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "yLabel.mnemonic")).getKeyCode());
        this.yLabel.setLabelFor(this.ySpinner);
      }
      if (this.elevationLabel != null) {
        this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "elevationLabel.mnemonic")).getKeyCode());
        this.elevationLabel.setLabelFor(this.elevationSpinner);
      }
      if (this.angleLabel != null) {
        this.angleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "angleLabel.mnemonic")).getKeyCode());
        this.angleLabel.setLabelFor(this.angleSpinner);
      }
      if (this.keepProportionsCheckBox != null) {
        this.keepProportionsCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "keepProportionsCheckBox.mnemonic")).getKeyCode());
      }
      if (this.widthLabel != null) {
        this.widthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "widthLabel.mnemonic")).getKeyCode());
        this.widthLabel.setLabelFor(this.widthSpinner);
      }
      if (this.depthLabel != null) {
        this.depthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "depthLabel.mnemonic")).getKeyCode());
        this.depthLabel.setLabelFor(this.depthSpinner);
      }
      if (this.heightLabel != null) {
        this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "heightLabel.mnemonic")).getKeyCode());
        this.heightLabel.setLabelFor(this.heightSpinner);
      }
      if (this.basePlanItemCheckBox != null) {
        this.basePlanItemCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "basePlanItemCheckBox.mnemonic")).getKeyCode());
      }
      if (this.mirroredModelCheckBox != null) {
        this.mirroredModelCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "mirroredModelCheckBox.mnemonic")).getKeyCode());
      }
      if (this.defaultColorAndTextureRadioButton != null) {
        this.defaultColorAndTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "defaultColorAndTextureRadioButton.mnemonic"))
            .getKeyCode());
        this.colorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "colorRadioButton.mnemonic")).getKeyCode());
        this.textureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "textureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.defaultShininessRadioButton != null) {
        this.defaultShininessRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "defaultShininessRadioButton.mnemonic"))
            .getKeyCode());
        this.mattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "mattRadioButton.mnemonic")).getKeyCode());
        this.shinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "shinyRadioButton.mnemonic")).getKeyCode());
      }
      if (this.visibleCheckBox != null) {
        this.visibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "visibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.lightPowerLabel != null) {
        this.lightPowerLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "lightPowerLabel.mnemonic")).getKeyCode());
        this.lightPowerLabel.setLabelFor(this.lightPowerSpinner);
      }
    }*/
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final HomeFurnitureController controller) {
    //int labelAlignment = OperatingSystem.isMacOSX()
    //    ? GridBagConstraints.LINE_END
    //    : GridBagConstraints.LINE_START;

    // First row    
    boolean priceDisplayed = this.priceLabel != null;

	  JLabel namePanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, priceDisplayed  ?  "nameAndPricePanel.title"  : "namePanel.title"));
	  rootView.addView(namePanel, rowInsets);

  //  JPanel namePanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
  //      com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, priceDisplayed  ?  "nameAndPricePanel.title"  : "namePanel.title"));
   // int rowGap = OperatingSystem.isMacOSXLeopardOrSuperior() ? 0 : 5;
    if (this.nameLabel != null) {
		rootView.addView(this.nameLabel, rowInsets);
		rootView.addView(this.nameTextField, rowInsets);
     /* namePanel.add(this.nameLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 5), 0, 0));
      namePanel.add(this.nameTextField, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));*/
    }
    if (this.nameVisibleCheckBox != null) {
		rootView.addView(this.nameVisibleCheckBox, rowInsets);
     // namePanel.add(this.nameVisibleCheckBox, new GridBagConstraints(
     //     2, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
      //    GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    if (this.descriptionLabel != null) {
		rootView.addView(this.descriptionLabel, rowInsets);
		rootView.addView(this.descriptionTextField, rowInsets);
     /* namePanel.add(this.descriptionLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          new Insets(5, 0, 0, 5), 0, 0));
      namePanel.add(this.descriptionTextField, new GridBagConstraints(
          1, 1, priceDisplayed  ? 1  : 3, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, priceDisplayed  ? 10  : 0), 0, 0));*/
    }
    if (priceDisplayed) {
		rootView.addView(this.priceLabel, rowInsets);
		rootView.addView(this.priceSpinner, rowInsets);
      /*namePanel.add(this.priceLabel, new GridBagConstraints(
          this.descriptionLabel != null  ? 2  : 0, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          new Insets(5, 0, 0, 5), 0, 0));
      namePanel.add(this.priceSpinner, new GridBagConstraints(
          this.descriptionLabel != null  ? 3  : 1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));*/
    }
   /* if (namePanel.getComponentCount() > 0) {
      add(namePanel, new GridBagConstraints(0, 0, 3, 1, 0, 0, labelAlignment, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, rowGap, 0), 0, 0));
    }*/

	  JLabel locationPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "locationPanel.title"));
	  rootView.addView(locationPanel, rowInsets);
    // Location panel
   /* JPanel locationPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "locationPanel.title"));
    Insets labelInsets = new Insets(0, 0, 5, 5);
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);*/
    if (this.xLabel != null) {
		rootView.addView(this.xLabel, rowInsets);
		rootView.addView(this.xSpinner, rowInsets);
      /*locationPanel.add(this.xLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          labelInsets, 0, 0));
      locationPanel.add(this.xSpinner, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));*/
    }
    if (this.yLabel != null) {
		rootView.addView(this.yLabel, rowInsets);
		rootView.addView(this.ySpinner, rowInsets);
      /*locationPanel.add(this.yLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, GridBagConstraints.NONE,
          labelInsets, 0, 0));
      locationPanel.add(this.ySpinner, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));*/
    }
    if (this.elevationLabel != null) {
		rootView.addView(this.elevationLabel, rowInsets);
		rootView.addView(this.elevationSpinner, rowInsets);
      /*locationPanel.add(this.elevationLabel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      locationPanel.add(this.elevationSpinner, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));*/
    }
    if (this.angleLabel != null) {
		rootView.addView(this.angleLabel, rowInsets);
		rootView.addView(this.angleSpinner, rowInsets);
      /*locationPanel.add(this.angleLabel, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      locationPanel.add(this.angleSpinner, new GridBagConstraints(
          1, 3, 1, 1, 0, 1, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));*/
    }
    if (this.basePlanItemCheckBox != null) {
		rootView.addView(this.basePlanItemCheckBox, rowInsets);
      //locationPanel.add(this.basePlanItemCheckBox, new GridBagConstraints(
       //   0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START,
      //    GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    /*if (locationPanel.getComponentCount() > 0) {
      add(locationPanel, new GridBagConstraints(
          0, 1, 1, 1, 1, 0, labelAlignment, GridBagConstraints.BOTH, new Insets(
          0, 0, rowGap, 0), 0, 0));
    }*/

	  JLabel sizePanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "sizePanel.title"));
	  rootView.addView(sizePanel, rowInsets);

    // Size panel
   // JPanel sizePanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
   //     com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "sizePanel.title"));
    if (this.widthLabel != null) {
		rootView.addView(this.widthLabel, rowInsets);
		rootView.addView(this.widthSpinner, rowInsets);
      /*sizePanel.add(this.widthLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      sizePanel.add(this.widthSpinner, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, -10, 0));*/
    }
    if (this.depthLabel != null) {
		rootView.addView(this.depthLabel, rowInsets);
		rootView.addView(this.depthSpinner, rowInsets);
      /*sizePanel.add(this.depthLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      sizePanel.add(this.depthSpinner, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, -10, 0));*/
    }
    if (this.heightLabel != null) {
		rootView.addView(this.heightLabel, rowInsets);
		rootView.addView(this.heightSpinner, rowInsets);
      /*sizePanel.add(this.heightLabel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      sizePanel.add(this.heightSpinner, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, -10, 0));*/
    }
    if (this.keepProportionsCheckBox != null) {
		rootView.addView(this.keepProportionsCheckBox, rowInsets);
      //sizePanel.add(this.keepProportionsCheckBox, new GridBagConstraints(
      //    0, 3, 2, 1, 0, 1, GridBagConstraints.LINE_START,
      //    GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.mirroredModelCheckBox != null) {
		rootView.addView(this.mirroredModelCheckBox, rowInsets);
		//sizePanel.add(this.mirroredModelCheckBox, new GridBagConstraints(
        //  0, 4, 2, 1, 0, 1, GridBagConstraints.LINE_START,
        //  GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    /*if (sizePanel.getComponentCount() > 0) {
      add(sizePanel, new GridBagConstraints(
          1, 1, 2, 1, 1, 0, labelAlignment, 
          GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));
    }*/
	  JLabel paintPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "colorAndTexturePanel.title"));
	  rootView.addView(paintPanel, rowInsets);

    //final JPanel paintPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
    //    com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "colorAndTexturePanel.title"));
    if (this.defaultColorAndTextureRadioButton != null) {
		rootView.addView(this.defaultColorAndTextureRadioButton, rowInsets);
		rootView.addView(this.colorRadioButton, rowInsets);
		rootView.addView(this.colorButton, rowInsets);

     /* int buttonPadY;
      int buttonsBottomInset;
      if (OperatingSystem.isMacOSXLeopardOrSuperior() 
          && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
        // Ensure the top and bottom of segmented buttons are correctly drawn 
        buttonPadY = 4;
        buttonsBottomInset = -4;
      } else {
        buttonPadY = 0;
        buttonsBottomInset = 0;
      }
      // Color and Texture panel
      paintPanel.add(this.defaultColorAndTextureRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      paintPanel.add(this.colorRadioButton, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      paintPanel.add(this.colorButton, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, buttonsBottomInset, 0), 0, buttonPadY));*/
      if (this.textureComponent != null) {
		  rootView.addView(this.textureRadioButton, rowInsets);
//		  rootView.addView(this.textureComponent, rowInsets);
       /* paintPanel.add(this.textureRadioButton, new GridBagConstraints(
            0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
        paintPanel.add(this.textureComponent, new GridBagConstraints(
            1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, new Insets(5, 0, buttonsBottomInset, 0), 0, buttonPadY));*/
      }
      if (this.modelMaterialsComponent != null) {
		  rootView.addView(this.modelMaterialsRadioButton, rowInsets);
//		  rootView.addView(this.modelMaterialsComponent, rowInsets);
        /*paintPanel.add(this.modelMaterialsRadioButton, new GridBagConstraints(
            0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
        paintPanel.add(this.modelMaterialsComponent, new GridBagConstraints(
            1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, new Insets(5, 0, buttonsBottomInset, 0), 0, buttonPadY));*/
      }
      //add(paintPanel, new GridBagConstraints(
      //    0, 2, 1, 1, 0, 0, labelAlignment,
      //    GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));
      
      controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
				//TODO: more visibility
              //paintPanel.setVisible(controller.isTexturable());
            }
          });
      //paintPanel.setVisible(controller.isTexturable());
    }
    if (this.defaultShininessRadioButton != null) {
		JLabel shininessPanel = new JLabel(activity, preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "shininessPanel.title"));
		rootView.addView(shininessPanel, rowInsets);
		rootView.addView(this.defaultShininessRadioButton, rowInsets);
		rootView.addView(this.mattRadioButton, rowInsets);
		rootView.addView(this.shinyRadioButton, rowInsets);



      // Shininess panel
 /*     final JPanel shininessPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.HomeFurniturePanel.class, "shininessPanel.title"));
      shininessPanel.add(this.defaultShininessRadioButton, new GridBagConstraints(
          0, 0, 1, 1, 0, 1, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      shininessPanel.add(this.mattRadioButton, new GridBagConstraints(
          0, 1, 1, 1, 0, 1, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      shininessPanel.add(this.shinyRadioButton, new GridBagConstraints(
          0, 2, 1, 1, 0, 1, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
      if (paintPanel.getComponentCount() == 7) {
        shininessPanel.add(new JLabel(), new GridBagConstraints(
            0, 3, 1, 1, 0, 1, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
      }
      add(shininessPanel, new GridBagConstraints(
          1, 2, 2, 1, 0, 0, labelAlignment, 
          GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));*/
      
      controller.addPropertyChangeListener(HomeFurnitureController.Property.TEXTURABLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              //TODO: more visibility stuff
				//shininessPanel.setVisible(controller.isTexturable());
            }
          });
      //shininessPanel.setVisible(controller.isTexturable());
    }
    // Last row
    if (this.visibleCheckBox != null) {
		rootView.addView(this.visibleCheckBox, rowInsets);
      //add(this.visibleCheckBox, new GridBagConstraints(
       //   0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
       //   GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
    }
    if (this.lightPowerLabel != null) {
		rootView.addView(this.lightPowerLabel, rowInsets);
		rootView.addView(this.lightPowerSpinner, rowInsets);
     // add(this.lightPowerLabel, new GridBagConstraints(
     //     1, 3, 1, 1, 0, 0, labelAlignment,
     //     GridBagConstraints.NONE, new Insets(0, 10, 0, 5), 0, 0));
     // add(this.lightPowerSpinner, new GridBagConstraints(
     //     2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
     //     GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    }


	  this.setTitle(dialogTitle);
	  rootView.addView(closeButton, labelInsets);
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(VCView parentView) {
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
