package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.ConstUtils;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.APIUrls.FEE_BILLING;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Billing_Activity extends AppActivity {

    private RecyclerView recyclerView;

    private Nson terbayarList = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        setComponent();
        viewBilling();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Fee Terbayar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setComponent(){
        initRv();
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AturFeeBilling_Activity.class);
                startActivityForResult(intent, ConstUtils.REQUEST_BIAYA);
            }
        });
    }

    private void initRv(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(terbayarList, R.layout.item_billing_terbayar){
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_bulan, TextView.class).setText(terbayarList.get(position).get("BULAN").asString());
                viewHolder.find(R.id.tv_tgl_bayar, TextView.class).setText(terbayarList.get(position).get("TANGGAL_BAYAR").asString());
                viewHolder.find(R.id.tv_total_billing, TextView.class).setText(RP + NumberFormatUtils.formatRp(terbayarList.get(position).get("TOTAL_FEE").asString()));
                viewHolder.find(R.id.tv_cashback, TextView.class).setText(RP + NumberFormatUtils.formatRp(terbayarList.get(position).get("CASHBACK").asString()));
                viewHolder.find(R.id.tv_total_bayar, TextView.class).setText(RP + NumberFormatUtils.formatRp(terbayarList.get(position).get("NOMINAL").asString()));

                String bankOto = terbayarList.get(position).get("NAMA_BANK").asString() + " - "  + terbayarList.get(position).get("REKENING_BANK").asString();
                viewHolder.find(R.id.tv_bank_oto, TextView.class).setText(bankOto);

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

            }
        }));
    }


    private void viewBilling() {
        if(!Tools.isNetworkAvailable(getActivity())){
            showWarning("TIDAK ADA KONEKSI INTERNET");
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    terbayarList.asArray().clear();
                    terbayarList.asArray().addAll(result.asArray());
                    Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
            viewBilling();
    }
}