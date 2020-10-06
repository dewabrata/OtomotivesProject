package com.rkrzmail.oto.modules.primary.checkin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.TimePicker_Dialog;
import com.rkrzmail.oto.modules.YearPicker_Dialog;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.DATA;

public class Checkin2_Activity extends AppActivity {

    private EditText etNorangka, etNomesin;
    private TextView tvTgl;
    private NikitaAutoComplete etWarna;
    private Nson readCheckin;
    private long tahunBeli = 0;
    private long tahunSekarang = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin2_);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_checkin2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        readCheckin = Nson.readJson(getIntentStringExtra(DATA));

        etWarna = findViewById(R.id.et_warna_checkin2);
        tvTgl = findViewById(R.id.tv_tanggal_checkin2);
        etNorangka = findViewById(R.id.et_noRangka_checkin2);
        etNomesin = findViewById(R.id.et_noMesin_checkin2);

        initListener();
    }

    private void initListener(){
        Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_beli_checkin2, LinearLayout.class), false);
        tvTgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateSpinnerDialog(tvTgl, "Tanggal Beli Kendaraan");
            }
        });

        find(R.id.tv_tahun_checkin2, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tahunBeliDialog();
            }
        });

        find(R.id.tv_tahun_checkin2, TextView.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                try {
                    @SuppressLint("SimpleDateFormat") Date inputYear = new SimpleDateFormat("yyyy").parse(find(R.id.tv_tahun_checkin2, TextView.class).getText().toString());
                    @SuppressLint("SimpleDateFormat") Date validationYear = new SimpleDateFormat("yyyy").parse(String.valueOf(year - 4));
                    tahunBeli = inputYear.getTime();
                    tahunSekarang = validationYear.getTime();
                    if (tahunBeli < tahunSekarang) {
                        Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_beli_checkin2, LinearLayout.class), false);
                        find(R.id.tv_disable).setVisibility(View.VISIBLE);
                    } else {
                        Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_beli_checkin2, LinearLayout.class), true);
                        find(R.id.tv_disable).setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

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

        find(R.id.btn_lanjut_checkin2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (find(R.id.tv_tahun_checkin2, TextView.class).getText().toString().isEmpty()) {
                    showInfo("Tahun Produksi Tidak Valid");
                    find(R.id.tv_tahun_checkin2, TextView.class).performClick();
                } else if (tvTgl.isEnabled() && tvTgl.getText().toString().isEmpty()) {
                    showWarning("Tanggal Berisi Harus Di Isi");
                    tvTgl.performClick();
                } else if (etNorangka.getText().toString().isEmpty()) {
                    etNorangka.setError("No. Rangka Harus Di isi");
                    etNorangka.requestFocus();
                } else if (etNomesin.getText().toString().isEmpty()) {
                    etNomesin.setError("No. Mesin Harus Di isi");
                    etNomesin.requestFocus();
                } else {
                    setSelanjutnya(readCheckin);
                }
            }
        });
    }

    public void tahunBeliDialog() {
        YearPicker_Dialog dialog = new YearPicker_Dialog();
        dialog.show(getSupportFragmentManager(), "YearPicker");
        dialog.getYears(new TimePicker_Dialog.OnClickDialog() {
            @Override
            public void getTime(int day, int hours, int minutes) {

            }

            @Override
            public void getYear(int year) {
                find(R.id.tv_tahun_checkin2, TextView.class).setText(String.valueOf(year));
            }
        });
    }

    private void setSelanjutnya(final Nson readCheckin) {
        final String warna = etWarna.getText().toString();
        final String tahun = find(R.id.tv_tahun_checkin2, TextView.class).getText().toString();
        final String tanggalBeli = tvTgl.getText().toString();
        final String noRangka = etNorangka.getText().toString();
        final String noMesin = etNomesin.getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("regris", "2");
                args.put("id", readCheckin.get("id").asString());
                args.put("warna", warna);
                args.put("tahun", tahun);
                args.put("tanggalbeli", tanggalBeli);
                args.put("norangka", noRangka);
                args.put("nomesin", noMesin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("checkin"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Intent intent = new Intent();
                    intent.putExtra(DATA, readCheckin.toJson());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    showWarning("Gagal");
                }
            }
        });
    }
}
