package com.eteks.renovations3d.j3d.mouseover;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import java.util.ArrayList;

import org.jogamp.java3d.CapabilityNotSetException;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.SceneGraphPath;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point2f;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.mindblowing.utils.LongHoldHandler;


public class HomeComponent3DMouseHandler extends MouseOverHandler
{
	private static final float SINGLE_TAP_MAX = 200;
	private static final float DOUBLE_TAP_MAX = 600;

	private static final float ALLOWABLE_WAVER_DP = 10;

	private int allowableWaverPx = 10;

	private final Home home;
	private final UserPreferences preferences;
	private final HomeController3D controller;
	private Renovations3DActivity activity;

	private static final int INVALID_POINTER_ID = -1;

	// The ‘active pointer’ is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;

	private long lastDownTime = 0;
	private Point2f lastDownLocation = null;
	private long lastUpTime = 0;
	private Point2f lastUpLocation = null;// do I need this
	private Selectable lastDownObject = null;

	private Point2f currentLocation = new Point2f();

	private LongHoldHandler longHoldHandler;

	private boolean enabled = true;

	public HomeComponent3DMouseHandler(Home home, UserPreferences preferences, HomeController3D controller,  Renovations3DActivity activity)
	{
		this.home = home;
		this.preferences = preferences;
		this.controller = controller;
		this.activity = activity;

		final float scale = activity.getResources().getDisplayMetrics().density;
		allowableWaverPx = (int) (ALLOWABLE_WAVER_DP * scale + 0.5f);
	}

	/**
	 * return true if this handler handled the event
	 *
	 * @param ev
	 * @return
	 */
	public boolean onTouch(android.view.View v, MotionEvent ev)
	{
		// fast exit when disabled
		if(!enabled)
			return false;

		if(longHoldHandler == null)
		{
			longHoldHandler = new LongHoldHandler(v.getResources().getDisplayMetrics(), 750, 100, longHoldHandlerCallback);
		}

		//allow the long down hold to inspect it, it might consume jitters
		if(longHoldHandler.onTouch(v,  ev))
			return true;

		if (ev.getPointerCount() == 1)
		{
			final int action = MotionEventCompat.getActionMasked(ev);

			final int pointerIndex = MotionEventCompat.getActionIndex(ev);
			final float x = MotionEventCompat.getX(ev, pointerIndex);
			final float y = MotionEventCompat.getY(ev, pointerIndex);
			currentLocation.set(x, y);

			switch (action & MotionEvent.ACTION_MASK)
			{
				// cancel things if a second finger comes along, notice drop through
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_POINTER_DOWN:
				case MotionEvent.ACTION_POINTER_UP:
					return cancel();

				case MotionEvent.ACTION_MOVE:
				{
					// if the move is tiny (allowableWaverPx pixels from start) then just hold fire, and tell caller we are still handling the events
					if (lastDownLocation != null && currentLocation.distance(lastDownLocation) < allowableWaverPx)
						return true;

					// otherwise cancel
					return cancel();
				}

				case MotionEvent.ACTION_DOWN:
				{
					mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
					lastDownTime = ev.getEventTime();
					lastDownLocation = new Point2f(currentLocation);
					// tell the caller they can have te event if they want too
					return false;
				}

				case MotionEvent.ACTION_UP:
				{
					// have we generally been cancelled
					if (lastDownTime == 0)
						return cancel();


					// single tap is a down then up in 200 ms, double tap is a up then up in 600ms?

					// are we fast enough to be a tap not a hold?
					if ((ev.getEventTime() - lastDownTime) < SINGLE_TAP_MAX)
					{
						// don't allow any selection or editing whilst a dialog is up
						if (activity.currentDialog == null || !activity.currentDialog.isShowing())
						{
							int tapCount = 1;

							// are we close enough to any previous tap to be a double tap?
							if (lastUpTime != 0 && (ev.getEventTime() - lastUpTime) < DOUBLE_TAP_MAX)
							{
								tapCount = 2;
								// try to avoid triples
								lastUpTime = 0;
								//lastUpLocation = null;
							}
							else
							{
								// remember the up times for the next time around
								lastUpTime = ev.getEventTime();
								//lastUpLocation = new Point2f(currentLocation);
							}

							pickCanvas.setShapeLocation((int) ev.getX(), (int) ev.getY());

							try
							{
								PickInfo pickInfo = pickCanvas.pickClosest();
								if (pickInfo != null)
								{
									SceneGraphPath sg = pickInfo.getSceneGraphPath();
									Node pickedParent = sg.getNode(0);
									Object userData = pickedParent.getUserData();

									if (userData instanceof Selectable)
									{
										Selectable clickedSelectable = (Selectable) userData;
										ArrayList<Selectable> items = new ArrayList<Selectable>();
										items.add(clickedSelectable);

										// this can be a very slow process let's get off the edt shall we?
										this.home.setSelectedItems(items);
										this.home.setAllLevelsSelection(true);

										// double tap on the same object
										if (tapCount == 2 && lastDownObject == clickedSelectable)
										{
											// Modify selected item on a double click
											if (clickedSelectable instanceof Wall)
											{
												controller.modifySelectedWalls();
											}
											else if (clickedSelectable instanceof HomePieceOfFurniture)
											{
												controller.modifySelectedFurniture();
											}
											else if (clickedSelectable instanceof Room)
											{
												controller.modifySelectedRooms();
											}
											else if (clickedSelectable instanceof Label)
											{
												controller.modifySelectedLabels();
											}

											//TODO: you know the ground could be selectable and editable, but just not have an outline?
											// and it could bring up the 3d attributes window
										}

										lastDownObject = clickedSelectable;
									}
								}
							}
							catch (IllegalStateException e)
							{
								//PJ when does this get thrown I wonder
								e.printStackTrace();
							}
							catch (CapabilityNotSetException e)
							{
								// seen on firebase crash
								//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/3fb14576?duration=2592000000
								//Exception org.jogamp.java3d.CapabilityNotSetException: PickInfo: PICK_GEOMETRY mode - no capability to ALLOW_COORDINATE_READ
								e.printStackTrace();
							}
						}
					}
					else
					{
						// this has been a hold so cancel everything now
						return cancel();
					}
					return true;
				}
			}

		}

		return cancel();
	}

