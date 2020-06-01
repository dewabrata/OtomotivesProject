package com.rkrzmail.oto.modules.terima_part;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.biayamekanik.AturBiayaMekanik2;
import com.rkrzmail.oto.modules.penugasan.AturPenugasan_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class TerimaPart extends AppActivity {

    public static final String TAG = "TerimaPart";
    private RecyclerView recyclerView_terimaPart;
    final int REQUEST_TERIMA_PART = 666;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terima_part);

        initToolbar();
        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Terima Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_terima_part);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturTerimaPart.class);
                startActivityForResult(intent, REQUEST_TERIMA_PART);
            }
        });

        recyclerView_terimaPart = (RecyclerView) findViewById(R.id.recyclerView_terimaPart);
        recyclerView_terimaPart.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_terimaPart.setHasFixedSize(true);
        recyclerView_terimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_terima_part){

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                String tgl = Tools.setFormatDayAndMonth(nListArray.get(position).get("TANGGAL_PENERIMAAN").asString());
                String tglInv = Tools.setFormatDayAndMonth(nListArray.get(position).get("JATUH_TEMPO_INV").asString());

                viewHolder.find(R.id.txtTanggal, TextView.class).setText(tgl);
                viewHolder.find(R.id.txtSupplier, TextView.class).setText(nListArray.get(position).get("SUPPLIER").asString());;
                viewHolder.find(R.id.txtPembayaran, TextView.class).setText(nListArray.get(position).get("PEMBAYARAN").asString());;
                viewHolder.find(R.id.txtTotal, TextView.class).setText(nListArray.get(position).get("NET").asString());;
                viewHolder.find(R.id.txtNoDo, TextView.class).setText(nListArray.get(position).get("NO_DO").asString());;
                viewHolder.find(R.id.txtJatuhTempo, TextView.class).setText(tglInv);
                viewHolder.find(R.id.txtUser, TextView.class).setText("USER :" + nListArray.get(position).get("USER").asString());;
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

            }
        }));
        reload("");

    }

    private void reload(final String data){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", data);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewterimapart"),args)) ;

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView_terimaPart.getAdapter().notifyDataSetChanged();
                }else {
                    Log.d(TAG, "error");
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
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

        adapterSearchView(mSearchView, "search", "viewterimapart", "NO_DO");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                reload(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

}
