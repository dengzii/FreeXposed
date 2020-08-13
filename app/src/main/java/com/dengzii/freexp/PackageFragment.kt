package com.dengzii.freexp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import java.io.File

/**
 * author : dengzi
 * e-mail : master@dengzii.com
 * time   : 2020/8/12
 * desc   : none
 *
 */
class PackageFragment : Fragment() {

    private lateinit var mView: View
    private val mViewProgress by lazy { mView.findViewById<ViewGroup>(R.id.ll_progress) }
    private val mTvProgress by lazy { mView.findViewById<TextView>(R.id.tv_progress) }
    private val mProgressBar by lazy { mView.findViewById<ProgressBar>(R.id.progress) }

    private val mPackageInfoList = mutableListOf<AppInfo>()
    private val mAdapter = SuperAdapter(mPackageInfoList)
    private val mXpModuleList = mutableListOf<AppInfo>()

    companion object {
        fun create(): Fragment {
            return PackageFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putInt("type", 1)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_package_list, container)
        val rvPackageList = mView.findViewById<RecyclerView>(R.id.rv_package_list)
        rvPackageList.layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false)
        rvPackageList.adapter = mAdapter
        mAdapter.addViewHolderForType(AppInfo::class.java, PackageInfoViewHolder::class.java)
        mAdapter.setOnItemClickListener { source, itemData, position, other ->
            if (source.id == R.id.cl_item) {
                onAppListClick(itemData as AppInfo)
            } else {

            }
        }
        getInstalledPackage()
        return mView
    }

    @SuppressLint("SetTextI18n")
    private fun getInstalledPackage() {
        mViewProgress.visibility = View.VISIBLE
        Thread {
            val packageInfoList = context!!.packageManager.getInstalledPackages(0)
            val appCount = packageInfoList.size
            var current = 0
            val appInfoList = packageInfoList.map {
                AppInfo.fromPackageInfo(it, activity!!).apply {
                    init(activity!!)
                    if (isXpModule) {
                        mXpModuleList.add(this)
                    }
                    current++
                    activity?.runOnUiThread {
                        mTvProgress.text = "$current/$appCount\n ${this.packageName}"
                        mProgressBar.max = appCount
                        mProgressBar.progress = current
                    }
                }
            }.sortedBy {
                (it.updateTime shl if (it.isSystem) 0 else 1) * -1
            }
            mPackageInfoList.addAll(appInfoList)
            activity?.runOnUiThread {
                mViewProgress.visibility = View.GONE
                mAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    private fun onAppListClick(appInfo: AppInfo) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(appInfo.appName)
        builder.setIcon(appInfo.icon)
        builder.setMessage(appInfo.toString())
        builder.setPositiveButton("Ok") { d, _ ->

        }
        builder.setNegativeButton("Start") { _, _ ->
            val intent = context!!.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent == null) {
                Toast.makeText(context, "Cannot start application", Toast.LENGTH_SHORT).show()
                return@setNegativeButton
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context!!.startActivity(intent)
        }
        builder.create().show()
    }

    private fun configModuleForPackage(appInfo: AppInfo, modules: List<String>) {
        val spDir = File(appInfo.spDir)
        if (!spDir.canWrite()) {
            if (!ShellUtils.chmod(spDir.absolutePath, 777).success) {
                Toast.makeText(context!!, "chmod shared_spref dir to 777 failed.",
                        Toast.LENGTH_SHORT).show()
                return
            }
        }
        val context = appInfo.createPackageContext(context!!)
        val sp = context.getSharedPreferences(FreeXpLoader.SP_FREE_XP_CONFIG, Context.MODE_PRIVATE)
        sp.edit().putStringSet(FreeXpLoader.KEY_FREE_XP_MODULE_LIST, modules.toSet()).apply()
    }
}