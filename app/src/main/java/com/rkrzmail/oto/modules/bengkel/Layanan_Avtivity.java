package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
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
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.EDIT;

public class Layanan_Avtivity extends AppActivity {

    private static final String TAG = "Layanan_Activity";
    private RecyclerView rvLayanan;
    private LinearLayout lyContainerFilter;
    private BottomSheetBehavior filterBottomSheet;

    private String sortBy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_filter);
        initToolbar();
        initComponent();
        initFilterBottomSheet();
        initSortByJenisLayanan();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        lyContainerFilter = findViewById(R.id.ly_container_filter_layanan);

        find(R.id.ly_container_filter_saldo).setVisibility(View.GONE);
        find(R.id.ly_container_filter_kontrol_layanan).setVisibility(View.GONE);
        find(R.id.fab_tambah).setVisibility(View.VISIBLE);

        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
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

                viewHolder.find(R.id.tv_varian_kendaraan, TextView.class).setText(nListArray.get(position).get("VARIAN").asString());
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

        viewLayanan("", "");
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewLayanan("", "");
            }
        });

        find(R.id.btn_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filterBottomSheet.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    filterBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        find(R.id.img_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    private void initFilterBottomSheet() {
        filterBottomSheet = BottomSheetBehavior.from(lyContainerFilter);
        filterBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;

                    case BottomSheetBehavior.STATE_EXPANDED: {

                    }
                    break;

                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        find(R.id.view_blur).setVisibility(View.GONE);
                    }
                    break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float slideOffset) {
                find(R.id.view_blur).setVisibility(View.VISIBLE);
                find(R.id.view_blur).setAlpha(slideOffset);
            }
        });
    }

    private void initSortByJenisLayanan() {
        final Nson jenisLayananList = Nson.newArray();
        jenisLayananList.add(Nson.newObject().set("tittle", "ALL"));
        jenisLayananList.add(Nson.newObject().set("tittle", "PAKET LAYANAN"));
        jenisLayananList.add(Nson.newObject().set("tittle", "AFTER SALES SERVIS"));

        RecyclerView recyclerView = findViewById(R.id.rv_jenis_layanan);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager
                (
                        1,
                        StaggeredGridLayoutManager.HORIZONTAL
                );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(jenisLayananList, R.layout.item_sort_by) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                if(sortBy.isEmpty()){
                    viewHolder.find(R.id.img_check_selected).setVisibility(jenisLayananList.get(position).get("tittle").asString().equals("ALL") ? View.VISIBLE : View.GONE);
                }else{
                    viewHolder.find(R.id.img_check_selected).setVisibility(sortBy.equals(jenisLayananList.get(position).get("tittle").asString()) ? View.VISIBLE : View.GONE);
                }
                viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(jenisLayananList.get(position).get("tittle").asString());
                viewHolder.find(R.id.ly_container_status).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortBy = jenisLayananList.get(position).get("tittle").asString();
                        if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        if (jenisLayananList.get(position).get("tittle").asString().equals("ALL")) {
                            viewLayanan("", "");
                        } else {
                            viewLayanan("", jenisLayananList.get(position).get("tittle").asString());
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void viewLayanan(final String search, final String sortBy) {
        if(!Tools.isNetworkAvailable(getActivity())){
            showWarning("TIDAK ADA KONEKSI INTERNET");
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("search", search);
                args.put("status", "TIDAK AKTIF");
                args.put("sortBy", sortBy);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvLayanan.getAdapter()).notifyDataSetChanged();

                } else {
                    Log.d(TAG, result.get("status").asString());
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                viewLayanan("", "");
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

        adapterSearchView(mSearchView, "spec", "viewlayanan", "VARIAN", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                viewLayanan(query, "");
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
}
