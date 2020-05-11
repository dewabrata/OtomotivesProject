package com.rkrzmail.oto.modules.layanan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class AturLayanan_Activity extends AppActivity {

    private Spinner sp_jenis_layanan, sp_nama_principal, sp_nama_layanan, sp_status;
    private EditText et_biaya_minimal, et_biaya_layanan, et_disc_booking, et_dp, et_jasa_lain1,
            et_jasa_lain2, et_disc_part1, et_disc_part2, et_percent1, et_percent2, et_percent3, et_percent4;
    private Button btn_simpan_atur_layanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_layanan_);

        initToolbar();
        initComponent();


    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_layanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent(){
        sp_jenis_layanan = findViewById(R.id.sp_jenis_layanan);
        sp_nama_layanan = findViewById(R.id.sp_nama_layanan);
        sp_status = findViewById(R.id.sp_status_layanan);
        sp_nama_principal = findViewById(R.id.sp_nama_principal);
        et_biaya_layanan = findViewById(R.id.et_biaya_layanan);
        et_biaya_minimal = findViewById(R.id.et_biaya_minimal);
        et_disc_booking =  findViewById(R.id.et_disc_booking);
        et_dp = findViewById(R.id.et_dp_layanan);
        et_jasa_lain1 = findViewById(R.id.et_jasa_lain1);
        et_jasa_lain2 = findViewById(R.id.et_jasa_lain2);
        et_disc_part1 = findViewById(R.id.et_disc_part1);
        et_disc_part2 = findViewById(R.id.et_disc_part2);
        et_percent1 = findViewById(R.id.et_percent1);
        et_percent2 = findViewById(R.id.et_percent2);
        et_percent3 = findViewById(R.id.et_percent3);
        et_percent4 = findViewById(R.id.et_percent4);

        find(R.id.btn_simpan_atur_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        sp_jenis_layanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();

                if (item.equalsIgnoreCase("PAKET LAYANAN")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), true);

                    if(item.equalsIgnoreCase("OTOMOTOVIES") && item.equalsIgnoreCase("PAKET LAYANAN")){
                        sp_nama_principal.setEnabled(false);
                    }else {
                        sp_nama_principal.setEnabled(true);
                    }

                }else{
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveData(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){

                }

            }
        });



    }
}
