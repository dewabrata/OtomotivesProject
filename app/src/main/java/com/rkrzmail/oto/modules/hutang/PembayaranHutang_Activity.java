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

public class PembayaranHutang_Activity extends AppActivity {

    private EditText etTotalHutang, etDisc, etTotalBayar, etSelisih, etBankBerbayar, etNorek, etNamarek, etNoTrace, etBiayaTf;
    private Spinner spPembayaran, spRekAsal;
    private TextView tvTglBayar, tvTglJatuhTempo;
    private ImageView imgCamera;

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
        etDisc = findViewById(R.id.et_discPembayaran_bayarHutang);
        etSelisih = findViewById(R.id.et_selisih_bayarHutang);
        etBankBerbayar = findViewById(R.id.et_bankBerbayar_bayarHutang);
        etNorek = findViewById(R.id.et_noRek_bayarHutang);
        etNamarek = findViewById(R.id.et_namaRek_bayarHutang);
        etNoTrace = findViewById(R.id.et_noTrace_bayarHutang);
        etBiayaTf = findViewById(R.id.et_biayaTf_bayarHutang);
        spPembayaran = findViewById(R.id.sp_pembayaran_bayarHutang);
        spRekAsal = findViewById(R.id.sp_rekAsal_bayarHutang);
        tvTglJatuhTempo = findViewById(R.id.tv_tglJatuhTempo_bayarHutang);
        tvTglBayar = findViewById(R.id.tv_tglBayar_bayarPiutang);
        imgCamera = findViewById(R.id.img_camera_bayarHutang);

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
