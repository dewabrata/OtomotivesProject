package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.part.AdapterListBasic;
import com.rkrzmail.oto.gmod.part.People;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.DataGenerator;

import java.util.List;
import java.util.Map;

public class BiayaMekanikActivity extends AppActivity {
    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListBasic mAdapter;
    final int REQUEST_BIAYA_MEKANIK = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        parent_view = findViewById(android.R.id.content);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DAFTAR BIAYA MEKANIK");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    private void reload(final String nama){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("id", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("biayamekanik.php"),args)) ;

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
    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        List<People> items = DataGenerator.getPeopleData(this);
        items.addAll(DataGenerator.getPeopleData(this));
        items.addAll(DataGenerator.getPeopleData(this));

       /* //set data and list adapter
        mAdapter = new AdapterListBasic(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, People obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });*/

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_biaya_mekanik){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtMekanik1, TextView.class).setText("MEKANIK 1 : Rp " + nListArray.get(position).get("MEKANIK_1").asString());

                viewHolder.find(R.id.txtMekanik2, TextView.class).setText("MEKANIK 2 : Rp " + nListArray.get(position).get("MEKANIK_2").asString());

                viewHolder.find(R.id.txtMekanik3, TextView.class).setText("MEKANIK 3 : Rp " + nListArray.get(position).get("MEKANIK_3").asString());

                viewHolder.find(R.id.txtTanggalSet, TextView.class).setText("TANGGAL SET : " + nListArray.get(position).get("TANGGAL_SET").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                /*Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/

                //Snackbar.make(parent_view, "Item  "+position+"  clicked", Snackbar.LENGTH_SHORT).show();

            }
        }));

        reload("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_BIAYA_MEKANIK && resultCode == RESULT_OK){
            Intent intent =  new Intent();
            intent.putExtra("DATA", getIntentStringExtra(data, "DATA"));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
