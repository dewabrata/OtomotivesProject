package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PartBerkala_Activity extends AppActivity {

    private RecyclerView rvPartBerkala;
    public static final int REQUEST_BIAYA = 15;
    public static final int REQUEST_PART = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
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
                viewHolder.find(R.id.tv_namaMasterPart_partBerkala, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_kmBerkala_partBerkala, TextView.class).setText(nListArray.get(position).get("KM").asString());
                viewHolder.find(R.id.tv_kmSaatIni_partBerkala, TextView.class).setText(nListArray.get(position).get("BERKALA_KM").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), Part_Activity.class);
                intent.putExtra("data", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_PART);
            }
        }));

        catchData();
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "ALL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_PART){
            Intent intent = new Intent(getActivity(), JumlahPart_Checkin_Activity.class);
            intent.putExtra("data", Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            startActivityForResult(intent, REQUEST_BIAYA);
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_BIAYA){
            Intent i = new Intent();
            Log.d("PART_BERKALA_BOOKING", "Part berkala : " +  Nson.readJson(getIntentStringExtra(data, "data")));
            i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            setResult(RESULT_OK, i);
            finish();
        }
    }
}
