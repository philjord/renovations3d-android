/*
 * LabelPanel.java 29 nov. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.view.View;
import android.view.WindowManager;

import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.renovations3d.android.swingish.JSpinner2;
import com.eteks.renovations3d.android.swingish.JTextField;
import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Label editing panel.
 * @author Emmanuel Puybaret
 */
public class LabelPanel extends AndroidDialogView implements DialogView {
  private final boolean         labelModification;
  private final LabelController controller;
  private JLabel textLabel;
  private JTextField textTextField;
  //private JLabel                fontNameLabel;//there are only 4 fonts on android!http://stackoverflow.com/questions/12128331/how-to-change-fontfamily-of-textview-in-android/13329907#13329907
  //private FontNameComboBox      fontNameComboBox;
  private JLabel                fontSizeLabel;
  private JSpinner2 fontSizeSpinner;
  private JLabel                colorLabel;
  private ColorButton           colorButton;
  private NullableCheckBox visibleIn3DViewCheckBox;
  private JLabel                pitchLabel;
  private JRadioButton pitch0DegreeRadioButton;
  private JRadioButton pitch90DegreeRadioButton;
  private JLabel                elevationLabel;
  private JSpinner2 elevationSpinner;
  private String                dialogTitle;

  /**
   * Creates a panel that displays label data.
   * @param modification specifies whether this panel edits an existing label or new one
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public LabelPanel(boolean modification,
                    UserPreferences preferences,
                    LabelController controller, Activity activity) {
	  super(preferences, activity, R.layout.dialog_labelpanel);
    this.labelModification = modification;
    this.controller = controller;
    createComponents(modification, preferences, controller);
    layoutComponents(controller, preferences);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(boolean modification, 
                                UserPreferences preferences, 
                                final LabelController controller) {
    // Create text label and its text field bound to NAME controller property
    this.textLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "textLabel.text"));
    this.textTextField = new AutoCompleteTextField(activity, controller.getText(), 20, preferences.getAutoCompletionStrings("LabelText"));
    //if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
    //  SwingTools.addAutoSelectionOnFocusGain(this.textTextField);
    //}
    final PropertyChangeListener textChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          textTextField.setText(controller.getText());
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);

	  textTextField.addTextChangedListener(new TextWatcher(){
		  public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
		  public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
		  public void afterTextChanged(Editable arg0) {
			  controller.removePropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
			  String text = textTextField.getText().toString();
			  if (text == null || text.trim().length() == 0) {
				  controller.setText("");
			  } else {
				  controller.setText(text);
			  }
			  controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
		  }
	  });

    /*this.textTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
          String text = textTextField.getText(); 
          if (text == null || text.trim().length() == 0) {
            controller.setText("");
          } else {
            controller.setText(text);
          }
          controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });*/
    
