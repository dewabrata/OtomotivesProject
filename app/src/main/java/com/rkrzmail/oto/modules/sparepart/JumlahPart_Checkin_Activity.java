package com.rkrzmail.oto.modules.sparepart;

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
import com.rkrzmail.utils.Tools;

import java.util.Objects;

import static com.rkrzmail.srv.PercentFormat.calculatePercentage;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.PARTS_UPPERCASE;
import static com.rkrzmail.utils.ConstUtils.PART_WAJIB;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;
import static com.rkrzmail.utils.ConstUtils.TAMBAH_PART;

public class JumlahPart_Checkin_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "HargaPart____";
    private EditText etHpp, etHargaJual, etDiscPart, etBiayaJasa, etDiscJasa, etDp, etWaktuPesan, etJumlah;

    private final Nson sendData = Nson.newObject();

    private boolean isFlexible = false, isPartKosong = false, isPartWajib = false, isTambahPart = false;
    private String idLokasiPart = "", hpp = "";
    private String inspeksi = "";
    private int stock = 0;
    private double discPart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_harga_part_);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Jumlah & Harga Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        if(getIntent().hasExtra(TAMBAH_PART)){
            isTambahPart = true;
        }

        if (getIntent().hasExtra(DATA)) {
            loadData(DATA, getIntent());
        } else {
            find(R.id.ly_waktu_inspeksi).setVisibility(View.GONE);
            find(R.id.ly_waktu_kerja).setVisibility(View.GONE);
            etBiayaJasa.setVisibility(View.GONE);
            isPartWajib = true;
            final Nson nson = Nson.readJson(getIntentStringExtra(PART_WAJIB));
            stock = nson.get("STOCK").asInteger();
            Log.d(TAG, "data : " + nson);
            hpp = nson.get("HPP").asString();
            idLokasiPart = nson.get("LOKASI_PART_ID").asString();

            //find(R.id.btn_img_waktu_inspeksi).setEnabled(false);
            //find(R.id.et_waktu_set_inspeksi, EditText.class).setText(getIntent().getStringExtra("WAKTU_INSPEKSI"));
            initPartKosongValidation(nson,true);

            boolean isMasterPartOrParts;
            boolean isDiskon = false;
            int finalTotal;

            if (getIntent().hasExtra(MASTER_PART)) {
                isMasterPartOrParts = true;
                if (!getIntent().getStringExtra(MASTER_PART).isEmpty()) {
                    isDiskon = true;
                }
                etJumlah.setText("" + nson.get("JUMLAH").asString());
            } else {
                isMasterPartOrParts = false;
                if (!getIntent().getStringExtra(PARTS_UPPERCASE).isEmpty()) {
                    isDiskon = true;
                }
                etJumlah.setText(nson.get("JUMLAH").asString());
            }

            if (getIntent().getIntExtra("HARGA_LAYANAN", 0) > 0) {
                finalTotal = getIntent().getIntExtra("HARGA_LAYANAN", 0) - calculateDiscFasilitas(
                        Integer.parseInt(isMasterPartOrParts ?
                                getIntent().getStringExtra(MASTER_PART) : getIntent().getStringExtra(PARTS_UPPERCASE)),
                        getIntent().getIntExtra("HARGA_LAYANAN", 0));
                etHargaJual.setText(RP + finalTotal);
            } else {
                if (nson.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") ||
                        nson.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                    find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                    try {
                        etHpp.setText(RP + formatRp(hpp));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    etHargaJual.setEnabled(true);
                    isFlexible = true;
                } else {
                    if (Tools.isNumeric(nson.get("HARGA_JUAL").asString())) {
                        if (isDiskon) {
                            finalTotal = Integer.parseInt(nson.get("HARGA_JUAL").asString())
                                    - calculateDiscFasilitas(
                                    Integer.parseInt(isMasterPartOrParts ?
                                            getIntent().getStringExtra(MASTER_PART) : getIntent().getStringExtra(PARTS_UPPERCASE)),
                                    Integer.parseInt(nson.get("HARGA_JUAL").asString()));
                        } else {
                            finalTotal = Integer.parseInt(nson.get("HARGA_JUAL").asString());
                        }
                        etHargaJual.setText(RP + finalTotal);
                    } else {
                        etHargaJual.setEnabled(true);
                    }
                }
            }

            Log.d(TAG, "initComponent: " + nson);
            etJumlah.setEnabled(false);
            etBiayaJasa.setEnabled(false);
            etBiayaJasa.setText("0");
            find(R.id.btn_img_waktu_kerja, ImageButton.class).setEnabled(false);
            //find(R.id.et_waktuDefault, EditText.class).setText(loadWaktuKerja("0", nson.get("WAKTU_KERJA_JAM").asString(), nson.get("WAKTU_KERJA_MENIT").asString()));
            find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).getVisibility() == View.VISIBLE) {
                        if (etHargaJual.isEnabled()) {
                            if (!etHargaJual.getText().toString().isEmpty() && !etHpp.getText().toString().isEmpty()) {
                                int hppPart = Integer.parseInt(formatOnlyNumber(etHpp.getText().toString()));
                                int hargaJualPart = Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString()));
                                if (hargaJualPart < hppPart) {
                                    Messagebox.showDialog(getActivity(), "Konfirmasi", "Harga Jual Kurang Dari Hpp Part", "Lanjut", "Batal", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            nextForm(nson);
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            etHargaJual.setText("");
                                            etHargaJual.requestFocus();
                                        }
                                    });
                                } else {
                                    nextForm(nson);
                                }
                            } else {
                                etHargaJual.setError("Harga Jual Flexible, Harus Di isi");
                            }
                        }
                    } else {
                        nextForm(nson);
                    }
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
        find(R.id.img_clear).setOnClickListener(this);
        find(R.id.img_clear2).setOnClickListener(this);
        find(R.id.btn_img_waktu_inspeksi).setOnClickListener(this);
        find(R.id.btn_img_waktu_kerja).setOnClickListener(this);
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

    private void initPartKosongValidation(final Nson nson, final boolean isPartWajib) {
        if (stock == 0) {
            if(isPartWajib){
                showWarning("Part Wajib Layanan Stock Kosong!");
                Intent i = new Intent();
                i.putExtra("PART_KOSONG_PART_WAJIB", "OK");
                setResult(RESULT_OK, i);
                finish();
                return;
            }
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Buka Form Part Kosong ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            if (nson.get("DISCOUNT_PART").asString() != null) {
                                //find(R.id.ly_disc_part_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            }
                            if (nson.get("DISCOUNT_JASA") != null) {
                                //find(R.id.ly_discJasa_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            }
                            isPartKosong = true;
                            find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class).setVisibility(View.VISIBLE);
                            etDp.setText(Tools.convertToDoublePercentage(getSetting("DP_PERSEN")) + "%");
                            etWaktuPesan.setText(nson.get("WAKTU_PESAN_HARI").asString());
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isPartWajib) {
                                isPartKosong = false;
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

    @Override
    protected void onResume() {
        super.onResume();
        isPartKosong = false;
    }

    @SuppressLint("SetTextI18n")
    private void loadData(final String intentExtra, Intent intent) {
        final Nson nson = Nson.readJson(getIntentStringExtra(intent, intentExtra));
        hpp = nson.get("HPP").asString();
        stock = nson.get("STOCK_RUANG_PART").asInteger();
        Log.d("parts__", "data : " + nson);
        idLokasiPart = nson.get("LOKASI_PART_ID").asString();
        if (stock == 0) {
            isPartKosong = true;
            initPartKosongValidation(nson,false);
        }

        if(nson.get("FINAL_INS").asString().equals("Y") && !nson.get("FINAL_INS").asString().isEmpty()){
            inspeksi = "Y";
        }else{
            inspeksi = "N";
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

        if (!nson.get("DISCOUNT_PART").asString().isEmpty()) {
            find(R.id.ly_disc_part_jumlah_harga_part).setVisibility(View.VISIBLE);
            if(!nson.get("HARGA_JUAL").asString().isEmpty()){
                discPart = calculateDiscFasilitas(nson.get("DISCOUNT_PART").asDouble(), nson.get("HARGA_JUAL").asInteger());
            }
            etDiscPart.setText(RP + formatRp(String.valueOf(discPart)));
        }

        if (!nson.get("DISCOUNT_JASA_PASANG").asString().isEmpty()) {
            find(R.id.ly_discJasa_jumlah_harga_part).setVisibility(View.VISIBLE);
            etDiscJasa.setText(nson.get("DISCOUNT_JASA_PASANG").asString() + " %");
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

                if(!isPartKosong && (!etJumlah.getText().toString().isEmpty() && Integer.parseInt(etJumlah.getText().toString()) > stock)){
                    etJumlah.setError("Jumlah Part Melebihi Stock");
                    etJumlah.requestFocus();
                    return;
                }

                if (find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).getVisibility() == View.VISIBLE) {
                    if (etHargaJual.isEnabled()) {
                        if (!etHargaJual.getText().toString().isEmpty() && !etHpp.getText().toString().isEmpty()) {
                            int hppPart = Integer.parseInt(formatOnlyNumber(etHpp.getText().toString()));
                            int hargaJualPart = Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString()));
                            if (hargaJualPart < hppPart) {
                                Messagebox.showDialog(getActivity(), "Konfirmasi", "Harga Jual Kurang Dari Hpp Part", "Lanjut", "Batal", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        nextForm(nson);
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        etHargaJual.setText("");
                                        etHargaJual.requestFocus();
                                    }
                                });
                            } else {
                                nextForm(nson);
                            }
                        } else {
                            etHargaJual.setError("Harga Jual Flexible, Harus Di isi");
                        }
                    }
                } else {
                    nextForm(nson);
                }
            }
        });

    }

    private void nextForm(Nson nson) {
        int hargaPart = Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString()));
        int hargaJasa = Integer.parseInt(formatOnlyNumber(etBiayaJasa.getText().toString()));
        int jumlahPart = 0, totalPart = 0, totalJasa = 0;
        int totalHarga = 0;
        double discJasa = 0;
        String partKosong = "";
        String hari = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(0, 2);
        String jam = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(3, 5);
        String menit = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(6, 8);
        String inspeksiJam = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(3, 5);
        String inspeksiMenit = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(6, 8);

        if (etJumlah.getText().toString().isEmpty()) {
            jumlahPart++;
        } else {
            jumlahPart = Integer.parseInt(etJumlah.getText().toString());
        }

        if(!etDiscJasa.getText().toString().isEmpty()){
            discJasa = Double.parseDouble(etDiscJasa.getText().toString()
                    .replace("%", "")
                    .replace(",", "."));
            discJasa = calculatePercentage(discJasa, hargaJasa);
        }

        if(discJasa > 0) {
            totalJasa = (int) (hargaJasa - discJasa);
        }
        if(discPart > 0) {
            totalPart = (int) (hargaPart - discPart) * jumlahPart;
        }else{
            totalPart = hargaPart * jumlahPart;
        }

        totalHarga =  totalPart + hargaJasa;

        if(isPartKosong){
            partKosong = "YA";
            sendData.set("WAKTU_KERJA", "");
            sendData.set("WAKTU_INSPEKSI", "");
            sendData.set("DP", formatOnlyNumber(formatRp(String.valueOf(calculateDp(Double.parseDouble(getSetting("DP_PERSEN")), totalPart)))));
        }else{
            sendData.set("WAKTU_KERJA", find(R.id.et_waktuSet, EditText.class).getText().toString());
            sendData.set("WAKTU_INSPEKSI", find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString());
            sendData.set("DP", "");
        }

        if (isPartWajib) {
            sendData.set("HARGA_PART", hargaPart);
            sendData.set("PERGANTIAN", getIntentStringExtra("PERGANTIAN"));
            sendData.set("INSPEKSI", "");
        } else {

            if (hargaPart > 0) {
                sendData.set("HARGA_PART", hargaPart);
            }

            sendData.set("INSPEKSI", inspeksi);
            sendData.set("PERGANTIAN", "0");
        }

        sendData.set("NAMA_PART", nson.get("NAMA_PART").asString());
        sendData.set("NO_PART", nson.get("NO_PART").asString());
        sendData.set("PART_ID", nson.get("PART_ID").asString());
        sendData.set("JUMLAH", jumlahPart);
        sendData.set("DISCOUNT_JASA", discJasa);
        sendData.set("DISCOUNT_PART", discPart);
        sendData.set("MERK", nson.get("MERK").asString());
        sendData.set("HARGA_JASA", hargaJasa);
        sendData.set("HARGA_JASA_NET", totalJasa);
        sendData.set("LOKASI_PART_ID", idLokasiPart);
        sendData.set("WAKTU_KERJA_HARI", hari);
        sendData.set("WAKTU_KERJA_JAM", jam);
        sendData.set("WAKTU_KERJA_MENIT", menit);
        sendData.set("WAKTU_INSPEKSI_JAM", inspeksiJam);
        sendData.set("WAKTU_INSPEKSI_MENIT", inspeksiMenit);
        sendData.set("JASA_EXTERNAL", "");
        sendData.set("HPP", hpp);
        sendData.set("WAKTU_PESAN", etWaktuPesan.getText().toString());
        sendData.set("NET", totalHarga);

        Intent i = new Intent();
        i.putExtra(DATA, sendData.toJson());
        i.putExtra("PART_KOSONG", partKosong);
        i.putExtra("TOTAL_PART", totalPart);
        setResult(RESULT_OK, i);
        finish();
    }

    private double calculateDp(double dp, int harga) {
        if (dp > 0 && harga > 0) {
            return (dp / 100) * harga;
        }
        return 0;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_clear:
                etHargaJual.setText("");
                break;
            case R.id.img_clear2:
                etBiayaJasa.setText("");
                break;
            case R.id.btn_img_waktu_inspeksi:
                if(inspeksi.equals("Y")){
                    getTimesDialog(find(R.id.et_waktu_set_inspeksi, EditText.class));
                }else{
                    showWarning("Part tidak perlu INSPEKSI");
                }
                break;
            case R.id.btn_img_waktu_kerja:
                getTimesDialog(find(R.id.et_waktuSet, EditText.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            loadData(PART, data);
        }
    }
}