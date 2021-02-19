package com.rkrzmail.srv;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils {

    @SuppressLint("SimpleDateFormat")
    public static String formatDate(String value, String fromPattern, String toPattern){
        if(value.isEmpty()) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(fromPattern);
        Date tgl = new Date();
        try {
            tgl = sdf.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //yyyy-MM-dd HH:mm:ss
        sdf = new SimpleDateFormat(toPattern);
        return sdf.format(tgl);
    }
}
