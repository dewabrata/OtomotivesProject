package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.TimePicker_Dialog;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_LAYANAN;
import static com.rkrzmail.utils.APIUrls.GET_BENGKEL_PRINCIPAL;
import static com.rkrzmail.utils.APIUrls.GET_KENDARAAN_LAYANAN;
import static com.rkrzmail.utils.APIUrls.GET_VARIAN_BY_NAMA_LAYANAN;
import static com.rkrzmail.utils.APIUrls.JURNAL_PEMBAYARAN;
import static com.rkrzmail.utils.APIUrls.VIEW_DATA_BENGKEL;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.EDIT;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturLayanan_Activity extends AppActivity {

    private static final int REQUEST_DESKRIPSI = 12;
    public static final int REQUEST_DISC_PART = 13;
    public static final int REQUEST_JASA_LAIN = 14;
    private static final String TAG = "AturLayanan___";

    private Spinner spJenisLayanan, spNamaPrincipal, spNamaLayanan, spStatus;
    private RecyclerView rvLayanan;

    private final Nson layananPaketList = Nson.newArray();
    private final Nson layananOtolist = Nson.newArray();
    private final Nson layananRecallList = Nson.newArray();
    private final Nson layananAfterList = Nson.newArray();
    private final Nson dataLayananList = Nson.newArray();
    private final Nson principalList = Nson.newArray();
    private final Nson discPartList = Nson.newArray();
    private final Nson discJasaList = Nson.newArray();
    private final Nson layananBengkelAvail = Nson.newArray();
    private final Nson dataVarianKendaraanList = Nson.newArray();
    private final Nson dataModelKendaraanList = Nson.newArray();
    private Nson loadVarianByNamaLayanan = Nson.newArray();

    private Nson editNson;

    boolean isUpdate = false;
    String loadVarian = "", varianSelected = "", merkSelected = "", loadMerk = "";
    private String jenisLayanan = "",
            layananId = "",
            namaLayanan = "",
            idPrincipal = "",
            namaPrincipal = "",
            kendaraan = "",
            garansi = "",
            loadNamaLayanan = "";
    private int maxDisc = 0, size = 0;
    private boolean isDiscList,
            isPaket = false,
            isAfterService = false,
            isRecall = false,
            isOtomotives = false, isAdd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_layanan);
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        initToolbar();
        //     getDataLayanan();
        getDataKendaraan();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        editNson = Nson.readJson(getIntentStringExtra(EDIT));
        kendaraan = getSetting("JENIS_KENDARAAN");
        spJenisLayanan = findViewById(R.id.sp_jenis_layanan);
        spNamaLayanan = findViewById(R.id.sp_nama_layanan);
        spStatus = findViewById(R.id.sp_status_layanan);
        spNamaPrincipal = findViewById(R.id.sp_nama_principal);
        rvLayanan = findViewById(R.id.recyclerView_layanan);

        initData();
        initListener();
        initButton();
        initRecylerview();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        List<String> jenisList = Arrays.asList(getResources().getStringArray(R.array.atur_layanan));
        List<String> statusList = Arrays.asList(getResources().getStringArray(R.array.status_layanan));

        if (getIntent().hasExtra(ADD)) {
            isAdd = true;
            Nson data = Nson.readJson(getIntentStringExtra(ADD));
            for (int i = 0; i < data.size(); i++) {
                layananBengkelAvail.add(data.get(i).get("NAMA_LAYANAN").asString());
            }


        } else if (getIntent().hasExtra(EDIT)) {
            isUpdate = true;
            namaPrincipal = editNson.get("PRINCIPAL").asString();
            loadVarian = editNson.get("VARIAN").asString();
            loadMerk = editNson.get("MERK").asString();
            loadNamaLayanan = editNson.get("NAMA_LAYANAN").asString();
            String loadModel = editNson.get("MODEL").asString();

            Tools.setViewAndChildrenEnabled(find(R.id.rl_merk, RelativeLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.rl_varian, RelativeLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.vg_jenis_layanan, LinearLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.vg_nama_principal, LinearLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.vg_nama_layanan, LinearLayout.class), false);

            find(R.id.btn_simpan_atur_layanan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_atur_layanan, Button.class).setEnabled(true);
            find(R.id.ly_biaya_paket, TextInputLayout.class).setEnabled(true);
            find(R.id.ly_disc_booking, TextInputLayout.class).setEnabled(true);


            find(R.id.et_discBooking_layanan, EditText.class).setText(editNson.get("DISCOUNT_BOOKING").asString());
            if (Utility.isNumeric(editNson.get("BIAYA_PAKET").asString())) {
                find(R.id.et_biayaPaket_layanan, EditText.class).setText("Rp. " + NumberFormatUtils.formatRp(editNson.get("BIAYA_PAKET").asString()));
            } else {
                find(R.id.et_biayaPaket_layanan, EditText.class).setText("Rp. " + NumberFormatUtils.formatRp("0"));
            }

            find(R.id.sp_garansi_atur_layanan, Spinner.class).setSelection(Tools.getIndexSpinner(spJenisLayanan, editNson.get("STATUS").asString()));
            find(R.id.et_discBooking_layanan, EditText.class).setText(editNson.get("DISCOUNT_BOOKING").asString());
            find(R.id.et_waktu_kerja_hari, EditText.class).setText(editNson.get("WAKTU_LAYANAN_HARI").asString());
            find(R.id.et_waktu_kerja_jam, EditText.class).setText(editNson.get("WAKTU_LAYANAN_JAM").asString());
            find(R.id.et_waktu_kerja_menit, EditText.class).setText(editNson.get("WAKTU_LAYANAN_MENIT").asString());
            find(R.id.et_garansi_km, EditText.class).setText(editNson.get("MAX_GARANSI_KM").asString());
            find(R.id.et_garansi_hari, EditText.class).setText(editNson.get("MAX_GARANSI_HARI").asString());
            find(R.id.et_keterangan, EditText.class).setText(editNson.get("KETERANGAN_LAYANAN").asString());
        }

        setSpinnerOffline(statusList, find(R.id.sp_garansi_atur_layanan, Spinner.class), editNson.get("GARANSI_BERSAMA").asString());
        setSpinnerOffline(statusList, spStatus, editNson.get("STATUS").asString());
        setSpinnerOffline(jenisList, spJenisLayanan, editNson.get("JENIS_LAYANAN").asString());
        setSpNamaPrincipal(namaPrincipal);
        setSpinnerFromApi(spNamaLayanan, "action", "view", VIEW_LAYANAN, "NAMA_LAYANAN", editNson.get("NAMA_LAYANAN").asString());
        setSpinnerFromApi(spNamaPrincipal, "action", "principal", "databengkel", "NAMA", editNson.get("PRINCIPAL").asString());
        final boolean finalIsUpdate = isUpdate;

        find(R.id.btn_simpan_atur_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spJenisLayanan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("Silahkan Pilih Jenis Layanan");
                    spJenisLayanan.performClick();
                    return;
                }
                if (spNamaLayanan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("Silahkan Pilih Nama Layanan");
                    spNamaLayanan.performClick();
                    return;
                } else if (spNamaLayanan.getSelectedItem().toString().equalsIgnoreCase("AFTER SALES SERVIS") ||
                        spNamaLayanan.getSelectedItem().toString().equalsIgnoreCase("RECALL")) {
                    if (spNamaPrincipal.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spNamaPrincipal.performClick();
                        showWarning("Nama Principal Harus Di Pilih");
                        return;
                    }
                } else if (spNamaLayanan.getSelectedItem().toString().equalsIgnoreCase("PAKET LAYANAN")) {
                    if (find(R.id.et_biayaPaket_layanan, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_biayaPaket_layanan, EditText.class).setError("Biaya Paket Harus Di Isi");
                        return;
                    }
                }

                if (merkSelected.isEmpty()) {
                    showWarning("MERK KENDARAAN HARUS DI PILIH");
                    return;
                }
                if (varianSelected.isEmpty()) {
                    showWarning("VARIAN KENDARAAN HARUS DI PILIH");
                    return;
                }

                if (finalIsUpdate) {
                    updateData(editNson);
                } else {
                    saveData();
                }
            }
        });

    }

    private List<String> removeEmptyString(String[] args) {
        List<String> argsList = new ArrayList<>();
        if (args.length > 0) {
            for (String index : args) {
                if (!index.equals(" ") || !index.isEmpty()) {
                    index = index.replace(",", "");
                    argsList.add(index);
                }
            }
        }
        return argsList;
    }

    private void updateData(final Nson id) {
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("status", spStatus.getSelectedItem().toString());
                args.put("merk", merkSelected);
                args.put("varian", varianSelected);
                args.put("model", null);
                args.put("keterangan", find(R.id.et_keterangan, EditText.class).getText().toString());
                if (!jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    args.put("biaya", "0");
                } else {
                    args.put("biaya", formatOnlyNumber(find(R.id.et_biayaPaket_layanan, EditText.class).getText().toString()));
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Mengupdate Layanan");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void initListener() {
        find(R.id.et_biayaPaket_layanan, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_biayaPaket_layanan, EditText.class)));
        find(R.id.et_discBooking_layanan, EditText.class).addTextChangedListener(new NumberFormatUtils().percentTextWatcher(find(R.id.et_discBooking_layanan, EditText.class)));
        find(R.id.sp_garansi_atur_layanan, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Tools.setViewAndChildrenEnabled(find(R.id.tl_fee, TextInputLayout.class), position != 0 || !item.equalsIgnoreCase("YA"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("TIDAK AKTIF")) {
                    //Tools.setViewAndChildrenEnabled(find(R.id.ly_layanan, LinearLayout.class), false);
                } else {
                    //Tools.setViewAndChildrenEnabled(find(R.id.ly_layanan, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spJenisLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

                jenisLayanan = parent.getItemAtPosition(position).toString();
                Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), jenisLayanan.equals("AFTER SALES SERVIS"));
                Tools.setViewAndChildrenEnabled(find(R.id.vg_nama_principal, LinearLayout.class), jenisLayanan.equals("AFTER SALES SERVIS"));
                //Tools.setViewAndChildrenEnabled(find(R.id.ly_kendaraan, LinearLayout.class), jenisLayanan.equals("PAKET LAYANAN"));

                if (jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    if (!isUpdate) spNamaLayanan.setEnabled(true);
                    find(R.id.sp_garansi_atur_layanan, Spinner.class).setEnabled(true);
                } else if (jenisLayanan.equalsIgnoreCase("OTOMOTIVES")) {
                    if (!isUpdate) spNamaLayanan.setEnabled(true);
                    find(R.id.btn_deskripsi_aturLayanan, Button.class).setEnabled(true);
                } else if (jenisLayanan.equalsIgnoreCase("AFTER SALES SERVIS") || jenisLayanan.equalsIgnoreCase("RECALL")) {
                    find(R.id.btn_deskripsi_aturLayanan, Button.class).setEnabled(true);
                    find(R.id.btn_simpan_atur_layanan, Button.class).setEnabled(true);
                }

                setSpinnerNamaLayanan();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spNamaPrincipal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initButton() {
        find(R.id.btn_deskripsi_aturLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DeskripsiLayanan_Activiy.class);
                i.putExtra("deskripsi", spNamaLayanan.getSelectedItem().toString());
                startActivityForResult(i, REQUEST_DESKRIPSI);
            }
        });

        find(R.id.btn_discPart_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDiscList = true;
                maxDisc++;
                Intent i = new Intent(getActivity(), TambahLayanan.class);
                i.putExtra("disc_part", "disc_part");
                startActivityForResult(i, REQUEST_DISC_PART);
            }
        });

        find(R.id.btn_jasaLain_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDiscList = false;
                maxDisc++;
                Intent i = new Intent(getActivity(), TambahLayanan.class);
                i.putExtra("jasa_lain", "jasa_lain");
                startActivityForResult(i, REQUEST_JASA_LAIN);
            }
        });

        find(R.id.btn_img_waktu_kerja).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWaktuKerja();
            }
        });
    }

    private void initRecylerview() {
        rvLayanan.setHasFixedSize(true);
        rvLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_tambah_layanan) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_namaLayanan_layanan, TextView.class).setText(isDiscList ?
                                nListArray.get(position).get("NAMA_MASTER").asString() : nListArray.get(position).get("KELOMPOK_PART").asString());
                        viewHolder.find(R.id.tv_discAktivitas_layanan, TextView.class).setText(isDiscList ?
                                nListArray.get(position).get("DISC_PART").asString() : nListArray.get(position).get("AKTIVITAS").asString());
                        viewHolder.find(R.id.tv_discWaktuKerja_layanan, TextView.class).setText(isDiscList ?
                                nListArray.get(position).get("DISC_JASA").asString() : nListArray.get(position).get("WAKTU").asString());
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                    }
                })
        );
    }

    private void saveData() {
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
            return;
        }
        final String jenisLayanan = spJenisLayanan.getSelectedItem().toString().toUpperCase();
        final String namaLayanan = spNamaLayanan.getSelectedItem().toString().toUpperCase();
        final String principal = spNamaPrincipal.getSelectedItem().toString().equals("--PILIH--") ? "" : spNamaPrincipal.getSelectedItem().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("layananid", layananId);
                args.put("jenis", jenisLayanan);
                args.put("principal", principal);
                args.put("nama", namaLayanan);
                args.put("keterangan", find(R.id.et_keterangan, EditText.class).getText().toString());
                args.put("discbook", find(R.id.et_discBooking_layanan, EditText.class).getText().toString());
                if (!jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    args.put("biaya", "0");
                } else {
                    args.put("biaya", formatOnlyNumber(find(R.id.et_biayaPaket_layanan, EditText.class).getText().toString()));
                }
                args.put("garansi", find(R.id.sp_garansi_atur_layanan, Spinner.class).getSelectedItem().toString());
                args.put("fgb", formatOnlyNumber(find(R.id.et_feeGB_layanan, EditText.class).getText().toString()));
                args.put("kendaraan", kendaraan);
                args.put("merk", merkSelected);
                args.put("model", null);
                args.put("varian", varianSelected);
                args.put("tanggal", currentDateTime());
                if (discPartList.size() > 0) {
                    args.put("ganti", discPartList.toJson());
                } else {
                    args.put("ganti", "");
                }
                if (discJasaList.size() > 0) {
                    args.put("jasa", discJasaList.toJson());
                } else {
                    args.put("jasa", "");
                }
                args.put("jenis_kendaraan", kendaraan);
                args.put("waktuLayananHari", find(R.id.et_waktu_kerja_hari, EditText.class).getText().toString());
                args.put("waktuLayananJam", find(R.id.et_waktu_kerja_jam, EditText.class).getText().toString());
                args.put("waktuLayananMenit", find(R.id.et_waktu_kerja_menit, EditText.class).getText().toString());
                args.put("maxGaransiKM", find(R.id.et_garansi_km, EditText.class).getText().toString());
                args.put("maxGaransiHari", find(R.id.et_garansi_hari, EditText.class).getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menambahkan Layanan");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }

    private void getDataLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "OTOMOTIVES");
                args.put("layanan", "BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("JENIS").asString().equals("PAKET LAYANAN")) {
                            layananPaketList.add(result.get(i));
                        } else if (result.get(i).get("JENIS").asString().equalsIgnoreCase("AFTER SALES SERVIS")) {
                            layananAfterList.add(result.get(i));
                        } else if (result.get(i).get("JENIS").asString().equalsIgnoreCase("RECALL")) {
                            layananRecallList.add(result.get(i));
                        } else if (result.get(i).get("JENIS").asString().equalsIgnoreCase("OTOMOTIVES")) {
                            layananOtolist.add(result.get(i));
                        }
                    }

                } else {
                    showInfoDialog("Konfirmasi", "Gagal Load Nama Layanan, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getDataLayanan();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }


            }
        });
    }

    private void setSpinnerNamaLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "OTOMOTIVES");
                args.put("layanan", "BENGKEL");
                args.put("jenisLayanan", jenisLayanan);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                result = result.get("data");
                dataLayananList.asArray().clear();
                dataLayananList.asArray().addAll(result.asArray());

                final List<String> namaLayananList = new ArrayList<>();
                namaLayananList.add("--PILIH--");
                if (dataLayananList.size() > 0) {
                    for (int i = 0; i < dataLayananList.size(); i++) {
                        namaLayananList.add(dataLayananList.get(i).get("NAMA_LAYANAN").asString());
                    }
                }

                setSpinnerOffline(namaLayananList, spNamaLayanan, loadNamaLayanan);
                spNamaLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        namaLayanan = parent.getItemAtPosition(position).toString();
                        if (position != 0) {
                            setSpVarianKendaraan();
                            if (dataLayananList.size() > 0) {
                                for (int i = 0; i < dataLayananList.size(); i++) {
                                    if (dataLayananList.get(i).get("NAMA_LAYANAN").asString().equals(namaLayanan)) {
                                        layananId = dataLayananList.get(i).get("ID").asString();
                                        if (!dataLayananList.get(i).get("PRINCIPAL").asString().equals("N")) {
                                            namaPrincipal = dataLayananList.get(i).get("PRINCIPAL").asString();
                                        } else {
                                            namaPrincipal = "--PILIH--";
                                        }
                                        setSelectionSpinner(namaPrincipal, spNamaPrincipal);
                                        if (dataLayananList.get(i).get("GARANSI").asString().equals("BERSAMA")) {
                                            find(R.id.sp_garansi_atur_layanan, Spinner.class).setSelection(Tools.getIndexSpinner(find(R.id.sp_garansi_atur_layanan, Spinner.class), "YA"));
                                            find(R.id.et_feeGB_layanan, EditText.class).setText("Rp. " + formatRp(dataLayananList.get(i).get("FEE_NON_PAKET").asString()));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

    }

    private void getDataKendaraan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
               /*
                args.remove("nama");
                args.put("nama", "MODEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
                result = result.get("data");
                if (result.size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        dataModelKendaraanList.add(Nson.newObject()
                                .set("TYPE", result.get(i).get("TYPE").asString())
                                .set("MODEL", result.get(i).get("MODEL").asString())
                        );
                    }
                }
                */

                args.put("nama", "DATA_KENDARAAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
                result = result.get("data");
                if (result.size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        dataVarianKendaraanList.add(Nson.newObject()
                                .set("TYPE", result.get(i).get("TYPE").asString())
                                .set("JENIS", result.get(i).get("JENIS").asString())
                                .set("VARIAN", result.get(i).get("VARIAN").asString())
                                .set("MODEL", result.get(i).get("MODEL").asString())
                                .set("MERK", result.get(i).get("MERK").asString())
                        );
                    }
                }
            }

            @Override
            public void runUI() {
                setSpMerkKendaraan(loadMerk);
            }
        });
    }

    private void setSpMerkKendaraan(final String selection) {
        newTask(new Messagebox.DoubleRunnable() {
            List<String> merkList = new ArrayList<>();

            @Override
            public void run() {
                Nson loadMerk = Nson.readJson(UtilityAndroid.getSetting(getApplicationContext(), "MERK_KENDARAAN_ARRAY", ""));
                Nson merkArray = Nson.newArray();
                merkArray.asArray().addAll(loadMerk.asArray());

                merkList.add("--PILIH--");
                if (merkArray.size() > 1) {
                    for (int i = 0; i < merkArray.size(); i++) {
                        merkList.add(merkArray.get(i).get("MERK").asString());
                    }
                } else {
                    String merk = merkArray.get(0).get("MERK").asString();
                    merkList.add(merk);
                }

                merkList = Tools.removeDuplicates(merkList);
            }

            @Override
            public void runUI() {
                setSpinnerOffline(merkList, find(R.id.sp_merk_kendaraan, Spinner.class), selection);
                if (merkList.size() == 2) {
                    find(R.id.sp_merk_kendaraan, Spinner.class).setSelection(1);
                    Tools.setViewAndChildrenEnabled(find(R.id.rl_merk, RelativeLayout.class), false);
                }
                find(R.id.sp_merk_kendaraan, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        merkSelected = parent.getItemAtPosition(position).toString().equals("--PILIH--") ? "" : parent.getItemAtPosition(position).toString();
                        if (!merkSelected.isEmpty()) {
                            if(!isUpdate){
                                setSpVarianKendaraan();
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

    }

    private void setSpVarianKendaraan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args1 = new String[6];
                args1[0] = "CID=" + getSetting("CID");
                args1[1] = "namaLayanan=" + namaLayanan;
                args1[2] = "jenisKendaraan=" + kendaraan;
                args1[3] = "merkKendaraan=" + merkSelected;
                args1[4] = "varian=" + loadVarian;

                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_VARIAN_BY_NAMA_LAYANAN), args1));
            }

            @Override
            public void runUI() {
                result = result.get("data");
                final List<String> varianList = new ArrayList<>();
                varianList.add("--PILIH--");
                if (result.size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        varianList.add(result.get(i).get("VARIAN").asString());
                    }
                }

                setSpinnerOffline(varianList, find(R.id.sp_varian_kendaraan, Spinner.class), loadVarian);
                find(R.id.sp_varian_kendaraan, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        varianSelected = position != 0 ? parent.getItemAtPosition(position).toString() : "";
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    private void setSpModelKendaraan() {
        newTask(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                List<String> modelList = new ArrayList<>();
                modelList.add("--PILIH--");
                for (int i = 0; i < dataModelKendaraanList.size(); i++) {
                    if (dataModelKendaraanList.get(i).get("TYPE").asString().equals(kendaraan)) {
                        modelList.add(dataModelKendaraanList.get(i).get("MODEL").asString());
                    }
                }

                modelList = Tools.removeDuplicates(modelList);
                find(R.id.sp_model_kendaraan, MultiSelectionSpinner.class).setItems(modelList, null);
                find(R.id.sp_model_kendaraan, MultiSelectionSpinner.class).setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {
                        if (strings.size() > 0) {

                        }
                    }
                });
            }
        });
    }

    private void setSpNamaPrincipal(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[4];
                args[0] = "CID=" + getSetting("CID");
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_BENGKEL_PRINCIPAL), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                    if(result.size() > 0){
                        principalList.add("--PILIH--");
                        for (int i = 0; i < result.size(); i++) {
                            principalList.add(result.get(i).get("NAMA_PRINCIPAL").asString());
                        }
                        if(principalList.size() == 2){
                            setSpinnerOffline(principalList.asArray(), spNamaPrincipal, 2);
                        }else{
                            setSpinnerOffline(principalList.asArray(), spNamaPrincipal, selection);
                        }
                    }else{
                        showWarning("PRINCIPAL TIDAK TERSEDIA, SILAHKAN PILIH JENIS LAYANAN LAIN", Toast.LENGTH_LONG);
                        spJenisLayanan.setSelection(0);
                        spJenisLayanan.performClick();
                        viewFocus(spJenisLayanan);
                    }
                }
            }
        });
    }

    private void getWaktuKerja() {
        TimePicker_Dialog timePickerDialog = new TimePicker_Dialog();
        timePickerDialog.show(getSupportFragmentManager(), "TimePicker");
        timePickerDialog.getTimes(new TimePicker_Dialog.OnClickDialog() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void getTime(int day, int hours, int minutes) {
                find(R.id.et_waktu_kerja_hari, EditText.class).setText(String.valueOf(day));
                find(R.id.et_waktu_kerja_jam, EditText.class).setText(String.valueOf(hours));
                find(R.id.et_waktu_kerja_menit, EditText.class).setText(String.valueOf(minutes));
            }

            @Override
            public void getYear(int year) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (maxDisc == 2) {
                find(R.id.btn_discPart_layanan, Button.class).setEnabled(false);
                find(R.id.btn_jasaLain_layanan, Button.class).setEnabled(false);
            }

            if (requestCode == REQUEST_DISC_PART) {
                isDiscList = true;
                discPartList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                Log.d("Tambah____", "AcceptPart__: " + discPartList);
            } else if (requestCode == REQUEST_JASA_LAIN) {
                isDiscList = false;
                discJasaList.add(Nson.readJson(getIntentStringExtra(data, "data")));
                nListArray.add(Nson.readJson(getIntentStringExtra(data, "data")));
                Log.d("Tambah____", "AcceptJasa__: " + discJasaList);
            }
            Log.d("Tambah____", "id: " + layananId);

            //rvLayanan.getAdapter().notifyDataSetChanged();
        }
    }
}
