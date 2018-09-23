package com.eteks.renovations3d;


import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.mindblowing.renovations3d.R;
import com.transitionseverywhere.ChangeText;
import com.transitionseverywhere.TransitionManager;

import org.jogamp.vecmath.Point2f;

import javaawt.geom.Point2D;

/**
 * Created by phil on 7/23/2017.
 */

public class Tutorial
{
	private static final String PREF_TUTORIAL_CURRENT_STEP = "PREF_TUTORIAL_CURRENT_STEP";
	private static final String PREF_TUTORIAL_IS_ENABLED = "PREF_TUTORIAL_IS_ENABLED";

	private static final double MIN_YAW_PITCH_DELTA = 0.2f;
	private static final float FURNITURE_MOVED_PX = 50f;
	private static final float FURNITURE_ROTATED = (float) Math.PI / 4;
	private static int MIN_PLAN_MOVE_PX = 50;

	private Renovations3DActivity activity;
	private ViewGroup view;
	private ViewGroup viewVideoText;

	private TextView tutorialTitle;
	private TextView textView;
	private VideoView videoView;
	private int currentVideoId = -1;

	ImageButton prevButton;
	ImageButton skipButton;
	ImageButton hideButton;
	//Button resetButton;


	private TutorialStep currentStep = null;
	private TutorialStep[] tutorialSteps = new TutorialStep[]{new TutorialStep0(),//
			new TutorialStep1(),//
			new TutorialStep2(),//
			new TutorialStep3(),//
			new TutorialStep4(),//
			new TutorialStep5(),//
			new TutorialStep6(),//
			new TutorialStep7(),//
			new TutorialStep8(),//
			new TutorialStep9(),//
			new TutorialStep10(),//
			new TutorialStep11(),//
			new TutorialStep12(),//
			new TutorialStep13(),//
			new TutorialStep14(),//
			new TutorialStep15(),//
			new TutorialStep16(),//
			new TutorialStep17(),//
			new TutorialStepFinish()};


	/**
	 * Inflate the source view and hand in to here
	 *
	 * @param view
	 */
	public Tutorial(final Renovations3DActivity activity, ViewGroup view)
	{
		this.activity = activity;
		this.view = view;

		viewVideoText = (ViewGroup) view.findViewById(R.id.tutorialVideoTextView);
		tutorialTitle = (TextView) view.findViewById(R.id.tutorialTitle);
		textView = (TextView) view.findViewById(R.id.tutorialTextView);
		videoView = (VideoView) view.findViewById(R.id.tutorialVideoView);

		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
		{
			@Override
			public void onPrepared(MediaPlayer mp)
			{
				mp.setLooping(true);
			}
		});

		prevButton = (ImageButton) view.findViewById(R.id.tutorialPrevButton);
		skipButton = (ImageButton) view.findViewById(R.id.tutorialSkipButton);
		hideButton = (ImageButton) view.findViewById(R.id.tutorialHideButton);
		//resetButton = (Button) view.findViewById(R.id.tutorialResetButton);


		prevButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (currentStep != null)
					currentStep.prev(false);
			}
		});

		skipButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (currentStep != null)
					currentStep.skip(false);
			}
		});

		hideButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setEnable(false);
			}
		});

		for (int i = 0; i < tutorialSteps.length; i++)
			tutorialSteps[i].id = i;

		SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
		int currentStepIdx = settings.getInt(PREF_TUTORIAL_CURRENT_STEP, 0);
		tutorialSteps[currentStepIdx].stepStarted(false);

		boolean tutorialEnabled = settings.getBoolean(PREF_TUTORIAL_IS_ENABLED, true);
		setEnable(tutorialEnabled);
	}

	public void setEnable(boolean enable)
	{
		if (enable)
		{
			view.setVisibility(View.VISIBLE);
			activity.getAdMobManager().hide();

			// restart if ended
			if (currentStep instanceof TutorialStepFinish)
			{
				tutorialSteps[0].stepStarted(false);
			}

			if (currentStep != null && currentStep.id > 0)
				Renovations3DActivity.logFireBaseContent("Tutorial redisplayed");
			else
				Renovations3DActivity.logFireBaseContent("Tutorial started");

		}
		else
		{
			view.setVisibility(View.GONE);
			activity.getAdMobManager().show();
			Renovations3DActivity.logFireBaseContent("Tutorial hidden");
		}

		SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREF_TUTORIAL_IS_ENABLED, enable);
		editor.apply();
	}

	public boolean isEnabled()
	{
		return view.getVisibility() == View.VISIBLE;
	}

	public void actionComplete(TutorialAction tutorialAction)
	{
		if (currentStep != null)
		{
			currentStep.actionComplete(tutorialAction);
		}
	}

	public void actionComplete(TutorialAction tutorialAction, Object data)
	{
		if (currentStep != null && !currentStep.isCompleted())
		{
			currentStep.actionComplete(tutorialAction, data);
		}
	}

	public void onResume()
	{
		if (videoView != null)
		{
			if (currentVideoId != -1)
			{
				videoView.start();
			}
		}
	}

	public void onPause()
	{
		if (videoView != null)
		{
			videoView.pause();
		}
	}


	public enum TutorialAction
	{
		PLAN_MOVED, //intro
		CREATE_WALL_TOOL_SELECTED, WALL_PLACED, WALL_CREATION_FINISHED, //create wall
		CREATE_ROOM_TOOL_SELECTED, CREATE_ROOM_FINISHED, ROOM_FLOOR_TEXTURE_CHANGED, //create room
		FURNITURE_CATALOG_SHOWN, FURNITURE_ADDED, FURNITURE_UPDATED, FURNITURE_ROTATED, //add furniture
		VIEW_SHOWN_3D, CAMERA_MOVED_3D, VIRTUAL_VISIT_STARTED, SEARCH_DONE, DOOR_ADDED, DOOR_INSERTED_IN_WALL// add door
	}

	private abstract class TutorialStep
	{
		public int id = -1;
		//used by each step to not completed twice until restarted
		protected boolean completed = false;

		public void stepStarted(boolean showWellDone)
		{
			completed = false;
			currentStep = this;

			//first and last should set false
			prevButton.setEnabled(true);
			skipButton.setEnabled(true);

			SharedPreferences settings = activity.getSharedPreferences(activity.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(PREF_TUTORIAL_CURRENT_STEP, id);
			editor.apply();

			Renovations3DActivity.logFireBaseContent("Tutorial step displayed  - " + this.id);
		}

		public abstract void actionComplete(TutorialAction tutorialAction);

		//optional
		public void actionComplete(TutorialAction tutorialAction, Object data)
		{

		}

		public void skip(boolean showWellDone)
		{
			tutorialSteps[id + 1].stepStarted(showWellDone);
		}

		public void prev(boolean showWellDone)
		{
			tutorialSteps[id - 1].stepStarted(showWellDone);
		}

		protected void configureUI(final String title, final String text, int vidId, boolean showWellDone)
		{
			if (showWellDone)
			{
				runOnUIDelayed(500, new Runnable()
				{
					public void run()
					{
						TransitionManager.beginDelayedTransition(view,
								new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
						tutorialTitle.setText(R.string.tutorialWellDone);
						textView.setText(text);
					}
				});
				runOnUIDelayed(3000, new Runnable()
				{
					public void run()
					{
						TransitionManager.beginDelayedTransition(view,
								new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
						tutorialTitle.setText(title);
					}
				});
			}
			else
			{
				// fade the text out then in
				TransitionManager.beginDelayedTransition(view,
						new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
				tutorialTitle.setText(title);
				textView.setText(text);
			}

			currentVideoId = vidId;
			if (currentVideoId != -1)
			{
				String uri = "android.resource://" + activity.getPackageName() + "/" + currentVideoId;
				videoView.setVideoURI(Uri.parse(uri));
				videoView.start();
			}
			else
			{
				videoView.stopPlayback();
			}
		}


		protected void runOnUIDelayed(final long delay, final Runnable r)
		{
			Thread t = new Thread()
			{
				public void run()
				{
					try
					{
						Thread.sleep(delay);
						activity.runOnUiThread(r);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			};
			t.start();
		}

		public boolean isCompleted()
		{
			return completed;
		}

		protected void completed()
		{
			completed = true;
			// note firebase kills this to a-z style names so id is better
			Renovations3DActivity.logFireBaseContent("Tutorial step completed  - " + this.id);
			skip(true);
		}

	}


	private class TutorialStep0 extends TutorialStep
	{
		private Point2D firstMoveLocation = null;

		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			skipButton.setEnabled(false);// force them to move the plan
			prevButton.setEnabled(false);

			String title = activity.getString(R.string.tutStep0Title);
			String text = activity.getString(R.string.tutStep0Text);
			int vidId = R.raw.tut0edit_edit;

			configureUI(title, text, vidId, showWellDone);


		}

		@Override
		public void prev(boolean showWellDone)
		{
			// this should never be called
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			// not used as the position data is required
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction, Object data)
		{
			if (tutorialAction == TutorialAction.PLAN_MOVED)
			{
				Point2D p = (Point2D) data;
				if (firstMoveLocation == null)
				{
					firstMoveLocation = p;
				}
				else if (p.distance(firstMoveLocation) > MIN_PLAN_MOVE_PX)
				{
					completed();
				}
			}
		}
	}

	private class TutorialStep1 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep1Title), activity.getString(R.string.tutStep1Text), R.raw.tut1edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.CREATE_WALL_TOOL_SELECTED)
			{
				completed();
			}
		}
	}

	private class TutorialStep2 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep2Title), activity.getString(R.string.tutStep2Text), R.raw.tut2edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.WALL_PLACED)
			{
				completed();
			}
		}
	}

	private class TutorialStep3 extends TutorialStep
	{
		private int wallsPlacedInThisStep = 0;

		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep3Title), activity.getString(R.string.tutStep3Text), R.raw.tut3edit_edit, showWellDone);
			wallsPlacedInThisStep = 0;
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.WALL_PLACED)
			{
				wallsPlacedInThisStep++;
				if (wallsPlacedInThisStep >= 3)
				{
					completed();
				}
			}
		}
	}

	private class TutorialStep4 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep4Title), activity.getString(R.string.tutStep4Text), R.raw.tut4edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.WALL_CREATION_FINISHED)
			{
				completed();
			}
		}
	}

	private class TutorialStep5 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep5Title), activity.getString(R.string.tutStep5Text), R.raw.tut5edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.CREATE_ROOM_TOOL_SELECTED)
			{
				completed();
			}
		}
	}

	private class TutorialStep6 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep6Title), activity.getString(R.string.tutStep6Text), R.raw.tut6edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.CREATE_ROOM_FINISHED)
			{
				completed();
			}
		}
	}

	private class TutorialStep7 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep7Title), activity.getString(R.string.tutStep7Text), R.raw.tut7edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.ROOM_FLOOR_TEXTURE_CHANGED)
			{
				completed();
			}
		}
	}

	private class TutorialStep8 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep8Title), activity.getString(R.string.tutStep8Text), R.raw.tut8edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.FURNITURE_CATALOG_SHOWN)
			{
				completed();
			}
		}
	}

	private class TutorialStep9 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep9Title), activity.getString(R.string.tutStep9Text), R.raw.tut9edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.FURNITURE_ADDED)
			{
				completed();
			}
		}
	}

	private class TutorialStep10 extends TutorialStep
	{
		private HomePieceOfFurniture watchedPiece = null;
		private Point2f startPoint = new Point2f();
		private float rotation = 0;
		private boolean movedEnough = false;
		private boolean rotatedEnough = false;

		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep10Title), activity.getString(R.string.tutStep10Text), R.raw.tut10edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			// not used as the position data is required
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction, Object data)
		{
			if (tutorialAction == TutorialAction.FURNITURE_UPDATED)
			{
				HomePieceOfFurniture piece = (HomePieceOfFurniture) data;

				if (piece == watchedPiece)
				{
					Point2f currentPoint = new Point2f(piece.getX(), piece.getY());
					if (currentPoint.distance(startPoint) > FURNITURE_MOVED_PX)
					{
						movedEnough = true;
					}

					if (Math.abs(piece.getAngle() - rotation) > FURNITURE_ROTATED)
					{
						rotatedEnough = true;
					}

					if (movedEnough && rotatedEnough)
					{
						completed();
					}
				}
				else
				{
					watchedPiece = piece;
					startPoint.set(piece.getX(), piece.getY());
					rotation = piece.getAngle();
					movedEnough = false;
					rotatedEnough = false;
				}
			}
		}
	}

	private class TutorialStep11 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep11Title), activity.getString(R.string.tutStep11Text), R.raw.tut11edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.VIEW_SHOWN_3D)
			{
				completed();
			}
		}
	}

	private class TutorialStep12 extends TutorialStep
	{
		private Point2f accumYP = new Point2f();

		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep12Title), activity.getString(R.string.tutStep12Text), R.raw.tut12edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			// not used as the position data is required
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction, Object data)
		{
			if (tutorialAction == TutorialAction.CAMERA_MOVED_3D)
			{
				Point2f yp = (Point2f) data;
				accumYP.add(yp);
				if (Math.abs(accumYP.x) > MIN_YAW_PITCH_DELTA || Math.abs(accumYP.y) > MIN_YAW_PITCH_DELTA)
				{
					completed();
				}
			}
		}
	}

	private class TutorialStep13 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep13Title), activity.getString(R.string.tutStep13Text), R.raw.tut13edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.VIRTUAL_VISIT_STARTED)
			{
				completed();
			}
		}
	}

	private class TutorialStep14 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep14Title), activity.getString(R.string.tutStep14Text), R.raw.tut14edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.FURNITURE_CATALOG_SHOWN)
			{
				completed();
			}
		}
	}

	private class TutorialStep15 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep15Title), activity.getString(R.string.tutStep15Text), R.raw.tut15edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			// not used as the data is required
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction, Object data)
		{
			if (tutorialAction == TutorialAction.SEARCH_DONE)
			{
				String s = (String) data;
				if ("door".equalsIgnoreCase(s))
				{
					completed();
				}
			}
		}
	}

	private class TutorialStep16 extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep16Title), activity.getString(R.string.tutStep16Text), R.raw.tut16edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			if (tutorialAction == TutorialAction.DOOR_ADDED)
			{
				completed();
			}
		}
	}

	private class TutorialStep17 extends TutorialStep
	{
		private HomeDoorOrWindow watchedPiece = null;
		private Point2f startPoint = new Point2f();
		private Point2f startDims = new Point2f();
		private boolean movedEnough = false;

		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStep17Title), activity.getString(R.string.tutStep17Text), R.raw.tut17edit_edit, showWellDone);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			// not used as the position data is required
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction, Object data)
		{
			if (tutorialAction == TutorialAction.FURNITURE_UPDATED && data instanceof HomeDoorOrWindow)
			{
				final HomeDoorOrWindow piece = (HomeDoorOrWindow) data;

				if (piece == watchedPiece)
				{
					Point2f currentPoint = new Point2f(piece.getX(), piece.getY());
					//NOTE piece.isBoundToWall() is almost always false, even when in a wall
					if (currentPoint.distance(startPoint) > FURNITURE_MOVED_PX)
					{
						movedEnough = true;
					}

					// We get here through an x or y change that has just a moment ago wiped the wall bound state coming from the plancontroller
					// but the next call in that controller after this returns is to set the wall bound state back to true (if it is)
					// so this tutorial needs to get off this thread and check the state in 500mms when it should be about right
					Thread t = new Thread(){public void run() {
						try {
							Thread.sleep(500);
							if (!completed && movedEnough && piece.isBoundToWall())
							{
								completed();
							}
						}catch(InterruptedException e){}
					}};
					t.start();
				}
				else
				{
					watchedPiece = piece;
					startPoint.set(piece.getX(), piece.getY());
					startDims.set(piece.getWidth(), piece.getDepth());
					movedEnough = false;
				}
			}
		}
	}

	private class TutorialStepFinish extends TutorialStep
	{
		@Override
		public void stepStarted(boolean showWellDone)
		{
			super.stepStarted(showWellDone);
			configureUI(activity.getString(R.string.tutStepFinishTitle), activity.getString(R.string.tutStepFinishText), -1, showWellDone);

			// after 30 seconds hide the tutorial automatically
			runOnUIDelayed(30000, new Runnable()
			{
				public void run()
				{
					Tutorial.this.setEnable(false);
				}
			});
		}

		@Override
		public void skip(boolean showWellDone)
		{
			//all done hide, note false not showWellDone
			Tutorial.this.setEnable(false);
		}

		@Override
		public void actionComplete(TutorialAction tutorialAction)
		{
			//ignored as nothing completes this
		}
	}

}
