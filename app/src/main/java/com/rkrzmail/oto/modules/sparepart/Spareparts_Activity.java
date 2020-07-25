package com.rkrzmail.oto.modules.sparepart;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import java.util.Map;

public class Spareparts_Activity extends AppActivity {

    private static final int REQUEST_ATUR = 21;
    public static final int REQUEST_CARI = 32;
    private RecyclerView recyclerView;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_3);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SPAREPART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CariPart_Activity.class);
                startActivityForResult(intent, REQUEST_CARI);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_spareparts) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                String stock = nListArray.get(position).get("STOCK").asString() + "/" + nListArray.get(position).get("STOCK_MINIMUM").asString();

                viewHolder.find(R.id.txtNamaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.txtNoPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_StockMinStock_spareparts, TextView.class).setText(stock);
                viewHolder.find(R.id.txtHargaJual, TextView.class).setText(nListArray.get(position).get("HARGA_JUAL").asString());
                viewHolder.find(R.id.tv_keluar_spareparts, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_pending_spareparts, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_terpasang_spareparts, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.txtMerk, TextView.class).setText(nListArray.get(position).get("MERK").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent();
                intent.putExtra("part", nListArray.get(position).toJson());
                setResult(RESULT_OK, intent);
                finish();
            }
        }));

        reload("");
    }


    private void reload(final String nama) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

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

        adapterSearchView(mSearchView, "search", "viewsparepart", "NAMA");
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
        if (requestCode == REQUEST_CARI && resultCode == RESULT_OK) {
            Intent i = new Intent(getActivity(), AturParts_Activity.class);
            i.putExtra("part", getIntentStringExtra(data, "part"));
            startActivity(i);
        } else if (requestCode == REQUEST_ATUR && resultCode == RESULT_OK) {
            reload("");
        }
    }
}
