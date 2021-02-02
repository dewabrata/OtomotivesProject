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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_TERIMA_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class TerimaPart_Activity extends AppActivity {

    private static final String TAG = "TerimaPart";
    private static final int REQUEST_DETAIL = 667;
    private RecyclerView recyclerView_terimaPart;
    public static final int REQUEST_TERIMA_PART = 666;
    private DecimalFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Terima Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        recyclerView_terimaPart = findViewById(R.id.recyclerView);
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult( new Intent(getActivity(), AturTerimaPart_Activity.class), REQUEST_TERIMA_PART);
            }
        });

        viewTerimaPart("");
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewTerimaPart("");
            }
        });

        initRecylerview();
    }

    private void initRecylerview(){
        recyclerView_terimaPart.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_terimaPart.setHasFixedSize(true);
        recyclerView_terimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_terima_part){
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                String tgl = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("TANGGAL_PENERIMAAN").asString());
                String tglInv = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("JATUH_TEMPO_INV").asString());

                if(nListArray.get(position).get("NAMA_SUPPLIER").asString().equals("") && nListArray.get(position).get("NO_PONSEL_SUPPLIER").asString().equals("")) {
                    viewHolder.find(R.id.tr_supplier, TableRow.class).setVisibility(View.GONE);
                    viewHolder.find(R.id.tr_no_supplier, TableRow.class).setVisibility(View.GONE);
                }else {
                    viewHolder.find(R.id.tr_supplier, TableRow.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tr_no_supplier, TableRow.class).setVisibility(View.VISIBLE);
                }

                viewHolder.find(R.id.txtTanggal, TextView.class).setText(tgl);
                viewHolder.find(R.id.txtSupplier, TextView.class).setText(nListArray.get(position).get("NAMA_SUPPLIER").asString());
                if(nListArray.get(position).get("NO_PONSEL_SUPPLIER").asString().startsWith("62") && !nListArray.get(position).get("NO_PONSEL_SUPPLIER").asString().contains("+")){
                    viewHolder.find(R.id.tv_no_ponsel_supplier, TextView.class).setText("+" + nListArray.get(position).get("NO_PONSEL_SUPPLIER").asString());
                }
                viewHolder.find(R.id.txtPembayaran, TextView.class).setText(nListArray.get(position).get("PEMBAYARAN").asString());
                viewHolder.find(R.id.txtNoDo, TextView.class).setText(nListArray.get(position).get("NO_DO").asString());
                viewHolder.find(R.id.txtJatuhTempo, TextView.class).setText(tglInv);
                try{
                    viewHolder.find(R.id.txtTotal, TextView.class).setText(RP + formatRp(nListArray.get(position).get("NET").asString()));
                    viewHolder.find(R.id.tv_ongkir, TextView.class).setText(RP + formatRp(nListArray.get(position).get("ONGKOS_KIRIM").asString()));
                }catch(Exception e){
                    Log.d(TAG, "exception: " + e.getMessage() + "cause : " + e.getCause());
                }
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), DetailTerimaPart_Activity.class);
                i.putExtra(PART, nListArray.get(position).toJson());
                startActivityForResult(i, REQUEST_DETAIL);
            }
        }));
    }

    private void viewTerimaPart(final String data){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                swipeProgress(true);
                args.put("action", "view");
                args.put("flag", "TERIMA_PART");
                args.put("search", data);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TERIMA_PART),args)) ;
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView_terimaPart.getAdapter().notifyDataSetChanged();
                }else {
                    showError("Mohon Di Coba Kembali " + result.get("message").asString());
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
        mSearchView.setQueryHint("Cari No. DO"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "viewterimapart", "NO_DO", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                viewTerimaPart(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_TERIMA_PART)
            viewTerimaPart("");
        else if(resultCode == RESULT_OK && requestCode == REQUEST_DETAIL)
            viewTerimaPart("");
    }
}
