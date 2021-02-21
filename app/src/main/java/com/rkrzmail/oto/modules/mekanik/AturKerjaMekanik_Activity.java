package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.oto.modules.checkin.History_Activity;
import com.rkrzmail.oto.modules.checkin.TambahPartJasaDanBatal_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_KELUHAN;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_HISTORY;
import static com.rkrzmail.utils.ConstUtils.REQUEST_TAMBAH_PART_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.TAMBAH_PART;
import static com.rkrzmail.utils.ConstUtils.TIDAK_MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.TOTAL_BIAYA;

public class AturKerjaMekanik_Activity extends AppActivity implements View.OnClickListener {

    private EditText etNoAntrian;
    private EditText etJenis;
    private EditText etLayanan;
    private EditText etNopol;
    private EditText etNoKunci;
    private EditText etNamaPelanggan;
    private EditText etMulai;
    private EditText etSelesai;
    private EditText etSisaWaktu;
    private EditText etPengambilan;
    private RecyclerView rvPointLayanan, rvKeluhan;
    private ImageButton imgStart;

    private long timerWork = 0;
    private final long oneSeconds = 1000;

    private final Nson partJasaList = Nson.newArray();
    private final Nson keluhanList = Nson.newArray();
    private Nson n;

    private String sisaWaktuPaused = "";
    private String idCheckin = "", mekanik = "", catatanMekanik = "", idMekanikKerja = "", statusDone = "", noHp = "";
    private String totalBiaya = "";
    private String merkLKKWajib = "";
    private boolean isGaransiLKK = false;

    private int countClick = 0;
    private int kmKendaraan = 0;
    private int waktuHari = 0, waktuJam = 0, waktuMenit = 0;

    private boolean isRework = false;
    private boolean isStart = false;
    private boolean isStop = false;
    private boolean isHplus = false;
    private boolean isInspeksi = false;
    private boolean isNotWait = false, isKonfirmasiTambahan = false;
    private boolean isDissmissAndStop = false;
    private boolean isLkk = false;

