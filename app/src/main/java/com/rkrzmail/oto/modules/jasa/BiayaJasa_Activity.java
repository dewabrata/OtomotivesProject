package com.rkrzmail.oto.modules.jasa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class BiayaJasa_Activity extends AppActivity {

    private EditText etMasterPart, etAktivitas, etBiaya, etWaktuKerja, etTotalKerja;
    private DecimalFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biaya_jasa_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Biaya Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        formatter = new DecimalFormat("##,##");

        etBiaya = findViewById(R.id.et_biaya_biayaJasa);
        etMasterPart = findViewById(R.id.et_masterPart_biayaJasa);
        etAktivitas = findViewById(R.id.et_aktivitas_biayaJasa);
        etWaktuKerja = findViewById(R.id.et_kerjaMulai_biayaJasa);
        etTotalKerja = findViewById(R.id.et_kerjaSelesai_biayaJasa);

        etBiaya.addTextChangedListener(textWatcher);
        find(R.id.img_clear, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etBiaya.setText("");
            }
        });

        final Nson n = Nson.readJson(getIntentStringExtra("data"));
        Log.d("BIAYAJASALAIN", "JASA : " + n);
        if (getIntent().hasExtra("jasa_lain")) {
            int totalJam = 0;
            final StringBuilder namaStr = new StringBuilder();
            final StringBuilder idStr = new StringBuilder();

            for (int j = 0; j < n.size(); j++) {
                if(namaStr.length() > 0) namaStr.append(", ");
                if(idStr.length() > 0) idStr.append(", ");

                namaStr.append(n.get(j).get("NAMA").asString());
                idStr.append(n.get(j).get("JASA_ID").asString());

                totalJam = totalJam + Integer.parseInt(n.get(j).get("WAKTU").asString().replace(":", ""));
            }
            etMasterPart.setText(namaStr.toString());
            etTotalKerja.setText(totalJam + " Menit");
            find(R.id.btn_simpan_biayaJasa).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Nson nson = Nson.newObject();
                    nson.set("NAMA", namaStr.toString());
                    nson.set("JASA_ID", idStr.toString());
                    nson.set("HARGA_JASA", etBiaya.getText().toString());
                    nson.set("AKTIVITAS_LAIN", etAktivitas.getText().toString());
                    nson.set("LAMA_KERJA", etWaktuKerja.getText().toString());

                    Intent intent = new Intent();
                    intent.putExtra("data", nson.toJson());
                    Log.d("BIAYAJASALAIN", "JASA : " + nson);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else if (getIntent().hasExtra("jasa_berkala")) {
            etMasterPart.setText(n.get("NAMA").asString());
            etTotalKerja.setText(n.get("WAKTU").asString());
            find(R.id.btn_simpan_biayaJasa).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Nson nson2 = Nson.newObject();

                    nson2.set("NAMA", n.get("NAMA").asString());
                    nson2.set("JASA_ID", n.get("JASA_ID").asString());
                    nson2.set("HARGA_JASA", etBiaya.getText().toString());
                    nson2.set("AKTIVITAS_LAIN", etAktivitas.getText().toString());
                    nson2.set("LAMA_KERJA", etWaktuKerja.getText().toString());

                    Intent intent = new Intent();
                    intent.putExtra("data", nson2.toJson());
                    Log.d("BIAYAJASALAIN", "JASA : " + n);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (s.toString().trim().length() == 0) {
                find(R.id.img_clear, ImageButton.class).setVisibility(View.GONE);
            } else {
                find(R.id.img_clear, ImageButton.class).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (etBiaya == null) return;
            etBiaya.removeTextChangedListener(this);
            String editable = s.toString();
            try {
                String cleanString = editable.replaceAll("[^0-9]", "");
                String formatted = Tools.formatRupiah(cleanString);
                etBiaya.setText(formatted);
                etBiaya.setSelection(formatted.length());
            } catch (Exception e) {
                e.printStackTrace();
            }

            etBiaya.addTextChangedListener(this);
        }
    };
}
