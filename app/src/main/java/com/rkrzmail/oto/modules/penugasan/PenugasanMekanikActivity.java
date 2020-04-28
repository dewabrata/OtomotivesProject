package com.rkrzmail.oto.modules.penugasan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.ControlLayanan;
import com.rkrzmail.oto.gmod.Pendaftaran1;
import com.rkrzmail.oto.gmod.Penugasan_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PenugasanMekanikActivity extends AppActivity {

    private RecyclerView rvPenugasan;
    private static final int REQUEST_PENUGASAN  = 123;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penugasan_mekanik);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Penugasan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_tugas);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                //Intent intent =  new Intent(getActivity(), PendaftaranLayananActivity.class);
                Intent intent =  new Intent(getActivity(), Penugasan_Activity.class);
                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        });

        rvPenugasan = (RecyclerView) findViewById(R.id.recyclerView_penugasan);
        rvPenugasan.setLayoutManager(new LinearLayoutManager(this));
        rvPenugasan.setHasFixedSize(true);

        Nson nson = Nson.readNson("");

        rvPenugasan.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_penugasan){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tvNamaMekanik, TextView.class).setText("NAMA MEKANIK : " + nListArray.get(position).get("NAMA MEKANIK").asString());
                viewHolder.find(R.id.tvAntrian, TextView.class).setText("TIPE ANTRIAN : " + nListArray.get(position).get("TIPE ANTRIAN").asString());
                viewHolder.find(R.id.tvLokasi, TextView.class).setText("LOKASI : " + nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tvStart, TextView.class).setText("JAM MULAI : " + nListArray.get(position).get("JAM MULAI").asString());
                viewHolder.find(R.id.tvFinish, TextView.class).setText("JAM SELESAI : " + nListArray.get(position).get("JAM SELESAI").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        }));
    }

    private void reload(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


            }

            @Override
            public void runUI() {
                if (result.isNsonArray()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.asArray());
                    rvPenugasan.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PENUGASAN && resultCode == RESULT_OK){


        }


    }
}
