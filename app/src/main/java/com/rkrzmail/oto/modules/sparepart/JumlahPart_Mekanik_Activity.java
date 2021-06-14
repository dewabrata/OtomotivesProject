package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.Objects;

import static com.rkrzmail.srv.NumberFormatUtils.calculatePercentage;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.PARTS_UPPERCASE;
import static com.rkrzmail.utils.ConstUtils.RP;

public class JumlahPart_Mekanik_Activity extends AppActivity implements View.OnClickListener {

    private EditText etHpp, etHargaJual, etHargaJasa, etJumlah;
    private Spinner spKondisi;

    private final Nson sendData = Nson.newObject();
    private Nson data = Nson.newObject();
    private String kondisi;
    private boolean isFlexible = false;
    private boolean isMekanik1 = false, isMekanik2 = false, isMekanik3 = false;
    private int biayaMekanik1 = 0, biayaMekanik2 = 0, biayaMekanik3 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_part_mekanik);
        initToolbar();
        initComponent();
        setComponent();
        initData();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Jumlah & Harga Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        etHpp = findViewById(R.id.et_hpp);
        etHargaJual = findViewById(R.id.et_harga_jual);
        etHargaJasa = findViewById(R.id.et_harga_jasa);
        etJumlah = findViewById(R.id.et_jumlah_part);
        spKondisi = findViewById(R.id.sp_kondisi_part);
    }

    private void setComponent(){
        setSpinnerFromApi(spKondisi, "nama", "KONDISI PART", VIEW_MST, "KONDISI");
        spKondisi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kondisi = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        watcher(find(R.id.img_clear, ImageButton.class), etHargaJual);
        watcher(find(R.id.img_clear2, ImageButton.class), etHargaJasa);
        find(R.id.img_clear).setOnClickListener(this);
        find(R.id.img_clear2).setOnClickListener(this);
        find(R.id.btn_simpan).setOnClickListener(this);
    }

    @SuppressLint({"SetTextI18n","DefaultLocale"})
    private void initData(){
        data = Nson.readJson(getIntentStringExtra(DATA));
        etJumlah.setText("1");
        if(!data.asString().isEmpty()){
            biayaMekanik1 =  data.get("BIAYA_MEKANIK_1").asInteger();
            biayaMekanik2 =  data.get("BIAYA_MEKANIK_2").asInteger();
            biayaMekanik3 =  data.get("BIAYA_MEKANIK_3").asInteger();

            etHargaJasa.setText(RP + NumberFormatUtils.formatRp(String.valueOf(calculateBiayaMekanik(data))));
            if (data.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setHint("HARGA JUAL FLEXIBLE");
                etHpp.setText(RP + NumberFormatUtils.formatRp(data.get("HARGA_JUAL").asString()));
                etHargaJual.setEnabled(true);
                isFlexible = true;
            }else{
                find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.GONE);
                etHargaJual.setText("Rp. " + NumberFormatUtils.formatRp(data.get("HARGA_JUAL").asString()));
            }
        }
    }

    private int calculateBiayaMekanik(Nson data){
        int waktuHari = 0, waktuJam = 0, waktuMenit = 0;
        if(!data.get("RATA2_WAKTU_KERJA_HARI").asString().isEmpty()){
            waktuHari = data.get("RATA2_WAKTU_KERJA_HARI").asInteger() * 24 * 60;
        }
        if(!data.get("RATA2_WAKTU_KERJA_JAM").asString().isEmpty()){
            waktuJam = data.get("RATA2_WAKTU_KERJA_JAM").asInteger() * 60;
        }
        if(!data.get("RATA2_WAKTU_KERJA_MENIT").asString().isEmpty()){
            waktuMenit = data.get("RATA2_WAKTU_KERJA_MENIT").asInteger();
        }
        int totalKerjaMenit = waktuHari + waktuJam + waktuMenit;

        int biaya = 0;
        if (data.get("JENIS_MEKANIK").asInteger() == 1) {
            biaya = totalKerjaMenit * biayaMekanik1 / 60;
            isMekanik1 = true;
        } else if (data.get("JENIS_MEKANIK").asInteger() == 2) {
            biaya = totalKerjaMenit * biayaMekanik2 / 60;
            isMekanik2 = true;
        } else if (data.get("JENIS_MEKANIK").asInteger() == 3) {
            biaya = totalKerjaMenit * biayaMekanik3 / 60;
            isMekanik3 = true;
        }

        return biaya;
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
                    String cleanString = formatOnlyNumber(str);
                    String formatted = RP + formatRp(cleanString);
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

    private void setIntent() {
        int hargaPart = Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString()));
        int hargaJasa = Integer.parseInt(formatOnlyNumber(etHargaJasa.getText().toString()));
        int jumlahPart = 0, totalPart = 0, totalJasa = 0;

        if (etJumlah.getText().toString().isEmpty()) {
            jumlahPart++;
        } else {
            jumlahPart = Integer.parseInt(etJumlah.getText().toString());
        }

        totalPart = hargaPart * jumlahPart;
        int totalHarga = totalPart + hargaJasa;

        sendData.set("NAMA_PART", data.get("NAMA_PART").asString());
        sendData.set("NO_PART", data.get("NO_PART").asString());
        sendData.set("PART_ID", data.get("PART_ID").asString());
        sendData.set("JUMLAH", jumlahPart);
        sendData.set("MERK", data.get("MERK").asString());
        sendData.set("HARGA_JASA", hargaJasa);
        sendData.set("HARGA_PART", totalPart);
        sendData.set("HARGA_JASA_NET", totalJasa);
        sendData.set("NET", totalHarga);
        sendData.set("KONDISI", kondisi);

        Intent i = new Intent();
        i.putExtra(DATA, sendData.toJson());
        i.putExtra("PART_KOSONG", "N");
        i.putExtra("TOTAL_PART", totalPart);
        setResult(RESULT_OK, i);
        finish();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_clear:
                etHargaJual.setText("");
                break;
            case R.id.img_clear2:
                etHargaJasa.setText("");
                break;
            case R.id.btn_simpan:
                if(kondisi.equals("--PILIH--")){
                    setErrorSpinner(spKondisi, "KONDISI HARUS DI PILIH");
                }else if(etHargaJual.getText().toString().equals("Rp. 0") || etHargaJual.getText().toString().isEmpty()){
                    etHargaJual.setError("HARGA JUAL HARUS DI ISI");
                    viewFocus(etHargaJual);
                }else{
                    int hargaJual = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etHargaJual.getText().toString()));
                    int hpp = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etHpp.getText().toString()));
                    if(hpp > hargaJual){
                        showInfoDialog("KONFIRMASI", "HARGA JUAL KURANG DARI HPP, LANJUTKAN ?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setIntent();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }else{
                        setIntent();
                    }
                }
                break;
        }
    }
}