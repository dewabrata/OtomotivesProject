package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturDiscountJasaLain_Activity extends AppActivity {

    private EditText etDiscPart;
    private Spinner spKelompokPart, spPekerjaan;

    private Nson kelompokPartList = Nson.newArray(), availList = Nson.newArray();
    private String idJasa = "";
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_jasa_lain_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_discJasa);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Discount Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDiscPart = findViewById(R.id.et_discPart_discJasa);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discJasa);
        spKelompokPart = findViewById(R.id.sp_kelompokPart_discJasa);
        etDiscPart.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDiscPart));

        loadData();
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItem().toString());
                args.put("kategori", spKelompokPart.getSelectedItem().toString());
                args.put("diskon", etDiscPart.getText().toString().replace("%", "").trim());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
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

    @SuppressLint("SetTextI18n")
    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        Log.d("Disc___", "loadData: " + data);
        Intent intent = getIntent();
        if (intent.hasExtra(DATA)) {
            setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", data.get("PEKERJAAN").asString());
            setSpStatus(data.get("STATUS").asString());
            setSpKelompokPart(data.get("KATEGORI_JASA_LAIN").asString());

            isUpdate = true;
            etDiscPart.setText(data.get("DISCOUNT_JASA").asString());
            find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(data);
                }
            });
            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(spKelompokPart.getSelectedItem().toString().equals("--PILIH--")){
                        showWarning("Kelompok Part Belum di Pilig");
                    }else{
                        updateData(data);
                    }
                }
            });
        } else {
            viewAvail();
            setSpStatus("");
            setSpKelompokPart("");
            setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", "");

            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(spKelompokPart.getSelectedItem().toString().equals("--PILIH--")){
                        showWarning("Kelompok Part Belum di Pilig");
                    }else{
                        saveData();
                    }
                }
            });
        }
    }

    private void setSpKelompokPart(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("flag", "KELOMPOK PART");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JASA_LAIN), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    data = data.get("data");
                    kelompokPartList.add("--PILIH--");
                    for (int i = 0; i < data.size(); i++) {
                        kelompokPartList.add(data.get(i).get("KELOMPOK_PART").asString());
                    }
                    Log.d("JASA__", "MEKANIK : " + kelompokPartList);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, kelompokPartList.asArray()){
                        @Override
                        public boolean isEnabled(int position) {
                            if(!isUpdate){
                                for (int i = 0; i < availList.size(); i++) {
                                    if (availList.get(i).asString().equals(kelompokPartList.get(position).asString())) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }

                        @SuppressLint("WrongConstant")
                        @Override
                        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View mView = null;
                            if(!isUpdate){
                                for (int i = 0; i < availList.size(); i++) {
                                    if (availList.get(i).asString().equals(kelompokPartList.get(position).asString())) {
                                        TextView mTextView = new TextView(getContext());
                                        mTextView.setVisibility(View.GONE);
                                        mTextView.setHeight(0);
                                        mView = mTextView;
                                        break;
                                    }else{
                                        mView = super.getDropDownView(position, null, parent);
                                    }
                                }
                            }
                            return mView;
                        }
                    };

                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spKelompokPart.setAdapter(spinnerAdapter);
                    if (!selection.isEmpty()) {
                        for (int in = 0; in < spKelompokPart.getCount(); in++) {
                            if (spKelompokPart.getItemAtPosition(in).toString().equals(selection)) {
                                spKelompokPart.setSelection(in);
                                break;
                            }
                        }
                    }
                } else {
                    showInfoDialog("Jasa Lain Gagal di Muat", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpKelompokPart("");
                        }
                    });
                }
            }
        });

        spKelompokPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (availList.get(position).get("KELOMPOK_PART").asString().equals(parent.getSelectedItem().toString())) {
                    idJasa = availList.get(position).get("ID").asString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void viewAvail() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        availList.add(result.get(i).get("KATEGORI_JASA_LAIN"));
                    }
                } else {
                    showError("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }


    private void updateData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItem().toString());
                args.put("kategori", spKelompokPart.getSelectedItem().toString());
                args.put("diskon", etDiscPart.getText().toString().replace("%", ""));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
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

    private void deleteData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
//                delete parameter action : delete, id
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("id").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menyimpan Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }

    private void setSpStatus(String status) {
        List<String> statusList = new ArrayList<>();
        statusList.add("TIDAK AKTIF");
        statusList.add("AKTIF");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_status, Spinner.class).setAdapter(statusAdapter);
        if (!status.isEmpty()) {
            for (int i = 0; i < find(R.id.sp_status, Spinner.class).getCount(); i++) {
                if (find(R.id.sp_status, Spinner.class).getItemAtPosition(i).equals(status)) {
                    find(R.id.sp_status, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
    }
}