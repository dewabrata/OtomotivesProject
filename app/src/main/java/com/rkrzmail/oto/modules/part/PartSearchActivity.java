package com.rkrzmail.oto.modules.part;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MainActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;
import com.rkrzmail.utils.ViewAnimation;

import java.util.Map;

public class PartSearchActivity extends AppActivity {

    private Toolbar toolbar;
    private EditText et_search;
    private ImageButton bt_clear;

    private ProgressBar progress_bar;
    private LinearLayout lyt_no_result;

    private RecyclerView recyclerView;
    private AdapterSuggestionSearch mAdapterSuggestion;
    private LinearLayout lyt_suggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_toolbar_light);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this, R.color.grey_5);
        //Tools.setSystemBarLight(this);
        getSupportActionBar().setTitle("Part");

        //findViewById(R.id.toolbars).setVisibility(View.GONE);
    }

    private void initComponent() {
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        lyt_no_result = (LinearLayout) findViewById(R.id.lyt_no_result);

        lyt_suggestion = (LinearLayout) findViewById(R.id.lyt_suggestion);
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.addTextChangedListener(textWatcher);

        bt_clear = (ImageButton) findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerSuggestion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter suggestion
        mAdapterSuggestion = new AdapterSuggestionSearch(this);
        recyclerView.setAdapter(mAdapterSuggestion);
        showSuggestionSearch();
        mAdapterSuggestion.setOnItemClickListener(new AdapterSuggestionSearch.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                et_search.setText(viewModel);
                ViewAnimation.collapse(lyt_suggestion);
                hideKeyboard();
                searchAction(viewModel);
            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });
        findViewById(R.id.bt_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getActivity(), PartFavoriteActivity.class);

                startActivity(intent);
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction("");
                    return true;
                }
                return false;
            }
        });

        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSuggestionSearch();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });
    }

    private void showSuggestionSearch() {
        mAdapterSuggestion.refreshItems();
        //ViewAnimation.expand(lyt_suggestion);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchAction(final String cari) {
        progress_bar.setVisibility(View.VISIBLE);
        ViewAnimation.collapse(lyt_suggestion);
        lyt_no_result.setVisibility(View.GONE);

        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress_bar.setVisibility(View.GONE);
                    lyt_no_result.setVisibility(View.VISIBLE);
                }
            }, 2000);
            mAdapterSuggestion.addSearchHistory(query);
        } else {
            Toast.makeText(this, "Please fill search input", Toast.LENGTH_SHORT).show();
        }


        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("search", cari);
                //  args.put("email", UtilityAndroid.getSetting(getApplicationContext(),"EMA",""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }

                progress_bar.setVisibility(View.GONE);
                lyt_no_result.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
            finish();
        }else  if (item.getItemId() == R.id.action_search) {
             findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
             toolbar.setVisibility(View.GONE);
         } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_part, menu);
        //menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }


}
