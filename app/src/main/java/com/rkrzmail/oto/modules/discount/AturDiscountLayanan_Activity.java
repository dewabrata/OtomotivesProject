package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.DISCOUNT_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturDiscountLayanan_Activity extends AppActivity {

    private Spinner spPekerjaan;
    private String lokasi;
    private List<String> listChecked = new ArrayList<>();
    private ViewGroup layoutCheckBox;
    private Spinner spLayanan;
    private List<String> layananList = new ArrayList<>();
    private Nson dataLayananList = Nson.newArray();
    private String layanan = "", idLayanan = "";
    private boolean flagTenda = false, flagBengkel = false, flagMssg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discLayanan);
        spLayanan = findViewById(R.id.sp_paketLayanan_discLayanan);

        find(R.id.et_discPart_discLayanan, EditText.class).addTextChangedListener(new PercentFormat
                (find(R.id.et_discPart_discLayanan, EditText.class)));

        try {
            setSpLayanan();
            loadData();
        } catch (Exception e) {
            Log.d("Exception__", "initComponent: " + e.getMessage());
        }

        find(R.id.cb_tenda_discLayanan, CheckBox.class).setOnCheckedChangeListener(listener);
        find(R.id.cb_bengkel_discLayanan, CheckBox.class).setOnCheckedChangeListener(listener);
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        final Nson n = Nson.readJson(getIntentStringExtra("data"));
        Intent i = getIntent();
        if (n.get("LOKASI").asString().equalsIgnoreCase("TENDA") && n.get("LOKASI").asString().equalsIgnoreCase("BENGKEL")) {
            flagTenda = true;
            flagBengkel = true;
        } else if (n.get("LOKASI").asString().equalsIgnoreCase("TENDA")) {
            flagTenda = true;
        } else if (n.get("LOKASI").asString().equalsIgnoreCase("BENGKEL")) {
            flagBengkel = true;
        }
        if (n.get("MESSAGE_PELANGGAN").asString().equalsIgnoreCase("YA")) {
            flagMssg = true;
        }
        if (i.hasExtra(DATA)) {
            setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", n.get("PEKERJAAN").asString());
            layanan = n.get("NAMA_LAYANAN").asString();
            setSpStatus(n.get("STATUS").asString());
            find(R.id.et_discPart_discLayanan, EditText.class).setText(n.get("DISKON").asString());
            find(R.id.cb_bengkel_discLayanan, CheckBox.class).setChecked(flagBengkel);
            find(R.id.cb_tenda_discLayanan, CheckBox.class).setChecked(flagTenda);

            find(R.id.btn_hapus_discLayanan, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_discLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(n);
                }
            });
            find(R.id.btn_simpan_discLayanan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_discLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(n);
                }
            });
        } else {
            setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
            find(R.id.btn_simpan_discLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addData();
                }
            });
        }
    }

    private void addData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItem().toString());
                args.put("nama", find(R.id.sp_paketLayanan_discLayanan, Spinner.class).getSelectedItem().toString());
                args.put("diskon", find(R.id.et_discPart_discLayanan, EditText.class).getText().toString());
                args.put("layananId", idLayanan);

                for(String str : listChecked){
                    args.put("lokasi", str);
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_LAYANAN), args));
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
                args.put("nama", find(R.id.sp_paketLayanan_discLayanan, Spinner.class).getSelectedItem().toString());
                args.put("diskon", find(R.id.et_discPart_discLayanan, EditText.class).getText().toString());
                args.put("layananId", idLayanan);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }

    private void deleteData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "delete");
                args.put("id", id.get("ID").asString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_LAYANAN), args));
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

    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                listChecked.add(buttonView.getText().toString());
                Log.d("Disc___", "initComponent: " + listChecked);
            } else {
                listChecked.remove(buttonView.getText().toString());
            }
        }
    };

    private void setSpStatus(String status){
        List<String> statusList = new ArrayList<>();
        statusList.add("TIDAK AKTIF");
        statusList.add("AKTIF");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_status, Spinner.class).setAdapter(statusAdapter);
        if(!status.isEmpty()){
            for (int i = 0; i < find(R.id.sp_status, Spinner.class).getCount(); i++) {
                if(find(R.id.sp_status, Spinner.class).getItemAtPosition(i).equals(status)){
                    find(R.id.sp_status, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
    }

    private void setSpLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("status", "AKTIF");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                layananList.add("--PILIH--");
                dataLayananList.add("");
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        dataLayananList.add(Nson.newObject()
                                .set("ID", result.get(i).get("LAYANAN_ID").asString())
                                .set("NAMA_LAYANAN", result.get(i).get("NAMA_LAYANAN").asString()));
                        layananList.add(result.get(i).get("NAMA_LAYANAN").asString());
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
                }else{
                    Messagebox.showDialog(getActivity(), "Konfirmasi", "Layanan Gagal di Muat, Muat Ulang?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpLayanan();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    showError(ERROR_INFO);
                }
            }
        });

        spLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().toString().equals(dataLayananList.get(i).get("NAMA_LAYANAN").asString())){
                    idLayanan = dataLayananList.get(i).get("ID").asString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
