package com.mindblowing.swingish;

import java.util.ArrayList;
import java.util.TimerTask;

public class ActioningTimerTask extends TimerTask {
	private ArrayList<ActionListener> listenerList = new ArrayList<ActionListener>();
	private String actionCommand;

	public ActioningTimerTask(ActionListener actionListener) {
		addActionListener(actionListener);
	}
	public void setActionCommand(String command) {
		this.actionCommand = command;
	}
	public String getActionCommand() {
		return actionCommand;
	}
	@Override
	public void run() {
		fireActionPerformed(new ActionListener.ActionEvent(ActioningTimerTask.this, 0, getActionCommand(),
						System.currentTimeMillis(),
						0));
	}

	protected void fireActionPerformed(ActionListener.ActionEvent e) {
		// Guaranteed to return a non-null array
		ActionListener[] listeners = getActionListeners();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i=listeners.length-1; i>=0; i-=1) {
				((ActionListener)listeners[i]).actionPerformed(e);
		}
	}


	public ActionListener[] getActionListeners() {
		return listenerList.toArray(new ActionListener[0]);
	}

	public void addActionListener(ActionListener listener) {
		listenerList.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listenerList.remove(listener);
	}
}
