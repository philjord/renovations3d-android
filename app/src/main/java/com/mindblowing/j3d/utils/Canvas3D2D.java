package com.mindblowing.j3d.utils;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2ES2;

import org.jogamp.java3d.Canvas3D;

import java.io.IOException;


import com.mindblowing.hudbasics.graph.demos.ui.Label;
import com.mindblowing.hudbasics.graph.demos.ui.SceneUIController;
import com.mindblowing.hudbasics.graph.demos.ui.UIShape;

public class Canvas3D2D extends Canvas3D
{
	public boolean isLeft = false;
	private RegionRenderer renderer;
	private RenderState rs = RenderState.createRenderState(SVertex.factory());
	private SceneUIController sceneUIController;
	private final float sceneDist = 10.0F;
	private final float zNear = 0.1F;
	private final float zFar = 100.0F;
	private int renderModes = 0;
	private Font font;
	private final float fontSizeFpsPVP = 0.038F;
	private float dpiH = 96.0F;

	public Canvas3D2D(boolean offScreen)
	{
		super(offScreen);
	}
	public Canvas3D2D(GLWindow glwin) {
		super(glwin);
		this.initRenderer();
	}

	public Canvas3D2D() {
		this.initRenderer();
	}

	public void addNotify() {
		super.addNotify();
		this.initOverlySystem();
	}

	private void initRenderer() {
		this.renderer = RegionRenderer.create(this.rs, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
		this.rs.setHintMask(2);
		this.sceneUIController = new SceneUIController(10.0F, 0.1F, 100.0F);
		this.sceneUIController.setRenderer(this.renderer);
		this.sceneUIController.init(this.getGLWindow());
	}

	public void initOverlySystem() {
		Object upObj = this.getGLWindow().getUpstreamWidget();
		if(upObj instanceof Window) {
			Window ioe = (Window)upObj;
			MonitorDevice mm = ioe.getMainMonitor();
			float[] monitorDPI = mm.getPixelsPerMM(new float[2]);
			monitorDPI[0] *= 25.4F;
			monitorDPI[1] *= 25.4F;
			float[] sDPI = ioe.getPixelsPerMM(new float[2]);
			sDPI[0] *= 25.4F;
			sDPI[1] *= 25.4F;
			this.dpiH = sDPI[1];
		} else {
			System.err.println("Using default DPI of " + this.dpiH);
		}

		try {
			this.font = FontFactory.get(0).getDefault();
		} catch (IOException var6) {
			throw new RuntimeException(var6);
		}
	}

	public void postRender() {
		if(this.getGLWindow() !=null && this.getGLWindow().getGL() != null) {
			GL2ES2 gl = this.getGLWindow().getGL().getGL2ES2();
			gl.glDisable(2960);
			this.sceneUIController.display(this.getGLWindow());
		}

	}

	public Label createLabel() {
		float pixelSizeFPS = 0.038F * (float)this.getGLWindow().getSurfaceHeight();
		Label ret = new Label(this.renderer.getRenderState().getVertexFactory(), this.renderModes, this.font, pixelSizeFPS * 0.1F, "");
		this.sceneUIController.addShape(ret);
		return ret;
	}

	public void addUIShape(UIShape uiShape) {
		this.sceneUIController.addShape(uiShape);
	}

	public void removeUIShape(UIShape uiShape) {
		this.sceneUIController.removeShape(uiShape);
	}

	public Vertex.Factory<? extends Vertex> getVertexFactory() {
		return this.renderer.getRenderState().getVertexFactory();
	}

	public int getRenderMode() {
		return this.renderModes;
	}

	public float getPixelSize() {
		return 0.038F * (float)this.getGLWindow().getSurfaceHeight();
	}

	public Font getFont() {
		return this.font;
	}
}
