package com.rkrzmail.oto.modules.discount;

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
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.DISCOUNT_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DISCOUNT;
import static com.rkrzmail.utils.ConstUtils.RP;

public class DiscountLayanan_Activity extends AppActivity {

    private RecyclerView rvDiscLayanan;

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
        getSupportActionBar().setTitle("Discount Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturDiscountLayanan_Activity.class), REQUEST_DISCOUNT);
            }
        });

        rvDiscLayanan = findViewById(R.id.recyclerView);
        rvDiscLayanan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDiscLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_discount_layanan) {
                    boolean expandable = true;

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        String pekerjaan = nListArray.get(position).get("PEKERJAAN").asString();
                        String[] pekerjaanSplit = pekerjaan.split(",");
                        if(pekerjaanSplit.length == 1){
                            pekerjaan = pekerjaan.replace(",", "");
                        }
                        viewHolder.find(R.id.tv_pekerjaan_discLayanan, TextView.class).setText(pekerjaan);
                        viewHolder.find(R.id.tv_paketLayanan_discLayanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());
                        viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                        viewHolder.find(R.id.tv_disc_discLayanan, TextView.class).setText(nListArray.get(position).get("DISCOUNT_LAYANAN").asString() + " %");
                        if (Utility.isNumeric(nListArray.get(position).get("HARGA").asString())) {
                            viewHolder.find(R.id.tv_harga_discLayanan, TextView.class).setText(RP + NumberFormatUtils.formatRp(nListArray.get(position).get("HARGA").asString()));
                        } else {
                            viewHolder.find(R.id.tv_harga_discLayanan, TextView.class).setText(nListArray.get(position).get("HARGA").asString());
                        }
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), AturDiscountLayanan_Activity.class);
                        i.putExtra(DATA, nListArray.get(position).toJson());
                        startActivityForResult(i, REQUEST_DISCOUNT);
                    }
                })
        );

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewData("");
            }
        });

        viewData("");
    }


    private void viewData(final String nama) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_LAYANAN), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvDiscLayanan.getAdapter().notifyDataSetChanged();
                } else {
                    showError(ERROR_INFO);
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
        mSearchView.setQueryHint("Cari Layanan"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "aturdiskonlayanan", "NAMA_LAYANAN", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                viewData(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DISCOUNT)
            viewData("");
    }
}