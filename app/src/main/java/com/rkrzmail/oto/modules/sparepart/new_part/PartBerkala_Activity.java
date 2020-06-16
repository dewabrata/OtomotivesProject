package com.rkrzmail.oto.modules.sparepart.new_part;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.rkrzmail.oto.modules.primary.checkin.Checkin3_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PartBerkala_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvPartBerkala;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_2);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Part Berkala");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        initToolbar();
        rvPartBerkala = findViewById(R.id.recyclerView);
        rvPartBerkala.setLayoutManager(new LinearLayoutManager(this));
        rvPartBerkala.setHasFixedSize(true);
        rvPartBerkala.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_berkala) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_namaMasterPart_partBerkala, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_kmPenggantian_partBerkala, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_kmSaatIni_partBerkala, TextView.class).setText(nListArray.get(position).get("").asString());

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

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), Checkin3_Activity.class);
                intent.putExtra("NAMA_PART", nListArray.get(position).toJson());
                setResult(RESULT_OK);
                startActivity(intent);
                finish();
            }
        }));
        catchData();
        find(R.id.btn_simpan).setOnClickListener(this);
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvPartBerkala.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_simpan) {

        }
    }
}
