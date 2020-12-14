package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Absensi_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;


public class Absen_Absensi_Fragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvAbsen;

    private Nson absenList = Nson.newArray();

    public Absen_Absensi_Fragment() {

    }


    public static Absen_Absensi_Fragment newInstance(String param1, String param2) {
        Absen_Absensi_Fragment fragment = new Absen_Absensi_Fragment();
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
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        initHideToolbar(view);
        initRecylerviewAbsen(view);
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
            viewAbsensi((Absensi_MainTab_Activity)getActivity());
        }
    }

    private void initRecylerviewAbsen(View view) {
        rvAbsen = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvAbsen.setHasFixedSize(true);
        rvAbsen.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAbsen.setAdapter(new NikitaRecyclerAdapter(absenList, R.layout.item_absensi) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                String jamMulai = Tools.setFormatDateTimeFromDb(absenList.get(position).get("ABSEN_MULAI").asString(), "HH:mm:ss", "HH:mm", false);
                String jamPulang =  Tools.setFormatDateTimeFromDb(absenList.get(position).get("ABSEN_SELESAI").asString(), "HH:mm:ss", "HH:mm", false);
                viewHolder.find(R.id.tv_mulai_kerja, TextView.class).setText(jamMulai);
                viewHolder.find(R.id.tv_selesai_kerja, TextView.class).setText(jamPulang);
                viewHolder.find(R.id.tv_tanggal_kerja, TextView.class).setText(absenList.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.tv_bulan_kerja, TextView.class).setText(Tools.getmonth(absenList.get(position).get("BULAN").asInteger()));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewAbsensi((Absensi_MainTab_Activity)getActivity());
            }
        });
    }


    public void viewAbsensi(final AppActivity activity) {
        activity.newTask(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("kategori", "ABSEN");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ABSEN), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    absenList.asArray().clear();
                    absenList.asArray().addAll(result.get("data").asArray());
                    rvAbsen.getAdapter().notifyDataSetChanged();
                    rvAbsen.scheduleLayoutAnimation();
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