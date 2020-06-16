package com.rkrzmail.oto.modules.jasa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.RupiahFormat;

import java.util.Map;

public class BiayaJasa_Activity extends AppActivity {

    private EditText etWaktu, etMekanik, etTotal, etBiaya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biaya_jasa_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_jasaLain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Biaya Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etWaktu = findViewById(R.id.et_waktuKerja_biayaJasa);
        etMekanik = findViewById(R.id.et_mekanik_biayaJasa);
        etTotal = findViewById(R.id.et_total_biayaJasa);
        etBiaya = findViewById(R.id.et_biaya_biayaJasa);

        etBiaya.addTextChangedListener(new RupiahFormat(etBiaya));

        find(R.id.btn_simpan_biayaJasa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }


    private void saveData() {
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
                    showInfo("GAGAL!");
                }
            }
        });
    }
}
