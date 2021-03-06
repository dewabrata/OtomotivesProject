package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.rkrzmail.utils.APIUrls.JURNAL;
import static com.rkrzmail.utils.APIUrls.JURNAL_AKTIVITAS;
import static com.rkrzmail.utils.APIUrls.JURNAL_KAS;
import static com.rkrzmail.utils.APIUrls.JURNAL_KAS_BANK;
import static com.rkrzmail.utils.APIUrls.JURNAL_PEMBAYARAN;
import static com.rkrzmail.utils.APIUrls.JURNAL_PERIODE;
import static com.rkrzmail.utils.APIUrls.JURNAL_TRANSAKSI;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturJurnal_Activity extends AppActivity implements View.OnClickListener {

    private TextView tvTglTransaksi, tvKontak, tvTglJatuhTempo, tvPeriodeAwal, tvPeriodeAkhir;
    private Spinner spAktivitas, spPembayaran, spRekInternal;
    private EditText etKet, etNominal, etNota, etBiayaTf, etNilaiSisa, etNamaPerusahaan, etUmurAsset;
    private SpinnerDialog spDialogTransaksi;
    private NikitaAutoComplete etBankTerbayar;

    private final Nson dataTransaksiList = Nson.newArray();
    private final Nson dataRekeningList = Nson.newArray();
    private final List<String> tipePembayaranList = new ArrayList<>();

    private String noRek = "", namaBank = "";
    private String aktivitas = "";
    private String transaksi = "";
    private String tipePembayaran = "";
    private String tglAwal = "", tglAkhir = "";

    private int transaksiID = 0;
    private int umurBulan = 0;
    private int lastBalanceKas = 0, lastBalanceKasBank = 0;

    private boolean isPeriode = false;
    private boolean isSetoranTunai = false;
    private boolean isBayarOrBeli = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_jurnal_);
        initToolbar();
        initComponent();
        loadData();
        if (!getIntent().hasExtra(DATA)) {
            getLastBalanceKas();
            find(R.id.et_created_date, EditText.class).setVisibility(View.GONE);
            find(R.id.et_created_user, EditText.class).setVisibility(View.GONE);
        } else {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_container, LinearLayout.class), false);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jurnal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initComponent() {
        tvTglTransaksi = findViewById(R.id.tv_tgl_transaksi);
        tvKontak = findViewById(R.id.tv_kontak);
        tvTglJatuhTempo = findViewById(R.id.tv_tgl_jatuh_tempo);
        spAktivitas = findViewById(R.id.sp_aktivitas);
        spPembayaran = findViewById(R.id.sp_pembayaran);
        spRekInternal = findViewById(R.id.sp_norek);
        etKet = findViewById(R.id.et_ket_jurnal);
        etNominal = findViewById(R.id.et_nominal_jurnal);
        etBankTerbayar = findViewById(R.id.et_bankTerbayar_jurnal);
        etNota = findViewById(R.id.et_notaTrace_jurnal);
        etBiayaTf = findViewById(R.id.et_biayaTf_jurnal);
        tvPeriodeAkhir = findViewById(R.id.tv_periode_akhir);
        tvPeriodeAwal = findViewById(R.id.tv_periode_awal);
        etNilaiSisa = findViewById(R.id.et_nilai_sisa);
        etNamaPerusahaan = findViewById(R.id.et_nama_perusahaan);
        etUmurAsset = findViewById(R.id.et_umur_asset);

        defaultEnabled();
        initAutoCompleteNamaBank();

        etBiayaTf.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaTf));
        etNominal.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etNominal));
        tvPeriodeAkhir.setOnClickListener(this);
        tvPeriodeAwal.setOnClickListener(this);
        find(R.id.vg_kontak).setOnClickListener(this);
        find(R.id.vg_tgl_jatuh_tempo).setOnClickListener(this);
        find(R.id.vg_tgl).setOnClickListener(this);

        find(R.id.cb_asset, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                etUmurAsset.setEnabled(compoundButton.isChecked());
                etNilaiSisa.setEnabled(compoundButton.isChecked());
            }
        });
        find(R.id.btn_simpan, Button.class).setOnClickListener(this);
    }

    private void defaultEnabled() {
        Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), false);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), tipePembayaran.equals("TRANSFER") || tipePembayaran.contains("KREDIT") || tipePembayaran.contains("DEBET"));
        Tools.setViewAndChildrenEnabled(find(R.id.tl_ket, TextInputLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.vg_kontak, RelativeLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.tl_nama_perusahaan, TextInputLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.tl_bank_terbayar, TextInputLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.tl_nota, TextInputLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.tl_biaya_tf, TextInputLayout.class), true);

        etBiayaTf.setEnabled(false);
        etUmurAsset.setEnabled(false);
        etNilaiSisa.setEnabled(false);
        etBankTerbayar.setEnabled(false);
    }

    private void defaultHints() {
        /*etKet.setText(" ");
        etNamaPerusahaan.setText(" ");
        etNominal.setText(" ");
        etBankTerbayar.setText(" ");
        etNota.setText(" ");
        etBiayaTf.setText(" ");
        etUmurAsset.setText(" ");
        etNilaiSisa.setText(" ");*/
    }

    private void viewFocus(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setFocusable(true);
                view.requestFocusFromTouch();
                view.requestFocus();
                view.performClick();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));

        tvTglTransaksi.setText(data.get("TANGGAL").asString());
        etKet.setText(data.get("KETERANGAN").asString());
        tvKontak.setText(data.get("NAMA_KONTAK").asString() + "\n" + data.get("NO_PONSEL").asString());
        etNamaPerusahaan.setText(data.get("NAMA_PERUSAHAAN").asString());
        etNominal.setText(RP + NumberFormatUtils.formatRp(data.get("NOMINAL").asString()));
        tvTglJatuhTempo.setText(data.get("JATUH_TEMPO").asString());
        etBankTerbayar.setText(data.get("NAMA_BANK_TERBAYAR").asString());
        etNota.setText(data.get("NO_NOTA/TRACE").asString());
        etBiayaTf.setText(RP + NumberFormatUtils.formatRp(data.get("BIAYA_TRANSFER").asString()));
        find(R.id.tv_tgl_awal, TextView.class).setText(data.get("TANGGAL_MULAI_SEWA").asString());
        find(R.id.tv_tgl_akhir, TextView.class).setText(data.get("TANGGAL_SELESAI_SEWA").asString());
        etUmurAsset.setText(data.get("UMUR_ASSET").asString());
        etNilaiSisa.setText(data.get("NILAI_SISA").asString());
        find(R.id.et_created_date, EditText.class).setText(data.get("CREATED_DATE").asString());
        find(R.id.et_created_user, EditText.class).setText(data.get("CREATED_USER").asString());

        setSpAktivitas(data.get("AKTIVITAS").asString());
        setSpTransaksi(data.get("TRANSAKSI").asString());
        setSpRek(data.get("NAMA_BANK_REKENING_INTERNAL").asString() + " - " + data.get("NOMOR_REKENING_INTERNAL").asString());
        setSpPembayaran(data.get("PEMBAYARAN").asString());

        //find(R.id.cb_asset, CheckBox.class).setChecked(data.get("J"));
    }

    private void setSpAktivitas(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL_AKTIVITAS)));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    List<String> aktivitasList = new ArrayList<>();
                    aktivitasList.add("--PILIH--");
                    result = result.get("data");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            aktivitasList.add(result.get(i).get("AKTIVITAS").asString());
                        }
                    }
                    ArrayAdapter<String> aktivitasAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, aktivitasList);
                    aktivitasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spAktivitas.setAdapter(aktivitasAdapter);
                    spAktivitas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i != 0) {
                                aktivitas = adapterView.getItemAtPosition(i).toString();
                                isBayarOrBeli = aktivitas.equals("BAYAR") || aktivitas.equals("BELI");
                                find(R.id.cb_asset, CheckBox.class).setEnabled(aktivitas.equals("BELI"));
                                if (spAktivitas.isEnabled()) {
                                    setSpTransaksi("");
                                }
                                if (!aktivitas.equals("--PILIH--")) {
                                    if (!tipePembayaran.equals("--PILIH--")) {
                                        if (!transaksi.equals("--PILIH--")) {
                                            getPeriode();
                                        }
                                    }
                                }
                            } else {
                                isBayarOrBeli = false;
                                aktivitas = "";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spAktivitas.getCount(); i++) {
                            if (spAktivitas.getItemAtPosition(i).toString().equals(selection)) {
                                spAktivitas.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void setSpPembayaran(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "aktivitas" + "=" + aktivitas;
                args[1] = "transaksi" + "=" + transaksi;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    List<String> pembayaranList = new ArrayList<>();
                    pembayaranList.add("--PILIH--");
                    result = result.get("data");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            pembayaranList.add(result.get(i).get("PEMBAYARAN").asString());
                        }
                    }
                    ArrayAdapter<String> pembayaranAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, pembayaranList);
                    pembayaranAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPembayaran.setAdapter(pembayaranAdapter);
                    spPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i != 0) {
                                tipePembayaran = adapterView.getItemAtPosition(i).toString();
                                Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), tipePembayaran.equals("INVOICE"));
                                Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), isSetoranTunai || tipePembayaran.equals("TRANSFER") || tipePembayaran.contains("DEBET"));
                                etNota.setEnabled(tipePembayaran.contains("DEBET") && (!aktivitas.equals("TERIMA") && !aktivitas.equals("--PILIH--")));
                                etBankTerbayar.setEnabled((aktivitas.equals("BELI") || aktivitas.equals("BAYAR")) && tipePembayaran.equals("TRANSFER"));
                                if (tipePembayaran.equals("TRANSFER")) {
                                    if (!etBankTerbayar.getText().toString().isEmpty() && !namaBank.isEmpty()) {
                                        etBiayaTf.setEnabled(!etBankTerbayar.getText().toString().equals(namaBank));
                                    }
                                }
                                if (!tipePembayaran.equals("TRANSFER")) {
                                    etBankTerbayar.setText("");
                                    spRekInternal.setSelection(0);
                                }
                                if (!aktivitas.equals("--PILIH--")) {
                                    if (!tipePembayaran.equals("--PILIH--")) {
                                        if (!transaksi.equals("--PILIH--")) {
                                            getPeriode();
                                        }
                                    }
                                }
                            } else {
                                tipePembayaran = "";
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spPembayaran.getCount(); i++) {
                            if (spPembayaran.getItemAtPosition(i).toString().equals(selection)) {
                                spPembayaran.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void setSpTransaksi(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[2];
                args[0] = "aktivitas=" + aktivitas;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL_TRANSAKSI), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    ArrayList<String> transaksiList = new ArrayList<>();
                    dataTransaksiList.add("");
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        transaksiList.add(result.get(i).get("TRANSAKSI").asString());
                    }

                    if (!selection.isEmpty()) {
                        find(R.id.btn_transaksi, Button.class).setText(selection);
                    } else {
                        find(R.id.btn_transaksi, Button.class).setText("--PILIH--");
                    }
                    find(R.id.btn_transaksi).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spDialogTransaksi.showSpinerDialog();
                        }
                    });

                    spDialogTransaksi = new SpinnerDialog(getActivity(), transaksiList, "Pilih Jenis Transaksi", R.style.DialogAnimations_SmileWindow, "Tutup");
                    spDialogTransaksi.setCancellable(true); // for cancellable
                    spDialogTransaksi.setShowKeyboard(false);// for open keyboard by default
                    spDialogTransaksi.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int position) {
                            transaksi = item;
                            if (transaksi.equalsIgnoreCase("PENARIKAN TUNAI BANK") ||
                                    transaksi.equalsIgnoreCase("SETORAN TUNAI BANK")) {// TUNAI BANK
                                isSetoranTunai = true;
                                disableFieldsSetoran();
                            } else {
                                defaultEnabled();
                                isSetoranTunai = false;
                            }
                            find(R.id.btn_transaksi, Button.class).setText(transaksi);
                            spDialogTransaksi.closeSpinerDialog();
                            if (find(R.id.btn_transaksi, Button.class).isEnabled()) {
                                setSpPembayaran("");
                            }
                            if (!aktivitas.equals("--PILIH--")) {
                                if (!tipePembayaran.equals("--PILIH--")) {
                                    if (!transaksi.equals("--PILIH--")) {
                                        getPeriode();
                                    }
                                }
                            }
                        }
                    });
                } else {
                    showInfo("GAGAL MUAT DAFTAR TRANSAKSI");
                }
            }
        });
    }

    public void setSpRek(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    ArrayList<String> str = new ArrayList<>();
                    result = result.get("data");
                    str.add("--PILIH--");
                    dataRekeningList.add("");
                    for (int i = 0; i < result.size(); i++) {
                        dataRekeningList.add(Nson.newObject()
                                .set("ID", result.get(i).get("ID"))
                                .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                                .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                                .set("EDC", result.get(i).get("EDC_ACTIVE"))
                                .set("OFF_US", result.get(i).get("OFF_US"))
                                .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                        str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                    }

                    ArrayList<String> newStr = Tools.removeDuplicates(str);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRekInternal.setAdapter(adapter);
                } else {
                    showError("GAGAL MEMUAT DATA BANK INTERNAL");
                }
            }
        });

        spRekInternal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(dataRekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = dataRekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = dataRekeningList.get(i).get("BANK_NAME").asString();
                    if (isSetoranTunai) tvKontak.setText(namaBank);
                    if (tipePembayaran.equals("TRANSFER")) {
                        if (!etBankTerbayar.getText().toString().trim().isEmpty()) {
                            etBiayaTf.setEnabled(!etBankTerbayar.getText().toString().trim().equals(namaBank));
                        }
                    }
                } else {
                    noRek = "";
                    namaBank = "";
                    tvKontak.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (!selection.isEmpty()) {
            for (int i = 0; i < spRekInternal.getCount(); i++) {
                if (spRekInternal.getItemAtPosition(i).toString().equals(selection)) {
                    spRekInternal.setSelection(i);
                    break;
                }
            }
        }
    }

    private void initAutoCompleteNamaBank() {
        etBankTerbayar.setThreshold(3);
        etBankTerbayar.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "AUTO COMPLETE");
                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
                if (result.get("data").asArray().isEmpty()) {
                    return result.get("message");
                }
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                findView(convertView, R.id.title, TextView.class).setText((getItem(position).get("BANK_NAME").asString()));
                return convertView;
            }
        });

        etBankTerbayar.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.progress_bar));
        etBankTerbayar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String bankName = n.get("BANK_NAME").asString();
                etBiayaTf.setEnabled(!bankName.equalsIgnoreCase(namaBank));
                etBankTerbayar.setText(bankName);
                etBankTerbayar.setSelection(etBankTerbayar.getText().length());
                if (namaBank.isEmpty() && noRek.isEmpty()) {
                    viewFocus(spRekInternal);
                    TextView errorText = (TextView) spRekInternal.getSelectedView();
                    errorText.setError("REKENING INTERNAL BELUM DI PILIH");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                }
                //viewBankBengkel(bankName);
            }
        });
    }

    private void disableFieldsSetoran() {
        //nominal, norek
        Tools.setViewAndChildrenEnabled(find(R.id.ly_container, LinearLayout.class), false);
        Tools.setViewAndChildrenEnabled(find(R.id.tl_nominal, TextInputLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), true);//
        Tools.setViewAndChildrenEnabled(find(R.id.vg_tgl, LinearLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_aktivitas, LinearLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_pembayaran, RelativeLayout.class), true);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_periode, RelativeLayout.class), true);

        find(R.id.btn_transaksi).setEnabled(true);
        find(R.id.btn_simpan, Button.class).setEnabled(true);

    }

    public void getDatePickerDialog(final boolean minOrMax) {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                if (minOrMax)
                    tvTglTransaksi.setText(formattedTime);
                else
                    tvTglJatuhTempo.setText(formattedTime);
            }
        }, year, month, day);

        if (minOrMax)
            datePickerDialog.setMaxDate(cldr);
        else
            datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }


    @SuppressLint({"IntentReset", "NonConstantResourceId"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vg_tgl:
                getDatePickerDialog(true);
                break;
            case R.id.vg_kontak:
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.vg_tgl_jatuh_tempo:
                getDatePickerDialog(false);
                break;
            case R.id.tv_periode_awal:
                if (isPeriode)
                    getDateSpinnerDialog(find(R.id.tv_tgl_awal, TextView.class), "PILIH PERIODE AWAL");
                else
                    showWarning("PERIODE TIDAK TERSEDIA UNTUK TRANSAKSI INI");
                break;
            case R.id.tv_periode_akhir:
                if (isPeriode) {
                    if (find(R.id.tv_tgl_awal, TextView.class).getText().toString().isEmpty()) {
                        viewFocus(tvPeriodeAwal);
                        showWarning("TANGGAL AWAL HARUS DI PILIH");
                    } else {
                        getDateSpinnerDialog(find(R.id.tv_tgl_akhir, TextView.class), "PILIH PERIODE TERAKHIR");
                    }
                } else {
                    showWarning("PERIODE TIDAK TERSEDIA UNTUK TRANSAKSI INI");
                }
                break;
            case R.id.btn_simpan:
                int nominal = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etNominal.getText().toString()));
                if (isSetoranTunai) {
                    if (tvTglTransaksi.getText().toString().isEmpty()) {
                        viewFocus(tvTglTransaksi);
                        tvTglTransaksi.setError("TANGGAL TRANSAKSI HARUS DI ISI");
                    } else if (etNominal.getText().toString().isEmpty()) {
                        viewFocus(etNominal);
                        etNominal.setError("NOMINAL HARUS DI ISI");
                    } else if (etNominal.getText().toString().equalsIgnoreCase("Rp. 0")) {
                        viewFocus(etNominal);
                        etNominal.setError("NOMINAL HARUS DI ISI");
                    } else if (tipePembayaran.equals("--PILIH--") || tipePembayaran.isEmpty()) {
                        viewFocus(spPembayaran);
                        showWarning("PEMBAYARAN HARUS DI PILIH");
                    } else if (noRek.isEmpty() || namaBank.isEmpty()) {
                        viewFocus(spRekInternal);
                        showWarning("REKENING INTERNAL HARUS DI PILIH");
                    } else if (tipePembayaran.equals("CASH") && (isBayarOrBeli && nominal > lastBalanceKas)) {
                        viewFocus(etNominal);
                        etNominal.setError("TOTAL BAYAR MELEBIHI KAS");
                    } else if ((tipePembayaran.equals("TRANSFER") || tipePembayaran.contains("DEBET")) && (isBayarOrBeli && nominal > lastBalanceKasBank)) {
                        viewFocus(etNominal);
                        etNominal.setError("TOTAL BAYAR MELEBIHI KAS BANK");
                    } else {
                        saveDataUsingBody();
                    }
                } else {
                    if (tvTglTransaksi.getText().toString().isEmpty()) {
                        viewFocus(tvTglTransaksi);
                        tvTglTransaksi.setError("TANGGAL TRANSAKSI HARUS DI ISI");
                    } else if (aktivitas.equals("--PILIH--")) {
                        viewFocus(spAktivitas);
                        showWarning("AKTIVITAS HARUS DI PILIH");
                    } else if (find(R.id.btn_transaksi, Button.class).getText().toString().equals("--PILIH--")) {
                        viewFocus(find(R.id.btn_transaksi, Button.class));
                        showWarning("TRANSAKSI HARUS DI PILIH");
                    } else if (etNominal.getText().toString().equalsIgnoreCase("Rp. 0")) {
                        viewFocus(etNominal);
                        etNominal.setError("NOMINAL HARUS DI ISI");
                    } else if (etNominal.getText().toString().isEmpty()) {
                        viewFocus(etNominal);
                        etNominal.setError("NOMINAL HARUS DI ISI");
                    } else if (tipePembayaran.equals("--PILIH--") || tipePembayaran.isEmpty()) {
                        viewFocus(spPembayaran);
                        showWarning("PEMBAYARAN HARUS DI PILIH");
                    } else if (tipePembayaran.equals("TRANSFER")) {
                        if (noRek.isEmpty() || namaBank.isEmpty()) {
                            viewFocus(spRekInternal);
                            showWarning("REKENING INTERNAL HARUS DI PILIH");
                        } else if (etBankTerbayar.isEnabled() && etBankTerbayar.getText().toString().isEmpty()) {
                            viewFocus(etBankTerbayar);
                            showWarning("BANK TERBAYAR HARUS DI ISI");
                        } else if ((aktivitas.equals("BELI") || aktivitas.equals("BAYAR")) &&
                                !namaBank.equalsIgnoreCase(etBankTerbayar.getText().toString())) {
                            viewFocus(etBiayaTf);
                            etBiayaTf.setError("BIAYA TF HARUS DI ISI UNTUK BANK BERBEDA");
                        } else if (isBayarOrBeli && nominal > lastBalanceKasBank) {
                            viewFocus(etNominal);
                            etNominal.setError("TOTAL BAYAR MELEBIHI KAS BANK");
                        } else if (isPeriode && find(R.id.tv_tgl_awal, TextView.class).getText().toString().isEmpty()) {
                            viewFocus(find(R.id.tv_tgl_awal, TextView.class));
                            find(R.id.tv_tgl_awal, TextView.class).setError("TOTAL BAYAR MELEBIHI KAS BANK");
                        } else if (isPeriode && find(R.id.tv_tgl_akhir, TextView.class).getText().toString().isEmpty()) {
                            viewFocus(find(R.id.tv_tgl_akhir, TextView.class));
                            find(R.id.tv_tgl_akhir, TextView.class).setError("TOTAL BAYAR MELEBIHI KAS BANK");
                        } else {
                            saveDataUsingBody();
                        }
                    } else if (tipePembayaran.contains("DEBET")) {
                        if (noRek.isEmpty() || namaBank.isEmpty()) {
                            viewFocus(spRekInternal);
                            showWarning("REKENING INTERNAL HARUS DI PILIH");
                        } else if (etNota.getText().toString().isEmpty()) {
                            viewFocus(etNota);
                            etNota.setError("NO NOTA HARUS DI ISI");
                        } else if (isBayarOrBeli && nominal > lastBalanceKasBank) {
                            viewFocus(etNominal);
                            etNominal.setError("TOTAL BAYAR MELEBIHI KAS BANK");
                        } else {
                            saveDataUsingBody();
                        }
                    } else if (isPeriode && find(R.id.tv_tgl_awal, TextView.class).getText().toString().isEmpty()) {
                        viewFocus(find(R.id.tv_tgl_awal, TextView.class));
                        find(R.id.tv_tgl_awal, TextView.class).setError("TOTAL BAYAR MELEBIHI KAS BANK");
                    } else if (isPeriode && find(R.id.tv_tgl_akhir, TextView.class).getText().toString().isEmpty()) {
                        viewFocus(find(R.id.tv_tgl_akhir, TextView.class));
                        find(R.id.tv_tgl_akhir, TextView.class).setError("TOTAL BAYAR MELEBIHI KAS BANK");
                    } else {
                        saveDataUsingBody();
                    }
                }
                break;
        }
    }

    public void getDateSpinnerDialog(final TextView dateTime, String tittle) {
        Calendar cal = Calendar.getInstance();
        int day, month, year;
        day = cal.get(Calendar.DAY_OF_MONTH);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);

        android.app.DatePickerDialog.OnDateSetListener mDateListener = new android.app.DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                dateTime.setText(formattedTime);
            }
        };
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateListener,
                year, month, day
        );
        dialog.setTitle(tittle);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void getPeriode() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[4];
                args[0] = "aktivitas=" + aktivitas;
                args[1] = "transaksi=" + transaksi;
                args[2] = "pembayaran=" + tipePembayaran;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL_PERIODE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    isPeriode = result.get("data").asString().equals("Y");
                    if (!isPeriode) {
                        find(R.id.tv_tgl_awal, TextView.class).setText("");
                        find(R.id.tv_tgl_akhir, TextView.class).setText("");
                    }
                }
            }
        });
    }

    private void getLastBalanceKas() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[2];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(JURNAL_KAS), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    lastBalanceKas = result.get("data").get("BALANCE_KAS").asInteger();
                    lastBalanceKasBank = result.get("data").get("BALANCE_KAS_BANK").asInteger();
                }
            }
        });
    }


    private void saveDataUsingBody() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            Response response;

            @Override
            public void run() {
                int totalTf = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etNominal.getText().toString().trim()));
                if (tipePembayaran.equals("TRANSFER")) {
                    int totalBiayaTf = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etBiayaTf.getText().toString()));
                    totalTf = totalTf + totalBiayaTf;
                }

                RequestBody formBody = new FormEncodingBuilder()
                        .add("cid", UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim())
                        .add("chekin_id", "chekin_id_value")
                        .add("tanggal", DateFormatUtils.formatDateToDatabase(tvTglTransaksi.getText().toString()))
                        .add("aktivitas", aktivitas.trim())
                        .add("transaksi", transaksi.trim())
                        .add("keterangan", etKet.getText().toString().trim())
                        .add("nama_kontak", tvKontak.getText().toString().replaceAll("[^a-zA-Z]", "").trim())
                        .add("no_ponsel", tvKontak.getText().toString().replaceAll("[^0-9]", "").trim())
                        .add("nama_perusahaan", etNamaPerusahaan.getText().toString().trim())
                        .add("nama_kreditur", tvKontak.getText().toString().replaceAll("[^a-zA-Z]", "").trim())
                        .add("pembayaran", tipePembayaran.trim())
                        .add("nominal", NumberFormatUtils.formatOnlyNumber(etNominal.getText().toString().trim()))
                        .add("tanggal_jatuh_tempo", DateFormatUtils.formatDate(tvTglJatuhTempo.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss"))
                        .add("nama_bank", namaBank.trim())
                        .add("nomor_rekening", noRek.trim())
                        .add("nama_bank_terbayar", etBankTerbayar.getText().toString().trim())
                        .add("no_nota_trace", etNota.getText().toString().trim())
                        .add("biaya_transfer", NumberFormatUtils.formatOnlyNumber(etBiayaTf.getText().toString()))
                        .add("tanggal_mulai_sewa", DateFormatUtils.formatDate(tvPeriodeAwal.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss"))
                        .add("tanggal_selesai_sewa", DateFormatUtils.formatDate(tvPeriodeAkhir.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss"))
                        .add("umur_aset", etUmurAsset.getText().toString().trim())
                        .add("totalTfdanBiaya", String.valueOf(totalTf))
                        .add("user_created", UtilityAndroid.getSetting(getApplicationContext(), "user", ""))
                        .build();
                try {
                    Request request = new Request.Builder()
                            .url(AppApplication.getBaseUrlV4(JURNAL))
                            .post(formBody)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    response = client.newCall(request).execute();
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showError(e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void runUI() {
                if (response.isSuccessful()) {
                    try {
                        result = Nson.readJson(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int httpResponse = response.code();
                    showSuccess("SUKSES MENYIMPAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showWarning("GAGAL MENYIMPAN AKTIVITAS");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!getIntent().hasExtra(DATA)) {
            showInfoDialog("Konfirmasi", "Anda Yakin ingin Keluar ?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CONTACT) {
            Uri contactData = data != null ? data.getData() : null;
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            tvKontak.setText(contactName + "\n" + number);
        }
    }
}
