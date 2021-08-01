package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class DetailJualPart_Activity extends AppActivity {

    private int jualPartID = 0;
    private final Nson partList = Nson.newArray();
    private final Nson partBatalList = Nson.newArray();

    private RecyclerView rvJualPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_jual_part);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Penjualan Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (!data.asString().isEmpty()) {
            jualPartID = data.get("ID").asInteger();
            find(R.id.et_nama_pelanggan, EditText.class).setText(data.get("NAMA_PELANGGAN").asString());
            find(R.id.et_no_ponsel, EditText.class).setText(data.get("NO_PONSEL").asString());
            find(R.id.et_jenis_kendaraan, EditText.class).setText(data.get("JENIS_KENDARAAN").asString());
            find(R.id.et_tanggal, EditText.class).setText(data.get("TANGGAL").asString());
            find(R.id.et_user, EditText.class).setText(data.get("NAMA_PENJUAL").asString());
        }

        initRecylerviewPelanggan();
        viewData();

        find(R.id.btn_batal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog("Konfrimasi", "Batalkan Jual Part ?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        batalJualPart();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void initRecylerviewPelanggan() {
        rvJualPart = findViewById(R.id.recyclerView);
        rvJualPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJualPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                        .setText(RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("JUMLAH").asString());
            }
        });
    }

    private void viewData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("detail", "Y");
                args.put("jualPartID", String.valueOf(jualPartID));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturjualpart"), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    partList.asArray().clear();
                    partList.asArray().addAll(result.asArray());
                    if(partList.size() > 0){
                        for (int i = 0; i < partList.size(); i++) {
                            partBatalList.add(Nson.newObject()
                                    .set("LOKASI_ID", partList.get(i).get("LOKASI_ID").asString())
                                    .set("PART_ID", partList.get(i).get("PART_ID").asString())
                                    .set("DETAIL_ID", partList.get(i).get("DETAIL_ID").asString())
                                    .set("JUMLAH", partList.get(i).get("JUMLAH").asString())
                            );
                        }
                    }
                    Objects.requireNonNull(rvJualPart.getAdapter()).notifyDataSetChanged();
                } else {
                    showError("Gagal Memuat Aktifitas");
                }
            }
        });
    }

    private void batalJualPart(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("jualPartID", String.valueOf(jualPartID));
                args.put("jualPartDetailIDList", partBatalList.toJson());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturjualpart"), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("JUAL PART BERHASIL DI BATALKAN");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });

    }

}