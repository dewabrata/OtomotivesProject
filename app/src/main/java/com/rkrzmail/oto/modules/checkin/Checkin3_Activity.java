package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.jasa.BiayaJasa_Activity;
import com.rkrzmail.oto.modules.jasa.JasaExternal_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLainBerkala_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.JumlahPart_Checkin_Activity;
import com.rkrzmail.oto.modules.sparepart.PartBerkala_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.GET_HISTORY_USULAN_MEKANIK;
import static com.rkrzmail.utils.APIUrls.ROLLBACK_TRANSACTIONS;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
import static com.rkrzmail.utils.ConstUtils.BATAL;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_OTOMOTIVES;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.PARTS_UPPERCASE;
import static com.rkrzmail.utils.ConstUtils.PART_WAJIB;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BATAL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_HARGA_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_BERKALA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_EXTERNAL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_JASA_LAIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PART_BERKALA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PART_EXTERNAL;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class Checkin3_Activity extends AppActivity implements View.OnClickListener {

    public static final String TAG = "Checkin3___";

    private RecyclerView rvPart, rvJasaLain, rvKeluhan, rvHistoryPart, rvHistoryJasa, rvPointLayanan;
    private NikitaAutoComplete etKeluhan;
    private Spinner spLayanan;
    private AlertDialog alertDialog;
    RecyclerView rvPartWajib;
    View dialogView;

    private final Nson layananAFS = Nson.newArray();
    private final Nson layananAFSHistory = Nson.newArray();
    private final Nson layananArray = Nson.newArray();
    private final Nson dataLayananList = Nson.newArray();
    private final Nson partList = Nson.newArray();
    private final Nson jasaList = Nson.newArray();
    private final Nson lokasiLayananList = Nson.newArray();
    private final Nson partWajibList = Nson.newArray();
    private final Nson jasaGaransiList = Nson.newArray();
    private final Nson daftarPartDummy = Nson.newArray();
    private final Nson varianList = Nson.newArray();
    private final Nson masterPartList = Nson.newArray();
    private final Nson partIdList = Nson.newArray();
    private final Nson keluhanList = Nson.newArray();
    private final Nson partJasaList = Nson.newArray();
    private final Nson pointLayananList = Nson.newArray();
    private final Nson rekomendasiPartList = Nson.newArray();
    private final Nson rekomendasiJasaList = Nson.newArray();

    private Nson data;

    private String namaLayanan = "",
            jenisLayanan = "",
            waktuMekanik = "",
            waktuInspeksi = "",
            waktuLayanan = "";
    private String nopol = "";
    private String pekerjaan = "";
    private final String jenisAntrian = "";
    private String isOutsource = "";
    private final String noAntrian = "";
    private String discFasilitas = "";
    private double discLayanan = 0;
    private String biayaLayanan = "", biayaPergantian = "";
    private String layananId = "";

    private int kmKendaraan = 0;
    private int totalHarga = 0;
    private final int totalPartJasa = 0;
    private int batasanKm = 0;
    private int batasanBulan = 0;
    private int jumlahPartWajib = 0;
    private int feeNonPaket = 0;
    private final int idAntrian = 0;
    private int waktuPesan = 0;
    private int totalHargaPartKosong = 0;
    private int hargaPartLayanan = 0; // afs, recall, otomotives
    private int partId = 0, jumlahPart = 0;
    private int totalDp = 0;
    private int checkinID = 0;
    private int kendaraanID = 0;

    private final List<Integer> jumlahList = new ArrayList<>();
    private final Tools.TimePart dummyTime = Tools.TimePart.parse("00:00:00");
    private double sisaDp = 0;
    private boolean
            flagPartWajib = false,
            flagMasterPart = false,
            isPartWajib = false,
            isSelanjutnya = false,
            isDelete = false;
    private boolean isPartKosong = false;
    private boolean isBatal = false;
    private boolean isJasaExternal = false;
    private boolean isHplusLayanan = false;
    private boolean isAFService = false;
    private boolean isRemoved = false;
    private boolean isKonfirmasi = false;
    private boolean isOnResumed = false;
    private boolean isPartJasaUsulan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin3);
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        loadData();
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("NewApi")
    private void initToolbarPartWajib(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Part Wajib Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @SuppressLint("NewApi")
    private void initToolbarKonfirmasi(View dialogView) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Konfirmasi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initComponent() {
        find(R.id.btn_jasaLain_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_sparePart_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_jasaLainBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partExternal_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_lanjut_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_batal_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_point_layanan, Button.class).setOnClickListener(this);
        find(R.id.btn_usulan_mekanik, Button.class).setOnClickListener(this);

        etKeluhan = findViewById(R.id.et_keluhan_checkin1);
        spLayanan = findViewById(R.id.sp_layanan_checkin3);
        rvKeluhan = findViewById(R.id.recyclerView_keluhan);

        setSpNamaLayanan();
        componentValidation();
        initRecylerViewPart();
        initRecylerviewJasaLain();
        initRecylerViewKeluhan();
        initAutoCompleteKeluhan();

        find(R.id.fab_tambah_keluhan, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etKeluhan.getText().toString().isEmpty() || etKeluhan.getText().toString().length() < 5) {
                    etKeluhan.setError("Keluhan Min 5 Karakter");
                } else {
                    isRemoved = true;
                    keluhanList.add(Nson.newObject().set("KELUHAN", etKeluhan.getText().toString()));
                    initRecylerViewKeluhan();
                    rvKeluhan.getAdapter().notifyDataSetChanged();
                    etKeluhan.setText("");
                }
            }
        });
    }

    private void loadData() {
        data = Nson.readJson(getIntentStringExtra(DATA));
        kmKendaraan = data.get("km").asInteger();
        checkinID = data.get("CHECKIN_ID").asInteger();
        kendaraanID = data.get("KENDARAAN_ID").asInteger();
        pekerjaan = data.get("pekerjaan").asString();
        nopol = data.get("NOPOL").asString();

        getPartJasaHistory();
    }

    private void initAutoCompleteKeluhan() {
        etKeluhan.setThreshold(0);
        etKeluhan.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "KELUHAN");
                args.put("keluhan", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));
                return result.get("data");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }

                findView(convertView, R.id.title, TextView.class).setText(getItem(position).get("KELUHAN").asString());
                return convertView;
            }
        });

        etKeluhan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_keluhan));
        etKeluhan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etKeluhan.setText(n.get("KELUHAN").asString());
                Tools.hideKeyboard(getActivity());
            }
        });

    }

    private void initRecylerViewPart() {
        if (isKonfirmasi) {
            rvPart = dialogView.findViewById(R.id.recyclerView);
        } else {
            rvPart = findViewById(R.id.recyclerView_part_checkin3);
        }
        rvPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPart.setHasFixedSize(false);
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(partList.get(position).get("NO_PART").asString());
                try {
                    if (Tools.isNumeric(partList.get(position).get("HARGA_PART").asString())) {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(partList.get(position).get("HARGA_PART").asString()));
                    } else {
                        viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(partList.get(position).get("HARGA_PART").asString());
                    }
                    if (Tools.isNumeric(partList.get(position).get("HARGA_JASA").asString()) || !partList.get(position).get("HARGA_JASA").asString().isEmpty()) {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                                RP + formatRp(partList.get(position).get("HARGA_JASA").asString()));
                    } else {
                        viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText("");
                    }
                } catch (Exception e) {
                    showError(e.getMessage());
                }

                viewHolder.find(R.id.tv_discount_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(partList.get(position).get("DISCOUNT_PART").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class).setVisibility(View.GONE);

                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Part ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                totalKurangWaktuLayanan(Tools.TimePart.parse(partList.get(position).get("WAKTU_KERJA").asString()));
                                totalKurangWaktuLayanan(Tools.TimePart.parse(partList.get(position).get("WAKTU_INSPEKSI").asString()));
                                totalKurangWaktuLayanan(Tools.TimePart.parse(formatOnlyNumber(partList.get(position).get("WAKTU_PESAN").asString())));

                                totalHarga -= partList.get(position).get("NET").asInteger();
                                find(R.id.et_totalBiaya_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalHarga)));
                                if (!jasaList.get(position).get("DP").asString().isEmpty()) {
                                    totalDp -= jasaList.get(position).get("DP").asInteger();
                                    find(R.id.et_dp_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalDp)));
                                }

                                partList.asArray().remove(position);
                                notifyItemRemoved(position);
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
        });
    }

    private void initRecylerviewJasaLain() {
        if (isKonfirmasi) {
            rvJasaLain = dialogView.findViewById(R.id.recyclerView2);
        } else {
            rvJasaLain = findViewById(R.id.recyclerView_jasalain_checkin3);
        }
        rvJasaLain.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvJasaLain.setHasFixedSize(false);
        rvJasaLain.setAdapter(new NikitaRecyclerAdapter(jasaList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("NAMA_KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(jasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText("Rp. " + formatRp(jasaList.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Hapus Jasa Lain ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                totalKurangWaktuLayanan(Tools.TimePart.parse(jasaList.get(position).get("WAKTU_KERJA").asString()));
                                totalKurangWaktuLayanan(Tools.TimePart.parse(jasaList.get(position).get("WAKTU_INSPEKSI").asString()));

                                totalHarga -= jasaList.get(position).get("NET").asInteger();
                                find(R.id.et_totalBiaya_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalHarga)));

                                jasaList.asArray().remove(position);
                                notifyItemRemoved(position);
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
        });
    }

    @SuppressLint("NewApi")
    private void initRecylerviewPartWajib() {
        rvPartWajib = dialogView.findViewById(R.id.recyclerView);
        rvPartWajib.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPartWajib.setHasFixedSize(false);
        rvPartWajib.setAdapter(new NikitaRecyclerAdapter(flagMasterPart ? masterPartList : partWajibList, R.layout.item_master_part_wajib_layanan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                if (flagMasterPart) {
                    //viewHolder.find(R.id.tv_nama_master_part, TextView.class).setText(masterPartList.get("NAMA_MASTER").asString());
                    viewHolder.find(R.id.tv_merkPart, TextView.class).setText(masterPartList.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_namaPart, TextView.class).setText(masterPartList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart, TextView.class).setText(masterPartList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_stockPart, TextView.class).setText(masterPartList.get(position).get("STOCK").asString());
                    viewHolder.find(R.id.tv_harga_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(masterPartList.get(position).get("HARGA_PART").asString()));

                    if (!masterPartList.get(position).get("FREKWENSI_PART_TO_KENDARAAN").asString().isEmpty()) {
                        viewHolder.find(R.id.img_check).setVisibility(View.VISIBLE);
                    }
                } else {
                    //viewHolder.find(R.id.cardView_master_part, CardView.class).setVisibility(View.GONE);
                    //viewHolder.find(R.id.tv_nama_master_part, TextView.class).setText(partWajibList.get("NAMA_MASTER").asString());
                    viewHolder.find(R.id.tv_namaPart, TextView.class).setText(partWajibList.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart, TextView.class).setText(partWajibList.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_stockPart, TextView.class).setText(partWajibList.get(position).get("STOCK").asString());
                    viewHolder.find(R.id.tv_merkPart, TextView.class).setText(partWajibList.get(position).get("MERK_PART").asString());
                    viewHolder.find(R.id.tv_harga_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(partWajibList.get(position).get("HARGA_PART").asString()));

                    if (!partWajibList.get(position).get("FREKWENSI_PART_TO_KENDARAAN").asString().isEmpty()) {
                        viewHolder.find(R.id.img_check).setVisibility(View.VISIBLE);
                    }
                }
            }

        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                daftarPartDummy.add(parent.get(position).get("PART_ID").asString());
                Intent i = new Intent(getActivity(), JumlahPart_Checkin_Activity.class);
                if (flagMasterPart) {
                    i.putExtra(PART_WAJIB, masterPartList.get(position).toJson());
                    i.putExtra(MASTER_PART, discFasilitas);
                } else {
                    i.putExtra(PARTS_UPPERCASE, discFasilitas);
                    i.putExtra(PART_WAJIB, partWajibList.get(position).toJson());
                }
                i.putExtra("KM", kmKendaraan);
                i.putExtra("WAKTU_INSPEKSI", waktuInspeksi);
                i.putExtra("HARGA_LAYANAN", hargaPartLayanan);
                i.putExtra("PERGANTIAN", biayaPergantian);
                startActivityForResult(i, REQUEST_HARGA_PART);
            }
        }));
    }

    private void initRecylerViewKeluhan() {
        rvKeluhan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvKeluhan.setHasFixedSize(false);
        rvKeluhan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View v) {
                        isRemoved = false;
                        keluhanList.asArray().remove(position);
                        notifyItemRemoved(position);
                        Objects.requireNonNull(rvKeluhan.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void componentValidation() {
        find(R.id.cb_estimasi_checkin3, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(false);
                    /*if (totalHarga > 0) {
                        find(R.id.et_totalBiaya_checkin3, EditText.class).setText("Rp." + formatRp(String.valueOf(totalHarga)));
                    } else {
                        showWarning("Tambahkan Part & Jasa Terlebih Dahulu");
                    }*/
                } else {
                    find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setEnabled(true);
                }
            }
        });

        find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                find(R.id.cb_estimasi_checkin3, CheckBox.class).setEnabled(!buttonView.isChecked());
            }
        });
    }

    private void setSpNamaLayanan() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("spec", "BENGKEL");
                args.put("status", "AKTIF");
                args.put("modelKendaraan", data.get("model").asString());
                args.put("varianKendaraan", data.get("varian").asString());
                args.put("jenisKendaraan", data.get("jenisKendaraan").asString());
                args.put("kendaraan", data.get("kendaraan").asString());
                args.put("pekerjaaan", data.get("pekerjaan").asString());
                args.put("kendaraanID", String.valueOf(kendaraanID));

                //get AFS HISTORY
                args.put("layanan", "AFS HISTORY");
                args.put("nopol", data.get("NOPOL").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
                result = result.get("data");

                if (result.asArray().size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        layananAFSHistory.add(result.get(i).get("NAMA_LAYANAN").asString());
                    }
                }

                args.remove("layanan");
                args.put("layanan", "CHECKIN");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
                dataLayananList.asArray().addAll(result.get("data").asArray());
                Log.d(TAG, "List Layanan Data : " + dataLayananList);
                layananArray.add("--PILIH--");
                for (int i = 0; i < dataLayananList.size(); i++) {
                    layananArray.add(dataLayananList.get(i).get("NAMA_LAYANAN").asString());
                    if (dataLayananList.get(i).get("JENIS_LAYANAN").asString().equals("AFTER SALES SERVIS")) {
                        layananAFS.add(dataLayananList.get(i).get("NAMA_LAYANAN").asString());
                    }
                }

                if (data.get("availHistory").asBoolean() && !data.get("km").asString().isEmpty()) {
                    layananArray.add("GARANSI LAYANAN");
                }
                layananArray.add("PERAWATAN LAINNYA");
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, layananArray.asArray()) {
                        @Override
                        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = null;
                            //AFS VALIDATION
                            if (layananAFS.size() > 0) {
                                for (int i = 0; i < layananAFS.size(); i++) {
                                    if (layananAFS.get(i).asString().equals(layananArray.get(position).asString())) {
                                        if (!parseTanggalBeliKendaraan(data.get("tahunProduksi"), false) ||
                                                data.get("km").asString().isEmpty()) {
                                            TextView mTextView = new TextView(getContext());
                                            mTextView.setVisibility(View.GONE);
                                            mTextView.setHeight(0);
                                            view = mTextView;
                                            break;
                                        } else {
                                            view = super.getDropDownView(position, null, parent);
                                        }
                                    } else {
                                        view = super.getDropDownView(position, null, parent);
                                    }
                                }

                                if (layananAFSHistory.size() > 0) {
                                    for (int i = 0; i < layananAFSHistory.size(); i++) {
                                        if (layananAFSHistory.get(i).asString().equals(layananArray.get(position).asString())) {
                                            TextView mTextView = new TextView(getContext());
                                            mTextView.setVisibility(View.GONE);
                                            mTextView.setHeight(0);
                                            view = mTextView;
                                            break;
                                        }
                                    }
                                }

                                return view;
                            } else {
                                view = super.getDropDownView(position, null, parent);
                            }

                            return view;
                        }
                    };
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLayanan.setAdapter(spinnerAdapter);
                } else {
                    showInfo(ERROR_INFO);
                }
            }
        });

        if (layananArray.size() == 2) spLayanan.setSelection(1);

        spLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.equalsIgnoreCase("--PILIH--")) {
                    find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.GONE);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_btnPart_checkin3, LinearLayout.class), false);
                    find(R.id.btn_lanjut_checkin3, Button.class).setEnabled(false);
                } else {
                    find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.VISIBLE);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_btnPart_checkin3, LinearLayout.class), true);
                    find(R.id.btn_lanjut_checkin3, Button.class).setEnabled(true);
                }

                if (item.equals("GARANSI LAYANAN")) {
                    if (data.get("isExpiredKm").asBoolean()) {
                        showWarning("GARANSI LAYANAN EXPIRED, MAX KM : " + data.get("expiredKmVal").asString(), Toast.LENGTH_LONG);
                        spLayanan.setSelection(0);
                        return;
                    } else {
                        if (data.get("isExpiredHari").asBoolean()) {
                            showWarning("GARANSI LAYANAN EXPIRED, MAX HARI : " + data.get("expiredHariVal").asString(), Toast.LENGTH_LONG);
                            spLayanan.setSelection(0);
                            return;
                        }
                    }
                }

                for (int i = 0; i < dataLayananList.size(); i++) {
                    partWajibList.asArray().clear();
                    masterPartList.asArray().clear();
                    pointLayananList.asArray().clear();

                    if (dataLayananList.get(i).get("NAMA_LAYANAN").asString().equalsIgnoreCase(item)) {
                        int batasanNonPaketKm = dataLayananList.get(i).get("BATASAN_NON_PAKET_KM").asInteger();
                        int batasanNonPaketBulan = dataLayananList.get(i).get("BATASAN_NON_PAKET_BULAN").asInteger();

                        if (dataLayananList.get(i).get("JENIS_LAYANAN").asString().equals("AFTER SALES SERVIS") &&
                                (data.get("km").asInteger() > batasanNonPaketKm && data.get("km").asInteger() > 0)) {
                            showWarning(dataLayananList.get(i).get("NAMA_LAYANAN").asString() +
                                    " EXPIRED, MAX KM :" + batasanNonPaketKm + ", MAX BULAN : " + batasanNonPaketBulan, Toast.LENGTH_LONG);
                            spLayanan.setSelection(0);
                            return;
                        } else if (dataLayananList.get(i).get("JENIS_LAYANAN").asString().equals("AFTER SALES SERVIS") &&
                                parseTanggalBeliKendaraan(data.get("tanggalBeli"), true)) {
                            showWarning(dataLayananList.get(i).get("NAMA_LAYANAN").asString() +
                                    " EXPIRED, MAX KM :" + batasanNonPaketKm + ", MAX BULAN : " + batasanNonPaketBulan, Toast.LENGTH_LONG);
                            spLayanan.setSelection(0);
                            return;
                        }

                        if (dataLayananList.get(i).get("BIAYA_PAKET").asString().matches(".*\\d.*")) {
                            biayaLayanan = dataLayananList.get(i).get("BIAYA_PAKET").asString();
                        } else {
                            biayaLayanan = dataLayananList.get(i).get("BIAYA_NON_PAKET").asString();
                        }

                        if (totalHarga > 0) {
                            totalHarga = totalHarga - Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString()));
                        }

                        discLayanan = dataLayananList.get(i).get("DISCOUNT_LAYANAN").asDouble();
                        totalHarga = Math.max(totalHarga, 0) + Integer.parseInt(formatOnlyNumber(biayaLayanan));
                        if (discLayanan > 0) {
                            discLayanan = (int) NumberFormatUtils.calculatePercentage(discLayanan, totalHarga);
                            totalHarga -= discLayanan;
                        }
                        find(R.id.et_totalBiaya_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalHarga)));
                        find(R.id.tv_biayaLayanan_checkin, TextView.class).setText("Rp." + formatRp(biayaLayanan));

                        feeNonPaket = dataLayananList.get(i).get("FEE_NON_PAKET").asInteger();
                        layananId = dataLayananList.get(i).get("LAYANAN_ID").asString();
                        jenisLayanan = dataLayananList.get(i).get("JENIS_LAYANAN").asString();
                        waktuLayanan = totalWaktuKerja(
                                dataLayananList.get(i).get("WAKTU_LAYANAN_HARI").asString(),
                                dataLayananList.get(i).get("WAKTU_LAYANAN_JAM").asString(),
                                dataLayananList.get(i).get("WAKTU_LAYANAN_MENIT").asString());
                        waktuMekanik = totalWaktuKerja(
                                dataLayananList.get(i).get("WAKTU_MEKANIK_HARI").asString(),
                                dataLayananList.get(i).get("WAKTU_MEKANIK_JAM").asString(),
                                dataLayananList.get(i).get("WAKTU_MEKANIK_MENIT").asString());
                        waktuInspeksi = totalWaktuKerja(
                                dataLayananList.get(i).get("WAKTU_INSPEKSI_HARI").asString(),
                                dataLayananList.get(i).get("WAKTU_INSPEKSI_JAM").asString(),
                                dataLayananList.get(i).get("WAKTU_INSPEKSI_MENIT").asString());

                        dummyTime.add(Tools.TimePart.parse(waktuLayanan));
                        //timePartsList.add(Tools.TimePart.parse(waktuLayanan));
                        //dummyTime.add(Tools.TimePart.parse(waktuMekanik)).add(Tools.TimePart.parse(waktuInspeksi));
                        find(R.id.tv_waktu_layanan, TextView.class).setText("Total Waktu Layanan : " + dummyTime.toString());
                        find(R.id.tv_jenis_antrian, TextView.class).setText(validasiAntrian(isPartKosong));

                        if (dataLayananList.get(i).get("LAYANAN_PART").size() > 0) {
                            partWajibList.asArray().addAll(dataLayananList.get(i).get("LAYANAN_PART").asArray());
                        }
                        if (jasaGaransiList.get(i).get("LAYANAN_GARANSI").size() > 0) {
                            jasaGaransiList.asArray().addAll(dataLayananList.get(i).get("LAYANAN_GARANSI").asArray());
                        }
                        if (dataLayananList.get(i).get("LAYANAN_JASA").size() > 0) {
                            pointLayananList.asArray().addAll(dataLayananList.get(i).get("LAYANAN_JASA").asArray());
                        }
                        find(R.id.cardView_namaLayanan, CardView.class).setVisibility(View.VISIBLE);
                        try {
                            if (!Tools.isNumeric(dataLayananList.get(i).get("BIAYA_PAKET").asString())) {
                                find(R.id.tv_biayaLayanan_checkin, TextView.class).setText("");
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "BiayaLayanan: " + e.getMessage());
                        }
                        find(R.id.tv_namaLayanan_checkin, TextView.class).setText(dataLayananList.get(i).get("NAMA_LAYANAN").asString());
                        lokasiLayananList.add(Nson.newObject().set("EMG", dataLayananList.get(i).get("LOKASI_LAYANAN_EMG"))
                                .set("HOME", dataLayananList.get(i).get("LOKASI_LAYANAN_HOME"))
                                .set("TENDA", dataLayananList.get(i).get("LOKASI_LAYANAN_TENDA"))
                                .set("TENDA", dataLayananList.get(i).get("LOKASI_LAYANAN_BENGKEL")));

                        batasanKm = dataLayananList.get(i).get("BATASAN_NON_PAKET_KM").asInteger();
                        batasanBulan = dataLayananList.get(i).get("BATASAN_NON_PAKET_BULAN").asInteger();
                        break;
                    } else {
                        totalHarga = 0;
                        biayaLayanan = "0";
                        discLayanan = 0;
                        layananId = item;
                        find(R.id.tv_waktu_layanan, TextView.class).setText("Total Waktu Layanan : " + "00:00:00");
                        find(R.id.tv_namaLayanan_checkin, TextView.class).setText(item);
                        find(R.id.tv_biayaLayanan_checkin, TextView.class).setText("");
                        find(R.id.et_totalBiaya_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalHarga)));
                    }
                }

                find(R.id.btn_point_layanan).setEnabled(pointLayananList.size() > 0);

                if (discLayanan > 0) {
                    showInfoDialog("DISCOUNT LAYANAN", "DISCOUNT LAYANAN Rp. " + NumberFormatUtils.formatRp(String.valueOf((int) discLayanan)), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                }
                if (partWajibList.size() > 0) {
                    for (int i = 0; i < partWajibList.size(); i++) {
                        masterPartList.asArray().addAll(partWajibList.get(i).get("PARTS").asArray());
                        hargaPartLayanan = partWajibList.get(i).get("HARGA").asInteger();
                        partIdList.add(partWajibList.get(i).get("PART_ID"));
                        discFasilitas = partWajibList.get(i).get("DISC_FASILITAS").asString();
                        biayaPergantian = partWajibList.get(i).get("PERGANTIAN").asString();
                        jumlahPartWajib += partWajibList.get(i).get("JUMLAH").asInteger();
                        partWajibList.get(i).remove("PARTS");
                        jumlahList.add(partWajibList.get(i).get("JUMLAH").asInteger());
                    }

                    if (masterPartList.size() > 0) {
                        for (int i = 0; i < masterPartList.size(); i++) {
                            partIdList.add(masterPartList.get(i).get("PART_ID"));
                        }
                        flagMasterPart = true;
                    }

                    Log.d(TAG, "PART_ID : " + partIdList);
                    Log.d(TAG, "PART_WAJIB : " + partWajibList);
                    Log.d(TAG, "MASTER_PART : " + masterPartList);

                    initPartWajibLayanan();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean parseTanggalBeliKendaraan(Nson data, boolean isBatasan) {
        if (data.asString().isEmpty())
            return false;

        long nowYearTimeMilis = 0;
        long tanggalBeliTimeMilis = 0;
        long bulanBeliTimeMilis = 0;
        long nowMonthTimeMilies = 0;
        int minYearAFS = Calendar.getInstance().get(Calendar.YEAR) - 4;

        try {
            if (isBatasan) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
                Date now = sdfMonth.parse(currentDateTime("yyyy-MM"));
                nowMonthTimeMilies = now.getTime();
                String subsBulan = data.asString().substring(0, 7);
                Date bulanBeli = sdfMonth.parse(subsBulan);
                bulanBeliTimeMilis = bulanBeli.getTime();

                if (nowMonthTimeMilies <= bulanBeliTimeMilis) {
                    return true;
                }
            } else {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
                Date now = sdfYear.parse(String.valueOf(minYearAFS));
                nowYearTimeMilis = now.getTime();
                //String subsTahun = data.asString().substring(0, 4);
                Date tglBeli = sdfYear.parse(data.asString());
                tanggalBeliTimeMilis = tglBeli.getTime();

                if (nowYearTimeMilis <= tanggalBeliTimeMilis) {
                    return true;
                }
            }

        } catch (ParseException e) {
            Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }

        return false;
    }

    @SuppressLint("InflateParams")
    private void initPartWajibLayanan() {
        if (partWajibList.size() > 0 || masterPartList.size() > 0) {
            flagPartWajib = true;
            isPartWajib = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.activity_list_basic, null);
            builder.setView(dialogView);
            alertDialog = builder.create();

            SwipeRefreshLayout swipeRefreshLayout = dialogView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setEnabled(false);
            initToolbarPartWajib(dialogView);
            initRecylerviewPartWajib();
            rvPartWajib.getAdapter().notifyDataSetChanged();

            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertDialog = builder.show();
        } else {
            showError("PART WAJIB NULL");
        }
    }

    @Override
    public void onBackPressed() {
        showInfoDialog("KONFIRMASI", "Kembali Ke Halaman Sebelumnya?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Checkin3_Activity.super.onBackPressed();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

    }

    @SuppressLint("SimpleDateFormat")
    private String validasiAntrian(boolean isHplusPartKosong) {
        String jenisAntrian = "";
        String totalLayanan = find(R.id.tv_waktu_layanan, TextView.class).getText().toString().replace("Total Waktu Layanan : ", "");
        String r = getSetting("MAX_ANTRIAN_EXPRESS_MENIT");
        String s = getSetting("MAX_ANTRIAN_STANDART_MENIT");

        Tools.TimePart maxAntrianExpress = Tools.TimePart.parse(getSetting("MAX_ANTRIAN_EXPRESS_MENIT"));
        Tools.TimePart maxAntrianStandard = Tools.TimePart.parse(getSetting("MAX_ANTRIAN_STANDART_MENIT"));
        Tools.TimePart totalLamaLayanan = Tools.TimePart.parse(find(R.id.tv_waktu_layanan, TextView.class).getText().toString().replace("Total Waktu Layanan : ", ""));
        try {
            Date maxExpress = new SimpleDateFormat("HH:mm:ss").parse(maxAntrianExpress.toString());
            Date maxStandard = new SimpleDateFormat("HH:mm:ss").parse(maxAntrianStandard.toString());
            Date totalAntrian = new SimpleDateFormat("HH:mm:ss").parse(totalLamaLayanan.toString());
            int hplus = Integer.parseInt(totalLayanan.substring(0, 2));

            long express = maxExpress != null ? maxExpress.getTime() : 0;
            long standard = maxStandard != null ? maxStandard.getTime() : 0;
            long totalWaktu = totalAntrian != null ? totalAntrian.getTime() : 0;

            if (isHplusPartKosong) {
                jenisAntrian = "H+";
            } else {
                if (totalWaktu <= express) {
                    jenisAntrian = "EXPRESS";
                } else if (totalWaktu <= standard) {
                    jenisAntrian = "STANDART";
                } else if (hplus > 0) {
                    jenisAntrian = "H+";
                } else {
                    jenisAntrian = "EXTRA";
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return jenisAntrian;
    }

    private void showDialogPart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_konfirmasi_part_jasa, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        isKonfirmasi = true;
        initToolbarKonfirmasi(dialogView);
        initRecylerViewPart();
        initRecylerviewJasaLain();

        TextView tvTittle = dialogView.findViewById(R.id.tv_tittle_konfirmasi);
        LinearLayout lyPart = dialogView.findViewById(R.id.ly_part);
        LinearLayout lyJasaLain = dialogView.findViewById(R.id.ly_jasa_lain);
        Button btnLanjut = dialogView.findViewById(R.id.btn_simpan);

        tvTittle.setText("*KURANGI PART / JASA YG BERLEBIH DENGAN TOMBOL X");
        lyPart.setVisibility(partList.size() == 0 ? View.GONE : View.VISIBLE);
        lyJasaLain.setVisibility(jasaList.size() == 0 ? View.GONE : View.VISIBLE);
        btnLanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {
                    setSelanjutnya("LAYANAN ESTIMASI", "");
                } else if (find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).isChecked()) {
                    setSelanjutnya("TUNGGU KONFIRMASI", "");
                } else {
                    setSelanjutnya("CHECKIN 3", "");
                }
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isKonfirmasi = false;
                rvPart = findViewById(R.id.recyclerView_part_checkin3);
                rvJasaLain = findViewById(R.id.recyclerView_jasalain_checkin3);
                initRecylerViewPart();
                initRecylerviewJasaLain();
                Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.btn_jasaLain_checkin3:
                isPartJasaUsulan = false;

                i = new Intent(getActivity(), JasaLain_Activity.class);
                if (jasaList.size() > 0) {
                    i.putExtra(JASA_LAIN, jasaList.toJson());
                }
                i.putExtra("KENDARAAN_ID", kendaraanID);
                i.putExtra("KM", kmKendaraan);
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_sparePart_checkin3:
                isPartKosong = false;
                flagPartWajib = false;
                isPartJasaUsulan = false;

                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_LOKASI, RUANG_PART);
                i.putExtra("NAMA_LAYANAN", namaLayanan);
                i.putExtra("LAYANAN_ID", layananId);
                i.putExtra("KENDARAAN_ID", kendaraanID);
                i.putExtra("PEKERJAAN", pekerjaan);
                i.putExtra("PART_ID", partId);
                i.putExtra("JUMLAH", jumlahPart);
                i.putExtra("CHECKIN", true);
                startActivityForResult(i, REQUEST_CARI_PART);
                break;
            case R.id.btn_jasaLainBerkala_checkin3:
                i = new Intent(getActivity(), JasaLainBerkala_Activity.class);
                startActivityForResult(i, REQUEST_JASA_BERKALA);
                break;
            case R.id.btn_partBerkala_checkin3:
                i = new Intent(getActivity(), PartBerkala_Activity.class);
                startActivityForResult(i, REQUEST_PART_BERKALA);
                break;
            case R.id.btn_partExternal_checkin3:
                isPartJasaUsulan = false;
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_OTOMOTIVES, "");
                startActivityForResult(i, REQUEST_PART_EXTERNAL);
                break;
            case R.id.btn_lanjut_checkin3:
                if (jasaList.asArray().isEmpty() && partList.asArray().isEmpty() && spLayanan.getSelectedItem().toString().equals("PERAWATAN LAINNYA")) {
                    showWarning("PART - JASA HARUS DI MASUKKAN", Toast.LENGTH_LONG);
                } else {
                    if(jasaList.asArray().isEmpty() || partList.asArray().isEmpty()){
                        setSelanjutnya("CHECKIN 3", "");
                    }else{
                        showDialogPart();
                    }
                }
                break;
            case R.id.btn_batal_checkin3:
                //batal
                showInfo("Layanan Di Batalkan Pelanggan, Silahkan Isi Field Keterangan Tambahan");
                i = new Intent(getActivity(), Checkin4_Activity.class);
                i.putExtra(BATAL, "");
                startActivityForResult(i, REQUEST_BATAL);
                break;
            case R.id.btn_usulan_mekanik:
                initDialogRekomendasiMekanik();
                break;
            case R.id.btn_point_layanan:
                initDialogPointLayanan();
                break;
        }
    }

    private void initToolbarHistory(View dialogView, boolean isUsulan) {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (isUsulan)
            Objects.requireNonNull(getSupportActionBar()).setTitle("Rekomendasi Mekanik");
        else
            Objects.requireNonNull(getSupportActionBar()).setTitle("Point Layanan");

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initDialogPointLayanan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_history_part_jasa, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        try {
            dialogView.findViewById(R.id.ly_part).setVisibility(View.GONE);
            initToolbarHistory(dialogView, false);
            initRecyclerviewPointLayanan(dialogView);
            Objects.requireNonNull(rvPointLayanan.getAdapter()).notifyDataSetChanged();
        } catch (Exception e) {
            showError(e.getMessage());
        }

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        alertDialog.show();
    }


    private void initDialogRekomendasiMekanik() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_history_part_jasa, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        initToolbarHistory(dialogView, true);
        initRvJasaRekomendasi(dialogView);
        initRvPartRekomendasi(dialogView);

        if (rekomendasiPartList.size() == 0)
            dialogView.findViewById(R.id.ly_part).setVisibility(View.GONE);
        else
            Objects.requireNonNull(rvHistoryPart.getAdapter()).notifyDataSetChanged();

        if (rekomendasiJasaList.get("JASA_LAIN").size() == 0)
            dialogView.findViewById(R.id.ly_jasa).setVisibility(View.GONE);
        else
            Objects.requireNonNull(rvHistoryJasa.getAdapter()).notifyDataSetChanged();

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        alertDialog.show();
    }

    private void initRecyclerviewPointLayanan(View view) {
        rvPointLayanan = view.findViewById(R.id.rv_jasa);
        rvPointLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvPointLayanan.setHasFixedSize(true);
        rvPointLayanan.setAdapter(new NikitaRecyclerAdapter(pointLayananList, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText("" + (position + 1));

                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(pointLayananList.get(position).get("KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(pointLayananList.get(position).get("AKTIVITAS").asString());
                if (pointLayananList.get(position).get("HARGA_JASA_LAIN") == null) {
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class).setVisibility(View.GONE);
                } else {
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                            .setText(RP + formatRp(pointLayananList.get(position).get("HARGA_JASA_LAIN").asString()));
                }
            }
        });
    }


    private void initRvPartRekomendasi(View dialogView) {
        rvHistoryPart = dialogView.findViewById(R.id.rv_part);
        rvHistoryPart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvHistoryPart.setHasFixedSize(false);
        rvHistoryPart.setAdapter(new NikitaRecyclerAdapter(rekomendasiPartList, R.layout.item_part_booking3_checkin3) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);
                viewHolder.find(R.id.img_delete).setVisibility(View.GONE);
                viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                viewHolder.find(R.id.tv_no, TextView.class).setText("" + (position + 1));

                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(rekomendasiPartList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(rekomendasiPartList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(rekomendasiPartList.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class)
                        .setText(rekomendasiPartList.get(position).get("HARGA_JASA_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class)
                        .setText(rekomendasiPartList.get(position).get("HARGA_PART").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                isPartJasaUsulan = true;
                Intent i = new Intent(getActivity(), JumlahPart_Checkin_Activity.class);
                i.putExtra("KM", kmKendaraan);
                i.putExtra(DATA, parent.get(position).toJson());
                i.putExtra("bengkel", "");
                startActivityForResult(i, REQUEST_HARGA_PART);
            }
        }));

    }

    private void initRvJasaRekomendasi(View dialogView) {
        rvHistoryJasa = dialogView.findViewById(R.id.rv_jasa);
        rvHistoryJasa.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvHistoryJasa.setHasFixedSize(false);
        rvHistoryJasa.setAdapter(new NikitaRecyclerAdapter(rekomendasiJasaList.get("JASA_LAIN"), R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.img_delete, ImageButton.class).setVisibility(View.GONE);
                viewHolder.find(R.id.view_mark_tambah_jasa).setVisibility(View.GONE);

                viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                        .setText(rekomendasiJasaList.get(position).get("NAMA_KELOMPOK_PART").asString());
                viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                        .setText(rekomendasiJasaList.get(position).get("AKTIVITAS").asString());
                viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                        .setText("Rp. " + formatRp(rekomendasiJasaList.get(position).get("HARGA_JASA_LAIN").asString()));

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                isPartJasaUsulan = true;
                Intent intent = new Intent(getActivity(), BiayaJasa_Activity.class);
                intent.putExtra("IS_USULAN_MEKANIK", false);
                intent.putExtra("KM", kmKendaraan);
                intent.putExtra(DATA, parent.get(position).toJson());
                intent.putExtra(JASA_LAIN, Nson.readJson(getIntentStringExtra(JASA_LAIN)).toJson());
                startActivityForResult(intent, REQUEST_JASA_LAIN);
            }
        }));
    }

    private void getPartJasaHistory() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[4];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                args[1] = "nopol=" + nopol;
                args[2] = "pekerjaan=" + pekerjaan;
                args[3] = "kendaraanID=" + kendaraanID;

                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_HISTORY_USULAN_MEKANIK), args));
                rekomendasiJasaList.asArray().addAll(result.get("data").get("JASA_LAIN").asArray());
                rekomendasiPartList.asArray().addAll(result.get("data").get("PART").asArray());

            }

            @Override
            public void runUI() {
                if (rekomendasiJasaList.size() == 0 && rekomendasiPartList.size() == 0) {
                    find(R.id.btn_usulan_mekanik).setEnabled(false);
                }
            }
        });
    }

    private void setSelanjutnya(final String status, final String alasanBatal) {
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        final String layanan = spLayanan.getSelectedItem().toString();
        final String layananEstimasi = find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked() ? "Y" : "N";
        final String total = formatOnlyNumber(find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString());
        final String dp = formatOnlyNumber(find(R.id.et_dp_checkin3, EditText.class).getText().toString());
        final String sisa = formatOnlyNumber(find(R.id.et_sisa_checkin3, EditText.class).getText().toString());
        final String tungguKonfirmasi = find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).isChecked() ? "Y" : "N";
        final int totalPartJasa = jasaList.size() + partList.size();
        final String waktuLayanan = find(R.id.tv_waktu_layanan, TextView.class).getText().toString().replace("Total Waktu Layanan : ", "");
        final String noPonsel = nson.get("noPonsel").asString();
        final String nopol = nson.get("nopol").asString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                isSelanjutnya = true;
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jenisKendaraan", nson.get("jenisKendaraan").asString());
                args.put("keluhanList", keluhanList.toJson());
                args.put("jenisCheckin", "3");
                args.put("id", nson.get("CHECKIN_ID").asString());
                args.put("status", status);
                args.put("alasanBatal", alasanBatal);
                args.put("layanan", layanan);
                args.put("layananestimasi", layananEstimasi);
                args.put("total", total);
                args.put("dp", dp);
                args.put("sisa", sisa);
                args.put("tunggu", tungguKonfirmasi);
                //jasaList = Tools.removeDuplicates(jasaList);
                //partList = Tools.removeDuplicates(partList);
                args.put("partbook", partList.toJson());
                args.put("jasabook", jasaList.toJson());
                args.put("antrian", find(R.id.tv_jenis_antrian, TextView.class).getText().toString());
                //args.put("biayaLayanan", formatOnlyNumber(find(R.id.tv_biayaLayanan_checkin, TextView.class).getText().toString()));
                //inserting waktu layanan part
                args.put("biayaLayanan", formatOnlyNumber(biayaLayanan));
                args.put("layananId", formatOnlyNumber(layananId));
                args.put("waktuLayananHari", waktuLayanan.substring(0, 2));
                args.put("waktuLayananJam", waktuLayanan.substring(3, 5));
                args.put("waktuLayananMenit", waktuLayanan.substring(6, 8));
                args.put("feeNonPaket", String.valueOf(feeNonPaket));
                args.put("noPonsel", noPonsel);//for message queue
                args.put("nopol", formatNopol(nopol));
                args.put("discountLayanan", String.valueOf(discLayanan));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(alertDialog != null)
                        alertDialog.dismiss();

                    Intent intent = new Intent(getActivity(), KontrolLayanan_Activity.class);
                    intent.putExtra("NOPOL", nopol);
                    if (status.equalsIgnoreCase("LAYANAN ESTIMASI")) {
                        result = result.get("data").get(0);
                        showSuccess("ESTIMASI BIAYA PELAYANAN  KENDARAAN " + nson.get("kendaraanPelanggan").asString());
                        //showMessageInvalidNotif(getActivity(), result.get("data").get("MESSAGE_INFO").asString(), null);
                        showNotification(getActivity(), "Checkin Layanan Estimasi", formatNopol(nopol), "CHECKIN", intent);
                        setResult(RESULT_OK);
                        finish();
                    } else if (status.equalsIgnoreCase("TUNGGU KONFIRMASI")) {
                        showSuccess("Layanan Di tambahkan Ke Daftar Kontrol Layanan");
                        setResult(RESULT_OK);
                        finish();
                    } else if (status.equalsIgnoreCase("BATAL CHECKIN")) {
                        showSuccess("Layanan Di Batalkan Pelanggan, Data Di Masukkan Ke Kontrol Layanan");
                        showNotification(getActivity(), "Checkin di Batalkan", formatNopol(nopol), "CHECKIN", intent);
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        nson.set("TOTAL", find(R.id.et_totalBiaya_checkin3, EditText.class).getText().toString());
                        nson.set("WAKTU_LAYANAN", waktuLayanan);
                        nson.set("LOKASI_LAYANAN", lokasiLayananList.toJson());
                        nson.set("PART_KOSONG", isPartKosong);
                        nson.set("OUTSOURCE", isOutsource);
                        nson.set("JENIS_ANTRIAN", find(R.id.tv_jenis_antrian, TextView.class).getText().toString());
                        nson.set("DP", formatOnlyNumber(find(R.id.et_dp_checkin3, EditText.class).getText().toString()));
                        nson.set("SISA", formatOnlyNumber(find(R.id.et_sisa_checkin3, EditText.class).getText().toString()));
                        nson.set("WAKTU_PESAN", waktuPesan);
                        nson.set("noponsel", noPonsel);
                        nson.set("nopol", nopol);

                        if (layanan.equals("PERAWATAN LAINNYA")) {
                            nson.set("JENIS_LAYANAN", layanan);
                        } else {
                            nson.set("JENIS_LAYANAN", jenisLayanan);
                        }
                        Intent i = new Intent(getActivity(), Checkin4_Activity.class);
                        i.putExtra(DATA, nson.toJson());
                        i.putExtra("PART_LIST", partList.toJson());
                        i.putExtra("JASA_LIST", jasaList.toJson());
                        startActivityForResult(i, REQUEST_CHECKIN);
                    }
                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void rollbackCheckin() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "ROLLBACK CHECKIN");
                args.put("checkinID", String.valueOf(checkinID));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ROLLBACK_TRANSACTIONS), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equals("OK")) {
                    showSuccess("TRANSAKSI BERHASIL DI BATALKAN");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("TRANSAKSI ERROR, SILAHKAN HUBUNGI ADMINISTRATOR", Toast.LENGTH_LONG);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i;
        Nson dataAccept;
        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_JASA_LAIN:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        if (isPartJasaUsulan) {
                            for (int j = 0; j < rekomendasiJasaList.size(); j++) {
                                if (rekomendasiJasaList.get(j).get("JASA_LAIN_ID").asString().equals(dataAccept.get("JASA_ID").asString())) {
                                    rekomendasiJasaList.remove(j);
                                    break;
                                }
                            }
                            Objects.requireNonNull(rvHistoryPart.getAdapter()).notifyDataSetChanged();
                        }

                        Log.d(TAG, "JASA : " + dataAccept);
                        isOutsource = dataAccept.get("OUTSOURCE").asString();
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));

                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("NET").asString()));
                        jasaList.add(dataAccept);
                        partJasaList.add(dataAccept);
                        Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_JASA_BERKALA:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        jasaList.add(dataAccept);
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                        Objects.requireNonNull(rvJasaLain.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_CARI_PART:
                        i = new Intent(getActivity(), JumlahPart_Checkin_Activity.class);
                        i.putExtra("KM", kmKendaraan);
                        i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
                        i.putExtra("bengkel", "");
                        //Log.d("JUMLAH_HARGA_PART", "INTENT : "   + Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                        startActivityForResult(i, REQUEST_HARGA_PART);
                        break;
                    case REQUEST_PART_BERKALA:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_PART").asString()));
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_PART_EXTERNAL:
                        i = new Intent(getActivity(), JasaExternal_Activity.class);
                        i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
                        startActivityForResult(i, REQUEST_JASA_EXTERNAL);
                        break;
                    case REQUEST_JASA_EXTERNAL:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        isJasaExternal = true;
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                        totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("HARGA_JASA").asString()));
                        partList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_HARGA_PART:
                        dataAccept = Nson.readJson(getIntentStringExtra(data, DATA));
                        partId = dataAccept.get("PART_ID").asInteger();
                        jumlahPart = dataAccept.get("JUMLAH").asInteger();

                        try {
                            totalHarga += Integer.parseInt(formatOnlyNumber(dataAccept.get("NET").asString()));
                            if (data != null && data.getStringExtra("PART_KOSONG").equals("Y")) {
                                isPartKosong = true;
                                totalDp += Integer.parseInt(formatOnlyNumber(dataAccept.get("DP").asString()));
                                sisaDp = totalHarga - totalDp;
                                setDpAndSisa();
                            }
                        } catch (Exception e) {
                            showError(e.getMessage());
                        }

                        if (flagPartWajib) {
                            if (data != null && data.hasExtra("PART_KOSONG_PART_WAJIB")) {
                                showWarning("Part Wajib Layanan Tidak Tersedia");
                                finish();
                                return;
                            }
                            jumlahPartWajib--;
                            for (int x = 0; x < masterPartList.size(); x++) {
                                if (masterPartList.get(x).get("PART_ID").asString().equals(dataAccept.get("PART_ID").asString())) {
                                    isDelete = true;
                                    masterPartList.remove(x);
                                    break;
                                }
                            }
                            rvPartWajib.getAdapter().notifyDataSetChanged();
                            if (jumlahPartWajib == 0) {
                                flagPartWajib = false;
                                alertDialog.dismiss();
                            }
                        } else {
                            if (!dataAccept.get("WAKTU_PESAN").asString().isEmpty()) {
                                waktuPesan += dataAccept.get("WAKTU_PESAN").asInteger();
                                String hariWaktuPesan = dataAccept.get("WAKTU_PESAN").asString() + ":00:" + "00";
                                totalTambahWaktuLayanan(Tools.TimePart.parse(hariWaktuPesan));
                            }
                        }
                        if (isPartJasaUsulan) {
                            for (int j = 0; j < rekomendasiPartList.size(); j++) {
                                if (rekomendasiPartList.get(j).get("PART_ID").asString().equals(dataAccept.get("PART_ID").asString())) {
                                    rekomendasiPartList.remove(j);
                                    break;
                                }
                            }
                            Objects.requireNonNull(rvHistoryPart.getAdapter()).notifyDataSetChanged();
                        }
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_KERJA").asString()));
                        totalTambahWaktuLayanan(Tools.TimePart.parse(dataAccept.get("WAKTU_INSPEKSI").asString()));
                        partList.add(dataAccept);
                        partJasaList.add(dataAccept);
                        Objects.requireNonNull(rvPart.getAdapter()).notifyDataSetChanged();
                        break;
                    case REQUEST_CHECKIN:
                        setResult(RESULT_OK);
                        finish();
                        break;
                    case REQUEST_BATAL:
                        isBatal = true;
                        setSelanjutnya("BATAL CHECKIN", getIntentStringExtra(data, "alasanBatal"));
                        break;
                    case REQUEST_LAYANAN:
                        setSpNamaLayanan();
                        break;
                }
                find(R.id.et_totalBiaya_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalHarga)));
            }
        } catch (Exception e) {
            showError(e.getMessage());
            Log.d(TAG, "onActivityResult: " + e.getMessage() + e.getCause());
        }
    }

    @SuppressLint("SetTextI18n")
    private void totalTambahWaktuLayanan(Tools.TimePart waktuLayanan) {
        dummyTime.add(waktuLayanan);
        find(R.id.tv_waktu_layanan, TextView.class).setText("Total Waktu Layanan : " + dummyTime.toString());
        Log.d(TAG, "TOTAL WAKTU : " + dummyTime.toString());
        find(R.id.tv_jenis_antrian, TextView.class).setText(validasiAntrian(isPartKosong));
    }

    @SuppressLint("SetTextI18n")
    private void totalKurangWaktuLayanan(Tools.TimePart waktuLayanan) {
        dummyTime.subtraction(waktuLayanan);
        find(R.id.tv_waktu_layanan, TextView.class).setText("Total Waktu Layanan : " + dummyTime.toString());
        Log.d(TAG, "TOTAL WAKTU : " + dummyTime.toString());
        find(R.id.tv_jenis_antrian, TextView.class).setText(validasiAntrian(isPartKosong));
    }


    @SuppressLint("SetTextI18n")
    private void setDpAndSisa() {
        find(R.id.tv_dp_bengkel, TextView.class).setVisibility(View.VISIBLE);
        find(R.id.tv_dp_bengkel, TextView.class).setText("DP Bengkel : " + getSetting("DP_PERSEN") + " %");
        find(R.id.et_sisa_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(sisaDp)));
        find(R.id.et_dp_checkin3, EditText.class).setText(RP + formatRp(String.valueOf(totalDp)));
    }
}
