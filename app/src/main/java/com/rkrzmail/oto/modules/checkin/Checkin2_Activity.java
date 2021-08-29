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
import android.widget.ProgressBar;
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
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

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
        setContentView(R.layout.activity_checkin2);
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        initComponent();
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_checkin2);
        setSupportActionBar(toolbar);
        if (getIntent().hasExtra("KONFIRMASI DATA")) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(getIntent().getStringExtra("KONFIRMASI DATA"));
            isKonfirmasi = true;
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        initToolbar();
        etWarna = findViewById(R.id.et_warna_checkin2);
        tvTgl = findViewById(R.id.tv_tanggal_checkin2);
        etNorangka = findViewById(R.id.et_noRangka_checkin2);
        etNomesin = findViewById(R.id.et_noMesin_checkin2);
        etKodeTipe = findViewById(R.id.et_kode_tipe);

        readCheckin = Nson.readJson(getIntentStringExtra(DATA));
        find(R.id.et_nopol).setVisibility(getIntent().hasExtra("KONFIRMASI DATA") ? View.VISIBLE : View.GONE);
        if (!isKonfirmasi) {
            find(R.id.btn_lanjut_checkin2, Button.class).setText("LEWATI");
            etNorangka.setText(readCheckin.get("noRangka").asString());
            etNomesin.setText(readCheckin.get("noMesin").asString());
            tvTgl.setText(readCheckin.get("tglBeli").asString());
            etKodeTipe.setText(readCheckin.get("KODE_TIPE").asString());
            find(R.id.tv_tahun_checkin2, TextView.class).setText(readCheckin.get("tahunProduksi").asString());
            /*etNomesin.setVisibility(View.GONE);
            etNorangka.setVisibility(View.GONE);*/
            etWarna.setText(readCheckin.get("WARNA").asString());
            noHp = readCheckin.get("noPonsel").asString();
            find(R.id.et_kota_kab, NikitaAutoComplete.class).setLoadingIndicator((ProgressBar) findViewById(R.id.pb_et_kotakab));
            remakeAutoCompleteMaster( find(R.id.et_kota_kab, NikitaAutoComplete.class), "DAERAH", "KOTA_KAB");
        } else {
            viewDataKendaraan();
            merkKendaraan = getIntentStringExtra("MERK");
            noHp = getIntentStringExtra("NO_PONSEL");
            find(R.id.et_nopol, EditText.class).setText(getIntentStringExtra("NOPOL"));
            find(R.id.btn_lanjut_checkin2, Button.class).setText("SIMPAN");
            find(R.id.tl_alamat).setVisibility(View.GONE);
            find(R.id.tl_kota_kab).setVisibility(View.GONE);
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
                int year = Calendar.getInstance().get(Calendar.YEAR) - 4;
                try {
                    @SuppressLint("SimpleDateFormat") Date inputYear = new SimpleDateFormat("yyyy").parse(find(R.id.tv_tahun_checkin2, TextView.class).getText().toString());
                    @SuppressLint("SimpleDateFormat") Date validationYear = new SimpleDateFormat("yyyy").parse(String.valueOf(year));
                    assert inputYear != null;
                    tahunBeli = inputYear.getTime();
                    tahunSekarang = validationYear.getTime();
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_beli_checkin2, LinearLayout.class), tahunBeli > tahunSekarang);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        find(R.id.btn_lanjut_checkin2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isKonfirmasi) {
                    if (etWarna.getText().toString().isEmpty()) {
                        etWarna.setError("Warna Wajib di Isi");
                        etWarna.requestFocus();
                    } else if (etNomesin.getText().toString().isEmpty()) {
                        etNomesin.setError("Kode Tipe Harus di Isi");
                        etNomesin.requestFocus();
                    } else if (etNorangka.getText().toString().isEmpty()) {
                        etNorangka.setError("Kode Tipe Harus di Isi");
                        etNorangka.requestFocus();
                    } else {
                        setSelanjutnya(readCheckin);
                    }
                } else {
                    if (find(R.id.tv_tahun_checkin2, TextView.class).getText().toString().isEmpty()) {
                        showInfo("Tahun Produksi Tidak Valid");
                        find(R.id.tv_tahun_checkin2, TextView.class).performClick();
                    } else if (tvTgl.isEnabled() && tvTgl.getText().toString().isEmpty()) {
                        showWarning("Tanggal Berisi Harus Di Isi");
                        tvTgl.performClick();
                    } else {
                        setSelanjutnya(readCheckin);
                    }
                }
            }
        });
    }

    private void initAutoCompleteKodeTipe() {
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

    private void initAutoCompleteWarna() {
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
                args.put("id", isKonfirmasi ? getIntentStringExtra(ID) : readCheckin.get("CHECKIN_ID").asString());
                args.put("idDataKendaraan", isKonfirmasi ? getIntentStringExtra("DATA_KENDARAAN_ID") : readCheckin.get("DATA_KENDARAAN_ID").asString());
                args.put("warna", warna);
                args.put("tahun", tahun);
                args.put("tanggalbeli", tanggalBeli);
                args.put("norangka", noRangka);
                args.put("nomesin", noMesin);
                args.put("checkinId", getIntentStringExtra(ID));//konfirmasi data
                args.put("kodeTipe", etKodeTipe.getText().toString());
                args.put("noPonsel", noHp);
                args.put("nopol", getIntentStringExtra("NOPOL"));
                args.put("alamat", find(R.id.et_alamat, EditText.class).getText().toString());
                args.put("kotaKab", find(R.id.et_kota_kab, EditText.class).getText().toString());

                String nopolEditText = find(R.id.et_nopol, EditText.class).getText().toString().replace(" ", "");
                if (getIntentStringExtra("NOPOL").equals(nopolEditText)) {
                    args.put("koreksiNopol", "");
                } else {
                    args.put("koreksiNopol", nopolEditText);
                }

                args.put("merk", getIntentStringExtra("MERK"));
                if (isKonfirmasi) {
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
                    if (readCheckin.get("tanggalBeli").asString().isEmpty()) {
                        readCheckin.set("tanggalBeli", tanggalBeli);
                    }
                    if (readCheckin.get("tahunProduksi").asString().isEmpty()) {
                        readCheckin.set("tahunProduksi", tahun);
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
                args.put("noPolisi", getIntentStringExtra("NOPOL"));
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
                    find(R.id.et_alamat, EditText.class).setText(result.get("ALAMAT").asString());
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }
}
