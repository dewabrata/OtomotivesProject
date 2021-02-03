package com.rkrzmail.oto.modules.bengkel;

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
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KOMISI_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_SARAN;

public class AturSaran_Activity extends AppActivity {

    private EditText etJudul,etKet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_saran);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Saran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etJudul = findViewById(R.id.et_Judul_saran);
        etKet = findViewById(R.id.et_keterangan_Saran);
        find(R.id.btn_simpan_saran, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etJudul.getText().toString().isEmpty() && etKet.getText().toString().isEmpty()){
                    showInfo("Harap Lengkapi Form Saran");
                }else {
                    saveData();
                }
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("judulSaran", etJudul.getText().toString().toUpperCase());
                args.put("ketSaran", etKet.getText().toString().toUpperCase());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SARAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }
}