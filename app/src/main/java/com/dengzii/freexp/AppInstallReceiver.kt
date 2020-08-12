package com.dengzii.freexp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        intent ?: return
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                println("Added=> ${intent.dataString}")
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                println("Removed=> ${intent.dataString}")
            }
            Intent.ACTION_PACKAGE_CHANGED -> {
                println("Changed=> ${intent.dataString}")
            }
        }
    }
}