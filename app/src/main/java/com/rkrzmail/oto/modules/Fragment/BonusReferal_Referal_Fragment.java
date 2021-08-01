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
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Referal_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.BONUS_REFFERAL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class BonusReferal_Referal_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private RecyclerView rvBonus;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final Nson bonusList = Nson.newArray();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        activity = (Referal_MainTab_Activity) getActivity();
        rvBonus = fragmentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        initHideToolbar();
        initRv();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVisible()){
            viewList();
        }
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRv() {
        rvBonus.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        rvBonus.setHasFixedSize(true);
        rvBonus.setAdapter(new NikitaRecyclerAdapter(bonusList, R.layout.item_bonus_refferal) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_kontak, TextView.class).setText(bonusList.get(position).get("NAMA_KONTAK").asString());
                viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(bonusList.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.tv_nama_bengkel, TextView.class).setText(bonusList.get(position).get("NAMA_BENGKEL").asString());
                viewHolder.find(R.id.tv_tgl_aktif, TextView.class).setText(bonusList.get(position).get("TANGGAL_AKTIVE").asString());
                viewHolder.find(R.id.tv_jumlah_fee, TextView.class).setText(RP + NumberFormatUtils.formatRp(bonusList.get(position).get("TOTAL_FEE").asString()));
                viewHolder.find(R.id.tv_jumlah_bonus, TextView.class).setText(RP + NumberFormatUtils.formatRp(bonusList.get(position).get("TOTAL_BONUS").asString()));
                viewHolder.find(R.id.tv_tgl_transfer, TextView.class).setText(bonusList.get(position).get("TANGGAL_TRANSFER").asString());
            }
        });
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

    private void viewList() {
        activity.newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(BONUS_REFFERAL), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    bonusList.asArray().clear();
                    bonusList.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvBonus.getAdapter()).notifyDataSetChanged();
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL) {

        }
    }
}
