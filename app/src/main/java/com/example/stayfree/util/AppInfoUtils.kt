package com.example.stayfree.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

object AppInfoUtils {

    data class InstalledApp(
        val packageName: String,
        val appName: String,
        val icon: Drawable?
    )

    fun getInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    appName = pm.getApplicationLabel(info).toString(),
                    icon = try { pm.getApplicationIcon(info.packageName) } catch (e: Exception) { null }
                )
            }
            .sortedBy { it.appName }
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
