package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.FEE_BILLING;
import static com.rkrzmail.utils.APIUrls.JURNAL_KAS;
import static com.rkrzmail.utils.APIUrls.SAVE_REFFERAL;
import static com.rkrzmail.utils.APIUrls.SAVE_SALDO;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Atur_Saldo_Activity extends AppActivity {

    private Nson getData, dataRekeningList = Nson.newArray();
    private String noRek = "", namaBank = "";
    private String akun;

    private boolean isView = false;
    private int saldoSize = 0;
    private int lastBalanceKas = 0, lastBalanceKasBank = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_saldo);
        initToolbar();
        initData();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Penyesuain Saldo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        saldoSize = getIntentIntegerExtra("SALDO_SIZE");
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (!data.asString().isEmpty()) {
            isView = true;
            setDisabledView();
            setSpAkun(data.get("AKUN").asString());
            setSpRek(data.get("NAMA_BANK").asString() + " - " + data.get("NO_REKENING").asString());
            find(R.id.et_saldo_disesuaikan, EditText.class).setText(RP + NumberFormatUtils.formatRp(data.get("SALDO_PENYESUAIAN").asString()));
            find(R.id.et_saldo_akhir, EditText.class).setText(RP + NumberFormatUtils.formatRp(data.get("SALDO_AKHIR").asString()));
            find(R.id.et_keterangan, EditText.class).setText(data.get("KETERANGAN").asString());
        }else{
            setSpAkun("");
            setSpRek("");
        }

        find(R.id.et_saldo_disesuaikan, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_saldo_disesuaikan, EditText.class)));
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (akun.equals("--PILIH--")) {
                    setErrorSpinner(find(R.id.sp_akun, Spinner.class), "AKUN HARUS DI PILIH");
                } else if (akun.equals("BANK") && namaBank.isEmpty()) {
                    setErrorSpinner(find(R.id.sp_norek, Spinner.class), "NO. REKENING HARUS DI PILIH");
                } else if (NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_disesuaikan, EditText.class).getText().toString()).equals("0")) {
                    find(R.id.et_saldo_disesuaikan, EditText.class).setError("PENYESUAIAN HARUS DI ISI");
                    viewFocus(find(R.id.et_saldo_disesuaikan, EditText.class));
                } else if (find(R.id.et_keterangan, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_keterangan, EditText.class).setError("KETERANGAN HARUS DI ISI");
                    viewFocus(find(R.id.et_keterangan, EditText.class));
                } else {
                    saveData();
                }
            }
        });
    }

    private void setDisabledView() {
        Tools.setViewAndChildrenEnabled(find(R.id.ly_akun, RelativeLayout.class), !isView);
        find(R.id.et_saldo_disesuaikan, EditText.class).setEnabled(!isView);
        findViewById(R.id.btn_simpan).setVisibility(View.GONE);
        find(R.id.et_keterangan, EditText.class).setEnabled(!isView);
    }

    private void setSpAkun(String selection) {
        List<String> akunList = Arrays.asList("--PILIH--", "KAS", "BANK", "E-WALLET");
        setSpinnerOffline(akunList, find(R.id.sp_akun, Spinner.class), selection);
        find(R.id.sp_akun, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                akun = parent.getItemAtPosition(position).toString();

                Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, RelativeLayout.class), akun.equals("BANK") && !isView);
                if (!find(R.id.ly_norek, RelativeLayout.class).isEnabled()) {
                    find(R.id.sp_norek, Spinner.class).setSelection(0);
                }

                if (position != 0 && !isView) {
                    getLastBalanceKas(noRek, akun);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            Response response;

            @Override
            public void run() {
                RequestBody formBody = new FormEncodingBuilder()
                        .add("CID", UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim())
                        .add("akun", akun)
                        .add("keterangan", find(R.id.et_keterangan, EditText.class).getText().toString())
                        .add("saldoPenyesuaian", NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_disesuaikan, EditText.class).getText().toString()))
                        .add("saldoAkhir", NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_akhir, EditText.class).getText().toString()))
                        .add("saldoSize", String.valueOf(saldoSize))
                        .add("noRekening", noRek)
                        .add("namaBank", namaBank)
                        .add("user", UtilityAndroid.getSetting(getApplicationContext(), "user", ""))
                        .build();
                try {
                    Request request = new Request.Builder()
                            .url(AppApplication.getBaseUrlV4(SAVE_SALDO))
                            .post(formBody)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    response = client.newCall(request).execute();
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showError(e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void runUI() {
                if (response.isSuccessful()) {
                    try {
                        result = Nson.readJson(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showSuccess("SUKSES MENYIMPAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(response.body().toString());
                }
            }
        });
    }

    private void getLastBalanceKas(final String noRek, final String jenisAkun) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                args[1] = "noRekeningInternal=" + noRek;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL_KAS), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    lastBalanceKas = result.get("data").get("BALANCE_KAS").asInteger();
                    lastBalanceKasBank = result.get("data").get("BALANCE_KAS_BANK").asInteger();
                    if(jenisAkun.equals("BANK")){
                        if (lastBalanceKasBank > 0) {
                            find(R.id.et_saldo_akhir, EditText.class).setText(RP + NumberFormatUtils.formatRp(lastBalanceKasBank));
                        }
                    }else{
                        if (lastBalanceKas > 0) {
                            find(R.id.et_saldo_akhir, EditText.class).setText(RP + NumberFormatUtils.formatRp(lastBalanceKas));
                        }
                    }

                }
            }
        });
    }


    public void setSpRek(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    ArrayList<String> str = new ArrayList<>();
                    result = result.get("data");
                    str.add("--PILIH--");
                    dataRekeningList.add("");
                    for (int i = 0; i < result.size(); i++) {
                        dataRekeningList.add(Nson.newObject()
                                .set("ID", result.get(i).get("ID"))
                                .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                                .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                                .set("EDC", result.get(i).get("EDC_ACTIVE"))
                                .set("OFF_US", result.get(i).get("OFF_US"))
                                .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                        str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                    }
                    setSpinnerOffline(str, find(R.id.sp_norek, Spinner.class), selection);
                } else {
                    showError("GAGAL MEMUAT DATA BANK INTERNAL");
                }
            }
        });

        find(R.id.sp_norek, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(dataRekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = dataRekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = dataRekeningList.get(i).get("BANK_NAME").asString();
                    if(!isView) getLastBalanceKas(noRek, "BANK");
                } else {
                    noRek = "";
                    namaBank = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}