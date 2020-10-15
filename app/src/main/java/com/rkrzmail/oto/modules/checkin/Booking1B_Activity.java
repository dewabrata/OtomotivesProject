package com.rkrzmail.oto.modules.checkin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class Booking1B_Activity extends AppActivity implements View.OnClickListener {

    private Spinner spLokasiLayanan;
    private EditText etLonglat, etAlamat;
    private Nson nson;
    private CheckBox cbAntarJemput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking1_b_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spLokasiLayanan = findViewById(R.id.sp_pekerjaan_booking1b);
        etLonglat = findViewById(R.id.et_longlat_booking1b);
        etAlamat = findViewById(R.id.et_alamat_booking1b);
        cbAntarJemput = findViewById(R.id.cb_layanan_booking1b);

        componentValidation();

        find(R.id.btn_radius_booking1b, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkRadius();
            }
        });

        find(R.id.btn_simpan, Button.class).setText("Lanjut");
        find(R.id.btn_simpan, Button.class).setOnClickListener(this);
        find(R.id.btn_hapus, Button.class).setText("Batal");
        find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
        find(R.id.btn_hapus, Button.class).setOnClickListener(this);
    }

    private void componentValidation() {
        nson = Nson.readNson(getIntentStringExtra("data"));
        spLokasiLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("BENGKEL")) {
                    find(R.id.cb_layanan_booking1b, CheckBox.class).setEnabled(true);
                    etLonglat.setEnabled(false);
                    etAlamat.setEnabled(false);
                }else if(item.equalsIgnoreCase("HOME")){
                    find(R.id.cb_layanan_booking1b, CheckBox.class).setEnabled(false);
                    etLonglat.setEnabled(true);
                    etAlamat.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Log.d("Book1b", "tambah : " + nson.get("derek"));
        if(nson.get("derek").asString().equalsIgnoreCase("YA")){
            cbAntarJemput.setVisibility(View.GONE);
            Tools.getIndexSpinner(spLokasiLayanan, "BENGKEL");
            spLokasiLayanan.setEnabled(false);
        }

        find(R.id.cb_layanan_booking1b, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){

                }
            }
        });
    }

    void hitungBiaya(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    String total = result.get("BIAYA_TRANSPORT").asString();
                }else{
                    showInfo("Silahkan Coba Lagi");
                }

            }
        });
    }

    private void setSelanjutnya() {
        nson = Nson.readNson(getIntentStringExtra("data"));
        nson.set("lokasi", spLokasiLayanan.getSelectedItem().toString());
        nson.set("antarjemput",  cbAntarJemput.isChecked() ? "YA" : "TIDAK");
        nson.set("alamat", etAlamat.getText().toString());
        nson.set("longlat", etLonglat.getText().toString());
        nson.set("biayatransport", "biayatransport");

        if(nson.get("statusbook").asString().equals("")){
            if(spLokasiLayanan.getSelectedItem().toString().equalsIgnoreCase("HOME")){
                nson.set("statusbook", "BOOK HOME");
            }else if(spLokasiLayanan.getSelectedItem().toString().equalsIgnoreCase("BENGKEL")
                    && !find(R.id.cb_layanan_booking1b, CheckBox.class).isChecked()){
                nson.set("statusbook", "BOOK BENGKEL");
            }else if(spLokasiLayanan.getSelectedItem().toString().equalsIgnoreCase("BENGKEL")
                    && find(R.id.cb_layanan_booking1b, CheckBox.class).isChecked()){
                nson.set("statusbook", "BOOK JEMPUT");
            }
        }

        Log.e("statusbook", "Book : " + nson);
        Intent i = new Intent(getActivity(), Booking2_Activity.class);
        i.putExtra("data", nson.toJson());
        startActivityForResult(i, KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN);
    }

    private void checkRadius() {
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {

            }
        });
    }

    private void cancelBook() {
        final EditText input = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Alasan Batal")
                .setView(input)
                .setPositiveButton("Kirim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editTextInput = input.getText().toString();
                        newProses(new Messagebox.DoubleRunnable() {
                            Nson result;
                            @Override
                            public void run() {
                                Map<String, String> args = AppApplication.getInstance().getArgsData();
                                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
                            }

                            @Override
                            public void runUI() {
                                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                                    setResult(RESULT_OK);
                                    finish();
                                }else{

                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Tidak Jadi Batal", null)
                .create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_simpan:
                if (spLokasiLayanan.getSelectedItem().toString().equalsIgnoreCase("HOME")) {
                    if (etLonglat.getText().toString().isEmpty()) {
                        etLonglat.setError("Longlat di perlukan");
                        return;
                    }
                    if (etAlamat.getText().toString().isEmpty()) {
                        etAlamat.setError("Alamat Di Perlukan");
                        return;
                    }
                }

                setSelanjutnya();
                break;
            case R.id.btn_hapus:
                cancelBook();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN){
            setResult(RESULT_OK);
            finish();
        }
    }
}
