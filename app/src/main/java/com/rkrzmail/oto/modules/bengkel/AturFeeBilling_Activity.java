package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.FEE_BILLING;
import static com.rkrzmail.utils.APIUrls.MST_REK_OTO_V4;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturFeeBilling_Activity extends AppActivity implements View.OnClickListener {

    private EditText etBulan, etTotalFee, etCashBack, etNetFee;
    private Spinner spRekOto;
    private RecyclerView rvDetail;
    AlertDialog dialogDetailFee;

    private final Nson dataRekening = Nson.newArray();
    private final Nson dataDetailFee = Nson.newArray();

    private String namaBank, noRek, namaRek;
    private String expiredTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_fee_biling);
        initToolbar();
        initComponent();
        setComponent();
        viewFee();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Bayar Fee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etBulan = findViewById(R.id.et_bulan);
        etTotalFee = findViewById(R.id.et_total_fee);
        etCashBack = findViewById(R.id.et_cashback);
        etNetFee = findViewById(R.id.et_net_fee);
        spRekOto = findViewById(R.id.sp_rek_oto);
    }

    private void setComponent() {
        setSpRekOto();
        find(R.id.btn_nilai_unik).setOnClickListener(this);
        find(R.id.btn_detail_fee).setOnClickListener(this);
    }

    private void showDialogDetailFee() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);
        dialogDetailFee = builder.create();

        SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);
        initRvDetail(dialogView);
        initToolbarDetail(dialogView);
        viewDetailFee();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_nilai_unik:
                if(namaBank.isEmpty()){
                    setErrorSpinner(spRekOto, "REKENING OTO HARUS DI PILIH");
                }else{
                    generateNilaiUnik();
                }
                break;
            case R.id.btn_detail_fee:
                if (!etBulan.getText().toString().isEmpty()) {
                    showDialogDetailFee();
                } else {
                    showWarning("BULAN FEE ERROR");
                }

                break;
        }
    }

    private void copyToClipBoard(String text) {
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
    }

    private void initRvDetail(View dialogView) {
        rvDetail = dialogView.findViewById(R.id.recyclerView);
        rvDetail.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDetail.setHasFixedSize(true);
        rvDetail.setAdapter(new NikitaRecyclerAdapter(dataDetailFee, R.layout.item_detail_fee_billing) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_no_bukti_bayar, TextView.class)
                        .setText(dataDetailFee.get(position).get("NO_BUKTI_BAYAR").asString());
                viewHolder.find(R.id.tv_transaksi, TextView.class)
                        .setText(RP + NumberFormatUtils.formatRp(dataDetailFee.get(position).get("TRANSAKSI").asString()));
                viewHolder.find(R.id.tv_total_transaksi, TextView.class)
                        .setText(RP + NumberFormatUtils.formatRp(dataDetailFee.get(position).get("GRAND_TOTAL").asString()));
                viewHolder.find(R.id.tv_total_fee, TextView.class)
                        .setText(RP + NumberFormatUtils.formatRp(dataDetailFee.get(position).get("TOTAL_FEE").asString()));
            }
        });
    }

    private void initToolbarDetail(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Fee Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void setSpRekOto() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            Response response;
            @Override
            public void run() {
                RequestBody formBody = new FormEncodingBuilder()
                        .add("CID",UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim())
                        .build();
                try {
                    Request request = new Request.Builder()
                            .url(AppApplication.getBaseUrlV4(MST_REK_OTO_V4))
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
                        List<String> rekList = new ArrayList<>();
                        dataRekening.add("");
                        rekList.add("--PILIH--");
                        result = result.get("data");
                        if (result.size() > 0) {
                            for (int i = 0; i < result.size(); i++) {
                                rekList.add(result.get(i).get("NAMA_BANK").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                                dataRekening.add(Nson.newObject()
                                        .set("NAMA_BANK", result.get(i).get("NAMA_BANK").asString())
                                        .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                                        .set("NAMA_REKENING", result.get(i).get("NAMA_REKENING").asString())
                                );
                            }
                        }
                        ArrayAdapter<String> aktivitasAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, rekList);
                        aktivitasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spRekOto.setAdapter(aktivitasAdapter);
                        spRekOto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if (i != 0) {
                                    namaBank = dataRekening.get(i).get("NAMA_BANK").asString();
                                    noRek = dataRekening.get(i).get("NO_REKENING").asString();
                                    namaRek = dataRekening.get(i).get("NAMA_REKENING").asString();
                                } else {
                                    namaBank = "";
                                    noRek = "";
                                    namaRek = "";
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    showWarning(response.message());
                }
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void generateNilaiUnik() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "nilaiUnik");
                args.put("totalFee", NumberFormatUtils.formatOnlyNumber(etTotalFee.getText().toString()));
                args.put("bulan", etBulan.getText().toString());
                args.put("namaBank", namaBank);
                args.put("noRekBank", noRek);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                } else {
                    showError(result.get("message").asString());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_nilai_unik_billing, null);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();

                Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Bayar Sesuai Nilai Unik");

                final EditText etNilaiUnik = dialogView.findViewById(R.id.et_nilai_unik);
                EditText etTanggalExpired = dialogView.findViewById(R.id.et_tanggal);
                EditText etJamExpired = dialogView.findViewById(R.id.et_jam);
                ImageButton imgCopy = dialogView.findViewById(R.id.img_btn_copy);
                Button btnOk =  dialogView.findViewById(R.id.btn_ok);

                etNilaiUnik.setText(RP + NumberFormatUtils.formatRp(result.get("NILAI_UNIK").asString()));
                expiredTime = DateFormatUtils.formatDate(result.get("EXPIRED_DATE").asString(), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm");
                String[] splitExpired = expiredTime.split(" ");
                etTanggalExpired.setText(splitExpired[0]);
                etJamExpired.setText(splitExpired[1]);

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveData();
                    }
                });

                imgCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyToClipBoard(NumberFormatUtils.formatOnlyNumber(etNilaiUnik.getText().toString()));
                        showSuccess("TOTAL BAYAR BERHASIL DI COPY");
                    }
                });

                if (alertDialog != null) {
                    if (alertDialog.getWindow() != null)
                        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        });
    }


    private void viewDetailFee() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "detailFee");
                args.put("bulan", etBulan.getText().toString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    dataDetailFee.asArray().clear();
                    dataDetailFee.asArray().addAll(result.asArray());
                    if (dialogDetailFee != null) {
                        if (dialogDetailFee.getWindow() != null)
                            dialogDetailFee.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialogDetailFee.show();
                    }
                    Objects.requireNonNull(rvDetail.getAdapter()).notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void viewFee() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        final String bulan = new SimpleDateFormat("MMMM").format(cal.getTime());

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "totalFee");
                args.put("bulan", bulan);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    etBulan.setText(Tools.getBulanByEnglishName(result.get("BULAN").asString()));
                    etTotalFee.setText(RP + NumberFormatUtils.formatRp(result.get("TOTAL_FEE").asString()));
                } else {
                    if(result.get("message").asString().equals("BILLING SUDAH TERBAYAR")){
                        showInfoDialog("BILLING SUDAH TERBAYAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    }else{
                        showError(result.get("message").asString());
                    }

                }
            }
        });
    }

    private void saveData(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("bulan", etBulan.getText().toString());
                args.put("namaBank", namaBank);
                args.put("noRek", noRek);
                args.put("namaRek", namaRek);
                args.put("waktuExpired", expiredTime);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(FEE_BILLING), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

}