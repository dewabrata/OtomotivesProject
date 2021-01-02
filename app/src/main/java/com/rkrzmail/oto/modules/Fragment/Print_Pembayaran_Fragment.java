package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.PRINT_BUKTI_BAYAR;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;


public class Print_Pembayaran_Fragment extends Fragment {

    private RecyclerView rvPrint;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson pembayaranList = Nson.newArray();
    private String idCheckin = "";

    public Print_Pembayaran_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewPrintPembayaran();
        }
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }


    private void initRecylerviewPembayaran(View view) {
        rvPrint = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvPrint.setHasFixedSize(true);
        rvPrint.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPrint.setAdapter(new NikitaRecyclerAdapter(pembayaranList, R.layout.item_print_pembayaran) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(pembayaranList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(pembayaranList.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.tv_no_bukti_bayar, TextView.class).setText(pembayaranList.get(position).get("NO_BUKTI_BAYAR").asString());
                viewHolder.find(R.id.tv_total_bayar, TextView.class).setText(pembayaranList.get(position).get("TOTAL_BAYAR").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Nson parent, View view, final int position) {
                Messagebox.showDialog(getActivity(), "Konfirmasi", "Print Bukti Bayar ?", "OK", "Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(PRINT_BUKTI_BAYAR(parent.get(position).get("NO_BUKTI_BAYAR").asString())));
                        startActivity(i);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewPrintPembayaran();
            }
        });
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

    private void viewPrintPembayaran() {
        ((Pembayaran_MainTab_Activity) getActivity()).newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "PRINT");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                pembayaranList.asArray().clear();
                pembayaranList.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    rvPrint.getAdapter().notifyDataSetChanged();
                    rvPrint.scheduleLayoutAnimation();
                } else {
                    ((Pembayaran_MainTab_Activity) getActivity()).showError(ERROR_INFO);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL)
            viewPrintPembayaran();
    }
}