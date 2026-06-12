package com.example.stayfree.util

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

object AppInfoUtils {

    data class InstalledApp(
        val packageName: String,
        val appName: String,
        val icon: Drawable?
    )

    /**
     * Lists launchable apps via the MAIN/LAUNCHER intent, matching the
     * <queries> declaration in the manifest. Works on API 30+ package
     * visibility filtering without QUERY_ALL_PACKAGES.
     */
    fun getInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(launcherIntent, 0)
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    appName = info.applicationInfo.loadLabel(pm).toString(),
                    icon = try { pm.getApplicationIcon(info.packageName) } catch (e: Exception) { null }
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
}
