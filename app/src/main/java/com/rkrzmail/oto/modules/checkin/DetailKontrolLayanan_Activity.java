package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ESTIMASI_WAKTU;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.REQUEST_TAMBAH_PART_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.TAMBAH;
import static com.rkrzmail.utils.ConstUtils.TAMBAH_PART;
import static com.rkrzmail.utils.ConstUtils.TIDAK_MENUNGGU;
import static com.rkrzmail.utils.ConstUtils.TOTAL_BIAYA;

public class DetailKontrolLayanan_Activity extends AppActivity {

    private static final String TAG = "DetailKontrol__";

    private EditText etNoAntrian, etStatus, etNopol, etNoKunci, etNamaPelanggan, etTotal,
            etDp, etSisa, etEstimasiSebelum, etEstimasiLama, etEstimasiSelesai, etPengambilan, etAlasanBatal, etNamaLayanan;
    private RecyclerView rvLayananParts, rvLayananJasa;
    private Spinner spAktifitas, spNamaMekanik;
    private TextView tvNamaLayanan, tvBiayaLayanan;

    private Nson mekanikArray = Nson.newArray();
    private Nson detailCheckinListParts = Nson.newArray(), detailCheckinListJasa = Nson.newArray();

    private boolean isMekanik = false; // true = part, false = jasa
    private boolean isBatal = false;

