package com.rkrzmail.oto.modules;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    public static final String TAG = "BiayaMekanik2Activity";
    private RecyclerView rvListBasic2;
    //private AdapterListBasic mAdapter;
    final int REQUEST_BIAYA_MEKANIK2 = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic2);
        rvListBasic2 = findViewById(R.id.rvListBasic2);

        initToolbar();
        initComponent();

        FloatingActionButton fab = (FloatingActionButton) find(R.id.fab_add_atur);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturBiayaMekanik2.class);

                startActivityForResult(intent, REQUEST_BIAYA_MEKANIK2);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DAFTAR BIAYA MEKANIK2");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

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
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(getActivity(), AturBiayaMekanik2.class);
                intent.putExtra("MEKANIK 1",nListArray.get(position).get("MEKANIK_1").asString());
                intent.putExtra("MEKANIK 2",nListArray.get(position).get("MEKANIK_2").asString());
                intent.putExtra("MEKANIK 3",nListArray.get(position).get("MEKANIK_3").asString());
                intent.putExtra("TANGGAL SET",nListArray.get(position).get("TANGGAL_SET").asString());
                intent.putExtra("USER",nListArray.get(position).get("USER").asString());
                intent.putExtra("ID", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_BIAYA_MEKANIK2);
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewbiayamekanik"),args)) ;

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvListBasic2.getAdapter().notifyDataSetChanged();
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
        if (requestCode==REQUEST_BIAYA_MEKANIK2 && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            reload();
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
