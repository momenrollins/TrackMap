package com.houseofdevelopment.gps.utils

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.houseofdevelopment.gps.R


class ViewPagerAdapter(
    val context: Context,
    fm: FragmentManager,
   private val fragmentList : ArrayList<Fragment>
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        if (position == 0) {
            title = context.getString(R.string.single)
        } else if (position == 1) {
            title = context.getString(R.string.group)
        }
        return title
    }
}
