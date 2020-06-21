package com.rkrzmail.oto.modules.sparepart.new_part;

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
import com.rkrzmail.oto.modules.primary.checkin.Checkin3_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class Part_Activity extends AppActivity {

    private RecyclerView rvPart;
    public static final int REQUEST_CHECKIN = 12;
    public static final int REQUEST_BOOKING = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_2);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        initToolbar();
        rvPart = findViewById(R.id.recyclerView);
        rvPart.setLayoutManager(new LinearLayoutManager(this));
        rvPart.setHasFixedSize(true);
        rvPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_namaPart_part_partExternal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_noPart_part_partExternal, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_stock_part_partExternal, TextView.class).setText(nListArray.get(position).get("").asString());

            }


            @Override
            public int getItemCount() {
                if (nListArray.size() == 0) {
                    find(R.id.btn_simpan_jasaBerkala).setVisibility(View.GONE);
                } else {
                    find(R.id.btn_simpan_jasaBerkala).setVisibility(View.VISIBLE);

                }
                return super.getItemCount();
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), Checkin3_Activity.class);
                intent.putExtra("NAMA_PART", nListArray.get(position).toJson());
                setResult(RESULT_OK);
                startActivity(intent);
                finish();
            }
        }));
        catchData();
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvPart.getAdapter().notifyDataSetChanged();
                } else {
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
        mSearchView.setQueryHint("Cari Part"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        //adapterSearchView(mSearchView, "search", "caripart", "NAMA");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                //cariPart(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BOOKING) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN) {

        }
    }
}
