package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.PartBengkel_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartNonLokasi_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartOto_PartHome_Activity;
import com.rkrzmail.oto.modules.Fragment.PartTeralokasikan_Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART_SUGGESTION;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART;

public class PartHome_MainTab_Activity extends AppActivity {

    ViewPager vpPart;
    TabLayout tabPart;
    FragmentsAdapter pagerAdapter;
    ArrayList<Fragment> fragments;
    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        vpPart = findViewById(R.id.vp);
        tabPart = findViewById(R.id.tablayout);

        fragments = new ArrayList<>();
        fragments.add(new PartBengkel_PartHome_Fragment());
        fragments.add(new PartOto_PartHome_Activity());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpPart.setAdapter(pagerAdapter);
        vpPart.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabPart));
        tabPart.setupWithViewPager(vpPart);
    }

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

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof PartBengkel_PartHome_Fragment) {
                adapterSearchView(mSearchView, "spec", VIEW_SPAREPART, "NAMA_PART", "");
                break;
            }else if(fragment instanceof PartOto_PartHome_Activity){
                adapterSearchView(mSearchView, "", VIEW_CARI_PART_SUGGESTION, "NAMA_PART", CARI_PART);
                break;
            }
        }

        adapterSearchView(mSearchView, "spec", VIEW_SPAREPART, "NAMA_PART", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();

                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof PartBengkel_PartHome_Fragment) {
                        ((PartBengkel_PartHome_Fragment) fragment).viewALLPart(query);
                        break;
                    }else if(fragment instanceof PartOto_PartHome_Activity){
                        ((PartOto_PartHome_Activity) fragment).viewALLPart(query);
                        break;
                    }
                }

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

}