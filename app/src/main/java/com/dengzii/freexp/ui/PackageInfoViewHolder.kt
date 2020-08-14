package com.dengzii.freexp.ui

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dengzii.adapter.AbsViewHolder
import com.dengzii.freexp.AppInfo
import com.dengzii.freexp.R

class PackageInfoViewHolder(p: ViewGroup) : AbsViewHolder<AppInfo>(p) {

    private val mIvIcon by lazy { findViewById<ImageView>(R.id.iv_package_icon) }
    private val mIvXpModule by lazy { findViewById<ImageView>(R.id.iv_is_module) }
    private val mIvSettings by lazy { findViewById<ImageView>(R.id.iv_settings) }
    private val mTvPackage by lazy { findViewById<TextView>(R.id.tv_package_name) }
    private val mTvLabel by lazy { findViewById<TextView>(R.id.tv_package_label) }
    private val mTvVersion by lazy { findViewById<TextView>(R.id.tv_package_version) }
    private val mClItem by lazy { findViewById<View>(R.id.cl_item) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_package_info)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindData(data: AppInfo, position: Int) {

        mTvLabel.text = data.appName
        mIvIcon.setImageDrawable(data.icon)
        mTvPackage.text = data.packageName
        mTvVersion.text = data.versionName
        mIvXpModule.visibility = if (data.isXpModule) View.VISIBLE else View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mIvSettings.imageTintList = context.resources.getColorStateList(
                    if (data.configXpModules.isNotEmpty()) {
                        R.color.colorPrimaryLight
                    } else {
                        android.R.color.darker_gray
                    },
                    context.theme)
        }
        if (data.isSystem) {
            mTvLabel.setTextColor(context.resources.getColor(R.color.colorAccent))
        } else {
            mTvLabel.setTextColor(context.resources.getColor(android.R.color.black))
        }
        mClItem.setOnClickListener {
            onViewClick(it, null)
        }
        mIvSettings.setOnClickListener {
            onViewClick(it, null)
        }
    }

}