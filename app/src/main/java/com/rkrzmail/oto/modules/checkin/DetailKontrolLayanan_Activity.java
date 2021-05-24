package com.rkrzmail.oto.modules.checkin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;
import com.rkrzmail.oto.modules.mekanik.PerintahKerjaMekanik_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.ANTRIAN;
import static com.rkrzmail.utils.APIUrls.VIEW_KELUHAN;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ESTIMASI_WAKTU;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_HISTORY;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;
import static com.rkrzmail.utils.ConstUtils.REQUEST_TAMBAH_PART_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_WA;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.TAMBAH_PART;
import static com.rkrzmail.utils.ConstUtils.TIDAK_MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.TOTAL_BIAYA;

public class DetailKontrolLayanan_Activity extends AppActivity {

    private static final String TAG = "DetailKontrol__";

    private EditText etNoAntrian, etStatus, etNopol, etNoKunci, etNamaPelanggan, etTotal,
            etDp, etSisa, etEstimasiSebelum, etEstimasiLama, etEstimasiSelesai, etKeteranganTambahan, etNamaLayanan;
    private RecyclerView rvDetail, rvKeluhan, rvPart, rvJasaLain;
    private Spinner spAktifitas, spNamaMekanik;
    private TextView tvNamaLayanan, tvBiayaLayanan, tvTglAmbil, tvJamAmbil;
    private AlertDialog alertDialog;
    private View dialogView;

    private final Nson mekanikArray = Nson.newArray();
    private final Nson idMekanikArray = Nson.newArray();
    private final Nson detailCheckinList = Nson.newArray();
    private final Nson partCheckinList = Nson.newArray();
    private final Nson batalPartJasaList = Nson.newArray();
    private final Nson partMessage = Nson.newArray();
    private final Nson keluhanList = Nson.newArray();
    private final Nson kurangiPartJasaList = Nson.newArray();
    private final Nson partList = Nson.newArray();
    private final Nson jasaList = Nson.newArray();

    private boolean isMekanik = false, isMekanikFromCheckin = false; // true = part, false = jasa
    private boolean isKurangi = false;
    private boolean isMekanikSelected = false;
    private boolean isEstimasi = false, isKonfirmasiTambahan = false;
    private boolean isModifiedTidakMenunggu = false;

    private String idCheckin = "", idAntrian = "";
    private String status = "";
    private String namaMekanik = "", idMekanik = "";
    private String jenisAntrian = "", noPonsel = "";
    private String merkKendaraan = "";
    private String waktuEstimasi = "", tglEstimasi = "";

