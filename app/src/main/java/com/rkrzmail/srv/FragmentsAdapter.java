package com.rkrzmail.srv;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.naa.data.Nson;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.oto.modules.Fragment.TabSchedule_Fragment;
import com.rkrzmail.oto.modules.Fragment.TabTambahan_Fragment;
import com.rkrzmail.oto.modules.Fragment.TabUsaha_Fragment;
import com.rkrzmail.oto.modules.sparepart.LokasiPart_Activity;
import com.rkrzmail.oto.modules.Fragment.PartNonLokasi_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartTeralokasikan_Fragment;
import com.rkrzmail.oto.modules.Fragment.BatalPart_Tugas_Part_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartKosong_Tugas_Part_Fragment;
import com.rkrzmail.oto.modules.Fragment.Permintaan_Tugas_Part_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tersedia_TugasPart_Fragment;
import com.rkrzmail.oto.modules.sparepart.TugasPart_MainTab_Activity;

import java.util.ArrayList;

public class FragmentsAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private ArrayList<Fragment> fragments;
    private Bundle bundle;
    private String keyBundle;
    private Nson data;

    public FragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    public FragmentsAdapter(FragmentManager fm, Context context, ArrayList<Fragment> fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }

    public FragmentsAdapter(FragmentManager fm, Context context, ArrayList<Fragment> fragments, Nson data, String keyBundle) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
        this.keyBundle = keyBundle;
        this.data = data;
        this.bundle = setArguments(data, keyBundle);
    }

    public Bundle setArguments(Nson nson, String key){
        bundle = new Bundle();
        bundle.putSerializable(key, nson);
        return bundle;
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
                    PartTeralokasikan_Fragment teralokasikanFragment = new PartTeralokasikan_Fragment();
                    teralokasikanFragment.setArguments(setArguments(data, keyBundle));
                    return teralokasikanFragment;
                case 1:
                    fragment = Fragment.instantiate(context, PartNonLokasi_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if(context instanceof TugasPart_MainTab_Activity){
            switch (i){
                case 0:
                    Permintaan_Tugas_Part_Fragment permintaan_tugas_part_fragment = new Permintaan_Tugas_Part_Fragment();
                    permintaan_tugas_part_fragment.setArguments(setArguments(data, keyBundle));
                    return permintaan_tugas_part_fragment;
                case 1:
                    Tersedia_TugasPart_Fragment tersediaTugasPartFragment = new Tersedia_TugasPart_Fragment();
                    tersediaTugasPartFragment.setArguments(setArguments(data, keyBundle));
                    return tersediaTugasPartFragment;
                case 2:
                    fragment = Fragment.instantiate(context, BatalPart_Tugas_Part_Fragment.class.getName());
                    break;
                case 3:
                    fragment = Fragment.instantiate(context, PartKosong_Tugas_Part_Fragment.class.getName());
                    break;
            }
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
                    return "Usaha";
                case 1:
                    return "Tamabhan";
                case 2:
                    return "Schedule";
            }
        }
        if(context instanceof  TugasPart_MainTab_Activity){
            switch (position){
                case 0:
                    return "Permintaan";
                case 1:
                    return "Tersedia";
                case 2:
                    return "Batal";
                case 3:
                    return "Kosong";
            }
        }
        return null;
    }
}
