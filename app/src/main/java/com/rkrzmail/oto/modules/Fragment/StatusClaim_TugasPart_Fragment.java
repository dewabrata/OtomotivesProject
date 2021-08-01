package com.rkrzmail.oto.modules.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
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
import com.rkrzmail.oto.modules.mekanik.ClaimGaransiStatus_Activity;
import com.rkrzmail.oto.modules.Adapter.TugasPart_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class StatusClaim_TugasPart_Fragment extends Fragment {

    View fragmentView;
    RecyclerView rvClaimGaransi;
    
    private final Nson garansiList = Nson.newArray();
    private AppActivity activity;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        activity = (TugasPart_MainTab_Activity) getActivity();

        initHideToolbar();
        initRv();
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initRv() {
        rvClaimGaransi = fragmentView.findViewById(R.id.recyclerView);
        rvClaimGaransi.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvClaimGaransi.setHasFixedSize(true);
        rvClaimGaransi.setAdapter(new NikitaRecyclerAdapter(garansiList, R.layout.item_claimgaransi_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tv_nopolisi, TextView.class).setText(garansiList.get(position).get("NO_POLISI").asString());
                viewHolder.find(R.id.tv_jenisKendaraan, TextView.class).setText(garansiList.get(position).get("JENIS_KENDARAAN").asString());
                viewHolder.find(R.id.tv_tgl_beli, TextView.class).setText(garansiList.get(position).get("TANGGAL_PEMBELIAN").asString());
                viewHolder.find(R.id.tv_km, TextView.class).setText(garansiList.get(position).get("KM").asString());
                viewHolder.find(R.id.tv_tgl_lkk_claim, TextView.class).setText(garansiList.get(position).get("TANGGAL_LKK").asString());
                viewHolder.find(R.id.tv_nama_part, TextView.class).setText(garansiList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_claim, TextView.class).setText(garansiList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_harga, TextView.class).setText(garansiList.get(position).get("BIAYA_TRANSFER").asString());
                viewHolder.find(R.id.tv_status, TextView.class).setText(garansiList.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_keterangan, TextView.class).setText(garansiList.get(position).get("KETERANGAN").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), ClaimGaransiStatus_Activity.class);
                i.putExtra(DATA, garansiList.get(position).toJson());
                startActivity(i);
            }
        }));
        catchData();

    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CLAIM), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    garansiList.asArray().clear();
                    garansiList.asArray().addAll(result.get("data").asArray());
                    if(rvClaimGaransi.getAdapter() != null )
                        rvClaimGaransi.getAdapter().notifyDataSetChanged();
                } else {
                    activity.showError(result.get("message").asString());
                }
            }
        });
    }


}
