package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class AturPenjualanPart_Activity extends AppActivity {


    private static final int REQUEST_TOTAL_PART = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_penjualan_part_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_jualPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penjualan Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        find(R.id.et_jenisKendaraan_jualPart);
        find(R.id.et_namaPelanggan_jualPart);
        find(R.id.et_namaUsaha_jualPart);
        find(R.id.et_noPhone_jualPart);
        find(R.id.btn_lanjut_jualPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CariPart_Activity.class);
                setResult(RESULT_OK);
                startActivityForResult(intent, REQUEST_TOTAL_PART);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_TOTAL_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));

            nson.set("jenis", find(R.id.et_jenisKendaraan_jualPart, TextView.class).getText().toString());
            nson.set("namapelanggan", find(R.id.et_namaPelanggan_jualPart, TextView.class).getText().toString());
            nson.set("nusaha", find(R.id.et_namaUsaha_jualPart, TextView.class).getText().toString());
            nson.set("nohp", find(R.id.et_noPhone_jualPart, TextView.class).getText().toString());

            Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
            i.putExtra("part", nson.toJson());
            startActivity(i);
        }
    }
}

