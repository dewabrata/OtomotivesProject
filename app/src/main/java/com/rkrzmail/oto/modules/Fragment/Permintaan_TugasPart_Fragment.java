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
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;

public class Permintaan_TugasPart_Fragment extends Fragment {

    RecyclerView rvPermintaanPart;
    Nson permintaanPartList = Nson.newArray();

    public Permintaan_TugasPart_Fragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Nson dataList = Nson.newArray();
            dataList.add(getArguments().getString(TUGAS_PART_PERMINTAAN));
            Log.d("Tersedia__", "onCreate: " + dataList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        rvPermintaanPart = view.findViewById(R.id.recyclerView);
        initRecylerviewPermintaan();
        viewPartPermintaan();
        return view;
    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRecylerviewPermintaan(){
        rvPermintaanPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPermintaanPart.setHasFixedSize(true);
        rvPermintaanPart.setAdapter(new NikitaRecyclerAdapter(permintaanPartList, R.layout.item_batal_tersedia_permintaan_tugas_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(permintaanPartList.get(position).get("MEKANIK").asString());
                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(permintaanPartList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_nopol, TextView.class).setText(permintaanPartList.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.tv_tgl_checkin, TextView.class).setText(permintaanPartList.get(position).get("TANGGAL_CHECKIN").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), Status_TugasPart_Activity.class);
                i.putExtra(TUGAS_PART_PERMINTAAN, "");
                i.putExtra(DATA, permintaanPartList.get(position).toJson());
                startActivityForResult(i, REQUEST_DETAIL);
            }
        }));
    }

    @SuppressLint("NewApi")
    private void viewPartPermintaan() {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("detail", "");
                args.put("status", "PERMINTAAN");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    permintaanPartList.asArray().clear();
                    permintaanPartList.asArray().addAll(result.get("data").asArray());
                    rvPermintaanPart.getAdapter().notifyDataSetChanged();
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
            viewPartPermintaan();
    }
}