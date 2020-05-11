package com.rkrzmail.oto.modules.jurnal;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class AturJurnal_Activity extends AppActivity {

    private Button date, mulaiSewa, selesaiSewa, kontak, simpan;
    private Spinner pos, jenisTransaksi;
    private EditText et_tanggalJurnal, et_mulai_sewa, et_selesai_sewa, et_nota;
    private TextInputLayout tl_date, tl_mulai_sewa, tl_selesai_sewa;
    private LinearLayout ly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_jurnal_);

        initToolbar();
        initComponent();

    }

    private void initToolbar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_jurnal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        date = findViewById(R.id.btn_date_jurnal);
        mulaiSewa = findViewById(R.id.btn_mulai_sewa);
        selesaiSewa = findViewById(R.id.btn_selesai_sewa);
        kontak = findViewById(R.id.btn_kontak_jurnal);
        simpan = findViewById(R.id.btn_simpan_jurnal);
        pos = findViewById(R.id.sp_pos_jurnal);
        jenisTransaksi = findViewById(R.id.sp_jenis_jurnal);
        et_tanggalJurnal = findViewById(R.id.et_tanggal_jurnal);
        et_mulai_sewa = findViewById(R.id.et_mulai_sewa);
        et_selesai_sewa = findViewById(R.id.et_selesai_sewa);
        et_nota = findViewById(R.id.et_nota_jurnal);
        tl_date = findViewById(R.id.tl_date);
        tl_mulai_sewa = findViewById(R.id.tl_mulai_sewa);
        tl_selesai_sewa = findViewById(R.id.tl_selesai_sewa);
        ly = findViewById(R.id.ly_btn_date_jurnal);


    }
}
