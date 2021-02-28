package com.rkrzmail.srv;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.rkrzmail.utils.ConstUtils.DATE_DEFAULT_PATTERN;

public class DateFormatUtils {

    @SuppressLint("SimpleDateFormat")
    public static String formatDate(String value, String fromPattern, String toPattern){
        if(value.isEmpty()) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(fromPattern);
        Date tgl = null;
        try {
            tgl = sdf.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //yyyy-MM-dd HH:mm:ss
        sdf = new SimpleDateFormat(toPattern);
        return sdf.format(tgl);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateDefault(String value, String toPattern){
        if(value.isEmpty()) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_DEFAULT_PATTERN, Locale.ENGLISH);
        Date tgl = null;
        try {
            tgl = sdf.parse(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sdf = new SimpleDateFormat(toPattern);
        return sdf.format(tgl);
    }
}
