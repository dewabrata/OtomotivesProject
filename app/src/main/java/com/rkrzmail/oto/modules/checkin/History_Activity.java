package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_KELUHAN;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class History_Activity extends AppActivity {

    private RecyclerView rvHistory, rvPartJasa, rvKeluhan;
    private AlertDialog alertDialog;

    private Nson partJasaList = Nson.newArray();
    private Nson keluhanList = Nson.newArray();

    private String catatanMekanik = "";
    private boolean isAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initToolbar();
        initRvHistory();
        initData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getIntent().hasExtra("ALL")) {
            isAll = true;
            getSupportActionBar().setTitle("History");
        } else {
            getSupportActionBar().setTitle("History Kendaraan");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (data != null) {
            nListArray.asArray().clear();
            nListArray.asArray().addAll(data.asArray());
            rvHistory.getAdapter().notifyDataSetChanged();
        }
        if (isAll) {
            viewHistoryAll(getIntentStringExtra("NOPOL"));
        }
    }

    private void initDetailHistory(String idCheckin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_history_detail, null);
        builder.setView(dialogView);

        EditText etCatatanMekanik = dialogView.findViewById(R.id.et_catatan_mekanik);
        etCatatanMekanik.setText(catatanMekanik);

        initRvPartJasa(dialogView);
        initRecylerviewKeluhan(dialogView);
        viewKeluhan(idCheckin);
        viewPartJasa(idCheckin);

        builder.create();
        alertDialog = builder.show();
    }

    private void initRvPartJasa(View dialogView) {
        rvPartJasa = dialogView.findViewById(R.id.rv_part_jasa);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setHasFixedSize(true);
        rvHistory.setAdapter(new NikitaMultipleViewAdapter(partJasaList, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);

                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText("" + (position + 1));

                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class)
                            .setVisibility(View.GONE);
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                            .setVisibility(View.GONE);
                } else {
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("AKTIVITAS").asString());
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class).setVisibility(View.GONE);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewKeluhan(View dialogView) {
        rvKeluhan = dialogView.findViewById(R.id.recyclerView);
        rvKeluhan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvKeluhan.setHasFixedSize(true);
        rvKeluhan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText(keluhanList.get(position).get("NO").asString() + ". ");
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
            }
        });
    }

    private void initRvHistory() {
        rvHistory = findViewById(R.id.recyclerView);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setHasFixedSize(true);
        NikitaRecyclerAdapter nikitaRecyclerAdapter = null;
        if (isAll) {
            nikitaRecyclerAdapter = new NikitaRecyclerAdapter(nListArray, R.layout.item_history_all) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                    super.onBindViewHolder(viewHolder, position);

                    viewHolder.find(R.id.tv_tgl_pembayaran, TextView.class).setText(nListArray.get(position).get("TANGGAL_PEMBAYARAN").asString());
                    viewHolder.find(R.id.tv_bidang_usaha, TextView.class).setText(nListArray.get(position).get("BIDANG_USAHA").asString());
                    viewHolder.find(R.id.tv_nama_layanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());

                    String ket;
                    if (nListArray.get(position).get("CID").asString().equals(UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim())) {
                        ket = "BENGKEL";
                        viewHolder.find(R.id.tv_total_biaya, TextView.class).setText(RP + formatRp(nListArray.get(position).get("TOTAL_BIAYA").asString()));
                        viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(nListArray.get(position).get("NAMA_MEKANIK").asString());
                    } else {
                        ket = "LAIN";
                        viewHolder.find(R.id.tv_total_biaya, TextView.class).setText("");
                        viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText("");
                    }
                    viewHolder.find(R.id.tv_ket_bengkel, TextView.class).setText(ket);

                }
            }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Nson parent, View view, int position) {
                    catatanMekanik = nListArray.get(position).get("CATATAN_MEKANIK").asString();
                    initDetailHistory(nListArray.get(position).get("CHECKIN_ID").asString());
                }
            });
        } else {
            nikitaRecyclerAdapter = new NikitaRecyclerAdapter(nListArray, R.layout.item_history_checkin) {
                @Override
                public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                    super.onBindViewHolder(viewHolder, position);
                    String date = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("CREATED_DATE").asString());

                    viewHolder.find(R.id.tv_tgl_historyCheckin, TextView.class).setText(nListArray.get(position).get("CREATED_DATE").asString());
                    viewHolder.find(R.id.tv_km_historyCheckin, TextView.class).setText(nListArray.get(position).get("KM").asString());
                    viewHolder.find(R.id.tv_namaLayanan_historyCheckin, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                    viewHolder.find(R.id.tv_pelanggan, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                    viewHolder.find(R.id.tv_total_biaya, TextView.class).setText(RP + formatRp(nListArray.get(position).get("TOTAL_BIAYA").asString()));

                }
            };
        }

        rvHistory.setAdapter(nikitaRecyclerAdapter);
    }

    private void viewHistoryAll(final String nopol) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "HISTORY");
                args.put("detail", "ALL");
                args.put("nopol", nopol);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvHistory.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void viewKeluhan(final String idCheckin) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KELUHAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    keluhanList.asArray().clear();
                    keluhanList.asArray().addAll(result.get("data").asArray());
                    rvKeluhan.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }


    @SuppressLint("NewApi")
    private void viewPartJasa(final String idCheckin) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "HISTORY");
                args.put("detail", "PART JASA");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partJasaList.asArray().clear();
                    partJasaList.asArray().addAll(result.get("data").asArray());
                    rvPartJasa.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
