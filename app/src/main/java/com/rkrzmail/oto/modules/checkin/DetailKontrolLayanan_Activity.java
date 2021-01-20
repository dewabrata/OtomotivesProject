package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.SET_ANTRIAN;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ESTIMASI_WAKTU;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
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
            etDp, etSisa, etEstimasiSebelum, etEstimasiLama, etEstimasiSelesai, etPengambilan, etKeteranganTambahan, etNamaLayanan;
    private RecyclerView rvDetail;
    private Spinner spAktifitas, spNamaMekanik;
    private TextView tvNamaLayanan, tvBiayaLayanan;

    private Nson mekanikArray = Nson.newArray(), idMekanikArray = Nson.newArray();
    private Nson detailCheckinList = Nson.newArray(), partCheckinList = Nson.newArray();
    private Nson batalPartJasaList = Nson.newArray();
    private final Nson partMessage = Nson.newArray();

    private boolean isMekanik = false, isMekanikFromCheckin = false; // true = part, false = jasa
    private boolean isKurangi = false;
    private boolean isMekanikSelected = false;
    private boolean isEstimasi = false, isKonfirmasiTambahan = false;

    private String idCheckin = "", idAntrian = "";
    private String status = "";
    private String namaMekanik = "", idMekanik = "";
    private String jenisAntrian = "", noPonsel = "";
    private String merkKendaraan = "";

    private int totalTambahPart = 0;
    private int totalBiaya = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontrol_layanan_);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Kontrol Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        etPengambilan = findViewById(R.id.et_pengambilan);
        spAktifitas = findViewById(R.id.sp_aktifitas);
        spNamaMekanik = findViewById(R.id.sp_nama_mekanik);
        rvDetail = findViewById(R.id.recyclerView);
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan);
        tvBiayaLayanan = findViewById(R.id.tv_biaya_layanan);

        initRecyclerviewDetail();
        loadData();
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), false);
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        Log.d(TAG, "loadData: " + data);

        totalBiaya = data.get("TOTAL_BIAYA").asInteger();
        idCheckin = data.get(ID).asString();
        jenisAntrian = data.get("ANTRIAN").asString();
        noPonsel = data.get("NO_PONSEL").asString();
        merkKendaraan = data.get("NO_PONSEL").asString();
        isEstimasi = data.get("STATUS").asString().equals("LAYANAN ESTIMASI") & !data.get("STATUS").asString().isEmpty();
        isKonfirmasiTambahan = data.get("KONFIRMASI_TAMBAHAN").asString().equals("Y") & !data.get("KONFIRMASI_TAMBAHAN").asString().isEmpty();

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
                data.get("WAKTU_KERJA_HARI").asString(),
                data.get("WAKTU_KERJA_JAM").asString(),
                data.get("WAKTU_KERJA_MENIT").asString()));
        etEstimasiSelesai.setText(Tools.setFormatDateTimeFromDb(data.get("ESTIMASI_SELESAI").asString(), "yyyy-MM-dd hh:mm", "dd/MM-hh:mm", false));
        etPengambilan.setText(data.get("JAM_PENGAMBILAN").asString());
        etKeteranganTambahan.setText(data.get("KETERANGAN_TAMBAHAN").asString());
        etNamaLayanan.setText(data.get("LAYANAN").asString());
        tvNamaLayanan.setText(data.get("LAYANAN").asString());
        tvBiayaLayanan.setText(RP + formatRp(formatOnlyNumber(data.get("BIAYA_LAYANAN").asString())));

        find(R.id.cb_tungguConfirm_biaya, CheckBox.class).setChecked(data.get("TUNGGU_KONFIRMASI_BIAYA").asString().equals("Y"));
        find(R.id.cb_tidak_menunggu, CheckBox.class).setChecked(data.get("PELANGGAN_TIDAK_MENUNGGU").asString().equals("Y"));
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

                if (spAktifitas.getSelectedItem().toString().equals("--PILIH--")) {
                    showWarning("Aktivitas Harus di Pilih");
                    spAktifitas.performClick();
                    spAktifitas.requestFocus();
                    return;
                }
                updateData(idCheckin);
            }
        });

        setSpAktifitas();
        setSpMekanik(data.get("MEKANIK").asString());
        getDetailCheckin(idCheckin);
    }

    private void initRecyclerviewDetail() {
        rvDetail.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        rvDetail.setHasFixedSize(false);
        rvDetail.setAdapter(new NikitaMultipleViewAdapter(detailCheckinList, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);

                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(detailCheckinList.get(position).get("NO").asString() + ". ");
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                            RP + formatRp(detailCheckinList.get(position).get("HARGA_PART").asString()));
                    if (!detailCheckinList.get(position).get("JASA_EXTERNAL").asString().isEmpty()) {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(detailCheckinList.get(position).get("JASA_EXTERNAL").asString()));
                    } else {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(detailCheckinList.get(position).get("HARGA_JASA_PART").asString()));
                    }

                    if (isKurangi) {
                        viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                        viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Messagebox.showDialog(getActivity(), "Konfirmasi", "Kurangi Part?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        batalPartJasaList.add(Nson.newObject()
                                                .set("ID", detailCheckinList.get(position).get("CHECKIN_DETAIL_ID"))
                                                .set("PART_ID", detailCheckinList.get(position).get("PART_ID"))
                                                .set("JASA_ID", "")
                                                .set("JUMLAH", detailCheckinList.get(position).get("JUMLAH"))
                                                .set("TUGAS_PART_ID", detailCheckinList.get(position).get("TUGAS_PART_ID"))
                                                .set("NET", detailCheckinList.get(position).get("NET"))
                                        );
                                        detailCheckinList.asArray().remove(position);
                                        notifyItemRemoved(position);
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
                } else if (itemType == ITEM_VIEW_2) {
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                            .setText(RP + formatRp(detailCheckinList.get(position).get("HARGA_JASA").asString()));
                    viewHolder.find(R.id.tv_no, TextView.class).setText(detailCheckinList.get(position).get("NO").asString() + ". ");
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(detailCheckinList.get(position).get("AKTIVITAS").asString());
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class).
                            setText(RP + formatRp(detailCheckinList.get(position).get("HARGA_JASA_LAIN").asString()));
                    if (isKurangi) {
                        viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                        viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Messagebox.showDialog(getActivity(), "Konfirmasi", "Kurangi Jasa?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        batalPartJasaList.add(Nson.newObject()
                                                .set("ID", detailCheckinList.get(position).get("CHECKIN_DETAIL_ID"))
                                                .set("PART_ID", "")
                                                .set("JASA_ID", detailCheckinList.get(position).get("JASA_ID"))
                                                .set("JUMLAH", detailCheckinList.get(position).get("JUMLAH"))
                                                .set("TUGAS_PART_ID", detailCheckinList.get(position).get("TUGAS_PART_ID"))
                                                .set("NET", detailCheckinList.get(position).get("NET"))
                                        );
                                        detailCheckinList.asArray().remove(position);
                                        notifyItemRemoved(position);
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
                            partMessage.add(Nson.newObject()
                                    .set("DETAIL_ID", detailCheckinList.get(i).get("CHECKIN_DETAIL_ID").asString())
                                    .set("PART_ID", detailCheckinList.get(i).get("PART_ID").asInteger())
                                    .set("JUMLAH", detailCheckinList.get(i).get("JUMLAH").asInteger())
                                    .set("LOKASI_PART_ID", detailCheckinList.get(i).get("JUMLAH").asInteger())
                                    .set("NET", detailCheckinList.get(i).get("NET").asInteger())
                            );
                        }
                    }
                    Log.d("part__", "runUI: " + partMessage);
                    rvDetail.getAdapter().notifyDataSetChanged();
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
                || etStatus.getText().toString().equals("PELAYANAN SELESAI")
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
        } else if (etStatus.getText().toString().equals("TAMBAH PART - JASA") &&
                (isKonfirmasiTambahan &&
                        find(R.id.cb_tidak_menunggu, CheckBox.class).isChecked())) {
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
                    startActivityForResult(intent, REQUEST_NEW_CS);
                }

                if (status.equals("MESSAGE PELANGGAN")) {
                    moveWa();
                }
                if (status.equals("KURANGI PART - JASA")) {
                    isKurangi = true;
                    rvDetail.getAdapter().notifyDataSetChanged();
                } else {
                    isKurangi = false;
                    rvDetail.getAdapter().notifyDataSetChanged();
                }
                if (status.equals("CHECKIN")) {
                    setNoAntrian(jenisAntrian);
                    status = "CHECKIN ANTRIAN";
                }

                etKeteranganTambahan.setEnabled(status.equals("MESSAGE PELANGGAN") || status.equals("BATAL BENGKEL") || status.equals("BATAL PELANGGAN"));
                if (status.equals("TAMBAH PART - JASA")) { //|| status.equals("TAMBAH PART - JASA MSG") || status.equals("TAMBAH PART - JASA OK")
                    Intent intent = new Intent(getActivity(), TambahPartJasaDanBatal_Activity.class);
                    //intent.putExtra(ID, dataDetailList.toJson());
                    intent.putExtra("CHECKIN_ID", idCheckin);
                    intent.putExtra(TOTAL_BIAYA, formatOnlyNumber(etTotal.getText().toString()));
                    intent.putExtra(TAMBAH_PART, "");
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

    private String mssgTambahPart() {
        String result = "";
        return result;
    }

    private void moveWa() {
        Messagebox.showDialog(getActivity(),
                "Konfirmasi", "Message Pelanggan ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
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
                        dialog.dismiss();
                    }
                });
    }

    private void updateData(final String id) {
        if (status.contains("MESSAGE") || status.equals("DATA KENDARAAN")) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("status", status);
                args.put("id", id);

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
                    showSuccess("Aktivitas Berhasil di Perbaharui");
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_ANTRIAN), args));
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
