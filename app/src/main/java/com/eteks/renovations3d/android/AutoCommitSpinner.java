/*
 * AutoSelectSpinner.java 10 sept. 2008
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

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.NumberPicker;


import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;



import java.text.Format;


/**
 * A spinner which commits its value during edition and selects 
 * the value displayed in its editor when it gains focus.
 * @author Emmanuel Puybaret
 */
public class AutoCommitSpinner extends NumberPicker
{
	private SpinnerNumberModel model;

  /**
   * Creates a spinner with a given <code>model</code>.
   */
  public AutoCommitSpinner(Context context, SpinnerNumberModel model) {
    this(context, model, null);
  }
  
  /**
   * Creates a spinner with a given <code>model</code> and <code>format</code>.
   */
  public AutoCommitSpinner(Context context, SpinnerNumberModel model2,
                           Format format) {
	  super(context);
	  this.model = model2;

	  float multiplier = 1 / model.getStepSize();
	  this.setMinValue((int)(model.getMinimum() * multiplier));
	  this.setMaxValue((int)(model.getMaximum() * multiplier));

	  setFormat(format);
	  resetDisplayValues();
	  this.setValue((int)(model.getValue() * multiplier));

	  //get rid of dman keybaord for now
	  EditText numberPickerChild = (EditText) getChildAt(0);
	  numberPickerChild.setFocusable(false);
	  numberPickerChild.setInputType(InputType.TYPE_NULL);


	  model.addChangeListener(new ChangeListener(){
		  @Override
		  public void stateChanged(ChangeEvent ev)
		  {
			  float multiplier = 1 / model.getStepSize();
			  setMinValue((int)(model.getMinimum() * multiplier));
			  setMaxValue((int)(model.getMaximum() * multiplier));
			  resetDisplayValues();
			  setValue((int)(model.getValue() * multiplier));
		  }
	  } );


	  //TODO: this spinner should set the value to teh model, so it can be gotten from there


    /*JComponent editor = getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
      final JFormattedTextField textField = ((JSpinner.DefaultEditor)editor).getTextField();
      SwingTools.addAutoSelectionOnFocusGain(textField);
      // Commit text during edition
      if (textField.getFormatterFactory() instanceof DefaultFormatterFactory) {
        DefaultFormatterFactory formatterFactory = (DefaultFormatterFactory)textField.getFormatterFactory();
        JFormattedTextField.AbstractFormatter defaultFormatter = formatterFactory.getDefaultFormatter();
        if (defaultFormatter instanceof DefaultFormatter) {
          ((DefaultFormatter)defaultFormatter).setCommitsOnValidEdit(true);
        }
        if (defaultFormatter instanceof NumberFormatter) {
          final NumberFormatter numberFormatter = (NumberFormatter)defaultFormatter;
          // Create a delegate of default formatter to change value returned by getFormat
          NumberFormatter editFormatter = new NumberFormatter() {
              private boolean keepFocusedTextUnchanged;

              {
                // Add a listener to spinner text field that keeps track when the user typed a character
                final KeyAdapter keyListener = new KeyAdapter() {
                    public void keyTyped(KeyEvent ev) {
                      // keyTyped isn't called for UP and DOWN keys of text field input map
                      keepFocusedTextUnchanged = true;
                    };
                  };
                textField.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent ev) {
                      textField.addKeyListener(keyListener);
                    }
                    
                    public void focusLost(FocusEvent ev) {
                      textField.removeKeyListener(keyListener);
                    };
                  });
              }
              
              @Override
              public Format getFormat() {
                Format format = super.getFormat();
                // Use a different format depending on whether the text field has focus or not
                if (textField.hasFocus() && format instanceof DecimalFormat) {
                  // No grouping when text field has focus 
                  DecimalFormat noGroupingFormat = (DecimalFormat)format.clone();
                  noGroupingFormat.setGroupingUsed(false);
                  return noGroupingFormat;
                } else {
                  return format;
                }
              }
            
              @SuppressWarnings({"rawtypes"})
              @Override
              public Comparable getMaximum() {
                return numberFormatter.getMaximum();
              }
              
              @SuppressWarnings({"rawtypes"})
              @Override
              public Comparable getMinimum() {
                return numberFormatter.getMinimum();
              }
              
              @SuppressWarnings({"rawtypes"})
              @Override
              public void setMaximum(Comparable maximum) {
                numberFormatter.setMaximum(maximum);
              }
              
              @SuppressWarnings({"rawtypes"})
              @Override
              public void setMinimum(Comparable minimum) {
                numberFormatter.setMinimum(minimum);
              }
              
              @Override
              public Class<?> getValueClass() {
                return numberFormatter.getValueClass();
              }
              
              @Override
              public String valueToString(Object value) throws ParseException {
                if (textField.hasFocus()
                    && this.keepFocusedTextUnchanged) {
                  this.keepFocusedTextUnchanged = false;
                  return textField.getText();
                } else {
                  return super.valueToString(value);
                }
              }
            };
          editFormatter.setCommitsOnValidEdit(true);
          textField.setFormatterFactory(new DefaultFormatterFactory(editFormatter));
        }
      }
    }
    
    if (format != null) {
      setFormat(format);
    }*/
  }

	private void resetDisplayValues()
	{
		String[] valueDisplays = new String[(int)(((model.getMaximum() - model.getMinimum())) / model.getStepSize())+1];
		int idx = 0;
		for(float i = model.getMinimum(); i <= model.getMaximum(); i += model.getStepSize() )
		{
			float val = i - model.getMinimum();
			String strVal;
			if (currentFormat != null)
				strVal = currentFormat.format(val);
			else
				strVal = "" + val;

			valueDisplays[idx] = strVal;
			idx++;
		}
		this.setDisplayedValues(valueDisplays);
	}

  /**
   * Sets the format used to display the value of this spinner.
   */
  private Format currentFormat;
  public void setFormat(Format format) {

	  this.currentFormat = format;
	  resetDisplayValues();

   /* JComponent editor = getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
      JFormattedTextField textField = ((JSpinner.DefaultEditor)editor).getTextField();
      AbstractFormatter formatter = textField.getFormatter();
      if (formatter instanceof NumberFormatter) {
        ((NumberFormatter)formatter).setFormat(format);
        fireStateChanged();
      }
    }*/
  }


}
