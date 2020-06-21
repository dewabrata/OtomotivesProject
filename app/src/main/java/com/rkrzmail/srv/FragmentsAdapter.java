package com.rkrzmail.srv;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.rkrzmail.oto.modules.sparepart.lokasi_part.LokasiPart_Activity;
import com.rkrzmail.oto.modules.sparepart.lokasi_part.PartNonLokasi_Fragment;
import com.rkrzmail.oto.modules.sparepart.lokasi_part.PartTeralokasikan_Fragment;
import com.rkrzmail.oto.modules.profile.ProfileBengkel_Activity;
import com.rkrzmail.oto.modules.profile.TabSchedule_Fragment;
import com.rkrzmail.oto.modules.profile.TabTambahan_Fragment;
import com.rkrzmail.oto.modules.profile.TabUsaha_Fragment;

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

        if (context instanceof ProfileBengkel_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, TabUsaha_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, TabTambahan_Fragment.class.getName());
                    break;
                case 2:
                    fragment = Fragment.instantiate(context, TabSchedule_Fragment.class.getName());
                    break;

            }
            return fragment;
        }

        if (context instanceof LokasiPart_Activity) {
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

        return fragment;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (context instanceof LokasiPart_Activity) {
            switch (position) {
                case 0:
                    return "Lokasi";
                case 1:
                    return "Non Lokasi";
            }
        }
        if (context instanceof ProfileBengkel_Activity) {
            switch (position) {
                case 0:
                    return "USAHA";
                case 1:
                    return "TAMBAHAN";
                case 2:
                    return "SCHEDULE";
            }
        }
        return null;
    }
}
