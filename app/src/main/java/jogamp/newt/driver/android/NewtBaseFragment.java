package jogamp.newt.driver.android;
/**
 * Copyright 2011 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

import java.util.ArrayList;
import java.util.List;

import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.opengl.FPSCounter;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;

import com.jogamp.newt.Window;
import com.jogamp.opengl.GLEventListenerState;
import com.jogamp.opengl.GLStateKeeper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class NewtBaseFragment extends Fragment
{
	protected List<Window> newtWindows = new ArrayList<Window>();
	List<GLAutoDrawable> glAutoDrawables = new ArrayList<GLAutoDrawable>();

	GLAnimatorControl animator = null;

	boolean setThemeCalled = false;

	protected void startAnimation(final boolean start) {
		if(null != animator) {
			final boolean res;
			if( start ) {
				if( animator.isPaused() ) {
					res = animator.resume();
				} else {
					res = animator.start();
				}
			} else {
				res = animator.stop();
			}
			Log.d(MD.TAG, "Animator global: start "+start+", result "+res);
		}
		for(int i=0; i<glAutoDrawables.size(); i++) {
			final GLAnimatorControl anim = glAutoDrawables.get(i).getAnimator();
			if(null != anim) {
				final boolean res;
				if( start ) {
					if( anim.isPaused() ) {
						res = anim.resume();
					} else {
						res = anim.start();
					}
				} else {
					res = anim.stop();
				}
				Log.d(MD.TAG, "Animator glad["+i+"]: start "+start+", result "+res);
			}
		}
	}

	public NewtBaseFragment() {
		super();
	}

	/**
	 *
	 * @param androidWindow
	 * @param newtWindow
	 * @return
	 * @throws IllegalArgumentException if the <code>newtWindow</code>'s {@link Window#getDelegatedWindow() delegate} is not an AndroidDriver.
	 */
	public View getContentView(final android.view.Window androidWindow, final Window newtWindow) throws IllegalArgumentException {
		final Window delegateWindow = newtWindow.getDelegatedWindow();
		if(delegateWindow instanceof WindowDriver) {
			adaptTheme4Transparency(delegateWindow.getRequestedCapabilities());
			layoutForNEWTWindow(androidWindow, delegateWindow);
			final WindowDriver newtAWindow = (WindowDriver)delegateWindow;
			this.registerNEWTWindow(newtWindow);
			return newtAWindow.getAndroidView();
		} else {
			throw new IllegalArgumentException("Given NEWT Window is not an Android Window: " + newtWindow.getClass().getName());
		}
	}

	/**
	 * This is one of the three registration methods (see below).
	 * <p>
	 * This methods registers the given NEWT window to ensure it's destruction at {@link #onDestroy()}.
	 * </p>
	 * <p>
	 * If adding a {@link GLAutoDrawable} implementation, the {@link GLAnimatorControl} retrieved by {@link GLAutoDrawable#getAnimator()}
	 * will be used for {@link #onPause()} and {@link #onResume()}.
	 * </p>
	 * <p>
	 * If adding a {@link GLAutoDrawable} implementation, the {@link GLEventListenerState} will preserve it's state
	 * when {@link #onPause()} is called. A later {@link #onResume()} will
	 * reinstate the {@link GLEventListenerState}.
	 * </p>
	 *
	 * @param newtWindow
	 * @throws IllegalArgumentException if the <code>newtWindow</code>'s {@link Window#getDelegatedWindow() delegate} is not an AndroidDriver.
	 * @see #getContentView(android.view.Window, Window)
	 */
	public void registerNEWTWindow(final Window newtWindow) throws IllegalArgumentException {
		final Window delegateWindow = newtWindow.getDelegatedWindow();
		Log.d(MD.TAG, "registerNEWTWindow: Type "+newtWindow.getClass().getName()+", delegate "+delegateWindow.getClass().getName());
		if(delegateWindow instanceof WindowDriver) {
			final WindowDriver newtAWindow = (WindowDriver)delegateWindow;
			newtAWindow.registerActivity(getActivity());
		} else {
			throw new IllegalArgumentException("Given NEWT Window's Delegate is not an Android Window: "+delegateWindow.getClass().getName());
		}
		newtWindows.add(newtWindow);
		if(newtWindow instanceof GLAutoDrawable) {
			glAutoDrawables.add((GLAutoDrawable)newtWindow);
		}
		if(newtWindow instanceof GLStateKeeper) {
			((GLStateKeeper)newtWindow).setGLStateKeeperListener(glStateKeeperListener);
		}
	}
	private final GLStateKeeper.Listener glStateKeeperListener = new GLStateKeeper.Listener() {
		@Override
		public void glStatePreserveNotify(final GLStateKeeper glsk) {
			Log.d(MD.TAG, "GLStateKeeper Preserving: 0x"+Integer.toHexString(glsk.hashCode()));
		}
		@Override
		public void glStateRestored(final GLStateKeeper glsk) {
			Log.d(MD.TAG, "GLStateKeeper Restored: 0x"+Integer.toHexString(glsk.hashCode()));
			startAnimation(true);
		}
	};

	/**
	 * Convenient method to set the Android window's flags to fullscreen or size-layout depending on the given NEWT window.
	 * <p>
	 * Must be called before creating the view and adding any content, i.e. setContentView() !
	 * </p>
	 * @param androidWindow
	 * @param newtWindow
	 */
	public void layoutForNEWTWindow(final android.view.Window androidWindow, final Window newtWindow) {
		if(null == androidWindow || null == newtWindow) {
			throw new IllegalArgumentException("Android or NEWT Window null");
		}

		if( newtWindow.isFullscreen() || newtWindow.isUndecorated() ) {
			androidWindow.requestFeature(android.view.Window.FEATURE_NO_TITLE);
		}
		if( newtWindow.isFullscreen() ) {
			androidWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			androidWindow.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			androidWindow.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			androidWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		if(newtWindow.getWidth()>0 && newtWindow.getHeight()>0 && !newtWindow.isFullscreen()) {
			androidWindow.setLayout(newtWindow.getWidth(), newtWindow.getHeight());
		}
	}

	/**
	 * Convenient method to set this context's theme to transparency depending on {@link CapabilitiesImmutable#isBackgroundOpaque()}.
	 * <p>
	 * Must be called before creating the view and adding any content, i.e. setContentView() !
	 * </p>
	 */
	protected void adaptTheme4Transparency(final CapabilitiesImmutable caps) {
		if(!caps.isBackgroundOpaque()) {
			setTransparencyTheme();
		}
	}

	/**
	 * Convenient method to set this context's theme to transparency.
	 * <p>
	 * Must be called before creating the view and adding any content, i.e. setContentView() !
	 * </p>
	 * <p>
	 * Is normally issued by {@link #getContentView(android.view.Window, Window)}
	 * if the requested NEWT Capabilities ask for transparency.
	 * </p>
	 * <p>
	 * Can be called only once.
	 * </p>
	 */
	public void setTransparencyTheme() {
		if(!setThemeCalled) {
			setThemeCalled = true;
			final Context ctx = getActivity().getApplicationContext();
			final String frn = ctx.getPackageName()+":style/Theme.Transparent";
			final int resID = ctx.getResources().getIdentifier("Theme.Transparent", "style", ctx.getPackageName());
			if(0 == resID) {
				Log.d(MD.TAG, "SetTransparencyTheme: Resource n/a: "+frn);
			} else {
				Log.d(MD.TAG, "SetTransparencyTheme: Setting style: "+frn+": 0x"+Integer.toHexString(resID));
				ctx.setTheme(resID);
			}
		}
	}

	/**
	 * Setting up a global {@Link GLAnimatorControl} for {@link #onPause()} and {@link #onResume()}.
	 * <p>
	 * Note that if adding a {@link GLAutoDrawable} implementation via {@link #registerNEWTWindow(Window)},
	 * {@link #getContentView(android.view.Window, Window)}
	 * their {@link GLAnimatorControl} retrieved by {@link GLAutoDrawable#getAnimator()} will be used as well.
	 * In this case, using this global {@Link GLAnimatorControl} is redundant.
	 * </p>
	 * @see #registerNEWTWindow(Window)
	 * @see #getContentView(android.view.Window, Window)
	 */
	public void setAnimator(final GLAnimatorControl animator) {
		this.animator = animator;
		if(!animator.isStarted()) {
			animator.start();
		}
		animator.pause();
	}


	public android.view.Window getWindow() {

		return getActivity().getWindow();

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.d(MD.TAG, "onCreate.0");

		super.onCreate(savedInstanceState);

		// Extraordinary cleanup, for cases of 'onCreate()' calls w/ valid states,
		// i.e. w/o having onDestroy() being called.
		// Could happened due to spec when App process is killed for memory exhaustion or other reasons.
		cleanup();

		jogamp.common.os.android.StaticContext.init(this.getActivity().getApplicationContext());
		Log.d(MD.TAG, "onCreate.X");
	}

	@Override
	public void onStart() {
		Log.d(MD.TAG, "onStart.0");

		super.onStart();

		Log.d(MD.TAG, "onStart.X");
	}

	@Override
	public void onResume() {
		Log.d(MD.TAG, "onResume.0");

		super.onResume();

		for(int i=0; i<newtWindows.size(); i++) {
			final Window win = newtWindows.get(i);
			win.setVisible(true);
			if(win instanceof FPSCounter) {
				((FPSCounter)win).resetFPSCounter();
			}
		}
		startAnimation(true);
		Log.d(MD.TAG, "onResume.X");
	}

	@Override
	public void onPause() {
		Log.d(MD.TAG, "onPause.0");
		if( !getActivity().isFinishing() ) {
			int ok=0, fail=0;
			for(int i=0; i<glAutoDrawables.size(); i++) {
				final GLAutoDrawable glad = glAutoDrawables.get(i);
				if(glad instanceof GLStateKeeper) {
					if( ((GLStateKeeper)glad).preserveGLStateAtDestroy(true) ) {
						ok++;
					} else {
						fail++;
					}
				}
			}
			Log.d(MD.TAG, "GLStateKeeper.Mark2Preserve: Total "+glAutoDrawables.size()+", OK "+ok+", Fail "+fail);
		}
		startAnimation(false);

		super.onPause();

		Log.d(MD.TAG, "onPause.X");
	}

	@Override
	public void onStop() {
		Log.d(MD.TAG, "onStop.0");
		for(int i=0; i<newtWindows.size(); i++) {
			final Window win = newtWindows.get(i);
			win.setVisible(false);
		}

		super.onStop();

		Log.d(MD.TAG, "onStop.X");
	}

	/**
	 * Performs cleaning up all references,
	 * <p>
	 * Cleaning and destroying up all preserved GLEventListenerState
	 * and clearing the preserve-flag of all GLStateKeeper.
	 * </p>
	 * <p>
	 * Destroying all GLWindow.
	 * </p>
	 */
	private void cleanup() {
		Log.d(MD.TAG, "cleanup.0");
		int glelsKilled = 0, glelsClean = 0;
		for(int i=0; i<glAutoDrawables.size(); i++) {
			final GLAutoDrawable glad = glAutoDrawables.get(i);
			if(glad instanceof GLStateKeeper) {
				final GLStateKeeper glsk = (GLStateKeeper)glad;
				glsk.preserveGLStateAtDestroy(false);
				final GLEventListenerState glels = glsk.clearPreservedGLState();
				if( null != glels) {
					glels.destroy();
					glelsKilled++;
				} else {
					glelsClean++;
				}
			}
		}
		Log.d(MD.TAG, "cleanup.1: GLStateKeeper.ForceDestroy: Total "+glAutoDrawables.size()+", destroyed "+glelsKilled+", clean "+glelsClean);
		for(int i=0; i<newtWindows.size(); i++) {
			final Window win = newtWindows.get(i);
			win.destroy();
		}
		newtWindows.clear();
		glAutoDrawables.clear();
		Log.d(MD.TAG, "cleanup.1: StaticContext.getContext: "+jogamp.common.os.android.StaticContext.getContext());
		jogamp.common.os.android.StaticContext.clear();
		Log.d(MD.TAG, "cleanup.X");
	}

	@Override
	public void onDestroy() {
		Log.d(MD.TAG, "onDestroy.0");
		cleanup(); // normal cleanup

		super.onDestroy();

		Log.d(MD.TAG, "onDestroy.X");
	}
}

