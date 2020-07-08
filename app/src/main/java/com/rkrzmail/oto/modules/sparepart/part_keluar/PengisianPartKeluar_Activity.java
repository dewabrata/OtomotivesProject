package com.rkrzmail.oto.modules.sparepart.part_keluar;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;

import java.util.Map;

public class PengisianPartKeluar_Activity extends AppActivity {

    private static final int REQUEST_PART_KELUAR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengisian_part_keluar_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_partKeluar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Part Keluar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        find(R.id.sp_penggunaan_partKeluar, Spinner.class).removeViewsInLayout(0, 0);

        find(R.id.btn_lanjut_partKeluar, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelanjutnya();
            }
        });

        Intent i = getIntent();
        if (i.hasExtra("NAMA")) {

            find(R.id.et_jumlah_partKeluar, EditText.class).setVisibility(View.VISIBLE);
            find(R.id.btn_lanjut_partKeluar, Button.class).setText(getResources().getString(R.string.simpan_uppercase));
            find(R.id.btn_lanjut_partKeluar, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveData();
                }
            });
        }

    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Nson nson = Nson.readJson(getIntentStringExtra("NAMA"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("penggunaan", nson.get("penggunaan").asString());
                args.put("jumlah", find(R.id.et_jumlah_partKeluar, EditText.class).getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(getActivity(), PartKeluar_Activity.class));
                    finish();
                } else {
                    showInfo("Gagal Menambahkan Aktifitas Mohon Di Coba Kembali");
                }
            }
        });

    }


    private void setSelanjutnya() {
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                Nson nson = Nson.newObject();
                nson.set("penggunaan", find(R.id.sp_penggunaan_partKeluar, Spinner.class).getSelectedItem().toString());
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("keluar part", nson.toJson());
                startActivityForResult(i, REQUEST_PART_KELUAR);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PART_KELUAR && resultCode == RESULT_OK) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));

        }
    }
}
