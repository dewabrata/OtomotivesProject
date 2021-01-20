package com.rkrzmail.oto.modules.komisi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NumberFormatUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KOMISI_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class AturKomisiJasaLain_Activity extends AppActivity {

    private Spinner spTipeJasa, spAktifitas, spStatus;
    private EditText etKomisi;

    private final List<String> tipeJasaList = Arrays.asList(
            "-- PILIH --",
            "JASA PART",
            "JASA LAIN"
    );
    private final List<String> aktivitasList = Arrays.asList(
            "--PILIH--",
            "BOOKING",
            "CHECK IN, TAMBAH PART-JASA",
            "PENUGASAN MEKANIK",
            "PENUGASAN MEKANIK",
            "MEKANIK SELESAI",
            "INSPEKSI SELESAI",
            "CASH, DEBET, KREDIT, INVOICE"
    );
    private double komisiPercent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_jasa_lain_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spAktifitas = findViewById(R.id.sp_aktifitas);
        spTipeJasa = findViewById(R.id.sp_tipe_jasa);
        spStatus = findViewById(R.id.sp_status);
        etKomisi = findViewById(R.id.et_komisi_percent);

        etKomisi.addTextChangedListener(new TextWatcher() {
            int prevLength = 0; // detected keyEvent action delete
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevLength = charSequence.length();
            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) return;
                etKomisi.removeTextChangedListener(this);
                try {
                    text = new NumberFormatUtils().formatOnlyNumber(text);
                    double percentValue = Double.parseDouble(text.isEmpty() ? "0" : text) / 1000;

                    NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                    percentageFormat.setMinimumFractionDigits(1);
                    String percent = percentageFormat.format(percentValue);

                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(6);

                    etKomisi.setFilters(filterArray);
                    etKomisi.setText(percent);
                    etKomisi.setSelection(percent.length() - 1);

                    if (!find(R.id.tv_total_komisi, TextView.class).getText().toString().isEmpty()) {
                        double komisiAvail = Double.parseDouble(find(R.id.tv_total_komisi, TextView.class).getText().toString()
                                .replace("TOTAL : ", "")
                                .replace("%", "")
                                .replace(",", "."));
                        double komisiInput = Double.parseDouble(etKomisi.getText().toString()
                                .replace("%", "")
                                .replace(",", "."));
                        double result = prevLength > editable.length() ?  komisiPercent - komisiInput : komisiAvail - komisiInput;

                        find(R.id.tv_total_komisi, TextView.class).setText("TOTAL : " + NumberFormatUtils.formatPercent(result) + " %");
                    }
                } catch (NumberFormatException e) {
                    Log.e("percent_", "onTextChanged: ", e);
                }

                etKomisi.addTextChangedListener(this);
            }
        });

        loadData();
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));

        etKomisi.setText(data.get("KOMISI").asString());
        komisiPercent = getIntent().getDoubleExtra("AVAIL KOMISI", 0);
        find(R.id.tv_total_komisi, TextView.class).setText("TOTAL : " + komisiPercent + " %");
        setSpinnerOffline(tipeJasaList, spTipeJasa, getIntent().hasExtra(ADD) ? "" : data.get("").asString());
        setSpinnerOffline(aktivitasList, spAktifitas, getIntent().hasExtra(ADD) ? "" : data.get("").asString());
        setSpinnerOffline(Arrays.asList("--PILIH--", "AKTIF", "NON AKTIF"), spStatus, getIntent().hasExtra(ADD) ? "" : data.get("").asString());

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.btn_simpan, Button.class).getText().toString().equals("SIMPAN")) {
                    if (spStatus.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("STATUS HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (spTipeJasa.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("TIPE JASA HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (spAktifitas.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("AKTIVITAS HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (formatOnlyNumber(find(R.id.tv_total_komisi, TextView.class).getText().toString()).equals("0")) {
                        showWarning("KOMISI TIDAK VALID", Toast.LENGTH_LONG);
                        etKomisi.requestFocus();
                    } else {
                        saveData();
                    }

                } else {
                    updateData(data);
                }
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("komisi", etKomisi.getText().toString());
                args.put("aktivitas", spAktifitas.getSelectedItem().toString());
                args.put("tipeJasa", spTipeJasa.getSelectedItem().toString());
                args.put("status", spStatus.getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_JASA_LAIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }

    private void updateData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("id", nson.get("id").asString());
                args.put("komisi", etKomisi.getText().toString());
                args.put("aktivitas", spAktifitas.getSelectedItem().toString());
                args.put("kategori", spTipeJasa.getSelectedItem().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_JASA_LAIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Memperbarui Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal Memperbarui Aktivitas!");
                }
            }
        });
    }
}
