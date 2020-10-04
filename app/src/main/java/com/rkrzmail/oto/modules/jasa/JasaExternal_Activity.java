package com.rkrzmail.oto.modules.jasa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.primary.checkin.Checkin3_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.utils.Tools;

import static com.rkrzmail.utils.ConstUtils.DATA;

public class JasaExternal_Activity extends AppActivity {

    private static final String TAG = "JasaEx__";
    private Nson sendData = Nson.newObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jasa_external);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jasa External");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        watcher(find(R.id.img_clear2, ImageButton.class), find(R.id.et_biayaJasa, EditText.class));
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        Log.d(TAG, "Jasa External : " + nson);

        String waktuDefault = Checkin3_Activity.totalWaktu("00", nson.get("WAKTU_KERJA_JAM").asString(), nson.get("WAKTU_KERJA_MENIT").asString());
        find(R.id.et_waktuDefault, EditText.class).setText(waktuDefault);
        find(R.id.btn_lanjut, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(find(R.id.et_waktuSet, EditText.class).getText().toString().isEmpty()){
                    find(R.id.img_waktuKerja, ImageButton.class).performClick();
                    showWarning("Waktu Harus Di Isi");
                }else if(find(R.id.et_biayaJasa, EditText.class).getText().toString().isEmpty()){
                    find(R.id.et_biayaJasa, EditText.class).setError("Biaya Jasa Harus Di isi");
                    find(R.id.et_biayaJasa, EditText.class).requestFocus();
                }else{
                    sendData.set("PART_ID", nson.get("PART_ID").asString());
                    sendData.set("JUMLAH", "");
                    sendData.set("NAMA_PART", nson.get("NAMA_MASTER").asString());
                    sendData.set("NO_PART", nson.get("NOMOR_PART_NOMOR").asString());
                    sendData.set("MERK", nson.get("MERK").asString());
                    sendData.set("DISCOUNT_JASA", "");
                    sendData.set("DISCOUNT_PART", "");
                    sendData.set("HARGA_JASA",  find(R.id.et_biayaJasa, EditText.class).getText().toString().replaceAll("[^0-9]+", ""));
                    sendData.set("HARGA_PART", "PART EXTERNAL");
                    sendData.set("WAKTU", find(R.id.et_waktuSet, EditText.class).getText().toString());

                    Intent i = new Intent();
                    i.putExtra(DATA, sendData.toJson());
                    i.putExtra("external", true);
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });

        find(R.id.img_waktuKerja, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimesDialog(find(R.id.et_waktuSet, EditText.class));
            }
        });

        find(R.id.img_clear2, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find(R.id.et_biayaJasa, EditText.class).setText("");
            }
        });
    }

    public void watcher (final ImageButton imageButton, final EditText editText){
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.toString().length() == 0){
                    imageButton.setVisibility(View.GONE);
                }else{
                    imageButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText == null) return;
                String str = s.toString();
                if (str.isEmpty()) return;
                editText.removeTextChangedListener(this);
                try {
                    String cleanString = str.replaceAll("[^0-9]", "");
                    String formatted = Tools.formatRupiah(cleanString);
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editText.addTextChangedListener(this);
            }
        };
        editText.addTextChangedListener(textWatcher);
    }
}