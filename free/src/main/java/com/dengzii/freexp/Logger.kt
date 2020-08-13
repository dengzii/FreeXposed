package com.dengzii.freexp

import de.robv.android.xposed.XposedBridge


object Logger {
    var processName = "-"

    fun simple(log: Any) {
        XposedBridge.log(log.toString())
    }

    fun log(log: Any) {
        val rLog = log.toString().replace("\r\n", "\r\n║ ")
        XposedBridge.log(
            "FreeXp/$processName  ${getCallerClassName()}\r\n" +
                    "╔═══════════════════════════════════ VERBOSE ══════════════════════════════════\r\n" +
                    "║ $rLog\r\n" +
                    "╚══════════════════════════════════════════════════════════════════════════════\r\n"
        )
    }

    fun important(log: Any) {
        val rLog = log.toString().replace("\r\n", "\r\n║ ")
        XposedBridge.log(
            "FreeXp/$processName  ${getCallerClassName()}\r\n" +
                    "################################## IMPORTANT ##################################\r\n" +
                    "## $rLog\r\n" +
                    "###############################################################################\r\n"
        )
    }

    fun e(e: Throwable) {
        XposedBridge.log(
            "FreeXp/$processName  ${getCallerClassName()}\r\n" +
                    "********************************** EXCEPTION **********************************\r\n" +
                    "** ${e.message}\r\n" +
                    "*******************************************************************************\r\n"
        )
        XposedBridge.log(e)
    }

    private fun getCallerClassName(): String {
        val stackTraceElement = Thread.currentThread().stackTrace
        val s = if (stackTraceElement.size >= 5) stackTraceElement[4] else stackTraceElement[2]
        return s.className + "." + s.methodName + "(" + s.fileName + ":" + s.lineNumber + ")"
    }
}