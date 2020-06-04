package com.rkrzmail.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.rkrzmail.oto.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SpinnerNoDefault extends android.support.v7.widget.AppCompatSpinner {


    public SpinnerNoDefault(Context context) {
        super(context);
    }

    public void initialize(String[] items) {
        initialize(Arrays.asList(items), android.R.layout.simple_spinner_item, android.R.layout.simple_spinner_dropdown_item);
    }

    public void initialize(List<String> items, @LayoutRes int mItemResId, @LayoutRes int dropdownResId) {
        List<String> itemsWithEmpty = new ArrayList<>(items);
        itemsWithEmpty.add("");

        String[] choicesArray = itemsWithEmpty.toArray(new String[itemsWithEmpty.size()]);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), mItemResId, choicesArray) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText(null);
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(parent.getContext().getString(R.string.spinner_no_default_hint));
                }
                return v;
            }

            @Override
            public int getCount() {
                // Don't display last item. It is used as hint.
                return super.getCount() - 1;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(dropdownResId);
        setAdapter(spinnerArrayAdapter);
        setSelection(itemsWithEmpty.size() - 1);
    }
}