package com.rkrzmail.oto.modules.jasa;

import android.annotation.SuppressLint;
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

import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.JASA_LAIN;

public class BiayaJasa_Activity extends AppActivity implements View.OnClickListener {

    private EditText etKelompokPart, etAktivitas, etWaktuKerja, etWaktuDefault;
    private NikitaAutoComplete etBiaya;

    private int jasaId = 0;
    private String hari = "", jam = "", menit = "";
    private String inspeksiJam = "", inspeksiMenit = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biaya_jasa_);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Biaya Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etBiaya = findViewById(R.id.et_biaya_biayaJasa);
        etKelompokPart = findViewById(R.id.et_kelompokPart_biayaJasa);
        etAktivitas = findViewById(R.id.et_aktivitas_biayaJasa);
        etWaktuKerja = findViewById(R.id.et_waktuSet);
        etWaktuDefault = findViewById(R.id.et_waktuDefault);

        initListener();
        initData();
    }

    private void initData(){
        final Nson n = Nson.readJson(getIntentStringExtra(DATA));
        Log.d("BIAYAJASALAIN", "JASA : " + n);
        if (getIntent().hasExtra(JASA_LAIN)) {
            etKelompokPart.setText(n.get("KELOMPOK_PART").toString());
            etAktivitas.setText(n.get("AKTIVITAS").asString());
            etAktivitas.setEnabled(false);
            jasaId = n.get(ID).asInteger();
            //etWaktuDefault.setText(totalJam + " Menit");
            find(R.id.btn_simpan_biayaJasa).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(etBiaya.getText().toString().isEmpty()){
                        etBiaya.setError("Biaya Jasa Harus di Isi");
                        etBiaya.requestFocus();
                    }else{
                        Nson nson = Nson.newObject();
                        nson.set("NAMA_KELOMPOK_PART", etKelompokPart.getText().toString());
                        nson.set("JASA_ID", jasaId);
                        nson.set("HARGA_JASA", formatOnlyNumber(etBiaya.getText().toString()));
                        nson.set("AKTIVITAS", etAktivitas.getText().toString());
                        nson.set("WAKTU", etWaktuKerja.getText().toString());
                        nson.set("OUTSOURCE", find(R.id.cb_outsource, CheckBox.class).isChecked() ? "Y" : "N");
                        nson.set("NET", formatOnlyNumber(etBiaya.getText().toString()));

                        hari = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(0, 2);
                        jam = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(3, 5);
                        menit = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(6, 8);
                        inspeksiJam = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(3, 5);
                        inspeksiMenit = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(6, 8);

                        nson.set("WAKTU_KERJA_HARI", hari);
                        nson.set("WAKTU_KERJA_JAM", jam);
                        nson.set("WAKTU_KERJA_MENIT", menit);
                        nson.set("WAKTU_INSPEKSI_JAM", inspeksiJam);
                        nson.set("WAKTU_INSPEKSI_MENIT", inspeksiMenit);

                        Intent intent = new Intent();
                        intent.putExtra(DATA, nson.toJson());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
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
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

    private void initListener(){
        etBiaya.addTextChangedListener(textWatcher);
        find(R.id.img_clear, ImageButton.class).setOnClickListener(this);
        find(R.id.btn_img_waktu_kerja).setOnClickListener(this);
        find(R.id.btn_img_waktu_inspeksi).setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_clear:
                etBiaya.setText("");
                break;
            case R.id.btn_img_waktu_kerja:
                getTimesDialog(find(R.id.et_waktuSet, EditText.class));
                break;
            case R.id.btn_img_waktu_inspeksi:
                getTimesDialog(find(R.id.et_waktu_set_inspeksi, EditText.class));
                break;
        }
    }
}
