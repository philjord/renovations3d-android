/*
 * ThreadedTaskPanel.java 29 sept. 2008
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskView;

import java.util.Timer;
import java.util.TimerTask;

import javaawt.EventQueue;
import javaawt.VMEventQueue;


/**
 * A MVC view of a threaded task.
 * @author Emmanuel Puybaret
 */
public class ThreadedTaskPanel extends LinearLayout//extends JPanel
		implements ThreadedTaskView {
  private final UserPreferences        preferences;
  private final ThreadedTaskController controller;
  private JLabel taskLabel;
 // private JProgressBar                 taskProgressBar;
  private ProgressBar taskProgressBar;
 // private JDialog                      dialog;
  private AlertDialog dialog;
  private boolean                      taskRunning;
	private Context context;

  public ThreadedTaskPanel(String taskMessage, 
                           UserPreferences preferences, 
                           ThreadedTaskController controller,
  							Context context) {
    //super(new BorderLayout(5, 5));
	  super(context);
    this.preferences = preferences;
    this.controller = controller;
	  this.context = context;
    createComponents(taskMessage);
    layoutComponents();
  }

	/**
	 * Creates and initializes components.
	 */
	private void createComponents(String taskMessage) {
		this.taskLabel = new JLabel(context, taskMessage);
		this.taskProgressBar = new ProgressBar(context);
		this.taskProgressBar.setIndeterminate(true);
	}

	/**
	 * Layouts panel components in panel with their labels.
	 */
	private void layoutComponents() {
		//add(this.taskLabel, BorderLayout.NORTH);
		//add(this.taskProgressBar, BorderLayout.SOUTH);

		setOrientation(LinearLayout.VERTICAL);
		setPadding(10,10,10,10);

		this.addView(this.taskLabel);
		this.addView(this.taskProgressBar);
	}


	/**
   * Sets the status of the progress bar shown by this panel as indeterminate.
   * This method may be called from an other thread than EDT.  
   */
  public void setIndeterminateProgress() {
    if (EventQueue.isDispatchThread()) {
      this.taskProgressBar.setIndeterminate(true);
    } else {
      // Ensure modifications are done in EDT
      invokeLater(new Runnable() {
          public void run() {
            setIndeterminateProgress();
          }
        });
    }
  }
  
  /**
   * Sets the current value of the progress bar shown by this panel.  
   * This method may be called from an other thread than EDT.  
   */
  public void setProgress(final int value, 
                          final int minimum, 
                          final int maximum) {
    if (VMEventQueue.isDispatchThread()) {
      this.taskProgressBar.setIndeterminate(false);
      /*this.taskProgressBar.setValue(value);
      this.taskProgressBar.setMinimum(minimum);
      this.taskProgressBar.setMaximum(maximum);*/
		// no set set
		if(minimum!=0)
			throw new UnsupportedOperationException("no non 0 min!");
		this.taskProgressBar.setProgress(value);
		this.taskProgressBar.setMax(maximum);
    } else {
      // Ensure modifications are done in EDT
      invokeLater(new Runnable() {
          public void run() {
            setProgress(value, minimum, maximum);
          }
        });
    }
  }
  
  /**
   * Executes <code>runnable</code> asynchronously in the Event Dispatch Thread.
   * Caution !!! This method may be called from an other thread than EDT.  
   */
  public void invokeLater(Runnable runnable) {
    VMEventQueue.invokeLater(runnable);
  }

  /**
   * Sets the running status of the threaded task. 
   * If <code>taskRunning</code> is <code>true</code>, a waiting dialog will be shown.
   */


  public void setTaskRunning(boolean taskRunning, com.eteks.sweethome3d.viewcontroller.View executingView) {
    this.taskRunning = taskRunning;

	  if (taskRunning && this.dialog == null) {
		  String dialogTitle = this.preferences.getLocalizedString(
				  com.eteks.sweethome3d.android_props.ThreadedTaskPanel.class, "threadedTask.title");
		  final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		  builder.setTitle(dialogTitle);
		  builder.setView(this);
		  builder.setPositiveButton(this.preferences.getLocalizedString(
				  com.eteks.sweethome3d.android_props.ThreadedTaskPanel.class, "cancelButton.text"),
				  new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface d, int w) {
						  switch (w)
						  {
							  case DialogInterface.BUTTON_POSITIVE:
								  if (ThreadedTaskPanel.this.taskRunning)
								  {
									  dialog = null;
									  controller.cancelTask();
								  }
								  break;
						  }}});
		  builder.setCancelable(false);
		  dialog = builder.create();

		  // Wait 200 ms before showing dialog to avoid displaying it
		  // when the task doesn't last so long
		  final Timer timer = new Timer(dialogTitle, true);
		  timer.schedule(new TimerTask (){
			  public void run() {
				  timer.cancel();
				  timer.purge();
				  if (controller.isTaskRunning()) {
					  // Ensure modifications are done in EDT
					  invokeLater(new Runnable() {
						  public void run() {
							  // very rare crash if renovs is dispaose by now
							  //https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/2fea2349?duration=2592000000
							  // can't reporduce by give this a whorl
							  //http://stackoverflow.com/questions/7811993/error-binderproxy45d459c0-is-not-valid-is-your-activity-running
							 if(!(context instanceof Activity) || !((Activity) context).isFinishing())
							  {
								  dialog.show();
							  }
					  }});
					  // moved up to cancel button
					 /* if (ThreadedTaskPanel.this.taskRunning
							  && (cancelButton == optionPane.getValue()
							  || new Integer(JOptionPane.CLOSED_OPTION).equals(optionPane.getValue()))) {
						  dialog = null;
						  controller.cancelTask();
					  }*/
				  }
			  }
		  }, 200);

	  } else if (!taskRunning && this.dialog != null && !((Activity) context).isFinishing() ) {
		  this.dialog.dismiss();
	  }

/*    if (taskRunning && this.dialog == null) {
      String dialogTitle = this.preferences.getLocalizedString(
          ThreadedTaskPanel.class, "threadedTask.title");
      final JButton cancelButton = new JButton(this.preferences.getLocalizedString(
          ThreadedTaskPanel.class, "cancelButton.text"));
      
      final JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE,
          JOptionPane.DEFAULT_OPTION, null, new Object [] {cancelButton});
      cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            optionPane.setValue(cancelButton);
          }
        });
      this.dialog = optionPane.createDialog(SwingUtilities.getRootPane((JComponent)executingView), dialogTitle);
      
      // Wait 200 ms before showing dialog to avoid displaying it 
      // when the task doesn't last so long
      new Timer(200, new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            ((Timer)ev.getSource()).stop();
            if (controller.isTaskRunning()) {
              dialog.setVisible(true);
              
              dialog.dispose();
              if (ThreadedTaskPanel.this.taskRunning 
                  && (cancelButton == optionPane.getValue() 
                      || new Integer(JOptionPane.CLOSED_OPTION).equals(optionPane.getValue()))) {
                dialog = null;
                controller.cancelTask();
              }
            }
          }
        }).start();
    } else if (!taskRunning && this.dialog != null) {
      this.dialog.setVisible(false);
    }*/
  }
}
