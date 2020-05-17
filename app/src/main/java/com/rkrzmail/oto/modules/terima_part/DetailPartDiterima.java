package com.rkrzmail.oto.modules.terima_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailPartDiterima extends AppActivity {

    private static final String TAG = "DetailPartDiterima";
    private static final int REQUEST_DETAIL_PART_DITERIMA = 4242;
    private Spinner spinnerLokasiSimpan, spinnerPenempatan;
    private Button btnScan, btnTutup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_part_diterima);

        btnScan = findViewById(R.id.btnScan);
        btnTutup = findViewById(R.id.btnTutup);

        initToolbar();
        initComponent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_detailpart_terima);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args2 = AppApplication.getInstance().getArgsData();

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"),args2)) ;
                Log.d("NO_PART", result.get(0).get("NO_PART").asString());
            }

            @Override
            public void runUI() {

                if (result.get("status").asString().equalsIgnoreCase("OK")){
                    find(R.id.txtNoPart, EditText.class).setText(result.get("data").get(0).get("NO_PART").asString());
                    find(R.id.txtNamaPart, EditText.class).setText(result.get("data").get(0).get("NAMA_PART").asString());
                    Log.d("NO_PART", result.get("data").get(0).get("NO_PART").asString());
                }
            }
        });
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DETAIL PART DITERIMA");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent(){

//        spinnerView1();
//
//        spinnerLokasiSimpan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
//                String item = parent.getItemAtPosition(position).toString();
//                if (item.equalsIgnoreCase("GUDANG")) {
//                    spinnerLokasiSimpan.setEnabled(false);
//                } else if (item.equalsIgnoreCase("RUANG PART BENGKEL")) {
//                    spinnerLokasiSimpan.setEnabled(true);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//
//        spinnerView2();
//
//        spinnerPenempatan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
//                String item = parent.getItemAtPosition(position).toString();
//                if (item.equalsIgnoreCase("PALET001")) {
//                    spinnerLokasiSimpan.setEnabled(true);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        find(R.id.btnTutup, Button.class).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (find(R.id.txtJumlah, EditText.class).getText().toString().equalsIgnoreCase("")){
                    showError("Jumlah harus di isi");return;
                }else if (find(R.id.txtHargaBeliUnit, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Harga beli unit harus di isi");
                }else if (find(R.id.txtDiskonBeli, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Diskon beli harus di isi");
                }
                insertdata();
            }
        });

    }

    private void insertdata(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String jumlah = find(R.id.txtJumlah, EditText.class).getText().toString();
                String hargabeliunit = find(R.id.txtHargaBeliUnit, EditText.class).getText().toString();
                String diskonbeli = find(R.id.txtDiskonBeli, EditText.class).getText().toString();
                String lokasisimpan = find(R.id.txtLokasiSimpan, Spinner.class).getSelectedItem().toString();
                String penempatan = find(R.id.txtPenempatan, Spinner.class).getSelectedItem().toString();

                args.put("jumlah", jumlah);
                args.put("hargabeliunit", hargabeliunit);
                args.put("diskonbeli", diskonbeli);
                args.put("lokasi", lokasisimpan);
                args.put("penempatan", penempatan);

                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));
                data.toJson().equalsIgnoreCase("data");

            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, data.get("status").asString());
                    startActivity(new Intent(DetailPartDiterima.this, TerimaPart.class));
                    finish();
                } else {
                    showError(data.get("status").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }

    private void addData(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String jumlah = find(R.id.txtJumlah, EditText.class).getText().toString();
                String hargabeliunit = find(R.id.txtHargaBeliUnit, EditText.class).getText().toString();
                String diskonbeli = find(R.id.txtDiskonBeli, EditText.class).getText().toString();
                String lokasisimpan = find(R.id.txtLokasiSimpan, Spinner.class).getSelectedItem().toString();
                String penempatan = find(R.id.txtPenempatan, Spinner.class).getSelectedItem().toString();

                args.put("action", "add");
                args.put("jumlah", jumlah);
                args.put("hargabeliunit", hargabeliunit);
                args.put("diskonbeli", diskonbeli);
                args.put("lokasi", lokasisimpan);
                args.put("penempatan", penempatan);


                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));
                result.toJson().equalsIgnoreCase("data");
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    startActivity(new Intent(DetailPartDiterima.this, DetailPartDiterima.class));
                    finish();
                } else {
                    showError(result.get("status").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }

//    private void spinnerView1(){
////        List<Integer> noFolder = new ArrayList<Integer>();
////        for(int i = 1; i <= 100; i++){
////            noFolder.add(i);
////        }
//        ArrayAdapter<CharSequence> lokasi_simpan = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
//        lokasi_simpan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerLokasiSimpan.setAdapter(lokasi_simpan);
//    }
//
//    private void spinnerView2(){
////        List<Integer> noFolder = new ArrayList<Integer>();
////        for(int i = 1; i <= 100; i++){
////            noFolder.add(i);
////        }
//        ArrayAdapter<CharSequence> penempatan = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
//        penempatan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerPenempatan.setAdapter(penempatan);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK && resultCode == REQUEST_DETAIL_PART_DITERIMA){

        }
    }
}
