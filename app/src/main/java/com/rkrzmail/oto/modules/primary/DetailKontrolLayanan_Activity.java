package com.rkrzmail.oto.modules.primary;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontrol_layanan_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_kontrolLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kontrol Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNoAntrian = findViewById(R.id.et_noAntrian_kontrolLayanan);
        etStatus = findViewById(R.id.et_status_kontrolLayanan);
        etNopol = findViewById(R.id.et_nopol_kontrolLayanan);
        etNoKunci = findViewById(R.id.et_noKunci_kontrolLayanan);
        etNamaLayanan = findViewById(R.id.et_namaLayanan_kontrolLayanan);
        etNamaPelanggan = findViewById(R.id.et_namaP_kontrolLayanan);
        etTotal = findViewById(R.id.et_totalBiaya_kontrolLayanan);
        etDp = findViewById(R.id.et_dp_kontrolLayanan);
        etSisa = findViewById(R.id.et_sisa_kontrolLayanan);
        etSebelum = findViewById(R.id.et_Esebelum_kontrolLayanan);
        etWaktu = findViewById(R.id.et_Ewaktu_kontrolLayanan);
        etSelesai = findViewById(R.id.et_Eselesai_kontrolLayanan);
        etAlasanBatal = findViewById(R.id.et_aBatal_kontrolLayanan);
        etPengambilan = findViewById(R.id.et_pengambilan_kontrolLayanan);
        spAktifitas = findViewById(R.id.sp_aktifitas_kontrolLayanan);
        spNamaMekanik = findViewById(R.id.sp_namaMekanik_kontrolLayanan);
        rvItem = findViewById(R.id.recyclerView_detailKontrolLayanan);

        loadData();
        rvItem.setLayoutManager(new LinearLayoutManager(this));
        rvItem.setHasFixedSize(true);
        rvItem.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_nama_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_harga_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_disc_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_net_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
            }
        });

        find(R.id.btn_simpan_kontrolLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
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
