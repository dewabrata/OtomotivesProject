package com.rkrzmail.oto.modules.mekanik;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class BiayaMekanik2Activity extends AppActivity {

    public static final String TAG = "BiayaMekanik2___";
    private static final int REQUEST_ATUR = 10;
    private RecyclerView rvListBasic2;
    private TextView txtTgl;
    private Nson listUmk = Nson.newObject();
    private String umk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Biaya Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        for (int i = 0; i < data.get("BENGKEL").size(); i++) {
            if(data.get("BENGKEL").get(i).containsKey("UMK")){
               umk = data.get("BENGKEL").get(i).get("UMK").asString();
            }
        }
        Log.d(TAG, "initComponent: " + umk);

        FloatingActionButton fab = findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturBiayaMekanik2.class);
                intent.putExtra("UMK", umk);
                startActivityForResult(intent, REQUEST_ATUR);
            }
        });

        rvListBasic2 = findViewById(R.id.recyclerView);
        rvListBasic2.setLayoutManager(new LinearLayoutManager(this));
        rvListBasic2.setHasFixedSize(true);
        rvListBasic2.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_biaya_mekanik2) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                String tglSet = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("TANGGAL_SET").asString());

                viewHolder.find(R.id.tv_tgl_biayaMekanik, TextView.class).setText(tglSet);
                viewHolder.find(R.id.tv_user_biayaMekanik, TextView.class).setText(nListArray.get(position).get("USER").asString());
                viewHolder.find(R.id.tv_minBayar_biayaMekanik, TextView.class).setText("Rp. " + nListArray.get(position).get("UPAH_MINIM").asString());
                viewHolder.find(R.id.tv_mekanik1_biayaMekanik, TextView.class).setText("Rp. " + nListArray.get(position).get("MEKANIK_1").asString());
                viewHolder.find(R.id.tv_mekanik2_biayaMekanik, TextView.class).setText("Rp. " + nListArray.get(position).get("MEKANIK_2").asString());
                viewHolder.find(R.id.tv_mekanik3_biayaMekanik, TextView.class).setText("Rp. " + nListArray.get(position).get("MEKANIK_3").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), AturBiayaMekanik2.class);
                intent.putExtra("USER", nListArray.get(position).toJson());
                intent.putExtra("UMK", umk);
                startActivityForResult(intent, REQUEST_ATUR);
            }
        }));
        reload();
    }

    private void reload() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewbiayamekanik"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvListBasic2.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ATUR) {
            reload();
        }
    }
}
