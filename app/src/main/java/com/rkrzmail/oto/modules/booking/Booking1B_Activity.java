package com.rkrzmail.oto.modules.booking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class Booking1B_Activity extends AppActivity {

    private Spinner spLokasiLayanan;
    private EditText etLonglat, etAlamat;
    private CheckBox cbAntar, cbJemput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking1_b_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking1b);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking 1B");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spLokasiLayanan = findViewById(R.id.sp_pekerjaan_booking1b);
        etLonglat = findViewById(R.id.et_longlat_booking1b);
        etAlamat = findViewById(R.id.et_alamat_booking1b);
        cbAntar = findViewById(R.id.cb_antar_booking1b);
        cbJemput = findViewById(R.id.cb_jemput_booking1b);

        find(R.id.btn_radius_booking1b, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        find(R.id.btn_lanjut_booking1b, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        find(R.id.btn_batal_booking1b, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