    // Create font name label and combo box bound to controller FONT_NAME property
  /*  this.fontNameLabel = new TextView(activity);fontNameLabel.setText(SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.LabelPanel.class, "fontNameLabel.text"));
    this.fontNameComboBox = new FontNameComboBox(preferences);
    this.fontNameComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          String selectedItem = (String)fontNameComboBox.getSelectedItem();
          controller.setFontName(selectedItem == FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME 
              ? null : selectedItem);
        }
      });
    PropertyChangeListener fontNameChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (controller.isFontNameSet()) {
            String fontName = controller.getFontName();
            fontNameComboBox.setSelectedItem(fontName == null 
                ? FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME : fontName);
          } else {
            fontNameComboBox.setSelectedItem(null);
          }
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.FONT_NAME, fontNameChangeListener);
    fontNameChangeListener.propertyChange(null);
*/
    // Create font size label and its spinner bound to FONT_SIZE controller property
    String unitName = preferences.getLengthUnit().getName();
    this.fontSizeLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.LabelPanel.class,
        "fontSizeLabel.text", unitName));
	final NullableSpinnerNumberModel.NullableSpinnerLengthModel fontSizeSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
			  preferences, 5, 999);
    this.fontSizeSpinner = new NullableSpinner2(activity, fontSizeSpinnerModel);
    final PropertyChangeListener fontSizeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Float fontSize = controller.getFontSize();
          fontSizeSpinnerModel.setNullable(fontSize == null);
          fontSizeSpinnerModel.setValue(fontSize);
        }
      };
    fontSizeChangeListener.propertyChange(null);
    controller.addPropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
    fontSizeSpinnerModel.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
          controller.setFontSize(((Number)fontSizeSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
        }
      });

    // Create color label and button bound to controller COLOR property
    this.colorLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "colorLabel.text"));
    
    this.colorButton = new ColorButton(activity, preferences);
   // if (OperatingSystem.isMacOSX()) {
    //  this.colorButton.putClientProperty("JButton.buttonType", "segmented");
    //  this.colorButton.putClientProperty("JButton.segmentPosition", "only");
    //}
    this.colorButton.setColorDialogTitle(preferences
        .getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          controller.setColor(colorButton.getColor());
        }
      });
    controller.addPropertyChangeListener(LabelController.Property.COLOR, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          colorButton.setColor(controller.getColor());
        }
      });

    // Create pitch components bound to PITCH controller property
    final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          update3DViewComponents(controller);
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
    this.visibleIn3DViewCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "visibleIn3DViewCheckBox.text"));
    if (controller.isPitchEnabled() != null) {
      this.visibleIn3DViewCheckBox.setChecked(controller.isPitchEnabled());
    } else {
      this.visibleIn3DViewCheckBox.setNullable(true);
      this.visibleIn3DViewCheckBox.setChecked(false);
    }
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
          if (visibleIn3DViewCheckBox.isNullable()) {
            visibleIn3DViewCheckBox.setNullable(false);
          }
          if (Boolean.FALSE.equals(visibleIn3DViewCheckBox.isSelected())) {
            controller.setPitch(null);
          } else if (pitch90DegreeRadioButton.isSelected()) {
            controller.setPitch((float)(Math.PI / 2));
          } else {
            controller.setPitch(0f);
          }
          update3DViewComponents(controller);
          controller.addPropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
        }
      });
    this.pitchLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "pitchLabel.text"));
    this.pitch0DegreeRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "pitch0DegreeRadioButton.text"));
	  View.OnClickListener pitchRadioButtonsItemListener = new View.OnClickListener() {
		  public void onClick(View v) {
          if (pitch0DegreeRadioButton.isSelected()) {
            controller.setPitch(0f);
          } else if (pitch90DegreeRadioButton.isSelected()) {
            controller.setPitch((float)(Math.PI / 2));
          } 
        }
      };
    this.pitch0DegreeRadioButton.setOnClickListener(pitchRadioButtonsItemListener);
    this.pitch90DegreeRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "pitch90DegreeRadioButton.text"));
    this.pitch90DegreeRadioButton.setOnClickListener(pitchRadioButtonsItemListener);
    ButtonGroup pitchGroup = new ButtonGroup();
    pitchGroup.add(this.pitch0DegreeRadioButton);
    pitchGroup.add(this.pitch90DegreeRadioButton);
    
    // Create elevation label and its spinner bound to ELEVATION controller property
    this.elevationLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "elevationLabel.text", unitName));

	  final NullableSpinnerNumberModel.NullableSpinnerLengthModel elevationSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerLengthModel(
			  preferences, 0f, preferences.getLengthUnit().getMaximumElevation());
    this.elevationSpinner = new NullableSpinner2(activity, elevationSpinnerModel);
    elevationSpinnerModel.setNullable(controller.getElevation() == null);
    elevationSpinnerModel.setValue(controller.getElevation());
    final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationSpinnerModel.setNullable(ev.getNewValue() == null);
          elevationSpinnerModel.setValue((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
          controller.setElevation(((Number)elevationSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
        }
      });

    update3DViewComponents(controller);
    
    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class,
        modification 
            ? "labelModification.title"
            : "labelCreation.title");

  }

  private void update3DViewComponents(LabelController controller) {
    boolean visibleIn3D = Boolean.TRUE.equals(controller.isPitchEnabled());
    this.pitch0DegreeRadioButton.setEnabled(visibleIn3D);
    this.pitch90DegreeRadioButton.setEnabled(visibleIn3D);
    this.elevationSpinner.setEnabled(visibleIn3D);
    if (controller.getPitch() != null) {
      if (controller.getPitch() == 0) {
        this.pitch0DegreeRadioButton.setSelected(true);
      } else if (controller.getPitch() == (float)(Math.PI / 2)) {
        this.pitch90DegreeRadioButton.setSelected(true);
      }
    }
  }
  

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(final LabelController controller, UserPreferences preferences)
  {
	  JLabel nameAndStylePanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "textAndStylePanel.title"));
	  swapOut(nameAndStylePanel, R.id.labelpanel_nameAndStylePanel);
	  swapOut(this.textLabel, R.id.labelpanel_textLabel);
	  swapOut(this.textTextField, R.id.labelpanel_textTextField);
	  //hide the input keyboard unless the text is blank
	  if(this.textTextField.getText().toString() != null && this.textTextField.getText().toString().length() > 0)
	  		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


	  //PJPJPJ no font styles on android
	//  swapOut(this.fontNameLabel, R.id.labelpanel_);
 /*   Dimension preferredSize = this.fontNameComboBox.getPreferredSize();
    preferredSize.width = Math.min(preferredSize.width, this.textTextField.getPreferredSize().width);
    this.fontNameComboBox.setPreferredSize(preferredSize);
    nameAndStylePanel.add(this.fontNameComboBox, */


	  swapOut(this.fontSizeLabel, R.id.labelpanel_fontSizeLabel);
	  swapOut(this.fontSizeSpinner, R.id.labelpanel_fontSizeSpinner);
	  swapOut(this.colorLabel, R.id.labelpanel_colorLabel);
	  swapOut(this.colorButton, R.id.labelpanel_colorButton);

	  JLabel rendering3DPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "rendering3DPanel.title"));
	  swapOut(rendering3DPanel, R.id.labelpanel_rendering3DPanel);
	  swapOut(this.visibleIn3DViewCheckBox, R.id.labelpanel_visibleIn3DViewCheckBox);
	  swapOut(this.pitchLabel, R.id.labelpanel_pitchLabel);
	  swapOut(this.pitch0DegreeRadioButton, R.id.labelpanel_pitch0DegreeRadioButton);
	  swapOut(this.pitch90DegreeRadioButton, R.id.labelpanel_pitch90DegreeRadioButton);
	  swapOut(this.elevationLabel, R.id.labelpanel_elevationLabel);
	  swapOut(this.elevationSpinner, R.id.labelpanel_elevationSpinner);

	  this.setTitle(dialogTitle);
	  swapOut(closeButton, R.id.labelpanel_closeButton);
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
  /*  if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.textTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      if (this.labelModification) {
        this.controller.modifyLabels();
      } else {
        this.controller.createLabel();
      }
    }*/

	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  if (labelModification) {
				  controller.modifyLabels();
			  } else {
				  controller.createLabel();
			  }
		  }
	  });
	  this.show();
  }
}
