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
import android.view.MenuInflater;
import android.view.MenuItem;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.SearchListener;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.PartBengkel_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartOto_PartHome_Fragment;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART_SUGGESTION;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART;

public class PartHome_MainTab_Activity extends AppActivity implements SearchView.OnQueryTextListener, SearchListener.IFragmentListener {

    public static String SEARCH_PART = "SEARCH PART";
    ViewPager vpPart;
    TabLayout tabPart;
    FragmentsAdapter pagerAdapter;
    ArrayList<Fragment> fragments;
    SearchView mSearchView;

    PartBengkel_PartHome_Fragment partBengkel;
    PartOto_PartHome_Fragment partOto;

    ArrayList<SearchListener.ISearch> iSearch = new ArrayList<>();
    SearchListener.IDataCallback dataCallback = null;
    private String queryText;

    private Nson partOtoList = Nson.newArray();

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
        fragments.add(new PartOto_PartHome_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpPart.setAdapter(pagerAdapter);
        vpPart.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabPart));
        vpPart.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabPart.setupWithViewPager(vpPart);
        tabPart.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Cari Part");
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

//        if(isPartBengkel){
//            adapterSearchView(mSearchView, "spec", VIEW_SPAREPART, "NAMA_PART", "");
//        }else{
//            adapterSearchView(mSearchView, "", VIEW_CARI_PART_SUGGESTION, "NAMA_PART", "OTO");
//        }

        return super.onCreateOptionsMenu(menu);
    }

    private void searchInFragment(boolean isSearch, String query){
        final FragmentsAdapter adapter = (FragmentsAdapter) vpPart.getAdapter();
        if(isSearch){
            for (int i = 0; i < adapter.getCount(); i++) {
                Fragment fragments = (Fragment) vpPart.getAdapter().instantiateItem(vpPart, i);
                if(fragments.isAdded()){
                    if(fragments instanceof PartBengkel_PartHome_Fragment){
                        partBengkel = (PartBengkel_PartHome_Fragment) fragments;
                        partBengkel.viewALLPart(query);
                        break;
                    }else if(fragments instanceof PartOto_PartHome_Fragment){
                        partOto = (PartOto_PartHome_Fragment) fragments;
                        partOto.viewALLPart(query);
                        break;
                    }
                }
            }
        }else{
            for (int i = 0; i < Objects.requireNonNull(adapter).getCount(); i++) {
                Fragment fragments = (Fragment) vpPart.getAdapter().instantiateItem(vpPart, i);
                if(fragments.isAdded()){
                    if(fragments instanceof PartBengkel_PartHome_Fragment){
                        partBengkel = (PartBengkel_PartHome_Fragment) fragments;
                        break;
                    }else if(fragments instanceof PartOto_PartHome_Fragment){
                        partOto = (PartOto_PartHome_Fragment) fragments;
                        break;
                    }
                }
            }

        }
    }

    @Override
    public boolean onQueryTextSubmit(String queryText) {
        this.queryText = queryText;
        pagerAdapter.setTextQueryChanged(queryText);

        for(SearchListener.ISearch searchLocal : iSearch){
            searchLocal.onTextQuery(queryText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void setiDataCallback(SearchListener.IDataCallback dataCallback) {
        this.dataCallback = dataCallback;
        //dataCallback.onFragmentCreated();
    }

    @Override
    public void addiSearch(SearchListener.ISearch iSearch) {
        this.iSearch.add(iSearch);
    }

    @Override
    public void removeISearch(SearchListener.ISearch iSearch) {
        this.iSearch.remove(iSearch);
    }

    public void viewALLPartOto(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("search", cari);
                args.put("isPartHome", "Y");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    partOtoList.asArray().clear();
                    partOtoList.asArray().addAll(result.asArray());
                } else {
                    showError("Gagal Mencari Part");
                }
            }
        });
    }

}