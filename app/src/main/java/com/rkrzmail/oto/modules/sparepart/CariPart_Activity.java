package com.rkrzmail.oto.modules.sparepart;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CariPart_Activity extends AppActivity {

    private static final String SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY";
    private static final int MAX_HISTORY_ITEMS = 10;
    private Pendaftaran1.AutoSuggestAdapter autoSuggestAdapter;
    private RecyclerView rvCariPart;
    private boolean flagCariPart = false,
            flagKelompokPart = false,
            flag, flagGlobal = false,
            flagBengkel = false,
            flagMasterPart = false;
    private Toolbar toolbar;
    int countForCariPart = 0;
    private String cari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //flag = true is for view part and flag false for else than part (MASTER)
        if (getIntent().hasExtra("flag")) {
            getSupportActionBar().setTitle("Cari Part");
            flag = true;
            flagCariPart = true;
        } else if (getIntent().hasExtra("KELOMPOK_PART")) {
            getSupportActionBar().setTitle("Cari Kelompok Part");
            flag = false;
            flagKelompokPart = true;
        } else if (getIntent().hasExtra("MASTER_PART")) {
            getSupportActionBar().setTitle("Cari Master Part");
            flagMasterPart = true;
            flag = true;
        } else if (getIntent().hasExtra("global")) {
            getSupportActionBar().setTitle("Cari Part Otomotives");
            flagGlobal = true;
            flag = true;
        } else if (getIntent().hasExtra("bengkel")) {
            getSupportActionBar().setTitle("Cari Part Bengkel");
            flagBengkel = true;
            flag = false;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        if (countForCariPart == 0) {
            cariPart("");
        }

        rvCariPart = (RecyclerView) findViewById(R.id.recyclerView);
        rvCariPart.setLayoutManager(new LinearLayoutManager(this));
        rvCariPart.setHasFixedSize(true);
        rvCariPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_cari_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        if (flagGlobal || flagBengkel) {
                            viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                            viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                            viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(flag ? nListArray.get(position).get("NOMOR_PART_NOMOR").asString() : nListArray.get(position).get("NO_PART").asString());
                            viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setVisibility(flag ? View.GONE : View.VISIBLE);
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setVisibility(flag ? View.GONE : View.VISIBLE);
                            viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(flag ? "" : nListArray.get(position).get("STOCK").asString());
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setText(flag ? "" : nListArray.get(position).get("PENDING").asString());
                        } else {
                            viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setVisibility(View.GONE);
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setVisibility(View.GONE);

                            viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(flag ? (
                                    flagMasterPart ?
                                            nListArray.get(position).get("NAMA_MASTER").asString() : nListArray.get(position).get("MERK").asString()) : (
                                    flagKelompokPart ?
                                            nListArray.get(position).get("KATEGORI").asString() : nListArray.get(position).get("MERK").asString()));

                            viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(flag ? (
                                    flagMasterPart ?
                                            nListArray.get(position).get("NAMA_LAIN").asString() : nListArray.get(position).get("MERK").asString()) : (
                                    flagKelompokPart ?
                                            nListArray.get(position).get("KELOMPOK").asString() : nListArray.get(position).get("MERK").asString()));

                            viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(flag ? (
                                    flagMasterPart ?
                                            nListArray.get(position).get("").asString() : nListArray.get(position).get("MERK").asString()) : (
                                    flagKelompokPart ?
                                            nListArray.get(position).get("KELOMPOK_LAIN").asString() : nListArray.get(position).get("MERK").asString()));

                        }
                        //viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(flag ? nListArray.get(position).get("STOCK").asString() : nListArray.get(position).get("TYPE").asString());
                    }

                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent intent = new Intent();
                        intent.putExtra("part", nListArray.get(position).toJson());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
        );
    }

    private void cariPart(final String cari) {

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", getIntentStringExtra("flag"));
                if (flagCariPart) {
                    flag = true;
                    if (countForCariPart > 0) {
                        args.remove("flag");
                    }
                    args.put("search", cari);
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));
                } else if (flagGlobal || countForCariPart == 2) {
                    flag = true;
                    args.put("action", "view");
                    args.put("search", cari);
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
                } else if (flagBengkel || countForCariPart == 5) {
                    flag = false;
                    args.put("action", "view");
                    args.put("search", cari);
                    args.put("spec", "Bengkel");
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
                } else if (flagKelompokPart || countForCariPart == 4) {
                    flag = false;
                    args.put("nama", getIntentStringExtra("KELOMPOK_PART"));
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewmst"), args));
                } else if (flagMasterPart || countForCariPart == 3) {
                    flag = true;
                    args.put("flag", getIntentStringExtra("MASTER_PART"));
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));
                }
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                } else {
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

        adapterSearchView(mSearchView, flagBengkel ? "spec" : "", flagBengkel ? "viewsparepart" : "caripartsugestion", "NAMA_PART");

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                //cariPart(newText);
                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                cari = query;
                if (flag) {
                    if (flagGlobal) {
                        countForCariPart = 2;
                        cariPart(query);
                    } else if (flagMasterPart) {
                        countForCariPart = 3;
                        cariPart(query);
                    }
                } else {
                    if (flagKelompokPart) {
                        countForCariPart = 4;
                        cariPart(query);
                    } else if (flagBengkel) {
                        countForCariPart = 5;
                        cariPart(query);
                    }
                }
                mSearchView.setIconified(true);
                mSearchView.isIconfiedByDefault();
                mSearchView.clearFocus();

                return true;
            }
        };

        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
}
