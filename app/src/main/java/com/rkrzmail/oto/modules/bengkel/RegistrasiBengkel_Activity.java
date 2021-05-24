package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.LoginActivity;
import com.rkrzmail.oto.modules.MapPicker_Dialog;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.CHECK_REFFERAL;
import static com.rkrzmail.utils.APIUrls.SET_REGISTRASI;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_PONSEL;

public class RegistrasiBengkel_Activity extends AppActivity implements View.OnClickListener, MapPicker_Dialog.GetLocation {

    private static final int REQUEST_REFEREAL = 56;
    private static final int REQUEST_MAPS = 57;
    private static final String TAG = "REHIST___";
    private EditText etKontakPerson, etNoPonsel, etEmail, etNamaBengkel, etAlamat, etJabatan;
    private MultiSelectionSpinner spBidangUsaha, spMerkKendaraan;
    private Spinner spKendaraan, spKodeRefferal;
    private NikitaAutoComplete etKotaKab;

    private String[] itemsMerk;
    private String typeKendaraan, bidangUsaha = "";
    private boolean isKategori, isRegist = false;

    private String latitude = "", longitude = "";
    private String noPonselReferee = "";
    private String kodeRefferal = "";

    private final List<String> motorList = new ArrayList<>();
    private final List<String> mobilList = new ArrayList<>();
    private final List<String> allList = new ArrayList<>();
    private final List<String> noHpList = new ArrayList<>();
    private final List<String> merkMotorList = new ArrayList<>();
    private final List<String> merkMobilList = new ArrayList<>();
    private final List<String> allMerkList = new ArrayList<>();

