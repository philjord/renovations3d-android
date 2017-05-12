/*
 * WizardPane.java 7 juin 07
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
import android.view.Window;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eteks.renovations3d.android.swingish.ActionListener;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JPanel;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.WizardController;
import com.mindblowing.renovations3d.R;


/**
 * Wizard pane. 
 * @author Emmanuel Puybaret
 */
public class WizardPane extends AndroidDialogView implements DialogView {
  private final UserPreferences  preferences;
  private final WizardController controller;
  private JButton          backOptionButton;
  private JButton          nextFinishOptionButton;
  private String           defaultTitle;
  //private JDialog          dialog;

	private JPanel messagePanel;

  /**
   * Creates a wizard view controlled by <code>controller</code>.
   */
  public WizardPane(UserPreferences preferences,
                    final WizardController controller, Activity activity)
  {
	  super(preferences, activity, R.layout.dialog_wizard_pane, true);
    this.preferences = preferences;
    this.controller = controller;
    this.defaultTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WizardPane.class, "wizard.title");

    //setMessage(new JPanel(new BorderLayout(10, 0)));

    createOptionButtons(preferences, controller);    
    //setOptionType(DEFAULT_OPTION);
    String cancelOption = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WizardPane.class, "cancelOption");
    // Make backOptionButton appear at left of nextFinishOptionButton

	  //PJ close becomes cancel
	  this.closeButton.setText(cancelOption);
	  swapOut(closeButton, R.id.wizard_pane_cancel);

   /* if (UIManager.getBoolean("OptionPane.isYesLast")
        || OperatingSystem.isMacOSX()) {
      setOptions(new Object [] {this.nextFinishOptionButton, this.backOptionButton, cancelOption});      
    } else {
      setOptions(new Object [] {this.backOptionButton, this.nextFinishOptionButton, cancelOption});      
    }
    setInitialValue(this.nextFinishOptionButton);*/
    
    // Update wizard pane content and icon
    updateStepView(controller);
    controller.addPropertyChangeListener(WizardController.Property.STEP_VIEW, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateStepView(controller);
          }
        });
    
    updateStepIcon(controller);
    controller.addPropertyChangeListener(WizardController.Property.STEP_ICON, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateStepIcon(controller);
          }
        });
  }

  private void createOptionButtons(UserPreferences preferences, 
                                   final WizardController controller) {
    this.backOptionButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WizardPane.class, "backOptionButton.text"));
    this.backOptionButton.setEnabled(controller.isBackStepEnabled());
    controller.addPropertyChangeListener(WizardController.Property.BACK_STEP_ENABLED, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            backOptionButton.setEnabled(controller.isBackStepEnabled());
          }
        });

    this.nextFinishOptionButton = new JButton(activity, "");
    this.nextFinishOptionButton.setEnabled(controller.isNextStepEnabled());
    controller.addPropertyChangeListener(WizardController.Property.NEXT_STEP_ENABLED, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nextFinishOptionButton.setEnabled(controller.isNextStepEnabled());
          }
        });
    
    // Update nextFinishButton text and mnemonic
    updateNextFinishOptionButton(controller);
    controller.addPropertyChangeListener(WizardController.Property.LAST_STEP, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateNextFinishOptionButton(controller);
          }
        });
    
    // Add action listeners
    this.backOptionButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.goBackToPreviousStep();
        }
      });
    
    this.nextFinishOptionButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (controller.isLastStep()) {
            controller.finish();
		//PJ this setValue system doesn't seem to be required
			  WizardPane.this.dismiss();
           /* setValue(nextFinishOptionButton);
            if (dialog != null) {
              dialog.setVisible(false);
            }*/
          } else {
            controller.goToNextStep();
          }
        }
      });


	  //PJPJ add them to the gui
	  swapOut(backOptionButton, R.id.wizard_pane_back);
	  swapOut(nextFinishOptionButton, R.id.wizard_pane_next);
  }

  /**
   * Sets whether this wizard view is displaying the last step or not.
   */
  private void updateNextFinishOptionButton(WizardController controller) {
    this.nextFinishOptionButton.setText(SwingTools.getLocalizedLabelText(this.preferences, com.eteks.sweethome3d.android_props.WizardPane.class,
        controller.isLastStep() 
            ? "finishOptionButton.text" 
            : "nextOptionButton.text"));

  }
  
  /**
   * Updates the step view displayed by this wizard view.
   */
  private void updateStepView(WizardController controller)
  {
	  // Clean previous step view
	  if( messagePanel != null)
	  {
		  messagePanel.setVisibility(android.view.View.GONE);
	  }

	  // Add new step view
	  messagePanel = (JPanel)controller.getStepView();
	  if (messagePanel  != null) {
		  swapOut(messagePanel, R.id.wizard_pane_message);
		  messagePanel.setId(R.id.wizard_pane_message);
		  messagePanel.setVisibility(android.view.View.VISIBLE);
	  }
	  this.inflatedView.postInvalidate();


   /* JPanel messagePanel = (JPanel)getMessage();

    // Clean previous step view
    Component previousStepView = ((BorderLayout)messagePanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
    if (previousStepView != null) {
      messagePanel.remove(previousStepView);
    }
    // Add new step view
    View stepView = controller.getStepView();
    if (stepView  != null) {
      messagePanel.add((JComponent)stepView, BorderLayout.CENTER);
    } 
    if (this.dialog != null && !this.controller.isResizable()) {
      this.dialog.pack();
    }*/
  }

  /**
   * Updates the step icon displayed by this wizard view.
   */
  private void updateStepIcon(WizardController controller) {
  /*  JPanel messagePanel = (JPanel)getMessage();
    Component previousStepIconLabel = ((BorderLayout)messagePanel.getLayout()).getLayoutComponent(BorderLayout.WEST);
    if (previousStepIconLabel != null) {
      // Clean previous icon label
      messagePanel.remove(previousStepIconLabel);
    }
    // Add new icon
    URL stepIcon = controller.getStepIcon();
    if (stepIcon != null) {
      Color backgroundColor1 = new Color(163, 168, 226);
      Color backgroundColor2 = new Color(80, 86, 158);
      try {
        // Read gradient colors used to paint icon background 
        String [] stepIconBackgroundColors = this.preferences.getLocalizedString(
            WizardPane.class, "stepIconBackgroundColors").trim().split(" ");
        backgroundColor1 = new Color(Integer.decode(stepIconBackgroundColors [0]));
        if (stepIconBackgroundColors.length == 1) {
          backgroundColor2 = backgroundColor1;  
        } else if (stepIconBackgroundColors.length == 2) {
          backgroundColor2 = new Color(Integer.decode(stepIconBackgroundColors [1]));;
        }
      } catch (IllegalArgumentException ex) {
        // Don't change colors if stepIconBackgroundColors doesn't exist or if colors are wrongly formatted
      }
         
      final Color gradientColor1 = backgroundColor1;
      final Color gradientColor2 = backgroundColor2;
      JLabel iconLabel = new JLabel(new ImageIcon(stepIcon)) {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D)g;
             // Paint a blue gradient behind icon
            g2D.setPaint(new GradientPaint(0, 0, gradientColor1, 
                                           0, getHeight(), gradientColor2));
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
          }
        };
      // Use a bevel border 1 pixel wide
      iconLabel.setBorder(new BevelBorder(BevelBorder.LOWERED) {
          @Override
          public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
          }
          
          @Override
          protected void paintLoweredBevel(Component c, Graphics g, int x, int y,
                                           int width, int height)  {
            Color oldColor = g.getColor();
            g.translate(x, y);

            g.setColor(getShadowInnerColor(c));
            g.drawLine(0, 0, 0, height - 1);
            g.drawLine(0, 0, width - 1, 0);

            g.setColor(getHighlightInnerColor(c));
            g.drawLine(0, height - 1, width - 1, height - 1);
            g.drawLine(width - 1, 1, width - 1, height - 2);

            g.translate(-x, -y);
            g.setColor(oldColor);
          }
        });
      // We don't use JOptionPane icon to let icon background spread in all height 
      messagePanel.add(iconLabel, BorderLayout.LINE_START);
    } */
  }
  
  /**
   * Displays this wizard view in a modal dialog.
   */
  public void displayView(View parentView) {
    /*this.dialog = createDialog(SwingUtilities.getRootPane((JComponent)parentView),
        this.controller.getTitle() != null 
            ? this.controller.getTitle() 
            : this.defaultTitle);*/

	//PJ this title may not be displayed on small screens
	 // see above
	    this.setTitle(this.controller.getTitle() != null
			  ? this.controller.getTitle()
			  : this.defaultTitle);
    this.controller.addPropertyChangeListener(WizardController.Property.TITLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
			  WizardPane.this.setTitle(controller.getTitle() != null
                                ? controller.getTitle() 
                                : defaultTitle);
          }
        });

    
 /*   this.dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    this.dialog.setResizable(this.controller.isResizable());
    this.controller.addPropertyChangeListener(WizardController.Property.RESIZABLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            dialog.setResizable(controller.isResizable());
          }
        });
    
    // Pack again because resize decorations may have changed dialog preferred size
    this.dialog.pack();
    this.dialog.setMinimumSize(getSize());
    this.dialog.setVisible(true);
    this.dialog.dispose();*/

	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  // nothing perhaps?
		  }
	  });

	  // this can be recalled
	  if(!this.isShowing())
	  	this.show();
  }
}
