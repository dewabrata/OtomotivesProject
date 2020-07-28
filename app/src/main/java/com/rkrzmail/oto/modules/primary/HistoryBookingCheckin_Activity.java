package com.rkrzmail.oto.modules.primary;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
        getSupportActionBar().setTitle("History Booking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        final String nopol = getIntentStringExtra("NOPOL");
        initToolbar();
        rvHistory = findViewById(R.id.recyclerView);

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("history", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvHistory.getAdapter().notifyDataSetChanged();
                    Log.d("Booking2List", "" +  result.get("data"));
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setHasFixedSize(true);
        rvHistory.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_history_booking) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                String date = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("TANGGAL_IN").asString());

                viewHolder.find(R.id.tv_status_historyBooking, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_ket_historyBooking, TextView.class).setText(nListArray.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.tv_jam_historyBooking, TextView.class).setText(nListArray.get(position).get("JAM_BOOKING").asString());
                viewHolder.find(R.id.tv_user_historyBooking, TextView.class).setText(nListArray.get(position).get("CREATED_USER").asString());
                viewHolder.find(R.id.tv_tgl_historyBooking, TextView.class).setText(date);
            }
        });
    }
}
