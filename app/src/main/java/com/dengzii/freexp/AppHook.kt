package com.dengzii.freexp

import android.content.Context
import com.dengzii.freexp.utils.XpLogUtils
import dalvik.system.PathClassLoader
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
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
        XpLogUtils.simple("FreeXp hooked app: ${context.packageName}")

        val spf = context.getSharedPreferences(FreeXpLoader.SP_FREE_XP_CONFIG, Context.MODE_PRIVATE)
        val modules = spf.getStringSet(FreeXpLoader.KEY_FREE_XP_MODULE_LIST, emptySet())
        if (modules.isNullOrEmpty()) {
            XpLogUtils.simple("FreeXp sp config not found.")
            return
        }
        lpparam.classLoader = context.classLoader
        modules.forEach {
            loadModule(context, it)
        }
    }

    private fun loadModule(context: Context, moduleLocation: String) {
        val module = moduleLocation.split("#")
        if (module.size != 2) {
            XpLogUtils.simple("FreeXp load module failed: $moduleLocation")
            return
        }
        XpLogUtils.simple("FreeXp load module: $moduleLocation")
        try {
            val clazz = getApkClass(context, module[0], module[1])
            val obj = clazz.newInstance()
            try {
                clazz.getDeclaredMethod(methodInitZygote, startupParam.javaClass)
                        .invoke(obj, startupParam)
            } catch (e: NoSuchMethodException) {

            }
            clazz.getDeclaredMethod(methodHandleLoadPackage, lpparam::class.java)
                    .invoke(obj, lpparam)
        } catch (e: Throwable) {
            XpLogUtils.important("FreeXp load module failed!" +
                    " process: ${lpparam.processName}, reason : ${e.message}")
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
            XpLogUtils.important("FreeXp create package context exception")
            throw e
        }
        val apkPath = moduleContext.packageCodePath
        val apk = File(apkPath)
        if (!apk.exists()) {
            throw RuntimeException("FreeXp module apk not found ! $apkPath")
        }
        val pathClassLoader = PathClassLoader(apk.absolutePath, ClassLoader.getSystemClassLoader())
        return Class.forName(classPatch, true, pathClassLoader)
    }
}