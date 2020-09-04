package com.rkrzmail.oto.modules.discount;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.RupiahFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class AturSpotDiscount_Activity extends AppActivity {

    private EditText etNoPonsel, etNama, etTransaksi, etDisc, etNet, etSpot, etTotal;
    private  SearchView mSearchView;

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

        etNama = findViewById(R.id.et_namaPelanggan_disc);
        etDisc = findViewById(R.id.et_discLain_disc);
        etNet = findViewById(R.id.et_netTransaksi_disc);
        etNoPonsel = findViewById(R.id.et_noPonsel_disc);
        etTotal = findViewById(R.id.et_total_disc);
        etTransaksi = findViewById(R.id.et_transaksi_disc);
        etSpot = findViewById(R.id.et_spotDiscount_disc);

        etSpot.addTextChangedListener(new RupiahFormat(etSpot));


        find(R.id.btn_simpan_disc, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
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
                args.put("tanggal", currentDateTime());
                args.put("nama", etNama.getText().toString());
                args.put("transaksi", etTransaksi.getText().toString());
                args.put("totaltransaksi", etTotal.getText().toString());
                args.put("nettransaksi", etTransaksi.getText().toString());
                args.put("diskonlain", etDisc.getText().toString());
                args.put("diskonspot", etSpot.getText().toString());
                args.put("user", getSetting("user"));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonspot"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(getActivity(), SpotDiscount_Activity.class));
                } else {
                    showInfo("GAGAL");
                }
            }
        });
    }


    private void deleteData(final Nson id){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //                delete : CID, action(delete), id
                args.put("action", "delete");
                args.put("id", id.get("id").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonspot"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(getActivity(), SpotDiscount_Activity.class));
                } else {
                    showInfo("GAGAL");
                }
            }
        });
    }

    private void updateData(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //                update : CID, action(update), id, diskonspot
                args.put("action", "update");
                args.put("diskonspot", etSpot.getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonspot"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(getActivity(), SpotDiscount_Activity.class));
                    finish();
                } else {
                    showInfo("GAGAL");
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

        adapterSearchView(mSearchView, "search", "nomorponsel", "NO_PONSEL");
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
