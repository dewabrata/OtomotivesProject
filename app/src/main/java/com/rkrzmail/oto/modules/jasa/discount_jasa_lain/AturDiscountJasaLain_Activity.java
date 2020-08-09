package com.rkrzmail.oto.modules.jasa.discount_jasa_lain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AturDiscountJasaLain_Activity extends AppActivity implements View.OnClickListener {

    private MultiSelectionSpinner spPekerjaan;
    private EditText etDiscPart;
    private TextView tvTgl;
    private Spinner spAktifitas, spKelompokPart;
    private String aktivitas = "", kelompokPart = "", pekerjaan = "";
    private List<String> partList = new ArrayList<>(), aktivitasList = new ArrayList<>(), dummy = new ArrayList<>();

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
        spKelompokPart = findViewById(R.id.sp_kelompokPart_discJasa);

        //setSpinnerFromApi(spKelompokPart, "nama", "PART", "viewmst", "KELOMPOK", "KELOMPOK_LAIN");
        /*setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "PEKERJAAN", "");*/

        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));
        tvTgl.setOnClickListener(this);
        try {
            setSpPekerjaan();
            setSpKelompokPart();
            loadData();
        } catch (Exception e) {
            Log.d("Exception___", "initComponent: " + e.getMessage());
        }

    }

    private void saveData() {
        final String parseTgl = Tools.setFormatDayAndMonthToDb(tvTgl.getText().toString());
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("tanggal", parseTgl);
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("kategori", spKelompokPart.getSelectedItem().toString());
                //spAktifitas.getSelectedItem().toString()
                //args.put("aktivitas", "OKAY");
                args.put("pesan", find(R.id.cb_mssg_discJasa, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("diskon", etDiscPart.getText().toString());

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

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        Log.d("Disc___", "loadData: " + data);
        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            kelompokPart = data.get("KATEGORI_JASA_LAIN").asString();
            aktivitas = data.get("AKTIVITAS").asString();
            dummy.add(data.get("PEKERJAAN").asString());

            etDiscPart.setText(data.get("DISC_JASA").asString());
            tvTgl.setText(data.get("TANGGAL").asString());
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
        } else {
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                }
            });
        }
    }

    private void updateData(final Nson id) {
        final String parseTgl = Tools.setFormatDayAndMonthToDb(tvTgl.getText().toString());
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
//              update parameter action : update, CID, tanggal, pekerjaan, kategori, aktivitas, pesan, diskon
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("tanggal", parseTgl);
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("kategori", spKelompokPart.getSelectedItem().toString());
                //args.put("aktifitas", spAktifitas.getSelectedItem().toString());
                args.put("pesan", find(R.id.cb_mssg_discJasa, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("diskon", etDiscPart.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Memperbarui Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Memperbarui Aktivitas!");
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
                    spKelompokPart.setAdapter(spinnerAdapter);
                    if (!kelompokPart.isEmpty()) {
                        for (int in = 0; in < spKelompokPart.getCount(); in++) {
                            if (spKelompokPart.getItemAtPosition(in).toString().contains(kelompokPart)) {
                                spKelompokPart.setSelection(in);
                            }
                        }
                    }
                }
            }
        });
    }

    private void setSpPekerjaan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "PEKERJAAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewmst"), args));
            }

            @Override
            public void runUI() {
                List<String> pekerjaanList = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    pekerjaanList.add(result.get("data").get(i).get("PEKERJAAN").asString());
                }
                try {
                    spPekerjaan.setItems(pekerjaanList);
                    spPekerjaan.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedIndices(List<Integer> indices) {

                        }

                        @Override
                        public void selectedStrings(List<String> strings) {

                        }
                    });
                    spPekerjaan.setSelection(dummy, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    showInfo("Perlu di Muat Ulang");
                }
            }
        });
    }

    private void setSpAktifitas() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discJasa:
                getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }
}
