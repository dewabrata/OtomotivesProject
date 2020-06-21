package com.rkrzmail.oto.modules.registrasi_bengkel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.LocationPicker_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.ArrayList;
import java.util.Map;

public class RegistrasiBengkel_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_REFEREAL = 56;
    private static final int REQUEST_MAPS = 57;
    private EditText etKodeRef, etNamaPemilik, etNoPonsel, etEmail, etNamaBengkel, etAlamat;
    private Spinner spBidangUsaha;
    private TextView tvLokasi;
    private MultiSelectionSpinner spKendaraan;
    private NikitaAutoComplete etKotaKab;
    private ArrayList<String> dummies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi_bengkel_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Registrasi Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etKodeRef = findViewById(R.id.et_kodeRef_regist);
        etAlamat = findViewById(R.id.et_alamat_regist);
        etEmail = findViewById(R.id.et_email_regist);
        etKotaKab = findViewById(R.id.et_kotakab_regist);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_regist);
        etNamaPemilik = findViewById(R.id.et_namaPemilik_regist);
        etNoPonsel = findViewById(R.id.et_noPhone_regist);
        spBidangUsaha = findViewById(R.id.sp_usaha_regist);
        spKendaraan = findViewById(R.id.sp_jenisKendaraan_regist);
        tvLokasi = findViewById(R.id.tv_lokasi_regist);

        String aggrement = "Setuju dengan <font color=#F44336><u> Syarat & kondisi </u></font> pemakain Otomotives";
        find(R.id.cb_setuju_regist, CheckBox.class).setText(Html.fromHtml(aggrement));

        etKotaKab.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_et_kotakab_regist));
        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");
        setMultiSelectionSpinnerFromApi(spKendaraan, "nama", "BENGKEL", "viewmst", "TYPE");

        tvLokasi.setOnClickListener(this);
        find(R.id.btn_simpan_regist, Button.class).setOnClickListener(this);
        find(R.id.btn_check_regist, Button.class).setOnClickListener(this);

    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                //parameter : action : add, nohp, nama, email, namabengkel, jenis,
                // kategori, alamat, daerah, lokasi
                args.put("action", "add");
                args.put("nohp", etNoPonsel.getText().toString());
                args.put("nama", etNamaPemilik.getText().toString());
                args.put("email", etEmail.getText().toString());
                args.put("namabengkel", etNamaBengkel.getText().toString());
                args.put("jenis", spKendaraan.getSelectedItemsAsString());
                //args.put("kategori", spBidangUsaha.getSelectedItem().toString());
                //args.put("setuju", find(R.id.cb_setuju_regist, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("alamat", etAlamat.getText().toString());
                args.put("daerah", etKotaKab.getText().toString());
                //args.put("lokasi", tvLokasi.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("regristasi"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Registrasi Berhasil");
                    finish();
                } else {
                    showInfo("Registrasi Gagal, Silahkan Cek Data Anda Kembali");
                }
            }
        });
    }

    private boolean dataValidation() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("registrasi"), args));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    nListArray.add(result.get("data").get(i).get("NO_PONSEL"));
                    nListArray.add(result.get("data").get(i).get("NAMA_BENGKEL"));
                    nListArray.add(result.get("data").get(i).get("LOKASI"));
                    nListArray.add(result.get("data").get(i).get("EMAIL"));
                }
            }
        });
        return true;
    }

    @Override
    public void onClick(View view) {
        String info = "Silahkan lengkapi";
        switch (view.getId()) {
            case R.id.btn_simpan_regist:
                if (etNamaPemilik.getText().toString().isEmpty()) {
                    etNamaPemilik.setError(info + "Nama Pemilik");
                    etNamaPemilik.requestFocus();
                    return;
                }
                if (etNoPonsel.getText().toString().isEmpty()) {
                    etNoPonsel.setError(info + "No. Ponsel");
                    etNoPonsel.requestFocus();
                    return;
                }
                if (etEmail.getText().toString().isEmpty()) {
                    etEmail.setError(info + "Email");
                    etEmail.requestFocus();
                    return;
                }
                if (etNamaBengkel.getText().toString().isEmpty()) {
                    etNamaBengkel.setError(info + "Nama Bengkel");
                    etNamaBengkel.requestFocus();
                    return;
                }
                if (spKendaraan.getSelectedItemsAsString().equalsIgnoreCase("")) {
                    showInfo(info + "Kendaraan");
                    spKendaraan.requestFocus();
                    return;
                }
                if (etKotaKab.getText().toString().isEmpty()) {
                    etKotaKab.setError(info + "Kota / Kab");
                    etKotaKab.requestFocus();
                    return;
                }
                if (etAlamat.getText().toString().isEmpty() && etAlamat.length() < 7) {
                    etAlamat.setError(info + "Alamat");
                    etAlamat.requestFocus();
                    return;
                }
                if(!find(R.id.cb_setuju_regist, CheckBox.class).isChecked()) {
                    showInfo("Silahkan Seujui Syarat Dan Ketentuan Aplikasi");
                    return;
                }
                //if(tvLokasi.getText().toString().equalsIgnoreCase(""))

//                    showInfo("Silahkan Isi Lokasi ");return;
//
                saveData();
                break;
            case R.id.btn_check_regist:
//                Intent i = new Intent(getActivity(), Referal_Activity.class);
//                startActivityForResult(i, REQUEST_REFEREAL);
                break;
            case R.id.tv_lokasi_regist:
                startActivityForResult(new Intent(getActivity(), LocationPicker_Activity.class), REQUEST_MAPS);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_MAPS){
            String latitude = data.getStringExtra("lat");
            String longitude = data.getStringExtra("lon");
            //to double
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            Log.d("cobacoba", "Lat : " + String.valueOf(lat) + "\n" +
                    "Long : " + String.valueOf(lon));
            String lokasi = "Lat : " + String.valueOf(lat) + "\n" +
                    "Long : " + String.valueOf(lon);

            tvLokasi.setText(lokasi);
        }
    }
}
