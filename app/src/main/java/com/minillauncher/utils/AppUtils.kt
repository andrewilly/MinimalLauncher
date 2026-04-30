package com.minillauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import java.util.Locale

/**
 * Utility for loading and launching apps.
 * Uses LauncherApps API (the correct API for launcher apps).
 */
object AppUtils {

    private const val TAG = "MinimalLauncher"

    /**
     * Load all launchable apps using the LauncherApps service.
     * This is the CORRECT way for a launcher to query apps.
     * queryIntentActivities may return empty on some devices.
     */
    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val startTime = System.currentTimeMillis()

        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            if (launcherApps == null) {
                Log.e(TAG, "LauncherApps service is null! Falling back to PackageManager.")
                return loadAppsFallback(context)
            }

            val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
            if (userManager == null) {
                Log.e(TAG, "UserManager service is null! Falling back to PackageManager.")
                return loadAppsFallback(context)
            }

            val profiles = userManager.userProfiles
            Log.d(TAG, "User profiles: ${profiles.size}")

            for (profile in profiles) {
                try {
                    val activityList = launcherApps.getActivityList(null, profile)
                    Log.d(TAG, "Profile ${profile}: ${activityList.size} apps")

                    for (launcherActivityInfo in activityList) {
                        try {
                            val appInfo = launcherActivityInfo.applicationInfo
                            val pkg = appInfo.packageName

                            // Skip ourselves
                            if (pkg == context.packageName) continue

                            val label = launcherActivityInfo.label.toString()
                            val className = launcherActivityInfo.componentName.className
                            val icon = context.packageManager.getApplicationIcon(pkg)
                            val firstInstallTime = try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    context.packageManager.getPackageInfo(
                                        pkg, PackageManager.PackageInfoFlags.of(0)
                                    ).firstInstallTime
                                } else {
                                    @Suppress("DEPRECATION")
                                    context.packageManager.getPackageInfo(pkg, 0).firstInstallTime
                                }
                            } catch (_: PackageManager.NameNotFoundException) { 0L }

                            apps.add(AppInfo(
                                label = label,
                                packageName = pkg,
                                className = className,
                                icon = icon,
                                firstInstallTime = firstInstallTime
                            ))
                        } catch (e: Exception) {
                            Log.w(TAG, "Error reading app: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading apps for profile: ${e.message}")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "LauncherApps failed, using fallback: ${e.message}")
            return loadAppsFallback(context)
        }

        // Sort alphabetically by label
        apps.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label.toString() })

        val elapsed = System.currentTimeMillis() - startTime
        Log.i(TAG, "Loaded ${apps.size} apps in ${elapsed}ms")
        return apps
    }

    /**
     * Fallback method using PackageManager for devices where LauncherApps doesn't work.
     */
    private fun loadAppsFallback(context: Context): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback also failed: ${e.message}")
            return apps
        }

        for (resolveInfo in resolveInfos) {
            try {
                val info = resolveInfo.activityInfo
                if (info.packageName == context.packageName) continue
                apps.add(AppInfo(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.packageName,
                    className = info.name,
                    icon = info.loadIcon(pm)
                ))
            } catch (e: Exception) {
                Log.w(TAG, "Fallback skip: ${e.message}")
            }
        }

        apps.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label.toString() })
        Log.i(TAG, "Fallback loaded ${apps.size} apps")
        return apps
    }

    /**
     * Launch an app by its package name and activity class.
     * Uses LauncherApps (proper launcher API).
     */
    fun launchApp(context: Context, appInfo: AppInfo) {
        try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            if (launcherApps != null) {
                val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
                val profile = userManager?.userProfiles?.firstOrNull() ?: android.os.Process.myUserHandle()
                launcherApps.startMainActivity(appInfo.componentName, profile, null, null)
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "LauncherApps launch failed: ${e.message}")
        }

        // Fallback to Intent-based launch
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = appInfo.componentName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback launch failed: ${e.message}")
        }
    }

    fun openAppSettings(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
