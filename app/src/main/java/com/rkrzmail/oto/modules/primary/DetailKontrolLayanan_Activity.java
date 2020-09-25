package com.rkrzmail.oto.modules.primary;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class DetailKontrolLayanan_Activity extends AppActivity {

    private EditText etNoAntrian, etStatus, etNopol, etNoKunci, etNamaPelanggan, etNamaLayanan, etTotal,
            etDp, etSisa, etSebelum, etWaktu, etSelesai, etPengambilan, etAlasanBatal;
    private RecyclerView rvItem;
    private Spinner spAktifitas, spNamaMekanik;
    private Nson partList = Nson.newArray(),
            jasaList = Nson.newArray();
    private boolean isListRecylerview; // true = part, false = jasa

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
        etSebelum = findViewById(R.id.et_Esebelum);
        etWaktu = findViewById(R.id.et_Ewaktu);
        etSelesai = findViewById(R.id.et_Eselesai);
        etAlasanBatal = findViewById(R.id.et_ket_batal);
        etPengambilan = findViewById(R.id.et_pengambilan);
        spAktifitas = findViewById(R.id.sp_aktifitas);
        spNamaMekanik = findViewById(R.id.sp_namaMekanik);
        rvItem = findViewById(R.id.recyclerView_detailKontrolLayanan);

        loadData();

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra("data"));
        etNoAntrian.setText(n.get("").asString());
        etStatus.setText(n.get("").asString());
        etNopol.setText(n.get("").asString());
        etNoKunci.setText(n.get("").asString());
        etNamaLayanan.setText(n.get("").asString());
        etNamaPelanggan.setText(n.get("").asString());
        etTotal.setText(n.get("").asString());
        etDp.setText(n.get("").asString());
        etSisa.setText(n.get("").asString());
        etSebelum.setText(n.get("").asString());
        etWaktu.setText(n.get("").asString());
        etSelesai.setText(n.get("").asString());
        etPengambilan.setText(n.get("").asString());
        etAlasanBatal.setText(n.get("").asString());
    }

    private void initRecyclerview(){
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

                if(isListRecylerview){
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(nListArray.get(position).get("BIAYA_JASA").asString());
                }
            }
        });
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
