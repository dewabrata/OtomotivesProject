package com.rkrzmail.oto.modules.terima_part;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.biayamekanik.AturBiayaMekanik2;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

public class TerimaPart extends AppActivity {

    public static final String TAG = "TerimaPart";
    private RecyclerView recyclerView_terimaPart;
    private TextView txtTgl;
    //private AdapterListBasic mAdapter;
    final int REQUEST_TERIMA_PART = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terima_part);

        initToolbar();
        initComponent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_terima_part);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturTerimaPart.class);
                startActivityForResult(intent, REQUEST_TERIMA_PART);
            }
        });
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TERIMA PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        recyclerView_terimaPart = (RecyclerView) findViewById(R.id.recyclerView_terimaPart);
        recyclerView_terimaPart.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_terimaPart.setHasFixedSize(true);
        recyclerView_terimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_terima_part){

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                //.setText("TANGGAL :" + nListArray.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.txtTanggal, TextView.class);
                viewHolder.find(R.id.txtSupplier, TextView.class);
                viewHolder.find(R.id.txtPembayaran, TextView.class);
                viewHolder.find(R.id.txtTotal, TextView.class);
                viewHolder.find(R.id.txtNoDo, TextView.class);
                viewHolder.find(R.id.txtJatuhTempo, TextView.class);
                viewHolder.find(R.id.txtUser, TextView.class);

            }
        });
        reload();
    }

    private void reload(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_TERIMA_PART && resultCode == RESULT_OK){
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
