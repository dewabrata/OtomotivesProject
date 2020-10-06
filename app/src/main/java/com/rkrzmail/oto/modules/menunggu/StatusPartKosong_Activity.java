package com.rkrzmail.oto.modules.menunggu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class StatusPartKosong_Activity extends AppActivity {

    private RecyclerView rvPartKosong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        FloatingActionButton fab = findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), UpdatePartKosong_Activity.class));
                finish();
            }
        });

        rvPartKosong = findViewById(R.id.recyclerView);
        rvPartKosong.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPartKosong.setHasFixedSize(true);
        rvPartKosong.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_kosong) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tglDisc = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("").asString());

                viewHolder.find(R.id.tv_tgl_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_user_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_status_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_ket_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());


            }
        });
        catchData();
    }

    private void catchData() {
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
                    rvPartKosong.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo("Gagal Meload Aktifitas");
                }
            }
        });
    }
}
