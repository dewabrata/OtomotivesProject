package com.rkrzmail.oto.modules.Fragment;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rkrzmail.oto.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Setoran_Pembayaran_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Setoran_Pembayaran_Fragment extends Fragment {


    public Setoran_Pembayaran_Fragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        return view;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

}