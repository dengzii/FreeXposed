package com.dengzii.freexp

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter

/**
 * author : dengzi
 * e-mail : master@dengzii.com
 * time   : 2020/8/12
 * desc   : none
 *
 */
class PackageFragment : Fragment() {

    private val mPackageInfoList = mutableListOf<PackageInfo>()
    private val mAdapter = SuperAdapter(mPackageInfoList)

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
        val view = inflater.inflate(R.layout.fragment_package_list, container)
        val rvPackageList = view.findViewById<RecyclerView>(R.id.rv_package_list)
        rvPackageList.layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false)
        rvPackageList.adapter = mAdapter
        mAdapter.addViewHolderForType(PackageInfo::class.java, PackageInfoViewHolder::class.java)
        return view
    }

    override fun onStart() {
        super.onStart()
        getInstalledPackage()
    }

    private fun getInstalledPackage() {
        val packageInfoList = context!!.packageManager.getInstalledPackages(0)
                .sortedBy {
                    val isSystem = (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) ==
                            ApplicationInfo.FLAG_SYSTEM
                    (it.firstInstallTime shl if (isSystem) 0 else 1) * -1
                }
        mPackageInfoList.addAll(packageInfoList)
        mAdapter.notifyDataSetChanged()
    }
}