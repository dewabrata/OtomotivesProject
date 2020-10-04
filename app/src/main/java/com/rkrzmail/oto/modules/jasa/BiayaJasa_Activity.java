package com.rkrzmail.oto.modules.jasa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;

import static com.rkrzmail.utils.ConstUtils.DATA;

public class BiayaJasa_Activity extends AppActivity {

    private EditText etKelompokPart, etAktivitas, etWaktuKerja, etWaktuDefault;
    private NikitaAutoComplete etBiaya;
    private int jasaId = 0;

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

        etBiaya = findViewById(R.id.et_biaya_biayaJasa);
        etKelompokPart = findViewById(R.id.et_kelompokPart_biayaJasa);
        etAktivitas = findViewById(R.id.et_aktivitas_biayaJasa);
        etWaktuKerja = findViewById(R.id.et_waktuSet);
        etWaktuDefault = findViewById(R.id.et_waktuDefault);

        etBiaya.addTextChangedListener(textWatcher);
        find(R.id.img_clear, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etBiaya.setText("");
            }
        });

        find(R.id.img_waktuKerja, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimesDialog(find(R.id.et_waktuSet, EditText.class));
            }
        });

        final Nson n = Nson.readJson(getIntentStringExtra(DATA));
        Log.d("BIAYAJASALAIN", "JASA : " + n);
        if (getIntent().hasExtra("jasa_lain")) {
            etKelompokPart.setText(n.get("KELOMPOK_PART").toString());
            etAktivitas.setText(n.get("AKTIVITAS").asString());
            etAktivitas.setEnabled(false);
            jasaId = n.get("NO").asInteger();
            //etWaktuDefault.setText(totalJam + " Menit");
            find(R.id.btn_simpan_biayaJasa).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Nson nson = Nson.newObject();
                    nson.set("NAMA_KELOMPOK_PART", etKelompokPart.getText().toString());
                    nson.set("JASA_ID", jasaId);
                    nson.set("HARGA_JASA", etBiaya.getText().toString().replaceAll("[^0-9]+", ""));
                    nson.set("AKTIVITAS", etAktivitas.getText().toString());
                    nson.set("WAKTU", etWaktuKerja.getText().toString());
                    nson.set("OUTSOURCE", find(R.id.cb_outsource, CheckBox.class).isChecked() ? "Y" : "N");

                    Intent intent = new Intent();
                    intent.putExtra(DATA, nson.toJson());
                    Log.d("BIAYAJASALAIN", "JASA : " + nson);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else if (getIntent().hasExtra("jasa_berkala")) {
            etKelompokPart.setText(n.get("NAMA").asString());
            etWaktuDefault.setText(n.get("WAKTU").asString());
            find(R.id.btn_simpan_biayaJasa).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Nson nson2 = Nson.newObject();

                    nson2.set("NAMA_KELOMPOK_PART", n.get("NAMA").asString());
                    nson2.set("JASA_ID", n.get("JASA_ID").asString());
                    nson2.set("HARGA_JASA", etBiaya.getText().toString());
                    nson2.set("AKTIVITAS", etAktivitas.getText().toString());
                    nson2.set("WAKTU", etWaktuKerja.getText().toString());
                    nson2.set("DISCOUNT_JASA", "");

                    Intent intent = new Intent();
                    intent.putExtra(DATA, nson2.toJson());
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
