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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.Capture;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.JumlahPart_HargaPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
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
import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.BATAL_PART;
import static com.rkrzmail.utils.ConstUtils.BENGKEL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ESTIMASI_WAKTU;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.PARTS_UPPERCASE;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CODE_SIGN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_HARGA_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;
import static com.rkrzmail.utils.ConstUtils.TAMBAH_PART;
import static com.rkrzmail.utils.ConstUtils.TIDAK_MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.TOTAL_BIAYA;

public class TambahPartJasaDanBatal_Activity extends AppActivity implements View.OnClickListener {

    public static final String TAG = "Tambah___";

    private RecyclerView rvPart, rvJasaLain;
    private Nson partList = Nson.newArray();
    private Nson jasaList = Nson.newArray();
    private Tools.TimePart dummyTime = Tools.TimePart.parse("00:00:00");

    private String idCheckinDetail = "", idCheckin = "";
    private int totalBiaya = 0;
    private int totalTambah = 0;
    private int totalBatal = 0, countBatal = 0;

    private boolean isWait = false, isPartKosong = false, isBatal = false, isTambah = false, isNotWait = false;
    private boolean isSign = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_part_jasalain);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Tambah Part - Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        rvPart = findViewById(R.id.rv_part);
        rvJasaLain = findViewById(R.id.rv_jasa_lain);
        initRecylerViewPart();
        initRecylerviewJasaLain();
        initData();
        initListener();
    }

    private void initListener() {
        find(R.id.btn_jasa_lain).setOnClickListener(this);
        find(R.id.btn_part).setOnClickListener(this);
        find(R.id.btn_simpan).setOnClickListener(this);
        find(R.id.tv_max_tgl_konfirmasi).setOnClickListener(this);
        find(R.id.tv_max_jam_konfirmasi).setOnClickListener(this);
        find(R.id.btn_ttd).setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        idCheckinDetail = getIntentStringExtra(ID);
        idCheckin = getIntentStringExtra("CHECKIN_ID");
        totalBiaya += Integer.parseInt(getIntentStringExtra(TOTAL_BIAYA));
        find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));

        if (getIntent().hasExtra(TAMBAH_PART)) {
            isTambah = true;
            String estimasiLama;
            if(!getIntentStringExtra(ESTIMASI_WAKTU).contains(":")){
                estimasiLama = "00:00";
            }else{
                estimasiLama = getIntentStringExtra(ESTIMASI_WAKTU);
            }
            dummyTime.add(Tools.TimePart.parse("00:" + estimasiLama));
            find(R.id.et_estimasi_lama, EditText.class).setText(estimasiLama);
            if (getIntent().hasExtra(TIDAK_MENUNGGU)) {
                isNotWait = true;
                find(R.id.ly_tidak_menunggu).setVisibility(View.VISIBLE);
                find(R.id.tl_total_setelah_tambah, TextInputLayout.class).setHint("TAMBAH / BATAL");
            } else {
                isWait = true;
                find(R.id.ly_menunggu).setVisibility(View.VISIBLE);
                find(R.id.tl_total_setelah_tambah, TextInputLayout.class).setHint("TOTAL TAMBAH");
            }
        } else if (getIntent().hasExtra(BATAL_PART)) {
            isBatal = true;
            find(R.id.ly_btn_part_jasa).setVisibility(View.GONE);
            find(R.id.ly_waktu_estimasi).setVisibility(View.GONE);
            find(R.id.ly_tidak_menunggu).setVisibility(View.GONE);
            find(R.id.ly_menunggu).setVisibility(View.GONE);
            find(R.id.tl_total_setelah_tambah, TextInputLayout.class).setHint("TOTAL BATAL");
            jasaList.asArray().addAll(Nson.readJson(getIntentStringExtra(JASA_LAIN)).asArray());
            partList.asArray().addAll(Nson.readJson(getIntentStringExtra(PARTS_UPPERCASE)).asArray());
            if (jasaList.size() > 0) {
                rvPart.getAdapter().notifyDataSetChanged();
            }
            if (partList.size() > 0) {
                rvJasaLain.getAdapter().notifyDataSetChanged();
            }
        }
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

                find(R.id.tv_max_jam_konfirmasi, TextView.class).setText(sdf.format(date));
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setTitle("Pilih Jam Konfirmasi");
        timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
    }

    public void getDatePickerMaxConfirm() {
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

                find(R.id.tv_max_tgl_konfirmasi, TextView.class).setText(sdf.format(date));
            }
        }, year, month, day);

        datePickerDialog.setMaxDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private void initRecylerViewPart() {
        if (isBatal) {
            setMargins(rvPart, 0, 0, 0, 0);
        }
        rvPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPart.setHasFixedSize(false);
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));

                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setText(partList.get(position).get("MERK").asString());
                if (isBatal) {
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Part / Jasa ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    totalBatal += Integer.parseInt(formatOnlyNumber(partList.get(position).get("HARGA_PART").asString()));
                                    totalBatal += Integer.parseInt(formatOnlyNumber(partList.get(position).get("HARGA_JASA").asString()));
                                    find(R.id.et_total_tambah_or_batal, EditText.class).setText(RP + formatRp(String.valueOf(totalBatal)));
                                    int subtraction = totalBiaya - totalBatal;
                                    find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(subtraction)));
                                    showInfo(partList.get(position).get("NAMA_PART").asString() + " di Batalkan");
                                    partList.asArray().remove(position);
                                    notifyItemRemoved(position);
                                    countBatal++;
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void initRecylerviewJasaLain() {
        rvJasaLain.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJasaLain.setHasFixedSize(false);
        rvJasaLain.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("NAMA_KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText(RP + formatRp(jasaList.get(position).get("HARGA_JASA").asString()));

                if (isBatal) {
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Part / Jasa ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    totalBatal += Integer.parseInt(formatOnlyNumber(jasaList.get(position).get("HARGA_JASA").asString()));
                                    find(R.id.et_total_tambah_or_batal, EditText.class).setText(RP + formatRp(String.valueOf(totalBatal)));
                                    int subtraction = totalBiaya - totalBatal;
                                    find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(subtraction)));
                                    showInfo(jasaList.get(position).get("KELOMPOK_PART").asString() + " di Batalkan");
                                    jasaList.asArray().remove(position);
                                    notifyItemRemoved(position);
                                    countBatal++;
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    private void updateTambahOrBatal() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                if (isTambah) {
                    args.put("aktivitas", "TAMBAH PART - JASA");
                    args.put("detailId", idCheckinDetail);
                    args.put("parts", partList.toJson());
                    args.put("jasaLain", jasaList.toJson());
                }
                if (isBatal) {
                    args.put("aktivitas", "BATAL PART");
                    args.put("jumlahBatal", String.valueOf(totalBatal));
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_KONTROL_LAYANAN), args));
                Log.d("cok__", "run: " + result);
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Update Aktivitas Berhasil");
                    Intent i = new Intent();
                    i.putExtra(DATA, idCheckin);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void totalWaktuLayanan(Tools.TimePart waktuLayanan) {
        dummyTime.add(waktuLayanan);
        find(R.id.et_estimasi_selesai, EditText.class).setText(dummyTime.toString());
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_jasa_lain:
                i = new Intent(getActivity(), JasaLain_Activity.class);
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_part:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                startActivityForResult(i, REQUEST_CARI_PART);
                break;
            case R.id.btn_simpan:
                if (idCheckinDetail.isEmpty()) {
                    showError("ID CHECKIN NULL");
                }else if(isWait){
                    if(!isSign){
                        showWarning("Tanda Tangan belum terisi");
                    }
                }else {
                    updateTambahOrBatal();
                }
                break;
            case R.id.tv_max_tgl_konfirmasi:
                getDatePickerMaxConfirm();
                break;
            case R.id.tv_max_jam_konfirmasi:
                getTimePickerDialogEstimasiSelesai();
                break;
            case R.id.btn_ttd:
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
                        ActivityCompat.requestPermissions(TambahPartJasaDanBatal_Activity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(TambahPartJasaDanBatal_Activity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
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


    @SuppressLint({"NewApi", "SetTextI18n"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Nson dataAccept;
        Intent i;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SIGN:
                    isSign = true;
                    @SuppressLint("SdCardPath") File imgFile = null;
                    if (data != null) {
                        imgFile = (File) Objects.requireNonNull(data.getExtras()).get("imagePath");
                    }
                    if (imgFile != null && imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        find(R.id.img_tandaTangan, ImageView.class).setImageBitmap(myBitmap);
                    }
                    break;
                case REQUEST_JASA_LAIN:
                    try {
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        dataAccept.set("CHECKIN_ID", idCheckin);
                        jasaList.add(dataAccept);
                        Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();

                        totalBiaya += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));
                        totalTambah += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));

                        totalWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                    } catch (Exception e) {
                        showWarning(JASA_LAIN + e.getMessage(), Toast.LENGTH_LONG);
                    }
                    break;
                case REQUEST_CARI_PART:
                    i = new Intent(getActivity(), JumlahPart_HargaPart_Activity.class);
                    i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
                    i.putExtra(TAMBAH_PART, "");
                    startActivityForResult(i, REQUEST_HARGA_PART);
                    break;
                case REQUEST_HARGA_PART:
                    try {
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        dataAccept.set("CHECKIN_ID", idCheckin);
                        dataAccept.set("CHECKIN_DETAIL_ID", idCheckinDetail);
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();

                        totalBiaya += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                        totalBiaya += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));
                        totalTambah += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                        totalTambah += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));

                        totalWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                    } catch (Exception e) {
                        showWarning(PART + e.getMessage(), Toast.LENGTH_LONG);
                    }
                    break;
            }

            find(R.id.et_total_tambah_or_batal, EditText.class).setText(RP + formatRp(String.valueOf(totalTambah)));
            find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
        }
    }
}