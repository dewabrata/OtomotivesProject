package com.rkrzmail.oto.modules.karyawan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class AturKaryawan extends AppActivity {
    public static final String TAG = "AturKaryawan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_karyawan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("KARYAWAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"),args2)) ;
                Log.d("NAMA", result.get(0).get("NAMA").asString());
            }

            @Override
            public void runUI() {

                if (result.get("status").asString().equalsIgnoreCase("OK")){
                    find(R.id.txtNamaKaryawan, EditText.class).setText(result.get("data").get(0).get("NAMA").asString());
                    //find(R.id.txtNik, EditText.class).setText(result.get("data").get(0).get("NO_PART").asString());
                    find(R.id.txtNoPonsel, EditText.class).setText(result.get("data").get(0).get("NO_PONSEL").asString());
                    find(R.id.txtTglMasuk, EditText.class).setText(result.get("data").get(0).get("TANGGAL_MASUK").asString());

                    Log.d("NAMA", result.get("data").get(0).get("NAMA").asString());
                }
            }
        });


        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                String waktu = find(R.id.txtWaktuPesan, EditText.class).getText().toString().replace(" ", "").toUpperCase();
//                if (waktu.equalsIgnoreCase("")) {
//                    showError("Waktu Pesan Boleh Kosong");
//                    return;
//                } else if (find(R.id.txtStockMinimum, EditText.class).getText().toString().equalsIgnoreCase("")) {
//                    showError("Stock Minimum Tidak Boleh Kosong");
//                    return;
//                } else if (find(R.id.txtMarginharga, EditText.class).getText().toString().equalsIgnoreCase("")) {
//                    showError("Margin / Harga Tidak Boleh Kosong");
//                    return;
//                }

//                Intent intent = new Intent(getActivity(), MenuActivity.class);
//                setResult(RESULT_OK, intent);
//                finish();
                simpan();
            }
        });

    }

    private void simpan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            String waktu, stock, margin;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
                String dateTime = simpleDateFormat.format(calendar.getTime());


                String waktu = find(R.id.txtWaktuPesan, EditText.class).getText().toString();
                //waktu = waktu.toUpperCase().trim().replace(" ", "");

                String stock = find(R.id.txtStockMinimum, EditText.class).getText().toString();
                // stock = stock.toUpperCase().trim().replace(" ", "");

                String margin = find(R.id.txtMarginharga, EditText.class).getText().toString();
                //margin = margin.toUpperCase().trim().replace(" ", "");

                args.put("waktu_pesan", waktu);
                args.put("stock", stock);
                args.put("margin", margin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("v3/aturkaryawan"), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, "success add data" + result.get("status").asString());
                    startActivity(new Intent(AturKaryawan.this, Karyawan.class));
                    finish();
                } else {
                    showError(result.get("message").asString());
                    Log.d(TAG, "error");
                }
            }

              /*  find(R.id.txtNoPart, EditText.class).setText("NO. PART : " + result.get(0).get("NO_PART").asString());
                find(R.id.txtNamaPart, EditText.class).setText("NAMA PART : " + result.get(0).get("NAMA").asString());
                find(R.id.txtMerk, EditText.class).setText("MERK : " + result.get(0).get("MERK").asString());
                find(R.id.txtStatus, EditText.class).setText("STATUS : " + result.get(0).get("STATUS").asString());
                find(R.id.txtWaktuGanti, EditText.class).setText("WAKTU GANTI : " + result.get(0).get("WAKTU_GANTI").asString());
                find(R.id.txtHet, EditText.class).setText("HET : " + result.get(0).get("HET").asString());


               */

        });
    }

}

