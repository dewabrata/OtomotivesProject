package com.rkrzmail.oto.modules.komisi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KOMISI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturKomisiPart_Activity extends AppActivity {

    private EditText etKomisi;
    private Spinner spStatus, spAktivitas, spGroupPart;

    private Nson availKomisi = Nson.newArray();
    private Nson grupPartData = Nson.newArray();
    private final List<String> aktivitasList = Arrays.asList(
            "--PILIH--",
            "BOOKING",
            "CHECK IN, TAMBAH PART-JASA, JUAL PART, SERAH TERIMA PART",
            "PENUGASAN MEKANIK",
            "MEKANIK SELESAI",
            "INSPEKSI SELESAI",
            "CASH, DEBET, KREDIT, INVOICE"
    );

    private double komisiPercent = 0;
    private String groupPartId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_part);
        initToolbar();
        initComponent();
        loadData();
        initListener();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spAktivitas = findViewById(R.id.sp_aktifitas);
        spGroupPart = findViewById(R.id.sp_grup_part);
        spStatus = findViewById(R.id.sp_status);
        etKomisi = findViewById(R.id.et_komisi_percent);
    }

    private void initListener() {
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

                        if (komisiInput > komisiPercent) {
                            find(R.id.tl_komisi_percent, TextInputLayout.class).setErrorEnabled(true);
                            find(R.id.tl_komisi_percent, TextInputLayout.class).setError("KOMISI TIDAK VALID");
                        } else {
                            find(R.id.tl_komisi_percent, TextInputLayout.class).setErrorEnabled(false);
                        }

                        double result = prevLength > editable.length() ? komisiPercent - komisiInput : komisiAvail - komisiInput;
                        find(R.id.tv_total_komisi, TextView.class).setText("TOTAL : " + NumberFormatUtils.formatPercent(result));
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

        spGroupPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase(grupPartData.get(i).get("NAMA_GROUP_PART").asString())) {
                    groupPartId = grupPartData.get(i).get("ID").asString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));

        availKomisi.add(getIntentStringExtra("AVAIL"));
        spAktivitas.setEnabled(getIntent().hasExtra(ADD));
        spGroupPart.setEnabled(getIntent().hasExtra(ADD));
        etKomisi.setText(data.get("KOMISI_PERCENT").asString());

        setSpGroupPart(getIntent().hasExtra(ADD) ? "" : data.get("NAMA_GROUP_PART").asString());
        setSpAktivitas(getIntent().hasExtra(ADD) ? "" : data.get("AKTIVITAS").asString());
        setSpinnerOffline(Arrays.asList("--PILIH--", "AKTIF", "NON AKTIF"), spStatus, getIntent().hasExtra(ADD) ? "" : data.get("STATUS").asString());

        find(R.id.btn_simpan, Button.class).setText(getIntent().hasExtra(ADD) ? "SIMPAN" : "UPDATE");
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.btn_simpan, Button.class).getText().toString().equals("SIMPAN")) {
                    if (spStatus.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("STATUS HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (spGroupPart.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("GROUP PART HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (spAktivitas.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("AKTIVITAS HARUS DI PILIH", Toast.LENGTH_LONG);
                    } else if (find(R.id.tl_komisi_percent, TextInputLayout.class).isErrorEnabled()) {
                        showWarning("KOMISI TIDAK VALID", Toast.LENGTH_LONG);
                        etKomisi.requestFocus();
                    } else {
                        saveData();
                    }

                } else {
                    if (find(R.id.tl_komisi_percent, TextInputLayout.class).isErrorEnabled()) {
                        showWarning("KOMISI TIDAK VALID", Toast.LENGTH_LONG);
                        etKomisi.requestFocus();
                    } else {
                        updateData(data);
                    }
                }
            }
        });
    }

    private void setSpAktivitas(String selection) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, aktivitasList) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                final View v = super.getDropDownView(position, convertView, parent);
                v.post(new Runnable() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void run() {
                        ((TextView) v.findViewById(android.R.id.text1)).setSingleLine(false);
                        ((TextView) v.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
                        ((TextView) v.findViewById(android.R.id.text1)).setTextAlignment(Gravity.CENTER);
                    }
                });
                return v;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAktivitas.setAdapter(spinnerAdapter);
        if (!selection.isEmpty()) {
            for (int in = 0; in < spAktivitas.getCount(); in++) {
                if (spAktivitas.getItemAtPosition(in).toString().contains(selection)) {
                    spAktivitas.setSelection(in);
                    break;
                }
            }
        }
    }

    private void setSpGroupPart(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "GROUP PART");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    final List<String> grupPartList = new ArrayList<>();
                    grupPartList.add("--PILIH--");
                    grupPartData.add("--PILIH--");
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        grupPartData.add(Nson.newObject()
                                .set("ID", result.get(i).get("ID").asString())
                                .set("NAMA_GROUP_PART", result.get(i).get("NAMA_GROUP_PART").asString())
                        );
                        grupPartList.add(result.get(i).get("NAMA_GROUP_PART").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, grupPartList)/*{
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = null;
                            if(availKomisi.size() > 0){
                                for (int i = 0; i < availKomisi.size(); i++) {
                                    if(availKomisi.get(i).get("NAMA_GROUP_PART").asString().equals(grupPartList.get(position))){
                                        TextView mTextView = new TextView(getContext());
                                        mTextView.setVisibility(View.GONE);
                                        mTextView.setHeight(0);
                                        view = mTextView;
                                        break;
                                    }else{
                                        view = super.getDropDownView(position, null, parent);
                                    }
                                }
                            }else{
                                view = super.getDropDownView(position, null, parent);
                            }
                            view = super.getDropDownView(position, null, parent);
                            return view;
                        }
                    }*/;
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spGroupPart.setAdapter(spinnerAdapter);
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < grupPartList.size(); i++) {
                            if (spGroupPart.getItemAtPosition(i).toString().equals(selection)) {
                                spGroupPart.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    showError(ERROR_INFO);
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
                args.put("aktivitas", spAktivitas.getSelectedItem().toString());
                args.put("idGroupPart", groupPartId);
                args.put("groupPart", spGroupPart.getSelectedItem().toString());
                args.put("komisiPercent", NumberFormatUtils.clearPercent(etKomisi.getText().toString()));
                args.put("status", spStatus.getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menyimpan Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(ERROR_INFO);
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
                args.put("groupPart", spGroupPart.getSelectedItem().toString());
                args.put("komisiPercent", etKomisi.getText().toString());
                args.put("status", spStatus.getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Memperbarui Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(ERROR_INFO);
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
                    find(R.id.tv_total_komisi, TextView.class).setText("TOTAL : " + komisiPercent + " %");
                } else {
                    showError(ERROR_INFO);
                }
            }
        });

    }
}
