package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.MultipartRequest;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.ANTRIAN;
import static com.rkrzmail.utils.APIUrls.ANTRIAN_MULAI;
import static com.rkrzmail.utils.APIUrls.GET_MST_PERLENGKAPAN;
import static com.rkrzmail.utils.APIUrls.ROLLBACK_TRANSACTIONS;
import static com.rkrzmail.utils.APIUrls.SAVE_IMAGE_CHECKIN;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ONEDAY;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CODE_SIGN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Checkin4_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "Checking4____";

    private Bitmap ttdBitmap = null;

    private final Nson mekanikArray = Nson.newArray();
    private final Nson idMekanikArray = Nson.newArray();
    private final Nson penugasanMekanikList = Nson.newArray();
    private Nson fotoKondisiList = Nson.newArray();
    private Nson getData;

    private boolean isSign = false, isBatal = false, isMekanik = false;
    private boolean isExpressAndStandard = false, isExtra = false, isHplusPartKosong = false, isDp = false, isHpLus = false;

    private String waktuLayananHplusExtra = "", jenisLayanan = "", waktuLayananStandartExpress = "";
    private String tglEstimasi = "", waktuEstimasi = "", antrianSebelumnya = "";
    private String ttdBase64 = "";
    private String idMekanik = "";
    private String jenisAntrian = "";
    private StringBuilder builderPerlengkapan = new StringBuilder();

    private int idAntrian = 0;
    private int waktuPesan = 0;
    private int checkinID = 0;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin4);
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        initComponent();
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        showInfoDialog("KONFIRMASI", "Kembali Ke Halaman Sebelumnya?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Checkin4_Activity.super.onBackPressed();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
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
        checkinID = getData.get("CHECKIN_ID").asInteger();
        waktuLayananStandartExpress = getData.get("WAKTU_LAYANAN").asString();
        jenisLayanan = getData.get("JENIS_LAYANAN").asString();
        waktuPesan = getData.get("WAKTU_PESAN").asInteger();
        jenisAntrian = getData.get("JENIS_ANTRIAN").asString();

        find(R.id.et_lamaWaktu_checkin, EditText.class).setText(waktuLayananStandartExpress);
        find(R.id.tv_jenis_antrian, TextView.class).setText("Jenis Antrian : " + getData.get("JENIS_ANTRIAN").asString());
        setNoAntrian(getData.get("JENIS_ANTRIAN").asString());
        setSpMekanik("");
        setSpBbm();
        setSpPerlengkapan();

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
        //find(R.id.btn_foto_kondisi).setEnabled(false);
        find(R.id.btn_foto_kondisi).setOnClickListener(this);

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
                waktuAmbil[0] = sdf.format(date);
                find(R.id.tv_tgl_ambil, TextView.class).setText(waktuAmbil[0]);
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
                find(R.id.tv_jam_ambil, TextView.class).setText(waktuAmbil[0]);
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
                    if (!waktuLayananStandartExpress.isEmpty()) {
                        int waktuJam = Integer.parseInt(waktuLayananStandartExpress.substring(3, 5));
                        int waktuMenit = Integer.parseInt(waktuLayananStandartExpress.substring(6, 8));
                        find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(totalWaktu(waktuJam, waktuMenit));
                    } else {
                        find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(NumberFormatUtils.formatTime(0, 0));
                    }
                } else {
                    showWarning(result.get("error").asString());
                }
            }
        });
    }

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    private String totalWaktu(int waktuJam, int waktuMenit) {
        if (waktuJam == 0 && waktuMenit == 0) return NumberFormatUtils.formatTime(0, 0);
        Tools.TimePart timePart = Tools.TimePart.parse("00:" + currentDateTime("HH:mm"));
        timePart.add(Tools.TimePart.parse(String.format("%02d:%02d:%02d", 0, waktuJam, waktuMenit)));
        return timePart.toString().substring(3, 8);
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
        estimasiSebelum = estimasiSebelum.isEmpty() ? currentDateTime("HH:mm") : estimasiSebelum;
        final String estimasiSelesai = find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString();
        final String noPonsel = getData.get("noPonsel").asString();
        final String nopol = getData.get("nopol").asString();

        final String finalEstimasiSebelum = estimasiSebelum;

        if (fotoKondisiList.size() > 0) {
            uploadFotoKondisi();
        }

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                ttdBase64 = bitmapToBase64(ttdBitmap);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jenisCheckin", "4");
                args.put("id", String.valueOf(checkinID));
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
                    args.put("waktuambil", "");
                }

                args.put("sk", sk);
                args.put("waktuLayananHari", hari);
                args.put("waktuLayananJam", jam);
                args.put("waktuLayananMenit", menit);
                args.put("estimasiSebelum", finalEstimasiSebelum);
                args.put("estimasiSelesai", estimasiSelesai);
                args.put("keterangan", find(R.id.et_ket_checkin4, EditText.class).getText().toString());
                args.put("lokasiLayanan", "BENGKEL");
                args.put("noPonsel", noPonsel);
                args.put("nopol", nopol);
                args.put("ttd", ttdBase64);
                args.put("tinggalkanSTNK", find(R.id.cb_tinggalkan_stnk, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("partbook", getIntentStringExtra("PART_LIST"));
                args.put("jasabook", getIntentStringExtra("JASA_LIST"));
                args.put("perlengkapan", builderPerlengkapan.toString());
                args.put("fotoKondisiList", fotoKondisiList.toJson());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    AppApplication.getMessageTrigger();
                    Intent intent = new Intent(getActivity(), KontrolLayanan_Activity.class);
                    intent.putExtra("NOPOL", nopol);

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

    private void setSpPerlengkapan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "action=get";
                args[1] = "kendaraan=" + getSetting("JENIS_KENDARAAN");
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_MST_PERLENGKAPAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    List<String> perlengkapanList = new ArrayList<>();
                    result = result.get("data");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            perlengkapanList.add(result.get(i).get("NAMA_PERLENGKAPAN").asString());
                        }
                    }

                    find(R.id.sp_perlengkapan, MultiSelectionSpinner.class).setItems(perlengkapanList);
                    find(R.id.sp_perlengkapan, MultiSelectionSpinner.class).setTittle("Pilih Perlengkapan");
                    find(R.id.sp_perlengkapan, MultiSelectionSpinner.class).setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedIndices(List<Integer> indices) {

                        }

                        @Override
                        public void selectedStrings(List<String> strings) {
                            if (strings.size() > 0) {
                                builderPerlengkapan = new StringBuilder();
                                for (String perlengkapan : strings) {
                                    builderPerlengkapan.append(perlengkapan).append(", ");
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {
                boolean flag = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    showWarning("Ijinkan Aplikasi Untuk Mengakses Camera dan Dokumen");
                }
            }
        }
    }

    private void rollbackCheckin() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "ROLLBACK CHECKIN");
                args.put("checkinID", String.valueOf(checkinID));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ROLLBACK_TRANSACTIONS), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equals("OK")) {
                    showSuccess("TRANSAKSI BERHASIL DI BATALKAN");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("TRANSAKSI ERROR, SILAHKAN HUBUNGI ADMINISTRATOR", Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void getImagePickOrCamera(int requestCode) {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{WRITE_EXTERNAL_STORAGE
                    , READ_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            final List<Intent> intents = new ArrayList<>();
            intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

            Intent result = Intent.createChooser(intents.remove(0), null);
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
            startActivityForResult(result, requestCode);
        }
    }

    ImageView imgDepan, imgBelakang, imgKiri, imgKanan, imgTambahan1, imgTambahan2;

    @SuppressLint("InflateParams")
    private void showDialogFotoKondisi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_foto_kondisi, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        initToolbarFoto(dialogView);

        imgDepan = dialogView.findViewById(R.id.img_kondisi_depan);
        imgBelakang = dialogView.findViewById(R.id.img_kondisi_belakang);
        imgKanan = dialogView.findViewById(R.id.img_kondisi_kanan);
        imgKiri = dialogView.findViewById(R.id.img_kondisi_kiri);
        imgTambahan1 = dialogView.findViewById(R.id.img_kondisi_tambahan1);
        imgTambahan2 = dialogView.findViewById(R.id.img_kondisi_tambahan2);

        Nson bitmap = fotoKondisiList.get(0);
        final Nson saveFotoList = Nson.newArray();

        setFotoKondisi(imgDepan, bitmap.get("depan").asBitmap());
        setFotoKondisi(imgBelakang, bitmap.get("belakang").asBitmap());
        setFotoKondisi(imgKanan, bitmap.get("kanan").asBitmap());
        setFotoKondisi(imgKiri, bitmap.get("kiri").asBitmap());
        setFotoKondisi(imgTambahan1, bitmap.get("tambahan1").asBitmap());
        setFotoKondisi(imgTambahan2, bitmap.get("tambahan2").asBitmap());

        imgDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fotoKondisiList.get(0).get("depan").asBitmap() != null) {
                    notifyDeleteFoto("depan", imgDepan);
                } else {
                    getImagePickOrCamera(1001);
                }
            }
        });

        imgBelakang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fotoKondisiList.get(0).get("belakang").asBitmap() != null) {
                    notifyDeleteFoto("belakang", imgBelakang);
                } else {
                    getImagePickOrCamera(1002);
                }
            }
        });

        imgKanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fotoKondisiList.get(0).get("kanan").asBitmap() != null) {
                    notifyDeleteFoto("kanan", imgKanan);
                } else {
                    getImagePickOrCamera(1003);
                }
            }
        });

        imgKiri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fotoKondisiList.get(0).get("kiri").asBitmap() != null) {
                    notifyDeleteFoto("kiri", imgKiri);
                } else {
                    getImagePickOrCamera(1004);
                }
            }
        });

        imgTambahan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fotoKondisiList.get(0).get("tambahan1").asBitmap() != null) {
                    notifyDeleteFoto("tambahan1", imgTambahan1);
                } else {
                    getImagePickOrCamera(1005);
                }
            }
        });

        imgTambahan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fotoKondisiList.get(0).get("tambahan2").asBitmap() != null) {
                    notifyDeleteFoto("tambahan2", imgTambahan2);
                } else {
                    getImagePickOrCamera(1006);
                }
            }
        });

        dialogView.findViewById(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFotoList.add(Nson.newObject()
                        .set("depan", getBitmap(imgDepan))
                        .set("belakang", getBitmap(imgBelakang))
                        .set("kanan", getBitmap(imgKanan))
                        .set("kiri", getBitmap(imgKiri))
                        .set("tambahan1", getBitmap(imgTambahan1))
                        .set("tambahan2", getBitmap(imgTambahan2))
                );
                fotoKondisiList = saveFotoList;
                if (alertDialog.isShowing())
                    alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btn_batal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (
                            saveFotoList.get(0).get("depan").asBitmap() != null ||
                                    saveFotoList.get(0).get("belakang").asBitmap() != null ||
                                    saveFotoList.get(0).get("kanan").asBitmap() != null ||
                                    saveFotoList.get(0).get("kiri").asBitmap() != null ||
                                    saveFotoList.get(0).get("tambahan1").asBitmap() != null ||
                                    saveFotoList.get(0).get("tambahan2").asBitmap() != null
                    ) {
                        showInfoDialog("Konfimasi", "Batal Simapn Foto ?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fotoKondisiList.asArray().clear();
                                if (alertDialog.isShowing())
                                    alertDialog.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                    } else {
                        if (alertDialog.isShowing())
                            alertDialog.dismiss();
                    }
                } catch (Exception e) {
                    alertDialog.dismiss();
                }
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog = builder.show();
    }

    private Bitmap getBitmap(ImageView imageView) {
        try {
            if (imageView == null || imageView.getTag().equals("original")) return null;

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            return drawable.getBitmap();
        } catch (Exception e) {
            return null;
        }
    }

    private void setFotoKondisi(ImageView imageView, Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            setSizeFotoKondisi(imageView, false);
        } else {
            imageView.setImageResource(R.drawable.icon_camera_fill);
            setSizeFotoKondisi(imageView, true);
        }
    }

    private void setSizeFotoKondisi(ImageView imageView, boolean isIcon) {
        if (isIcon) {
            imageView.setTag("original");
            imageView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            80,
                            80)
            );
        } else {
            imageView.setTag("ready");
            imageView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT)
            );
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.requestLayout();
        }

    }

    @SuppressLint("NewApi")
    private void initToolbarFoto(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Foto Kondisi");
    }

    private void notifyDeleteFoto(final String name, final ImageView imageView) {
        showInfoDialog("Konfirmasi", "Hapus Foto ?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fotoKondisiList.remove(fotoKondisiList.get(0).get(name));
                imageView.setTag("original");
                setFotoKondisi(imageView, null);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void getImageUri(final Uri imageUri, final Bundle imgBundle, final ImageView imgKondisi) {
        final Bitmap[] bitmap = {null};
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    ImageView dummy = new ImageView(getActivity());
                    dummy.setImageURI(imageUri);
                    BitmapDrawable drawable = (BitmapDrawable) dummy.getDrawable();
                    bitmap[0] = drawable.getBitmap();
                } else {
                    bitmap[0] = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap[0] != null) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap[0].compress(Bitmap.CompressFormat.PNG, 0, bos);
                        }
                    }
                });
            }

            @Override
            public void runUI() {
                if (bitmap[0] != null && imgKondisi != null) {
                    bitmap[0] = Tools.getResizedBitmap(bitmap[0], 500);
                    setSizeFotoKondisi(imgKondisi, false);
                    imgKondisi.setImageBitmap(bitmap[0]);
                }
            }
        });
    }

    private void notificationUploadFoto(int progress, boolean isSuccessfuly) {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        @SuppressLint("InlinedApi") int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, "Upload Foto Kondisi", importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.speed)
                .setContentTitle("Upload Foto")
                .setContentText("Mengupload Foto")
                .setProgress(100, progress, false)
                .setPriority(Notification.PRIORITY_LOW);

        notificationManager.notify(notificationId, mBuilder.build());
        if (isSuccessfuly) {
            notificationManager.cancel(notificationId);
        }
    }

    private void threadAliveChecker(final Thread thread) {
        final int[] progress = {0};
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!thread.isAlive()) {
                    notificationUploadFoto(progress[0], true);
                    timer.cancel();
                } else {
                    progress[0] += 10;
                    notificationUploadFoto(progress[0], false);
                }
            }
        }, 500, 500);
    }

    private void uploadFotoKondisi() {
        Thread uploadThread = new Thread(new Runnable() {
            Nson response;

            @Override
            public void run() {
                Nson photoUpload = fotoKondisiList.get(0);

                MultipartRequest request = new MultipartRequest(getActivity());
                request.addString("CID", getSetting("CID"));
                request.addString("checkinID", String.valueOf(checkinID));
                request.addString("nopol", getData.get("nopol").asString());
                request.addString("jenisFoto", "fotoKondisi");

                request.addImageFile("depan", fotoKondisiNaming("depan.png"), getBitmapAsByte(photoUpload.get("depan").asBitmap()));
                request.addImageFile("belakang", fotoKondisiNaming("belakang.png"), getBitmapAsByte(photoUpload.get("belakang").asBitmap()));
                request.addImageFile("kanan", fotoKondisiNaming("kanan.png"), getBitmapAsByte(photoUpload.get("kanan").asBitmap()));
                request.addImageFile("kiri", fotoKondisiNaming("kiri.png"), getBitmapAsByte(photoUpload.get("kiri").asBitmap()));
                request.addImageFile("tambahan1", fotoKondisiNaming("tambahan1.png"), getBitmapAsByte(photoUpload.get("tambahan1").asBitmap()));
                request.addImageFile("tambahan2", fotoKondisiNaming("tambahan2.png"), getBitmapAsByte(photoUpload.get("tambahan2").asBitmap()));

                response = Nson.readJson(request.execute(AppApplication.getBaseUrlV4(SAVE_IMAGE_CHECKIN)));
            }
        });

        uploadThread.start();
        threadAliveChecker(uploadThread);
    }

    private String fotoKondisiNaming(String name) {
        return currentDateTime("ddMMyyyy") + "-" + getData.get("nopol").asString() + "-" + name;
    }


    private byte[] getBitmapAsByte(Bitmap bitmap) {
        if (bitmap == null) return new byte[]{};

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    private void validateSaveData() {
        if (!find(R.id.cb_aggrement_checkin4, CheckBox.class).isChecked()) {
            showWarning("SILAHKAN SETUJUI SYARAT DAN KETENTUAN BENGKEL", Toast.LENGTH_LONG);
        } else if (ttdBitmap == null) {
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
                validateSaveData();

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
            case R.id.btn_foto_kondisi:
                showDialogFotoKondisi();
                break;
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras = null;
            Uri imageUri = null;
            try {
                if (data != null) {
                    extras = data.getExtras();
                    if (extras == null)
                        imageUri = data.getData();
                }

            } catch (Exception e) {
                showError(e.getMessage(), Toast.LENGTH_LONG);
            }

            switch (requestCode) {
                case REQUEST_CODE_SIGN:
                    isSign = true;
                    // @SuppressLint("SdCardPath") File imgFile = null;
                    Bitmap bitmap = null;
                    if (data != null) {
                        byte[] bytes = data.getByteArrayExtra("bytesBitmap");
                        ttdBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                    if (ttdBitmap != null) {
                        find(R.id.img_tandaTangan_checkin4, ImageView.class).setImageBitmap(ttdBitmap);
                    }

                    break;
                case REQUEST_MEKANIK:
                    setSpMekanik("");
                    showSuccess("Berhasil Mencatatkan Mekanik");
                    break;
                case 1001:
                    getImageUri(imageUri, extras, imgDepan);
                    break;
                case 1002:
                    getImageUri(imageUri, extras, imgBelakang);
                    break;
                case 1003:
                    getImageUri(imageUri, extras, imgKanan);
                    break;
                case 1004:
                    getImageUri(imageUri, extras, imgKiri);
                    break;
                case 1005:
                    getImageUri(imageUri, extras, imgTambahan1);
                    break;
                case 1006:
                    getImageUri(imageUri, extras, imgTambahan2);
                    break;
            }

        }
    }
}
