package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;
import com.rkrzmail.oto.modules.bengkel.User_MainTab_Activity;
import com.rkrzmail.oto.modules.hutang.AturPembayaranHutang_Activity;
import com.rkrzmail.oto.modules.hutang.Hutang_MainTab_Activity;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.HUTANG;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class DaftarUser_User_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private View fragmentView;
    private AppActivity activity;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson userList = Nson.newArray();


    public DaftarUser_User_Fragment(){
        
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (User_MainTab_Activity) getActivity();
        fragmentView = inflater.inflate(R.layout.activity_list_basic_with_fab, container, false);
        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);
        
        FloatingActionButton fab = (FloatingActionButton) fragmentView.findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturUser_Activity.class), REQUEST_DETAIL);
            }
        });

        initHideToolbar();
        initRv();
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
            viewUser("");
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

    private void initRv() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(userList, R.layout.item_user) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtNamaKaryawan, TextView.class).setText(userList.get(position).get("NAMA").asString());
                viewHolder.find(R.id.txtNoPonsel, TextView.class).setText("+" + userList.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.txtPosisi, TextView.class).setText(userList.get(position).get("POSISI").asString());
                viewHolder.find(R.id.tv_fungsiMekanik, TextView.class).setText(userList.get(position).get("FUNGSI_MEKANIK").asString());
                viewHolder.find(R.id.txtStatus, TextView.class).setText(userList.get(position).get("STATUS").asString());
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturUser_Activity.class);
                intent.putExtra(DATA, userList.get(position).toJson());
                startActivityForResult(intent, 10);
            }
        }));
    }

    private void viewUser(final String cari) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    userList.asArray().clear();
                    userList.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }else{
                    activity.showError("Mohon di coba kembali");
                }
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL){
            viewUser("");
        }
    }
}
