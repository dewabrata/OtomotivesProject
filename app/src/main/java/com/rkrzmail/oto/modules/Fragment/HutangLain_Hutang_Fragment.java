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
import com.rkrzmail.oto.modules.hutang.AturPembayaranHutang_Activity;
import com.rkrzmail.oto.modules.hutang.Hutang_MainTab_Activity;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.HUTANG;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class HutangLain_Hutang_Fragment extends Fragment {


    private RecyclerView recyclerView;
    private View fragmentView;
    private AppActivity activity;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson hutangList = Nson.newArray();

    public HutangLain_Hutang_Fragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (Hutang_MainTab_Activity) getActivity();
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);
        initHideToolbar();
        initRvHutang();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewHutang();
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
            viewHutang();
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


    private void initRvHutang(){
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(hutangList, R.layout.item_hutang_lain){
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                String tgl = DateFormatUtils.formatDate(hutangList.get(position).get("CREATED_DATE").asString(), "yyyy-MM-dd HH:mm:ss", "dd/MM");
                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_tipe_kewajiban, TextView.class).setText(hutangList.get(position).get("TIPE_KEWAJIBAN").asString());
                if(hutangList.get(position).get("TIPE_KEWAJIBAN").asString().equals("DONASI")){
                    viewHolder.find(R.id.tv_bayar_sebelumnya, TextView.class).setText(RP + NumberFormatUtils.formatRp(hutangList.get(position).get("NOMINAL").asString()));
                }else{
                    viewHolder.find(R.id.tv_bayar_sebelumnya, TextView.class).setText(RP + NumberFormatUtils.formatRp(hutangList.get(position).get("").asString()));
                }
                viewHolder.find(R.id.tv_total, TextView.class).setText(RP + NumberFormatUtils.formatRp(hutangList.get(position).get("TOTAL_HUTANG").asString()));
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturPembayaranHutang_Activity.class);
                intent.putExtra(DATA, parent.get(position).toJson());
                intent.putExtra("HUTANG_LAIN", true);
                startActivityForResult(intent, REQUEST_DETAIL);
            }
        }));
    }

    private void viewHutang() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("jenis", "HUTANG LAIN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(HUTANG), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    hutangList.asArray().clear();
                    hutangList.asArray().addAll(result.get("data").asArray());
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
            viewHutang();
        }
    }
}
