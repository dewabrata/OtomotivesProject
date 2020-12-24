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
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.ADD;
import static com.rkrzmail.utils.ConstUtils.EDIT;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturLayanan_Activity extends AppActivity {

    private static final int REQUEST_DESKRIPSI = 12;
    public static final int REQUEST_DISC_PART = 13;
    public static final int REQUEST_JASA_LAIN = 14;
    private static final String TAG = "AturLayanan___";

    private Spinner spJenisLayanan, spNamaPrincipal, spNamaLayanan, spStatus;
    private ArrayAdapter<String> adapter;
    private RecyclerView rvLayanan;
    private DecimalFormat formatter;
    private Nson
            layananPaketList = Nson.newArray(),
            layananOtolist = Nson.newArray(),
            layananRecallList = Nson.newArray(),
            layananAfterList = Nson.newArray(),
            dataLayananList = Nson.newArray(),
            principalList = Nson.newArray(),
            discPartList = Nson.newArray(),
            discJasaList = Nson.newArray(),
            layananBengkelAvail = Nson.newArray(),
            editNson;
    private String jenisLayanan = "",
            layananId = "",
            namaLayanan = "",
            idPrincipal = "",
            namaPrincipal = "",
            kendaraan = "",
            merk = "",
            model = "",
            varian = "",
            keterangan = "",
            item = "", garansi = "";
    private int maxDisc = 0, size = 0;
    private boolean isDiscList,
            isPaket = false,
            isAfterService = false,
            isRecall = false,
            isOtomotives = false, isAdd = false; //true for discPart, false for discJasa
    private String jenisKendaraan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_layanan_);
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
        initToolbar();
        editNson = Nson.readJson(getIntentStringExtra("edit"));
        kendaraan = getSetting("JENIS_KENDARAAN");
        spJenisLayanan = findViewById(R.id.sp_jenis_layanan);
        spNamaLayanan = findViewById(R.id.sp_nama_layanan);
        spStatus = findViewById(R.id.sp_status_layanan);
        spNamaPrincipal = findViewById(R.id.sp_nama_principal);
        rvLayanan = findViewById(R.id.recyclerView_layanan);

        initData();
        initListener();
        setSpNamaPrincipal("");
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

            setSpinnerOffline(statusList, spStatus, "");
            setSpinnerOffline(jenisList, spJenisLayanan, "");
            setSpinnerOffline(statusList, find(R.id.sp_garansi_atur_layanan, Spinner.class), "");

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
                    } else if (spNamaLayanan.getSelectedItem().toString().equalsIgnoreCase("AFTER SALES SERVICES") || spNamaLayanan.getSelectedItem().toString().equalsIgnoreCase("RECALL")) {
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
                    saveData();
                }
            });
        } else if (getIntent().hasExtra(EDIT)) {
            Tools.setViewAndChildrenEnabled(find(R.id.parent_ly_layanan, LinearLayout.class), false);
            spStatus.setEnabled(true);

            find(R.id.btn_simpan_atur_layanan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_atur_layanan, Button.class).setEnabled(true);
            find(R.id.ly_biaya_paket, TextInputLayout.class).setEnabled(true);
            find(R.id.ly_disc_booking, TextInputLayout.class).setEnabled(true);

            namaPrincipal = editNson.get("PRINCIPAL").asString();
            namaLayanan = editNson.get("NAMA_LAYANAN").asString();
            find(R.id.et_discBooking_layanan, EditText.class).setText(editNson.get("DISCOUNT_BOOKING").asString());
            try {
                find(R.id.et_biayaPaket_layanan, EditText.class).setText("Rp. " + formatRp(editNson.get("BIAYA_PAKET").asString()));
            } catch (Exception e) {
                showError("NumberException: " + e.getMessage());
            }


            find(R.id.sp_garansi_atur_layanan, Spinner.class).setSelection(Tools.getIndexSpinner(spJenisLayanan, editNson.get("STATUS").asString()));
            find(R.id.et_discBooking_layanan, EditText.class).setText(editNson.get("DISCOUNT_BOOKING").asString());
            find(R.id.btn_simpan_atur_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(editNson);
                }
            });

            setSpinnerOffline(statusList, find(R.id.sp_garansi_atur_layanan, Spinner.class), editNson.get("GARANSI_BERSAMA").asString());
            setSpinnerOffline(statusList, spStatus, editNson.get("STATUS").asString());
            setSpinnerOffline(jenisList, spJenisLayanan, editNson.get("JENIS_LAYANAN").asString());
            setSpinnerFromApi(spNamaLayanan, "action", "view", VIEW_LAYANAN, "NAMA_LAYANAN", editNson.get("NAMA_LAYANAN").asString());
            setSpinnerFromApi(spNamaPrincipal, "action", "principal", "databengkel", "NAMA", editNson.get("PRINCIPAL").asString());

        }


    }

    private void updateData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("status", spStatus.getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Mengupdate Layanan");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menambahkan Layanan");
                }
            }
        });
    }

    private void initListener() {
        find(R.id.et_biayaPaket_layanan, EditText.class).addTextChangedListener(new RupiahFormat(find(R.id.et_biayaPaket_layanan, EditText.class)));
        find(R.id.et_discBooking_layanan, EditText.class).addTextChangedListener(new PercentFormat(find(R.id.et_discBooking_layanan, EditText.class)));
        find(R.id.sp_garansi_atur_layanan, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (position == 0 && item.equalsIgnoreCase("YA")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.tl_fee, TextInputLayout.class), false);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.tl_fee, TextInputLayout.class), true);
                }
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
                if (position == 0) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_garansi, LinearLayout.class), false);
                }
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PAKET LAYANAN")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.parent_ly_layanan, LinearLayout.class), true);
                    spNamaPrincipal.setEnabled(false);
                    spNamaLayanan.setEnabled(true);
                    find(R.id.sp_garansi_atur_layanan, Spinner.class).setEnabled(true);
                    jenisLayanan = item;
                } else if (item.equalsIgnoreCase("OTOMOTIVES")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.parent_ly_layanan, LinearLayout.class), false);
                    spJenisLayanan.setEnabled(true);
                    spNamaLayanan.setEnabled(true);
                    find(R.id.btn_deskripsi_aturLayanan, Button.class).setEnabled(true);
                    spStatus.setEnabled(true);
                    jenisLayanan = item;
                } else if (item.equalsIgnoreCase("AFTER SALES SERVICES") || item.equalsIgnoreCase("RECALL")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.parent_ly_layanan, LinearLayout.class), false);
                    spJenisLayanan.setEnabled(true);
                    find(R.id.btn_deskripsi_aturLayanan, Button.class).setEnabled(true);
                    spStatus.setEnabled(true);
                    spNamaLayanan.setEnabled(true);
                    spNamaPrincipal.setEnabled(true);
                    jenisLayanan = item;
                    find(R.id.btn_simpan_atur_layanan, Button.class).setEnabled(true);
                } else if (item.equalsIgnoreCase("PERAWATAN LAINYA")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.parent_ly_layanan, LinearLayout.class), false);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.parent_ly_layanan, LinearLayout.class), true);
                }
                //find(R.id.btn_discPart_layanan, Button.class).setEnabled(true);
                //find(R.id.btn_jasaLain_layanan, Button.class).setEnabled(true);
                setNamaLayanan();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spNamaLayanan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (dataLayananList.size() > 0) {
                    for (int i = 0; i < dataLayananList.size(); i++) {
                        if (dataLayananList.get(i).get("NAMA_LAYANAN").asString().contains(item)) {
                            layananId = dataLayananList.get(i).get("ID").asString();
                            kendaraan = dataLayananList.get(i).get("KENDARAAN").asString();
                            merk = dataLayananList.get(i).get("MERK").asString();
                            model = dataLayananList.get(i).get("MODEL").asString();
                            varian = dataLayananList.get(i).get("VARIAN").asString();
                            keterangan = dataLayananList.get(i).get("KETERANGAN_LAYANAN").asString();
                            jenisKendaraan = dataLayananList.get(i).get("JENIS_KENDARAAN").asString();
                            if (!dataLayananList.get(i).get("PRINCIPAL").asString().equals("N")) {
                                namaPrincipal = dataLayananList.get(i).get("PRINCIPAL").asString();
                            } else {
                                namaPrincipal = "--PILIH--";
                            }
                            setSpNamaPrincipal(namaPrincipal);
                            if (dataLayananList.get(i).get("GARANSI").asString().equals("BERSAMA")) {
                                find(R.id.sp_garansi_atur_layanan, Spinner.class).setSelection(Tools.getIndexSpinner(find(R.id.sp_garansi_atur_layanan, Spinner.class), "YA"));
                                find(R.id.et_feeGB_layanan, EditText.class).setText("Rp. " + formatRp(dataLayananList.get(i).get("FEE_NON_PAKET").asString()));
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                args.put("keterangan", keterangan);
                args.put("discbook", find(R.id.et_discBooking_layanan, EditText.class).getText().toString());
                if (!jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                    args.put("biaya", jenisLayanan);
                } else {
                    args.put("biaya", formatOnlyNumber(find(R.id.et_biayaPaket_layanan, EditText.class).getText().toString()));
                }
                args.put("garansi", find(R.id.sp_garansi_atur_layanan, Spinner.class).getSelectedItem().toString());
                args.put("fgb", formatOnlyNumber(find(R.id.et_feeGB_layanan, EditText.class).getText().toString()));
                args.put("kendaraan", kendaraan);
                args.put("merk", merk);
                args.put("model", model);
                args.put("varian", varian);
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
                args.put("jenis_kendaraan", jenisKendaraan);

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

    private void setNamaLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "OTOMOTIVES");
                args.put("layanan", "BENGKEL");
                if (layananPaketList.size() == 0 ||
                        layananAfterList.size() == 0 ||
                        layananRecallList.size() == 0 || layananOtolist.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
                }
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
                    Log.d(TAG, "Layanan : " + layananPaketList);

                    if (jenisLayanan.equalsIgnoreCase("PAKET LAYANAN")) {
                        setSpinner(spNamaLayanan, layananPaketList, namaLayanan);
                    } else if (jenisLayanan.equalsIgnoreCase("RECALL")) {
                        setSpinner(spNamaLayanan, layananRecallList, namaLayanan);
                    } else if (jenisLayanan.equalsIgnoreCase("AFTER SALES SERVICES")) {
                        setSpinner(spNamaLayanan, layananAfterList, namaLayanan);
                    } else {
                        setSpinner(spNamaLayanan, layananOtolist, namaLayanan);
                    }

                } else {
                    showInfoDialog("Gagal Load Nama Layanan, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setNamaLayanan();
                        }
                    });
                }
            }
        });
    }

    private void setSpinner(Spinner spinner, Nson listItem, String selection) {
        List<String> dataList = new ArrayList<>();
        dataList.add("--PILIH--");
        for (int i = 0; i < listItem.size(); i++) {
            dataList.add(listItem.get(i).get("NAMA_LAYANAN").asString());
        }
        dataLayananList.asArray().addAll(listItem.asArray());
        final List<String> data = Tools.removeDuplicates(dataList);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data) {
            @Override
            public boolean isEnabled(int position) {
                if (isAdd) {
                    for (int i = 0; i < layananBengkelAvail.size(); i++) {
                        if (layananBengkelAvail.get(i).asString().equals(data.get(position))) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @SuppressLint("WrongConstant")
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView = null;
                for (int i = 0; i < layananBengkelAvail.size(); i++) {
                    if (layananBengkelAvail.get(i).asString().equals(data.get(position))) {
                        TextView mTextView = new TextView(getContext());
                        mTextView.setVisibility(View.GONE);
                        mTextView.setHeight(0);
                        mView = mTextView;
                        break;
                    } else {
                        mView = super.getDropDownView(position, null, parent);
                    }
                }
                return mView;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selection.isEmpty()) {
            for (int in = 0; in < spinner.getCount(); in++) {
                if (spinner.getItemAtPosition(in).toString().contains(selection)) {
                    spinner.setSelection(in);
                }
            }
        }
    }

    private void setSpNamaPrincipal(final String principal) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Principal");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("databengkel"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    principalList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        principalList.add(result.get(i).get("NAMA"));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, principalList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spNamaPrincipal.setAdapter(spinnerAdapter);
                    if (!principal.isEmpty()) {
                        for (int in = 0; in < spNamaPrincipal.getCount(); in++) {
                            if (spNamaPrincipal.getItemAtPosition(in).toString().contains(principal)) {
                                spNamaPrincipal.setSelection(in);
                                break;
                            }
                        }
                    }
                }
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
