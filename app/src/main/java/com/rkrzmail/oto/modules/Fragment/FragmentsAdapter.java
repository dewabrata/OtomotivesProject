package com.rkrzmail.oto.modules.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.naa.data.Nson;
import com.rkrzmail.oto.modules.bengkel.Absensi_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.oto.modules.sparepart.LokasiPart_MainTab_Activity;
import com.rkrzmail.oto.modules.sparepart.OutSource_Activity;
import com.rkrzmail.oto.modules.sparepart.PartHome_MainTab_Activity;
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

        if (context instanceof LokasiPart_MainTab_Activity) {
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
                    Permintaan_TugasPart_Fragment permintaan_tugas_part_fragment = new Permintaan_TugasPart_Fragment();
                    permintaan_tugas_part_fragment.setArguments(setArguments(data, keyBundle));
                    return permintaan_tugas_part_fragment;
                case 1:
                    Tersedia_TugasPart_Fragment tersediaTugasPartFragment = new Tersedia_TugasPart_Fragment();
                    tersediaTugasPartFragment.setArguments(setArguments(data, keyBundle));
                    return tersediaTugasPartFragment;
                case 2:
                    fragment = Fragment.instantiate(context, BatalPart_TugasPart_Fragment.class.getName());
                    break;
                case 3:
                    fragment = Fragment.instantiate(context, PartKosong_TugasPart_Fragment.class.getName());
                    break;
                case 4:
                    fragment = Fragment.instantiate(context, OutSource_Activity.class.getName());
                    break;
            }
        }

        if (context instanceof Pembayaran_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Transaksi_Pembayaran_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Print_Pembayaran_Fragment.class.getName());
                    break;
                case 2:
                    fragment = Fragment.instantiate(context, Cash_Pembayaran_Fragment.class.getName());
                    break;
                case 3:
                    fragment = Fragment.instantiate(context, Setoran_Pembayaran_Fragment.class.getName());
                    break;

            }
            return fragment;
        }

        if (context instanceof Absensi_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Absen_Absensi_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Schedule_Absensi_Fragment.class.getName());
                    break;
                case 2:
                    fragment = Fragment.instantiate(context, Komisi_Absensi_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof PartHome_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, PartBengkel_PartHome_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, PartOto_PartHome_Activity.class.getName());
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
        if (context instanceof LokasiPart_MainTab_Activity) {
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
                    return "Permintaan Part";
                case 1:
                    return "Part Tersedia";
                case 2:
                    return "Part Batal";
                case 3:
                    return "Part Kosong";
                case 4:
                    return "OutSource";
            }
        }
        if (context instanceof Pembayaran_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Transaksi";
                case 1:
                    return "Bukti";
                case 2:
                    return "Cash";
                case 3:
                    return "Setoran";
            }
        }
        if (context instanceof Absensi_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Absen";
                case 1:
                    return "Schedule";
                case 2:
                    return "Komisi";
            }
        }

        if (context instanceof PartHome_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Bengkel";
                case 1:
                    return "Otomotives";
            }
        }
        return null;
    }
}
