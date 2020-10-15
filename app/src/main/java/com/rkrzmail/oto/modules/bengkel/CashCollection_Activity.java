package com.rkrzmail.oto.modules.bengkel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class CashCollection_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_collection_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cash Collection");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        find(R.id.et_total_cashCollection);
        find(R.id.et_sisa_cashCollection);
        find(R.id.et_namaKasir_cashCollection);
        find(R.id.et_terhutang_cashCollection);
        find(R.id.et_noTrack_cashCollection);

        find(R.id.sp_tipe_cashCollection, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String items = parent.getItemAtPosition(position).toString();
                if(items.equalsIgnoreCase("CASH")){
                    find(R.id.et_noTrack_cashCollection).setEnabled(false);
                    find(R.id.sp_noRek_cashCollection).setEnabled(false);
                }else{
                    find(R.id.et_noTrack_cashCollection).setEnabled(true);
                    find(R.id.sp_noRek_cashCollection).setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        find(R.id.sp_noRek_cashCollection, Spinner.class);


        find(R.id.btn_simpan_cashCollection, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void loadData(){
        Nson n = Nson.readJson(getIntentStringExtra("data"));
        find(R.id.et_namaKasir_cashCollection, EditText.class).setText(n.get("").asString());
        find(R.id.et_terhutang_cashCollection, EditText.class).setText(n.get("").asString());
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }
}
