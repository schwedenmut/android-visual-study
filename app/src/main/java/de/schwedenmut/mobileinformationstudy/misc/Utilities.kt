package de.schwedenmut.mobileinformationstudy.misc

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.util.*



object Utilities {

    fun isPermissionGranted(context: Context?): Boolean {
        var permissionGranted = false

        val appOps = context?.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode: Int = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        when (AppOpsManager.MODE_DEFAULT) {
            mode -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionGranted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
            }
            else -> permissionGranted = (mode == AppOpsManager.MODE_ALLOWED)
        }
        return permissionGranted
    }

    fun isServiceRunning(classname: String, context: Context): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
            if (classname == service.service.className) {
                return true
            }
        }
        return false
    }

    @SuppressLint("WrongConstant")
    fun getRecentPackage(context: Context): String {

        val usageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        } else {
            context.getSystemService("usagestats") as UsageStatsManager
        }
        var recentTime: Long = 0
        var recentPkg = ""
        val beginCalendar = Calendar.getInstance()
        beginCalendar.timeInMillis = System.currentTimeMillis() - 2000
        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = System.currentTimeMillis()
        val queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCalendar.timeInMillis, endCalendar.timeInMillis)
        queryUsageStats.forEach {
            if (it.lastTimeStamp > recentTime) {
                recentTime = it.lastTimeStamp
                recentPkg = it.packageName
            }
            //Log.d("recentPkg", recentPkg + " - " + recentTime)
        }
        return recentPkg
    }

    fun saveBitmap(bitmap: Bitmap, file: File, compressFormat: Bitmap.CompressFormat) {
        val out = FileOutputStream(file)
        bitmap.compress(compressFormat, 100, out)
        out.flush()
        out.close()
    }
}
