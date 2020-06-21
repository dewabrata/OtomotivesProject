package com.rkrzmail.oto.modules.message;

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

import java.util.Map;

public class ManualMessage_Activity extends AppActivity {

    private EditText etKendaraan, etNohp, etMssg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_message);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manual Message");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        etKendaraan = findViewById(R.id.et_kendaraan_mssg);
        etNohp = findViewById(R.id.et_noHp_mssg);
        etMssg = findViewById(R.id.et_mssg_mssg);

        find(R.id.sp_pekerjaan_mssg);
        find(R.id.sp_penerima_mssg);
        find(R.id.btn_simpan_mssg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void saveData() {
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
