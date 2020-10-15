package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.rkrzmail.oto.modules.sparepart.Status_PartKosong_Activity;
import com.rkrzmail.oto.modules.sparepart.TugasPart_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PART_KOSONG;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_KOSONG;

public class PartKosong_TugasPart_Fragment extends Fragment {


    RecyclerView rvPartKosong;
    private Nson partKosongList = Nson.newArray();

    public PartKosong_TugasPart_Fragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Nson dataList = Nson.newArray();
            dataList.add(getArguments().getString(TUGAS_PART_KOSONG));
            Log.d("Tersedia__", "onCreate: " + dataList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        rvPartKosong = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRecylerview();
        viewPartKosong();
    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRecylerview(){
        rvPartKosong.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPartKosong.setHasFixedSize(true);
        rvPartKosong.setAdapter(new NikitaRecyclerAdapter(partKosongList, R.layout.item_part_kosong){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nopol, TextView.class).setText(partKosongList.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.tv_namaPart, TextView.class).setText(partKosongList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart, TextView.class).setText(partKosongList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_jumlah, TextView.class).setText(partKosongList.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.tv_merk, TextView.class).setText(partKosongList.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_harga, TextView.class).setText(partKosongList.get(position).get("HARGA").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), Status_PartKosong_Activity.class);
                i.putExtra(DATA, partKosongList.get(position).toJson());
                startActivityForResult(i, REQUEST_PART_KOSONG);
            }
        }));
    }

    @SuppressLint("NewApi")
    private void viewPartKosong() {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("parts", "KOSONG");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partKosongList.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvPartKosong.getAdapter()).notifyDataSetChanged();
                } else {
                    ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).showInfo("Gagal Memperbaharui Status");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_PART_KOSONG){
            viewPartKosong();
        }
    }
}