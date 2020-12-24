package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class HistoryBookingCheckin_Activity extends AppActivity {

    private RecyclerView rvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History Kendaraan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        initRecylerview();
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (data != null) {
            nListArray.asArray().clear();
            nListArray.asArray().addAll(data.asArray());
            rvHistory.getAdapter().notifyDataSetChanged();
        }
    }


    private void initRecylerview() {
        rvHistory = findViewById(R.id.recyclerView);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setHasFixedSize(true);
        rvHistory.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_history_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                String date = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("CREATED_DATE").asString());

                viewHolder.find(R.id.tv_tgl_historyCheckin, TextView.class).setText(nListArray.get(position).get("CREATED_DATE").asString());
                viewHolder.find(R.id.tv_km_historyCheckin, TextView.class).setText(nListArray.get(position).get("KM").asString());
                viewHolder.find(R.id.tv_namaLayanan_historyCheckin, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                viewHolder.find(R.id.tv_pelanggan, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_total_biaya, TextView.class).setText(RP + formatRp(nListArray.get(position).get("TOTAL_BIAYA").asString()));

            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
        super.onBackPressed();
    }
}
