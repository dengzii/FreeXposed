package com.dengzii.freexp

import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private val mViewPager by lazy { findViewById<ViewPager2>(R.id.vp_viewpager) }
    private val mTabs by lazy { findViewById<TabLayout>(R.id.tl_tabs) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!ShellUtils.requestRoot(packageCodePath)) {
            Toast.makeText(this, "获取 Root 权限失败.", Toast.LENGTH_SHORT).show()
        }
        initView()
    }

    private fun initView() {
        mViewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return PackageFragment.create()
            }
        }
        TabLayoutMediator(mTabs, mViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All"
                1 -> "Freed"
                2 -> "Other"
                else -> "-"
            }
        }.attach()
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