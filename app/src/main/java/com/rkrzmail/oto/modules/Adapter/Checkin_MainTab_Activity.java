package com.rkrzmail.oto.modules.Adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Checkin1_Checkin_Fragment;
import com.rkrzmail.oto.modules.Fragment.KatalogLayanan_Checkin_Fragment;
import com.rkrzmail.oto.modules.bengkel.Billing_Activity;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Objects;

public class Checkin_MainTab_Activity extends AppActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }

        if (getSetting("STATUS_BENGKEL").equals("BATAL") || getSetting("STATUS_BENGKEL").equals("NON AKTIVE")) {
            Intent intent = new Intent(getActivity(), Billing_Activity.class);
            showNotification(getActivity(), "Billing", "Billing Belum Terbayar", "CHECKIN", intent);
            showWarning("ANDA TIDAK BISA MELAKUKAN TRANSAKSI!");
            Tools.setViewAndChildrenEnabled(find(R.id.ly_checkin1, LinearLayout.class), false);
        }

        initToolbar();
        setTab();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setTab() {
        viewPager = findViewById(R.id.vp);
        tabLayout = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Checkin1_Checkin_Fragment());
        fragments.add(new KatalogLayanan_Checkin_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
    }
}
