package com.dengzii.freexp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.dengzii.adapter.AbsViewHolder

class PackageInfoViewHolder(p: ViewGroup) : AbsViewHolder<PackageInfo>(p) {

    private val mIvIcon by lazy { findViewById<ImageView>(R.id.iv_package_icon) }
    private val mTvPackage by lazy { findViewById<TextView>(R.id.tv_package_name) }
    private val mTvLabel by lazy { findViewById<TextView>(R.id.tv_package_label) }
    private val mTvVersion by lazy { findViewById<TextView>(R.id.tv_package_version) }
    private val mClItem by lazy { findViewById<View>(R.id.cl_item) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_package_info)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindData(data: PackageInfo, position: Int) {
        val isSystemApp = (data.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) ==
                ApplicationInfo.FLAG_SYSTEM

        mTvLabel.text = data.applicationInfo.loadLabel(context.packageManager).toString()
        mIvIcon.setImageDrawable(data.applicationInfo.loadIcon(context.packageManager))
        mTvPackage.text = data.applicationInfo.packageName
        mTvVersion.text = data.versionName
        if (isSystemApp) {
            mTvLabel.setTextColor(context.resources.getColor(R.color.colorAccent))
        } else {
            mTvLabel.setTextColor(context.resources.getColor(android.R.color.black))
        }
        mClItem.setOnClickListener {
            showDetailInfo(data)
        }
    }

    private fun showDetailInfo(packageInfo: PackageInfo) {
        val builder = AlertDialog.Builder(context)
        val info = StringBuilder()
        info.append("versionName: ").append(packageInfo.versionName)
                .append("\r\n")
                .append("firstInstallTime: ").append(packageInfo.firstInstallTime)
                .append("\r\n")
                .append("activities: ").append(packageInfo.activities?.size ?: -1)
                .append("\r\n")
                .append("lastUpdateTime: ").append(packageInfo.lastUpdateTime)
                .append("\r\n")
                .append("packageName: ").append(packageInfo.packageName)
                .append("\r\n")
                .append("sharedUserId: ").append(packageInfo.sharedUserId)
                .append("\r\n")
                .append("sourceDir: ").append(packageInfo.applicationInfo.sourceDir)
                .append("\r\n")
                .append("nativeLibraryDir: ").append(packageInfo.applicationInfo.nativeLibraryDir)
                .append("\r\n")
                .append("dataDir: ").append(packageInfo.applicationInfo.dataDir)
                .append("\r\n")
                .append("processName: ").append(packageInfo.applicationInfo.processName)
                .append("\r\n")
                .append("className: ").append(packageInfo.applicationInfo.className)
                .append("\r\n")
                .append("uid: ").append(packageInfo.applicationInfo.uid)
                .append("\r\n")
                .append("enabled: ").append(packageInfo.applicationInfo.enabled)
                .append("\r\n")
                .append("targetSdkVersion: ").append(packageInfo.applicationInfo.targetSdkVersion)

        builder.setTitle(packageInfo.applicationInfo.loadLabel(context.packageManager).toString())
        builder.setIcon(packageInfo.applicationInfo.loadIcon(context.packageManager))
        builder.setMessage(info.toString())
        builder.setPositiveButton("OK") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("Start") { d, _ ->
            val intent = context.packageManager.getLaunchIntentForPackage(packageInfo.packageName)
            if (intent == null) {
                Toast.makeText(context, "Cannot start application", Toast.LENGTH_SHORT).show()
                return@setNegativeButton
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            intent.putExtra("", "")
            context.startActivity(intent)
        }
        builder.create().show()
    }
}