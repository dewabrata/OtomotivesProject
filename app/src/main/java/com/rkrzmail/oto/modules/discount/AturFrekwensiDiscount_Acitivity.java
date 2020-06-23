package com.rkrzmail.oto.modules.discount;

import android.content.Intent;
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

public class AturFrekwensiDiscount_Acitivity extends AppActivity implements View.OnClickListener {

    private TextView tvTgl;
    private EditText etFrekwensi, etDisc;
    private Spinner spLayanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_frekwensi_discount);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_freDisc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Frekwensi Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        Nson nson = Nson.readJson(getIntentStringExtra(""));
        Intent intent = getIntent();

        if (intent.hasExtra("")) {
            find(R.id.btn_hapus_freDisc, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_freDisc, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteData();
                }
            });
        }

        tvTgl = findViewById(R.id.tv_tglEffect_freDisc);
        etDisc = findViewById(R.id.et_disc_freDisc);
        etFrekwensi = findViewById(R.id.et_frekwensi_freDisc);

        etDisc.addTextChangedListener(new PercentFormat(etDisc));
        tvTgl.setOnClickListener(this);
        find(R.id.btn_simpan_freDisc, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catchData();
            }
        });
    }

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("referal"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Gagal");
                }
            }
        });
    }

    private void deleteData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("referal"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Gagal");
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_freDisc:
                Tools.getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }
}
