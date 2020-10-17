package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.jasa.JasaExternal_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLainBerkala_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.JumlahPart_HargaPart_Activity;
import com.rkrzmail.oto.modules.sparepart.PartBerkala_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.BATAL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_OTOMOTIVES;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.PARTS_UPPERCASE;
import static com.rkrzmail.utils.ConstUtils.PART_WAJIB;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BATAL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_HARGA_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_BERKALA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_EXTERNAL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PART_BERKALA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PART_EXTERNAL;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class Checkin3_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvPart;
    private RecyclerView rvJasaLain;
    public static final String TAG = "Checkin3___";
    private Spinner spLayanan;
    private AlertDialog alertDialog;
    RecyclerView rvPartWajib;
    View dialogView;

    private Nson layananArray = Nson.newArray();
    private Nson dataLayananList = Nson.newArray();
    private Nson partList = Nson.newArray();
    private Nson jasaList = Nson.newArray();
    private Nson lokasiLayananList = Nson.newArray();
    private Nson partWajibList = Nson.newArray();
    private Nson jasaGaransiList = Nson.newArray();
    private Nson daftarPartDummy = Nson.newArray();
    private Nson varianList = Nson.newArray();
    private Nson masterPartList = Nson.newArray();
    private Nson partIdList = Nson.newArray();
    private Nson partKosongList = Nson.newArray();

    private String namaLayanan, jenisLayanan = "", waktuMekanik = "", waktuInspeksi = "", waktuLayanan = "";
    private String jenisAntrian = "", isOutsource = "", noAntrian = "", discFasilitas = "";
    private int totalHarga = 0,
            totalPartJasa = 0,
            batasanKm = 0,
            batasanBulan = 0,
            jumlahPartWajib = 0;
    private int idAntrian = 0;
    private int hargaPartLayanan = 0; // afs, recall, otomotives
    private double sisaDp = 0;
    private List<Tools.TimePart> timePartsList = new ArrayList<>();
    private List<Integer> jumlahList = new ArrayList<>();
    private Tools.TimePart dummyTime = Tools.TimePart.parse("00:00:00");

    private boolean flagPartWajib = false,
            flagMasterPart = false,
            isPartWajib = false, isSelanjutnya = false, isDelete = false;
    private boolean isPartKosong = false;
    private boolean isBatal = false;
    private boolean isJasaExternal = false;
    private boolean isHplusLayanan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin3_);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("NewApi")
    private void initToolbarPartWajib(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Part Wajib Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initComponent() {
        initToolbar();
        find(R.id.btn_jasaLain_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_sparePart_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_jasaLainBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partExternal_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_lanjut_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_batal_checkin3, Button.class).setOnClickListener(this);

        spLayanan = findViewById(R.id.sp_layanan_checkin3);
        rvPart = findViewById(R.id.recyclerView_part_checkin3);
        rvJasaLain = findViewById(R.id.recyclerView_jasalain_checkin3);

        viewLayananBengkel();
        setSpNamaLayanan();
        componentValidation();
        initRecylerViewPart();
        initRecylerviewJasaLain();
    }

    private void initRecylerViewPart() {
        rvPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPart.setHasFixedSize(true);
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                try {
                    if (Tools.isNumeric(partList.get(position).get("HARGA_PART").asString())) {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                    } else {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(partList.get(position).get("HARGA_PART").asString());
                    }
                    if (Tools.isNumeric(partList.get(position).get("HARGA_JASA").asString()) || !partList.get(position).get("HARGA_JASA").asString().isEmpty()) {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));
                    } else {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText("");
                    }
                } catch (Exception e) {
                    showError(e.getMessage());
                }
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setText(partList.get(position).get("MERK").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Aktifitas ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nListArray.asArray().remove(position);
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
        });
    }

    private void initRecylerviewJasaLain() {
        rvJasaLain.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJasaLain.setHasFixedSize(true);
        rvJasaLain.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("NAMA_KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText("Rp. " + formatRp(jasaList.get(position).get("HARGA_JASA").asString()));
            }
        });
    }

    @SuppressLint("NewApi")
    private void initRecylerviewPartWajib(View dialogView) {
        rvPartWajib = dialogView.findViewById(R.id.recyclerView);
        rvPartWajib.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPartWajib.setHasFixedSize(false);
        rvPartWajib.setAdapter(new NikitaRecyclerAdapter(flagMasterPart ? masterPartList : partWajibList, R.layout.item_master_part_wajib_layanan) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                if (flagMasterPart) {
                    //viewHolder.find(R.id.tv_nama_master_part, TextView.class).setText(masterPartList.get("NAMA_MASTER").asString());
                    viewHolder.find(R.id.tv_merkPart, TextView.class).setText(masterPartList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_namaPart, TextView.class).setText(masterPartList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart, TextView.class).setText(masterPartList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_stockPart, TextView.class).setText(masterPartList.get(position).get("STOCK").asString());
                } else {
                    //viewHolder.find(R.id.cardView_master_part, CardView.class).setVisibility(View.GONE);
                    viewHolder.find(R.id.tv_nama_master_part, TextView.class).setText(partWajibList.get("NAMA_MASTER").asString());
                    viewHolder.find(R.id.tv_namaPart, TextView.class).setText(partWajibList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart, TextView.class).setText(partWajibList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_stockPart, TextView.class).setText(partWajibList.get(position).get("STOCK").asString());
                    viewHolder.find(R.id.tv_merkPart, TextView.class).setText(partWajibList.get(position).get("MERK_PART").asString());
                }

                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (flagMasterPart) {
                            masterPartList.asArray().remove(position);
                            notifyItemRemoved(position);
                        } else {
                            partWajibList.asArray().remove(position);
                            notifyItemRemoved(position);
                        }
                    }
                });

                if (isDelete) {
                    viewHolder.find(R.id.img_delete, ImageButton.class).performClick();
                }
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                daftarPartDummy.add(parent.get(position).get("PART_ID").asString());
                Intent i = new Intent(getActivity(), JumlahPart_HargaPart_Activity.class);
                if (flagMasterPart) {
                    i.putExtra(PART_WAJIB, masterPartList.get(position).toJson());
                    i.putExtra(MASTER_PART, discFasilitas);
                } else {
                    i.putExtra(PARTS_UPPERCASE, discFasilitas);
                    i.putExtra(PART_WAJIB, partWajibList.get(position).toJson());
                }
                i.putExtra("WAKTU_INSPEKSI", waktuInspeksi);
                i.putExtra("HARGA_LAYANAN", hargaPartLayanan);
                startActivityForResult(i, REQUEST_HARGA_PART);
            }
        }));
        Objects.requireNonNull(rvPartWajib.getAdapter()).notifyDataSetChanged();
    }

    private void setSelanjutnya(final String status, final String alasanBatal) {
        final boolean[] isTrue = {false};
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        final String layanan = spLayanan.getSelectedItem().toString();
        final String layananEstimasi = find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked() ? "Y" : "N";
        final String total = find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String dp = find(R.id.et_dp_checkin3, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String sisa = find(R.id.et_sisa_checkin3, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String tungguKonfirmasi = find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).isChecked() ? "Y" : "N";
        final int totalPartJasa = jasaList.size() + partList.size();
        final String waktuLayanan = dummyTime.toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                isSelanjutnya = true;
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("check", "1");
                args.put("id", nson.get("id").asString());
                args.put("status", status);

                if (isBatal) {
                    args.put("isBatal", "true");
                    args.put("alasanBatal", alasanBatal);
                } else {
                    if (isPartKosong) {
                        if (partKosongList.size() > 0) {
                            args.put("isPartKosong", "true");
                            args.put("partKosongList", partKosongList.toJson());
                        }
                    }

                    args.put("layanan", layanan);
                    args.put("layananestimasi", layananEstimasi);
                    args.put("total", total);
                    args.put("dp", dp);
                    args.put("sisa", sisa);
                    args.put("tunggu", tungguKonfirmasi);
                    args.put("partbook", partList.toJson());
                    args.put("jasabook", jasaList.toJson());
                    args.put("antrian", find(R.id.tv_jenis_antrian, TextView.class).getText().toString());
                    args.put("biayaLayanan", formatOnlyNumber(find(R.id.tv_biayaLayanan_checkin, TextView.class).getText().toString()));
                    //inserting waktu layanan part
                }

                args.put("waktuLayananHari", waktuLayanan.substring(0, 2));
                args.put("waktuLayananJam", waktuLayanan.substring(3, 5));
                args.put("waktuLayananMenit", waktuLayanan.substring(6, 8));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    isTrue[0] = true;
                    if (status.equalsIgnoreCase("LAYANAN ESTIMASI")) {
                        showSuccess("Layanan Di tambahkan Ke Daftar Kontrol Layanan");
                        setResult(RESULT_OK);
                        finish();
                    } else if (status.equalsIgnoreCase("TUNGGU KONFIRMASI")) {
                        showSuccess("Layanan Di tambahkan Ke Daftar Kontrol Layanan");
                        setResult(RESULT_OK);
                        finish();
                    } else if (status.equalsIgnoreCase("BATAL CHECKIN")) {
                        showSuccess("Layanan Di Batalkan Pelanggan, Data Di Masukkan Ke Kontrol Layanan");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        nson.set("TOTAL", find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString());
                        nson.set("WAKTU_LAYANAN", dummyTime.toString());
                        nson.set("LOKASI_LAYANAN", lokasiLayananList.toJson());
                        nson.set("PART_KOSONG", isPartKosong);
                        nson.set("OUTSOURCE", isOutsource);
                        nson.set("JENIS_ANTRIAN", find(R.id.tv_jenis_antrian, TextView.class).getText().toString());
                        nson.set("DP", formatOnlyNumber(find(R.id.et_dp_checkin3, EditText.class).getText().toString()));
                        nson.set("SISA", formatOnlyNumber(find(R.id.et_sisa_checkin3, EditText.class).getText().toString()));

                        if (layanan.equals("PERAWATAN LAINNYA")) {
                            nson.set("JENIS_LAYANAN", layanan);
                        } else {
                            nson.set("JENIS_LAYANAN", jenisLayanan);
                        }
                        Intent i = new Intent(getActivity(), Checkin4_Activity.class);
                        i.putExtra(DATA, nson.toJson());
                        startActivityForResult(i, REQUEST_CHECKIN);
                    }
                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void componentValidation() {
        find(R.id.cb_estimasi_checkin3, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(false);
                    if (totalHarga > 0) {
                        find(R.id.et_totalBiaya_checkin3, EditText.class).setText("Rp." + formatRp(String.valueOf(totalHarga)));
                    } else {
                        showWarning("Tambahkan Part & Jasa Terlebih Dahulu");
                    }
                } else {
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(true);
                }
            }
        });

        find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    find(R.id.cb_estimasi_checkin3, CheckBox.class).setEnabled(false);
                } else {
                    find(R.id.cb_estimasi_checkin3, CheckBox.class).setEnabled(true);
                }
            }
        });
    }

    private void setSpNamaLayanan() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("layanan", "CHECKIN");
                args.put("spec", "Bengkel");
                args.put("status", "AKTIF");
                args.put("model", data.get("model").asString());
                args.put("varian", data.get("varian").asString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    dataLayananList.asArray().addAll(result.asArray());
                    Log.d(TAG, "List Layanan Data : " + dataLayananList);
                    layananArray.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        layananArray.add(result.get(i).get("NAMA_LAYANAN").asString());
                    }
                    layananArray.add("PERAWATAN LAINNYA");

                    Log.d(TAG, "List Nama Layanan : " + layananArray);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, layananArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLayanan.setAdapter(spinnerAdapter);
                } else {
                    showInfo("Nama Layanan Gagal Di Muat");
                    setSpNamaLayanan();
                }
            }
        });

        spLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.equalsIgnoreCase("--PILIH--")) {
                    find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.GONE);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_btnPart_checkin3, LinearLayout.class), false);
                    find(R.id.btn_lanjut_checkin3, Button.class).setEnabled(false);
                } else {
                    find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.VISIBLE);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_btnPart_checkin3, LinearLayout.class), true);
                    find(R.id.btn_lanjut_checkin3, Button.class).setEnabled(true);
                }

                for (int i = 0; i < dataLayananList.size(); i++) {
                    partWajibList.asArray().clear();
                    masterPartList.asArray().clear();
                    if (dataLayananList.get(i).get("NAMA_LAYANAN").asString().equalsIgnoreCase(item)) {
                        jenisLayanan = dataLayananList.get(i).get("JENIS_LAYANAN").asString();
                        waktuLayanan = totalWaktu(
                                dataLayananList.get(i).get("WAKTU_LAYANAN_HARI").asString(),
                                dataLayananList.get(i).get("WAKTU_LAYANAN_JAM").asString(),
                                dataLayananList.get(i).get("WAKTU_LAYANAN_MENIT").asString());
                        waktuMekanik = totalWaktu(
                                dataLayananList.get(i).get("WAKTU_MEKANIK_HARI").asString(),
                                dataLayananList.get(i).get("WAKTU_MEKANIK_JAM").asString(),
                                dataLayananList.get(i).get("WAKTU_MEKANIK_MENIT").asString());
                        waktuInspeksi = totalWaktu(
                                dataLayananList.get(i).get("WAKTU_INSPEKSI_HARI").asString(),
                                dataLayananList.get(i).get("WAKTU_INSPEKSI_JAM").asString(),
                                dataLayananList.get(i).get("WAKTU_INSPEKSI_MENIT").asString());

                        //timePartsList.add(Tools.TimePart.parse(waktuLayanan));
                        timePartsList.add(Tools.TimePart.parse(waktuMekanik));
                        timePartsList.add(Tools.TimePart.parse(waktuInspeksi));
                        dummyTime.add(Tools.TimePart.parse(waktuMekanik)).add(Tools.TimePart.parse(waktuInspeksi));

                        Tools.TimePart mekanikWaktu = Tools.TimePart.parse(waktuMekanik).add(Tools.TimePart.parse(waktuInspeksi));
                        find(R.id.tv_waktu_layanan, TextView.class).setText("Total Waktu Layanan : " + mekanikWaktu.toString());
                        find(R.id.tv_jenis_antrian, TextView.class).setText(validasiAntrian(isPartKosong));

                        if (dataLayananList.get(i).get("LAYANAN_PART").size() > 0) {
                            partWajibList.asArray().addAll(dataLayananList.get(i).get("LAYANAN_PART").asArray());
                        }
                        if (jasaGaransiList.get(i).get("LAYANAN_GARANSI").size() > 0) {
                            jasaGaransiList.asArray().addAll(dataLayananList.get(i).get("LAYANAN_GARANSI").asArray());
                        }
                        find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.VISIBLE);
                        try {
                            if (Tools.isNumeric(dataLayananList.get(i).get("BIAYA_PAKET").asString())) {
                                totalHarga += dataLayananList.get(i).get("BIAYA_PAKET").asInteger();
                                find(R.id.tv_biayaLayanan_checkin, TextView.class).setText("Rp." + formatRp(dataLayananList.get(i).get("BIAYA_PAKET").asString()));
                            } else {
                                find(R.id.tv_biayaLayanan_checkin, TextView.class).setText("");
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "BiayaLayanan: " + e.getMessage());
                        }
                        find(R.id.tv_namaLayanan_checkin, TextView.class).setText(dataLayananList.get(i).get("NAMA_LAYANAN").asString());
                        lokasiLayananList.add(Nson.newObject().set("EMG", dataLayananList.get(i).get("LOKASI_LAYANAN_EMG"))
                                .set("HOME", dataLayananList.get(i).get("LOKASI_LAYANAN_HOME"))
                                .set("TENDA", dataLayananList.get(i).get("LOKASI_LAYANAN_TENDA"))
                                .set("TENDA", dataLayananList.get(i).get("LOKASI_LAYANAN_BENGKEL")));

                        batasanKm = dataLayananList.get(i).get("BATASAN_NON_PAKET_KM").asInteger();
                        batasanBulan = dataLayananList.get(i).get("BATASAN_NON_PAKET_BULAN").asInteger();
                        break;
                    }
                }

                if (partWajibList.size() > 0) {
                    for (int i = 0; i < partWajibList.size(); i++) {
                        masterPartList.asArray().addAll(partWajibList.get(i).get("PARTS").asArray());
                        hargaPartLayanan = partWajibList.get(i).get("HARGA").asInteger();
                        partIdList.add(partWajibList.get(i).get("PART_ID"));
                        discFasilitas = partWajibList.get(i).get("DISC_FASILITAS").asString();
                        jumlahPartWajib += partWajibList.get(i).get("JUMLAH").asInteger();
                        partWajibList.get(i).remove("PARTS");
                        jumlahList.add(partWajibList.get(i).get("JUMLAH").asInteger());
                    }

                    if (masterPartList.size() > 0) {
                        for (int i = 0; i < masterPartList.size(); i++) {
                            partIdList.add(masterPartList.get(i).get("PART_ID"));
                        }
                        flagMasterPart = true;
                    }

                    Log.d(TAG, "PART_ID : " + partIdList);
                    Log.d(TAG, "PART_WAJIB : " + partWajibList);
                    Log.d(TAG, "MASTER_PART : " + masterPartList);

                    initPartWajibLayanan();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    public static String totalWaktu(String hari, String jam, String menit) {
        boolean isSubsString = false;
        String[] result = new String[3];
        result[0] = hari;
        result[1] = jam;
        result[2] = menit;

        int incrementWaktu = 0;
        int calculateJam = 0;
        int calculateHari = 0;

        for (String s : result) {
            if (s.contains(":")) {
                isSubsString = true;
                break;
            }
        }

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

    private void viewLayananBengkel() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("layanan", "NAMA LAYANAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        varianList.add(result.get(i).get("VARIAN").asString());
                    }
                    Log.d(TAG, "VARIANLIST : " + varianList);
                }
            }
        });
    }

    private void initPartWajibLayanan() {
        if (partWajibList.size() > 0 || masterPartList.size() > 0) {
            flagPartWajib = true;
            isPartWajib = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.activity_list_basic, null);
            builder.setView(dialogView);
            builder.setCancelable(true);

            initToolbarPartWajib(dialogView);
            initRecylerviewPartWajib(dialogView);

            builder.create();
            alertDialog = builder.show();
        } else {
            showError("PART WAJIB NULL");
        }
    }

    private String validasiAntrian(boolean isHplusPartKosong) {
        String jenisAntrian = "";
        String totalLayanan = find(R.id.tv_waktu_layanan, TextView.class).getText().toString().replace("Total Waktu Layanan : ", "");
        Tools.TimePart maxAntrianExpress = Tools.TimePart.parse(getSetting("MAX_ANTRIAN_EXPRESS_MENIT"));
        Tools.TimePart maxAntrianStandard = Tools.TimePart.parse(getSetting("MAX_ANTRIAN_STANDART_MENIT"));
        Tools.TimePart totalLamaLayanan = Tools.TimePart.parse(find(R.id.tv_waktu_layanan, TextView.class).getText().toString().replace("Total Waktu Layanan : ", ""));
        try {
            @SuppressLint("SimpleDateFormat") Date maxExpress = new SimpleDateFormat("HH:mm:ss").parse(maxAntrianExpress.toString());
            @SuppressLint("SimpleDateFormat") Date maxStandard = new SimpleDateFormat("HH:mm:ss").parse(maxAntrianStandard.toString());
            @SuppressLint("SimpleDateFormat") Date totalAntrian = new SimpleDateFormat("HH:mm:ss").parse(totalLamaLayanan.toString());
            int hplus = Integer.parseInt(totalLayanan.substring(0, 2));
            //after is >
            //before is <
            long express = maxExpress.getTime();
            long standard = maxStandard.getTime();
            long totalWaktu = totalAntrian.getTime();
            if (isHplusPartKosong) {
                jenisAntrian = "H+";
            } else {
                if (totalWaktu <= express) {
                    jenisAntrian = "EXPRESS";
                } else if (totalWaktu <= standard) {
                    jenisAntrian = "STANDART";
                } else if (hplus > 0) {
                    jenisAntrian = "H+";
                    isHplusLayanan = true;
                } else {
                    jenisAntrian = "EXTRA";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return jenisAntrian;
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.btn_jasaLain_checkin3:
                i = new Intent(getActivity(), JasaLain_Activity.class);
                if (jasaList.size() > 0) {
                    i.putExtra(JASA_LAIN, jasaList.toJson());
                }
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_sparePart_checkin3:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                startActivityForResult(i, REQUEST_CARI_PART);
                break;
            case R.id.btn_jasaLainBerkala_checkin3:
                i = new Intent(getActivity(), JasaLainBerkala_Activity.class);
                startActivityForResult(i, REQUEST_JASA_BERKALA);
                break;
            case R.id.btn_partBerkala_checkin3:
                i = new Intent(getActivity(), PartBerkala_Activity.class);
                startActivityForResult(i, REQUEST_PART_BERKALA);
                break;
            case R.id.btn_partExternal_checkin3:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_OTOMOTIVES, "");
                startActivityForResult(i, REQUEST_PART_EXTERNAL);
                break;
            case R.id.btn_lanjut_checkin3:
                // Check in Antrian (CHECKIN 4), layanan estimasi status (LAYANAN ESTIMASI ISCHECKED()), TUNGGU KONFIRMASI (TUNGGU KONFIRMASI ISCHECKED())
                if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {
                    setSelanjutnya("LAYANAN ESTIMASI", "");
                } else if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {
                    setSelanjutnya("TUNGGU KONFIRMASI", "");
                } else {
                    setSelanjutnya("CHECKIN 3", "");
                }
                break;
            case R.id.btn_batal_checkin3:
                //batal
                showInfo("Layanan Di Batalkan Pelanggan, Silahkan Isi Field Keterangan Tambahan");
                i = new Intent(getActivity(), Checkin4_Activity.class);
                i.putExtra(BATAL, "");
                startActivityForResult(i, REQUEST_BATAL);
                break;
        }
    }


    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i;
        Nson dataAccept;
        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_JASA_LAIN:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        Log.d(TAG, "JASA : " + dataAccept);
                        isOutsource = dataAccept.get("OUTSOURCE").asString();
                        Tools.TimePart waktu = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu);
                        totalWaktuLayanan(timePartsList);
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));
                        jasaList.add(dataAccept);
                        Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_JASA_BERKALA:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        jasaList.add(dataAccept);
                        Tools.TimePart waktu1 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu1);
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                        Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_CARI_PART:
                        flagPartWajib = false;
                        i = new Intent(getActivity(), JumlahPart_HargaPart_Activity.class);
                        i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
                        i.putExtra("bengkel", "");
                        //Log.d("JUMLAH_HARGA_PART", "INTENT : "   + Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                        startActivityForResult(i, REQUEST_HARGA_PART);
                        break;
                    case REQUEST_PART_BERKALA:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        Tools.TimePart waktu2 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu2);
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_PART_EXTERNAL:
                        i = new Intent(getActivity(), JasaExternal_Activity.class);
                        i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
                        startActivityForResult(i, REQUEST_JASA_EXTERNAL);
                        break;
                    case REQUEST_JASA_EXTERNAL:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        isJasaExternal = true;
                        Tools.TimePart waktu3 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu3);
                        totalWaktuLayanan(timePartsList);
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_HARGA_PART:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        try {
                            totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                            totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));
                            sisaDp = totalHarga - calculateDp(Double.parseDouble(getSetting("DP_PERSEN")), totalHarga);
                        } catch (Exception e) {
                            totalHarga = 0;
                        }
                        if (flagPartWajib) {
                            isDelete = true;
                            jumlahPartWajib--;
                            if (jumlahPartWajib == 0) {
                                alertDialog.dismiss();
                            }
                        } else {
                            if (dataAccept.get("PART_KOSONG") != null && dataAccept.get("PART_KOSONG").asString().equals("true")) {
                                Nson partKosong = Nson.readJson(getIntentStringExtra(data, "PART_KOSONG_LIST"));
                                partKosong = partKosong.get("PART_KOSONG");
                                partKosongList.add(partKosong);
                                Log.d(TAG, "PARTKOSONG : " + partKosongList);
                                isPartKosong = true;
                                try {
                                    setDpAndSisa();
                                    dataAccept.remove("PART_KOSONG");
                                } catch (Exception e) {
                                    showError("PART KOSONG " + e.getMessage());
                                }
                            }
                            Tools.TimePart waktu4 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                            timePartsList.add(waktu4);
                            totalWaktuLayanan(timePartsList);
                            if (isHplusLayanan) {
                                setDpAndSisa();
                            }
                        }
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_CHECKIN:
                        setResult(RESULT_OK);
                        finish();
                        break;
                    case REQUEST_BATAL:
                        isBatal = true;
                        setSelanjutnya("BATAL CHECKIN", getIntentStringExtra(data, "alasanBatal"));
                        break;
                    case REQUEST_LAYANAN:
                        setSpNamaLayanan();
                        break;
                }

                find(R.id.et_totalBiaya_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalHarga)));
                Log.d(TAG, "Timepartlist : " + timePartsList);
            }
        } catch (Exception e) {
            showError(e.getMessage());
            Log.d(TAG, "onActivityResult: " + e.getMessage() + e.getCause());
        }
        Log.d(TAG, "PART : " + partList + "\n" + "JASA : " + jasaList);
    }

    @SuppressLint("SetTextI18n")
    private void totalWaktuLayanan(List<Tools.TimePart> timePartsList) {
        for (Tools.TimePart timePart : timePartsList) {
            dummyTime = dummyTime.add(timePart);
            find(R.id.tv_waktu_layanan, TextView.class).setText("Total Waktu Layanan : " + dummyTime.toString());
            Log.d(TAG, "TOTAL WAKTU : " + dummyTime);
        }
        find(R.id.tv_jenis_antrian, TextView.class).setText(validasiAntrian(isPartKosong));
    }


    private double calculateDp(double dp, int harga) {
        if (dp > 0 && harga > 0) {
            return  (dp / 100) * harga;
        }
        return 0;
    }

    @SuppressLint("SetTextI18n")
    private void setDpAndSisa() {
        find(R.id.tv_dp_bengkel, TextView.class).setVisibility(View.VISIBLE);
        find(R.id.tv_dp_bengkel, TextView.class).setText("DP Bengkel : " + getSetting("DP_PERSEN") + " %");
        find(R.id.et_sisa_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(sisaDp)));
        find(R.id.et_dp_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(calculateDp(Double.parseDouble(getSetting("DP_PERSEN")), totalHarga))));
    }
}
