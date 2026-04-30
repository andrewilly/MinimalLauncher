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

    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0) // no special flags
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }
        } catch (e: Exception) {
            Log.e("AppUtils", "Error loading apps: ${e.message}")
            emptyList()
        }

        Log.d("AppUtils", "Found ${resolveInfos.size} launchable apps")

        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .mapNotNull { resolveInfo ->
                try {
                    val info = resolveInfo.activityInfo
                    AppInfo(
                        label = info.loadLabel(packageManager),
                        packageName = info.packageName,
                        className = info.name,
                        icon = info.loadIcon(packageManager),
                        firstInstallTime = try {
                            @Suppress("DEPRECATION")
                            packageManager.getPackageInfo(info.packageName, 0).firstInstallTime
                        } catch (_: PackageManager.NameNotFoundException) { 0L }
                    )
                } catch (e: Exception) {
                    Log.w("AppUtils", "Skipping ${resolveInfo.activityInfo.packageName}: ${e.message}")
                    null
                }
            }
            .sortedWith(compareBy { it.label.toString().uppercase(Locale.getDefault()) })
    }

    fun launchApp(context: Context, appInfo: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = appInfo.componentName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        context.startActivity(intent)
    }

    fun openAppSettings(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openLauncherSettings(context: Context) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
