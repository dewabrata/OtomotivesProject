package com.rkrzmail.oto.modules.jurnal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class DaftarJurnal_Activity extends AppActivity {

    private RecyclerView rvJurnal;
    private NikitaAutoComplete cariJurnal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_jurnal_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_daftar_jurnal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        FloatingActionButton fab = findViewById(R.id.fab_tambah_jurnal);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AturJurnal_Activity.class));
            }
        });

        rvJurnal = findViewById(R.id.recyclerView_daftar_jurnal);
        cariJurnal = findViewById(R.id.et_cariJurnal);

        rvJurnal.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJurnal.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jurnal) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_tanggal_jurnal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_transaksi_jurnal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_pos_jurnal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_ket_jurnal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_total_jurnal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_pembayaran_jurnal, TextView.class).setText(nListArray.get(position).get("").asString());

            }
        });

        cariJurnal.setThreshold(3);
        cariJurnal.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                return super.onFindNson(context, bookTitle);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        });


    }

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("spotdiscount"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Gagal");
                }
            }
        });
    }
}
