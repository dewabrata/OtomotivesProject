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
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;

import java.util.Map;

public class AturBiayaMekanikActivity extends AppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biaya_mekanik);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("ATUR BIAYA MEKANIK");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mekanik1 = find(R.id.txtMekanik1, EditText.class).getText().toString().replace(" ","").toUpperCase();
                if (mekanik1.equalsIgnoreCase("")){
                    showError("Mekanik 1 Tidak Boleh Kosong");return;
                }else if (find(R.id.txtMekanik2, EditText.class).getText().toString().equalsIgnoreCase("")){
                    showError("Mekanik 2 Tidak Boleh Kosong");return;
                }else if (find(R.id.txtMekanik3, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Mekanik 3 Tidak Boleh Kosong");
                }else if (find(R.id.txtWaktuBayar1, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Waktu Bayar 1 Tidak Boleh Kosong");
                }else if (find(R.id.txtWaktuBayar2, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Waktu Bayar 2 Tidak Boleh Kosong");
                }else if (find(R.id.txtWaktuBayar3, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Waktu Bayar 3 Tidak Boleh Kosong");
                    return;
                }

                Intent intent = new Intent(getActivity(), MenuActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        simpan();
    }


    private void simpan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                String mekanik1 = find(R.id.txtMekanik1, EditText.class).getText().toString();
                mekanik1 = mekanik1.toUpperCase().trim().replace(" ", "");

                String waktu1 = find(R.id.txtWaktuBayar1, EditText.class).getText().toString();
                waktu1 = waktu1.toUpperCase().trim().replace(" ", "");

                String mekanik2 = find(R.id.txtMekanik2, EditText.class).getText().toString();
                mekanik2 = mekanik2.toUpperCase().trim().replace(" ", "");

                String waktu2 = find(R.id.txtWaktuBayar2, EditText.class).getText().toString();
                waktu2 = waktu2.toUpperCase().trim().replace(" ", "");

                String mekanik3 = find(R.id.txtMekanik3, EditText.class).getText().toString();
                mekanik3 = mekanik3.toUpperCase().trim().replace(" ", "");

                String waktu3 = find(R.id.txtWaktuBayar3, EditText.class).getText().toString();
                waktu3 = waktu3.toUpperCase().trim().replace(" ", "");

                args.put("mekanik_1", mekanik1);
                args.put("mekanik_2", mekanik2);
                args.put("mekanik_3", mekanik3);
                args.put("waktu_1", waktu1);
                args.put("waktu_2", waktu2);
                args.put("waktu_3", waktu3);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("biayamekanik.php"), args));
            }

            public void runUI() {

                find(R.id.txtUMK, EditText.class).setText("UPAH MINIMUM KOTA : Rp " + result.get(0).get("UMK").asString());
                find(R.id.txtUpah, EditText.class).setText("UPAH / JAM : Rp " + result.get(0).get("UPAH_MINIM").asString());
            }
        });
    }
}





