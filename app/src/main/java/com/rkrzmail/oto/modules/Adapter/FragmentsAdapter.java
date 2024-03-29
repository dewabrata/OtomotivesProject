package com.rkrzmail.oto.modules.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.naa.data.Nson;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.AlertPart_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.AturGajiUser_User_Fragment;
import com.rkrzmail.oto.modules.Fragment.AturUser_User_Fragment;
import com.rkrzmail.oto.modules.Fragment.BonusReferal_Referal_Fragment;
import com.rkrzmail.oto.modules.Fragment.Cash_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Checkin1_Checkin_Fragment;
import com.rkrzmail.oto.modules.Fragment.Dashboard_Angka_Fragment;
import com.rkrzmail.oto.modules.Fragment.Dashboard_Statistik_Fragment;
import com.rkrzmail.oto.modules.Fragment.EWallet_Rekening_Fragment;
import com.rkrzmail.oto.modules.Fragment.Fee_Billing_Fragment;
import com.rkrzmail.oto.modules.Fragment.HutangLain_Hutang_Fragment;
import com.rkrzmail.oto.modules.Fragment.HutangUsaha_Hutang_Fragment;
import com.rkrzmail.oto.modules.Fragment.Invoice_Piutang_Fragment;
import com.rkrzmail.oto.modules.Fragment.Jadwal_Kehadiran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Jadwal_Schedule_Fragment;
import com.rkrzmail.oto.modules.Fragment.KatalogLayanan_Checkin_Fragment;
import com.rkrzmail.oto.modules.Fragment.KomisiUser_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.OutSource_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartBengkel_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartKosong_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartNonLokasi_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartOto_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartTeralokasikan_Fragment;
import com.rkrzmail.oto.modules.Fragment.Permintaan_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Print_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Referal_Referal_Fragment;
import com.rkrzmail.oto.modules.Fragment.RekeningBank_Rekening_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.Setoran_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.StatusClaim_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tambahan_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.Terbayar_Billing_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tersedia_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Transaksi_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Transaksi_Piutang_Fragment;
import com.rkrzmail.oto.modules.Fragment.Usaha_Profile_Fragment;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.oto.modules.hutang.Hutang_MainTab_Activity;
import com.rkrzmail.oto.modules.hutang.Piutang_MainTab_Activity;

import java.util.ArrayList;

public class FragmentsAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private ArrayList<Fragment> fragments;
    private Bundle bundle;
    private String keyBundle;
    private Nson data;

    private String searchQueryText;
    private String searchViewTag;
    private int tabPosition;

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
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);

    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        if (context instanceof ProfileBengkel_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Usaha_Profile_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Tambahan_Profile_Fragment.class.getName());
                    break;
                case 2:
                    fragment = Fragment.instantiate(context, Schedule_Profile_Fragment.class.getName());
                    break;

            }
            return fragment;
        }

        if (context instanceof LokasiPart_MainTab_Activity_OLD) {
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
//                case 2:
//                    fragment = Fragment.instantiate(context, BatalPart_TugasPart_Fragment.class.getName());
//                    break;
                case 2:
                    fragment = Fragment.instantiate(context, PartKosong_TugasPart_Fragment.class.getName());
                    break;
                case 3:
                    fragment = Fragment.instantiate(context, OutSource_TugasPart_Fragment.class.getName());
                    break;
                case 4:
                    fragment = Fragment.instantiate(context, StatusClaim_TugasPart_Fragment.class.getName());
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
                    fragment = Fragment.instantiate(context, KomisiUser_Absensi_Fragment.class.getName());
                    break;
            }
            return fragment;
        }
        if (context instanceof Dashboard_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Dashboard_Angka_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Dashboard_Statistik_Fragment.class.getName());
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
                    fragment = Fragment.instantiate(context, AlertPart_PartHome_Fragment.class.getName());
                    break;
                case 2:
                    fragment = Fragment.instantiate(context, PartOto_PartHome_Fragment.class.getName());
                    break;

            }
            return fragment;
        }
        if (context instanceof Schedule_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Jadwal_Schedule_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Jadwal_Kehadiran_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if(context instanceof Piutang_MainTab_Activity){
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Transaksi_Piutang_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Invoice_Piutang_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof Hutang_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, HutangUsaha_Hutang_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, HutangLain_Hutang_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof AturUser_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, AturUser_User_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, AturGajiUser_User_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof Billing_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Terbayar_Billing_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, Fee_Billing_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof Referal_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Referal_Referal_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, BonusReferal_Referal_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof RekeningBank_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, RekeningBank_Rekening_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, EWallet_Rekening_Fragment.class.getName());
                    break;
            }
            return fragment;
        }

        if (context instanceof Checkin_MainTab_Activity) {
            switch (i) {
                case 0:
                    fragment = Fragment.instantiate(context, Checkin1_Checkin_Fragment.class.getName());
                    break;
                case 1:
                    fragment = Fragment.instantiate(context, KatalogLayanan_Checkin_Fragment.class.getName());
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
        if (context instanceof LokasiPart_MainTab_Activity_OLD) {
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
                    return "Tambahan";
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
                    return "Kosong";
                case 3:
                    return "Out";
                case 4:
                    return "Claim";
            }
        }
        if (context instanceof Pembayaran_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Transaksi";
                case 1:
                    return "Terbayar";
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
        if (context instanceof Dashboard_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Angka";
                case 1:
                    return "Statistik";
            }
        }

        if (context instanceof PartHome_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Bengkel";
                case 1:
                    return "Alert";
                case 2:
                    return "Otomotives";
            }
        }
        if (context instanceof Schedule_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Jadwal";
                case 1:
                    return "Kehadiran";
            }
        }
        if(context instanceof Piutang_MainTab_Activity){
            switch (position){
                case 0:
                    return "Transaksi";
                case 1:
                    return "Invoice";
            }
        }
        if (context instanceof Hutang_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Hutang Usaha";
                case 1:
                    return "Hutang Lain";
            }
        }

        if (context instanceof AturUser_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Personal";
                case 1:
                    return "Upah";
            }
        }

        if (context instanceof Billing_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Terbayar";
                case 1:
                    return "Fee";
            }
        }

        if (context instanceof Referal_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Referral";
                case 1:
                    return "Bonus";
            }
        }
        if (context instanceof RekeningBank_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Bank";
                case 1:
                    return "Wallet";
            }
        }

        if (context instanceof Checkin_MainTab_Activity) {
            switch (position) {
                case 0:
                    return "Check-In";
                case 1:
                    return "Paket";
            }
        }

        return null;
    }

    public void setTextQueryChanged(String newText, int position) {
        searchQueryText = newText;
        tabPosition = position;
    }
}
