package com.rkrzmail.oto.modules.jurnal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

    private TextView date, mulaiSewa, selesaiSewa, kontak;
    private Spinner pos, jenisTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_jurnal_);

        initToolbar();
        initComponent();

    }

    private void initToolbar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_jurnal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        date = findViewById(R.id.tv_date_jurnal);
        kontak = findViewById(R.id.tv_kontak_jurnal);
        mulaiSewa = findViewById(R.id.tv_mulaiSewa_jurnal);
        selesaiSewa = findViewById(R.id.tv_selesaiSewa_jurnal);
        pos = findViewById(R.id.sp_pos_jurnal);
        jenisTransaksi = findViewById(R.id.sp_jenis_jurnal);

        date.setOnClickListener(this);
        kontak.setOnClickListener(this);
        mulaiSewa.setOnClickListener(this);
        selesaiSewa.setOnClickListener(this);

        pos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();

                if (item.equalsIgnoreCase("SEWA GEDUNG DAN LAINYA")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_periodeSewa_jurnal, LinearLayout.class), false);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_periodeSewa_jurnal, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        jenisTransaksi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();

                if (item.equalsIgnoreCase("PENDAPATAN")) {

                } else {

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_lanjut_jurnal, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_date_jurnal:
                Tools.getDatePickerDialogTextView(getActivity(), date);
                break;
            case R.id.tv_kontak_jurnal:
                break;
            case R.id.tv_mulaiSewa_jurnal:
                Tools.getDatePickerDialogTextView(getActivity(), mulaiSewa);
                break;
            case R.id.tv_selesaiSewa_jurnal:
                Tools.getDatePickerDialogTextView(getActivity(), selesaiSewa);
                break;
        }
    }

    private void saveData() {
        final String tanggal = date.getText().toString();
        final String mSewa = mulaiSewa.getText().toString();
        final String sSewa = selesaiSewa.getText().toString();
        final String poss = pos.getSelectedItem().toString();
        final String transaksi = jenisTransaksi.getSelectedItem().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                if (find(R.id.ly_periodeSewa_jurnal, LinearLayout.class).isEnabled()) {
                    args.put("", mSewa);
                    args.put("", sSewa);
                } else {
                    args.put("", tanggal);
                    args.put("", poss);
                    args.put("", transaksi);
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("jurnal"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Intent i = new Intent(getActivity(), PembayaranJurnal_Activity.class);
                    i.putExtra("tanggal", tanggal);
                    i.putExtra("msewa", mSewa);
                    i.putExtra("ssewa", sSewa);
                    i.putExtra("pos", poss);
                    i.putExtra("transaksi", transaksi);
                    startActivity(i);
                    finish();
                } else {
                    showInfo("GAGAL");
                }
            }
        });

    }


}
