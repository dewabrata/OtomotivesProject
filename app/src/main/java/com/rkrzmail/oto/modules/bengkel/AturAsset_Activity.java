package com.rkrzmail.oto.modules.bengkel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rkrzmail.oto.modules.mekanik.PerintahKerjaMekanik_Activity;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ASSET;
import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturAsset_Activity extends AppActivity {

    private Spinner spStatus, spTipePembayaran, spNoRek;

    private Nson data;
    private Nson rekeningList = Nson.newArray();
    private String namaBank = "", noRek = "";
    private String status = "", tipePembayaran = "";
    private boolean isPembeli = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_asset);
        initToolbar();
        initComponent();
        initData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Update Status Aset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        spStatus = findViewById(R.id.sp_status);
        spTipePembayaran = findViewById(R.id.sp_tipe_pembayaran);
        spNoRek = findViewById(R.id.sp_norek);

        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                status = adapterView.getItemAtPosition(i).toString();
                if(status.equals("TERJUAL")){
                    find(R.id.et_harga_jual).setEnabled(true);
                    find(R.id.sp_tipe_pembayaran).setEnabled(true);
                    isPembeli = true;
                }else{
                    isPembeli = false;
                    find(R.id.sp_tipe_pembayaran).setEnabled(false);
                    find(R.id.et_harga_jual).setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tipePembayaran = adapterView.getItemAtPosition(i).toString();
                if (tipePembayaran.equals("CASH")) {
                    spNoRek.setSelection(0);
                    spNoRek.setEnabled(false);
                } else if (tipePembayaran.equals("TRANSFER")) {
                    spNoRek.setEnabled(true);
                    setSpRek();
                } else {
                    spNoRek.setSelection(0);
                    spNoRek.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void initData(){
        data = Nson.readJson(getIntentStringExtra(DATA));

        find(R.id.et_nama_asset, EditText.class).setText(data.get("NAMA_ASET").asString());
        find(R.id.et_no_asset, EditText.class).setText(data.get("NILAI_PENYUSUTAN").asString());
        find(R.id.et_nilai_sisa, EditText.class).setText(data.get("NILAI_SISA").asString());

        setSpinnerOffline(Arrays.asList("--PILIH--", "TRANSFER", "CASH"), spTipePembayaran, data.get("TIPE_PEMBAYARAN").asString());
        setSpinnerOffline(Arrays.asList("--PILIH--", "TERJUAL", "RUSAK TOTAL", "HILANG"), spStatus, data.get("STATUS").asString());
    }

    public void setSpRek() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> str = new ArrayList<>();
                result = result.get("data");
                str.add("--PILIH--");
                rekeningList.add("");
                for (int i = 0; i < result.size(); i++) {
                    rekeningList.add(Nson.newObject()
                            .set("ID", result.get(i).get("ID"))
                            .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                            .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                            .set("EDC", result.get(i).get("EDC_ACTIVE"))
                            .set("OFF_US", result.get(i).get("OFF_US"))
                            .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                    str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                }

                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spNoRek.setAdapter(adapter);
            }
        });

        spNoRek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(rekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = rekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = rekeningList.get(i).get("BANK_NAME").asString();
                } else {
                    noRek = "";
                    namaBank = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveData(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("status", status);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ASSET), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("AKTIVITAS BERHASIL DI PERBAHARUI");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo(ERROR_INFO);
                }
            }
        });
    }


}
