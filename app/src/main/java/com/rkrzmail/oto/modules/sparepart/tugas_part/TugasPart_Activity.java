package com.rkrzmail.oto.modules.sparepart.tugas_part;

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
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class TugasPart_Activity extends AppActivity {

    private RecyclerView rvTugasPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tugas Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvTugasPart = findViewById(R.id.recyclerView);

        rvTugasPart.setLayoutManager(new LinearLayoutManager(this));
        rvTugasPart.setHasFixedSize(true);
        rvTugasPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_tugas_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);

                        viewHolder.find(R.id.tv_noAntrian_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_layanan_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_tglJam_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_namaP_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_user_tugasPart, TextView.class).setText(nListArray.get(position).get("").asString());
                    }

                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        String status = nListArray.get(position).get("").asString();
                        Intent i;
                        if(status.equalsIgnoreCase("SIAP")){

                        }else if(status.equalsIgnoreCase("PERMINTAAN")){

                        }else{

                        }
                    }
                })
        );
    }

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvTugasPart.getAdapter().notifyDataSetChanged();
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

        //adapterSearchView(mSearchView, "search", "viewsparepart", "NAMA");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                //reload(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

}
