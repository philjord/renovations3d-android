/**
 * Copyright 2010 JogAmp Community. All rights reserved.
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

package com.mindblowing.hudbasics.util;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;

public class NEWTFocusAdapter implements WindowListener, FocusEventCountAdapter {

    String prefix;
    int focusCount;
    boolean verbose = true;

    public NEWTFocusAdapter(final String prefix) {
        this.prefix = prefix;
        reset();
    }

    public void setVerbose(final boolean v) { verbose = false; }

    public boolean focusLost() {
        return focusCount<0;
    }

    public boolean focusGained() {
        return focusCount>0;
    }

    public void reset() {
        focusCount = 0;
    }

    public void windowGainedFocus(final WindowEvent e) {
        if(focusCount<0) { focusCount=0; }
        focusCount++;
        if( verbose ) {
            System.err.println("FOCUS NEWT GAINED [fc "+focusCount+"]: "+prefix+", "+e);
        }
    }

    public void windowLostFocus(final WindowEvent e) {
        if(focusCount>0) { focusCount=0; }
        focusCount--;
        if( verbose ) {
            System.err.println("FOCUS NEWT LOST   [fc "+focusCount+"]: "+prefix+", "+e);
        }
    }

    public void windowResized(final WindowEvent e) { }
    public void windowMoved(final WindowEvent e) { }
    public void windowDestroyNotify(final WindowEvent e) { }
    public void windowDestroyed(final WindowEvent e) { }
    public void windowRepaint(final WindowUpdateEvent e) { }

    public String toString() { return prefix+"[focusCount "+focusCount+"]"; }
}

