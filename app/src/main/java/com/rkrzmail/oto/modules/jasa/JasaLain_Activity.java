package com.rkrzmail.oto.modules.jasa;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.JASA_LAIN;

public class JasaLain_Activity extends AppActivity {

    private RecyclerView rvJasa;
    private static final int REQUEST_BIAYA = 11;
    //private EditText etAktivitasLain;
    private Nson nson = Nson.newArray();
    private int counting = 0;
    ArrayList<Boolean> flagChecked = new ArrayList<>();
    boolean isCari = false;
    private SearchView mSearchView;
    boolean isAvail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        if (getIntent().hasExtra("NEW")) {
            isAvail = true;
        }
        rvJasa = findViewById(R.id.recyclerView);
        catchData("");
        rvJasa.setLayoutManager(new LinearLayoutManager(this));
        rvJasa.setHasFixedSize(true);
        rvJasa.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jasa_lain) {

            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                /*viewHolder.find(R.id.cb_jasaLain_jasa, CheckBox.class).setTag("check");
                viewHolder.find(R.id.cb_jasaLain_jasa, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.isChecked()) {
                            buttonView.setChecked(true);
                            nson.add(nListArray.get(position));
                        } else {
                            buttonView.setChecked(false);
                            nson.asArray().remove(nson.get(nListArray.get(position)));
                        }
                    }
                });*/
                viewHolder.find(R.id.tv_kelompokPart_jasaLain, TextView.class).setText(nListArray.get(position).get("KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktivitas_jasaLain, TextView.class).setText(nListArray.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_waktu_jasaLain, TextView.class).setText(nListArray.get(position).get("WAKTU").asString());
                viewHolder.find(R.id.tv_mekanik_jasaLain, TextView.class).setText(nListArray.get(position).get("MEKANIK").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent intent = new Intent(getActivity(), BiayaJasa_Activity.class);
                intent.putExtra(DATA, parent.get(position).toJson());
                intent.putExtra(JASA_LAIN, Nson.readJson(getIntentStringExtra(JASA_LAIN)).toJson());
                Log.d("JASA_LAIN_CLASS", "JASA : " + parent);
                startActivityForResult(intent, REQUEST_BIAYA);

            }
        }));
    }

    private void catchData(final String cari) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("cari", "c");
                args.put("search", cari);
                if (isCari) {
                    args.remove("search");
                    args.put("cari", cari);
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JASA_LAIN), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvJasa.getAdapter()).notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Jasa Lain"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "", VIEW_JASA_LAIN, "NAMA", "");
        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                isCari = true;
                catchData(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAvail){
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BIAYA) {
            Intent i = new Intent();
            i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, DATA)).toJson());
            Log.d("JASA_LAIN_CLASS", "SENDD : " + Nson.readJson(getIntentStringExtra(data, DATA)));
            setResult(RESULT_OK, i);
            finish();
        }
    }
}
