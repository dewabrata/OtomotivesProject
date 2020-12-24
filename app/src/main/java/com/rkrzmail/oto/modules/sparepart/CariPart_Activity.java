package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
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
import com.rkrzmail.utils.Tools;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART_SUGGESTION;
import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_BENGKEL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_CLAIM;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_OTOMOTIVES;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_TERALOKASIKAN;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class CariPart_Activity extends AppActivity {

    private static final String SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY";
    private static final int MAX_HISTORY_ITEMS = 10;
    private RecyclerView rvCariPart;
    private boolean flagCariPart = false,
            flagKelompokPart = false,
            flag, flagGlobal = false,
            flagBengkel = false,
            flagMasterPart = false,
            isLokasi = false,
            isTeralokasikan = false,
            isPartCheckin= false;
    private Toolbar toolbar;
    int countForCariPart = 0;
    private String cari;
    private Nson partLokasiPart = Nson.newArray(), partClaim = Nson.newArray();

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
        } else if (getIntent().hasExtra(CARI_PART_OTOMOTIVES)) {
            getSupportActionBar().setTitle("Cari Part Otomotives");
            flagGlobal = true;
            flag = true;
        } else if (getIntent().hasExtra(CARI_PART_BENGKEL)) {
            getSupportActionBar().setTitle("Cari Part Bengkel");
            flagBengkel = true;
            flag = false;
        } else if (getIntent().hasExtra(CARI_PART_LOKASI)) {
            isLokasi = true;
            getSupportActionBar().setTitle("Cari Part Bengkel");
            flag = true;
        }else if(getIntent().hasExtra(CARI_PART_TERALOKASIKAN)){
            isTeralokasikan = true;
            getSupportActionBar().setTitle("Cari Part Bengkel");
            flag = true;
        }else if (getIntent().hasExtra(CARI_PART_CLAIM)){
            isPartCheckin = true;
            getSupportActionBar().setTitle("Cari Part Garansi");

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvCariPart = (RecyclerView) findViewById(R.id.recyclerView);
        rvCariPart.setLayoutManager(new LinearLayoutManager(this));
        rvCariPart.setHasFixedSize(true);
        if(isLokasi){
            initRecylerViewCarPartLokasi();
            cariPartWithLokasi("");
        }else if(isTeralokasikan){
            initRecylerViewCarPartTeralokasikan();
            cariPartTeralokasikan("");
        }else if(isPartCheckin){
            initRecylerViewCarPartClaim();
            cariPartCheckin("");
        }else{
            if (countForCariPart == 0) {
                cariPart("");
            }
            initRecylerviewCariPart();
        }
    }

    private void initRecylerviewCariPart() {
        rvCariPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_cari_part) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        if (flagGlobal || flagBengkel) {
                            viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                            viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                            viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                            viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setVisibility(flag ? View.GONE : View.VISIBLE);
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setVisibility(flag ? View.GONE : View.VISIBLE);
                            viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(flag ? "" : nListArray.get(position).get("STOCK").asString());
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setText(flag ? "" : nListArray.get(position).get("PENDING").asString());
                            viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setVisibility(flag ? View.GONE : View.VISIBLE);
                            viewHolder.find(R.id.tv_cari_hpp, TextView.class).setVisibility(flag ? View.GONE : View.VISIBLE);
                            if(partLokasiPart.get(position).get("HARGA_JUAL").asString().equals("FLEXIBLE")){
                                viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText("");
                                viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText(RP + formatRp(nListArray.get(position).get("HPP").asString()));
                            }else{
                                viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText( RP + formatRp(nListArray.get(position).get("HARGA_JUAL").asString()));
                                viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText("");
                            }
//                            if(nListArray.get(position).get("HARGA_JUAL").asString().equals("FLEXIBLE")){
//                                viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText("");
//                                viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText("");
//                            }else{
//
//                            }
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
                        intent.putExtra(PART, nListArray.get(position).toJson());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
        );
    }

    private void initRecylerViewCarPartLokasi() {
        rvCariPart.setAdapter(new NikitaRecyclerAdapter(partLokasiPart, R.layout.item_daftar_cari_part) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(partLokasiPart.get(position).get("MERK").asString());
                        viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(partLokasiPart.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(partLokasiPart.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(partLokasiPart.get(position).get("STOCK_RUANG_PART").asString());
                        if(partLokasiPart.get(position).get("HARGA_JUAL").asString().equals("FLEXIBLE")){
                            viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText("");
                            viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText(RP + formatRp(partLokasiPart.get(position).get("HPP").asString()));
                        }else{
                            viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText( RP + formatRp(partLokasiPart.get(position).get("HARGA_JUAL").asString()));
                            viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText("");
                        }
                        if(!partLokasiPart.get(position).get("LOKASI").asString().equals("*")){
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setText(partLokasiPart.get(position).get("LOKASI").asString());
                        }else{
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setText("");
                        }
                    }

                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent intent = new Intent();
                        intent.putExtra(PART, partLokasiPart.get(position).toJson());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
        );
    }

    private void initRecylerViewCarPartTeralokasikan() {
        rvCariPart.setAdapter(new NikitaRecyclerAdapter(partLokasiPart, R.layout.item_lokasi_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_noFolder, TextView.class).setText(partLokasiPart.get(position).get("KODE").asString());
                        viewHolder.find(R.id.tv_lokasiPart, TextView.class).setText(partLokasiPart.get(position).get("LOKASI").asString());
                        viewHolder.find(R.id.tv_namaPart, TextView.class).setText( partLokasiPart.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_nomor_part, TextView.class).setText(partLokasiPart.get(position).get("NOMOR_PART_NOMOR").asString());
                        viewHolder.find(R.id.tv_merk, TextView.class).setText(partLokasiPart.get(position).get("MERK").asString());
                        viewHolder.find(R.id.tv_stock, TextView.class).setText(partLokasiPart.get(position).get("STOCK").asString());
                        viewHolder.find(R.id.tv_pending, TextView.class).setText(partLokasiPart.get(position).get("PENDING").asString());

                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent intent = new Intent();
                        intent.putExtra(PART, partLokasiPart.get(position).toJson());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
        );
    }

    private void initRecylerViewCarPartClaim() {
        rvCariPart.setAdapter(new NikitaRecyclerAdapter(partClaim, R.layout.item_daftar_cari_part) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(partClaim.get(position).get("MERK").asString());
                        viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(partClaim.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(partClaim.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(partClaim.get(position).get("STOCK").asString());
                        viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText(RP + formatRp(partClaim.get(position).get("HARGA_PART").asString()));
                        viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText("");
                        if(!partClaim.get(position).get("LOKASI").asString().equals("*")){
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setText(partClaim.get(position).get("LOKASI").asString());
                        }else{
                            viewHolder.find(R.id.tv_cari_pending, TextView.class).setText("");
                        }
                    }

                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent intent = new Intent();
                        intent.putExtra(PART, partClaim.get(position).toJson());
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
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_CARI_PART), args));
                } else if (flagGlobal || countForCariPart == 2) {
                    countForCariPart = 2;
                    flag = true;
                    args.put("action", "view");
                    args.put("search", cari);
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
                } else if (flagBengkel || countForCariPart == 5) {
                    countForCariPart = 5;
                    flag = false;
                    args.put("action", "view");
                    args.put("search", cari);
                    args.put("spec", "Bengkel");
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
                } else if (flagKelompokPart || countForCariPart == 4) {
                    countForCariPart = 4;
                    flag = false;
                    args.put("nama", getIntentStringExtra("KELOMPOK_PART"));
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
                } else if (flagMasterPart || countForCariPart == 3) {
                    countForCariPart = 3;
                    flag = true;
                    args.put("flag", getIntentStringExtra("MASTER_PART"));
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_CARI_PART), args));
                }
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                } else {
                    showError(result.get("status").asString());
                }
            }
        });
    }

    private void cariPartWithLokasi(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("search", cari);
                args.put("lokasi", getIntentStringExtra(CARI_PART_LOKASI));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partLokasiPart.asArray().clear();
                    partLokasiPart.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Gagal Mencari Part");
                }
            }
        });
    }

    private void cariPartTeralokasikan(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                args.put("flag", "TERALOKASI");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LOKASI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partLokasiPart.asArray().clear();
                    partLokasiPart.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void cariPartCheckin(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("search", cari);
                args.put("idCheckin", getIntentStringExtra(CARI_PART_CLAIM));
                args.put("lokasi", "Claim");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partClaim.asArray().clear();
                    partClaim.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
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

        if(isLokasi){
            adapterSearchView(mSearchView, "spec", VIEW_SPAREPART, "NAMA_PART", "");
        }else if(flagGlobal && countForCariPart == 2){
            adapterSearchView(mSearchView,"", VIEW_CARI_PART_SUGGESTION, "NAMA_PART", CARI_PART);
        }else if(flagBengkel && countForCariPart == 5){
            adapterSearchView(mSearchView,"spec", VIEW_SPAREPART, "NAMA_PART", "");
        }else if(isTeralokasikan){
            adapterSearchView(mSearchView,"", VIEW_LOKASI_PART, "NAMA_PART", CARI_PART_TERALOKASIKAN);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                //cariPart(newText);
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                cari = query;
                if (flag) {
                    if(isLokasi){
                        cariPartWithLokasi(query);
                    }
                   if(isTeralokasikan){
                       cariPartTeralokasikan(query);
                   }
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
