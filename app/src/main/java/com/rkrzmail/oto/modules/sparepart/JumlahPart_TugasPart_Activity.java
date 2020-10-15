package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_TUGAS_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_BATAL;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;

public class JumlahPart_TugasPart_Activity extends AppActivity {

    private EditText etStock, etNofolder, etJumlahPart, etJumlahRequest;

    private boolean isPermintaan = false;
    private boolean isBatal = false;
    private boolean isTerseida = false;

    private int counScanPart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_part_tugas_part);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Jumlah Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etStock = findViewById(R.id.et_stock_jumlahTp);
        etNofolder = findViewById(R.id.et_noFolder_jumlahTp);
        etJumlahPart = findViewById(R.id.et_jumlah_part);
        etJumlahRequest = findViewById(R.id.et_jumlah_request);
        loadData();
    }

    private void loadData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        if (getIntent().hasExtra(TUGAS_PART_PERMINTAAN)) {
            isPermintaan = true;
            find(R.id.tl_jumlah, TextInputLayout.class).setHint(getResources().getString(R.string.jumlah_permintaan));
            etStock.setText(nson.get("STOCK").asString());
            etNofolder.setText(nson.get("KODE").asString());
        } else if (getIntent().hasExtra(TUGAS_PART_BATAL)) {
            isBatal = true;
            find(R.id.tl_stock).setVisibility(View.GONE);
            find(R.id.tl_jumlah, TextInputLayout.class).setHint(getResources().getString(R.string.jumlah_batal));
        }

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              updateTugasPart();
            }
        });

        find(R.id.img_scan_barcode_jumlah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), BarcodeActivity.class), REQUEST_BARCODE);
            }
        });
    }


    private void updateTugasPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "PERMINTAAN");
                if (isPermintaan) {

                }
                if (isBatal) {

                }
                if(isTerseida){

                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_TUGAS_PART), args));
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

    public void getDataBarcode(final String nopart) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "BARCODE");
                args.put("nopart", nopart);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    counScanPart++;
                    etJumlahRequest.setText(counScanPart);
                } else {
                    //error
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            getDataBarcode(data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "");
            showSuccess(data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "");
        }
    }
}
