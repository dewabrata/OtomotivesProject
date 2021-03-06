package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
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
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.JURNAL;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Jurnal_Activity extends AppActivity {

    private RecyclerView rvJurnal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        initRv();
        FloatingActionButton fab = findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturJurnal_Activity.class), REQUEST_DETAIL);
            }
        });

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewJurnal();
            }
        });
        viewJurnal();
    }

    private void initRv(){
        rvJurnal = findViewById(R.id.recyclerView);
        rvJurnal.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJurnal.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jurnal) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tglJurnal = DateFormatUtils.formatDate(nListArray.get(position).get("TANGGAL").asString(), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy");

                viewHolder.find(R.id.tv_tgl_jurnal, TextView.class).setText(tglJurnal);
                viewHolder.find(R.id.tv_transaksi_jurnal, TextView.class).setText(nListArray.get(position).get("TRANSAKSI").asString());
                viewHolder.find(R.id.tv_nama_kontak, TextView.class).setText(nListArray.get(position).get("NAMA_KONTAK").asString());
                viewHolder.find(R.id.tv_nominal_jurnal, TextView.class).setText(RP + NumberFormatUtils.formatRp(nListArray.get(position).get("NOMINAL").asString()));
                viewHolder.find(R.id.tv_pembayaran_jurnal, TextView.class).setText(nListArray.get(position).get("PEMBAYARAN").asString());
                viewHolder.find(R.id.tv_aktifitas_jurnal, TextView.class).setText(nListArray.get(position).get("AKTIVITAS").asString());

                //
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturJurnal_Activity.class);
                intent.putExtra(DATA, parent.get(position).toJson());
                startActivityForResult(intent, REQUEST_DETAIL);
            }
        }));
    }

    private void viewJurnal() {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                String[] args = new String[2];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvJurnal.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo("Gagal");
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

        adapterSearchView(mSearchView, "search", "caripart", "NAMA", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                //cariPart(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_DETAIL){
            viewJurnal();
        }
    }
}
