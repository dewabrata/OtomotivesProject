package com.rkrzmail.oto.modules.pembayaran;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class RincianPembelian_Activity extends AppActivity {

    private RecyclerView rvRinciBeli;
    private TextView tvNamaP, tvNoPhone, tvNamaUsaha, tvFrek, tvTotalPart, tvTotalDisc,
            tvTotal, tvTotalBiaya, tvPpn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_pembelian);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_rincianPembelian);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rincian Pembelian Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        tvNamaP = findViewById(R.id.tv_namaP_rincianPembelian);
        tvNoPhone = findViewById(R.id.tv_noPhone_rincianPembelian);
        tvNamaUsaha = findViewById(R.id.tv_namaUsaha_rincianPembelian);
        tvFrek = findViewById(R.id.tv_frek_rincianPembelian);
        tvTotalPart = findViewById(R.id.tv_totalPart_rincianPembelian);
        tvTotalDisc = findViewById(R.id.tv_totalDisc_rincianPembelian);
        tvTotal = findViewById(R.id.tv_totalBayar_rincianPembelian);
        tvTotalBiaya = findViewById(R.id.tv_totalPenjualan_rincianPembelian);
        tvPpn = findViewById(R.id.tv_totalPpn_rincianPembelian);

        find(R.id.btn_lanjut_rincianPembelian, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
