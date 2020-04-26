package com.rkrzmail.oto.gmod;

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
import com.rkrzmail.oto.modules.Utils;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class HistoryActivity extends AppActivity {
    final int BARCODE_RESULT = 12;
    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("HISTORY " + formatNopol( getIntentStringExtra("NOPOL") ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);


        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.activity_history_item){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtBengkel, TextView.class).setText(nListArray.get(position).get("BENGKEL").asString());
                viewHolder.find(R.id.txtNamaLayanan, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                viewHolder.find(R.id.txtGantiOli, TextView.class).setText(nListArray.get(position).get("GANTI_OLI").asString());
                viewHolder.find(R.id.txtCatatanMekanik, TextView.class).setText(nListArray.get(position).get("CATATAN_MEKANIK").asString());
                viewHolder.find(R.id.txtTanggal, TextView.class).setText(nListArray.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.txtKM, TextView.class).setText(nListArray.get(position).get("KM").asString());
                viewHolder.find(R.id.txtKMBerikutnya, TextView.class).setText(nListArray.get(position).get("KM_BERIKUTNYA").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
               /* //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/
            }
        }));

        reload();

    }

    private void reload(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                args.put("nopol",  getIntentStringExtra("NOPOL"));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("history.php"),args)) ;

            }

            @Override
            public void runUI() {
                if (result.isNsonArray()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }
    final int REQUEST_FILTER = 12;
    final int REQUEST_CONTROL = 33;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILTER && resultCode == RESULT_OK){

        }else if (requestCode == REQUEST_PERSETUAN && resultCode == RESULT_OK){
            /*setResult(RESULT_OK);
            finish();*/
            reload();
        }else if (requestCode == REQUEST_CONTROL && resultCode == RESULT_OK){
            reload();
        }
    }

    final int REQUEST_PERSETUAN  = 123;
}
