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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.lokasi_part.AturLokasiPart_Activity;
import com.rkrzmail.oto.modules.lokasi_part.LokasiPart_Activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;


public class AturTerimaPart extends AppActivity {

    private static final String TAG = "AturTerimaPart";
    private static final int REQUEST_ATUR_TERIMA_PART = 4141;
    private Spinner spinnerSupplier, spinnerPembayaran;
    private ImageView imgSearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_terima_part);
        spinnerSupplier = findViewById(R.id.spinnerSupplier);
        spinnerPembayaran = findViewById(R.id.spinnerPembayaran);
        imgSearching = findViewById(R.id.imgSearching);

        initToolbar();
        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ATUR TERIMA PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        spinnerView1();

        spinnerPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("TRANSFER DIMUKA")) {
                    spinnerPembayaran.setEnabled(false);
                } else if (item.equalsIgnoreCase("CASH DIMUKA")) {
                    spinnerPembayaran.setEnabled(false);
                } else if (item.equalsIgnoreCase("COD")){
                    spinnerPembayaran.setEnabled(false);
                } else if (item.equalsIgnoreCase("INV")){
                    spinnerPembayaran.setEnabled(false);
                } else if (item.equalsIgnoreCase("KONSIGNMENT")){
                    spinnerPembayaran.setEnabled(false);
                } else  if (item.equalsIgnoreCase("PINJAMAN")){
                    spinnerPembayaran.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerView2();

        spinnerSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PRINCIPAL")) {
                    spinnerSupplier.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btnSelanjutnya, Button.class).setOnClickListener(new View.OnClickListener() {
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

//                Calendar calendar = Calendar.getInstance();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
//                String dateTime = simpleDateFormat.format(calendar.getTime());


                String supplier = find(R.id.txtNamaSupplier, EditText.class).getText().toString();
                String namasupplier = find(R.id.txtNamaSupplier, EditText.class).getText().toString();
                String nodo = find(R.id.txtNoDo, EditText.class).getText().toString();
                String ongkoskirim = find(R.id.txtOngkosKirim, EditText.class).getText().toString();

//                String tglpesan = find(R.id.img_calender1, EditText.class).getText().toString();
//                String tglterima = find(R.id.img_calender2, EditText.class).getText().toString();
//                String jatuhtempo = find(R.id.img_calender3, EditText.class).getText().toString();


//                args.put("supplier", supplier);
//                args.put("namasupplier", namasupplier);
//                args.put("nodo", nodo);
//                args.put("ongkoskirim", ongkoskirim);

               // result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));




            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    startActivity(new Intent(AturTerimaPart.this, DetailPartDiterima.class));
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
        spinnerPembayaran.setAdapter(pembayaran);
    }

    private void spinnerView2(){
        ArrayAdapter<CharSequence> supplier = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        supplier.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSupplier.setAdapter(supplier);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK && resultCode == REQUEST_ATUR_TERIMA_PART){

        }
    }

}
