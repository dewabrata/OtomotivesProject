package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_TERIMA_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.ALL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturDetail_TerimaPart_Activity extends AppActivity implements View.OnFocusChangeListener {

    private static final String TAG = "DetailPart__";
    private static final String TAMBAH_PART = "TAMBAH";
    private Spinner spinnerLokasiSimpan;
    private EditText txtNoPart, txtNamaPart, txtJumlah, txtHargaBeliUnit, etDiscRp, etDiscPercent, etHargaBersih, etPenempatan;
    private RecyclerView rvTerimaPart;
    private AlertDialog alertDialog;

    private final Nson partAvailableList = Nson.newArray();
    private Nson data;
    private String scanResult, penempatan, lokasiPart = "", merkPart = "";
    private String kodeFolder = "";
    private String jenisPembayaran = "";
    private int jumlahAllPart = 0, count = 0, partId;
    private int lastBalance = 0;
    private long totalAll = 0;

    private boolean isDelete = false;
    private boolean isPembayaranActive = false;

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
        rvTerimaPart = findViewById(R.id.recyclerView_terimaPart);
        etDiscRp = findViewById(R.id.et_discRp_terimaPart);
        etDiscPercent = findViewById(R.id.et_discPercent_terimaPart);
        etHargaBersih = findViewById(R.id.et_hargaBersih_detailTerimaPart);
        etPenempatan = findViewById(R.id.et_penempatan_detailTerimaPart);

        data = Nson.readJson(getIntentStringExtra("detail"));
        isPembayaranActive = UtilityAndroid.getSetting(getApplicationContext(), "PEMBAYARAN_ACTIVE", "").equals("Y");
        lastBalance = data.get("BALANCE").asInteger();
        jenisPembayaran = data.get("pembayaran").asString();

        Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasi, LinearLayout.class), false);
        initListener();
        initRecylerView();

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
                    showWarning("PART TIDAK BOLEH KOSONG", Toast.LENGTH_LONG);
                    return;
                }
                if (isPembayaranActive && (jenisPembayaran.equals("TRANSFER") || jenisPembayaran.equals("CASH ON DELIVERY"))
                        && totalAll > lastBalance) {
                    showWarning("BALANCE KAS " + (jenisPembayaran.equals("CASH ON DELIVERY") ? "" : "BANK") + "TIDAK CUKUP");
                    return;
                }
                insertdata();
            }
        });

        findViewById(R.id.fab_tambah_terimaPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void dialogKonfirmasi(final Nson data) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info_terima_part, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        TextView tvNamaPart = dialogView.findViewById(R.id.tv_nama_part);
        TextView tvNoPart = dialogView.findViewById(R.id.tv_no_part);
        TextView tvHargaPart = dialogView.findViewById(R.id.tv_harga_part);
        TextView tvJumlahPart = dialogView.findViewById(R.id.tv_jumlah);
        TextView tvDiscount = dialogView.findViewById(R.id.tv_discount);
        TextView tvHargaNet = dialogView.findViewById(R.id.tv_harga_net);
        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);
        Button btnBatal = dialogView.findViewById(R.id.btn_batal);

        tvNamaPart.setText(data.get("NAMA_PART").asString());
        tvNoPart.setText(data.get("NO_PART").asString());
        tvHargaPart.setText(RP + NumberFormatUtils.formatRp(data.get("HARGA_BELI").asString()));
        tvJumlahPart.setText(data.get("JUMLAH").asString());
        tvDiscount.setText(data.get("DISCOUNT").asString().contains(",") || data.get("DISCOUNT").asString().contains(".") ?
                data.get("DISCOUNT").asString() + " %" : RP + NumberFormatUtils.formatRp(data.get("DISCOUNT").asString()));
        tvHargaNet.setText(RP + NumberFormatUtils.formatRp(data.get("NET").asString()));

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    nListArray.add(data);
                    Objects.requireNonNull(rvTerimaPart.getAdapter()).notifyDataSetChanged();
                    Tools.clearForm(find(R.id.ly_detailPart, LinearLayout.class));
                    Tools.hideKeyboard(getActivity());
                    showInfo("PART DI TAMBAHKAN");
                    count++;
                } catch (Exception e) {
                    showError("SILAHKAN HUBUNGI SUPPORT, " + e.getMessage());
                }
                alertDialog.dismiss();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });

        builder.setCancelable(false);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void initListener() {
        txtJumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                txtJumlah.removeTextChangedListener(this);
                String jumlahBeli = editable.toString();
                if (jumlahBeli.isEmpty()) return;

                String hargaBeli = formatOnlyNumber(txtHargaBeliUnit.getText().toString());
                String discPercent = etDiscPercent.getText().toString();
                String discRp = formatOnlyNumber(etDiscRp.getText().toString());

                int hargaBersih = 0;
                int allDiskon = 0;

                if (!hargaBeli.isEmpty()) {
                    hargaBersih = Integer.parseInt(jumlahBeli) * Integer.parseInt(hargaBeli);
                    if (!discPercent.isEmpty()) {
                        allDiskon = (int) ((Double.parseDouble(discPercent) * hargaBersih) / 10);
                        hargaBersih = hargaBersih - allDiskon;
                    } else if (!discRp.isEmpty()) {
                        allDiskon = Integer.parseInt(jumlahBeli) * Integer.parseInt(discRp);
                        hargaBersih = hargaBersih - allDiskon;
                    }

                    etHargaBersih.setText(RP + new NumberFormatUtils().formatRp(String.valueOf(hargaBersih)));
                }

                txtJumlah.addTextChangedListener(this);
            }
        });

        txtHargaBeliUnit.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(txtHargaBeliUnit));
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
                        etHargaBersih.setText(RP + new NumberFormatUtils().formatRp(String.valueOf(totalHargaBeli)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDiscRp.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etDiscRp));
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
                        etHargaBersih.setText(RP + new NumberFormatUtils().formatRp(String.valueOf(finall)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etDiscPercent.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDiscPercent));
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
                        int disc = (int) ((Double.parseDouble(diskon) * totalHargaBeli) / 100);
                        int finall = totalHargaBeli - disc;
                        etHargaBersih.setText(RP + new NumberFormatUtils().formatRp(String.valueOf(finall)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                args.put("parts", nListArray.toJson());
                args.put("rekening", nson.get("rekening").asString());
                args.put("penempatan", etPenempatan.getText().toString());
                args.put("jumlahall", String.valueOf(jumlahAllPart));
                args.put("totalAll", String.valueOf(totalAll));
                args.put("principal", nson.get("PRINCIPAL").asString());
                args.put("namaPerusahaan", nson.get("PERUSAHAAN").asString());
                args.put("noTrace", nson.get("NO_TRACE").asString());

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
            }
        });
    }

    private void addData() {
        if (txtJumlah.getText().toString().isEmpty() || txtHargaBeliUnit.getText().toString().isEmpty()) {
            showWarning("INFORMASI PART TIDAK LENGKAP");
            txtJumlah.requestFocus();
            txtHargaBeliUnit.requestFocus();
            return;
        }
        int jumlah = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(txtJumlah.getText().toString()));
        if (jumlah > 0) {
            jumlahAllPart += jumlah;
        }
        Log.d(TAG, "total_part : " + jumlahAllPart);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Nson dataAdd = Nson.newObject();
        dataAdd.set("NO_PART", txtNoPart.getText().toString());
        dataAdd.set("NAMA_PART", txtNamaPart.getText().toString());
        dataAdd.set("JUMLAH", txtJumlah.getText().toString());
        dataAdd.set("HARGA_BELI", NumberFormatUtils.formatOnlyNumber(txtHargaBeliUnit.getText().toString()));
        dataAdd.set("NET", NumberFormatUtils.formatOnlyNumber(etHargaBersih.getText().toString()));
        dataAdd.set("PART_ID", partId);
        dataAdd.set("KODE", kodeFolder);
        dataAdd.set("MERK", merkPart);
        dataAdd.set("LOKASI_SIMPAN", spinnerLokasiSimpan.getSelectedItem().toString());
        dataAdd.set("NON_HPP", find(R.id.cb_non_hpp, CheckBox.class).isChecked() ? "Y" : "N");

        if (find(R.id.cb_non_hpp, CheckBox.class).isChecked())
            find(R.id.cb_non_hpp, CheckBox.class).setChecked(false);

        String disc = "";
        if (etDiscPercent.isEnabled()) {
            disc = etDiscPercent.getText().toString();
            dataAdd.set("DISCOUNT", disc);
        } else if (etDiscRp.isEnabled()) {
            disc = formatOnlyNumber(etDiscRp.getText().toString());
            dataAdd.set("DISCOUNT", disc);
        } else {
            dataAdd.set("DISCOUNT", disc);
        }
        totalAll += Long.parseLong(NumberFormatUtils.formatOnlyNumber(etHargaBersih.getText().toString()));
        dialogKonfirmasi(dataAdd);
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
                    } else {
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
                etDiscRp.setText("");
                etDiscRp.setEnabled(!hasFocus);
                break;
            case R.id.et_discRp_terimaPart:
                etDiscPercent.setText("");
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

    @SuppressLint("SetTextI18n")
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
            find(R.id.et_non_hpp, EditText.class).setText(RP + NumberFormatUtils.formatRp(n.get("HPP").asString()));
            setSpinnerLokasiSimpan(lokasiPart);
            etPenempatan.setText(penempatan);
        } else if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK) {
            try {
                getDataBarcode(data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "");
                showSuccess(data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
