package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Absensi_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.PEMBAYARAN_KOMISI;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.RP;

public class KomisiUser_Absensi_Fragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvKomisi, rvKomisiDetail;
    View dialogView;
    AlertDialog alertDialog;

    AppActivity activity;
    private final Nson komisiUserList = Nson.newArray();
    private final Nson komisiDetailList = Nson.newArray();

    public KomisiUser_Absensi_Fragment() {

    }


    public static KomisiUser_Absensi_Fragment newInstance(String param1, String param2) {
        KomisiUser_Absensi_Fragment fragment = new KomisiUser_Absensi_Fragment();
        return fragment;
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
        View view = inflater.inflate(R.layout.activity_komisi_karyawan, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        activity = (Absensi_MainTab_Activity) getActivity();
        initHideToolbar(view);
        initRecylerviewSchedule(view);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewKomisi();
            }
        });
        return view;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewKomisi();
        }
    }

    private void initRecylerviewSchedule(View view) {
        rvKomisi = view.findViewById(R.id.recyclerView);
        rvKomisi.setHasFixedSize(true);
        rvKomisi.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKomisi.setAdapter(new NikitaRecyclerAdapter(komisiUserList, R.layout.item_komisi_karyawan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                //String tgl = DateFormatUtils.formatDate(scheduleList.get(position).get("TANGGAL").asString(), "yyyy-MM-dd", "dd/MM");
                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(komisiUserList.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.tv_komisi_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(komisiUserList.get(position).get("TOTAL_KOMISI_PART").asString()));
                viewHolder.find(R.id.tv_hari, TextView.class).setText(komisiUserList.get(position).get("HARI").asString());
                viewHolder.find(R.id.tv_komisi_layanan, TextView.class).setText(RP + NumberFormatUtils.formatRp(komisiUserList.get(position).get("TOTAL_KOMISI_LAYANAN").asString()));
                viewHolder.find(R.id.tv_komisi_jasa, TextView.class).setText(RP + NumberFormatUtils.formatRp(komisiUserList.get(position).get("TOTAL_KOMISI_JASA").asString()));
                viewHolder.find(R.id.tv_total_komisi, TextView.class).setText(RP + NumberFormatUtils.formatRp(komisiUserList.get(position).get("TOTAL_KOMISI").asString()));

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                showDialogDetail(parent.get(position).get("TANGGAL").asString());
            }
        }));
    }


    private void initRvDetail() {
        rvKomisiDetail = dialogView.findViewById(R.id.recyclerView);
        rvKomisiDetail.setHasFixedSize(true);
        rvKomisiDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKomisiDetail.setAdapter(new NikitaRecyclerAdapter(komisiDetailList, R.layout.item_komisi_karyawan_detail) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                String komisiPercent = komisiDetailList.get(position).get("BASIS_KOMISI_PERCENT").asString();
                komisiPercent = komisiPercent.replace(",", ".");
                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(komisiDetailList.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.tv_komisi_percent, TextView.class).setText(NumberFormatUtils.formatPercent(Double.parseDouble(komisiPercent)));
                viewHolder.find(R.id.tv_jenis_komisi, TextView.class).setText(komisiDetailList.get(position).get("JENIS_KOMISI").asString());
                viewHolder.find(R.id.tv_jumlah_komisi, TextView.class).setText(RP + NumberFormatUtils.formatRp(komisiDetailList.get(position).get("JUMLAH_KOMISI").asString()));
            }
        });
    }

    private void initToolbarDetail() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Detail Komisi");
    }

    @SuppressLint("InflateParams")
    private void showDialogDetail(String tglKomisi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);

        initToolbarDetail();
        initRvDetail();
        viewDetailKomisi(tglKomisi);
    }

    private void viewDetailKomisi(final String tanggalKomisi) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "DETAIL KOMISI USER");
                args.put("tanggalKomisi", tanggalKomisi);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PEMBAYARAN_KOMISI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    komisiDetailList.asArray().clear();
                    komisiDetailList.asArray().addAll(result.get("data").asArray());
                    rvKomisiDetail.getAdapter().notifyDataSetChanged();
                    if (alertDialog != null) {
                        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        alertDialog.show();
                    }
                } else {
                    activity.showError(ERROR_INFO);
                }
            }
        });
    }


    private void viewKomisi() {
        activity.newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "KOMISI USER");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PEMBAYARAN_KOMISI), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    activity.find(R.id.tv_total_balance, TextView.class).setText("BALANCE " + RP + NumberFormatUtils.formatRp(result.get(0).get("BALANCE").asString()));
                    if (result.size() > 0) {
                        komisiUserList.asArray().clear();
                        komisiUserList.asArray().addAll(result.asArray());
                    }
                    Objects.requireNonNull(rvKomisi.getAdapter()).notifyDataSetChanged();
                    rvKomisi.scheduleLayoutAnimation();
                } else {
                    activity.showError(ERROR_INFO);
                }
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
}
