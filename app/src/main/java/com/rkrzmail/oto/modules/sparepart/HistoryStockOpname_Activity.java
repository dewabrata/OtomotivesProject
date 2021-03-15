package com.rkrzmail.oto.modules.sparepart;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_STOCK_OPNAME;
import static com.rkrzmail.utils.ConstUtils.ALL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_TERALOKASIKAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_OPNAME;

public class HistoryStockOpname_Activity extends AppActivity {

    private RecyclerView recyclerView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History Stock Opname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getActivity(), CariPart_Activity.class);
                intent.putExtra(CARI_PART_LOKASI, ALL);
                startActivityForResult(intent, REQUEST_CARI_PART);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_history_stock_opname) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.tv_lokasi_historyStock, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_noFolder_historyStock, TextView.class).setText(nListArray.get(position).get("NO_FOLDER").asString());
                viewHolder.find(R.id.tv_namaPart_historyStock, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_historyStock, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_stock_historyStock, TextView.class).setText(nListArray.get(position).get("STOCK").asString());
                viewHolder.find(R.id.tv_merk_historyStock, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_pending_historyStock, TextView.class).setText(nListArray.get(position).get("PENDING_STOCK").asString());
                viewHolder.find(R.id.tv_opname_historyStock, TextView.class).setText(nListArray.get(position).get("OPNAME").asString());

                String tgl =  Tools.setFormatDateTimeFromDb(nListArray.get(position).get("CREATED_DATE").asString(), "", "dd/MM hh:mm", true);

                viewHolder.find(R.id.tv_tgl, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_user, TextView.class).setText(nListArray.get(position).get("USER").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                if(!parent.get(position).get("PENYESUAIAN_ID").asString().isEmpty()){
                    intent = new Intent(getActivity(), AturPenyesuain_StockOpname_Activity.class);
                    intent.putExtra("VIEW", "");
                    intent.putExtra(DATA, nListArray.get(position).toJson());
                    startActivityForResult(intent, REQUEST_OPNAME);
                }else{
                    showWarning("TIDAK TERDAPAT PENYESUAIAN UNTUK " + parent.get(position).get("NAMA_PART").asString());
                }

            }
        }));

        reload("");
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload("");
            }
        });
    }

    private void reload(final String cari) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_STOCK_OPNAME), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon di coba kembali");
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
        mSearchView.setQueryHint("Cari No. DO"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "", SET_STOCK_OPNAME, "NAMA_PART", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                reload(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_OPNAME) {
            reload("");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            intent = new Intent(getActivity(), AturStockOpname_Activity.class);
            intent.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
            startActivityForResult(intent, REQUEST_OPNAME);
        }
    }
}
