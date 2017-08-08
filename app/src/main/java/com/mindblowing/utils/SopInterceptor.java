package com.mindblowing.utils;

import android.util.Log;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by phil on 3/7/2016.
 */
public class SopInterceptor extends PrintStream
{
	private String tag;

	public SopInterceptor(OutputStream out, String tag)
	{
		super(out, true);
		this.tag = tag;
	}

	@Override
	public void print(String s)
	{
		Log.w(tag, s);
	}
}
