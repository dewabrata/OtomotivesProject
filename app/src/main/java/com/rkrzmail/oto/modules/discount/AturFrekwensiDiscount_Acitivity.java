package com.rkrzmail.oto.modules.discount;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.rkrzmail.utils.APIUrls.DISCOUNT_FREKWENSI;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturFrekwensiDiscount_Acitivity extends AppActivity {

    private EditText etDisc;
    private Button btnLayanan;
    private SpinnerDialog spDialogLayanan;

    private final Nson dataLayananList = Nson.newArray();
    private final ArrayList<String> layananList = new ArrayList<>();

    private String frekwensi = "";
    private int idLayanan = 0;
    private int discountID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_frekwensi_discount);
        initToolbar();
        initComponent();
        loadData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Frekwensi Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDisc = findViewById(R.id.et_disc_freDisc);
        btnLayanan = findViewById(R.id.btn_layanan);
        etDisc.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDisc));
    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        boolean isUpdate = false;
        if (!data.asString().isEmpty()) {
            isUpdate = true;
            discountID = data.get("ID").asInteger();
            btnLayanan.setEnabled(false);
            btnLayanan.setText(data.get("NAMA_LAYANAN").asString());
            etDisc.setText(data.get("DISCOUNT_TRANSAKSI").asString());
        }

        setSpLayanan();
        setSpStatus(data.get("STATUS").asString());
        setSpFrekwensi(data.get("FREKWENSI").asString());

        final boolean finalIsUpdate = isUpdate;
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalIsUpdate) {
                    if (frekwensi.isEmpty()) {
                       setErrorSpinner(find(R.id.sp_frekwensi, Spinner.class), "FREKWENSI HARUS DI PILIH");
                    } else if (etDisc.getText().toString().isEmpty()) {
                        etDisc.setError("HARUS DI ISI");
                        viewFocus(etDisc);
                    } else {
                        saveData(true);
                    }
                } else {
                    if (btnLayanan.getText().toString().equals("--PILIH--")) {
                        btnLayanan.setError("HARUD DI PILIH");
                        viewFocus(btnLayanan);
                    } else if (frekwensi.isEmpty()) {
                        setErrorSpinner(find(R.id.sp_frekwensi, Spinner.class), "FREKWENSI HARUS DI PILIH");
                    }  else if (etDisc.getText().toString().isEmpty()) {
                        etDisc.setError("HARUS DI ISI");
                        viewFocus(etDisc);
                    } else {
                        saveData(false);
                    }
                }

            }
        });

    }

    private void setSpFrekwensi(final String selection){
        List<String> frekwensiList = new ArrayList<>();
        frekwensiList.add("--PILIH--");
        for (int i = 1; i <= 10; i++) {
            frekwensiList.add(String.valueOf(i));
        }

        setSpinnerOffline(frekwensiList, find(R.id.sp_frekwensi, Spinner.class), selection);
        find(R.id.sp_frekwensi, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    frekwensi = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSpStatus(String status) {
        List<String> statusList = new ArrayList<>();
        statusList.add("TIDAK AKTIF");
        statusList.add("AKTIF");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_status, Spinner.class).setAdapter(statusAdapter);
        if (!status.isEmpty()) {
            for (int i = 0; i < find(R.id.sp_status, Spinner.class).getCount(); i++) {
                if (find(R.id.sp_status, Spinner.class).getItemAtPosition(i).equals(status)) {
                    find(R.id.sp_status, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
    }

    private void setSpLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "BENGKEL");
                args.put("layanan", "DISCOUNT LAYANAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        dataLayananList.add(Nson.newObject()
                                .set("ID", result.get(i).get("LAYANAN_ID").asString())
                                .set("NAMA_LAYANAN", result.get(i).get("NAMA_LAYANAN").asString()));
                        layananList.add(result.get(i).get("NAMA_LAYANAN").asString());
                    }

                    spDialogLayanan = new SpinnerDialog(getActivity(), layananList, "PILIH JENIS LAYANAN");
                    spDialogLayanan.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int position) {
                            if (item.equals(dataLayananList.get(position).get("NAMA_LAYANAN").asString())) {
                                idLayanan = dataLayananList.get(position).get("ID").asInteger();
                                btnLayanan.setText(item);
                            }
                        }
                    });

                    btnLayanan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spDialogLayanan.showSpinerDialog();
                        }
                    });

                } else {
                    Messagebox.showDialog(getActivity(), "Konfirmasi", "Layanan Gagal di Muat, Muat Ulang?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpLayanan();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    showError(ERROR_INFO);
                }
            }
        });
    }


    private void saveData(final boolean isUpdate) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                if (isUpdate) {
                    args.put("action", "update");
                } else {
                    args.put("action", "add");
                }

                args.put("discountID", String.valueOf(discountID));
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("layananID", String.valueOf(idLayanan));
                args.put("namaLayanan", btnLayanan.getText().toString());
                args.put("frekwensi", frekwensi);
                args.put("diskonTransaksi", NumberFormatUtils.clearPercent(etDisc.getText().toString()));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_FREKWENSI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("BERHASIL MENYIMPAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }
}
