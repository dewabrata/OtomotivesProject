package com.rkrzmail.oto.modules.layanan;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class Layanan_Avtivity extends AppActivity {

    private static final String TAG = "Layanan_Activity";
    private RecyclerView rvLayanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layanan__avtivity);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_layanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent(){

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_layanan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturLayanan_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        rvLayanan = (RecyclerView) findViewById(R.id.recyclerView_layanan);
        rvLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvLayanan.setHasFixedSize(true);

        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_layanan){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_jenis_layanan, TextView.class).setText(nListArray.get(position).get("JENIS_LAYANAN").asString());
                viewHolder.find(R.id.tv_nama_layanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());
                viewHolder.find(R.id.tv_lokasi_layanan, TextView.class).setText(nListArray.get(position).get("LOKASI_LAYANAN").asString());
                viewHolder.find(R.id.tv_status_layanan, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText(nListArray.get(position).get("BIAYA").asString());

            }
        });

        catchData("");
    }

    private void catchData(final String nama) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvLayanan.getAdapter().notifyDataSetChanged();
                    Log.d(TAG, result.get("status").asString());
                    Log.d("NAMA", result.get("search").get("data").asString());

                } else {
                    Log.d(TAG, result.get("status").asString());
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
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

        adapterSearchView(mSearchView, "search", "viewlayanan", "NAMA_LAYANAN");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
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
}
