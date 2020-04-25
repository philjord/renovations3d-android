package com.mindblowing.swingish;

import javaawt.Point;
import javaawt.Rectangle;


public abstract class JViewPort extends JComponent
{
	// note scrolled X is in pixel space (not model space as PlanComponent uses sometimes)
	private float scrolledX;
	private float scrolledY;

	//pixel coords
	protected float getXScroll() {
		return scrolledX;
	}

	//pixel coords
	protected float getYScroll() {
		return scrolledY;
	}

	protected void moveXScroll(float deltaScrolledX) {
		this.scrolledX += deltaScrolledX;
	}

	protected void moveYScroll(float deltaScrolledY) {
		this.scrolledY += deltaScrolledY;
	}

	protected void setXScroll(float scrolledX) {
		this.scrolledX = scrolledX;
	}

	protected void setYScroll(float scrolledY) {
		this.scrolledY = scrolledY;
	}

	public void setScrollPosition(Point p) {
		setXScroll((float) p.getX());
		setYScroll((float) p.getY());
	}

	//pixel space
	protected void scrollRectToVisible(Rectangle shapePixelBounds) {
		System.out.println("pw " + getWidth() + " cw " +shapePixelBounds.width + " at " +(shapePixelBounds.x) );
		float dx = positionAdjustment(getWidth(), shapePixelBounds.width, shapePixelBounds.x);
		float dy = positionAdjustment(getHeight(), shapePixelBounds.height, shapePixelBounds.y);

		if (dx != 0) {
			moveXScroll(-dx);
		}
		if (dy != 0) {
			moveYScroll(-dy);
		}
	}

	//pixel space, scrolled distance
	public Point getViewPosition() {
		return new Point((int)scrolledX, (int)scrolledY);
	}

	//pixel space, x,y always 0,0, no scroll info
	public Rectangle getVisibleRect() {
		return new Rectangle(0, 0, getWidth(), getHeight());
	}

	/* Taken from JViewport
	 Used by the scrollRectToVisible method to determine the
		  *  proper direction and amount to move by. The integer variables are named
		  *  width, but this method is applicable to height also. The code assumes that
		  *  parentWidth/childWidth are positive and childAt can be negative.
		  */
	protected float positionAdjustment(float parentWidth, float childWidth, float childAt) {
		//   +-----+
		//   | --- |     No Change
		//   +-----+
		if (childAt >= 0 && childWidth + childAt <= parentWidth) {System.out.println("1");
			return 0;
		}

		//   +-----+
		//  ---------   No Change
		//   +-----+
		if (childAt <= 0 && childWidth + childAt >= parentWidth) {System.out.println("2");
			return 0;
		}

		//   +-----+          +-----+
		//   |   ----    ->   | ----|
		//   +-----+          +-----+
		if (childAt > 0 && childWidth <= parentWidth) {System.out.println("3");
			return -childAt + parentWidth - childWidth;
		}

		//   +-----+             +-----+
		//   |  --------  ->     |--------
		//   +-----+             +-----+
		if (childAt >= 0 && childWidth >= parentWidth) {System.out.println("4");
			return -childAt;
		}

		//   +-----+          +-----+
		// ----    |     ->   |---- |
		//   +-----+          +-----+
		if (childAt <= 0 && childWidth <= parentWidth) {System.out.println("5");
			return -childAt;
		}

		//   +-----+             +-----+
		//-------- |      ->   --------|
		//   +-----+             +-----+
		if (childAt < 0 && childWidth >= parentWidth) {System.out.println("6");
			return -childAt + parentWidth - childWidth;
		}
		System.out.println("7");
		return 0;
	}
}
