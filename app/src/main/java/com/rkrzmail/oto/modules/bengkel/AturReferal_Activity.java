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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;

public class AturReferal_Activity extends AppActivity implements View.OnClickListener {

    private EditText etNamaPemilik, etEmail, etNamaBengkel, etAlamat;
    private NikitaAutoComplete etKotaKab;
    private TextView tvKontak;
    private Spinner spKendaraan;
    private MultiSelectionSpinner spBidangUsaha;

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
        getSupportActionBar().setTitle("Referensikan Otomotives");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etAlamat = findViewById(R.id.et_alamat_ref);
        etEmail = findViewById(R.id.et_email_ref);
        etKotaKab = findViewById(R.id.et_kotakab_ref);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_ref);
        etNamaPemilik = findViewById(R.id.et_namaPemilik_ref);
        tvKontak = findViewById(R.id.tv_kontak);
        spBidangUsaha = findViewById(R.id.sp_bidang_usaha);
        spKendaraan = findViewById(R.id.sp_jenis_kendaraan);

        remakeAutoCompleteMaster(etKotaKab, "DAERAH", "KOTA_KAB");
        minEntryEditText(etNamaPemilik, 5, find(R.id.tl_cp, TextInputLayout.class), "Panjang Nama Min. 5 Karakter");
        minEntryEditText(etNamaBengkel, 8, find(R.id.tl_namaBengkel, TextInputLayout.class), "Nama Bengkel Min. 5 Karakter");
        minEntryEditText(etAlamat, 20, find(R.id.tl_alamat, TextInputLayout.class), "Alamat Min. 20 Karakter");

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_email, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains("@") || !s.toString().contains(".com")) {
                    find(R.id.tl_email, TextInputLayout.class).setError("Email Tidak Valid");
                } else {
                    find(R.id.tl_email, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });
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

    private void setSpKendaraan(){
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
                        if(!kendaraanList.contains(result.get(i).get("TYPE").asString())){
                            kendaraanList.add(result.get(i).get("TYPE").asString());
                        }
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

                            if(i != 0){
                                setSpBidangUsaha();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }else{
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
                    spBidangUsaha.setItems(isKategori ? motorList : mobilList);
                    spBidangUsaha.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedIndices(List<Integer> indices) {

                        }

                        @Override
                        public void selectedStrings(List<String> strings) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    showInfo("Perlu di Muat Ulang Bidang Usaha");
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("regristrasi"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Registrasi Gagal, Silahkan Cek Data Anda Kembali");
                }
            }
        });
    }

    private void componentValidation() {
        String namaBengkel = etNamaBengkel.getText().toString();
        String namaPemilik = etNamaPemilik.getText().toString();
        String alamat = etAlamat.getText().toString();
        String email = etEmail.getText().toString();
        String ponsel = tvKontak.getText().toString();
        String kotaKab = etKotaKab.getText().toString();
        if (namaBengkel.isEmpty() && namaPemilik.isEmpty() && alamat.isEmpty() && email.isEmpty() && ponsel.isEmpty() && kotaKab.isEmpty()) {
            showInfo("Silahkan Lengkapi Data Anda");
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
        if(requestCode == REQUEST_CONTACT){
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
