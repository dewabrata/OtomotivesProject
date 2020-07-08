package com.rkrzmail.oto.modules.komisi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AturKomisiLayanan_Activity extends AppActivity {

    private MultiSelectionSpinner spPosisi;
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_komisiLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Komisi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPosisi = findViewById(R.id.sp_namaPosisi_komisiLayanan);
        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA");

        loadData();

        find(R.id.sp_namaLayanan_komisiLayanan, Spinner.class);
        find(R.id.et_komisi_komisiLayanan, EditText.class).addTextChangedListener(new PercentFormat(find(R.id.et_komisi_komisiLayanan, EditText.class)));
        find(R.id.btn_simpan_komisiLayanan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void loadData() {
        Intent i = getIntent();
        final Nson nson = Nson.readJson(getIntentStringExtra("data"));
        if (i.hasExtra("data")) {
            find(R.id.sp_namaLayanan_komisiLayanan, Spinner.class).setSelection
                    (Tools.getIndexSpinner(find(R.id.sp_namaLayanan_komisiLayanan, Spinner.class), nson.get("NAMA_LAYANAN").asString()));
            find(R.id.et_komisi_komisiLayanan, EditText.class).setText(nson.get("KOMISI").asString());
            // nson.get("POSISI").asStringArray()
            //
            String posisi = nson.get("POSISI").asString();
            String[] pos = posisi.split(",");
            //spPosisi.setSelection(pos);
            System.out.println(pos);

            find(R.id.btn_hapus_komisiLayanan, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_komisiLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(nson);
                }
            });
            find(R.id.btn_simpan_komisiLayanan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_komisiLayanan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(nson);
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
                //args.put("nama",  find(R.id.sp_namaLayanan_komisiLayanan, Spinner.class).getSelectedItem().toString());
                args.put("komisi", find(R.id.et_komisi_komisiLayanan, EditText.class).getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisilayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal Menyimpan Aktifitas");
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
                args.put("nama", find(R.id.sp_namaLayanan_komisiLayanan, Spinner.class).getSelectedItem().toString());
                args.put("komisi", find(R.id.et_komisi_komisiLayanan, EditText.class).getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisilayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Memperbarui Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal memuat Aktifitas");
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
                    showInfo("Sukses Menghapus Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal memuat Aktifitas");
                }
            }
        });
    }
}
