package com.example.android.x_packrat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Responsible for creating and displaying the appropriate fragment when the user changes the active
 * tab
 */
public class BelongingsTabAdapter extends FragmentPagerAdapter {

    public BelongingsTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch(position){
            case 0:
                return new BelongingsFragment();
            case 1:
                return new SoldFragment();
            case 2:
                return new DiscardedFragment();
            case 3:
                return new DonatedFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
