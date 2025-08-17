/*
 * DimensionLinePanel.java 04 mai 2023
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.DimensionLineController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.ButtonGroup;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.JRadioButton;

import javaawt.Color;

/**
 * Dimension line editing panel.
 * @author Emmanuel Puybaret
 */
public class DimensionLinePanel extends AndroidDialogView implements DialogView {
  private final boolean         dimensionLineModification;
  private final DimensionLineController controller;
  private JLabel               xStartLabel;
  private JSpinner             xStartSpinner;
  private JLabel               yStartLabel;
  private JSpinner             yStartSpinner;
  private JLabel               elevationStartLabel;
  private JSpinner             elevationStartSpinner;
  private JLabel               xEndLabel;
  private JSpinner             xEndSpinner;
  private JLabel               yEndLabel;
  private JSpinner             yEndSpinner;
  private JLabel               distanceToEndPointLabel;
  private JSpinner             distanceToEndPointSpinner;
  private JLabel               offsetLabel;
  private JSpinner             offsetSpinner;
  private JRadioButton         planDimensionLineRadioButton;
  private JRadioButton         elevationDimensionLineRadioButton;
  private JLabel               lengthFontSizeLabel;
  private JSpinner             lengthFontSizeSpinner;
  private JLabel               colorLabel;
  private ColorButton          colorButton;;
  private NullableCheckBox     visibleIn3DViewCheckBox;
  private JLabel               pitchLabel;
  private JRadioButton         pitch0DegreeRadioButton;
  private JRadioButton         pitch90DegreeRadioButton;
  private String               dialogTitle;

