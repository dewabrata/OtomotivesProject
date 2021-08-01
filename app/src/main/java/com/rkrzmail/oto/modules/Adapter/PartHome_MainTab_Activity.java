package com.rkrzmail.oto.modules.Adapter;

import android.annotation.SuppressLint;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.AlertPart_PartHome_Fragment;
import com.rkrzmail.srv.SearchListener;
import com.rkrzmail.oto.modules.Adapter.FragmentsAdapter;
import com.rkrzmail.oto.modules.Fragment.PartBengkel_PartHome_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartOto_PartHome_Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class PartHome_MainTab_Activity extends AppActivity {

    public static final String SEARCH_PART = "SEARCH PART";
    public static final String SEARCH_TAG = "SEARCH TAG";
    public static final String TAB_POSITION = "TAB POSITION";

    private ViewPager vpPart;
    private FragmentsAdapter pagerAdapter;
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
        TabLayout tabPart = findViewById(R.id.tablayout);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new PartBengkel_PartHome_Fragment());
        fragments.add(new AlertPart_PartHome_Fragment());
        fragments.add(new PartOto_PartHome_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpPart.setAdapter(pagerAdapter);
        vpPart.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabPart));
        tabPart.setupWithViewPager(vpPart);
    }
}