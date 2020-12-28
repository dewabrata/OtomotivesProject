package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturTenda_Activity;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.APIUrls.VIEW_KELUHAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.ONEDAY;
import static com.rkrzmail.utils.Tools.getDayOfWeek;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class AturSchedule_Activity extends AppActivity implements View.OnClickListener {

    private TextView tvMulai_Kerja, tvSelesai_Kerja, tv_tanggal,tv_tanggal2;
    private Spinner sp_status,spLokasi, spUser;
    private RecyclerView rcSchedule;
    private String izin = "", tanggalString= "", hari="", namauser, hari2="";
    private CheckBox cbCopy ;
    private boolean isIzin = false, isSakit=false, isTrue=false, isIzinlamabat=false;
    private Nson userList = Nson.newArray(), lokasiArray = Nson.newArray() ,scheduleArray = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_schedule);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        tv_tanggal = findViewById(R.id.tv_tanggal);
        tv_tanggal2 = findViewById(R.id.tv_tanggal2);
        tvMulai_Kerja = findViewById(R.id.tv_mulaiKerja);
        tvSelesai_Kerja = findViewById(R.id.tv_selesaiKerja);
        sp_status = findViewById(R.id.sp_statusSchedule);
        spLokasi = findViewById(R.id.sp_lokasi);
        spUser = findViewById(R.id.sp_userSchedule);
        cbCopy = findViewById(R.id.cb_copydata);
        rcSchedule = findViewById(R.id.recyclerViewSchedule);
        setSpUser();
        setSpLokasi();


        sp_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.equalsIgnoreCase("KERJA")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), true);
                    cbCopy.setEnabled(true);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_900));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_900));
                    isTrue=true;
                    isSakit=false;
                    isIzin=false;

                } else if (item.equalsIgnoreCase("LIBUR")){
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_40));
                    isTrue=true;
                    isSakit=false;
                    isIzin=false;

                } else if (item.equalsIgnoreCase("CUTI")){
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_40));
                    isTrue=true;
                    isSakit=false;
                    isIzin=false;

                } else if (item.equalsIgnoreCase("IZIN")){
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_40));
                    isTrue=true;
                    isSakit=false;
                    isIzin=true;

                } else if (item.equalsIgnoreCase("SAKIT")){
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_40));
                    isSakit=true;
                    isIzin=false;

                } else if (item.equalsIgnoreCase("IZIN TERLAMBAT")){
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_40));
                    isIzinlamabat=true;
                    isTrue=false;
                    isSakit=false;
                    isIzin=false;
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(getColor(R.color.grey_40));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertData();
            }

        });

        cbCopy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){

                }
            }
        });

        tvMulai_Kerja.setOnClickListener(this);
        tvSelesai_Kerja.setOnClickListener(this);
        tv_tanggal.setOnClickListener(this);
        tv_tanggal2.setOnClickListener(this);
        find(R.id.ic_tanggal).setOnClickListener(this);
        find(R.id.ic_tanggal2).setOnClickListener(this);
    }

    private void insertData() {
        final String masuk = tvMulai_Kerja.getText().toString().trim();
        final String selesai = tvSelesai_Kerja.getText().toString().trim();
        final String tanggal = tv_tanggal.getText().toString().trim();
        final String tanggal2 = tv_tanggal2.getText().toString().trim();
        final String status = sp_status.getSelectedItem().toString().toUpperCase();
        if(status.contains("IZIN TERLAMBAT")){
            izin = "Y";
        }
        final String lokasi = spLokasi.getSelectedItem().toString().toUpperCase();
        final String copy = find(R.id.cb_copydata, CheckBox.class).isChecked() ? "Y" : "N";

        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("kategori", "TEST");
                args.put("nama", namauser);
                args.put("tanggal", setFormatDayAndMonthToDb(tanggal));
                args.put("tanggal2", setFormatDayAndMonthToDb(tanggal2));
                args.put("hari", hari);
                args.put("status", status);
                args.put("scheduleMulai", masuk);
                args.put("scheduleSelesai", selesai);
                args.put("lokasi", lokasi);
                args.put("izinTerlambat", izin);
                args.put("copyData", copy);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ABSEN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menambahkan Schedule");
                    viewSchedule(namauser);
                    setDefault();
                } else {
                    showError("Menambahkan data gagal!");
                }
            }
        });
    }

    private void setDefault(){
        tv_tanggal.setText("");
        tv_tanggal2.setText("");
        sp_status.setSelection(0);
        spLokasi.setSelection(0);
        tvMulai_Kerja.setText("");
        tvSelesai_Kerja.setText("");
        cbCopy.setChecked(false);
        cbCopy.setEnabled(false);
    }

    private void viewSchedule(final String item){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("kategori", "SCHEDULE");
                args.put("nama", item);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ABSEN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    scheduleArray.asArray().clear();
                    scheduleArray.asArray().addAll(result.get("data").asArray());
                    initRecylerview();
                } else {
                    //showInfo(result.get("message").asString());
                }
            }
        });

    }

    private void setSpLokasi() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    lokasiArray.add("BENGKEL");
                    for (int i = 0; i < result.get("data").size(); i++) {
                        lokasiArray.add(result.get("data").get(i).get("NAMA_LOKASI").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLokasi.setAdapter(spinnerAdapter);

                } else {
                    showInfo("Lokasi Gagal Di Muat");
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void setSpUser() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "USER");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    userList.asArray().clear();
                    userList.add("--PILIH--");
                    for (int i = 0; i < result.get("data").size(); i++) {
                        userList.add(result.get("data").get(i).get("NAMA").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, userList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spUser.setAdapter(spinnerAdapter);
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });

        spUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                namauser = parent.getSelectedItem().toString();
                viewSchedule(namauser);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initRecylerview() {
        rcSchedule.setHasFixedSize(true);
        rcSchedule.setLayoutManager(new LinearLayoutManager(this));
        rcSchedule.setAdapter(new NikitaRecyclerAdapter(scheduleArray, R.layout.item_schedule_user) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_schedule_tanggal, TextView.class).setText(scheduleArray.get(position).get("TANGGAL").asString());
                        viewHolder.find(R.id.tv_schedule_hari, TextView.class).setText(scheduleArray.get(position).get("HARI").asString());
                        viewHolder.find(R.id.tv_schedule_jammulai, TextView.class).setText(scheduleArray.get(position).get("SCHEDULE_MULAI").asString());
                        viewHolder.find(R.id.tv_schedule_jampulang, TextView.class).setText(scheduleArray.get(position).get("SCHEDULE_SELESAI").asString());
                        viewHolder.find(R.id.tv_schedule_lokasi, TextView.class).setText(scheduleArray.get(position).get("LOKASI").asString());
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                    }
                })
        );
    }

    private Calendar parseWaktuNow() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long totalDate = current;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

    private Calendar parseWaktu3harilalu() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long hari = 3 * ONEDAY;
        long totalDate = current - hari;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

    private Calendar parseWaktu1blnlalu() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long hari = 30 * ONEDAY;
        long totalDate = current - hari;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

    private Calendar parseWaktu1Bulan() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long bulan = 30 * ONEDAY;
        long totalDate = current + bulan;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

    public void getDatePickerStatus() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_WEEK);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                hari = ParseDateofWeek(dayOfMonth,monthOfYear,year);

                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                tanggalString += formattedTime;
                find(R.id.tv_tanggal, TextView.class).setText(formattedTime);

            }
        }, year, month, day);

