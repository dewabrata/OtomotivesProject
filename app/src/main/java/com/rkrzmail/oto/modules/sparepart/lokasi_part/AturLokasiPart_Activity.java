package com.rkrzmail.oto.modules.sparepart.lokasi_part;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

public class AturLokasiPart_Activity extends AppActivity {

    private static final String TAG = "AturLokasiPart_Activity";
    private static final int REQUEST_ATUR_LOKASI_PART = 3232;
    private EditText no_part, nama_part, merk_part;
    private Spinner sp_lokasi_part, sp_penempatan_part, sp_noRakPalet_part, sp_tinggiRak_part, sp_noFolder_part;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_lokasi_part);
        initToolbar();

        no_part = findViewById(R.id.et_no_part);
        nama_part = findViewById(R.id.et_nama_part);
        merk_part = findViewById(R.id.et_merk_part);
        sp_lokasi_part = findViewById(R.id.sp_lokasiPart);
        sp_penempatan_part = findViewById(R.id.sp_penempatan_part);
        sp_noRakPalet_part = findViewById(R.id.sp_norakPalet_part);
        sp_tinggiRak_part = findViewById(R.id.sp_tinggiRak);
        sp_noFolder_part = findViewById(R.id.sp_noFolder_part);

        initComponent();
        final Nson data = Nson.readJson(getIntentStringExtra("NAMA"));

        Intent i = getIntent();
        if(i.hasExtra("NAMA")){
            no_part.setText(data.get("NO_PART_ID").asString());
            nama_part.setText(data.get("NAMA").asString());
            merk_part.setText(data.get("MERK_PART").asString());
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_lokasi_part);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Lokasi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        spinnerView();
        sp_penempatan_part.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PALET")) {
                    sp_tinggiRak_part.setEnabled(false);
                } else if (item.equalsIgnoreCase("RAK")) {
                    sp_tinggiRak_part.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_simpan_lokasi_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                insertData();
            }
        });
    }

    private void insertData() {
       newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String nopart = no_part.getText().toString().toUpperCase();
//                String namapart = nama_part.getText().toString().toUpperCase();
//                String merkpart = merk_part.getText().toString().toUpperCase();
                String lokasi = sp_lokasi_part.getSelectedItem().toString().toUpperCase();
                String tempat = sp_penempatan_part.getSelectedItem().toString().toUpperCase();
                String norakpalet = sp_noRakPalet_part.getSelectedItem().toString().toUpperCase();
                //String tinggirak = sp_tinggiRak_part.getSelectedItem().toString().toUpperCase();
                String nofolder = sp_noFolder_part.getSelectedItem().toString().toUpperCase();

                args.put("lokasi", lokasi);
                args.put("tempat", tempat);
                args.put("palet",  norakpalet);
                args.put("folder", nofolder);
                //args.put("tanggal", );
                //args.put("user", );
                args.put("nopart", nopart);
                //args.put("stock", );

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlokasipart"), args));
                result.toJson().equalsIgnoreCase("data");

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    startActivity(new Intent(AturLokasiPart_Activity.this, LokasiPart_Activity.class));
                    finish();
                } else {
                    showError(result.get("status").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }

    private void spinnerView(){
        //No. Rak Spinner
        List<Integer> noRak = new ArrayList<Integer>();
        for(int i = 1; i <= 100; i++){
            noRak.add(i);
        }
        ArrayAdapter<Integer> rakAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, noRak);
        rakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_noRakPalet_part.setAdapter(rakAdapter);
        //Tinggi Rak Spinner
        List<Integer> tinggiRak = new ArrayList<Integer>();
        for (int i = 1; i <= 10; i++) {
            tinggiRak.add(i);
        }
        ArrayAdapter<Integer> tingiRak_adapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, tinggiRak);
        tingiRak_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_tinggiRak_part.setAdapter(tingiRak_adapter);
        //No folder Spinner
        List<Integer> noFolder = new ArrayList<Integer>();
        for(int i = 1; i <= 100; i++){
            noFolder.add(i);
        }
        ArrayAdapter<Integer> folderAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, noFolder);
        folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_noFolder_part.setAdapter(folderAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            Nson nson = Nson.readJson(getIntentStringExtra("row"));
            no_part.setText(nson.get("NO_PART_ID").asString());
            nama_part.setText(nson.get("NAMA").asString());
            merk_part.setText(nson.get("MERK_PART").asString());
        }
    }
}
