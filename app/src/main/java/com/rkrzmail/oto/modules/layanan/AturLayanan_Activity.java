package com.rkrzmail.oto.modules.layanan;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AturLayanan_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_DESKRIPSI = 12;
    public static final int REQUEST_DISC_PART = 13;
    public static final int REQUEST_JASA_LAIN = 14;
    private Spinner sp_jenis_layanan, sp_nama_principal, sp_nama_layanan, sp_status;
    private RecyclerView rvLayanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_layanan_);
        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        sp_jenis_layanan = findViewById(R.id.sp_jenis_layanan);
        sp_nama_layanan = findViewById(R.id.sp_nama_layanan);
        sp_status = findViewById(R.id.sp_status_layanan);
        sp_nama_principal = findViewById(R.id.sp_nama_principal);
        rvLayanan = findViewById(R.id.recyclerView_layanan);

        //find(R.id.et_ket_layanan, EditText.class).setText();
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
                    //Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_namaPrincipal, LinearLayout.class), false);
                } else if (item.equalsIgnoreCase("OTOMOTIVES")) {
                    //Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_namaPrincipal, LinearLayout.class), false);
                }else{
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), false);
                    //Tools.setViewAndChildrenEnabled(find(R.id.layout_biaya, LinearLayout.class), false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_namaPrincipal, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_discPart_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TambahLayanan.class);
                i.putExtra("disc_part", "disc_part");
                startActivityForResult(i, REQUEST_DISC_PART);
            }
        });

        find(R.id.btn_jasaLain_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), TambahLayanan.class);
                i.putExtra("jasa_lain", "jasa_lain");
                startActivityForResult(i, REQUEST_JASA_LAIN);
            }
        });

        initRecylerview();
    }

    private void initRecylerview(){
        rvLayanan.setHasFixedSize(true);
        rvLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_komisi_jasa_lain){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaLayanan_layanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());
                viewHolder.find(R.id.tv_discAktivitas_layanan, TextView.class).setText(nListArray.get(position).get("DISC_AKTIVITAS").asString());
                viewHolder.find(R.id.tv_discWaktuKerja_layanan, TextView.class).setText(nListArray.get(position).get("DISC_WAKTU_KERJA").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                    }
                })
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_DISC_PART){
                nListArray.add(getIntentStringExtra(data, "data"));
                rvLayanan.getAdapter().notifyDataSetChanged();
            }else if(requestCode == REQUEST_JASA_LAIN){
                nListArray.add(getIntentStringExtra(data, "data"));
                rvLayanan.getAdapter().notifyDataSetChanged();
            }
        }
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
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                if (jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
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
