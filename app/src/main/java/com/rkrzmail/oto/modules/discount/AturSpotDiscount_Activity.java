package com.rkrzmail.oto.modules.discount;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class AturSpotDiscount_Activity extends AppActivity {

    private NikitaAutoComplete cariNoponsel;
    private EditText etNoPonsel, etNama, etTransaksi, etDisc, etNet, etSpot, etTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_spot_discount);
        initToolbar();
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_disc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Spot Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        cariNoponsel = findViewById(R.id.et_cariNoPonsel_disc);
        etNama = findViewById(R.id.et_namaPelanggan_disc);
        etDisc = findViewById(R.id.et_discLain_disc);
        etNet = findViewById(R.id.et_netTransaksi_disc);
        etNoPonsel = findViewById(R.id.et_noPonsel_disc);
        etTotal = findViewById(R.id.et_total_disc);
        etTransaksi = findViewById(R.id.et_transaksi_disc);
        etSpot = findViewById(R.id.et_spotDiscount_disc);

        cariNoponsel.setThreshold(7);
        cariNoponsel.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                return super.onFindNson(context, bookTitle);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nama_part, parent, false);
                }

                etNoPonsel.setText(getItem(position).get("").asString());
                etNama.setText(getItem(position).get("").asString());
                etTransaksi.setText(getItem(position).get("").asString());
                etDisc.setText(getItem(position).get("").asString());
                etNet.setText(getItem(position).get("").asString());
                etTotal.setText(getItem(position).get("").asString());

                return convertView;
            }
        });

        find(R.id.btn_simpan_disc, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("spotdisc"), args));
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
