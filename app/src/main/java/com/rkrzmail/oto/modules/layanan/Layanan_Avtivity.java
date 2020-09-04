package com.rkrzmail.oto.modules.layanan;

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
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.Map;

public class Layanan_Avtivity extends AppActivity {

    private static final String TAG = "Layanan_Activity";
    private RecyclerView rvLayanan;
    private DecimalFormat formatter;
    String lokasiLayanan = "";

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
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        formatter = new DecimalFormat("###,###,###");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AturLayanan_Activity.class);
                i.putExtra("add", "");
                startActivityForResult(i, 10);
            }
        });

        rvLayanan = (RecyclerView) findViewById(R.id.recyclerView);
        rvLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvLayanan.setHasFixedSize(true);

        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_layanan) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                 lokasiLayanan = nListArray.get(position).get("LOKASI_LAYANAN_EMG").asString()
                        + ", " + nListArray.get(position).get("LOKASI_LAYANAN_HOME").asString()
                        + ", " + nListArray.get(position).get("LOKASI_LAYANAN_TENDA").asString()
                        + ", " + nListArray.get(position).get("LOKASI_LAYANAN_BENGKEL").asString();

                viewHolder.find(R.id.tv_jenis_layanan, TextView.class).setText(nListArray.get(position).get("JENIS_LAYANAN").asString());
                viewHolder.find(R.id.tv_nama_layanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());
                viewHolder.find(R.id.tv_lokasi_layanan, TextView.class).setText(lokasiLayanan);
                viewHolder.find(R.id.tv_status_layanan, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                if(Tools.isNumeric(nListArray.get(position).get("HARGA_JUAL").asString())){
                    viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText("Rp. " + formatter.format(Double.parseDouble(nListArray.get(position).get("BIAYA_PAKET").asString())));
                }else{
                    viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText(nListArray.get(position).get("BIAYA_PAKET").asString());
                }
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), AturLayanan_Activity.class);
                i.putExtra("edit", nListArray.get(position).toJson());
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
