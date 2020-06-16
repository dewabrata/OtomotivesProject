package com.rkrzmail.oto.modules.pembayaran;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.rkrzmail.oto.modules.biayamekanik.AturBiayaMekanik2;
import com.rkrzmail.oto.modules.biayamekanik.BiayaMekanik2Activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class AturPembayaran_Activity extends AppActivity {

    private EditText etGrandTotal, etNoKartu, etNoTrack, etTotalBayar, etKembalian;
    private Spinner spTipePembayaran, spNoRek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_pembayaran_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_aturPembayaran);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etGrandTotal = findViewById(R.id.et_grandTotal_aturPembayaran);
        etNoKartu = findViewById(R.id.et_noKartu_aturPembayaran);
        etNoTrack = findViewById(R.id.et_noTrack_aturPembayaran);
        etTotalBayar = findViewById(R.id.et_totalBayar_aturPembayaran);
        etKembalian = findViewById(R.id.et_kembalian_aturPembayaran);
        spTipePembayaran = findViewById(R.id.sp_tipePembayaran_aturPembayaran);
        spNoRek = findViewById(R.id.sp_noRek_aturPembayaran);

        find(R.id.btn_simpan_aturPembayaran, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void componentValidation() {
        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
//                if(item.equalsIgnoreCase("CASH")){
//                    spNoRek.setEnabled(false);
//                    spBank.setEnabled(false);
//                    etNoTrack.setEnabled(false);
//                    return;
//                }else{
//                    spNoRek.setEnabled(true);
//                    spBank.setEnabled(true);
//                    etNoTrack.setEnabled(true);
//                    return;
//                }
//                if(item.equalsIgnoreCase("TRANSFER")){
//                    spBank.setEnabled(false);
//                    etNoKartu.setEnabled(false);
//                    etTotalBayar.setEnabled(false);
//                    etKembalian.setText("Rp. 0.-");
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), DaftarPembayaran_Activity.class));
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }
}
