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

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.SearchListener;
import com.rkrzmail.oto.modules.Fragment.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.PartBengkel_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartOto_PartHome_Fragment;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_CARI_PART_SUGGESTION;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;

public class PartHome_MainTab_Activity extends AppActivity implements SearchView.OnQueryTextListener, SearchListener.IFragmentListener, TabLayout.BaseOnTabSelectedListener {

    public static final String SEARCH_PART = "SEARCH PART";
    public static final String SEARCH_TAG = "SEARCH TAG";

    private ViewPager vpPart;
    private TabLayout tabPart;
    private FragmentsAdapter pagerAdapter;
    private ArrayList<Fragment> fragments;
    private SearchView mSearchView;

    private final ArrayList<SearchListener.ISearch> iSearch = new ArrayList<>();
    private final ArrayList<SearchListener.ISearchAutoComplete> searchAutoCompleteArrayList = new ArrayList<>();
    private String queryText;
    private int tabPosition;
    private String searchViewTag;

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
        tabPart.setupWithViewPager(vpPart);
        tabPart.addOnTabSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        try {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            mSearchView = new SearchView(getSupportActionBar().getThemedContext());
            mSearchView.setQueryHint("Cari Part");
            mSearchView.setMaxWidth(Integer.MAX_VALUE);

            final MenuItem searchMenu = menu.findItem(R.id.action_search);
            searchMenu.setActionView(mSearchView);
            searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setSubmitButtonEnabled(true);
            mSearchView.setOnQueryTextListener(this);
        } catch (Exception e) {
            showError(e.getMessage());
        }

        final SearchView.SearchAutoComplete searchAutoComplete = mSearchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.drawable.bg_radius_white);

        for (SearchListener.ISearchAutoComplete autoComplete : searchAutoCompleteArrayList) {
            autoComplete.attachAdapter(searchAutoComplete);
        }

        //adapterSearchView(mSearchView, "", VIEW_CARI_PART_SUGGESTION, "NAMA_PART", "OTO");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String queryText) {
        this.queryText = queryText;
        pagerAdapter.setTextQueryChanged(queryText);
        pagerAdapter.setSearchViewTag(tabPosition == 0 ? "BENGKEL" : "OTO");

        for (SearchListener.ISearch searchLocal : iSearch) {
            searchLocal.onTextQuery(queryText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    public void addiSearch(SearchListener.ISearch iSearch, SearchListener.ISearchAutoComplete autoComplete) {
        this.iSearch.add(iSearch);
        this.searchAutoCompleteArrayList.add(autoComplete);
    }

    @Override
    public void removeISearch(SearchListener.ISearch iSearch, SearchListener.ISearchAutoComplete autoComplete) {
        this.iSearch.remove(iSearch);
        this.searchAutoCompleteArrayList.remove(autoComplete);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabPosition = tab.getPosition();
        this.searchViewTag = tabPosition == 0 ? "BENGKEL" : "OTO";
        vpPart.setCurrentItem(tab.getPosition());
        pagerAdapter.setSearchViewTag(searchViewTag);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}