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
import com.rkrzmail.oto.modules.TimePicker_Dialog;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.Objects;

import static com.rkrzmail.srv.NumberFormatUtils.calculatePercentage;
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
    private double discPart = 0, discFasilitas = 0;

    private String idLokasiPart = "", hpp = "";
    private String inspeksi = "", garansiPart = "";
    private String waktuPesan = "";
    private int stock = 0, countClick = 0;
    private int berkalaKm = 0, berkalaBulan = 0, kmKendaraan = 0, batasanGaransiKm = 0, batasanGaransiBulan = 0;
    private int biayaMekanik1 = 0, biayaMekanik2 = 0, biayaMekanik3 = 0;

    private boolean isMekanik1 = false, isMekanik2 = false, isMekanik3 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_harga_part);
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

        initData();
        initListener();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void initData() {
        find(R.id.ly_waktu_inspeksi).setVisibility(View.GONE);
        if (getIntent().hasExtra(TAMBAH_PART)) {
            isTambahPart = true;
        }

        if (getIntent().hasExtra(DATA)) {
            loadData(DATA, getIntent());
        } else {
            final Nson nson = Nson.readJson(getIntentStringExtra(PART_WAJIB));

            etBiayaJasa.setVisibility(View.GONE);
            find(R.id.ly_waktu_kerja).setVisibility(View.GONE);

            isPartWajib = true;
            batasanGaransiKm = nson.get("GARANSI_PART_KM").asInteger();
            batasanGaransiBulan = nson.get("GARANSI_PART_BULAN").asInteger();
            kmKendaraan = getIntentIntegerExtra("KM");
            garansiPart = nson.get("GARANSI_PART_PABRIKAN").asString();
            berkalaBulan = Integer.parseInt(formatOnlyNumber(nson.get("BERKALA_BULAN").asString()));
            berkalaKm = Integer.parseInt(formatOnlyNumber(nson.get("BERKALA_KM").asString())) + kmKendaraan;
            stock = nson.get("STOCK").asInteger();
            hpp = nson.get("HPP").asString();
            idLokasiPart = nson.get("LOKASI_PART_ID").asString();
            if(stock == 0)
                initPartKosongValidation(nson, true);

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

            if (!nson.get("DISCOUNT_PART").asString().isEmpty()) {
                if (!nson.get("HARGA_JUAL").asString().isEmpty()) {
                    discPart = calculateDisc(nson.get("DISCOUNT_PART").asDouble(), nson.get("HARGA_JUAL").asInteger());
                }
                //showDialogDisc(nson.get("DISCOUNT_PART").asDouble());
                etDiscPart.setText(RP + NumberFormatUtils.formatRp(String.valueOf((int) discPart)));
            }

            if (nson.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") ||
                    nson.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                try {
                    etHpp.setText(RP + NumberFormatUtils.formatRp(nson.get("HARGA_JUAL").asString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etHargaJual.setEnabled(true);
                isFlexible = true;
            } else {
                if (Tools.isNumeric(nson.get("HARGA_JUAL").asString())) {
                    if (isDiskon) {
                        finalTotal = Integer.parseInt(nson.get("HARGA_JUAL").asString())
                                - calculateDisc(
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

            /*if (getIntent().getIntExtra("HARGA_LAYANAN", 0) > 0) {
                discFasilitas = Double.parseDouble(isMasterPartOrParts ?
                        getIntent().getStringExtra(MASTER_PART) : getIntent().getStringExtra(PARTS_UPPERCASE));
                finalTotal = getIntent().getIntExtra("HARGA_LAYANAN", 0) - calculateDisc(discFasilitas,
                        getIntent().getIntExtra("HARGA_LAYANAN", 0));
                etHargaJual.setText(RP + finalTotal);
            } else {

            }*/

            etBiayaJasa.setEnabled(false);
            etBiayaJasa.setText("0");
            find(R.id.btn_img_waktu_kerja, ImageButton.class).setEnabled(false);
            find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    countClick++;
                    if (countClick == 1) {
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
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            });
        }
    }

    private void showDialogDisc(double discount) {
        showInfoDialog("DISCOUNT PART", "PART MENDAPATKAN DISCOUNT SEBESAR " + discount + " %", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private int calculateDisc(double diskon, int harga) {
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
        etJumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_jumlah_part, TextInputLayout.class).setHelperTextEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isPartKosong) {
                    String text = editable.toString();
                    if (!text.isEmpty()) {
                        int jumlah = Integer.parseInt(text);
                        if (jumlah > stock && !isPartKosong) {
                            find(R.id.tl_jumlah_part, TextInputLayout.class).setErrorEnabled(true);
                            find(R.id.tl_jumlah_part, TextInputLayout.class).setError("Jumlah Melebihi Stock Tersedia");
                        } else {
                            find(R.id.tl_jumlah_part, TextInputLayout.class).setErrorEnabled(false);
                        }
                    }
                }
            }
        });
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
        waktuPesan = nson.get("WAKTU_PESAN_HARI").asString();
        if (stock == 0) {
            if (isPartWajib) {
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void loadData(final String intentExtra, Intent intent) {
        final Nson nson = Nson.readJson(getIntentStringExtra(intent, intentExtra));
        Log.d("parts__", "data : " + nson);

        biayaMekanik1 =  nson.get("BIAYA_MEKANIK_1").asInteger();
        biayaMekanik2 =  nson.get("BIAYA_MEKANIK_2").asInteger();
        biayaMekanik3 =  nson.get("BIAYA_MEKANIK_3").asInteger();
        batasanGaransiKm = nson.get("GARANSI_PART_KM").asInteger();
        batasanGaransiBulan = nson.get("GARANSI_PART_BULAN").asInteger();
        kmKendaraan = getIntentIntegerExtra("KM");
        garansiPart = nson.get("GARANSI_PART_PABRIKAN").asString();
        berkalaBulan = Integer.parseInt(formatOnlyNumber(nson.get("BERKALA_BULAN").asString()));
        berkalaKm = Integer.parseInt(formatOnlyNumber(nson.get("BERKALA_KM").asString())) + kmKendaraan;
        hpp = nson.get("HPP").asString();
        stock = nson.get("STOCK_RUANG_PART").asInteger();
        idLokasiPart = nson.get("LOKASI_PART_ID").asString();
        String waktuDefault = String.format("%02d:%02d:%02d", nson.get("RATA2_WAKTU_KERJA_HARI").asInteger(), nson.get("RATA2_WAKTU_KERJA_JAM").asInteger(), nson.get("RATA2_WAKTU_KERJA_MENIT").asInteger());
        find(R.id.et_waktuDefault, EditText.class).setText(waktuDefault);
        etJumlah.setText("1");
        etBiayaJasa.setText(RP + NumberFormatUtils.formatRp(String.valueOf(calculateBiayaMekanik(nson))));
        if (stock == 0) {
            isPartKosong = true;
            if(!isTambahPart) initPartKosongValidation(nson, false);
        }

        if (nson.get("FINAL_INS").asString().equals("Y") && !nson.get("FINAL_INS").asString().isEmpty()) {
            inspeksi = "Y";
        } else {
            inspeksi = "N";
        }

        if (nson.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") || nson.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
            find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
            find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setHint("HARGA JUAL FLEXIBLE");
            try {
                etHpp.setText(RP + formatRp(nson.get("HARGA_JUAL").asString()));
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
            if (!nson.get("HARGA_JUAL").asString().isEmpty()) {
                discPart = calculateDisc(nson.get("DISCOUNT_PART").asDouble(), nson.get("HARGA_JUAL").asInteger());
            }
            //showDialogDisc(nson.get("DISCOUNT_PART").asDouble());
            etDiscPart.setText(RP + NumberFormatUtils.formatRp(String.valueOf((int) discPart)));
        }

        find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countClick++;
                if (find(R.id.et_waktuSet, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00)) &&
                        find(R.id.et_waktuDefault, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00))) {
                    find(R.id.et_waktuSet, EditText.class).requestFocus();
                    showWarning("Waktu Kerja Harus di Isi");
                    return;
                }

                if (!isPartKosong && find(R.id.tl_jumlah_part, TextInputLayout.class).isHelperTextEnabled()) {
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
                    if (countClick == 1) {
                        nextForm(nson);
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            }
        });

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

    private void nextForm(Nson nson) {
        int hargaPart = Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString()));
        int hargaJasa = Integer.parseInt(formatOnlyNumber(etBiayaJasa.getText().toString()));
        int jumlahPart = 0, totalPart = 0, totalJasa = 0;
        int totalHarga = 0;
        double discJasa = 0;
        String partKosong = "";
        String hari;
        String jam;
        String menit;
        String inspeksiJam = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(3, 5);
        String inspeksiMenit = find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString().substring(6, 8);

        if(find(R.id.et_waktuSet, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00)) &&
                !find(R.id.et_waktuDefault, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00))){
            hari = find(R.id.et_waktuDefault, EditText.class).getText().toString().substring(0, 2);
            jam = find(R.id.et_waktuDefault, EditText.class).getText().toString().substring(3, 5);
            menit = find(R.id.et_waktuDefault, EditText.class).getText().toString().substring(6, 8);
        }else{
            hari = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(0, 2);
            jam = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(3, 5);
            menit = find(R.id.et_waktuSet, EditText.class).getText().toString().substring(6, 8);
        }


        if (etJumlah.getText().toString().isEmpty()) {
            jumlahPart++;
        } else {
            jumlahPart = Integer.parseInt(etJumlah.getText().toString());
        }

        if (!etDiscJasa.getText().toString().isEmpty()) {
            discJasa = Double.parseDouble(etDiscJasa.getText().toString()
                    .replace("%", "")
                    .replace(",", "."));
            discJasa = calculatePercentage(discJasa, hargaJasa);
        }

        if (discJasa > 0) {
            totalJasa = (int) (hargaJasa - discJasa);
        }
        if (discPart > 0) {
            totalPart = (int) (hargaPart - discPart) * jumlahPart;
        } else {
            totalPart = hargaPart * jumlahPart;
        }

        totalHarga = totalPart + hargaJasa;

        if (isPartKosong) {
            partKosong = "Y";
            sendData.set("DP", formatOnlyNumber(formatRp(String.valueOf(calculateDp(Double.parseDouble(getSetting("DP_PERSEN")), totalPart)))));
        } else {
            partKosong = "N";
            sendData.set("DP", "");
        }

        if (isPartWajib) {
            sendData.set("HARGA_PART", hargaPart);
            sendData.set("PERGANTIAN", getIntentStringExtra("PERGANTIAN"));
            sendData.set("INSPEKSI", "");
            sendData.set("DISCOUNT_PART", discPart);
        } else {

            if (hargaPart > 0) {
                sendData.set("HARGA_PART", hargaPart);
            }

            sendData.set("INSPEKSI", inspeksi);
            sendData.set("PERGANTIAN", "0");
            sendData.set("DISCOUNT_PART", discPart);
        }
        sendData.set("BERKALA_KM", berkalaKm);
        sendData.set("BERKALA_BULAN", getBerkalaBulan(berkalaBulan));
        sendData.set("WAKTU_INSPEKSI", find(R.id.et_waktu_set_inspeksi, EditText.class).getText().toString());
        if(find(R.id.et_waktuSet, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00)) &&
                !find(R.id.et_waktuDefault, EditText.class).getText().toString().equals(getResources().getString(R.string._00_00_00))){
            sendData.set("WAKTU_KERJA", find(R.id.et_waktuDefault, EditText.class).getText().toString());
        }else{
            sendData.set("WAKTU_KERJA", find(R.id.et_waktuSet, EditText.class).getText().toString());
        }

        if (kmKendaraan > 0) {
            if (kmKendaraan < batasanGaransiKm) {
                sendData.set("GARANSI", garansiPart);
            } else {
                sendData.set("GARANSI", "N");
            }
        } else {
            sendData.set("GARANSI", "");
        }

        sendData.set("NAMA_PART", nson.get("NAMA_PART").asString());
        sendData.set("NO_PART", nson.get("NO_PART").asString());
        sendData.set("PART_ID", nson.get("PART_ID").asString());
        sendData.set("JUMLAH", jumlahPart);
        sendData.set("DISCOUNT_JASA", discJasa);
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
        sendData.set("WAKTU_PESAN", waktuPesan);
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
                if (inspeksi.equals("Y")) {
                    getTimesDialog(find(R.id.et_waktu_set_inspeksi, EditText.class));
                } else {
                    showWarning("Part tidak perlu INSPEKSI");
                }
                break;
            case R.id.btn_img_waktu_kerja:
                getWaktuKerja();
                break;
        }
    }

    private void getWaktuKerja() {
        TimePicker_Dialog timePickerDialog = new TimePicker_Dialog();
        timePickerDialog.show(getSupportFragmentManager(), "TimePicker");
        timePickerDialog.getTimes(new TimePicker_Dialog.OnClickDialog() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void getTime(int day, int hours, int minutes) {
                find(R.id.et_waktuSet, EditText.class).setText(String.format("%02d", day) + ":" + String.format("%02d", hours) + ":" + String.format("%02d", minutes));
                Nson nson = Nson.newObject();
                nson.set("RATA2_WAKTU_KERJA_HARI", day);
                nson.set("RATA2_WAKTU_KERJA_JAM", hours);
                nson.set("RATA2_WAKTU_KERJA_MENIT", minutes);

                if(isMekanik1){
                    nson.set("JENIS_MEKANIK", 1);
                }else{
                    if(isMekanik2){
                        nson.set("JENIS_MEKANIK", 2);
                    }else{
                        if(isMekanik3){
                            nson.set("JENIS_MEKANIK", 3);
                        }
                    }
                }

                int biaya = calculateBiayaMekanik(nson);
                etBiayaJasa.setText(RP + NumberFormatUtils.formatRp(biaya));
            }

            @Override
            public void getYear(int year) {

            }
        });
    }


    @SuppressLint("DefaultLocale")
    private String getBerkalaBulan(int berkalaBulan) {
        if (berkalaBulan == 0) return "";
        String nows = currentDateTime("dd/MM/yyyy");
        String[] split = nows.split("/");

        int day = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int year = Integer.parseInt(split[2]);

        if (berkalaBulan >= 12) {
            berkalaBulan = berkalaBulan - 12;
            year += 1;
        }

        month += berkalaBulan;
        if (month >= 12) {
            month -= 12;
            year += 1;
            if (month >= 12) {
                month -= 12;
                year += 1;
            }
        }

        return String.format("%02d/%02d/%02d", day, month, year);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            loadData(PART, data);
        }
    }
}
