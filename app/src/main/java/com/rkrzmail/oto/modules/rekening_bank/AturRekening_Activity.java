package com.rkrzmail.oto.modules.rekening_bank;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.discount.SpotDiscount_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class AturRekening_Activity extends AppActivity {

    private Spinner spBank;
    private EditText etNoRek, etNamaRek;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    final String dateTime = simpleDateFormat.format(Calendar.getInstance().getTime());

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

        setSpBank();

        loadData();
        find(R.id.btn_simpan_rekening, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
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
                args.put("norek", etNoRek.getText().toString());
                args.put("bank", spBank.getSelectedItem().toString());
                args.put("namarek", etNamaRek.getText().toString());
                args.put("edc", find(R.id.cb_edc_rekening, CheckBox.class).isChecked() ? "ACTIVE" : "TIDAK_ACTIVE");
                args.put("off_us", find(R.id.cb_offUs_rekening, CheckBox.class).isChecked() ? "ACTIVE" : "TIDAK_ACTIVE");
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
        final Nson n = Nson.readJson(getIntentStringExtra("data"));
        Intent i = getIntent();
        if (i.hasExtra("data")) {
            etNoRek.setText(n.get("NO_REKENING").asString());
            etNamaRek.setText(n.get("NAMA_REKENING").asString());
            spBank.setSelection(Tools.getIndexSpinner(spBank, n.get("BANK_NAME").asString()));
            Log.d("cobacoba", n.get("ID").asString());

            if (n.get("EDC_ACTIVE").asString().equalsIgnoreCase("ACTIVE")) {
                find(R.id.cb_edc_rekening, CheckBox.class).setChecked(true);
            } else {
                find(R.id.cb_edc_rekening, CheckBox.class).setChecked(false);
            }
            if (n.get("OFF_US").asString().equalsIgnoreCase("ACTIVE")) {
                find(R.id.cb_offUs_rekening, CheckBox.class).setChecked(true);
            } else {
                find(R.id.cb_offUs_rekening, CheckBox.class).setChecked(false);
            }

            find(R.id.btn_hapus_rekening).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_rekening).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(n);
                }
            });

            find(R.id.btn_simpan_rekening, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_rekening, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData();
                }
            });
        }

    }

    private void updateData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("norek", etNoRek.getText().toString());
                args.put("bank", spBank.getSelectedItem().toString());
                args.put("namarek", etNamaRek.getText().toString());
                args.put("edc", find(R.id.cb_edc_rekening, CheckBox.class).isChecked() ? "ACTIVE" : "TIDAK ACTIVE");
                args.put("off_us", find(R.id.cb_offUs_rekening, CheckBox.class).isChecked() ? "ACTIVE" : "TIDAK ACTIVE");
                //args.put("status", );
                args.put("tanggal", dateTime);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("setrekeningbank"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Memperbaharui Aktifitas");
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("setrekeningbank"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menghapus Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("GAGAL");
                }
            }
        });
    }

    public void setSpBank() {
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
                ArrayList<String> str = new ArrayList<>();
                str.add("Belum Di Pilih");
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get("BANK_CODE").asString() +
                            " " + result.get("data").get(i).get("BANK_NAME").asString());
                }
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (position == getCount()) {
                            ((TextView) v.findViewById(android.R.id.text1)).setText(null);
                            ((TextView) v.findViewById(android.R.id.text1)).setHint(""); //"Hint to be displayed"
                        }
                        return v;
                    }

                    @Override
                    public int getCount() {
                        return super.getCount();            // you don't display last item. It is used as hint.
                    }
                };
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spBank.setAdapter(spinnerAdapter);
                notifyDataSetChanged(spBank);
            }
        });
    }
}
