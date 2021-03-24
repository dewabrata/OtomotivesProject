package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class DaftarUser_Activity extends AppActivity {

    private RecyclerView recyclerView;

    private Nson userList = Nson.newArray();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        initRv();
        viewUser("");

        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturUser_MainTab_Activity.class), REQUEST_DETAIL);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initRv() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(userList, R.layout.item_user) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtNamaKaryawan, TextView.class).setText(userList.get(position).get("NAMA").asString());
                viewHolder.find(R.id.et_no_ponsel, TextView.class).setText("+" + userList.get(position).get("NO_PONSEL").asString());
                viewHolder.find(R.id.txtPosisi, TextView.class).setText(userList.get(position).get("POSISI").asString());
                viewHolder.find(R.id.tv_fungsiMekanik, TextView.class).setText(userList.get(position).get("FUNGSI_MEKANIK").asString());
                viewHolder.find(R.id.txtStatus, TextView.class).setText(userList.get(position).get("STATUS").asString());
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), AturUser_MainTab_Activity.class);
                intent.putExtra(DATA, userList.get(position).toJson());
                startActivityForResult(intent, REQUEST_DETAIL);
            }
        }));
    }


    private void viewUser(final String cari) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    userList.asArray().clear();
                    userList.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }else{
                    showError("Mohon di coba kembali");
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
        mSearchView.setQueryHint("Cari Nama Karyawan"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        adapterSearchView(mSearchView, "search", "aturkaryawan", "NAMA", "");
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                //filter(newText);
                return true;
            }
            public boolean onQueryTextSubmit(String query) {
                //searchMenu.collapseActionView();
                viewUser(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL){
            viewUser("");
        }
    }
}