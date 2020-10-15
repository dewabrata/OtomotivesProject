package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.ATUR;
import static com.rkrzmail.utils.ConstUtils.CARI_PART;
import static com.rkrzmail.utils.ConstUtils.DETAIL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class JumlahPart_PartKeluar_Activity extends AppActivity {

    private int countScan = 0;
    private Nson partKeluarList = Nson.newObject();
    private Nson nson;
    private boolean isAtur = false, isDetail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengisian_part_keluar_);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if(getIntent().hasExtra(ATUR)){
            isAtur = true;
            getSupportActionBar().setTitle("Jumlah Part");
        }else if(getIntent().hasExtra(DETAIL)){
            getSupportActionBar().setTitle("Jumlah Kembali");
            isDetail = true;
        }
    }

    private void initComponent() {
        initToolbar();
        if(isDetail){
            find(R.id.tl_jumlah_part, TextInputLayout.class).setHint("JUMLAH");
            find(R.id.tl_stock_part_keluar, TextInputLayout.class).setHint("SISA");
            find(R.id.et_no_folder, EditText.class).setVisibility(View.GONE);
            nson = Nson.readJson(getIntentStringExtra(DETAIL));
            find(R.id.et_stock_partKeluar, EditText.class).setText(nson.get("SISA").asString());
        }else{
            find(R.id.tl_jumlah_part, TextInputLayout.class).setHint("JUMLAH MINTA");
            find(R.id.tl_stock_part_keluar, TextInputLayout.class).setHint("STOCK");
            nson = Nson.readJson(getIntentStringExtra("part"));
            find(R.id.et_stock_partKeluar, EditText.class).setText(nson.get("STOCK_RUANG_PART").asString());
        }

        initButton(nson);

        find(R.id.et_no_folder, EditText.class).setText(nson.get("KODE").asString());
        initTextListener();
    }

    private void initTextListener(){
        find(R.id.et_jumlah_partKeluar, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.et_jumlah_partKeluar, EditText.class).removeTextChangedListener(this);
                String jumlah = find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString();
                String stock = find(R.id.et_stock_partKeluar, EditText.class).getText().toString();
                if (!jumlah.equals("") && !stock.equals("")) {
                    if (Integer.parseInt(jumlah) > Integer.parseInt(stock)) {
                        find(R.id.tl_jumlah_part, TextInputLayout.class).setError("Jumlah Minta Melebihi Stock");
                        find(R.id.btn_simpan, Button.class).setEnabled(false);
                    } else {
                        find(R.id.tl_jumlah_part, TextInputLayout.class).setErrorEnabled(false);
                        find(R.id.btn_simpan, Button.class).setEnabled(true);
                    }
                }
                find(R.id.et_jumlah_partKeluar, EditText.class).addTextChangedListener(this);
            }
        });
    }

    private void initButton(final Nson nson) {
        find(R.id.img_scan_barcode, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                if (find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_jumlah_partKeluar, EditText.class).setError("Jumlah Harus Di Isi");
                } else {
                    if(!isDetail){
                        if(isAtur){
                            nson.set("JUMLAH", find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString());
                            i.putExtra("part", nson.toJson());
                        }else{
                            partKeluarList.set("NAMA_PART", nson.get("NAMA_PART"));
                            partKeluarList.set("NO_PART", nson.get("NO_PART"));
                            partKeluarList.set("MERK", nson.get("MERK"));
                            partKeluarList.set("STOCK_RUANG_PART", nson.get("STOCK_RUANG_PART"));
                            partKeluarList.set("KODE", nson.get("KODE"));
                            partKeluarList.set("PART_ID", nson.get("PART_ID"));
                            partKeluarList.set("PENDING", nson.get("PENDING"));
                            partKeluarList.set("JUMLAH", find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString());
                            i.putExtra("part", partKeluarList.toJson());
                        }
                        setResult(RESULT_OK, i);
                        finish();
                    }else{
                        updatePartKeluar(nson);
                    }
                }
            }
        });
    }

    public void getDataBarcode(final String nopart) {
        final Nson nson = Nson.readJson(getIntentStringExtra("part"));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "BARCODE");
                args.put("nopart", nopart);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_CARI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    nson.set("PART_ID", result.get("NO").asString());
                    nson.set("NO_PART", result.get("NOMOR_PART_NOMOR").asString());
                    nson.set("NAMA_PART", result.get("NAMA_PART").asString());
                    countScan++;
                    find(R.id.et_jumlah_partKeluar, EditText.class).setText(String.valueOf(countScan));
                } else {
                    //error
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void updatePartKeluar(final Nson nson){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("stock", find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString());
                args.put("partid", nson.get("PART_ID").asString());
                args.put("id", nson.get("ID_PART_KELUAR").asString());
                args.put("mekanik", nson.get("USER").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpartkeluar"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
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
            getDataBarcode(data != null ? data.getStringExtra("TEXT") : "");
            showSuccess(data != null ? data.getStringExtra("TEXT") : "");
        }
    }
}
