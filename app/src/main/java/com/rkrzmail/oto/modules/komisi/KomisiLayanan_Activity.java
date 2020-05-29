package com.rkrzmail.oto.modules.komisi;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class KomisiLayanan_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_komisi_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_komisiLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = findViewById(R.id.fab_tambah_komisiLayanan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AturKomisiLayanan_Activity.class));
                finish();
            }
        });
    }

}