//        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                find(R.id.tv_tanggal2, TextView.class).setEnabled(false);
//            }
//        });
        if(isSakit){
            datePickerDialog.setMinDate(parseWaktu3harilalu());
        } else if(isTrue){
            datePickerDialog.setMinDate(parseWaktuNow());
        }else if (isIzinlamabat){
            datePickerDialog.setMinDate(parseWaktu1blnlalu());
        }

        if(isIzin || isSakit){
            datePickerDialog.setMaxDate(parseWaktuNow());
        }else {
            datePickerDialog.setMaxDate(parseWaktu1Bulan());
        }
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public void getDatePickerStatus2() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_WEEK);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                hari2 = ParseDateofWeek(dayOfMonth,monthOfYear,year);

                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                tanggalString += formattedTime;
                find(R.id.tv_tanggal2, TextView.class).setText(formattedTime);

            }
        }, year, month, day);

//        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                find(R.id.tv_tanggal, TextView.class).setText(waktuLayanan);
//            }
//        });
        if(isSakit){
            datePickerDialog.setMinDate(parseWaktu3harilalu());
        } else if(isTrue){
            datePickerDialog.setMinDate(parseWaktuNow());
        }else if (isIzinlamabat){
            datePickerDialog.setMinDate(parseWaktu1blnlalu());
        }

        if(isIzin || isSakit){
            datePickerDialog.setMaxDate(parseWaktuNow());
        }else {
            datePickerDialog.setMaxDate(parseWaktu1Bulan());
        }
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private String ParseDateofWeek (int date, int month, int year){
        if(date>0 && month>0 && year>0){
            int day = 0;
            SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                String newDate = date + "/" + (month + 1) + "/" + year;
                Date myDate = inFormat.parse(newDate);
                //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
                Calendar c = Calendar.getInstance();
                c.setTime(myDate);
                day = c.get(Calendar.DAY_OF_WEEK);
                switch (day) {
                    case Calendar.SUNDAY:
                        return "Minggu";
                    case Calendar.MONDAY:
                        return "Senin";
                    case Calendar.TUESDAY:
                        return "Selesa";
                    case Calendar.WEDNESDAY:
                        return "Rabu";
                    case Calendar.THURSDAY:
                        return "Kamis";
                    case Calendar.FRIDAY:
                        return "Jumat";
                    case Calendar.SATURDAY:
                        return "Sabtu";
                    default:
                        return "";
                }
                //return simpleDateFormat.format(myDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_mulaiKerja:
                getTimePickerDialogTextView(getActivity(), tvMulai_Kerja);
                break;
            case R.id.tv_selesaiKerja:
                getTimePickerDialogTextView(getActivity(), tvSelesai_Kerja);
                break;
//            case R.id.tv_tanggal:
//                find(R.id.tv_tanggal2, TextView.class).setEnabled(true);
//                getDatePickerStatus();
//                break;
//            case R.id.tv_tanggal2:
//                getDatePickerStatus2();
//                break;
            case R.id.ic_tanggal:
                find(R.id.ic_tanggal2, TextView.class).setEnabled(true);
                getDatePickerStatus();
                break;
            case R.id.ic_tanggal2:
                getDatePickerStatus2();
                break;
        }
    }


}
