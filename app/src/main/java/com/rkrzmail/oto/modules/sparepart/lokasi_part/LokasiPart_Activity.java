package com.rkrzmail.oto.modules.sparepart.lokasi_part;

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
import android.view.animation.AccelerateInterpolator;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.FragmentsAdapter;

import java.util.ArrayList;
import java.util.List;

public class LokasiPart_Activity extends AppActivity {

    private static final String TAG = "LokasiPart_Activity";
    private ViewPager vpLokasiPart;
    private TabLayout tabLayout;
    private ArrayList<Fragment> fragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lokasi_part);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lokasi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        initToolbar();
        vpLokasiPart = findViewById(R.id.vp_lokasiPart);
        tabLayout = findViewById(R.id.tablayout_lokasiPart);
        fragments = new ArrayList<>();
        fragments.add(new PartTeralokasikan_Fragment());
        fragments.add(new PartNonLokasi_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments);

        vpLokasiPart.setAdapter(pagerAdapter);
        vpLokasiPart.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(vpLokasiPart);


        //tabLayout.addOnTabSelectedListener();
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

        adapterSearchView(mSearchView, "search", "viewlokasipart", "NAMA");

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
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment instanceof PartTeralokasikan_Fragment) {
                            ((PartTeralokasikan_Fragment) fragment).initComponent(query);
                            break;
                        }
                    }
                }
                return false;
            }
        };

        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
}
