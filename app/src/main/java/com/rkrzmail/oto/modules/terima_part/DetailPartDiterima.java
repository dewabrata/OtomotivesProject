package com.rkrzmail.oto.modules.terima_part;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.modules.lokasi_part.CariPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DetailPartDiterima extends AppActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "DetailPartDiterima";
    final int REQUEST_BARCODE = 13;
    private static final String TAMBAH_PART = "TAMBAH";
    private Spinner spinnerLokasiSimpan, spinnerPenempatan;
    private EditText txtNoPart, txtNamaPart, txtJumlah, txtHargaBeliUnit, txtDiskonBeli;
    private RecyclerView rvTerimaPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_part_diterima);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DETAIL PART DITERIMA");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent() {

        txtNoPart = findViewById(R.id.txtNoPart);
        txtNamaPart = findViewById(R.id.et_namaPart_terimaPart);
        txtJumlah = findViewById(R.id.tv_jumlah_terimaPart);
        txtHargaBeliUnit = findViewById(R.id.txtHargaBeliUnit);
        txtDiskonBeli = findViewById(R.id.txtDiskonBeli);
        spinnerLokasiSimpan = findViewById(R.id.spinnerLokasiSimpan);
        spinnerPenempatan = findViewById(R.id.spinnerPenempatan);
        rvTerimaPart = findViewById(R.id.recyclerView_detailTerimaPart);

        initRecylerView();

        spinnerLokasiSimpan.setOnItemSelectedListener(this);
        spinnerPenempatan.setOnItemSelectedListener(this);

        txtHargaBeliUnit.addTextChangedListener(new RupiahFormat(txtHargaBeliUnit));
        txtDiskonBeli.addTextChangedListener(new RupiahFormat(txtDiskonBeli));

        find(R.id.btn_scan_terimaPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(intent, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan_terimaPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertdata();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_detailpart_terima);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // addData();
                //buka cari part

                //scan barcode

            }
        });
    }


    private void insertdata() {
        //send to servevr
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Nson nson = Nson.readNson(getIntentStringExtra("detail"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String jumlah = txtJumlah.getText().toString();
                String hargabeliunit = txtHargaBeliUnit.getText().toString();
                String diskonbeli = txtDiskonBeli.getText().toString();
                diskonbeli = diskonbeli.trim().replace(" %", "");
                String lokasisimpan = spinnerLokasiSimpan.getSelectedItem().toString();
                String penempatan = spinnerPenempatan.getSelectedItem().toString();

//                int hargaSemuaUnit = Integer.parseInt(jumlah) * Integer.parseInt(hargabeliunit);
//                int diskonPerUnit = Integer.parseInt(hargabeliunit) - ((Integer.parseInt(hargabeliunit) * Integer.parseInt(diskonbeli)) / 100);

                //tipe,nama, nodo, tglpesan, tglterima, pembayaran, jatuhtempo, ongkir, namapart,
                // nopart, jumlah, hargabeli, diskon, lokasi, penempatan

                String spJumlah = getSetting("jumlah");
                String spBeli = getSetting("hargabeli");
                String spDiskon = getSetting("diskonbeli");
                String spLokasi = getSetting("lokasisimpan");
                String spTempat = getSetting("penempatan");

                if (find(R.id.fab_tambah_detailpart_terima, FloatingActionButton.class).callOnClick()) {
                    args.put("jumlah", getSetting("jumlah"));
                    args.put("hargabeliunit", getSetting("hargabeli"));
                    args.put("diskonbeli", getSetting("diskonbeli"));
                    args.put("lokasi", getSetting("lokasisimpan"));
                    args.put("penempatan", getSetting("penempatan"));
                    Log.d("total", spJumlah);
                }else{
                    args.put("namapart", "");
                    args.put("nopart", "");
                    args.put("jumlah", jumlah);
                    args.put("hargabeliunit", hargabeliunit);
                    args.put("diskonbeli", diskonbeli);
                    args.put("lokasi", lokasisimpan);
                    args.put("penempatan", penempatan);
                }

                args.put("nodo", nson.get("nodo").asString());
                args.put("tipe", nson.get("tipe").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("tglpesan", nson.get("tglpesan").asString());
                args.put("tglterima", nson.get("tglterima").asString());
                args.put("pembayaran", nson.get("pembayaran").asString());
                args.put("jatuhtempo", nson.get("jatuhtempo").asString());
                args.put("ongkir", nson.get("ongkir").asString());


                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(DetailPartDiterima.this, TerimaPart.class));
                    finish();
                } else {
                    showError("Gagal Menambahkan Aktifitas");
                }
            }
        });
    }

    private void addData(Nson rowPart, int jumalh) {
        Nson dataAdd =  Nson.newObject();
        for (int i = 0; i < nListArray.size(); i++) {
            if (nListArray.get(i).get("idpart").asString().equalsIgnoreCase(rowPart.get("PART_ID").asString() )){
                int jumlahbaru = nListArray.get(i).get("jumlah").asInteger()+jumalh;
                nListArray.get(i).set("jumlah", jumlahbaru);
                rvTerimaPart.getAdapter().notifyDataSetChanged();
                return;
            }
        }
        dataAdd.set("nopart", rowPart.get("NO_PART").asString());
        dataAdd.set("idpart", rowPart.get("PART_ID").asString());
        dataAdd.set("namapart", rowPart.get("NAMA_PART").asString());
        dataAdd.set("jumlah", jumalh);
        nListArray.add(dataAdd);
        rvTerimaPart.getAdapter().notifyDataSetChanged();

        Tools.clearForm(find(R.id.ly_detailPart, LinearLayout.class));
    }

    private void initRecylerView() {
        rvTerimaPart.setLayoutManager(new LinearLayoutManager(this));
        rvTerimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_detail_terima_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_noPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("nopart").asString());
                viewHolder.find(R.id.tv_namaPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("namapart").asString());
                viewHolder.find(R.id.tv_jumlah_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("jumlah").asString());
                // viewHolder.find(R.id.tv_pembayaran_detailTerimaPart, TextView.class).setText(nListArray.get(position).get().asString());
                viewHolder.find(R.id.tv_harga_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("harga").asString());
            }
        });

    }

    public void barcode() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("barcode", "");
                args.put("flag","NOPART");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")){
                    if (result.get("data").size()>=1){
                        addData(result.get("data"), 1);
                    }else{
                        //tidak ditemukan
                        showError("tidak ditemukan");
                    }
                }else{
                    //error
                    showError(result.get("message").asString());
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AturTerimaPart.REQUEST_DETAIL_PART && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
            addData(Nson.readJson(data.getStringExtra("row")), 1);
        } else if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            String barCode = getIntentStringExtra(data, "TEXT");
            barcode();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (view.getId()) {
            case R.id.spinnerLokasiSimpan:

                break;
            case R.id.spinnerPenempatan:

                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    android.support.v7.widget.SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new android.support.v7.widget.SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Part"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "search", "caripart", "NAMA");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_searchPart) {
            Intent i = new Intent(this, CariPart_Activity.class);
            i.putExtra("data", "data");
            startActivityForResult(i, RESULT_OK);
        }
        return super.onOptionsItemSelected(item);
    }

}
