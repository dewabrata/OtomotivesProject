package com.rkrzmail.oto.modules.lokasi_part;

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
import com.rkrzmail.oto.modules.sparepart.AturParts_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.ArrayList;
import java.util.Map;

public class CariPart_Activity extends AppActivity {

    private Pendaftaran1.AutoSuggestAdapter autoSuggestAdapter;
    private RecyclerView rvCariPart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_part_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_cariPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cari Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }


    private void initComponent(){

        rvCariPart = (RecyclerView) findViewById(R.id.recyclerView_cariPart);
        rvCariPart.setLayoutManager(new LinearLayoutManager(this));
        rvCariPart.setHasFixedSize(true);

        rvCariPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_cari_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                if(nListArray.get("MERK_PART").asString() == null){
                    find(R.id.tv_cari_merkPart, TextView.class).setVisibility(View.GONE);
                }

                viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(nListArray.get(position).get("NO_PART_ID").asString());
                viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(nListArray.get(position).get("STOCK").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                       /* Intent intent = getIntent();
                        if (intent.hasExtra("cari")) {
                            intent = new Intent(getActivity(), AturParts_Activity.class);
                            intent.putExtra("NAMA", nListArray.get(position).toJson());
                            startActivity(intent);
                        } else if (intent.hasExtra("lokasi")) {
                            intent = new Intent(getActivity(), AturLokasiPart_Activity.class);
                            intent.putExtra("NAMA", nListArray.get(position).toJson());
                            startActivity(intent);
                        }*/

                        Intent intent = new Intent();
                        intent.putExtra("row", parent.get(position).toJson());
                        setResult(RESULT_OK, intent);

                    }
                })
        );
    }

    private void cariPart(final String cari){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", cari);
                args.put("flag", getIntentStringExtra("flag"));
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

        adapterSearchView(mSearchView, "search", "caripart", "NAMA");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                cariPart(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
}
