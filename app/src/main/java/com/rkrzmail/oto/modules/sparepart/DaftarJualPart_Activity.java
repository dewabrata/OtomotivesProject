package com.rkrzmail.oto.modules.sparepart;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DaftarJualPart_Activity extends AppActivity {

    private static final int REQUEST_DETAIL = 13;
    private TextView tvTotal;
    private RecyclerView rvTotalJualPart;
    private static final int REQUEST_CARI_PART = 14;
    private int count = 0;
    private ArrayList<Integer> harga = new ArrayList<>();
    private DecimalFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_jual_part_);
        //Bundle b = getIntent().getExtras();
//        if (b != null) {
//            for (String key : b.keySet()) {
//                nListArray.add(b.get(key).toString().replace("\\", ""));
//            }
//
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daftar Jual Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        formatter = new DecimalFormat("###,###,###");
        tvTotal = findViewById(R.id.tv_totalHarga_jualPart);
        rvTotalJualPart = findViewById(R.id.recyclerView_jualPart);
        if (tvTotal.isFocusable()) {
            Tools.hideKeyboard(getActivity());
        }

        nListArray.add(Nson.readJson(getIntentStringExtra("data")));
        //Log.d("terimaData", "data "+  Nson.readJson(getIntentStringExtra( "data")));
        int harga = 0;
        for (int i = 0; i < nListArray.size(); i++) {
            String hargaJual = nListArray.get(i).get("HARGA_JUAL").asString().replaceAll("[^0-9]", "");
            harga = harga + Integer.parseInt(hargaJual);
        }
        String finalTotal = String.valueOf(harga);
        tvTotal.setText("Total : Rp. " + formatter.format(Double.valueOf(finalTotal)));

        rvTotalJualPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvTotalJualPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_total_jual_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        int hargaJualll = nListArray.get(position).get("HARGA_JUAL").asInteger();
                        //tvTotal.setText(String.valueOf(sum));
                        viewHolder.find(R.id.tv_noPart_jualPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_namaPart_jualPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                        viewHolder.find(R.id.tv_harga_jualPart, TextView.class).setText(nListArray.get(position).get("HARGA_JUAL").asString());
                        viewHolder.find(R.id.tv_disc_jualPart, TextView.class).setText(nListArray.get(position).get("DISC").asString());
                        viewHolder.find(R.id.tv_jumlah_jualPart, TextView.class).setText(nListArray.get(position).get("JUMLAH").asString());
                        String str = nListArray.get(position).get("TOTAL").asString();
                        viewHolder.find(R.id.tv_total_jualPart, TextView.class).setText("Rp. " + formatter.format(Double.valueOf(str)));
                    }

                    @Override
                    public int getItemCount() {
                        if (nListArray.size() == 0) {
                            find(R.id.btn_simpan_jualPart, Button.class).setVisibility(View.GONE);
                            tvTotal.setVisibility(View.GONE);
                        } else {
                            find(R.id.btn_simpan_jualPart, Button.class).setVisibility(View.VISIBLE);
                            tvTotal.setVisibility(View.VISIBLE);
                        }
                        return super.getItemCount();
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        nListArray.asArray().remove(nListArray.get(position));
                        rvTotalJualPart.getAdapter().notifyDataSetChanged();
//                        Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
//                        i.putExtra("part", nListArray.get(position).toJson());
//                        startActivityForResult(i, REQUEST_DETAIL);
                    }
                })
        );

        find(R.id.btn_simpan_jualPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        find(R.id.btn_search, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("flag", "ALL");
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Nson n = Nson.readJson(getIntentStringExtra("part"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //action(add), CID, jenis, nohp, nama, nusaha, nopart, napart, hpart, disc, jumlah, total

                args.put("action", "add");
                args.put("jenis", n.get("jenis").asString());
                args.put("nohp", n.get("nohp").asString());
                //args.put("nama", n.get("nama").asString());
                args.put("nusaha", n.get("nusaha").asString());
                args.put("nopart", n.get("PART_ID").asString());
                args.put("napart", n.get("NAMA").asString());
                //args.put("datapart", nlisArray.toJson());
                //args.put("hpart",);
//                args.put("disc",);
//                args.put("jumlah",);
                args.put("total", tvTotal.getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturjualpart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Data");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagagl Menyimpan Data");
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
            i.putExtra("part", Nson.readJson(getIntentStringExtra(data,"part")).toJson());
            //Log.d("partpartpart", "data" + Nson.readJson(getIntentStringExtra(data,"part")));
            startActivityForResult(i, REQUEST_DETAIL);
        }else if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "part")));
            rvTotalJualPart.getAdapter().notifyDataSetChanged();
            int harga = 0;
            for (int i = 0; i < nListArray.size(); i++) {
                String hargaJual = nListArray.get(i).get("HARGA_JUAL").asString().replaceAll("[^0-9]", "");
                harga = harga + Integer.parseInt(hargaJual);
            }
            String finalTotal = String.valueOf(harga);
            tvTotal.setText("Total : Rp. " + formatter.format(finalTotal));
        }
    }
}
