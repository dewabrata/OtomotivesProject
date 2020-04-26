package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class KendaraanCari extends AppActivity {
    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kendaraancari);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("PENCARIAN KENDARAAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        find(R.id.bt_cari).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload(find(R.id.et_search, EditText.class).getText().toString());
            }
        });


        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.kendaraan_item){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtNama, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                //viewHolder.find(R.id.txtStatus, TextView.class).setText(nListArray.get(position).get("STATUS").asString());


            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
               /* Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/
            }
        }));

        find(R.id.et_search, EditText.class).setText(getIntent().getStringExtra("cari"));

        reload(find(R.id.et_search, EditText.class).getText().toString());
    }

    private void reload(final String nama){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("nama",  nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("kendaraan.php"),args)) ;

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
}
