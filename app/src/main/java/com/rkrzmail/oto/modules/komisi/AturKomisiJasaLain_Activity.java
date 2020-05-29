package com.rkrzmail.oto.modules.komisi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.R;

public class AturKomisiJasaLain_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_jasa_lain_);
        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_komisiJasaLain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Komisi Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
