package com.rkrzmail.srv;

import android.text.Editable;
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

        editText.removeTextChangedListener(this);
        if (editText == null) return;
        editText.removeTextChangedListener(this);

        NumberFormat format = NumberFormat.getPercentInstance(new Locale("in", "ID"));
        format.setMinimumFractionDigits(1);
        String percentNumber = format.format(Tools.convertToDoublePercentage(editText.getText().toString())/1000);

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
