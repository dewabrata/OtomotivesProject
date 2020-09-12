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
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.RupiahFormat;

import java.text.DecimalFormat;
import java.util.Map;

public class DetailJualPart_Activity extends AppActivity {

    private EditText etHpp, etHargaJual, etDisc, etJumlah;
    private int finalStock, stock, minStock;
    private String namaPart;
    private Nson parts = Nson.newObject();
    private boolean isJual;

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
        final Nson nson = Nson.readJson(getIntentStringExtra("part"));
        namaPart = nson.get("NAMA_PART").asString();
        getJualPart();
        Log.d("detailpartttt", "data" + Nson.readJson(getIntentStringExtra("part")));

        etDisc = findViewById(R.id.et_disc_detailPart);
        etHargaJual = findViewById(R.id.et_hargaJual_detailPart);
        etHpp = findViewById(R.id.et_hpp_detailPart);
        etJumlah = findViewById(R.id.et_jumlah_detailPart);

        loadData();

        etHargaJual.addTextChangedListener(new RupiahFormat(etHargaJual));
        etJumlah.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
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
                stock = nson.get("STOCK").asInteger();
                minStock = nson.get("STOCK_MINIMUM").asInteger();
                String text = s.toString();
                if (!text.equals("")) {
                    int jumlah = Integer.parseInt(text);
                    if (jumlah > stock) {
                        find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(true);
                        find(R.id.tl_jumlah, TextInputLayout.class).setError("Jumlah Melebihi Stock tersedia");
                    } else if (jumlah > minStock) {
                        find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(true);
                        find(R.id.tl_jumlah, TextInputLayout.class).setError("Jumlah Melebihi Stock Minimum");
                    } else {
                        find(R.id.tl_jumlah, TextInputLayout.class).setHelperTextEnabled(false);
                    }
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
            Nson n = Nson.readJson(getIntentStringExtra("part"));
            Log.d("detail__", "data : " + n);
            if (n.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE") || n.get("HARGA_JUAL").asString().equalsIgnoreCase("FLEXIBLE")) {
                find(R.id.tl_hpp, TextInputLayout.class).setVisibility(View.VISIBLE);
                etHpp.setEnabled(false);
                etHpp.setText("Rp. " + formatRp(n.get("HPP").asString()));
                etHargaJual.setEnabled(true);
                showInfo("Pola Harga Jual Flexible, Silahkan Masukkan Harga Jual", Toast.LENGTH_LONG);
            } else if (n.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("NOMINAL")) {
                etHargaJual.setText("Rp. " + formatRp(n.get("HARGA_JUAL").asString()));
            } else if (n.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("BELI + MARGIN")) {
                etHargaJual.setText("Rp. " + formatRp(n.get("HARGA_JUAL").asString()));
            } else if (n.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("RATA - RATA + MARGIN")) {
                etHargaJual.setText("Rp. " + formatRp(n.get("HARGA_JUAL").asString()));
            }else if (n.get("POLA_HARGA_JUAL").asString().equalsIgnoreCase("HET")) {
                etHargaJual.setText("Rp. " + formatRp(n.get("HARGA_JUAL").asString()));
            }


            etDisc.setText(n.get("DISCOUNT").asString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        Intent i = this.getIntent();
        Nson n = Nson.readJson(getIntentStringExtra(i, "part"));

        parts.set("NAMA_PELANGGAN", n.get("NAMA_PELANGGAN").asString());
        parts.set("JENIS_KENDARAAN", n.get("JENIS_KENDARAAN").asString());
        parts.set("NAMA_USAHA", n.get("NAMA_USAHA").asString());
        parts.set("NO_PONSEL", n.get("NO_PONSEL").asString());
        parts.set("PART_ID", n.get("PART_ID").asString());
        parts.set("NO_PART", n.get("NO_PART").asString());
        parts.set("NAMA_PART", n.get("NAMA_PART").asString());
        parts.set("HARGA_JUAL", etHargaJual.getText().toString());
        parts.set("JUMLAH", etJumlah.getText().toString());
        parts.set("DISC", etDisc.getText().toString());
        int jumlah = 0;
        if (etJumlah.getText().toString().isEmpty()) {
            jumlah++;
        } else {
            jumlah = Integer.parseInt(etJumlah.getText().toString());
        }

        parts.set("TOTAL", jumlah * Integer.parseInt(etHargaJual.getText().toString().trim().replaceAll("[^0-9]", "")));
        parts.set("NET", jumlah * Integer.parseInt(etHargaJual.getText().toString().trim().replaceAll("[^0-9]", "")));

        Intent intent = new Intent();
        intent.putExtra("part", parts.toJson());
        Log.d("partcuy", "dataParts : " + parts.toJson());
        Log.d("partcuy", "fromCariPart : " + n.toJson());

        setResult(RESULT_OK, intent);
        finish();
    }

    private void getJualPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturjualpart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.size(); i++) {
                        if (namaPart.equalsIgnoreCase(result.get("data").get(i).get("NAMA").asString())) {
                            showWarning("Part Duplikat", Toast.LENGTH_LONG);
                            finish();
                        }
                    }
                }
            }
        });
    }
}