    private AlertDialog alertDialog;
    private CountDownTimer cTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kerja_mekanik);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Perintah Kerja Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbarKeluhan(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Keluhan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initToolbarPointLayanan(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Point Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initComponent() {
        etNoAntrian = findViewById(R.id.et_noAntrian_kerjaMekanik);
        etJenis = findViewById(R.id.et_jenis_kerjaMekanik);
        etLayanan = findViewById(R.id.et_layanan_kerjaMekanik);
        etNopol = findViewById(R.id.et_nopol_kerjaMekanik);
        etNoKunci = findViewById(R.id.et_noKunci_kerjaMekanik);
        etNamaPelanggan = findViewById(R.id.et_namaP_kerjaMekanik);
        etMulai = findViewById(R.id.et_Ewaktu_kerjaMekanik);
        etSelesai = findViewById(R.id.et_Eselesai_kerjaMekanik);
        imgStart = findViewById(R.id.imgBtn_start);
        ImageButton imgNote = findViewById(R.id.imgBtn_note);
        ImageButton imgStop = findViewById(R.id.imgBtn_stop);
        //EditText etCatatanMekanik = findViewById(R.id.et_catatan_mekanik);
        etSisaWaktu = findViewById(R.id.et_sisa_waktu);
        etPengambilan = findViewById(R.id.et_pengambilan);

        loadData();

        imgNote.setOnClickListener(this);
        find(R.id.img_btn_keluhan).setOnClickListener(this);
        find(R.id.img_btn_point_layanan).setOnClickListener(this);
        find(R.id.img_btn_lkk).setOnClickListener(this);
        imgStart.setOnClickListener(this);
        imgStop.setOnClickListener(this);
        find(R.id.img_btn_tambah_part).setOnClickListener(this);
        find(R.id.img_btn_history).setOnClickListener(this);
        find(R.id.img_btn_katalog).setOnClickListener(this);
        find(R.id.img_btn_diagnostic).setOnClickListener(this);
        find(R.id.img_btn_my_code).setOnClickListener(this);
        find(R.id.img_btn_berkala).setOnClickListener(this);
        find(R.id.img_btn_tambah_part).setOnClickListener(this);
    }

    private void loadData() {
        n = Nson.readJson(getIntentStringExtra(DATA));

        find(R.id.tl_jam_home).setVisibility(n.get("JAM_HOME").asString().isEmpty() | n.get("JAM_HOME") == null ? View.GONE : View.VISIBLE);
        find(R.id.tl_alamat).setVisibility(n.get("ALAMAT").asString().isEmpty() | n.get("ALAMAT") == null ? View.GONE : View.VISIBLE);
        find(R.id.tl_no_kunci).setVisibility(n.get("NO_KUNCI").asString().isEmpty() | n.get("NO_KUNCI") == null ? View.GONE : View.VISIBLE);
        find(R.id.tl_pengambilan).setVisibility(n.get("PENGAMBILAN").asString().isEmpty() | n.get("PENGAMBILAN") == null ? View.GONE : View.VISIBLE);
        isHplus = n.get("ANTRIAN").asString().equals("H+");
        isRework = n.get("STATUS_SELESAI").asString().equals("MEKANIK PAUSE");

        merkLKKWajib = n.get("MERK_LKK_WAJIB").asString();
        totalBiaya = n.get("TOTAL_BIAYA").asString();
        kmKendaraan = n.get("KM").asInteger();
        isNotWait = n.get("TIDAK_MENUNGGU").asString().equals("Y") & !n.get("TIDAK_MENUNGGU").asString().isEmpty();
        isKonfirmasiTambahan = n.get("KONFIRMASI_TAMBAHAN").asString().equals("Y") & !n.get("KONFIRMASI_TAMBAHAN").asString().isEmpty();
        String lamaLayanan = totalWaktuKerja(
                n.get("WAKTU_KERJA_HARI").asString(),
                n.get("WAKTU_KERJA_JAM").asString(),
                n.get("WAKTU_KERJA_MENIT").asString());
        noHp = n.get("NO_PONSEL").asString();
        etNoAntrian.setText(n.get("NO_ANTRIAN").asString());
        etNopol.setText(formatNopol(n.get("NOPOL").asString()));
        etNoKunci.setText(n.get("NO_KUNCI").asString());
        etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
        etMulai.setText(lamaLayanan);
        etSelesai.setText(Tools.setFormatDateTimeFromDb(n.get("ESTIMASI_SELESAI").asString(), "yyyy-MM-dd hh:mm", "dd/MM-hh:mm", false));
        etJenis.setText(n.get("JENIS_KENDARAAN").asString());
        etLayanan.setText(n.get("LAYANAN").asString());
        etPengambilan.setText(n.get("PENGAMBILAN").asString());
        etSisaWaktu.setText(n.get("SISA_WAKTU").asString());
        idCheckin = n.get(ID).asString();
        mekanik = n.get("MEKANIK").asString();
        idMekanikKerja = n.get("MEKANIK_KERJA_ID").asString();
        sisaWaktuPaused = n.get("SISA_WAKTU").asString();

        find(R.id.cb_tidak_menunggu, CheckBox.class).setChecked(isNotWait);
        find(R.id.cb_tambahPartJasa, CheckBox.class).setChecked(n.get("KONFIRMASI_TAMBAHAN").asString().equals("Y"));
        find(R.id.cb_buangPart, CheckBox.class).setChecked(n.get("BUANG_PART").asString().equals("Y"));

        viewLayananPartJasa();
        viewKeluhan();
        setTimer();
    }

    @SuppressLint("SetTextI18n")
    private void setTimer() {
        if (isRework) {
            try {
                int[] multiplier = {3600000, 60000};
                String[] splits = sisaWaktuPaused.split(":");
                for (int i = 0; i < splits.length; i++) {
                    timerWork += (Integer.parseInt(splits[i]) * multiplier[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date mulai = null;
            Date selesai = null;
            try {
                mulai = sdf.parse(etMulai.getText().toString());
                selesai = sdf.parse(etSelesai.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long waktuMulai = 0;
            if (mulai != null) {
                waktuMulai = mulai.getTime();
            }
            long waktuSelesai = 0;
            if (selesai != null) {
                waktuSelesai = selesai.getTime();
            }
            timerWork = waktuSelesai - waktuMulai;
        }
    }


    private void initRecyclerviewPointLayanan(View view) {
        rvPointLayanan = view.findViewById(R.id.recyclerView);
        rvPointLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvPointLayanan.setHasFixedSize(true);
        rvPointLayanan.setAdapter(new NikitaMultipleViewAdapter(partJasaList, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);

                if (partJasaList.get(position).get("STATUS_DETAIL").asString().equals("TAMBAH PART - JASA")) {
                    viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.VISIBLE);
                } else {
                    viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                }

                viewHolder.find(R.id.img_delete).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText("" + (position + 1));
                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class)
                            .setText(RP + formatRp(partJasaList.get(position).get("HARGA_JASA_PART").asString()));
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                            .setText(RP + formatRp(partJasaList.get(position).get("HARGA_PART").asString()));
                } else {
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(partJasaList.get(position).get("AKTIVITAS").asString());
                    if (partJasaList.get(position).get("HARGA_JASA_LAIN") == null) {
                        viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class).setVisibility(View.GONE);
                    } else {
                        viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                                .setText(RP + formatRp(partJasaList.get(position).get("HARGA_JASA_LAIN").asString()));
                    }

                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewKeluhan(View dialogView) {
        rvKeluhan = dialogView.findViewById(R.id.recyclerView);
        rvKeluhan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvKeluhan.setHasFixedSize(true);
        rvKeluhan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText(keluhanList.get(position).get("NO").asString() + ". ");
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
            }
        });

    }

    private void initEditTextDialog(final boolean isKm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.item_single_field_edit_text, null);
        builder.setView(dialogView);

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(isKm ? "KM Kendaraan" : "Catatan Mekanik");

        TextInputLayout tlEt = dialogView.findViewById(R.id.tl_edit_text);
        final EditText etEditText = dialogView.findViewById(R.id.et_edit_text);
        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);

        if (isKm) {
            tlEt.setHint("KM");
            etEditText.setText("");
            etEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setCancelable(false);
        } else {
            tlEt.setHint("CATATAN MEKANIK");
            etEditText.setText(catatanMekanik);
            etEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            if (isDissmissAndStop) {
                builder.setCancelable(false);
            } else {
                builder.setCancelable(true);
            }

        }

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isKm) {
                    if (etEditText.getText().toString().isEmpty()) {
                        showWarning("KM HARUS DI ISI");
                    } else {
                        kmKendaraan = Integer.parseInt(formatOnlyNumber(etEditText.getText().toString()));
                        startWork();
                        alertDialog.dismiss();
                    }
                } else {
                    catatanMekanik = etEditText.getText().toString();
                    alertDialog.dismiss();
                    if (isDissmissAndStop) {
                        stopWork();
                    }
                }
            }
        });

        builder.create();
        alertDialog = builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initKeluhanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);
        initToolbarKeluhan(dialogView);
        initRecylerviewKeluhan(dialogView);
        if (keluhanList.size() > 0) {
            Objects.requireNonNull(rvKeluhan.getAdapter()).notifyDataSetChanged();
        }

        builder.create();
        alertDialog = builder.show();
    }

    @SuppressLint("NewApi")
    private void initPointLayananDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);
        initToolbarPointLayanan(dialogView);
        initRecyclerviewPointLayanan(dialogView);
        Objects.requireNonNull(rvPointLayanan.getAdapter()).notifyDataSetChanged();

        builder.create();
        alertDialog = builder.show();
    }

    @SuppressLint("NewApi")
    private void viewKeluhan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KELUHAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    keluhanList.asArray().clear();
                    keluhanList.asArray().addAll(result.get("data").asArray());
                    Log.d("no__", "runUI: " + keluhanList);
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void viewLayananPartJasa() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("detail", "PART JASA CHECKIN");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PERINTAH_KERJA_MEKANIK), args));
                partJasaList.asArray().clear();
                partJasaList.asArray().addAll(result.get("data").asArray());

                args.remove("detail");
                args.put("detail", "JASA LAYANAN");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PERINTAH_KERJA_MEKANIK), args));
                partJasaList.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < partJasaList.size(); i++) {
                        waktuHari += partJasaList.get(i).get("WAKTU_KERJA_HARI").asInteger();
                        waktuJam += partJasaList.get(i).get("WAKTU_KERJA_JAM").asInteger();
                        waktuMenit += partJasaList.get(i).get("WAKTU_KERJA_MENIT").asInteger();
                        if (partJasaList.get(i).get("GARANSI_LAYANAN").asString().equals("Y")) {
                            isGaransiLKK = true;
                        }
                        if (partJasaList.get(i).get("MERK").asString().equals(merkLKKWajib)) {
                            isLkk = true;
                        }
                        if (partJasaList.get(i).get("INSPEKSI_PART").asString().equals("Y") ||
                                partJasaList.get(i).get("INSPEKSI_JASA").asString().equals("Y") ||
                                partJasaList.get(i).get("INSPEKSI_MST_PART").asString().equals("Y") ||
                                partJasaList.get(i).get("INSPEKSI_MST_JASA").asString().equals("Y")) {
                            isInspeksi = true;
                        }
                    }
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    private void startWork() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                if (isRework) {
                    args.put("aktivitas", "RESTART");
                } else {
                    args.put("aktivitas", "START");
                }
                args.put("km", String.valueOf(kmKendaraan));
                args.put("idKerja", idMekanikKerja);
                args.put("id", idCheckin);
                args.put("mekanik", mekanik);
                args.put("sisaWaktu", etSisaWaktu.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    viewLayananPartJasa();
                    if (!isRework) {
                        idMekanikKerja = result.get("data").get(0).asString();
                    }
                    if (!isHplus) {
                        startTimer();
                    }
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }

    private void pauseWork() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("id", idCheckin);
                args.put("aktivitas", "PAUSE");
                args.put("iddetail", idMekanikKerja);
                args.put("sisaWaktu", etSisaWaktu.getText().toString());
                args.put("catatan", catatanMekanik);
                args.put("km", String.valueOf(kmKendaraan));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Pekerjaan di Paused");
                    stopTimer();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(ERROR_INFO);
                }
            }

        });
    }

    private void stopWork() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("km", String.valueOf(kmKendaraan));
                args.put("aktivitas", "DONE");
                args.put("id", idCheckin);
                args.put("iddetail", idMekanikKerja);
                args.put("sisaWaktu", etSisaWaktu.getText().toString());
                args.put("catatan", catatanMekanik);
                args.put("nopol", formatNopol(etNopol.getText().toString()));
                args.put("noPonsel", noHp);
                args.put("tidakMenunggu", isNotWait ? "Y" : "N");
                if (isInspeksi) {
                    statusDone = "PENUGASAN INSPEKSI";
                    args.put("status", statusDone);
                } else {
                    statusDone = "PELAYANAN SELESAI";
                    args.put("status", statusDone);
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    //showMessageInvalidNotif(getActivity(), result.get("data").get("MESSAGE_INFO").asString(), null);
                    stopTimer();
                    showSuccess("Pekerjaan Selesai");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }

    private void startTimer() {
        cTimer = new CountDownTimer(timerWork, oneSeconds) {
            public void onTick(long timer) {
                int seconds = (int) (timer / 1000) % 60;
                int minutes = (int) ((timer / (1000 * 60)) % 60);
                int hours = (int) ((timer / (1000 * 60 * 60)) % 24);
                @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d", hours, minutes);
                etSisaWaktu.setText(result);
                etSisaWaktu.requestFocus();
            }

            public void onFinish() {
            }
        };

        cTimer.start();
    }

    private void stopTimer() {
        if (cTimer != null)
            cTimer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public void onBackPressed() {
        if (isStart) {
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Pekerjaan sedang berlangsung, Pause Pekerjaan?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pauseWork();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

        } else {
            finish();
        }
    }

    @SuppressLint({"NewApi", "NonConstantResourceId"})
    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.imgBtn_note:
                isDissmissAndStop = false;
                initEditTextDialog(false);
                break;
            case R.id.imgBtn_stop:
                if (isStop) {
                    if (catatanMekanik.isEmpty()) {
                        isDissmissAndStop = true;
                        showWarning("Catatan Harus di Isi", Toast.LENGTH_LONG);
                        initEditTextDialog(false);
                    } else {
                        stopWork();
                    }
                } else {
                    showWarning("Pekerjaan Belum di Mulai");
                }
                break;
            case R.id.imgBtn_start:
                countClick++;
                if (countClick == 1) {
                    isStart = true;
                    isStop = true;
                    imgStart.setImageDrawable(getResources().getDrawable(R.drawable.icon_paused));
                    if (kmKendaraan == 0) {
                        initEditTextDialog(true);
                    } else {
                        startWork();
                    }

                } else if (countClick > 1) {
                    boolean isPaused = true;
                    imgStart.setImageDrawable(getResources().getDrawable(R.drawable.icon_start));
                    pauseWork();
                }
                break;
            case R.id.img_btn_point_layanan:
                initPointLayananDialog();
                break;
            case R.id.img_btn_keluhan:
                initKeluhanDialog();
                break;
            case R.id.img_btn_lkk:
                SetDataForClaim();
                break;
            case R.id.img_btn_tambah_part:
                intent = new Intent(getActivity(), TambahPartJasaDanBatal_Activity.class);
                intent.putExtra("CHECKIN_ID", idCheckin);
                intent.putExtra("NO_PONSEL", noHp);
                intent.putExtra(TOTAL_BIAYA, formatOnlyNumber(totalBiaya));
                intent.putExtra(TAMBAH_PART, "");
                intent.putExtra("NOPOL", etNopol.getText().toString());
                intent.putExtra("KM", kmKendaraan);
                if (isNotWait) {
                    intent.putExtra(TIDAK_MENUNGGU, TIDAK_MENUNGGU);
                } else {
                    intent.putExtra(MENUNGGU, MENUNGGU);
                }
                intent.putExtra("KONFIRMASI_TAMBAH", isKonfirmasiTambahan);
                startActivityForResult(intent, REQUEST_TAMBAH_PART_JASA_LAIN);
                break;
            case R.id.img_btn_history:
                intent = new Intent(getActivity(), History_Activity.class);
                intent.putExtra("ALL", "ALL");
                intent.putExtra("NOPOL", etNopol.getText().toString().replaceAll(" ", ""));
                startActivityForResult(intent, REQUEST_HISTORY);
                break;
            case R.id.img_btn_katalog:
                showInfo("SEDANG DALAM TAHAP PENGEMBANGAN");
                break;
            case R.id.img_btn_diagnostic:
                showInfo("SEDANG DALAM TAHAP PENGEMBANGAN");
                break;
            case R.id.img_btn_my_code:
                startActivityForResult(new Intent(getActivity(), MyCode.class), REQUEST_BARCODE);
                break;
            case R.id.img_btn_berkala:
                showInfo("SEDANG DALAM TAHAP PENGEMBANGAN");
                break;
        }
    }

    private void SetDataForClaim() {
        Nson nson = Nson.newObject();
        nson.set("IDCHECKIN", idCheckin);
        nson.set("TANGGAL_CHECKIN", n.get("TANGGAL_CHECKIN").asString());
        nson.set("NAMA_MEKANIK", mekanik);
        nson.set("NOPOL", n.get("NOPOL").asString());
        nson.set("KM", n.get("KM").asString());
        nson.set("MERK", n.get("MERK").asString());
        nson.set("VARIAN", n.get("VARIAN").asString());
        nson.set("KODE_TIPE", n.get("KODE_TIPE").asString());
        nson.set("TAHUN_PRODUKSI", n.get("TAHUN_PRODUKSI").asString());
        nson.set("TANGGAL_PEMBELIAN", n.get("TANGGAL_PEMBELIAN").asString());


        Intent i = new Intent(getActivity(), LkkClaimMekanik_Activity.class);
        i.putExtra(DATA, nson.toJson());
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_DETAIL:
                    showSuccess("SUKSES MENAMBAHKAN CLAIM PARTS", Toast.LENGTH_LONG);
                    break;
                case REQUEST_TAMBAH_PART_JASA_LAIN:
                case REQUEST_BARCODE:
                    viewLayananPartJasa();
                    break;
            }
        }
    }
}