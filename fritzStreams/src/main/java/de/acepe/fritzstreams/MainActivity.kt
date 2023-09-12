package de.acepe.fritzstreams

import android.app.ActionBar
import android.app.FragmentTransaction
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.WindowManager
import de.acepe.fritzstreams.backend.StreamsModel
import de.acepe.fritzstreams.ui.fragments.DownloadFragment
import de.acepe.fritzstreams.ui.fragments.StreamsOverviewFragment
import de.acepe.fritzstreams.util.Utilities
import java.util.*

class MainActivity : FragmentActivity(), ActionBar.TabListener {

    private val model: StreamsModel by lazy {
        ViewModelProviders.of(this).get(StreamsModel::class.java)
    }

    private var mViewPager: ViewPager? = null

    private val isTablet: Boolean
        get() = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utilities.verifyStoragePermissions(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        setContentView(R.layout.main_activity)

        actionBar?.setHomeButtonEnabled(false)
        actionBar?.navigationMode = ActionBar.NAVIGATION_MODE_TABS

        val fm = supportFragmentManager
        if (isTablet) {
            val ft = fm.beginTransaction()
            ft.replace(R.id.streams_frag_container, StreamsOverviewFragment())
            ft.commit()

            val ft2 = fm.beginTransaction()
            ft2.replace(R.id.downloads_frag_container, DownloadFragment())
            ft2.commit()

        } else {
            val fragments = ArrayList<Fragment>(3)
            fragments.add(StreamsOverviewFragment())
            fragments.add(DownloadFragment())

            val mAppSectionsPagerAdapter = AppSectionsPagerAdapter(fm, fragments)
            mViewPager = findViewById(R.id.pager)
            mViewPager!!.adapter = mAppSectionsPagerAdapter
            mViewPager!!.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    actionBar!!.setSelectedNavigationItem(position)
                }
            })
            for (i in 0 until mAppSectionsPagerAdapter.count) {
                val tab = actionBar!!.newTab()
                        .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                        .setTabListener(this)
                actionBar!!.addTab(tab)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.downloadServiceAdapter.detachFromService()
    }

    override fun onTabUnselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}

    override fun onTabSelected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
        mViewPager!!.currentItem = tab.position
    }

    override fun onTabReselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}

    private inner class AppSectionsPagerAdapter internal constructor(fm: FragmentManager, private val mFragments: List<Fragment>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val tabTitles = resources.getStringArray(R.array.navigation_array)
            return tabTitles[position]
        }
    }
}
