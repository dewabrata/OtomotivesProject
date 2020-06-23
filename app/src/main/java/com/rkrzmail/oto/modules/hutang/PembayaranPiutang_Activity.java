package com.rkrzmail.oto.modules.hutang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class PembayaranPiutang_Activity extends AppActivity {

    private EditText etTotalHutang, etTotalBayar, etSelisih;
    private Spinner spKondisi, spTipePembayaran, spRekPenerima;
    private TextView tvTgl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran_piutang);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pembayaran Piutang");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();

        etTotalBayar = findViewById(R.id.et_totalBayar_bayarPiutang);
        etTotalHutang = findViewById(R.id.et_totalPiutang_bayarPiutang);
        etSelisih = findViewById(R.id.et_selisih_bayarPiutang);
        spKondisi = findViewById(R.id.sp_kondisi_bayarPiutang);
        spTipePembayaran = findViewById(R.id.sp_tipeBayar_bayarPiutang);
        spRekPenerima = findViewById(R.id.sp_rekPenerima_bayarPiutang);
        tvTgl = findViewById(R.id.tv_tglBayar_bayarPiutang);

        tvTgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.getDatePickerDialogTextView(getActivity(), tvTgl);
            }
        });

        find(R.id.btn_simpan_bayarPiutang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void componentValidation(){
        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String items = parent.getSelectedItem().toString();
                if(items.equalsIgnoreCase("CASH")){
                    spRekPenerima.setEnabled(false);
                }else{
                    spRekPenerima.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

}
