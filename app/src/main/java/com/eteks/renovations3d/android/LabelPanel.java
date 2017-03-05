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
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.renovations3d.android.swingish.JTextField;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.renovations3d.android.swingish.ButtonGroup;

import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;



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
  private JSpinner fontSizeSpinner;
  private JLabel                colorLabel;
  private ColorButton           colorButton;
  private NullableCheckBox visibleIn3DViewCheckBox;
  private JLabel                pitchLabel;
  private JRadioButton pitch0DegreeRadioButton;
  private JRadioButton pitch90DegreeRadioButton;
  private JLabel                elevationLabel;
  private JSpinner elevationSpinner;
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
	  //super(new GridBagLayout());
	  super(preferences, activity);
    this.labelModification = modification;
    this.controller = controller;
    createComponents(modification, preferences, controller);
    setMnemonics(preferences);
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
    final SpinnerLengthModel fontSizeSpinnerModel = new SpinnerLengthModel(
        preferences, 5, 999);
    this.fontSizeSpinner = new NullableSpinner(activity, fontSizeSpinnerModel);
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
          controller.setFontSize((float)fontSizeSpinnerModel.getValue());
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
    final SpinnerLengthModel elevationSpinnerModel = new SpinnerLengthModel(
        preferences, 0f, preferences.getLengthUnit().getMaximumElevation()/20);//PJPJP 10k is too much
    this.elevationSpinner = new AutoCommitSpinner(activity, elevationSpinnerModel);
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
          controller.setElevation((float)elevationSpinnerModel.getValue());
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
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
 /*   if (!OperatingSystem.isMacOSX()) {
      this.textLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "textLabel.mnemonic")).getKeyCode());
      this.textLabel.setLabelFor(this.textTextField);
      this.fontNameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "fontNameLabel.mnemonic")).getKeyCode());
      this.fontNameLabel.setLabelFor(this.fontNameComboBox);
      this.fontSizeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(LabelPanel.class, "fontSizeLabel.mnemonic")).getKeyCode());
      this.fontSizeLabel.setLabelFor(this.fontSizeSpinner);
      this.visibleIn3DViewCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString( 
          LabelPanel.class, "visibleIn3DViewCheckBox.mnemonic")).getKeyCode());
      this.pitch0DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString( 
          LabelPanel.class, "pitch0DegreeRadioButton.mnemonic")).getKeyCode());
      this.pitch90DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString( 
          LabelPanel.class, "pitch90DegreeRadioButton.mnemonic")).getKeyCode());
      this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
    }*/
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(final LabelController controller, UserPreferences preferences) {
   // int labelAlignment = OperatingSystem.isMacOSX()
   //     ? GridBagConstraints.LINE_END
   //     : GridBagConstraints.LINE_START;

	  JLabel nameAndAreaPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "textAndStylePanel.title"));
	  rootView.addView(nameAndAreaPanel, rowInsets);

    //JPanel nameAndStylePanel = SwingTools.createTitledPanel(
    //    preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "textAndStylePanel.title"));
	  rootView.addView(this.textLabel, labelInsets);//, new GridBagConstraints(
        //0, 0, 1, 1, 0, 0, labelAlignment,
       // GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
	  rootView.addView(this.textTextField, labelInsets);//, new GridBagConstraints(
	  //hide the input keyboard unless the text is blank
	if(this.textTextField.getText().toString()!=null &&this.textTextField.getText().toString().length()>0)
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
       // 1, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
       // GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
	//  rootView.addView(this.fontNameLabel, labelInsets);//, new GridBagConstraints(
    //    0, 1, 1, 1, 0, 0, labelAlignment,
     //   GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
 /*   Dimension preferredSize = this.fontNameComboBox.getPreferredSize();
    preferredSize.width = Math.min(preferredSize.width, this.textTextField.getPreferredSize().width);
    this.fontNameComboBox.setPreferredSize(preferredSize);
    nameAndStylePanel.add(this.fontNameComboBox, new GridBagConstraints(
        1, 1, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));*/
	  rootView.addView(this.fontSizeLabel, labelInsets);//, new GridBagConstraints(
       // 0, 2, 1, 1, 0, 0, labelAlignment,
       // GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
	  rootView.addView(this.fontSizeSpinner, labelInsets);//, new GridBagConstraints(
       // 1, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
       // GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 0));
	  rootView.addView(this.colorLabel, labelInsets);//, new GridBagConstraints(
       // 2, 2, 1, 1, 0, 0, labelAlignment,
       // GridBagConstraints.NONE, new Insets(0, 10, 0, 5), 0, 0));
	  rootView.addView(this.colorButton, labelInsets);//, new GridBagConstraints(
   //     3, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
   //     GridBagConstraints.NONE, new Insets(0, 0, 0, OperatingSystem.isMacOSX()  ? 6  : 0), 0, 0));
   // int rowGap = OperatingSystem.isMacOSXLeopardOrSuperior() ? 0 : 5;
   // add(nameAndStylePanel, new GridBagConstraints(
    //    0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
    //    GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));

	  JLabel rendering3DPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "rendering3DPanel.title"));
	  rootView.addView(rendering3DPanel, rowInsets);

    //JPanel rendering3DPanel = SwingTools.createTitledPanel(
   //     preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "rendering3DPanel.title"));

	  View div = new View(activity);
	  div.setMinimumHeight(1);
	  div.setBackgroundColor(Color.GRAY);
	  rootView.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    rootView.addView(this.visibleIn3DViewCheckBox, labelInsets);//, new GridBagConstraints(
        //0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
       // GridBagConstraints.NONE, new Insets(0, OperatingSystem.isMacOSX() ? -8 : 0, 5, 0), 0, 0));
	  rootView.addView(this.pitchLabel, labelInsets);//, new GridBagConstraints(
       // 0, 1, 1, 1, 0, 0, labelAlignment,
       // GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
	  rootView.addView(this.pitch0DegreeRadioButton, labelInsets);//, new GridBagConstraints(
       // 1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
       // GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
	  rootView.addView(this.pitch90DegreeRadioButton, labelInsets);//, new GridBagConstraints(
       // 2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
       // GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
	  rootView.addView(this.elevationLabel, labelInsets);//, new GridBagConstraints(
       // 0, 3, 1, 1, 0, 0, labelAlignment,
       // GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
	  rootView.addView(this.elevationSpinner, labelInsets);//, new GridBagConstraints(
       // 1, 3, 2, 1, 1, 0, GridBagConstraints.LINE_START,
       // GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
   // add(rendering3DPanel, new GridBagConstraints(
   //     0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
   //     GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

	  this.setTitle(dialogTitle);
	  rootView.addView(closeButton, labelInsets);
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




	/**
	 * Nullable spinner model displaying length values matching preferences unit.
	 */
	public static class SpinnerLengthModel extends SpinnerNumberModel
	{
		private final UserPreferences preferences;

		/**
		 * Creates a model managing lengths between the given <code>minimum</code> and <code>maximum</code> values in centimeter.
		 */
		public SpinnerLengthModel(UserPreferences preferences, float minimum, float maximum)
		{
			this(preferences, minimum, minimum, maximum);
		}

		/**
		 * Creates a model managing lengths between the given <code>minimum</code> and <code>maximum</code> values in centimeter.
		 */
		public SpinnerLengthModel(UserPreferences preferences, float value, float minimum, float maximum)
		{
			super(value, minimum, maximum,
					preferences.getLengthUnit() == LengthUnit.INCH
							|| preferences.getLengthUnit() == LengthUnit.INCH_DECIMALS
							? LengthUnit.inchToCentimeter(0.125f) : 0.5f);
			this.preferences = preferences;
		}

		/**
		 * Returns the displayed value in centimeter.
		 */
		public Float getLength()
		{
			return Float.valueOf(((Number) getValue()).floatValue());
		}

		public void setNullable(boolean nullable)
		{
			//ignored this.nullable = nullable;
		}
	}


}
