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
import android.widget.Toast;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.Checkin3_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import static com.rkrzmail.utils.ConstUtils.DATA;

public class JasaExternal_Activity extends AppActivity {

    private static final String TAG = "JasaEx__";
    private Nson sendData = Nson.newObject();
    private String inspeksi = "";

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
        if(nson.get("FINAL_INS").asString().equals("Y") && !nson.get("FINAL_INS").asString().isEmpty()){
            inspeksi = "Y";
        }else{
            inspeksi = "N";
        }
        find(R.id.ly_waktu_inspeksi).setVisibility(View.GONE);
        find(R.id.et_biayaJasa, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_biayaJasa, EditText.class), find(R.id.img_clear2, ImageButton.class)));

        String waktuDefault =totalWaktuKerja("00", nson.get("WAKTU_KERJA_JAM").asString(), nson.get("WAKTU_KERJA_MENIT").asString());
        find(R.id.et_waktuDefault, EditText.class).setText(waktuDefault);
        find(R.id.btn_lanjut, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(find(R.id.et_waktuSet, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00))){
                    showWarning("Waktu Kerja Harus di Isi", Toast.LENGTH_LONG);
                }else{
                    setSelanjutnya(nson);
                }

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
                if(inspeksi.equals("N")){
                    showWarning("Part tidak perlu INSPEKSI");
                }else{
                    getTimesDialog(find(R.id.et_waktu_set_inspeksi, EditText.class));
                }

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
            sendData.set("PART_ID", nson.get("PART_ID").asString());
            sendData.set("JUMLAH", "0");
            sendData.set("NAMA_PART", nson.get("MASTER_NAME").asString());
            sendData.set("NO_PART", nson.get("NO_PART").asString());
            sendData.set("MERK", nson.get("MERK").asString());
            sendData.set("DISCOUNT_JASA", "0");
            sendData.set("DISCOUNT_PART", "0");
            sendData.set("HARGA_JASA", formatOnlyNumber(find(R.id.et_biayaJasa, EditText.class).getText().toString()));
            sendData.set("HARGA_PART", "0");
            sendData.set("WAKTU_KERJA", find(R.id.et_waktuSet, EditText.class).getText().toString());
            sendData.set("WAKTU_INSPEKSI", find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString());
            sendData.set("NET", formatOnlyNumber(find(R.id.et_biayaJasa, EditText.class).getText().toString()));
            sendData.set("WAKTU_KERJA_HARI", formatOnlyNumber(hari));
            sendData.set("WAKTU_KERJA_JAM", formatOnlyNumber(jam));
            sendData.set("WAKTU_KERJA_MENIT", formatOnlyNumber(menit));
            sendData.set("WAKTU_INSPEKSI_JAM", formatOnlyNumber(inspeksiJam));
            sendData.set("WAKTU_INSPEKSI_MENIT", formatOnlyNumber(inspeksiMenit));
            sendData.set("LOKASI_PART_ID", "0");
            sendData.set("JASA_EXTERNAL", formatOnlyNumber(find(R.id.et_biayaJasa, EditText.class).getText().toString()));
            sendData.set("HPP", "0");
            sendData.set("WAKTU_PESAN", "0");
            sendData.set("DP", "0");
            sendData.set("PERGANTIAN", "0");

            Intent i = new Intent();
            i.putExtra(DATA, sendData.toJson());
            i.putExtra("external", true);
            setResult(RESULT_OK, i);
            finish();
        }
    }
}