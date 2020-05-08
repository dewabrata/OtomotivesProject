package com.rkrzmail.oto.modules.terima_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class DetailPartDiterima extends AppActivity {

    private static final String TAG = "DetailPartDiterima";
    private static final int REQUEST_DETAIL_PART_DITERIMA = 4242;
    private Spinner spinnerLokasiSimpan, spinnerPenempatan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_part_diterima);
        spinnerLokasiSimpan = findViewById(R.id.spinnerLokasiSimpan);
        spinnerPenempatan = findViewById(R.id.spinnerPenempatan);

        initToolbar();
        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DETAIL PART DITERIMA");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        spinnerView1();

        spinnerLokasiSimpan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("GUDANG")) {
                    spinnerLokasiSimpan.setEnabled(false);
                } else if (item.equalsIgnoreCase("RUANG PART BENGKEL")) {
                    spinnerLokasiSimpan.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerView2();

        spinnerPenempatan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PALET001")) {
                    spinnerLokasiSimpan.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void insertdata(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

//                Calendar calendar = Calendar.getInstance();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
//                String dateTime = simpleDateFormat.format(calendar.getTime());


                String namapart = find(R.id.txtNamaPart, EditText.class).getText().toString();
                String jumlah = find(R.id.txtJumlah, EditText.class).getText().toString();
                String hargabeliunit = find(R.id.txtHargaBeliUnit, EditText.class).getText().toString();
                String diskonbeli = find(R.id.txtDiskonBeli, EditText.class).getText().toString();

//                args.put("namapart", namapart);
//                args.put("jumlah", jumlah);
//                args.put("hargabeliunit", hargabeliunit);
//                args.put("diskonbeli", diskonbeli);

                // result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("detailpartditerima"), args));


            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    startActivity(new Intent(DetailPartDiterima.this, TerimaPart.class));
                    finish();
                } else {
                    showError(result.get("status").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }

    private void spinnerView1(){
        ArrayAdapter<CharSequence> pembayaran = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        pembayaran.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLokasiSimpan.setAdapter(pembayaran);
    }

    private void spinnerView2(){
        ArrayAdapter<CharSequence> supplier = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        supplier.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPenempatan.setAdapter(supplier);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK && resultCode == REQUEST_DETAIL_PART_DITERIMA){

        }
    }
}
