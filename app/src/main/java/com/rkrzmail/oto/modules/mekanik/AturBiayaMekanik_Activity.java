package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.BIAYA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturBiayaMekanik_Activity extends AppActivity {

    private final int average = 160;
    private int upahPerJam = 0, umk = 0;
    private int biayaMekanikID = 0;
    private String daerah = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_biaya_mekanik);
        initToolbar();
        initComponent();
        viewUMK();
        viewBiayaMekanik();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Biaya Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.et_mekanik1_biayaMekanik, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_mekanik1_biayaMekanik, EditText.class)));
        find(R.id.et_mekanik2_biayaMekanik, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_mekanik2_biayaMekanik, EditText.class)));
        find(R.id.et_mekanik3_biayaMekanik, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_mekanik3_biayaMekanik, EditText.class)));

        find(R.id.btn_simpan_biayaMekanik).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.et_mekanik1_biayaMekanik, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_mekanik1_biayaMekanik, EditText.class).setError("Harus Di isi");
                    find(R.id.et_mekanik1_biayaMekanik, EditText.class).requestFocus();
                } else if (find(R.id.et_mekanik2_biayaMekanik, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_mekanik2_biayaMekanik, EditText.class).setError("Harus Di isi");
                    find(R.id.et_mekanik2_biayaMekanik, EditText.class).requestFocus();
                } else if (find(R.id.et_mekanik3_biayaMekanik, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_mekanik3_biayaMekanik, EditText.class).setError("Harus Di isi");
                    find(R.id.et_mekanik3_biayaMekanik, EditText.class).requestFocus();
                } else {
                    saveData();
                }

            }
        });
    }


    private void viewUMK() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("nama", "UMK");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    umk = result.get("UMK").asInteger();
                    if (umk > 0) {
                        upahPerJam = umk / average;
                        find(R.id.et_upahMin_biayaMekanik, EditText.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(umk)));
                        find(R.id.et_upahJam_biayaMekanik, EditText.class).setText((RP + NumberFormatUtils.formatRp(String.valueOf(upahPerJam))));
                    }
                } else {
                    viewUMK();
                }
            }
        });
    }

    private void viewBiayaMekanik() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(BIAYA_MEKANIK), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (!result.asArray().isEmpty()) {
                        result = result.get(0);
                        biayaMekanikID = result.get("ID").asInteger();
                        upahPerJam = result.get("UPAH_MINIM").asInteger();
                        if(upahPerJam == 0){
                            upahPerJam = umk / average;
                        }

                        find(R.id.et_mekanik1_biayaMekanik, EditText.class).setText(RP + NumberFormatUtils.formatRp(result.get("MEKANIK_1").asString()));
                        find(R.id.et_mekanik2_biayaMekanik, EditText.class).setText(RP + NumberFormatUtils.formatRp(result.get("MEKANIK_2").asString()));
                        find(R.id.et_mekanik3_biayaMekanik, EditText.class).setText(RP + NumberFormatUtils.formatRp(result.get("MEKANIK_3").asString()));
                        find(R.id.et_minBayar_biayaMekanik, EditText.class).setText(result.get("MIN_WAKTU_BERBAYAR").asString());
                        find(R.id.et_upahMin_biayaMekanik, EditText.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(umk)));
                        find(R.id.et_upahJam_biayaMekanik, EditText.class).setText((RP + NumberFormatUtils.formatRp(String.valueOf(upahPerJam))));
                    } else {
                        viewUMK();
                    }
                } else {
                    viewBiayaMekanik();
                }
            }
        });
    }


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String mekanik1 = find(R.id.et_mekanik1_biayaMekanik, EditText.class).getText().toString();
                String mekanik2 = find(R.id.et_mekanik2_biayaMekanik, EditText.class).getText().toString();
                String mekanik3 = find(R.id.et_mekanik3_biayaMekanik, EditText.class).getText().toString();

                args.put("action", "add");
                args.put("biayaMekanikID", String.valueOf(biayaMekanikID));
                args.put("mekanik1", NumberFormatUtils.formatOnlyNumber(mekanik1));
                args.put("mekanik2", NumberFormatUtils.formatOnlyNumber(mekanik2));
                args.put("mekanik3", NumberFormatUtils.formatOnlyNumber(mekanik3));
                args.put("minWaktuBerbayar", find(R.id.et_minBayar_biayaMekanik, EditText.class).getText().toString());
                args.put("umk", String.valueOf(umk));
                args.put("upahPerJam", String.valueOf(upahPerJam));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(BIAYA_MEKANIK), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("SUKSES MEMPERBAHARUI AKTIFITAS");
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }
}