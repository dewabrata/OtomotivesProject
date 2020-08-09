package com.rkrzmail.oto.modules.primary.booking;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.jasa.JasaLainBerkala_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.JumlahHargaPart_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.PartBerkala_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Booking3_Activity extends AppActivity implements View.OnClickListener {

    public static final String TAG = "Booking3_Activity_Log";
    public static final int REQUEST_HARGA_PART = 20;
    private RecyclerView rvBooking3;
    public static final int REQUEST_PART = 15;
    public static final int REQUEST_JASA_LAIN = 16;
    public static final int REQUEST_JASA_BERKALA = 17;
    public static final int REQUEST_PART_BERKALA = 18;
    public static final int REQUEST_PART_EXTERNAL = 19;
    private DecimalFormat formatter;

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
        formatter = new DecimalFormat("###,###,###");
        find(R.id.btn_jasaLain_booking3, Button.class).setOnClickListener(this);
        find(R.id.btn_part_booking3, Button.class).setOnClickListener(this);
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
                String hargaPart = nListArray.get(position).get("HARGA_PART").asString();
                String hargaJasa = nListArray.get(position).get("HARGA_JASA").asString();
                String net = nListArray.get(position).get("NET").asString();

                if (!hargaJasa.isEmpty()) {
                    viewHolder.find(R.id.tv_harga_booking3_checkin3, TextView.class).setText(hargaJasa);
                    viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(hargaJasa);
                } else if (!hargaPart.isEmpty()) {
                    viewHolder.find(R.id.tv_harga_booking3_checkin3, TextView.class).setText(hargaPart);
                    viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(hargaPart);
                }
                viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(net);
                viewHolder.find(R.id.tv_nama_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_disc_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("DISC").asString());
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
        Nson nson = Nson.readJson(getIntentStringExtra("booking1"));

//        StringBuilder namas = new StringBuilder();
//        StringBuilder partId = new StringBuilder();
//        StringBuilder jasaId = new StringBuilder();
        List<Integer> hargaParts = new ArrayList<>();
        List<Integer> hargaJasas = new ArrayList<>();

        for (int i = 0; i < nListArray.size(); i++) {
//            if (partId.length() > 0) partId.append(", ");
//            //if (jasaId.length() > 0) jasaId.append(", ");
//            if (namas.length() > 0) namas.append(", ");
//
//            namas.append(nListArray.get(i).get("NAMA"));
//            partId.append(nListArray.get(i).get("PART_ID"));
//            jasaId.append(nListArray.get(i).get("JASA_ID"));

            if(nListArray.get(i).get("HARGA_JASA") != null && nListArray.get(i).get("HARGA_PART") != null){
                String hargaPart = nListArray.get(i).get("HARGA_PART").asString().replaceAll("[^0-9]+", "");
                String hargaJasa = nListArray.get(i).get("HARGA_JASA").asString().replaceAll("[^0-9]+", "");
                if(!hargaJasa.equals("")){
                    hargaJasas.add(Integer.parseInt(hargaJasa));
                }
                if(!hargaPart.equals("")){
                    hargaParts.add(Integer.parseInt(hargaPart));
                }
            }
        }

        int total =  sumNumbers(hargaParts) + sumNumbers(hargaJasas);
        Log.d(TAG, "finalHargaPart: " + total);
//        Log.d(TAG, "PART_JASA: " + namas.toString());
//        Log.d(TAG, "PART_ID: " + partId + "\n" + "JASA_ID : " + jasaId);
        Log.d(TAG, "HARGA_JASA: " + hargaJasas + "\n" + "HARGA_PART : " + hargaParts);
        Log.d(TAG, "SIZE : " + nListArray.size());

//        nson.set("PART_ID", partId.toString());
//        nson.set("JASA_ID", jasaId.toString());
//        nson.set("HARGA_PART", totalPart);
//        nson.set("HARGA_JASA", totalJasa);
//        nson.set("PART_JASA", namas.toString());
//        nson.set("JUMLAH", nListArray.size());
//        nson.set("DISCOUNT_PART", nListArray.get("DISC_PART").asString());
//        nson.set("DISCOUNT_JASA", nListArray.get("DISC_JASA").asString());
        nson.set("partbook", nListArray);
        nson.set("TOTAL", total);
        Intent i = new Intent();
        i.putExtra("booking3", nson.toJson());
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.btn_jasaLain_booking3:
                i = new Intent(getActivity(), JasaLain_Activity.class);
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_part_booking3:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("flag", "ALL");
                startActivityForResult(i, REQUEST_PART);
                break;
            case R.id.btn_jasaLainBerkala_booking3:
                Nson n = Nson.readJson(getIntentStringExtra("KM_SEKARANG"));
                String km = n.get("km").asString();
                Log.d("Booking3List", "KM : " + n.get("km"));

                i = new Intent(getActivity(), JasaLainBerkala_Activity.class);
                i.putExtra("KM_SAAT_INI", km);
                Log.d("Booking3List", "KM 3 : " + km);
                startActivityForResult(i, REQUEST_JASA_BERKALA);
                break;
            case R.id.btn_partBerkala_booking3:
                i = new Intent(getActivity(), PartBerkala_Activity.class);
                startActivityForResult(i, REQUEST_PART_BERKALA);
                break;
            case R.id.btn_partExternal_booking3:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("flag", "MASTER_PART");
                startActivityForResult(i, REQUEST_PART_EXTERNAL);
                break;

        }
    }

    private static int sumNumbers(List<Integer> harga) {
        int sum = 0;
        for (int i = 0; i < harga.size(); i++) {
            sum += harga.get(i);
        }
        return sum;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_JASA_LAIN:
                    StringBuilder stringBuilder = new StringBuilder();
                    Nson nson = Nson.readJson(getIntentStringExtra(data, "data"));
                    nListArray.add(nson);
                    Log.d(TAG, "REQUEST_JASA_LAIN : " + nson);
                    rvBooking3.getAdapter().notifyDataSetChanged();
                    break;
                case REQUEST_JASA_BERKALA:
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    Log.d(TAG, "REQUEST_JASA_BERKALA : " + nListArray);
                    rvBooking3.getAdapter().notifyDataSetChanged();
                    break;
                case REQUEST_PART:
                    i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                    i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "nopart")).toJson());
                    startActivityForResult(i, REQUEST_HARGA_PART);
                    break;
                case REQUEST_PART_BERKALA:
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    rvBooking3.getAdapter().notifyDataSetChanged();
                    break;
                case REQUEST_PART_EXTERNAL:
                    i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                    i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    startActivityForResult(i, REQUEST_HARGA_PART);
                    break;
                case REQUEST_HARGA_PART:
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    Log.d(TAG, "REQUEST_PART " + Nson.readJson(getIntentStringExtra(data, "data")));
                    rvBooking3.getAdapter().notifyDataSetChanged();
                    break;
            }
        }

        Log.d(TAG, "TOTAL : " + nListArray);
    }
}
