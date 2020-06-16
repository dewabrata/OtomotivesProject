package com.rkrzmail.oto.modules.sparepart.pengembalian_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;

public class JumlahPengembalian_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_BARCODE = 10;
    private EditText etNopart, etNamapart, etJumlah;
    private Spinner spNofolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_pengembalian_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jumlah Pengembalian");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNopart = findViewById(R.id.et_noPart_jumlahPengembalian);
        etNamapart = findViewById(R.id.et_namaPart_jumlahPengembalian);
        etJumlah = findViewById(R.id.et_jumlah_jumlahPengembalian);
        spNofolder = findViewById(R.id.sp_noFolder_jumlahPengembalian);

        find(R.id.btn_scan_jumlahPengembalian, Button.class).setOnClickListener(this);
        find(R.id.btn_simpan_jumlahPengembalian, Button.class).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_scan_jumlahPengembalian:
                i = new Intent(getActivity(), BarcodeActivity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_BARCODE);
                break;
            case R.id.btn_simpan_jumlahPengembalian:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {

        }
    }
}
