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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturPembayaranInvoice_Activity;
import com.rkrzmail.oto.modules.checkin.History_Activity;
import com.rkrzmail.oto.modules.hutang.Piutang_MainTab_Activity;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.PIUTANG;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Invoice_Piutang_Fragment extends Fragment {
    
    private RecyclerView recyclerView;
    private View fragmentView;
    private AppActivity activity;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson invoiceList = Nson.newArray();
    
    public void Invoice_Piutang_Fragment (){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        activity = (Piutang_MainTab_Activity) getActivity();
        recyclerView = fragmentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        initHideToolbar();
        initRecylerViewInvoice();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewInvoice();
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
            viewInvoice();
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


    private void initRecylerViewInvoice(){
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(invoiceList, R.layout.item_invoice_piutang){
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);

                String tgl = DateFormatUtils.formatDate(invoiceList.get(position).get("TANGGAL_JATUH_TEMPO").asString(), "yyyy-MM-dd", "dd/MM");

                viewHolder.find(R.id.tv_nama_terhutang, TextView.class).setText(invoiceList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_nominal, TextView.class).setText(RP + NumberFormatUtils.formatRp(invoiceList.get(position).get("JUMLAH_INVOICE").asString()));
                viewHolder.find(R.id.tv_jatuh_tempo, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_no_invoice, TextView.class).setText(invoiceList.get(position).get("NO_INVOICE").asString());
                viewHolder.find(R.id.tv_tgl_invoice, TextView.class).setText(invoiceList.get(position).get("TANGGAL_INVOICE").asString());
                viewHolder.find(R.id.img_more, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.img_more, ImageButton.class));
                        popup.inflate(R.menu.menu_detail);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                if (menuItem.getItemId() == R.id.action_detail) {

                                }
                                return true;
                            }
                        });
                        popup.show();
                    }
                });
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturPembayaranInvoice_Activity.class);
                intent.putExtra(DATA, parent.get(position).toJson());
                startActivityForResult(intent, REQUEST_DETAIL);
            }
        }));
    }

    private void viewInvoice() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("jenis", "INVOICE");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PIUTANG), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    invoiceList.asArray().clear();
                    invoiceList.asArray().addAll(result.get("data").asArray());
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
            viewInvoice();
        }
    }
}
