package com.rkrzmail.oto.modules.Adapter;

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
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.OutSource_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartKosong_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Permintaan_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.StatusClaim_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tersedia_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Adapter.FragmentsAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class TugasPart_MainTab_Activity extends AppActivity {

    private Nson partTersediaList = Nson.newArray();
    private Nson partPermintaanList = Nson.newArray();
    private Nson partKosongList = Nson.newArray();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
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
        ViewPager vpTugasParts = findViewById(R.id.vp);
        TabLayout tabLayoutTugasParts = findViewById(R.id.tablayout);
        tabLayoutTugasParts.setTabMode(TabLayout.MODE_SCROLLABLE);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Permintaan_TugasPart_Fragment());
        fragments.add(new Tersedia_TugasPart_Fragment());
        //fragments.add(new BatalPart_TugasPart_Fragment());
        fragments.add(new PartKosong_TugasPart_Fragment());
        fragments.add(new OutSource_TugasPart_Fragment());
        fragments.add(new StatusClaim_TugasPart_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpTugasParts.setAdapter(pagerAdapter);
        vpTugasParts.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutTugasParts));
        tabLayoutTugasParts.setupWithViewPager(vpTugasParts);
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
