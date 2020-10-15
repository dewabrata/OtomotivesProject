package com.rkrzmail.oto.modules.mekanik;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.DetailKontrolLayanan_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PerintahKerjaMekanik_Activity extends AppActivity {

    private RecyclerView rvKerjaMekanik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perintah_kerja_mekanik_);
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
        rvKerjaMekanik = findViewById(R.id.recyclerView_kerjaMekanik);
        rvKerjaMekanik.setLayoutManager(new LinearLayoutManager(this));
        rvKerjaMekanik.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_kerja_mekanik) {
                    @Override
                    public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, final int position) {
                        super.onBindViewHolder(viewHolder, position);

                        viewHolder.find(R.id.tv_jenis_kerjaMekanik, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_nopol_kerjaMekanik, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_layanan_kerjaMekanik, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_namaP_kerjaMekanik, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_noAntrian_kerjaMekanik, TextView.class).setText(nListArray.get(position).get("").asString());

                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), DetailKontrolLayanan_Activity.class);
                        i.putExtra("data", nListArray.get(position).toJson());
                        startActivity(i);
                    }
                })
        );
        catchData("");
    }

    private void catchData(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvKerjaMekanik.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }
}
