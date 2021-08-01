package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaMultipleViewAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_INSPEKSI;
import static com.rkrzmail.utils.APIUrls.VIEW_INSPEKSI;
import static com.rkrzmail.utils.APIUrls.VIEW_KELUHAN;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.Tools.removeDuplicates;

public class AturInspeksi_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvPointLayanan, rvKeluhanPerlengkapan, rvRekomendasi;
    private AlertDialog alertDialog;

    private final Nson keluhanList = Nson.newArray();
    private final Nson rekomendasiMekanikList = Nson.newArray();
    private final Nson fotoKondisiList = Nson.newArray();
    private final Nson perlengkapanList = Nson.newArray();

    private int countClick = 0;

    private String idMekanikKerja = "", idCheckin = "";
    private String mekanik = "", catatan = "", noPonsel = "";
    private String tidakMenunggu = "";
    private String nopol = "";

    private boolean isRework = false, isStart = false, isStop = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_inspeksi);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inspeksi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbarKeluhan(View dialogView, boolean isKeluhan) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(isKeluhan ? "Keluhan" : "Perlengkapan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initToolbarRekomendasi(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rekomendasi Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @SuppressLint("NewApi")
    private void initToolbarFoto(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Foto Kondisi");
    }


    private void initComponent() {
        rvPointLayanan = findViewById(R.id.recyclerView);
        initRecyclerviewPointLayanan();
        loadData();

        find(R.id.imgBtn_note).setOnClickListener(this);
        find(R.id.btn_keluhan).setOnClickListener(this);
        find(R.id.imgBtn_start).setOnClickListener(this);
        find(R.id.imgBtn_stop).setOnClickListener(this);
        find(R.id.btn_rekomendasi_mekanik).setOnClickListener(this);
        find(R.id.btn_foto_kondisi).setOnClickListener(this);
        find(R.id.btn_perlengkapan).setOnClickListener(this);
    }

    private void loadData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));

        find(R.id.et_catatan_mekanik, EditText.class).setText(nson.get("CATATAN_MEKANIK").asString());
        idCheckin = nson.get(ID).asString();
        idMekanikKerja = nson.get("MEKANIK_KERJA_ID").asString();
        mekanik = nson.get("MEKANIK").asString();
        noPonsel = nson.get("NO_PONSEL").asString();
        tidakMenunggu = nson.get("TIDAK_MENUNGGU").asString();
        nopol = nson.get("NOPOL").asString();

        String perlengkapan = nson.get("PERLENGKAPAN").asString();
        if (!perlengkapan.isEmpty()) {
            String[] perlengkapanSplit = perlengkapan.split(", ");
            perlengkapanList.asArray().clear();
            perlengkapanList.asArray().addAll(Arrays.asList(perlengkapanSplit));
        }

        viewLayananPartJasa();
        viewKeluhan();
    }


    private void initNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.item_single_field_edit_text, null);
        builder.setView(dialogView);

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Catatan Inspeksi");

        final EditText etCatatan = dialogView.findViewById(R.id.et_edit_text);
        etCatatan.setText(catatan.isEmpty() ? "" : catatan);
        etCatatan.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                catatan = etCatatan.getText().toString();
                find(R.id.et_catatan_mekanik, EditText.class).setText(
                        find(R.id.et_catatan_mekanik, EditText.class).getText().toString()
                                + ", \n"
                                + catatan
                );
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        if (alertDialog != null) {
            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertDialog = builder.show();
        }
    }

    private void initRekomendasiMekanikDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        rvRekomendasi = dialogView.findViewById(R.id.recyclerView);
        initRvRekomendasi();
        if(rekomendasiMekanikList.size() > 0){
            rvRekomendasi.getAdapter().notifyDataSetChanged();
        }
        initToolbarRekomendasi(dialogView);

        alertDialog = builder.create();
        if (alertDialog != null) {
            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertDialog = builder.show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showKeluhanPerlengkapanDialog(boolean isKeluhan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);

        initToolbarKeluhan(dialogView, isKeluhan);
        initRecylerviewKeluhan(dialogView, isKeluhan);

        Objects.requireNonNull(rvKeluhanPerlengkapan.getAdapter()).notifyDataSetChanged();

        alertDialog = builder.create();
        if (alertDialog != null) {
            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertDialog = builder.show();
        }
    }

    private void initRvRekomendasi() {
        rvRekomendasi.setLayoutManager(new LinearLayoutManager(this));
        rvRekomendasi.setHasFixedSize(false);
        rvRekomendasi.setAdapter(new NikitaMultipleViewAdapter(rekomendasiMekanikList, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);
                int no = position + 1;

                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);

                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(rekomendasiMekanikList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(rekomendasiMekanikList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(rekomendasiMekanikList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class)
                            .setText(rekomendasiMekanikList.get(position).get("JUMLAH").asString());
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                            .setText(RP + NumberFormatUtils.formatRp(rekomendasiMekanikList.get(position).get("HARGA_PART").asString()));
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(no + ". ");
                } else if (itemType == ITEM_VIEW_2) {
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(no + ". ");
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(rekomendasiMekanikList.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(rekomendasiMekanikList.get(position).get("AKTIVITAS").asString());
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                            .setText(RP + NumberFormatUtils.formatRp(rekomendasiMekanikList.get(position).get("HARGA_JASA_LAIN").asString()));
                }
            }
        });
    }


    private void initRecyclerviewPointLayanan() {
        rvPointLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvPointLayanan.setHasFixedSize(false);
        rvPointLayanan.setAdapter(new NikitaMultipleViewAdapter(nListArray, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);
                int no = position + 1;

                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);

                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("JUMLAH").asString());
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText("");
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(no + ". ");
                } else if (itemType == ITEM_VIEW_2) {
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(no + ". ");
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("AKTIFITAS").asString());
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class).setVisibility(View.GONE);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewKeluhan(View dialogView, boolean isKeluhan) {
        rvKeluhanPerlengkapan = dialogView.findViewById(R.id.recyclerView);
        rvKeluhanPerlengkapan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvKeluhanPerlengkapan.setHasFixedSize(true);
        if(isKeluhan){
            rvKeluhanPerlengkapan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
                @Override
                public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(keluhanList.get(position).get("NO").asString() + ". ");
                    viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                }
            });

        }else{
            rvKeluhanPerlengkapan.setAdapter(new NikitaRecyclerAdapter(perlengkapanList, R.layout.item_keluhan) {
                @Override
                public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                    super.onBindViewHolder(viewHolder, position);
                    int pos = position + 1;
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(pos + ". ");
                    viewHolder.find(R.id.tv_keluhan, TextView.class).setText(perlengkapanList.get(position).asString());
                    viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                }
            });
        }
    }

    @SuppressLint("NewApi")
    private void viewLayananPartJasa() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                nListArray.asArray().clear();
                fotoKondisiList.asArray().clear();

                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("detail", "PART JASA CHECKIN");
                args.put("id", idCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_INSPEKSI), args));
                nListArray.asArray().addAll(result.get("data").asArray());

                args.remove("detail");
                args.put("detail", "JASA LAYANAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_INSPEKSI), args));
                result = result.get("data");
                for (int i = 0; i < result.size(); i++) {
                    if (nListArray.size() > 0) {
                        for (int j = 0; j < nListArray.size(); j++) {
                            if (!result.get(i).get("JASA_LAIN_ID").asString().isEmpty()
                                    && !nListArray.get(j).get("JASA_ID").asString().equals(result.get(i).get("JASA_LAIN_ID").asString())) {
                                nListArray.add(result.get(i));
                                break;
                            }
                        }

                    } else {
                        nListArray.add(result.get(i));
                    }
                }
                args.remove("detail");
                args.put("detail", "REKOMENDASI MEKANIK");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PERINTAH_KERJA_MEKANIK), args));
                result = result.get("data");
                rekomendasiMekanikList.asArray().clear();
                rekomendasiMekanikList.asArray().addAll(result.asArray());

                args.remove("detail");
                args.put("detail", "fotoKondisi");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KONTROL_LAYANAN), args));
                fotoKondisiList.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                rvPointLayanan.getAdapter().notifyDataSetChanged();
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
                args.put("aktivitas", "DONE");
                args.put("id", idCheckin);
                args.put("idKerja", idMekanikKerja);
                args.put("catatan", catatan);
                args.put("noPonsel", noPonsel);
                args.put("tidakMenunggu", tidakMenunggu);
                args.put("nopol", nopol);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_INSPEKSI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    //showMessageInvalidNotif(getActivity(), result.get("data").get("MESSAGE_INFO").asString(), null);
                    showSuccess("Pekerjaan Selesai");
                    AppApplication.getMessageTrigger();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning(ERROR_INFO);
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
                } else {
                    showWarning(ERROR_INFO);
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
                    args.put("aktivitas", "REWORK");
                } else {
                    args.put("aktivitas", "START");
                }

                args.put("idKerja", idMekanikKerja);
                args.put("id", idCheckin);
                args.put("catatan", catatan);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_INSPEKSI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (isRework) {
                        showInfo("Pekerjaan di Mulai Kembali");
                    } else {
                        showInfo("Pekerjaan di Mulai");
                    }
                } else {
                    showWarning(ERROR_INFO);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isStart) {
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Pekerjaan sedang berlangsung, Stop Pekerjaan?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopWork();
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

    @SuppressLint("InflateParams")
    private void showDialogFotoKondisi() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_foto_kondisi, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        initToolbarFoto(dialogView);

        final ImageView imgDepan = dialogView.findViewById(R.id.img_kondisi_depan);
        final ImageView imgBelakang = dialogView.findViewById(R.id.img_kondisi_belakang);
        final ImageView imgKanan = dialogView.findViewById(R.id.img_kondisi_kanan);
        final ImageView imgKiri = dialogView.findViewById(R.id.img_kondisi_kiri);
        final ImageView imgTambahan1 = dialogView.findViewById(R.id.img_kondisi_tambahan1);
        final ImageView imgTambahan2 = dialogView.findViewById(R.id.img_kondisi_tambahan2);

        dialogView.findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.ly_button).setVisibility(View.GONE);

        if (fotoKondisiList.size() > 0) {
            for (int i = 0; i < fotoKondisiList.size(); i++) {
                switch (fotoKondisiList.get(i).get("TIPE_FOTO").asString()) {
                    case "depan":
                        getFotoKondisi(imgDepan, fotoKondisiList.get(i).get("FILE_FOTO").asString());
                        break;
                    case "belakang":
                        getFotoKondisi(imgBelakang, fotoKondisiList.get(i).get("FILE_FOTO").asString());
                        break;
                    case "kanan":
                        getFotoKondisi(imgKanan, fotoKondisiList.get(i).get("FILE_FOTO").asString());
                        break;
                    case "kiri":
                        getFotoKondisi(imgKiri, fotoKondisiList.get(i).get("FILE_FOTO").asString());
                        break;
                    case "tambahan1":
                        getFotoKondisi(imgTambahan1, fotoKondisiList.get(i).get("FILE_FOTO").asString());
                        break;
                    case "tambahan2":
                        getFotoKondisi(imgTambahan2, fotoKondisiList.get(i).get("FILE_FOTO").asString());
                        break;
                }
            }
        }

        imgDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgDepan.getDrawable() == getResources().getDrawable(R.drawable.icon_camera_fill)){
                    showWarning("FOTO KONDISI DEPAN TIDAK TERSEDIA");
                }else{
                    showDialogPreviewFoto("Depan", getBitmap(imgDepan));
                }
            }
        });

        imgBelakang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgBelakang.getDrawable() == getResources().getDrawable(R.drawable.icon_camera_fill)){
                    showWarning("FOTO KONDISI DEPAN TIDAK TERSEDIA");
                }else{
                    showDialogPreviewFoto("Belakang", getBitmap(imgBelakang));
                }
            }
        });

        imgKanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgKanan.getDrawable() == getResources().getDrawable(R.drawable.icon_camera_fill)){
                    showWarning("FOTO KONDISI DEPAN TIDAK TERSEDIA");
                }else{
                    showDialogPreviewFoto("Kanan", getBitmap(imgKanan));
                }
            }
        });

        imgKiri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgKiri.getDrawable() == getResources().getDrawable(R.drawable.icon_camera_fill)){
                    showWarning("FOTO KONDISI DEPAN TIDAK TERSEDIA");
                }else{
                    showDialogPreviewFoto("Kiri", getBitmap(imgKiri));
                }
            }
        });

        imgTambahan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgTambahan1.getDrawable() == getResources().getDrawable(R.drawable.icon_camera_fill)){
                    showWarning("FOTO KONDISI DEPAN TIDAK TERSEDIA");
                }else{
                    showDialogPreviewFoto("Tambahan 1", getBitmap(imgTambahan1));
                }
            }
        });

        imgTambahan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgTambahan2.getDrawable() == getResources().getDrawable(R.drawable.icon_camera_fill)){
                    showWarning("FOTO KONDISI DEPAN TIDAK TERSEDIA");
                }else{
                    showDialogPreviewFoto("Tambahan 2", getBitmap(imgTambahan2));
                }
            }
        });


        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog = builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewFoto(String tittle, Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Preview " + tittle);

        if(bitmap != null){
            img.setImageBitmap(bitmap);
        }

        btnSimpan.setVisibility(View.GONE);
        btnCancel.setText("Tutup");
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private Bitmap getBitmap(ImageView imageView) {
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            return drawable.getBitmap();
        } catch (Exception e) {
            return null;
        }
    }

    private void getFotoKondisi(ImageView imageView, String url) {
        imageView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.requestLayout();

        Glide.with(getActivity()).load(url).into(imageView);
    }


    @SuppressLint({"NewApi", "NonConstantResourceId"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtn_note:
                initNoteDialog();
                break;
            case R.id.imgBtn_stop:
                if (isStop) {
                    stopWork();
                } else {
                    showWarning("Pekerjaan Belum di Mulai");
                }
                break;
            case R.id.imgBtn_start:
                countClick++;
                if (countClick == 1) {
                    isStart = true;
                    isStop = true;
                    isRework = false;
                    find(R.id.imgBtn_start, ImageButton.class).setImageDrawable(getResources().getDrawable(R.drawable.icon_rework));
                } else if (countClick > 1) {
                    isRework = true;
                    find(R.id.imgBtn_start, ImageButton.class).setImageDrawable(getResources().getDrawable(R.drawable.icon_start));
                    countClick = 0;
                }
                startWork();
                break;
            case R.id.btn_keluhan:
                if(keluhanList.size() == 0)
                    showInfo("KELUHAN PELANGGAN TIDAK TERSEDIA");
                else
                    showKeluhanPerlengkapanDialog(true);
                break;
            case R.id.btn_rekomendasi_mekanik:
                if(rekomendasiMekanikList.size() == 0)
                    showInfo("REKOMENDASI MEKANIK TIDAK TERSEDIA");
                else
                    initRekomendasiMekanikDialog();
                break;
            case R.id.btn_foto_kondisi:
                showDialogFotoKondisi();
                break;
            case R.id.btn_perlengkapan:
                if(perlengkapanList.size() == 0)
                    showInfo("PERLENGKAPAN TIDAK TERSEDIA");
                else
                    showKeluhanPerlengkapanDialog(false);
                break;
        }
    }
}
