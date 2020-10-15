package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
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

import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.EDIT;

public class Layanan_Avtivity extends AppActivity {

    private static final String TAG = "Layanan_Activity";
    private RecyclerView rvLayanan;

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
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AturLayanan_Activity.class);
                if(nListArray.size() > 0){
                    i.putExtra(ADD, nListArray.toJson());
                }else{
                    i.putExtra(ADD, "");
                }
                startActivityForResult(i, 10);
            }
        });

        rvLayanan = (RecyclerView) findViewById(R.id.recyclerView);
        rvLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvLayanan.setHasFixedSize(true);

        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_layanan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String lokasiLayanan = "";
                 if(!nListArray.get(position).get("LOKASI_LAYANAN_EMG").asString().equals("")){
                     lokasiLayanan += nListArray.get(position).get("LOKASI_LAYANAN_EMG").asString() + " ,";
                 }
                 if(!nListArray.get(position).get("LOKASI_LAYANAN_HOME").asString().equals("")){
                     lokasiLayanan += nListArray.get(position).get("LOKASI_LAYANAN_HOME").asString() + ", ";
                 }
                 if(!nListArray.get(position).get("LOKASI_LAYANAN_TENDA").asString().equals("")){
                     lokasiLayanan += nListArray.get(position).get("LOKASI_LAYANAN_TENDA").asString() + ", ";
                 }
                 if(!nListArray.get(position).get("LOKASI_LAYANAN_BENGKEL").asString().equals("")){
                     lokasiLayanan += nListArray.get(position).get("LOKASI_LAYANAN_BENGKEL").asString();
                 }

                viewHolder.find(R.id.tv_jenis_layanan, TextView.class).setText(nListArray.get(position).get("JENIS_LAYANAN").asString());
                viewHolder.find(R.id.tv_nama_layanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());
                viewHolder.find(R.id.tv_lokasi_layanan, TextView.class).setText(lokasiLayanan);
                viewHolder.find(R.id.tv_status_layanan, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                if(Tools.isNumeric(nListArray.get(position).get("BIAYA_PAKET").asString())){
                    viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText("Rp. " + formatRp(nListArray.get(position).get("BIAYA_PAKET").asString()));
                }else{
                    viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText(nListArray.get(position).get("BIAYA_PAKET").asString());
                }
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), AturLayanan_Activity.class);
                i.putExtra(EDIT, nListArray.get(position).toJson());
                startActivityForResult(i, 10);
            }
        }));

        catchData("");
    }

    private void catchData(final String nama) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("search", nama);
                args.put("status", "TIDAK AKTIF");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvLayanan.getAdapter().notifyDataSetChanged();

                } else {
                    Log.d(TAG, result.get("status").asString());
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                catchData("");
            }
        }
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

        adapterSearchView(mSearchView, "spec", "viewlayanan", "NAMA_LAYANAN", "");
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
