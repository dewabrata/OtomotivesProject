package com.rkrzmail.oto.modules.komisi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AturKomisiLayanan_Activity extends AppActivity {

    private MultiSelectionSpinner spPosisi;
    private Spinner spLayanan;
    private List<String> layananList = new ArrayList<>();
    private String layanan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPosisi = findViewById(R.id.sp_namaPosisi_komisiLayanan);
        spLayanan = findViewById(R.id.sp_namaLayanan_komisiLayanan);

        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "NAMA", "");

        try{
            setSpLayanan();
            loadData();
        }catch (Exception e){
            Log.d("Exception__", "initComponent: " + e.getMessage());
        }
        find(R.id.et_komisi_komisiLayanan, EditText.class).addTextChangedListener(new NumberFormatUtils().percentTextWatcher(find(R.id.et_komisi_komisiLayanan, EditText.class)));
    }

    private void loadData() {
        Intent i = getIntent();
        final Nson nson = Nson.readJson(getIntentStringExtra("data"));
        if (i.hasExtra("data")) {
            layanan =  nson.get("NAMA_LAYANAN").asString();
            find(R.id.et_komisi_komisiLayanan, EditText.class).setText(nson.get("KOMISI").asString());

            find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(nson);
                }
            });
            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(nson);
                }
            });
        }else{
            find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                }
            });
        }
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("posisi", spPosisi.getSelectedItemsAsString());
                args.put("nama", spLayanan.getSelectedItem().toString());
                args.put("komisi", find(R.id.et_komisi_komisiLayanan, EditText.class).getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisilayanan"), args));
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
                args.put("id", nson.get("ID").asString());
                args.put("posisi", spPosisi.getSelectedItemsAsString());
                args.put("nama", spLayanan.getSelectedItem().toString());
                args.put("komisi", find(R.id.et_komisi_komisiLayanan, EditText.class).getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisilayanan"), args));
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

    private void deleteData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("ID").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisilayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menghapus Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal memuat Aktifitas");
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                layananList.add("Belum Di Pilih");
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        layananList.add(result.get("data").get(i).get("NAMA_LAYANAN").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, layananList);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLayanan.setAdapter(spinnerAdapter);
                    if (!layanan.isEmpty()) {
                        for (int in = 0; in < spLayanan.getCount(); in++) {
                            if (spLayanan.getItemAtPosition(in).toString().contains(layanan)) {
                                spLayanan.setSelection(in);
                            }
                        }
                    }
                }
            }
        });
    }
}
