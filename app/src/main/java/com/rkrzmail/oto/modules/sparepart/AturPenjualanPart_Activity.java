package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.part_keluar.PengisianPartKeluar_Activity;

public class AturPenjualanPart_Activity extends AppActivity {


    private static final int REQUEST_CARI_PART = 11;
    private static final int REQEST_DAFTAR_JUAL = 12;

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
        find(R.id.btn_lanjut_jualPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CariPart_Activity.class);
                intent.putExtra("flag", "ALL");
                startActivityForResult(intent, REQUEST_CARI_PART);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));
            nson.set("jenis", find(R.id.et_jenisKendaraan_jualPart, TextView.class).getText().toString());
            nson.set("namapelanggan", find(R.id.et_namaPelanggan_jualPart, TextView.class).getText().toString());
            nson.set("nusaha", find(R.id.et_namaUsaha_jualPart, TextView.class).getText().toString());
            nson.set("nohp", find(R.id.et_noPhone_jualPart, TextView.class).getText().toString());

            Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
            i.putExtra("part", nson.toJson());
            //Log.d("datanihcuy", "data "+  nson.toJson());
            startActivityForResult(i, REQEST_DAFTAR_JUAL);
        }else if (resultCode == RESULT_OK && requestCode == REQEST_DAFTAR_JUAL) {
            Log.d("datanihcuy", "data "+  Nson.readJson(getIntentStringExtra(data, "part")));
            Intent i = new Intent(getActivity(), DaftarJualPart_Activity.class);
            i.putExtra("data",  Nson.readJson(getIntentStringExtra(data, "part")).toJson());
            startActivityForResult(i, PenjualanPart_Activity.REQUEST_PENJUALAN);
        }else if(resultCode == RESULT_OK && requestCode == PenjualanPart_Activity.REQUEST_PENJUALAN){
            setResult(RESULT_OK);
            finish();
        }
    }
}

