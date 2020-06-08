package com.rkrzmail.oto.modules.sparepart.tugas_part;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class DetailTugasPart_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvDetailTugasPart;
    private EditText etLayanan, etPelanggan, etNopol, etMekanik, etDiterima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tugas_part_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail_tugasPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tugas Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etLayanan = findViewById(R.id.et_layanan_tugasPart);
        etPelanggan = findViewById(R.id.et_pelanggan_tugasPart);
        etNopol = findViewById(R.id.et_nopol_tugasPart);
        etMekanik = findViewById(R.id.et_mekanik_tugasPart);
        etDiterima = findViewById(R.id.et_diterima_tugasPart);

        rvDetailTugasPart = findViewById(R.id.recyclerView_tugasPart);
        rvDetailTugasPart.setLayoutManager(new LinearLayoutManager(this));
        rvDetailTugasPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_tugas_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_noFolder_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_namaPart_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_noPart_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_stock_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_jumlah_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                    }
                })
        );
        find(R.id.btn_simpan_tugasPart).setOnClickListener(this);
    }

    private void catchData() {
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

    private void saveData() {
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_simpan_tugasPart) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

        }
    }
}
