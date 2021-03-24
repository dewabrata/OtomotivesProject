package com.rkrzmail.oto.modules.bengkel;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.part.AdapterListBasic;
import com.rkrzmail.oto.modules.Fragment.AturUser_User_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.AturGajiUser_User_Fragment;

import java.util.ArrayList;

public class AturUser_MainTab_Activity extends AppActivity {

    private RecyclerView recyclerView;
    private AdapterListBasic mAdapter;

    private String isSuccess = "";

    ViewPager vpAbsensi;
    TabLayout tabAbsensi;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent(){
        vpAbsensi = findViewById(R.id.vp);
        tabAbsensi = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new AturUser_User_Fragment());
        fragments.add(new AturGajiUser_User_Fragment());


        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpAbsensi.setAdapter(pagerAdapter);
        vpAbsensi.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabAbsensi));
        tabAbsensi.setupWithViewPager(vpAbsensi);
    }


}
