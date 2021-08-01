package com.rkrzmail.oto.modules.jasa;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_STOCK_OPNAME;
import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART_SUGGESTION;
import static com.rkrzmail.utils.APIUrls.VIEW_JASA_LAIN;
import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
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
                intent.putExtra("IS_USULAN_MEKANIK", getIntent().getBooleanExtra("IS_USULAN_MEKANIK", false));
                intent.putExtra("KM", getIntentIntegerExtra("KM"));
                intent.putExtra(DATA, parent.get(position).toJson());
                intent.putExtra(JASA_LAIN, Nson.readJson(getIntentStringExtra(JASA_LAIN)).toJson());
                startActivityForResult(intent, REQUEST_BIAYA);

            }
        }));

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                catchData("");
            }
        });
        catchData("");
    }

    private void catchData(final String cari) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("flag", "ALL");
                args.put("search", cari);
                args.put("kendaraanID", String.valueOf(getIntentIntegerExtra("KENDARAAN_ID")));
                if(isCari){
                    args.remove("search");
                    args.put("cari", cari);
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JASA_LAIN), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvJasa.getAdapter()).notifyDataSetChanged();
                    isCari = false;
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

        //adapterSearchView(mSearchView, "", VIEW_JASA_LAIN, "KELOMPOK_PART", "ALL");
        adapterSearchView();
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

    private void adapterSearchView() {
        final String[] searchObject = {""};
        final SearchView.SearchAutoComplete searchAutoComplete = mSearchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.drawable.bg_radius_white);
        searchAutoComplete.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                searchObject[0] = bookTitle;

                args.put("action", "view");
                args.put("flag", "ALL");
                args.put("search", bookTitle);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JASA_LAIN), args));
                result = result.get("data");
                result = Tools.removeDuplicates(result);

                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                String search;
                if(searchObject[0].toLowerCase().equals(getItem(position).get("NAMA_LAIN").asString().toLowerCase())){
                    search = getItem(position).get("NAMA_LAIN").asString();
                }else{
                    search = getItem(position).get("KELOMPOK_PART").asString();
                }

                findView(convertView, R.id.title, TextView.class).setText(search);
                return convertView;
            }
        });
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(i)));
                String object;
                if(searchObject[0].toLowerCase().equals(n.get("NAMA_LAIN").asString().toLowerCase())){
                    object = n.get("NAMA_LAIN").asString();
                } else {
                    object = n.get("KELOMPOK_PART").asString();
                }

                find(R.id.search_src_text, SearchView.SearchAutoComplete.class).setText(object);
                find(R.id.search_src_text, SearchView.SearchAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(i)));
                mSearchView.setQuery(object, true);
            }
        });
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
