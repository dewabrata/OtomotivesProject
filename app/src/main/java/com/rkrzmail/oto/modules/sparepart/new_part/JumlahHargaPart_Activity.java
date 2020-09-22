package com.rkrzmail.oto.modules.sparepart.new_part;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.naa.data.Nson;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.primary.booking.Booking3_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;

public class JumlahHargaPart_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "HargaPart____";
    private static final int REQUEST_CARI_PART = 12;
    private EditText etHpp, etHargaJual, etDiscPart, etBiayaJasa, etDiscJasa, etDp, etWaktuPesan, etJumlah;
    private Toolbar toolbar;
    private DecimalFormat formatter;
    private Nson sendData = Nson.newObject();
    private boolean isFlexible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_harga_part_);
        initComponent();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jumlah & Harga Part");
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        initToolbar();
        etHpp = findViewById(R.id.et_hpp_jumlah_harga_part);
        etHargaJual = findViewById(R.id.et_hargaJual_jumlah_harga_part);
        etDiscJasa = findViewById(R.id.et_discJasa_jumlah_harga_part);
        etDiscPart = findViewById(R.id.et_discPart_jumlah_harga_part);
        etDp = findViewById(R.id.et_dp_jumlah_harga_part);
        etWaktuPesan = findViewById(R.id.et_waktu_jumlah_harga_part);
        etJumlah = findViewById(R.id.et_jumlah_jumlah_harga_part);
        etBiayaJasa = findViewById(R.id.et_jasa_jumlah_harga_part);

        if(getIntent().hasExtra("data")){
            initData("data", getIntent());
        }else{
            Nson nson = Nson.readJson(getIntentStringExtra("partWajib"));
            etJumlah.setEnabled(false);
            etBiayaJasa.setEnabled(false);
            etJumlah.setText(nson.get("JUMLAH").asString());
            find(R.id.img_waktuKerja, ImageButton.class).setEnabled(false);
            find(R.id.et_waktuDefault, EditText.class).setText(
                    loadWaktuKerja(nson.get("WAKTU_KERJA_HARI").asString(), nson.get("WAKTU_KERJA_JAM").asString(), nson.get("WAKTU_KERJA_MENIT").asString()));
            find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextForm();
                }
            });
        }


        find(R.id.img_clear, ImageButton.class).setOnClickListener(this);
        find(R.id.img_clear2, ImageButton.class).setOnClickListener(this);

        watcher(find(R.id.img_clear, ImageButton.class), etHargaJual);
        watcher(find(R.id.img_clear2, ImageButton.class), etBiayaJasa);

        find(R.id.ly_jumlahHarga_part, LinearLayout.class);
        find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class);
    }

    private String loadWaktuKerja(String hari, String jam, String menit){
        StringBuilder result = new StringBuilder();
        if(!hari.equals("0") || hari.equals("")){
            result.append(hari).append(":");
        }else {
            result.append("00").append(":");
        }
        if(!jam.equals("0") || jam.equals("")) {
            result.append(jam).append(":");
        }else{
            result.append("00").append(":");
        }
        if(!menit.equals("0") || menit.equals("")){
            result.append(menit).append(":");
        }else{
            result.append("00").append(":");
        }

        return result.toString();
    }

    @SuppressLint("SetTextI18n")
    private void initData(String getIntent, Intent intent) {
        Nson nson = Nson.readJson(getIntentStringExtra(intent, getIntent));
        Log.d(TAG, "data : " + nson);
        if (nson.get("STOCK").asInteger() == 0) {
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Buka Form Part Kosong ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_disc_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_discJasa_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class).setVisibility(View.VISIBLE);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(getActivity(), CariPart_Activity.class);
                            i.putExtra("bengkel", "");
                            startActivityForResult(i, REQUEST_CARI_PART);
                        }
                    });
        }

        if (nson.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") || nson.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
            find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
            try {
                etHpp.setText("Rp. " + formatRp(nson.get("HPP").asString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            etHargaJual.setEnabled(true);
            isFlexible = true;
        } else {
            try {
                etHargaJual.setText("Rp. " + formatRp(nson.get("HARGA_JUAL").asString()));
            } catch (Exception e) {
                Log.d(TAG, "HargaJual: " + e.getMessage());
            }
        }

        if (nson.containsKey("DISCOUNT_PART") && !nson.get("DISCOUNT_PART").asString().equalsIgnoreCase("")) {
            etDiscPart.setVisibility(View.VISIBLE);
            etDiscPart.setText(nson.get("DISCOUNT_PART").asString());
        }

        if (nson.containsKey("DISC_JASA") && !nson.get("DISC_JASA").asString().equalsIgnoreCase("")) {
            etDiscJasa.setVisibility(View.VISIBLE);
            etDiscJasa.setText(nson.get("DISC_JASA").asString());
        }

        find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.et_waktuSet, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_waktuSet, EditText.class).requestFocus();
                    find(R.id.et_waktuSet, EditText.class).setError("Masukkan Waktu Kerja");
                    return;
                }
                if (find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).getVisibility() == View.VISIBLE) {
                    if (etHargaJual.isEnabled()) {
                        if (!etHargaJual.getText().toString().isEmpty() && !etHpp.getText().toString().isEmpty()) {
                            int hppPart = Integer.parseInt(etHpp.getText().toString().replaceAll("[^0-9]+", ""));
                            int hargaJualPart = Integer.parseInt(etHargaJual.getText().toString().replaceAll("[^0-9]+", ""));
                            if (hargaJualPart < hppPart) {
                                Messagebox.showDialog(getActivity(), "Konfirmasi", "Harga Jual Kurang Dari Hpp Part", "Lanjut", "Batal", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        nextForm();
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        etHargaJual.setText("");
                                        etHargaJual.requestFocus();
                                    }
                                });
                            }
                        } else {
                            etHargaJual.setError("Harga Jual Harus Di isi");
                        }
                    }
                    return;
                }
                if (etBiayaJasa.getText().toString().isEmpty()) {
                    etBiayaJasa.setError("Biaya Jasa Tidak Boleh Kosong");
                    etBiayaJasa.requestFocus();
                    return;
                }
                nextForm();

            }
        });
        find(R.id.img_waktuKerja, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimesDialog(find(R.id.et_waktuSet, EditText.class));
            }
        });
    }

    private void nextForm() {
        Nson nson = Nson.readJson(getIntentStringExtra("data"));
        Log.d(TAG, "Data Harga Part: " + nson);
        String harga = etHargaJual.getText().toString().replaceAll("[^0-9]+", "");
        String jasa = etBiayaJasa.getText().toString().replaceAll("[^0-9]+", "");
        int jumlah = 0;
        if (etJumlah.getText().toString().isEmpty()) {
            jumlah++;
        } else {
            jumlah = Integer.parseInt(etJumlah.getText().toString());
        }

        //nson.set("BIAYA_JASA", jasa);
        sendData.set("NAMA_PART", nson.get("NAMA_PART").asString());
        sendData.set("NO_PART", nson.get("NO_PART").asString());
        sendData.set("PART_ID", nson.get("PART_ID").asString());
        sendData.set("JUMLAH", jumlah);
        sendData.set("DISCOUNT_JASA", "");
        sendData.set("DISCOUNT_PART", "");
        sendData.set("MERK", nson.get("MERK").asString());
        sendData.set("HARGA_JASA", jasa);
        sendData.set("WAKTU", find(R.id.et_waktuSet, EditText.class).getText().toString());

        if (!harga.isEmpty() && !jasa.isEmpty()) {
            int totall = Integer.parseInt(harga) * jumlah;
            //nson.set("HARGA_PART", "Rp" + totall);
            //nson.set("HARGA_NET", totall);
            sendData.set("HARGA_PART", totall);
        }

        Intent i = new Intent();
        i.putExtra("data", sendData.toJson());
        setResult(RESULT_OK, i);
        finish();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_clear:
                etHargaJual.setText("");
                break;
            case R.id.img_clear2:
                etBiayaJasa.setText("");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            initData("part", data);
        }
    }
}
