package com.rkrzmail.oto.modules.registrasi_bengkel;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class ReferensikanOtomotives_Activity extends AppActivity implements View.OnClickListener {

    private EditText etNamaPemilik, etNoPonsel, etEmail, etNamaBengkel, etAlamat;
    private NikitaAutoComplete etKotaKab;
    private Spinner spBidangUsaha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referensikan_otomotives_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_refOto);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Referensikan Otomotives");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etAlamat = findViewById(R.id.et_alamat_ref);
        etEmail = findViewById(R.id.et_email_ref);
        etKotaKab = findViewById(R.id.et_kotakab_ref);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_ref);
        etNamaPemilik = findViewById(R.id.et_namaPemilik_ref);
        etNoPonsel = findViewById(R.id.et_noPhone_ref);
        spBidangUsaha = findViewById(R.id.sp_usaha_ref);


        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");
        find(R.id.btn_simpan_ref, Button.class).setOnClickListener(this);

    }

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("registrasi"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Registrasi Gagal, Silahkan Cek Data Anda Kembali");
                }
            }
        });
    }

    private void componentValidation() {
        String namaBengkel = etNamaBengkel.getText().toString();
        String namaPemilik = etNamaPemilik.getText().toString();
        String alamat = etAlamat.getText().toString();
        String email = etEmail.getText().toString();
        String ponsel = etNoPonsel.getText().toString();
        String kotaKab = etKotaKab.getText().toString();
        if (namaBengkel.isEmpty() && namaPemilik.isEmpty() && alamat.isEmpty() && email.isEmpty() && ponsel.isEmpty() && kotaKab.isEmpty()) {
            showInfo("Silahkan Lengkapi Data Anda");
        } else {
            catchData();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_simpan_ref:
                componentValidation();
                break;
        }
    }
}
