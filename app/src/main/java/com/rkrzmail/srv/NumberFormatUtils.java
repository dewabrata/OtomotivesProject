package com.rkrzmail.srv;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.rkrzmail.utils.Tools;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static com.rkrzmail.utils.ConstUtils.RP;

public class NumberFormatUtils {


    public TextWatcher rupiahTextWatcher(final EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (s.isEmpty()) return;
                editText.removeTextChangedListener(this);
                try {
                    String cleanString = formatOnlyNumber(s);
                    String formatted = RP + formatRp(cleanString);
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editText.addTextChangedListener(this);
            }
        };
    }

    public TextWatcher percentTextWatcher(final EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) return;
                editText.removeTextChangedListener(this);

                NumberFormat format = NumberFormat.getPercentInstance(new Locale("in", "ID"));
                format.setMinimumFractionDigits(1);
                String percentNumber = format.format(Tools.convertToDoublePercentage(editText.getText().toString()) / 1000);

                editText.setText(percentNumber);
                editText.setSelection(percentNumber.length() - 1);
                editText.addTextChangedListener(this);
            }
        };
    }

    public String formatRp(String currency) {
        if (!currency.equals("")) {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            return formatter.format(Double.parseDouble(currency));
        }
        return "0";
    }

    public String formatOnlyNumber(String text) {
        if (text == null || text.equals("") || text.equals("00"))
            return "0";
        else
            return text.replaceAll("[^0-9]+", "");
    }

}
