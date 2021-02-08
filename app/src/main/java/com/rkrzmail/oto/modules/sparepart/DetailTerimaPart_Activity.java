package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_TERIMA_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class DetailTerimaPart_Activity extends AppActivity {

    private EditText etNamaSup, etUser, etTglTerima, etTglPesan, etNodo, etPembayaran, etInv, etOngkir, etNoSupplier;
    private RecyclerView rvTerimaPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_terima_part_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Detail Terima Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etNamaSup = findViewById(R.id.et_namaSup_terimaPart);
        etUser = findViewById(R.id.et_user_terimaPart);
        etTglTerima = findViewById(R.id.et_tglTerima_terimaPart);
        etTglPesan = findViewById(R.id.et_tglPesan_terimaPart);
        etNodo = findViewById(R.id.et_nodo_terimaPart);
        etPembayaran = findViewById(R.id.et_pembayaran_terimaPart);
        etInv = findViewById(R.id.et_tempoInv_terimaPart);
        etOngkir = findViewById(R.id.et_ongkir_terimaPart);
        rvTerimaPart = findViewById(R.id.recyclerView);
        etNoSupplier = findViewById(R.id.et_noSup_terimaPart);

        loadData();
        initRecylerview();
    }

    private void loadData() {
        Nson data = Nson.readJson(getIntentStringExtra(PART));
        viewDetailTerimaPart(data);
        if(data.get("NAMA_SUPPLIER").asString().isEmpty() && data.get("NO_PONSEL_SUPPLIER").asString().isEmpty()){
            etNamaSup.setVisibility(View.GONE);
            etNoSupplier.setVisibility(View.GONE);
        }
        String noHp = "";
        if(!data.get("NO_PONSEL_SUPPLIER").asString().startsWith("+")){
            noHp = "+" + data.get("NO_PONSEL_SUPPLIER").asString();
        }
        etNamaSup.setText(data.get("NAMA_SUPPLIER").asString());
        etNoSupplier.setText(noHp);
        etUser.setText(data.get("NAMA_USER").asString());
        etTglTerima.setText(data.get("TANGGAL_PENERIMAAN").asString());
        etTglPesan.setText(data.get("TANGGAL_PESAN").asString());
        etNodo.setText(data.get("NO_DO").asString());
        etPembayaran.setText(data.get("PEMBAYARAN").asString());
        etInv.setText(data.get("JATUH_TEMPO_INV ").asString());
        etOngkir.setText(data.get("ONGKOS_KIRIM").asString());
    }

    private void initRecylerview(){
        rvTerimaPart.setLayoutManager(new LinearLayoutManager(this));
        rvTerimaPart.setHasFixedSize(true);
        rvTerimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_di_terima) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_noPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_namaPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_jumlah_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.tv_net_detailTerimaPart, TextView.class).setText(RP + formatRp(nListArray.get(position).get("NET").asString()));
                viewHolder.find(R.id.tv_harga_detailTerimaPart, TextView.class).setText(RP + formatRp(nListArray.get(position).get("HARGA_BELI").asString()));
                viewHolder.find(R.id.tv_disc_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("DISCOUNT").asString());
                viewHolder.find(R.id.tv_merk_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
            }
        });
    }

    private void viewDetailTerimaPart(final Nson idTerimaPart){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("flag", "DETAIL");
                args.put("id", idTerimaPart.get("ID").asString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TERIMA_PART),args)) ;
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d("Terima__", "detail: " + result.get("data"));
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvTerimaPart.getAdapter().notifyDataSetChanged();
                }else {
                    showError(result.get("message").asString());
                }
            }
        });
    }
}
