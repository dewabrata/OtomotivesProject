package com.rkrzmail.oto.modules.collection;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class NonCashCollection_Activity extends AppActivity {

    private RecyclerView rvColl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_2);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Non-Cash Collection");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvColl = findViewById(R.id.recyclerView);
        rvColl.setLayoutManager(new LinearLayoutManager(this));
        rvColl.setHasFixedSize(true);
        rvColl.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_kosong) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                //tanggal = tgl, status = notrack, user = bank, jumlah = ket
                viewHolder.find(R.id.tv_tgl_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_user_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_status_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_ket_partKosong, TextView.class).setText(nListArray.get(position).get("").asString());

            }

            @Override
            public int getItemCount() {
                if (nListArray.size() == 0) {
                    find(R.id.btn_simpan).setVisibility(View.GONE);
                } else {
                    find(R.id.btn_simpan).setVisibility(View.VISIBLE);

                }
                return super.getItemCount();
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
                    rvColl.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }
}
