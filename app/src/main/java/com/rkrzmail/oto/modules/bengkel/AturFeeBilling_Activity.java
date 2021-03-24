package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;

public class AturFeeBilling_Activity extends AppActivity implements View.OnClickListener {

    private EditText etTotalHari, etTotalTransaksi, etTotalFee, etCashBack, etNetFee, etJumlahBayar;
    private Spinner spRekOto;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_fee_biling);
        initToolbar();
        initComponent();
        setComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bayar Fee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void initComponent(){
        etTotalHari = findViewById(R.id.et_total_hari);
        etTotalTransaksi = findViewById(R.id.et_total_transaksi);
        etTotalFee = findViewById(R.id.et_total_fee);
        etCashBack = findViewById(R.id.et_cashback);
        etNetFee = findViewById(R.id.et_net_fee);
        etJumlahBayar = findViewById(R.id.et_jumlah_bayar);
        spRekOto = findViewById(R.id.sp_rek_oto);
    }

    private void setComponent(){
        find(R.id.btn_simpan).setOnClickListener(this);
        find(R.id.img_btn_copy).setOnClickListener(this);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_simpan:
                break;
            case R.id.img_btn_copy:
                copyToClipBoard(NumberFormatUtils.formatOnlyNumber(etJumlahBayar.getText().toString()));
                showSuccess("TOTAL BAYAR BERHASIL DI COPY");
                break;
        }
    }

    private void copyToClipBoard(String text){
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
    }

}