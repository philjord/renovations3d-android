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


import com.mindblowing.swingish.JSpinnerJogDial;
import com.mindblowing.swingish.SpinnerNumberModel;



import java.text.Format;


/**
 * A spinner which commits its value during edition and selects 
 * the value displayed in its editor when it gains focus.
 * @author Emmanuel Puybaret
 */
public class AutoCommitSpinnerJogDial extends JSpinnerJogDial
{
  /**
   * Creates a spinner with a given <code>model</code>.
   */
  public AutoCommitSpinnerJogDial(Context context, SpinnerNumberModel model) {
    this(context, model, null);
  }
  
  /**
   * Creates a spinner with a given <code>model</code> and <code>format</code>.
   */
  public AutoCommitSpinnerJogDial(Context context, SpinnerNumberModel model,
								  Format format) {
	super(context, model, format);

	  //TODO: this has no more function a than a spinner? what's autocommit mean?

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




}
