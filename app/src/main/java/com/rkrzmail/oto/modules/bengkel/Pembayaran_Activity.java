package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.DetailKontrolLayanan_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class Pembayaran_Activity extends AppActivity {

    private RecyclerView rvPembayaran;
    private Nson sample = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initToolbar();
        initRecylerviewPembayaran();
        //viewPembayaran("");
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecylerviewPembayaran() {
        rvPembayaran = findViewById(R.id.recyclerView);
        rvPembayaran.setHasFixedSize(true);
        rvPembayaran.setLayoutManager(new LinearLayoutManager(this));
        rvPembayaran.setAdapter(new NikitaRecyclerAdapter(nListArray, sample, R.layout.item_pembayaran, R.layout.item_jual_part_pelanggan, "PART", "JASA") {

            @Override
                    public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, final int position) {
                        super.onBindViewHolder(viewHolder, position);

                        switch (viewHolder.getItemViewType()) {
                            case NikitaRecyclerAdapter.VIEW_1:
                                viewHolder.find(R.id.tv_jenis_kendaraan, TextView.class).setText(nListArray.get(position).get("JENIS_KENDARAAN").asString());
                                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                                viewHolder.find(R.id.tv_nopol, TextView.class).setText(nListArray.get(position).get("NOPOL").asString());
                                viewHolder.find(R.id.tv_layanan, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                                viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(nListArray.get(position).get("NO_PONSEL").asString());
                                break;
                            case NikitaRecyclerAdapter.VIEW_2:
                                viewHolder.find(R.id.tv_status_pelanggan, TextView.class).setText(sample.get(position).get("SAMPLE").asString());
                                break;
                        }


                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), DetailKontrolLayanan_Activity.class);
                        i.putExtra(DATA, nListArray.get(position).toJson());
                        startActivityForResult(i, REQUEST_DETAIL);
                    }
                })
        );

        nListArray.add(Nson.newObject().set("PART", "PART").set("NAMA_PELANGGAN", "sample1"));
        sample.add(Nson.newObject().set("JASA", "JASA").set("SAMPLE", "sample5"));
        rvPembayaran.getAdapter().notifyDataSetChanged();
    }

    private void viewPembayaran(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvPembayaran.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari No. Polisi"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        //adapterSearchView(mSearchView, "nopol", "viewnopol", "NOPOL");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                viewPembayaran(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {

        }
    }
}

