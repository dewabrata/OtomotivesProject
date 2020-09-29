package com.rkrzmail.oto.modules.sparepart.stock_opname;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.rkrzmail.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PENYESUAIAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class Penyesuain_Activity extends AppActivity {

    String kodeLokasi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyesuain);
        initToolbar();
        setSpLokasi();

        Nson penyesuaian = Nson.readJson(getIntentStringExtra(PENYESUAIAN));
        List<String> lokasiList = new ArrayList<>();

        for (int i = 0; i < penyesuaian.size(); i++) {
            lokasiList.add(penyesuaian.get(i).get("LOKASI").asString());
        }
        setSpPenyesuaian(lokasiList, penyesuaian.get("FUNGSI").asString());
        initButton();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penyesuaian");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setPenyesuaian() {
        Nson penyesuaian = Nson.newObject();

        penyesuaian.set("PENYESUAIAN", find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString());
        penyesuaian.set("FOLDER_LAIN", find(R.id.et_no_folder_lain, EditText.class).getText().toString());
        penyesuaian.set("KET", find(R.id.et_ket_penyesuaian, EditText.class).getText().toString());
        penyesuaian.set("USER_SAKSI", find(R.id.et_user_saksi_penyesuaian, EditText.class).getText().toString());


        Intent i = new Intent();
        i.putExtra(DATA, penyesuaian.toJson());
        setResult(RESULT_OK, i);
        finish();
    }

    private void setSpPenyesuaian(List<String> penyesuaian, String fungsi) {
        List<String> penyesuaianList = new ArrayList<>();
        penyesuaianList.add("--PILIH--");
        penyesuaianList.add("HILANG");
        penyesuaianList.add("RUSAK");
        if (fungsi.equals("SUPPLIES")) {
            penyesuaianList.add("PEMAKAIAN");
        }
        if (penyesuaian.size() == 1) {
            penyesuaianList.clear();
            penyesuaianList.add("--PILIH--");
            penyesuaianList.add("SALAH CATAT"); //HANYA AKTIF BILA LEBIH BESAR DARI STOCK
        } else {
            penyesuaianList.add("PINDAH LOKASI"); //HANYA AKTIF BILA LOKASI TERSEDIA GUDANG DAN DISPLAY
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, penyesuaianList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_sebab_penyesuaian, Spinner.class).setAdapter(spinnerAdapter);
    }

    private void setSpLokasi() {
        Nson penyesuaian = Nson.readJson(getIntentStringExtra(PENYESUAIAN));
        List<String> lokasiList = new ArrayList<>();
        final List<String> kodeList = new ArrayList<>();
        if (penyesuaian.asArray().size() > 1) {
            penyesuaian.remove(0);
            lokasiList.add("--PILIH--");
            kodeList.add("");
        }
        for (int i = 0; i < penyesuaian.size(); i++) {
            lokasiList.add(penyesuaian.get(i).get("LOKASI").asString());
            kodeList.add(penyesuaian.get(i).get("KODE").asString());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_lokasi_stockOpname, Spinner.class).setAdapter(spinnerAdapter);
        if (lokasiList.size() == 1) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasi_part, LinearLayout.class), false);
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
                    showInfo("OK");
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

    public void getDataBarcode(final String user) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);

                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String barcode = getIntentStringExtra(data, "TEXT");
            getDataBarcode(barcode);
        }
    }
}
