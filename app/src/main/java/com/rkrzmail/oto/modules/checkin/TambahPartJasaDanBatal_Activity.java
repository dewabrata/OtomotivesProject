package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.rkrzmail.oto.modules.sparepart.JumlahPart_Mekanik_Activity;
import com.rkrzmail.oto.modules.sparepart.JumlahPart_Checkin_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
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

import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.ATUR_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.BATAL_PART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ESTIMASI_WAKTU;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.PART;
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
import static com.rkrzmail.utils.ConstUtils.USULAN_MEKANIK;

public class TambahPartJasaDanBatal_Activity extends AppActivity implements View.OnClickListener {

    public static final String TAG = "Tambah___";

    private RecyclerView rvPart, rvJasaLain;
    private View dialogView;
    private AlertDialog alertDialog;
    private final Nson partList = Nson.newArray();
    private final Nson jasaList = Nson.newArray();
    private final Tools.TimePart dummyTime = Tools.TimePart.parse("00:00:00");

    private String idCheckin = "";
    private String layanan = "";
    private String nopol = "";
    private String noPonsel = "";
    private String layananId = "";
    private String pekerjaan = "";

    // private Nson idCheckinDetailList = Nson.newArray();
    private int kendaraanID = 0;
    private int totalBiaya = 0;
    private int totalTambah = 0;
    private int totalBatal = 0, countBatal = 0;
    private int countClick = 0;
    private int kmKendaraan = 0;

    private boolean isKonfirmasi = false;
    private boolean isWait = false, isPartKosong = false, isBatal = false, isTambah = false, isNotWait = false;
    private boolean isSign = false;
    private boolean isKonfirmasiTambah = false;
    private boolean isUsulanMekanik = false;

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
        if (getIntent().hasExtra(USULAN_MEKANIK)) {
            isUsulanMekanik = true;
            setGoneUsulanMekanik();
            Objects.requireNonNull(getSupportActionBar()).setTitle("Usulan Part - Jasa Lain");
        }else{
            Objects.requireNonNull(getSupportActionBar()).setTitle("Tambah Part - Jasa Lain");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initRecylerViewPart();
        initRecylerviewJasaLain();
        initData();
        initListener();
    }

    private void initListener() {
        find(R.id.btn_jasa_lain).setOnClickListener(this);
        find(R.id.btn_part).setOnClickListener(this);
        find(R.id.btn_simpan).setOnClickListener(this);
        find(R.id.img_btn_kalender_konfirmasi).setOnClickListener(this);
        find(R.id.img_btn_jam_konfirmasi).setOnClickListener(this);
        find(R.id.img_btn_kalender_estimasi).setOnClickListener(this);
        find(R.id.img_btn_jam_estimasi).setOnClickListener(this);
        find(R.id.btn_ttd).setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        idCheckin = getIntentStringExtra("CHECKIN_ID");
        nopol = getIntentStringExtra("NOPOL");
        noPonsel = getIntentStringExtra("NO_PONSEL");
        kmKendaraan = getIntentIntegerExtra("KM");
        totalBiaya = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(getIntentStringExtra(TOTAL_BIAYA)));
        layanan = getIntentStringExtra("LAYANAN");
        layananId = getIntentStringExtra("LAYANAN_ID");
        kendaraanID = getIntentIntegerExtra("KENDARAAN_ID");
        pekerjaan = getIntentStringExtra("PEKERJAAN");

        find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
        find(R.id.tv_nama_pelanggan, TextView.class).setText(getIntentStringExtra("NAMA_PELANGGAN"));
        find(R.id.tv_nopol, TextView.class).setText(nopol);
        find(R.id.tv_jenis, TextView.class).setText(getIntentStringExtra("JENIS_KENDARAAN"));
        find(R.id.tv_layanan, TextView.class).setText(getIntentStringExtra("LAYANAN"));
        find(R.id.tv_estimasi_selesai, TextView.class).setText(getIntentStringExtra("ESTIMASI_SELESAI"));

