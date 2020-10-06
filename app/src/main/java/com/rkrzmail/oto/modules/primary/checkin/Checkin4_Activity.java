package com.rkrzmail.oto.modules.primary.checkin;

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
import com.rkrzmail.oto.modules.user.AturUser_Activity;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.SET_ANTRIAN;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CODE_SIGN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;

public class Checkin4_Activity extends AppActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "Checking4____";
    private Bitmap ttd;
    private SeekBar seekBar;
    private Nson mekanikArray = Nson.newArray();
    private boolean isSign = false, isBatal = false;
    private long oneDay = 86400000;
    private String waktuLayananHplusExtra = "", jenisLayanan = "", waktuLayananStandartExpress = "";
    private String tglEstimasi = "", waktuEstimasi = "";
    private boolean isExpressAndStandard = false, isExtraAndHplus = false;
    private Nson getData;
    private int idAntrian = 0;

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
            find(R.id.et_ket_checkin4, EditText.class).requestFocus();
            showInfo("Silahkan Isi Keterangan Batal");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.putExtra("alasanBatal", find(R.id.et_ket_checkin4, EditText.class).getText().toString());
                    setResult(RESULT_OK, i);
                    finish();
                }
            });
            return;
        }

        initData();
        initListener();
        setSpMekanik();

        find(R.id.cb_aggrement_checkin4, CheckBox.class);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        getData = Nson.readJson(getIntentStringExtra(DATA));
        Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), false);
        waktuLayananStandartExpress = getData.get("WAKTU_LAYANAN").asString();
        idAntrian = getData.get("ID_ANTRIAN").asInteger();
        jenisLayanan = getData.get("JENIS_LAYANAN").asString();
        find(R.id.et_no_antrian_checkin4, EditText.class).setText(getData.get("NO_ANTRIAN").asString());
        find(R.id.et_lamaWaktu_checkin, EditText.class).setText(waktuLayananStandartExpress);
        find(R.id.tv_jenis_antrian, TextView.class).setText(getData.get("JENIS_ANTRIAN").asString());

        if (getData.get("JENIS_ANTRIAN").asString().equals("EXTRA")) {
            isExtraAndHplus = true;
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
            find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
        } else if (getData.get("JENIS_ANTRIAN").asString().equals("H+")) {
            isExtraAndHplus = true;
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
            find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setEnabled(false);
            find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
        } else {
            isExpressAndStandard = true;
            Tools.TimePart waktuMulai = Tools.TimePart.parse("00:" + currentDateTime("hh:mm"));
            find(R.id.et_mulaiWaktu_checkin, TextView.class).setText(waktuMulai.toString());
            Tools.TimePart waktuLayanan = Tools.TimePart.parse(waktuLayananStandartExpress);
            Tools.TimePart totalWaktuSelesai = waktuMulai.add(waktuLayanan);
            find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(totalWaktuSelesai.toString());
            Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), false);
            find(R.id.tv_disable_estimasi).setVisibility(View.VISIBLE);
            find(R.id.tv_tgl_estimasi_checkin4, TextView.class).setText(currentDateTime());
            find(R.id.tv_jam_estimasi_checkin4, TextView.class).setText(find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString().substring(3, 8));
        }

        try {
            find(R.id.et_totalBiaya_checkin4, EditText.class).setText(getData.get("TOTAL").asString());
        } catch (Exception e) {
            showError(e.getMessage());
        }

        Log.d(TAG, "initComponent: " + getData);
    }

    private void initListener() {
        find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).setOnCheckedChangeListener(this);
        find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setOnCheckedChangeListener(this);
        find(R.id.tv_waktu_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.tv_tgl_estimasi_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.tv_jam_estimasi_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
        find(R.id.btn_hapus, Button.class).setText("BATAL");
        find(R.id.btn_hapus, Button.class).setOnClickListener(this);
        find(R.id.btn_simpan, Button.class).setOnClickListener(this);

        seekBar = findViewById(R.id.seekBar_bbm);
        seekBar.setMax(100);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                find(R.id.tv_ketBbbm_checkin4, TextView.class).setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        find(R.id.btn_ttd_checkin4).setOnClickListener(this);
    }



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
                isExtraAndHplus = true;

                try{
                    String totalBiaya = find(R.id.et_totalBiaya_checkin4, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
                    find(R.id.et_dp_checkin4, EditText.class).setText(String.valueOf(calculateDp(Integer.parseInt(totalBiaya))));
                    find(R.id.et_sisa_checkin4, EditText.class).setText("");
                    showInfo(String.valueOf(calculateDp(Integer.parseInt(totalBiaya))));
                }catch (Exception e){
                    showError(e.getMessage());
                }

                Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), true);
                find(R.id.tv_disable_estimasi).setVisibility(View.GONE);
                find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setEnabled(false);
                find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setChecked(true);
            } else {
                isExtraAndHplus = false;
                result = jenisLayanan;
            }
            showInfo(result);
        }
        return result;
    }

    private int calculateDp(int totalBiaya){
        if(totalBiaya > 0 && getSetting("DP_PERSEN") != null){
           return (int) ( (double) Integer.parseInt(getSetting("DP_PERSEN")) * totalBiaya ) / 100;
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
                if(isExtraAndHplus){
                    find(R.id.et_selesaiWaktu_checkin, TextView.class).setText(waktuLayananHplusExtra);
                }
                try {
                    find(R.id.tv_jenis_antrian, TextView.class).setText(parseEstimasiSelesai(tglEstimasi, getData.get("JENIS_ANTRIAN").asString()));
                    Log.d(TAG, "WAKTU: " + parseEstimasiSelesai(tglEstimasi, getData.get("JENIS_ANTRIAN").asString()));
                } catch (ParseException e) {
                    showError(e.getMessage());
                }
                Log.d(TAG, "TGL: " + tglEstimasi);
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
                waktuLayananHplusExtra +=  " " + formattedTime;
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
                    if(validateWaktuAmbil(waktuEstimasi, waktuAmbil[0])){
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

    private void updateAntrian(final String jenisAntrian){
        String antrian = "";
        if (jenisAntrian.equals("H+")) {
            antrian = "H";
        }

        final String finalAntrian = antrian;
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("status", "HPLUS");
                args.put("statusantri",  find(R.id.tv_jenis_antrian, TextView.class).getText().toString());
                args.put("id", String.valueOf(idAntrian));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_ANTRIAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Antrian Terupdate");
                    result = result.get("data").get(0);
                    String updateAntrian = generateNoAntrian(jenisAntrian, result.asString());
                    find(R.id.et_no_antrian_checkin4, EditText.class).setText(updateAntrian);

                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private boolean validateWaktuAmbil(String jamEstimasi, String jamAmbil) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        Date waktuAmbil = sdf.parse(jamAmbil);
        Date waktuEstimasi = sdf.parse(jamEstimasi);

        return !waktuAmbil.after(waktuEstimasi);
    }

    private String generateNoAntrian(String statusAntrian, String noAntrian) {
        String result = "";
        String currentDateTime = Tools.setFormatDayAndMonthFromDb(currentDateTime("yyyy-MM-dd"), "dd/MM");
        if (!noAntrian.equals("")) {
            if (statusAntrian.equals("STANDART")) {
                result = "S" + "." + currentDateTime + "." + noAntrian;
            } else if (statusAntrian.equals("EXTRA")) {
                result = "E" + "." + currentDateTime + "." + noAntrian;
            } else if (statusAntrian.equals("EXPRESS")) {
                result = "EX" + "." + currentDateTime + "." + noAntrian;
            } else {
                result = "H" + "." + currentDateTime + "." + noAntrian;
            }
        }
        return result;
    }

    private void saveData(final String status) {
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        final String namaMekanik = find(R.id.sp_namaMekanik_checkin4, Spinner.class).getSelectedItem().toString();
        final String antrian = find(R.id.et_no_antrian_checkin4, EditText.class).getText().toString();
        final String levelBbm = find(R.id.tv_ketBbbm_checkin4, TextView.class).getText().toString();
        final String tidakMenunggu = find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String konfirmTambahan = find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String buangPart = find(R.id.cb_buangPart_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String waktuAmbil = find(R.id.tv_waktu_checkin4, TextView.class).getText().toString();
        final String sk = find(R.id.cb_aggrement_checkin4, CheckBox.class).isChecked() ? "Y" : "N";
        final String hari = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(0, 3);
        final String jam = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(3, 6);
        final String menit = find(R.id.et_lamaWaktu_checkin, EditText.class).getText().toString().substring(5, 7);
        Log.d(TAG, "hari : " + hari);
        Log.d(TAG, "jam : " + jam);
        Log.d(TAG, "menit : " + menit);
        final String estimasiSebelum = find(R.id.et_mulaiWaktu_checkin, TextView.class).getText().toString();
        final String estimasiSesudah = find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString();
        final String estimasiSelesai = find(R.id.et_selesaiWaktu_checkin, TextView.class).getText().toString();
        //final String ttd = find(R.id.img_tandaTangan_checkin4 , ImageView.class).getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("check", "2");
                args.put("id", nson.get("id").asString());
                args.put("status", status);
                args.put("mekanik", namaMekanik);
                args.put("antrian", antrian);
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
                args.put("estimasiSelesai", estimasiSesudah);
                //args.put("ttd", ttd);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (status.equalsIgnoreCase("BATAL CHECKIN 4")) {
                        showSuccess("Layanan di Batalkan, Data Di masukkan Ke Daftar Kontrol Layanan");
                    } else {
                        showSuccess("Data Pelanggan Berhasil Di masukkan Ke Daftar Kontrol Layanan");
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void setSpMekanik() {
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
                    mekanikArray.add("--PILIH--");
                    for (int i = 0; i < data.get("data").size(); i++) {
                        //idMekanikArray.add(Nson.newObject().set("ID", data.get("data").get(i).get("ID").asString()).set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }

                    Log.d(TAG, "List : " + mekanikArray);

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mekanikArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    find(R.id.sp_namaMekanik_checkin4, Spinner.class).setAdapter(spinnerAdapter);
                } else {
                    showInfoDialog("Nama Mekanik Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpMekanik();
                        }
                    });
                }
            }
        });
    }

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
                    saveData("CHECKIN ANTRIAN");
                }
                break;
            case R.id.btn_ttd_checkin4:
                if(!checkPermission()){
                    Intent intent = new Intent(getActivity(), Capture.class);
                    startActivityForResult(intent, REQUEST_CODE_SIGN);
                }else{
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_konfirmTambah_checkin4:
                if (buttonView.isChecked()) {
                    find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setEnabled(false);
                } else {
                    find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class).setEnabled(true);
                }
                break;
            case R.id.cb_tidakMenunggu_checkin4:
                if (buttonView.isChecked()) {
                    find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).setEnabled(false);
                    find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).setChecked(false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), true);
                    find(R.id.tv_disable_waktu_antar).setVisibility(View.GONE);
                } else {
                    find(R.id.cb_konfirmTambah_checkin4, CheckBox.class).setEnabled(true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_waktuAmbil, LinearLayout.class), false);
                    find(R.id.tv_disable_waktu_antar).setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("");
                alertBuilder.setMessage("");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(Checkin4_Activity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(Checkin4_Activity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {
                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
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
            setSpMekanik();
            showSuccess("Berhasil Mencatatkan Mekanik");
        }
    }
}
