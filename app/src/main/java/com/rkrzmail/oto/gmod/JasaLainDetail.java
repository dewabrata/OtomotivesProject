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

public class JasaLainDetail extends AppActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jasalain_detail);
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
                Intent intent = new Intent(getActivity(), PilihPartActivity.class);
                intent.putExtra("DATA", getIntentStringExtra("DATA"));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "helo");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("jasalain.php"), args));

            }

            @Override
            public void runUI() {
                if (result.get("JASALAIN").asString().equalsIgnoreCase("TRUE")) {
                    showError("");
                }

            }
        });

        Nson result = Nson.readNson( getIntentStringExtra("DATA"));

        if (result.isNson()) {
            if (result.size() >= 1) {

//        TextView textView= findViewById(R.id.txtNamaJasaLain);
//        textView.setText(nson.get("NAMA").asString());

                find(R.id.txtNamaJasaLain, TextView.class).setText(result.get("NAMA").asString());
                find(R.id.txtWaktu, TextView.class).setText(result.get("WAKTU").asString());
                find(R.id.txtJasa, TextView.class).setText(result.get("JASA").asString());

                return;
            }
        }


//        TextView text= findViewById(R.id.txtWaktu);
//        text.setText("Waktunya   : " + nson);
//        TextView te= findViewById(R.id.txtJasa);
//        te.setText("Namanya   : " + nson);

    }

}
