package com.rkrzmail.oto.modules.tenda;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
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

public class Tenda_Activity extends AppActivity {

    private static final String TAG = "Tenda_Activity";
    private RecyclerView rvTenda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenda);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_daftar_tenda);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Tenda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_tenda);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturTenda_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        rvTenda = (RecyclerView) findViewById(R.id.recyclerView_daftar_tenda);
        rvTenda.setLayoutManager(new LinearLayoutManager(this));
        rvTenda.setHasFixedSize(true);

        rvTenda.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_tenda) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                String tgl = Tools.setFormatDayAndMonth(nListArray.get(position).get("TANGGAL").asString());
                //DataGenerator.getStringsShort(getActivity());

                viewHolder.find(R.id.tv_tanggalMulai_tenda, TextView.class).setText(nListArray.get(position).get("TANGGAL_MULAI").asString());
                viewHolder.find(R.id.tv_tipeTenda, TextView.class).setText(nListArray.get(position).get("TIPE").asString());
                viewHolder.find(R.id.tv_namaLokasi_tenda, TextView.class).setText(nListArray.get(position).get("NAMA_LOKASI").asString());
                viewHolder.find(R.id.tv_jamBuka_tenda, TextView.class).setText(nListArray.get(position).get("JAM_BUKA").asString());
                viewHolder.find(R.id.tv_tanggalSelesai_tenda, TextView.class).setText(nListArray.get(position).get("TANGGAL_SELESAI").asString());
                viewHolder.find(R.id.tv_jamTutup_tenda, TextView.class).setText(nListArray.get(position).get("JAM_TUTUP").asString());
            }
        });
        catchData();
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvTenda.getAdapter().notifyDataSetChanged();
                } else {
                    Log.d(TAG, "error");
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
