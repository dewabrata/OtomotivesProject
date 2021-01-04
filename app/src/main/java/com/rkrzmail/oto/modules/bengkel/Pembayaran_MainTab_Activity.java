package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Cash_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Print_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Setoran_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.Transaksi_Pembayaran_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class Pembayaran_MainTab_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        ViewPager vpPembayaran = findViewById(R.id.vp);
        TabLayout tabLayoutPembayaran = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Transaksi_Pembayaran_Fragment());
        fragments.add(new Print_Pembayaran_Fragment());
        fragments.add(new Cash_Pembayaran_Fragment());
        fragments.add(new Setoran_Pembayaran_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpPembayaran.setAdapter(pagerAdapter);
        vpPembayaran.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutPembayaran));
        tabLayoutPembayaran.setupWithViewPager(vpPembayaran);
    }


    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari No. Polisi"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        //adapterSearchView(mSearchView, "nopol", "viewnopol", "NOPOL");
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

}

