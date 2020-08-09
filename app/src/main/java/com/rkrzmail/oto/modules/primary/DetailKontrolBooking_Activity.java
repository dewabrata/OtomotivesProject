package com.rkrzmail.oto.modules.primary;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class DetailKontrolBooking_Activity extends AppActivity {

    private Nson getData;
    private RecyclerView rvLayanan;
    private static final String TAG = "DETAIL_KONTROL_BOOKING___";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontrol_booking);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kontrol Booking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("LongLogTag")
    private void initComponent(){
        initToolbar();
        rvLayanan = findViewById(R.id.recyclerView);
        initRecyelerView();

        spinnerNoDefaultOffline(find(R.id.sp_status_kontrolBooking, Spinner.class), getResources().getStringArray(R.array.status_kontrol));
        spinnerNoDefaultOffline(find(R.id.sp_jenisBook_kontrolBooking, Spinner.class), getResources().getStringArray(R.array.jenis_booking));
        spinnerNoDefaultOffline(find(R.id.sp_aktifitas_kontrolBooking, Spinner.class), getResources().getStringArray(R.array.aktifitas_kontrol));
        spinnerNoDefaultOffline(find(R.id.sp_mekanik_kontrolBooking, Spinner.class), getResources().getStringArray(R.array.aktifitas_kontrol));

        getData = Nson.readJson(getIntentStringExtra("data"));
        Log.d(TAG, "meesagess: " + getData);

        find(R.id.et_alamat_kontrolBooking, EditText.class).setText(getData.get("ALAMAT").asString());
        find(R.id.et_keluhan_kontrolBooking, EditText.class).setText(getData.get("KELUHAN").asString());
        find(R.id.et_nopol_kontrolBooking, EditText.class).setText(getData.get("NOPOL").asString());
        find(R.id.et_kendaraan_kontrolBooking, EditText.class).setText(getData.get("JENIS_KENDARAAN").asString());
        find(R.id.et_noponsel_kontrolBooking, EditText.class).setText(getData.get("NO_PONSEL").asString());
        find(R.id.et_namaLayanan_kontrolBooking, EditText.class).setText(getData.get("LAYANAN").asString());
        find(R.id.et_namaP_kontrolBooking, EditText.class).setText(getData.get("NAMA_PELANGGAN").asString());
        find(R.id.et_totalBiaya_kontrolBooking, EditText.class).setText(getData.get("TOTAL_BIAYA").asString());
        find(R.id.et_dp_kontrolBooking, EditText.class).setText(getData.get("DP").asString());
        find(R.id.et_sisa_kontrolBooking, EditText.class).setText(getData.get("SISA").asString());
        find(R.id.et_ketBatal_kontrolBooking, EditText.class).setText(getData.get("").asString());
        find(R.id.et_tgl_kontrolBooking, EditText.class).setText(getData.get("TANGGAL").asString());
        find(R.id.et_jam_kontrolBooking, EditText.class).setText(getData.get("JAM").asString());
        find(R.id.et_waktu_kontrolBooking, EditText.class).setText(getData.get("WAKTU").asString());
        find(R.id.et_namaUser_kontrolBooking, EditText.class).setText(getData.get("USER").asString());

        nListArray.asArray().clear();
        nListArray.asArray().addAll(getData.get("PART_BOOK").asArray());


        find(R.id.btn_simpan_kontrolBooking, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdate();
            }
        });
    }

    void saveUpdate(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("aktifitas", find(R.id.sp_aktifitas_kontrolBooking, Spinner.class).getSelectedItem().toString());
                args.put("mekanik",find(R.id.sp_mekanik_kontrolBooking, Spinner.class).getSelectedItem().toString());
                //args.put("penjemput",);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkontrolbooking"), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    void initRecyelerView(){
        rvLayanan.setHasFixedSize(true);
        rvLayanan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_booking3_checkin3){
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
    }
}
