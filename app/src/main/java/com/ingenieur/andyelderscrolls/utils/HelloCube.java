package com.ingenieur.andyelderscrolls.utils;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

/**
 * Created by phil on 11/26/2016.
 */

public class HelloCube
{

	private static NewtMiscKeyHandler newtMiscKeyHandler = new NewtMiscKeyHandler();
	private static SimpleUniverse univ = null;
	private static BranchGroup scene = null;

	public static void setup(GLWindow gl_window)
	{
		try
		{
			//float[] fs = new float[2];
			//gl_window.getCurrentSurfaceScale(fs);
			//System.out.println("getCurrentSurfaceScale " + fs[0] + " " + fs[1]);


			gl_window.setDefaultGesturesEnabled(false);

			SimpleShaderAppearance.setVersionES300();
			Canvas3D canvas3D = new Canvas3D(gl_window);
			createUniverse(canvas3D);
			scene = createSceneGraph();
			univ.addBranchGraph(scene);

			canvas3D.addNotify();

			gl_window.addKeyListener(newtMiscKeyHandler);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public static BranchGroup createSceneGraph()
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create the TransformGroup node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at run time. Add it to
		// the root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objRoot.addChild(objTrans);

		// Create a simple Shape3D node; add it to the scene graph.
		objTrans.addChild(new Cube(0.4));

		// Create a new Behavior object that will perform the
		// desired operation on the specified transform and add
		// it into the scene graph.
		Transform3D yAxis = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, 4000);

		RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		rotator.setSchedulingBounds(bounds);
		objRoot.addChild(rotator);

		// Have Java 3D perform optimizations on this scene graph.
		objRoot.compile();

		return objRoot;
	}

	public static SimpleUniverse createUniverse(Canvas3D c)
	{


		// Create simple universe with view branch
		univ = new SimpleUniverse(c);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		univ.getViewingPlatform().setNominalViewingTransform();

		// Ensure at least 5 msec per frame (i.e., < 200Hz)
		univ.getViewer().getView().setMinimumFrameCycleTime(5);

		return univ;
	}


	private static class NewtMiscKeyHandler implements KeyListener
	{
		public NewtMiscKeyHandler()
		{
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_H)
			{
				System.out.println("H");
			}
			else if (e.getKeyCode() == KeyEvent.VK_L)
			{

			}

		}

		@Override
		public void keyReleased(KeyEvent arg0)
		{

		}
	}

}
