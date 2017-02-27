package de.acepe.fritzstreams;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.DownloadServiceAdapter;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.ui.fragments.CacheFragment;
import de.acepe.fritzstreams.ui.fragments.DownloadFragment;
import de.acepe.fritzstreams.ui.fragments.SettingsFragment;
import de.acepe.fritzstreams.ui.fragments.StreamsOverviewFragment;

public class MainActivity extends FragmentActivity
        implements
            ActionBar.TabListener,
            DownloadFragment.DownloadServiceAdapterSupplier,
            StreamsOverviewFragment.StreamsCache {

    private static final String TAG_CACHE_FRAGMENT = "CacheFragment";

    private ViewPager mViewPager;
    private ActionBar actionBar;
    private CacheFragment mCacheFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getActionBar();

        setContentView(R.layout.main_activity);

        FragmentManager fm = getSupportFragmentManager();
        mCacheFragment = (CacheFragment) fm.findFragmentByTag(TAG_CACHE_FRAGMENT);
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mCacheFragment == null) {
            Log.i(TAG_CACHE_FRAGMENT, "Cache Fragement created in Activity");
            mCacheFragment = new CacheFragment();
            fm.beginTransaction().add(mCacheFragment, TAG_CACHE_FRAGMENT).commit();
        }

        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        if (isTablet()) {
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.streams_frag_container, new StreamsOverviewFragment());
            ft.commit();

            android.support.v4.app.FragmentTransaction ft2 = fm.beginTransaction();
            ft2.add(R.id.downloads_frag_container, new DownloadFragment());
            ft2.commit();
        } else {
            List<Fragment> fragments = new ArrayList<>(3);
            fragments.add(new StreamsOverviewFragment());
            fragments.add(new DownloadFragment());
            fragments.add(new SettingsFragment());

            AppSectionsPagerAdapter mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(fm, fragments);
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mAppSectionsPagerAdapter);
            mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsIntent);
        return true;
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

    @Override
    public void addStream(StreamInfo streamInfo) {
        mCacheFragment.addStream(streamInfo);
    }

    @Override
    public StreamInfo getStream(StreamInfo.Stream stream, Calendar day) {
        return mCacheFragment.getStream(stream, day);
    }

    @Override
    public void scheduleDownload(DownloadInfo streamDownload) {
        mCacheFragment.scheduleDownload(streamDownload);
    }

    @Override
    public DownloadServiceAdapter getDownloader() {
        return mCacheFragment.getDownloadServiceAdapter();
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
