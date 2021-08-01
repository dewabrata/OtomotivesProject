package com.rkrzmail.oto.modules.Adapter;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.BonusReferal_Referal_Fragment;
import com.rkrzmail.oto.modules.Fragment.Referal_Referal_Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class Referal_MainTab_Activity extends AppActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        setTab();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Referral");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setTab() {
        viewPager = findViewById(R.id.vp);
        tabLayout = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Referal_Referal_Fragment());
        fragments.add(new BonusReferal_Referal_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
    }
}
