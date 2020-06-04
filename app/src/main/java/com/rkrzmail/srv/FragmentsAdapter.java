package com.rkrzmail.srv;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.rkrzmail.oto.modules.lokasi_part.PartNonLokasi_Fragment;
import com.rkrzmail.oto.modules.lokasi_part.PartTeralokasikan_Fragment;

import java.util.ArrayList;

public class FragmentsAdapter extends FragmentStatePagerAdapter {

    Context context;
    ArrayList<Fragment> fragments;

    public FragmentsAdapter(FragmentManager fm, Context context, ArrayList<Fragment> fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i) {
            case 0:
                fragment = Fragment.instantiate(context, PartTeralokasikan_Fragment.class.getName());
                break;
            case 1:
                fragment = Fragment.instantiate(context, PartNonLokasi_Fragment.class.getName());
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Lokasi";
            case 1:
                return "Non Lokasi";
        }
        return null;
    }
}
