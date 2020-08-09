package com.rkrzmail.oto.modules.primary.checkin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Checkin2_Activity extends AppActivity {

    private EditText etTahun, etNorangka, etNomesin;
    private TextView tvTgl;
    private NikitaAutoComplete etWarna;
    private static final int REQUEST_BOOKING = 12;
    private static final int REQUEST_CHECKIN = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin2_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_checkin2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etWarna = findViewById(R.id.et_warna_checkin2);
        etTahun = findViewById(R.id.et_tahun_checkin2);
        tvTgl = findViewById(R.id.tv_tanggal_checkin2);
        etNorangka = findViewById(R.id.et_noRangka_checkin2);
        etNomesin = findViewById(R.id.et_noMesin_checkin2);

        componentValidation();
        tvTgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getDatePickerDialogTextView(getActivity(), tvTgl);
            }
        });
        find(R.id.btn_lanjut_checkin2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelanjutnya();
            }
        });
    }

    private void componentValidation() {
        Tools.setViewAndChildrenEnabled(find(R.id.ly_tahun_checkin2, LinearLayout.class), false);

        etWarna.setThreshold(3);
        etWarna.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("warna", bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nopol, parent, false);
                }
                findView(convertView, R.id.txtNopol, TextView.class).setText(formatNopol(getItem(position).get("").asString()));
                return convertView;
            }
        });
        etWarna.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        etTahun.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 4) {
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    try {
                        Date inputYear = new SimpleDateFormat("yyyy").parse(editable.toString());
                        Date validationYear = new SimpleDateFormat("yyyy").parse(String.valueOf(year - 1));
                        if (validationYear.after(inputYear)) {
                            Tools.setViewAndChildrenEnabled(find(R.id.ly_tahun_checkin2, LinearLayout.class), true);
                        } else {
                            Tools.setViewAndChildrenEnabled(find(R.id.ly_tahun_checkin2, LinearLayout.class), false);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setSelanjutnya() {
        Nson nson = Nson.readJson(getIntentStringExtra("data"));

        nson.set("warna", etWarna.getText().toString());
        nson.set("tahun", etTahun.getText().toString());
        nson.set("norangka", etNorangka.getText().toString());
        nson.set("nomesin", etNomesin.getText().toString());
        nson.set("tanggal", tvTgl.getText().toString());

        Intent intent = new Intent();
        intent.putExtra("data", nson.toJson());
        setResult(RESULT_OK, intent);
        finish();
    }
}
