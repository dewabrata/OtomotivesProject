package com.rkrzmail.oto.modules.sparepart.stock_opname;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PENYESUAIAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PENYESUAIAN;

public class StockOpname_Activity extends AppActivity {

    private EditText noFolder, noPart, etJumlahOpname, namaPart, etPending, etMerk, etStock;
    private String partId = "";
    private int stockBeda = 0;
    private int counterBarcode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_opname);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stock Opname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        noFolder = findViewById(R.id.et_noFolder_stockOpname);
        noPart = findViewById(R.id.et_noPart_stockOpname);
        etJumlahOpname = findViewById(R.id.et_jumlah_opname);
        namaPart = findViewById(R.id.et_namaPart_stockOpname);
        etPending = findViewById(R.id.et_pending);
        etMerk = findViewById(R.id.et_merkPart_stockOpname);
        etStock = findViewById(R.id.et_stock);

        loadData();

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int stockPending, totalStock = 0;
                int stockAwal = Integer.parseInt(etStock.getText().toString());
                int stockAkhir = Integer.parseInt(etJumlahOpname.getText().toString());

                if (!etPending.getText().toString().isEmpty()) {
                    stockPending = Integer.parseInt(etPending.getText().toString());
                    totalStock = stockAwal + stockPending;
                } else {
                    stockPending = 0;
                }

                //stockAwal < stockAkhir ||
                if (stockAkhir < stockAwal ) {
                    stockBeda = stockAwal - stockAkhir;
                    showInfo("Diperlukan Penyesuaian");
                    Intent i = new Intent(getActivity(), Penyesuain_Activity.class);
                    i.putExtra(PENYESUAIAN, "");
                    startActivityForResult(i, REQUEST_PENYESUAIAN);
                }else if(totalStock > stockAkhir){

                }else {
                    saveData(null);
                }
            }
        });

        find(R.id.img_scan_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });
    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        noFolder.setText(data.get("KODE").asString());
        noPart.setText(data.get("NOMOR_PART_NOMOR").asString());
        namaPart.setText(data.get("NAMA_PART").asString());
        etMerk.setText(data.get("MERK").asString());
        etStock.setText(data.get("STOCK").asString());
        etPending.setText(data.get("PENDING").asString());
        setSpLokasi(data.get("LOKASI").asString());
        partId = data.get("PART_ID").asString();
    }

    private void saveData(final Nson penyesuaianNson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");

                if (penyesuaianNson != null) {
                    args.put("sebab", penyesuaianNson.get("SEBAB").asString());
                    args.put("folder_lain", penyesuaianNson.get("FOLDER_LAIN").asString());
                    args.put("alasan", penyesuaianNson.get("KET").asString());
                    args.put("user_saksi", penyesuaianNson.get("USER_SAKSI").asString());
                    args.put("stock_beda", String.valueOf(stockBeda));
                    args.put("penyesuaian", "DUMMY");
                }

                args.put("part_id", partId);
                args.put("nama_part", namaPart.getText().toString());
                args.put("no_part", noPart.getText().toString());
                args.put("merk", etMerk.getText().toString());
                args.put("lokasi", find(R.id.sp_lokasi_stockOpname, Spinner.class).getSelectedItem().toString());
                args.put("opname", etJumlahOpname.getText().toString());
                args.put("no_folder", noFolder.getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("stockopname"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    private void setSpLokasi(String lokasi) {
        List<String> lokasiList = new ArrayList<>();
        lokasiList.add("RUANG PART");
        lokasiList.add("GUDANG");
        lokasiList.add("DISPLAY");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_lokasi_stockOpname, Spinner.class).setAdapter(spinnerAdapter);
        if (!lokasi.equals("")) {
            for (int i = 0; i < find(R.id.sp_lokasi_stockOpname, Spinner.class).getCount(); i++) {
                if (find(R.id.sp_lokasi_stockOpname, Spinner.class).getItemAtPosition(i).equals(lokasi)) {
                    find(R.id.sp_lokasi_stockOpname, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
    }

    public void getDataBarcode(final String nopart) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("lokasi", "ALL");
                args.put("search", nopart);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    if (result.size() == 0) {
                        showError("Part Tidak Tersedia Di Bengkel");
                        return;
                    }
                    counterBarcode++;
                    noPart.setText(result.get("NO_PART").asString());
                    namaPart.setText(result.get("NAMA_PART").asString());
                    etMerk.setText(result.get("MERK").asString());
                    noFolder.setText(result.get("KODE").asString());
                    etStock.setText(result.get("STOCK_RUANG_PART").asString());
                    etPending.setText(result.get("PENDING").asString());
                    etJumlahOpname.setText("" + counterBarcode);
                    setSpLokasi(result.get("LOKASI").asString());
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    //-->Awal Menu Search<--//
    SearchView mSearchView;

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Part.."); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        //adapterSearchView(mSearchView, "search", "aturdisconjasalain", "KATEGORI");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                //catchData(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
    //-->Akhir Menu Search<--//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            String barcode = getIntentStringExtra(data, "TEXT");
            getDataBarcode(barcode);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_PENYESUAIAN) {
            saveData(Nson.readJson(getIntentStringExtra(data, DATA)));
        }
    }
}
