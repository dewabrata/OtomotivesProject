package com.rkrzmail.oto.modules.discount;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AturFrekwensiDiscount_Acitivity extends AppActivity implements View.OnClickListener {

    private TextView tvTgl;
    private EditText etDisc;
    private Spinner spLayanan, spFrekwensi;
    private List<String> layananList = new ArrayList<>();
    private String layanan = "", frekwensi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_frekwensi_discount);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Frekwensi Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();

        tvTgl = findViewById(R.id.tv_tglEffect_freDisc);
        etDisc = findViewById(R.id.et_disc_freDisc);
        spFrekwensi = findViewById(R.id.sp_frekwensi);
        spLayanan = findViewById(R.id.sp_paketLayanan_freDisc);

        etDisc.addTextChangedListener(new PercentFormat(etDisc));
        tvTgl.setOnClickListener(this);
        try{
            loadData();
        }catch (Exception e){
            Log.d("Exception__", "initComponent: " + e.getMessage());
        }
    }

    private void initSpinner(){
        List<String> frekwensiList = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            frekwensiList.add(String.valueOf(i));
        }
        setSpinnerOffline(frekwensiList, spFrekwensi, frekwensi);
        setSpLayanan();
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
                layananList.add("--PILIH");
                if(result.get("status").asString().equalsIgnoreCase("OK")){
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

    private void loadData() {
        final Nson nson = Nson.readJson(getIntentStringExtra("data"));
        final Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            layanan = nson.get("PAKET_LAYANAN").asString();
            tvTgl.setText(nson.get("TANGGAL").asString());
            etDisc.setText(nson.get("DISCOUNT").asString());
            frekwensi = nson.get("FREKWENSI").asString();

            find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteData(nson);
                }
            });

            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(etDisc.getText().toString().isEmpty()){
                        etDisc.setError("Harus di isi");
                        return;
                    }
                    try {
                        Date tglSekarang = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
                        Date tglEffective = new SimpleDateFormat("dd/MM/yyyy").parse(tvTgl.getText().toString());
                        if (!tglSekarang.after(tglEffective) || tglSekarang.equals(tglEffective)) {
                            showWarning("Tanggal Invalid");
                            return;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (tvTgl.getText().toString().isEmpty()) {
                        showWarning("Tanggal Effective Harus Di isi");
                        return;
                    }
                    updateData(nson);
                }
            });

        }else{
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(etDisc.getText().toString().isEmpty()){
                        etDisc.setError("Harus di isi");
                        return;
                    }
                    try {
                        Date tglSekarang = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
                        Date tglEffective = new SimpleDateFormat("dd/MM/yyyy").parse(tvTgl.getText().toString());
                        if (!tglSekarang.after(tglEffective) || tglSekarang.equals(tglEffective)) {
                            showWarning("Tanggal Invalid");
                            return;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (tvTgl.getText().toString().isEmpty()) {
                        showWarning("Tanggal Effective Harus Di isi");
                        return;
                    }

                    saveData();
                }
            });
        }

        initSpinner();
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
                args.put("paket", spLayanan.getSelectedItem().toString());
                //args.put("frekuensi", etFrekwensi.getText().toString());
                args.put("diskon", etDisc.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturfrekuensidiskon"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal menyimpan Aktifitas");
                }
            }
        });
    }

    private void updateData(final Nson nson) {
        final String parseTgl = Tools.setFormatDayAndMonthToDb(tvTgl.getText().toString());
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", nson.get("ID").asString());
                args.put("tanggal", parseTgl);
                args.put("paket", spLayanan.getSelectedItem().toString());
                //args.put("frekuensi", etFrekwensi.getText().toString());
                args.put("diskon", etDisc.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturfrekuensidiskon"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Update Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal memperbaharui Aktifitas");
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

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturfrekuensidiskon"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menghapus Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal megnhapus Aktifitas");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_freDisc:
                getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }
}
