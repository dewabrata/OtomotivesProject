package com.rkrzmail.srv;


import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.rkrzmail.utils.Tools;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

    public TextWatcher rupiahTextWatcher(final EditText editText, final ImageButton imgDelete) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().length() == 0) {
                    imgDelete.setVisibility(View.GONE);
                } else {
                    imgDelete.setVisibility(View.VISIBLE);
                }
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

    public TextWatcher rupiahTextWatcher(final EditText editText, final EditText enabled) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enabled.setEnabled(true);
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
                String text = editable.toString();
                if (text.isEmpty()) return;
                editText.removeTextChangedListener(this);

                try {
                    text = setPercentage(text);
                    editText.setFilters(getPercentFilter());
                    editText.setText(text);
                    editText.setSelection(text.length() - 1);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                editText.addTextChangedListener(this);
            }
        };
    }

    public static String setPercentage(String values) {
        if (values.isEmpty()) return "0";

        values = formatOnlyNumber(values);
        double percentValue = Double.parseDouble(values) / 1000;
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(1);
        return percentageFormat.format(percentValue);
    }

    public static InputFilter[] getPercentFilter() {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(6);
        return filterArray;
    }

    public static String formatRp(String currency) {
        if (!currency.equals("")) {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            return formatter.format(Double.parseDouble(currency));
        }
        return "0";
    }

    public static String formatPercent(double percentValue) {
        if (percentValue == 0) return "0.0";

        double result = percentValue / 100;
        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(1);

        return percentageFormat.format(result);
    }

    public static String clearPercent(String value) {
        if (value.isEmpty() || value == null)
            return "0";
        else
            return value.trim().replace("%", "").replace(",", ".");
    }


    public static String formatOnlyNumber(String text) {
        if (text == null || text.equals("") || text.equals("00"))
            return "0";
        else
            return text.replaceAll("[^0-9]+", "");
    }

    public static double calculatePercentage(double percent, int value) {
        if (percent > 0 && value > 0) {
            return (percent / 100) * value;
        }
        return 0;
    }

}
