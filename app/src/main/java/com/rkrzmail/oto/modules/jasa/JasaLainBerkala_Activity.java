package com.rkrzmail.oto.modules.jasa;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class JasaLainBerkala_Activity extends AppActivity {

    private RecyclerView rvJasaBerkala;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jasa_lain_berkala_);
        initComponent();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_jasaBerkala);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jasa Lain Berkala");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvJasaBerkala = findViewById(R.id.recyclerView_jasaBerkala);
        rvJasaBerkala.setLayoutManager(new LinearLayoutManager(this));
        rvJasaBerkala.setHasFixedSize(true);
        rvJasaBerkala.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jasa_lain_berkala) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_aktifitas_jasaBerkala, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_waktu_jasaBerkala, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_kmSaatIni_jasaBerkala, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_kmPenggantian_jasaBerkala, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_jasa_jasaBerkala, TextView.class).setText(nListArray.get(position).get("").asString());

            }

            @Override
            public int getItemCount() {
                if (nListArray.size() == 0) {
                    find(R.id.btn_simpan_jasaBerkala).setVisibility(View.GONE);
                } else {
                    find(R.id.btn_simpan_jasaBerkala).setVisibility(View.VISIBLE);

                }
                return super.getItemCount();
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
//                Intent intent =  new Intent(getActivity(), Checkin3_Activity.class);
//                intent.putExtra("JASA", nListArray.get(position).toJson());
//                setResult(RESULT_OK);
//                startActivity(intent);
//                finish();
            }
        }));
        catchData();
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvJasaBerkala.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
                }
            }
        });
    }

}
