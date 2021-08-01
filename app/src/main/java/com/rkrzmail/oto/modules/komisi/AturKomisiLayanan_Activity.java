package com.rkrzmail.oto.modules.komisi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.rkrzmail.srv.NumberFormatUtils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KOMISI_LAYANAN;
import static com.rkrzmail.utils.APIUrls.KOMISI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturKomisiLayanan_Activity extends AppActivity {

    private Spinner spLayanan, spAktivitas, spStatus;
    private EditText etKomisi;
    private Nson layananList = Nson.newArray(), dataList = Nson.newArray();

    private final List<String> aktivitasList = Arrays.asList(
            "--PILIH--",
            "BOOKING",
            "CHECKIN ANTRIAN",
            "PENUGASAN",
            "MEKANIK SELESAI",
            "INSPEKSI SELESAI",
            "CASH, DEBET, KREDIT, INVOICE"
    );
    private double komisiPercent = 0;
    private String layananId="", namaLayanan="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_layanan);
        initToolbar();
        initComponent();
        loadData();
        initListener();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spAktivitas = findViewById(R.id.sp_layanan_aktivitas);
        spLayanan = findViewById(R.id.sp_komisi_layanan);
        spStatus = findViewById(R.id.sp_layanan_status);
        etKomisi = findViewById(R.id.et_komisi_layanan_percent);
        setSpLayanan();
    }

    private void initListener(){
        spLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if(dataList.size() > 0){
                    for (int i = 0; i < dataList.size(); i++) {
                        if (dataList.get(i).get("NAMA_LAYANAN").asString().contains(item)) {
                            layananId = dataList.get(i).get("ID").asString();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        etKomisi.addTextChangedListener(new TextWatcher() {
            int prevLength = 0; // detected keyEvent action delete
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

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


                    if (!find(R.id.tv_total_komisi_layanan, TextView.class).getText().toString().isEmpty()) {
                        double komisiAvail = Double.parseDouble(find(R.id.tv_total_komisi_layanan, TextView.class).getText().toString()
                                .replace("TOTAL : ", "")
                                .replace("%", "")
                                .replace(",", "."));
                        double komisiInput = Double.parseDouble(etKomisi.getText().toString()
                                .replace("%", "")
                                .replace(",", "."));

                        if(komisiInput > komisiPercent){
                            find(R.id.tl_komisilayanan_percent, TextInputLayout.class).setErrorEnabled(true);
                            find(R.id.tl_komisilayanan_percent, TextInputLayout.class).setError("KOMISI TIDAK VALID");
                        }else{
                            find(R.id.tl_komisilayanan_percent, TextInputLayout.class).setErrorEnabled(false);
                        }

                        double result = prevLength > editable.length() ? komisiPercent - komisiInput : komisiAvail - komisiInput;
                        find(R.id.tv_total_komisi_layanan, TextView.class).setText("TOTAL : " + NumberFormatUtils.formatPercent(result));
                    }
                } catch (NumberFormatException e) {
                    Log.e("percent_", "onTextChanged: ", e);
                }

                etKomisi.addTextChangedListener(this);
            }
        });

        spAktivitas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getKomisi(adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        namaLayanan = data.get("NAMA_LAYANAN").asString();
        spAktivitas.setEnabled(getIntent().hasExtra(ADD));
        spLayanan.setEnabled(getIntent().hasExtra(ADD));
        find(R.id.btn_simpan_komisiLayanan, Button.class).setText(getIntent().hasExtra(ADD) ? "SIMPAN" : "UPDATE");
        etKomisi.setText(data.get("KOMISI_PERCENT").asString());
        find(R.id.tv_total_komisi_layanan, TextView.class).setText("TOTAL : " + komisiPercent + " %");
        setSpinnerOffline(aktivitasList, spAktivitas, getIntent().hasExtra(ADD) ? "" : data.get("AKTIVITAS").asString());
        setSpinnerOffline(Arrays.asList("--PILIH--", "AKTIF", "NON AKTIF"), spStatus, getIntent().hasExtra(ADD) ? "" : data.get("STATUS").asString());

        find(R.id.btn_simpan_komisiLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.btn_simpan_komisiLayanan, Button.class).getText().toString().equals("SIMPAN")) {
                    if (spAktivitas.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("STATUS HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (spLayanan.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("TIPE JASA HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (spStatus.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("AKTIVITAS HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (formatOnlyNumber(find(R.id.tv_total_komisi_layanan, TextView.class).getText().toString()).equals("0")) {
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
                args.put("aktivitas", spAktivitas.getSelectedItem().toString() + ",");
                args.put("nama", spLayanan.getSelectedItem().toString());
                args.put("komisi", NumberFormatUtils.clearPercent(etKomisi.getText().toString()));
                args.put("status", spStatus.getSelectedItem().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
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
                args.put("idKomisi", nson.get("ID").asString());
                args.put("komisi", NumberFormatUtils.clearPercent(etKomisi.getText().toString()));
                args.put("status", spStatus.getSelectedItem().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Memperbarui Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Memperbarui Aktifitas");
                }
            }
        });
    }
    
    private void setSpLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "OTOMOTIVES");
                args.put("layanan", "BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                layananList.add("--PILIH--");
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        layananList.add(result.get("data").get(i).get("NAMA_LAYANAN").asString());
                        dataList.add(result.get("data").get(i));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, layananList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLayanan.setAdapter(spinnerAdapter);
                    if (!namaLayanan.isEmpty()) {
                        for (int in = 0; in < spLayanan.getCount(); in++) {
                            if (spLayanan.getItemAtPosition(in).toString().contains(namaLayanan)) {
                                spLayanan.setSelection(in);
                            }
                        }
                    }

                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getKomisi(final String aktivitas) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "KOMISI");
                args.put("aktivitas", aktivitas);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    komisiPercent = 0;
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    if (nListArray.size() > 0) {
                        for (int i = 0; i < nListArray.size(); i++) {
                            komisiPercent += nListArray.get(i).get("KOMISI_PERCENT").asDouble();
                        }
                    }
                    komisiPercent = 100 - komisiPercent;
                    find(R.id.tv_total_komisi_layanan, TextView.class).setText("TOTAL : " + komisiPercent + " %");
                } else {
                    showError(ERROR_INFO);
                }
            }
        });

    }
}
