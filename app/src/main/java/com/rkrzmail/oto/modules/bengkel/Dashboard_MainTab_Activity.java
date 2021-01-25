package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Dashboard_Angka_Fragment;
import com.rkrzmail.oto.modules.Fragment.Dashboard_Keuangan_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class Dashboard_MainTab_Activity extends AppActivity {

    ViewPager vpDashboard;
    TabLayout tabDashboard;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard__maintab);
        initToolbar();
        initComponent();
    }
    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Dasboard");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        vpDashboard = findViewById(R.id.vp_Dashboard);
        tabDashboard = findViewById(R.id.tablayout_Dashboard);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Dashboard_Angka_Fragment());
        fragments.add(new Dashboard_Keuangan_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpDashboard.setAdapter(pagerAdapter);
        vpDashboard.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabDashboard));
        tabDashboard.setupWithViewPager(vpDashboard);


    }
}