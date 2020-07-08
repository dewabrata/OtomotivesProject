package com.rkrzmail.oto.modules.layanan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AturLayanan_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_DESKRIPSI = 12;
    private Spinner sp_jenis_layanan, sp_nama_principal, sp_nama_layanan, sp_status;
    private EditText et_biaya_minimal, et_biaya_layanan, et_disc_booking, et_dp, et_jasa_lain1,
            et_jasa_lain2, et_disc_part1, et_disc_part2, et_percent1, et_percent2, et_percent3, et_percent4, etPenggantian;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        etPenggantian = findViewById(R.id.et_penggantian_part);

        et_dp.addTextChangedListener(new RupiahFormat(et_dp));
        et_biaya_layanan.addTextChangedListener(new RupiahFormat(et_biaya_layanan));
        et_disc_booking.addTextChangedListener(new PercentFormat(et_disc_booking));
        et_percent1.addTextChangedListener(new PercentFormat(et_percent1));
        et_percent2.addTextChangedListener(new PercentFormat(et_percent2));
        et_percent3.addTextChangedListener(new PercentFormat(et_percent3));
        et_percent4.addTextChangedListener(new PercentFormat(et_percent4));

        setSpinnerFromApi(sp_nama_layanan, "", "", "viewlayanan", "NAMA_LAYANAN");

        find(R.id.btn_simpan_atur_layanan, Button.class).setOnClickListener(this);
        find(R.id.btn_deskripsi_aturLayanan, Button.class).setOnClickListener(this);

        sp_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("NON_ACTIVE")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_layanan, LinearLayout.class), false);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_layanan, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp_jenis_layanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                if (position == 0) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), false);
                }
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PAKET LAYANAN")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_namaPrincipal, LinearLayout.class), false);
                } else if (item.equalsIgnoreCase("OTOMOTIVES")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_namaPrincipal, LinearLayout.class), false);
                }else{
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), false);
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_namaPrincipal, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveData(){
        final String jenisLayanan = sp_jenis_layanan.getSelectedItem().toString().toUpperCase();
        final String namaLayanan = sp_nama_layanan.getSelectedItem().toString().toUpperCase();
        final String status = sp_status.getSelectedItem().toString().toUpperCase();
//                String namaPrincipal;
//                if(sp_nama_principal.getSelectedItemPosition() != 0){
//                    namaPrincipal = sp_nama_principal.getSelectedItem().toString().toUpperCase();
//                }else{
//                    namaPrincipal = null;
//                }
        String biayaMin = et_biaya_minimal.getText().toString();
        final String biayaLayanan = et_biaya_layanan.getText().toString();
        final String discBooking = et_disc_booking.getText().toString();
        final String dp = et_dp.getText().toString();
        final String jasaLain1 = et_jasa_lain1.getText().toString();
        final String jasaLain2 = et_jasa_lain2.getText().toString();
        final String disc1 = et_disc_part1.getText().toString();
        final String disc2 = et_disc_part2.getText().toString();
        String percent1 = et_percent1.getText().toString();
        String percent2 = et_percent2.getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                CID, action (update), namalayanan (4 jenis nama layanan, capslock, wajib),
//                jenisservice, id, status (kalo nama layanan otomotif)
//                CID, action (update), namalayanan (4 jenis nama layanan, capslock, wajib),
//                jenisservice, id, status, principal (kalo nama layanan recall/fasilitas)
//                CID, action (update), namalayanan (4 jenis nama layanan, capslock, wajib),
//                jenisservice, id, status, biaya, dcbook, hpd, layananpartid, layananaktifitasid,
//                diskonjasa,diskonpart (kalo nama layanan paketlayanan)
                args.put("action", "update");
                if (jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    args.put("biaya", biayaLayanan);
                    args.put("dcbook", discBooking);
                    args.put("hpd", dp);
                    args.put("jenisservice", jenisLayanan);
                    args.put("layananpartid", jasaLain1);
                    args.put("diskanjasa", disc1);
                    args.put("diskonpart", disc2);
                    args.put("layananaktifitasid", jasaLain2);
                    args.put("namalayanan", namaLayanan);
                    args.put("status", status);
                } else if (jenisLayanan.equalsIgnoreCase("FASILITAS") && jenisLayanan.equalsIgnoreCase("RECALL")) {
                    // args.put("principal", namaPrincipal);
                    args.put("jenisservice", jenisLayanan);
                    args.put("namalayanan", namaLayanan);
                    args.put("status", status);
                } else if (jenisLayanan.equalsIgnoreCase("OTOMOTIVES")) {
                    args.put("namalayanan", namaLayanan);
                    args.put("jenisservice", jenisLayanan);
                    args.put("status", status);
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlayanan"),args)) ;
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    showInfo("Sukses Menambahkan Layanan");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal Menambahkan Layanan");
                }

            }
        });
    }

    private void getDeskripsiLayanan() {
        final String jenisLayanan = sp_jenis_layanan.getSelectedItem().toString().toUpperCase();
        final String namaLayanan = sp_nama_layanan.getSelectedItem().toString().toUpperCase();
        String status = sp_status.getSelectedItem().toString().toUpperCase();
//                String namaPrincipal;
//                if(sp_nama_principal.getSelectedItemPosition() != 0){
//                    namaPrincipal = sp_nama_principal.getSelectedItem().toString().toUpperCase();
//                }else{
//                    namaPrincipal = null;
//                }
        String biayaMin = et_biaya_minimal.getText().toString();
        String biayaLayanan = et_biaya_layanan.getText().toString();
        String discBooking = et_disc_booking.getText().toString();
        String dp = et_dp.getText().toString();
        String jasaLain1 = et_jasa_lain1.getText().toString();
        String jasaLain2 = et_jasa_lain2.getText().toString();
        String disc1 = et_disc_part1.getText().toString();
        String disc2 = et_disc_part2.getText().toString();
        String percent1 = et_percent1.getText().toString();
        String percent2 = et_percent2.getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                Nson nson = Nson.newObject();
                nson.set("namalayanan", namaLayanan);
                nson.set("jenislayanan", jenisLayanan);
                nson.set("pelaksana", "");
                nson.set("lokasi", "");
                nson.set("kendaraan", "");
                nson.set("merk", "");
                nson.set("model", "");
                nson.set("jenis", "");
                nson.set("varian", "");
                nson.set("batasanfasilitas", "");
                nson.set("biayajasa", "");
                nson.set("garansilayanan", "");
                nson.set("penggantian", "");
                nson.set("expiration", "");
                nson.set("norangka", "");
                nson.set("nomesin", "");
                nson.set("waktulayanan", "");
                nson.set("jenisantrian", "");
                nson.set("waktuinspection", "");
                nson.set("finalinspection", "");
                Intent i = new Intent(getActivity(), DeskripsiLayanan_Activiy.class);
                i.putExtra("deskripsi", nson.toJson());
                startActivityForResult(i, REQUEST_DESKRIPSI);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_simpan_atur_layanan:
                saveData();
                break;
            case R.id.btn_deskripsi_aturLayanan:
                getDeskripsiLayanan();
                break;
        }
    }
}
