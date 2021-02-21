package com.rkrzmail.oto.modules.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rkrzmail.oto.R;

public class Kewajiban_Hutang_Fragment extends Fragment {

    private View fragmentView;

    public Kewajiban_Hutang_Fragment (){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        return fragmentView;
    }
}
