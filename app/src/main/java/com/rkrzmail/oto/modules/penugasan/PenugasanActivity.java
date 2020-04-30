package com.rkrzmail.oto.modules.penugasan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.ControlLayanan;
import com.rkrzmail.oto.gmod.AturPenugasan_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PenugasanActivity extends AppActivity {

    public static final String TAG = "PenugasanActivity";
    private RecyclerView rvPenugasan;
    private static final int REQUEST_PENUGASAN  = 123;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penugasan);

        initToolbar();
        initComponent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_tugas);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getActivity(), AturPenugasan_Activity.class);

                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        });


    }

    private void initToolbar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        rvPenugasan = (RecyclerView) findViewById(R.id.recyclerView_penugasan);
        rvPenugasan.setLayoutManager(new LinearLayoutManager(this));
        rvPenugasan.setHasFixedSize(true);

        rvPenugasan.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_penugasan){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tvNamaMekanik, TextView.class).setText("Nama Mekanik : " + nListArray.get(position).get("NAMA_MEKANIK").asString());
                viewHolder.find(R.id.tvAntrian, TextView.class).setText("Tipe Antrian : " + nListArray.get(position).get("TIPE_ANTRIAN").asString());
                viewHolder.find(R.id.tvLokasi, TextView.class).setText("Lokasi Penugasan : " + nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tvStart, TextView.class).setText("Jam Masuk : " + nListArray.get(position).get("JAM_MASUK").asString());
                viewHolder.find(R.id.tvFinish, TextView.class).setText("Jam Pulang : " + nListArray.get(position).get("JAM_PULANG").asString());


            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(getActivity(), AturPenugasan_Activity.class);
                intent.putExtra("ID", nListArray.get(position).get("ID").asString());
                intent.putExtra("ID", nListArray.get(position).toJson());
                //intent.putExtra("id", nListArray.get(position).get("id").asString());
                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        }));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int direction) {
               MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
                   Nson result ;
                   @Override
                   public void run() {
                       Map<String, String> args = AppApplication.getInstance().getArgsData();
                       String action = "delete";
                       args.put("action", action);
                       args.put("namamekanik", result.get("data").asString());
                       result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("v3/aturpenugasanmekanik"),args));
//                       result.get("data").get(viewHolder.getAdapterPosition()).remove("namamekanik");
                   }

                   @Override
                   public void runUI() {
                       if (result.get("status").asString().equalsIgnoreCase("OK")) {
                           nListArray.asArray().remove(viewHolder.getAdapterPosition());
                           rvPenugasan.getAdapter().notifyDataSetChanged();
                       }else{
                           showError("Mohon Di Coba Kembali" + result.get("message").asString());
                       }

                   }
               });
               showInfo("onSwiped");
            }
        }).attachToRecyclerView(rvPenugasan);
        catchData();
    }

    private void catchData(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("daftarpenugasan"), args)) ;
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvPenugasan.getAdapter().notifyDataSetChanged();
                    Log.d(TAG, "reload data");
                }else {
                    Log.d(TAG, "error");
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PENUGASAN && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            catchData();
        }
    }
}
