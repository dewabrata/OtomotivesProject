package com.rkrzmail.oto.modules.bengkel;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.List;
import java.util.Map;

public class ReferensikanOtomotives_Activity extends AppActivity implements View.OnClickListener {

    private EditText etNamaPemilik, etEmail, etNamaBengkel, etAlamat;
    private NikitaAutoComplete etKotaKab;
    private TextView tvNoPonsel;
    private MultiSelectionSpinner spKendaraan, spBidangUsaha;
    public static final int REQUEST_CONTACT = 91;
    private String typeKendaraan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referensikan_otomotives_);
        initToolbar();
        initComponent();
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
        tvNoPonsel = findViewById(R.id.tv_kontak_ref);
        spBidangUsaha = findViewById(R.id.sp_usaha_ref);
        spKendaraan = findViewById(R.id.sp_kendaraan_ref);

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
                if (!s.toString().contains("@")) {
                    find(R.id.tl_email, TextInputLayout.class).setError("Email Tidak Valid");
                } else {
                    find(R.id.tl_email, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });
        setMultiSelectionSpinnerFromApi(spKendaraan, "nama", "BENGKEL", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {
                for(String data : strings){
                    typeKendaraan += data;
                    initBidangUsaha(typeKendaraan);
                }
            }
        }, "TYPE", "");
        find(R.id.btn_simpan_ref, Button.class).setOnClickListener(this);
        tvNoPonsel.setOnClickListener(new View.OnClickListener() {
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

    private void catchData() {
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
        String ponsel = tvNoPonsel.getText().toString();
        String kotaKab = etKotaKab.getText().toString();
        if (namaBengkel.isEmpty() && namaPemilik.isEmpty() && alamat.isEmpty() && email.isEmpty() && ponsel.isEmpty() && kotaKab.isEmpty()) {
            showInfo("Silahkan Lengkapi Data Anda");
        } else {
            catchData();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_simpan_ref:
                componentValidation();
                break;
        }
    }

    private void initBidangUsaha(final String type){
        setMultiSelectionSpinnerFromApi(spBidangUsaha, "nama", "BENGKEL", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {
                for(String str : strings){
                    if(str.contains(type)){
                        strings.remove(str);
                    }
                }
            }
        }, "TYPE", "KATEGORI");
    }

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
                tvNoPonsel.setText(contactName + "\n" + number);
            }
        }

    }
}
