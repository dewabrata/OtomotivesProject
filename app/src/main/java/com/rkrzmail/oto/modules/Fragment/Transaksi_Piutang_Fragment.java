package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturTotalInvoice_Activity;
import com.rkrzmail.oto.modules.hutang.Piutang_MainTab_Activity;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.PIUTANG;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Transaksi_Piutang_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private View fragmentView;
    private AppActivity activity;
    private Button btnLanjut;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson transaksiList = Nson.newArray();
    private Nson transaksiMarkList = Nson.newArray();
    private int totalInvoice = 0;

    public void Transaksi_Pembayaran_Fragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_list_basic_with_btn, container, false);
        activity = (Piutang_MainTab_Activity) getActivity();
        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        btnLanjut = fragmentView.findViewById(R.id.btn_simpan);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewTransaksi();
            }
        });

        btnLanjut.setText(getResources().getString(R.string.lanjut_uppercase));
        btnLanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(transaksiMarkList.size() > 0){
                    Intent intent = new Intent(getActivity(), AturTotalInvoice_Activity.class);
                    intent.putExtra("TOTAL_INV", totalInvoice);
                    intent.putExtra(DATA, transaksiMarkList.toJson());
                    startActivityForResult(intent, REQUEST_DETAIL);
                }else {
                    activity.showWarning("BELUM ADA TRANSAKSI DI PILIH");
                }
            }
        });

        initHideToolbar();
        initRecylerviewTransaksi();
        return fragmentView;
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewTransaksi();
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }


    private void initRecylerviewTransaksi() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(transaksiList, R.layout.item_transaksi_piutang) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                //super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.cb_mark_transaksi, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            if (transaksiMarkList.size() > 0) {
                                if (!transaksiMarkList.get(0).get("PRINCIPAL").asString().isEmpty() &&
                                        transaksiMarkList.get(0).get("PRINCIPAL").asString().equals(transaksiList.get(position).get("PRINCIPAL").asString())) {
                                    viewHolder.find(R.id.cb_mark_transaksi, CheckBox.class).setChecked(true);
                                    totalInvoice += transaksiList.get(position).get("JUMLAH_INVOICE").asInteger();
                                    transaksiMarkList.add(transaksiList.get(position));
                                } else if (!transaksiMarkList.get(0).get("NAMA_PELANGGAN").asString().isEmpty() &&
                                        transaksiMarkList.get(0).get("NAMA_PELANGGAN").asString().equals(transaksiList.get(position).get("NAMA_PELANGGAN").asString())) {
                                    viewHolder.find(R.id.cb_mark_transaksi, CheckBox.class).setChecked(true);
                                    totalInvoice += transaksiList.get(position).get("JUMLAH_INVOICE").asInteger();
                                    transaksiMarkList.add(transaksiList.get(position));
                                } else {
                                    viewHolder.find(R.id.cb_mark_transaksi, CheckBox.class).setChecked(false);
                                    activity.showWarning("TIDAK BISA MEMILIH NAMA PELANGGAN / NAMA PRINCIPAL YG BERBEDA", Toast.LENGTH_LONG);
                                }
                            } else {
                                viewHolder.find(R.id.cb_mark_transaksi, CheckBox.class).setChecked(true);
                                totalInvoice += transaksiList.get(position).get("JUMLAH_INVOICE").asInteger();
                                transaksiMarkList.add(transaksiList.get(position));
                            }

                        } else{
                            compoundButton.setChecked(false);
                            if(totalInvoice > 0) totalInvoice -= transaksiList.get(position).get("JUMLAH_INVOICE").asInteger();
                            for (int i = 0; i < transaksiMarkList.size(); i++) {
                                if (transaksiMarkList.get(i).get("ID").asInteger() == transaksiList.get(position).get("ID").asInteger()) {
                                    transaksiMarkList.remove(i);
                                }
                            }
                        }
                    }
                });

                String tgl = DateFormatUtils.formatDate(transaksiList.get(position).get("TANGGAL_PEMBAYARAN").asString(), "yyyy-MM-dd", "dd/MM");
                int totalTransaksi = transaksiList.get(position).get("JUMLAH_INVOICE").asInteger();

                viewHolder.find(R.id.tv_total_transaksi, TextView.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(totalTransaksi)));
                viewHolder.find(R.id.tv_tgl_transaksi, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_nopol, TextView.class).setText(activity.formatNopol(transaksiList.get(position).get("NOPOL").asString()));
                viewHolder.find(R.id.tv_jenis_kendaraan, TextView.class).setText(transaksiList.get(position).get("JENIS_KENDARAAN").asString());
                viewHolder.find(R.id.tv_layanan, TextView.class).setText(transaksiList.get(position).get("LAYANAN").asString());

                if(transaksiList.get(position).get("PRINCIPAL").asString().isEmpty()){
                    viewHolder.find(R.id.tv_nama_customer, TextView.class).setText(transaksiList.get(position).get("NAMA_PELANGGAN").asString());
                }else{
                    viewHolder.find(R.id.tv_nama_customer, TextView.class).setText(transaksiList.get(position).get("NAMA_PELANGGAN").asString() + "\n" + transaksiList.get(position).get("PRINCIPAL").asString());
                }

                if (transaksiList.get(position).get("FEE_NON_PAKET").asInteger() == 0 || transaksiList.get(position).get("FEE_NON_PAKET").asString().isEmpty()) {
                    viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText(RP + NumberFormatUtils.formatRp(transaksiList.get(position).get("BIAYA_LAYANAN_NET").asString()));
                    viewHolder.find(R.id.tv_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(transaksiList.get(position).get("HARGA_PART_NET").asString()));
                    viewHolder.find(R.id.tv_jasa_lain, TextView.class).setText(RP + NumberFormatUtils.formatRp(transaksiList.get(position).get("BIAYA_JASA_LAIN_NET").asString()));
                } else {
                    viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText(RP + NumberFormatUtils.formatRp(transaksiList.get(position).get("FEE_NON_PAKET").asString()));
                    viewHolder.find(R.id.tv_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(transaksiList.get(position).get("PENGGANTIAN_PART").asString()));
                    viewHolder.find(R.id.tv_jasa_lain, TextView.class).setText(RP + NumberFormatUtils.formatRp(transaksiList.get(position).get("").asString()));
                }

                if(transaksiList.get(position).get("NO_MESIN").asString().isEmpty() && transaksiList.get(position).get("KODE_TIPE").asString().isEmpty()){
                    viewHolder.find(R.id.row_no_mesin, TableRow.class).setVisibility(View.GONE);
                    viewHolder.find(R.id.row_kode_tipe, TableRow.class).setVisibility(View.GONE);
                }else{
                    viewHolder.find(R.id.tv_no_mesin, TextView.class).setText(transaksiList.get(position).get("NO_MESIN").asString());
                    viewHolder.find(R.id.tv_kode_tipe, TextView.class).setText(transaksiList.get(position).get("KODE_TIPE").asString());
                }
            }

            @Override
            public int getItemCount() {
                activity.find(R.id.btn_simpan).setVisibility(transaksiList.size() == 0 ? View.GONE : View.VISIBLE);
                return transaksiList.size();
            }

        });
    }

    private void viewTransaksi() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("jenis", "TRANSAKSI");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PIUTANG), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    transaksiList.asArray().clear();
                    transaksiMarkList.asArray().clear();
                    totalInvoice = 0;
                    transaksiList.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL) {
            viewTransaksi();
        }
    }
}
