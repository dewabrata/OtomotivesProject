package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.Checkin2_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class KonfirmasiData_Pembayaran_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi_data_pembayaran);
        initToolbar();
        initComponent();
        viewDataPelanggan();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Konfirmasi Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        setSpinnerFromApi(find(R.id.sp_pekerjaan, Spinner.class), "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataPelanggan();
            }
        });
    }

    private void updateDataPelanggan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("checkinId", getIntentStringExtra(ID));
                args.put("konfirmasi", "DATA PELANGGAN");
                args.put("noPonsel", find(R.id.et_noPonsel, EditText.class).getText().toString());
                args.put("pemilik", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("namaPelanggan", find(R.id.et_namaPelanggan, NikitaAutoComplete.class).getText().toString());
                args.put("pekerjaan", find(R.id.sp_pekerjaan, Spinner.class).getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Nson readData = Nson.readJson(getIntentStringExtra(DATA));
                    readData.set("NO_PONSEL", find(R.id.et_noPonsel, NikitaAutoComplete.class).getText().toString());
                    readData.set("NAMA_PELANGGAN", find(R.id.et_namaPelanggan, NikitaAutoComplete.class).getText().toString());
                    readData.set("PEMILIK", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                    Intent i = new Intent();
                    i.putExtra(DATA, readData.toJson());
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }

    private void viewDataPelanggan(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Konfirmasi Data");
                args.put("checkinId", getIntentStringExtra(ID));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    setSpinnerFromApi(find(R.id.sp_pekerjaan, Spinner.class),
                            "nama",
                            "PEKERJAAN",
                            "viewmst",
                            "PEKERJAAN",
                            result.get("PEKERJAAN").asString());
                    find(R.id.et_namaPelanggan, NikitaAutoComplete.class).setText(result.get("NAMA_PELANGGAN").asString());
                    find(R.id.et_noPonsel, NikitaAutoComplete.class).setText(result.get("NO_PONSEL").asString());
                    if(result.get("PEMILIK").asString().equals("Y")){
                        find(R.id.cb_pemilik, CheckBox.class).setChecked(true);
                    }
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }


}