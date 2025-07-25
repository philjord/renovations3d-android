# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\phil\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# if you get warnings that stop proguard building you can swap all -dontwarn below to this catch all statement
#-ignorewarnings



#-optimizations code/simplification/arithmetic
#-optimizations code/simplification/cast
#-optimizations field/*
#-optimizations class/merging/*


#jogl
-dontwarn jogamp.opengl.**
-dontwarn com.jogamp.opengl.**
-dontwarn com.jogamp.common.util.awt.**
-dontwarn com.jogamp.nativewindow.**

#my code
-dontwarn com.eteks.sweethome3d.HomeFrame
-dontwarn com.eteks.sweethome3d.HomeFramePane**
-dontwarn com.eteks.sweethome3d.Renovations3D**
-dontwarn com.eteks.sweethome3d.viewcontroller.HelpController**
-dontwarn com.eteks.sweethome3d.model.HomeFurnitureGroup
-dontwarn com.eteks.sweethome3d.plugin.**
-dontwarn com.eteks.sweethome3d.swing.**

# please KEEP ALL THE NAMES
-keepnames class ** { *; }
-dontobfuscate


#if any class not found issues occured replace all "-keep" lines below with these 2 lines
# -keep class com.jogamp.** { *; }
# -keep class jogamp.** { *; }

#gluegen-rt-android.jar
-keep class jogamp.common.os.android.AndroidUtilsImpl { *; }

#joal-android.jar
-keep class com.jogamp.openal.** { *; }
-keep class jogamp.openal.** { *; }

#jogl-all-android.jar
-keep class com.jogamp.nativewindow.egl.EGLGraphicsDevice { *; }
-keep class com.jogamp.opengl.egl.** { *; }


-keep class jogamp.graph.font.typecast.TypecastFontConstructor { *; }
-keep class jogamp.graph.curve.opengl.shader.** { *; }

-keep class jogamp.newt.driver.** { *; }
-keep class jogamp.opengl.** { *; }


#in fact flag it
-keep class com.eteks.sweethome3d.** { *; }
-keep class renovations3d.** { *; }
-keep class javaawt.** { *; }
-keep class javaswing.** { *; }
-keep class sun.util.calendar.ZoneInfo { *; }



-dontwarn android.security.**
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }


