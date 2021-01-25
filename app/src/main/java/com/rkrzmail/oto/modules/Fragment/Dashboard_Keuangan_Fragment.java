package com.rkrzmail.oto.modules.Fragment;

import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rkrzmail.oto.R;

public class Dashboard_Keuangan_Fragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private String argsRefresh = "";

    public Dashboard_Keuangan_Fragment() {

    }

    public static Dashboard_Keuangan_Fragment newInstance(String param1, String param2) {
        Dashboard_Keuangan_Fragment fragment = new Dashboard_Keuangan_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            argsRefresh = getArguments().toString();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        initHideToolbar(view);

        return view;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

}