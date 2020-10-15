package com.rkrzmail.oto.modules.bengkel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.discount.SpotDiscount_Activity;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class PembayaranJurnal_Activity extends AppActivity {

    private EditText etTotal, etNoTrack, etNoRek, etBiayaTf;
    private Spinner spTipePembayaran, spNoRek, spBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran_jurnal);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_pembayaran);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etTotal = findViewById(R.id.et_total_pembayaran);
        etNoTrack = findViewById(R.id.et_noTrack_pembayaran);
        etNoRek = findViewById(R.id.et_noRek_pembayaran);
        etBiayaTf = findViewById(R.id.et_biayaTf_pembayaran);
        spTipePembayaran = findViewById(R.id.sp_tipePembayaran_pembayaran);
        spNoRek = findViewById(R.id.sp_noRek_pembayaran);
        spBank = findViewById(R.id.sp_bank_pembayaran);

        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();

                if (item.equalsIgnoreCase("CASH")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_pembayaran, LinearLayout.class), false);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_pembayaran, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_simpan_pembayaran, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void saveData() {
        final String total = etTotal.getText().toString();
        final String noTrack = etNoTrack.getText().toString();
        final String noRek = etNoRek.getText().toString();
        final String biayaTf = etBiayaTf.getText().toString();
        final String tipePembayaran = spTipePembayaran.getSelectedItem().toString();
        final String sNoRek = spNoRek.getSelectedItem().toString();
        final String bank = spBank.getSelectedItem().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                if (find(R.id.ly_pembayaran, LinearLayout.class).isEnabled()) {
                    args.put("", total);
                    args.put("", noTrack);
                    args.put("", noRek);
                    args.put("", biayaTf);
                    args.put("", sNoRek);
                    args.put("", bank);
                } else {
                    args.put("", tipePembayaran);
                    args.put("", total);
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("jurnal"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(getActivity(), SpotDiscount_Activity.class));
                } else {
                    showInfo("GAGAL");
                }
            }
        });

    }
}
