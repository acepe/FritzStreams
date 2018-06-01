package de.acepe.fritzstreams;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import de.acepe.fritzstreams.ui.fragments.DownloadFragment;
import de.acepe.fritzstreams.ui.fragments.StreamsOverviewFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
        implements
        ActionBar.TabListener {

    private static final String TAG_CACHE_FRAGMENT = "CacheFragment";

    private ViewPager mViewPager;
    private ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        actionBar = getActionBar();

        setContentView(R.layout.main_activity);


        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        FragmentManager fm = getSupportFragmentManager();
        if (isTablet()) {
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.streams_frag_container, new StreamsOverviewFragment());
            ft.commit();

            android.support.v4.app.FragmentTransaction ft2 = fm.beginTransaction();
            ft2.replace(R.id.downloads_frag_container, new DownloadFragment());
            ft2.commit();

        } else {
            List<Fragment> fragments = new ArrayList<>(3);
            fragments.add(new StreamsOverviewFragment());
            fragments.add(new DownloadFragment());

            AppSectionsPagerAdapter mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(fm, fragments);
            mViewPager = findViewById(R.id.pager);
            mViewPager.setAdapter(mAppSectionsPagerAdapter);
            mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
            });
            for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
                ActionBar.Tab tab = actionBar.newTab()
                        .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                        .setTabListener(this);
                actionBar.addTab(tab);
            }
        }
    }

    private boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;

        AppSectionsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] tabTitles = getResources().getStringArray(R.array.navigation_array);
            return tabTitles[position];
        }
    }
}
