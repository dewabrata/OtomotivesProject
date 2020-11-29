package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.Map;

import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class DetailJualPart_Activity extends AppActivity {

    private EditText etHpp, etHargaJual, etDisc, etJumlah;
    private int finalStock, stock, minStock;
    private String namaPart;
    private Nson parts = Nson.newObject();
    private Nson getData;

    private String idLokasiPart = "";
    private boolean isJual, isPartKosong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_jual_part_);
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
        etHargaJual.addTextChangedListener(new RupiahFormat(etHargaJual));
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
                    if (jumlah > stock) {
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
                }
                Log.d("Jual__", "onClick: " + stock);
                try {
                    if (Integer.parseInt(etJumlah.getText().toString()) > stock) {
                        etJumlah.setError("Jumlah Tidak Valid");
                        etJumlah.requestFocus();
                        return;
                    }
                    if (Integer.parseInt(etJumlah.getText().toString()) > minStock) {
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
            namaPart = getData.get("NAMA_PART").asString();
            idLokasiPart = getData.get("LOKASI_PART_ID").asString();
            Log.d("detail__", "data : " + getData);

            if (getData.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") || getData.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                find(R.id.tl_hpp, TextInputLayout.class).setVisibility(View.VISIBLE);
                etHpp.setEnabled(false);
                etHpp.setText("Rp. " + formatRp(getData.get("HPP").asString()));
                etHargaJual.setEnabled(true);
                showInfo("Pola Harga Jual Flexible, Silahkan Masukkan Harga Jual", Toast.LENGTH_LONG);
            } else if (getData.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("NOMINAL")) {
                etHargaJual.setText("Rp. " + formatRp(getData.get("HARGA_JUAL").asString()));
            } else if (getData.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("BELI + MARGIN")) {
                etHargaJual.setText("Rp. " + formatRp(getData.get("HARGA_JUAL").asString()));
            } else if (getData.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("RATA - RATA + MARGIN")) {
                etHargaJual.setText("Rp. " + formatRp(getData.get("HARGA_JUAL").asString()));
            } else if (getData.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("HET")) {
                etHargaJual.setText("Rp. " + formatRp(getData.get("HARGA_JUAL").asString()));
            }

            etDisc.setText(getData.get("DISCOUNT").asString());

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
                            //find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            if (nson.get("DISCOUNT_PART").asString() != null) {
                                //find(R.id.ly_disc_part_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            }
                            if (nson.get("DISCOUNT_JASA") != null) {
                                //find(R.id.ly_discJasa_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            }
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
