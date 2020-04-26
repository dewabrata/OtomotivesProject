package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class PartDetail extends AppActivity {
    final int REQUEST_JUMLAH = 72;
    final int REQUEST_JUMLAH2 = 52;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.part_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("DETAIL");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        find(R.id.tblBatal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        find(R.id.tblLanjut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (getIntentStringExtra("y").equalsIgnoreCase("y")) {
//                    Intent intent1 = new Intent(getActivity(), JumlahPartActivity.class);
//                    intent1.putExtra("DATA2", getIntentStringExtra("DATA2"));
//                    startActivityForResult(intent1, REQUEST_JUMLAH2);
//                } else {
                    Intent intent = new Intent(getActivity(), PilihPartActivity.class);
                    intent.putExtra("DATA", getIntentStringExtra("DATA"));
                    startActivityForResult(intent, REQUEST_JUMLAH);
//                }
            }
        });

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", "helo");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkhistory.php"), args));

            }

            @Override
            public void runUI() {
                if (result.get("HISTORY").asString().equalsIgnoreCase("TRUE")) {
                    showError("");
                }

            }
        });

        Nson result = Nson.readNson(getIntentStringExtra("DATA"));
        Nson result2 = Nson.readNson(getIntentStringExtra("DATA2"));

        if (result.isNson()) {
            if (result.size() >= 1) {


                find(R.id.txtNama, TextView.class).setText(result.get("NAMA").asString());
                find(R.id.txtKeterangan, TextView.class).setText(result.get("PEMBUAT").asString());
                find(R.id.txtStock, TextView.class).setText(result.get("STOCK").asString());
                find(R.id.txtHarga, TextView.class).setText(result.get("HARGA").asString());

                return;
            }
        } else if (result2.isNson()) {
            if (result2.size() >= 1) {

                find(R.id.txtNama, TextView.class).setText(result2.get("NAMA").asString());
                find(R.id.txtKeterangan, TextView.class).setText(result2.get("PEMBUAT").asString());
                find(R.id.txtStock, TextView.class).setText(result2.get("STOCK").asString());
                find(R.id.txtHarga, TextView.class).setText(result2.get("HARGA").asString());

                return;
            }
        }
    }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_JUMLAH && resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("DATA", getIntentStringExtra(data, "DATA"));
                setResult(RESULT_OK, intent);
                finish();
            } else if (requestCode == REQUEST_JUMLAH2 && resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("DATA2", getIntentStringExtra(data, "DATA2"));
                setResult(RESULT_OK, intent);
                finish();
            }
        }


}
