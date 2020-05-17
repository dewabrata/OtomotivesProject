package com.rkrzmail.oto.modules.booking;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class Booking_Activity extends AppActivity {

    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel;
    private EditText  etNamaPelanggan, etKeluhan, etKm, etAlamat;
    private Spinner spKondisiKendaraan, spLayanan, spLokasiLayanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_);
        initToolbar();
        initComponent();
    }


    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking 1");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        etNopol = (NikitaAutoComplete) findViewById(R.id.et_nopol_booking1);
        etJenisKendaraan = (NikitaAutoComplete) findViewById(R.id.et_jenisKendaraan_booking1);
        etNoPonsel = (NikitaAutoComplete) findViewById(R.id.et_noPonsel_booking1);
        etNamaPelanggan = findViewById(R.id.et_namaPelanggan_booking1);
        etKeluhan = findViewById(R.id.et_keluhan_booking1);
        etKm = findViewById(R.id.et_km_booking1);
        etAlamat = findViewById(R.id.et_alamat_booking1);
        spKondisiKendaraan = findViewById(R.id.sp_kondisi_booking1);
        spLayanan = findViewById(R.id.sp_layanan_booking1);
        spLokasiLayanan = findViewById(R.id.sp_lokasiLayanan_booking1);

        componentValidation();

        find(R.id.btn_history_booking1, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHistory();
            }
        });

        find(R.id.btn_lanjut_booking1, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextBooking();
            }
        });
    }

    private void showHistory(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking1"), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){

                }else{
                    showInfo("GAGAL!");
                }
            }
        });

    }

    private void nextBooking(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking1"), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){

                    
                }else{
                    showInfo("GAGAL!");
                }
            }
        });

    }

    private void componentValidation(){
        etNopol.setThreshold(3);
        etNopol.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                return super.onFindNson(context, bookTitle);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
//                //jika nopol terdaftar maka enable tombol history
//                etJenisKendaraan.setText();
//                etNoPonsel.setText();
//                etNamaPelanggan.setText();
            }

        });

        etJenisKendaraan.setThreshold(3);
        etJenisKendaraan.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                return super.onFindNson(context, bookTitle);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
                //etJenisKendaraan.setText();
            }
        });

        etNoPonsel.setThreshold(7);
        etNoPonsel.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                return super.onFindNson(context, bookTitle);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        });

        spLokasiLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("ALAMAT")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.tl_alamat_booking), false);
                } else if (item.equalsIgnoreCase("HOME")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.tl_alamat_booking), true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_cb_booking), false);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(find(R.id.cb_jemput_booking1, CheckBox.class).isChecked()){
            etAlamat.setEnabled(true);
        }
    }
}
