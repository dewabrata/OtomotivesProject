package com.rkrzmail.oto.modules.primary.checkin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import java.util.Map;

public class Checkin3_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_LAYANAN = 11;
    private RecyclerView rvCheckin3;
    public static final String TAG = "Checkin3___";
    private Spinner spLayanan;
    private LinearLayout viewGroup;
    private Nson
            layananArray = Nson.newArray(),
            biayaArray = Nson.newArray(),
            partList = Nson.newArray(),
            jasaList = Nson.newArray();
    private String biayaLayanan, namaLayanan;
    private boolean isListRecylerview, isJasaExternal = false; // true = part, false = jasa
    private int countClick = 0, totalHarga = 0;

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

    private void initComponent() {
        initToolbar();
        find(R.id.btn_jasaLain_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_sparePart_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_jasaLainBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partExternal_checkin3, Button.class).setOnClickListener(this);
        spLayanan = findViewById(R.id.sp_layanan_checkin3);
        viewGroup = findViewById(R.id.parent_ly_checkin3);
        rvCheckin3 = findViewById(R.id.recyclerView_checkin3);

        setSpNamaLayanan();
        spLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.equalsIgnoreCase("--PILIH--")) {
                    find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.GONE);
                }
                for (int i = 0; i < layananArray.size(); i++) {
                    if (biayaArray.get(i).get("NAMA_LAYANAN").asString().equalsIgnoreCase(item)) {
                        find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.VISIBLE);
                        try {
                            find(R.id.tv_biayaLayanan_checkin, TextView.class).setText("Rp." + formatRp(biayaArray.get(i).get("BIAYA_PAKET").asString()));
                        } catch (Exception e) {
                            Log.d(TAG, "BiayaLayanan: " + e.getMessage());
                        }
                        find(R.id.tv_namaLayanan_checkin, TextView.class).setText(biayaArray.get(i).get("NAMA_LAYANAN").asString());
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        initRecylerView();
        componentValidation();
        find(R.id.btn_lanjut_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_batal_checkin3, Button.class).setOnClickListener(this);
    }

    private void initRecylerView() {
        rvCheckin3.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCheckin3.setHasFixedSize(true);
        rvCheckin3.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                try {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(isListRecylerview ? (nListArray.get(position).get("NAMA_PART").asString()) : nListArray.get(position).get("NAMA_KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(isListRecylerview ? nListArray.get(position).get("NO_PART").asString() : nListArray.get(position).get("AKTIVITAS").asString());

                    if (Tools.isNumeric(nListArray.get(position).get("HARGA_NET").asString()) && nListArray.get(position).containsKey("HARGA_NET")) {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                                .setText("Rp. " + formatRp(nListArray.get(position).get("HARGA_NET").asString()));
                    } else {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText("");
                    }
                    if (Tools.isNumeric(nListArray.get(position).get("BIAYA_JASA").asString()) && nListArray.get(position).containsKey("BIAYA_JASA")) {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText("Rp. " + formatRp(nListArray.get(position).get("BIAYA_JASA").asString()));
                    } else {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText("");
                    }
                    if (nListArray.get(position).containsKey("MERK")) {
                        viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setVisibility(View.GONE);
                    } else {
                        viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setText("");
                    }

                } catch (Exception e) {
                    Log.d(TAG, "onBindViewHolder: " + e.getMessage());
                    showError(e.getMessage() + " " + e.getCause());
                }
            }
        });
        rvCheckin3.getAdapter().notifyDataSetChanged();
    }

    private void nextCheckin() {
        Nson getData = Nson.readJson(getIntentStringExtra("data"));
        getData.set("partbook", nListArray);
        getData.set("LAYANAN", spLayanan.getSelectedItem().toString());
        getData.set("TOTAL_BIAYA", totalHarga);
        //getData.set("NAMA_MEKANIK", find(R.id.sp_namaMekanik_checkin3, Spinner.class).getSelectedItem().toString());
        Intent i = new Intent(getActivity(), Checkin4_Activity.class);
        i.putExtra("data", getData.toJson());
        startActivityForResult(i, KontrolLayanan_Activity.REQUEST_CHECKIN);
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "batal");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("checkin"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("GAGAL!");
                }
            }
        });

    }

    private void componentValidation() {
        find(R.id.cb_estimasi_checkin3, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(false);
                    if(totalHarga > 0){
                        find(R.id.et_totalBiaya_checkin3, EditText.class).setText("Rp." + formatRp( String.valueOf(totalHarga)));
                    }else{
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
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("status", "TIDAK AKTIF");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    biayaArray.asArray().addAll(result.asArray());
                    if (result.asArray().size() == 0) {
                        showInfo("Layanan Belum Tercatatkan");
                        showInfoDialog("Tambah Layanan ? ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturLayanan_Activity.class), REQUEST_LAYANAN);
                            }
                        });
                        return;
                    }
                    layananArray.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        layananArray.add(result.get(i).get("NAMA_LAYANAN").asString());
                    }

                    Log.d(TAG, "List : " + layananArray);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, layananArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLayanan.setAdapter(spinnerAdapter);
                } else {

                }
            }
        });
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
                if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {
                    saveData();
                } else {
                    nextCheckin();
                }
                break;
            case R.id.btn_batal_checkin3:
                saveData();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Booking3_Activity.REQUEST_JASA_LAIN:
                    isListRecylerview = false;
                    countClick = 2;
                    Nson nson = Nson.readJson(getIntentStringExtra(data, "data"));
                    jasaList.add(nson);
                    nListArray.add(nson);
                    Log.d(TAG, "REQUEST_JASA_LAIN : " + nson);
                    //int harga = nson.get("").asInteger();
                    //find(R.id.et_totalBiaya_checkin4, EditText.class).setText();
                    break;
                case Booking3_Activity.REQUEST_JASA_BERKALA:
                    countClick = 2;
                    isListRecylerview = false;
                    jasaList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    Log.d(TAG, "REQUEST_JASA_BERKALA : " + nListArray);
                    break;
                case Booking3_Activity.REQUEST_PART:
                    countClick = 1;
                    isListRecylerview = true;
                    i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                    i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    i.putExtra("bengkel", "");
                    //Log.d("JUMLAH_HARGA_PART", "INTENT : "   + Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    startActivityForResult(i, Booking3_Activity.REQUEST_HARGA_PART);
                    break;
                case Booking3_Activity.REQUEST_PART_BERKALA:
                    countClick = 1;
                    isListRecylerview = true;
                    partList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    break;
                case Booking3_Activity.REQUEST_PART_EXTERNAL:
                    countClick = 1;
                    i = new Intent(getActivity(), JasaExternal_Activity.class);
                    i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    startActivityForResult(i, Booking3_Activity.REQUEST_JASA_EXTERNAL);
                    break;
                case Booking3_Activity.REQUEST_JASA_EXTERNAL:
                    countClick = 1;
                    isListRecylerview = true;
                    isJasaExternal = getIntent().getBooleanExtra("external", true);
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    jasaList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    break;
                case Booking3_Activity.REQUEST_HARGA_PART:
                    countClick = 1;
                    isListRecylerview = true;
                    partList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    Log.d(TAG, "REQUEST_PART " + Nson.readJson(getIntentStringExtra(data, "data")));
                    break;
                case KontrolLayanan_Activity.REQUEST_CHECKIN:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
            //rvCheckin3.getAdapter().notifyDataSetChanged();
            for (int j = 0; j < nListArray.size(); j++) {
                if(nListArray.get(j).containsKey("HARGA_NET") && Tools.isNumeric(nListArray.get(j).get("HARGA_NET").asString().replaceAll("[^0-9]+", ""))){
                    totalHarga += Integer.parseInt(nListArray.get(j).get("HARGA_NET").asString().replaceAll("[^0-9]+", ""));
                }
                if(nListArray.get(j).containsKey("BIAYA_JASA") && Tools.isNumeric(nListArray.get(j).get("BIAYA_JASA").asString().replaceAll("[^0-9]+", ""))){
                    totalHarga += Integer.parseInt(nListArray.get(j).get("BIAYA_JASA").asString().replaceAll("[^0-9]+", ""));
                }
            }
            if(totalHarga > 0){
                String total = String.valueOf(totalHarga);
                showInfo(formatRp(total));
            }
        }
        Log.d(TAG, "PART : " + partList + "\n" + "JASA : " + jasaList);
    }
}
