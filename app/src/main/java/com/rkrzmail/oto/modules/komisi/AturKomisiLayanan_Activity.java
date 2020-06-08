package com.rkrzmail.oto.modules.komisi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;

import java.util.ArrayList;

public class AturKomisiLayanan_Activity extends AppActivity {

    private MultiSelectionSpinner spPosisi;
    private ArrayList<String> dummies = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_komisiLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Komisi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPosisi = findViewById(R.id.sp_namaPosisi_komisiLayanan);
        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA");
    }
}
