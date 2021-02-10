package com.rkrzmail.oto.modules.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Schedule_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static android.view.View.GONE;
import static com.rkrzmail.utils.APIUrls.SET_SCHEDULE;


public class Jadwal_Kehadiran_Fragment extends Fragment {

    private AppActivity activity;
    private RecyclerView rcKehadiran;
    private Nson kehadiranArray = Nson.newArray();

    public Jadwal_Kehadiran_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        activity = ((Schedule_MainTab_Activity) getActivity());
        viewKehadiran();

        rcKehadiran = view.findViewById(R.id.recyclerView);
        return view;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(GONE);
    }

    private void viewKehadiran(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_SCHEDULE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    kehadiranArray.asArray().clear();
                    kehadiranArray.asArray().addAll(result.get("data").asArray());
                    initRecylerview();
                } else {
//                    activity.showInfo(result.get("message").asString());
                }
            }
        });

    }

    private void initRecylerview() {
        rcKehadiran.setHasFixedSize(true);
        rcKehadiran.setLayoutManager(new LinearLayoutManager(activity));
        rcKehadiran.setAdapter(new NikitaRecyclerAdapter(kehadiranArray, R.layout.item_schedule_kehadiran) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_kehadiran_tanggal, TextView.class).setText(kehadiranArray.get(position).get("CREATED_DATE").asString());
                        viewHolder.find(R.id.tv_kehadiran_nama, TextView.class).setText(kehadiranArray.get(position).get("NAMA_USER").asString());
                        viewHolder.find(R.id.tv_kehadiran_scmulai, TextView.class).setText(kehadiranArray.get(position).get("SCHEDULE_MULAI").asString());
                        viewHolder.find(R.id.tv_kehadiran_scakhir, TextView.class).setText(kehadiranArray.get(position).get("SCHEDULE_SELESAI").asString());
                        viewHolder.find(R.id.tv_kehadiran_hari, TextView.class).setText(kehadiranArray.get(position).get("NAMA_HARI").asString());
                        viewHolder.find(R.id.tv_kehadiran_status, TextView.class).setText(kehadiranArray.get(position).get("STATUS").asString());
                        viewHolder.find(R.id.tv_kehadiran_tglmulai, TextView.class).setText(kehadiranArray.get(position).get("TANGGAL_MULAI").asString());
                        viewHolder.find(R.id.tv_kehadiran_tglakhir, TextView.class).setText(kehadiranArray.get(position).get("TANGGAL_SELESAI").asString());
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                    }
                })
        );
    }

}