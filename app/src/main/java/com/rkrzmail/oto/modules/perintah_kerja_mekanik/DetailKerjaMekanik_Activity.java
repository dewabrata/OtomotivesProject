package com.rkrzmail.oto.modules.perintah_kerja_mekanik;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class DetailKerjaMekanik_Activity extends AppActivity {

    private EditText etNoAntrian, etJenis, etLayanan, etNopol, etNoKunci, etNamaPelanggan, etWaktu, etSelesai;
    private RecyclerView rvItem;
    private Nson partList = Nson.newArray(),
            jasaList = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kerja_mekanik);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_kerjaMekanik);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Perintah Kerja Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNoAntrian = findViewById(R.id.et_noAntrian_kerjaMekanik);
        etJenis = findViewById(R.id.et_jenis_kerjaMekanik);
        etLayanan = findViewById(R.id.et_layanan_kerjaMekanik);
        etNopol = findViewById(R.id.et_nopol_kerjaMekanik);
        etNoKunci = findViewById(R.id.et_noKunci_kerjaMekanik);
        etNamaPelanggan = findViewById(R.id.et_namaP_kerjaMekanik);
        etWaktu = findViewById(R.id.et_Ewaktu_kerjaMekanik);
        etSelesai = findViewById(R.id.et_Eselesai_kerjaMekanik);
        rvItem = findViewById(R.id.recyclerView_kerjaMekanik);

        loadData();
        rvItem.setLayoutManager(new LinearLayoutManager(this));
        rvItem.setHasFixedSize(true);
        rvItem.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
//                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("").asString());
//                viewHolder.find(R.id.tv_harga_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("").asString());
//                viewHolder.find(R.id.tv_disc_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("").asString());
//                viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("").asString());
            }
        });

        find(R.id.btn_lanjut_kerjaMekanik, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PointLayanan_Activity.class));
            }
        });
    }

    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra("data"));
        etNoAntrian.setText(n.get("").asString());
        etNopol.setText(n.get("").asString());
        etNoKunci.setText(n.get("").asString());
        etNamaPelanggan.setText(n.get("").asString());
        etWaktu.setText(n.get("").asString());
        etSelesai.setText(n.get("").asString());
        etJenis.setText(n.get("").asString());
        etLayanan.setText(n.get("").asString());
    }

    private void setSelanjutnya() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
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

}