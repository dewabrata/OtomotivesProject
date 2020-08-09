package com.rkrzmail.oto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
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
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class AppActivity extends AppCompatActivity {

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

    public void showSuccess(String text) {
        MDToast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
    }

    public void showError(String text) {
        MDToast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
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

    public String getIntentStringExtra(String key) {
        return getIntentStringExtra(getIntent(), key);
    }

    public String editTextToString(EditText editText){
        return editText.getText().toString();
    }

    public String getIntentStringExtra(Intent intent, String key) {
        if (intent != null && intent.getStringExtra(key) != null) {
            return intent.getStringExtra(key);
        }
        return "";
    }

    public void showInfoDialog(String message, DialogInterface.OnClickListener onClickListener) {
        if (onClickListener == null) {
            onClickListener = onClickListenerDismiss;
        }
        Messagebox.showDialog(getActivity(), "", message, "OK", "", onClickListener, null);
    }

    public String currentDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

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
        //datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public static void getTimePickerDialogTextView(Context context, final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
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


    public void adapterSearchView(android.support.v7.widget.SearchView searchView, final String arguments, final String api, final String jsonObject) {
        android.support.v7.widget.SearchView.SearchAutoComplete searchAutoComplete = (android.support.v7.widget.SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownBackgroundResource(R.drawable.bg_radius_white);
        searchAutoComplete.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put(arguments, bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(api), args));

                for (int i = 0; i < result.get("data").size(); i++) {
                    return result.get("data");
                }

                return result.get("search");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }

                String search = null;

                if (getItem(position).get("NAMA_LAIN").asString().equalsIgnoreCase("")) {
                    search = getItem(position).get(jsonObject).asString();
                } else {
                    search = getItem(position).get(jsonObject).asString() + " ( " + getItem(position).get("NAMA_LAIN").asString() + " ) ";
                }

                findView(convertView, R.id.title, TextView.class).setText(search);
                //findView(convertView, R.id.tv_find_cari_namaPart, TextView.class).setText(search);
                //findView(convertView, R.id.tv_find_cari_namaPart, TextView.class).setText();

                return convertView;
            }
        });

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(i)));
                find(android.support.v7.appcompat.R.id.search_src_text, android.support.v7.widget.SearchView.SearchAutoComplete.class).setText(n.get(jsonObject).asString());
                find(android.support.v7.appcompat.R.id.search_src_text, android.support.v7.widget.SearchView.SearchAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(i)));
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
                for (int i = 0; i < result.get("data").size(); i++) {
                    if (result.get("data").get(i).get(jsonObject[0]).asString().equalsIgnoreCase(bookTitle)) {
                        return result.get("data").get(i).get(jsonObject[0]);
                    } else {
                        return result.get("data");
                    }
                }
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

    public void setMultiSelectionSpinnerFromApi(final MultiSelectionSpinner spinner, final String params, final String arguments, final String api,  final MultiSelectionSpinner.OnMultipleItemsSelectedListener listener, final String... jsonObject) {
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
                str.removeAll(Arrays.asList("", null));
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                try {
                    spinner.setItems(newStr);
                    spinner.setSelection(newStr, false);
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
                str.add("Belum Di Pilih");
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

    public void setSpinnerFromApi(final Spinner spinner, final String params, final String arguments, final String api, final String... jsonObject) {
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
                str.add("Belum Di Pilih");
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get(jsonObject[0]).asString() + " - " + result.get("data").get(i).get(jsonObject[1]).asString());
                }
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                notifyDataSetChanged(spinner);
            }
        });
    }

    private EditText getAllEditText(ViewGroup v) {
        EditText invalid = null;
        for (int i = 0; i < v.getChildCount(); i++) {
            Object child = v.getChildAt(i);
            if (child instanceof EditText) {
                EditText e = (EditText) child;
                if (e.getText().length() == 0) {    // Whatever logic here to determine if valid.

                    return e;   // Stops at first invalid one. But you could add this to a list.
                }
            } else if (child instanceof ViewGroup) {
                invalid = getAllEditText((ViewGroup) child);  // Recursive call.
                if (invalid != null) {
                    break;
                }
            }
        }
        return invalid;
    }

    /*public boolean validateFields(ViewGroup viewGroup) {
        EditText emptyText = getAllEditText(viewGroup);
        if (emptyText != null) {
            emptyText.setError("Harus di isi");
            emptyText.requestFocus();
        }
        return emptyText != null;
    }*/

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
}