    private String idCheckinDetail = "", idCheckin = "";
    private String status = "";
    private String namaMekanik = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontrol_layanan_);
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
        initToolbar();

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
        etAlasanBatal = findViewById(R.id.et_ket_batal);
        etPengambilan = findViewById(R.id.et_pengambilan);
        spAktifitas = findViewById(R.id.sp_aktifitas);
        spNamaMekanik = findViewById(R.id.sp_nama_mekanik);
        rvLayananParts = findViewById(R.id.recyclerView_detail_part);
        rvLayananJasa = findViewById(R.id.recyclerView_detail_jasa);
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan);
        tvBiayaLayanan = findViewById(R.id.tv_biaya_layanan);

        loadData();
        initRecyclerviewParts();
        initRecyclerviewJasa();
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), false);
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        Log.d(TAG, "loadData: " + data);

        setSpMekanik(data.get("MEKANIK").asString());
        setSpAktifitas();
        getDetailCheckin(data);
        idCheckinDetail = data.get(ID).asString();

        etNoAntrian.setText(data.get("NO_ANTRIAN").asString());
        etStatus.setText(data.get("STATUS").asString());
        etNopol.setText(formatNopol(data.get("NOPOL").asString()));
        etNoKunci.setText(data.get("").asString());
        etNamaPelanggan.setText(data.get("NAMA_PELANGGAN").asString());
        etTotal.setText(RP + formatRp(data.get("TOTAL_BIAYA").asString()));
        etDp.setText(RP + formatRp(data.get("DP").asString()));
        etSisa.setText(RP + formatRp(data.get("SISA").asString()));
        etEstimasiSebelum.setText(data.get("ESTIMASI_SEBELUM").asString());
        etEstimasiLama.setText(Tools.setFormatDateTimeFromDb(data.get("ESTIMASI_SELESAI").asString(), "yyyy-MM-dd hh:mm", "dd/MM-hh:mm", false));
        etEstimasiSelesai.setText(data.get("ESTIMASI_SESUDAH").asString());
        etPengambilan.setText(data.get("JAM_PENGAMBILAN").asString());
        etAlasanBatal.setText(data.get("ALASAN_BATAL").asString());
        etNamaLayanan.setText(data.get("LAYANAN").asString());
        tvNamaLayanan.setText(data.get("LAYANAN").asString());
        tvBiayaLayanan.setText(RP + formatRp(formatOnlyNumber(data.get("BIAYA_LAYANAN").asString())));

        if (data.get("TUNGGU_KONFIRMASI_BIAYA").asString().equals("Y")) {
            find(R.id.cb_tungguConfirm_biaya, CheckBox.class).setChecked(true);
        }
        if (data.get("PELANGGAN_TIDAK_MENUNGGU").asString().equals("Y")) {
            find(R.id.cb_tidak_menunggu, CheckBox.class).setChecked(true);
        }
        if (data.get("KONFIRMASI_TAMBAHAN").asString().equals("Y")) {
            find(R.id.cb_konfirm_tambah, CheckBox.class).setChecked(true);
        }
        if (data.get("BUANG_PART").asString().equals("Y")) {
            find(R.id.cb_buangPart, CheckBox.class).setChecked(true);
        }
        if (data.get("ANTAR_JEMPUT").asString().equals("Y")) {
            find(R.id.cb_antar, CheckBox.class).setChecked(true);
        }

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!status.isEmpty() && status.equals("BATAL PELANGGAN")) {
                    if (etAlasanBatal.getText().toString().isEmpty()) {
                        etAlasanBatal.setError("Keterangan Batal Harus di Isi");
                        return;
                    }
                }

                if (isMekanik && namaMekanik.equals("--PILIH--")) {
                    showWarning("Nama Mekanik Harus di Pilih");
                    return;
                }

                updateData(idCheckinDetail);
            }
        });
    }

    private void initRecyclerviewParts() {
        rvLayananParts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLayananParts.setHasFixedSize(true);
        rvLayananParts.setAdapter(new NikitaRecyclerAdapter(detailCheckinListParts, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(detailCheckinListParts.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(detailCheckinListParts.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(detailCheckinListParts.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(detailCheckinListParts.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(detailCheckinListParts.get(position).get("MERK").asString());
                if(isBatal){
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Aktifitas ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    detailCheckinListParts.asArray().remove(position);
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
                }else{
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                }
            }
        });
    }

    private void initRecyclerviewJasa() {
        rvLayananJasa.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLayananJasa.setHasFixedSize(true);
        rvLayananJasa.setAdapter(new NikitaRecyclerAdapter(detailCheckinListJasa, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(detailCheckinListJasa.get(position).get("KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(detailCheckinListJasa.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText(RP + formatRp(detailCheckinListJasa.get(position).get("HARGA_JASA").asString()));
                if(isBatal){
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Aktifitas ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    detailCheckinListParts.asArray().remove(position);
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
                }else{
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                }
            }
        });
    }

    private void getDetailCheckin(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("detail", "TRUE");
                args.put("id", id.get(ID).asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    idCheckin = result.get("CHECKIN_ID").asString();
                    for (int i = 0; i < result.size(); i++) {
                        if (!result.get(i).get("NAMA_PART").asString().isEmpty()) {
                            detailCheckinListParts.add(result.get(i));
                        }
                        if (!result.get(i).get("KELOMPOK_PART").asString().isEmpty()) {
                            detailCheckinListJasa.add(result.get(i));
                        }
                    }
                    rvLayananJasa.getAdapter().notifyDataSetChanged();
                    rvLayananParts.getAdapter().notifyDataSetChanged();
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
                    Log.d(TAG, "MEKANIK : " + mekanikArray);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mekanikArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spNamaMekanik.setAdapter(spinnerAdapter);
                    if (!mekanik.isEmpty()) {
                        for (int in = 0; in < spNamaMekanik.getCount(); in++) {
                            if (spNamaMekanik.getItemAtPosition(in).toString().contains(mekanik)) {
                                spNamaMekanik.setSelection(in);
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
        aktifitasList.add("KONFIRMASI BIAYA");
        aktifitasList.add("BATAL BENGKEL");
        aktifitasList.add("BATAL PART");
        aktifitasList.add("BATAL PELANGGAN");
        aktifitasList.add("GANTI MEKANIK");
        aktifitasList.add("PENUGASAN MEKANIK");
        aktifitasList.add("TAMBAH MEKANIK");
        aktifitasList.add("PERINTAH ANTAR");
        aktifitasList.add("TAMBAH PART - JASA");
        aktifitasList.add("TAMBAH PART - JASA MSG"); //perlu persetujuan
        aktifitasList.add("TAMBAH PART - JASA OK");
        aktifitasList.add("TAMBAH PART - JASA TOLAK");

        ArrayAdapter<String> aktifitasAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, aktifitasList);
        aktifitasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAktifitas.setAdapter(aktifitasAdapter);
        spAktifitas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                status = parent.getSelectedItem().toString();
                if (status.equals("PENUGASAN MEKANIK")
                        || status.equals("GANTI MEKANIK")
                        || status.equals("TAMBAH MEKANIK")) {
                    isMekanik = true;
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), true);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_mekanik, LinearLayout.class), false);
                }

                if (status.equals("MESSAGE PELANGGAN")
                        || status.equals("BATAL BENGKEL")
                        || status.equals("BATAL PELANGGAN")) {
                    etAlasanBatal.setEnabled(true);
                } else {
                    etAlasanBatal.setEnabled(false);
                }

                Intent intent = new Intent(getActivity(), TambahPartJasaDanBatal_Activity.class);
                intent.putExtra(ID, idCheckinDetail);
                intent.putExtra("CHECKIN_ID", idCheckin);
                intent.putExtra(TOTAL_BIAYA, etTotal.getText().toString().replaceAll("[^0-9]+", ""));

                if (status.equals("BATAL PART")) {
                    isBatal = true;
                    rvLayananParts.getAdapter().notifyDataSetChanged();
                    rvLayananJasa.getAdapter().notifyDataSetChanged();
                    /*intent.putExtra(BATAL_PART, "");
                    if(detailCheckinListJasa.size() > 0){
                        intent.putExtra(JASA_LAIN, detailCheckinListJasa.toJson());
                    }
                    if(detailCheckinListParts.size() > 0){
                        intent.putExtra(PARTS_UPPERCASE, detailCheckinListParts.toJson());
                    }
                    startActivityForResult(intent, REQUEST_TAMBAH_PART_JASA_LAIN);*/
                }else{
                    isBatal = false;
                    rvLayananParts.getAdapter().notifyDataSetChanged();
                    rvLayananJasa.getAdapter().notifyDataSetChanged();
                }

                if (status.equals("TAMBAH PART - JASA")) { //|| status.equals("TAMBAH PART - JASA MSG") || status.equals("TAMBAH PART - JASA OK")
                    intent.putExtra(TAMBAH_PART, "");
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

    private void updateData(final String id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("status", status);
                args.put("id", id);
                if (isMekanik) {
                    args.put("aktivitas", "MEKANIK");
                    args.put("mekanik", namaMekanik);
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
                    showInfo("Gagal");
                }
            }
        });
    }

    // if part not yet accepted they cannot be cancel
    private void viewSerahTerimaPart(){
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
            Nson nson = Nson.readJson(getIntentStringExtra(data, TAMBAH));
            getDetailCheckin(nson.get(ID));
        }
    }
}
