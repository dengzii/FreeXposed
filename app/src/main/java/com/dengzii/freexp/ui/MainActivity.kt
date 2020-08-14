package com.dengzii.freexp.ui

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dengzii.freexp.AppInfo
import com.dengzii.freexp.AppInstallReceiver
import com.dengzii.freexp.R
import com.dengzii.freexp.utils.ShellUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private val mViewProgress by lazy { findViewById<ViewGroup>(R.id.ll_progress) }
    private val mTvProgress by lazy { findViewById<TextView>(R.id.tv_progress) }
    private val mProgressBar by lazy { findViewById<ProgressBar>(R.id.progress) }

    private val mViewPager by lazy { findViewById<ViewPager2>(R.id.vp_viewpager) }
    private val mTabs by lazy { findViewById<TabLayout>(R.id.tl_tabs) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!ShellUtils.requestRoot(packageCodePath)) {
            Toast.makeText(this, "获取 Root 权限失败.", Toast.LENGTH_SHORT).show()
        }
        loadInstalledPackage()
    }

    private fun initView() {

        mViewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> PackageFragment.createAllPackageList()
                    1 -> PackageFragment.createEnableModuleList()
                    3 -> PackageFragment.createXpModuleList()
                    else -> PackageFragment.createXpModuleList()
                }
            }
        }
        TabLayoutMediator(mTabs, mViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Packages"
                1 -> "EnabledList"
                2 -> "ModuleList"
                else -> "-"
            }
        }.attach()
    }

    @SuppressLint("SetTextI18n")
    private fun loadInstalledPackage() {
        mViewProgress.visibility = View.VISIBLE
        val xpModuleList = mutableListOf<AppInfo>()
        val enableModuleList = mutableListOf<AppInfo>()
        Thread {
            val packageInfoList = packageManager.getInstalledPackages(0)
            val appCount = packageInfoList.size
            var current = 0
            val appInfoList = packageInfoList.map {
                AppInfo.fromPackageInfo(it, this).apply {
                    init(this@MainActivity)
                    if (isXpModule) {
                        xpModuleList.add(this)
                    }
                    if (configXpModules.isNotEmpty()) {
                        enableModuleList.add(this)
                    }
                    current++
                    runOnUiThread {
                        mTvProgress.text = "$current/$appCount\n ${this.packageName}"
                        mProgressBar.max = appCount
                        mProgressBar.progress = current
                    }
                }
            }.sortedBy {
                (it.updateTime shl if (it.isSystem) 0 else 1) * -1
            }
            AppInfo.sPackageList.clear()
            AppInfo.sPackageList.addAll(appInfoList)
            AppInfo.sXpModuleList.clear()
            AppInfo.sXpModuleList.addAll(xpModuleList)
            AppInfo.sModuleEnabledList.clear()
            AppInfo.sModuleEnabledList.addAll(enableModuleList)
            runOnUiThread {
                mViewProgress.visibility = View.GONE
                initView()
            }
        }.start()
    }

    private fun registerAppInstallReceiver() {
        val filter = IntentFilter()
        filter.addAction("android.intent.action.PACKAGE_ADDED")
        filter.addAction("android.intent.action.PACKAGE_REMOVED")
        filter.addAction("android.intent.action.PACKAGE_CHANGED")
        filter.addDataScheme("package")
        registerReceiver(AppInstallReceiver(), filter)
    }
}