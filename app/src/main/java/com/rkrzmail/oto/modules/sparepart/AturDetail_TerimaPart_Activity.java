package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_TERIMA_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.ALL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;

public class AturDetail_TerimaPart_Activity extends AppActivity implements View.OnFocusChangeListener {

    private static final String TAG = "DetailPart__";
    private static final String TAMBAH_PART = "TAMBAH";
    private Spinner spinnerLokasiSimpan;
    private EditText txtNoPart, txtNamaPart, txtJumlah, txtHargaBeliUnit, etDiscRp, etDiscPercent, etHargaBersih, etPenempatan;
    private RecyclerView rvTerimaPart;
    private Nson partAvailableList = Nson.newArray();
    private String scanResult, penempatan, lokasiPart = "", merkPart = "";
    private int jumlahAllPart = 0, count = 0, partId;
    private boolean isDelete = false;
    private String kodeFolder = "";

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
        txtHargaBeliUnit = findViewById(R.id.et_hargaBeli_detailTerimaPart);
        spinnerLokasiSimpan = findViewById(R.id.sp_lokasiSimpan_terimaPart);
        rvTerimaPart = findViewById(R.id.recyclerView_terimaPart);
        etDiscRp = findViewById(R.id.et_discRp_terimaPart);
        etDiscPercent = findViewById(R.id.et_discPercent_terimaPart);
        etHargaBersih = findViewById(R.id.et_hargaBersih_detailTerimaPart);
        etPenempatan = findViewById(R.id.et_penempatan_detailTerimaPart);

        Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasi, LinearLayout.class), false);
        componentValidation();

        find(R.id.img_scan_terimaPart, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan_terimaPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nListArray == null) {
                    showWarning("Part Tidak Boleh Kosong");
                    return;
                }
                insertdata();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_terimaPart);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtJumlah.getText().toString().isEmpty() || txtHargaBeliUnit.getText().toString().isEmpty()) {
                    showWarning("INFORMASI TIDAK LENGKAP");
                    txtJumlah.requestFocus();
                    txtHargaBeliUnit.requestFocus();
                    return;
                }
                int jumlah = Integer.parseInt(txtJumlah.getText().toString());
                if (jumlah > 0) {
                    jumlahAllPart += jumlah;
                }
                Log.d(TAG, "total_part : " + jumlahAllPart);
                addData();
            }
        });
    }

    private void componentValidation() {
        etHargaBersih.addTextChangedListener(new RupiahFormat(etHargaBersih));
        txtHargaBeliUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String hargaBeli = txtHargaBeliUnit.getText().toString();
                hargaBeli = hargaBeli.replace("Rp", "").replaceAll("\\W", "");
                try {
                    if (!hargaBeli.equals("")) {
                        int totalHargaBeli = Integer.parseInt(txtJumlah.getText().toString()) * Integer.parseInt(hargaBeli);
                        etHargaBersih.setText(String.valueOf(totalHargaBeli));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (txtHargaBeliUnit == null) return;
                String strings = s.toString();
                if (strings.isEmpty()) return;
                txtHargaBeliUnit.removeTextChangedListener(this);
                try {
                    String cleanString = strings.replaceAll("[^0-9]", "");
                    String formatted = Tools.formatRupiah(cleanString);
                    txtHargaBeliUnit.setText(formatted);
                    txtHargaBeliUnit.setSelection(formatted.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtHargaBeliUnit.addTextChangedListener(this);
            }
        });
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
                        int disc = Integer.parseInt(txtJumlah.getText().toString()) * Integer.parseInt(diskon);
                        int totalHargaBeli = Integer.parseInt(txtJumlah.getText().toString()) * Integer.parseInt(hargaBeli);
                        int finall = totalHargaBeli - disc;
                        etHargaBersih.setText(String.valueOf(finall));
                    }
                } catch (Exception e) {
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
                        int finall = totalHargaBeli - disc;
                        etHargaBersih.setText(String.valueOf(finall));
                    }
                } catch (Exception e) {
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
                String percentNumber = format.format(Tools.convertToDoublePercentage(etDiscPercent.getText().toString()) / 1000);

                etDiscPercent.setText(percentNumber);
                etDiscPercent.setSelection(percentNumber.length() - 1);
                etDiscPercent.addTextChangedListener(this);
            }
        });
        etDiscRp.setOnFocusChangeListener(this);
        etDiscPercent.setOnFocusChangeListener(this);
    }

    private void insertdata() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Nson nson = Nson.readJson(getIntentStringExtra("detail"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("nodo", nson.get("nodo").asString());
                args.put("tipe", nson.get("tipe").asString());
                args.put("namaSupplier", nson.get("namaSupplier").asString());
                args.put("noSupplier", nson.get("noSupplier").asString());
                args.put("tglpesan", nson.get("tglpesan").asString());
                args.put("tglterima", nson.get("tglterima").asString());
                args.put("pembayaran", nson.get("pembayaran").asString());
                args.put("jatuhtempo", nson.get("jatuhtempo").asString());
                args.put("ongkir", nson.get("ongkir").asString().replaceAll("[^0-9]+", ""));
                //recyleview(array) to json
                args.put("parts", nListArray.toJson());
                //args.put("user", getSetting("NAMA_USER"));
                args.put("rekening", nson.get("rekening").asString());
                args.put("penempatan", etPenempatan.getText().toString());
                args.put("jumlahall", String.valueOf(jumlahAllPart));

                Log.d(TAG, "PART :  " + nListArray);
                Log.d(TAG, "send data : " + args);
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_TERIMA_PART), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menambahkan Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menambahkan Aktifitas");
                }
                Log.d(TAG, "send data : " + data.get("message"));
            }
        });
    }

    private void addData() {
        Nson dataAdd = Nson.newObject();
        dataAdd.set("NO_PART", txtNoPart.getText().toString());
        dataAdd.set("NAMA_PART", txtNamaPart.getText().toString());
        dataAdd.set("JUMLAH", txtJumlah.getText().toString());
        dataAdd.set("HARGA_BELI", txtHargaBeliUnit.getText().toString());
        dataAdd.set("NET", etHargaBersih.getText().toString());
        dataAdd.set("PART_ID", partId);
        dataAdd.set("KODE", kodeFolder);
        dataAdd.set("MERK", merkPart);
        dataAdd.set("LOKASI_SIMPAN", spinnerLokasiSimpan.getSelectedItem().toString());

        if (etDiscPercent.isEnabled()) {
            dataAdd.set("DISCOUNT", etDiscPercent.getText().toString());
        } else if (etDiscRp.isEnabled()) {
            dataAdd.set("DISCOUNT", etDiscRp.getText().toString());
        }

        initRecylerView();
        nListArray.add(dataAdd);
        rvTerimaPart.getAdapter().notifyDataSetChanged();
        Tools.clearForm(find(R.id.ly_detailPart, LinearLayout.class));
        showInfo("Part Di tambahkan");
        count++;
    }

    private void initRecylerView() {
        rvTerimaPart.setLayoutManager(new LinearLayoutManager(this));
        rvTerimaPart.setHasFixedSize(true);
        rvTerimaPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_detail_terima_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_noPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_namaPart_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_jumlah_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.tv_lokasi_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("LOKASI_SIMPAN").asString());
                viewHolder.find(R.id.tv_folder_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("KODE").asString());
                viewHolder.find(R.id.tv_merk_detailTerimaPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());

                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nListArray.asArray().remove(position);
                        notifyItemRemoved(position);
                    }
                });
            }
        });
    }

    public void getDataBarcode(final String nopart) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            int jumlahScan = 0;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "BARCODE");
                args.put("nopart", nopart);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    if (result.size() == 0) {
                        showError("Part Tidak Tersedia Di Bengkel", Toast.LENGTH_LONG);
                    }else{
                        jumlahScan++;
                        txtJumlah.setText("" + jumlahScan);
                        txtNoPart.setText(result.get("NO_PART").asString());
                        txtNamaPart.setText(result.get("NAMA_PART").asString());
                        etPenempatan.setText(penempatan);
                        lokasiPart = result.get("LOKASI").asString();
                        penempatan = result.get("PENEMPATAN").asString();
                        kodeFolder = result.get("KODE").asString();
                        merkPart = result.get("MERK").asString();
                        setSpinnerLokasiSimpan(lokasiPart);
                    }
                } else {
                    showError("Scan Barcode Tidak Valid");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent i = new Intent(this, CariPart_Activity.class);
            i.putExtra(CARI_PART_LOKASI, ALL);
            startActivityForResult(i, REQUEST_CARI_PART);
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_discPercent_terimaPart:
                etDiscRp.setEnabled(!hasFocus);
                break;
            case R.id.et_discRp_terimaPart:
                etDiscPercent.setEnabled(!hasFocus);
                break;
        }
    }

    private void setSpinnerLokasiSimpan(String lokasi) {
        List<String> lokasiList = new ArrayList<>();
        lokasiList.add("*");
        lokasiList.add("RUANG PART");
        lokasiList.add("GUDANG");
        lokasiList.add("DISPLAY");
        if (lokasi.equals("*") || lokasi.equals("")) {
            spinnerLokasiSimpan.setEnabled(false);
        } else {
            spinnerLokasiSimpan.setEnabled(true);
            lokasiList.remove("*");
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLokasiSimpan.setAdapter(spinnerAdapter);
        if (!lokasi.equals("")) {
            for (int i = 0; i < spinnerLokasiSimpan.getCount(); i++) {
                if (spinnerLokasiSimpan.getItemAtPosition(i).equals(lokasi)) {
                    spinnerLokasiSimpan.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CARI_PART && resultCode == RESULT_OK) {
            Nson n = Nson.readJson(getIntentStringExtra(data, "part"));
            partId = n.get("PART_ID").asInteger();
            lokasiPart = n.get("LOKASI").asString();
            penempatan = n.get("PENEMPATAN").asString();
            kodeFolder = n.get("KODE").asString();
            merkPart = n.get("MERK").asString();
            txtNoPart.setText(n.get("NO_PART").asString());
            txtNamaPart.setText(n.get("NAMA_PART").asString());
            setSpinnerLokasiSimpan(lokasiPart);
            etPenempatan.setText(penempatan);
        } else if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            try{
                getDataBarcode(data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "");
                showSuccess(data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
