package com.dengzii.freexp.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import com.dengzii.freexp.AppInfo
import com.dengzii.freexp.FreeXpLoader
import com.dengzii.freexp.R
import com.dengzii.freexp.utils.ShellUtils
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
    private val mPackageInfoList = mutableListOf<AppInfo>()
    private val mAdapter = SuperAdapter(mPackageInfoList)
    private val mListType by lazy { arguments?.getInt("type") ?: -1 }

    companion object {

        fun createAllPackageList() = create(1)
        fun createXpModuleList() = create(2)
        fun createEnableModuleList() = create(3)

        private fun create(type: Int): Fragment {
            return PackageFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putInt("type", type)
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
        mAdapter.setOnItemClickListener { source, itemData, position, _ ->
            if (source.id == R.id.cl_item) {
                onAppClick(itemData as AppInfo)
            } else {
                onConfigModuleClick(itemData as AppInfo, position)
            }
        }
        mPackageInfoList.addAll(
                when (mListType) {
                    1 -> AppInfo.sPackageList
                    2 -> AppInfo.sXpModuleList
                    3 -> AppInfo.sModuleEnabledList
                    else -> AppInfo.sXpModuleList
                }
        )
        mAdapter.notifyDataSetChanged()
        return mView
    }

    override fun onResume() {
        super.onResume()
        if (mListType == 3) {
            mPackageInfoList.clear()
            mPackageInfoList.addAll(AppInfo.sModuleEnabledList)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun onConfigModuleClick(appInfo: AppInfo, pos: Int) {
        if (AppInfo.sXpModuleList.isEmpty()) {
            Toast.makeText(context, "No xp module found.", Toast.LENGTH_SHORT).show()
            return
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Module")
        val allModule =
                AppInfo.sXpModuleList.map {
                    it.appName
                }.toTypedArray()

        val enableModuleList =
                AppInfo.sXpModuleList.map {
                    appInfo.configXpModules.contains(it.xpModuleName)
                }.toBooleanArray()
        builder.setMultiChoiceItems(allModule, enableModuleList) { _, index, selected ->
            enableModuleList[index] = selected
        }
        builder.setPositiveButton("SAVE") { d, _ ->
            val selected = AppInfo.sXpModuleList
                    .mapNotNull { it.xpModuleName }
                    .filterIndexed { index, _ ->
                        enableModuleList[index]
                    }
            configModuleForPackage(appInfo, selected)
            d.dismiss()
            mAdapter.notifyItemChanged(pos)
        }
        builder.create().show()
    }

    private fun onAppClick(appInfo: AppInfo) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(appInfo.appName)
        builder.setIcon(appInfo.icon)
        builder.setMessage(appInfo.toString())
        builder.setPositiveButton("OK") { d, _ ->
            d.dismiss()
        }
        builder.setNegativeButton("LAUNCH") { _, _ ->
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
            if (!spDir.exists()) {
                if (spDir.parentFile?.canWrite() != true &&
                        !ShellUtils.chmod(spDir.parentFile!!.absolutePath, 777).success) {
                    Toast.makeText(context!!, "chmod data dir to 777 failed.",
                            Toast.LENGTH_SHORT).show()
                    return
                }
                spDir.mkdir()
            }
            if (!ShellUtils.chmod(spDir.absolutePath, 777).success) {
                Toast.makeText(context!!, "chmod shared_spref dir to 777 failed.",
                        Toast.LENGTH_SHORT).show()
                return
            }
        }
        val context = appInfo.createPackageContext(context!!)
        val sp = context.getSharedPreferences(FreeXpLoader.SP_FREE_XP_CONFIG, Context.MODE_PRIVATE)
        sp.edit().putStringSet(FreeXpLoader.KEY_FREE_XP_MODULE_LIST, modules.toSet()).apply()
        appInfo.configXpModules.clear()
        appInfo.configXpModules.addAll(modules)

        if (modules.isNotEmpty()) {
            AppInfo.sModuleEnabledList.add(appInfo)
        } else if (AppInfo.sModuleEnabledList.contains(appInfo)) {
            AppInfo.sModuleEnabledList.remove(appInfo)
        }
    }
}