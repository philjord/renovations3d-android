package com.eteks.sweethomeavr.utils;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnElapsedFrames;
import org.jogamp.java3d.WakeupOnElapsedTime;
import org.jogamp.vecmath.Point3d;

import java.util.Iterator;

public class FPSSopCounter
{
	public static int FRAME_SAMPLE = 5;
	public static int TIME_SAMPLE = 500;
	private long currtime;
	private long lasttime;
	private long deltatime;
	private BranchGroup behaviorBranchGroup;
	private FPSSopCounter.FramesBehavior framesBehavior;
	private FPSSopCounter.TimeBehavior timeBehavior;

	private int numOfFrames;
	private long timeOfFrames;

	public FPSSopCounter()
	{
		this.currtime = 0L;
		this.lasttime = 0L;
		this.behaviorBranchGroup = new BranchGroup();
		this.framesBehavior = new FPSSopCounter.FramesBehavior();
		this.timeBehavior = new FPSSopCounter.TimeBehavior();
		this.numOfFrames = 0;
		this.timeOfFrames = 0L;
		this.framesBehavior.setSchedulingBounds(new BoundingSphere( new Point3d(), Double.POSITIVE_INFINITY));
		this.behaviorBranchGroup.addChild(this.framesBehavior);
		this.timeBehavior.setSchedulingBounds(new BoundingSphere( new Point3d(), Double.POSITIVE_INFINITY));
		this.behaviorBranchGroup.addChild(this.timeBehavior);
	}

	public BranchGroup getBehaviorBranchGroup()
	{
		return this.behaviorBranchGroup;
	}

	/**
	 * Note can't be passive so be careful where you use this class
	 */
	private class FramesBehavior extends Behavior
	{
		private WakeupOnElapsedFrames wakeUp;

		private FramesBehavior()
		{
			this.wakeUp = new WakeupOnElapsedFrames(0);
		}

		public void initialize()
		{
			this.wakeupOn(this.wakeUp);
		}

		public void processStimulus(Iterator<WakeupCriterion> critera)
		{
			FPSSopCounter.this.currtime = System.currentTimeMillis();
			FPSSopCounter.this.deltatime = FPSSopCounter.this.currtime - FPSSopCounter.this.lasttime;
			FPSSopCounter.this.lasttime = System.currentTimeMillis();
			FPSSopCounter.this.numOfFrames = FPSSopCounter.this.numOfFrames + 1;
			FPSSopCounter.this.timeOfFrames = FPSSopCounter.this.timeOfFrames + FPSSopCounter.this.deltatime;

			this.wakeupOn(this.wakeUp);
		}
	}

	private class TimeBehavior extends Behavior
	{
		private WakeupOnElapsedTime wakeUp;

		private TimeBehavior()
		{
			this.wakeUp = new WakeupOnElapsedTime((long) FPSSopCounter.TIME_SAMPLE);
		}

		public void initialize()
		{
			this.wakeupOn(this.wakeUp);
		}

		public void processStimulus(Iterator<WakeupCriterion> critera)
		{
			double fps = (double) FPSSopCounter.this.numOfFrames / ((double) FPSSopCounter.this.timeOfFrames / 1000.0D);

			FPSSopCounter.this.numOfFrames = 0;
			FPSSopCounter.this.timeOfFrames = 0L;

			count++;// each 500 milli so 10 = 5sec
			if (count > 10)
			{
				System.out.println("FPS" + ((int) Math.rint(fps * 10) / 10));
				count = 0;
			}


			this.wakeupOn(this.wakeUp);
		}
		private int count = 0;
	}
}


