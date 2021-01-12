package com.rkrzmail.srv;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

import com.rkrzmail.utils.Tools;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Locale;

public class PercentFormat implements TextWatcher {

    private final WeakReference<EditText> editTextWeakReference;

    public PercentFormat(EditText editText) {
        editTextWeakReference = new WeakReference<EditText>(editText);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        EditText editText = editTextWeakReference.get();

        String text = editable.toString();
        if (text == null || text.isEmpty()) return;
        editText.removeTextChangedListener(this);

        text = new NumberFormatUtils().formatOnlyNumber(text);
        double percentValue = Double.parseDouble(text.isEmpty() ? "0" : text) / 1000;

        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(1);
        String percentNumber = format.format(percentValue);

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(6);

        editText.setFilters(filterArray);
        editText.setText(percentNumber);
        editText.setSelection(percentNumber.length() -1);
        editText.addTextChangedListener(this);
    }


    public static double calculatePercentage(double percent, int value) {
        if (percent > 0 && value > 0) {
            return (percent / 100) * value;
        }
        return 0;
    }

}
