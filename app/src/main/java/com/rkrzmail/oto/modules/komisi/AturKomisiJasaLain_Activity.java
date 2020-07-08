package com.rkrzmail.oto.modules.komisi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;

public class AturKomisiJasaLain_Activity extends AppActivity {

    private MultiSelectionSpinner spPosisi;
    private Spinner spKategori, spAktifitas;
    private EditText etKomisi;

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

        etKomisi.addTextChangedListener(new PercentFormat(etKomisi));
        setSpinnerFromApi(spKategori, "nama", "PART", "viewmst", "KATEGORI");
        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA");

        find(R.id.btn_simpan_komisiJasaLain, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        loadData();
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

            etKomisi.setText(data.get("KOMISI").asString());
            //spAktifitas.setSelection(Tools.getIndexSpinner(spAktifitas, data.get("AKTIVITAS").asString()));
            //spPosisi.setSelection(data.get("AKTIVITAS").asStringArray());
            spKategori.setSelection(Tools.getIndexSpinner(spKategori, data.get("KATEGORI_PART").asString()));
            find(R.id.btn_hapus_komisiJasaLain, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_komisiJasaLain, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(data);
                }
            });

            find(R.id.btn_simpan_komisiJasaLain, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_komisiJasaLain, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(data);
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

}
