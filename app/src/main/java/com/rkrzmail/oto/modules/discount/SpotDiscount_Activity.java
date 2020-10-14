package com.rkrzmail.oto.modules.discount;

import android.app.AlertDialog;
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
import android.view.LayoutInflater;
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

import static com.rkrzmail.utils.ConstUtils.REQUEST_DISC_SPOT;

public class SpotDiscount_Activity extends AppActivity {

    private RecyclerView rvDisc, rvPelanggan;
    private SearchView mSearchView;
    private AlertDialog alertDialog;
    private View dialogView;
    private Nson pelangganList = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Spot Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();

        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDialogPelanggan();
            }
        });

        initRecylerviewDisc();
        viewDiscSpot("");
        viewPelanggan();
    }

    private void initRecylerviewDisc(){
        rvDisc = findViewById(R.id.recyclerView);
        rvDisc.setHasFixedSize(true);
        rvDisc.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDisc.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_spot_discount) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                String tglSet = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("").asString());

                viewHolder.find(R.id.tv_tanggal_spotDisc, TextView.class).setText(tglSet);
                viewHolder.find(R.id.tv_user_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_namaPelanggan_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_transaksi_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_disc_spotDisc, TextView.class).setText(nListArray.get(position).get("").asString());
            }
        });
    }

    private void initRecylerviewPelanggan(View dialogView){
        rvPelanggan = dialogView.findViewById(R.id.recyclerView);
        rvPelanggan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPelanggan.setHasFixedSize(true);
        rvPelanggan.setAdapter(new NikitaRecyclerAdapter(pelangganList, R.layout.item_pelanggan){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(pelangganList.get(position).get("NAMA_PELANGGAM").asString());
                viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(pelangganList.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.tv_total_biaya, TextView.class).setText(pelangganList.get(position).get("TOTAL_BIAYA").asString());
                viewHolder.find(R.id.tv_layanan, TextView.class).setText(pelangganList.get(position).get("LAYANAN").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                startActivityForResult(new Intent(getActivity(), AturSpotDiscount_Activity.class), REQUEST_DISC_SPOT);
            }
        }));
    }

    private void viewDiscSpot(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonspot"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Gagal");
                }
            }
        });
    }

    private void viewPelanggan(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonspot"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    pelangganList.asArray().addAll(result.get("data").asArray());
                } else {
                    showInfo("Gagal");
                }
            }
        });
    }

    private void initDialogPelanggan(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);
        builder.create();

        initRecylerviewPelanggan(dialogView);
        alertDialog = builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Diskon"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "aturdiskonspot", "NAMA", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                viewDiscSpot(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_DISC_SPOT)
            viewDiscSpot("");
    }
}
