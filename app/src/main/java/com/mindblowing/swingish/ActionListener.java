package com.mindblowing.swingish;

/**
 * Created by phil on 2/7/2017.
 */

public interface ActionListener {
	void actionPerformed(ActionListener.ActionEvent ev);
	class ActionEvent {
		private String command;
		private Object source;

		public ActionEvent()	{

		}
		public ActionEvent( Object source, int id, String command, long time, int x)	{
			this.command = command;
			this.source = source;
		}
		public String getActionCommand() {
			return command;
		}

		public Object getSource() {
			return source;
		}
	}
}