    private int totalTambahPart = 0;
    private int totalBiaya = 0;
    private int kmKendaraan = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontrol_layanan_);
        initToolbar();
        initComponent();
        initRecyclerviewDetail();
        loadData();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Kontrol Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbarKeluhan(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Keluhan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    private void initComponent() {
        etNoAntrian = findViewById(R.id.et_noAntrian);
        etStatus = findViewById(R.id.et_status);
        etNopol = findViewById(R.id.et_nopol);
        etNoKunci = findViewById(R.id.et_noKunci);
        etNamaLayanan = findViewById(R.id.et_namaLayanan);
        etNamaPelanggan = findViewById(R.id.et_nama_pelanggan);
        etTotal = findViewById(R.id.et_totalBiaya);
        etDp = findViewById(R.id.et_dp);
        etSisa = findViewById(R.id.et_sisa);
        etEstimasiSebelum = findViewById(R.id.et_estimasi_sebelum);
        etEstimasiLama = findViewById(R.id.et_estimasi_lama);
        etEstimasiSelesai = findViewById(R.id.et_estimasi_selesai);
        etKeteranganTambahan = findViewById(R.id.et_keterangan_tambahan);
        spAktifitas = findViewById(R.id.sp_aktifitas);
        spNamaMekanik = findViewById(R.id.sp_nama_mekanik);
        rvDetail = findViewById(R.id.recyclerView);
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan);
        tvBiayaLayanan = findViewById(R.id.tv_biaya_layanan);
        tvTglAmbil = findViewById(R.id.tv_tgl_ambil);
        tvJamAmbil = findViewById(R.id.tv_jam_ambil);
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), false);
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        Log.d(TAG, "loadData: " + data);

        try {
            String[] splitJamEstimasi = data.get("ESTIMASI_SELESAI").asString().split("-");
            waktuEstimasi = splitJamEstimasi[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        totalBiaya = data.get("TOTAL_BIAYA").asInteger();
        idCheckin = data.get(ID).asString();
        jenisAntrian = data.get("ANTRIAN").asString();
        noPonsel = data.get("NO_PONSEL").asString();
        merkKendaraan = data.get("NO_PONSEL").asString();
        isEstimasi = data.get("STATUS").asString().equals("LAYANAN ESTIMASI") & !data.get("STATUS").asString().isEmpty();
        kmKendaraan = data.get("KM").asInteger();

        find(R.id.et_catatan_mekanik, EditText.class).setText(data.get("CATATAN_MEKANIK").asString());
        etNoAntrian.setText(data.get("NO_ANTRIAN").asString());
        etStatus.setText(data.get("STATUS_KONTROL").asString());
        etNopol.setText(formatNopol(data.get("NOPOL").asString()));
        etNoKunci.setText(data.get("NO_KUNCI").asString());
        etNamaPelanggan.setText(data.get("NAMA_PELANGGAN").asString());
        //etTotal.setText(RP + formatRp(data.get("TOTAL_BIAYA").asString()));
        etDp.setText(RP + formatRp(data.get("DP").asString()));
        etSisa.setText(RP + formatRp(data.get("SISA").asString()));
        etEstimasiSebelum.setText(data.get("ESTIMASI_SEBELUM").asString());
        etEstimasiLama.setText(totalWaktuKerja(
                data.get("WAKTU_LAYANAN_LAMA_HARI").asString(),
                data.get("WAKTU_LAYANAN_LAMA_JAM").asString(),
                data.get("WAKTU_LAYANAN_LAMA_MENIT").asString()));
        String estimasiSelesai;
        if((jenisAntrian.equals("STANDART") || jenisAntrian.equals("EXPRESS")) && data.get("PELANGGAN_TIDAK_MENUNGGU").asString().equals("N")){
            estimasiSelesai = data.get("ESTIMASI_SELESAI").asString();
        }else{
            estimasiSelesai = Tools.setFormatDateTimeFromDb(data.get("ESTIMASI_SELESAI").asString(), "yyyy-MM-dd hh:mm", "dd/MM-hh:mm", false);
        }
        etEstimasiSelesai.setText(estimasiSelesai);
        try {
            String[] waktuAmbil = data.get("WAKTU_AMBIL").asString().split(" ");
            tvTglAmbil.setText(waktuAmbil[0]);
            tvJamAmbil.setText(waktuAmbil[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        etKeteranganTambahan.setText(data.get("KETERANGAN_TAMBAHAN").asString());
        etNamaLayanan.setText(data.get("LAYANAN").asString());
        tvNamaLayanan.setText(data.get("LAYANAN").asString());
        tvBiayaLayanan.setText(RP + formatRp(formatOnlyNumber(data.get("BIAYA_LAYANAN").asString())));

        find(R.id.btn_history).setEnabled(!data.get("TOTAL_SERVICE").asString().isEmpty());
        find(R.id.cb_tidak_menunggu, CheckBox.class).setChecked(data.get("PELANGGAN_TIDAK_MENUNGGU").asString().equals("Y"));
        find(R.id.cb_tungguConfirm_biaya, CheckBox.class).setChecked(data.get("TUNGGU_KONFIRMASI_BIAYA").asString().equals("Y"));
        find(R.id.cb_konfirm_tambah, CheckBox.class).setChecked(data.get("KONFIRMASI_TAMBAHAN").asString().equals("Y"));
        find(R.id.cb_buangPart, CheckBox.class).setChecked(data.get("BUANG_PART").asString().equals("Y"));
        find(R.id.cb_antar, CheckBox.class).setChecked(data.get("ANTAR_JEMPUT").asString().equals("Y"));
        isMekanikFromCheckin = !data.get("MEKANIK").asString().isEmpty();

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!status.isEmpty() && status.equals("BATAL PELANGGAN")) {
                    if (etKeteranganTambahan.getText().toString().isEmpty()) {
                        etKeteranganTambahan.setError("Keterangan Batal Harus di Isi");
                        return;
                    }
                }

                if (isMekanik && namaMekanik.equals("--PILIH--")) {
                    showWarning("Nama Mekanik Harus di Pilih");
                    return;
                }

                /*if (spAktifitas.getSelectedItem().toString().equals("--PILIH--")) {
                    showWarning("Aktivitas Harus di Pilih");
                    spAktifitas.performClick();
                    spAktifitas.requestFocus();
                    return;
                }*/
                if (find(R.id.cb_tidak_menunggu, CheckBox.class).isChecked() && tvJamAmbil.getText().toString().isEmpty()) {
                    showWarning("WAKTU AMBIL HARUS DI ISI UNTUK PELANGGAN TIDAK MENUNGGU", Toast.LENGTH_LONG);
                } else {
                    if (status.equals("KURANGI PART - JASA")) {
                        showDialogKonfirmasi();
                    } else {
                        updateData(idCheckin);
                    }
                }
            }
        });

        find(R.id.cb_tidak_menunggu, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!compoundButton.isChecked()) {
                    tvTglAmbil.setText("");
                    tvJamAmbil.setText("");
                }
            }
        });

        find(R.id.btn_keluhan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initKeluhanDialog();
            }
        });

        find(R.id.btn_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), History_Activity.class);
                intent.putExtra("ALL", "ALL");
                intent.putExtra("NOPOL", etNopol.getText().toString().replaceAll(" ", ""));
                startActivityForResult(intent, REQUEST_HISTORY);
            }
        });

        tvJamAmbil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTimePickerDialogWaktuAmbil();
            }
        });
        tvTglAmbil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDatePickerTglAmbil();
            }
        });

        getDetailCheckin(idCheckin);
        setSpMekanik(data.get("MEKANIK").asString());
        viewKeluhan();
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
        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
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

    private void initRecyclerviewDetail() {
        rvDetail.setLayoutManager(new LinearLayoutManager(this));
        rvDetail.setHasFixedSize(false);
        rvDetail.setAdapter(new NikitaMultipleViewAdapter(detailCheckinList, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);

                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText("" + (position + 1));
                if (isKurangi) {
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String tittle = detailCheckinList.get(position).get("PARENT VIEW TYPE").asInteger() == ITEM_VIEW_1 ? "Part ?" : "Jasa Lain ?";
                            Messagebox.showDialog(getActivity(), "Konfirmasi", "Kurangi " + tittle, "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int index = position;
                                    kurangiPartJasaList.add(detailCheckinList.get(position));
                                    if (!detailCheckinList.get(position).get("PART_ID").asString().isEmpty()) {
                                        partList.add(detailCheckinList.get(position));
                                    } else if (!detailCheckinList.get(position).get("JASA_ID").asString().isEmpty()) {
                                        jasaList.add(detailCheckinList.get(position));
                                    }

                                    totalBiaya -= detailCheckinList.get(position).get("NET").asInteger();
                                    etTotal.setText(RP + formatRp(String.valueOf(totalBiaya)));
                                    detailCheckinList.asArray().remove(position);
                                    notifyItemRemoved(position);
                                    rvDetail.getAdapter().notifyDataSetChanged();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                } else {
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                }

                if (detailCheckinList.get(position).get("STATUS_DETAIL").asString().equals("TAMBAH PART - JASA") &&
                        !etStatus.getText().toString().contains("SELESAI")) {
                    viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.VISIBLE);
                } else {
                    viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                }

                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                            RP + formatRp(detailCheckinList.get(position).get("HARGA_PART").asString()));
                    if (!detailCheckinList.get(position).get("JASA_EXTERNAL").asString().isEmpty()) {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(detailCheckinList.get(position).get("JASA_EXTERNAL").asString()));
                    } else {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(detailCheckinList.get(position).get("HARGA_JASA_PART").asString()));
                    }
                } else if (itemType == ITEM_VIEW_2) {
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                            .setText(RP + formatRp(detailCheckinList.get(position).get("HARGA_JASA").asString()));
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("AKTIVITAS").asString());
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class).
                            setText(RP + formatRp(detailCheckinList.get(position).get("HARGA_JASA_LAIN").asString()));

                }
            }
        });
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
                    find(R.id.btn_keluhan, Button.class).setEnabled(!keluhanList.asArray().isEmpty());
                    Log.d("no__", "runUI: " + keluhanList);
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void getDetailCheckin(final String id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("detail", "TRUE");
                args.put("id", id);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (totalBiaya == 0) {
                        totalBiaya = result.get("data").get(0).get("TOTAL_BIAYA").asInteger();
                    }
                    etTotal.setText(RP + formatRp(String.valueOf(totalBiaya)));
                    detailCheckinList.asArray().clear();
                    detailCheckinList.asArray().addAll(result.get("data").asArray());
                    for (int i = 0; i < detailCheckinList.size(); i++) {
                        partCheckinList.add(Nson.newObject()
                                .set("CHECKIN_DETAIL_ID", detailCheckinList.get(i).get("CHECKIN_DETAIL_ID"))
                                .set("PART_ID", detailCheckinList.get(i).get("PART_ID"))
                                .set("JUMLAH", detailCheckinList.get(i).get("JUMLAH"))
                                .set("CHECKIN_ID", idCheckin)
                        );
                        if (detailCheckinList.get(i).get("STATUS_DETAIL").asString().equals("TAMBAH PART - JASA")) {
                            isKonfirmasiTambahan = detailCheckinList.get(i).get("KONFIRMASI_TAMBAHAN").asString().equals("Y");
                            partMessage.add(Nson.newObject()
                                    .set("DETAIL_ID", detailCheckinList.get(i).get("CHECKIN_DETAIL_ID").asString())
                                    .set("PART_ID", detailCheckinList.get(i).get("PART_ID").asInteger())
                                    .set("JUMLAH", detailCheckinList.get(i).get("JUMLAH").asInteger())
                                    .set("LOKASI_PART_ID", detailCheckinList.get(i).get("LOKASI_PART_ID").asInteger())
                                    .set("NET", detailCheckinList.get(i).get("NET").asInteger())
                            );
                        }
                    }
                    Log.d("part__", "runUI: " + partMessage);
                    rvDetail.getAdapter().notifyDataSetChanged();
                    setSpAktifitas();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void setSpMekanik(final String mekanik) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("penugasan", "KONTROL LAYANAN");
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
                    idMekanikArray.add(0);
                    for (int i = 0; i < data.get("data").size(); i++) {
                        idMekanikArray.add(Nson.newObject().set("ID", data.get("data").get(i).get("NO_PONSEL").asString()).set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }
                    Log.d(TAG, "MEKANIK : " + mekanikArray);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mekanikArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spNamaMekanik.setAdapter(spinnerAdapter);
                    if (!mekanik.isEmpty()) {
                        for (int in = 0; in < spNamaMekanik.getCount(); in++) {
                            if (spNamaMekanik.getItemAtPosition(in).toString().contains(mekanik)) {
                                spNamaMekanik.setSelection(in);
                                spNamaMekanik.setEnabled(false);
                                isMekanikSelected = true;
                                break;
                            }
                        }
                    }
                } else {
                    showInfoDialog("Nama Mekanik Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpMekanik("");
                        }
                    });
                }
            }
        });

        spNamaMekanik.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                namaMekanik = parent.getSelectedItem().toString();
                if (idMekanikArray.get(position).get("NAMA").asString().equals(parent.getSelectedItem().toString())) {
                    idMekanik = idMekanikArray.get(position).get("ID").asString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSpAktifitas() {
        List<String> aktifitasList = new ArrayList<>();
        aktifitasList.add("--PILIH--");
        aktifitasList.add("MESSAGE PELANGGAN");
        aktifitasList.add("DATA KENDARAAN");
        if (etStatus.getText().toString().equals("CHECKIN ANTRIAN") ||
                (etStatus.getText().toString().contains("DP, ANTRIAN") && !etStatus.getText().toString().equals("TUNGGU DP"))) {
            aktifitasList.add("BATAL BENGKEL");
            aktifitasList.add("BATAL PELANGGAN");
            aktifitasList.add("PENUGASAN MEKANIK");
        } else if (etStatus.getText().toString().equals("BATAL PART")
                || etStatus.getText().toString().equals("MEKANIK SELESAI")
                || etStatus.getText().toString().equals("PERINTAH ANTAR")
                || etStatus.getText().toString().equals("REFUND DP")) {

        } else if (etStatus.getText().toString().equals("LAYANAN ESTIMASI")) {
            aktifitasList.add("CHECKIN");
        } else if (etStatus.getText().toString().equals("KONFIRMASI BIAYA")) {
            aktifitasList.add("BIAYA OK");
            aktifitasList.add("BATAL DP");
            aktifitasList.add("BATAL PELANGGAN");
        } else if (etStatus.getText().toString().equals("TUNGGU KONFIRMASI")) {
            aktifitasList.add("PENUGASAN MEKANIK");
        } else if (etStatus.getText().toString().equals("TUNGGU DP")) {
            aktifitasList.add("BATAL PELANGGAN");
        } else if (etStatus.getText().toString().equals("KURANGI PART")) {
            aktifitasList.add("GANTI MEKANIK");
            aktifitasList.add("TAMBAH PART - JASA");
            aktifitasList.add("BATAL PART");
            aktifitasList.add("BATAL BENGKEL");
        } else if (etStatus.getText().toString().equals("CASH") ||
                etStatus.getText().toString().equals("DEBIT") ||
                etStatus.getText().toString().equals("KREDIT") ||
                etStatus.getText().toString().equals("INVOICE") ||
                etStatus.getText().toString().equals("EPAY")) {
            aktifitasList.add("CHECK OUT");
        } else if (isKonfirmasiTambahan && !etStatus.getText().toString().contains("SELESAI")) {
            aktifitasList.add("BATAL BENGKEL");
            aktifitasList.add("BATAL PELANGGAN");
            aktifitasList.add("TAMBAH PART - JASA OKAY");
            aktifitasList.add("TAMBAH PART - JASA DI TOLAK");
            aktifitasList.add("KURANGI PART - JASA");
            aktifitasList.add("GANTI MEKANIK");
        } else if (etStatus.getText().toString().equals("PART MEKANIK")) {
            aktifitasList.add("BATAL BENGKEL");
            aktifitasList.add("BATAL PELANGGAN");
            aktifitasList.add("KURANGI PART - JASA");
            aktifitasList.add("TAMBAH PART - JASA");
            aktifitasList.add("GANTI MEKANIK");
        } else if (etStatus.getText().toString().equals("TAMBAH PART - JASA OKAY") ||
                etStatus.getText().toString().equals("TAMBAH PART - JASA DI TOLAK") ||
                etStatus.getText().toString().equals("TAMBAH PART - JASA") ||
                etStatus.getText().toString().equals("PENUGASAN MEKANIK") ||
                etStatus.getText().toString().equals("MEKANIK PAUSE") ||
                etStatus.getText().toString().equals("MEKANIK MULAI") ||
                etStatus.getText().toString().equals("GANTI MEKANIK")
        ) {
            aktifitasList.add("BATAL BENGKEL");
            aktifitasList.add("BATAL PELANGGAN");
            aktifitasList.add("KURANGI PART - JASA");
            aktifitasList.add("GANTI MEKANIK");
        }else if(etStatus.getText().toString().contains("SELESAI")) {
            isKonfirmasiTambahan = false;
            aktifitasList.add("TAMBAH PART - JASA");
            aktifitasList.add("KURANGI PART - JASA");
        }

        ArrayAdapter<String> aktifitasAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, aktifitasList);
        aktifitasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAktifitas.setAdapter(aktifitasAdapter);
        spAktifitas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                status = parent.getSelectedItem().toString();
                if (status.equals("PENUGASAN MEKANIK") && !isMekanikSelected) {
                    isMekanik = true;
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), true);
                } else if (status.equals("GANTI MEKANIK")) {
                    isMekanik = true;
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), true);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), false);
                }

                if (status.equals("DATA KENDARAAN")) {
                    Intent intent = new Intent(getActivity(), Checkin2_Activity.class);
                    intent.putExtra(ID, idCheckin);
                    intent.putExtra("NO_PONSEL", noPonsel);
                    intent.putExtra("KONFIRMASI DATA", "Data Kendaraan");
                    intent.putExtra("MERK", merkKendaraan);
                    intent.putExtra("NOPOL", etNopol.getText().toString().replaceAll(" ", ""));
                    startActivityForResult(intent, REQUEST_NEW_CS);
                }

                if (status.equals("MESSAGE PELANGGAN")) {
                    moveWa();
                }
                if (status.equals("KURANGI PART - JASA")) {
                    isKurangi = true;
                    Objects.requireNonNull(rvDetail.getAdapter()).notifyDataSetChanged();
                } else {
                    isKurangi = false;
                    Objects.requireNonNull(rvDetail.getAdapter()).notifyDataSetChanged();
                }
                if (status.equals("CHECKIN")) {
                    setNoAntrian(jenisAntrian);
                    status = "CHECKIN ANTRIAN";
                }

                etKeteranganTambahan.setEnabled(status.equals("MESSAGE PELANGGAN") || status.equals("BATAL BENGKEL") || status.equals("BATAL PELANGGAN"));
                if (status.equals("TAMBAH PART - JASA")) { //|| status.equals("TAMBAH PART - JASA MSG") || status.equals("TAMBAH PART - JASA OK")
                    Intent intent = new Intent(getActivity(), TambahPartJasaDanBatal_Activity.class);
                    //intent.putExtra(ID, dataDetailList.toJson());
                    intent.putExtra("KM", kmKendaraan);
                    intent.putExtra("CHECKIN_ID", idCheckin);
                    intent.putExtra(TOTAL_BIAYA, formatOnlyNumber(etTotal.getText().toString()));
                    intent.putExtra(TAMBAH_PART, "");
                    intent.putExtra("NO_PONSEL", noPonsel);
                    intent.putExtra("NOPOL", etNopol.getText().toString());
                    intent.putExtra("KONFIRMASI_TAMBAH", isKonfirmasiTambahan);
                    intent.putExtra("LAYANAN", etNamaLayanan.getText().toString());
                    intent.putExtra(ESTIMASI_WAKTU, etEstimasiSebelum.getText().toString());
                    if (find(R.id.cb_tidak_menunggu, CheckBox.class).isChecked()) {
                        intent.putExtra(TIDAK_MENUNGGU, TIDAK_MENUNGGU);
                    } else {
                        intent.putExtra(MENUNGGU, MENUNGGU);
                    }
                    startActivityForResult(intent, REQUEST_TAMBAH_PART_JASA_LAIN);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void moveWa() {
        Messagebox.showDialog(getActivity(),
                "Konfirmasi", "Message Pelanggan ?", "WhatsApp", "Telephone", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + formatOnlyNumber(noPonsel)));//+ "&text=" + Utility.urlEncode("Konfirmasi"))
                        startActivityForResult(intent, REQUEST_WA);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int PERMISSION_ALL = 1;
                        String[] PERMISSIONS = {
                                Manifest.permission.CALL_PHONE
                        };
                        if (!hasPermissions(PERMISSIONS)) {
                            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
                        }
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + formatOnlyNumber(noPonsel)));
                        startActivity(intent);
                    }
                });
    }

    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("NewApi")
    private void initToolbarKonfirmasi() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Konfirmasi Kurangi Part - Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initRvKurangiPart() {
        rvPart = dialogView.findViewById(R.id.recyclerView);
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
                try {
                    if (Tools.isNumeric(partList.get(position).get("HARGA_PART").asString())) {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                    } else {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                                .setText(partList.get(position).get("HARGA_PART").asString());
                    }
                    if (Tools.isNumeric(partList.get(position).get("HARGA_JASA_PART").asString()) ||
                            !partList.get(position).get("HARGA_JASA_PART").asString().isEmpty()) {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(partList.get(position).get("HARGA_JASA_PART").asString()));
                    } else {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText("");
                    }
                } catch (Exception e) {
                    showError(e.getMessage());
                }
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("MERK").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
            }
        });
    }

    private void initRvKurangiJasaLain() {
        rvJasaLain = dialogView.findViewById(R.id.recyclerView2);
        rvJasaLain.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJasaLain.setHasFixedSize(false);
        rvJasaLain.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);

                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText("Rp. " + formatRp(jasaList.get(position).get("HARGA_JASA_LAIN").asString()));

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

        initToolbarKonfirmasi();
        initRvKurangiPart();
        initRvKurangiJasaLain();

        TextView tvTittle = dialogView.findViewById(R.id.tv_tittle_konfirmasi);
        LinearLayout lyPart = dialogView.findViewById(R.id.ly_part);
        LinearLayout lyJasaLain = dialogView.findViewById(R.id.ly_jasa_lain);
        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);
        Button btnBatal = dialogView.findViewById(R.id.btn_hapus);

        btnBatal.setVisibility(View.VISIBLE);
        btnBatal.setText("BATAL");
        tvTittle.setText("*KONFIRMASI KURANGI PART - JASA");

        if(partList.size() > 0){
            lyPart.setVisibility(View.VISIBLE);
            rvPart.getAdapter().notifyDataSetChanged();
        }else{
            lyPart.setVisibility(View.GONE);
        }

        if(jasaList.size() > 0){
            lyJasaLain.setVisibility(View.VISIBLE);
            rvJasaLain.getAdapter().notifyDataSetChanged();
        }else{
            lyJasaLain.setVisibility(View.GONE);
        }

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < kurangiPartJasaList.size(); i++) {
                    if(!detailCheckinList.asArray().contains(kurangiPartJasaList.get(i))){
                        detailCheckinList.add(kurangiPartJasaList.get(i));
                    }
                }
                rvDetail.getAdapter().notifyDataSetChanged();
                clearList();
                alertDialog.dismiss();
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int position = 0; position < kurangiPartJasaList.size(); position++) {
                    batalPartJasaList.add(Nson.newObject()
                            .set("ID", kurangiPartJasaList.get(position).get("CHECKIN_DETAIL_ID").asInteger())
                            .set("PART_ID", kurangiPartJasaList.get(position).get("PARENT VIEW TYPE").asInteger() == 1 ? kurangiPartJasaList.get(position).get("PART_ID").asInteger() : "")
                            .set("JASA_ID", kurangiPartJasaList.get(position).get("PARENT VIEW TYPE").asInteger() == 2 ? kurangiPartJasaList.get(position).get("JASA_ID").asInteger() : "")
                            .set("JUMLAH", kurangiPartJasaList.get(position).get("JUMLAH").asInteger())
                            .set("TUGAS_PART_ID", kurangiPartJasaList.get(position).get("TUGAS_PART_ID").asInteger())
                            .set("LOKASI_PART_ID", kurangiPartJasaList.get(position).get("LOKASI_PART_ID").asInteger())
                            .set("NET", kurangiPartJasaList.get(position).get("NET").asInteger())//
                    );
                }
                updateData(idCheckin);
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        alertDialog.setCancelable(false);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private void clearList(){
        partList.asArray().clear();
        jasaList.asArray().clear();
        kurangiPartJasaList.asArray().clear();
    }


    private void updateData(final String idCheckin) {
        if (status.contains("MESSAGE") || status.equals("DATA KENDARAAN")) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            String tglAmbil = find(R.id.tv_tgl_ambil, TextView.class).getText().toString();

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("status", status);
                args.put("idCheckin", idCheckin);
                args.put("nopol", formatNopol(etNopol.getText().toString()));
                if (find(R.id.cb_tidak_menunggu, CheckBox.class).isChecked()) {
                    if (tglAmbil.isEmpty()) {
                        tglAmbil = currentDateTime("dd/MM");
                    } else {
                        tglAmbil = Tools.formatDate(tglAmbil, "dd/MM");
                    }
                    args.put("waktuAmbil", tglAmbil + " " + tvJamAmbil.getText().toString());
                } else {
                    args.put("waktuAmbi", "");
                }

                args.put("tidakMenunggu", find(R.id.cb_tidak_menunggu, CheckBox.class).isChecked() ? "Y" : "N");
                if (isEstimasi) {
                    args.put("isEstimasi", "Y");
                }

                if (isMekanik) {
                    args.put("aktivitas", "MEKANIK");
                    args.put("mekanik", namaMekanik);
                    args.put("mekanikId", idMekanik);
                }

                switch (status) {
                    case "KURANGI PART - JASA":
                        args.put("aktivitas", "KURANGI PART - JASA");
                        args.put("partJasaList", batalPartJasaList.toJson());
                        break;
                    case "BATAL DP":
                        args.put("aktivitas", "BATAL DP");
                        args.put("partJasaList", partCheckinList.toJson());
                        break;
                    case "TAMBAH PART - JASA OKAY":
                        args.put("aktivitas", "TAMBAH PART - JASA MESSAGE");
                        args.put("isPartMessage", "MESSAGE OKAY");
                        args.put("partJasaList", partMessage.toJson());
                        break;
                    case "TAMBAH PART - JASA DI TOLAK":
                        args.put("aktivitas", "TAMBAH PART - JASA MESSAGE");
                        args.put("isPartMessage", "MESSAGE BATAL");
                        args.put("partJasaList", partMessage.toJson());
                        break;
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (result.get("data").asString().equals("PENUGASAN MEKANIK")) {
                        Intent intent = new Intent(getActivity(), PerintahKerjaMekanik_Activity.class);
                        showNotification(getActivity(), "PENUGASAN MEKANIK", formatNopol(etNopol.getText().toString()), "MEKANIK", intent);
                    }
                    showSuccess("AKTIVITAS BERHASIL DI PERBAHARUI");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo(ERROR_INFO);
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ANTRIAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    idAntrian = result.asString();
                    find(R.id.et_no_antrian_checkin4, EditText.class).setText(generateNoAntrian(jenisAntrian, result.asString()));
                    Log.d(TAG, "NO_ANTRIAN: " + generateNoAntrian(jenisAntrian, result.asString()));
                }
            }
        });
    }

    // if part not yet accepted they cannot be cancel
    private void viewSerahTerimaPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("Gagal");
                }
            }
        });
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

            }
        });
        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }


    private void getTimePickerDialogWaktuAmbil() {
        final String[] waktuAmbil = {""};
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
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
                tvJamAmbil.setText(formattedTime);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    if (validateWaktuAmbil(false, waktuEstimasi, waktuAmbil[0])) {
                        showWarning("WAKTU AMBIL HARUS MELEBIHI ESTIMASI SELESAI");
                        tvJamAmbil.post(new Runnable() {
                            @Override
                            public void run() {
                                tvJamAmbil.performClick();
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        timePickerDialog.setTitle("Tentukan Waktu Ambil");
        timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
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

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_MEKANIK) {
            setSpMekanik("");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_TAMBAH_PART_JASA_LAIN) {
            totalTambahPart = Integer.parseInt(getIntentStringExtra(data, "TOTAL_TAMBAH"));
            getDetailCheckin(getIntentStringExtra(data, ID));
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN) {
            loadData();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS) {
            loadData();
        }
    }
}
