package com.rkrzmail.oto.modules.rekening_bank;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.TimePicker_Dialog;
import com.rkrzmail.oto.modules.discount.SpotDiscount_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AturRekening_Activity extends AppActivity {

    private Spinner spBank;
    private EditText etNoRek, etNamaRek;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    final String dateTime = simpleDateFormat.format(Calendar.getInstance().getTime());
    private String namaBank = "";
    private List<String> dataBank = new ArrayList<>();
    private List<Boolean> isCheckedList = new ArrayList<>();
    private Nson n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_rekening_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rekening Bank");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNoRek = findViewById(R.id.et_noRek_rekening);
        spBank = findViewById(R.id.sp_bank_rekening);
        etNamaRek = findViewById(R.id.et_namaRek_rekening);
        try {
            setSpBank();
            loadData();
        } catch (Exception e) {
            Log.e("Exception__", "initComponent: " + e.getMessage());
        }
        find(R.id.cb_edc_rekening, CheckBox.class).setOnCheckedChangeListener(listener);
        find(R.id.cb_offUs_rekening, CheckBox.class).setOnCheckedChangeListener(listener);
    }

    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                isCheckedList.add(buttonView.isChecked());
                Log.d("isChecked__", "add : " + isCheckedList);
            } else {
                isCheckedList.remove(buttonView.isChecked());
                Log.d("isChecked__", "remove : " + isCheckedList);

            }
        }
    };


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("norek", etNoRek.getText().toString());
                args.put("bank", spBank.getSelectedItem().toString());
                args.put("namarek", etNamaRek.getText().toString());
                args.put("edc", find(R.id.cb_edc_rekening, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("off_us", find(R.id.cb_offUs_rekening, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("status", "");
                args.put("tanggal", dateTime);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("setrekeningbank"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menambahkan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("GAGAL");
                }
            }
        });
    }

    private void loadData() {
        n = Nson.readJson(getIntentStringExtra("data"));
        Log.d("Nson____", "loadData: " + n);
        Intent i = getIntent();

        if (i.hasExtra("data")) {
            namaBank = n.get("BANK_NAME").asString();
            etNoRek.setText(n.get("NO_REKENING").asString());
            etNamaRek.setText(n.get("NAMA_REKENING").asString());
            if (n.get("EDC_ACTIVE").asString().equalsIgnoreCase("Y")) {
                find(R.id.cb_edc_rekening, CheckBox.class).setChecked(true);
            } else {
                find(R.id.cb_edc_rekening, CheckBox.class).setChecked(false);
            }
            if (n.get("OFF_US").asString().equalsIgnoreCase("Y")) {
                find(R.id.cb_offUs_rekening, CheckBox.class).setChecked(true);
            } else {
                find(R.id.cb_offUs_rekening, CheckBox.class).setChecked(false);
            }

            find(R.id.btn_hapus).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(n);
                }
            });

            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (spBank.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spBank.performClick();
                        showWarning("Silahkan Pilih Nama Bank");
                    } else if (etNoRek.getText().toString().isEmpty()) {
                        etNoRek.setError("Harus Di isi");
                    } else if (etNamaRek.getText().toString().isEmpty()) {
                        etNamaRek.setError("Harus Di isi");
                    } else {
                        updateData(n);
                    }
                }
            });
        } else {
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (spBank.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spBank.performClick();
                        showWarning("Silahkan Pilih Nama Bank");
                    } else if (etNoRek.getText().toString().isEmpty()) {
                        etNoRek.setError("Harus Di isi");
                    } else if (etNamaRek.getText().toString().isEmpty()) {
                        etNamaRek.setError("Harus Di isi");
                    } else {
                        saveData();
                    }

                }
            });
        }
    }

    private void updateData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("norek", etNoRek.getText().toString());
                args.put("bank", spBank.getSelectedItem().toString());
                args.put("namarek", etNamaRek.getText().toString());
                args.put("edc", find(R.id.cb_edc_rekening, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("off_us", find(R.id.cb_offUs_rekening, CheckBox.class).isChecked() ? "Y" : "N");
                //args.put("status", );
                args.put("tanggal", dateTime);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("setrekeningbank"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Memperbaharui Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal menambahkan Aktifitas");
                }
            }
        });
    }

    private void deleteData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("ID").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("setrekeningbank"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menghapus Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal menghapus Aktifitas");
                }
            }
        });
    }

    private void setSpBank() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BANK");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewmst"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    dataBank.add("--PILIH--");
                    for (int i = 0; i < result.get("data").size(); i++) {
                        dataBank.add(result.get("data").get(i).get("BANK_CODE").asString() +
                                " " + result.get("data").get(i).get("BANK_NAME").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataBank);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spBank.setAdapter(spinnerAdapter);
                    if (!namaBank.isEmpty()) {
                        for (int in = 0; in < spBank.getCount(); in++) {
                            if (spBank.getItemAtPosition(in).toString().contains(namaBank)) {
                                spBank.setSelection(in);
                            }
                        }
                    }
                } else {
                    showInfoDialog("Daftar Bank Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpBank();
                        }
                    });
                }

            }
        });
    }
}
