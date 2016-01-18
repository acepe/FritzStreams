package de.acepe.fritzstreams;

import java.util.LinkedList;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import de.acepe.fritzstreams.backend.Downloader;
import de.acepe.fritzstreams.ui.fragments.CalendarFragment;
import de.acepe.fritzstreams.ui.fragments.DownloadFragment;
import de.acepe.fritzstreams.ui.fragments.SettingsFragment;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private ViewPager mViewPager;
    private LinkedList<Fragment> mFragments = new LinkedList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.mApp = getApplication();
        App.downloader = new Downloader(this);

        setContentView(R.layout.main_activity);

        mFragments.add(new CalendarFragment());
        mFragments.add(new DownloadFragment());
        mFragments.add(new SettingsFragment());

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();
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

    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
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
