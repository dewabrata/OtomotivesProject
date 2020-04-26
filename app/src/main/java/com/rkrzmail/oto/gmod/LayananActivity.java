package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.rkrzmail.oto.modules.DaftarkanPelayananActivity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class LayananActivity extends AppActivity {
    final int BARCODE_RESULT = 12;
    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layanan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                //Intent intent =  new Intent(getActivity(), PendaftaranLayananActivity.class);
                Intent intent =  new Intent(getActivity(), Pendaftaran1.class);
                startActivityForResult(intent, REQUEST_PERSETUAN);
            }
        });

        getSupportActionBar().setTitle("DAFTAR PELAYANAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        find(R.id.bt_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LayananFilterActivity.class);
                startActivityForResult(intent, REQUEST_FILTER);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Nson nson = Nson.readNson("");


        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.activity_layanan_item2){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtStatus, TextView.class).setText(formatNopol(nListArray.get(position).get("STATUS").asString()));
                viewHolder.find(R.id.txtLayanan, TextView.class).setText("LAYANAN : " + nListArray.get(position).get("LAYANAN").asString());
                viewHolder.find(R.id.txtNama, TextView.class).setText("NAMA : " + nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.txtNoAntrian, TextView.class).setText("NO. ANTRIAN : " + nListArray.get(position).get("ANTRIAN").asString());
                viewHolder.find(R.id.txtNoPol, TextView.class).setText("NO. POL : " + nListArray.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.txtNamaUser, TextView.class).setText("MEKANIK : " + nListArray.get(position).get("MEKANIK").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);

            }
        }));

        reload();

    }

    private void reload(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(),"CID",""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(),"EMA",""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"),args)) ;

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
