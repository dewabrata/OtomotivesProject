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

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.Checkin2_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class KonfirmasiData_Pembayaran_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi_data_pembayaran);
        initToolbar();
        initComponent();
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
        find(R.id.et_noPonsel, EditText.class).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && !find(R.id.et_noPonsel, EditText.class).getText().toString().contains("+62 ")){
                    find(R.id.et_noPonsel, EditText.class).setText("+62 ");
                }
            }
        });

        find(R.id.et_noPonsel, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.et_noPonsel, EditText.class).removeTextChangedListener(this);
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting < 4 && !find(R.id.et_noPonsel, EditText.class).getText().toString().contains("+62 ")) {
                    find(R.id.et_noPonsel, EditText.class).setText("+62 ");
                    Selection.setSelection(find(R.id.et_noPonsel, EditText.class).getText(), find(R.id.et_noPonsel, EditText.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.et_noPonsel, EditText.class).requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
                find(R.id.et_noPonsel, EditText.class).addTextChangedListener(this);
            }
        });

        find(R.id.btn_lanjut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nson readData = Nson.readJson(getIntentStringExtra(DATA));
                readData.set("NO_PONSEL", find(R.id.et_noPonsel, NikitaAutoComplete.class).getText().toString());
                readData.set("NAMA_PELANGGAN", find(R.id.et_namaPelanggan, NikitaAutoComplete.class).getText().toString());
                readData.set("PEMILIK", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");

                Intent intent = new Intent(getActivity(), Checkin2_Activity.class);
                intent.putExtra("KONFIRMASI DATA", "");
                intent.putExtra(DATA, readData.toJson());
                startActivityForResult(intent, REQUEST_NEW_CS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS) {
            Intent i = new Intent();
            i.putExtra(DATA, getIntentStringExtra(data, DATA));
            setResult(RESULT_OK, i);
            finish();
        }
    }
}