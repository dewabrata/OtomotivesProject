package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class AturParts_Activity extends AppActivity {
    public static final String TAG = "AturPartNew";

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
        getSupportActionBar().setTitle("SPAREPART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        final Nson data = Nson.readJson(getIntentStringExtra("NAMA"));
        Intent i = getIntent();
        if (i.hasExtra("NAMA")) {
            find(R.id.et_namaPart_part, EditText.class).setText(data.get("NAMA").asString());
            find(R.id.et_noPart_part, EditText.class).setText(data.get("NO_PART").asString());
            find(R.id.et_merk_part, EditText.class).setText(data.get("MERK").asString());
            find(R.id.et_status_part, EditText.class).setText(data.get("STATUS").asString());
            find(R.id.et_waktuGanti_part, EditText.class).setText(data.get("WAKTU_GANTI").asString());
            find(R.id.et_waktuPesan_part, EditText.class).setText(data.get("HET").asString());

            find(R.id.btn_hapus_part, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_part, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int stock = Integer.parseInt(find(R.id.et_waktuPesan_part, EditText.class).getText().toString());
                    if (stock < 0) {
                        Tools.alertDialog(getActivity(), "Part Tidak Dapat Di hapus");
                        return;
                    } else if (stock == 0) {
                        deleteData();
                    }
                }
            });

            find(R.id.btn_simpan_part, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateData();
                }
            });
        }
//        else if(i.hasExtra("NO_PART")){
//            find(R.id.txtNamaPart, EditText.class).setText(data.get("NAMA").asString());
//            find(R.id.txtNoPart, EditText.class).setText(data.get("NO_PART").asString());
//            find(R.id.txtMerk, EditText.class).setText(data.get("MERK").asString());
//            find(R.id.txtStatus, EditText.class).setText(data.get("STATUS").asString());
//            find(R.id.txtWaktuGanti, EditText.class).setText(data.get("WAKTU_GANTI").asString());
//            find(R.id.txtHet, EditText.class).setText(data.get("HET").asString());
//        }

        setTextListener();
        find(R.id.sp_polaHarga_part, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("FLEXIBLE")) {
                    find(R.id.et_marginHarga_part, EditText.class).setEnabled(false);
                } else {
                    find(R.id.et_marginHarga_part, EditText.class).setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_simpan_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String waktu = find(R.id.et_waktuPesan_part, EditText.class).getText().toString().replace(" ", "").toUpperCase();
                if (waktu.isEmpty()) {
                    showError("Waktu Pesan Boleh Kosong");
                    return;
                } else if (find(R.id.et_stockMin_part, EditText.class).getText().toString().isEmpty()) {
                    showError("Stock Minimum Tidak Boleh Kosong");
                    return;
                } else if (find(R.id.et_marginHarga_part, EditText.class).getText().toString().isEmpty()) {
                    showError("Margin / Harga Tidak Boleh Kosong");
                    return;
                } else if (find(R.id.et_stockTersedia_part, EditText.class).getText().toString().isEmpty()) {
                    showError("Stock Tersedia Tidak Boleh Kosong");
                    return;
                }
                addData();
            }
        });

    }

    private void deleteData() {
        final String namaPart = find(R.id.et_namaPart_part, EditText.class).getText().toString();
        final String noPart = find(R.id.et_noPart_part, EditText.class).getText().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Nson nson = Nson.readJson(getIntentStringExtra("NAMA"));

                args.put("action", "delete");
                args.put("nopart", noPart);
                args.put("namapart", namaPart);
                args.put("id", nson.get("partid").asString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("atursparepart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menghapus Part");
                    startActivity(new Intent(AturParts_Activity.this, Spareparts_Activity.class));
                    finish();
                } else {
                    showInfo("Gagal Menghapus Part");
                }

            }
        });
    }

    private void updateData() {

        final String stockMin = find(R.id.et_stockMin_part, EditText.class).getText().toString();
        final String noPart = find(R.id.et_noPart_part, EditText.class).getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Nson nson = Nson.readJson(getIntentStringExtra("NAMA"));

                args.put("action", "update");
                args.put("stock", stockMin);
                args.put("partid", nson.get("PART_ID").asString());
                args.put("nopart", noPart);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("atursparepart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Memperbarui Part");
                    startActivity(new Intent(AturParts_Activity.this, Spareparts_Activity.class));
                    finish();
                } else {
                    showInfo("Gagal Memperbarui Part");
                }

            }
        });
    }

    private void addData() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String dateTime = simpleDateFormat.format(calendar.getTime());

        final String waktuPesan = find(R.id.et_waktuPesan_part, EditText.class).getText().toString();
        final String stockMin = find(R.id.et_stockMin_part, EditText.class).getText().toString();
        final String margin = find(R.id.et_marginHarga_part, EditText.class).getText().toString();
        final String namaPart = find(R.id.et_namaPart_part, EditText.class).getText().toString();
        final String noPart = find(R.id.et_noPart_part, EditText.class).getText().toString();
        final String merkPart = find(R.id.et_merk_part, EditText.class).getText().toString();
        final String status = find(R.id.et_status_part, EditText.class).getText().toString();
        final String waktuGanti = find(R.id.et_waktuGanti_part, EditText.class).getText().toString();
        final String polaHarga = find(R.id.sp_polaHarga_part, Spinner.class).getSelectedItem().toString();
        final String het = find(R.id.et_het_part, EditText.class).getText().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Calendar calendar = Calendar.getInstance();

                args.put("action", "add");
                args.put("nama", namaPart);
                args.put("nopart", noPart);
                //args.put("namalain", );
                args.put("merk", merkPart);
                args.put("status", status);
                args.put("waktuganti", waktuGanti);
                args.put("waktupesan", waktuPesan);
                args.put("stockminim", stockMin);
                args.put("het", het);
                args.put("polahargajual", polaHarga);
                args.put("waktu_pesan", waktuPesan);
                args.put("marginharga", margin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("atursparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menambahkan Part");
                    startActivity(new Intent(AturParts_Activity.this, Spareparts_Activity.class));
                    finish();
                } else {
                    showError("Gagal Menambahkan Part");
                }
            }
        });
    }

    private void setTextListener() {
        find(R.id.et_marginHarga_part, EditText.class).addTextChangedListener(new PercentFormat(find(R.id.et_marginHarga_part, EditText.class)));
        find(R.id.et_stockMin_part, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (find(R.id.et_stockMin_part, EditText.class) == null) return;
                String s = editable.toString();
                if (s.isEmpty()) return;
                find(R.id.et_stockMin_part, EditText.class).removeTextChangedListener(this);
                try {
                    String originalString = s.toString();

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);
                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("###,###");
                    String formattedString = formatter.format(longval);

                    //setting text after format to EditText
                    find(R.id.et_stockMin_part, EditText.class).setText(formattedString);
                    find(R.id.et_stockMin_part, EditText.class).setSelection(find(R.id.et_stockMin_part, EditText.class).getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                find(R.id.et_stockMin_part, EditText.class).addTextChangedListener(this);
            }
        });

    }
}
