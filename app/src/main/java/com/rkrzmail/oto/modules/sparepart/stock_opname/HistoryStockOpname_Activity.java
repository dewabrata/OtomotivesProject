package com.rkrzmail.oto.modules.sparepart.stock_opname;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.user.AturUser_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class HistoryStockOpname_Activity extends AppActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_3);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History Stock Opname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent() {
        initToolbar();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), StockOpname_Activity.class), 10);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_history_stock_opname) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                 String tglMasuk = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("").asString());

                viewHolder.find(R.id.tv_lokasi_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_noFolder_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_namaPart_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_noPart_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_stock_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_merk_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_pending_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_opname_historyStock, TextView.class).setText(nListArray.get(position).get("").asString());

            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturUser_Activity.class);
                intent.putExtra("data", nListArray.get(position).toJson());
                startActivityForResult(intent, 10);
            }
        }));
        reload("");
    }

    private void reload(final String cari) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon di coba kembali");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                reload("");
            }
        }
    }
}
