package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Pembayaran_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.RP;


public class Cash_Pembayaran_Fragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvCashColl, rvCashTransaksi;
    private Nson cashColl = Nson.newArray(), cashTransaksi = Nson.newArray();
    private View dialogView;
    private AlertDialog alertDialog;
    private boolean isDialog = false;

    public Cash_Pembayaran_Fragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        initRecylerviewPembayaran(view);
        return view;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
    private void initToolbarCash() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ((Pembayaran_MainTab_Activity) getActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((Pembayaran_MainTab_Activity) getActivity()).getSupportActionBar()).setTitle("Cash Transaksi");
        ((Pembayaran_MainTab_Activity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewCashColl("");
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

    private void initRecylerviewPembayaran(View view) {
        rvCashColl = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvCashColl.setHasFixedSize(true);
        rvCashColl.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCashColl.setAdapter(new NikitaRecyclerAdapter(cashColl, R.layout.item_cash_pembayaran) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(cashColl.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.tv_total_cash, TextView.class).setText(RP + ((Pembayaran_MainTab_Activity) getActivity()).formatRp(cashColl.get(position).get("TOTAL_CASH").asString()));
                viewHolder.find(R.id.tv_total_balance, TextView.class).setText(RP + ((Pembayaran_MainTab_Activity) getActivity()).formatRp(cashColl.get(position).get("SALDO_KASIR").asString()));
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                initDialogTransaksi(cashColl.get(position).get("TANGGAL").asString());
            }
        }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewCashColl("");
            }
        });
    }

    private void initRecylerviewCashTransaksi() {
        rvCashTransaksi = dialogView.findViewById(R.id.recyclerView);
        rvCashTransaksi.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCashTransaksi.setHasFixedSize(false);
        rvCashTransaksi.setAdapter(new NikitaRecyclerAdapter(cashTransaksi, R.layout.item_cash_transaksi) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                String jam = Tools.setFormatDateTimeFromDb(cashTransaksi.get(position).get("CREATED_DATE").asString(), "", "HH:mm", true);
                viewHolder.find(R.id.tv_no_bukti_bayar, TextView.class).setText(cashTransaksi.get(position).get("NO_BUKTI_BAYAR").asString());
                viewHolder.find(R.id.tv_total, TextView.class).setText(RP + ((Pembayaran_MainTab_Activity) getActivity()).formatRp(cashTransaksi.get(position).get("GRAND_TOTAL").asString()));
                viewHolder.find(R.id.tv_transaksi, TextView.class).setText(cashTransaksi.get(position).get("TIPE_PEMBAYARAN1").asString());
                viewHolder.find(R.id.tv_jam, TextView.class).setText(jam);
            }
        });
    }

    private void initDialogTransaksi(String tanggalPembayaran) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        isDialog = true;
        initToolbarCash();
        initRecylerviewCashTransaksi();
        viewCashColl(tanggalPembayaran);
        SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);

        alertDialog = builder.create();
        alertDialog = builder.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isDialog = false;
            }
        });
    }

    private void viewCashColl(final String tglPembayaran) {
        ((Pembayaran_MainTab_Activity) getActivity()).newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "CASH");
                if (isDialog) {
                    args.put("detail", "true");
                    args.put("tanggalPembayaran", tglPembayaran);
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                    cashTransaksi.asArray().clear();
                    cashTransaksi.asArray().addAll(result.get("data").asArray());
                } else {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                    cashColl.asArray().clear();
                    cashColl.asArray().addAll(result.get("data").asArray());
                }
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (isDialog) {
                        rvCashTransaksi.getAdapter().notifyDataSetChanged();
                        rvCashTransaksi.scheduleLayoutAnimation();
                    } else {
                        rvCashColl.getAdapter().notifyDataSetChanged();
                        rvCashColl.scheduleLayoutAnimation();
                    }
                } else {
                    ((Pembayaran_MainTab_Activity) getActivity()).showError(ERROR_INFO);
                }
            }
        });
    }

}