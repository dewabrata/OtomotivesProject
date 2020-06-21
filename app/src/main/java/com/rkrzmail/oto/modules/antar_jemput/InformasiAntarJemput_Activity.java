package com.rkrzmail.oto.modules.antar_jemput;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class InformasiAntarJemput_Activity extends AppActivity {

    private EditText etNamaP, etNohp, etAlamat, etNopol, etJenisKendaraan, etTgl, etJam;
    private Spinner spKondisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informasi_antar_jemput_);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Antar - Jemput");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        etNamaP = findViewById(R.id.et_namaP_antarJemput);
        etNohp = findViewById(R.id.et_noHp_antarJemput);
        etAlamat = findViewById(R.id.et_alamat_antarJemput);
        etNopol = findViewById(R.id.et_nopol_antarJemput);
        etJenisKendaraan = findViewById(R.id.et_jenisKendaraan_antarJemput);
        etTgl = findViewById(R.id.et_tgl_antarJemput);
        etJam = findViewById(R.id.et_jam_antarJemput);
        spKondisi = findViewById(R.id.sp_kondisiKendaraan_antarJemput);

        find(R.id.btn_simpan_antarJemput).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private void loadData(){
        Nson n = Nson.readJson(getIntentStringExtra("data"));

        etNamaP.setText(n.get("").asString());
        etNohp.setText(n.get("").asString());
        etAlamat.setText(n.get("").asString());
        etNopol.setText(n.get("").asString());
        etJenisKendaraan.setText(n.get("").asString());
        etTgl.setText(n.get("").asString());
        etJam.setText(n.get("").asString());
        spKondisi.setSelection(Tools.getIndexSpinner(spKondisi, n.get("").asString()));

    }

    private void setSelanjutnya() {
        //Nson n = Nson.newObject();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                args.put("action", "view");
//                args.put("search", cari);
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
