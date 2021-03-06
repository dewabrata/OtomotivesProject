package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ASSET;
import static com.rkrzmail.utils.APIUrls.PIUTANG;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Asset_Activity extends AppActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initToolbar();
        initRvAset();
        viewAsset("");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Aset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRvAset() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_aset) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                String tgl = DateFormatUtils.formatDate(nListArray.get(position).get("TANGGAL_BELI").asString(), "yyyy-MM-dd", "dd/MM/yyyy");
                viewHolder.find(R.id.tv_nama_aset, TextView.class).setText(nListArray.get(position).get("NAMA_ASET").asString());
                viewHolder.find(R.id.tv_nomor_aset, TextView.class).setText(RP + NumberFormatUtils.formatRp(nListArray.get(position).get("NILAI_PENYUSUTAN").asString()));
                viewHolder.find(R.id.tv_tgl_beli, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_umur_aset, TextView.class).setText(nListArray.get(position).get("UMUR").asString());
                viewHolder.find(R.id.tv_harga, TextView.class).setText(RP + NumberFormatUtils.formatRp(nListArray.get(position).get("HARGA_BELI").asString()));
                viewHolder.find(R.id.tv_nilai_akhir, TextView.class).setText(RP + NumberFormatUtils.formatRp(nListArray.get(position).get("NILAI_AKHIR").asString()));
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturAsset_Activity.class);
                intent.putExtra(DATA, parent.get(position).toJson());
                startActivityForResult(intent, REQUEST_DETAIL);
            }
        }));
    }


    private void viewAsset(final String search) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                String[] args = new String[2];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(ASSET), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }


    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Part"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        //adapterSearchView(mSearchView, "search", "viewsparepart", "NAMA");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                viewAsset(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {
            viewAsset("");
        }
    }
}
