package com.rkrzmail.oto.modules.layanan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class Layanan_Avtivity extends AppActivity {

    private static final String TAG = "Layanan_Activity";
    private RecyclerView rvLayanan;
    private NikitaAutoComplete cariLayanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layanan__avtivity);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_layanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent(){

        cariLayanan = findViewById(R.id.et_cariLayanan);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_layanan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturLayanan_Activity.class);
                startActivity(intent);
            }
        });

        rvLayanan = (RecyclerView) findViewById(R.id.recyclerView_layanan);
        rvLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvLayanan.setHasFixedSize(true);

        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_layanan){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_jenis_layanan, TextView.class).setText("JENIS LAYANAN : " + nListArray.get(position).get("JENIS_LAYANAN").asString());
                viewHolder.find(R.id.tv_nama_layanan, TextView.class).setText("NAMA LAYANAN : " + nListArray.get(position).get("NAMA_LAYANAN").asString());
                viewHolder.find(R.id.tv_lokasi_layanan, TextView.class).setText("LOKASI : " + nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_status_layanan, TextView.class).setText("STATUS : " + nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText("BIAYA : " + nListArray.get(position).get("BIAYA").asString());

            }
        });

        catchData("");

        cariLayanan.setThreshold(3);
        cariLayanan.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"),args)) ;
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenis_layanan, parent, false);
                }

                findView(convertView, R.id.tv_find_cari_layanan, TextView.class).setText( (getItem(position).get("JENIS_LAYANAN").asString()));

                return convertView;
            }
        });

        cariLayanan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append( n.get("JENIS_LAYANAN").asString()  ).append(" ");

                find(R.id.et_cariLayanan, NikitaAutoComplete.class).setText(stringBuilder.toString());
                find(R.id.et_cariLayanan, NikitaAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(position)));

                find (R.id.tv_find_cari_layanan, TextView.class).setText(n.get("JENIS_LAYANAN").asString());
            }
        });

        cariLayanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getText = cariLayanan.getText().toString().toUpperCase();
                catchData(getText);
            }
        });
    }

    private void catchData(final String nama) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvLayanan.getAdapter().notifyDataSetChanged();
                    Log.d(TAG, result.get("status").asString());
                    Log.d("NAMA", result.get("search").get("data").asString());

                } else {
                    Log.d(TAG, result.get("status").asString());
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }
}
