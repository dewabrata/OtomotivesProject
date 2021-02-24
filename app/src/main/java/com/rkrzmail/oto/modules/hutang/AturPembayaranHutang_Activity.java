package com.rkrzmail.oto.modules.hutang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class AturPembayaranHutang_Activity extends AppActivity {

    private EditText etTotalHutang, etDiscRp, etDiscPercent, etTotalBayar, etSelisih, etBankBerbayar, etNorek, etNamarek, etNoTrace, etBiayaTf;
    private Spinner spPembayaran, spRekAsal;
    private TextView tvTglBayar, tvTglJatuhTempo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran_hutang_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pembayaran Hutang");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        etTotalBayar = findViewById(R.id.et_totalBayar_bayarHutang);
        etTotalHutang = findViewById(R.id.et_totalHutang_bayarHutang);
        etDiscRp = findViewById(R.id.et_disc_rupiah);
        etDiscPercent = findViewById(R.id.et_disc_percent);
        etSelisih = findViewById(R.id.et_selisih_bayarHutang);
        etBankBerbayar = findViewById(R.id.et_bankBerbayar_bayarHutang);
        etNorek = findViewById(R.id.et_noRek_bayarHutang);
        etNamarek = findViewById(R.id.et_namaRek_bayarHutang);
        etNoTrace = findViewById(R.id.et_noTrace_bayarHutang);
        etBiayaTf = findViewById(R.id.et_biayaTf_bayarHutang);
        spPembayaran = findViewById(R.id.sp_tipe_pembayaran);
        spRekAsal = findViewById(R.id.sp_norek);
        tvTglJatuhTempo = findViewById(R.id.tv_tgl_jatuh_tempo);
        tvTglBayar = findViewById(R.id.tv_tglBayar_bayarPiutang);

        find(R.id.btn_simpan_bayarHutang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void componentValidation(){

    }

    private void loadData(){
        Nson n = Nson.readJson(getIntentStringExtra(""));


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
