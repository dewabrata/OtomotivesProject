package com.rkrzmail.oto.modules.sparepart.terima_part;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DetailPartDiterima extends AppActivity implements View.OnFocusChangeListener {

    private static final String TAG = "DetailPartDiterima";
    private static final int REQUEST_CARI_PART = 12;
    final int REQUEST_BARCODE = 13;
    private static final String TAMBAH_PART = "TAMBAH";
    private Spinner spinnerLokasiSimpan, spinnerPenempatan;
    private EditText txtNoPart, txtNamaPart, txtJumlah, txtHargaBeliUnit, etDiscRp, etDiscPercent, etHargaBersih;
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
        getSupportActionBar().setTitle("Detail Part Di Terima");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        txtNoPart = findViewById(R.id.et_noPart_terimaPart);
        txtNamaPart = findViewById(R.id.et_namaPart_terimaPart);
        txtJumlah = findViewById(R.id.et_jumlah_terimaPart);
        txtHargaBeliUnit = findViewById(R.id.et_hargaBeli_detailTerimaPart);
        spinnerLokasiSimpan = findViewById(R.id.sp_lokasiSimpan_terimaPart);
        spinnerPenempatan = findViewById(R.id.sp_Penempatan_terimaPart);
        rvTerimaPart = findViewById(R.id.recyclerView_terimaPart);
        etDiscRp = findViewById(R.id.et_discRp_terimaPart);
        etDiscPercent = findViewById(R.id.et_discPercent_terimaPart);
        etHargaBersih = findViewById(R.id.et_hargaBersih_detailTerimaPart);

        componentValidation();

        find(R.id.img_scan_terimaPart, ImageView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), BarcodeActivity.class), REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan_terimaPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertdata();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_terimaPart);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               addData();
            }
        });
    }

    private void componentValidation(){
        //        spinnerLokasiSimpan.setOnItemSelectedListener(this);
//        spinnerPenempatan.setOnItemSelectedListener(this);
        etHargaBersih.addTextChangedListener(new RupiahFormat(etHargaBersih));
        txtHargaBeliUnit.addTextChangedListener(new RupiahFormat(txtHargaBeliUnit));

        etDiscRp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String hargaBeli = txtHargaBeliUnit.getText().toString();
                hargaBeli = hargaBeli.replace("Rp", "").replaceAll("\\W", "");
                String diskon = s.toString();
                diskon = diskon.replace("Rp", "").replaceAll("\\W", "");
                try {
                    if (!hargaBeli.equals("") && !diskon.equals("")) {
                        int totalHargaBeli = Integer.parseInt(txtJumlah.getText().toString()) * Integer.parseInt(hargaBeli);
                        int finall = totalHargaBeli - Integer.parseInt(diskon);
                        etHargaBersih.setText(String.valueOf(finall));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etDiscRp == null) return;
                String s = editable.toString();
                if (s.isEmpty()) return;
                etDiscRp.removeTextChangedListener(this);
                try {
                    String cleanString = s.replaceAll("[^0-9]", "");
                    String formatted = Tools.formatRupiah(cleanString);
                    etDiscRp.setText(formatted);
                    etDiscRp.setSelection(formatted.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etDiscRp.addTextChangedListener(this);
            }
        });

        etDiscPercent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String hargaBeli = txtHargaBeliUnit.getText().toString();
                hargaBeli = hargaBeli.replace("Rp", "").replaceAll("\\W", "");
                String diskon = s.toString();
                diskon = diskon.replace("%", "").replaceAll(",", ".");
                try {
                    if (!hargaBeli.equals("") && !diskon.equals("")) {
                        int totalHargaBeli = Integer.parseInt(txtJumlah.getText().toString()) * Integer.parseInt(hargaBeli);
                        int disc = (int) ((Double.parseDouble(diskon) * totalHargaBeli) / 10);
                        Log.d("hargaharga", String.valueOf(disc));
                        int finall =  totalHargaBeli - disc;
                        etHargaBersih.setText(String.valueOf(finall));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etDiscPercent.removeTextChangedListener(this);
                if (etDiscPercent == null) return;
                etDiscPercent.removeTextChangedListener(this);

                NumberFormat format = NumberFormat.getPercentInstance(new Locale("in", "ID"));
                format.setMinimumFractionDigits(1);
                String percentNumber = format.format(Tools.convertToDoublePercentage(etDiscPercent.getText().toString())/1000);

                etDiscPercent.setText(percentNumber);
                etDiscPercent.setSelection(percentNumber.length() -1 );
                etDiscPercent.addTextChangedListener(this);
            }
        });

        etDiscRp.setOnFocusChangeListener(this);
        etDiscPercent.setOnFocusChangeListener(this);
    }

    private void insertdata() {
        //send to servevr
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;
            @Override
            public void run() {
                Nson nson = Nson.readNson(getIntentStringExtra("detail"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                //tipe,nama, nodo, tglpesan, tglterima, pembayaran, jatuhtempo, ongkir, namapart,
                // nopart, jumlah, hargabeli, diskon, lokasi, penempatan

                args.put("nodo", nson.get("nodo").asString());
                args.put("tipe", nson.get("tipe").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("tglpesan", nson.get("tglpesan").asString());
                args.put("tglterima", nson.get("tglterima").asString());
                args.put("pembayaran", nson.get("pembayaran").asString());
                args.put("jatuhtempo", nson.get("jatuhtempo").asString());
                args.put("ongkir", nson.get("ongkir").asString());
                //recyleview(array) to json
                args.put("parts", nListArray.toJson());

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

    private void addData() {
        Nson dataAdd =  Nson.newObject();

        dataAdd.set("NO_PART", txtNoPart.getText().toString());
        dataAdd.set("NAMA", txtNamaPart.getText().toString());
        dataAdd.set("JUMLAH", txtJumlah.getText().toString());
        dataAdd.set("HARGA_BELI", txtHargaBeliUnit.getText().toString());
        dataAdd.set("NET", etHargaBersih.getText().toString());
        if(etDiscPercent.isEnabled()){
            dataAdd.set("DISCOUNT", etDiscPercent.getText().toString());
        }else if(etDiscRp.isEnabled()){
            dataAdd.set("DISCOUNT", etDiscRp.getText().toString());
        }
        dataAdd.set("LOKASI_SIMPAN", spinnerLokasiSimpan.getSelectedItem().toString());

        initRecylerView();
        nListArray.add(dataAdd);
        rvTerimaPart.getAdapter().notifyDataSetChanged();

        Tools.clearForm(find(R.id.ly_detailPart, LinearLayout.class));
    }

    private void initRecylerView() {
        rvTerimaPart.setLayoutManager(new LinearLayoutManager(this));
        rvTerimaPart.setHasFixedSize(true);
        rvTerimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_detail_terima_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_noPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_namaPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_jumlah_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.tv_net_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NET").asString());
                viewHolder.find(R.id.tv_harga_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("HARGA_BELI").asString());
                viewHolder.find(R.id.tv_disc_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("DISCOUNT").asString());
                viewHolder.find(R.id.tv_lokasi_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("LOKASI_SIMPAN").asString());
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
                        //addData(result.get("data"), 1);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_searchPart) {
            Intent i = new Intent(this, CariPart_Activity.class);
            i.putExtra("flag", "ALL");
            startActivityForResult(i, REQUEST_CARI_PART);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()){
            case R.id.et_discPercent_terimaPart:
                if(hasFocus){
                    etDiscRp.setEnabled(false);
                }else{
                    etDiscRp.setEnabled(true);
                }
                break;
            case R.id.et_discRp_terimaPart:
                if(hasFocus){
                    etDiscPercent.setEnabled(false);
                }else{
                    etDiscPercent.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CARI_PART && resultCode == RESULT_OK) {
            Nson n = Nson.readJson(getIntentStringExtra(data, "part"));
            txtNoPart.setText(n.get("NO_PART").asString());
            txtNamaPart.setText(n.get("NAMA").asString());
            // addData(Nson.readJson(data.getStringExtra("row")), 1);
        } else if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            String barCode = getIntentStringExtra(data, "TEXT");
            barcode();
        }
    }
}
