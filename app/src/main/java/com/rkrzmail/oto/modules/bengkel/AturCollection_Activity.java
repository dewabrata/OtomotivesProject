package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_COLLECTION;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_COLLECTION;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturCollection_Activity extends AppActivity {

    private Nson rekeningList = Nson.newArray();
    private String namaBank = "", noRek = "";
    private String tipeColl = "";
    private String kasirId = "";
    private int sisaTerhutang = 0, setor = 0, terhutang = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_collection);
        initToolbar();
        initComponent();
        loadData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Collection");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.et_jumlah_setor, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_jumlah_setor, EditText.class)));
        find(R.id.et_jumlah_setor, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty() &&
                        !find(R.id.et_total_cash, EditText.class).getText().toString().isEmpty()) {
                    try {
                        setor = Integer.parseInt(formatOnlyNumber(editable.toString()));
                        terhutang = Integer.parseInt(formatOnlyNumber(find(R.id.et_total_cash, EditText.class).getText().toString()));
                        sisaTerhutang = terhutang - setor;
                        find(R.id.et_sisa_cashCollection, EditText.class).setText(RP + formatRp(String.valueOf(sisaTerhutang)));
                    } catch (Exception e) {
                        setor = 0;
                        terhutang = 0;
                    }

                }
            }
        });

        find(R.id.et_namaKasir_cashCollection);
        find(R.id.et_total_cash);

        find(R.id.sp_tipe_cashCollection, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipeColl = parent.getItemAtPosition(position).toString();
                if (tipeColl.equalsIgnoreCase("CASH")) {
                    find(R.id.et_noTrack_cashCollection).setEnabled(false);
                    find(R.id.sp_rek).setEnabled(false);
                } else {
                    find(R.id.et_noTrack_cashCollection).setEnabled(true);
                    find(R.id.sp_rek).setEnabled(true);
                    setSpRek();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        find(R.id.sp_rek, Spinner.class);

        find(R.id.btn_simpan_cashCollection, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipeColl.equals("SETOR TUNAI") && find(R.id.sp_rek, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                    showWarning("No. Rekening Harus di Pilih");
                    find(R.id.sp_rek, Spinner.class).performClick();
                    find(R.id.sp_rek, Spinner.class).requestFocus();
                } else if (tipeColl.equals("SETOR TUNAI") && find(R.id.et_noTrack_cashCollection, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_noTrack_cashCollection, EditText.class).setError("No. Trace Harus di isi");
                    find(R.id.et_noTrack_cashCollection, EditText.class).requestFocus();
                } else if (find(R.id.et_jumlah_setor, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_jumlah_setor, EditText.class).setError("Jumlah Setor Harus di Isi");
                    find(R.id.et_jumlah_setor, EditText.class).requestFocus();
                } else if (setor > terhutang) {
                    find(R.id.et_jumlah_setor, EditText.class).setError("Setoran Tidak Valid");
                    find(R.id.et_jumlah_setor, EditText.class).requestFocus();
                } else {
                    saveData();
                }

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra(DATA));
        kasirId = n.get("KASIR_ID").asString();
        find(R.id.et_namaKasir_cashCollection, EditText.class).setText(n.get("NAMA").asString());
        find(R.id.et_total_cash, EditText.class).setText(RP + formatRp(n.get("SALDO_KASIR").asString()));
        getDataTotal();
    }

    private void saveData() {
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("namaKasir", find(R.id.et_namaKasir_cashCollection, EditText.class).getText().toString());
                args.put("totalTerhutang", String.valueOf(terhutang));
                args.put("totalBayar", String.valueOf(setor));
                args.put("lebihKurang", String.valueOf(sisaTerhutang));
                args.put("kasirId", kasirId);
                args.put("tipeSetoran", tipeColl);
                args.put("namaBank", namaBank);
                args.put("noRekening", noRek);
                args.put("isCollectNonCash", find(R.id.cb_collect_non_cash, CheckBox.class).isChecked() ? "Y" : "N");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_COLLECTION), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Collection Berhasil");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }

    private void getDataTotal() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "totalHarian");
                args.put("userID", kasirId);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_COLLECTION), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                result = result.get("data");
                int totalTf = 0, totalKredit = 0, totalDebet = 0,
                        totalEwallet = 0, totalInv = 0, transaksiTf = 0,
                        transaksiKredit = 0, transaksiDebet = 0, transaksiEwallet = 0, transaksiInv = 0, transaksiCash = 0;

                if (result.size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        switch (result.get(i).get("TIPE_PEMBAYARAN").asString()) {
                            case "TRANSFER":
                                totalTf = result.get(i).get("TOTAL").asInteger();
                                transaksiTf = result.get(i).get("TRANSAKSI").asInteger();
                                break;
                            case "DEBET":
                                totalDebet = result.get(i).get("TOTAL").asInteger();
                                transaksiDebet = result.get(i).get("TRANSAKSI").asInteger();
                                break;
                            case "KREDIT":
                                totalKredit = result.get(i).get("TOTAL").asInteger();
                                transaksiKredit = result.get(i).get("TRANSAKSI").asInteger();
                                break;
                            case "E-WALLET":
                                totalEwallet = result.get(i).get("TOTAL").asInteger();
                                transaksiEwallet = result.get(i).get("TRANSAKSI").asInteger();
                                break;
                            case "INVOICE":
                                totalInv = result.get(i).get("TOTAL").asInteger();
                                transaksiInv = result.get(i).get("TRANSAKSI").asInteger();
                                break;
                            case "CASH":
                                transaksiCash = result.get(i).get("TRANSAKSI").asInteger();
                                break;
                        }
                    }
                }

                find(R.id.et_total_transfer, EditText.class).setText(RP + NumberFormatUtils.formatRp(totalTf));
                find(R.id.et_total_debet, EditText.class).setText(RP + NumberFormatUtils.formatRp(totalDebet));
                find(R.id.et_total_kredit, EditText.class).setText(RP + NumberFormatUtils.formatRp(totalKredit));
                find(R.id.et_total_ewallet, EditText.class).setText(RP + NumberFormatUtils.formatRp(totalEwallet));
                find(R.id.et_total_invoice, EditText.class).setText(RP + NumberFormatUtils.formatRp(totalInv));
                find(R.id.et_transaksi_transfer, EditText.class).setText(String.valueOf(transaksiTf));
                find(R.id.et_transaksi_debet, EditText.class).setText(String.valueOf(transaksiDebet));
                find(R.id.et_transaksi_ewallet, EditText.class).setText(String.valueOf(transaksiEwallet));
                find(R.id.et_transaksi_inv, EditText.class).setText(String.valueOf(transaksiInv));
                find(R.id.et_transaksi_kredit, EditText.class).setText(String.valueOf(transaksiKredit));
                find(R.id.et_transaksi_cash, EditText.class).setText(String.valueOf(transaksiCash));
                find(R.id.et_transaksi_cash, EditText.class).setVisibility(View.GONE);
            }
        });

    }

    public void setSpRek() {
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
                ArrayList<String> str = new ArrayList<>();
                result = result.get("data");
                str.add("--PILIH--");
                rekeningList.add("");
                for (int i = 0; i < result.size(); i++) {
                    rekeningList.add(Nson.newObject()
                            .set("ID", result.get(i).get("ID"))
                            .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                            .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                            .set("EDC", result.get(i).get("EDC_ACTIVE"))
                            .set("OFF_US", result.get(i).get("OFF_US"))
                            .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                    str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                }

                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                find(R.id.sp_rek, Spinner.class).setAdapter(adapter);
            }
        });

        find(R.id.sp_rek, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(rekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = rekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = rekeningList.get(i).get("BANK_NAME").asString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}
