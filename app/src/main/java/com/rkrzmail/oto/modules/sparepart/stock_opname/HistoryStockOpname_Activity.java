package com.rkrzmail.oto.modules.sparepart.stock_opname;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_STOCK_OPNAME;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_TERALOKASIKAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_OPNAME;

public class HistoryStockOpname_Activity extends AppActivity {

    private RecyclerView recyclerView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
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
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getActivity(), CariPart_Activity.class);
                intent.putExtra(CARI_PART_TERALOKASIKAN, "");
                startActivityForResult(intent, REQUEST_CARI_PART);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_history_stock_opname) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tv_lokasi_historyStock, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_noFolder_historyStock, TextView.class).setText(nListArray.get(position).get("NO_FOLDER").asString());
                viewHolder.find(R.id.tv_namaPart_historyStock, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_historyStock, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_stock_historyStock, TextView.class).setText(nListArray.get(position).get("STOCK_LOKASI_PART").asString());
                viewHolder.find(R.id.tv_merk_historyStock, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_pending_historyStock, TextView.class).setText(nListArray.get(position).get("PENDING_STOCK").asString());
                viewHolder.find(R.id.tv_opname_historyStock, TextView.class).setText(nListArray.get(position).get("OPNAME").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                /*intent = new Intent(getActivity(), CariPart_Activity.class);
                intent.putExtra(CARI_PART_LOKASI, nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_OPNAME);*/
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_STOCK_OPNAME), args));
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
        if (resultCode == RESULT_OK && requestCode == REQUEST_OPNAME) {
            reload("");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            intent = new Intent(getActivity(), AturStockOpname_Activity.class);
            intent.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
            startActivityForResult(intent, REQUEST_OPNAME);
        }
    }
}
