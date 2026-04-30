package com.minillauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Locale

/**
 * Utility object for loading and managing installed applications.
 */
object AppUtils {

    /**
     * Load all launchable apps installed on the device.
     * Filters out this launcher itself from the list.
     */
    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }

        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val label = activityInfo.loadLabel(packageManager)
                val icon = activityInfo.loadIcon(packageManager)

                AppInfo(
                    label = label,
                    packageName = activityInfo.packageName,
                    className = activityInfo.name,
                    icon = icon,
                    firstInstallTime = try {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(activityInfo.packageName, 0).firstInstallTime
                    } catch (e: PackageManager.NameNotFoundException) {
                        0L
                    }
                )
            }
            .sortedWith(compareBy<AppInfo> { it.label.toString().uppercase(Locale.getDefault()) })
    }

    /**
     * Launch an application by its package and class name.
     */
    fun launchApp(context: Context, appInfo: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = appInfo.componentName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        context.startActivity(intent)
    }

    /**
     * Open the system app settings page for a specific app.
     */
    fun openAppSettings(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Open the system settings page for this launcher.
     */
    fun openLauncherSettings(context: Context) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
