package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Schedule_MainTab_Activity;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.utils.Tools;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;
import static com.rkrzmail.oto.AppActivity.getTimePickerDialogTextView;
import static com.rkrzmail.utils.APIUrls.SET_SCHEDULE;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.ONEDAY;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;


public class Jadwal_Schedule_Fragment extends Fragment {

    private TextView tvMulai_Kerja, tvSelesai_Kerja, tv_tanggal;
    private Spinner sp_status, spLokasi, spUser;
    private Button btnSimpan;
    private RecyclerView rcSchedule;
    private CheckBox cbCopy;
    private AppActivity activity;
    Calendar myCalendar = Calendar.getInstance();
    private AlertDialog alertDialog;
    private View dialogView;

    private String izin = "", tanggalString = "", hari = "", namauser, hari2 = "", idSchedule="";
    private String userId = "";
    private String status = "";
    private boolean isIzin = false, isSakit = false, isTrue = false, isIzinlamabat = false;

    private final List<Date> dateList = new ArrayList<>();
    private final Nson tanggalList = Nson.newArray();
    private final Nson userList = Nson.newArray();
    private final Nson lokasiArray = Nson.newArray();
    private final Nson scheduleArray = Nson.newArray();
    private final Nson userData = Nson.newArray();


    public Jadwal_Schedule_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_atur_schedule, container, false);

        activity = ((Schedule_MainTab_Activity) getActivity());
        initHideToolbar(view);
        initComponent(view);
        initRecylerview();
        return view;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent(View v) {
        tv_tanggal = v.findViewById(R.id.tv_tanggal_schedule);
        tvMulai_Kerja = v.findViewById(R.id.tv_mulaiKerja);
        tvSelesai_Kerja = v.findViewById(R.id.tv_selesaiKerja);
        sp_status = v.findViewById(R.id.sp_statusSchedule);
        spLokasi = v.findViewById(R.id.sp_lokasi);
        spUser = v.findViewById(R.id.sp_userSchedule);
        cbCopy = v.findViewById(R.id.cb_copydata);
        rcSchedule = v.findViewById(R.id.recyclerView);
        btnSimpan = v.findViewById(R.id.btn_simpan);
        setSpUser("");
        setSpLokasi("");
        setSpStatus("");

        sp_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                status = parent.getSelectedItem().toString();
                if (status.equalsIgnoreCase("KERJA")) {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), true);
                    cbCopy.setEnabled(true);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_900));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_900));
                    isTrue = true;
                    isSakit = false;
                    isIzin = false;

                } else if (status.equalsIgnoreCase("LIBUR")) {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    isTrue = true;
                    isSakit = false;
                    isIzin = false;

                } else if (status.equalsIgnoreCase("CUTI")) {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    isTrue = true;
                    isSakit = false;
                    isIzin = false;

                } else if (status.equalsIgnoreCase("IZIN")) {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    isTrue = true;
                    isSakit = false;
                    isIzin = true;

                } else if (status.equalsIgnoreCase("SAKIT")) {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    isSakit = true;
                    isIzin = false;

                } else if (status.equalsIgnoreCase("IZIN TERLAMBAT")) {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    isIzinlamabat = true;
                    isTrue = false;
                    isSakit = false;
                    isIzin = false;
                } else {
                    Tools.setViewAndChildrenEnabled(activity.find(R.id.ly_mulaiselesai, LinearLayout.class), false);
                    tvMulai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                    tvSelesai_Kerja.setTextColor(activity.getColor(R.color.grey_40));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnSimpan.getText().toString().equals("SIMPAN")){
                    insertData();
                }else {
                    updateData();
                }
            }

        });

        cbCopy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {

                }
            }
        });

        tvMulai_Kerja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimePickerDialogTextView(getActivity(), tvMulai_Kerja);
            }
        });
        tvSelesai_Kerja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimePickerDialogTextView(getActivity(), tvSelesai_Kerja);
            }
        });

        tv_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sp_status.getSelectedItem().equals("--PILIH--")) {
                    activity.showInfo("STATUS HARUS DI PILIH");
                    sp_status.performClick();
                } else {
                    showDialogMultipleDatePicker();
                    //getStatusDatepicker();
                }
            }
        });
    }

    private void insertData() {
        final String masuk = tvMulai_Kerja.getText().toString().trim();
        final String selesai = tvSelesai_Kerja.getText().toString().trim();
        final String tanggal = tv_tanggal.getText().toString().trim();
        final String status = sp_status.getSelectedItem().toString().toUpperCase();
        if (status.contains("IZIN TERLAMBAT")) {
            izin = "Y";
        }
        final String lokasi = spLokasi.getSelectedItem().toString().toUpperCase();
        final String copy = activity.find(R.id.cb_copydata, CheckBox.class).isChecked() ? "Y" : "N";

        if(tanggalList.asArray().isEmpty()){
            tanggalList.asArray().clear();
            String[] date = tanggal.split("-");
            int day = Integer.parseInt(date[2]);
            int month = Integer.parseInt(date[1]);
            int year = Integer.parseInt(date[0]);
            tanggalList.add(Nson.newObject()
                    .set("TANGGAL", tanggal)
                    .set("HARI", ParseDateofWeek(day, month, year))
                    .set("NO_MINGGU", getNoMinggu(year, month, day))
            );
        }

        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("kategori", "SCHEDULE");
                args.put("nama", namauser);
                args.put("tanggal", setFormatDayAndMonthToDb(tanggal));
                //args.put("tanggal2", setFormatDayAndMonthToDb(tanggal2));
                args.put("status", status);
                args.put("scheduleMulai", DateFormatUtils.formatDate(masuk, "HH:mm", "HH:mm:ss"));
                args.put("scheduleSelesai", DateFormatUtils.formatDate(selesai, "HH:mm", "HH:mm:ss"));
                args.put("lokasi", lokasi);
                args.put("izinTerlambat", izin);
                args.put("copyData", copy);
                args.put("userId", userId);
                //array tanggal
                args.put("tanggalList", tanggalList.toJson());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_SCHEDULE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (btnSimpan.getText().toString().equals("SIMPAN")) {
                        activity.showSuccess("Berhasil Menambahkan Schedule");
                    }else {
                        activity.showSuccess("Berhasil Update Schedule");
                    }
                    viewSchedule(userId);
                    setDefault();
                } else {
                    if (btnSimpan.getText().toString().equals("SIMPAN")) {
                        activity.showError("Menambahkan data gagal!");
                    }else {
                        activity.showError("Update data gagal!");
                    }
                }
            }
        });
    }

    private void updateData() {
        final String masuk = tvMulai_Kerja.getText().toString().trim();
        final String selesai = tvSelesai_Kerja.getText().toString().trim();
        final String tanggal = tv_tanggal.getText().toString().trim();
        final String status = sp_status.getSelectedItem().toString().toUpperCase();
        if (status.contains("IZIN TERLAMBAT")) {
            izin = "Y";
        }
        final String lokasi = spLokasi.getSelectedItem().toString().toUpperCase();
        final String copy = activity.find(R.id.cb_copydata, CheckBox.class).isChecked() ? "Y" : "N";

        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "delete");
                args.put("kategori", "SCHEDULE");
                args.put("scheduleId", idSchedule);
