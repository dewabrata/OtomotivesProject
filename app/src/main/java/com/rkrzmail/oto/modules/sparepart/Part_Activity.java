package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

public class Part_Activity extends AppActivity {

    private RecyclerView rvPart;
    private static final int REQUEST_CHECKIN = 12;
    private static final int REQUEST_BOOKING = 13;
    private static final int REQUEST_HARGA = 14;
    private static final int REQUEST_CARI_PART = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvPart = findViewById(R.id.recyclerView);

        nListArray.add(Nson.readJson(getIntentStringExtra("data")));

        rvPart.setLayoutManager(new LinearLayoutManager(this));
        rvPart.setHasFixedSize(true);
        rvPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_namaPart_part_partExternal, TextView.class).setText(nListArray.get(position).get("NAMA_PABRIKAN").asString());
                viewHolder.find(R.id.tv_noPart_part_partExternal, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_stock_part_partExternal, TextView.class).setText(nListArray.get(position).get("STOCK").asString());
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent();
                intent.putExtra("data", nListArray.get(position).toJson());
                setResult(RESULT_OK, intent);
                finish();
            }
        }));

    }

}