    private StringBuilder merkBuilder = new StringBuilder();
    private StringBuilder bidangUsahaBuilder = new StringBuilder();

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi_bengkel);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Registrasi Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        spKodeRefferal = findViewById(R.id.sp_kode_refferal);
        etAlamat = findViewById(R.id.et_alamat_regist);
        etEmail = findViewById(R.id.et_email_regist);
        etKotaKab = findViewById(R.id.et_kotakab_regist);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_regist);
        etKontakPerson = findViewById(R.id.et_cp_regist);
        etNoPonsel = findViewById(R.id.et_noPhone_regist);
        spBidangUsaha = findViewById(R.id.sp_usaha_regist);
        spKendaraan = findViewById(R.id.sp_jenisKendaraan_regist);
        etJabatan = findViewById(R.id.et_jabatan_regist);
        spMerkKendaraan = findViewById(R.id.sp_merkKendaraan_regist);

        etNoPonsel.setText("6281381768836");
        minEntryEditText(etKontakPerson, 5, find(R.id.tl_cp_regist, TextInputLayout.class), "Panjang Nama Min. 5 Karakter");
        minEntryEditText(etNamaBengkel, 8, find(R.id.tl_namaBengkel_regist, TextInputLayout.class), "Nama Bengkel Min. 5 Karakter");
        minEntryEditText(etAlamat, 20, find(R.id.tl_alamat_regist, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");
        getNoPonsel();
        setSpKendaraan("");

        if (getIntent().hasExtra("NO_PONSEL")) {
            etNoPonsel.setEnabled(false);
            etNoPonsel.setText("+" + getIntentStringExtra("NO_PONSEL"));
        }
        etNoPonsel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                } else if (counting < 4) {
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    etNoPonsel.requestFocus();
                } else {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noHpList.size() > 0) {
                    String noHp = etNoPonsel.getText().toString().replaceAll("[^0-9]+", "");
                    for (String no : noHpList) {
                        if (noHp.equalsIgnoreCase(no)) {
                            find(R.id.tl_nohp_regist, TextInputLayout.class).setError("No. Hp Sudah Terdaftar");
                        }
                    }
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

        final MapPicker_Dialog mapPicker_dialog = new MapPicker_Dialog();
        mapPicker_dialog.getBengkelLocation(this);
        find(R.id.btn_lokasi_regist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPicker_dialog.show(getSupportFragmentManager(), null);
            }
        });

        etKotaKab.setLoadingIndicator((ProgressBar) findViewById(R.id.pb_et_kotakab_regist));
        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");

        Log.d(TAG, "initComponent: " + motorList.size());
        find(R.id.btn_simpan_regist, Button.class).setOnClickListener(this);
        getRefferal();
    }

    private void setSpKendaraan(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    List<String> kendaraanList = new ArrayList<>();
                    kendaraanList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        if (!kendaraanList.contains(result.get(i).get("TYPE").asString())) {
                            kendaraanList.add(result.get(i).get("TYPE").asString());
                        }
                    }
                    ArrayAdapter<String> kendaraanAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, kendaraanList);
                    kendaraanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spKendaraan.setAdapter(kendaraanAdapter);
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spKendaraan.getCount(); i++) {
                            if (spKendaraan.getItemAtPosition(i).toString().equals(selection)) {
                                spKendaraan.setSelection(i);
                                break;
                            }
                        }
                    }
                    spKendaraan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            motorList.clear();
                            mobilList.clear();
                            allList.clear();
                            merkMobilList.clear();
                            merkMotorList.clear();
                            allMerkList.clear();
                            if (spKendaraan.getItemAtPosition(i).toString().contains("MOTOR")) {
                                isKategori = true;
                            } else if (spKendaraan.getItemAtPosition(i).toString().contains("MOBIL")) {
                                isKategori = false;
                            } else {
                                count++;
                            }
                            if (i != 0) {
                                setSpBidangUsaha(null);
                                setSpMerkKendaraan(spKendaraan.getItemAtPosition(i).toString());
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else {
                    setSpKendaraan("");
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
                args.put("nohp", etNoPonsel.getText().toString().replaceAll("[^0-9]+", ""));
                args.put("nama", etKontakPerson.getText().toString());
                args.put("email", etEmail.getText().toString());
                args.put("nama_bengkel", etNamaBengkel.getText().toString());
                args.put("jenis", spKendaraan.getSelectedItem().toString().trim());
                args.put("kategori", spBidangUsaha.getSelectedItemsAsString());
                args.put("persetujuan", find(R.id.cb_setuju_regist, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("alamat", etAlamat.getText().toString());
                args.put("daerah", etKotaKab.getText().toString());
                args.put("merk_kendaraan", spMerkKendaraan.getSelectedItemsAsString());
                args.put("tanggal_regist", currentDateTime());
                args.put("tanggal_aktif", currentDateTime());
                args.put("kodeRefferal", kodeRefferal);
                args.put("noPonselReferee", noPonselReferee);
                args.put("latitudeLokasi", latitude);
                args.put("longitudeLokasi", longitude);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REGISTRASI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Registrasi Berhasil");
                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.putExtra("NO_PONSEL", etNoPonsel.getText().toString().replaceAll("[^0-9]+", ""));
                    startActivity(i);
                    finish();
                } else {
                    if (result.get("message").asString().toLowerCase().contains("duplicate")) {
                        showError("No Ponsel Sudah Terdaftar");
                        etNoPonsel.setText("");
                        etNoPonsel.requestFocus();
                    } else {
                        showError("Registrasi Gagal, Silahkan Cek Data Anda Kembali");
                    }
                }
            }
        });
    }

    private void getNoPonsel() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "ALL");
                args.put("CID", "KOSONG");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_NOMOR_PONSEL), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        noHpList.add(result.get("data").get(i).get("NO_PONSEL").asString());
                    }
                    Log.d(TAG, "getNoPonsel: " + noHpList);
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        String info = "Silahkan lengkapi ";
        switch (view.getId()) {
            case R.id.btn_simpan_regist:
                if (etKontakPerson.getText().toString().isEmpty() || etKontakPerson.getText().toString().length() < 5
                        || find(R.id.tl_cp_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etKontakPerson.setError(info + "Nama Pemilik");
                    etKontakPerson.requestFocus();
                    return;
                }
                if (etNoPonsel.getText().toString().isEmpty() || etNoPonsel.getText().toString().length() < 6
                        || find(R.id.tl_nohp_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etNoPonsel.setError(info + "No. Ponsel");
                    etNoPonsel.requestFocus();
                    return;
                }
                if (etEmail.getText().toString().isEmpty() || !etEmail.getText().toString().contains("@") ||
                        find(R.id.tl_email_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etEmail.setError(info + "Email");
                    etEmail.requestFocus();
                    return;
                }
                if (etNamaBengkel.getText().toString().isEmpty() || etNamaBengkel.getText().toString().length() < 8 ||
                        find(R.id.tl_namaBengkel_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etNamaBengkel.setError(info + "Nama Bengkel");
                    etNamaBengkel.requestFocus();
                    return;
                }
                if (spKendaraan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showInfo(info + "Kendaraan");
                    spKendaraan.requestFocus();
                    return;
                }
                if (etKotaKab.getText().toString().isEmpty()) {
                    etKotaKab.setError(info + "Kota / Kab");
                    etKotaKab.requestFocus();
                    return;
                }
                if (etAlamat.getText().toString().isEmpty() || etAlamat.getText().toString().length() < 20 ||
                        find(R.id.tl_alamat_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etAlamat.setError(info + "Alamat");
                    etAlamat.requestFocus();
                    return;
                }
                if (!find(R.id.cb_setuju_regist, CheckBox.class).isChecked()) {
                    showInfo("Silahkan Setujui Syarat Dan Ketentuan Aplikasi");
                    return;
                }

                saveData();

                break;
        }
    }

    private void setSpMerkKendaraan(final String jenisKendaraan) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("CID", "CID");
                args.put("flag", "Merk");
                args.put("jenisKendaraan", jenisKendaraan);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    if (result.get("data").get(i).get("TYPE").asString().equalsIgnoreCase("MOTOR")) {
                        merkMotorList.add(result.get("data").get(i).get("MERK").asString());
                    } else if (result.get("data").get(i).get("TYPE").asString().equalsIgnoreCase("MOBIL")) {
                        merkMobilList.add(result.get("data").get(i).get("MERK").asString());
                    }
                }

                try {
                    Log.d(TAG, "runUI: " + allMerkList);
                    spMerkKendaraan.setItems(isKategori ? merkMotorList : merkMobilList);
                    spMerkKendaraan.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedIndices(List<Integer> indices) {

                        }

                        @Override
                        public void selectedStrings(List<String> strings) {
                            if(strings.size() > 0){
                                merkBuilder = new StringBuilder();
                                for (int i = 0; i < strings.size(); i++) {
                                    merkBuilder.append(strings.get(i)).append(", ");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    showWarning("Perlu di Muat Ulang Merk Kendaraan");
                }
            }
        });
    }

    private void getRefferal() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + NumberFormatUtils.formatOnlyNumber(etNoPonsel.getText().toString());
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(CHECK_REFFERAL), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                    final Nson dataRefferalList = Nson.newArray();
                    List<String> kodeList = new ArrayList<>();
                    kodeList.add("--PILIH--");
                    dataRefferalList.add("");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if(kodeList.size() > 0){
                                for (int j = 0; j < kodeList.size(); j++) {
                                    String referee = result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString();
                                    if(!kodeList.get(i).equals(referee)){
                                        kodeList.add(result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString());
                                        dataRefferalList.add(Nson.newObject()
                                                .set("COMPARISON", result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString())
                                                .set("NO_REFERRAL", result.get(i).get("NO_REFERRAL").asString())
                                                .set("NO_PONSEL_REFEREE", result.get(i).get("NO_PONSEL_REFEREE").asString())
                                                .set("KONTAK_PERSON", result.get(i).get("KONTAK_PERSON").asString())
                                                .set("JABATAN", result.get(i).get("JABATAN").asString())
                                                .set("NAMA_BENGKEL", result.get(i).get("NAMA_BENGKEL").asString())
                                                .set("ALAMAT", result.get(i).get("ALAMAT").asString())
                                                .set("KOTA_KAB", result.get(i).get("KOTA_KAB").asString())
                                                .set("BIDANG_USAHA", result.get(i).get("BIDANG_USAHA").asString())
                                                .set("JENIS_KENDARAAN", result.get(i).get("JENIS_KENDARAAN").asString())
                                        );
                                        break;
                                    }
                                }
                            }else{
                                dataRefferalList.add(Nson.newObject()
                                        .set("COMPARISON", result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString())
                                        .set("NO_REFERRAL", result.get(i).get("NO_REFERRAL").asString())
                                        .set("NO_PONSEL_REFEREE", result.get(i).get("NO_PONSEL_REFEREE").asString())
                                        .set("KONTAK_PERSON", result.get(i).get("KONTAK_PERSON").asString())
                                        .set("JABATAN", result.get(i).get("JABATAN").asString())
                                        .set("NAMA_BENGKEL", result.get(i).get("NAMA_BENGKEL").asString())
                                        .set("ALAMAT", result.get(i).get("ALAMAT").asString())
                                        .set("KOTA_KAB", result.get(i).get("KOTA_KAB").asString())
                                        .set("BIDANG_USAHA", result.get(i).get("BIDANG_USAHA").asString())
                                        .set("JENIS_KENDARAAN", result.get(i).get("JENIS_KENDARAAN").asString())
                                );
                                kodeList.add(result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString());
                            }
                        }
                    }

                    setSpinnerOffline(kodeList, spKodeRefferal, "");
                    spKodeRefferal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String reff = parent.getItemAtPosition(position).toString();
                            if(position != 0 && reff.equals(dataRefferalList.get(position).get("COMPARISON").asString())){
                                kodeRefferal = dataRefferalList.get(position).get("NO_REFERRAL").asString();
                                noPonselReferee = dataRefferalList.get(position).get("NO_PONSEL_REFEREE").asString();
                                etKontakPerson.setText(dataRefferalList.get(position).get("KONTAK_PERSON").asString());
                                etJabatan.setText(dataRefferalList.get(position).get("JABATAN").asString());
                                etNamaBengkel.setText(dataRefferalList.get(position).get("NAMA_BENGKEL").asString());
                                etAlamat.setText(dataRefferalList.get(position).get("ALAMAT").asString());
                                etKotaKab.setText(dataRefferalList.get(position).get("KOTA_KAB").asString());

                                String bidangUsaha = dataRefferalList.get(position).get("BIDANG_USAHA").asString();
                                String[] biidangUsahaSplit = bidangUsaha.split(",");
                                List<String> selectionBidangUsaha = new ArrayList<>(Arrays.asList(biidangUsahaSplit));
                                setSpKendaraan(dataRefferalList.get(position).get("JENIS_KENDARAAN").asString());
                                setSpBidangUsaha(selectionBidangUsaha);
                            }else{
                                noPonselReferee = "";
                                etKontakPerson.setText("");
                                etJabatan.setText("");
                                etNamaBengkel.setText("");
                                etAlamat.setText("");
                                etKotaKab.setText("");
                                setSpKendaraan("");
                                setSpBidangUsaha(null);
                            }


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }
        });

    }

    private void setSpBidangUsaha(final List<String> selectionList) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    if (result.get("data").get(i).get("TYPE").asString().equalsIgnoreCase("MOTOR")) {
                        motorList.add(result.get("data").get(i).get("KATEGORI") + " - " + result.get("data").get(i).get("TYPE"));
                    } else if (result.get("data").get(i).get("TYPE").asString().equalsIgnoreCase("MOBIL")) {
                        mobilList.add(result.get("data").get(i).get("KATEGORI") + " - " + result.get("data").get(i).get("TYPE"));
                    }
                }
                try {
                    if (selectionList != null) {
                        if (isKategori) {
                            spBidangUsaha.setItems(motorList, selectionList);
                        } else {
                            spBidangUsaha.setItems(mobilList, selectionList);
                        }
                    } else {
                        spBidangUsaha.setItems(isKategori ? motorList : mobilList);
                    }

                    spBidangUsaha.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedIndices(List<Integer> indices) {

                        }

                        @Override
                        public void selectedStrings(List<String> strings) {
                            if(strings.size() > 0){
                                bidangUsahaBuilder = new StringBuilder();
                                for (int i = 0; i < strings.size(); i++) {
                                    bidangUsahaBuilder.append(strings.get(i)).append(", ");
                                }
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    showInfo("Perlu di Muat Ulang Bidang Usaha");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void getLatLong(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
