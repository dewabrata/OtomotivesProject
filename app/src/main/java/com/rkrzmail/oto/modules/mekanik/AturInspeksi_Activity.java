package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_INSPEKSI;
import static com.rkrzmail.utils.APIUrls.ATUR_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_INSPEKSI;
import static com.rkrzmail.utils.APIUrls.VIEW_KELUHAN;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.Tools.removeDuplicates;

public class AturInspeksi_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvPointLayanan, rvKeluhan;
    private AlertDialog alertDialog;

    private String idCheckin = "";
    private Nson keluhanList = Nson.newArray();
    private int countClick = 0;
    private String idMekanikKerja = "";
    private String mekanik = "", catatan = "";
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

    private void initToolbarKeluhan(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Keluhan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    private void initComponent() {
        rvPointLayanan = findViewById(R.id.recyclerView);
        initRecyclerviewPointLayanan();
        loadData();

        find(R.id.imgBtn_note).setOnClickListener(this);
        find(R.id.btn_keluhan).setOnClickListener(this);
        find(R.id.imgBtn_start).setOnClickListener(this);
        find(R.id.imgBtn_stop).setOnClickListener(this);
    }

    private void loadData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));

        idCheckin = nson.get(ID).asString();
        idMekanikKerja = nson.get("MEKANIK_KERJA_ID").asString();
        mekanik = nson.get("MEKANIK").asString();

        viewLayananPartJasa();
        viewKeluhan();
    }


    private void initNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.item_note, null);
        builder.setView(dialogView);

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Catatan Mekanik");

        final EditText etCatatan = dialogView.findViewById(R.id.et_catatan_mekanik);
        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               catatan = etCatatan.getText().toString();
               alertDialog.dismiss();
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

        initToolbarKeluhan(dialogView);
        initRecylerviewKeluhan(dialogView);
        Objects.requireNonNull(rvKeluhan.getAdapter()).notifyDataSetChanged();

        builder.create();
        alertDialog = builder.show();
    }

    private void initRecyclerviewPointLayanan() {
        rvPointLayanan.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        rvPointLayanan.setHasFixedSize(false);
        rvPointLayanan.setAdapter(new NikitaMultipleViewAdapter(nListArray, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);
                int no = position + 1;
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

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_INSPEKSI), args));
                for (int i = 0; i < result.size(); i++) {
                    if(!result.get("data").get(i).get("PART_ID").asString().isEmpty()){
                        nListArray.add(result.get("data").get(i));
                    }
                    if(!result.get("data").get(i).get("JASA_ID").asString().isEmpty()){
                        nListArray.add(result.get("data").get(i));
                    }
                }
                args.remove("detail");
                args.put("detail", "JASA LAYANAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_INSPEKSI), args));
                for (int i = 0; i < result.size(); i++) {
                    for (int j = 0; j < nListArray.size(); j++) {
                        if (!result.get("data").get(i).get("JASA_LAIN_ID").asString().isEmpty()
                                && !nListArray.get(j).get("JASA_ID").asString().equals(result.get("data").get(i).get("JASA_LAIN_ID").asString())) {
                            nListArray.add(result.get("data").get(i));
                            break;
                        }
                    }
                }
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    rvPointLayanan.getAdapter().notifyDataSetChanged();
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
                args.put("aktivitas", "DONE");
                args.put("id", idCheckin);
                args.put("idKerja", idMekanikKerja);
                args.put("catatan", catatan);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_INSPEKSI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Pekerjaan Selesai");
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
                    if(isRework){
                        showInfo("Pekerjaan di Mulai Kembali");
                    }else{
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

        }else{
            finish();
        }
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
                initKeluhanDialog();
                break;
        }
    }
}
