package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.JURNAL;
import static com.rkrzmail.utils.APIUrls.SAVE_REFFERAL;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;

public class AturReferal_Activity extends AppActivity implements View.OnClickListener {

    private EditText etKontakPerson, etNamaBengkel, etAlamat, etJabatan;
    private NikitaAutoComplete etKotaKab;
    private TextView tvKontak;
    private Spinner spKendaraan;
    private MultiSelectionSpinner spBidangUsaha;

    private StringBuilder bidangUsahaBuilder = new StringBuilder();
    private String typeKendaraan;
    private boolean isKategori = false;
    private List<String> motorList = new ArrayList<>(),
            mobilList = new ArrayList<>(),
            allList = new ArrayList<>(),
            noHpList = new ArrayList<>(),
            merkMotorList = new ArrayList<>(),
            merkMobilList = new ArrayList<>(), allMerkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_referal);
        initToolbar();
        initComponent();
        setSpKendaraan();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Referensikan Otomotives");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etAlamat = findViewById(R.id.et_alamat_ref);
        etKotaKab = findViewById(R.id.et_kotakab_ref);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_ref);
        etKontakPerson = findViewById(R.id.et_namaPemilik_ref);
        tvKontak = findViewById(R.id.tv_kontak);
        spBidangUsaha = findViewById(R.id.sp_bidang_usaha);
        spKendaraan = findViewById(R.id.sp_jenis_kendaraan);
        etJabatan = findViewById(R.id.et_jabatan_ref);

        String aggrement = "Setuju dengan <font color=#F44336><u> Syarat & kondisi </u></font> Refferal Bengkel Pro";
        find(R.id.tv_setuju_regist, TextView.class).setText(Html.fromHtml(aggrement));
        find(R.id.tv_setuju_regist, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo("Show Aggrement");
            }
        });

        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");
        minEntryEditText(etKontakPerson, 5, find(R.id.tl_cp, TextInputLayout.class), "Panjang Nama Min. 5 Karakter");
        minEntryEditText(etNamaBengkel, 8, find(R.id.tl_namaBengkel, TextInputLayout.class), "Nama Bengkel Min. 5 Karakter");
        minEntryEditText(etAlamat, 20, find(R.id.tl_alamat, TextInputLayout.class), "Alamat Min. 20 Karakter");

        find(R.id.btn_simpan_ref, Button.class).setOnClickListener(this);
        tvKontak.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setSpKendaraan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "JENIS_KENDARAAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    List<String> kendaraanList = new ArrayList<>();
                    kendaraanList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        kendaraanList.add(result.get(i).get("TYPE").asString());
                    }
                    ArrayAdapter<String> kendaraanAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, kendaraanList);
                    kendaraanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spKendaraan.setAdapter(kendaraanAdapter);
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
                            }

                            if (i != 0) {
                                setSpBidangUsaha();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else {
                    setSpKendaraan();
                }
            }
        });
    }

    private void setSpBidangUsaha() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "bidangUsaha");
                if(motorList.size() == 0 && mobilList.size() == 0){
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("JENIS_KENDARAAN").asString().equalsIgnoreCase("MOTOR")) {
                            motorList.add(result.get(i).get("BIDANG_USAHA").asString());
                        } else if (result.get(i).get("JENIS_KENDARAAN").asString().equalsIgnoreCase("MOBIL")) {
                            mobilList.add(result.get(i).get("BIDANG_USAHA").asString());
                        }
                    }
                }
            }

            @Override
            public void runUI() {
                try {
                    spBidangUsaha.setItems(isKategori ? motorList : mobilList);
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
                }
            }
        });
    }

    private String formatPhone(String phone) {
        if (phone.startsWith("+62")) {
            phone = phone.substring(1);
        } else if (phone.startsWith("0")) {
            phone = "62" + phone.substring(1);
        }
        phone = Utility.replace(phone," ","");
        return phone.trim();
    }


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            Response response;
            @Override
            public void run() {
                RequestBody formBody = new FormEncodingBuilder()
                        .add("cid", UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim())
                        .add("no_ponsel_referee", UtilityAndroid.getSetting(getApplicationContext(), "user", ""))
                        .add("no_ponsel", NumberFormatUtils.formatOnlyNumber(formatPhone(tvKontak.getText().toString())))
                        .add("kontak_person", etKontakPerson.getText().toString())
                        .add("jabatan", etJabatan.getText().toString())
                        .add("nama_bengkel", etNamaBengkel.getText().toString())
                        .add("jenis_kendaraan", spKendaraan.getSelectedItem().toString())
                        .add("bidang_usaha", bidangUsahaBuilder.toString())
                        .add("alamat", etAlamat.getText().toString())
                        .add("kota_kab", etKotaKab.getText().toString())
                        .build();
                try {
                    Request request = new Request.Builder()
                            .url(AppApplication.getBaseUrlV4(SAVE_REFFERAL))
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
                    AppApplication.getMessageTrigger();
                    showSuccess("SUKSES MENYIMPAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(response.body().toString());
                }
            }
        });
    }

    private void componentValidation() {
        String namaBengkel = etNamaBengkel.getText().toString();
        String kontakPerson = etKontakPerson.getText().toString();
        String alamat = etAlamat.getText().toString();
        String kontak = tvKontak.getText().toString();
        String kotaKab = etKotaKab.getText().toString();

        if (kontak.isEmpty()) {
            tvKontak.setError("KONTAK HARUS DI MASUKKAN");
            viewFocus(tvKontak);
        } else if (kontakPerson.isEmpty()) {
            etKontakPerson.setError("KONTAK PERSON HARUS DI ISI");
            viewFocus(etKontakPerson);
        } else if (etJabatan.getText().toString().isEmpty()) {
            etJabatan.setError("JABATAN HARUS DI ISI");
            viewFocus(etJabatan);
        } else if (namaBengkel.isEmpty()) {
            etNamaBengkel.setError("NAMA BENGKEL HARUS DI ISI");
            viewFocus(etNamaBengkel);
        } else if (spKendaraan.getSelectedItem().toString().equals("--PILIH--")) {
            setErrorSpinner(spKendaraan, "JENIS KENDARAAN HARUS DI PILIH");
        } else if (spBidangUsaha.getSelectedItemsAsString().isEmpty()) {
            showWarning("BIDANG USAHA HARUS DI PILIH");
        } else if (alamat.isEmpty()) {
            etAlamat.setError("ALAMAT HARUS DI ISI");
            viewFocus(etAlamat);
        } else if (kotaKab.isEmpty()) {
            etKotaKab.setError("KOTA/KAB HARUS DI ISI");
            viewFocus(etKotaKab);
        } else if (!find(R.id.cb_setuju_regist, CheckBox.class).isChecked()) {
            showWarning("SEUJUTUI SYARAT DAM KETENTUAN REFFERAL");
        } else {
            saveData();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_simpan_ref) {
            componentValidation();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                tvKontak.setText(contactName + "\n" + number);
            }
        }

    }
}
