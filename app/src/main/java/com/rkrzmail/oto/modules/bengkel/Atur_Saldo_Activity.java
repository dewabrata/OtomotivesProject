package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.GET_BALANCE_ALL_USER;
import static com.rkrzmail.utils.APIUrls.GET_BALANCE_KAS_DAN_BANK_BENGKEL;
import static com.rkrzmail.utils.APIUrls.GET_EWALLET;
import static com.rkrzmail.utils.APIUrls.SAVE_SALDO;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Atur_Saldo_Activity extends AppActivity {

    private Nson getData;
    private final Nson dataRekeningList = Nson.newArray();
    private final Nson dataKasir = Nson.newArray();
    private final Nson dataEwallet = Nson.newArray();
    private Nson dataBalanceBengkel = Nson.newArray();

    private String loadKasir = "", loadRekening = "", loadEwallet = "";
    private String noRek = "", namaBank = "";
    private String kasirId = "";
    private String akun;

    private boolean isView = false;
    private boolean isKasir = false;
    private boolean isEwallet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_saldo);
        isKasir = getIntent().getBooleanExtra("KASIR", false);
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
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        find(R.id.container_akun).setVisibility(isKasir ? View.GONE : View.VISIBLE);
        find(R.id.tv_label_saldo, TextView.class).setText(isKasir ? "KASIR" : getResources().getString(R.string.rekening_epay_uppercase));
        getDataUserAndRekening();

        if (!data.asString().isEmpty()) {
            isView = true;
            isEwallet = data.get("AKUN").asString().equals("E-WALLET");
            loadKasir = data.get("NAMA").asString();
            loadEwallet = data.get("NAMA_BANK").asString();
            loadRekening = data.get("NAMA_BANK").asString() + " - " + data.get("NO_REKENING").asString();
            setDisabledView();
            if (!isKasir)
                setSpAkun(data.get("AKUN").asString());

            find(R.id.et_saldo_disesuaikan, EditText.class).setText(RP + NumberFormatUtils.formatRp(data.get("SALDO_PENYESUAIAN").asString()));
            find(R.id.et_saldo_akhir, EditText.class).setText(RP + NumberFormatUtils.formatRp(data.get("SALDO_AKHIR").asString()));
            find(R.id.et_keterangan, EditText.class).setText(data.get("KETERANGAN").asString());
        } else {
            if (!isKasir) {
                setSpAkun("");
                getLastBalanceKas();
            }
        }

        find(R.id.et_saldo_disesuaikan, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_saldo_disesuaikan, EditText.class)));
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int saldoAkhir = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_akhir, EditText.class).getText().toString()));
                int saldoPenyesuaian = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_disesuaikan, EditText.class).getText().toString()));

                if (isKasir) {
                    if (saldoAkhir > 0 && saldoPenyesuaian > saldoAkhir) {
                        find(R.id.et_saldo_disesuaikan, EditText.class).setError("PENYESUAIAN TIDAK VALID");
                        viewFocus(find(R.id.et_saldo_disesuaikan, EditText.class));
                    } else if (saldoPenyesuaian == 0) {
                        find(R.id.et_saldo_disesuaikan, EditText.class).setError("PENYESUAIAN HARUS DI ISI");
                        viewFocus(find(R.id.et_saldo_disesuaikan, EditText.class));
                    } else if (find(R.id.et_keterangan, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_keterangan, EditText.class).setError("KETERANGAN HARUS DI ISI");
                        viewFocus(find(R.id.et_keterangan, EditText.class));
                    } else {
                        saveData();
                    }
                } else {
                    if (akun.equals("--PILIH--")) {
                        setErrorSpinner(find(R.id.sp_akun, Spinner.class), "AKUN HARUS DI PILIH");
                    } else if (akun.equals("BANK") && namaBank.isEmpty()) {
                        setErrorSpinner(find(R.id.sp_norek, Spinner.class), "NO. REKENING HARUS DI PILIH");
                    } else if (akun.equals("KASIR") && kasirId.isEmpty()) {
                        setErrorSpinner(find(R.id.sp_norek, Spinner.class), "USER HARUS DI PILIH");
                    } else if (NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_disesuaikan, EditText.class).getText().toString()).equals("0")) {
                        find(R.id.et_saldo_disesuaikan, EditText.class).setError("PENYESUAIAN HARUS DI ISI");
                        viewFocus(find(R.id.et_saldo_disesuaikan, EditText.class));
                    } else if (akun.equals("KASIR") && saldoAkhir > saldoPenyesuaian) {
                        find(R.id.et_saldo_disesuaikan, EditText.class).setError("SALDO PENYESUAIAN TIDAK BOLEH KURANG DARI SALDO AKHIR");
                        viewFocus(find(R.id.et_saldo_disesuaikan, EditText.class));
                    } else if (find(R.id.et_keterangan, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_keterangan, EditText.class).setError("KETERANGAN HARUS DI ISI");
                        viewFocus(find(R.id.et_keterangan, EditText.class));
                    } else {
                        saveData();
                    }
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
                Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, RelativeLayout.class), (akun.equals("BANK") || akun.equals("KASIR")) && !isView);
                if (!find(R.id.ly_norek, RelativeLayout.class).isEnabled()) {
                    find(R.id.sp_norek, Spinner.class).setSelection(0);
                }

                if (!isView) {
                    if (akun.equals("KAS")) {
                        int balance = dataBalanceBengkel.get("CASH").get("BALANCE").asInteger();
                        find(R.id.et_saldo_akhir, EditText.class).setText(RP + NumberFormatUtils.formatRp(balance));
                    } else {
                        setSpRek("", akun.equals("E-WALLET"));
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getDataUserAndRekening() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                if (isKasir) {
                    String[] args = new String[3];
                    args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                    result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_BALANCE_ALL_USER), args));
                    dataKasir.asArray().clear();
                    dataKasir.add("");
                    dataKasir.asArray().addAll(result.get("data").asArray());
                } else {
                    String[] args1 = new String[3];
                    args1[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                    result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_EWALLET), args1));
                    dataEwallet.asArray().clear();
                    dataEwallet.add("");
                    dataEwallet.add(result.get("data"));

                    Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                    args2.put("action", "view");
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args2));
                    result = result.get("data");
                    dataRekeningList.asArray().clear();
                    dataRekeningList.add("");
                    for (int i = 0; i < result.size(); i++) {
                        dataRekeningList.add(Nson.newObject()
                                .set("ID", result.get(i).get("ID"))
                                .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                                .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                                .set("EDC", result.get(i).get("EDC_ACTIVE"))
                                .set("OFF_US", result.get(i).get("OFF_US"))
                                .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                    }

                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (isKasir)
                    setSpRek(loadKasir, false);
                else
                    setSpRek(loadRekening, isEwallet);
            }
        });

    }

    private void getLastBalanceKas() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_BALANCE_KAS_DAN_BANK_BENGKEL), args));
                dataBalanceBengkel.asArray().clear();
                dataBalanceBengkel.asArray().addAll(result.get("data").asArray());
                dataBalanceBengkel = dataBalanceBengkel.get(0);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {

            }
        });
    }

    private void setSpRek(final String selection, final boolean isEwallet) {
        newProses(new Messagebox.DoubleRunnable() {
            final ArrayList<String> dataList = new ArrayList<>();

            @Override
            public void run() {
                dataList.add("--PILIH--");
                if (isKasir) {
                    for (int i = 0; i < dataKasir.size(); i++) {
                        if (!dataKasir.get(i).asString().isEmpty()) {
                            dataList.add(dataKasir.get(i).get("NAMA").asString());
                        }
                    }
                } else {
                    if (isEwallet) {
                        for (int i = 0; i < dataEwallet.size(); i++) {
                            if (!dataEwallet.get(i).asString().isEmpty()) {
                                dataList.add(dataEwallet.get(i).get("NAMA_EWALLET").asString());
                            }
                        }
                    } else {
                        for (int i = 0; i < dataRekeningList.size(); i++) {
                            if (!dataRekeningList.get(i).asString().isEmpty()) {
                                dataList.add(dataRekeningList.get(i).get("BANK_NAME").asString() + " - " + dataRekeningList.get(i).get("NO_REKENING").asString());
                            }
                        }
                    }
                }
            }

            @Override
            public void runUI() {
                setSpinnerOffline(dataList, find(R.id.sp_norek, Spinner.class), selection);
                if (isEwallet) {
                    find(R.id.sp_norek, Spinner.class).setSelection(1);
                }
            }
        });

        find(R.id.sp_norek, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int balance = 0;
                if (isKasir) {
                    noRek = "";
                    namaBank = "";
                    if (adapterView.getSelectedItem().toString().equals(dataKasir.get(i).get("NAMA").asString())) {
                        kasirId = dataKasir.get(i).get("KASIR_ID").asString();
                        balance = dataKasir.get(i).get("SALDO_KASIR").asInteger();
                    } else {
                        kasirId = "";
                    }
                } else {
                    if (isEwallet) {
                        if (adapterView.getSelectedItem().toString().equals(dataEwallet.get(i).get("NAMA_EWALLET").asString())) {
                            noRek = dataEwallet.get(i).get("MERCHANT_ID").asString();
                            namaBank = dataEwallet.get(i).get("NAMA_EWALLET").asString();
                        } else {
                            noRek = "";
                            namaBank = "";
                        }
                    } else {
                        kasirId = "";
                        if (adapterView.getSelectedItem().toString().equals(dataRekeningList.get(i).get("COMPARISON").asString())) {
                            noRek = dataRekeningList.get(i).get("NO_REKENING").asString();
                            namaBank = dataRekeningList.get(i).get("BANK_NAME").asString();
                        } else {
                            noRek = "";
                            namaBank = "";
                        }
                    }
                    Nson balanceList = dataBalanceBengkel.get("BANK");
                    if (balanceList.size() > 0) {
                        for (int j = 0; j < balanceList.size(); j++) {
                            if (balanceList.get(j).get("NO_REKENING").asString().equals(noRek)) {
                                balance = balanceList.get(j).get("BALANCE").asInteger();
                                break;
                            }
                        }
                    }
                }

                if (i != 0 && !isView) {
                    find(R.id.et_saldo_akhir, EditText.class).setText(RP + NumberFormatUtils.formatRp(balance));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
                        .add("akun", isKasir ? "KASIR" : akun)
                        .add("keterangan", find(R.id.et_keterangan, EditText.class).getText().toString())
                        .add("saldoPenyesuaian", NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_disesuaikan, EditText.class).getText().toString()))
                        .add("saldoAkhir", NumberFormatUtils.formatOnlyNumber(find(R.id.et_saldo_akhir, EditText.class).getText().toString()))
                        .add("noRekening", noRek)
                        .add("namaBank", namaBank)
                        .add("user", UtilityAndroid.getSetting(getApplicationContext(), "user", ""))
                        .add("kasirID", kasirId)
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

}