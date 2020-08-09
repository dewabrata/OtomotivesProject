package com.rkrzmail.oto.modules.registrasi_bengkel;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RegistrasiBengkel_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_REFEREAL = 56;
    private static final int REQUEST_MAPS = 57;
    private static final String TAG = "REHIST___";
    private EditText etKodeRef, etNamaPemilik, etNoPonsel, etEmail, etNamaBengkel, etAlamat, etJabatan;
    private MultiSelectionSpinner spKendaraan, spBidangUsaha, spMerkKendaraan;
    private NikitaAutoComplete etKotaKab;
    private String[] itemsMerk;
    private String typeKendaraan, bidangUsaha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi_bengkel_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Registrasi Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etKodeRef = findViewById(R.id.et_kodeRef_regist);
        etAlamat = findViewById(R.id.et_alamat_regist);
        etEmail = findViewById(R.id.et_email_regist);
        etKotaKab = findViewById(R.id.et_kotakab_regist);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_regist);
        etNamaPemilik = findViewById(R.id.et_cp_regist);
        etNoPonsel = findViewById(R.id.et_noPhone_regist);
        spBidangUsaha = findViewById(R.id.sp_usaha_regist);
        spKendaraan = findViewById(R.id.sp_jenisKendaraan_regist);
        etJabatan = findViewById(R.id.et_jabatan_regist);
        spMerkKendaraan = findViewById(R.id.sp_merkKendaraan_regist);

        minEntryEditText(etNamaPemilik, 5, find(R.id.tl_cp_regist, TextInputLayout.class), "Panjang Nama Min. 5 Karakter");
        minEntryEditText(etNamaBengkel, 8, find(R.id.tl_namaBengkel_regist, TextInputLayout.class), "Nama Bengkel Min. 5 Karakter");
        minEntryEditText(etAlamat, 20, find(R.id.tl_alamat_regist, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");

        etNoPonsel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() < 6) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                } else {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                } else if (!s.toString().contains("+62 ")) {
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                }

            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_email_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains("@")) {
                    find(R.id.tl_email_regist, TextInputLayout.class).setError("Email Tidak Valid");
                } else {
                    find(R.id.tl_email_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });


        String aggrement = "Setuju dengan <font color=#F44336><u> Syarat & kondisi </u></font> pemakain Bengkel Pro";
        find(R.id.tv_setuju_regist, TextView.class).setText(Html.fromHtml(aggrement));
        find(R.id.tv_setuju_regist, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo("Show Aggrement");
            }
        });
        find(R.id.btn_lokasi_regist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        etKotaKab.setLoadingIndicator((ProgressBar) findViewById(R.id.pb_et_kotakab_regist));
        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");

        itemsMerk = new String[]{"HONDA", "YAMAHA", "SUZUKI"};
        spMerkKendaraan.setItems(itemsMerk);
        spMerkKendaraan.setSelection(Arrays.asList(itemsMerk), false);
        spMerkKendaraan.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });

        setMultiSelectionSpinnerFromApi(spKendaraan, "nama", "BENGKEL", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "TYPE", "");
        setMultiSelectionSpinnerFromApi(spBidangUsaha, "nama", "BENGKEL", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "KATEGORI", "TYPE");

        find(R.id.btn_simpan_regist, Button.class).setOnClickListener(this);
        find(R.id.btn_check_regist, Button.class).setOnClickListener(this);
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                //nama, nohp, email, namabengkel, jenis, kategori, alamat, daerah, lokasi
                args.put("action", "add");
                args.put("nohp", etNoPonsel.getText().toString());
                args.put("nama", etNamaPemilik.getText().toString());
                args.put("email", etEmail.getText().toString());
                args.put("nama_bengkel", etNamaBengkel.getText().toString());
                args.put("jenis", spKendaraan.getSelectedItemsAsString());
                //args.put("jabatan", etJabatan.getText().toString());
                args.put("kategori", spBidangUsaha.getSelectedItem().toString());
                args.put("persetujuan", find(R.id.cb_setuju_regist, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("alamat", etAlamat.getText().toString());
                args.put("daerah", etKotaKab.getText().toString());
                //args.put("lokasi", tvLokasi.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("regristrasi"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Registrasi Berhasil");
                    finish();
                } else {
                    showInfo("Registrasi Gagal, Silahkan Cek Data Anda Kembali");
                }
            }
        });
    }

    private boolean dataValidation() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("registrasi"), args));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    nListArray.add(result.get("data").get(i).get("NO_PONSEL"));
                    nListArray.add(result.get("data").get(i).get("NAMA_BENGKEL"));
                    nListArray.add(result.get("data").get(i).get("LOKASI"));
                    nListArray.add(result.get("data").get(i).get("EMAIL"));
                }
            }
        });
        return true;
    }

    @Override
    public void onClick(View view) {
        String info = "Silahkan lengkapi ";
        switch (view.getId()) {
            case R.id.btn_simpan_regist:
                if (etNamaPemilik.getText().toString().isEmpty() || etNamaPemilik.getText().toString().length() < 5) {
                    etNamaPemilik.setError(info + "Nama Pemilik");
                    etNamaPemilik.requestFocus();
                    return;
                }else if (etNoPonsel.getText().toString().isEmpty() || etNoPonsel.getText().toString().length() < 6) {
                    etNoPonsel.setError(info + "No. Ponsel");
                    etNoPonsel.requestFocus();
                    return;
                }else if (etEmail.getText().toString().isEmpty() || !etEmail.getText().toString().contains("@")) {
                    etEmail.setError(info + "Email");
                    etEmail.requestFocus();
                    return;
                }else if (etNamaBengkel.getText().toString().isEmpty() || etNamaBengkel.getText().toString().length() < 8) {
                    etNamaBengkel.setError(info + "Nama Bengkel");
                    etNamaBengkel.requestFocus();
                    return;
                }else if (spKendaraan.getSelectedItemsAsString().equalsIgnoreCase("")) {
                    showInfo(info + "Kendaraan");
                    spKendaraan.requestFocus();
                    return;
                }else if (etKotaKab.getText().toString().isEmpty()) {
                    etKotaKab.setError(info + "Kota / Kab");
                    etKotaKab.requestFocus();
                    return;
                }else if (etAlamat.getText().toString().isEmpty() || etAlamat.getText().toString().length() < 20) {
                    etAlamat.setError(info + "Alamat");
                    etAlamat.requestFocus();
                    return;
                }
                if (!find(R.id.cb_setuju_regist, CheckBox.class).isChecked()) {
                    showInfo("Silahkan Setujui Syarat Dan Ketentuan Aplikasi");
                }else{
                    saveData();
                }


                break;
            case R.id.btn_check_regist:
//                Intent i = new Intent(getActivity(), Referal_Activity.class);
//                startActivityForResult(i, REQUEST_REFEREAL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
