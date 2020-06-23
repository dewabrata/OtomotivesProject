package com.rkrzmail.oto.modules.jurnal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class AturJurnal_Activity extends AppActivity implements View.OnClickListener {

    private TextView tvTglTransaksi,tvKontak, tvTglJatuhTempo, tvTglMulaiSewa, tvTglSelesaiSewa;
    private Spinner spAktivitas, spTransaksi, spPembayaran, spRekAsal, spRekTujuan, spTahunAset, spBulanAset;
    private EditText etKet, etNominal, etBankTerbayar, etNota, etBiayaTf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_jurnal_);

        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        tvTglTransaksi = findViewById(R.id.tv_tglTransaksi_jurnal);
        tvKontak = findViewById(R.id.tv_kontak_jurnal);
        tvTglMulaiSewa = findViewById(R.id.tv_mulaiSewa_jurnal);
        tvTglSelesaiSewa = findViewById(R.id.tv_selesaiSewa_jurnal);
        tvTglJatuhTempo = findViewById(R.id.tv_tglJatuhTempo_jurnal);
        spAktivitas = findViewById(R.id.sp_aktivitas_jurnal);
        spTransaksi = findViewById(R.id.sp_transaksi_jurnal);
        spPembayaran = findViewById(R.id.sp_pembayaran_jurnal);
        spRekAsal = findViewById(R.id.sp_rekAsal_jurnal);
        spRekTujuan = findViewById(R.id.sp_rekTujuan_jurnal);
        spTahunAset = findViewById(R.id.sp_tahunAsset_jurnal);
        spBulanAset = findViewById(R.id.sp_bulanAsset_jurnal);
        etKet = findViewById(R.id.et_ket_jurnal);
        etNominal = findViewById(R.id.et_nominal_jurnal);
        etBankTerbayar = findViewById(R.id.et_bankTerbayar_jurnal);
        etNota = findViewById(R.id.et_notaTrace_jurnal);
        etBiayaTf = findViewById(R.id.et_biayaTf_jurnal);

        tvTglTransaksi.setOnClickListener(this);
        tvKontak.setOnClickListener(this);
        tvTglMulaiSewa.setOnClickListener(this);
        tvTglSelesaiSewa.setOnClickListener(this);


        find(R.id.btn_simpan_jurnal, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglTransaksi_jurnal:
                Tools.getDatePickerDialogTextView(getActivity(), tvTglTransaksi);
                break;
            case R.id.tv_kontak_jurnal:
                break;
            case R.id.tv_mulaiSewa_jurnal:
                Tools.getDatePickerDialogTextView(getActivity(), tvTglMulaiSewa);
                break;
            case R.id.tv_selesaiSewa_jurnal:
                Tools.getDatePickerDialogTextView(getActivity(), tvTglSelesaiSewa);
                break;
        }
    }

    private void saveData() {
//        final String tanggal = date.getText().toString();
//        final String mSewa = mulaiSewa.getText().toString();
//        final String sSewa = selesaiSewa.getText().toString();
//        final String poss = pos.getSelectedItem().toString();
//        final String transaksi = jenisTransaksi.getSelectedItem().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("jurnal"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("GAGAL");
                }
            }
        });

    }


}
