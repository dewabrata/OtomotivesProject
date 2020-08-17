package com.rkrzmail.oto.modules.sparepart;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AturParts_Activity extends AppActivity {

    public static final String TAG = "AturPartNew__";
    private String namaPart, noPart;
    private List<String> parts = new ArrayList<>();
    private boolean isParts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_parts);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_part);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Parts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        final Nson aturParts = Nson.readJson(getIntentStringExtra("atur_part"));
        final Nson addParts = Nson.readJson(getIntentStringExtra("data"));
        //isParts = true for aturParts, isParts = false for addParts
        if(getIntent().hasExtra("atur_part")){
            isParts = true;
            find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String stockMin = find(R.id.et_stockMin_part, EditText.class).getText().toString();
                    String stockTersedia = find(R.id.et_stockTersedia_part, EditText.class).getText().toString();
                    if (Integer.parseInt(stockTersedia) < Integer.parseInt(stockMin)) {
                        showInfoDialog("Part Tidak Dapat Di hapus", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    deleteData(aturParts);
                }
            });
            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateData(aturParts);
                }
            });
        }else if(getIntent().hasExtra("data")){
            isParts = false;
            namaPart = addParts.get("NAMA_PART").asString();
            getContainsSparepart();
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String waktu = find(R.id.et_waktuPesan_part, EditText.class).getText().toString().replace(" ", "").toUpperCase();
                    if (waktu.isEmpty()) {
                        showWarning("Waktu Pesan Tidak Boleh Kosong");
                        return;
                    }
                    if (find(R.id.et_stockMin_part, EditText.class).getText().toString().isEmpty()) {
                        showWarning("Stock Minimum Tidak Boleh Kosong");
                        return;
                    }
                    if (find(R.id.et_stockTersedia_part, EditText.class).getText().toString().isEmpty()) {
                        showWarning("Stock Tersedia Tidak Boleh Kosong");
                        return;
                    }
                    if (find(R.id.tl_margin, TextInputLayout.class).getVisibility() == View.VISIBLE) {
                        if (find(R.id.et_hargaJual_part, EditText.class).getText().toString().isEmpty()) {
                            showWarning("Margin / Harga Tidak Boleh Kosong");
                            return;
                        }
                    }
                    addData(addParts);
                }
            });
        }

        find(R.id.et_namaPart_part, EditText.class).setText(isParts ? aturParts.get("NAMA_PART").asString() : addParts.get("NAMA_PART").asString());
        find(R.id.et_noPart_part, EditText.class).setText(isParts ? aturParts.get("NO_PART").asString() : addParts.get("NOMOR_PART_NOMOR").asString());
        find(R.id.et_merk_part, EditText.class).setText(isParts ? aturParts.get("MERK").asString() : addParts.get("MERK").asString());
        find(R.id.et_status_part, EditText.class).setText(isParts ? aturParts.get("STATUS").asString() : addParts.get("STATUS_PRODUKSI_STAT").asString());
        find(R.id.et_waktuGanti_part, EditText.class).setText(isParts ? aturParts.get("WAKTU_GANTI").asString() : addParts.get("WAKTU_GANTI").asString());
        find(R.id.et_het_part, EditText.class).setText(isParts ? aturParts.get("HET").asString() : addParts.get("HET").asString());
        find(R.id.et_stockTersedia_part, EditText.class).setText(isParts ? aturParts.get("STOCK").asString() : addParts.get("STOCK").asString());
        find(R.id.et_stockMin_part, EditText.class).setText(aturParts.get("STOCK_MINIMUM").asString());
        find(R.id.sp_polaHarga_part, Spinner.class).setSelection(Tools.getIndexSpinner(find(R.id.sp_polaHarga_part, Spinner.class), aturParts.get("POLA_HARGA_JUAL").asString()));
        find(R.id.et_hargaJual_part, EditText.class).setText(aturParts.get("HARGA_JUAL").asString());

        Log.d(TAG, "Nson : " + aturParts + "\n" + "Atur : " + addParts);
        setTextListener();
        find(R.id.sp_polaHarga_part, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("FLEXIBLE")) {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.GONE);
                } else if (item.equalsIgnoreCase("NOMINAL")) {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.VISIBLE);
                    find(R.id.et_hargaJual_part, EditText.class).setHint("HARGA");
                }else if (item.equalsIgnoreCase("HET")) {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.GONE);
                    find(R.id.et_hargaJual_part, EditText.class).setText("");
                }else {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.VISIBLE);
                    find(R.id.et_hargaJual_part, EditText.class).setHint("MARGIN");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void getContainsSparepart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.size(); i++) {
                        if (namaPart.equalsIgnoreCase(result.get("data").get(i).get("NAMA_PART").asString())) {
                            showWarning("Part Duplikat");
                            finish();
                        }
                    }
                }
            }
        });
    }

    private void deleteData(final Nson id) {
        final String namaPart = find(R.id.et_namaPart_part, EditText.class).getText().toString();
        final String noPart = find(R.id.et_noPart_part, EditText.class).getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "delete");
                args.put("id", id.get("ID").asString());
                args.put("nopart", noPart);
                args.put("namapart", namaPart);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("atursparepart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menghapus Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menghapus Part");
                }

            }
        });
    }

    private void updateData(final Nson id) {
        final String stock = find(R.id.et_stockTersedia_part, EditText.class).getText().toString();
        final String stockMin = find(R.id.et_stockMin_part, EditText.class).getText().toString();
        final String waktuPesan = find(R.id.et_waktuPesan_part, EditText.class).getText().toString();
        final String hargaJual = find(R.id.et_hargaJual_part, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String polaHarga = find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("stock", stock);
                args.put("partid", id.get("PART_ID").asString());
                args.put("waktupesan", waktuPesan);
                args.put("stokminim", stockMin);
                args.put("hargajual", hargaJual);
                args.put("polahargajual", polaHarga);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("atursparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Memperbarui Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Memperbarui Part");
                }

            }
        });
    }

    private void addData(final Nson id) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateTime = simpleDateFormat.format(calendar.getTime());
        final String waktuPesan = find(R.id.et_waktuPesan_part, EditText.class).getText().toString();
        final String stockMin = find(R.id.et_stockMin_part, EditText.class).getText().toString();
        final String hargaJual = find(R.id.et_hargaJual_part, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String namaPart = find(R.id.et_namaPart_part, EditText.class).getText().toString();
        final String noPart = find(R.id.et_noPart_part, EditText.class).getText().toString();
        final String merkPart = find(R.id.et_merk_part, EditText.class).getText().toString();
        final String status = find(R.id.et_status_part, EditText.class).getText().toString();
        final String waktuGanti = find(R.id.et_waktuGanti_part, EditText.class).getText().toString();
        final String polaHarga = find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString();
        final String het = find(R.id.et_het_part, EditText.class).getText().toString();
        final String stock = find(R.id.et_stockTersedia_part, EditText.class).getText().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //CID , partid , nama , nopart , merk , hargajual , waktupesan , stokminim ,
                // hpp , status , waktuganti , polahargajual
                args.put("action", "add");
                args.put("nama", namaPart);
                args.put("nopart", noPart);
                args.put("partid", id.get("NO").asString());
                args.put("merk", merkPart);
                args.put("hargajual", hargaJual);
                args.put("waktupesan", waktuPesan);
                args.put("stokminim", stockMin);
                args.put("hpp", het);
                args.put("status", status);
                args.put("waktuganti", waktuGanti);
                args.put("polahargajual", polaHarga);
                args.put("stock", stock);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("atursparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menambahkan Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menambahkan Part");
                }
            }
        });
    }

    private void setTextListener() {
        find(R.id.et_hargaJual_part, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                find(R.id.et_hargaJual_part, EditText.class).removeTextChangedListener(this);
                String s = editable.toString();
                if (!find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().equals("")) {
                    if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("NOMINAL")) {
                        try {
                            String cleanString = s.replaceAll("[^0-9]", "");
                            String formatted = Tools.formatRupiah(cleanString);
                            find(R.id.et_hargaJual_part, EditText.class).setText(formatted);
                            find(R.id.et_hargaJual_part, EditText.class).setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                            find(R.id.et_hargaJual_part, EditText.class).setSelection(formatted.length());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("BELI + MARGIN")) {
                        NumberFormat format = NumberFormat.getPercentInstance(new Locale("in", "ID"));
                        format.setMinimumFractionDigits(1);
                        format.setMaximumFractionDigits(1);
                        String percentNumber = format.format(Tools.convertToDoublePercentage(find(R.id.et_hargaJual_part, EditText.class).getText().toString()) / 1000);
                        find(R.id.et_hargaJual_part, EditText.class).setText(percentNumber);
                        find(R.id.et_hargaJual_part, EditText.class).setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
                        find(R.id.et_hargaJual_part, EditText.class).setSelection(percentNumber.length() - 1);
                    } else if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("RATA - RATA + MARGIN")) {
                        NumberFormat format = NumberFormat.getPercentInstance(new Locale("in", "ID"));
                        format.setMinimumFractionDigits(1);
                        format.setMaximumFractionDigits(1);
                        String percentNumber = format.format(Tools.convertToDoublePercentage(find(R.id.et_hargaJual_part, EditText.class).getText().toString()) / 1000);
                        find(R.id.et_hargaJual_part, EditText.class).setText(percentNumber);
                        find(R.id.et_hargaJual_part, EditText.class).setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
                        find(R.id.et_hargaJual_part, EditText.class).setSelection(percentNumber.length() - 1);
                    }
                }
                find(R.id.et_hargaJual_part, EditText.class).addTextChangedListener(this);
            }
        });
        find(R.id.et_stockMin_part, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                String st = find(R.id.et_stockTersedia_part, EditText.class).getText().toString();
                if (!s.equals("") && !st.equals("")) {
                    int stock = Integer.parseInt(s);
                    int stockAda = Integer.parseInt(st);
                    if (stock > stockAda) {
                        find(R.id.tl_stock_min, TextInputLayout.class).setError("Stock Min Tidak Valid");
                    } else {
                        find(R.id.tl_stock_min, TextInputLayout.class).setErrorEnabled(false);
                    }
                }
            }
        });

    }
}
