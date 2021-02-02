package com.rkrzmail.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.maps.GoogleMap;
import com.naa.data.Nson;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Tools {

    public static void setSystemBarColor(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(color));
        }
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void clearSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = act.getWindow();
            window.setStatusBarColor(ContextCompat.getColor(act, R.color.colorPrimaryDark));
        }
    }

    /**
     * Making notification bar transparent
     */
    public static void setSystemBarTransparent(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void displayImageOriginal(Context ctx, ImageView img, @DrawableRes int drawable) {
        try {
            Glide.with(ctx).load(drawable)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageRound(final Context ctx, final ImageView img, @DrawableRes int drawable) {
        try {
            Glide.with(ctx).load(drawable).asBitmap().centerCrop().into(new BitmapImageViewTarget(img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    img.setImageDrawable(circularBitmapDrawable);
                }
            });
        } catch (Exception e) {
        }
    }

    public static void displayImageOriginal(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDateEvent(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("EEE, MMM dd yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedTimeEvent(Long time) {
        SimpleDateFormat newFormat = new SimpleDateFormat("h:mm a");
        return newFormat.format(new Date(time));
    }

    public static String getEmailFromName(String name) {
        if (name != null && !name.equals("")) {
            String email = name.replaceAll(" ", ".").toLowerCase().concat("@mail.com");
            return email;
        }
        return name;
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static GoogleMap configActivityMaps(GoogleMap googleMap) {
        // set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);

        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(true);
        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        return googleMap;
    }

    public static void copyToClipboard(Context context, String data) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clipboard", data);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
        nested.post(new Runnable() {
            @Override
            public void run() {
                nested.scrollTo(500, targetView.getBottom());
            }
        });
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }

    public static boolean toggleArrow(boolean show, View view) {
        return toggleArrow(show, view, true);
    }

    public static boolean toggleArrow(boolean show, View view, boolean delay) {
        if (show) {
            view.animate().setDuration(delay ? 200 : 0).rotation(180);
            return true;
        } else {
            view.animate().setDuration(delay ? 200 : 0).rotation(0);
            return false;
        }
    }

    public static void changeNavigateionIconColor(Toolbar toolbar, @ColorInt int color) {
        Drawable drawable = toolbar.getNavigationIcon();
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void changeOverflowMenuIconColor(Toolbar toolbar, @ColorInt int color) {
        try {
            Drawable drawable = toolbar.getOverflowIcon();
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } catch (Exception e) {
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static String toCamelCase(String input) {
        input = input.toLowerCase();
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static String insertPeriodically(String text, String insert, int period) {
        StringBuilder builder = new StringBuilder(text.length() + insert.length() * (text.length() / period) + 1);
        int index = 0;
        String prefix = "";
        while (index < text.length()) {
            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index, Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }

    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static int getIndexSpinner(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    @SuppressLint("SimpleDateFormat")
    public static String setFormatDayAndMonthToDb(String date) {
        if(date.isEmpty()) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date tgl = new Date();
        try {
            tgl = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(tgl);
    }

    @SuppressLint("SimpleDateFormat")
    public static String setFormatDayAndMonthFromDb(String date) {
        if (!date.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date tgl = new Date();
            try {
                tgl = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat("dd MMM");
            return sdf.format(tgl);
        }

        return "";
    }


    @SuppressLint("SimpleDateFormat")
    public static String setFormatDayAndMonthFromDb(String date, String pattern) {
        if (!date.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date tgl = new Date();
            try {
                tgl = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat(pattern);
            return sdf.format(tgl);
        }

        return "";
    }

    @SuppressLint("SimpleDateFormat")
    public static String setFormatDateTimeFromDb(String date) {
        if (!date.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            Date tgl = new Date();
            try {
                tgl = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat("dd MM");
            return sdf.format(tgl);
        }

        return "";
    }

    @SuppressLint("SimpleDateFormat")
    public static String setFormatDateTimeFromDb(String date, String fromPattern, String setPattern, boolean isDefaultPattern) {
        if (!date.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat(isDefaultPattern ? "yyyy-MM-dd hh:mm:ss" : fromPattern);
            Date tgl = new Date();
            try {
                tgl = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat(setPattern);
            return sdf.format(tgl);
        }

        return "";
    }

    @SuppressLint("SimpleDateFormat")
    public static String setDateTimeToDb(String date) {
        if (!date.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date tgl = new Date();
            try {
                tgl = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //yyyy-MM-dd HH:mm:ss
            sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return sdf.format(tgl);
        }
        return "";
    }

    @SuppressLint("SimpleDateFormat")
    public static String parseTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date tgl = new Date();
        try {
            tgl = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //yyyy-MM-dd HH:mm:ss
        sdf = new SimpleDateFormat("d HH:mm");
        String fotmatDate = sdf.format(tgl);
        return fotmatDate;
    }

    public static String formatRupiah(String number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(parseDouble(number));
    }

    public static void clearForm(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setText("");
                ((EditText) view).clearFocus();
            }
            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearForm((ViewGroup) view);
        }
    }

    public  static String getDayOfWeek(int dy, Boolean isDefault ) {
        Calendar c = Calendar.getInstance();
        int day = 0;
        if(isDefault){
            day = c.get(Calendar.DAY_OF_WEEK);
        }else{
            day=dy;
        }

        switch (day) {
            case Calendar.SUNDAY:
                return "Minggu";
            case Calendar.MONDAY:
                return "Senin";
            case Calendar.TUESDAY:
                return "Selesa";
            case Calendar.WEDNESDAY:
                return "Rabu";
            case Calendar.THURSDAY:
                return "Kamis";
            case Calendar.FRIDAY:
                return "Jumat";
            case Calendar.SATURDAY:
                return "Sabtu";
            default:
                return "";
        }
    }

    public static String formatPercent(String number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getPercentInstance(localeID);
        formatRupiah.setMaximumFractionDigits(4);
        String percentNumber = formatRupiah.format(Tools.parseDouble(number) * 100);
        //double formatDouble = Tools.parseDouble(number);
        return percentNumber;
    }

    public static double parseDouble(String strNumber) {
        if (strNumber != null && strNumber.length() > 0) {
            try {
                return Double.parseDouble(strNumber);
            } catch (Exception e) {
                return -1;   // or some value to mark this field is wrong. or make a function validates field first ...
            }
        } else
            return 0;
    }

    public static double convertToDoublePercentage(String value) {
        double convertedNumber = 0;
        NumberFormat nf = new DecimalFormat("##,##");
        try {
            convertedNumber = nf.parse(value).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedNumber;
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    public static <T> List<T> removeDuplicates(List<T> list) {
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    public static Nson removeDuplicates(Nson list) {
        Nson nson = Nson.newArray();
        for (Object object : list.asArray()) {
            if (!nson.asArray().contains(object)) {
                nson.asArray().add(object);
            }
        }
        return nson;
    }

    public static void hideKeyboard(Activity context){
        try {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
        }
    }


    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static boolean isNumeric(String str) {
        if(str.isEmpty() || str == null) return false;
        return str.matches("-?\\d+(.\\d+)?");
    }

    public static class TimePart {

        int days = 0;
        int hours = 0;
        int minutes = 0;

        public static TimePart parse(String in) {
            if (in != null) {
                String[] arr = in.split(":");
                TimePart tp = new TimePart();
                tp.days = ((arr.length >= 1) ? Integer.parseInt(arr[0]) : 0);
                tp.hours = ((arr.length >= 2) ? Integer.parseInt(arr[1]) : 0);
                tp.minutes = ((arr.length >= 2) ? Integer.parseInt(arr[2]) : 0);
                return tp;
            }
            return null;
        }

        public TimePart subtraction(TimePart a) {
            int of = 0;
            this.minutes -= a.minutes + of;
            of = 0;
            while (this.minutes >= 60) {
                of++;
                this.minutes -= 60;
            }
            this.hours -= a.hours + of;
            of = 0;
            while (this.hours >= 24) {
                of++;
                this.hours -= 24;
            }
            this.days -= a.days + of;
            return this;
        }

        public TimePart add(TimePart a) {
            int of = 0;
            this.minutes += a.minutes + of;
            of = 0;
            while (this.minutes >= 60) {
                of++;
                this.minutes -= 60;
            }
            this.hours += a.hours + of;
            of = 0;
            while (this.hours >= 24) {
                of++;
                this.hours -= 24;
            }
            this.days += a.days + of;
            return this;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public String toString() {
            return String.format("%02d:%02d:%02d", days, hours, minutes);
        }


    }

    public static String getmonth(int month) {
        if (month == 0 || String.valueOf(month) == null || String.valueOf(month).isEmpty()) return "";
            switch (month) {
                case 1:
                    return "January";

                case 2:
                    return "February";

                case 3:
                    return "March";

                case 4:
                    return "April";

                case 5:
                    return "May";

                case 6:
                    return "June";

                case 7:
                    return "July";

                case 8:
                    return "August";

                case 9:
                    return "September";

                case 10:
                    return "October";

                case 11:
                    return "November";

                case 12:
                    return "December";
            }
        return "January";
    }

    public static String getDay(int day){
        try {
            switch (day) {
                case 0:
                    return "Senin";
                case 1:
                    return "Selasa";
                case 2:
                    return "Rabu";
                case 3:
                    return "Kamis";
                case 4:
                    return "Jum`at";
                case 5:
                    return "Sabtu";
                case 6:
                    return "Minggu";
                default:
                    return "";
            }
        }catch (Exception e){
            return "";
        }
    }


    public static String isSingleQuote(String text){
        if(text.isEmpty()) return "";

        StringBuilder textBuilder = new StringBuilder(text);
        for (int i = 0; i < textBuilder.length(); i++) {
            if(textBuilder.charAt(i) == '\''){
                textBuilder.insert(i, '\'');
                break;
            }
        }

        return textBuilder.toString();
    }
}
