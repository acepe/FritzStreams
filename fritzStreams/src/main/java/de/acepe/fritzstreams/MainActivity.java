package de.acepe.fritzstreams;

import java.util.Calendar;
import java.util.LinkedList;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
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

    private final LinkedList<Fragment> mFragments = new LinkedList<>();
    private ViewPager mViewPager;
    private ActionBar actionBar;

    private CacheFragment mCacheFragment;
    private DownloadFragment mDownloadFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.mApp = getApplication();

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

        mDownloadFragment = new DownloadFragment();
        mFragments.add(new StreamsOverviewFragment());
        mFragments.add(mDownloadFragment);
        mFragments.add(new SettingsFragment());

        AppSectionsPagerAdapter mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(fm);

        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

        AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] tabTitles = getResources().getStringArray(R.array.navigation_array);
            return tabTitles[position];
        }
    }
}
