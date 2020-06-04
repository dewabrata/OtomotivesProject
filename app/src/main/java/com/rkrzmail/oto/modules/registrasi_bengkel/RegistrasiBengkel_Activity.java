package com.rkrzmail.oto.modules.registrasi_bengkel;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.ArrayList;
import java.util.Map;

public class RegistrasiBengkel_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_REFEREAL = 56;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_register);
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

        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");
        setMultiSelectionSpinnerFromApi(spKendaraan, "nama", "BENGKEL", "viewmst", "TYPE", dummies);


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

                args.put("action", "add");
                args.put("nohp", etNoPonsel.getText().toString());
                args.put("nama", etNamaPemilik.getText().toString());
                args.put("email", etEmail.getText().toString());
                args.put("namabengkel", etNamaBengkel.getText().toString());
                //args.put("jenis", spKendaraan.getSelectedItem().toString());
                //args.put("kategori", spBidangUsaha.getSelectedItem().toString());
                args.put("alamat", etAlamat.getText().toString());
                args.put("daerah", etKotaKab.getText().toString());
                //args.put("lokasi", tvLokasi.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("registrasi"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Registrasi Berhasil");
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
        switch (view.getId()) {
            case R.id.btn_simpan_regist:
                if (etNoPonsel.getText().toString().isEmpty() && etNamaPemilik.getText().toString().isEmpty()
                        && etNamaBengkel.getText().toString().isEmpty() && etKotaKab.getText().toString().isEmpty()
                        && etEmail.getText().toString().isEmpty() && etAlamat.getText().toString().isEmpty()
                        && tvLokasi.getText().toString().equalsIgnoreCase("")) {
                    showInfo("Data Tidak Lengkap");
                    return;
                }
                saveData();

                break;
            case R.id.btn_check_regist:
//                Intent i = new Intent(getActivity(), Referal_Activity.class);
//                startActivityForResult(i, REQUEST_REFEREAL);
                break;
            case R.id.tv_lokasi_regist:

                break;
        }
    }
}
