package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import com.rkrzmail.oto.gmod.part.AdapterListBasic;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.DaftarUser_User_Fragment;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.GajiUser_User_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.ArrayList;
import java.util.Map;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class User_MainTab_Activity extends AppActivity {

    private RecyclerView recyclerView;
    private AdapterListBasic mAdapter;

    private String isSuccess = "";

    ViewPager vpAbsensi;
    TabLayout tabAbsensi;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent(){
        vpAbsensi = findViewById(R.id.vp);
        tabAbsensi = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new DaftarUser_User_Fragment());
        fragments.add(new GajiUser_User_Fragment());


        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpAbsensi.setAdapter(pagerAdapter);
        vpAbsensi.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabAbsensi));
        tabAbsensi.setupWithViewPager(vpAbsensi);
    }


    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Nama Karyawan"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        adapterSearchView(mSearchView, "search", "aturkaryawan", "NAMA", "");
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
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
}
