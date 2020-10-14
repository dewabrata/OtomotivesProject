package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_DISC_SPOT;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.EDIT;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturSpotDiscount_Activity extends AppActivity {

    private EditText etTotalBiaya, etDisc, etNet, etSpot, etTotalFinal;
    private SearchView mSearchView;
    private TextView tvNamaPelanggan;

    private String idCheckin = "", idDiscSpot = "";
    private boolean isAdd = false, isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_spot_discount);
        initToolbar();
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_disc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Spot Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDisc = findViewById(R.id.et_discLain_disc);
        etNet = findViewById(R.id.et_netTransaksi_disc);
        etTotalFinal = findViewById(R.id.et_total_disc);
        etTotalBiaya = findViewById(R.id.et_total_biaya);
        etSpot = findViewById(R.id.et_spotDiscount_disc);
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan);
        initData();
        initListener();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (getIntent().hasExtra(ADD)) {
            idCheckin = data.get(ID).asString();
            isAdd = true;
        } else if (getIntent().hasExtra(EDIT)) {
            isEdit = true;
            idDiscSpot = data.get(ID).asString();
        }
        tvNamaPelanggan.setText(data.get("NAMA_PELANGGAN").asString());
        etTotalBiaya.setText(RP + formatRp(data.get("TOTAL_BIAYA").asString()));
        etDisc.setText(data.get("").asString());
        etNet.setText(data.get("").asString());
    }

    private void initListener() {
        etSpot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String hargaBeli = etTotalBiaya.getText().toString();
                hargaBeli = hargaBeli.replace("Rp", "").replaceAll("\\W", "");
                String diskon = s.toString();
                diskon = diskon.replace("%", "").replaceAll(",", ".");
                try {
                    if (!hargaBeli.equals("") && !diskon.equals("")) {
                        int disc = (int) ((Double.parseDouble(diskon) * Integer.parseInt(hargaBeli)) / 10);
                        int finall = Integer.parseInt(hargaBeli) - disc;
                        etTotalFinal.setText(RP + formatRp(String.valueOf(finall)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etSpot.removeTextChangedListener(this);
                if (etSpot == null) return;
                etSpot.removeTextChangedListener(this);

                NumberFormat format = NumberFormat.getPercentInstance(new Locale("in", "ID"));
                format.setMinimumFractionDigits(1);
                String percentNumber = format.format(Tools.convertToDoublePercentage(etSpot.getText().toString()) / 1000);

                etSpot.setText(percentNumber);
                etSpot.setSelection(percentNumber.length() - 1);
                etSpot.addTextChangedListener(this);
            }
        });

        find(R.id.btn_simpan_disc, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdd) {
                    saveData();
                } else if (isEdit) {
                    updateData();
                }
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //add : CID, action(add), tanggal, nama, transaksi, totaltransaksi, nettransaksi,
                //diskonlain, diskonspot, user

                args.put("action", "add");
                args.put("idCheckin", idCheckin);
                args.put("tanggal", currentDateTime());
                args.put("nama", tvNamaPelanggan.getText().toString());
                args.put("transaksi", formatOnlyNumber(etTotalBiaya.getText().toString()));
                args.put("totaltransaksi", formatOnlyNumber(etTotalFinal.getText().toString()));
                args.put("nettransaksi", formatOnlyNumber(etTotalBiaya.getText().toString()));
                args.put("diskonlain", formatOnlyNumber(etDisc.getText().toString()));
                args.put("diskonspot", formatOnlyNumber(etSpot.getText().toString()));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISC_SPOT), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menambahkan Discount Spot");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }


    private void deleteData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //                delete : CID, action(delete), id
                args.put("action", "delete");
                args.put("id", id.get("id").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISC_SPOT), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void updateData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("id", idDiscSpot);
                args.put("diskonspot", formatOnlyNumber(etSpot.getText().toString()));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISC_SPOT), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Memperbaharui Discount Spot");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari No. Ponsel Pelanggan"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "nomorponsel", "NO_PONSEL", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                //cariPart(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
}
