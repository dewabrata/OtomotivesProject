package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import static com.rkrzmail.utils.APIUrls.ATUR_PEMBAYARAN;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;

public class Checkin2_Activity extends AppActivity {

    private EditText etNorangka, etNomesin;
    private TextView tvTgl;
    private NikitaAutoComplete etWarna, etKodeTipe;
    private Nson readCheckin;
    private long tahunBeli = 0;
    private long tahunSekarang = 0;
    private boolean isKonfirmasi = false;
    private String merkKendaraan = "";
    private String noHp = "";
    private String tanggalBeliKendaraam = "";

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
        if (getIntent().hasExtra("KONFIRMASI DATA")) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Konfirmasi Data Kendaraan");
            viewDataKendaraan();
            isKonfirmasi = true;
            merkKendaraan = getIntentStringExtra("MERK");
            noHp = getIntentStringExtra("NO_PONSEL");
            find(R.id.btn_lanjut_checkin2, Button.class).setText("Simpan");
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etWarna = findViewById(R.id.et_warna_checkin2);
        tvTgl = findViewById(R.id.tv_tanggal_checkin2);
        etNorangka = findViewById(R.id.et_noRangka_checkin2);
        etNomesin = findViewById(R.id.et_noMesin_checkin2);
        etKodeTipe = findViewById(R.id.et_kode_tipe);

        readCheckin = Nson.readJson(getIntentStringExtra(DATA));
        if(readCheckin != null && !isKonfirmasi){
            etNorangka.setText(readCheckin.get("noRangka").asString());
            etNomesin.setText(readCheckin.get("noMesin").asString());
            tvTgl.setText(readCheckin.get("tglBeli").asString());
            etKodeTipe.setText(readCheckin.get("KODE_TIPE").asString());
            find(R.id.tv_tahun_checkin2, TextView.class).setText(readCheckin.get("tahunProduksi").asString());
        }

        initListener();
        initAutoCompleteKodeTipe();
        initAutoCompleteWarna();
    }

    private void initListener() {
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
                    if (tahunBeli <= tahunSekarang) {
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

        find(R.id.btn_lanjut_checkin2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (find(R.id.tv_tahun_checkin2, TextView.class).getText().toString().isEmpty()) {
                    showInfo("Tahun Produksi Tidak Valid");
                    find(R.id.tv_tahun_checkin2, TextView.class).performClick();
                } else if (tvTgl.isEnabled() && tvTgl.getText().toString().isEmpty()) {
                    showWarning("Tanggal Berisi Harus Di Isi");
                    tvTgl.performClick();
                }else if(etWarna.getText().toString().isEmpty()){
                    etWarna.setError("Warna Wajib di Isi");
                    etWarna.requestFocus();
                }else {
                    setSelanjutnya(readCheckin);
                }
            }
        });
    }

    private void initAutoCompleteKodeTipe(){
        etKodeTipe.setThreshold(3);
        etKodeTipe.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "KODE TIPE");
                args.put("merk", isKonfirmasi ? merkKendaraan : readCheckin.get("merk").asString());
                args.put("kodeTipe", bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion_single, parent, false);
                }
                findView(convertView, R.id.tv_text_suggesttion, TextView.class).setText(getItem(position).get("KODE_TIPE").asString());
                return convertView;
            }
        });

        etKodeTipe.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_kode_tipe));
        etKodeTipe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etKodeTipe.setText(n.get("KODE_TIPE").asString().toUpperCase());
            }
        });
    }

    private void initAutoCompleteWarna(){
        etWarna.setThreshold(3);
        etWarna.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "WARNA");
                args.put("warna", bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion_single, parent, false);
                }
                findView(convertView, R.id.tv_text_suggesttion, TextView.class).setText(formatNopol(getItem(position).get("WARNA").asString()));
                return convertView;
            }
        });

        etWarna.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_warna));
        etWarna.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etWarna.setText(n.get("WARNA").asString().toUpperCase());
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
                args.put("jenisCheckin", "2");
                args.put("id", readCheckin.get("CHECKIN_ID").asString());
                args.put("idDataKendaraan", readCheckin.get("DATA_KENDARAAN_ID").asString());
                args.put("warna", warna);
                args.put("tahun", tahun);
                args.put("tanggalbeli", tanggalBeli);
                args.put("norangka", noRangka);
                args.put("nomesin", noMesin);
                args.put("checkinId", getIntentStringExtra(ID));
                args.put("kodeTipe", etKodeTipe.getText().toString());
                args.put("noPonsel", noHp);
                if(isKonfirmasi){
                    args.put("isKonfirmasi", "Y");
                }


                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    readCheckin.set("NO_RANGKA", noRangka);
                    readCheckin.set("NO_MESIN", noMesin);
                    readCheckin.set("CHECKIN_ID", readCheckin.get("CHECKIN_ID").asString());
                    if(!readCheckin.containsKey("tanggalBeli")){
                        readCheckin.set("tanggalBeli", tvTgl.getText().toString());
                    }

                    Intent intent = new Intent();
                    intent.putExtra(DATA, readCheckin.toJson());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }

    private void viewDataKendaraan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "KONFIRMASI DATA");
                args.put("checkinId", getIntentStringExtra(ID));
                args.put("noPonsel", noHp);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_NOMOR_POLISI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    etWarna.setText(result.get("WARNA").asString());
                    find(R.id.tv_tahun_checkin2, TextView.class).setText(result.get("TAHUN_PRODUKSI").asString());
                    tvTgl.setText(result.get("TANGGAL_BELI").asString());
                    etNorangka.setText(result.get("NO_RANGKA").asString());
                    etNomesin.setText(result.get("NO_MESIN").asString());
                    etKodeTipe.setText(result.get("CODE_TYPE").asString());
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }
}
