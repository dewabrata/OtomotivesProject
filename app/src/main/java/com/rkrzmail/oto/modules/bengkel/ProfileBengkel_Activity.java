package com.rkrzmail.oto.modules.bengkel;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Schedule_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tambahan_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.Usaha_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;

import java.util.ArrayList;

public class ProfileBengkel_Activity extends AppActivity {

    private ViewPager vpProfile;
    private TabLayout tabLayout;
    private ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        initToolbar();
        vpProfile = findViewById(R.id.vp);
        tabLayout = findViewById(R.id.tablayout);

        fragments = new ArrayList<>();
        fragments.add(new Usaha_Profile_Fragment());
        fragments.add(new Tambahan_Profile_Fragment());
        fragments.add(new Schedule_Profile_Fragment());

        FragmentsAdapter fragmentsAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments);

        vpProfile.setAdapter(fragmentsAdapter);
        vpProfile.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(vpProfile);
    }

}
