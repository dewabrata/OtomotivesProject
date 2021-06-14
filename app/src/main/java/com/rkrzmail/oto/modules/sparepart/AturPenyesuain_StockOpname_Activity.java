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
import com.naa.utils.MessageMsg;
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
import static com.rkrzmail.utils.APIUrls.SET_STOCK_OPNAME;
import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.PENYESUAIAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class AturPenyesuain_StockOpname_Activity extends AppActivity {

    String kodeLokasi = "";
    boolean isStockLebih = false;
    boolean isStockKurang = false;
    private boolean isView = false;
    private int jumlahLokasi = 0;

    private int newLokasiID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyesuain);
        initToolbar();
        if (getIntent().hasExtra("VIEW")) {
            isView = true;
            loadData();
        } else {
            if (getIntent().hasExtra("STOCK LEBIH")) {
                isStockLebih = true;
            } else if (getIntent().hasExtra("STOCK KURANG")) {
                isStockKurang = true;
            }
            //viewAllLokasiPart();
            setSpPenyesuaian("");
            setSpLokasi("");
            initButton();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penyesuaian Stock Opname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        setDisableIsView();
        setSpPenyesuaian(data.get("SEBAB").asString());
        setSpLokasi(data.get("LOKASI").asString());
        find(R.id.et_no_folder_lain, EditText.class).setText(data.get("FOLDER_LAIN").asString());
        find(R.id.et_ket_penyesuaian, EditText.class).setText(data.get("ALASAN").asString());
        find(R.id.et_user_saksi_penyesuaian, EditText.class).setText(data.get("USER_SAKSI").asString());
        find(R.id.btn_simpan).setVisibility(View.GONE);
        find(R.id.img_scan_barcode).setVisibility(View.GONE);
    }

    private void setDisableIsView() {
        Tools.setViewAndChildrenEnabled(find(R.id.rl_sebab_penyesuaian, LinearLayout.class), false);
        Tools.setViewAndChildrenEnabled(find(R.id.rl_lokasi_part, LinearLayout.class), false);
        find(R.id.et_no_folder_lain, EditText.class).setEnabled(false);
        find(R.id.et_ket_penyesuaian, EditText.class).setEnabled(false);
        find(R.id.et_user_saksi_penyesuaian, EditText.class).setEnabled(false);
    }

    private void setPenyesuaian() {
        Nson penyesuaian = Nson.newObject();

        penyesuaian.set("PENYESUAIAN", find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString());
        penyesuaian.set("FOLDER_LAIN", find(R.id.et_no_folder_lain, EditText.class).getText().toString());
        penyesuaian.set("KET", find(R.id.et_ket_penyesuaian, EditText.class).getText().toString());
        penyesuaian.set("USER_SAKSI", find(R.id.et_user_saksi_penyesuaian, EditText.class).getText().toString());
        penyesuaian.set("NEW_LOKASI_ID", newLokasiID);


        Intent i = new Intent();
        i.putExtra(DATA, penyesuaian.toJson());
        i.putExtra("FINISH", true);
        setResult(RESULT_OK, i);
        finish();
    }

    private void setSpPenyesuaian(String selection) {
        List<String> penyesuaianList = new ArrayList<>();
        Nson lokasiArray = Nson.readJson(getIntentStringExtra(PENYESUAIAN));
        penyesuaianList.add("--PILIH--");
        if (isView) {
            penyesuaianList.add("SALAH CATAT");
            penyesuaianList.add("HILANG");
            penyesuaianList.add("RUSAK");
            penyesuaianList.add("PINDAH LOKASI");
        } else {
            if (isStockLebih) {
                penyesuaianList.add("SALAH CATAT"); //HANYA AKTIF BILA LEBIH BESAR DARI STOCK
            } else {
                penyesuaianList.add("HILANG");
                penyesuaianList.add("RUSAK");
                if (lokasiArray.size() > 1) {
                    penyesuaianList.add("PINDAH LOKASI"); //HANYA AKTIF BILA LOKASI TERSEDIA GUDANG DAN DISPLAY / BERBEDA
                }
            }
        }

        setSpinnerOffline(penyesuaianList, find(R.id.sp_sebab_penyesuaian, Spinner.class), "");
        if (!selection.isEmpty()) {
            for (int i = 0; i < find(R.id.sp_sebab_penyesuaian, Spinner.class).getCount(); i++) {
                if (selection.equals(find(R.id.sp_sebab_penyesuaian, Spinner.class).getItemAtPosition(i).toString())) {
                    find(R.id.sp_sebab_penyesuaian, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
        find(R.id.sp_sebab_penyesuaian, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Tools.setViewAndChildrenEnabled(
                        find(R.id.rl_lokasi_part, LinearLayout.class),
                        parent.getItemAtPosition(position).toString().equals("PINDAH LOKASI")
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSpLokasi(String selection) {
        final Nson penyesuaian = Nson.readJson(getIntentStringExtra(PENYESUAIAN));
        final List<String> lokasiList = new ArrayList<>();
        lokasiList.add("--PILIH--");
        if (isView) {
            lokasiList.add("RUANG PART");
            lokasiList.add("DISPLAY");
            lokasiList.add("PALET");
        } else {
            penyesuaian.add("");
                for (int i = 0; i < penyesuaian.size(); i++) {
                if(!penyesuaian.get(i).get("LOKASI").asString().isEmpty()){
                    lokasiList.add(penyesuaian.get(i).get("LOKASI").asString());
                }
            }
        }
        setSpinnerOffline(lokasiList, find(R.id.sp_lokasi_stockOpname, Spinner.class), "");
        if (lokasiList.size() == 1) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasi_part, LinearLayout.class), false);
        }

        if (!selection.isEmpty()) {
            for (int i = 0; i < find(R.id.sp_lokasi_stockOpname, Spinner.class).getCount(); i++) {
                if (find(R.id.sp_lokasi_stockOpname, Spinner.class).getItemAtPosition(i).toString().equals(selection)) {
                    find(R.id.sp_lokasi_stockOpname, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
        find(R.id.sp_lokasi_stockOpname, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                if(!isView){
                    if(i != 0){
                        newLokasiID = penyesuaian.get(i).get("LOKASI_PART_ID").asInteger();
                        find(R.id.et_no_folder_lain, EditText.class).setText(penyesuaian.get(i).get("KODE").asString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (parent.getSelectedItemPosition() == 1) {
                    //showInfo("OK");
                    //find(R.id.et_no_folder_lain, EditText.class).setText(kodeLokasi);
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

                if (find(R.id.et_user_saksi_penyesuaian, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_user_saksi_penyesuaian, EditText.class).requestFocus();
                    showWarning("User Saksi Belum di Scan");
                    return;
                }

                if (find(R.id.et_ket_penyesuaian, EditText.class).getText().toString().isEmpty()) {
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

    private void viewAllLokasiPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "viewLokasi");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LOKASI_PART), args));
                result = result.get("data").get(0);
                jumlahLokasi = result.get("TOTAL_LOKASI").asInteger();
            }

            @Override
            public void runUI() {

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
                        if (nson.get("data").asArray().isEmpty()) {
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
