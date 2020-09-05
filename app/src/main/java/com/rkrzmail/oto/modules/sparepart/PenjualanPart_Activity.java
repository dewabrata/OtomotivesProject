package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class PenjualanPart_Activity extends AppActivity {

    private RecyclerView rvJualPart;
    public static final int REQUEST_PENJUALAN = 110;
    public static final int RESULT_DETAIL = 5;
    private boolean isNamaUsaha = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_3);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penjualan Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        FloatingActionButton fab = findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturPenjualanPart_Activity.class), REQUEST_PENJUALAN);
            }
        });

        rvJualPart = findViewById(R.id.recyclerView);
        rvJualPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJualPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jual_part) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tgl = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("TANGGAL").asString());

                viewHolder.find(R.id.tv_tgl_jualPart, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_nama_namaUsaha_jualPart, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString() + (isNamaUsaha ? " / " : "") + nListArray.get(position).get("NAMA_USAHA").asString());
                viewHolder.find(R.id.tv_noPhone_jualPart, TextView.class).setText(nListArray.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.tv_disc_jualPart, TextView.class).setText(nListArray.get(position).get("DISCOUNT").asString());
                viewHolder.find(R.id.tv_status_jualPart, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_userJual_jualPart, TextView.class).setText(nListArray.get(position).get("USER_JUAL").asString());
                viewHolder.find(R.id.tv_userUpdate_jualPart, TextView.class).setText(nListArray.get(position).get("USER_UPDATE").asString());
                //HARGA KOTOR ?
                viewHolder.find(R.id.tv_harga_jualPart, TextView.class).setText(nListArray.get(position).get("HARGA_BERSIH").asString());
                viewHolder.find(R.id.tv_total_jualPart, TextView.class).setText(nListArray.get(position).get("TOTAL").asString());
            }
        });
        catchData("");
    }

    private void catchData(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturjualpart"), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    if(nListArray.size() > 0){
                        for (int i = 0; i < nListArray.size(); i++) {
                            if(!nListArray.get(i).get("NAMA_USAHA").equals("") || !nListArray.get(i).get("NAMA_PELANGGAN").equals("") ){
                                isNamaUsaha = true;
                            }
                        }
                    }
                    rvJualPart.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Gagal Memuat Aktifitas");
                }
            }
        });
    }

    android.support.v7.widget.SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new android.support.v7.widget.SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Nama / Nama Usaha"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "aturjualpart", "NAMA_USAHA");
        android.support.v7.widget.SearchView.OnQueryTextListener queryTextListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                catchData(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_PENJUALAN){
            catchData("");
        }
    }
}
