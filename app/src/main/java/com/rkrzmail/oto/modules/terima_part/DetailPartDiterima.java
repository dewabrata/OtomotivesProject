package com.rkrzmail.oto.modules.terima_part;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DetailPartDiterima extends AppActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "DetailPartDiterima";
    final int REQUEST_BARCODE = 13;
    private static final String TAMBAH_PART = "TAMBAH";
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
        txtJumlah = findViewById(R.id.tv_jumlah_terimaPart);
        txtHargaBeliUnit = findViewById(R.id.txtHargaBeliUnit);
        txtDiskonBeli = findViewById(R.id.txtDiskonBeli);
        spinnerLokasiSimpan = findViewById(R.id.spinnerLokasiSimpan);
        spinnerPenempatan = findViewById(R.id.spinnerPenempatan);

        spinnerLokasiSimpan.setOnItemSelectedListener(this);
        spinnerPenempatan.setOnItemSelectedListener(this);

        txtHargaBeliUnit.addTextChangedListener(new RupiahFormat(txtHargaBeliUnit));
        txtDiskonBeli.addTextChangedListener(new RupiahFormat(txtDiskonBeli));

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
                diskonbeli = diskonbeli.trim().replace(" %", "");
                String lokasisimpan = spinnerLokasiSimpan.getSelectedItem().toString();
                String penempatan = spinnerPenempatan.getSelectedItem().toString();

//                int hargaSemuaUnit = Integer.parseInt(jumlah) * Integer.parseInt(hargabeliunit);
//                int diskonPerUnit = Integer.parseInt(hargabeliunit) - ((Integer.parseInt(hargabeliunit) * Integer.parseInt(diskonbeli)) / 100);

                //tipe,nama, nodo, tglpesan, tglterima, pembayaran, jatuhtempo, ongkir, namapart,
                // nopart, jumlah, hargabeli, diskon, lokasi, penempatan

                String spJumlah = getSetting("jumlah");
                String spBeli = getSetting("hargabeli");
                String spDiskon = getSetting("diskonbeli");
                String spLokasi = getSetting("lokasisimpan");
                String spTempat = getSetting("penempatan");

                if (find(R.id.fab_tambah_detailpart_terima, FloatingActionButton.class).callOnClick()) {
                    args.put("jumlah", getSetting("jumlah"));
                    args.put("hargabeliunit", getSetting("hargabeli"));
                    args.put("diskonbeli", getSetting("diskonbeli"));
                    args.put("lokasi", getSetting("lokasisimpan"));
                    args.put("penempatan", getSetting("penempatan"));
                    Log.d("total", spJumlah);
                }else{
                    args.put("namapart", "");
                    args.put("nopart", "");
                    args.put("jumlah", jumlah);
                    args.put("hargabeliunit", hargabeliunit);
                    args.put("diskonbeli", diskonbeli);
                    args.put("lokasi", lokasisimpan);
                    args.put("penempatan", penempatan);
                }

                args.put("nodo", nson.get("nodo").asString());
                args.put("tipe", nson.get("tipe").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("tglpesan", nson.get("tglpesan").asString());
                args.put("tglterima", nson.get("tglterima").asString());
                args.put("pembayaran", nson.get("pembayaran").asString());
                args.put("jatuhtempo", nson.get("jatuhtempo").asString());
                args.put("ongkir", nson.get("ongkir").asString());


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

        ArrayList<String> jumlahTotal = new ArrayList<>();
        ArrayList<String> hargaBeliUnit = new ArrayList<>();
        ArrayList<String> diskonBeli = new ArrayList<>();
        ArrayList<String> lokasiSimpan = new ArrayList<>();
        ArrayList<String> tempat = new ArrayList<>();

        String[] jumlah = txtJumlah.getText().toString().trim().split(", ");
        String[] hargabeliunit = txtHargaBeliUnit.getText().toString().trim().split(", ");
        String[] diskonbeli = txtDiskonBeli.getText().toString().trim().split(", ");
        String[] lokasisimpan = spinnerLokasiSimpan.getSelectedItem().toString().trim().split(", ");
        String[] penempatan = spinnerPenempatan.getSelectedItem().toString().trim().split(", ");

        jumlahTotal.addAll(Arrays.asList(jumlah));
        hargaBeliUnit.addAll(Arrays.asList(hargabeliunit));
        diskonBeli.addAll(Arrays.asList(diskonbeli));
        lokasiSimpan.addAll(Arrays.asList(lokasisimpan));
        tempat.addAll(Arrays.asList(penempatan));

        setSetting("jumlah", String.valueOf(jumlahTotal));
        setSetting("hargabeliunit", String.valueOf(hargaBeliUnit));
        setSetting("diskonbeli", String.valueOf(diskonBeli));
        setSetting("lokasisimpan", String.valueOf(lokasiSimpan));
        setSetting("penempatan", String.valueOf(tempat));

        Tools.clearForm(find(R.id.ly_detailPart, LinearLayout.class));
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
        if (requestCode == AturTerimaPart.REQUEST_DETAIL_PART && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            String barCode = getIntentStringExtra(data, "TEXT");
            barcode();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (view.getId()) {
            case R.id.spinnerLokasiSimpan:

                break;
            case R.id.spinnerPenempatan:

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
