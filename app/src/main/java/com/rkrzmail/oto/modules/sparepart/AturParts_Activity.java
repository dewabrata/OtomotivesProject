package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_SPAREPART;
import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturParts_Activity extends AppActivity {

    public static final String TAG = "AturPartNew__";
    private String namaPart, noPart, harga = "";
    private List<String> parts = new ArrayList<>();
    private boolean isParts;
    private DecimalFormat formatter;

    private int margin = 0, total = 0;
    private String tempatPart = "";
    String penempatan = "", noRak = "", tinggiRak = "", noFolder = "", lokasi = "";

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

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        formatter = new DecimalFormat("###,###,###");
        final Nson aturParts = Nson.readJson(getIntentStringExtra("atur_part")); //EDIT PART
        final Nson addParts = Nson.readJson(getIntentStringExtra("data")); //ADD PART

        find(R.id.sp_lokasiPart, Spinner.class).setEnabled(false);
        //isParts = true for aturParts, isParts = false for addParts
        if (getIntent().hasExtra("atur_part")) {
            isParts = true;
            //find(R.id.et_hpp_part, EditText.class).setEnabled(false);
            find(R.id.et_stockTersedia_part, EditText.class).setEnabled(false);
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
                    if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("BELI + MARGIN") ||
                            find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("FLEXIBLE")) {
                        if (find(R.id.et_hpp_part, EditText.class).getText().toString().isEmpty()) {
                            find(R.id.et_hpp_part, EditText.class).setError("Masukkan HPP");
                            find(R.id.et_hpp_part, EditText.class).requestFocus();
                            return;
                        }
                    }
                    updateData(aturParts);
                }
            });
        } else if (getIntent().hasExtra("data")) {
            isParts = false;
            namaPart = addParts.get("NAMA_PART").asString();
            getContainsSparepart();
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String waktu = find(R.id.et_waktuPesan_part, EditText.class).getText().toString().replace(" ", "").toUpperCase();
                    if (waktu.isEmpty()) {
                        //showWarning("Waktu Pesan Tidak Boleh Kosong");
                        find(R.id.et_waktuPesan_part, EditText.class).requestFocus();
                        find(R.id.et_waktuPesan_part, EditText.class).setError("Masukkan Waktu Pesan");
                        return;
                    }
                    if (find(R.id.et_stockMin_part, EditText.class).getText().toString().isEmpty()) {
                        //showWarning("Stock Minimum Tidak Boleh Kosong");
                        find(R.id.et_stockMin_part, EditText.class).setError("Masukkan Stock Min.");
                        find(R.id.et_stockMin_part, EditText.class).requestFocus();
                        return;
                    }
                    if (find(R.id.et_stockTersedia_part, EditText.class).getText().toString().isEmpty()) {
                        //showWarning("Stock Tersedia Tidak Boleh Kosong");
                        find(R.id.et_stockTersedia_part, EditText.class).setError("Masukkan Stcok Tersedia");
                        find(R.id.et_stockTersedia_part, EditText.class).requestFocus();
                        return;
                    }
                    if (find(R.id.tl_margin, TextInputLayout.class).getVisibility() == View.VISIBLE) {
                        if (find(R.id.et_hargaJual_part, EditText.class).getText().toString().isEmpty()) {
                            find(R.id.et_hargaJual_part, EditText.class).setError("Masukkan Harga Jual");
                            find(R.id.et_hargaJual_part, EditText.class).requestFocus();
                            //showWarning("Margin / Harga Tidak Boleh Kosong");
                            return;
                        }
                    }
                    if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("BELI + MARGIN") ||
                            find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("FLEXIBLE")) {
                        if (find(R.id.et_hpp_part, EditText.class).getText().toString().isEmpty()) {
                            find(R.id.et_hpp_part, EditText.class).setError("Masukkan HPP");
                            find(R.id.et_hpp_part, EditText.class).requestFocus();
                            return;
                        }
                    }
                    saveData(addParts);
                }
            });
        }

        find(R.id.et_hpp_part, EditText.class).setText(isParts ? RP + NumberFormatUtils.formatRp(aturParts.get("HPP").asString()) : "");
        find(R.id.et_namaPart_part, EditText.class).setText(isParts ? aturParts.get("NAMA_PART").asString() : addParts.get("NAMA_PART").asString());
        find(R.id.et_noPart_part, EditText.class).setText(isParts ? aturParts.get("NO_PART").asString() : addParts.get("NO_PART").asString());
        find(R.id.et_merk_part, EditText.class).setText(isParts ? aturParts.get("MERK").asString() : addParts.get("MERK").asString());
        find(R.id.et_status_part, EditText.class).setText(isParts ? aturParts.get("STATUS").asString() : addParts.get("STATUS_PRODUKSI_STAT").asString());
        //find(R.id.et_waktuGanti_part, EditText.class).setText(isParts ?
        //aturParts.get("WAKTU_GANTI").asString() : addParts.get("WAKTU_KERJA_JAM").asString() +":"+ addParts.get("WAKTU_KERJA_MENIT").asString());
        find(R.id.et_stockTersedia_part, EditText.class).setText(aturParts.get("STOCK").asString());
        find(R.id.et_waktuPesan_part, EditText.class).setText(aturParts.get("WAKTU_PESAN_HARI").asString());
        find(R.id.et_stockMin_part, EditText.class).setText(aturParts.get("STOCK_MINIMUM").asString());
        find(R.id.et_hargaJual_part, EditText.class).setText("Rp. " + NumberFormatUtils.formatRp(aturParts.get("HPP").asString()));
        find(R.id.sp_polaHarga_part, Spinner.class).setSelection(Tools.getIndexSpinner(find(R.id.sp_polaHarga_part, Spinner.class), aturParts.get("POLA_HARGA_JUAL").asString()));
        try {
            find(R.id.et_het_part, EditText.class).setText(isParts ?
                    "Rp. " + NumberFormatUtils.formatRp(aturParts.get("HET").asString()) :
                    "Rp. " + NumberFormatUtils.formatRp(addParts.get("HET").asString()));
            if (!aturParts.get("MARGIN").asString().isEmpty()) {
                find(R.id.et_hargaJual_part, EditText.class).setText(NumberFormatUtils.formatPercent(Double.parseDouble(aturParts.get("MARGIN").asString())));
            } else {
                find(R.id.et_hargaJual_part, EditText.class).setText("Rp. " + NumberFormatUtils.formatRp(aturParts.get("HARGA_JUAL").asString()));
            }
        } catch (Exception e) {
            Log.d(TAG, "Number Exception : " + e.getMessage());
        }

        lokasi = aturParts.get("LOKASI").asString();
        noFolder = aturParts.get("NO_FOLDER").asString();
        noRak = aturParts.get("NO_RAK").asString();
        tinggiRak = aturParts.get("TINGKAT_RAK").asString();
        penempatan = aturParts.get("PENEMPATAN").asString();

        setTextListener();
        setSpinnerView();
        find(R.id.sp_polaHarga_part, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("NOMINAL")) {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.VISIBLE);
                    find(R.id.tl_margin, TextInputLayout.class).setHint("HARGA");
                } else if (item.equalsIgnoreCase("HET")) {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.GONE);
                    find(R.id.et_hargaJual_part, EditText.class).setText("");
                } else {
                    find(R.id.tl_margin, TextInputLayout.class).setVisibility(View.VISIBLE);
                    find(R.id.tl_margin, TextInputLayout.class).setHint("MARGIN");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSpinnerView() {
        final List<String> lokasiList = Arrays.asList("RUANG PART", "GUDANG", "DISPLAY");
        final List<String> noRakList = new ArrayList<String>();
        noRakList.add("--PILIH--");
        final List<String> tinggiRakList = new ArrayList<String>();
        tinggiRakList.add("--PILIH--");
        final List<String> noFolderList = new ArrayList<String>();
        noFolderList.add("--PILIH--");
        final List<String> penempatanList = Arrays.asList(getResources().getStringArray(R.array.penempatan_lokasi_part));

        for (int i = 1; i <= 100; i++) {
            if (!noRakList.contains(i)) {
                noRakList.add(String.valueOf(i));
            }
            if (!noFolderList.contains(i)) {
                noFolderList.add(String.valueOf(i));
            }
            if (i <= 10) {
                tinggiRakList.add(String.valueOf(i));
            }
        }

        setSpinnerOffline(penempatanList, find(R.id.sp_penempatan_part, Spinner.class), penempatan);
        setSpinnerOffline(noRakList, find(R.id.sp_norakPalet_part, Spinner.class), noRak);
        setSpinnerOffline(tinggiRakList, find(R.id.sp_tinggiRak, Spinner.class), tinggiRak);
        setSpinnerOffline(noFolderList, find(R.id.sp_noFolder_part, Spinner.class), noFolder);
        setSpinnerOffline(lokasiList, find(R.id.sp_lokasiPart, Spinner.class), lokasi);

        find(R.id.sp_penempatan_part, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                tempatPart = parent.getItemAtPosition(position).toString();
                if (tempatPart.equalsIgnoreCase("PALET")) {
                    find(R.id.sp_tinggiRak, Spinner.class).setEnabled(false);
                } else if (tempatPart.equalsIgnoreCase("RAK")) {
                    find(R.id.sp_tinggiRak, Spinner.class).setEnabled(true);
                } else {
                    find(R.id.sp_tinggiRak, Spinner.class).setEnabled(false);
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
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

    private String kodePenempatan(String tempat, String no, String tingkat, String folder) {
        String kode;
        if (tempat.equals("RAK")) {
            kode = "R" + "." + no + "." + tingkat + "." + folder;
        } else {
            kode = "P" + "." + no + "." + folder;
        }
        return kode;
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

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_SPAREPART), args));

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
        final String hargaJual = find(R.id.et_hargaJual_part, EditText.class).getText().toString();
        final String polaHarga = find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString();
        final String het = NumberFormatUtils.formatOnlyNumber(find(R.id.et_het_part, EditText.class).getText().toString());
        final String hpp = NumberFormatUtils.formatOnlyNumber(find(R.id.et_hpp_part, EditText.class).getText().toString());

        try {
            if (hargaJual.contains("%")) {
                harga = NumberFormatUtils.clearPercent(hargaJual);
                margin = (int) (Integer.parseInt(hpp) * Double.parseDouble(harga) / 100);
                total = margin + Integer.parseInt(hpp);
                Log.e("sparepart__", "saveData: " + total);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
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
                if (polaHarga.equalsIgnoreCase("HET")) {
                    args.put("hargajual", het);
                } else if (polaHarga.equalsIgnoreCase("BELI + MARGIN") ||
                        polaHarga.equalsIgnoreCase("RATA - RATA + MARGIN") ||
                        polaHarga.equalsIgnoreCase("FLEXIBLE")) {
                    args.put("hargajual", String.valueOf(total));
                    args.put("margin",  hargaJual.replace("%", ""));
                }
                args.put("polahargajual", polaHarga);
                args.put("hpp", hpp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_SPAREPART), args));
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

    private void saveData(final Nson id) {
        final String waktuPesan = find(R.id.et_waktuPesan_part, EditText.class).getText().toString();
        final String stockMin = find(R.id.et_stockMin_part, EditText.class).getText().toString();
        final String hargaJual = find(R.id.et_hargaJual_part, EditText.class).getText().toString();
        final String namaPart = find(R.id.et_namaPart_part, EditText.class).getText().toString();
        final String noPart = find(R.id.et_noPart_part, EditText.class).getText().toString();
        final String merkPart = find(R.id.et_merk_part, EditText.class).getText().toString();
        final String status = find(R.id.et_status_part, EditText.class).getText().toString();
        final String waktuGanti = find(R.id.et_waktuGanti_part, EditText.class).getText().toString();
        final String polaHarga = find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString();
        final String het = find(R.id.et_het_part, EditText.class).getText().toString();
        final String stock = find(R.id.et_stockTersedia_part, EditText.class).getText().toString();
        final String hpp = find(R.id.et_hpp_part, EditText.class).getText().toString().replaceAll("[^0-9]+", "");

        try {
            if (hargaJual.contains("%")) {
                harga = NumberFormatUtils.clearPercent(hargaJual);
                margin = (int) (Integer.parseInt(hpp) * Double.parseDouble(harga) / 100);
                total = margin + Integer.parseInt(hpp);
                Log.e("sparepart__", "saveData: " + total);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }

        final String nofolder = find(R.id.sp_noFolder_part, Spinner.class).getSelectedItem().toString().toUpperCase();
        final String rakOrPalet = find(R.id.sp_norakPalet_part, Spinner.class).getSelectedItem().toString();
        String tinggirak;
        if (find(R.id.sp_tinggiRak, Spinner.class).isEnabled()) {
            tinggirak = find(R.id.sp_tinggiRak, Spinner.class).getSelectedItem().toString();
        } else {
            tinggirak = "0";
        }
        final String finalTinggirak = tinggirak;
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("nama", namaPart);
                args.put("nopart", noPart);
                args.put("partid", id.get("ID").asString());
                args.put("merk", merkPart);
                if (polaHarga.equalsIgnoreCase("HET")) {
                    args.put("hargajual", het.replaceAll("[^0-9]+", ""));
                } else if (polaHarga.equalsIgnoreCase("BELI + MARGIN") ||
                        polaHarga.equalsIgnoreCase("RATA - RATA + MARGIN") ||
                        polaHarga.equalsIgnoreCase("FLEXIBLE")) {
                    args.put("hargajual", String.valueOf(total));
                    args.put("margin", hargaJual.replace("%", ""));
                }
                args.put("waktupesan", waktuPesan);
                args.put("stokminim", stockMin);
                args.put("het", het.replaceAll("[^0-9]+", ""));
                args.put("status", status);
                args.put("waktuganti", waktuGanti);
                args.put("polahargajual", polaHarga);
                args.put("stock", stock);
                args.put("hpp", hpp);
                args.put("penempatan", tempatPart);
                args.put("lokasi", find(R.id.sp_lokasiPart, Spinner.class).getSelectedItem().toString());
                if (tempatPart.equalsIgnoreCase("PALET")) {
                    args.put("palet", rakOrPalet);
                    args.put("rak", "0");
                } else {
                    args.put("rak", rakOrPalet);
                    args.put("palet", "0");
                }
                args.put("noFolder", nofolder);
                args.put("tingkatrak", finalTinggirak);
                args.put("kode", tempatPart.equals("--PILIH--") ? "*" : kodePenempatan(tempatPart, rakOrPalet, finalTinggirak, nofolder));
                args.put("kendaraan", UtilityAndroid.getSetting(getApplicationContext(), "JENIS_KENDARAAN", "").trim());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_SPAREPART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("BERHASIL MENAMBAHKAN PART");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    if (result.get("message").asString().contains("Duplicate entry")) {
                        showError("PART SUDAH DI TAMBAHKAN", Toast.LENGTH_LONG);
                    } else {
                        showError("GAGAL MENAMBAHKAN PART", Toast.LENGTH_LONG);
                    }
                }
            }
        });
    }

    private void setTextListener() {
        find(R.id.et_hpp_part, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_hpp_part, EditText.class)));
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
                String text = editable.toString();
                if (!find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().equals("")) {
                    find(R.id.et_hargaJual_part, EditText.class).setHint("HARGA JUAL");
                    if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("NOMINAL")) {
                        try {
                            text = NumberFormatUtils.formatOnlyNumber(text);
                            text = NumberFormatUtils.formatRp(text);

                            find(R.id.et_hargaJual_part, EditText.class).setText(text);
                            find(R.id.et_hargaJual_part, EditText.class).setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                            find(R.id.et_hargaJual_part, EditText.class).setSelection(text.length());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("BELI + MARGIN") ||
                            find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("FLEXIBLE")) {
                        text = NumberFormatUtils.setPercentage(text);

                        find(R.id.et_hargaJual_part, EditText.class).setText(text);
                        find(R.id.et_hargaJual_part, EditText.class).setFilters(NumberFormatUtils.getPercentFilterMargin());
                        find(R.id.et_hargaJual_part, EditText.class).setSelection(text.length() - 1);
                    } else if (find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("RATA - RATA + MARGIN")) {
                        java.text.NumberFormat format = java.text.NumberFormat.getPercentInstance(new Locale("in", "ID"));
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
