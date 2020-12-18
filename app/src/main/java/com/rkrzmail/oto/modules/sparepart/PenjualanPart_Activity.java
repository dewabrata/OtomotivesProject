package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.RP;

public class PenjualanPart_Activity extends AppActivity {

    private static final String TAG = "Penjualan__";
    private RecyclerView rvJualPartPelanggan, rvJualPartUsaha;
    public static final int REQUEST_PENJUALAN = 110;
    public static final int RESULT_DETAIL = 5;
    private boolean isNamaUsaha = false;
    private Nson pelangganList = Nson.newArray(), usahaList = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Penjualan Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initRecylerviewUsaha();
        initRecylerviewPelanggan();
        catchData("");
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                catchData("");
            }
        });
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturPenjualanPart_Activity.class), REQUEST_PENJUALAN);
            }
        });
    }



    private void initRecylerviewPelanggan() {
        rvJualPartPelanggan = findViewById(R.id.recyclerView2);
        rvJualPartPelanggan.setVisibility(View.VISIBLE);
        rvJualPartPelanggan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJualPartPelanggan.setAdapter(new NikitaRecyclerAdapter(pelangganList, R.layout.item_jual_part_pelanggan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tgl = Tools.setFormatDayAndMonthFromDb(pelangganList.get(position).get("TANGGAL").asString());
                String noHp = pelangganList.get(position).get("NO_PONSEL").asString();
                if (noHp.length() > 4) {
                    noHp = noHp.substring(noHp.length() - 4);
                }
                viewHolder.find(R.id.tv_tgl_jualPart, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_status_pelanggan, TextView.class).setText(pelangganList.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(pelangganList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_noPhone_jualPart, TextView.class).setText("XXXXXXXX" + noHp);
                viewHolder.find(R.id.tv_harga_jualPart, TextView.class).setText(RP + formatRp(pelangganList.get(position).get("TOTAL").asString()));
            }
        });
    }

    private void initRecylerviewUsaha() {
        rvJualPartUsaha = findViewById(R.id.recyclerView);
        rvJualPartUsaha.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJualPartUsaha.setAdapter(new NikitaRecyclerAdapter(usahaList, R.layout.item_jual_part_usaha) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tgl = Tools.setFormatDayAndMonthFromDb(usahaList.get(position).get("TANGGAL").asString());
                String noHp = pelangganList.get(position).get("NO_PONSEL").asString();

                if (noHp.length() > 4) {
                    noHp = noHp.substring(noHp.length() - 4);
                }

                if (!usahaList.get(position).get("NAMA_PELANGGAN").asString().isEmpty()) {
                    viewHolder.find(R.id.tv_namaUsaha_jualPart, TextView.class).setText(usahaList.get(position).get("NAMA_USAHA").asString() + " (" + usahaList.get(position).get("NAMA_PELANGGAN").asString() + ")");
                } else {
                    viewHolder.find(R.id.tv_namaUsaha_jualPart, TextView.class).setText(usahaList.get(position).get("NAMA_USAHA").asString());
                }

                viewHolder.find(R.id.tv_tgl_jualPart, TextView.class).setText(tgl);
                viewHolder.find(R.id.tv_status_usaha, TextView.class).setText(usahaList.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_disc_jualPart, TextView.class).setText(usahaList.get(position).get("DISCOUNT").asString());
                viewHolder.find(R.id.tv_user_usaha, TextView.class).setText(usahaList.get(position).get("USER_JUAL").asString());
                viewHolder.find(R.id.tv_total_jualPart, TextView.class).setText(RP + formatRp(usahaList.get(position).get("NET").asString()));
                viewHolder.find(R.id.tv_harga_jualPart, TextView.class).setText(RP + formatRp(usahaList.get(position).get("TOTAL").asString()));
                viewHolder.find(R.id.tv_noPhone_jualPart, TextView.class).setText("XXXXXXXX" + noHp);
            }
        });
    }

    private void catchData(final String cari) {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturjualpart"), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (!result.get(i).get("NAMA_PELANGGAN").asString().isEmpty() && result.get(i).get("NAMA_USAHA").asString().isEmpty()) {
                            pelangganList.add(result.get(i));
                        }
                        if (!result.get(i).get("NAMA_USAHA").asString().isEmpty()) {
                            usahaList.add(result.get(i));
                        }
                    }

                    Objects.requireNonNull(rvJualPartPelanggan.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(rvJualPartUsaha.getAdapter()).notifyDataSetChanged();
                } else {
                    showError("Gagal Memuat Aktifitas");
                }
            }
        });
    }

    android.support.v7.widget.SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new android.support.v7.widget.SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Nama / Nama Usaha"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "aturjualpart", "NAMA_USAHA", "");
        android.support.v7.widget.SearchView.OnQueryTextListener queryTextListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_PENJUALAN) {
            catchData("");
        }
    }
}
