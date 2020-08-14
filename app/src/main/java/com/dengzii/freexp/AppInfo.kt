package com.dengzii.freexp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.util.Log
import com.dengzii.freexp.utils.ShellUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * author : dengzi
 * e-mail : master@dengzii.com
 * time   : 2020/8/13
 * desc   : none
 *
 */
@Suppress("unused")
class AppInfo(private val packageInfo: PackageInfo, context: Context) {

    val versionName = packageInfo.versionName
    val packageName = packageInfo.packageName
    val sourceDir = packageInfo.applicationInfo.sourceDir
    val installTime = packageInfo.firstInstallTime
    val updateTime = packageInfo.lastUpdateTime
    val sharedUserId = packageInfo.sharedUserId
    val nativeLibDir = packageInfo.applicationInfo.nativeLibraryDir
    val dataDir = packageInfo.applicationInfo.dataDir
    val processName = packageInfo.applicationInfo.processName
    val className = packageInfo.applicationInfo.className
    val uid = packageInfo.applicationInfo.uid
    val targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion
    val taskAffinity = packageInfo.applicationInfo.taskAffinity
    val enabled = packageInfo.applicationInfo.enabled
    val spDir = "$dataDir/shared_prefs/"

    val isSystem = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) ==
            ApplicationInfo.FLAG_SYSTEM

    var configXpModules = mutableListOf<String>()
    var appName = ""
    lateinit var icon: Drawable
    var isXpModule = false
    var xpModuleName: String? = null

    companion object {
        val sPackageList = mutableSetOf<AppInfo>()
        val sXpModuleList = mutableSetOf<AppInfo>()
        val sModuleEnabledList = mutableSetOf<AppInfo>()

        fun fromPackageInfo(packageInfo: PackageInfo, context: Context): AppInfo {
            return AppInfo(packageInfo, context)
        }
    }

    fun init(context: Context) {
        try {
            val packageContext = createPackageContext(context)
            loadXpModuleInfo(packageContext)
            loadConfiguredXpModules(packageContext)

        } catch (e: Throwable) {
            Log.e(AppInfo::class.java.simpleName, "getXpModule: ", e)
        }
        icon = packageInfo.applicationInfo.loadIcon(context.packageManager)
        appName = packageInfo.applicationInfo.loadLabel(context.packageManager).toString()
    }

    fun createPackageContext(context: Context): Context {
        return context.createPackageContext(
                packageName, Context.CONTEXT_IGNORE_SECURITY
                or Context.CONTEXT_RESTRICTED)
    }

    @Throws(Throwable::class)
    private fun loadXpModuleInfo(packageContext: Context) {
        val assetManager = packageContext.resources.assets
        if (assetManager.list("")?.contains("xposed_init") != true) {
            return
        }
        xpModuleName = try {
            val input = assetManager.open("xposed_init")
            val reader = BufferedReader(InputStreamReader(input))
            packageName + "#" + reader.readLine()
        } catch (e: Throwable) {
            Log.e(AppInfo::class.java.simpleName, "loadXpModuleInfo: ", e)
            null
        }
        isXpModule = !xpModuleName.isNullOrBlank()
    }

    @Throws(Throwable::class)
    private fun loadConfiguredXpModules(packageContext: Context) {
        val spDirFile = File(spDir)
        if (!spDirFile.exists()) {
            return
        }
        if (!spDirFile.canWrite()) {
            if (!ShellUtils.chmod(spDirFile.absolutePath, 777).success) {
//                throw java.lang.Exception("chmod shared_prefs to 777 failed.")
                return
            }
        }
        val sp = packageContext.getSharedPreferences(
                FreeXpLoader.SP_FREE_XP_CONFIG, Context.MODE_PRIVATE)
        val xpConfig = sp.getStringSet(FreeXpLoader.KEY_FREE_XP_MODULE_LIST, emptySet()).orEmpty()
        configXpModules.addAll(xpConfig)
    }

    override fun toString(): String {
        val info = StringBuilder()
        info.append("versionName: ").append(versionName)
                .append("\r\n")
                .append("firstInstallTime: ").append(installTime)
                .append("\r\n")
                .append("activities: ").append(packageInfo.activities?.size ?: -1)
                .append("\r\n")
                .append("lastUpdateTime: ").append(updateTime)
                .append("\r\n")
                .append("packageName: ").append(packageName)
                .append("\r\n")
                .append("sharedUserId: ").append(sharedUserId)
                .append("\r\n")
                .append("sourceDir: ").append(sourceDir)
                .append("\r\n")
                .append("nativeLibraryDir: ").append(nativeLibDir)
                .append("\r\n")
                .append("dataDir: ").append(dataDir)
                .append("\r\n")
                .append("processName: ").append(processName)
                .append("\r\n")
                .append("className: ").append(className)
                .append("\r\n")
                .append("uid: ").append(uid)
                .append("\r\n")
                .append("enabled: ").append(enabled)
                .append("\r\n")
                .append("targetSdkVersion: ").append(targetSdkVersion)
        return info.toString()
    }
}