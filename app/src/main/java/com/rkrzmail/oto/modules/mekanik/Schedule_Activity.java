package com.rkrzmail.oto.modules.mekanik;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Map;

public class Schedule_Activity extends AppActivity {

    public static final String TAG = "PenugasanActivity";
    private RecyclerView rvPenugasan;
    private static final int REQUEST_PENUGASAN = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penugasan);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_tugas);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturSchedule_Activity.class), 10);
            }
        });

        rvPenugasan = (RecyclerView) findViewById(R.id.recyclerView_penugasan);
        rvPenugasan.setLayoutManager(new LinearLayoutManager(this));
        rvPenugasan.setHasFixedSize(true);
        rvPenugasan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_penugasan) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tvNamaMekanik, TextView.class).setText(nListArray.get(position).get("NAMA_MEKANIK").asString());
                viewHolder.find(R.id.tvAntrian, TextView.class).setText(nListArray.get(position).get("TIPE_ANTRIAN").asString());
                viewHolder.find(R.id.tvLokasi, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tvStart, TextView.class).setText(nListArray.get(position).get("JAM_MASUK").asString());
                viewHolder.find(R.id.tvFinish, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturSchedule_Activity.class);
                intent.putExtra("data", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        }));
        catchData();
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpenugasanmekanik"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvPenugasan.getAdapter().notifyDataSetChanged();
                } else {
                    Log.d(TAG, "error");
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_PENUGASAN){
            catchData();
        }else if(resultCode == RESULT_OK && requestCode == 10){
            catchData();
        }
    }
}
