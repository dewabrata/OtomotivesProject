package com.rkrzmail.oto.modules.penugasan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PenugasanActivity extends AppActivity {

    public static final String TAG = "PenugasanActivity";
    private RecyclerView rvPenugasan;
    private static final int REQUEST_PENUGASAN = 123;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penugasan);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_tugas);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturPenugasan_Activity.class);

                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        });

        rvPenugasan = (RecyclerView) findViewById(R.id.recyclerView_penugasan);
        rvPenugasan.setLayoutManager(new LinearLayoutManager(this));
        rvPenugasan.setHasFixedSize(true);

        rvPenugasan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_penugasan) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                //DataGenerator.getStringsShort(getActivity());

                viewHolder.find(R.id.tvNamaMekanik, TextView.class).setText(nListArray.get(position).get("NAMA_MEKANIK").asString());
                viewHolder.find(R.id.tvAntrian, TextView.class).setText(nListArray.get(position).get("TIPE_ANTRIAN").asString());
                viewHolder.find(R.id.tvLokasi, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tvStart, TextView.class).setText(nListArray.get(position).get("JAM_MASUK").asString());
                viewHolder.find(R.id.tvFinish, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());


            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), AturPenugasan_Activity.class);
                intent.putExtra("ID", nListArray.get(position).get("ID").asString());
                intent.putExtra("TIPE_ANTRIAN", nListArray.get(position).get("TIPE_ANTRIAN").asString());
                intent.putExtra("LOKASI", nListArray.get(position).get("LOKASI").asString());
                intent.putExtra("ID", nListArray.get(position).toJson());
                //intent.putExtra("id", nListArray.get(position).get("id").asString());
                startActivityForResult(intent, REQUEST_PENUGASAN);
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("daftarpenugasan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    List<Nson> sort = new ArrayList<>();
                    for (int i = 0; i < result.get("data").size(); i++) {
                        sort.add(result.get("data").get(i).get("NAMA_MEKANIK"));
                    }
                    Collections.sort(sort, new Comparator<Nson>() {
                        @Override
                        public int compare(Nson nson, Nson nson2) {
                            return nson.get("NAMA_MEKANIK").asString().compareTo(nson2.get("NAMA_MEKANIK").asString());
                        }
                    });

                    Log.d(TAG, "reload data");
                    rvPenugasan.getAdapter().notifyDataSetChanged();
                } else {
                    Log.d(TAG, "error");
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }
}
