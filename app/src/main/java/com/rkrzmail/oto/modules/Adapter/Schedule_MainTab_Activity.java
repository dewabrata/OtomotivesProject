package com.rkrzmail.oto.modules.Adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Jadwal_Kehadiran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Jadwal_Schedule_Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class Schedule_MainTab_Activity extends AppActivity {

    ViewPager vpSchedule;
    TabLayout tabAbsensi;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        vpSchedule = findViewById(R.id.vp);
        tabAbsensi = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Jadwal_Schedule_Fragment());
        fragments.add(new Jadwal_Kehadiran_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpSchedule.setAdapter(pagerAdapter);
        vpSchedule.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabAbsensi));
        tabAbsensi.setupWithViewPager(vpSchedule);

    }

}