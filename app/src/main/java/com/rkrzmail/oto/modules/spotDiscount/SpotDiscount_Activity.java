package com.rkrzmail.oto.modules.spotDiscount;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class SpotDiscount_Activity extends AppActivity {

    private RecyclerView rvDisc;
    private NikitaAutoComplete cariDiskon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_discount_);
        initToolbar();
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_spotDiscount);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Spot Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        rvDisc = findViewById(R.id.recyclerView_spotDiscount);
        cariDiskon = findViewById(R.id.et_cariDiskon);
        FloatingActionButton fab = findViewById(R.id.fab_tambah_discount);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AturDiscount_Activity.class));
            }
        });

        rvDisc.setHasFixedSize(true);
        rvDisc.setLayoutManager(new LinearLayoutManager(getActivity()));

        rvDisc.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_spot_discount) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                find(R.id.tv_tanggal_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                find(R.id.tv_user_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                find(R.id.tv_namaPelanggan_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                find(R.id.tv_transaksi_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                find(R.id.tv_disc_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
            }
        });

        cariDiskon.setThreshold(3);
        cariDiskon.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                return super.onFindNson(context, bookTitle);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        });

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
