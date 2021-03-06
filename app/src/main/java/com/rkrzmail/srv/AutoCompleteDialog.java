package com.rkrzmail.srv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;

public class AutoCompleteDialog {
    public interface OnItemClick {
        public void onClick(String item, int position);
    }

    ArrayList<String> items;
    Activity context;
    String dTitle, closeTitle = "Close";
    OnItemClick onItemClick;
    AlertDialog alertDialog;
    int pos;
    int style;
    boolean cancellable = false;
    boolean showKeyboard = false;

    public AutoCompleteDialog(Activity activity, ArrayList<String> items, String dialogTitle) {
        this.items = items;
        this.context = activity;
        this.dTitle = dialogTitle;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void showAutoCompleteDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        View v = context.getLayoutInflater().inflate(R.layout.dialog_autocomplete, null);
        TextView tvClose = (TextView) v.findViewById(R.id.close);
        TextView title = (TextView) v.findViewById(R.id.spinerTitle);
        final ListView listView = (ListView) v.findViewById(R.id.list);
        final EditText searchBox = v.findViewById(R.id.searchBox);
        final TextView tvOkay = v.findViewById(R.id.ok);
        final ProgressBar progressBar = v.findViewById(R.id.progress_bar);

        searchBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        title.setText(dTitle);
        if (isShowKeyboard()) {
            showKeyboard(searchBox);
        }

        adb.setView(v);
        alertDialog = adb.create();
        Objects.requireNonNull(alertDialog.getWindow()).getAttributes().windowAnimations = style;//R.style.DialogAnimations_SmileWindow;

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.items_view, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView t = (TextView) view.findViewById(R.id.text1);
                for (int j = 0; j < items.size(); j++) {
                    if (t.getText().toString().equalsIgnoreCase(items.get(j).toString())) {
                        pos = j;
                    }
                }
                onItemClick.onClick(t.getText().toString(), pos);
                closeDialog();
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                getNamaPrincipal(editable.toString(), progressBar, adapter);
            }
        });

        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });

        tvOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onClick(searchBox.getText().toString(), 0);
                closeDialog();
            }
        });

        alertDialog.setCancelable(isCancellable());
        alertDialog.setCanceledOnTouchOutside(isCancellable());
        alertDialog.show();
    }

    public void closeDialog() {
        hideKeyboard();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(context.getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void showKeyboard(final EditText ettext) {
        ettext.requestFocus();
        ettext.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                   keyboard.showSoftInput(ettext, 0);
                               }
                           }
                , 200);
    }

    private void getNamaPrincipal(final String text, final ProgressBar progressBar, final ArrayAdapter adapter){
        MessageMsg.newTask(context, new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "PRINCIPAL");
                args.put("nama", text);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));
            }

            @Override
            public void runUI() {
                progressBar.setVisibility(View.GONE);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    items.clear();
                    for (int i = 0; i < result.size(); i++) {
                        items.add(result.get(i).get("NAMA").asString());
                    }

                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
