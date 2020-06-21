package com.rkrzmail.oto.modules.antar_jemput;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class SerahTerima_Activity extends AppActivity {

    private EditText etNama, etPembayaran, etKet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serah_terima);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Antar - Jemput");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();

        etNama = findViewById(R.id.et_namaPenerima_antarJemput);
        etPembayaran = findViewById(R.id.et_noPembayaran_antarJemput);
        etKet = findViewById(R.id.et_ket_antarJemput);

        Intent i = getIntent();
        if(i.hasExtra("antar")){
            find(R.id.ly_antar_antarJemput, LinearLayout.class).setVisibility(View.VISIBLE);
            find(R.id.cb_stnk_antarJemput, CheckBox.class).setText("STNK DI KEMBALIKAN");
            find(R.id.btn_simpan_antarJemput, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else if(i.hasExtra("jemput")){
            find(R.id.ly_jemput_antarJemput, LinearLayout.class).setVisibility(View.VISIBLE);
            find(R.id.cb_stnk_antarJemput, CheckBox.class).setText("STNK DI TERIMA");
            find(R.id.btn_simpan_antarJemput, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        find(R.id.seekBar_bbm, SeekBar.class).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekBarProgress = 20;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                find(R.id.tv_ket_bbm, TextView.class).setText(seekBarProgress + "%");
            }
        });
    }

    private void saveData(){
        Nson antar = Nson.readJson(getIntentStringExtra("antar"));
        Nson jemput = Nson.readJson(getIntentStringExtra("jemput"));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //args.put("nopol", nopol);
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
