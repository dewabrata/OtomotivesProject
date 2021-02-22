package com.rkrzmail.oto.modules.mekanik;

import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;

public class ClaimGaransiPart_Activity extends AppActivity {

    RecyclerView rvClaimGaransi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_garansi_part_);
        initComponent();
    }

    private void initComponent() {
        rvClaimGaransi = findViewById(R.id.recyclerViewClaimGaransiPart);
        rvClaimGaransi.setLayoutManager(new LinearLayoutManager(this));
        rvClaimGaransi.setHasFixedSize(true);
        rvClaimGaransi.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_claimgaransi_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tv_nopolisi, TextView.class).setText(nListArray.get(position).get("NAMA_MEKANIK").asString());
                viewHolder.find(R.id.tv_jenisKendaraan, TextView.class).setText(nListArray.get(position).get("TIPE_ANTRIAN").asString());
                viewHolder.find(R.id.tv_tgl_beli, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_km, TextView.class).setText(nListArray.get(position).get("JAM_MASUK").asString());
                viewHolder.find(R.id.tv_tgl_lkk_claim, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_nama_part, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_noPart_claim, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_harga, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_keterangan, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
            }
        });
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