package com.rkrzmail.oto.modules;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.part.AdapterListBasic;
import com.rkrzmail.oto.modules.part.AturPartsNew;
import com.rkrzmail.oto.modules.part.People;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.DataGenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SparepartsNew extends AppActivity {
    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListBasic mAdapter;
    final int REQUEST_ATUR = 555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spareparts_new);
        parent_view = findViewById(android.R.id.content);

        initToolbar();
        initComponent();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_part2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getActivity(), AturPartsNew.class);
                startActivityForResult(intent, REQUEST_ATUR);
            }
        });
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SPAREPART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    private void reload(final String nama){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                args.put("nama", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("v3/viewsparepart"),args)) ;
                Log.i("hasil",result.toString());
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    Log.i("test","masuk");
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                    Collections.sort(nListArray.asArray(), new Comparator() {
                        @Override
                        public int compare(Object o, Object t1) {
                            if(o.equals(result.get("data")) ){

                            }
                            return 0;
                        }
                    });
                }else{
                    showError(result.get("massage").asString());
                }
               /* if (result.isNsonArray()) {

                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }*/
            }
        });
    }
    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        List<People> items = DataGenerator.getPeopleData(this);
        items.addAll(DataGenerator.getPeopleData(this));
        items.addAll(DataGenerator.getPeopleData(this));

        //set data and list adapter
        mAdapter = new AdapterListBasic(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, People obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.parts_new){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtNamaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());

                viewHolder.find(R.id.txtNoPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());

                viewHolder.find(R.id.txtStock, TextView.class).setText(nListArray.get(position).get("STOCK").asString());

                viewHolder.find(R.id.txtHargaJual, TextView.class).setText(nListArray.get(position).get("HARGA_JUAL").asString());

                viewHolder.find(R.id.txtMerk, TextView.class).setText(nListArray.get(position).get("MERK").asString());

                viewHolder.find(R.id.txtTerjual, TextView.class).setText(nListArray.get(position).get("TERJUAL").asString());

                viewHolder.find(R.id.txtMinStock, TextView.class).setText(nListArray.get(position).get("STOCK_MINIMUM").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                /*Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/

                //Snackbar.make(parent_view, "Item  "+position+"  clicked", Snackbar.LENGTH_SHORT).show();

                Intent intent =  new Intent(getActivity(), AturPartsNew.class);
                //intent.putExtra("DATA", nListArray.get(position).toJson());
                intent.putExtra("NAMA", nListArray.get(position).get("NAMA").asString());
                intent.putExtra("NO_PART", nListArray.get(position).get("NO_PART").asString());
                intent.putExtra("STOCK", nListArray.get(position).get("STOCK").asString());
                intent.putExtra("HARGA_JUAL", nListArray.get(position).get("HARGA_JUAL").asString());
                intent.putExtra("MERK", nListArray.get(position).get("MERK").asString());
                intent.putExtra("TERJUAL", nListArray.get(position).get("TERJUAL").asString());
                intent.putExtra("STOCK_MINIMUM", nListArray.get(position).get("STOCK_MINIMUM").asString());
                intent.putExtra("NAMA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_ATUR);
            }
        }));

        reload("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_ATUR && resultCode == RESULT_OK){
            Intent intent =  new Intent();
            intent.putExtra("DATA", getIntentStringExtra(data, "DATA"));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Search"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                //filter(newText);
                return true;
            }
            public boolean onQueryTextSubmit(String query) {
                //searchMenu.collapseActionView();
                //filter(null);
                reload(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
