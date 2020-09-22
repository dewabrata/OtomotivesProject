package com.rkrzmail.oto.modules.primary.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
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
import com.rkrzmail.oto.modules.layanan.AturLayanan_Activity;
import com.rkrzmail.oto.modules.primary.KontrolLayanan_Activity;
import com.rkrzmail.oto.modules.primary.booking.Booking3_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.JumlahHargaPart_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.PartBerkala_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.rkrzmail.oto.modules.primary.booking.Booking3_Activity.REQUEST_BATAL;

public class Checkin3_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_LAYANAN = 11;
    private RecyclerView rvPart, rvJasaLain, rvPartWajib;
    public static final String TAG = "Checkin3___";
    private Spinner spLayanan;
    private LinearLayout viewGroup;
    private AlertDialog alertDialog;
    private Nson
            layananArray = Nson.newArray(),
            dataLayananList = Nson.newArray(),
            partList = Nson.newArray(),
            jasaList = Nson.newArray(),
            dataAccept,
            lokasiLayananList = Nson.newArray(),
            partWajibList = Nson.newArray(),
            jasaGaransiList = Nson.newArray(),
            daftarPartDummy = Nson.newArray(),
            partBengkelList = Nson.newArray();
    private String biayaLayanan, namaLayanan;
    private int countClickPartWajib = 0, totalHarga = 0, totalPartJasa = 0, batasanKm = 0, batasanBulan = 0;
    private List<Tools.TimePart> timePartsList = new ArrayList<>();
    private Tools.TimePart dummyTime = Tools.TimePart.parse("00:00:00");
    private boolean flagPartWajib = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin3_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbarPartWajib(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Part Wajib Layanan");
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
        viewGroup = findViewById(R.id.parent_ly_checkin3);
        rvPart = findViewById(R.id.recyclerView_part_checkin3);
        rvJasaLain = findViewById(R.id.recyclerView_jasalain_checkin3);

        initPartBengkel();
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
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                try {
                    if (Tools.isNumeric(partList.get(position).get("HARGA_PART").asString())) {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                                .setText("Rp. " + formatRp(partList.get(position).get("HARGA_PART").asString()));
                    } else {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                                .setText("");
                    }
                } catch (Exception e) {
                    showError(e.getMessage());
                }
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText("Rp. " + formatRp(partList.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setText(partList.get(position).get("MERK").asString());
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

    private void initRecylerviewPartWajib(View dialogView) {
        rvPartWajib = dialogView.findViewById(R.id.recyclerView);
        rvPartWajib.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPartWajib.setHasFixedSize(true);
        rvPartWajib.setAdapter(new NikitaRecyclerAdapter(partWajibList, R.layout.item_part_wajib_layanan) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        if (partWajibList.get(position).get("MASTER_PART").asArray().size() > 0) {
                            viewHolder.find(R.id.tv_stockPart, TextView.class).setVisibility(View.GONE);
                            viewHolder.find(R.id.tv_nama_master_part, TextView.class).setText(partWajibList.get(position).get("NAMA_MASTER_PART").asString());
                            partWajibList = partWajibList.get(0).get("MASTER_PART");
                            viewHolder.find(R.id.tv_merkPart, TextView.class).setText(partWajibList.get("MERK").asString());
                            viewHolder.find(R.id.tv_namaPart, TextView.class).setText(partWajibList.get("NAMA_PART").asString());
                            viewHolder.find(R.id.tv_noPart, TextView.class).setText(partWajibList.get("NO_PART").asString());
                            return;
                        }

                        viewHolder.find(R.id.cardView_master_part, CardView.class).setVisibility(View.GONE);
                        viewHolder.find(R.id.tv_namaPart, TextView.class).setText(partWajibList.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_noPart, TextView.class).setText(partWajibList.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_stockPart, TextView.class).setText(partWajibList.get(position).get("STOCK").asString());
                        viewHolder.find(R.id.tv_merkPart, TextView.class).setText(partWajibList.get(position).get("MERK_PART").asString());

                    }

                    @Override
                    public int getItemCount() {
                        countClickPartWajib = partWajibList.size();
                        return countClickPartWajib;
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        daftarPartDummy.add(parent.get(position).get("PART_ID").asString());
                        Intent i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                        i.putExtra("partWajib", parent.get(position).toJson());
                        startActivityForResult(i, Booking3_Activity.REQUEST_HARGA_PART);
                    }
                })
        );
        rvPartWajib.getAdapter().notifyDataSetChanged();
    }

    private void setSelanjutnya(final String status, final String alasanBatal) {
        final Nson nson = Nson.readJson(getIntentStringExtra("data"));
        final String layanan = spLayanan.getSelectedItem().toString();
        final String layananEstimasi = find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked() ? "Y" : "N";
        final String total = find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String dp = find(R.id.et_dp_checkin3, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String sisa = find(R.id.et_sisa_checkin3, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
        final String tungguKonfirmasi = find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).isChecked() ? "Y" : "N";
        final int totalPartJasa = jasaList.size() + partList.size();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("check", "1");
                args.put("id", nson.get("id").asString());
                args.put("alasanBatal", alasanBatal);
                args.put("layanan", layanan);
                args.put("layananestimasi", layananEstimasi);
                args.put("total", total);
                args.put("dp", dp);
                args.put("sisa", sisa);
                args.put("tunggu", tungguKonfirmasi);
                args.put("status", status);
                args.put("partbook", partList.toJson());
                args.put("jasabook", jasaList.toJson());
                args.put("totalpartjasa", String.valueOf(totalPartJasa));

                Log.d(TAG, "PartBook : " + partList);
                Log.d(TAG, "JasaBook : " + jasaList);
                Log.d(TAG, "Params : " + args.toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("checkin"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (status.equalsIgnoreCase("LAYANAN ESTIMASI")) {
                        showSuccess("Layanan Di tambahkan Ke Daftar Kontrol Layanan");
                        setResult(RESULT_OK);
                        finish();
                    } else if (status.equalsIgnoreCase("KONFIRMASI BIAYA")) {
                        showSuccess("Layanan Di tambahkan Ke Daftar Kontrol Layanan");
                        setResult(RESULT_OK);
                        finish();
                    } else if (status.equalsIgnoreCase("BATAL CHECKIN 3")) {
                        if (!alasanBatal.equals("")) {
                            showSuccess("Layanan Di Batalkan Pelanggan, Data Di Masukkan Ke Kontrol Layanan");
                            setResult(RESULT_OK);
                            finish();
                            return;
                        }
                        showInfo("Layanan Di Batalkan Pelanggan, Silahkan Isi Field Keterangan Tambahan");
                        Intent i = new Intent(getActivity(), Checkin4_Activity.class);
                        i.putExtra("BATAL", "");
                        startActivityForResult(i, REQUEST_BATAL);
                    } else {
                        nson.set("total", find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString());
                        nson.set("waktu", dummyTime.toString());
                        nson.set("lokasiLayanan", lokasiLayananList.toJson());

                        Intent i = new Intent(getActivity(), Checkin4_Activity.class);
                        i.putExtra("data", nson.toJson());
                        startActivityForResult(i, KontrolLayanan_Activity.REQUEST_CHECKIN);
                    }
                } else {
                    showWarning("Gagal");
                }
            }
        });
    }

    private void initPartBengkel() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        partBengkelList.add(result.get(i).get("PART_ID")).asString();
                    }
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
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        Log.d(TAG, "setSpNamaLayanan: " + data);
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("layanan", "CHECKIN");
                args.put("spec", "Bengkel");
                args.put("status", "AKTIF");
                args.put("kendaraan", data.get("kendaraan").asString());
                args.put("merk", data.get("merk").asString());
                args.put("model", data.get("model").asString());
                args.put("varian", data.get("varian").asString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    dataLayananList.asArray().addAll(result.asArray());
                    Log.d(TAG, "List Layanan Data : " + dataLayananList);
                    /*if (result.asArray().size() == 0) {
                        showInfo("Layanan Belum Tercatatkan");
                        showInfoDialog("Tambah Layanan ? ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturLayanan_Activity.class), REQUEST_LAYANAN);
                            }
                        });
                        return;
                    }*/
                    layananArray.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        layananArray.add(result.get(i).get("NAMA_LAYANAN").asString());
                    }

                    Log.d(TAG, "List Nama Layanan : " + layananArray);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, layananArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLayanan.setAdapter(spinnerAdapter);
                } else {
                    showInfo("Nama Layanan Gagal Di Muat");
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
                    if (dataLayananList.get(i).get("NAMA_LAYANAN").asString().equalsIgnoreCase(item)) {
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
                Log.d(TAG, "PART_WAJIB : " + partWajibList);
                if (partWajibList.size() > 0) {
                    initPartWajibLayanan();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initPartWajibLayanan() {
        if (partWajibList.size() > 0) {
            flagPartWajib = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
            builder.setView(dialogView);
            builder.setCancelable(true);

            initToolbarPartWajib(dialogView);
            initRecylerviewPartWajib(dialogView);
            //builder.setCancelable(false);
            builder.create();
            alertDialog = builder.show();
        } else {
            showError("PART WAJIB NULL");
        }
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.btn_jasaLain_checkin3:
                i = new Intent(getActivity(), JasaLain_Activity.class);
                startActivityForResult(i, Booking3_Activity.REQUEST_JASA_LAIN);
                break;
            case R.id.btn_sparePart_checkin3:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("bengkel", "");
                startActivityForResult(i, Booking3_Activity.REQUEST_PART);
                break;
            case R.id.btn_jasaLainBerkala_checkin3:
                i = new Intent(getActivity(), JasaLainBerkala_Activity.class);
                startActivityForResult(i, Booking3_Activity.REQUEST_JASA_BERKALA);
                break;
            case R.id.btn_partBerkala_checkin3:
                i = new Intent(getActivity(), PartBerkala_Activity.class);
                startActivityForResult(i, Booking3_Activity.REQUEST_PART_BERKALA);
                break;
            case R.id.btn_partExternal_checkin3:
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("global", "");
                startActivityForResult(i, Booking3_Activity.REQUEST_PART_EXTERNAL);
                break;
            case R.id.btn_lanjut_checkin3:
                // Check in Antrian (CHECKIN 4), layanan estimasi status (LAYANAN ESTIMASI ISCHECKED()), Konfirmasi Biaya (KONFIRMASI BIAYA ISCHECKED())
                if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {
                    setSelanjutnya("LAYANAN ESTIMASI", "");
                } else if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {
                    setSelanjutnya("KONFIRMASI BIAYA", "");
                } else {
                    setSelanjutnya("CHECKIN 3", "");
                }
                break;
            case R.id.btn_batal_checkin3:
                //batal
                setSelanjutnya("BATAL CHECKIN 3", "");
                break;
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i;
        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case Booking3_Activity.REQUEST_JASA_LAIN:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, "data"));
                        Tools.TimePart waktu = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu);
                        totalHarga += Integer.parseInt(dataAccept.get("HARGA_JASA").asString().replaceAll("[^0-9]+", ""));
                        jasaList.add(dataAccept);
                        rvJasaLain.getAdapter().notifyDataSetChanged();
                        break;
                    case Booking3_Activity.REQUEST_JASA_BERKALA:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, "data"));
                        jasaList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                        Tools.TimePart waktu1 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu1);
                        totalHarga += Integer.parseInt(dataAccept.get("HARGA_PART").asString().replaceAll("[^0-9]+", ""));
                        rvJasaLain.getAdapter().notifyDataSetChanged();
                        break;
                    case Booking3_Activity.REQUEST_PART:
                        i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                        i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                        i.putExtra("bengkel", "");
                        //Log.d("JUMLAH_HARGA_PART", "INTENT : "   + Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                        startActivityForResult(i, Booking3_Activity.REQUEST_HARGA_PART);
                        break;
                    case Booking3_Activity.REQUEST_PART_BERKALA:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, "data"));
                        Tools.TimePart waktu2 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu2);
                        totalHarga += Integer.parseInt(dataAccept.get("HARGA_PART").asString().replaceAll("[^0-9]+", ""));
                        partList.add(dataAccept);
                        rvPart.getAdapter().notifyDataSetChanged();
                        break;
                    case Booking3_Activity.REQUEST_PART_EXTERNAL:
                        i = new Intent(getActivity(), JasaExternal_Activity.class);
                        i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                        startActivityForResult(i, Booking3_Activity.REQUEST_JASA_EXTERNAL);
                        break;
                    case Booking3_Activity.REQUEST_JASA_EXTERNAL:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, "data"));
                        Tools.TimePart waktu3 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu3);
                        totalHarga += Integer.parseInt(dataAccept.get("HARGA_JASA").asString().replaceAll("[^0-9]+", ""));
                        partList.add(dataAccept);
                        rvPart.getAdapter().notifyDataSetChanged();
                        break;
                    case Booking3_Activity.REQUEST_HARGA_PART:
                        if (flagPartWajib) {
                            for (int j = 0; j < partWajibList.size(); j++) {
                                if (partWajibList.get(j).get("NAMA_PART").asString().contains(daftarPartDummy.get(j).get("NAMA_PART").asString())) {
                                    partWajibList.asArray().remove(j);
                                    rvPartWajib.getAdapter().notifyDataSetChanged();
                                    break;
                                }
                            }
                            countClickPartWajib--;
                            if (countClickPartWajib == 0) {
                                alertDialog.dismiss();
                            }
                        }
                        dataAccept = Nson.readJson(getIntentStringExtra(data, "data"));
                        Tools.TimePart waktu4 = Tools.TimePart.parse(dataAccept.get("WAKTU").asString());
                        timePartsList.add(waktu4);
                        totalHarga += Integer.parseInt(dataAccept.get("HARGA_PART").asString().replaceAll("[^0-9]+", ""));
                        totalHarga += Integer.parseInt(dataAccept.get("HARGA_JASA").asString().replaceAll("[^0-9]+", ""));
                        partList.add(dataAccept);
                        rvPart.getAdapter().notifyDataSetChanged();
                        break;
                    case KontrolLayanan_Activity.REQUEST_CHECKIN:
                        setResult(RESULT_OK);
                        finish();
                        break;
                    case REQUEST_BATAL:
                        setSelanjutnya("BATAL CHECKIN 3", Nson.readJson(getIntentStringExtra("alasanBatal")).asString());
                        break;
                    case REQUEST_LAYANAN:
                        setSpNamaLayanan();
                        break;
                }

                if (totalHarga > 0) {
                    String total = String.valueOf(totalHarga);
                    find(R.id.et_totalBiaya_checkin3, EditText.class).setText("Rp." + formatRp(total));
                }
                Log.d(TAG, "Timepartlist : " + timePartsList);
                for (Tools.TimePart waktu : timePartsList) {
                    dummyTime = dummyTime.add(waktu);
                }
            }
        } catch (Exception e) {
            showError(e.getMessage());
            Log.d(TAG, "onActivityResult: " + e.getMessage());
        }
        Log.d(TAG, "PART : " + partList + "\n" + "JASA : " + jasaList);
    }
}
