package com.example.android.x_packrat;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.design.widget.TabLayout;

import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.sync.ReminderUtilities;
import com.example.android.x_packrat.utilities.NotificationUtils;

/**
 * Houses all four of the apps fragments. Launched when the app is launched.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle(getString(R.string.belongings_tab_title));
        }

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        BelongingsTabAdapter adapter = new BelongingsTabAdapter(getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Sets the appropriate title in the action bar based on the currently active tab
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        getSupportActionBar().setTitle(getString(R.string.belongings_tab_title));
                        break;
                    case 1:
                        getSupportActionBar().setTitle(getString(R.string.sold_tab_title));
                        break;
                    case 2:
                        getSupportActionBar().setTitle(getString(R.string.discarded_tab_title));
                        break;
                    case 3:
                        getSupportActionBar().setTitle(getString(R.string.donated_tab_title));
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_action_icons8_basketball_36);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_icons8_money_bag_36);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_action_icons8_waste_36);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_action_icons8_empty_box_36);
    }

    // temp method used to test notification and check its appearance
    public void testNotification(View v) {
        NotificationUtils.remindOfLongUnusedBelonging(this);
    }
}
