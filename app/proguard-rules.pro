# MinimalLauncher ProGuard rules

# Keep data classes serialized with Gson
-keep class com.minillauncher.utils.AppInfo { *; }

# Gson TypeToken reflection
-keepattributes Signature
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.reflect.TypeToken { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.** { *; }

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Activities (R8 can sometimes remove unused activities)
-keep class com.minillauncher.ui.** { *; }
