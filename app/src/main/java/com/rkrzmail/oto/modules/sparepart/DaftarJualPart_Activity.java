package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
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
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_JUAL_PART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class DaftarJualPart_Activity extends AppActivity {

    private TextView tvTotal;
    private RecyclerView rvTotalJualPart;
    private int count = 0;
    private ArrayList<Integer> harga = new ArrayList<>();
    private DecimalFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_jual_part_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daftar Jual Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        initToolbar();
        formatter = new DecimalFormat("###,###,###");
        tvTotal = findViewById(R.id.tv_totalHarga_jualPart);
        rvTotalJualPart = findViewById(R.id.recyclerView_jualPart);
        if (tvTotal.isFocusable()) {
            Tools.hideKeyboard(getActivity());
        }

        nListArray.add(Nson.readJson(getIntentStringExtra("data")));
        int harga = 0;
        for (int i = 0; i < nListArray.size(); i++) {
            String hargaJual = nListArray.get(i).get("TOTAL").asString().replaceAll("[^0-9]", "");
            harga = harga + Integer.parseInt(hargaJual);
        }
        String finalTotal = String.valueOf(harga);
        tvTotal.setText("Total : Rp. " + formatRp(finalTotal));

        rvTotalJualPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvTotalJualPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_total_jual_part) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                        super.onBindViewHolder(viewHolder, position);

                        viewHolder.find(R.id.tv_noPart_jualPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_namaPart_jualPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_harga_jualPart, TextView.class).setText(nListArray.get(position).get("HARGA_JUAL").asString());
                        viewHolder.find(R.id.tv_disc_jualPart, TextView.class).setText(nListArray.get(position).get("DISC").asString());
                        viewHolder.find(R.id.tv_jumlah_jualPart, TextView.class).setText(nListArray.get(position).get("JUMLAH").asString());
                        String str = nListArray.get(position).get("TOTAL").asString();
                        viewHolder.find(R.id.tv_total_jualPart, TextView.class).setText("Rp. " + formatRp(str));

                        viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                nListArray.asArray().remove(position);
                                notifyItemRemoved(position);
                            }
                        });
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
                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Nson fromAtur = Nson.readJson(getIntentStringExtra("data"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jeniskendaraan", fromAtur.get("JENIS_KENDARAAN").asString());
                args.put("tanggal", currentDateTime());
                args.put("nohp",fromAtur.get("NO_PONSEL").asString());
                args.put("namapelanggan",fromAtur.get("NAMA_PELANGGAN").asString());
                args.put("namausaha",fromAtur.get("NAMA_USAHA").asString());
                args.put("status", "PENDING");
                args.put("parts", nListArray.toJson());

                Log.d("data__", "NLIST_ARRAY : " + nListArray);
                Log.d("data__", "DATA : " + fromAtur);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_JUAL_PART), args));
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


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson =  Nson.readJson(getIntentStringExtra(data,"part"));
            Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
            i.putExtra("part", nson.toJson());
            Log.d("partpartpart", "data" + Nson.readJson(getIntentStringExtra(data,"part")));
            startActivityForResult(i, REQUEST_DETAIL);

        }else if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {
            Nson fromCariPart = Nson.readJson(getIntentStringExtra(data, "part"));
            Log.d("terimaData", "data "+ fromCariPart);
            nListArray.add(fromCariPart);
            rvTotalJualPart.getAdapter().notifyDataSetChanged();
            int harga = 0;
            for (int i = 0; i < nListArray.size(); i++) {
                String hargaJual = nListArray.get(i).get("TOTAL").asString().replaceAll("[^0-9]", "");
                harga = harga + Integer.parseInt(hargaJual);
            }
            String finalTotal = String.valueOf(harga);
            Log.d("nlistArray__", "data" + nListArray);

            tvTotal.setText("Total : Rp. " + formatter.format(Double.valueOf(finalTotal)));

        }
    }
}
