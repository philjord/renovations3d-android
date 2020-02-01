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
package jogamp.common.os.android;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;


//PJ If 2 NewtActivities are started in the same JVM then the second one will call clear() when disposed
// however the first one will presume it still has a fine StaticContext, and will have no ability to catch the clear event.
// In order to try to address this the context is now held by WeakReference, it will often be exactly the same context
// and if not it will still hold the same resources.
// therefore clear is ignored, resetting context allowed and the weak reference is presumed to allow clean up

public class StaticContext {
	//PJ weakref
	private static WeakReference<Context> appContext = null;
	private static WeakReference<ViewGroup> contentViewGroup = null;

	private static boolean DEBUG = false;

	/**
	 * Register Android application context for static usage.
	 *
	 * @param appContext mandatory application Context
	 * @throws RuntimeException if the context is already registered.
	 */
	public static final synchronized void init(final Context appContext) {
		init(appContext, null);
	}

	/**
	 * Register Android application context w/ a ViewGroup for static usage.
	 *
	 * @param appContext       mandatory application Context
	 * @param contentViewGroup optional ViewGroup acting as the Window's ContentView, usually a FrameLayout instance.
	 * @throws RuntimeException if the context is already registered.
	 */
	public static final synchronized void init(final Context appContext, final ViewGroup contentViewGroup) {
		//PJ re calling this method is fine
		/*if (null != StaticContext.appContext) {
			throw new RuntimeException("Context already set");
		}*/
		if (DEBUG) {Log.d(MD.TAG, "init(appCtx " + appContext + ", viewGroup " + contentViewGroup + ")");}
		//PJ weakref
		StaticContext.appContext = new WeakReference(appContext);
		StaticContext.contentViewGroup = new WeakReference(contentViewGroup);

	}

	/**
	 * Unregister the Android application Context and ViewGroup
	 */
	public static final synchronized void clear() {
		//PJ ignored as the context may still be wanted by other Activities
	/*	if (DEBUG) { Log.d(MD.TAG, "clear()"); }
		appContext = null;
		contentViewGroup = null;*/
	}

	/**
	 * Return the registered Android application Context
	 * @return
	 */
	public static final synchronized Context getContext() {
		//PJ weakref
		return appContext !=  null ? appContext.get() : null;
	}

	/**
	 * Return the registered Android ViewGroup acting as the Window's ContentView
	 * @return
	 */
	public static final synchronized ViewGroup getContentViewGroup(){
		//PJ weakref
		return contentViewGroup != null ? contentViewGroup.get() : null;
	}
}
