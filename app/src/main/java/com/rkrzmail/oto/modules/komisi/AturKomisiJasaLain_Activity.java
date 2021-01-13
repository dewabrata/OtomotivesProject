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

public class AturKomisiJasaLain_Activity extends AppActivity {

    private MultiSelectionSpinner spPosisi;
    private Spinner spKategori, spAktifitas;
    private EditText etKomisi;
    private String aktivitas = "", kelompokPart = "", pekerjaan = "";
    private List<String> partList = new ArrayList<>(), aktivitasList = new ArrayList<>(), dummy = new ArrayList<>();


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
        spPosisi = findViewById(R.id.sp_namaPosisi_komisiJasaLain);
        spAktifitas = findViewById(R.id.sp_aktifitas_komisiJasaLain);
        spKategori = findViewById(R.id.sp_kategori_komisiJasaLain);
        etKomisi = findViewById(R.id.et_komisi_komisiJasaLain);

        etKomisi.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etKomisi));
        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "NAMA", "");

        try{
            setSpKelompokPart();
            loadData();
        }catch (Exception e){
            Log.d("Exception___", "initComponent: " + e.getMessage());
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
                args.put("komisi", etKomisi.getText().toString());
                //args.put("aktivitas", spAktifitas.getSelectedItem().toString());
                args.put("kategori", spKategori.getSelectedItem().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisijasalain"), args));
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

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            kelompokPart = data.get("KATEGORI_PART").asString();
            etKomisi.setText(data.get("KOMISI").asString());
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
                    updateData(data);
                }
            });
        }else{
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                }
            });
        }
    }

    private void updateData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("id", nson.get("id").asString());
                args.put("posisi", spPosisi.getSelectedItemsAsString());
                args.put("komisi", etKomisi.getText().toString());
                //args.put("aktivitas", spAktifitas.getSelectedItem().toString());
                args.put("kategori", spKategori.getSelectedItem().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisijasalain"), args));
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

    private void deleteData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("id").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisijasalain"), args));
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

    private void setSpKelompokPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "PART");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewmst"), args));
            }

            @Override
            public void runUI() {
                partList.add("Belum Di Pilih");
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        partList.add(result.get("data").get(i).get("KELOMPOK").asString() + " - " + result.get("data").get(i).get("KELOMPOK_LAIN").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, partList);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spKategori.setAdapter(spinnerAdapter);
                    if (!kelompokPart.isEmpty()) {
                        for (int in = 0; in < spKategori.getCount(); in++) {
                            if (spKategori.getItemAtPosition(in).toString().contains(kelompokPart)) {
                                spKategori.setSelection(in);
                            }
                        }
                    }
                }
            }
        });
    }
}
