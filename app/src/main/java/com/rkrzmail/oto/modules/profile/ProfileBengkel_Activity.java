package com.rkrzmail.oto.modules.profile;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.FragmentsAdapter;

import java.util.ArrayList;

public class ProfileBengkel_Activity extends AppActivity {

    private ViewPager vpProfile;
    private TabLayout tabLayout;
    private ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_bengkel_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        initToolbar();
        vpProfile = findViewById(R.id.vp_profile);
        tabLayout = findViewById(R.id.tablayout_profile);

        fragments = new ArrayList<>();
        fragments.add(new TabUsaha_Fragment());
        fragments.add(new TabTambahan_Fragment());
        fragments.add(new TabUsaha_Fragment());

        FragmentsAdapter fragmentsAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments);

        vpProfile.setAdapter(fragmentsAdapter);
        vpProfile.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(vpProfile);
    }

}
