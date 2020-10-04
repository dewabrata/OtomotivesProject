package com.rkrzmail.oto.modules.jasa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_JASA_LAIN_BERKALA;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BIAYA;

public class JasaLainBerkala_Activity extends AppActivity {

    private RecyclerView rvJasaBerkala;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jasa Lain Berkala");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        //Nson nsonss = Nson.readJson(getIntentStringExtra("KM_SEKARANG"));
        //Log.d("JASALAINBERKALAAAA", "km :" + getIntent().getStringExtra("KM_SAAT_INI"));
        initToolbar();
        initRecylerview();
        catchData();
    }

    private void initRecylerview() {
        rvJasaBerkala = findViewById(R.id.recyclerView);
        rvJasaBerkala.setLayoutManager(new LinearLayoutManager(this));
        rvJasaBerkala.setHasFixedSize(true);
        rvJasaBerkala.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jasa_lain_berkala) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_masterPart_jasaBerkala, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_aktifitas_jasaBerkala, TextView.class).setText(nListArray.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_kmSaatIni_jasaBerkala, TextView.class).setText(nListArray.get(position).get("KM").asString());
                viewHolder.find(R.id.tv_waktu_jasaBerkala, TextView.class).setText(nListArray.get(position).get("WAKTU").asString());
                viewHolder.find(R.id.tv_kmPenggantian_jasaBerkala, TextView.class).setText(nListArray.get(position).get("KM_PENGGANTIAN").asString());
                viewHolder.find(R.id.tv_mekanik_jasaBerkala, TextView.class).setText(nListArray.get(position).get("TYPE_MEKANIK").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), BiayaJasa_Activity.class);
                i.putExtra("jasa_berkala", "");
                i.putExtra(DATA, nListArray.get(position).toJson());
                startActivityForResult(i, REQUEST_BIAYA);
            }
        }));
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JASA_LAIN_BERKALA), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    String km = getIntent().getStringExtra("KM_SAAT_INI");
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    nListArray.set("KM", km);
                    rvJasaBerkala.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BIAYA) {
            Intent i = new Intent();
            i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            Log.d(VIEW_JASA_LAIN_BERKALA, "SEND : " + Nson.readJson(getIntentStringExtra(data, "data")));
            setResult(RESULT_OK, i);
            finish();
        }
    }
}
