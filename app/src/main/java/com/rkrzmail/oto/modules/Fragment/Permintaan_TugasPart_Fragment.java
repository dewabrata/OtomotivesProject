package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;


public class Permintaan_TugasPart_Fragment extends Fragment {

    public RecyclerView rvPermintaan;
    public Nson permintaanList = Nson.newArray();
    boolean isVisited = false;

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
        rvPermintaan = view.findViewById(R.id.recyclerView);
        initRecylerviewPermintaan();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVisible()){
            viewPartPermintaan();
            Log.d("visi__", "setUserVisibleHint: " + "visible tersedia");
        }else{
            Log.d("visi__", "setUserVisibleHint: " + "invisible tersedia");
        }

    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewPermintaan() {
        rvPermintaan.setVisibility(View.VISIBLE);
        rvPermintaan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPermintaan.setHasFixedSize(true);
        rvPermintaan.setAdapter(new NikitaMultipleViewAdapter(permintaanList, R.layout.item_tersedia_permintaan_tugas_part, R.layout.item_permintaan_jual_part_tugas_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                int viewType = getItemViewType(position);
                if(viewType == ITEM_VIEW_1){
                    viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(permintaanList.get(position).get("MEKANIK").asString());
                    viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(permintaanList.get(position).get("NAMA_PELANGGAN").asString());
                    viewHolder.find(R.id.tv_nopol, TextView.class).setText(permintaanList.get(position).get("NOPOL").asString());
                    viewHolder.find(R.id.tv_tgl_checkin, TextView.class).setText(permintaanList.get(position).get("TANGGAL_CHECKIN").asString());
                    viewHolder.find(R.id.cv_tugas_part_checkin, CardView.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getActivity(), Status_TugasPart_Activity.class);
                            i.putExtra(TUGAS_PART_PERMINTAAN, "CHECKIN");
                            i.putExtra(DATA, permintaanList.get(position).toJson());
                            startActivityForResult(i, REQUEST_DETAIL);
                        }
                    });
                    
                }else{
                    String noHp = permintaanList.get(position).get("NO_PONSEL").asString();
                    if(noHp.length() > 4){
                        noHp = noHp.substring(noHp.length() - 4);
                    }
                    viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(permintaanList.get(position).get("USER_JUAL").asString());
                    viewHolder.find(R.id.tv_nama_pelanggan_nama_usaha, TextView.class).setText(
                            permintaanList.get(position).get("NAMA_PELANGGAN").asString() + " " + permintaanList.get(position).get("NAMA_USAHA").asString());
                    viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText("XXXXXXXX" + noHp);
                    viewHolder.find(R.id.tv_tgl, TextView.class).setText(permintaanList.get(position).get("TANGGAL").asString());
                    viewHolder.find(R.id.cv_pembayaran_jual_part, CardView.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getActivity(), Status_TugasPart_Activity.class);
                            i.putExtra(TUGAS_PART_PERMINTAAN, "JUAL PART");
                            i.putExtra(DATA, permintaanList.get(position).toJson());
                            startActivityForResult(i, REQUEST_DETAIL);
                        }
                    });
                }
            }
        });
    }
    
    @SuppressLint("NewApi")
    public void viewPartPermintaan() {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("detail", "");
                args.put("status", "PERMINTAAN");
                args.put("group", "CHECKIN");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
                permintaanList.asArray().clear();
                permintaanList.asArray().addAll(result.get("data").asArray());
                
                args.remove("group");
                args.put("group", "JUAL PART");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
                permintaanList.asArray().addAll(result.get("data").asArray());
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                   
                    
                    rvPermintaan.getAdapter().notifyDataSetChanged();
                } else {
                    ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL){
            viewPartPermintaan();
        }
    }
}