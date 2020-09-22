package com.rkrzmail.oto.modules.sparepart.terima_part;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

public class DetailTerimaPart_Activity extends AppActivity {

    private EditText etNamaSup, etUser, etTglTerima, etTglPesan, etNodo, etPembayaran, etInv, etOngkir;
    private RecyclerView rvTerimaPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_terima_part_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Detail Terima Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNamaSup = findViewById(R.id.et_namaSup_terimaPart);
        etUser = findViewById(R.id.et_user_terimaPart);
        etTglTerima = findViewById(R.id.et_tglTerima_terimaPart);
        etTglPesan = findViewById(R.id.et_tglPesan_terimaPart);
        etNodo = findViewById(R.id.et_nodo_terimaPart);
        etPembayaran = findViewById(R.id.et_pembayaran_terimaPart);
        etInv = findViewById(R.id.et_tempoInv_terimaPart);
        etOngkir = findViewById(R.id.et_ongkir_terimaPart);
        rvTerimaPart = findViewById(R.id.recyclerView_detailTerimaPart);

        loadData();

        rvTerimaPart.setLayoutManager(new LinearLayoutManager(this));
        rvTerimaPart.setHasFixedSize(true);
        rvTerimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_di_terima) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_noPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_namaPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_jumlah_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.tv_net_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NET").asString());
                viewHolder.find(R.id.tv_harga_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("HARGA_BELI").asString());
                viewHolder.find(R.id.tv_disc_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("DISCOUNT").asString());
                viewHolder.find(R.id.tv_merk_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
            }
        });
    }

    private void loadData() {
        Intent i = getIntent();
        Nson n = Nson.readJson(getIntentStringExtra(i, "part"));
        if(n.get("SUPPLIER").asString().equalsIgnoreCase("")){
            etNamaSup.setVisibility(View.GONE);
        }
        etNamaSup.setText(n.get("SUPPLIER").asString());
        etUser.setText(n.get("NAMA_USER").asString());
        etTglTerima.setText(n.get("TANGGAL_PENERIMAAN").asString());
        etTglPesan.setText(n.get("TANGGAL_PESAN").asString());
        etNodo.setText(n.get("NO_DO").asString());
        etPembayaran.setText(n.get("PEMBAYARAN").asString());
        etInv.setText(n.get("JATUH_TEMPO_INV ").asString());
        etOngkir.setText(n.get("ONGKOS_KIRIM").asString());

        nListArray.add(Nson.readJson(getIntentStringExtra(i, "part")));
    }
}
