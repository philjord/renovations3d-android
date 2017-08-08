package com.eteks.renovations3d.utils;

import com.eteks.renovations3d.Renovations3DActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;

/**
 * Created by phil on 8/7/2017.
 */

public class NoNPEGLWindow extends GLWindow
{
	public NoNPEGLWindow(Window var1)
	{
		super(var1);
	}

	public void display() {
		try
		{
			super.display();
		}catch(Exception e)
		{
			e.printStackTrace();
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NoNPEGLWindow display exception", e.getMessage());
			//TODO: what is the real solution here?
		}
	}
}