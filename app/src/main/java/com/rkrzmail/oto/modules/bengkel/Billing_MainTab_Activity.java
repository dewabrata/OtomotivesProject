package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Fee_Billing_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.KomisiUser_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Terbayar_Billing_Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class Billing_MainTab_Activity extends AppActivity {

    ViewPager vpBilling;
    TabLayout tabBilling;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        setComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Billing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setComponent(){
        vpBilling = findViewById(R.id.vp);
        tabBilling = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Terbayar_Billing_Fragment());
        fragments.add(new Fee_Billing_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpBilling.setAdapter(pagerAdapter);
        vpBilling.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabBilling));
        tabBilling.setupWithViewPager(vpBilling);
    }
}