package com.rkrzmail.oto.modules.lokasi_part.stock_opname;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class StockOpname_Activity extends AppActivity {

    private EditText noFolder, noPart, jumlahData, jumlahAkhir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_opname);
        initToolbar();

        noFolder = findViewById(R.id.et_noFolder_stockOpname);
        noPart = findViewById(R.id.et_noPart_stockOpname);
        jumlahData = findViewById(R.id.et_jumlahdata_stockOpname);
        jumlahAkhir = findViewById(R.id.et_jumlahakhir_stockOpname);

        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_stock_opname);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stock Opname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        find(R.id.btn_simpan_stockOpname, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
