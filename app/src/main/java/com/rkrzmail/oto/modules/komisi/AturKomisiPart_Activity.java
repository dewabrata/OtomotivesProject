package com.rkrzmail.oto.modules.komisi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.List;
import java.util.Map;

public class AturKomisiPart_Activity extends AppActivity {

    private EditText etKomisiPart, etKomisiJasa;
    private NikitaAutoComplete etMasterPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_part);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etKomisiPart = findViewById(R.id.et_komisiPart_komisiPart);
        etKomisiJasa = findViewById(R.id.et_komisiJasa_komisiPart);
        etMasterPart = findViewById(R.id.et_masterPart_komisiPart);

        etKomisiJasa.addTextChangedListener(new PercentFormat(etKomisiJasa));
        etKomisiPart.addTextChangedListener(new PercentFormat(etKomisiPart));

        setMultiSelectionSpinnerFromApi(find(R.id.sp_namaPosisi_komisiPart, MultiSelectionSpinner.class), "nama", "POSISI", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "NAMA", "");
        remakeAutoCompleteMaster(etMasterPart, "PART", "KELOMPOK");
        loadData();
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("posisi", find(R.id.sp_namaPosisi_komisiPart, MultiSelectionSpinner.class).getSelectedItemsAsString());
                args.put("komisijual", etKomisiPart.getText().toString());
                args.put("nama", etMasterPart.getText().toString());
                args.put("komisijasa", etKomisiJasa.getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisipart"), args));
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

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            etKomisiJasa.setText(data.get("KOMISI_JASA").asString());
            etKomisiPart.setText(data.get("KOMISI_JUAL").asString());
            etMasterPart.setText(data.get("NAMA_PART").asString());
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
            find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
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
                args.put("id", nson.get("ID").asString());
                args.put("posisi", find(R.id.sp_namaPosisi_komisiPart, MultiSelectionSpinner.class).getSelectedItemsAsString());
                args.put("komisijual", etKomisiPart.getText().toString());
                args.put("nama", etMasterPart.getText().toString());
                args.put("komisijasa", etKomisiJasa.getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisipart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Memperbarui Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showSuccess("Gagal Memperbarui Aktivitas!");
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("komisipart"), args));
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
}