	private boolean cancel()
	{
		lastDownTime = 0;
		lastDownLocation = null;
		lastUpTime = 0;
		//lastUpLocation = null;
		return false;
	}

	private LongHoldHandler.Callback longHoldHandlerCallback =	new LongHoldHandler.Callback (){
		public void longHoldRepeat(MotionEvent ev){
			//Note mostly repeat of above code with click count==2
			pickCanvas.setShapeLocation((int) ev.getX(), (int) ev.getY());

			try
			{
				PickInfo pickInfo = pickCanvas.pickClosest();
				if (pickInfo != null)
				{
					SceneGraphPath sg = pickInfo.getSceneGraphPath();
					Node pickedParent = sg.getNode(sg.nodeCount() - 1);
					Object userData = pickedParent.getUserData();

					if (userData instanceof Selectable)
					{
						Selectable clickedSelectable = (Selectable) userData;
						ArrayList<Selectable> items = new ArrayList<Selectable>();
						items.add(clickedSelectable);

						// this can be a very slow process let's get off the edt shall we?
						home.setSelectedItems(items);
						home.setAllLevelsSelection(true);


						// Modify selected item on a double click
						if (clickedSelectable instanceof Wall)
						{
							controller.modifySelectedWalls();
						}
						else if (clickedSelectable instanceof HomePieceOfFurniture)
						{
							controller.modifySelectedFurniture();
						}
						else if (clickedSelectable instanceof Room)
						{
							controller.modifySelectedRooms();
						}
						else if (clickedSelectable instanceof Label)
						{
							controller.modifySelectedLabels();
						}

						lastDownObject = clickedSelectable;
					}
				}
			}
			catch (IllegalStateException e)
			{
				//PJ when does this get thrown I wonder
				e.printStackTrace();
			}
			catch (CapabilityNotSetException e)
			{
				// seen on firebase crash
				//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/3fb14576?duration=2592000000
				//Exception org.jogamp.java3d.CapabilityNotSetException: PickInfo: PICK_GEOMETRY mode - no capability to ALLOW_COORDINATE_READ
				e.printStackTrace();
			}
		}
	};

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}