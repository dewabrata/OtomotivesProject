package com.rkrzmail.oto.modules.sparepart;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.Pendaftaran1;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class CariPart_Activity extends AppActivity {

    private static final String SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY";
    private static final int MAX_HISTORY_ITEMS = 10;
    private Pendaftaran1.AutoSuggestAdapter autoSuggestAdapter;
    private RecyclerView rvCariPart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cari Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        rvCariPart = (RecyclerView) findViewById(R.id.recyclerView);
        rvCariPart.setLayoutManager(new LinearLayoutManager(this));
        rvCariPart.setHasFixedSize(true);
        rvCariPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_cari_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(nListArray.get(position).get("STOCK").asString());
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent intent = new Intent();
                        intent.putExtra("part", nListArray.get(position).toJson());
                        intent.putExtra("nopart", nListArray.get(position).get("PART_ID"));
                        intent.putExtra("flag all", nListArray.get("flag").get("ALL").asString());
                        intent.putExtra("flag no part", nListArray.get("flag").get("NO_PART").asString());
                        intent.putExtra("flag master part", nListArray.get("flag").get("MASTER_PART").asString());
                        intent.putExtra("flag kelompok part", nListArray.get("flag").get("KELOMPOK_PART").asString());

                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
        );

        cariPart("");
    }

    private void cariPart(final String cari){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", getIntentStringExtra("flag"));
                //args.put("flag", "NOPART");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                }else{
                    showError("Gagal Mencari Part");
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

        Intent i = getIntent();
        if(i.hasExtra("flag master part") && i.hasExtra("flag kelompok part")){
            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {
                    return false;
                }

                public boolean onQueryTextSubmit(String query) {
                    searchMenu.collapseActionView();
                    cariPart(query);
                    return true;
                }
            };
            mSearchView.setOnQueryTextListener(queryTextListener);
            return true;
        }else{
            adapterSearchView(mSearchView, "search", "caripart", "NAMA");
            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {

                    return false;
                }

                public boolean onQueryTextSubmit(String query) {
                    searchMenu.collapseActionView();
                    cariPart(query);
                    return true;
                }
            };
            mSearchView.setOnQueryTextListener(queryTextListener);
            return true;
        }
    }
}
