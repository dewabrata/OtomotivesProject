package com.rkrzmail.oto.modules.discount;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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

public class DiscountPart_Activity extends AppActivity {

    private RecyclerView rvDiscPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_part_);
        initToolbar();
        initComponent();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_discPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = findViewById(R.id.fab_tambah_discountPart);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AturDiscountPart_Activity.class));
            }
        });

        rvDiscPart = findViewById(R.id.recyclerView_discount_part);
        rvDiscPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDiscPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_discount_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tglDisc = Tools.setFormatDayAndMonth(nListArray.get(position).get("").asString());

                viewHolder.find(R.id.tv_noPart_disc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_namaPart_disc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_hpp_disc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_pekerjaan_disc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_discount_disc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_tgl_disc, TextView.class).setText(tglDisc);

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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("spotdiscount"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Gagal");
                }
            }
        });
    }
}