package com.rkrzmail.oto.modules.sparepart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.RupiahFormat;

import java.util.Map;

public class DetailJualPart_Activity extends AppActivity {

    private EditText etHpp, etHargaJual, etDisc, etJumlah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_jual_part_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_detailPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Detail Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDisc = findViewById(R.id.et_disc_detailPart);
        etHargaJual = findViewById(R.id.et_hargaJual_detailPart);
        etHpp = findViewById(R.id.et_hpp_detailPart);
        etJumlah = findViewById(R.id.et_jumlah_detailPart);

        loadData();

        etHargaJual.addTextChangedListener(new RupiahFormat(etHargaJual));
        etJumlah.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    etJumlah.setText("1");
                }
            }
        });

        find(R.id.btn_simpan_detailPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etJumlah.getText().toString().equalsIgnoreCase("0")) {
                    showInfo("Jumlah Part Tidak Boleh Kosong");
                    return;
                }
                saveData();
            }
        });
    }

    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra("part"));
        etHpp.setText(n.get("HPP").asString());
        etDisc.setText(n.get("DISCOUNT").asString());
    }

    private void saveData() {
        Nson n = Nson.readJson(getIntentStringExtra("part"));
        n.set("JUMLAH", etJumlah.getText().toString());
        n.set("TOTAL", etJumlah.getText().toString());
        n.set("hpp", etHpp.getText().toString());
        n.set("DISC", etDisc.getText().toString());
        Intent i = new Intent(getActivity(), DaftarJualPart_Activity.class);
        i.putExtra("part", n.toJson());
        startActivity(i);
        finish();
    }
}
