package com.rkrzmail.oto.modules.rekening_bank;

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
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class RekeningBank_Activity extends AppActivity {

    private RecyclerView rvRekening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_3);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rekening Bank");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturRekening_Activity.class), 10);
            }
        });

        rvRekening = findViewById(R.id.recyclerView);
        rvRekening.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvRekening.setHasFixedSize(true);
        rvRekening.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_rekening) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_noRek_rekening, TextView.class).setText(nListArray.get(position).get("NO_REKENING").asString());
                viewHolder.find(R.id.tv_namaRek_rekening, TextView.class).setText(nListArray.get(position).get("NAMA_REKENING").asString());
                viewHolder.find(R.id.tv_edc_rekening, TextView.class).setText(nListArray.get(position).get("EDC_ACTIVE").asString());
                viewHolder.find(R.id.tv_offUs_rekening, TextView.class).setText(nListArray.get(position).get("OFF_US").asString());
                viewHolder.find(R.id.tv_bank_rekening, TextView.class).setText(nListArray.get(position).get("BANK_NAME").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), AturRekening_Activity.class);
                i.putExtra("data", nListArray.get(position).toJson());
                startActivityForResult(i, 10);
            }
        }));

        catchData();
    }

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("setrekeningbank"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvRekening.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo("Gagal Memuat Data, Coba Lagi!");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                catchData();
            }
        }
    }
}
