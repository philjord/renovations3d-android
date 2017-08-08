package com.mindblowing.swingish;

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

	// turns out the scrolling in this class is actually after scaling so I need to have this here,
	// one day I should ponder making it without scale so it's more natural, but then? who can say

	protected void moveScrolledX(float deltaScrolledX)
	{
		this.scrolledX += deltaScrolledX;
		//clamp for safety
		//ok so my scrolledX and Y are infact after scaling, so I need to clamp multiplied by scale!
		// but because scrolling also includes the plan min value I can't do it here

	}

	protected void moveScrolledY(float deltaScrolledY)
	{
		this.scrolledY += deltaScrolledY;
	}

	protected void setScrolledX(float scrolledX)
	{
		this.scrolledX = scrolledX;
	}

	protected void setScrolledY(float scrolledY)
	{
		this.scrolledY = scrolledY;
	}

	public void setViewPosition(Point p)
	{
		setScrolledX((float) p.getX());
		setScrolledY((float) p.getY());
	}


	private Rectangle getViewRect()
	{
		//TODO: this is way wrong as scrolling includes scale and plan min, set convertModelXToPixel
		return new Rectangle((int) scrolledX, (int) scrolledY, getWidth(), getHeight());
	}

	protected void scrollRectToVisible(Rectangle shapePixelBounds)
	{
		float dx = positionAdjustment(getWidth(), shapePixelBounds.width, shapePixelBounds.x);
		float dy = positionAdjustment(getHeight(), shapePixelBounds.height, shapePixelBounds.y);

		if (dx != 0 || dy != 0)
		{
			moveScrolledX(-dx);
			moveScrolledY(-dy);
		}
	}

	/* Taken from JViewport
	 Used by the scrollRectToVisible method to determine the
		  *  proper direction and amount to move by. The integer variables are named
		  *  width, but this method is applicable to height also. The code assumes that
		  *  parentWidth/childWidth are positive and childAt can be negative.
		  */
	protected float positionAdjustment(float parentWidth, float childWidth, float childAt)
	{
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
