package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.Capture;
import com.rkrzmail.utils.FileUtility;
import com.rkrzmail.utils.Tools;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ANTRIAN;
import static com.rkrzmail.utils.APIUrls.ANTRIAN_MULAI;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_ANTRIAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ONEDAY;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CODE_SIGN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Checkin4_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "Checking4____";

    private Bitmap ttd;

    private Nson mekanikArray = Nson.newArray(), idMekanikArray = Nson.newArray();
    private Nson getData;
    private Nson penugasanMekanikList = Nson.newArray();
    private Nson noKunciList = Nson.newArray();

    private boolean isSign = false, isBatal = false, isMekanik = false;
    private boolean isExpressAndStandard = false, isExtra = false, isHplusPartKosong = false, isDp = false, isHpLus = false;
    private String waktuLayananHplusExtra = "", jenisLayanan = "", waktuLayananStandartExpress = "";
    private String tglEstimasi = "", waktuEstimasi = "", antrianSebelumnya = "";
    private String ttdPath = "";
    private String idMekanik = "";
    private String jenisAntrian = "";
    private int idAntrian = 0;
    private int waktuPesan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin4_);
        initComponent();
        initToolbar();
        ttd = null;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        if (getIntent().hasExtra("BATAL")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_checkin4, LinearLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.ly_ttd, LinearLayout.class), false);
            find(R.id.et_ket_checkin4, EditText.class).requestFocus();
            showInfo("Silahkan Isi Keterangan Batal");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (find(R.id.et_ket_checkin4, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_ket_checkin4, EditText.class).setError("Alasan Batal Harus di Isi");
                    } else {
                        Intent i = new Intent();
                        i.putExtra("alasanBatal", find(R.id.et_ket_checkin4, EditText.class).getText().toString());
                        setResult(RESULT_OK, i);
                        finish();
                    }
                }
            });
            return;
        }
        initData();
        initListener();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        getData = Nson.readJson(getIntentStringExtra(DATA));
        Log.d("coba__", "DATA: " + getData);

        waktuLayananStandartExpress = getData.get("WAKTU_LAYANAN").asString();
        jenisLayanan = getData.get("JENIS_LAYANAN").asString();
        waktuPesan = getData.get("WAKTU_PESAN").asInteger();
        jenisAntrian = getData.get("JENIS_ANTRIAN").asString();

        find(R.id.et_lamaWaktu_checkin, EditText.class).setText(waktuLayananStandartExpress);
        find(R.id.tv_jenis_antrian, TextView.class).setText("Jenis Antrian : " + getData.get("JENIS_ANTRIAN").asString());
        setNoAntrian(getData.get("JENIS_ANTRIAN").asString());
        setSpMekanik("");
        setSpBbm();

        if (jenisAntrian.equals("EXTRA")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), true);
            find(R.id.tv_disable_waktu_antar).setVisibility(View.GONE);
            isExtra = true;
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
            find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
        } else if (jenisAntrian.equals("H+")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), true);
            find(R.id.tv_disable_waktu_antar).setVisibility(View.GONE);
            find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setChecked(true);
            isHplusPartKosong = getData.get("PART_KOSONG").asBoolean();
            isHpLus = true;
            find(R.id.et_dp_checkin4, EditText.class).setText(RP + formatRp(getData.get("DP").asString()));
            find(R.id.et_sisa_checkin4, EditText.class).setText(RP + formatRp(getData.get("SISA").asString()));
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
            find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
        } else {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), false);
            isExpressAndStandard = true;
            viewAntrianStandartExpress(jenisAntrian);
        }
        try {
            find(R.id.et_totalBiaya_checkin4, EditText.class).setText(getData.get("TOTAL").asString());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void initListener() {
        find(R.id.tv_jam_ambil, TextView.class).setOnClickListener(this);
        find(R.id.tv_tgl_ambil, TextView.class).setOnClickListener(this);
        find(R.id.tv_tgl_estimasi_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.tv_jam_estimasi_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
        find(R.id.btn_hapus, Button.class).setText("BATAL");
        find(R.id.btn_hapus, Button.class).setOnClickListener(this);
        find(R.id.btn_simpan, Button.class).setOnClickListener(this);
        find(R.id.btn_ttd_checkin4).setOnClickListener(this);
        find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), true);
                    find(R.id.tv_disable_waktu_antar).setVisibility(View.GONE);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), false);
                    find(R.id.tv_disable_waktu_antar).setVisibility(View.VISIBLE);
                }
            }
        });
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setEnabled(false);
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewAntrianStandartExpress(jenisAntrian);
            }
        });
    }

    private Calendar parseWaktuPesan() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }

        long pesanPart = waktuPesan * ONEDAY;
        long totalDate = current + pesanPart;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }


    @SuppressLint("SetTextI18n")
    private String parseEstimasiSelesai(String tglEstimasi, String jenisLayanan) throws ParseException {
        String result = "";
        long timeMilisEstimasi;
        long currentDateTimeMilis;
        Calendar totalEstimasi = Calendar.getInstance();
        Calendar currentDateTime = Calendar.getInstance();
        if (!tglEstimasi.equals("")) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            Date dateTimeEstimasi = sdfDate.parse(tglEstimasi);
            Date dateTimeNow = sdfDate.parse(currentDateTime("dd/MM/yyyy"));
            totalEstimasi.setTime(dateTimeEstimasi);
            currentDateTime.setTime(dateTimeNow);
            timeMilisEstimasi = totalEstimasi.getTimeInMillis();
            currentDateTimeMilis = currentDateTime.getTimeInMillis();
            if (timeMilisEstimasi > currentDateTimeMilis) {
                result = "H+";
                updateAntrian(result);
                isExtra = true;
                try {
                    int totalHarga = Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBiaya_checkin4, EditText.class).getText().toString()));
                    double dp = calculateDp(Double.parseDouble(getSetting("DP_PERSEN")), totalHarga);
                    double sisaDp = totalHarga - dp;

                    find(R.id.et_dp_checkin4, EditText.class).setText(RP + dp);
                    find(R.id.et_sisa_checkin4, EditText.class).setText(RP + formatRp(String.valueOf(sisaDp)));
                } catch (Exception e) {
                    showError(e.getMessage());
                }

                Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
                find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
            } else {
                isExtra = false;
                result = jenisLayanan;
            }
        }

        return result;
    }

    private double calculateDp(double dp, int harga) {
        if (dp > 0 && harga > 0) {
            return (dp / 100) * harga;
        }
        return 0;
    }

    public void getDatePickerCheckin() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                waktuLayananHplusExtra += formattedTime;
                find(R.id.tv_tgl_estimasi_checkin4, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                tglEstimasi = find(R.id.tv_tgl_estimasi_checkin4, TextView.class).getText().toString();
                if (isExtra) {
                    find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(waktuLayananHplusExtra);
                    try {
                        find(R.id.tv_jenis_antrian, TextView.class).setText(parseEstimasiSelesai(tglEstimasi, getData.get("JENIS_ANTRIAN").asString().replace("Jenis Antrian : ", "").trim()));
                    } catch (ParseException e) {
                        showError(e.getMessage());
                    }
                }
            }
        });

        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public void getTimePickerDialogEstimasiSelesai() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                String time = hourOfDay + ":" + minute;
                Date date = null;
                try {
                    date = sdf.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                waktuLayananHplusExtra += " " + formattedTime;
                waktuEstimasi = formattedTime;
                find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(waktuLayananHplusExtra);
                find(R.id.tv_jam_estimasi_checkin4, TextView.class).setText(formattedTime);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "WAKTU: " + waktuLayananHplusExtra);
            }
        });

        timePickerDialog.setTitle("Pilih Jam");
        timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
    }

    private void getDatePickerTglAmbil() {
        final String[] waktuAmbil = {""};
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                waktuAmbil[0] = formattedTime;
                find(R.id.tv_tgl_ambil, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    if (validateWaktuAmbil(true, tglEstimasi, waktuAmbil[0])) {
                        showWarning("Waktu Ambil Harus Melebihi Estimasi Selesai");
                        find(R.id.tv_jam_ambil, TextView.class).setText("");
                        find(R.id.tv_jam_ambil, TextView.class).performClick();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "WAKTU AMBIL: " + waktuLayananHplusExtra);
            }
        });
        datePickerDialog.setMinDate(parseWaktuPesan());
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private void getTimePickerDialogWaktuAmbil() {
        final String[] waktuAmbil = {""};
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                minute -= 10;
                String time = hourOfDay + ":" + minute;
                Date date = null;
                try {
                    date = sdf.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                waktuAmbil[0] = formattedTime;
                find(R.id.tv_jam_ambil, TextView.class).setText(formattedTime);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    if (validateWaktuAmbil(false, waktuEstimasi, waktuAmbil[0])) {
                        showWarning("Waktu Ambil Harus Melebihi Estimasi Selesai");
                        find(R.id.tv_jam_ambil, TextView.class).setText("");
                        find(R.id.tv_jam_ambil, TextView.class).performClick();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "WAKTU AMBIL: " + waktuLayananHplusExtra);
            }
        });

        timePickerDialog.setTitle("Tentukan Waktu Ambil");
        timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
    }

    private void updateAntrian(final String jenisAntrian) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("status", "HPLUS");
                args.put("statusantri", find(R.id.tv_jenis_antrian, TextView.class).getText().toString().replace("Jenis Antrian :", "").trim());
                args.put("id", String.valueOf(idAntrian));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ANTRIAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Antrian Terupdate");
                    result = result.get("data").get(0);
                    find(R.id.et_no_antrian_checkin4, EditText.class).setText(generateNoAntrian(jenisAntrian, result.asString()));
                    find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).setEnabled(true);
                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void setNoAntrian(final String jenisAntrian) {
        find(R.id.btn_refresh_antrian, ImageButton.class).setVisibility(View.GONE);
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("status", jenisAntrian);
                args.put("spec", "Bengkel");
                args.put("statusantri", jenisAntrian);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ANTRIAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    idAntrian = result.asInteger();
                    find(R.id.et_no_antrian_checkin4, EditText.class).setText(generateNoAntrian(jenisAntrian, result.asString()));
                    Log.d(TAG, "NO_ANTRIAN: " + generateNoAntrian(jenisAntrian, result.asString()));
                } else {
                    showError("Nomor Antrian Gagal di Muat, Harap Refresh!", Toast.LENGTH_LONG);
                    find(R.id.btn_refresh_antrian, ImageButton.class).setVisibility(View.VISIBLE);
                    find(R.id.btn_refresh_antrian, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setNoAntrian(jenisAntrian);
                        }
                    });
                }
            }
        });
    }

    private void viewAntrianStandartExpress(final String antrian) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                String[] args = new String[10];
                args[0] = "CID" + "=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                args[1] = "antrian" + "=" + antrian;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(ANTRIAN_MULAI), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), false);
                    find(R.id.tv_disable_estimasi).setVisibility(View.VISIBLE);
                    find(R.id.et_mulaiWaktu_checkin, TextView.class).setText(result.get("waktu_mulai").asString());
                } else {
                    showWarning(result.get("error").asString());
                }
            }
        });
    }

    private void totalWaktu(Nson result) {
        Tools.TimePart waktuMulai = null;
        Tools.TimePart waktuLayanan = Tools.TimePart.parse(waktuLayananStandartExpress);

        Log.d(TAG, "antrian: " + result);
        result = result.get("data");

        if (result.size() > 0) {
            result = result.get(0);
            String estimasi = result.get("ESTIMASI_SELESAI").asString();
//            String namaMekanik = result.get("MEKANIK").asString();
//            setSpMekanik(namaMekanik);
            if (estimasi.length() > 4) {
                estimasi = estimasi.substring(estimasi.length() - 5);
                waktuMulai = Tools.TimePart.parse("00:" + estimasi);
            }
        } else {
            waktuMulai = Tools.TimePart.parse("00:" + currentDateTime("hh:mm"));
        }

        assert waktuMulai != null;
        find(R.id.et_mulaiWaktu_checkin, TextView.class).setText(waktuMulai.toString().substring(3, 8));
        Tools.TimePart totalWaktuSelesai = waktuMulai.add(waktuLayanan);
        find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(totalWaktuSelesai.toString().substring(3, 8));
        find(R.id.tv_tgl_estimasi_checkin4, TextView.class).setText(currentDateTime());
        find(R.id.tv_jam_estimasi_checkin4, TextView.class).setText(find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString());
    }


    @SuppressLint("SimpleDateFormat")
    private boolean validateWaktuAmbil(boolean isTgl, String estimasi, String waktuAmbil) throws ParseException {
        Date jamTglAmbil;
        Date waktuEstimasi;
        SimpleDateFormat sdf;
        if (isTgl) {
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        } else {
            sdf = new SimpleDateFormat("hh:mm");
        }

        jamTglAmbil = sdf.parse(waktuAmbil);
        waktuEstimasi = sdf.parse(estimasi);
        return !jamTglAmbil.after(waktuEstimasi);
    }

    private void saveData(final String status) {
        final String namaMekanik = find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString().contains("--PILIH--") ? "" : find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString();
        final String antrian = find(R.id.tv_jenis_antrian, TextView.class).getText().toString().replace("Jenis Antrian : ", "").trim();
        final String levelBbm = find(R.id.sp_bbm, Spinner.class).getSelectedItem().toString();
        final String tidakMenunggu = find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String konfirmTambahan = find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String buangPart = find(R.id.cb_buangPart_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String[] tglAmbil = {find(R.id.tv_tgl_ambil, TextView.class).getText().toString()};
        final String jamAmbil = find(R.id.tv_jam_ambil, TextView.class).getText().toString();
        final String sk = find(R.id.cb_aggrement_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String hari = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(0, 2);
        final String jam = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(3, 5);
        final String menit = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(6, 8);
        String estimasiSebelum = find(R.id.et_mulaiWaktu_checkin, TextView.class).getText().toString();
        estimasiSebelum = estimasiSebelum.isEmpty() ? currentDateTime() + " " + currentDateTime("HH:mm") : estimasiSebelum;
        final String estimasiSelesai = find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString();
        final String noPonsel = getData.get("noPonsel").asString();
        final String nopol = getData.get("nopol").asString();

        final String finalEstimasiSebelum = estimasiSebelum;
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jenisCheckin", "4");
                args.put("id", getData.get("CHECKIN_ID").asString());
                args.put("status", isHplusPartKosong ? "TUNGGU DP" : status);
                args.put("mekanik", namaMekanik);
                args.put("mekanikId", idMekanik);
                args.put("antrian", antrian);
                args.put("noAntrian", find(R.id.et_no_antrian_checkin4, EditText.class).getText().toString());
                args.put("levelBbm", levelBbm);
                args.put("tidakmenunggu", tidakMenunggu);
                args.put("konfirmtambahan", konfirmTambahan);
                args.put("buangpart", buangPart);
                if (find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).isChecked()) {
                    if (tglAmbil[0].isEmpty()) {
                        tglAmbil[0] = currentDateTime("dd/MM");
                    } else {
                        tglAmbil[0] = Tools.formatDate(tglAmbil[0], "dd/MM");
                    }
                    args.put("waktuambil", tglAmbil[0] + " " + jamAmbil);
                } else {
                    args.put("waktuAmbi", "");
                }

                args.put("sk", sk);
                args.put("waktuLayananHari", hari);
                args.put("waktuLayananJam", jam);
                args.put("waktuLayananHMenit", menit);
                args.put("estimasiSebelum", finalEstimasiSebelum);
                args.put("estimasiSelesai", estimasiSelesai);
                args.put("keterangan", find(R.id.et_ket_checkin4, EditText.class).getText().toString());
                args.put("lokasiLayanan", "BENGKEL");
                args.put("noPonsel", noPonsel);
                args.put("nopol", nopol);
                args.put("ttd", ttdPath);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    AppApplication.getMessageTrigger();
                    Intent intent = new Intent(getActivity(), KontrolLayanan_Activity.class);
                    intent.putExtra("NOPOL", nopol);
                    Log.d(TAG, "runUI: " + result.get("data"));
                    noKunciList.asArray().addAll(result.get("data").asArray());
                    if (isHplusPartKosong) {
                        showSuccess("MOHON BAYARKAN UANG MUKA PELAYANAN " + jenisLayanan + ". RINCIAN BIAYA & UANG MUKA", Toast.LENGTH_LONG);
                        showDialogNoKunci(result.get("data").get("NO_KUNCI").asString());
                        showNotification(getActivity(), "Checkin Antrian ", formatNopol(nopol), "CHECKIN", intent);
                    } else {
                        if (status.equalsIgnoreCase("BATAL CHECKIN 4")) {
                            showSuccess("LAYANAN DI BATALKAN, DATA DI MASUKKAN KE DAFTAR KONTROL LAYANAN", Toast.LENGTH_LONG);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            showDialogNoKunci(result.get("data").get("NO_KUNCI").asString());
                            showNotification(getActivity(), "Checkin Antrian ", formatNopol(nopol), "CHECKIN", intent);
                            showSuccess("DATA PELANGGAN BERHASIL DI MASUKKAN KE DAFTAR KONTROL LAYANAN", Toast.LENGTH_LONG);
                        }
                    }
                } else {
                    showWarning(result.get("message").asString(), Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void showDialogNoKunci(String noKunci) {
        Messagebox.showDialog(getActivity(), "N0. KUNCI", noKunci, "Ya", "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                finish();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

    }

    private String generateNoKunci(Nson noList) {
        List<Integer> smallest = new ArrayList<>();
        String result = "";
        for (int i = 0; i < noList.size(); i++) {
            for (int j = 0; j < 20; j++) {
                if (noList.get(i).asInteger() < j) {
                    smallest.add(j);
                }
            }
        }
        Log.d(TAG, "generateNoKunci: " + smallest);
        return result;
    }


    private void setSpBbm() {
        List<String> lvlBbmList = new ArrayList<>();
        lvlBbmList.add("--PILIH--");
        for (int i = 10; i <= 100; i++) {
            if (i % 10 == 0)
                lvlBbmList.add(i + "%");
        }
        setSpinnerOffline(lvlBbmList, find(R.id.sp_bbm, Spinner.class), "");
    }

    private void setSpMekanik(final String namaMekanik) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("penugasan", "CHECKIN");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
//                    if (data.get("data").asArray().size() == 0) {
//                        showInfo("Mekanik Belum Tercatatkan, Silahkan Daftarkan Mekanik Di Menu USER");
//                        Messagebox.showDialog(getActivity(), "Mekanik Belum Di Catatkan", "Catatkan Mekanik ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                startActivityForResult(new Intent(getActivity(), AturUser_Activity.class), REQUEST_MEKANIK);
//                            }
//                        }, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        return;
//                    }
                    mekanikArray.asArray().clear();
                    mekanikArray.add("--PILIH--");
                    idMekanikArray.add(0);
                    for (int i = 0; i < data.get("data").size(); i++) {
                        idMekanikArray.add(Nson.newObject()
                                .set("ID", data.get("data").get(i).get("ID").asString())
                                .set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }
                    Log.d("id__", "array: " + idMekanikArray);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mekanikArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    find(R.id.sp_namaMekanik_checkin4, Spinner.class).setAdapter(spinnerAdapter);
                    if (!namaMekanik.isEmpty()) {
                        for (int i = 0; i < find(R.id.sp_namaMekanik_checkin4, Spinner.class).getCount(); i++) {
                            if (find(R.id.sp_namaMekanik_checkin4, Spinner.class).getItemAtPosition(i).equals(namaMekanik)) {
                                find(R.id.sp_namaMekanik_checkin4, Spinner.class).setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    Messagebox.showDialog(getActivity(), "Nama Mekanik Gagal Di Muat, Muat Ulang ?", "Muat Ulang ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpMekanik("");
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        find(R.id.sp_namaMekanik_checkin4, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getSelectedItem().toString().equals("--PILIH--")) {
                    isMekanik = true;
                    if (isExpressAndStandard) {
                        loadAvailMekanik(parent.getSelectedItem().toString(), find(R.id.tv_jenis_antrian, TextView.class).getText().toString());
                    }
                    if (idMekanikArray.get(position).get("NAMA").asString().equals(parent.getSelectedItem().toString())) {
                        idMekanik = idMekanikArray.get(position).get("ID").asString();
                        Log.d("id__", "onItemSelected: " + idMekanik);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadAvailMekanik(final String mekanik, final String antrian) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("penugasan", "CHECKIN");
                args.put("lokasi", "BENGKEL");
                args.put("mekanik", mekanik);
                args.put("antrian", antrian.replace("Jenis Antrian : ", ""));

                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    //totalWaktu(data);
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tgl_estimasi_checkin4:
                getDatePickerCheckin();
                break;
            case R.id.tv_tgl_ambil:
                getDatePickerTglAmbil();
                break;
            case R.id.tv_jam_ambil:
                getTimePickerDialogWaktuAmbil();
                break;
            case R.id.tv_jam_estimasi_checkin4:
                getTimePickerDialogEstimasiSelesai();
                break;
            case R.id.btn_hapus:
                isBatal = true;
                if (find(R.id.et_ket_checkin4, EditText.class).getText().toString().isEmpty()) {
                    find(R.id.et_ket_checkin4, EditText.class).setError("Keterangan Perlu Di isi");
                } else {
                    saveData("BATAL CHECKIN");
                }
                break;
            case R.id.btn_simpan:
                if (!find(R.id.cb_aggrement_checkin4, CheckBox.class).isChecked()) {
                    showWarning("SILAHKAN SETUJUI SYARAT DAN KETENTUAN BENGKEL", Toast.LENGTH_LONG);
                } else if (!isSign) {
                    showWarning("TANDA TANGAN WAJIB DI INPUT", Toast.LENGTH_LONG);
                } else if (find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).isChecked() &&
                        find(R.id.tv_jam_ambil, TextView.class).getText().toString().isEmpty()) {
                    showWarning("WAKTU AMBIL HARUS DI ISI", Toast.LENGTH_LONG);
                } else {
                    if (find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).isChecked() &&
                            find(R.id.sp_bbm, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                        find(R.id.sp_bbm, Spinner.class).performClick();
                        find(R.id.sp_bbm, Spinner.class).requestFocus();
                        showWarning("LEVEL BBM BELUM DI PILIH", Toast.LENGTH_LONG);
                    } else if ((isHplusPartKosong || isHpLus) &&
                            find(R.id.sp_bbm, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("LEVEL BBM BELUM DI PILIH", Toast.LENGTH_LONG);
                    } else if ((isExtra || isHplusPartKosong || isHpLus) &&
                            find(R.id.tv_tgl_estimasi_checkin4, TextView.class).getText().toString().isEmpty()) {
                        showWarning("TANGGAL ESTIMASI HARUS DI ISI", Toast.LENGTH_LONG);
                        find(R.id.tv_jam_estimasi_checkin4, TextView.class).requestFocus();
                    } else if ((isExtra || isHplusPartKosong || isHpLus) &&
                            find(R.id.tv_jam_estimasi_checkin4, TextView.class).getText().toString().isEmpty()) {
                        showWarning("JAM ESTIMASI HARUS DI ISI", Toast.LENGTH_LONG);
                        find(R.id.tv_tgl_estimasi_checkin4, TextView.class).requestFocus();
                    } else {
                        saveData("CHECKIN ANTRIAN");
                    }
                }
                break;
            case R.id.btn_ttd_checkin4:
                if (!checkPermission()) {
                    Intent intent = new Intent(getActivity(), Capture.class);
                    intent.putExtra("NOPOL", getData.get("nopol").asString());
                    startActivityForResult(intent, REQUEST_CODE_SIGN);
                } else {
                    if (checkPermission()) {
                        requestPermissionAndContinue();
                    } else {
                        Intent intent = new Intent(getActivity(), Capture.class);
                        startActivityForResult(intent, REQUEST_CODE_SIGN);
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {
                boolean flag = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    find(R.id.btn_ttd_checkin4).performClick();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SIGN) {
            isSign = true;
            @SuppressLint("SdCardPath") File imgFile = null;
            if (data != null) {
                imgFile = (File) Objects.requireNonNull(data.getExtras()).get("imagePath");
            }
            if (imgFile != null && imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ttdPath = FileUtility.encodeToStringBase64(imgFile.getAbsolutePath());
                find(R.id.img_tandaTangan_checkin4, ImageView.class).setImageBitmap(myBitmap);
                if (imgFile.exists()) {
                    imgFile.delete();
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_MEKANIK) {
            setSpMekanik("");
            showSuccess("Berhasil Mencatatkan Mekanik");
        }
    }
}
