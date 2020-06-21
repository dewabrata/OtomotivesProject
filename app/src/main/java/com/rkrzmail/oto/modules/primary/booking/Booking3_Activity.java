package com.rkrzmail.oto.modules.primary.booking;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.jasa.JasaLainBerkala_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.PartBerkala_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.Part_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

public class Booking3_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvBooking3;
    final int REQUEST_PENDAFTARAN = 14;
    final int REQUEST_PART = 15;
    final int REQUEST_JASA_LAIN = 16;
    final int REQUEST_JASA_BERKALA = 17;
    final int REQUEST_PART_BERKALA = 18;
    final int REQUEST_PART_EXTERNAL = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking3_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        rvBooking3.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_nama_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_harga_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_disc_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_net_booking3, TextView.class).setText(nListArray.get(position).get("").asString());

            }

            @Override
            public int getItemCount() {
                if (nListArray.size() == 0) {
                    find(R.id.btn_simpan_booking3, Button.class).setVisibility(View.GONE);
                } else {
                    find(R.id.btn_simpan_booking3, Button.class).setVisibility(View.VISIBLE);
                }
                return super.getItemCount();
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
        Nson nson = Nson.newObject();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
//        nson.set();
        Intent i = new Intent(getActivity(), Booking2_Activity.class);
        i.putExtra("data", nson.toJson());
        startActivity(i);

    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.btn_jasaLain_booking3:
                i = new Intent(getActivity(), JasaLain_Activity.class);
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_sparePart_booking3:
                i = new Intent(getActivity(), Part_Activity.class);
                startActivityForResult(i, REQUEST_PART);
                break;
            case R.id.btn_jasaLainBerkala_booking3:
                i = new Intent(getActivity(), JasaLainBerkala_Activity.class);
                startActivityForResult(i, REQUEST_JASA_BERKALA);
                break;
            case R.id.btn_partBerkala_booking3:
                i = new Intent(getActivity(), PartBerkala_Activity.class);
                startActivityForResult(i, REQUEST_PART_BERKALA);
                break;
            case R.id.btn_partExternal_booking3:
                i = new Intent(getActivity(), Part_Activity.class);
                startActivityForResult(i, REQUEST_PART_EXTERNAL);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
