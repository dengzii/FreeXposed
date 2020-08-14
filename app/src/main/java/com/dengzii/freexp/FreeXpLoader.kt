package com.dengzii.freexp

import android.app.Application
import android.content.Context
import android.os.SystemClock
import com.dengzii.freexp.utils.XpLogUtils
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class FreeXpLoader : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        const val KEY_FREE_XP_MODULE_LIST = "KEY_FREE_XP_MODULE_LIST"
        const val SP_FREE_XP_CONFIG = "FREE_XP_CONFIG"
    }

    private lateinit var startupParam: StartupParam

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        val mProcessName = lpparam.processName
        if (SystemClock.elapsedRealtime() < 40000) {
            XposedBridge.log("FreeXp waiting for system startup complete, boot elapsed time: "
                    + SystemClock.elapsedRealtime())
            return
        }
        if (lpparam.appInfo == null || lpparam.appInfo.uid == 1000) {
            XpLogUtils.simple("FreeXp skip app : ${lpparam.packageName} > $mProcessName")
            return
        }
//        val isSystem = lpparam.appInfo.flags and
//                (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1
        XpLogUtils.simple("FreeXp start hook")
        try {
            XposedHelpers.findAndHookMethod(Application::class.java,
                    "attach", Context::class.java,
                    AppHook(lpparam, startupParam))
        } catch (e: Throwable) {
            XpLogUtils.simple("FreeXp hook Application failed: ${e.message}")
        }
    }

    @Throws(Throwable::class)
    override fun initZygote(startupParam: StartupParam) {
        XpLogUtils.simple("XposedLoader.initZygote, ${startupParam.modulePath}")
        this.startupParam = startupParam
    }
}