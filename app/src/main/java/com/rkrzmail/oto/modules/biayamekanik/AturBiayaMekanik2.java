package com.rkrzmail.oto.modules.biayamekanik;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.RupiahFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class AturBiayaMekanik2 extends AppActivity {

    public static final String TAG = "AturBiayaMekanik2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_biaya_mekanik);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Biaya Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        final Nson data = Nson.readJson(getIntentStringExtra("USER"));
        final Intent i = getIntent();

        if (i.hasExtra("USER")) {
            find(R.id.et_upahMin_biayaMekanik, EditText.class).setText("Rp. " + data.get("UMK").asString());
            find(R.id.et_upahJam_biayaMekanik, EditText.class).setText("Rp. " + data.get("UPAH_MINIM").asString());
        }

        find(R.id.et_mekanik1_biayaMekanik, EditText.class).addTextChangedListener(new RupiahFormat(find(R.id.et_mekanik1_biayaMekanik, EditText.class)));
        find(R.id.et_mekanik2_biayaMekanik, EditText.class).addTextChangedListener(new RupiahFormat(find(R.id.et_mekanik2_biayaMekanik, EditText.class)));
        find(R.id.et_mekanik3_biayaMekanik, EditText.class).addTextChangedListener(new RupiahFormat(find(R.id.et_mekanik3_biayaMekanik, EditText.class)));
        find(R.id.et_upahMin_biayaMekanik, EditText.class).addTextChangedListener(new RupiahFormat(find(R.id.et_upahMin_biayaMekanik, EditText.class)));


        find(R.id.btn_simpan_biayaMekanik).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertdata();
            }
        });
    }

    private void insertdata() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String dateTime = simpleDateFormat.format(calendar.getTime());

                String waktu = find(R.id.et_upahMin_biayaMekanik, EditText.class).getText().toString();
                String mekanik1 = find(R.id.et_mekanik1_biayaMekanik, EditText.class).getText().toString();
                mekanik1 = mekanik1.trim().replace("Rp", " ");
                String mekanik2 = find(R.id.et_mekanik2_biayaMekanik, EditText.class).getText().toString();
                mekanik2 = mekanik2.trim().replace("Rp", " ");
                String mekanik3 = find(R.id.et_mekanik3_biayaMekanik, EditText.class).getText().toString();
                mekanik3 = mekanik3.trim().replace("Rp", " ");

                args.put("mekanik1", mekanik1);
                args.put("mekanik2", mekanik2);
                args.put("mekanik3", mekanik3);
                args.put("waktu", waktu);
                args.put("tanggal", dateTime);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbiayamekanik"), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    startActivity(new Intent(AturBiayaMekanik2.this, BiayaMekanik2Activity.class));
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }
}