package com.rkrzmail.oto.modules;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.rkrzmail.oto.gmod.AturBiayaMekanikActivity;
import com.rkrzmail.oto.modules.part.AdapterListBasic;
import com.rkrzmail.oto.modules.part.People;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.DataGenerator;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class BiayaMekanik2Activity extends AppActivity {

    private View parent_view;
    private RecyclerView rvListBasic2;
    //private AdapterListBasic mAdapter;
    final int REQUEST_BIAYA_MEKANIK = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic2);
        parent_view = findViewById(android.R.id.content);
        rvListBasic2 = findViewById(R.id.rvListBasic2);

        initToolbar();
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DAFTAR BIAYA MEKANIK2");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void reload(final String nama){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("id", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("viewbiayamekanik"),args)) ;

            }

            @Override
            public void runUI() {
                if (result.isNsonArray()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvListBasic2.getAdapter().notifyDataSetChanged();
                }else{
                    showError(result.get("message").asString());
                }
            }
        });
    }


    private void initComponent() {
        rvListBasic2 = (RecyclerView) findViewById(R.id.rvListBasic2);
        rvListBasic2.setLayoutManager(new LinearLayoutManager(this));
        rvListBasic2.setHasFixedSize(true);

        List<People> items = DataGenerator.getPeopleData(this);
        items.addAll(DataGenerator.getPeopleData(this));
        items.addAll(DataGenerator.getPeopleData(this));

        rvListBasic2 = (RecyclerView) findViewById(R.id.rvListBasic2);
        rvListBasic2.setLayoutManager(new LinearLayoutManager(this));
        rvListBasic2.setHasFixedSize(true);
        rvListBasic2.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_biaya_mekanik2){

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtMeka1, TextView.class).setText("MEKANIK 1 : Rp " + nListArray.get(position).get("MEKANIK_1").asString());
                viewHolder.find(R.id.txtMeka2, TextView.class).setText("MEKANIK 2 : Rp " + nListArray.get(position).get("MEKANIK_2").asString());
                viewHolder.find(R.id.txtMeka3, TextView.class).setText("MEKANIK 3 : Rp " + nListArray.get(position).get("MEKANIK_3").asString());
                viewHolder.find(R.id.txtTgl, TextView.class).setText("TANGGAL SET : " + nListArray.get(position).get("TANGGAL_SET").asString());
                viewHolder.find(R.id.txtUser, TextView.class).setText("USER : "+ nListArray.get(position).get("USER").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

                Intent intent =  new Intent(getActivity(), AturBiayaMekanikActivity.class);
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_BIAYA_MEKANIK);
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
