package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class PilihPartActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_part);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("PILIH PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        find(R.id.tblBatal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jumlah = find(R.id.txtJumlah, EditText.class).getText().toString().replace(" ", "").toUpperCase();
                if (jumlah.equalsIgnoreCase("")) {
                    showError("Jumlah Tidak Boleh Kosong");
                    return;
                } else if (getIntentStringExtra("DATA") != ("")) {
                    Intent intent = new Intent(getActivity(), Pendaftaran3.class);
                    intent.putExtra("DATA", getIntentStringExtra("DATA"));
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (getIntentStringExtra("DATA2") != ("")) {
                    Intent intent = new Intent(getActivity(), PenjualanPartActivity.class);
                    intent.putExtra("DATA2", getIntentStringExtra("DATA2"));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        simpan();
    }

    private void simpan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String jumlah = find(R.id.txtJumlah, EditText.class).getText().toString();
                jumlah = jumlah.toUpperCase().trim().replace(" ", "");
                args.put("jumlah", jumlah);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("pilihpart.php"), args));
            }

            public void runUI() {

                find(R.id.txtHPP, EditText.class).setText(result.get(0).get("HPP").asString());
                find(R.id.txtHargaJual, EditText.class).setText(result.get(0).get("HARGA_JUAL").asString());
                find(R.id.txtJasa, EditText.class).setText(result.get(0).get("JASA").asString());
                find(R.id.txtDiscount, EditText.class).setText(result.get(0).get("DISCOUNT").asString());
                find(R.id.txtDP, EditText.class).setText(result.get(0).get("DP").asString());
                find(R.id.txtWaktuPesan, EditText.class).setText(result.get(0).get("WAKTU_PESAN").asString());

            }
        });
    }
}

