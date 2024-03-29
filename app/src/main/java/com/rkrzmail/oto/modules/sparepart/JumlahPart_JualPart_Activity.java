package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.naa.data.Nson;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import static com.rkrzmail.srv.NumberFormatUtils.calculatePercentage;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class JumlahPart_JualPart_Activity extends AppActivity {

    private EditText etHpp, etHargaJual, etDisc, etJumlah;
    private int finalStock, stock, minStock;
    private String namaPart;
    private Nson parts = Nson.newObject();
    private Nson getData;

    private String idLokasiPart = "";
    private boolean isJual, isPartKosong = false;
    private double discPart = 0;
    private int hppPart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_detail_jual_part);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_detailPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Detail Jual Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDisc = findViewById(R.id.et_disc_detailPart);
        etHargaJual = findViewById(R.id.et_hargaJual_detailPart);
        etHpp = findViewById(R.id.et_hpp_detailPart);
        etJumlah = findViewById(R.id.et_jumlah_detailPart);

        loadData();
        initListener();
    }

    private void initListener() {
        etHargaJual.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etHargaJual));
        etJumlah.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (etJumlah.getText().toString().isEmpty() && hasFocus) {
                    etJumlah.setText("1");
                }
            }
        });

        etJumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                stock = getData.get("STOCK_RUANG_PART").asInteger();
                minStock = getData.get("STOCK_MINIMUM").asInteger();
                String text = s.toString();
                if (!text.equals("")) {
                    int jumlah = Integer.parseInt(text);
                    if (jumlah > stock && !isPartKosong) {
                        find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(true);
                        find(R.id.tl_jumlah, TextInputLayout.class).setError("Jumlah Melebihi Stock tersedia");
                    } else {
                        find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(false);
                    }
                     /*else if (jumlah > minStock) {
                        find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(true);
                        find(R.id.tl_jumlah, TextInputLayout.class).setError("Jumlah Melebihi Stock Minimum");
                    }*/
                }
            }
        });

        find(R.id.btn_simpan_detailPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etJumlah.getText().toString().isEmpty()) {
                    showInfo("Jumlah Part Tidak Boleh Kosong");
                    return;
                }
                if (find(R.id.tl_hpp, TextInputLayout.class).getVisibility() == View.VISIBLE) {
                    if (etHargaJual.getText().toString().isEmpty()) {
                        showWarning("Harga Jual Harus Di isi");
                        return;
                    }
                    if (Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString())) < Integer.parseInt(formatOnlyNumber(etHpp.getText().toString()))) {
                        etHargaJual.setError("Harga Jual Kurang dari HPP Part");
                        return;
                    }
                }
                Log.d("Jual__", "onClick: " + stock);
                try {
                    if (Integer.parseInt(etJumlah.getText().toString()) > stock && !isPartKosong) {
                        etJumlah.setError("Jumlah Tidak Valid");
                        etJumlah.requestFocus();
                        return;
                    }
                    if (Integer.parseInt(etJumlah.getText().toString()) > minStock && !isPartKosong) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Jumlah Melebihi Stock Mininum, Jual Part ?", "OK", "TIDAK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showSuccess("Part di Tambahkan ke Daftar Jual");
                                find(R.id.tl_jumlah, TextInputLayout.class).setErrorEnabled(false);
                                minStock = Integer.parseInt(etJumlah.getText().toString());
                                saveData();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                etJumlah.requestFocus();
                            }
                        });
                        return;
                    }

                } catch (Exception e) {
                    Log.d("Jual__", "Stcok : " + e.getMessage() + "cause : " + e.getCause());
                }
                saveData();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        try {
            getData = Nson.readJson(getIntentStringExtra(PART));
            initPartKosongValidation(getData);
            hppPart = getData.get("HPP").asInteger();
            namaPart = getData.get("NAMA_PART").asString();
            idLokasiPart = getData.get("LOKASI_PART_ID").asString();
            Log.d("detail__", "data : " + getData);

            if (getData.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                find(R.id.tl_hpp, TextInputLayout.class).setVisibility(View.VISIBLE);
                find(R.id.tl_hpp, TextInputLayout.class).setHint("HARGA JUAL FLEXIBLE");
                etHpp.setEnabled(false);
                etHpp.setText(RP + formatRp(getData.get("HARGA_JUAL").asString()));
                etHargaJual.setEnabled(true);
            } else {
                etHargaJual.setText(RP + formatRp(getData.get("HARGA_JUAL").asString()));

            }

            if (!getData.get("DISCOUNT_PART").asString().isEmpty()) {
                if (!getData.get("HARGA_JUAL").asString().isEmpty()) {
                    discPart = NumberFormatUtils.calculatePercentage(getData.get("DISCOUNT_PART").asDouble(), getData.get("HARGA_JUAL").asInteger());
                }
                etDisc.setText(RP + NumberFormatUtils.formatRp(String.valueOf((int) discPart)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPartKosongValidation(final Nson nson) {
        if (nson.get("STOCK_RUANG_PART").asInteger() == 0) {
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Buka Form Part Kosong ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isPartKosong = true;
                            find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.et_dp, EditText.class).setText(Tools.convertToDoublePercentage(getSetting("DP_PERSEN")) + "%");
                            find(R.id.et_waktu_pesan, EditText.class).setText(nson.get("WAKTU_PESAN_HARI").asString());
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            dialog.dismiss();
                        }
                    });
        }

    }


    private void saveData() {
        int jumlah = 0;
        double net = 0;

        if (etJumlah.getText().toString().isEmpty()) {
            jumlah++;
        } else {
            jumlah = Integer.parseInt(etJumlah.getText().toString());
        }
        int total = jumlah * Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString()));
        if (!etDisc.getText().toString().isEmpty()) {
            double disc = Double.parseDouble(formatOnlyNumber(etDisc.getText().toString()));
            net = (disc / 100) * total;
        } else {
            net = total;
        }

        parts.set("NAMA_PELANGGAN", getData.get("NAMA_PELANGGAN").asString());
        parts.set("JENIS_KENDARAAN", getData.get("JENIS_KENDARAAN").asString());
        parts.set("NAMA_USAHA", getData.get("NAMA_USAHA").asString());
        parts.set("NO_PONSEL", getData.get("NO_PONSEL").asString());
        parts.set("PART_ID", getData.get("PART_ID").asString());
        parts.set("NO_PART", getData.get("NO_PART").asString());
        parts.set("NAMA_PART", getData.get("NAMA_PART").asString());
        parts.set("HARGA_PART", formatOnlyNumber(etHargaJual.getText().toString()));
        parts.set("JUMLAH", etJumlah.getText().toString());
        parts.set("DISC", formatOnlyNumber(etDisc.getText().toString()));
        parts.set("LOKASI_PART_ID", idLokasiPart);
        parts.set("TOTAL", total);
        parts.set("NET", net);
        parts.set("HPP", hppPart);

        if (isPartKosong) {
            parts.set("WAKTU_PESAN", find(R.id.et_waktu_pesan, EditText.class).getText().toString());
            parts.set("DP", calculatePercentage(Double.parseDouble(formatOnlyNumber(find(R.id.et_dp, EditText.class).getText().toString())),
                    Integer.parseInt(formatOnlyNumber(etHargaJual.getText().toString())))
            );
        } else {
            parts.set("WAKTU_PESAN", "");
            parts.set("DP", "");
        }

        Intent intent = new Intent();
        intent.putExtra(PART, parts.toJson());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART)
            getData = Nson.readJson(getIntentStringExtra(data, PART));
        loadData();
    }
}
