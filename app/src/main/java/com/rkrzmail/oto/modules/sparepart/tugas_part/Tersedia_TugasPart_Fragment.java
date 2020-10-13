package com.rkrzmail.oto.modules.sparepart.tugas_part;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_TERSEDIA;


public class Tersedia_TugasPart_Fragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    RecyclerView rvPartTersedia;

    private Nson tersediaList = Nson.newArray();

    public Tersedia_TugasPart_Fragment() {

    }

    public static Tersedia_TugasPart_Fragment newInstance(String param1, String param2) {
        Tersedia_TugasPart_Fragment fragment = new Tersedia_TugasPart_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Nson dataList = Nson.newArray();
            dataList.add(getArguments().getString(TUGAS_PART_TERSEDIA));
            Log.d("Tersedia__", "onCreate: " + dataList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        initComponent(view);
        return view;
    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initComponent(View view){
        rvPartTersedia = view.findViewById(R.id.recyclerView);
        rvPartTersedia.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPartTersedia.setHasFixedSize(true);
        rvPartTersedia.setAdapter(new NikitaRecyclerAdapter(tersediaList, R.layout.item_tersedia_tugas_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(tersediaList.get(position).get("MEKANIK").asString());
                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(tersediaList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_nopol, TextView.class).setText(tersediaList.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.tv_tgl_checkin, TextView.class).setText(tersediaList.get(position).get("TANGGAL_CHECKIN").asString());
            }
        });
    }

}