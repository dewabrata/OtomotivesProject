package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.Capture;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.SET_ANTRIAN;
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

    private boolean isSign = false, isBatal = false, isMekanik = false;
    private boolean isExpressAndStandard = false, isExtra = false, isHplus = false, isDp = false;
    private String waktuLayananHplusExtra = "", jenisLayanan = "", waktuLayananStandartExpress = "";
    private String tglEstimasi = "", waktuEstimasi = "", antrianSebelumnya = "";
    private int idMekanik = 0;
    private int idAntrian = 0;
    private int waktuPesan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin4_);
        initComponent();
        ttd = null;
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        initToolbar();
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
        Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), false);
        Log.d("coba__", "DATA: " + getData);
        setSpMekanik("");
        setSpBbm();
        setNoAntrian(getData.get("JENIS_ANTRIAN").asString());

        waktuLayananStandartExpress = getData.get("WAKTU_LAYANAN").asString();
        jenisLayanan = getData.get("JENIS_LAYANAN").asString();
        waktuPesan = getData.get("WAKTU_PESAN").asInteger();

        find(R.id.et_lamaWaktu_checkin, EditText.class).setText(waktuLayananStandartExpress);
        find(R.id.tv_jenis_antrian, TextView.class).setText("Jenis Antrian : " + getData.get("JENIS_ANTRIAN").asString());

        if (getData.get("JENIS_ANTRIAN").asString().equals("EXTRA")) {
            isExtra = true;
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
            find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
        } else if (getData.get("JENIS_ANTRIAN").asString().equals("H+")) {
            isHplus = true;
            find(R.id.et_dp_checkin4, EditText.class).setText(RP + formatRp(getData.get("DP").asString()));
            find(R.id.et_sisa_checkin4, EditText.class).setText(RP + formatRp(getData.get("SISA").asString()));
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
            find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
            find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setEnabled(false);
        } else {
            isExpressAndStandard = true;
            viewAntrianStandartExpress(getData.get("JENIS_ANTRIAN").asString());
        }
        try {
            find(R.id.et_totalBiaya_checkin4, EditText.class).setText(getData.get("TOTAL").asString());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void initListener() {
        find(R.id.tv_waktu_checkin4, TextView.class).setOnClickListener(this);
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
                if(compoundButton.isChecked()){
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), true);
                    find(R.id.tv_disable_waktu_antar).setVisibility(View.GONE);
                }else{
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), false);
                    find(R.id.tv_disable_waktu_antar).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private Calendar parseWaktuPesan(){
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }

        long pesanPart = waktuPesan * ONEDAY;
        long totalDate = current + pesanPart;
        Log.d(TAG, "parseWaktuPesan: " + current);
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
                    int totalHarga =  Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBiaya_checkin4, EditText.class).getText().toString()));
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
        datePickerDialog.setMinDate(parseWaktuPesan());
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

    public void getTimePickerDialogWaktuAmbil() {
        final String[] waktuAmbil = {""};
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
                waktuAmbil[0] = formattedTime;
                find(R.id.tv_waktu_checkin4, TextView.class).setText(formattedTime);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    if (validateWaktuAmbil(waktuEstimasi, waktuAmbil[0])) {
                        showWarning("Waktu Ambil Harus Melebihi Estimasi Selesai");
                        find(R.id.tv_waktu_checkin4, TextView.class).setText("");
                        find(R.id.tv_waktu_checkin4, TextView.class).performClick();
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

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_ANTRIAN), args));
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
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("status", jenisAntrian);
                args.put("spec", "Bengkel");
                args.put("statusantri", jenisAntrian);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_ANTRIAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    idAntrian = result.asInteger();
                    find(R.id.et_no_antrian_checkin4, EditText.class).setText(generateNoAntrian(jenisAntrian, result.asString()));
                    Log.d(TAG, "NO_ANTRIAN: " + generateNoAntrian(jenisAntrian, result.asString()));
                }
            }
        });
    }

    private void viewAntrianStandartExpress(final String antrian) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("antrian", antrian);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_ANTRIAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), false);
                    find(R.id.tv_disable_estimasi).setVisibility(View.VISIBLE);
                    totalWaktu(result);
                } else {
                    showWarning(result.get("message").asString());
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

    @SuppressLint("DefaultLocale")
    public static String totalWaktu(String hari, String jam, String menit) {
        String[] result = new String[3];
        result[0] = hari;
        result[1] = jam;
        result[2] = menit;

        int incrementWaktu = 0;
        int calculateJam = 0;
        int calculateHari = 0;

        if (!menit.equals("0")) {
            int minutes = Integer.parseInt(menit);
            while (minutes >= 60) {
                incrementWaktu++;
                minutes -= 60;
            }
            if (incrementWaktu > 0) {
                calculateJam = incrementWaktu;
                result[2] = String.valueOf(minutes);
            }
        } else {
            result[2] = "0";
        }
        if (!jam.equals("0") || calculateJam > 0) {
            incrementWaktu = 0;
            int finalJam = Integer.parseInt(jam) + calculateJam;
            result[1] = String.valueOf(finalJam);
            while (finalJam >= 24) {
                incrementWaktu++;
                finalJam -= 24;
            }
            if (incrementWaktu > 0) {
                calculateHari = incrementWaktu;
            }
        } else {
            result[1] = "0";
        }
        if (!hari.equals("0") || calculateHari > 0) {
            int finalJam = Integer.parseInt(hari) + calculateHari;
            result[0] = String.valueOf(finalJam);
        } else {
            result[0] = "0";
        }

        return String.format("%02d:%02d:%02d", Integer.parseInt(result[0]), Integer.parseInt(result[1]), Integer.parseInt(result[2]));
    }

    private boolean validateWaktuAmbil(String jamEstimasi, String jamAmbil) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        Date waktuAmbil = sdf.parse(jamAmbil);
        Date waktuEstimasi = sdf.parse(jamEstimasi);

        return !waktuAmbil.after(waktuEstimasi);
    }

    private void saveData(final String status) {
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        final String namaMekanik = find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString().contains("--PILIH--") ? "" : find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString();
        final String antrian = find(R.id.tv_jenis_antrian, TextView.class).getText().toString().replace("Jenis Antrian : ", "").trim();
        final String levelBbm = find(R.id.sp_bbm, Spinner.class).getSelectedItem().toString();
        final String tidakMenunggu = find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String konfirmTambahan = find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String buangPart = find(R.id.cb_buangPart_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String waktuAmbil = find(R.id.tv_waktu_checkin4, TextView.class).getText().toString();
        final String sk = find(R.id.cb_aggrement_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String hari = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(0, 2);
        final String jam = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(3, 5);
        final String menit = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(6, 8);
        Log.d(TAG, "hari : " + hari);
        Log.d(TAG, "jam : " + jam);
        Log.d(TAG, "menit : " + menit);
        final String estimasiSebelum = find(R.id.et_mulaiWaktu_checkin, TextView.class).getText().toString();
        final String estimasiSesudah = find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString();
        final String estimasiSelesai = find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString();//currentDateTime("yyyy-MM-dd") + " " +
        //final String ttd = find(R.id.img_tandaTangan_checkin4 , ImageView.class).getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("check", "2");
                args.put("id", nson.get("id").asString());
                args.put("status", isHplus ? "TUNGGU DP" : status);
                args.put("mekanik", namaMekanik);
                args.put("mekanikId", String.valueOf(idMekanik));
                args.put("antrian", antrian);
                args.put("noAntrian", find(R.id.et_no_antrian_checkin4, EditText.class).getText().toString());
                args.put("levelBbm", levelBbm);
                args.put("tidakmenunggu", tidakMenunggu);
                args.put("konfirmtambahan", konfirmTambahan);
                args.put("buangpart", buangPart);
                args.put("waktuambil", waktuAmbil);
                args.put("sk", sk);
                args.put("waktuLayananHari", hari);
                args.put("waktuLayananJam", jam);
                args.put("waktuLayananHMenit", menit);
                args.put("estimasiSebelum", estimasiSebelum);
                args.put("estimasiSesudah", estimasiSesudah);
                args.put("estimasiSelesai", estimasiSelesai);
                args.put("keterangan", find(R.id.et_ket_checkin4, EditText.class).getText().toString());
                //args.put("ttd", ttd);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(isHplus){
                        showSuccess("MOHON BAYARKAN UANG MUKA PELAYANAN " + jenisLayanan + ". RINCIAN BIAYA & UANG MUKA");
                    }else{
                        if (status.equalsIgnoreCase("BATAL CHECKIN 4")) {
                            showSuccess("Layanan di Batalkan, Data Di masukkan Ke Daftar Kontrol Layanan");
                        } else {
                            showSuccess("Data Pelanggan Berhasil Di masukkan Ke Daftar Kontrol Layanan");
                        }
                    }

                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void setSpBbm(){
        List<String> lvlBbmList = new ArrayList<>();
        for (int i = 10; i <= 100; i++) {
            if(i % 10 == 0)
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
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    if (data.get("data").asArray().size() == 0) {
                        showInfo("Mekanik Belum Tercatatkan, Silahkan Daftarkan Mekanik Di Menu USER");
                        Messagebox.showDialog(getActivity(), "Mekanik Belum Di Catatkan", "Catatkan Mekanik ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturUser_Activity.class), REQUEST_MEKANIK);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    mekanikArray.asArray().clear();
                    mekanikArray.add("--PILIH--");
                    idMekanikArray.add(0);
                    for (int i = 0; i < data.get("data").size(); i++) {
                        idMekanikArray.add(Nson.newObject().set("ID", data.get("data").get(i).get("ID").asString()).set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }

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
                    if(idMekanikArray.get(position).get("NAMA").asString().equals(parent.getSelectedItem().toString())){
                        idMekanik = idMekanikArray.get(position).get("ID").asInteger();
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
                    totalWaktu(data);
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
            case R.id.tv_waktu_checkin4:
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
                    showWarning("Silahkan Setujui Syarat Dan Ketentuan Bengkel");
                }
                /*else if(find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString().equals("--PILIH--")){
                    showWarning("Nama Mekanik Belum Di pilih");
                    find(R.id.sp_namaMekanik_checkin4, Spinner.class).performClick();
                }*/
                else if (!isSign) {
                    showWarning("Tanda Tangan Wajib di Input");
                } else {
                    if (!find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                        saveData("CHECKIN ANTRIAN PENUGASAN");
                    } else {
                        saveData("CHECKIN ANTRIAN");
                    }
                }
                break;
            case R.id.btn_ttd_checkin4:
                if (!checkPermission()) {
                    Intent intent = new Intent(getActivity(), Capture.class);
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
                find(R.id.img_tandaTangan_checkin4, ImageView.class).setImageBitmap(myBitmap);
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_MEKANIK) {
            setSpMekanik("");
            showSuccess("Berhasil Mencatatkan Mekanik");
        }
    }
}
