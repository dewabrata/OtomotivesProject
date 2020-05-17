package com.rkrzmail.oto.modules.terima_part;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.lokasi_part.AturLokasiPart_Activity;
import com.rkrzmail.oto.modules.lokasi_part.LokasiPart_Activity;
import com.rkrzmail.utils.Tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class AturTerimaPart extends AppActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "AturTerimaPart";
    private static final int REQUEST_ATUR_TERIMA_PART = 4141;
    private Spinner spinnerSupplier, spinnerPembayaran;
    private ImageView img_calender1, img_calender2, img_calender3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_terima_part);
        spinnerSupplier = findViewById(R.id.spinnerSupplier);
        spinnerPembayaran = findViewById(R.id.spinnerPembayaran);
        img_calender1 = findViewById(R.id.img_calender1);
        img_calender2 = findViewById(R.id.img_calender2);
        img_calender3 = findViewById(R.id.img_calender3);

        img_calender1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");

            }
        });

        img_calender2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        img_calender3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");

            }
        });

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

        find(R.id.btnSelanjutnya, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (find(R.id.txtTipeSupplier, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("")){
                    showError("Tipe supplier harus di isi");return;
                }else if (find(R.id.txtNamaSupplier, EditText.class).getText().toString().equalsIgnoreCase("")){
                    showError("Nama supplier harus di isi");return;
                }else if (find(R.id.txtNoDo, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("No DO harus di isi");
                }else if (find(R.id.txtOngkosKirim, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Ongkos kirim harus di isi");
                }else if (find(R.id.txtPembayaran, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("")){
                    showError("Pembayaran harus di isi");return;
                }else if (find(R.id.img_calender1, DatePicker.class).toString().equalsIgnoreCase("")) {
                    showError(" harus di isi");
                }else if (find(R.id.img_calender2, DatePicker.class).toString().equalsIgnoreCase("")) {
                    showError("Ongkos kirim harus di isi");
                }else if (find(R.id.img_calender3, DatePicker.class).toString().equalsIgnoreCase("")) {
                    showError("Ongkos kirim harus di isi");
                }
                insertData();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

    }

    private void insertData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                final Map<String, String> args = AppApplication.getInstance().getArgsData();

                String tipe = find(R.id.txtTipeSupplier, Spinner.class).getSelectedItem().toString();
                String nama = find(R.id.txtNamaSupplier, EditText.class).getText().toString();
                String nodo = find(R.id.txtNoDo, EditText.class).getText().toString();
                String ongkir = find(R.id.txtOngkosKirim, EditText.class).getText().toString();
                String pembayaran = find(R.id.txtPembayaran, Spinner.class).getSelectedItem().toString();
                String tglpesan = find(R.id.img_calender1, DatePicker.class).toString().toUpperCase();
                String tglterima = find(R.id.img_calender2, DatePicker.class).toString().toUpperCase();
                String jatuhtempo = find(R.id.img_calender3, DatePicker.class).toString().toUpperCase();

                args.put("tipe", tipe);
                args.put("nama", nama);
                args.put("nodo", nodo);
                args.put("ongkir", ongkir);
                args.put("pembayaran", pembayaran);
                args.put("tglpesan", tglpesan);
                args.put("tglterima", tglterima);
                args.put("jatuhtempo", jatuhtempo);

               result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));
               result.toJson().equalsIgnoreCase("data");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK && resultCode == REQUEST_ATUR_TERIMA_PART){

        }
    }
}
