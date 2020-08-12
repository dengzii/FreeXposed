package com.dengzii.freexp

import android.content.Context
import android.content.pm.ApplicationInfo
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class FreeXpLoader : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private lateinit var startupParam: StartupParam

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        val mProcessName = lpparam.processName
        val isSystem = lpparam.appInfo.flags and
                (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1

        if (lpparam.appInfo == null || isSystem) {
            Logger.simple("Skip system application : ${lpparam.packageName} > $mProcessName")
            return
        }
        Logger.log("FreeXp start hook")

        try {
            val applicationClass = lpparam.classLoader.loadClass("android.app.Application")
            XposedHelpers.findAndHookMethod(applicationClass, "attach", Context::class.java,
                    AppHook(lpparam, startupParam))
        } catch (e: Throwable) {
            Logger.important("FreeXp load module failed: ${e.message}")
            Logger.e(e)
        }
    }

    @Throws(Throwable::class)
    override fun initZygote(startupParam: StartupParam) {
        XposedBridge.log("XposedLoader.initZygote, ${startupParam.modulePath}")
        this.startupParam = startupParam
    }
}