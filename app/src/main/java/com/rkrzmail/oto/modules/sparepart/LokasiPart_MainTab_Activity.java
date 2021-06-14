package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.PartNonLokasi_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartTeralokasikan_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LokasiPart_MainTab_Activity extends AppActivity {

    private static final String TAG = "LokasiPart_Activity";
    public static final int REQUEST_ATUR = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lokasi_part);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Ruang Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();

        ViewPager vpLokasiPart = findViewById(R.id.vp_lokasiPart);
        TabLayout tabLayout = findViewById(R.id.tablayout_lokasiPart);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new PartTeralokasikan_Fragment());
        fragments.add(new PartNonLokasi_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments);
        vpLokasiPart.setAdapter(pagerAdapter);
        vpLokasiPart.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(vpLokasiPart);
        //tabLayout.addOnTabSelectedListener();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_ATUR){

        }
//            ((PartTeralokasikan_Fragment) getSupportFragmentManager().getFragments()).getTeralokasikan("");
//            ((PartNonLokasi_Fragment) getSupportFragmentManager().getFragments()).getNonTeralokasikan("");
    }
}
