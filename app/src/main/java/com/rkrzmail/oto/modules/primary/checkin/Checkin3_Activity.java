package com.rkrzmail.oto.modules.primary.checkin;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.rkrzmail.oto.modules.jasa.JasaLainBerkala_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.primary.KontrolLayanan_Activity;
import com.rkrzmail.oto.modules.primary.booking.Booking3_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.JumlahHargaPart_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.PartBerkala_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class Checkin3_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvCheckin3;
    public static final String TAG = "Checkin3___";
    private Spinner spLayanan;
    private LinearLayout viewGroup;
    private Nson mekanikArray = Nson.newArray();


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

        setSpinnerFromApi(spLayanan, "action", "view", "viewlayanan", "NAMA_LAYANAN", "");
        setSpinnerFromApi(find(R.id.sp_namaMekanik_checkin3, Spinner.class), "action", "view", "mekanik", "NAMA", "");
        componentValidation();

        rvCheckin3 = findViewById(R.id.recyclerView_checkin3);
        rvCheckin3.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCheckin3.setHasFixedSize(true);
        rvCheckin3.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                String hargaPart = nListArray.get(position).get("HARGA_PART").asString();
                String hargaJasa = nListArray.get(position).get("HARGA_JASA").asString();
                String net = nListArray.get(position).get("NET").asString();

                if (!hargaJasa.isEmpty()) {
                    viewHolder.find(R.id.tv_harga_booking3_checkin3, TextView.class).setText(hargaJasa);
                    viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(hargaJasa);
                } else if (!hargaPart.isEmpty()) {
                    viewHolder.find(R.id.tv_harga_booking3_checkin3, TextView.class).setText(hargaPart);
                    viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(hargaPart);
                }
                viewHolder.find(R.id.tv_net_booking3_checkin3, TextView.class).setText(net);
                viewHolder.find(R.id.tv_nama_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_disc_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("DISC").asString());

            }
        });

        find(R.id.btn_lanjut_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_batal_checkin3, Button.class).setOnClickListener(this);


    }

    private void nextCheckin(){
        Nson getData = Nson.readJson(getIntentStringExtra("data"));
        getData.set("partbook", nListArray);
        getData.set("LAYANAN", spLayanan.getSelectedItem().toString());
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
                if(buttonView.isChecked()){
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(false);
                }else{
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(true);
                }
            }
        });

        find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){
                    find(R.id.cb_estimasi_checkin3, CheckBox.class).setEnabled(false);
                }else{
                    find(R.id.cb_estimasi_checkin3, CheckBox.class).setEnabled(true);
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
                i.putExtra("flag", "ALL");
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
                i.putExtra("flag", "MASTER_PART");
                startActivityForResult(i, Booking3_Activity.REQUEST_PART_EXTERNAL);
                break;
            case R.id.btn_lanjut_checkin3:
                if(find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()){
                    saveData();
                }else{
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
                    Nson nson = Nson.readJson(getIntentStringExtra(data, "data"));
                    nListArray.add(nson);
                    Log.d(TAG, "REQUEST_JASA_LAIN : " + nson);
                    rvCheckin3.getAdapter().notifyDataSetChanged();
                    //int harga = nson.get("").asInteger();
                    //find(R.id.et_totalBiaya_checkin4, EditText.class).setText();
                    break;
                case Booking3_Activity.REQUEST_JASA_BERKALA:
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    Log.d(TAG, "REQUEST_JASA_BERKALA : " + nListArray);
                    rvCheckin3.getAdapter().notifyDataSetChanged();
                    break;
                case Booking3_Activity.REQUEST_PART:
                    i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                    i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    //Log.d("JUMLAH_HARGA_PART", "INTENT : "   + Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    startActivityForResult(i, Booking3_Activity.REQUEST_HARGA_PART);
                    break;
                case Booking3_Activity.REQUEST_PART_BERKALA:
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    rvCheckin3.getAdapter().notifyDataSetChanged();
                    break;
                case Booking3_Activity.REQUEST_PART_EXTERNAL:
                    i = new Intent(getActivity(), JumlahHargaPart_Activity.class);
                    i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    startActivityForResult(i, Booking3_Activity.REQUEST_HARGA_PART);
                    break;
                case Booking3_Activity.REQUEST_HARGA_PART:
                    nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                    Log.d(TAG, "REQUEST_PART " + Nson.readJson(getIntentStringExtra(data, "data")));
                    rvCheckin3.getAdapter().notifyDataSetChanged();
                    break;
                case KontrolLayanan_Activity.REQUEST_CHECKIN:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
        Log.d(TAG, "TOTAL : " + nListArray);
    }
}
