package com.rkrzmail.oto.modules.komisi;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
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
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KOMISI_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class KomisiJasaLain_Activity extends AppActivity {

    private RecyclerView recyclerView;

    private double komisiAvail = 0;

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
        getSupportActionBar().setTitle("Komisi Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AturKomisiJasaLain_Activity.class);
                i.putExtra(ADD, "");
                startActivityForResult(i, REQUEST_DETAIL);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_komisi_jasa_lain) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);

                        viewHolder.find(R.id.tv_tipe_jasa, TextView.class).setText(nListArray.get(position).get("TIPE_JASA").asString());
                        viewHolder.find(R.id.tv_aktivitas_layanan, TextView.class).setText(nListArray.get(position).get("AKTIVITAS").asString());
                        viewHolder.find(R.id.tv_komisi_percent, TextView.class).setText(NumberFormatUtils.formatPercent(nListArray.get(position).get("KOMISI_PERCENT").asDouble()));
                        viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                    }

                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), AturKomisiJasaLain_Activity.class);
                        i.putExtra(DATA, nListArray.get(position).toJson());
                        startActivityForResult(i, REQUEST_DETAIL);
                    }
                })
        );

        catchData("");
    }

    private void catchData(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KOMISI_JASA_LAIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    if(nListArray.size() > 0){
                        for (int i = 0; i < nListArray.size(); i++) {
                            if(!nListArray.get(i).get("KOMISI_PERCENT").asString().isEmpty()){
                                komisiAvail += nListArray.get(i).get("KOMISI_PERCENT").asDouble();
                            }
                        }
                    }
                    komisiAvail = 100 - komisiAvail;
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo(ERROR_INFO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_DETAIL) {
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
        mSearchView.setQueryHint("Cari Part"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "komisijasalain", "KOMISI", "");
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
