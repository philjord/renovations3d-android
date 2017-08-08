package com.mindblowing.j3d.utils;

import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;

/**
 * Created by phil on 8/7/2017.
 */

public class NoNPEGLWindow extends GLWindow
{
	private ExceptionCallBack exceptionCallBack;
	public NoNPEGLWindow(Window var1, ExceptionCallBack exceptionCallBack)
	{
		super(var1);
		this.exceptionCallBack = exceptionCallBack;
	}

	public void display() {
		try
		{
			super.display();
		}
		catch(Exception e)
		{
			exceptionCallBack.handleException(e);
		}
	}

	public interface ExceptionCallBack
	{
		void handleException(Exception e);
	}
}