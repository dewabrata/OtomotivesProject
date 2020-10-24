package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_TUGAS_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_PERMINTAAN;
import static com.rkrzmail.utils.ConstUtils.TUGAS_PART_TERSEDIA;

public class Status_TugasPart_Activity extends AppActivity {

    private RecyclerView recyclerView;
    private EditText etPelanggan, etMekanik;
    private Toolbar toolbar;

    private Nson partSerahTerimaList = Nson.newArray();
    private boolean isPermintaan = false;
    private boolean isTersedia = false;
    private String mekanik = "", tanggalCheckin = "", nopol = "";
    private String idLokasiPart = "", idCheckinDetail = "";
    private int jumlahSerahTerima = 0;
    private Boolean isPermintaanList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_tugas_part);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (getIntent().hasExtra(TUGAS_PART_PERMINTAAN)) {
            getSupportActionBar().setTitle("Penyediaan Part");
            isPermintaan = true;
            find(R.id.img_scan_barcode).setVisibility(View.GONE);
        } else if (getIntent().hasExtra(TUGAS_PART_TERSEDIA)) {
            getSupportActionBar().setTitle("Serah Terima Part");
            isTersedia = true;
            find(R.id.btn_simpan).setVisibility(View.GONE);
        }
    }

    private void initComponent() {
        recyclerView = findViewById(R.id.recyclerView);
        etPelanggan = findViewById(R.id.et_pelanggan_statusTp);
        etMekanik = findViewById(R.id.et_user_statusTp);

        loadData();
        initListener();
        initRecyclerView();
    }

    private void initListener() {
        find(R.id.img_scan_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra(DATA));
        etPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
        etMekanik.setText(n.get("MEKANIK").asString());

        idLokasiPart = n.get("LOKASI_PART_ID").asString();
        idCheckinDetail = n.get("CHECKIN_DETAIL_ID").asString();
        nopol = n.get("NOPOL").asString();
        tanggalCheckin = n.get("TANGGAL_CHECKIN").asString();
        viewTugasPart();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_status_tugas_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);

                        viewHolder.find(R.id.tv_merk_statusTp, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                        viewHolder.find(R.id.tv_namaPart_statusTp, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_noPart_statusTp, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_jumlah, TextView.class).setText(nListArray.get(position).get("JUMLAH_PERMINTAAN").asString());

                        if (isPermintaan) {
                            viewHolder.find(R.id.tv_kode_lokasi_or_tersedia, TextView.class).
                                    setText(nListArray.get(position).get("KODE").asString());
                        } else {
                            viewHolder.find(R.id.tv_kode_lokasi_or_tersedia, TextView.class).
                                    setText(nListArray.get(position).get("JUMLAH_TERSEDIA").asString());
                        }
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        if (isPermintaan) {
                            Intent i = new Intent(getActivity(), JumlahPart_TugasPart_Activity.class);
                            i.putExtra(TUGAS_PART_PERMINTAAN, "");
                            i.putExtra(DATA, nListArray.get(position).toJson());
                            startActivityForResult(i, REQUEST_TUGAS_PART);
                        }
                        /*if(isTersedia){
                            i.putExtra(TUGAS_PART_TERSEDIA, nListArray.get(position).toJson());
                        }*/
                    }
                })
        );
    }

    private void viewTugasPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                if (isPermintaan) {
                    args.put("detail", "PERMINTAAN");
                }
                if (isTersedia) {
                    args.put("detail", "TERSEDIA");
                }
                args.put("mekanik", etMekanik.getText().toString());
                args.put("nopol", nopol);
                args.put("namaPelanggan", etPelanggan.getText().toString());
                args.put("tanggalCheckin", tanggalCheckin);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    if(isTersedia){
                        for (int i = 0; i < nListArray.size(); i++) {
                            partSerahTerimaList.add(Nson.newObject()
                                    .set("CHECKIN_DETAIL_ID", nListArray.get(i).get("CHECKIN_DETAIL_ID").asString())
                                    .set("JUMLAH", nListArray.get(i).get("JUMLAH_TERSEDIA").asString()));
                        }
                    }
                    Log.d("PART__", "SERAHTERIMA: " + partSerahTerimaList);
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    private void setSerahTerima() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("group", "SERAH TERIMA");
                //args.put("idCheckin", idCheckinDetail);
                args.put("partList", partSerahTerimaList.toJson());
                args.put("idLokasiPart", idLokasiPart);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_TUGAS_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Part Telah Di Serahterimakan", Toast.LENGTH_LONG);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String barcodeResult = data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "";
            MyCode.checkMyCode(this, barcodeResult, new MyCode.RunnableWD() {
                @Override
                public void runWD(Nson nson) {
                    if (nson.get("status").asString().equals("OK")) {
                        Log.d("Barcode__", "onActivityResult: " + nson);
                        nson = nson.get("data").get(0);
                        if(nson.get("NAMA").asString().equals(etMekanik.getText().toString())){
                            setSerahTerima();
                        }else{
                            showWarning("Mekanik Tidak Valid");
                        }
                    } else {
                        showError(nson.get("message").asString());
                    }
                }
            });
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_TUGAS_PART){
            viewTugasPart();
        }
    }
}
