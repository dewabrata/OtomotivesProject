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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.TimePicker_Dialog;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_LAYANAN;
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
    private DecimalFormat formatter;

    private final Nson layananPaketList = Nson.newArray();
    private final Nson layananOtolist = Nson.newArray();
    private final Nson layananRecallList = Nson.newArray();
    private final Nson layananAfterList = Nson.newArray();
    private final Nson dataLayananList = Nson.newArray();
    private final Nson principalList = Nson.newArray();
    private final Nson discPartList = Nson.newArray();
    private final Nson discJasaList = Nson.newArray();
    private final Nson layananBengkelAvail = Nson.newArray();
    private final Nson dataMerkKendaraanList = Nson.newArray();
    private final Nson dataVarianKendaraanList = Nson.newArray();
    private final Nson dataModelKendaraanList = Nson.newArray();

    private StringBuilder modelBuilder = new StringBuilder();
    private StringBuilder merkBuilder = new StringBuilder();

    private Nson editNson;
    List<String> loadMerkList = new ArrayList<>();
    List<String> loadModelList = new ArrayList<>();

    boolean isUpdate = false;
    String loadVarian = "", varianSelected;
    private String jenisLayanan = "",
            layananId = "",
            namaLayanan = "",
            idPrincipal = "",
            namaPrincipal = "",
            kendaraan = "",
            garansi = "";
    private int maxDisc = 0, size = 0;
    private boolean isDiscList,
            isPaket = false,
            isAfterService = false,
            isRecall = false,
            isOtomotives = false, isAdd = false;
    private String jenisKendaraan = "", modelKendaraan = "", tipeKendaraan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_layanan);
        if(!Tools.isNetworkAvailable(getActivity())){
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        initToolbar();
        getDataLayanan();
        getDataKendaraan();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
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
            namaLayanan = editNson.get("NAMA_LAYANAN").asString();
            namaPrincipal = editNson.get("PRINCIPAL").asString();
            loadVarian = editNson.get("VARIAN").asString();
            String loadMerk = editNson.get("MERK").asString();
            String loadModel = editNson.get("MODEL").asString();
            String[] splitMerk = loadMerk.split(", ");
            String[] splitModel = loadModel.split(", ");

            if (splitMerk.length > 0) {
                loadMerkList = removeEmptyString(splitMerk);
            } else {
                loadMerkList.add(loadMerk.replace(", ", ""));
            }
            if (splitModel.length > 0) {
                loadModelList = removeEmptyString(splitModel);
            } else {
                loadModelList.add(loadModel.replace(", ", ""));
            }

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

                if (merkBuilder.toString().isEmpty()) {
                    showWarning("MERK KENDARAAN HARUS DI PILIH");
                    return;
                }
                if(modelBuilder.toString().isEmpty()){
                    showWarning("MODEL KENDARAAN HARUS DI PILIH");
                    return;
                }
                if (varianSelected.isEmpty()) {
                    showWarning("VARIAN KENDARAAN HARUS DI PILIH");
                    return;
                }

                if(finalIsUpdate){
                    updateData(editNson);
                }else{
                    saveData();
                }
            }
        });

    }

    private List<String> removeEmptyString(String[] args){
        List<String> argsList = new ArrayList<>();
        if(args.length > 0){
            for(String index : args){
                if(!index.equals(" ") || !index.isEmpty()){
                    index = index.replace(",", "");
                    argsList.add(index);
                }
            }
        }
        return  argsList;
    }

    private void updateData(final Nson id) {
        if(!Tools.isNetworkAvailable(getActivity())){
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
                args.put("merk", merkBuilder.toString());
                args.put("varian", varianSelected);
                args.put("model", modelBuilder.toString());
                args.put("keterangan", find(R.id.et_keterangan, EditText.class).getText().toString());
                if (!jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    args.put("biaya", jenisLayanan);
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
                Tools.setViewAndChildrenEnabled(find(R.id.ly_kendaraan, LinearLayout.class), jenisLayanan.equals("PAKET LAYANAN"));

                if (jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    if(!isUpdate) spNamaLayanan.setEnabled(true);
                    find(R.id.sp_garansi_atur_layanan, Spinner.class).setEnabled(true);
                } else if (jenisLayanan.equalsIgnoreCase("OTOMOTIVES")) {
                    if(!isUpdate) spNamaLayanan.setEnabled(true);
                    find(R.id.btn_deskripsi_aturLayanan, Button.class).setEnabled(true);
                } else if (jenisLayanan.equalsIgnoreCase("AFTER SALES SERVIS") || jenisLayanan.equalsIgnoreCase("RECALL")) {
                    find(R.id.btn_deskripsi_aturLayanan, Button.class).setEnabled(true);
                    find(R.id.btn_simpan_atur_layanan, Button.class).setEnabled(true);
                }

                setSpinnerNamaLayanan(namaLayanan);
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
        if(!Tools.isNetworkAvailable(getActivity())){
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
                    args.put("biaya", jenisLayanan);
                } else {
                    args.put("biaya", formatOnlyNumber(find(R.id.et_biayaPaket_layanan, EditText.class).getText().toString()));
                }
                args.put("garansi", find(R.id.sp_garansi_atur_layanan, Spinner.class).getSelectedItem().toString());
                args.put("fgb", formatOnlyNumber(find(R.id.et_feeGB_layanan, EditText.class).getText().toString()));
                args.put("kendaraan", kendaraan);
                args.put("merk", merkBuilder.toString());
                args.put("model", modelBuilder.toString());
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
                args.put("jenis_kendaraan", jenisKendaraan.equals("") ? "*" : jenisKendaraan);
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
                        if (result.get(i).get("JENIS").asString().equals("PAKET LAYANAN")
                                && result.get(i).get("KENDARAAN").asString().contains(kendaraan)) {
                            layananPaketList.add(result.get(i));
                        } else if (result.get(i).get("JENIS").asString().equalsIgnoreCase("AFTER SALES SERVIS")
                                && result.get(i).get("KENDARAAN").asString().contains(kendaraan)) {
                            layananAfterList.add(result.get(i));
                        } else if (result.get(i).get("JENIS").asString().equalsIgnoreCase("RECALL")
                                && result.get(i).get("KENDARAAN").asString().contains(kendaraan)) {
                            layananRecallList.add(result.get(i));
                        } else if (result.get(i).get("JENIS").asString().equalsIgnoreCase("OTOMOTIVES") &&
                                result.get("KENDARAAN").asString().trim().contains(kendaraan)) {
                            layananOtolist.add(result.get(i));
                        }
                    }

                } else {
                    showInfoDialog("Gagal Load Nama Layanan, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getDataLayanan();
                        }
                    });
                }


            }
        });
    }

    private void setSpinnerNamaLayanan(final String selection) {
        newTask(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                dataLayananList.asArray().clear();
                if (jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    dataLayananList.asArray().addAll(layananPaketList.asArray());
                } else if (jenisLayanan.equalsIgnoreCase("RECALL")) {
                    dataLayananList.asArray().addAll(layananRecallList.asArray());
                } else if (jenisLayanan.equalsIgnoreCase("AFTER SALES SERVIS")) {
                    dataLayananList.asArray().addAll(layananAfterList.asArray());
                } else {
                    dataLayananList.asArray().addAll(layananOtolist.asArray());
                }

                final List<String> namaLayananList = new ArrayList<>();
                namaLayananList.add("--PILIH--");
                if (dataLayananList.size() > 0) {
                    for (int i = 0; i < dataLayananList.size(); i++) {
                        namaLayananList.add(dataLayananList.get(i).get("NAMA_LAYANAN").asString());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, namaLayananList) {
                   /* @SuppressLint("WrongConstant")
                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View mView = null;
                        if(layananBengkelAvail.size() > 0){
                            for (int i = 0; i < layananBengkelAvail.size(); i++) {
                                if (layananBengkelAvail.get(i).asString().equals(namaLayananList.get(position))) {
                                    TextView mTextView = new TextView(getContext());
                                    mTextView.setVisibility(View.GONE);
                                    mTextView.setHeight(0);
                                    mView = mTextView;
                                    break;
                                } else {
                                    mView = super.getDropDownView(position, null, parent);
                                }
                            }
                        }else{
                            mView = super.getDropDownView(position, null, parent);
                        }
                        return mView;
                    }*/
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                adapter.notifyDataSetChanged();
                spNamaLayanan.setAdapter(adapter);
                spNamaLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String item = parent.getItemAtPosition(position).toString();
                        if (position != 0) {
                            if (dataLayananList.size() > 0) {
                                for (int i = 0; i < dataLayananList.size(); i++) {
                                    if (dataLayananList.get(i).get("NAMA_LAYANAN").asString().equals(item)) {
                                        layananId = dataLayananList.get(i).get("ID").asString();
                                        kendaraan = dataLayananList.get(i).get("KENDARAAN").asString();
                                        jenisKendaraan = dataLayananList.get(i).get("JENIS_KENDARAAN").asString();
                                        modelKendaraan = dataLayananList.get(i).get("MODEL_KENDARAAN").asString();
                                        tipeKendaraan = dataLayananList.get(i).get("KENDARAAN").asString();

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

                        setSpMerkKendaraan();
                        setSpVarianKendaraan(null);
                        setSpModelKendaraan();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if (!selection.isEmpty()) {
                    for (int in = 0; in < spNamaLayanan.getCount(); in++) {
                        if (spNamaLayanan.getItemAtPosition(in).toString().equals(selection)) {
                            spNamaLayanan.setSelection(in);
                            break;
                        }
                    }
                }
            }
        });

    }

    private void getDataKendaraan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
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
                        );
                    }
                }

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

                args.put("action", "merkKendaraan");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_DATA_BENGKEL), args));
                result = result.get("data").get(0);
                String jenisKendaraan = result.get("JENIS_KENDARAAN").asString();
                String[] splitMerk = result.get("MERK_KENDARAAN").asString().split(", ");
                if (splitMerk.length > 0) {
                    for (String merk : splitMerk) {
                        dataMerkKendaraanList.add(Nson.newObject()
                                .set("TYPE", jenisKendaraan)
                                .set("MERK", merk.replace(",", ""))
                        );
                    }
                }else{
                    dataMerkKendaraanList.add(Nson.newObject()
                            .set("TYPE", jenisKendaraan)
                            .set("MERK", result.get("MERK_KENDARAAN").asString())
                    );

                }
            }

            @Override
            public void runUI() {
                setSpMerkKendaraan();
                setSpVarianKendaraan(null);
                setSpModelKendaraan();
            }
        });
    }

    private void setSpMerkKendaraan() {
        newTask(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                List<String> merkList = new ArrayList<>();
                merkList.add("--PILIH--");
                for (int i = 0; i < dataMerkKendaraanList.size(); i++) {
                    if (dataMerkKendaraanList.get(i).get("TYPE").asString().equals(kendaraan)) {
                        if (!merkList.contains(dataMerkKendaraanList.get(i).get("MERK").asString())) {
                            merkList.add(dataMerkKendaraanList.get(i).get("MERK").asString());
                        }
                    }
                }
                merkList = Tools.removeDuplicates(merkList);
                find(R.id.sp_merk_kendaraan, MultiSelectionSpinner.class).setItems(merkList, loadMerkList);
                find(R.id.sp_merk_kendaraan, MultiSelectionSpinner.class).setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {
                        if(strings.size() > 0){
                            merkBuilder = new StringBuilder();
                            for (int i = 0; i < strings.size(); i++) {
                                merkBuilder.append(strings.get(i)).append(", ");
                            }
                        }
                    }
                });
            }
        });

    }

    private void setSpVarianKendaraan(final List<String> varianByModel) {
        newTask(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {
                List<String> varianList = new ArrayList<>();
                varianList.add("--PILIH--");
                for (int i = 0; i < dataVarianKendaraanList.size(); i++) {
                    if (dataVarianKendaraanList.get(i).get("JENIS").asString().equals(jenisKendaraan)) {
                        if (!varianList.contains(dataVarianKendaraanList.get(i).get("VARIAN").asString())) {
                            varianList.add(dataVarianKendaraanList.get(i).get("VARIAN").asString());
                        }
                    }else{
                        if(varianByModel != null && varianByModel.size() > 0){
                            for (int j = 0; j < varianByModel.size(); j++) {
                                if(dataVarianKendaraanList.get(i).get("MODEL").asString().equals(varianByModel.get(j))){
                                    varianList.add(dataVarianKendaraanList.get(i).get("VARIAN").asString());
                                }
                            }
                        }else{
                            if(dataVarianKendaraanList.get(i).get("TYPE").asString().equals(kendaraan)){
                                if (!varianList.contains(dataVarianKendaraanList.get(i).get("VARIAN").asString())) {
                                    varianList.add(dataVarianKendaraanList.get(i).get("VARIAN").asString());
                                }
                            }
                        }
                    }
                }
                varianList = Tools.removeDuplicates(varianList);
                setSpinnerOffline(varianList, find(R.id.sp_varian_kendaraan, Spinner.class), loadVarian);
                find(R.id.sp_varian_kendaraan, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position != 0){
                            varianSelected = parent.getItemAtPosition(position).toString();
                        }else{
                            varianSelected = "";
                        }
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
                find(R.id.sp_model_kendaraan, MultiSelectionSpinner.class).setItems(modelList, loadModelList);
                find(R.id.sp_model_kendaraan, MultiSelectionSpinner.class).setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {
                        if(strings.size() > 0){
                            modelBuilder = new StringBuilder();
                            for (int i = 0; i < strings.size(); i++) {
                                modelBuilder.append(strings.get(i)).append(", ");
                            }
                            setSpVarianKendaraan(strings);
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
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "PRINCIPAL BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    principalList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        principalList.add(result.get(i).get("NAMA_PRINCIPAL"));
                    }

                    setSpinnerOffline(principalList.asArray(), spNamaPrincipal, selection);
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
