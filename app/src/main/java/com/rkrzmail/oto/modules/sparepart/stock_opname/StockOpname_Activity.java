package com.rkrzmail.oto.modules.sparepart.stock_opname;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.modules.sparepart.lokasi_part.LokasiPart_Activity;
import com.rkrzmail.oto.modules.sparepart.lokasi_part.Penyesuain_Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class StockOpname_Activity extends AppActivity {

    private static final int REQUEST_BARCODE_STOCK_OPNAME = 12;
    private static final int REQUEST_PENYESUAIAN = 567;
    private EditText noFolder, noPart, jumlahData, jumlahAkhir, namaPart;
    private ImageView imgBarcode;
    private ArrayList<String> indexOf_Opname = new ArrayList<String>();


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

    private void initComponent(){

        noFolder = findViewById(R.id.et_noFolder_stockOpname);
        noPart = findViewById(R.id.et_noPart_stockOpname);
        jumlahData = findViewById(R.id.et_jumlahdata_stockOpname);
        jumlahAkhir = findViewById(R.id.et_jumlahakhir_stockOpname);
        namaPart = findViewById(R.id.et_namaPart_stockOpname);
        imgBarcode = findViewById(R.id.imgBarcode_stockOpname);

        loadData();

        find(R.id.btn_simpan_stockOpname, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveUpdate();
            }
        });

        imgBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE_STOCK_OPNAME);
            }
        });
    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("NO_PART_ID"));
        Intent i = getIntent();
        if (i.hasExtra("NO_PART_ID")) {
            noFolder.setText(data.get("NO_FOLDER").asString());
            noPart.setText(data.get("NO_PART_ID").asString());
            namaPart.setText(data.get("NAMA").asString());
            jumlahData.setText(data.get("STOCK").asString());
        }


    }

    private void saveUpdate(){
        final Nson data = Nson.readJson(getIntentStringExtra("NO_PART_ID"));

        final String lokasi = data.get("LOKASI").asString();
        final String tempat = data.get("PENEMPATAN").asString();
        final String palet = data.get("PALET").asString();
        final String rak = data.get("RAK").asString();
        final String folder = data.get("NO_FOLDER").asString();
        final String user = data.get("USER").asString();
        final String nopart = data.get("NO_PART_ID").asString();
        final String stock = data.get("STOCK").asString();
        final int stockAwal = Integer.parseInt(jumlahData.getText().toString());
        final int stockAkhir = Integer.parseInt(jumlahAkhir.getText().toString());

        if (stockAwal < stockAkhir || stockAwal > stockAkhir) {
            int stockbeda = stockAkhir - stockAwal;
            showInfo("Diperlukan Penyesuaian");
            setSelanjutnya(stockbeda);
        } else {
            newProses(new Messagebox.DoubleRunnable() {
                Nson result;

                @Override
                public void run() {
                    Map<String, String> args = AppApplication.getInstance().getArgsData();

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                    String tanggal = simpleDateFormat.format(calendar.getTime());

                    //  CID, action(add), lokasi, tempat, palet, rak, folder, tanggal, user, nopart, stock

                    args.put("action", "add");
                    args.put("lokasi", lokasi);
                    args.put("tempat", tempat);
                    args.put("palet", palet);
                    args.put("rak", rak);
                    args.put("folder", folder);
                    args.put("tanggal", tanggal);
                    args.put("user", user);
                    args.put("nopart", nopart);
                    args.put("stock", stock);
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlokasipart"), args));
                }

                @Override
                public void runUI() {
                    if (result.get("status").asString().equalsIgnoreCase("OK")) {
                        startActivity(new Intent(getActivity(), LokasiPart_Activity.class));
                    } else {
                        showInfo("Gagal Opname Part");
                    }
                }
            });
        }

    }

    private void setSelanjutnya(final int stockBeda) {
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
            }

            @Override
            public void runUI() {
                final Nson data = Nson.readJson(getIntentStringExtra("NO_PART_ID"));
                Nson nson = Nson.newObject();

                // CID, action(update), lokasi, tempat, palet, rak, folder, tanggal, user, nopart, stock,
                // folderlain, stockbeda, sebab, alasan
                nson.set("folder", data.get("NO_FOLDER"));
                nson.set("nopart", data.get("NO_PART_ID"));
                nson.set("stock", data.get("STOCK"));
                nson.set("lokasi", data.get("LOKASI"));
                nson.set("user", data.get("USER"));
                nson.set("tempat", data.get("PENEMPATAN"));
                nson.set("palet", data.get("PALET"));
                nson.set("rak", data.get("RAK"));
                nson.set("stockbeda", stockBeda);

                Intent i = new Intent(getActivity(), Penyesuain_Activity.class);
                i.putExtra("penyesuaian", nson.toJson());
                startActivityForResult(i, REQUEST_PENYESUAIAN);

            }
        });
    }

    private void getBarcode(){
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {

                setResult(RESULT_OK);
                finish();
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
        if(requestCode == REQUEST_BARCODE_STOCK_OPNAME && resultCode == RESULT_OK){
            String barcode = getIntentStringExtra(data, "TEXT");
            getBarcode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
