package com.minillauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.Locale

object AppUtils {

    private const val TAG = "MinimalLauncher"

    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager

        Log.i(TAG, "Loading launchable apps...")

        // Method 1: Standard queryIntentActivities
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        var resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Method 1 failed: ${e.message}")
            emptyList()
        }

        Log.i(TAG, "Method 1 (queryIntentActivities): ${resolveInfos.size} apps")

        // Method 2: Fallback — get installed packages directly
        if (resolveInfos.isEmpty()) {
            Log.w(TAG, "Method 1 returned empty, trying getInstalledPackages fallback...")
            try {
                val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getInstalledPackages(0)
                }

                // Filter only packages that have a launcher activity
                resolveInfos = packages.filterNotNull().mapNotNull { pkgInfo ->
                    try {
                        val launchIntent = packageManager.getLaunchIntentForPackage(pkgInfo.packageName)
                        if (launchIntent != null) {
                            ResolveInfo().apply {
                                activityInfo = packageManager.getActivityInfo(launchIntent.component!!, 0)
                            }
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                Log.i(TAG, "Method 2 (getInstalledPackages): ${resolveInfos.size} apps")
            } catch (e: Exception) {
                Log.e(TAG, "Method 2 failed: ${e.message}")
            }
        }

        if (resolveInfos.isEmpty()) {
            Log.e(TAG, "CRITICAL: No apps found with either method!")
            return emptyList()
        }

        return resolveInfos
            .filter { it.activityInfo?.packageName != context.packageName }
            .mapNotNull { resolveInfo ->
                try {
                    val info = resolveInfo.activityInfo ?: return@mapNotNull null
                    AppInfo(
                        label = info.loadLabel(packageManager).toString(),
                        packageName = info.packageName,
                        className = info.name,
                        icon = info.loadIcon(packageManager)
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Skipping ${resolveInfo.activityInfo?.packageName}: ${e.message}")
                    null
                }
            }
            .sortedWith(compareBy { it.label.uppercase(Locale.getDefault()) })
    }

    fun launchApp(context: Context, appInfo: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = appInfo.componentName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch ${appInfo.packageName}: ${e.message}")
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
