package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.Status_TugasPart_Activity;
import com.rkrzmail.oto.modules.sparepart.TugasPart_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
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
        viewPartTersedia();
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
        rvPartTersedia.setAdapter(new NikitaRecyclerAdapter(tersediaList, R.layout.item_batal_tersedia_permintaan_tugas_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(tersediaList.get(position).get("MEKANIK").asString());
                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(tersediaList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_nopol, TextView.class).setText(tersediaList.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.tv_tgl_checkin, TextView.class).setText(tersediaList.get(position).get("TANGGAL_CHECKIN").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(),  Status_TugasPart_Activity.class);
                i.putExtra(TUGAS_PART_TERSEDIA, "");
                i.putExtra(DATA, tersediaList.get(position).toJson());
                startActivityForResult(i, REQUEST_DETAIL);
            }
        }));
    }

    @SuppressLint("NewApi")
    private void viewPartTersedia() {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("detail", "");
                args.put("status", "TERSEDIA");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    tersediaList.asArray().clear();
                    tersediaList.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvPartTersedia.getAdapter()).notifyDataSetChanged();
                } else {
                    ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL)
            viewPartTersedia();
    }
}