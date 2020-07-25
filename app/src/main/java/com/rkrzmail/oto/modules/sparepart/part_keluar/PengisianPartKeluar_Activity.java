package com.rkrzmail.oto.modules.sparepart.part_keluar;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class PengisianPartKeluar_Activity extends AppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengisian_part_keluar_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Part Keluar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        final Nson nson = Nson.readJson(getIntentStringExtra("part"));
        find(R.id.et_stock_partKeluar, EditText.class).setText(nson.get("STOCK").asString());

        find(R.id.et_jumlah_partKeluar, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.et_jumlah_partKeluar, EditText.class).removeTextChangedListener(this);
                if (find(R.id.et_jumlah_partKeluar, EditText.class) == null) return;

                String jumlah = find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString();
                String stock = find(R.id.et_stock_partKeluar, EditText.class).getText().toString();
                if (!jumlah.equals("") && !stock.equals("")) {
                    if (Integer.parseInt(jumlah) > Integer.parseInt(stock)) {
                        showInfo("Jumlah Permintaan Melebihi Stock");
                        find(R.id.btn_simpan, Button.class).setEnabled(false);
                    }
                }
//                int jumlah = Integer.parseInt(find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString());
//                int stock = Integer.parseInt( find(R.id.et_stock_partKeluar, EditText.class).getText().toString());
//

                find(R.id.et_jumlah_partKeluar, EditText.class).addTextChangedListener(this);
            }
        });

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData(nson);
            }
        });
    }

    private void saveData(final Nson nson) {
        final String namaPart = nson.get("NAMA").asString();
        final String partId = nson.get("PART_ID").asString();
        final String user = getSetting("user");
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("user", user);
                args.put("namapart", namaPart);
                args.put("partid", partId);
                args.put("penerima", getSetting("NAMA"));
                args.put("jumlah", find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpartkeluar"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menambahkan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagal Menambahkan Aktifitas Mohon Di Coba Kembali");
                }
            }
        });

    }
}
