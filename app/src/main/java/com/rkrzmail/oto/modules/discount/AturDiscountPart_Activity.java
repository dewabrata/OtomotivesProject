package com.rkrzmail.oto.modules.discount;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class AturDiscountPart_Activity extends AppActivity implements View.OnClickListener {
    private Spinner spPekerjaan;
    private EditText etDiscPart, etDiscJasa, etNoPart, etNamaPart;
    private TextView tvTgl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_part_);

        initComponent();
    }

    private void initToolbar() {
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_discPart);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        spPekerjaan = findViewById(R.id.sp_pekerjaan_disc);
        etDiscPart = findViewById(R.id.et_discPart_disc);
        etDiscJasa = findViewById(R.id.et_discJasa_disc);
        etNoPart = findViewById(R.id.et_noPart_disc);
        etNamaPart = findViewById(R.id.et_namaPart_disc);
        tvTgl = findViewById(R.id.tv_tglEffect_discPart);

        tvTgl.setOnClickListener(this);
        etDiscJasa.addTextChangedListener(new PercentFormat(etDiscJasa));
        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));

        find(R.id.btn_simpanPart_disc, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("spotdisc"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(getActivity(), SpotDiscount_Activity.class));
                } else {
                    showInfo("GAGAL");
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discPart:
                Tools.getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }
}
