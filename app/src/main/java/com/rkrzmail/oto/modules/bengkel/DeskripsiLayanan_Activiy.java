package com.rkrzmail.oto.modules.bengkel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class DeskripsiLayanan_Activiy extends AppActivity {

    //    private TextView tvNamaLayanan, tvJenisLayanan, tvNamPrincipal, tvPelaksana, tvLokasi, tvKendaraan, tvMerk,
//                    tvJenis, tvVarian, tvModel, tvBatasan, tvBiayaJasa, tvPenggantian, tv
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deskripsi_layanan__activiy);
        initToolbar();
        loadDeskripsi();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Deskripsi Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void loadDeskripsi(){

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if(result.get(i).get("NAMA_LAYANAN").asString().equalsIgnoreCase(getIntentStringExtra("deskripsi"))){
                            find(R.id.tv_namalayanan_deskripsiLayanan, TextView.class).setText(result.get(i).get("NAMA_LAYANAN").asString());
                            find(R.id.tv_jenislayanan_deskripsiLayanan, TextView.class).setText(result.get(i).get("JENIS_LAYANAN").asString());
                            find(R.id.tv_namaPrincipal_deskripsiLayanan, TextView.class).setText(result.get(i).get("PRINCIPAL").asString());

                            find(R.id.tv_pelaksana_deskripsiLayanan, TextView.class).setText(result.get(i).get("").asString());

                            find(R.id.tv_lokasi_deskripsiLayanan, TextView.class).setText(result.get(i).get("").asString());

                            find(R.id.tv_kendaraan_deskripsiLayanan, TextView.class).setText(result.get(i).get("KENDARAAN").asString());
                            find(R.id.tv_merk_deskripsiLayanan, TextView.class).setText(result.get(i).get("MERK").asString());
                            find(R.id.tv_model_deskripsiLayanan, TextView.class).setText(result.get(i).get("MODEL").asString());

                            find(R.id.tv_jenis_deskripsiLayanan, TextView.class).setText(result.get(i).get("").asString());

                            find(R.id.tv_varian_deskripsiLayanan, TextView.class).setText(result.get(i).get("VARIAN").asString());

                            find(R.id.tv_batasFailitas_deskripsiLayanan, TextView.class).setText(result.get(i).get("").asString());
                            find(R.id.tv_biayaJasa_deskripsiLayanan, TextView.class).setText(result.get(i).get("BIAYA_JASA").asString());
                            find(R.id.tv_penggantian_deskripsiLayanan, TextView.class).setText(result.get(i).get("").asString());

                            find(R.id.tv_garansiLayanan_deskripsiLayanan, TextView.class).setText(result.get(i).get("GARANSI").asString());
                            find(R.id.tv_penggantian2_deskripsiLayanan, TextView.class).setText(result.get(i).get("").asString());
                            find(R.id.tv_ex_deskripsiLayanan, TextView.class).setText(result.get(i).get("EXPIRATION_GARANSI_KM").asString());

                            find(R.id.tv_waktuLayanan_deskripsiLayanan, TextView.class).setText(result.get(i).get("WAKTU_LAYANAN_TIPE").asString());
                            find(R.id.tv_jenisAntrian_deskripsiLayanan, TextView.class).setText(result.get(i).get("JENIS_ANTRIAN").asString());
                            find(R.id.tv_finalInspection_deskripsiLayanan, TextView.class).setText(result.get(i).get("FINAL_INSPECTION").asString());
                        }
                    }
                } else {
                    showError("Gagal Menambahkan Layanan");
                }
            }
        });
    }
}
