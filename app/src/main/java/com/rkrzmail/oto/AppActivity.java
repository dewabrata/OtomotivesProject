package com.rkrzmail.oto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.FileOutputStream;
import java.util.ArrayList;
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
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    public String getSelectedSpinnerText(int id) {
        View v = find(id, Spinner.class).getSelectedView();
        if (v instanceof TextView) {
            return to(v, TextView.class).getText().toString();
        }
        return "";
    }

    public void showError(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    public String getIntentStringExtra(String key) {
        return getIntentStringExtra(getIntent(), key);
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


    public void adapterSearchView(android.support.v7.widget.SearchView searchView, final String arguments, final String api, final String jsonObject) {
        android.support.v7.widget.SearchView.SearchAutoComplete searchAutoComplete = (android.support.v7.widget.SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put(arguments, bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(api), args));

                for (int i = 0; i < result.get("data").size(); i++) {
                    if (result.get("data").get(i).get(jsonObject).asString().equalsIgnoreCase(bookTitle)) {
                        return result.get(jsonObject);
                    } else {
                        return result.get("data");
                    }
                }
                return result.get("search");

            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nama_, parent, false);
                }

                String search = null;

                if (getItem(position).get("NAMA_LAIN").asString().equalsIgnoreCase("")) {
                    search = getItem(position).get(jsonObject).asString();
                } else {
                    search = getItem(position).get(jsonObject).asString() + " ( " + getItem(position).get("NAMA_LAIN").asString() + " ) ";
                }

                findView(convertView, R.id.tv_find_cari_namaPart, TextView.class).setText(search);
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

    public void remakeAutoCompleteMaster(final NikitaAutoComplete editText, final String params, final String jsonObject) {

        editText.setThreshold(2);
        editText.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", params);
                args.put("search", bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewmst"), args));
                for (int i = 0; i < result.get("data").size(); i++) {
                    if (result.get("data").get(i).get(jsonObject).asString().equalsIgnoreCase(bookTitle)) {
                        return result.get("data").get(i).get(jsonObject);
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
                findView(convertView, R.id.title2, TextView.class).setText(getItem(position).get(jsonObject).asString());
                return convertView;
            }
        });

        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(i)));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get("KOTA_KAB").asString()).append(" ");

                editText.setText(stringBuilder.toString());
                editText.setTag(String.valueOf(adapterView.getItemAtPosition(i)));
            }
        });
    }

    public void setMultiSelectionSpinnerFromApi(final MultiSelectionSpinner spinner, final String params, final String arguments, final String api, final String jsonObject, final ArrayList<String> dummies) {
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
                    // nListArray.add(result.get("data").get(i).get("NAMA"));
                    str.add(result.get("data").get(i).get(jsonObject).asString());
                }
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                if (newStr.size() > -1) {
                    spinner.setItems(newStr);
                }
                spinner.setSelection(new int[]{});
                spinner.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {
                        dummies.addAll(strings);
                    }
                });
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
                List<String> str = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    // nListArray.add(result.get("data").get(i).get("NAMA"));
                    str.add(result.get("data").get(i).get(jsonObject).asString());
                }
                if (str.size() > -1) {
                    ArrayAdapter<String> folderAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, str);
                    folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(folderAdapter);
                }
            }
        });
    }

    private EditText traverseEditTexts(ViewGroup v) {
        EditText invalid = null;
        for (int i = 0; i < v.getChildCount(); i++) {
            Object child = v.getChildAt(i);
            if (child instanceof EditText) {
                EditText e = (EditText) child;
                if (e.getText().length() == 0) {    // Whatever logic here to determine if valid.

                    return e;   // Stops at first invalid one. But you could add this to a list.
                }
            } else if (child instanceof ViewGroup) {
                invalid = traverseEditTexts((ViewGroup) child);  // Recursive call.
                if (invalid != null) {
                    break;
                }
            }
        }
        return invalid;
    }

    public boolean validateFields(ViewGroup viewGroup) {
        EditText emptyText = traverseEditTexts(viewGroup);
        if (emptyText != null) {
            showInfo("Tidak Boleh Kosong");
            emptyText.requestFocus();      // Scrolls view to this field.
        }
        return emptyText == null;
    }
}


