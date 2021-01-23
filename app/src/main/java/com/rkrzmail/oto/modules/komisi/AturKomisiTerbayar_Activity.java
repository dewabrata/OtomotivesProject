package com.rkrzmail.oto.modules.komisi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KOMISI_PART;
import static com.rkrzmail.utils.APIUrls.PEMBAYARAN_KOMISI;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturKomisiTerbayar_Activity extends AppActivity {

    Spinner spUser;
    EditText etBalance, etJumlahBayar, etBalanceAkhir, etUserPenerima;

    private Nson userData = Nson.newArray();
    private String namaUser = "", userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_komisi_terbayar);
        initToolbar();
        initComponent();
        setSpUser();
        initListerner();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komisi Terbayar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spUser = findViewById(R.id.sp_nama_user);
        etBalance = findViewById(R.id.et_balance);
        etJumlahBayar = findViewById(R.id.et_jumlah_bayar);
        etBalanceAkhir = findViewById(R.id.et_balance_akhir);
        etUserPenerima = findViewById(R.id.et_user_penerima);
    }

    @SuppressLint("SetTextI18n")
    private void initListerner() {
        etJumlahBayar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) return;

                text = NumberFormatUtils.formatOnlyNumber(text);
                if (!etBalance.getText().toString().isEmpty()) {
                    int balanceAwal = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etBalance.getText().toString()));
                    int totalBayar = Integer.parseInt(text);
                    int balanceAkhir = balanceAwal - totalBayar;

                    etBalanceAkhir.setText(RP + formatRp(String.valueOf(balanceAkhir)));
                }

            }
        });

        spUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                namaUser = adapterView.getSelectedItem().toString();
                if (namaUser.equals(userData.get(i).get("NAMA").asString())) {
                    userId = userData.get(i).get("USER_ID").asString();
                    getBalanceUser();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etJumlahBayar.getText().toString().isEmpty()) {
                    etJumlahBayar.setError("JUMLAH BAYAR HARUS DI ISI");
                } else {
                    saveData();
                }
            }
        });

        find(R.id.img_scan_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });
    }

    private void setSpUser() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "USER");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    final List<String> userList = new ArrayList<>();
                    userList.add("--PILIH--");
                    result = result.get("data");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            userData.add(Nson.newObject()
                                    .set("USER_ID", result.get(i).get("NO_PONSEL").asString())
                                    .set("NAMA", result.get(i).get("NAMA").asString())
                            );
                            userList.add(result.get(i).get("NAMA").asString());
                        }
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, userList) {
                    };
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spUser.setAdapter(spinnerAdapter);
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }

    private void getBalanceUser() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "VIEW BALANCE");
                args.put("idUser", userId);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PEMBAYARAN_KOMISI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    etBalance.setText(result.get("BALANCE").asString());

                } else {
                    showError(ERROR_INFO);
                }
            }
        });

    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("idUser", userId);
                args.put("sisaBalance", NumberFormatUtils.formatOnlyNumber(etBalanceAkhir.getText().toString()));
                args.put("totalBayar", NumberFormatUtils.formatOnlyNumber(etJumlahBayar.getText().toString()));
                args.put("balance", NumberFormatUtils.formatOnlyNumber(etBalanceAkhir.getText().toString()));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PEMBAYARAN_KOMISI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("BERHASIL MENYIMPAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String barcodeResult = data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "";
            MyCode.checkMyCode(this, barcodeResult, new MyCode.RunnableWD() {
                @Override
                public void runWD(Nson nson) {
                    if (nson.get("status").asString().equals("OK")) {
                        if (nson.get("data").asArray().isEmpty()) {
                            showWarning("SCAN BARCODE TIDAK VALID", Toast.LENGTH_LONG);
                            return;
                        }
                        nson = nson.get("data").get(0);
                        etUserPenerima.setText(nson.get("NAMA").asString());
                    } else {
                        showError(ERROR_INFO);
                    }
                }
            });
        }
    }
}