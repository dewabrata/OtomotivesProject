package com.rkrzmail.oto.modules.bengkel;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class AturRekening_Activity extends AppActivity {

    private Spinner spBank;
    private EditText etNoRek, etNamaRek;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    final String dateTime = simpleDateFormat.format(Calendar.getInstance().getTime());

    private List<String> dataBank = new ArrayList<>();
    private List<Boolean> isCheckedList = new ArrayList<>();
    private Nson data, rekeningList = Nson.newArray();
    private String bankCode = "", namaBank = "";
    boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_rekening);
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

        setSpBank();
        loadData();
        viewEdcAndOffUs();

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

    private void loadData() {
        data = Nson.readJson(getIntentStringExtra(DATA));
        isUpdate = !data.asString().isEmpty();
        if (!data.asString().isEmpty()) {
            namaBank = data.get("BANK_NAME").asString();
            etNoRek.setText(data.get("NO_REKENING").asString());
            etNamaRek.setText(data.get("NAMA_REKENING").asString());

            find(R.id.cb_edc_rekening, CheckBox.class).setChecked(data.get("EDC_ACTIVE").asString().equalsIgnoreCase("Y"));
            find(R.id.cb_offUs_rekening, CheckBox.class).setChecked(data.get("OFF_US").asString().equalsIgnoreCase("Y"));
        }

        if (isUpdate) {
            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_hapus).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(data);
                }
            });
        }

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spBank.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    setErrorSpinner(spBank, "NAMA BANK HARUS DI PILIH");
                } else if (etNoRek.getText().toString().isEmpty()) {
                    etNoRek.setError("Harus Di isi");
                    viewFocus(etNoRek);
                } else if (etNamaRek.getText().toString().isEmpty()) {
                    etNamaRek.setError("Harus Di isi");
                    viewFocus(etNamaRek);
                } else {
                    saveData();
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

                args.put("action", isUpdate ? "update" : "add");
                args.put("norek", etNoRek.getText().toString());
                args.put("bankCode", bankCode);
                args.put("namarek", etNamaRek.getText().toString());
                args.put("edc", find(R.id.cb_edc_rekening, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("off_us", find(R.id.cb_offUs_rekening, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("tanggal", dateTime);
                args.put("id", data.get("ID").asString());
                args.put("bank", spBank.getSelectedItem().toString());
                args.put("publish", find(R.id.cb_publish, CheckBox.class).isChecked() ? "Y" : "N");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
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

    private void deleteData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("ID").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
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

    private void viewEdcAndOffUs() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("edc", "VIEW");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.size() > 0) {
                        find(R.id.cb_offUs_rekening, CheckBox.class).setEnabled(false);
                    }
                }
            }
        });
    }

    private void setSpBank() {
        final Nson bankData = Nson.newArray();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BANK");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    dataBank.add("--PILIH--");
                    bankData.add("");
                    for (int i = 0; i < result.size(); i++) {
                        bankData.add(Nson.newObject()
                                .set("BANK_CODE", result.get(i).get("BANK_CODE").asString())
                                .set("BANK_NAME", result.get(i).get("BANK_NAME").asString())
                                .set("COMPARISON", result.get(i).get("BANK_CODE").asString() +
                                        " " + result.get(i).get("BANK_NAME").asString())
                        );
                        dataBank.add(result.get(i).get("BANK_CODE").asString() +
                                " " + result.get(i).get("BANK_NAME").asString());
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

        spBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.equals(bankData.get(position).get("COMPARISON").asString())) {
                    bankCode = bankData.get(position).get("BANK_CODE").asString();
                    namaBank = bankData.get(position).get("BANK_NAME").asString();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
