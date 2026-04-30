# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.minillauncher.** { *; }
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
