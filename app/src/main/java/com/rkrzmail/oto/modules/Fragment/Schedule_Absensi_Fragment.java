package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
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
import com.rkrzmail.oto.modules.Adapter.Absensi_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;


public class Schedule_Absensi_Fragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvSchedule;

    private Nson scheduleList = Nson.newArray();

    public Schedule_Absensi_Fragment() {

    }


    public static Schedule_Absensi_Fragment newInstance(String param1, String param2) {
        Schedule_Absensi_Fragment fragment = new Schedule_Absensi_Fragment();
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
        initRecylerviewSchedule(view);
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
            viewSchedule();
        }
    }

    private void initRecylerviewSchedule(View view) {
        rvSchedule = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvSchedule.setHasFixedSize(true);
        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSchedule.setAdapter(new NikitaRecyclerAdapter(scheduleList, R.layout.item_absensi) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                //String tgl = DateFormatUtils.formatDate(scheduleList.get(position).get("TANGGAL").asString(), "yyyy-MM-dd", "dd/MM");
                String[] splitTgl = scheduleList.get(position).get("TANGGAL").asString().split("-");
                String bulan = splitTgl[1];

                viewHolder.find(R.id.tv_mulai_kerja, TextView.class).setText(scheduleList.get(position).get("SCHEDULE_MULAI").asString());
                viewHolder.find(R.id.tv_selesai_kerja, TextView.class).setText(scheduleList.get(position).get("SCHEDULE_SELESAI").asString());
                viewHolder.find(R.id.tv_tanggal_kerja, TextView.class).setText(scheduleList.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.tv_bulan_kerja, TextView.class).setText(Tools.getmonth(Integer.parseInt(bulan)));
                viewHolder.find(R.id.tv_lokasi, TextView.class).setText(scheduleList.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_status, TextView.class).setText(scheduleList.get(position).get("STATUS").asString());

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewSchedule();
            }
        });
    }

    private void viewSchedule() {
        ((Absensi_MainTab_Activity) Objects.requireNonNull(getActivity())).newTask(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("kategori", "SCHEDULE");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ABSEN), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    scheduleList.asArray().clear();
                    scheduleList.asArray().addAll(result.get("data").asArray());
                    rvSchedule.getAdapter().notifyDataSetChanged();
                    rvSchedule.scheduleLayoutAnimation();
                } else {
                    ((Absensi_MainTab_Activity) getActivity()).showError(ERROR_INFO);
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