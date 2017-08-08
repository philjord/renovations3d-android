package com.mindblowing.j3d.utils;

import com.jogamp.graph.font.FontFactory;
import com.mindblowing.hudbasics.graph.demos.ui.Label;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnElapsedTime;
import org.jogamp.vecmath.Point3d;

import java.io.IOException;
import java.util.Iterator;


public abstract class InfoText3D
{
	public static BoundingSphere defaultBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY);

	public static int TIME_SAMPLE = 100;
	private BranchGroup behaviorBranchGroup;
	private InfoText3D.TimeBehavior timeBehavior;
	private Label fpsLabel;

	public InfoText3D()
	{
		this.behaviorBranchGroup = new BranchGroup();
		this.timeBehavior = new InfoText3D.TimeBehavior();
		this.timeBehavior.setSchedulingBounds(defaultBounds);
		this.behaviorBranchGroup.addChild(this.timeBehavior);
	}

	public void addToCanvas(Canvas3D2D canvas3d2d)
	{
		float pixelSizeFPS = 0.00003F * (float) canvas3d2d.getGLWindow().getSurfaceHeight();
		try
		{
			this.fpsLabel = new Label(canvas3d2d.getVertexFactory(), 0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
			canvas3d2d.addUIShape(fpsLabel);
			this.fpsLabel.setEnabled(true);
			this.fpsLabel.translate(-0.98F, 0.9F, 0.0F);
			this.fpsLabel.setColor(1.0F, 1.0F, 0.0F, 1.0F);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public BranchGroup getBehaviorBranchGroup()
	{
		return this.behaviorBranchGroup;
	}

	protected abstract String getText();


	private class TimeBehavior extends Behavior
	{
		private WakeupOnElapsedTime wakeUp;

		private TimeBehavior()
		{
			this.wakeUp = new WakeupOnElapsedTime((long) InfoText3D.TIME_SAMPLE);
		}

		public void initialize()
		{
			this.wakeupOn(this.wakeUp);
		}

		public void processStimulus(Iterator<WakeupCriterion> critera)
		{
			String newText = getText();
			if (!newText.equals(fpsLabel.getText()))
				InfoText3D.this.fpsLabel.setText(newText);
			this.wakeupOn(this.wakeUp);
		}

	}
}


