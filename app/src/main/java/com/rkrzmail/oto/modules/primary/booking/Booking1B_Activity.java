package com.rkrzmail.oto.modules.primary.booking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import java.util.Map;

public class Booking1B_Activity extends AppActivity implements View.OnClickListener {

    private Spinner spLokasiLayanan;
    private EditText etLonglat, etAlamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking1_b_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking1b);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spLokasiLayanan = findViewById(R.id.sp_pekerjaan_booking1b);
        etLonglat = findViewById(R.id.et_longlat_booking1b);
        etAlamat = findViewById(R.id.et_alamat_booking1b);

        componentValidation();

        find(R.id.cb_layanan_booking1b, CheckBox.class);


        find(R.id.btn_radius_booking1b, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        find(R.id.btn_lanjut_booking1b, Button.class).setOnClickListener(this);
        find(R.id.btn_batal_booking1b, Button.class).setOnClickListener(this);
    }

    private void componentValidation() {
        spLokasiLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("HOME")) {
                    find(R.id.cb_layanan_booking1b, CheckBox.class).setEnabled(false);
                } else {
                    find(R.id.cb_layanan_booking1b, CheckBox.class).setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSelanjutnya() {
        // add : CID, action(add), nopol, jeniskendaraan, nopon, nama,
        // pemilik, kondisi, keluhan, lokasi, jemput,
        // antar, layanan, km, alamat, date, user, status,
        // tanggalbook, jambook, jemput, antar, hari, jam, biayatrans, biayalayan, dp
        Nson nson = Nson.readNson(getIntentStringExtra("data"));
        nson.set("lokasi", spLokasiLayanan.getSelectedItem().toString());
        nson.set("jemput", find(R.id.cb_layanan_booking1b, CheckBox.class).isChecked() ? "YA" : "TIDAK");
        nson.set("alamat", etAlamat.getText().toString());
        nson.set("longlat", etLonglat.getText().toString());

        Intent i = new Intent(getActivity(), Booking2_Activity.class);
        i.putExtra("data", nson.toJson());
        startActivity(i);
        finish();
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

                                } else {

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
            case R.id.btn_lanjut_booking1b:
                if (spLokasiLayanan.getSelectedItem().toString().equalsIgnoreCase("HOME")) {
                    if (etLonglat.getText().toString().isEmpty()) {
                        etLonglat.setError("Longlat di perlukan");
                        return;
                    } else if (etAlamat.getText().toString().isEmpty()) {
                        etAlamat.setError("Alamat Di Perlukan");
                        return;
                    }
                }
                setSelanjutnya();
                break;
            case R.id.btn_batal_booking1b:
                cancelBook();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
