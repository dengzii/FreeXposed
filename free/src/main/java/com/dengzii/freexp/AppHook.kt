package com.dengzii.freexp

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Bundle
import dalvik.system.PathClassLoader
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

/**
 * author : dengzi
 * e-mail : master@dengzii.com
 * time   : 2020/8/12
 * desc   : none
 *
 */
internal class AppHook(
        private val lpparam: XC_LoadPackage.LoadPackageParam,
        private val startupParam: IXposedHookZygoteInit.StartupParam)
    : XC_MethodHook() {

    private val methodHandleLoadPackage = "handleLoadPackage"
    private val methodInitZygote = "initZygote"

    @Throws(Throwable::class)
    override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)
        val context = param.args[0] as Context
        val app = param.thisObject as? Application ?: return

        app.onActivityStarted {
            if (intent.hasExtra(FreeXpLoader.KEY_FREE_XP_MODULE_LIST)) {
                val list = intent.getStringArrayExtra(FreeXpLoader.KEY_FREE_XP_MODULE_LIST)
                println("==>${list.joinToString("|")}")
                AlertDialog.Builder(this)
                        .setTitle("FreeXp")
                        .setNegativeButton("Ok") { _, _ -> }
                        .setMessage(list.joinToString("\r\n"))
                        .create()
                        .show()
            }
        }
        return
        lpparam.classLoader = context.classLoader

        val sp = context.getSharedPreferences("free_xp_config", Context.MODE_PRIVATE)
        val modulePath = sp.getStringSet("free_xp_module_class", setOf())
        if (modulePath.isNotEmpty()) {
            return
        }

        try {
            val clazz = getApkClass(
                    context, "", ""
            )
            val obj = clazz.newInstance()
            try {
                clazz.getDeclaredMethod(methodInitZygote, startupParam.javaClass)
                        .invoke(obj, startupParam)
                XposedBridge.log("invoke initZygote success")
            } catch (e: NoSuchMethodException) {
                XposedBridge.log("No initZygote method found ! ignored ...")
            }
            clazz.getDeclaredMethod(methodHandleLoadPackage, lpparam::class.java)
                    .invoke(obj, lpparam)
            XposedBridge.log("invoke handleLoadPackage success.")
        } catch (e: Throwable) {
            Logger.important("XPOSED HOT LOAD FAILED!" +
                    " Process: ${lpparam.processName}, reason : ${e.message}")
        }
    }

    @Suppress("SameParameterValue")
    @Throws(Throwable::class)
    private fun getApkClass(
            context: Context,
            packageName: String,
            classPatch: String
    ): Class<*> {
        val moduleContext = try {
            context.createPackageContext(
                    packageName, Context.CONTEXT_INCLUDE_CODE
                    or Context.CONTEXT_IGNORE_SECURITY
                    or Context.CONTEXT_RESTRICTED
            )
        } catch (e: Throwable) {
            Logger.important("create package context exception")
            throw e
        }
        val apkPath = moduleContext.packageCodePath
        val apk = File(apkPath)
        if (!apk.exists()) {
            throw RuntimeException("Apk not found ! $apkPath")
        }
        Logger.log("load class from > apk path: $apkPath, package: $packageName, class: $classPatch")
        val pathClassLoader = PathClassLoader(apk.absolutePath, ClassLoader.getSystemClassLoader())
        return Class.forName(classPatch, true, pathClassLoader)
    }

    private fun Application.onActivityStarted(block: Activity.() -> Unit) {
        this.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallBack() {
            override fun onActivityStarted(p0: Activity?) {
                super.onActivityStarted(p0)
                if (p0 == null) return
                block.invoke(p0)
            }
        })
    }

    abstract class ActivityLifecycleCallBack : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(p0: Activity?) {
        }

        override fun onActivityResumed(p0: Activity?) {
        }

        override fun onActivityStarted(p0: Activity?) {
        }

        override fun onActivityDestroyed(p0: Activity?) {
        }

        override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
        }

        override fun onActivityStopped(p0: Activity?) {
        }

        override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
        }
    }
}