package de.acepe.fritzstreams;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.LinkedList;

import de.acepe.fritzstreams.ui.fragments.CalendarFragment;
import de.acepe.fritzstreams.ui.fragments.SettingsFragment;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private final static String MENU_POSITION = "last_menu_position";
    private static final int PLAYER = 0;
    private static final int DOWNLOADS = 1;
    private static final int SETTINGS = 2;


    private LinkedList<Fragment> mFragments = new LinkedList<>();
    private ListView mDrawerList;
    private int mLastPosition = 0;
    private DrawerLayout mDrawerLayout;
    private boolean mInstanceSaved;
    private int mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mFragments.add(new CalendarFragment());
        mFragments.add(new SettingsFragment());

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        ArrayAdapter<String> navigationAdapter = new ArrayAdapter<>(this,
                R.layout.drawer_list_item,
                getResources().getStringArray(R.array.navigation_array));
        mDrawerList.setAdapter(navigationAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //if (savedInstanceState == null) {
        //   getFragmentManager().beginTransaction().add(R.id.container, new CalendarFragment()).commit();
        //}
        selectItem(mLastPosition, 0);

        //ActionBar actionBar = getActionBar();
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.YELLOW));

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle  outState) {
        outState.putInt(MENU_POSITION, mLastPosition);
        mInstanceSaved = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mInstanceSaved = false;

        mDrawerList
                .performItemClick(mDrawerList.getAdapter().getView(mLastPosition, null, null),
                        mLastPosition,
                        mDrawerList.getAdapter().getItemId(mLastPosition));
    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position != mLastPosition) {
                selectItem(position, 300);
            }
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(final int position, int delay) {
        mDrawerLayout.closeDrawer(mDrawerList);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mInstanceSaved) {
                    return;
                }
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                //ft.setCustomAnimations(R.animator.anim_fade_in, R.animator.anim_fade_out);

                switch (position) {
                    case PLAYER:
                        ft.replace(R.id.content_frame, mFragments.get(0)).commit();
                        mCurrentFragment = 0;
                        mLastPosition = position;
                        break;
                    case DOWNLOADS:
                        ft.replace(R.id.content_frame, mFragments.get(0)).addToBackStack(null).commit();
                        mCurrentFragment = 0;
                        mLastPosition = position;
                        break;
                    case SETTINGS:
                        ft.replace(R.id.content_frame, mFragments.get(1)).addToBackStack(null).commit();
                        mCurrentFragment = 1;
                        mLastPosition = position;
                        break;
                    default:
                        break;
                }
            }

        }, delay);
    }

}
