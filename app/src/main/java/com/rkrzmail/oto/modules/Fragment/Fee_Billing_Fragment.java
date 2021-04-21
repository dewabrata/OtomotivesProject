package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturFeeBilling_Activity;
import com.rkrzmail.oto.modules.bengkel.Billing_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.FEE_BILLING;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BIAYA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Fee_Billing_Fragment extends Fragment {

    private View fragmentView, dialogView;
    private AppActivity activity;
    private RecyclerView rvDetailFee, rvFeeBilling;
    private Button btnBayarFee;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog alertDialog;

    private final Nson feeList = Nson.newArray();
    private final Nson detailFeeList = Nson.newArray();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_fee_billing, container, false);
        activity = (Billing_MainTab_Activity) getActivity();
        rvFeeBilling = fragmentView.findViewById(R.id.recyclerView);
        btnBayarFee = fragmentView.findViewById(R.id.btn_bayar_fee);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        btnBayarFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AturFeeBilling_Activity.class);
                startActivityForResult(intent, REQUEST_BIAYA);
            }
        });

        initHideToolbar();
        initRvBilling();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewFee(false, "");
        }
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRvBilling() {
        rvFeeBilling.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        rvFeeBilling.setHasFixedSize(true);
        rvFeeBilling.setAdapter(new NikitaRecyclerAdapter(feeList, R.layout.item_fee_billing) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(feeList.get(position).get("").asString());
                viewHolder.find(R.id.tv_total_fee, TextView.class).setText(RP + NumberFormatUtils.formatRp(feeList.get(position).get("").asString()));
            }

            @Override
            public int getItemCount() {
                btnBayarFee.setEnabled(!feeList.asArray().isEmpty());
                return feeList.size();
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                showDialogDetail(parent.get(position).get("TANGGAL").asString());
            }
        }));
    }

    private void initRvDetail() {
        rvDetailFee = dialogView.findViewById(R.id.recyclerView);
        rvDetailFee.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        rvDetailFee.setHasFixedSize(true);
        rvDetailFee.setAdapter(new NikitaRecyclerAdapter(detailFeeList, R.layout.item_detail_fee_billing) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(detailFeeList.get(position).get("").asString());
                viewHolder.find(R.id.tv_no_bukti_bayar, TextView.class).setText(detailFeeList.get(position).get("").asString());
                viewHolder.find(R.id.tv_total_transaksi, TextView.class).setText(RP + NumberFormatUtils.formatRp(detailFeeList.get(position).get("").asString()));
                viewHolder.find(R.id.tv_total_fee, TextView.class).setText(RP + NumberFormatUtils.formatRp(detailFeeList.get(position).get("").asString()));
            }
        });
    }

    @SuppressLint("NewApi")
    private void initToolbarDetail() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Detail Fee");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @SuppressLint("InflateParams")
    private void showDialogDetail(String tanggal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        initToolbarDetail();
        initRvDetail();
        SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);
        viewFee(true, tanggal);
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


    private void viewFee(final boolean isDetail, final String tanggal) {
        activity.newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {

                Map<String, String> args = AppApplication.getInstance().getArgsData();
                if(isDetail){
                    args.put("action", "DETAIL FEE");
                    args.put("tanggal", tanggal);
                }else{
                    swipeProgress(true);
                    args.put("action", "FEE");
                    args.put("", "");
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @Override
            public void runUI() {
                if(!isDetail) swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(isDetail){
                        detailFeeList.asArray().clear();
                        detailFeeList.asArray().addAll(result.get("data").asArray());
                        rvDetailFee.getAdapter().notifyDataSetChanged();
                        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        alertDialog.show();
                    }else{
                        feeList.asArray().clear();
                        feeList.asArray().addAll(result.get("data").asArray());
                        rvFeeBilling.getAdapter().notifyDataSetChanged();
                    }

                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_BIAYA) {
                viewFee(false, "");
            }
        }
    }
}
