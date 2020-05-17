package com.rkrzmail.oto.modules.tenda;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class AturTenda_Activity extends AppActivity implements View.OnClickListener{

    private static final String TAG = "AturTenda_Activity";
    private static final int REQUEST_LOCATION = 1;
    private EditText etTglBuka, etTglSelesai, etLokasi, etAlamat, etLonglat, etJamBuka, etJamTutup;
    private RadioGroup rgTipe;
    private LocationManager locationManager;
    private  String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_tenda_);
        initToolbar();
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_tenda);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tenda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);


        etTglBuka = findViewById(R.id.et_tglMulai_tenda);
        etTglSelesai = findViewById(R.id.et_tglSelesai_tenda);
        etLokasi = findViewById(R.id.et_namaLokasi_tenda);
        etAlamat = findViewById(R.id.et_alamat_tenda);
        etLonglat = findViewById(R.id.et_longlat_tenda);
        etJamBuka = findViewById(R.id.et_jamBuka_tenda);
        etJamTutup = findViewById(R.id.et_jamSelesai_tenda);
        rgTipe = findViewById(R.id.rg_tipe_tenda);

        rgTipe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_tetap:
                        Tools.setViewAndChildrenEnabled(find(R.id.tl_alamat, TextInputLayout.class), true);
                        Tools.setViewAndChildrenEnabled(find(R.id.tl_longlat, TextInputLayout.class), true);
                        break;
                    case R.id.rb_mobile:
                        Tools.setViewAndChildrenEnabled(find(R.id.tl_alamat, TextInputLayout.class), false);
                        Tools.setViewAndChildrenEnabled(find(R.id.tl_longlat, TextInputLayout.class), false);
                        break;
                }
            }
        });

        find(R.id.btn_getLocation_tenda, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //Check gps is enable or not
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    //Write Function To enable gps
                    OnGPS();
                }
                else {
                    //GPS is already On then
                    getLocation();
                }
            }
        });

        etTglBuka.setOnClickListener(this);
        etTglSelesai.setOnClickListener(this);
        etJamBuka.setOnClickListener(this);
        etJamTutup.setOnClickListener(this);

        find(R.id.btn_simpan_tenda, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTglBuka.getText().toString().equalsIgnoreCase("")) {
                    etTglBuka.setError("Silahkan Di isi");
                } else if (etTglSelesai.getText().toString().equalsIgnoreCase("")) {
                    etTglSelesai.setError("Silahkan Di Isi");
                } else if (etLokasi.getText().toString().equalsIgnoreCase("")) {
                    etLokasi.setError("Silahkan Di Isi");
                } else if (etAlamat.getText().toString().equalsIgnoreCase("")) {
                    etAlamat.setError("Silahkan Di Isi");
                } else if (etLonglat.getText().toString().equalsIgnoreCase("")) {
                    etLonglat.setError("Silahkan Di Isi");
                } else if (etJamBuka.getText().toString().equalsIgnoreCase("")) {
                    etJamBuka.setError("Silahkan Di Isi");
                } else if (etJamTutup.getText().toString().equalsIgnoreCase("")) {
                    etJamTutup.setError("Silahkan Di Isi");
                } else {
                    saveData();
                }

            }
        });

    }

    private void getLocation() {
        //Check Permissions again

        if (ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),

                Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                etLonglat.setText(latitude + ", "+ longitude);

            }else {
                showInfo("Can't Get Your Location");
            }
        }

    }

    private void OnGPS() {
        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_tglMulai_tenda:
                Tools.getDatePickerDialog(getActivity(), etTglBuka);
                break;
            case R.id.et_tglSelesai_tenda:
                Tools.getDatePickerDialog(getActivity(), etTglSelesai);
                break;
            case R.id.et_jamBuka_tenda:
                Tools.getTimePickerDialog(getActivity(), etJamBuka);
                break;
            case R.id.et_jamSelesai_tenda:
                Tools.getTimePickerDialog(getActivity(), etJamTutup);
                break;

        }
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                int selectedTipe = rgTipe.getCheckedRadioButtonId();
                String tipe;
                String tglMulai = etTglBuka.getText().toString();
                String tglSelesai = etTglSelesai.getText().toString();
                String lokasi = etLokasi.getText().toString();
                String longlat = etLonglat.getText().toString();
                String alamat = etAlamat.getText().toString();
                String jamBuka = etJamBuka.getText().toString();
                String jamTutup = etJamTutup.getText().toString();

                args.put("action", "save");
                switch (selectedTipe) {
                    case R.id.rb_tetap:
                        tipe = find(R.id.rb_tetap, RadioButton.class).getText().toString();
                        Log.d("TIPE", tipe);
                        args.put("tipe", tipe);
                        break;
                    case R.id.rb_mobile:
                        tipe = find(R.id.rb_mobile, RadioButton.class).getText().toString();
                        Log.d("TIPE", tipe);
                        args.put("tipe", tipe);
                        break;
                }

                //args.put("id", id);
                args.put("tanggal", tglMulai);
                args.put("namalokasi", lokasi);
                args.put("alamat", alamat);
                args.put("lokasi", longlat);
                args.put("buka", jamBuka);
                args.put("tutup", jamTutup);
                args.put("status", "");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    showInfo("Berhasil Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), Tenda_Activity.class));
                    finish();
                } else {
                    showError("Gagal Menyiman Aktifitas ");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
