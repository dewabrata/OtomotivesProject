package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.discount.AturDiscountJasaLain_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;
import java.util.zip.Inflater;

import static com.rkrzmail.utils.ConstUtils.DATA;

public class OutSource_Activity extends Fragment {

    private RecyclerView recyclerView;
    private Nson nListArray = Nson.newArray();

    public OutSource_Activity() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        recyclerView = view.findViewById(R.id.recyclerView);
        initComponent();
        return view;
    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initComponent() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_outsource) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_nopol_outS, TextView.class).setText(nListArray.get(position).get("KATEGORI_JASA_LAIN").asString());
                        viewHolder.find(R.id.tv_tglCheckin_outS, TextView.class).setText("");
                        viewHolder.find(R.id.tv_tglOutS_outS, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_tglSelesai_outS, TextView.class).setText(nListArray.get(position).get("DISC_JASA").asString());
                        viewHolder.find(R.id.tv_biaya_outS, TextView.class).setText(nListArray.get(position).get("DISC_JASA").asString());
                        viewHolder.find(R.id.tv_pembayaran_outS, TextView.class).setText(nListArray.get(position).get("DISC_JASA").asString());
                        viewHolder.find(R.id.tv_supplier_outS, TextView.class).setText(nListArray.get(position).get("DISC_JASA").asString());
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), AturDiscountJasaLain_Activity.class);
                        i.putExtra(DATA, nListArray.get(position).toJson());
                        startActivity(i);
                    }
                })
        );

        catchData("");
    }

    @SuppressLint("NewApi")
    private void catchData(final String cari) {
        ((TugasPart_MainTab_Activity) Objects.requireNonNull(getActivity())).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                args.put("action", "view");
//                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
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

}
