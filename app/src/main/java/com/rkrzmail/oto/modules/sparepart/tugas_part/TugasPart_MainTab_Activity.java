package com.rkrzmail.oto.modules.sparepart.tugas_part;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.FragmentsAdapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_BATAL;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_KOSONG;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_TERSEDIA;

public class TugasPart_MainTab_Activity extends AppActivity {

    private Nson partTersediaList = Nson.newArray();
    private Nson partPermintaanList = Nson.newArray();
    private Nson partBatalList = Nson.newArray();
    private Nson partKosongList = Nson.newArray();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_tugas_part);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Tugas Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();

        viewPartPermintaan();
        viewPartTersedia();
        viewPartBatal();
        viewPartKosong();

        ViewPager vpTugasParts = findViewById(R.id.vp_tugas_part);
        TabLayout tabLayoutTugasParts = findViewById(R.id.tablayout_tugas_part);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Permintaan_Tugas_Part_Fragment());
        fragments.add(new Tersedia_TugasPart_Fragment());
        fragments.add(new BatalPart_Tugas_Part_Fragment());
        fragments.add(new PartKosong_Tugas_Part_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager());

        for (int i = 0; i < fragments.size(); i++) {
            if(fragments.get(i) instanceof Permintaan_Tugas_Part_Fragment){
                pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments, partPermintaanList, TUGAS_PART_PERMINTAAN);
            }else if(fragments.get(i) instanceof Tersedia_TugasPart_Fragment){
                pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments, partTersediaList, TUGAS_PART_TERSEDIA);
            }else if(fragments.get(i) instanceof BatalPart_Tugas_Part_Fragment){
                pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments, partBatalList, TUGAS_PART_BATAL);
            }else if(fragments.get(i) instanceof PartKosong_Tugas_Part_Fragment){
                pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments, partKosongList, TUGAS_PART_KOSONG);
            }
        }

        vpTugasParts.setAdapter(pagerAdapter);
        vpTugasParts.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutTugasParts));
        tabLayoutTugasParts.setupWithViewPager(vpTugasParts);
    }

    private void viewPartTersedia() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("parts", "TERSEDIA");
                
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partTersediaList.asArray().addAll(result.get("data").asArray());
                } else {
                    showInfo("Gagal Memperbaharui Status");
                }
            }
        });
    }

    private void viewPartPermintaan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                
                
                args.put("pars", "PERMINTAAN");
                
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partPermintaanList.asArray().addAll(result.get("data").asArray());
                } else {
                    showInfo("Gagal Memperbaharui Status");
                }
            }
        });
    }

    private void viewPartBatal() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                
                
                args.put("parts", "BATAL");
                
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partBatalList.asArray().addAll(result.get("data").asArray());
                } else {
                    showInfo("Gagal Memperbaharui Status");
                }
            }
        });
    }

    private void viewPartKosong() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                
                
                args.put("parts", "KOSONG");
                
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    partKosongList.asArray().addAll(result.get("data").asArray());
                } else {
                    showInfo("Gagal Memperbaharui Status");
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

        adapterSearchView(mSearchView, "search", "aturtugaspart", "USER", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);

                return true;
            }
        };

        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
