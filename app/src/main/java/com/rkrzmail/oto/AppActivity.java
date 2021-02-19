package com.rkrzmail.oto;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.modules.TimeHourPicker_Dialog;
import com.rkrzmail.oto.modules.TimePicker_Dialog;
import com.rkrzmail.oto.modules.YearPicker_Dialog;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.SET_STOCK_OPNAME;
import static com.rkrzmail.utils.APIUrls.VIEW_JASA_LAIN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;


public class AppActivity extends AppCompatActivity {


    public void swipeProgress(final boolean show) {
        if (!show) {
            find(R.id.swiperefresh, SwipeRefreshLayout.class).setRefreshing(show);
            return;
        }
        find(R.id.swiperefresh, SwipeRefreshLayout.class).post(new Runnable() {
            @Override
            public void run() {
                find(R.id.swiperefresh, SwipeRefreshLayout.class).setRefreshing(show);
            }
        });
    }


    public void hideKeyboard() {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public String getSetting(String key) {
        return UtilityAndroid.getSetting(getActivity(), key, "");
    }

    public String formatNopol(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
            } else if (i >= 1) {

                if (Utility.isNumeric(stringBuilder.length() >= 1 ? stringBuilder.charAt(stringBuilder.length() - 1) + "" : "") != Utility.isNumeric(s.charAt(i) + "")) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append(s.charAt(i));
            } else {
                stringBuilder.append(s.charAt(i));
            }
        }
        return stringBuilder.toString().trim().toUpperCase();
    }

    public void newTask(Messagebox.DoubleRunnable runnable) {
        MessageMsg.newTask(this, runnable);
    }


    public void newProses(Messagebox.DoubleRunnable runnable) {
        MessageMsg.showProsesBar(this, runnable);
    }

    public void setSetting(String key, String value) {
        UtilityAndroid.setSetting(getActivity(), key, value);
    }

    public Activity getActivity() {
        return this;
    }

    public void showInfo(String text) {
        MDToast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
    }

    public void showInfo(String text, int timeToast) {
        MDToast.makeText(getApplicationContext(), text, timeToast, MDToast.TYPE_INFO).show();
    }

    public void showSuccess(String text) {
        MDToast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
    }

    public void showSuccess(String text, int timeToast) {
        MDToast.makeText(getApplicationContext(), text, timeToast, MDToast.TYPE_SUCCESS).show();
    }

    public void showError(String text) {
        MDToast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
    }

    public void showError(String text, int timeToast) {
        MDToast.makeText(getApplicationContext(), text, timeToast, MDToast.TYPE_ERROR).show();
    }

    public void showWarning(String text, int timeToast) {
        MDToast.makeText(getApplicationContext(), text, timeToast, MDToast.TYPE_WARNING).show();
    }

    public void showWarning(String text) {
        MDToast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
    }

    public String getSelectedSpinnerText(int id) {
        View v = find(id, Spinner.class).getSelectedView();
        if (v instanceof TextView) {
            return to(v, TextView.class).getText().toString();
        }
        return "";
    }

    public void getSelectionSpinner(Spinner spinner, String value) {
        for (int in = 0; in < spinner.getCount(); in++) {
            if (spinner.getItemAtPosition(in).toString().contains(value)) {
                spinner.setSelection(in);
                break;
            }
        }
    }

    public String getIntentStringExtra(String key) {
        return getIntentStringExtra(getIntent(), key);
    }

    public int getIntentIntegerExtra(String key) {
        return getIntentIntExtra(getIntent(), key);
    }

    public String editTextToString(EditText editText) {
        return editText.getText().toString();
    }

    public String getIntentStringExtra(Intent intent, String key) {
        if (intent != null && intent.getStringExtra(key) != null) {
            return intent.getStringExtra(key);
        }
        return "";
    }

    public int getIntentIntExtra(Intent intent, String key) {
        if (intent != null) {
            return intent.getIntExtra(key, 0);
        } else {
            return 0;
        }
    }

    public void showInfoDialog(String message, DialogInterface.OnClickListener onClickListener) {
        if (onClickListener == null) {
            onClickListener = onClickListenerDismiss;
        }
        Messagebox.showDialog(getActivity(), "", message, "OK", "", onClickListener, null);
    }

    public String currentDateTime() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return simpleDateFormat.format(calendar.getTime());
    }

    public String currentDateTime(String pattern) {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        return simpleDateFormat.format(calendar.getTime());
    }

    public void showInfoDialog(String tittle, String message, DialogInterface.OnClickListener onClickListenerOK, DialogInterface.OnClickListener onClickListenerNO) {
        if (onClickListenerOK == null) {
            onClickListenerOK = onClickListenerDismiss;
        }
        Messagebox.showDialog(getActivity(), tittle, message, "OK", "TIDAK", onClickListenerOK, onClickListenerNO);
    }

    private final DialogInterface.OnClickListener onClickListenerDismiss = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void viewImage(ImageView img, String absolutePath) {
        viewImage(img, absolutePath, 128);
    }

    public Nson nListArray = Nson.newArray();

    public final void runOnActionThread(Runnable action) {
        new Thread(action).start();
    }

    public void notifyDataSetChanged(int id) {
        notifyDataSetChanged(findViewById(id));

    }

    public void notifyDataSetChanged(View view) {
        if (view instanceof ListView) {
            if (((ListView) view).getAdapter() instanceof ArrayAdapter) {
                ((ArrayAdapter) ((ListView) view).getAdapter()).notifyDataSetChanged();
            }
        } else if (view instanceof GridView) {
            if (((GridView) view).getAdapter() instanceof ArrayAdapter) {
                ((ArrayAdapter) ((GridView) view).getAdapter()).notifyDataSetChanged();
            }
        } else if (view instanceof Spinner) {
            if (((Spinner) view).getAdapter() instanceof ArrayAdapter) {
                ((ArrayAdapter) ((Spinner) view).getAdapter()).notifyDataSetChanged();
            }
        }

    }

    public static void viewImage(ImageView img, String absolutePath, int wmax) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, options);
        int scale = options.outWidth / wmax;


        options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        Bitmap bmp = BitmapFactory.decodeFile(absolutePath, options);

        img.setImageBitmap(bmp);
    }

    public static void onCompressImage(String file, int quality, int width, int maxpx) {
        String format = "png";
        width = width <= 10 ? 540 : width;
        maxpx = maxpx <= 10 ? 540 : maxpx;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file, options);
            int scale = options.outWidth / width;
            if (maxpx > 1) {
                scale = Math.max(options.outWidth, options.outHeight) / maxpx;
            }

            options = new BitmapFactory.Options();
            options.inSampleSize = scale + 1;
            Bitmap bmp = BitmapFactory.decodeFile(file, options);

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(format.equalsIgnoreCase("jpg") ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, quality, fos);

            fos.flush();
            fos.close();
        } catch (Exception e) {
        }
    }

    public void scanBarcode(View view, Activity activity) {
        new IntentIntegrator(activity).initiateScan();
    }

    public static void rotate(String file, final int move) {
        //mmust on other thread
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file);
            Matrix matrix = new Matrix();
            matrix.postRotate(move);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
        }
    }


    public <T extends View> T to(View v, Class<? super T> s) {
        return (T) (v);
    }

    public <T extends View> T find(int id) {
        return (T) findViewById(id);
    }

    public <T extends View> T find(int id, Class<? super T> s) {
        return (T) findViewById(id);
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }


    protected void onCreateA(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 2) {
                    check(AppActivity.this);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rkrzmail.loyalty");
        registerReceiver(receiver, filter);
    }

    protected void onDestroyA() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private Handler handler;
    AlertDialog alertDialog;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equalsIgnoreCase("com.rkrzmail.loyalty")) {
                    check(context);
                }
            }
        }
    };

    private void check(Context context) {
        if (haveNetworkConnection()) {
                        /*findViewById(R.id.navigation).setVisibility(View.VISIBLE);
                        findViewById(R.id.content).setVisibility(View.VISIBLE);*/

            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

        } else {
            //log
                        /*findViewById(R.id.navigation).setVisibility(View.INVISIBLE);
                        findViewById(R.id.content).setVisibility(View.INVISIBLE);*/

            if (alertDialog != null && alertDialog.isShowing()) {
            } else if (alertDialog != null) {
                alertDialog.show();
            } else {
                AlertDialog.Builder dlg = new AlertDialog.Builder(context);

                dlg.setTitle("No Intenet Connection");
                dlg.setMessage("Please Check your connection ");
                dlg.setCancelable(false);

                alertDialog = dlg.create();
                alertDialog.show();
            }
            handler.removeMessages(2);
            //check jangan jangan 30d lagi conect
            handler.sendEmptyMessageDelayed(2, 30000);

        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (handler != null) {
            handler.removeMessages(1);
            handler.removeMessages(2);
        }
    }

    public void minEntryEditText(EditText editText, final int min, final TextInputLayout textLayout, final String message) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    textLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.length() < min) {
                    textLayout.setError(message);
                } else {
                    textLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    textLayout.setErrorEnabled(false);
                }
            }
        });
    }

    public void getDatePickerDialogTextView(Context context, final TextView dateTime) {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                dateTime.setText(formattedTime);
            }
        }, year, month, day);
        //datePickerDialog.setMinDate();
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public void getDatePickerDialogTextView(Context context, final TextView dateTime, Calendar minDate) {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                dateTime.setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setMinDate(minDate);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }


    public void getDatePickerDialogTextView(Context context, final TextView dateTime, Calendar minDate, Calendar maxDate, Calendar[] disableDays) {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                dateTime.setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setMinDate(minDate);
        datePickerDialog.setMaxDate(maxDate);
        datePickerDialog.setDisabledDays(disableDays);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public static void getTimePickerDialogTextView(Context context, final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String time = hourOfDay + ":" + minutes;
                Date date = null;
                try {
                    date = sdf.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                textView.setText(formattedTime);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setTitle("Pilih Jam");
        timePickerDialog.show();
    }


    public void adapterSearchView(final android.support.v7.widget.SearchView searchView, final String arguments, final String api, final String jsonObject, final String flag) {
        final boolean[] isNoPart = new boolean[1];
        final boolean[] isJasaLain = new boolean[1];
        final boolean[] isStockOpname = new boolean[1];
        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.drawable.bg_radius_white);
        searchAutoComplete.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                if (!flag.equals("OTO")) {
                    args.put(arguments, "Bengkel");
                }

                args.put("flag", flag);
                args.put("search", bookTitle);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(api), args));
                result = result.get("data");
                result = Tools.removeDuplicates(result);
                isJasaLain[0] = api.equals(VIEW_JASA_LAIN) & result.get(0).get("AKTIVITAS").asString().toLowerCase().contains(bookTitle.toLowerCase());
                isNoPart[0] = (
                                api.equals(VIEW_SPAREPART) |
                                api.equals(VIEW_SUGGESTION) |
                                api.equals(VIEW_LOKASI_PART) |
                                api.equals(SET_STOCK_OPNAME)
                ) & result.get(0).get("NO_PART").asString().contains(bookTitle);
                isStockOpname[0] = api.equals(SET_STOCK_OPNAME) & result.get(0).get("KODE").asString().toLowerCase().contains(bookTitle.toLowerCase().replace(" ", ""));
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                String search;
                if (isNoPart[0]) {
                    search = getItem(position).get("NO_PART").asString();
                } else {
                    if (isJasaLain[0]) {
                        search = getItem(position).get("AKTIVITAS").asString();
                    } else {
                        if (isStockOpname[0]) {
                            search = getItem(position).get("KODE").asString();
                        } else {
                            if (!getItem(position).containsKey("NAMA_LAIN")) {
                                if (getItem(position).containsKey("NOPOL")) {
                                    search = formatNopol(getItem(position).get(jsonObject).asString());
                                } else {
                                    search = getItem(position).get(jsonObject).asString();
                                }
                            } else {
                                search = getItem(position).get(jsonObject).asString();
                            }
                        }
                    }
                }

                findView(convertView, R.id.title, TextView.class).setText(search);
                return convertView;
            }
        });
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(i)));
                String object;
                if (isNoPart[0]) {
                    object = n.get("NO_PART").asString();
                } else {
                    if (isJasaLain[0]) {
                        object = n.get("AKTIVITAS").asString();
                    } else {
                        if (isStockOpname[0]) {
                            object = n.get("KODE").asString();
                        } else {
                            object = n.get(jsonObject).asString();
                        }
                    }
                }
                find(R.id.search_src_text, SearchView.SearchAutoComplete.class).setText(object);
                find(R.id.search_src_text, SearchView.SearchAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(i)));
                searchView.setQuery(object, true);
            }
        });
    }

    public void remakeAutoCompleteMaster(final NikitaAutoComplete editText, final String params, final String... jsonObject) {
        editText.setThreshold(0);
        editText.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", params);
                args.put("search", bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewmst"), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                if (getItem(position).get("JENIS").asString() != null) {
                    findView(convertView, R.id.title, TextView.class).setText(getItem(position).get("JENIS").asString());
                }
                findView(convertView, R.id.title2, TextView.class).setText(getItem(position).get(jsonObject[0]).asString());
                return convertView;
            }
        });

        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(i)));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get(jsonObject[0]).asString()).append(" ");

                editText.setText(stringBuilder.toString());
                editText.setTag(String.valueOf(adapterView.getItemAtPosition(i)));
            }
        });
    }

    public void setMultiSelectionSpinnerFromApi(final MultiSelectionSpinner spinner, final String params, final String arguments, final String api, final MultiSelectionSpinner.OnMultipleItemsSelectedListener listener, final String... jsonObject) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put(params, arguments);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(api), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> str = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get(jsonObject[0]).asString() + " " + result.get("data").get(i).get(jsonObject[1]).asString());
                }
                str.removeAll(Arrays.asList(" ", null));
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                try {
                    spinner.setItems(newStr);
                    spinner.setListener(listener);
                } catch (Exception e) {
                    e.printStackTrace();
                    showInfo("Perlu di Muat Ulang");
                }
            }
        });
    }

    public void setSpinnerFromApi(final Spinner spinner, final String params, final String arguments, final String api, final String jsonObject) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put(params, arguments);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(api), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> str = new ArrayList<>();
                str.add("--PILIH--");
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get(jsonObject).asString());
                }
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                notifyDataSetChanged(spinner);
            }
        });
    }

    public void setSpinnerFromApi(final Spinner spinner, final String params, final String arguments, final String api, final String jsonObject, final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put(params, arguments);
                if (api.equals(VIEW_LAYANAN)) {
                    args.put("spec", "OTOMOTIVES");
                    args.put("layanan", "BENGKEL");
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(api), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> str = new ArrayList<>();
                str.add("--PILIH--");
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get(jsonObject).asString());
                }
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                if (!selection.isEmpty()) {
                    for (int i = 0; i < newStr.size(); i++) {
                        if (spinner.getItemAtPosition(i).toString().equals(selection)) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    public void setSpinnerOffline(List listItem, Spinner spinner, String selection) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, listItem);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        if (!selection.isEmpty()) {
            for (int in = 0; in < spinner.getCount(); in++) {
                if (spinner.getItemAtPosition(in).toString().contains(selection)) {
                    spinner.setSelection(in);
                    break;
                }
            }
        }
    }

    private EditText getAllEditText(ViewGroup v, boolean isEmpty) {
        EditText invalid = null;
        for (int i = 0; i < v.getChildCount(); i++) {
            Object child = v.getChildAt(i);
            if (child instanceof EditText) {
                EditText e = (EditText) child;
                if (e.getText().length() == 0 && !isEmpty) {    // Whatever logic here to determine if valid.
                    e.setError("Harus Di isi");
                    return e;   // Stops at first invalid one. But you could add this to a list.
                }
            } else if (child instanceof ViewGroup) {
                invalid = getAllEditText((ViewGroup) child, !isEmpty);  // Recursive call.
                if (invalid != null) {
                    break;
                }
            }
        }
        return invalid;
    }

    public boolean validateFields(ViewGroup viewGroup) {
        EditText emptyText = getAllEditText(viewGroup, false);
        if (emptyText != null) {
            emptyText.setError("Harus di isi");
            emptyText.requestFocus();
        }
        return emptyText != null;
    }

    public boolean validateFields(ViewGroup viewGroup, EditText editText) {
        boolean ex = editText.getText().toString().isEmpty();
        EditText emptyText = getAllEditText(viewGroup, ex);
        if (emptyText != null) {
            if (!ex) {
                emptyText.setError("test di isi");
                emptyText.requestFocus();
            }
        }
        return emptyText != null;
    }

    public void spinnerNoDefaultOffline(Spinner spinner, String[] resources) {
        ArrayAdapter<String> tipeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, resources) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText(null);
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(""); //"Hint to be displayed"
                }
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount();            // you don't display last item. It is used as hint.
            }
        };
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(tipeAdapter);
        spinner.setSelection(resources.length - 1);
        notifyDataSetChanged(spinner);
    }

    public void getTimesDialog(final EditText ddHHmm) {
        TimePicker_Dialog timePickerDialog = new TimePicker_Dialog();
        timePickerDialog.show(getSupportFragmentManager(), "TimePicker");
        timePickerDialog.getTimes(new TimePicker_Dialog.OnClickDialog() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void getTime(int day, int hours, int minutes) {
                ddHHmm.setText(String.format("%02d", day) + ":" + String.format("%02d", hours) + ":" + String.format("%02d", minutes));
            }

            @Override
            public void getYear(int year) {

            }
        });
    }

    public void getTimeHourDialog(final EditText ddHHmm) {
        TimeHourPicker_Dialog timePickerDialog = new TimeHourPicker_Dialog();
        timePickerDialog.show(getSupportFragmentManager(), "TimePicker");
        timePickerDialog.getTimes(new TimeHourPicker_Dialog.OnClickDialog() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void getTime(int hours, int minutes) {
                ddHHmm.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes));
            }

            @Override
            public void getYear(int year) {

            }
        });
    }

    public void getYearsDialog(final TextView etYear) {
        YearPicker_Dialog dialog = new YearPicker_Dialog();
        dialog.show(getSupportFragmentManager(), "YearPicker");
        dialog.getYears(new TimePicker_Dialog.OnClickDialog() {
            @Override
            public void getTime(int day, int hours, int minutes) {

            }

            @Override
            public void getYear(int year) {
                etYear.setText(String.valueOf(year));
            }
        });
    }

    public String formatRp(String currency) {
        if (!currency.equals("")) {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            return formatter.format(Double.parseDouble(currency));
        }
        return "0";
    }

    public SpannableString setUnderline(String text) {
        SpannableString content = new SpannableString(text);
        content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        return content;
    }

    public String formatOnlyNumber(String text) {
        if (text == null || text.equals("") || text.equals("00"))
            return "0";
        else
            return text.replaceAll("[^0-9]+", "");
    }

    public void watcherNamaPelanggan(final ImageButton imageButton, final EditText editText) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    imageButton.setVisibility(View.GONE);
                } else {
                    imageButton.setVisibility(View.VISIBLE);
                }
            }
        };
        editText.addTextChangedListener(textWatcher);
    }

    public void getDateSpinnerDialog(final TextView dateTime, String tittle) {
        Calendar cal = Calendar.getInstance();
        int day, month, year;
        day = cal.get(Calendar.DAY_OF_MONTH);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);

        android.app.DatePickerDialog.OnDateSetListener mDateListener = new android.app.DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                dateTime.setText(formattedTime);
            }
        };
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateListener,
                year, month, day
        );
        dialog.setTitle(tittle);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @SuppressLint("DefaultLocale")
    public String totalWaktuKerja(String hari, String jam, String menit) {
        boolean isSubsString = false;
        String[] result = new String[3];
        result[0] = hari;
        result[1] = jam;
        result[2] = menit;

        int incrementWaktu = 0;
        int calculateJam = 0;
        int calculateHari = 0;

        for (String s : result) {
            if (s.contains(":")) {
                isSubsString = true;
                break;
            }
        }

        try {
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
        } catch (Exception e) {
            return String.format("%02d:%02d:%02d", 0, 0, 0);
        }
    }

    public String generateNoAntrian(String statusAntrian, String noAntrian) {
        String result = "";
        String currentDateTime = Tools.setFormatDayAndMonthFromDb(currentDateTime("yyyy-MM-dd"), "dd/MM");
        if (!noAntrian.equals("")) {
            switch (statusAntrian) {
                case "STANDART":
                    result = "S" + "." + currentDateTime + "." + noAntrian;
                    break;
                case "EXTRA":
                    result = "E" + "." + currentDateTime + "." + noAntrian;
                    break;
                case "EXPRESS":
                    result = "EX" + "." + currentDateTime + "." + noAntrian;
                    break;
                default:
                    result = "H" + "." + currentDateTime + "." + noAntrian;
                    break;
            }
        }
        return result;
    }

    public boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("");
                alertBuilder.setMessage("");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void setIntent(Class destination, int reqCode) {
        Intent i = new Intent(getActivity(), destination);
        startActivityForResult(i, reqCode);
    }

    public void showNotification(Context context, String title, String body, String channelName, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt();
        String channelId = "channel-01";
        @SuppressLint("InlinedApi") int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.speed)
                .setContentTitle(title)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    public void showMessageInvalidNotif(Context context, String messageInfo, Intent intent) {
        if (
                Utility.isNumeric(messageInfo) ||
                        messageInfo.equalsIgnoreCase("Mysql") ||
                        messageInfo.isEmpty()
        )
            return;
        showNotification(context, "Info", messageInfo, "NOMOR PONSEL", intent);
    }

}