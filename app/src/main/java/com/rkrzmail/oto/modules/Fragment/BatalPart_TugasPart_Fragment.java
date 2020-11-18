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
import com.rkrzmail.oto.modules.sparepart.JumlahPart_TugasPart_Activity;
import com.rkrzmail.oto.modules.sparepart.TugasPart_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_BATAL;

public class BatalPart_TugasPart_Fragment extends Fragment {

    private RecyclerView rvBatalPart;
    private Nson partBatalList = Nson.newArray();

    public BatalPart_TugasPart_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Nson dataList = Nson.newArray();
            dataList.add(getArguments().getString(TUGAS_PART_BATAL));
            Log.d("Tersedia__", "onCreate: " + dataList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        initRecylerviewBatalPart(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewPartBatal();
        }
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRecylerviewBatalPart(View view) {
        rvBatalPart = view.findViewById(R.id.recyclerView);
        rvBatalPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvBatalPart.setHasFixedSize(true);
        rvBatalPart.setAdapter(new NikitaRecyclerAdapter(partBatalList, R.layout.item_status_tugas_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_merk_statusTp, TextView.class).setText(partBatalList.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_namaPart_statusTp, TextView.class).setText(partBatalList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_statusTp, TextView.class).setText(partBatalList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_jumlah, TextView.class).setText(partBatalList.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.tv_kode_lokasi_or_tersedia, TextView.class).setText(partBatalList.get(position).get("MEKANIK").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), JumlahPart_TugasPart_Activity.class);
                i.putExtra(TUGAS_PART_BATAL, "");
                i.putExtra(DATA, partBatalList.get(position).toJson());
                startActivityForResult(i, REQUEST_DETAIL);
            }
        }));
    }


    @SuppressLint("NewApi")
    private void viewPartBatal() {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("detail", "");
                args.put("status", "BATAL PART");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partBatalList.asArray().clear();
                    partBatalList.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvBatalPart.getAdapter()).notifyDataSetChanged();
                } else {
                    ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).showWarning(ERROR_INFO);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL) {
            viewPartBatal();
        }
    }
}