  /**
   * Creates a panel that displays wall data according to the units set in
   * <code>preferences</code>.
   * @param modification specifies whether this panel edits existing dimension lines or a new one
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public DimensionLinePanel(boolean modification,
                            UserPreferences preferences,
                            DimensionLineController controller, Activity activity) {
    //super(new GridBagLayout());
    super(preferences, activity, R.layout.dialog_dimensionlinepanel);
    this.dimensionLineModification = modification;
    this.controller = controller;
    createComponents(modification, preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(boolean modification,
                                final UserPreferences preferences,
                                final DimensionLineController controller) {
    // Get unit name matching current unit
    String unitName = preferences.getLengthUnit().getName();

    // Create X start label and its spinner bound to X_START controller property
    this.xStartLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "xLabel.text", unitName));
    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel xStartSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xStartSpinner = new NullableSpinner(activity, xStartSpinnerModel);
    xStartSpinnerModel.setNullable(controller.getXStart() == null);
    xStartSpinnerModel.setLength(controller.getXStart());
    final PropertyChangeListener xStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xStartSpinnerModel.setNullable(ev.getNewValue() == null);
          xStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.X_START, xStartChangeListener);
    xStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.X_START, xStartChangeListener);
          controller.setXStart(xStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.X_START, xStartChangeListener);
        }
      });

    // Create Y start label and its spinner bound to Y_START controller property
    this.yStartLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "yLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel yStartSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yStartSpinner = new NullableSpinner(activity, yStartSpinnerModel);
    yStartSpinnerModel.setNullable(controller.getYStart() == null);
    yStartSpinnerModel.setLength(controller.getYStart());
    final PropertyChangeListener yStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yStartSpinnerModel.setNullable(ev.getNewValue() == null);
          yStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.Y_START, yStartChangeListener);
    yStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.Y_START, yStartChangeListener);
          controller.setYStart(yStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.Y_START, yStartChangeListener);
        }
      });

    // Create elevation start label and its spinner bound to ELEVATION_START controller property
    this.elevationStartLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "elevationLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel elevationStartSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, 0, maximumLength);
    this.elevationStartSpinner = new NullableSpinner(activity, elevationStartSpinnerModel);
    elevationStartSpinnerModel.setNullable(controller.getElevationStart() == null);
    elevationStartSpinnerModel.setLength(controller.getElevationStart());
    final PropertyChangeListener elevationStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationStartSpinnerModel.setNullable(ev.getNewValue() == null);
          elevationStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.ELEVATION_START, elevationStartChangeListener);
    elevationStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.ELEVATION_START, elevationStartChangeListener);
          Float elevationStart = elevationStartSpinnerModel.getLength();
          if (elevationStart != null && controller.getElevationEnd() != null && controller.getElevationStart() != null) {
            controller.setElevationEnd(controller.getElevationEnd() + elevationStart - controller.getElevationStart());
          }
          controller.setElevationStart(elevationStart);
          controller.addPropertyChangeListener(DimensionLineController.Property.ELEVATION_START, elevationStartChangeListener);
        }
      });

    // Create X end label and its spinner bound to X_END controller property
    this.xEndLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "xLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel xEndSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xEndSpinner = new NullableSpinner(activity, xEndSpinnerModel);
    xEndSpinnerModel.setNullable(controller.getXEnd() == null);
    xEndSpinnerModel.setLength(controller.getXEnd());
    final PropertyChangeListener xEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xEndSpinnerModel.setNullable(ev.getNewValue() == null);
          xEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.X_END, xEndChangeListener);
    xEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.X_END, xEndChangeListener);
          controller.setXEnd(xEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.X_END, xEndChangeListener);
        }
      });

    // Create Y end label and its spinner bound to Y_END controller property
    this.yEndLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "yLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel yEndSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yEndSpinner = new NullableSpinner(activity, yEndSpinnerModel);
    yEndSpinnerModel.setNullable(controller.getYEnd() == null);
    yEndSpinnerModel.setLength(controller.getYEnd());
    final PropertyChangeListener yEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yEndSpinnerModel.setNullable(ev.getNewValue() == null);
          yEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.Y_END, yEndChangeListener);
    yEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.Y_END, yEndChangeListener);
          controller.setYEnd(yEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.Y_END, yEndChangeListener);
        }
      });

    // Create distance to end point label and its spinner bound to DISTANCE_TO_END_POINT controller property
    this.distanceToEndPointLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "distanceToEndPointLabel.text", unitName));
    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel distanceToEndPointSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, 2 * maximumLength * (float)Math.sqrt(2));
    this.distanceToEndPointSpinner = new NullableSpinner(activity, distanceToEndPointSpinnerModel);
    distanceToEndPointSpinnerModel.setNullable(controller.getDistanceToEndPoint() == null);
    distanceToEndPointSpinnerModel.setLength(controller.getDistanceToEndPoint());
    final PropertyChangeListener distanceToEndPointChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          distanceToEndPointSpinnerModel.setNullable(ev.getNewValue() == null);
          distanceToEndPointSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.DISTANCE_TO_END_POINT,
        distanceToEndPointChangeListener);
    distanceToEndPointSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.DISTANCE_TO_END_POINT,
              distanceToEndPointChangeListener);
          controller.setDistanceToEndPoint(distanceToEndPointSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.DISTANCE_TO_END_POINT,
              distanceToEndPointChangeListener);
        }
      });

    // Create offset label and its spinner bound to OFFSET controller property
    this.offsetLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "offsetLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel offsetSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -10000, 10000);
    this.offsetSpinner = new NullableSpinner(activity, offsetSpinnerModel);
    offsetSpinnerModel.setNullable(controller.getOffset() == null);
    offsetSpinnerModel.setLength(controller.getOffset());
    final PropertyChangeListener offsetChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          offsetSpinnerModel.setNullable(ev.getNewValue() == null);
          offsetSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.OFFSET, offsetChangeListener);
    offsetSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.OFFSET, offsetChangeListener);
          controller.setOffset(offsetSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.OFFSET, offsetChangeListener);
        }
      });

    // Orientation radio buttons bound to ORIENTATION controller property
    this.planDimensionLineRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "planDimensionLineRadioButton.text"));
    this.planDimensionLineRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (planDimensionLineRadioButton.isSelected()) {
            controller.setOrientation(DimensionLineController.DimensionLineOrientation.PLAN);
          }
        }
      });
    this.elevationDimensionLineRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "elevationDimensionLineRadioButton.text"));
    this.elevationDimensionLineRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (elevationDimensionLineRadioButton.isSelected()) {
            controller.setOrientation(DimensionLineController.DimensionLineOrientation.ELEVATION);
          }
        }
      });
    controller.addPropertyChangeListener(DimensionLineController.Property.ORIENTATION,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateOrientationRadioButtons(controller);
          }
        });

    ButtonGroup orientationButtonGroup = new ButtonGroup();
    orientationButtonGroup.add(this.planDimensionLineRadioButton);
    orientationButtonGroup.add(this.elevationDimensionLineRadioButton);

    // Create font size label and its spinner bound to FONT_SIZE controller property
    this.lengthFontSizeLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.swing.DimensionLinePanel.class,
        "lengthFontSizeLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel lenghtFontSizeSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
        preferences, 5, 999);
    this.lengthFontSizeSpinner = new NullableSpinner(activity, lenghtFontSizeSpinnerModel);
    final PropertyChangeListener fontSizeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Float fontSize = controller.getLengthFontSize();
          lenghtFontSizeSpinnerModel.setNullable(fontSize == null);
          lenghtFontSizeSpinnerModel.setLength(fontSize);
        }
      };
    fontSizeChangeListener.propertyChange(null);
    controller.addPropertyChangeListener(DimensionLineController.Property.LENGTH_FONT_SIZE, fontSizeChangeListener);
    lenghtFontSizeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.LENGTH_FONT_SIZE, fontSizeChangeListener);
          controller.setLengthFontSize(lenghtFontSizeSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.LENGTH_FONT_SIZE, fontSizeChangeListener);
        }
      });

    // Create color label and button bound to controller COLOR property
    this.colorLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "colorLabel.text"));

    this.colorButton = new ColorButton(activity, preferences);
    /*if (OperatingSystem.isMacOSX()) {
      this.colorButton.putClientProperty("JButton.buttonType", "segmented");
      this.colorButton.putClientProperty("JButton.segmentPosition", "only");
    }*/
    this.colorButton.setColorDialogTitle(preferences
        .getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor() != null ? controller.getColor() : Color.BLACK.getRGB());//getForeground().getRGB());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          controller.setColor(colorButton.getColor());
        }
      });
    controller.addPropertyChangeListener(DimensionLineController.Property.COLOR, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          colorButton.setColor(controller.getColor());
        }
      });

    this.visibleIn3DViewCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "visibleIn3DViewCheckBox.text"));
    this.visibleIn3DViewCheckBox.setNullable(controller.isVisibleIn3D() == null);
    this.visibleIn3DViewCheckBox.setValue(controller.isVisibleIn3D());
    final PropertyChangeListener visibilityChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          visibleIn3DViewCheckBox.setValue(controller.isVisibleIn3D());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.VISIBLE_IN_3D, visibilityChangeListener);
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.VISIBLE_IN_3D, visibilityChangeListener);
          controller.setVisibleIn3D(visibleIn3DViewCheckBox.getValue());
          if (visibleIn3DViewCheckBox.isNullable()) {
            visibleIn3DViewCheckBox.setNullable(false);
          }
          updateOrientationRadioButtons(controller);
          controller.addPropertyChangeListener(DimensionLineController.Property.VISIBLE_IN_3D, visibilityChangeListener);
        }
      });

    // Create pitch components bound to PITCH controller property
    this.pitchLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "pitchLabel.text"));
    this.pitch0DegreeRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "pitch0DegreeRadioButton.text"));
    ChangeListener pitchRadioButtonsItemListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (pitch0DegreeRadioButton.isSelected()) {
            controller.setPitch(0f);
          } else if (pitch90DegreeRadioButton.isSelected()) {
            controller.setPitch((float)(-Math.PI / 2));
          }
        }
      };
    this.pitch0DegreeRadioButton.addChangeListener(pitchRadioButtonsItemListener);
    this.pitch90DegreeRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.swing.DimensionLinePanel.class, "pitch90DegreeRadioButton.text"));
    this.pitch90DegreeRadioButton.addChangeListener(pitchRadioButtonsItemListener);
    final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateOrientationRadioButtons(controller);
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.PITCH, pitchChangeListener);
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.PITCH, pitchChangeListener);
          updateOrientationRadioButtons(controller);
          controller.addPropertyChangeListener(DimensionLineController.Property.PITCH, pitchChangeListener);
        }
      });

    ButtonGroup pitchGroup = new ButtonGroup();
    pitchGroup.add(this.pitch0DegreeRadioButton);
    pitchGroup.add(this.pitch90DegreeRadioButton);
    updateOrientationRadioButtons(controller);

    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class,
        modification
            ? "dimensionLineModification.title"
            : "dimensionLineCreation.title");
  }

  /**
   * Updates orientation radio buttons.
   */
  private void updateOrientationRadioButtons(DimensionLineController controller) {
    if (controller.getOrientation() == DimensionLineController.DimensionLineOrientation.PLAN) {
      this.planDimensionLineRadioButton.setSelected(true);
    } else if (controller.getOrientation() == DimensionLineController.DimensionLineOrientation.ELEVATION) {
      this.elevationDimensionLineRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.planDimensionLineRadioButton, this.elevationDimensionLineRadioButton);
    }
    boolean orientable = controller.isEditableDistance();
    this.planDimensionLineRadioButton.setEnabled(orientable);
    this.elevationDimensionLineRadioButton.setEnabled(orientable);

    if (controller.getPitch() == null
        || controller.getOrientation() == DimensionLineController.DimensionLineOrientation.ELEVATION) {
      SwingTools.deselectAllRadioButtons(this.pitch0DegreeRadioButton, this.pitch90DegreeRadioButton);
    } else if (controller.getPitch() == 0) {
      this.pitch0DegreeRadioButton.setSelected(true);
    } else if (Math.abs(controller.getPitch()) == (float)(Math.PI / 2)) {
      this.pitch90DegreeRadioButton.setSelected(true);
    } else {
      SwingTools.deselectAllRadioButtons(this.pitch0DegreeRadioButton, this.pitch90DegreeRadioButton);
    }
    boolean planOrientation = controller.getOrientation() == DimensionLineController.DimensionLineOrientation.PLAN;
    boolean visibleIn3D = Boolean.TRUE.equals(controller.isVisibleIn3D());
    this.pitch0DegreeRadioButton.setEnabled(visibleIn3D && planOrientation);
    this.pitch90DegreeRadioButton.setEnabled(visibleIn3D && planOrientation);

    this.elevationStartSpinner.setEnabled(visibleIn3D
        || controller.getOrientation() == DimensionLineController.DimensionLineOrientation.ELEVATION);
    this.xEndSpinner.setEnabled(planOrientation);
    this.yEndSpinner.setEnabled(planOrientation);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    /*if (!OperatingSystem.isMacOSX()) {
      this.xStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xStartLabel.setLabelFor(this.xStartSpinner);
      this.yStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yStartLabel.setLabelFor(this.yStartSpinner);
      this.elevationStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationStartLabel.setLabelFor(this.elevationStartSpinner);
      this.xEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xEndLabel.setLabelFor(this.xEndSpinner);
      this.yEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yEndLabel.setLabelFor(this.yEndSpinner);
      this.planDimensionLineRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "planDimensionLineRadioButton.mnemonic")).getKeyCode());
      this.elevationDimensionLineRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "elevationDimensionLineRadioButton.mnemonic")).getKeyCode());
      this.distanceToEndPointLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "distanceToEndPointLabel.mnemonic")).getKeyCode());
      this.distanceToEndPointLabel.setLabelFor(this.distanceToEndPointSpinner);
      this.offsetLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "offsetLabel.mnemonic")).getKeyCode());
      this.offsetLabel.setLabelFor(this.offsetSpinner);
      this.lengthFontSizeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "lengthFontSizeLabel.mnemonic")).getKeyCode());
      this.lengthFontSizeLabel.setLabelFor(this.lengthFontSizeSpinner);
      this.visibleIn3DViewCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.swing.DimensionLinePanel.class, "visibleIn3DViewCheckBox.mnemonic")).getKeyCode());
      this.pitch0DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.swing.DimensionLinePanel.class, "pitch0DegreeRadioButton.mnemonic")).getKeyCode());
      this.pitch90DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
              com.eteks.sweethome3d.swing.DimensionLinePanel.class, "pitch90DegreeRadioButton.mnemonic")).getKeyCode());
    }*/
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(UserPreferences preferences,
                                final DimensionLineController controller) {
    // First row
    final JLabel startPointPanel = new JLabel(activity,
        preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "startPointPanel.title"));
    swapOut(startPointPanel, R.id.dimline_panel_startPanel);
    swapOut(this.xStartLabel, R.id.dimline_panel_xStartLabel );
    swapOut(this.xStartSpinner, R.id.dimline_panel_xStartSpinner );
    swapOut(this.yStartLabel, R.id.dimline_panel_yStartLabel );
    swapOut(this.yStartSpinner, R.id.dimline_panel_yStartSpinner );
    swapOut(this.elevationStartLabel, R.id.dimline_panel_elevationLabel );
    swapOut(this.elevationStartSpinner, R.id.dimline_panel_elevationSpinner );

    // Second row
    final JLabel endPointPanel = new JLabel(activity,
        preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "endPointPanel.title"));

    swapOut(endPointPanel, R.id.dimline_panel_endPanel );
    swapOut(this.xEndLabel, R.id.dimline_panel_xEndLabel );
    swapOut(this.xEndSpinner, R.id.dimline_panel_xEndSpinner );
    swapOut(this.yEndLabel, R.id.dimline_panel_yEndLabel );
    swapOut(this.yEndSpinner, R.id.dimline_panel_yEndSpinner );
    swapOut(this.distanceToEndPointLabel, R.id.dimline_panel_distLabel );
    swapOut(this.distanceToEndPointSpinner, R.id.dimline_panel_distSpinner );
    swapOut(this.offsetLabel, R.id.dimline_panel_offsetLabel );
    swapOut(this.offsetSpinner, R.id.dimline_panel_offsetSpinner );
    swapOut(this.planDimensionLineRadioButton, R.id.dimline_panel_planDimension );
    swapOut(this.elevationDimensionLineRadioButton, R.id.dimline_panel_elevationDim );

    // Third row
    JLabel stylePanel = new JLabel(activity,
        preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "stylePanel.title"));
    swapOut(stylePanel, R.id.dimline_panel_stylePanel );
    swapOut(this.lengthFontSizeLabel, R.id.dimline_panel_fontSizeLabel );
    swapOut(this.lengthFontSizeSpinner, R.id.dimline_panel_fontSizeSpinner );
    swapOut(this.colorLabel, R.id.dimline_panel_colorLabel );
    swapOut(this.colorButton, R.id.dimline_panel_colorButton );

    // Fourth row
    JLabel rendering3DPanel = new JLabel(activity,
        preferences.getLocalizedString(com.eteks.sweethome3d.swing.DimensionLinePanel.class, "rendering3DPanel.title"));
    swapOut(rendering3DPanel, R.id.dimline_panel_3dRendLabel );
    //swapOut(this.pitchLabel, R.id. );
    swapOut(this.pitch0DegreeRadioButton, R.id.dimline_panel_labelHorizontalRadioButton );
    swapOut(this.pitch90DegreeRadioButton, R.id.dimline_panel_labelVerticalRadioButton );


    PropertyChangeListener distanceListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          distanceToEndPointLabel.setVisible(controller.isEditableDistance());
          distanceToEndPointSpinner.setVisible(controller.isEditableDistance());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.EDITABLE_DISTANCE, distanceListener);
    distanceListener.propertyChange(null);
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    //PJPJ TODO whats this edited spinner thing?
    /*JSpinner editedSpinner = this.distanceToEndPointSpinner.getValue() != null
        ? this.distanceToEndPointSpinner
        : this.offsetSpinner;
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, ((JSpinner.DefaultEditor)editedSpinner.getEditor()).getTextField()) == JOptionPane.OK_OPTION
        && this.controller != null) {
      if (this.dimensionLineModification) {
        this.controller.modifyDimensionLines();
      } else {
        this.controller.createDimensionLine();
      }
    }*/

    this.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        if (dimensionLineModification) {
          controller.modifyDimensionLines();
        } else {
          controller.createDimensionLine();
        }
      }
    });
    this.show();
  }
}
