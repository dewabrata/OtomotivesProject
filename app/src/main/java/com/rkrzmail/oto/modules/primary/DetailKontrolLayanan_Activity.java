package com.rkrzmail.oto.modules.primary;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.user.AturUser_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.RP;

public class DetailKontrolLayanan_Activity extends AppActivity {

    private static final String TAG = "DetailKontrol__";
    private EditText etNoAntrian, etStatus, etNopol, etNoKunci, etNamaPelanggan, etNamaLayanan, etTotal,
            etDp, etSisa, etEstimasiSebelum, etEstimasiLama, etEstimasiSelesai, etPengambilan, etAlasanBatal;
    private RecyclerView rvItem;
    private Spinner spAktifitas, spNamaMekanik;
    private Nson partList = Nson.newArray(),
            jasaList = Nson.newArray();
    private boolean isListRecylerview; // true = part, false = jasa
    private Nson mekanikArray = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontrol_layanan_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kontrol Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNoAntrian = findViewById(R.id.et_noAntrian);
        etStatus = findViewById(R.id.et_status);
        etNopol = findViewById(R.id.et_nopol);
        etNoKunci = findViewById(R.id.et_noKunci);
        etNamaLayanan = findViewById(R.id.et_namaLayanan);
        etNamaPelanggan = findViewById(R.id.et_namaP);
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
        rvItem = findViewById(R.id.recyclerView_detailKontrolLayanan);

        loadData();
        initRecyclerview();

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        Log.d(TAG, "loadData: " + data);
        setSpMekanik(data.get("MEKANIK").asString());
        setSpAktifitas();
        etNoAntrian.setText(data.get("ANTRIAN").asString());
        etStatus.setText(data.get("STATUS").asString());
        etNopol.setText(formatNopol(data.get("NOPOL").asString()));
        etNoKunci.setText(data.get("").asString());
        etNamaLayanan.setText(data.get("LAYANAN").asString());
        etNamaPelanggan.setText(data.get("NAMA_PELANGGAN").asString());
        etTotal.setText(RP + formatRp(data.get("TOTAL_BIAYA").asString()));
        etDp.setText(RP + formatRp(data.get("DP").asString()));
        etSisa.setText(RP + formatRp(data.get("SISA").asString()));
        etEstimasiSebelum.setText(data.get("ESTIMASI_SEBELUM").asString());
        etEstimasiLama.setText(Tools.setFormatDateTimeFromDb(data.get("ESTIMASI_SELESAI").asString(), "dd/MM-hh:mm"));
        etEstimasiSelesai.setText(data.get("ESTIMASI_SESUDAH").asString());
        etPengambilan.setText(data.get("JAM_PENGAMBILAN").asString());
        etAlasanBatal.setText(data.get("ALASAN_BATAL").asString());
        if (data.get("TUNGGU_KONFIRMASI_BIAYA").asString().equals("Y")) {
            find(R.id.cb_tungguConfirm_biaya, CheckBox.class).setChecked(true);
        }
        if (data.get("PELANGGAN_TIDAK_MENUNGGU").asString().equals("Y")) {
            find(R.id.cb_tidakMenunggu, CheckBox.class).setChecked(true);
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
    }

    private void initRecyclerview() {
        rvItem.setLayoutManager(new LinearLayoutManager(this));
        rvItem.setHasFixedSize(true);
        rvItem.setAdapter(new NikitaRecyclerAdapter(isListRecylerview ? partList : jasaList, R.layout.item_part_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(isListRecylerview ? R.id.tv_namaPart_booking3_checkin3 : R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(isListRecylerview ? nListArray.get(position).get("NAMA_PART").asString() : nListArray.get(position).get("NAMA_KELOMPOK_PART").asString());

                viewHolder.find(isListRecylerview ? R.id.tv_noPart_booking3_checkin3 : R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(isListRecylerview ? nListArray.get(position).get("NO_PART").asString() : nListArray.get(position).get("AKTIVITAS").asString());

                viewHolder.find(isListRecylerview ? R.id.tv_hargaNet_booking3_checkin3 : R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText(nListArray.get(position).get("HARGA_NET").asString());

                if (isListRecylerview) {
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("BIAYA_JASA").asString());
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
    }

    private void setSpAktifitas(){
        List<String> aktifitasList = new ArrayList<>();
        aktifitasList.add("--PILIH--");
        aktifitasList.add("BATAL BENGKEL");
        aktifitasList.add("BATAL PART");
        aktifitasList.add("BATAL PELANGGAN");
        aktifitasList.add("GANTI MEKANIK");
        aktifitasList.add("KONFIRMASI BIAYA");
        aktifitasList.add("PENUGASAN MEKANIK");
        aktifitasList.add("PERINTAH ANTAR");
        aktifitasList.add("TAMBAH MEKANIK");
        aktifitasList.add("TAMBAH PART-JASA");
        aktifitasList.add("TAMBAH PART-JASA MSG");
        aktifitasList.add("TAMBAH PART-JASA OK");
        aktifitasList.add("TAMBAH PART-JASA TOLAK");

        ArrayAdapter<String> aktifitasAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, aktifitasList);
        aktifitasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAktifitas.setAdapter(aktifitasAdapter);
    }

    private void updateData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
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

}
