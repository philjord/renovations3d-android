package com.eteks.renovations3d.j3d.mouseover;

import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Locale;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.utils.pickfast.PickCanvas;

public abstract class MouseOverHandler
{
	public static float MAX_MOUSE_RAY_DIST = 50f;// max pick dist 100 meters?

	protected Canvas3D canvas3D;

	protected PickCanvas pickCanvas;

	public void setConfig(Canvas3D canvas, Locale locale)
	{
		// de-register on the old canvas
		if (this.canvas3D != null)
		{

		}

		// set up new canvas
		this.canvas3D = canvas;
		if (this.canvas3D != null)
		{
			pickCanvas = new PickCanvas(canvas3D, locale);
			pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
			pickCanvas.setFlags(PickInfo.NODE | PickInfo.SCENEGRAPHPATH);
			pickCanvas.setTolerance(0.0f);// make sure it's a ray not a cone
		}
	}

}
