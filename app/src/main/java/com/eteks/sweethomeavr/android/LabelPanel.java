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
package com.eteks.sweethomeavr.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.sweethome3d.viewcontroller.VCView;
import com.eteks.sweethomeavr.android.swingish.ButtonGroup;

import com.eteks.sweethomeavr.android.swingish.SpinnerNumberModel;



/**
 * Label editing panel.
 * @author Emmanuel Puybaret
 */
public class LabelPanel extends Dialog implements DialogView {
  private final boolean         labelModification;
  private final LabelController controller;
  private TextView textLabel;
  private EditText textTextField;
  //private TextView                fontNameLabel;//there are only 4 fonts on android!http://stackoverflow.com/questions/12128331/how-to-change-fontfamily-of-textview-in-android/13329907#13329907
  //private FontNameComboBox      fontNameComboBox;
  private TextView                fontSizeLabel;
  private AutoCommitSpinner       fontSizeSpinner;
  private TextView                colorLabel;
  //private ColorButton           colorButton;
  private CheckBox visibleIn3DViewCheckBox;
  private TextView                pitchLabel;
  private RadioButton          pitch0DegreeRadioButton;
  private RadioButton pitch90DegreeRadioButton;
  private TextView                elevationLabel;
  private AutoCommitSpinner elevationSpinner;
  private String                dialogTitle;

	private Activity activity;
	private LinearLayout rootView;
	private Button closeButton;

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
	  super(activity);
    this.labelModification = modification;
    this.controller = controller;
	  this.activity = activity;
	  this.rootView = new LinearLayout(activity);
	  rootView.setOrientation(LinearLayout.VERTICAL);
	  ScrollView sv = new ScrollView(activity);
	  sv.addView(rootView);
	  this.setContentView(sv);
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
    this.textLabel = new TextView(activity);textLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "textLabel.text"));
    this.textTextField = new EditText(activity);textTextField.setText(controller.getText());//, 20, preferences.getAutoCompletionStrings("LabelText"));
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
    this.fontSizeLabel = new TextView(activity);fontSizeLabel.setText(SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.LabelPanel.class,
        "fontSizeLabel.text", unitName));
    final SpinnerLengthModel fontSizeSpinnerModel = new SpinnerLengthModel(
        preferences, 5, 999);
    this.fontSizeSpinner = new AutoCommitSpinner(activity, fontSizeSpinnerModel);
    final PropertyChangeListener fontSizeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Float fontSize = controller.getFontSize();
          //fontSizeSpinnerModel.setNullable(fontSize == null);
          fontSizeSpinner.setValue((int)(fontSize  / fontSizeSpinnerModel.getStepSize()));
        }
      };
    fontSizeChangeListener.propertyChange(null);
    controller.addPropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
    fontSizeSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
	{
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
          controller.removePropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
			// TODO: if the model was asked for the value it scould do this multiply happily and even return floats and do lots of nice ness
          controller.setFontSize(fontSizeSpinner.getValue() * fontSizeSpinnerModel.getStepSize());
          controller.addPropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
        }
      });

    // Create color label and button bound to controller COLOR property
    this.colorLabel = new TextView(activity);colorLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "colorLabel.text"));
    
