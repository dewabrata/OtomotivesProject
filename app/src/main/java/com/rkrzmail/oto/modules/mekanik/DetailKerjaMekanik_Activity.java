package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.rkrzmail.utils.APIUrls.ATUR_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.RP;

public class DetailKerjaMekanik_Activity extends AppActivity {

    private EditText etNoAntrian, etJenis, etLayanan, etNopol, etNoKunci, etNamaPelanggan, etMulai, etSelesai, etCatatanMekanik;
    private RecyclerView rvPart, rvJasa, rvPointLayanan, rvKeluhan;
    private ImageView imgStart, imgPause, imgStop, imgRestart;
    private TextView tvTimer;

    private String timer, cid, idCheckin, idAturPerintah, mekanik, catatanMekanik;
    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    private long timerWork = 0;
    private int Seconds, Minutes, MilliSeconds;
    private Nson partList = Nson.newArray(),
            jasaList = Nson.newArray(),
            keluhanList = Nson.newArray(),
            pointLayananList = Nson.newArray();

    private Handler handler;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kerja_mekanik);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Perintah Kerja Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbarKeluhan(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Keluhan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initToolbarPointLayanan(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Point Layanan");
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
        rvJasa = findViewById(R.id.recyclerView_jasa);
        rvPart = findViewById(R.id.recyclerView_part);
        imgStart = findViewById(R.id.imgStart);
        imgPause = findViewById(R.id.imgPause);
        imgRestart = findViewById(R.id.imgRestart);
        imgStop = findViewById(R.id.imgStop);
        etCatatanMekanik = findViewById(R.id.et_catatan_mekanik);
        tvTimer = findViewById(R.id.tv_timer);

        imgPause.setEnabled(false);
        imgRestart.setEnabled(false);
        imgStop.setEnabled(false);
        loadData();
        initRecyclerviewJasa();
        initRecyclerviewParts();
        initListener();
    }

    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 1000);
            handler.postDelayed(this, 0);
        }

    };

    private void startWork() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("aktivitas", "START");
                args.put("id", idCheckin);
                args.put("mekanik", mekanik);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    idAturPerintah = String.valueOf(result.get("data").get(0));
                } else {
                    showWarning(result.get("message").asString());
                }
            }

            @Override
            public void runUI() {
                new CountDownTimer(timerWork, 0) {
                    @Override
                    public void onTick(long l) {
                        @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(timerWork), TimeUnit.MILLISECONDS.toMinutes(timerWork));
                        tvTimer.setText(result);
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();
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
                args.put("iddetail", idAturPerintah);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showWarning(result.get("message").asString());
                }
            }

            @Override
            public void runUI() {

            }

        });
    }

    private void stopWork() {
        catatanMekanik = etCatatanMekanik.getText().toString().toUpperCase();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("aktivitas", "DONE");
                args.put("id", idCheckin);
                args.put("iddetail", idAturPerintah);
                args.put("catatan", catatanMekanik);


                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
                if (result.get("status").asString().equalsIgnoreCase("OK")) {


                } else {
                    showWarning(result.get("message").asString());
                }
            }

            @Override
            public void runUI() {

            }

        });
    }

    private void restartWork() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("aktivitas", "RESTART");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PERINTAH_KERJA_MEKANIK), args));
                if (result.get("status").asString().equalsIgnoreCase("OK")) {


                } else {
                    showWarning(result.get("message").asString());
                }
            }

            @Override
            public void runUI() {

            }

        });
    }

    private void initListener() {
        find(R.id.btn_lanjut_kerjaMekanik).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelanjutnya();
            }
        });

        find(R.id.btn_keluhan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initKeluhanDialog();
            }
        });
        find(R.id.btn_point_layanan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPointLayananDialog();
            }
        });

        handler = new Handler();

        imgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);

                imgStart.setEnabled(false);
                imgPause.setEnabled(true);
                imgStop.setEnabled(true);
                imgRestart.setEnabled(true);

                startWork();

            }
        });

        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);
                imgStart.setEnabled(true);
                imgPause.setEnabled(false);
                imgStop.setEnabled(true);
                imgRestart.setEnabled(true);
                pauseWork();

            }
        });

        imgRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                showInfo("Waktu Kerja di Ulang");
                imgStart.setEnabled(true);
                imgPause.setEnabled(false);
                imgStop.setEnabled(false);
                imgRestart.setEnabled(false);
                restartWork();

            }
        });

        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer = String.valueOf(Seconds);
                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                showInfo("Waktu Kerja Anda" + Minutes + "Menit");
                imgStart.setEnabled(true);
                imgPause.setEnabled(false);
                imgStop.setEnabled(false);
                imgRestart.setEnabled(false);

                stopWork();

            }
        });
    }

    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra(DATA));
        if (n.get("JAM_HOME").asString().isEmpty() || n.get("JAM_HOME") == null)
            find(R.id.et_jam_home).setVisibility(View.GONE);
        if (n.get("ALAMAT").asString().isEmpty() || n.get("ALAMAT") == null)
            find(R.id.et_alamat).setVisibility(View.GONE);
        if (n.get("NO_KUNCI").asString().isEmpty() || n.get("NO_KUNCI") == null)
            etNoKunci.setVisibility(View.GONE);
        if (n.get("PENGAMBILAN").asString().isEmpty() || n.get("PENGAMBILAN") == null)
            find(R.id.et_pengambilan).setVisibility(View.GONE);

        etNoAntrian.setText(n.get("NO_ANTRIAN").asString());
        etNopol.setText(n.get("NOPOL").asString());
        etNoKunci.setText(n.get("NO_KUNCI").asString());
        etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
        etMulai.setText(n.get("ESTIMASI_SEBELUM").asString());
        etSelesai.setText(n.get("ESTIMASI_SESUDAH").asString());
        etJenis.setText(n.get("JENIS_KENDARAAN").asString());
        etLayanan.setText(n.get("LAYANAN").asString());
        idCheckin = n.get(ID).asString();
        mekanik = n.get("MEKANIK").asString();

        viewLayananPartJasa();
        setTimer();
    }

    @SuppressLint("SetTextI18n")
    private void setTimer() {
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


    private void initRecyclerviewParts() {
        rvPart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("MERK").asString());

            }
        });
    }

    private void initRecyclerviewJasa() {
        rvJasa.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJasa.setHasFixedSize(true);
        rvJasa.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText(RP + formatRp(jasaList.get(position).get("HARGA_JASA").asString()));
            }
        });
    }

    private void initRecylerviewKeluhan(View dialogView) {
        rvKeluhan = dialogView.findViewById(R.id.recyclerView);
        rvKeluhan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvKeluhan.setHasFixedSize(true);
        rvKeluhan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
            }
        });

    }

    private void initRecylerviewPointLayanan(View dialogView) {
        rvPointLayanan = dialogView.findViewById(R.id.recyclerView);
        rvPointLayanan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPointLayanan.setHasFixedSize(true);
        rvPointLayanan.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_keluhan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);

            }
        });
    }

    private void initKeluhanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        initToolbarKeluhan(dialogView);
        initRecylerviewKeluhan(dialogView);
        viewKeluhan();

        builder.create();
        alertDialog = builder.show();
    }

    private void initPointLayananDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        initToolbarPointLayanan(dialogView);
        initRecylerviewPointLayanan(dialogView);

        builder.create();
        alertDialog = builder.show();
    }

    @SuppressLint("NewApi")
    private void viewKeluhan(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    keluhanList.asArray().clear();
                    keluhanList.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvKeluhan.getAdapter()).notifyDataSetChanged();
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
                args.put("detail", "TRUE");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (!result.get(i).get("NAMA_PART").asString().isEmpty()) {
                            partList.add(result.get(i));
                        }
                        if (!result.get(i).get("KELOMPOK_PART").asString().isEmpty()) {
                            jasaList.add(result.get(i));
                        }
                    }
                    Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(rvJasa.getAdapter()).notifyDataSetChanged();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }


    private void setSelanjutnya() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewinspeksi"), args));
                args.put("check", "check");
                args.put("id", idCheckin);
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.get(0).get("CHECKIN_DETAIL").asInteger() != 0) {
                        Intent intent = new Intent(getActivity(), InspectionFinal_Activity.class);
                        intent.putExtra("data", idCheckin);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), Pembayaran_Activity.class);
                        intent.putExtra("data", idCheckin);
                        startActivity(intent);
                    }
                } else {
                    showInfo("Gagal");
                }
            }
        });
    }

}