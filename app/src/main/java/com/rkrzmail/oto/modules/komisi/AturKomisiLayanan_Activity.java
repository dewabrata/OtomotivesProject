package com.rkrzmail.oto.modules.komisi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.R;

public class AturKomisiLayanan_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_layanan_);
        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_komisiLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Komisi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
