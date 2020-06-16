package com.rkrzmail.oto.modules.sparepart.pengembalian_part;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.rkrzmail.oto.modules.primary.checkin.Checkin3_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PartHarusDikembalikan_Activity extends AppActivity {

    private RecyclerView rvPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pengembalian Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        initToolbar();
        rvPart = findViewById(R.id.recyclerView);
        rvPart.setLayoutManager(new LinearLayoutManager(this));
        rvPart.setHasFixedSize(true);
        rvPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_pengembalian_part) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                //user = nopart, penggunaan = namapart, jumlah = tgl
                viewHolder.find(R.id.tv_tgl_pengembalianPart, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_penggunaan_pengembalianPart, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_user_pengembalianPart, TextView.class).setText(nListArray.get(position).get("").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), Checkin3_Activity.class);
                intent.putExtra("data", nListArray.get(position).toJson());
                setResult(RESULT_OK);
                startActivity(intent);
                finish();
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
                    rvPart.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
                }
            }
        });
    }
}
