package com.rkrzmail.oto.modules.sparepart.tugas_part;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.naa.data.Nson;
import com.rkrzmail.oto.R;

import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_KOSONG;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;

public class PartKosong_Tugas_Part_Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public PartKosong_Tugas_Part_Fragment() {

    }

    public static PartKosong_Tugas_Part_Fragment newInstance(String param1, String param2) {
        PartKosong_Tugas_Part_Fragment fragment = new PartKosong_Tugas_Part_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Nson dataList = Nson.newArray();
            dataList.add(getArguments().getString(TUGAS_PART_KOSONG));
            Log.d("Tersedia__", "onCreate: " + dataList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        return view;
    }

    private void initHideToolbar(View view){
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }
}