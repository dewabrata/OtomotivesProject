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
import com.rkrzmail.oto.modules.checkin.Checkin3_Activity;
import com.rkrzmail.utils.Tools;

import static com.rkrzmail.utils.ConstUtils.DATA;

public class JasaExternal_Activity extends AppActivity {

    private static final String TAG = "JasaEx__";
    private Nson sendData = Nson.newObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jasa_external);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jasa External");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        watcher(find(R.id.img_clear2, ImageButton.class), find(R.id.et_biayaJasa, EditText.class));
        Log.d(TAG, "Jasa External : " + nson);

        String waktuDefault =totalWaktuKerja("00", nson.get("WAKTU_KERJA_JAM").asString(), nson.get("WAKTU_KERJA_MENIT").asString());
        find(R.id.et_waktuDefault, EditText.class).setText(waktuDefault);
        find(R.id.btn_lanjut, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelanjutnya(nson);
            }
        });

        find(R.id.btn_img_waktu_kerja, ImageButton.class).setOnClickListener(new View.OnClickListener() {
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

        find(R.id.btn_img_waktu_inspeksi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTimesDialog(find(R.id.et_waktu_set_inspeksi, EditText.class));
            }
        });
    }

    private void setSelanjutnya(Nson nson) {
        boolean isWaktuKerjaDeafult = false;
        if(find(R.id.et_waktuSet, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00))){
            isWaktuKerjaDeafult = true;
        }
        String hari = isWaktuKerjaDeafult ? find(R.id.et_waktuDefault, EditText.class).getText().toString().substring(0, 2) : find(R.id.et_waktuSet, EditText.class).getText().toString().substring(0, 2);
        String jam = isWaktuKerjaDeafult ? find(R.id.et_waktuDefault, EditText.class).getText().toString().substring(3, 5) : find(R.id.et_waktuSet, EditText.class).getText().toString().substring(3, 5);
        String menit = isWaktuKerjaDeafult ? find(R.id.et_waktuDefault, EditText.class).getText().toString().substring(6, 8) : find(R.id.et_waktuSet, EditText.class).getText().toString().substring(6, 8);
        String inspeksiJam = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(3, 5);
        String inspeksiMenit = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(6, 8);

        if (find(R.id.et_waktuSet, EditText.class).getText().toString().isEmpty()) {
            find(R.id.btn_img_waktu_kerja, ImageButton.class).performClick();
            showWarning("Waktu Harus Di Isi");
        } else if (find(R.id.et_biayaJasa, EditText.class).getText().toString().isEmpty()) {
            find(R.id.et_biayaJasa, EditText.class).setError("Biaya Jasa Harus Di isi");
            find(R.id.et_biayaJasa, EditText.class).requestFocus();
        } else {
            sendData.set("PART_ID", nson.get("ID").asString());
            sendData.set("JUMLAH", "");
            sendData.set("NAMA_PART", nson.get("NAMA_MASTER").asString());
            sendData.set("NO_PART", nson.get("NOMOR_PART_NOMOR").asString());
            sendData.set("MERK", nson.get("MERK").asString());
            sendData.set("DISCOUNT_JASA", "");
            sendData.set("DISCOUNT_PART", "");
            sendData.set("HARGA_JASA", formatOnlyNumber(find(R.id.et_biayaJasa, EditText.class).getText().toString()));
            sendData.set("HARGA_PART", "");
            sendData.set("WAKTU_KERJA", find(R.id.et_waktuSet, EditText.class).getText().toString());
            sendData.set("WAKTU_INSPEKSI", find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString());
            sendData.set("NET", formatOnlyNumber(find(R.id.et_biayaJasa, EditText.class).getText().toString()));
            sendData.set("WAKTU_KERJA_HARI", hari);
            sendData.set("WAKTU_KERJA_JAM", jam);
            sendData.set("WAKTU_KERJA_NENIT", menit);
            sendData.set("WAKTU_INSPEKSI_JAM", inspeksiJam);
            sendData.set("WAKTU_INSPEKSI_MENIT", inspeksiMenit);
            sendData.set("LOKASI_PART_ID", "");
            sendData.set("JASA_EXTERNAL", formatOnlyNumber(find(R.id.et_biayaJasa, EditText.class).getText().toString()));
            sendData.set("HPP", "");
            sendData.set("WAKTU_PESAN", "");
            sendData.set("DP", "");

            Intent i = new Intent();
            i.putExtra(DATA, sendData.toJson());
            i.putExtra("external", true);
            setResult(RESULT_OK, i);
            finish();
        }
    }

    public void watcher(final ImageButton imageButton, final EditText editText) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    imageButton.setVisibility(View.GONE);
                } else {
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