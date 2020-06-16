package com.rkrzmail.oto.modules.sparepart.new_part;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class JumlahHargaPart_Activity extends AppActivity {

    private EditText etHpp, etHargaJual, etDiscPart, etJasa, etDiscJasa, etDp, etWaktuPesan, etJumlah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_harga_part_);
        initComponent();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_jumlah_harga_part);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jumlah & Harga Part");
    }

    private void initComponent() {
        initToolbar();
        etHpp = findViewById(R.id.et_hpp_jumlah_harga_part);
        etHargaJual = findViewById(R.id.et_hargaJual_jumlah_harga_part);
        etDiscJasa = findViewById(R.id.et_discJasa_jumlah_harga_part);
        etDiscPart = findViewById(R.id.et_discPart_jumlah_harga_part);
        etHargaJual = findViewById(R.id.et_hargaJual_jumlah_harga_part);
        etDp = findViewById(R.id.et_dp_jumlah_harga_part);
        etWaktuPesan = findViewById(R.id.et_waktu_jumlah_harga_part);
        etJumlah = findViewById(R.id.et_jumlah_jumlah_harga_part);
        etJasa = findViewById(R.id.et_jasa_jumlah_harga_part);

        find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void loadData() {
        Intent i = getIntent();
        if (i.hasExtra("")) {
            etHpp.setEnabled(true);
            etJasa.setEnabled(true);
            etWaktuPesan.setEnabled(true);
            etDp.setEnabled(true);
            etHargaJual.setEnabled(true);
            etDiscJasa.setEnabled(true);
            etDiscPart.setEnabled(true);
        }
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                } else {
                    showInfo("Gagal Menyimpan Aktivitas");
                }
            }
        });
    }
}
