package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class ClaimGaransiPart_Activity extends AppActivity {

    public static final int REQUEST_CLAIM = 99;
    RecyclerView rvClaimGaransi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Claim Garansi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        rvClaimGaransi = findViewById(R.id.recyclerView);
        rvClaimGaransi.setLayoutManager(new LinearLayoutManager(this));
        rvClaimGaransi.setHasFixedSize(true);
        rvClaimGaransi.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_claimgaransi_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tv_nopolisi, TextView.class).setText(nListArray.get(position).get("NO_POLISI").asString());
                viewHolder.find(R.id.tv_jenisKendaraan, TextView.class).setText(nListArray.get(position).get("JENIS_KENDARAAN").asString());
                viewHolder.find(R.id.tv_tgl_beli, TextView.class).setText(nListArray.get(position).get("TANGGAL_PEMBELIAN").asString());
                viewHolder.find(R.id.tv_km, TextView.class).setText(nListArray.get(position).get("KM").asString());
                viewHolder.find(R.id.tv_tgl_lkk_claim, TextView.class).setText(nListArray.get(position).get("TANGGAL_LKK").asString());
                viewHolder.find(R.id.tv_nama_part, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_claim, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_harga, TextView.class).setText(nListArray.get(position).get("BIAYA_TRANSFER").asString());
                viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_keterangan, TextView.class).setText(nListArray.get(position).get("KETERANGAN").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), ClaimGaransiStatus_Activity.class);
                i.putExtra(DATA, nListArray.get(position).toJson());
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
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvClaimGaransi.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }
}