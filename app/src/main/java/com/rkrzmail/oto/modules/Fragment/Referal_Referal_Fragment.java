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
import com.rkrzmail.oto.modules.bengkel.AturReferal_Activity;
import com.rkrzmail.oto.modules.Adapter.Referal_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.REFERAL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class Referal_Referal_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private RecyclerView rvRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final Nson refList = Nson.newArray();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_list_basic_with_fab, container, false);
        activity = (Referal_MainTab_Activity) getActivity();
        rvRef = fragmentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        initHideToolbar();
        initRv();

        fragmentView.findViewById(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), AturReferal_Activity.class), REQUEST_DETAIL);
            }
        });

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
        rvRef.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        rvRef.setHasFixedSize(true);
        rvRef.setAdapter(new NikitaRecyclerAdapter(refList, R.layout.item_referal) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_kontak, TextView.class).setText(refList.get(position).get("KONTAK_PERSON").asString());
                viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(refList.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.tv_nama_bengkel, TextView.class).setText(refList.get(position).get("NAMA_BENGKEL").asString());
                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(refList.get(position).get("CREATED_DATE").asString());
                viewHolder.find(R.id.tv_status, TextView.class).setText(refList.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_kode_ref, TextView.class).setText(refList.get(position).get("NO_REFERRAL").asString());
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
                args[1] = "userRefferee=" + UtilityAndroid.getSetting(getContext(), "user", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(REFERAL), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    refList.asArray().clear();
                    refList.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvRef.getAdapter()).notifyDataSetChanged();
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
