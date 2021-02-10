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
import android.widget.Toast;

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
import static com.rkrzmail.utils.APIUrls.VIEW_PELANGGAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class KonfirmasiData_Pembayaran_Activity extends AppActivity {

    private int idPelanggan = 0;

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
        find(R.id.et_noPonsel, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.et_noPonsel, EditText.class).removeTextChangedListener(this);
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting < 4 && ! find(R.id.et_noPonsel, EditText.class).getText().toString().contains("+62 ")) {
                    find(R.id.et_noPonsel, EditText.class).setText("+62 ");
                    Selection.setSelection( find(R.id.et_noPonsel, EditText.class).getText(),  find(R.id.et_noPonsel, EditText.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.et_noPonsel, EditText.class).requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
                find(R.id.et_noPonsel, EditText.class).addTextChangedListener(this);
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
                args.put("idPelanggan", String.valueOf(idPelanggan));
                args.put("checkinId", getIntentStringExtra(ID));
                args.put("konfirmasi", "DATA PELANGGAN");
                args.put("noPonsel", formatOnlyNumber(find(R.id.et_noPonsel, EditText.class).getText().toString()));
                args.put("isPemilik", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("namaPelanggan", find(R.id.et_namaPelanggan, NikitaAutoComplete.class).getText().toString());
                args.put("pekerjaan", find(R.id.sp_pekerjaan, Spinner.class).getSelectedItem().toString());
                args.put("nopol", getIntentStringExtra("NOPOL"));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PELANGGAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Nson readData = Nson.readJson(getIntentStringExtra(DATA));
                    readData.set("NO_PONSEL", formatOnlyNumber(find(R.id.et_noPonsel, NikitaAutoComplete.class).getText().toString()));
                    readData.set("NAMA_PELANGGAN", find(R.id.et_namaPelanggan, NikitaAutoComplete.class).getText().toString());
                    readData.set("PEMILIK", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                    Intent i = new Intent();
                    i.putExtra(DATA, readData.toJson());
                    i.putExtra("NO_PONSEL", formatOnlyNumber(find(R.id.et_noPonsel, NikitaAutoComplete.class).getText().toString()));
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    if(result.get("message").asString().contains("Duplicate")){
                        showWarning("NO PONSEL PELANGGAN SUDAH TERDAFTAR", Toast.LENGTH_LONG);
                    }else{
                        showWarning(ERROR_INFO);
                    }
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
                args.put("action", "KONFIRMASI DATA");
                args.put("checkinId", getIntentStringExtra(ID));
                args.put("noPonsel", getIntentStringExtra("NO_PONSEL"));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PELANGGAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    idPelanggan = result.get("ID").asInteger();
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