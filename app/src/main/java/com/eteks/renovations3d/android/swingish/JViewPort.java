package com.eteks.renovations3d.android.swingish;

import javaawt.Point;
import javaawt.Rectangle;

/**
 * Created by phil on 3/17/2017.
 */

public abstract class JViewPort extends JComponent
{
	private float scrolledX;
	private float scrolledY;

	protected float getScrolledX()
	{
		return scrolledX;
	}

	protected float getScrolledY()
	{
		return scrolledY;
	}

	protected void moveScrolledX(float deltaScrolledX)
	{
		this.scrolledX += deltaScrolledX;
		//clamp for safety
		this.scrolledX = scrolledX < -3000 ? -3000 : scrolledX > 3000 ? 3000 : scrolledX;
	}

	protected void moveScrolledY(float deltaScrolledY)
	{
		this.scrolledY += deltaScrolledY;
		//clamp for safety
		this.scrolledY = scrolledY < -3000 ? -3000 : scrolledY > 3000 ? 3000 : scrolledY;
	}

	protected void setScrolledX(float scrolledX)
	{
		//clamp for safety
		this.scrolledX = scrolledX < -3000 ? -3000 : scrolledX > 3000 ? 3000 : scrolledX;
	}

	protected void setScrolledY(float scrolledY)
	{
		//clamp for safety
		this.scrolledY = scrolledY < -3000 ? -3000 : scrolledY > 3000 ? 3000 : scrolledY;
	}

	public void setViewPosition(Point p)
	{
		setScrolledX((float) p.getX());
		setScrolledY((float) p.getY());
	}


	public Rectangle getViewRect()
	{
		//TODO: this is probably a good start right here, search in the plan component
		return new Rectangle((int) scrolledX, (int) scrolledY, getWidth(), getHeight());
	}

	protected void scrollRectToVisible(Rectangle shapePixelBounds)
	{
		// should be able to do this without using the move view somehow
		// thoughts, plan bounds only include the features on it, so zoom, pan don't change it, so plan bounds are not in thei world
		// test it all at 1 scale

		//TODO: should all this be dp for small phones?
		//TODO: test zoomed
		// take margin off both sides as the scrolling zone
/*		System.out.println("planbounds " +this.getPlanBounds());
		System.out.println("scrollRectToVisible " +shapePixelBounds);
		System.out.println("scrolledX " +scrolledX + " scrolledY " +scrolledY );
		System.out.println("getScale() " + getScale() + " getWidth() "+getWidth()+ " getHeight() "+ getHeight());

		// works well at scale of .3
		//float dx = positionAdjustment(getWidth() - (MARGIN_PX*2), shapePixelBounds.width, (shapePixelBounds.x*getScale() - scrolledX));
		//float dy = positionAdjustment(getHeight() - (MARGIN_PX*2), shapePixelBounds.height, (shapePixelBounds.y*getScale() - scrolledY));
		//at scale 0.83 y works but positive x doesn't

		float dx = positionAdjustment(getWidth() - (MARGIN_PX*2), shapePixelBounds.width, (shapePixelBounds.x*getScale() - scrolledX));
		float dy = positionAdjustment(getHeight() - (MARGIN_PX*2), shapePixelBounds.height, (shapePixelBounds.y*getScale() - scrolledY));



		System.out.println("dx  " +dx + " dy " +dy);
		if (dx != 0 || dy != 0)
		{
			float m = 100f * getScale();
			dx = dx > m ? m : dx < -m ? -m : dx;
			dy = dy > m ? m : dy < -m ? -m : dy;

			moveView(-dx/getScale(), -dy/getScale());
		}

		System.out.println("new scrolledX " +scrolledX + " new scrolledY " +scrolledY );*/
	}

	/* Taken from JViewport
	 Used by the scrollRectToVisible method to determine the
		  *  proper direction and amount to move by. The integer variables are named
		  *  width, but this method is applicable to height also. The code assumes that
		  *  parentWidth/childWidth are positive and childAt can be negative.
		  */
	private float positionAdjustment(float parentWidth, float childWidth, float childAt)
	{
		System.out.println("parentWidth  " + parentWidth + " childWidth " + childWidth + " childAt " + childAt);
		//   +-----+
		//   | --- |     No Change
		//   +-----+
		if (childAt >= 0 && childWidth + childAt <= parentWidth)
		{
			return 0;
		}

		//   +-----+
		//  ---------   No Change
		//   +-----+
		if (childAt <= 0 && childWidth + childAt >= parentWidth)
		{
			return 0;
		}

		//   +-----+          +-----+
		//   |   ----    ->   | ----|
		//   +-----+          +-----+
		if (childAt > 0 && childWidth <= parentWidth)
		{
			return -childAt + parentWidth - childWidth;
		}

		//   +-----+             +-----+
		//   |  --------  ->     |--------
		//   +-----+             +-----+
		if (childAt >= 0 && childWidth >= parentWidth)
		{
			return -childAt;
		}

		//   +-----+          +-----+
		// ----    |     ->   |---- |
		//   +-----+          +-----+
		if (childAt <= 0 && childWidth <= parentWidth)
		{
			return -childAt;
		}

		//   +-----+             +-----+
		//-------- |      ->   --------|
		//   +-----+             +-----+
		if (childAt < 0 && childWidth >= parentWidth)
		{
			return -childAt + parentWidth - childWidth;
		}

		return 0;
	}
}