        if (getIntent().hasExtra(TAMBAH_PART)) {
            isTambah = true;
            String estimasiLama;
            if (!getIntentStringExtra(ESTIMASI_WAKTU).contains(":")) {
                estimasiLama = "00:00";
            } else {
                estimasiLama = getIntentStringExtra(ESTIMASI_WAKTU);
            }
            dummyTime.add(Tools.TimePart.parse("00:" + estimasiLama));
            find(R.id.et_estimasi_lama, EditText.class).setText(estimasiLama);
            if (getIntent().hasExtra(TIDAK_MENUNGGU)) {
                isNotWait = true;
                find(R.id.ly_menunggu).setVisibility(View.GONE);
                find(R.id.tl_total_setelah_tambah, TextInputLayout.class).setHint("TAMBAH / BATAL");
            } else {
                isWait = true;
                find(R.id.ly_menunggu).setVisibility(View.VISIBLE);
                //find(R.id.ly_tidak_menunggu).setVisibility(View.VISIBLE);
                find(R.id.tl_total_setelah_tambah, TextInputLayout.class).setHint("TOTAL TAMBAH");
            }

            isKonfirmasiTambah = getIntent().getBooleanExtra("KONFIRMASI_TAMBAH", false);
            find(R.id.ly_not_konfirmasi_tambah).setVisibility(isKonfirmasiTambah ? View.VISIBLE : View.GONE);
            find(R.id.ly_waktu_estimasi).setVisibility(isKonfirmasiTambah ? View.GONE : View.VISIBLE);

        } else if (getIntent().hasExtra(BATAL_PART)) {
            isBatal = true;
            find(R.id.ly_btn_part_jasa).setVisibility(View.GONE);
            find(R.id.ly_waktu_estimasi).setVisibility(View.GONE);
            find(R.id.ly_tidak_menunggu).setVisibility(View.GONE);
            find(R.id.ly_menunggu).setVisibility(View.GONE);
            find(R.id.tl_total_setelah_tambah, TextInputLayout.class).setHint("TOTAL BATAL");
            //jasaList.asArray().addAll(Nson.readJson(getIntentStringExtra(JASA_LAIN)).asArray());
            //partList.asArray().addAll(Nson.readJson(getIntentStringExtra(PARTS_UPPERCASE)).asArray());
            if (jasaList.size() > 0) {
                rvPart.getAdapter().notifyDataSetChanged();
            }
            if (partList.size() > 0) {
                rvJasaLain.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void setGoneUsulanMekanik() {
        find(R.id.tl_total_setelah_tambah).setVisibility(View.GONE);
        find(R.id.tl_total_akhir).setVisibility(View.GONE);
        find(R.id.ly_waktu_estimasi).setVisibility(View.GONE);
        find(R.id.ly_not_konfirmasi_tambah).setVisibility(View.GONE);
        find(R.id.ly_menunggu).setVisibility(View.GONE);
    }

    public void getTimePicker(final EditText editText) {
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

                editText.setText(sdf.format(date));
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setTitle("Pilih Jam Konfirmasi");
        timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
    }

    public void getDatePicker(final EditText editText) {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1);
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                editText.setText(sdf.format(date));
            }
        }, year, month, day);

        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private void initRecylerViewPart() {
        if (isKonfirmasi) {
            rvPart = dialogView.findViewById(R.id.recyclerView);
        } else {
            rvPart = findViewById(R.id.rv_part);
        }
        rvPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPart.setHasFixedSize(false);
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));

                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Part / Jasa ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                totalBatal += Integer.parseInt(formatOnlyNumber(partList.get(position).get("HARGA_PART").asString()));
                                totalBatal += Integer.parseInt(formatOnlyNumber(partList.get(position).get("HARGA_JASA").asString()));

                                totalBiaya -= Integer.parseInt(formatOnlyNumber(partList.get(position).get("NET").asString()));
                                ;
                                totalTambah -= Integer.parseInt(formatOnlyNumber(partList.get(position).get("NET").asString()));
                                ;

                                find(R.id.et_total_tambah_or_batal, EditText.class).setText(RP + formatRp(String.valueOf(totalTambah)));
                                find(R.id.et_total_akhir, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));

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
        });
    }

    private void initRecylerviewJasaLain() {
        if (isKonfirmasi) {
            rvJasaLain = dialogView.findViewById(R.id.recyclerView2);
        } else {
            rvJasaLain = findViewById(R.id.rv_jasa_lain);
        }

        rvJasaLain.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJasaLain.setHasFixedSize(false);
        rvJasaLain.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("NAMA_KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText(RP + formatRp(jasaList.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Part / Jasa ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                totalBatal += Integer.parseInt(formatOnlyNumber(jasaList.get(position).get("HARGA_JASA").asString()));

                                totalBiaya -= Integer.parseInt(formatOnlyNumber(jasaList.get(position).get("NET").asString()));
                                ;
                                totalTambah -= Integer.parseInt(formatOnlyNumber(jasaList.get(position).get("NET").asString()));
                                ;

                                find(R.id.et_total_tambah_or_batal, EditText.class).setText(RP + formatRp(String.valueOf(totalTambah)));
                                find(R.id.et_total_akhir, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));

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
        });
    }

    @SuppressLint("NewApi")
    private void initToolbarKonfirmasi() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Konfirmasi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showDialogKonfirmasi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_konfirmasi_part_jasa, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        isKonfirmasi = true;
        initToolbarKonfirmasi();
        initRecylerViewPart();
        initRecylerviewJasaLain();

        TextView tvTittle = dialogView.findViewById(R.id.tv_tittle_konfirmasi);
        LinearLayout lyPart = dialogView.findViewById(R.id.ly_part);
        LinearLayout lyJasaLain = dialogView.findViewById(R.id.ly_jasa_lain);
        Button btnLanjut = dialogView.findViewById(R.id.btn_simpan);

        tvTittle.setText("*KURANGI PART / JASA YG BERLEBIH DENGAN TOMBOL X");
        lyPart.setVisibility(partList.size() == 0 ? View.GONE : View.VISIBLE);
        lyJasaLain.setVisibility(jasaList.size() == 0 ? View.GONE : View.VISIBLE);
        btnLanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countClick == 1) {
                    updateTambahOrBatal();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isKonfirmasi = false;
                rvPart = findViewById(R.id.recyclerView_part_checkin3);
                rvJasaLain = findViewById(R.id.recyclerView_jasalain_checkin3);
                initRecylerViewPart();
                initRecylerviewJasaLain();
                Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private void saveDataUsulanMekanik() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("aktivitas", "USULAN MEKANIK");
                args.put("partList", partList.toJson());
                args.put("jasaList", jasaList.toJson());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equals("OK")) {
                    showSuccess("USULAN PART & JASA BERHASIL DI TAMBAHKAN");
                    Intent intent = new Intent();
                    intent.putExtra("IS_USULAN", 1);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void updateTambahOrBatal() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            String konfirmasiTambah = "";

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String estimasiSesudah = find(R.id.et_tgl_estimasi, EditText.class).getText().toString() + " " +
                        find(R.id.et_jam_estimasi, EditText.class).getText().toString();
                args.put("action", "update");
                args.put("nopol", nopol);
                args.put("noPonsel", noPonsel);
                args.put("idCheckin", idCheckin);
                args.put("isPartKosong", isPartKosong ? "Y" : "N");
                if (isTambah) {
                    args.put("aktivitas", "TAMBAH PART - JASA");
                    //args.put("detailId", idCheckinDetail);
                    args.put("layanan", layanan);
                    args.put("parts", partList.toJson());
                    args.put("jasaLain", jasaList.toJson());
                    args.put("idDetail", getIntentStringExtra(ID));
                    args.put("estimasiSesudah", estimasiSesudah);

                    if (isWait || (isNotWait && !isKonfirmasiTambah)) {
                        konfirmasiTambah = "N";
                    } else {
                        konfirmasiTambah = "Y";
                    }
                    args.put("isKonfirmasiTambah", konfirmasiTambah);
                }
                if (isBatal) {
                    args.put("aktivitas", "BATAL PART");
                    args.put("jumlahBatal", String.valueOf(totalBatal));
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_KONTROL_LAYANAN), args));
                result.getError();
                Log.d("cok__", "run: " + result);
            }

            @Override
            public void runUI() {
                AppApplication.getMessageTrigger();
                if (result.asString().isEmpty()) {
                    showError("SEDANG ADA GANGGUAN SERVER, SILAHKAN HUBUNGI SUPPORT. ATAU CHECK KONTROL LAYANAN ", Toast.LENGTH_LONG);
                    finish();
                    return;
                }
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (konfirmasiTambah.equals("N")) {
                        Intent intent = new Intent(getActivity(), KontrolLayanan_Activity.class);
                        intent.putExtra("NOPOL", nopol);
                        showNotification(getActivity(), "Tambah Part - Jasa", formatNopol(nopol), "CHECKIN", intent);
                    }
                    showSuccess("Menambahkan Part Berhasil");
                    Intent i = new Intent();
                    i.putExtra(ID, idCheckin);
                    i.putExtra("TOTAL_TAMBAH", formatOnlyNumber(find(R.id.et_total_tambah_or_batal, EditText.class).getText().toString()));
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    showError(ERROR_INFO);
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
                i.putExtra("IS_USULAN_MEKANIK", isUsulanMekanik);
                i.putExtra("KENDARAAN_ID", kendaraanID);
                i.putExtra("KM", kmKendaraan);
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_part:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                i.putExtra("LAYANAN_ID", layananId);
                i.putExtra("KENDARAAN_ID", kendaraanID);
                i.putExtra("PEKERJAAN", pekerjaan);
                startActivityForResult(i, REQUEST_CARI_PART);
                break;
            case R.id.btn_simpan:
                countClick++;
                if (isUsulanMekanik) {
                    if (partList.asArray().isEmpty() && jasaList.asArray().isEmpty()) {
                        showWarning("PART DAN JASA BELUM DI TAMBAHKAN");
                    } else {
                        saveDataUsulanMekanik();
                    }
                } else {
                    if (isWait && !isSign) {
                        showWarning("TANDA TANGAN BELUM TERISI");
                    } else if (find(R.id.ly_not_konfirmasi_tambah).getVisibility() == View.VISIBLE &&
                            find(R.id.et_tgl_estimasi, EditText.class).getText().toString().isEmpty() &&
                            find(R.id.et_jam_estimasi, EditText.class).getText().toString().isEmpty()) {
                        showWarning("ESTIMASI SELSAI BELUM TERISI");
                    } else if (partList.asArray().isEmpty() && jasaList.asArray().isEmpty()) {
                        showWarning("PART DAN JASA BELUM DI TAMBAHKAN");
                    } else {
                        showDialogKonfirmasi();
                    }
                }

                break;
            case R.id.img_btn_kalender_konfirmasi:
                getDatePicker(find(R.id.et_tgl_konfirmasi, EditText.class));
                break;
            case R.id.img_btn_kalender_estimasi:
                getDatePicker(find(R.id.et_tgl_estimasi, EditText.class));
                break;
            case R.id.img_btn_jam_konfirmasi:
                getTimePicker(find(R.id.et_jam_konfirmasi, EditText.class));
                break;
            case R.id.img_btn_jam_estimasi:
                getTimePicker(find(R.id.et_jam_estimasi, EditText.class));
                break;
            case R.id.btn_ttd:
                if (!checkPermission()) {
                    Intent intent = new Intent(getActivity(), Capture.class);
                    intent.putExtra("NOPOL", nopol);
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


    @Override
    protected void onResume() {
        super.onResume();
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
                    dataAccept = Nson.readJson(getIntentStringExtra(data, PART));
                    isPartKosong = false;
                    if (isUsulanMekanik) {
                        i = new Intent(getActivity(), JumlahPart_Mekanik_Activity.class);
                        i.putExtra(DATA, dataAccept.toJson());
                    } else {
                        i = new Intent(getActivity(), JumlahPart_Checkin_Activity.class);
                        i.putExtra("KM", kmKendaraan);
                        i.putExtra(DATA, dataAccept.toJson());
                        i.putExtra(TAMBAH_PART, "");
                        i.putExtra("KM", kmKendaraan);
                    }
                    i.putExtra("LAYANAN_ID", layananId);
                    i.putExtra("KENDARAAN_ID", kendaraanID);
                    startActivityForResult(i, REQUEST_HARGA_PART);

                    break;
                case REQUEST_HARGA_PART:
                    try {
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        if(data != null && data.getStringExtra("PART_KOSONG").equals("Y"))
                            isPartKosong = true;
                        dataAccept.set("CHECKIN_ID", idCheckin);
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();

                        totalBiaya += Integer.parseInt(formatOnlyNumber(dataAccept.get("NET").asString()));
                        totalTambah += Integer.parseInt(formatOnlyNumber(dataAccept.get("NET").asString()));
                        if (!isUsulanMekanik) {
                            totalWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                            totalWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                        }
                    } catch (Exception e) {
                        showWarning(PART + e.getMessage(), Toast.LENGTH_LONG);
                    }
                    break;
            }

            if (isUsulanMekanik) {
                find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
            }
            find(R.id.et_total_tambah_or_batal, EditText.class).setText(RP + formatRp(String.valueOf(totalTambah)));
            find(R.id.et_total_akhir, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
        }
    }

    private void dialogIgnoreStock() {
        Messagebox.showDialog(getActivity(), "Konfirmasi", "PART KOSNG TIDAK BISA TAMBAH", "Ya", null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("KM", kmKendaraan);
                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                i.putExtra(TAMBAH_PART, "Y");
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        }, null);

    }
}