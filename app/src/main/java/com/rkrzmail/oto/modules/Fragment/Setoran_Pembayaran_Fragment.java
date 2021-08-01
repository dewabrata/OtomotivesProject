package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Pembayaran_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.RP;


public class Setoran_Pembayaran_Fragment extends Fragment {


    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvSetoran;
    private Nson setoranList = Nson.newArray();

    public Setoran_Pembayaran_Fragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        initRecylerviewPembayaran(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewSetoran();
        }
    }


    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
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

    private void initRecylerviewPembayaran(View view) {
        rvSetoran = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvSetoran.setHasFixedSize(true);
        rvSetoran.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSetoran.setAdapter(new NikitaRecyclerAdapter(setoranList, R.layout.item_setoran) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_tanggal, TextView.class).setText(setoranList.get(position).get("TANGGAL").asString());
                viewHolder.find(R.id.tv_total, TextView.class).setText(RP + ((Pembayaran_MainTab_Activity) Objects.requireNonNull(getActivity())).formatRp(setoranList.get(position).get("TOTAL_BAYAR").asString()));
                viewHolder.find(R.id.tv_terhutang, TextView.class).setText(RP + ((Pembayaran_MainTab_Activity)getActivity()).formatRp(setoranList.get(position).get("LEBIH_KURANG").asString()));
                viewHolder.find(R.id.tv_tipe_setoran, TextView.class).setText(setoranList.get(position).get("TIPE_SETORAN").asString());
                viewHolder.find(R.id.tv_penerima, TextView.class).setText(setoranList.get(position).get("PENERIMA").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

            }
        }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewSetoran();
            }
        });
    }

    private void viewSetoran() {
        ((Pembayaran_MainTab_Activity) Objects.requireNonNull(getActivity())).newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "SETORAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                setoranList.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                   rvSetoran.getAdapter().notifyDataSetChanged();
                } else {
                    ((Pembayaran_MainTab_Activity) getActivity()).showError(ERROR_INFO);
                }
            }
        });
    }


}