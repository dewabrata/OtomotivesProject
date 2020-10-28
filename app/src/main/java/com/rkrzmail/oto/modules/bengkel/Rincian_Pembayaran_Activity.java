package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_DP;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_JUAL_PART;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Rincian_Pembayaran_Activity extends AppActivity {

    View dialogView;
    RecyclerView rvPart, rvJasa, rvPartJualPart;

    private boolean isLayanan = false, isDp = false, isJualPart = false;
    private Nson partList = Nson.newArray(), jasaList = Nson.newArray();
    private String namaPelanggan = "", nopol = "", noHp = "", jenisKendaraan = "", layanan = "", tanggal = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_pembayaran);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getIntent().hasExtra(RINCIAN_LAYANAN)) {
            isLayanan = true;
            find(R.id.ly_rincian_jual_part).setVisibility(View.GONE); //layout rincian pembelian
            Objects.requireNonNull(getSupportActionBar()).setTitle(RINCIAN_LAYANAN);
            find(R.id.row_dp_persen).setVisibility(View.GONE);
            find(R.id.row_sisa_dp_persen).setVisibility(View.GONE);
        } else if (getIntent().hasExtra(RINCIAN_DP)) {
            isDp = true;
            find(R.id.ly_rincian_jual_part).setVisibility(View.GONE); //layout rincian pembelian
            Objects.requireNonNull(getSupportActionBar()).setTitle(RINCIAN_LAYANAN);
            find(R.id.row_dp).setVisibility(View.GONE);
            find(R.id.row_sisa_biaya).setVisibility(View.GONE);
            find(R.id.row_disc_spot).setVisibility(View.GONE);
            find(R.id.row_total_2).setVisibility(View.GONE);
            find(R.id.ly_ket).setVisibility(View.GONE);
            find(R.id.et_catatan).setVisibility(View.GONE);
        } else if (getIntent().hasExtra(RINCIAN_JUAL_PART)) {
            isJualPart = true;
            find(R.id.btn_jasa_part).setVisibility(View.GONE);
            find(R.id.ly_rincian_layanan).setVisibility(View.GONE); //layout rincian layanan
            Objects.requireNonNull(getSupportActionBar()).setTitle(RINCIAN_JUAL_PART);
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        loadData();
        viewRincianPembayaran();
        find(R.id.btn_jasa_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewJasaPart();
            }
        });
        find(R.id.btn_lanjut, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AturPembayaran_Activity.class);
                i.putExtra(DATA, find(R.id.tv_total_2, TextView.class).getText().toString().isEmpty() ? find(R.id.tv_total_1, TextView.class).getText().toString() : find(R.id.tv_total_2, TextView.class).getText().toString());
                startActivityForResult(i, REQUEST_DETAIL);
            }
        });
    }

    private void loadData(){
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));

        namaPelanggan = nson.get("NAMA_PELANGGAN").asString();
        nopol = nson.get("NOPOL").asString();
        noHp = nson.get("NO_PONSEL").asString();
        tanggal = nson.get("TANGGAL").asString();
        jenisKendaraan = nson.get("JENIS_KENDARAAN").asString();
        layanan = nson.get("LAYANAN").asString();
    }

    private void viewRincianPembayaran() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                if (isLayanan) {
                    args.put("jenisPembayaran","CHECKIN");
                }
                if (isDp) {
                    args.put("typeRincian", "RINCIAN DP");
                }
                if (isJualPart) {
                    args.put("jenisPembayaran","JUAL PART");
                }

                args.put("detail", "TRUE");
                args.put("namaPelanggan", namaPelanggan);
                args.put("nopol", nopol);
                args.put("noHp", noHp);
                args.put("tanggal", tanggal);
                args.put("jenisKendaraan", jenisKendaraan);
                args.put("layanan", layanan);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (isLayanan || isDp) {
                        loadDataRincianLayanan(result);
                    }
                    if (isJualPart) {
                        loadRincianJualPart(result);
                    }
                } else {
                    result.get("message").asString();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadDataRincianLayanan(Nson result) {
        result = result.get("data");
        int total1 = 0, sisaBiaya = 0, total2, totalLayanan = 0, totalDp = 0, totalPart = 0, totalJasa = 0, totalJasaPart = 0;
        for (int i = 0; i < result.size(); i++) {
            totalPart += result.get(i).get("HARGA_PART").asInteger();
            totalJasaPart += result.get(i).get("HARGA_JASA_PART").asInteger();
            totalJasa += result.get(i).get("HARGA_JASA_LAIN").asInteger();
            totalLayanan += result.get(i).get("BIAYA_LAYANAN").asInteger();

            if (result.get(i).get("DEREK").asString().equals("Y")) {
                find(R.id.cb_derek, CheckBox.class).setChecked(true);
            }
            if (result.get(i).get("BUANG_PART").asString().equals("Y")) {
                find(R.id.cb_buangPart, CheckBox.class).setChecked(true);
            }
            if (result.get(i).get("ANTAR_JEMPUT").asString().equals("Y")) {
                find(R.id.cb_antar_jemput, CheckBox.class).setChecked(true);
            }
            if (result.get(i).get("DISCOUNT_LAYANAN").asString().isEmpty() || result.get(i).get("DISCOUNT_LAYANAN").asString().equals("0")) {
                find(R.id.row_disc_layanan).setVisibility(View.GONE);
            }
            if (result.get(i).get("DISCOUNT_FREKWENSI").asString().isEmpty() || result.get(i).get("DISCOUNT_FREKWENSI").asString().equals("0")) {
                find(R.id.row_disc_frekwensi).setVisibility(View.GONE);
            }
            if (result.get(i).get("DISCOUNT_PART").asString().isEmpty() || result.get(i).get("DISCOUNT_PART").asString().equals("0")) {
                find(R.id.row_disc_part).setVisibility(View.GONE);
            }
            if (result.get(i).get("DISCOUNT_JASA_PART").asString().isEmpty() || result.get(i).get("DISCOUNT_JASA_PART").asString().equals("0")) {
                find(R.id.row_disc_jasa_part).setVisibility(View.GONE);
            }
            if (result.get(i).get("DISCOUNT_JASA_LAIN").asString().isEmpty() || result.get(i).get("DISCOUNT_JASA_LAIN").asString().equals("0")) {
                find(R.id.row_disc_jasa_lain).setVisibility(View.GONE);
            }
            if (result.get(i).get("DEREK").asString().isEmpty() || result.get(i).get("DEREK").asString().equals("0")) {
                find(R.id.row_transport).setVisibility(View.GONE);
            }
            if (result.get(i).get("DP").asString().isEmpty() || result.get(i).get("DP").asString().equals("0")) {
                find(R.id.row_dp).setVisibility(View.GONE);
            }
            if (result.get(i).get("DISCOUNT_SPOOT").asString().isEmpty() || result.get(i).get("DISCOUNT_SPOOT").asString().equals("0")) {
                find(R.id.row_disc_spot).setVisibility(View.GONE);
            }

            find(R.id.tv_nama_pelanggan, TextView.class).setText(result.get(i).get("NAMA_PELANGGAN").asString());
            find(R.id.tv_no_ponsel, TextView.class).setText((result.get(i).get("NO_PONSEL").asString()));
            find(R.id.tv_layanan, TextView.class).setText((result.get(i).get("LAYANAN").asString()));
            find(R.id.tv_nopol, TextView.class).setText((result.get(i).get("NOPOL").asString()));
            find(R.id.tv_frek, TextView.class).setText((result.get(i).get("FREKWENSI").asString()));

            find(R.id.tv_disc_layanan, TextView.class).setText((result.get(i).get("DISC_LAYANAN").asString()));
            find(R.id.tv_disc_frekwensi, TextView.class).setText((result.get(i).get("DISC_FREKWENSI").asString()));

            find(R.id.tv_disc_part, TextView.class).setText((result.get(i).get("DISC_PART").asString()));

            find(R.id.tv_disc_jasa_part, TextView.class).setText((result.get(i).get("DISC_JASA_PART").asString()));

            find(R.id.tv_disc_jasa_lain, TextView.class).setText((result.get(i).get("DISC_JASA_LAIN").asString()));
            find(R.id.tv_harga_derek_transport, TextView.class).setText(formatRp(result.get(i).get("HARGA_DEREK").asString()));
            find(R.id.tv_harga_penyimpanan, TextView.class).setText(formatRp(result.get(i).get("").asString()));

            find(R.id.tv_dp, TextView.class).setText(formatRp(result.get(i).get("DP").asString()));
            find(R.id.tv_sisa_biaya, TextView.class).setText((result.get(i).get("").asString()));
            find(R.id.tv_disc_spot, TextView.class).setText((result.get(i).get("DISC_SPOT").asString()));
            find(R.id.tv_total_2, TextView.class).setText((result.get(i).get("").asString()));
            find(R.id.et_ket_tambahan, EditText.class).setText((result.get(i).get("KETERANGAN").asString()));
            find(R.id.et_catatan, EditText.class).setText((result.get(i).get("CATATAN").asString()));
        }
        total1 = totalLayanan + totalJasa + totalJasaPart + totalPart;
        find(R.id.tv_totalLayanan, TextView.class).setText(RP +formatRp(String.valueOf(totalLayanan)));
        find(R.id.tv_harga_jasa_lain, TextView.class).setText(RP +formatRp(String.valueOf(totalJasa)));
        find(R.id.tv_harga_jasa_part, TextView.class).setText(RP +formatRp(String.valueOf(totalJasaPart)));
        find(R.id.tv_totalPart, TextView.class).setText(RP +formatRp(String.valueOf(totalPart)));
        find(R.id.tv_total_1, TextView.class).setText(RP + formatRp(String.valueOf(total1)));
        find(R.id.row_sisa_biaya).setVisibility(View.GONE);
        find(R.id.row_total_2).setVisibility(View.GONE);
        find(R.id.row_penyimpanan).setVisibility(View.GONE);
    }

    private void loadRincianJualPart(Nson result) {
        initRecylerviewPartPembelian();
        find(R.id.tv_nama_pelanggan_jual_part, TextView.class).setText(formatRp(result.get("NAMA_PELANGGAN").asString()));
        find(R.id.tv_no_ponsel_jual_part, TextView.class).setText(formatRp(result.get("NO_PONSEL").asString()));
        find(R.id.tv_frek, TextView.class).setText(formatRp(result.get("FREKWENSI").asString()));
        find(R.id.tv_nama_usaha_jual_part, TextView.class).setText(formatRp(result.get("NAMA_USAHA").asString()));
        find(R.id.tv_harga_part_jual_part, TextView.class).setText(formatRp(result.get("HARGA_PART").asString()));
        find(R.id.tv_harga_disc_jual_part, TextView.class).setText(formatRp(result.get("DISC_PART").asString()));
        find(R.id.tv_total_jual_part, TextView.class).setText(formatRp(result.get("").asString()));
        find(R.id.tv_total_ppn_jual_part, TextView.class).setText(formatRp(result.get("PPN").asString()));
        find(R.id.tv_total_penjualan_jual_part, TextView.class).setText(formatRp(result.get("").asString()));
    }

    private void viewJasaPart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        initToolbarDialog();
        getJasaPart();
        initRecylerviewJasaLayanan();
        initRecylerviewPartLayanan();

        builder.create();
        AlertDialog alertDialog = builder.show();
    }

    @SuppressLint("NewApi")
    private void initToolbarDialog() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Part & Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewPartPembelian() {
        rvPartJualPart = findViewById(R.id.recyclerView_rincian_pembelian);
        rvPartJualPart.setHasFixedSize(true);
        rvPartJualPart.setLayoutManager(new LinearLayoutManager(this));
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("MERK").asString());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewPartLayanan() {
        rvPart = dialogView.findViewById(R.id.recyclerView);
        rvPart.setHasFixedSize(true);
        rvPart.setLayoutManager(new LinearLayoutManager(this));
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("MERK").asString());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewJasaLayanan() {
        rvJasa = dialogView.findViewById(R.id.recyclerView2);
        rvJasa.setHasFixedSize(true);
        rvJasa.setLayoutManager(new LinearLayoutManager(this));
        rvJasa.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_part_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText(RP + formatRp(jasaList.get(position).get("HARGA_JASA").asString()));
            }
        });
    }

    @SuppressLint("NewApi")
    private void getJasaPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("", "");
                args.put("", "");
                args.put("", "");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partList.asArray().clear();
                    jasaList.asArray().clear();

                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        //idCheckinDetail = result.get(i).get(ID).asString();
                        if (!result.get(i).get("NAMA_PART").asString().isEmpty()) {
                            partList.add(result.get(i));
                        }
                        if (!result.get(i).get("KELOMPOK_PART").asString().isEmpty()) {
                            jasaList.add(result.get(i));
                        }
                    }
                    Objects.requireNonNull(rvJasa.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

}
