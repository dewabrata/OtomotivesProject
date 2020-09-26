package com.rkrzmail.oto.modules.sparepart.part_keluar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstString.ATUR;
import static com.rkrzmail.utils.ConstString.DETAIL;
import static com.rkrzmail.utils.ConstString.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstString.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstString.REQUEST_JUMLAH_PART_KELUAR;
import static com.rkrzmail.utils.ConstString.REQUEST_PART_KEMBALI;
import static com.rkrzmail.utils.ConstString.RP;
import static com.rkrzmail.utils.ConstString.TAG_PART_KELUAR;

public class DetailPartKeluar_Activity extends AppActivity {

    RecyclerView rvPartKeluar;
    int isFromCari = 0;
    int totalAllPart = 0;
    private boolean isAtur = false, isDetail = false;
    Nson nson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_part_keluar_);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (getIntent().hasExtra(ATUR)) {
            isAtur = true;
            getSupportActionBar().setTitle("Detail Part Keluar");
        } else if (getIntent().hasExtra(DETAIL)) {
            isDetail = true;
            getSupportActionBar().setTitle("Stock Part Keluar");
        }
    }

    private void initComponent() {
        initToolbar();
        if (isDetail) {
            find(R.id.ly_nama_mekanik).setVisibility(View.GONE);
            find(R.id.btn_search).setVisibility(View.GONE);
            find(R.id.btn_simpan).setVisibility(View.GONE);
            nson = Nson.readJson(getIntentStringExtra(DETAIL));
        } else {
            nson = Nson.readJson(getIntentStringExtra("part"));
            totalAllPart += nson.get("JUMLAH").asInteger();
            find(R.id.et_nama_mekanik, EditText.class).setText(nson.get("NAMA_MEKANIK").asString());
        }
        Log.d(TAG_PART_KELUAR, "initComponent: " + nson);
        initRecylerview(nson);
        initButton();
    }

    private void initButton() {
        find(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("cari_part_lokasi", "RUANG PART");
                startActivityForResult(i, REQUEST_CARI_PART);
                isFromCari++;
            }
        });

        find(R.id.img_scan_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void initRecylerview(final Nson nson) {
        rvPartKeluar = findViewById(R.id.recyclerView);
        rvPartKeluar.setLayoutManager(new LinearLayoutManager(this));
        rvPartKeluar.setHasFixedSize(true);
        rvPartKeluar.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_cari_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                        viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(isDetail ? nListArray.get(position).get("SISA").asString() : nListArray.get(position).get("JUMLAH").asString());
                        viewHolder.find(R.id.tv_cari_pending, TextView.class).setText(isDetail ? (Tools.isNumeric(nListArray.get(position).get("HPP").asString()) ? RP + formatRp(nListArray.get(position).get("HPP").asString()) : "") : nListArray.get(position).get("PENDING").asString());
                        viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(isDetail ? View.GONE : View.VISIBLE);
                        viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                nListArray.asArray().remove(position);
                                notifyItemRemoved(position);
                            }
                        });
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        if (isDetail) {
                            Intent i = new Intent(getActivity(), JumlahPartKeluar_Activity.class);
                            i.putExtra(DETAIL, parent.get(position).toJson());
                            startActivityForResult(i, REQUEST_PART_KEMBALI);
                        }
                    }
                })
        );

        nListArray.asArray().clear();
        if(isDetail){
            nListArray.asArray().addAll(nson.asArray());
        }else{
            nListArray.add(nson);
        }
        rvPartKeluar.getAdapter().notifyDataSetChanged();
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("jumlah", String.valueOf(totalAllPart));
                args.put("penerima", find(R.id.et_nama_mekanik, EditText.class).getText().toString());
                args.put("parts", nListArray.toJson());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpartkeluar"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menambahkan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal Menambahkan Aktifitas Mohon Di Coba Kembali");
                }
            }
        });
    }

    private void initPartKembali() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpartkeluar"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    isDetail = true;
                    nListArray.asArray().clear();
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("PART_KELUAR").asArray().size() > 0) {
                            nListArray.asArray().addAll(result.get(i).get("PART_KELUAR").asArray());
                        }
                    }
                    rvPartKeluar.getAdapter().notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    public void getDataBarcode(final String nama) {
        final Nson nson = Nson.readJson(getIntentStringExtra("part"));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "BARCODE");
                args.put("user", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewkaryawan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    nson.set("PART_ID", result.get("NO").asString());
                    nson.set("NO_PART", result.get("NOMOR_PART_NOMOR").asString());
                    nson.set("NAMA_PART", result.get("NAMA_PART").asString());
                } else {
                    //error
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));
            for (int i = 0; i < nListArray.size(); i++) {
                if (nListArray.get(i).get("PART_ID").asInteger() == nson.get("PART_ID").asInteger()) {
                    showWarning("Part Sudah Di tambahkan");
                    return;
                }
            }
            Intent i = new Intent(getActivity(), JumlahPartKeluar_Activity.class);
            i.putExtra("part", nson.toJson());
            startActivityForResult(i, REQUEST_JUMLAH_PART_KELUAR);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_JUMLAH_PART_KELUAR) {
            String namaMekanik = "";
            String lokasiPenugasan = "";
            Nson fromCariPart = Nson.readJson(getIntentStringExtra(data, "part"));
            totalAllPart += fromCariPart.get("JUMLAH").asInteger();
            for (int i = 0; i < nListArray.size(); i++) {
                if (nListArray.get(i).get("NAMA_MEKANIK") != null) {
                    namaMekanik = nListArray.get(i).get("NAMA_MEKANIK").asString();
                }
                if (nListArray.get(i).get("LOKASI") != null) {
                    lokasiPenugasan = nListArray.get(i).get("LOKASI").asString();
                }
            }
            Log.d(TAG_PART_KELUAR, "DAFTAR : " + fromCariPart);
            fromCariPart.set("LOKASI", lokasiPenugasan);
            fromCariPart.set("NAMA_MEKANIK", namaMekanik);
            nListArray.add(fromCariPart);
            rvPartKeluar.getAdapter().notifyDataSetChanged();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            getDataBarcode(data != null ? data.getStringExtra("TEXT") : "");
            showSuccess(data != null ? data.getStringExtra("TEXT") : "");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_PART_KEMBALI) {
            //initPartKembali();
            setResult(RESULT_OK);
            finish();
        }
        Log.d(TAG_PART_KELUAR, "LIST : " + nListArray);
        Log.d(TAG_PART_KELUAR, "TOTAL : " + totalAllPart);
    }
}