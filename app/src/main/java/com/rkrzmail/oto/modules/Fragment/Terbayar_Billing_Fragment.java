package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.rkrzmail.oto.modules.bengkel.Billing_MainTab_Activity;
import com.rkrzmail.oto.modules.hutang.AturPembayaranHutang_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.FEE_BILLING;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Terbayar_Billing_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private View fragmentView;
    private AppActivity activity;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson terbayarList = Nson.newArray();

    public Terbayar_Billing_Fragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (Billing_MainTab_Activity) getActivity();
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        initRv();
        initHideToolbar();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewTerbayar();
            }
        });
        return fragmentView;
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVisible()){
            viewTerbayar();
        }
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


    private void initRv(){
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(terbayarList, R.layout.item_billing_terbayar){
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_bulan, TextView.class).setText(terbayarList.get(position).get("").asString());
                viewHolder.find(R.id.tv_tgl_bayar, TextView.class).setText(terbayarList.get(position).get("").asString());
                viewHolder.find(R.id.tv_total_billing, TextView.class).setText(RP + NumberFormatUtils.formatRp(terbayarList.get(position).get("").asString()));
                viewHolder.find(R.id.tv_cashback, TextView.class).setText(RP + NumberFormatUtils.formatRp(terbayarList.get(position).get("").asString()));
                viewHolder.find(R.id.tv_total_bayar, TextView.class).setText(RP + NumberFormatUtils.formatRp(terbayarList.get(position).get("").asString()));

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

            }
        }));
    }

    private void viewTerbayar() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "TERBAYAR");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    terbayarList.asArray().clear();
                    terbayarList.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL){
            viewTerbayar();
        }
    }
}
