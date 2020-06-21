package com.rkrzmail.oto.modules.sparepart.tugas_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;

public class JumlahPart_TugasPart_Activity extends AppActivity{

    private static final int REQUEST_BARCODE = 10;
    private EditText etStock, etNofolder, etJmJs, etJsJk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_part_tugas_part);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jumlah Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etStock = findViewById(R.id.et_stock_jumlahTp);
        etNofolder = findViewById(R.id.et_noFolder_jumlahTp);
        etJmJs = findViewById(R.id.et_jumlahMinta_jumlahSisa_jumlahTp);
        etJsJk = findViewById(R.id.et_jumlahSedia_jumlahKembali_jumlahTp);


    }

    private void loadData(){
        Nson n = Nson.readJson(getIntentStringExtra(""));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {

        }
    }
}