/*    this.colorButton = new ColorButton(preferences);
    if (OperatingSystem.isMacOSX()) {
      this.colorButton.putClientProperty("JButton.buttonType", "segmented");
      this.colorButton.putClientProperty("JButton.segmentPosition", "only");
    }
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
      });*/

    // Create pitch components bound to PITCH controller property
    final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          update3DViewComponents(controller);
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
    this.visibleIn3DViewCheckBox = new CheckBox(activity);visibleIn3DViewCheckBox.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "visibleIn3DViewCheckBox.text"));
    if (controller.isPitchEnabled() != null) {
      this.visibleIn3DViewCheckBox.setChecked(controller.isPitchEnabled());
    } else {
      //this.visibleIn3DViewCheckBox.setNullable(true);
      this.visibleIn3DViewCheckBox.setChecked(false);
    }
    this.visibleIn3DViewCheckBox.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
          controller.removePropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
         // if (visibleIn3DViewCheckBox.isNullable()) {
         //   visibleIn3DViewCheckBox.setNullable(false);
         // }
          if (Boolean.FALSE.equals(visibleIn3DViewCheckBox.isChecked())) {
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
    this.pitchLabel = new TextView(activity);pitchLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "pitchLabel.text"));
    this.pitch0DegreeRadioButton = new RadioButton(activity);pitch0DegreeRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
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
    this.pitch90DegreeRadioButton = new RadioButton(activity);pitch90DegreeRadioButton.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "pitch90DegreeRadioButton.text"));
    this.pitch90DegreeRadioButton.setOnClickListener(pitchRadioButtonsItemListener);
    ButtonGroup pitchGroup = new ButtonGroup();
    pitchGroup.add(this.pitch0DegreeRadioButton);
    pitchGroup.add(this.pitch90DegreeRadioButton);
    
    // Create elevation label and its spinner bound to ELEVATION controller property
    this.elevationLabel = new TextView(activity);elevationLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.LabelPanel.class, "elevationLabel.text", unitName));
    final SpinnerLengthModel elevationSpinnerModel = new SpinnerLengthModel(
        preferences, 0f, preferences.getLengthUnit().getMaximumElevation()/20);//PJPJP 10k is too much
    this.elevationSpinner = new AutoCommitSpinner(activity, elevationSpinnerModel);
    //elevationSpinnerModel.setNullable(controller.getElevation() == null);
    elevationSpinner.setValue((int)(controller.getElevation() / elevationSpinnerModel.getStepSize()));
    final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          //elevationSpinnerModel.setNullable(ev.getNewValue() == null);
          elevationSpinner.setValue((int)((Float)ev.getNewValue() / elevationSpinnerModel.getStepSize()));
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
    elevationSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
	{
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
          controller.removePropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
          controller.setElevation(elevationSpinner.getValue() * elevationSpinnerModel.getStepSize());
          controller.addPropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
        }
      });

    update3DViewComponents(controller);
    
    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class,
        modification 
            ? "labelModification.title"
            : "labelCreation.title");

	  this.closeButton = new Button(activity);closeButton.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.HomePane.class, "CLOSE.Name"));
	  closeButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View view)
		  {
			  LabelPanel.this.dismiss();
		  }
	  });
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
	  Resources r = activity.getResources();
	  int px5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
	  int px10dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
	  int px15dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());
	  int px20dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

	  rootView.setPadding(px10dp,px10dp,px10dp,px10dp);

	  LinearLayout.LayoutParams  labelInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  labelInsets.setMargins(px10dp,px10dp,px15dp,px15dp);
	  LinearLayout.LayoutParams  labelInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  labelInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px15dp);
	  LinearLayout.LayoutParams  rightComponentInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rightComponentInsets.setMargins(px10dp,px10dp,px15dp,px10dp);
	  LinearLayout.LayoutParams  rightComponentInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rightComponentInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px10dp);
    
    //JPanel nameAndStylePanel = SwingTools.createTitledPanel(
    //    preferences.getLocalizedString(com.eteks.sweethome3d.android_props.LabelPanel.class, "textAndStylePanel.title"));
	  rootView.addView(this.textLabel, labelInsets);//, new GridBagConstraints(
        //0, 0, 1, 1, 0, 0, labelAlignment,
       // GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
	  rootView.addView(this.textTextField, labelInsets);//, new GridBagConstraints(
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
/*    nameAndStylePanel.add(this.colorButton, new GridBagConstraints(
        3, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, OperatingSystem.isMacOSX()  ? 6  : 0), 0, 0));*/
   // int rowGap = OperatingSystem.isMacOSXLeopardOrSuperior() ? 0 : 5;
   // add(nameAndStylePanel, new GridBagConstraints(
    //    0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
    //    GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));

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
  public void displayView(VCView parentView) {
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
	}


}
