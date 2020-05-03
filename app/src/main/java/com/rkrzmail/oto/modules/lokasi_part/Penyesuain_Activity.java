package com.rkrzmail.oto.modules.lokasi_part;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class Penyesuain_Activity extends AppActivity {

    private Spinner sp_penyesuaian, sp_noFolder_penyesuain, sp_ket_penyesuaian;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyesuain);
        initToolbar();
        sp_penyesuaian = findViewById(R.id.sp_penyesuaian);
        sp_noFolder_penyesuain = findViewById(R.id.sp_noFolder_penyesuaian);
        sp_ket_penyesuaian = findViewById(R.id.sp_ket_penyesuaian);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_penugasan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penyesuaian");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        find(R.id.btn_simpan_penyesuaian, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}
