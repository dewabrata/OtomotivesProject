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

import java.util.ArrayList;
import java.util.Map;

public class AturKomisiJasaLain_Activity extends AppActivity {

    private MultiSelectionSpinner spPosisi;
    private Spinner spKategori, spAktifitas;
    private ArrayList<String> dummies = new ArrayList<>();
    private EditText etKomisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_jasa_lain_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_komisiJasaLain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Komisi Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPosisi = findViewById(R.id.sp_namaPosisi_komisiJasaLain);
        spAktifitas = findViewById(R.id.sp_aktifitas_discJasa);
        spKategori = findViewById(R.id.sp_kategori_discJasa);
        etKomisi = findViewById(R.id.et_komisi_komisiJasaLain);

        setSpinnerFromApi(spKategori, "nama", "PART", "viewmst", "KATEGORI");
        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA");

        find(R.id.btn_simpan_komisiJasaLain, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    startActivity(new Intent(getActivity(), KomisiJasaLain_Activity.class));
                } else {
                    showInfo("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("ID"));
        Intent intent = getIntent();
    }

    private void updateData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Memperbarui Aktivitas");
                    startActivity(new Intent(getActivity(), KomisiJasaLain_Activity.class));
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    startActivity(new Intent(getActivity(), KomisiJasaLain_Activity.class));
                } else {
                    showInfo("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }

}
