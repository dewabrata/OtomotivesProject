package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturRekening_Activity;
import com.rkrzmail.srv.AutoCompleteDialog;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.rkrzmail.utils.APIUrls.JURNAL_KAS;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;
import static com.rkrzmail.utils.ConstUtils.REQUEST_REKENING;


public class AturTerimaPart_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturTerimaPart";

    private Spinner spSupplier, spPembayaran, spRekening;
    private TextView tvTglPesan, tvTglTerima, tvTglJatuhTempo, tvNamaSupplier;
    private EditText etNoDo, etOngkir;
    private NikitaAutoComplete etNamaPerusahaan;
    private Button btnSelanjutnya;
    private SpinnerDialog spDialogPerusahaan;
    private AutoCompleteDialog autoCompleteDialog;
    private DialogInterface dialogInterface;

    private final Nson dataRekeningList = Nson.newArray();
    private List<String> principalList = new ArrayList<>();
    private ArrayList<String> perusahaanList = new ArrayList<>();
    private ArrayAdapter<String> spRekAdapter;
    private List<String> data = new ArrayList<>();
    private String tglPesan, tglTerima;
    private String tipeSupplier = "";
    private String tipePembayaran = "";
    private int sizeSupplier = 0;
    private int lastBalanceKas = 0, lastBalanceKasBank = 0;
    final long[] tglPesanTimeMilis = {0};

    private boolean flagValidation = false;
    private boolean perusahaanEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_terima_part);
        initToolbar();
        initComponent();
        getLastBalanceKas("");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Terima Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkTgl();
    }

    private void initComponent() {
        spRekening = findViewById(R.id.sp_rekAsal_terimaPart);
        spSupplier = findViewById(R.id.spinnerSupplier);
        spPembayaran = findViewById(R.id.spinnerPembayaran);
        tvNamaSupplier = findViewById(R.id.tv_nama_supplier);
        etNoDo = findViewById(R.id.txtNoDo);
        etOngkir = findViewById(R.id.txtOngkosKirim);
        tvTglPesan = findViewById(R.id.tglPesan);
        tvTglTerima = findViewById(R.id.tglTerima);
        tvTglJatuhTempo = findViewById(R.id.tv_tgl_jatuh_tempo);
        btnSelanjutnya = findViewById(R.id.btnSelanjutnya);
        etNamaPerusahaan = findViewById(R.id.et_nama_perusahaan);

        setSpRek();
        initAutoCompletePerusahaan();
        setSpPrincipal();

        etOngkir.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etOngkir));
        Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), false);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), false);

        tvTglPesan.setOnClickListener(this);
        tvTglTerima.setOnClickListener(this);
        tvTglJatuhTempo.setOnClickListener(this);
        find(R.id.vg_kontak).setOnClickListener(this);
        find(R.id.btnSelanjutnya, Button.class).setOnClickListener(this);
        find(R.id.tv_tgl_jatuh_tempo).setOnClickListener(this);

        spSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                //nama perusahaan, enable bila tipe supplier selain pincipal,
                tipeSupplier = parent.getItemAtPosition(position).toString();
                perusahaanEnable = !tipeSupplier.equalsIgnoreCase("PRINCIPAL");
                if (tipeSupplier.equals("PRINCIPAL")) {
                    tvNamaSupplier.setText("");
                    etNamaPerusahaan.setText("");
                    setSpPrincipal();
                } else {
                    find(R.id.sp_nama_principal, Spinner.class).setSelection(0);
                }
                etNamaPerusahaan.setEnabled(!tipeSupplier.equals("PRINCIPAL") && !tipeSupplier.equals("--PILIH--"));
                Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_principal, LinearLayout.class), tipeSupplier.equals("PRINCIPAL") && sizeSupplier > 1);
                Tools.setViewAndChildrenEnabled(find(R.id.ly_namaSup_terimaPart, LinearLayout.class),
                        !tipeSupplier.equals("PRINCIPAL") &&
                                !tipeSupplier.equals("--PILIH--") &&
                                !tipeSupplier.equals("ECOMERCE"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                tipePembayaran = parent.getItemAtPosition(position).toString();
                find(R.id.et_no_trace).setEnabled(tipePembayaran.equals("TRANSFER"));

                if (tipePembayaran.equalsIgnoreCase("INVOICE")) {
                    find(R.id.et_no_trace, EditText.class).setText("");
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), false);
                } else if (tipePembayaran.equalsIgnoreCase("TRANSFER")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), true);
                    if (spRekening.getCount() == 0) {
                        showInfoDialog("Konfirmasi", "Rekening Belum Di tambah, Tambah Rekening ? ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogInterface = dialog;
                                startActivityForResult(new Intent(getActivity(), AturRekening_Activity.class), REQUEST_REKENING);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                    }
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), false);
                } else if (tipePembayaran.equals("KONSIGNMENT")) {
                    find(R.id.et_no_trace, EditText.class).setText("");
                    showWarning("TIPE PEMBAYARAN BELUM AKTIF");
                    spPembayaran.post(new Runnable() {
                        @Override
                        public void run() {
                            spPembayaran.performClick();
                        }
                    });
                } else {
                    find(R.id.et_no_trace, EditText.class).setText("");
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, LinearLayout.class), false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSpPrincipal() {
        if (principalList.size() > 0) {
            setSpinnerOffline(principalList, find(R.id.sp_nama_principal, Spinner.class), sizeSupplier > 1 ? "--PILIH--" : principalList.get(1));
        } else {
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
                            principalList.add(result.get(i).get("NAMA_PRINCIPAL").asString());
                        }
                        sizeSupplier = result.size();
                        setSpinnerOffline(principalList, find(R.id.sp_nama_principal, Spinner.class), "");
                    }
                }
            });
        }
    }

    private void initAutoCompletePerusahaan() {
        etNamaPerusahaan.setThreshold(3);
        etNamaPerusahaan.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "PERUSAHAAN");
                args.put("namaPerusahaan", bookTitle.replace("PT. ", ""));
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));

                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                String jenis;
                if (!getItem(position).get("NAMA_PERUSAHAAN").asString().isEmpty()) {
                    jenis = getItem(position).get("NAMA_PERUSAHAAN").asString();
                } else {
                    jenis = getItem(position).get("NAMA_USAHA_TOKO").asString();
                }
                if (!getItem(position).get("KOTA_KABUPATEN").asString().isEmpty()) {
                    jenis = jenis + "- " + getItem(position).get("KOTA_KABUPATEN").asString();
                }
                findView(convertView, R.id.title, TextView.class).setText(jenis);
                return convertView;
            }
        });

        etNamaPerusahaan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.progress_bar));
        etNamaPerusahaan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Nson nson = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String jenis;
                if (!nson.get("NAMA_PERUSAHAAN").asString().isEmpty()) {
                    jenis = nson.get("NAMA_PERUSAHAAN").asString();
                } else {
                    jenis = nson.get("NAMA_USAHA_TOKO").asString();
                }
                etNamaPerusahaan.setText(jenis);
            }
        });
    }

    private void setNamaPerusahaan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "NAMA PERUSAHAAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    perusahaanList.addAll(result.asArray());
                    autoCompleteDialog = new AutoCompleteDialog(getActivity(), perusahaanList, "ISI NAMA PERUSAHAAN");
                    autoCompleteDialog.setCancellable(true);
                    autoCompleteDialog.setShowKeyboard(false);
                    autoCompleteDialog.setOnItemClick(new AutoCompleteDialog.OnItemClick() {
                        @Override
                        public void onClick(String item, int position) {
                            find(R.id.et_nama_perusahaan, EditText.class).setText(item);
                            find(R.id.et_nama_perusahaan, EditText.class).setSelection(find(R.id.et_nama_perusahaan, EditText.class).getText().length());
                        }
                    });


                   /* spDialogPerusahaan = new SpinnerDialog(getActivity(), perusahaanList, "PILIH NAMA PERUSAHAAN");
                    spDialogPerusahaan.setCancellable(true);
                    spDialogPerusahaan.setShowKeyboard(false);
                    spDialogPerusahaan.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int position) {
                            find(R.id.et_nama_perusahaan, EditText.class).setText(item);
                            spDialogPerusahaan.closeSpinerDialog();
                        }
                    });*/
                    if (result.size() > 0) {

                    }
                }
            }
        });
    }

    public void setSpRek() {
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
                if (result.get("status").asString().equals("OK")) {
                    result = result.get("data");
                    ArrayList<String> str = new ArrayList<>();
                    if (result.size() > 0) {
                        str.add("--PILIH--");
                    }
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
                    spRekAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, str);
                    spRekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRekening.setAdapter(spRekAdapter);
                    spRekAdapter.notifyDataSetChanged();
                    spRekening.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            String item = adapterView.getSelectedItem().toString();
                            if (item.equals(dataRekeningList.get(i).get("COMPARISON").asString())) {
                                String noRek = dataRekeningList.get(i).get("NO_REKENING").asString();
                                String namaBank = dataRekeningList.get(i).get("BANK_NAME").asString();
                                getLastBalanceKas(noRek);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else {
                    setSpRek();
                }
            }
        });
    }

    private void checkTgl() {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewterimapart"), args2));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("TANGGAL_PESAN").asString());
                    data.add(result.get("data").get(i).get("TANGGAL_PENERIMAAN").asString());
                }
            }
        });
    }

    private Nson sendObject() {
        String tglpesan = Tools.setFormatDayAndMonthToDb(tvTglPesan.getText().toString());
        String tglterima = Tools.setFormatDayAndMonthToDb(tvTglTerima.getText().toString());
        String rek = spRekening.getSelectedItem().toString().equals("--PILIH--") ? "" : spRekening.getSelectedItem().toString();
        String jatuhtempo = Tools.setFormatDayAndMonthToDb(tvTglJatuhTempo.getText().toString());
        String tipe = spSupplier.getSelectedItem().toString().toUpperCase();
        String nama = tvNamaSupplier.getText().toString().toUpperCase();
        String nodo = etNoDo.getText().toString().toUpperCase();
        String ongkir = NumberFormatUtils.formatOnlyNumber(etOngkir.getText().toString());
        String pembayaran = spPembayaran.getSelectedItem().toString().toUpperCase();

        Nson nson = Nson.newObject();
        nson.set("nodo", nodo);
        nson.set("tglpesan", tglpesan);
        nson.set("tglterima", tglterima);
        nson.set("ongkir", ongkir);
        nson.set("pembayaran", pembayaran);
        nson.set("jatuhtempo", jatuhtempo);
        nson.set("namaSupplier", nama.replaceAll("[^a-zA-Z]", ""));
        nson.set("noSupplier", nama.replaceAll("[^0-9]", ""));
        nson.set("tipe", tipe);
        nson.set("rekening", rek);
        nson.set("PRINCIPAL", find(R.id.sp_nama_principal, Spinner.class).getSelectedItem().toString().equals("--PILIH--") ? "" : find(R.id.sp_nama_principal, Spinner.class).getSelectedItem().toString());
        nson.set("PERUSAHAAN", etNamaPerusahaan.getText().toString());
        nson.set("NO_TRACE", find(R.id.et_no_trace, EditText.class).getText().toString());
        nson.set("BALANCE", pembayaran.equals("CASH ON DELIVERY") ? lastBalanceKas : pembayaran.equals("TRANSFER") ? lastBalanceKasBank : 0);

        showInfo("Catatkan Detail Part");
        return nson;
    }

    private String getAllNumberFromString(String nama) {
        if (nama == null) return "";
        StringBuilder noSupplier = new StringBuilder();
        for (char number : nama.toCharArray()) {
            if (Character.isDigit(number)) {
                noSupplier.append(number);
            }
        }
        return noSupplier.toString().replaceAll("[^0-9]+", "");
    }

    @SuppressLint({"NonConstantResourceId", "IntentReset"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tglPesan:
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
                        tglPesanTimeMilis[0] = date != null ? date.getTime() : 0;
                        tvTglPesan.setText(formattedTime);
                    }
                }, year, month, day);

                datePickerDialog.setMaxDate(cldr);
                datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
                break;
            case R.id.tglTerima:
                if (tglPesanTimeMilis[0] > 0) {
                    getDatePickerDialogTextView(getActivity(), tvTglTerima, parseTglPesan(tglPesanTimeMilis[0]));
                } else {
                    showWarning("Tanggal Pesan Belum di Input");
                }

                break;
            case R.id.tv_tgl_jatuh_tempo:
                getDatePickerDialogTextView(getActivity(), tvTglJatuhTempo);
                break;
            case R.id.vg_kontak:
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                startActivityForResult(intent, REQUEST_CONTACT);
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnSelanjutnya:
                final String tglpesan = tvTglPesan.getText().toString();
                final String tglterima = tvTglTerima.getText().toString();

                if (tipeSupplier.equals("--PILIH--")) {
                    viewFocus(spSupplier);
                    showWarning("Supplier Harus Di Pilih");
                    return;
                }

                if (!tipeSupplier.equals("PRINCIPAL") && tvNamaSupplier.getText().toString().isEmpty()) {
                    viewFocus(tvNamaSupplier);
                    tvNamaSupplier.setError("Supplier Tidak Boleh Kosong");
                    return;
                }

                if (etNoDo.getText().toString().isEmpty()) {
                    viewFocus(etNoDo);
                    etNoDo.setError("No. DO Tidak Boleh Kosong");
                    return;
                }
                if (tipePembayaran.equalsIgnoreCase("--PILIH--")) {
                    viewFocus(spPembayaran);
                    showWarning("Pembayaran Harus Di Pilih");
                    return;
                }
                if (find(R.id.ly_norek, LinearLayout.class).isEnabled()) {
                    if (spRekening.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spRekening.requestFocus();
                        showWarning("Nomor Rekening Harus di Pilih");
                        return;
                    }
                }

                if (find(R.id.ly_tgl_jatuh_tempo).isEnabled()) {
                    if (tvTglJatuhTempo.getText().toString().isEmpty()) {
                        viewFocus(tvTglJatuhTempo);
                        tvTglJatuhTempo.setError("Masukkan Tanggal Jatuh Tempo");
                        return;
                    }
                }
                if (tglpesan.equalsIgnoreCase("TANGGAL PESAN")) {
                    showWarning("Masukkan Tanggal Pesan");
                    return;
                }
                if (tglterima.equalsIgnoreCase("TANGGAL TERIMA")) {
                    showWarning("Masukkan Tanggal Terima");
                    return;
                }

                try {
                    @SuppressLint("SimpleDateFormat") Date jatuhTempo = new SimpleDateFormat("dd/MM/yyyy").parse(tvTglJatuhTempo.getText().toString());
                    @SuppressLint("SimpleDateFormat") Date tanggalTerima2 = new SimpleDateFormat("dd/MM/yyyy").parse(tglterima);
                    if (find(R.id.ly_tgl_jatuh_tempo).isEnabled()) {
                        if (!jatuhTempo.after(tanggalTerima2)) {
                            showWarning("Tanggal Jatuh Tempo Invoice / Tanggal Terima Tidak Sesuai");
                            return;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (data.contains(tglPesan)) {
                    showInfoDialog("PENERIMAAN PART TELAH TERCATAT SEBELUMNYA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    return;
                }
                if (data.contains(tglTerima)) {
                    showInfoDialog("PENERIMAAN PART TELAH TERCATAT SEBELUMNYA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
                    return;
                }

                if(etNamaPerusahaan.isEnabled() && etNamaPerusahaan.getText().toString().isEmpty()){
                    etNamaPerusahaan.setError("NAMA PERUSAHAAN HARUS DI ISI");
                    viewFocus(etNamaPerusahaan);
                    return;
                }

                Intent i = new Intent(AturTerimaPart_Activity.this, AturDetail_TerimaPart_Activity.class);
                i.putExtra("detail", sendObject().toJson());
                startActivityForResult(i, TerimaPart_Activity.REQUEST_TERIMA_PART);
                break;
        }
    }

    private void getLastBalanceKas(final String noRek) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                args[1] = "noRekeningInternal=" + noRek;
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


    private Calendar parseTglPesan(long tglPesan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(tglPesan);
        return calendar;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TerimaPart_Activity.REQUEST_TERIMA_PART && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data != null ? data.getData() : null;
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                tvNamaSupplier.setText(contactName + "\n" + number);
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_REKENING) {
            dialogInterface.dismiss();
            setSpRek();
        }
    }
}