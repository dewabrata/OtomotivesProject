package com.rkrzmail.oto.modules.pembayaran;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class RincianLayanan_Activity extends AppActivity {

    private TextView tvNamaP, tvNoPhone, tvNopol, tvLayanan, tvFrek, tvTotalLayanan, tvPart, tvJasaPart, tvJasaLain, tvDisc,
            tvPenyimpanan, tvTotal, tvPpn, tvDp, tvTotalBiaya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_layanan_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_rincianLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rincian Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        tvNamaP = findViewById(R.id.tv_namaP_rincianLayanan);
        tvNoPhone = findViewById(R.id.tv_noPhone_rincianLayanan);
        tvNopol = findViewById(R.id.tv_nopol_rincianLayanan);
        tvLayanan = findViewById(R.id.tv_layanan_rincianLayanan);
        tvFrek = findViewById(R.id.tv_frek_rincianLayanan);
        tvTotalLayanan = findViewById(R.id.tv_totalLayanan_rincianLayanan);
        tvPart = findViewById(R.id.tv_totalPart_rincianLayanan);
        tvJasaPart = findViewById(R.id.tv_jasaPart_rincianLayanan);
        tvJasaLain = findViewById(R.id.tv_jasaLain_rincianLayanan);
        tvDisc = findViewById(R.id.tv_disc_rincianLayanan);
        tvPenyimpanan = findViewById(R.id.tv_penyimpanan_rincianLayanan);
        tvTotal = findViewById(R.id.tv_total_rincianLayanan);
        tvPpn = findViewById(R.id.tv_ppn_rincianLayanan);
        tvDp = findViewById(R.id.tv_dp_rincianLayanan);
        tvTotalBiaya = findViewById(R.id.tv_totalBiaya_rincianLayanan);

        find(R.id.cb_buangPart_rincianLayanan, CheckBox.class);
        find(R.id.cb_aj_rincianLayanan, CheckBox.class);
        find(R.id.cb_derek_rincianLayanan, CheckBox.class);

        find(R.id.btn_lanjut_rincianLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void load() {
        Nson n = Nson.readJson(getIntentStringExtra("layanan"));
    }

}
