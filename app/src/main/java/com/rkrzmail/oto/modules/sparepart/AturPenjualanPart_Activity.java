package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class AturPenjualanPart_Activity extends AppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_penjualan_part_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_jualPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Frekwensi Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.et_jenisKendaraan_jualPart);
        find(R.id.et_namaPelanggan_jualPart);
        find(R.id.et_namaUsaha_jualPart);
        find(R.id.et_noPhone_jualPart);
        find(R.id.btn_lanjut_jualPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelanjutnya();
            }
        });
    }

    private void setSelanjutnya() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                Nson nson = Nson.newObject();
                nson.set("jeniskendaraan", find(R.id.et_jenisKendaraan_jualPart, TextView.class).getText().toString());
                nson.set("namapelanggan", find(R.id.et_namaPelanggan_jualPart, TextView.class).getText().toString());
                nson.set("namausaha", find(R.id.et_namaUsaha_jualPart, TextView.class).getText().toString());
                nson.set("nopon", find(R.id.et_noPhone_jualPart, TextView.class).getText().toString());

//                Intent intent = new Intent(getActivity(), );
//                intent.putExtra("jualpart", nson.toJson());
//                startActivity(intent);


            }
        });
    }
}

