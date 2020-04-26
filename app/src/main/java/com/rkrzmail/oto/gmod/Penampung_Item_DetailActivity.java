package com.rkrzmail.oto.gmod;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import com.rkrzmail.oto.modules.part.People;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.DataGenerator;

import java.util.List;
import java.util.Map;

public class Penampung_Item_DetailActivity extends AppActivity {
    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListBasic mAdapter;

    final int REQUEST_DETAIL = 81;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//
//        getSupportActionBar().setTitle("PART");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parent_view = findViewById(android.R.id.content);
        Nson result = Nson.readNson( getIntentStringExtra("DATA"));

        initToolbar();
        initComponent();



    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LOKASI PART DETAIL");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    private void reload(final String nama){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
               // Nson result1 = Nson.readNson( getIntentStringExtra("ID"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                args.put("ID", getIntentStringExtra("ID"));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("getitemlokasi.php"),args)) ;


            }


            @Override
            public void runUI() {
                if (result.get(0).get("STATUS").asString().equalsIgnoreCase("OK")) {
                   // showError("");
                }

                    if (result.isNsonArray()) {
                        nListArray.asArray().clear();
                        nListArray.asArray().addAll(result.get(0).get("DETAIL").asArray());

                    }
            }
        });



//        if (result.isNson()) {
//            if (result.size() >= 1) {
//
////        TextView textView= findViewById(R.id.txtNamaJasaLain);
////        textView.setText(nson.get("NAMA").asString());
//
//                find(R.id.txtNoPart, TextView.class).setText(result.get("ID").asString());
////                find(R.id.txtWaktu, TextView.class).setText(result.get("WAKTU").asString());
////                find(R.id.txtJasa, TextView.class).setText(result.get("JASA").asString());
//
//                return;
//            }
//        }
    }

    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        List<People> items = DataGenerator.getPeopleData(this);
        items.addAll(DataGenerator.getPeopleData(this));
        items.addAll(DataGenerator.getPeopleData(this));

       /* //set data and list adapter
        mAdapter = new AdapterListBasic(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, People obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });*/

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_lokasi__persediaan__detail){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtNoPart, TextView.class).setText(nListArray.get(position).get("NO_PART_ID").asString());

                viewHolder.find(R.id.txtNamaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());

                viewHolder.find(R.id.txtStock, TextView.class).setText("Stock : " + nListArray.get(position).get("STOCK").asString());

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

//                Intent intent =  new Intent(getActivity(), JasaLainDetail.class);
//                intent.putExtra("DATA", nListArray.get(position).toJson());
//                startActivityForResult(intent, REQUEST_DETAIL);
            }
        }));

        reload("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_DETAIL && resultCode == RESULT_OK){
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
