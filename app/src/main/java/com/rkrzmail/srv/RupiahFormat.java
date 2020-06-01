package com.rkrzmail.srv;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.rkrzmail.utils.Tools;

import java.lang.ref.WeakReference;

public class RupiahFormat implements TextWatcher {

    private final WeakReference<EditText> editTextWeakReference;

    public RupiahFormat(EditText editText) {
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
        if (editText == null) return;
        String s = editable.toString();
        if (s.isEmpty()) return;
        editText.removeTextChangedListener(this);
        try {
            String cleanString = s.replaceAll("[^0-9]", "");
            String formatted = Tools.formatRupiah(cleanString);
            editText.setText(formatted);
            editText.setSelection(formatted.length());

        } catch (Exception e) {
            e.printStackTrace();
        }

        editText.addTextChangedListener(this);
    }
}
