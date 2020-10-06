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
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;

import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.PARTS;
import static com.rkrzmail.utils.ConstUtils.PART_WAJIB;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class JumlahHargaPart_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "HargaPart____";
    private EditText etHpp, etHargaJual, etDiscPart, etBiayaJasa, etDiscJasa, etDp, etWaktuPesan, etJumlah;
    private Toolbar toolbar;
    private DecimalFormat formatter;
    private Nson sendData = Nson.newObject();
    private boolean isFlexible = false, isPartKosong = false;

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
        initListener();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        if (getIntent().hasExtra(DATA)) {
            initData(DATA, getIntent());
        } else {
            final Nson nson = Nson.readJson(getIntentStringExtra(PART_WAJIB));
            initPartKosongValidation(nson, nson.get("STOCK").asInteger(), true);

            boolean isMasterPartOrParts;
            boolean isDiskon = false;
            int finalTotal;

            if (getIntent().hasExtra(MASTER_PART)) {
                isMasterPartOrParts = true;
                if(!getIntent().getStringExtra(MASTER_PART).isEmpty()){
                    isDiskon = true;
                }
                etJumlah.setText("" + getIntent().getIntExtra("jumlah", 0));
            } else {
                isMasterPartOrParts = false;
                if(!getIntent().getStringExtra(PARTS).isEmpty()){
                    isDiskon = true;
                }
                etJumlah.setText(nson.get("JUMLAH").asString());
            }

            if (nson.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") || nson.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                try {
                    etHpp.setText(RP + formatRp(nson.get("HPP").asString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etHargaJual.setEnabled(true);
                isFlexible = true;
            } else {
                if (Tools.isNumeric(nson.get("HARGA_JUAL").asString())) {
                    if(isDiskon){
                        finalTotal = Integer.parseInt(nson.get("HARGA_JUAL").asString())
                                - calculateDiscFasilitas(
                                Integer.parseInt(isMasterPartOrParts ?
                                        getIntent().getStringExtra(MASTER_PART) : getIntent().getStringExtra(PARTS)),
                                Integer.parseInt(nson.get("HARGA_JUAL").asString()));
                    }else{
                        finalTotal = Integer.parseInt(nson.get("HARGA_JUAL").asString());
                    }
                    etHargaJual.setText(RP + finalTotal);
                } else {
                    etHargaJual.setEnabled(true);
                }
            }
            Log.d(TAG, "initComponent: " + nson);
            etJumlah.setEnabled(false);
            etBiayaJasa.setEnabled(false);
            etBiayaJasa.setText("0");
            find(R.id.et_waktuDefault, EditText.class).setText("0");
            find(R.id.et_waktuSet, EditText.class).setText("0");
            find(R.id.img_waktuKerja, ImageButton.class).setEnabled(false);
            //find(R.id.et_waktuDefault, EditText.class).setText(loadWaktuKerja("0", nson.get("WAKTU_KERJA_JAM").asString(), nson.get("WAKTU_KERJA_MENIT").asString()));
            find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextForm(PART_WAJIB);
                }
            });
        }
    }

    private int calculateDiscFasilitas(double diskon, int harga) {
        if (diskon > 0 && harga > 0) {
            return (int) (diskon * harga) / 100;
        }
        return 0;
    }

    private void initListener() {
        watcher(find(R.id.img_clear, ImageButton.class), etHargaJual);
        watcher(find(R.id.img_clear2, ImageButton.class), etBiayaJasa);
        find(R.id.img_clear, ImageButton.class).setOnClickListener(this);
        find(R.id.img_clear2, ImageButton.class).setOnClickListener(this);
    }

    @SuppressLint("DefaultLocale")
    private String loadWaktuKerja(String hari, String jam, String menit) {
        String[] waktu = new String[3];
        if (!jam.equals("0") && !menit.equals("0")) {
            waktu[0] = hari;
            waktu[1] = jam;
            waktu[2] = menit;
        } else {
            waktu[0] = "0";
            waktu[1] = "0";
            waktu[2] = "0";
        }

        return String.format("%02d:%02d:%02d", Integer.parseInt(waktu[0]), Integer.parseInt(waktu[1]), Integer.parseInt(waktu[2]));
    }

    private void initPartKosongValidation(final Nson nson, int stock, final boolean isPartWajib) {
        if (stock == 0) {
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Buka Form Part Kosong ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_disc_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_discJasa_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class).setVisibility(View.VISIBLE);
                            etDp.setText(Tools.convertToDoublePercentage(getSetting("DP_PERSEN")) + "%");
                            etWaktuPesan.setText(nson.get("WAKTU_PESAN_HARI").asString());
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isPartWajib) {
                                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                                startActivityForResult(i, REQUEST_CARI_PART);
                            } else {
                                finish();
                                dialog.dismiss();
                            }
                        }
                    });
        }

    }

    @SuppressLint("SetTextI18n")
    private void initData(final String intentExtra, Intent intent) {
        final Nson nson = Nson.readJson(getIntentStringExtra(intent, intentExtra));
        Log.d(TAG, "data : " + nson);
        if (nson.get("STOCK_RUANG_PART").asInteger() == 0) {
            isPartKosong = true;
            initPartKosongValidation(nson, nson.get("STOCK_RUANG_PART").asInteger(), false);
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
                if (etBiayaJasa.getText().toString().isEmpty()) {
                    etBiayaJasa.setError("Biaya Jasa Tidak Boleh Kosong");
                    etBiayaJasa.requestFocus();
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
                                        nextForm(intentExtra);
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        etHargaJual.setText("");
                                        etHargaJual.requestFocus();
                                    }
                                });
                            } else {
                                nextForm(intentExtra);
                            }
                        } else {
                            etHargaJual.setError("Harga Jual Flexible, Harus Di isi");
                        }
                    }
                } else {
                    nextForm(intentExtra);
                }
            }
        });

        find(R.id.img_waktuKerja, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimesDialog(find(R.id.et_waktuSet, EditText.class));
            }
        });
    }

    private void nextForm(final String intentExtra) {
        Nson nson = Nson.readJson(getIntentStringExtra(intentExtra));
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
        if(isPartKosong){
            sendData.set("PART_KOSONG", "true");
        }

        if (!harga.isEmpty() && !jasa.isEmpty()) {
            int totall = Integer.parseInt(harga) * jumlah;
            sendData.set("HARGA_PART", totall);
        }

        if (intentExtra.equals(PART_WAJIB)) {
            sendData.set("HARGA_PART", etHargaJual.getText().toString().replaceAll("[^0-9]+", ""));
        }

        Intent i = new Intent();
        i.putExtra(DATA, sendData.toJson());
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
            initData(PART, data);
        }
    }
}
