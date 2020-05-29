package com.rkrzmail.oto.modules.layanan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class DeskripsiLayanan_Activiy extends AppActivity {

    //    private TextView tvNamaLayanan, tvJenisLayanan, tvNamPrincipal, tvPelaksana, tvLokasi, tvKendaraan, tvMerk,
//                    tvJenis, tvVarian, tvModel, tvBatasan, tvBiayaJasa, tvPenggantian, tv
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deskripsi_layanan__activiy);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_deskripsi_layanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Deskripsi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        Intent i = getIntent();
        Nson data = Nson.readJson(getIntentStringExtra("deskripsi"));

        find(R.id.tv_namalayanan_deskripsiLayanan, TextView.class).setText(data.get("namalayanan").asString());
        find(R.id.tv_jenislayanan_deskripsiLayanan, TextView.class).setText(data.get("jenislayanan").asString());
    }
}