//                args.put("nama", namauser);
//                args.put("tanggal", setFormatDayAndMonthToDb(tanggal));
//                args.put("status", status);
//                args.put("scheduleMulai", DateFormatUtils.formatDate(masuk, "HH:mm", "HH:mm:ss"));
//                args.put("scheduleSelesai", DateFormatUtils.formatDate(selesai, "HH:mm", "HH:mm:ss"));
//                args.put("lokasi", lokasi);
//                args.put("izinTerlambat", izin);
//                args.put("copyData", copy);
//                args.put("userId", userId);
//                args.put("tanggalList", tanggalList.toJson());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_SCHEDULE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    //activity.showSuccess("Berhasil Update Schedule");
                    insertData();
                } else {
                    activity.showError("Update data gagal!");
                }
            }
        });
    }

    private void setDefault() {
        tv_tanggal.setText("");
        spUser.setSelection(0);
        sp_status.setSelection(0);
        spLokasi.setSelection(0);
        tvMulai_Kerja.setText("");
        tvSelesai_Kerja.setText("");
        cbCopy.setChecked(false);
        cbCopy.setEnabled(false);
        idSchedule = "";
        btnSimpan.setText("SIMPAN");
    }

    private void viewSchedule(final String item) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("kategori", "SCHEDULE");
                args.put("userId", item);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_SCHEDULE), args));
            }

            @SuppressLint("SimpleDateFormat")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    dateList.clear();
                    scheduleArray.asArray().clear();
                    scheduleArray.asArray().addAll(result.asArray());
                    for (int i = 0; i < scheduleArray.size(); i++) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date loadDate = sdf.parse(scheduleArray.get(i).get("TANGGAL").asString());
                            dateList.add(loadDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    Objects.requireNonNull(rcSchedule.getAdapter()).notifyDataSetChanged();
                } else {
//                    activity.showInfo(result.get("message").asString());
                }
            }
        });

    }

    private void setSpLokasi(final String selection) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
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
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spLokasi.getCount(); i++) {
                            if (spLokasi.getItemAtPosition(i).equals(selection)) {
                                spLokasi.setSelection(i);
                                break;
                            }
                        }
                    }

                } else {
                    activity.showInfo("Lokasi Gagal Di Muat");
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void setSpUser(final String user) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
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
                    result = result.get("data");
                    userList.asArray().clear();
                    userData.asArray().clear();
                    userData.add("");
                    userList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        userList.add(result.get(i).get("NAMA").asString());
                        userData.add(Nson.newObject()
                                .set("USER_ID", result.get(i).get("NO_PONSEL").asString())
                                .set("NAMA", result.get(i).get("NAMA").asString()
                                ));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, userList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spUser.setAdapter(spinnerAdapter);
                    if (!user.isEmpty()) {
                        for (int i = 0; i < spUser.getCount(); i++) {
                            if (spUser.getItemAtPosition(i).equals(user)) {
                                spUser.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });

        spUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                namauser = parent.getSelectedItem().toString();
                if (namauser.equals(userData.get(position).get("NAMA").asString())) {
                    userId = userData.get(position).get("USER_ID").asString();
                    viewSchedule(userId);
                } else {
                    userId = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSpStatus (final String selection) {
        Resources res = getResources();
        String[] stringArray = res.getStringArray(R.array.atur_status);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stringArray) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                final View v = super.getDropDownView(position, convertView, parent);
                v.post(new Runnable() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void run() {
                        ((TextView) v.findViewById(android.R.id.text1)).setSingleLine(false);
                        ((TextView) v.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
                        ((TextView) v.findViewById(android.R.id.text1)).setTextAlignment(Gravity.CENTER);
                    }
                });
                return v;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_status.setAdapter(spinnerAdapter);
        if (!selection.isEmpty()) {
            for (int in = 0; in < sp_status.getCount(); in++) {
                if (sp_status.getItemAtPosition(in).toString().contains(selection)) {
                    sp_status.setSelection(in);
                    break;
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void initToolbarDatePicker() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle("Tanggal Schedule");
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showDialogMultipleDatePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_multiple_select_date_picker, null);
        builder.setView(dialogView);

        initToolbarDatePicker();
        Calendar minDate = Calendar.getInstance();
        final Calendar maxDate = Calendar.getInstance();
        final List<String> dateSelected = new ArrayList<>();
        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);
        Button btnBatal = dialogView.findViewById(R.id.btn_hapus);

        btnBatal.setVisibility(View.VISIBLE);
        btnBatal.setText("BATAL");
        final CalendarPickerView calendarPickerView = dialogView.findViewById(R.id.date_picker);

        minDate.add(Calendar.MONTH, -1);
        maxDate.add(Calendar.YEAR, 1); // max next year
        calendarPickerView.init(minDate.getTime(), maxDate.getTime()).inMode(CalendarPickerView.SelectionMode.MULTIPLE);
        if (dateList.size() > 0) {
            try{
                for (Date dateSelect : dateList) {
                    calendarPickerView.selectDate(dateSelect);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            try{
                calendarPickerView.selectDate(maxDate.getTime());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        calendarPickerView.setDateSelectableFilter(new CalendarPickerView.DateSelectableFilter() {
            @Override
            public boolean isDateSelectable(Date date) {
                if(status.equals("KERJA") && date.before(new Date())){
                    activity.showWarning("TIDAK BISA MEMILIH HARI SEBELUMNYA");
                    return false;
                }
                return true;
            }
        });

        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onDateSelected(Date date) {
                String tgl = DateFormatUtils.formatDateDefault(date.toString(), "yyyy-MM-dd");
                dateList.add(date);
                dateSelected.add(tgl);
            }

            @Override
            public void onDateUnselected(Date date) {
                if (dateList.size() > 0) {
                    for (int i = 0; i < dateList.size(); i++) {
                        if (dateList.get(i).equals(date)) {
                            dateList.remove(date);
                            break;
                        }
                    }
                }

                if (dateSelected.size() > 0) {
                    for (int i = 0; i < dateSelected.size(); i++) {
                        if (dateSelected.get(i).equals(DateFormat.getDateInstance(DateFormat.SHORT).format(date))) {
                            dateSelected.remove(i);
                            break;
                        }
                    }
                }
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                StringBuilder tanggalValues = new StringBuilder();
                for (int i = 0; i < dateSelected.size(); i++) {
                    String[] splitDate = dateSelected.get(i).split("-");
                    int day = Integer.parseInt(splitDate[2]);
                    int month = Integer.parseInt(splitDate[1]);
                    int year = Integer.parseInt(splitDate[0]);

                    tanggalList.add(Nson.newObject()
                            .set("TANGGAL", dateSelected.get(i))
                            .set("HARI", ParseDateofWeek(day, month, year))
                            .set("NO_MINGGU", getNoMinggu(year, month, day))
                    );
                    if (tanggalValues.length() > 0) tanggalValues.append(", ");
                    tanggalValues.append(dateSelected.get(i));
                }
                tv_tanggal.setText(tanggalValues.toString());
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private int getNoMinggu(int year, int month, int day) {
        if (year == 0 && month == 0) return 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        return cal.get(Calendar.WEEK_OF_MONTH);
    }

    private void initRecylerview() {
        rcSchedule.setHasFixedSize(false);
        rcSchedule.setLayoutManager(new LinearLayoutManager(activity.getActivity()));
        rcSchedule.setAdapter(new NikitaRecyclerAdapter(scheduleArray, R.layout.item_schedule_user) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_schedule_tanggal, TextView.class).setText(scheduleArray.get(position).get("TANGGAL").asString());
                        viewHolder.find(R.id.tv_schedule_hari, TextView.class).setText(scheduleArray.get(position).get("NAMA_HARI").asString());
                        viewHolder.find(R.id.tv_schedule_jammulai, TextView.class).setText(scheduleArray.get(position).get("SCHEDULE_MULAI").asString());
                        viewHolder.find(R.id.tv_schedule_jampulang, TextView.class).setText(scheduleArray.get(position).get("SCHEDULE_SELESAI").asString());
                        viewHolder.find(R.id.tv_schedule_lokasi, TextView.class).setText(scheduleArray.get(position).get("LOKASI").asString());
                        viewHolder.find(R.id.tv_status, TextView.class).setText(scheduleArray.get(position).get("STATUS").asString());
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        setDefault();
                        tv_tanggal.setText(scheduleArray.get(position).get("TANGGAL").asString());
                        setSpUser(scheduleArray.get(position).get("NAMA_USER").asString());
                        setSpStatus(scheduleArray.get(position).get("STATUS").asString());
                        setSpLokasi(scheduleArray.get(position).get("LOKASI").asString());
                        tvMulai_Kerja.setText(scheduleArray.get(position).get("SCHEDULE_MULAI").asString());
                        tvSelesai_Kerja.setText(scheduleArray.get(position).get("SCHEDULE_SELESAI").asString());
                        btnSimpan.setText("UPDATE");
                        idSchedule = scheduleArray.get(position).get("ID").asString();
                    }
                })
        );
    }

    private void getStatusDatepicker() {
        long now = System.currentTimeMillis() - 1000;

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        if (isSakit) {
            datePickerDialog.getDatePicker().setMinDate(now - ONEDAY * 3); //tigaharilalu
        } else if (isTrue) {
            datePickerDialog.getDatePicker().setMinDate(now);
        } else if (isIzinlamabat) {
            datePickerDialog.getDatePicker().setMinDate(now - ONEDAY * 30); //satubulanlalu
        }

        if (isIzin || isSakit) {
            datePickerDialog.getDatePicker().setMaxDate(now);
        } else {
            datePickerDialog.getDatePicker().setMaxDate(now + ONEDAY * 30); //satubulankedepan
        }
        datePickerDialog.show();
    }

    private void getStatusDatepicker2() {
        long now = System.currentTimeMillis() - 1000;

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date2, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        if (isSakit) {
            datePickerDialog.getDatePicker().setMinDate(now - ONEDAY * 3); //tigaharilalu
        } else if (isTrue) {
            datePickerDialog.getDatePicker().setMinDate(now);
        } else if (isIzinlamabat) {
            datePickerDialog.getDatePicker().setMinDate(now - ONEDAY * 30); //satubulanlalu
        }

        if (isIzin || isSakit) {
            datePickerDialog.getDatePicker().setMaxDate(now);
        } else {
            datePickerDialog.getDatePicker().setMaxDate(now + ONEDAY * 30); //satubulankedepan
        }
        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
            hari = ParseDateofWeek(dayOfMonth, monthOfYear, year);
            Date date = null;
            try {
                date = sdf.parse(newDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedTime = sdf.format(date);
//            tanggalString += formattedTime;
            tv_tanggal.setText(formattedTime);
        }

    };

    DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
            hari2 = ParseDateofWeek(dayOfMonth, monthOfYear, year);
            Date date = null;
            try {
                date = sdf.parse(newDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedTime = sdf.format(date);
        }

    };

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    private String ParseDateofWeek(int date, int month, int year) {
        if (date > 0 && month > 0 && year > 0) {
            int day = 0;
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                String newDate = String.format("%04d-%02d-%02d", year, month, date);
                Date myDate = inFormat.parse(newDate);
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

}