package com.dengzii.freexp.utils

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

object ShellUtils {

    class CommandResult(
            var result: Int,
            private var successMsg: String?,
            private var errorMsg: String?
    ) {
        val success = result == 0

        companion object {
            fun permissionDenied(): CommandResult {
                return CommandResult(-1, "", "Permission Denied")
            }
        }

        override fun toString(): String {
            return "CommandResult(result=$result, msg=$successMsg$errorMsg)"
        }
    }

    private const val COMMAND_SU = "su"
    private const val COMMAND_SH = "sh"
    private const val COMMAND_EXIT = "exit\n"
    private const val COMMAND_LINE_END = "\n"

    fun requestRoot(packageCodePath: String): Boolean {
        return chmod(packageCodePath, 777).success
    }

    fun chmod(path: String, mod: Int): CommandResult {
        return if (checkRootPermission()) {
            return execCommand("chmod $mod $path", true)
        } else {
            CommandResult.permissionDenied()
        }
    }

    fun checkRootPermission(): Boolean {
        return execCommand("echo root", isRoot = true, isNeedResultMsg = false).result == 0
    }

    fun execCommand(command: String, isRoot: Boolean): CommandResult {
        return execCommand(arrayOf(command), isRoot, true)
    }

    fun execCommand(
            commands: List<String>,
            isRoot: Boolean
    ): CommandResult {
        return execCommand(
                commands.toTypedArray(),
                isRoot, true
        )
    }

    fun execCommand(
            command: String, isRoot: Boolean,
            isNeedResultMsg: Boolean
    ): CommandResult {
        return execCommand(
                arrayOf(command),
                isRoot,
                isNeedResultMsg
        )
    }

    fun execCommand(
            commands: List<String>,
            isRoot: Boolean, isNeedResultMsg: Boolean
    ): CommandResult {
        return execCommand(
                commands.toTypedArray(),
                isRoot, isNeedResultMsg
        )
    }

    @JvmOverloads
    fun execCommand(
            commands: Array<String>?, isRoot: Boolean,
            isNeedResultMsg: Boolean = true
    ): CommandResult {
        var result = -1
        if (commands == null || commands.isEmpty()) {
            return CommandResult(result, null, null)
        }
        var process: Process? = null
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        var successMsg: StringBuilder? = null
        var errorMsg: StringBuilder? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec(
                    if (isRoot) COMMAND_SU else COMMAND_SH
            )
            os = DataOutputStream(process.outputStream)
            for (command in commands) {
                os.write(command.toByteArray())
                os.writeBytes(COMMAND_LINE_END)
                os.flush()
            }
            os.writeBytes(COMMAND_EXIT)
            os.flush()
            result = process.waitFor()
            // get command result
            if (isNeedResultMsg) {
                successMsg = StringBuilder()
                errorMsg = StringBuilder()
                successResult = BufferedReader(
                        InputStreamReader(
                                process.inputStream
                        )
                )
                errorResult = BufferedReader(
                        InputStreamReader(
                                process.errorStream
                        )
                )
                var s: String?
                while (successResult.readLine().also { s = it } != null) {
                    successMsg.append(s)
                }
                while (errorResult.readLine().also { s = it } != null) {
                    errorMsg.append(s)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
                successResult?.close()
                errorResult?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            process?.destroy()
        }
        Log.d("ShellUtils", "execCommand: ${commands.joinToString("")}," +
                " code=$result, suc=$successMsg, err=$errorMsg")
        return CommandResult(
                result,
                successMsg?.toString(),
                errorMsg?.toString()
        )
    }
}