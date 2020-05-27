package com.rkrzmail.oto.modules.terima_part;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;

import java.util.Map;

public class DetailPartDiterima extends AppActivity {

    private static final String TAG = "DetailPartDiterima";
    final int REQUEST_BARCODE = 13;
    private Spinner spinnerLokasiSimpan, spinnerPenempatan;
    private EditText txtNoPart, txtNamaPart, txtJumlah, txtHargaBeliUnit, txtDiskonBeli;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_part_diterima);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DETAIL PART DITERIMA");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent() {

        txtNoPart = findViewById(R.id.txtNoPart);
        txtNamaPart = findViewById(R.id.txtNamaPart);
        txtJumlah = findViewById(R.id.txtJumlah);
        txtHargaBeliUnit = findViewById(R.id.txtHargaBeliUnit);
        txtDiskonBeli = findViewById(R.id.txtDiskonBeli);
        spinnerLokasiSimpan = findViewById(R.id.spinnerLokasiSimpan);
        spinnerPenempatan = findViewById(R.id.spinnerPenempatan);

        find(R.id.btn_scan_terimaPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(intent, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan_terimaPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                insertdata();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_detailpart_terima);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });

    }

    private void insertdata() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;
            @Override
            public void run() {
                Nson nson = Nson.readNson(getIntentStringExtra("detail"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String jumlah = txtJumlah.getText().toString();
                String hargabeliunit = txtHargaBeliUnit.getText().toString();
                String diskonbeli = txtDiskonBeli.getText().toString();
                String lokasisimpan = spinnerLokasiSimpan.getSelectedItem().toString();
                String penempatan = spinnerPenempatan.getSelectedItem().toString();
                //tipe, nama, nodo, tglpesan, tglterima, pembayaran, jatuhtempo, ongkir, namapart,
                // nopart, jumlah, hargabeli, diskon, lokasi, penempatan
                args.put("nodo", nson.get("nodo").asString());
                args.put("tipe", nson.get("tipe").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("tglpesam", nson.get("tglpesan").asString());
                args.put("tglterima", nson.get("tglterima").asString());
                args.put("pembayaran", nson.get("pembayaran").asString());
                args.put("jatuhtempo", nson.get("jatuhtempo").asString());
                args.put("ongkir", nson.get("ongkir").asString());

                args.put("jumlah", jumlah);
                args.put("hargabeliunit", hargabeliunit);
                args.put("diskonbeli", diskonbeli);
                args.put("lokasi", lokasisimpan);
                args.put("penempatan", penempatan);

                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));

            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(DetailPartDiterima.this, TerimaPart.class));
                    finish();
                } else {
                    showError("Gagal Menambahkan Aktifitas");
                }
            }
        });
    }

    private void addData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));
            }

            @Override
            public void runUI() {
                startActivity(new Intent(DetailPartDiterima.this, TerimaPart.class));
                finish();
            }
        });
    }

    public void barcode() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(), "CID", ""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(), "EMA", ""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));

            }

            @Override
            public void runUI() {
                txtNamaPart.setText(result.get("").asString());
                txtNoPart.setText(result.get("").asString());
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK && resultCode == AturTerimaPart.REQUEST_DETAIL_PART) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            String barCode = getIntentStringExtra(data, "TEXT");
            barcode();
        }
    }
}
