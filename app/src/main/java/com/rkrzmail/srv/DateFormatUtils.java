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
        try{
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
        }catch (Exception e){
            return "";
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateToDatabase(String value){
        if(value.isEmpty()) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date tgl = null;
        try {
            tgl = sdf.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //yyyy-MM-dd HH:mm:ss
        sdf = new SimpleDateFormat("yyyy-MM-dd");
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

    @SuppressLint("DefaultLocale")
    public static String totalWaktuTimeStamps(String hari, String jam, String menit) {
        String[] result = new String[3];
        result[0] = hari;
        result[1] = jam;
        result[2] = menit;

        int incrementWaktu = 0;
        int calculateJam = 0;
        int calculateHari = 0;

        if (!menit.equals("0")) {
            int minutes = Integer.parseInt(menit);
            while (minutes >= 60) {
                incrementWaktu++;
                minutes -= 60;
            }
            if (incrementWaktu > 0) {
                calculateJam = incrementWaktu;
                result[2] = String.valueOf(minutes);
            }
        } else {
            result[2] = "0";
        }
        if (!jam.equals("0") || calculateJam > 0) {
            incrementWaktu = 0;
            int finalJam = Integer.parseInt(jam) + calculateJam;
            result[1] = String.valueOf(finalJam);
            while (finalJam >= 24) {
                incrementWaktu++;
                finalJam -= 24;
            }
            if (incrementWaktu > 0) {
                calculateHari = incrementWaktu;
            }
        } else {
            result[1] = "0";
        }
        if (!hari.equals("0") || calculateHari > 0) {
            int finalJam = Integer.parseInt(hari) + calculateHari;
            result[0] = String.valueOf(finalJam);
        } else {
            result[0] = "0";
        }

        return String.format("%02d:%02d:%02d", Integer.parseInt(result[0]), Integer.parseInt(result[1]), Integer.parseInt(result[2]));
    }

}
