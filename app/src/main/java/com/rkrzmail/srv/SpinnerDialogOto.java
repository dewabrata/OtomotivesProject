package com.rkrzmail.srv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class SpinnerDialogOto {

    List<String> items;
    Activity context;
    String dTitle, closeTitle = "Close";
    OnItemClick onItemClick;
    AlertDialog alertDialog;
    int pos;
    int style;
    boolean cancellable = false;
    boolean showKeyboard = false;
    boolean isFromApi = false;
    private String apiUrl, valueSearch, flagParamSearch, paramSearch;
    private Map<String, String> params;
    private EditText searchBox;
    private GifImageView gifProgressOto;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    public SpinnerDialogOto(Activity activity, ArrayList<String> items, String dialogTitle) {
        this.items = items;
        this.context = activity;
        this.dTitle = dialogTitle;
    }

    public SpinnerDialogOto(Activity activity, ArrayList<String> items, String dialogTitle, String closeTitle) {
        this.items = items;
        this.context = activity;
        this.dTitle = dialogTitle;
        this.closeTitle = closeTitle;
    }

    public SpinnerDialogOto(Activity activity, ArrayList<String> items, String dialogTitle, int style) {
        this.items = items;
        this.context = activity;
        this.dTitle = dialogTitle;
        this.style = style;
    }

    public SpinnerDialogOto(Activity activity, ArrayList<String> items, String dialogTitle, int style, String closeTitle) {
        this.items = items;
        this.context = activity;
        this.dTitle = dialogTitle;
        this.style = style;
        this.closeTitle = closeTitle;
    }

    public void bindOnSpinerListener(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setApiUrl(String apiUrl, String valueSearch) {
        this.apiUrl = apiUrl;
        this.valueSearch = valueSearch;
        this.isFromApi = true;
    }

    public void setParamsSearch(Map<String, String> params, String flagParamSearch) {
        this.params = params;
        this.paramSearch = paramSearch;
        this.flagParamSearch = flagParamSearch;
    }

    public void showSpinerDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        View v = context.getLayoutInflater().inflate(R.layout.dialog_autocomplete, null);
        adb.setView(v);
        if (isFromApi) {
            getDataFromApi();
        }

        TextView rippleViewClose =  v.findViewById(R.id.close);
        TextView title =  v.findViewById(R.id.spinerTitle);
        TextView tvOk = v.findViewById(R.id.ok);
        listView = v.findViewById(R.id.list);
        searchBox = v.findViewById(R.id.searchBox);
        gifProgressOto = v.findViewById(R.id.gif_progress);

        rippleViewClose.setText(closeTitle);
        title.setText(dTitle);
        setGifProgress();
        showKeyboard();
        setData();

        alertDialog = adb.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations_SmileWindow;


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onItemClick.onClick(adapterView.getItemAtPosition(i).toString());
                alertDialog.dismiss();
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFromApi) {
                    getDataFromApi();
                }
                adapter.getFilter().filter(searchBox.getText().toString());
            }
        });

        rippleViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSpinerDialog();
            }
        });

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSpinerDialog();
            }
        });

        alertDialog.setCancelable(isCancellable());
        alertDialog.setCanceledOnTouchOutside(isCancellable());
        alertDialog.show();
    }

    private void setData(){
        adapter = new ArrayAdapter<String>(context, R.layout.items_view, items);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    private void getDataFromApi() {
        MessageMsg.newTask(context, new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gifProgressOto.setVisibility(View.VISIBLE);
                    }
                });
                if(searchBox != null){
                    params.put(flagParamSearch, searchBox.getText().toString());
                }
                result = Nson.readJson(InternetX.postHttpConnection(apiUrl, params));
            }

            @Override
            public void runUI() {
                gifProgressOto.setVisibility(View.GONE);
                result = result.get("data");
                if (result.size() > 0) {
                    items.clear();
                    for (int i = 0; i < result.size(); i++) {
                        items.add(result.get(i).get(valueSearch).asString());
                    }
                }
                setData();
            }
        });
    }

    private void setGifProgress(){
        try {
            GifDrawable gifDrawable = new GifDrawable(this.context.getResources().openRawResource(R.raw.speed_progress_3));
            gifProgressOto.setImageDrawable(gifDrawable);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public void closeSpinerDialog() {
        hideKeyboard();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showKeyboard() {
        searchBox.requestFocus();
        searchBox.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                   keyboard.showSoftInput(searchBox, 0);
                               }
                           }
                , 200);
    }

    private boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    private boolean isShowKeyboard() {
        return showKeyboard;
    }

    public void setShowKeyboard(boolean showKeyboard) {
        this.showKeyboard = showKeyboard;
    }

    public interface OnItemClick{
        void onClick(String item);
    }
}
