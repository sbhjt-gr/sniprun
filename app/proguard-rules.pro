# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# BeanShell specific ProGuard rules
-keep class bsh.** { *; }
-dontwarn bsh.**
-keepattributes Signature
-keepattributes *Annotation*

# Keep reflection-related methods
-keepclassmembers class * {
    java.lang.reflect.Method *;
}

# Preserve classes used by BeanShell for interpretation
-keep class java.lang.reflect.** { *; }
-keep class java.util.** { *; }
-keep class java.io.** { *; }
-keep class java.math.** { *; }