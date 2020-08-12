package com.dengzii.freexp

import android.content.Context
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
class AppHook(
        private val lpparam: XC_LoadPackage.LoadPackageParam,
        private val startupParam: IXposedHookZygoteInit.StartupParam)
    : XC_MethodHook() {

    private val methodHandleLoadPackage = "handleLoadPackage"
    private val methodInitZygote = "initZygote"

    @Throws(Throwable::class)
    override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)
        val context = param.args[0] as Context
        lpparam.classLoader = context.classLoader

        val sp = context.getSharedPreferences("free_xp_config", Context.MODE_PRIVATE)
        val modulePath = File(sp.getString("free_xp_module_class_path", ""))
        if (!modulePath.exists()) {
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
                    " Process: ${lpparam.processName}, reason : ${e.localizedMessage}")
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
            Logger.important("create package context exception start \r\n".plus("===".repeat(30)))
            Logger.e(e)
            throw Exception(e)
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
}