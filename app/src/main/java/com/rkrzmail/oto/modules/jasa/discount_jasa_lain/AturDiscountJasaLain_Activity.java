package com.rkrzmail.oto.modules.jasa.discount_jasa_lain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class AturDiscountJasaLain_Activity extends AppActivity implements View.OnClickListener {

    private MultiSelectionSpinner spPekerjaan;
    private EditText etDiscPart;
    private TextView tvTgl;
    private Spinner spAktifitas, spKategori;

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
        tvTgl = findViewById(R.id.tv_tglEffect_discJasa);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discJasa);
        spAktifitas = findViewById(R.id.sp_aktifitas_discJasa);
        spKategori = findViewById(R.id.sp_kategori_discJasa);

        setSpinnerFromApi(spKategori, "nama", "PART", "viewmst", "KATEGORI");
        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");

        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));
        tvTgl.setOnClickListener(this);
        loadData();

        find(R.id.btn_simpan_discJasa, Button.class).setOnClickListener(new View.OnClickListener() {
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
//              add : parameter action : add, CID, tanggal, pekerjaan, kategori, aktivitas, pesan, diskon
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("tanggal", tvTgl.getText().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("kategori", spKategori.getSelectedItem().toString());
                //spAktifitas.getSelectedItem().toString()
                args.put("aktivitas", "OKAY");
                args.put("pesan", find(R.id.cb_mssg_discJasa, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("diskon", etDiscPart.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    startActivity(new Intent(getActivity(), DiscountJasaLain_Activity.class));
                    finish();
                } else {
                    showInfo("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("ID"));
        Intent intent = getIntent();
        if (intent.hasExtra("")) {
            etDiscPart.setText(data.get("").asString());
            spPekerjaan.setSelection(data.get("").asStringArray());
            spKategori.setSelection(Tools.getIndexSpinner(spKategori, data.get("").asString()));
            spAktifitas.setSelection(Tools.getIndexSpinner(spAktifitas, data.get("").asString()));
            tvTgl.setText(data.get("").asString());

            find(R.id.btn_hapus_discJasa, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_discJasa, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(Nson.readJson(getIntentStringExtra("ID")));
                }
            });
            find(R.id.btn_simpan_discJasa, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_discJasa, Button.class).setOnClickListener(new View.OnClickListener() {
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
//              update parameter action : update, CID, tanggal, pekerjaan, kategori, aktivitas, pesan, diskon
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("tanggal", tvTgl.getText().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("kategori", spKategori.getSelectedItem().toString());
                args.put("aktifitas", spAktifitas.getSelectedItem().toString());
                args.put("pesan", find(R.id.cb_mssg_discJasa, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("diskon", etDiscPart.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdisconjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Memperbarui Aktivitas");
                    startActivity(new Intent(getActivity(), DiscountJasaLain_Activity.class));
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
//                delete parameter action : delete, id
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("id").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdisconjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    startActivity(new Intent(getActivity(), DiscountJasaLain_Activity.class));
                } else {
                    showInfo("Gagal Menyimpan Aktivitas!");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discJasa:
                Tools.getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }
}
