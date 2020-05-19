package com.rkrzmail.oto.modules.layanan;

import android.content.Intent;
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
import com.naa.utils.InternetX;
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
    //private Button btn_simpan_atur_layanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_layanan_);

        initToolbar();
        initComponent();

        Intent i = getIntent();

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
                saveData();
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

                String jenisLayanan = sp_jenis_layanan.getSelectedItem().toString().toUpperCase();
                //String namaLayanan = sp_nama_layanan.getSelectedItem().toString().toUpperCase();
                String status = sp_status.getSelectedItem().toString().toUpperCase();
                //String namaPrincipal = sp_nama_principal.getSelectedItem().toString().toUpperCase();
                String biayaMin = et_biaya_minimal.getText().toString();
                String biayaLayanan = et_biaya_layanan.getText().toString();
                String discBooking = et_disc_booking.getText().toString();
                String dp = et_dp.getText().toString();
                String jasa1 = et_jasa_lain1.getText().toString();
                String jasa2 = et_jasa_lain2.getText().toString();
                String disc1 = et_disc_part1.getText().toString();
                String disc2 = et_disc_part2.getText().toString();
                String percent1 = et_percent1.getText().toString();
                String percent2 = et_percent2.getText().toString();

                if (find(R.id.layout_biaya, LinearLayout.class).isEnabled()) {
                    args.put("biaya", biayaLayanan);
                    args.put("dcbook", discBooking);
                    args.put("hdp", dp);
                }
                args.put("action", "update");
                args.put("jenisservice", jenisLayanan);
                //args.put("namalayanan", namaLayanan);
                args.put("status", status);
                //args.put("", namaPrincipal);

                args.put("", biayaMin);


                args.put("", jasa1);
                args.put("", jasa2);
                args.put("", disc1);
                args.put("", disc2);
                args.put("", percent1);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlayanan"),args)) ;
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    startActivity(new Intent(getActivity(), Layanan_Avtivity.class));
                } else {
                    showInfo("Gagal Menambahkan Layanan");
                }

            }
        });
    }
}
