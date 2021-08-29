package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.rkrzmail.oto.modules.sparepart.AturOutSource_Activity;
import com.rkrzmail.oto.modules.Adapter.TugasPart_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class OutSource_TugasPart_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private Nson nListArray = Nson.newArray();

    private AppActivity activity;

    public OutSource_TugasPart_Fragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        activity = (TugasPart_MainTab_Activity) getActivity();

        initHideToolbar(view);
        recyclerView = view.findViewById(R.id.recyclerView);
        initRv();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVisible()){
            viewData("");
        }
    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRv() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_outsource) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_nopol, TextView.class).setText(activity.formatNopol(nListArray.get(position).get("NOPOL").asString()));
                        viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                        viewHolder.find(R.id.tv_tgl_outsource, TextView.class).setText(nListArray.get(position).get("TANGGAL_OUTSOURCE").asString());
                        viewHolder.find(R.id.tv_kelompok_part, TextView.class).setText(nListArray.get(position).get("KELOMPOK_PART").asString());
                        viewHolder.find(R.id.tv_biaya, TextView.class).setText(nListArray.get(position).get("BIAYA_OUTSOURCE").asString());
                        viewHolder.find(R.id.tv_aktivitas_jasa, TextView.class).setText(nListArray.get(position).get("AKTIVITAS").asString());
                        viewHolder.find(R.id.tv_supplier, TextView.class).setText(nListArray.get(position).get("SUPPLIER").asString());
                        viewHolder.find(R.id.tv_harga_jasa_lain, TextView.class).setText(RP + NumberFormatUtils.formatRp(nListArray.get(position).get("HARGA_JASA_LAIN").asString()));
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        if(nListArray.get(position).get("STATUS").asString().equals("SELESAI")){
                            activity.showWarning("OUTSOURCE SUDAH SELESAI!");
                        }else{
                            Intent i = new Intent(getActivity(), AturOutSource_Activity.class);
                            i.putExtra(DATA, nListArray.get(position).toJson());
                            startActivity(i);
                        }
                    }
                })
        );
    }

    @SuppressLint("NewApi")
    private void viewData(final String cari) {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("status", "OUTSOURCE");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            viewData("");
        }
    }
}
