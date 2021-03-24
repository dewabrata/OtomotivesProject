package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.PENYESUAIAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class AturPenyesuain_StockOpname_Activity extends AppActivity {

    String kodeLokasi = "";
    boolean isStockLebih = false;
    boolean isStockKurang = false;
    private boolean isView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyesuain);
        initToolbar();
        if(getIntent().hasExtra("VIEW")){
            isView = true;
            loadData();
        }else{
            if(getIntent().hasExtra("STOCK LEBIH")){
                isStockLebih = true;
            }else if(getIntent().hasExtra("STOCK KURANG")){
                isStockKurang = true;
            }
            setSpLokasi("");
            setSpPenyesuaian("");
            initButton();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penyesuaian");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadData(){
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        Tools.setViewAndChildrenEnabled(find(R.id.ly_container, LinearLayout.class), false);

        setSpPenyesuaian(data.get("SEBAB").asString());
        setSpLokasi(data.get("LOKASI").asString());
        find(R.id.et_no_folder_lain, EditText.class).setText(data.get("FOLDER_LAIN").asString());
        find(R.id.et_ket_penyesuaian, EditText.class).setText(data.get("ALASAN").asString());
        find(R.id.et_user_saksi_penyesuaian, EditText.class).setText(data.get("USER_SAKSI").asString());
        find(R.id.btn_simpan).setVisibility(View.GONE);
        find(R.id.et_no_folder_lain, EditText.class).setTextColor(getResources().getColor(R.color.grey_900));
        find(R.id.et_ket_penyesuaian, EditText.class).setTextColor(getResources().getColor(R.color.grey_900));
        find(R.id.et_user_saksi_penyesuaian, EditText.class).setTextColor(getResources().getColor(R.color.grey_900));
    }

    private void setPenyesuaian() {
        Nson penyesuaian = Nson.newObject();

        penyesuaian.set("PENYESUAIAN", find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString());
        penyesuaian.set("FOLDER_LAIN", find(R.id.et_no_folder_lain, EditText.class).getText().toString());
        penyesuaian.set("KET", find(R.id.et_ket_penyesuaian, EditText.class).getText().toString());
        penyesuaian.set("USER_SAKSI", find(R.id.et_user_saksi_penyesuaian, EditText.class).getText().toString());

        Intent i = new Intent();
        i.putExtra(DATA, penyesuaian.toJson());
        i.putExtra("FINISH", true);
        setResult(RESULT_OK, i);
        finish();
    }

    private void setSpPenyesuaian(String selection) {
        List<String> penyesuaianList = new ArrayList<>();
        penyesuaianList.add("--PILIH--");
        if(isView){
            penyesuaianList.add("SALAH CATAT");
            penyesuaianList.add("HILANG");
            penyesuaianList.add("RUSAK");
            penyesuaianList.add("PINDAH LOKASI");
        }else{
            if(isStockLebih){
                penyesuaianList.add("SALAH CATAT"); //HANYA AKTIF BILA LEBIH BESAR DARI STOCK
            }else{
                penyesuaianList.add("HILANG");
                penyesuaianList.add("RUSAK");
                penyesuaianList.add("PINDAH LOKASI"); //HANYA AKTIF BILA LOKASI TERSEDIA GUDANG DAN DISPLAY
            }
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, penyesuaianList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_sebab_penyesuaian, Spinner.class).setAdapter(spinnerAdapter);
        if(!selection.isEmpty()){
            for (int i = 0; i < find(R.id.sp_sebab_penyesuaian, Spinner.class).getCount(); i++) {
                if(selection.equals(find(R.id.sp_sebab_penyesuaian, Spinner.class).getItemAtPosition(i).toString())){
                    find(R.id.sp_sebab_penyesuaian, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
    }

    private void setSpLokasi(String selection) {
        Nson penyesuaian = Nson.readJson(getIntentStringExtra(PENYESUAIAN));
        List<String> lokasiList = new ArrayList<>();
        final List<String> kodeList = new ArrayList<>();
        if(isView){
            lokasiList.add("--PILIH--");
            lokasiList.add("RUANG PART");
            lokasiList.add("DISPLAY");
            lokasiList.add("PALET");
        }else{
            if (penyesuaian.asArray().size() > 1) {
                penyesuaian.remove(0);
                lokasiList.add("--PILIH--");
                kodeList.add("");
            }
            for (int i = 0; i < penyesuaian.size(); i++) {
                lokasiList.add(penyesuaian.get(i).get("LOKASI").asString());
                kodeList.add(penyesuaian.get(i).get("KODE").asString());
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_lokasi_stockOpname, Spinner.class).setAdapter(spinnerAdapter);
        if (lokasiList.size() == 1) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasi_part, LinearLayout.class), false);
        }

        if(!selection.isEmpty()){
            for (int i = 0; i < find(R.id.sp_lokasi_stockOpname, Spinner.class).getCount(); i++) {
                if(find(R.id.sp_lokasi_stockOpname, Spinner.class).getItemAtPosition(i).toString().equals(selection)){
                    find(R.id.sp_lokasi_stockOpname, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
        find(R.id.sp_lokasi_stockOpname, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (kodeList.size() > 0) {
                    kodeLokasi = kodeList.get(position);
                    find(R.id.et_no_folder_lain, EditText.class).setText(kodeLokasi);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (parent.getSelectedItemPosition() == 1) {
                    //showInfo("OK");
                    find(R.id.et_no_folder_lain, EditText.class).setText(kodeLokasi);
                }
            }
        });
    }

    private void initButton() {
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                    find(R.id.sp_sebab_penyesuaian, Spinner.class).performClick();
                    showWarning("Sebab Penyesuaian Belum Di Pilih");
                    return;
                }
                if (find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString().equals("PINDAH LOKASI")) {
                    if (find(R.id.sp_lokasi_stockOpname, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                        find(R.id.sp_lokasi_stockOpname, Spinner.class).performClick();
                        showWarning("Lokasi Harus Di pilih");
                        return;
                    }
                }

                if(find(R.id.et_user_saksi_penyesuaian, EditText.class).getText().toString().isEmpty()){
                    find(R.id.et_user_saksi_penyesuaian, EditText.class).requestFocus();
                    showWarning("User Saksi Belum di Scan");
                    return;
                }

                if(find(R.id.et_ket_penyesuaian, EditText.class).getText().toString().isEmpty()){
                    find(R.id.et_ket_penyesuaian, EditText.class).setError("KETERANGAN HARUS DI ISI");
                    viewFocus(find(R.id.et_ket_penyesuaian, EditText.class));
                    return;
                }
                setPenyesuaian();
            }
        });

        find(R.id.img_scan_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String barcodeResult = data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "";
            MyCode.checkMyCode(this, barcodeResult, new MyCode.RunnableWD() {
                @Override
                public void runWD(Nson nson) {
                    if (nson.get("status").asString().equals("OK")) {
                        if(nson.get("data").asArray().isEmpty()){
                            showWarning("Scan Barcode Tidak Valid");
                            return;
                        }
                        nson = nson.get("data").get(0);
                        find(R.id.et_user_saksi_penyesuaian, EditText.class).setText(nson.get("NAMA").asString());
                    } else {
                        showError(ERROR_INFO);
                    }
                }
            });
        }
    }
}
