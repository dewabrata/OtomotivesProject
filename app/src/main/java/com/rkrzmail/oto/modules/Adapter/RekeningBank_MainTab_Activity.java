package com.rkrzmail.oto.modules.Adapter;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Dashboard_Angka_Fragment;
import com.rkrzmail.oto.modules.Fragment.Dashboard_Statistik_Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class RekeningBank_MainTab_Activity extends AppActivity {

    ViewPager vp;
    TabLayout tabLayout;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        setComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Rekening");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setComponent(){
        vp = findViewById(R.id.vp);
        tabLayout = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Dashboard_Angka_Fragment());
        fragments.add(new Dashboard_Statistik_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vp.setAdapter(pagerAdapter);
        vp.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(vp);
    }
}
