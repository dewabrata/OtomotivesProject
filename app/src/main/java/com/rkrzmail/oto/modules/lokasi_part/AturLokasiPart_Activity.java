package com.rkrzmail.oto.modules.lokasi_part;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Spinner;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class AturLokasiPart_Activity extends AppActivity {

    private EditText no_part, nama_part, merk_part;
    private Spinner sp_lokasi_part, sp_penempatan_part, sp_noRak_part, sp_tinggiRak_part, sp_noFolder_part;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_lokasi_part);
        initToolbar();

        no_part = findViewById(R.id.et_no_part);
        nama_part = findViewById(R.id.et_nama_part);
        merk_part = findViewById(R.id.et_merk_part);
        sp_lokasi_part = findViewById(R.id.sp_lokasiPart);
        sp_penempatan_part = findViewById(R.id.sp_norakPalet_part);
        sp_noRak_part = findViewById(R.id.sp_norakPalet_part);
        sp_tinggiRak_part = findViewById(R.id.sp_tinggiRak);
        sp_noFolder_part = findViewById(R.id.sp_noFolder_part);

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_lokasi_part);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Lokasi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

    }
}
