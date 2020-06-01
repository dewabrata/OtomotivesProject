package com.rkrzmail.oto.modules.booking;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class Booking3_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvBooking3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking3_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        find(R.id.btn_jasaLain_booking3, Button.class).setOnClickListener(this);
        find(R.id.btn_sparePart_booking3, Button.class).setOnClickListener(this);
        find(R.id.btn_jasaLainBerkala_booking3, Button.class).setOnClickListener(this);
        find(R.id.btn_partBerkala_booking3, Button.class).setOnClickListener(this);
        find(R.id.btn_partExternal_booking3, Button.class).setOnClickListener(this);

        rvBooking3 = findViewById(R.id.recyclerView_booking3);
        rvBooking3.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvBooking3.setHasFixedSize(true);

        rvBooking3.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_booking3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_nama_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_harga_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_disc_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_net_booking3, TextView.class).setText(nListArray.get(position).get("").asString());

            }
        });

        find(R.id.btn_simpan_booking3, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking3"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("GAGAL!");
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_jasaLain_booking3:
                break;
            case R.id.btn_sparePart_booking3:
                break;
            case R.id.btn_jasaLainBerkala_booking3:
                break;
            case R.id.btn_partBerkala_booking3:
                break;
            case R.id.btn_partExternal_booking3:
                break;

        }
    }
}
