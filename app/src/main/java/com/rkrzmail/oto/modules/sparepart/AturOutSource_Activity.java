package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_TUGAS_PART;
import static com.rkrzmail.utils.APIUrls.JURNAL_KAS;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;
import static com.rkrzmail.utils.ConstUtils.REQUEST_REKENING;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturOutSource_Activity extends AppActivity implements View.OnClickListener {

    private Spinner spStatus, spPembayaran, spRekInternal;
    private EditText etBiaya, etKeterangan;

    private final Nson dataRekeningList = Nson.newArray();
    private final Nson outsourceStatusList = Nson.newArray();
    private int checkinDetailID = 0;
    private int outsourceID = 0;
    private int lastBalanceKas = 0, lastBalanceKasBank = 0;

    private String status = "", statusBefore = "";
    private String tipePembayaran = "";
    private String namaBank = "", noRek = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_out_source_);
        initToolbar();
        setComponent();
        getLastBalanceKas("");
        viewStatusAvail();
        loadData();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Out Source");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setComponent() {
        spPembayaran = findViewById(R.id.sp_pembayaran);
        spRekInternal = findViewById(R.id.sp_rek_internal);
        etBiaya = findViewById(R.id.et_biaya_outsource);
        etKeterangan = findViewById(R.id.et_keterangan);
        spStatus = findViewById(R.id.sp_status);

        etBiaya.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiaya));

        find(R.id.vg_tgl_jatuh_tempo).setOnClickListener(this);
        find(R.id.tv_tgl_estimasi).setOnClickListener(this);
        find(R.id.tv_jam_estimasi).setOnClickListener(this);
        find(R.id.vg_kontak).setOnClickListener(this);
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.equals("SELESAI")) {
                    if (etBiaya.getText().toString().isEmpty() || etBiaya.getText().toString().equals("Rp. 0")) {
                        etBiaya.setError("BIAYA HARUS DI ISI");
                        viewFocus(etBiaya);
                    } else if (tipePembayaran.equals("--PILIH--")) {
                        setErrorSpinner(spPembayaran, "PEMBAYARAN HARUS DI PILIH");
                    } else if ((tipePembayaran.equals("TRANSFER") || tipePembayaran.equals("DEBET")) && namaBank.isEmpty()) {
                        setErrorSpinner(spRekInternal, "REKENING INTERNAL HARUS DI PILIH");
                    } else if (tipePembayaran.equals("INVOICE") && find(R.id.tv_tgl_jatuh_tempo, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.tv_tgl_jatuh_tempo, TextView.class).setError("TANGGAL JATUH TEMPO HARUS DI ISI");
                        viewFocus(find(R.id.tv_tgl_jatuh_tempo, TextView.class));
                    } else {
                        saveData();
                    }
                } else {
                    saveData();
                }
            }
        });

        spPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                tipePembayaran = parent.getItemAtPosition(position).toString();
                Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), status.equals("ORDER") && tipePembayaran.equals("INVOICE"));
                Tools.setViewAndChildrenEnabled(find(R.id.vg_no_rek, LinearLayout.class), status.equals("ORDER") && (tipePembayaran.equals("TRANSFER") || tipePembayaran.equals("DEBET")));

                if (tipePembayaran.equalsIgnoreCase("TRANSFER")) {
                    if (spRekInternal.getCount() == 0) {
                        showInfoDialog("Konfirmasi", "Rekening Belum Di tambah, Tambah Rekening ? ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturRekening_Activity.class), REQUEST_REKENING);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (!data.asString().isEmpty()) {
            try {
                String[] splitEstimasi = data.get("ESTIMASI_SELESAI").asString().split(" ");
                find(R.id.tv_tgl_estimasi, TextView.class).setText(splitEstimasi[0]);
                find(R.id.tv_jam_estimasi, TextView.class).setText(splitEstimasi[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            outsourceID = data.get("OUTSOURCE_ID").asInteger();
            find(R.id.tv_nama_supplier, TextView.class).setText(data.get("NAMA_SUPPLIER").asString() + "\n" + data.get("NO_PONSEL_SUPPLIER").asString());
            etBiaya.setText(RP + NumberFormatUtils.formatRp(data.get("BIAYA_OUTSOURCE").asString()));
            etKeterangan.setText(data.get("KETERANGAN").asString());
        }

        setSpStatus(data.get("STATUS").asString());
    }

    private void setSpStatus(final String loadStatus) {
        final List<String> statusList = Arrays.asList(getResources().getStringArray(R.array.status_out_source));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, statusList) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = null;
                if (outsourceStatusList.size() > 0) {
                    for (int i = 0; i < outsourceStatusList.size(); i++) {
                        if (outsourceStatusList.get(i).get("STATUS").asString().equals(statusList.get(position))) {
                            TextView mTextView = new TextView(getContext());
                            mTextView.setVisibility(View.GONE);
                            mTextView.setHeight(0);
                            view = mTextView;
                            return view;
                        } else {
                            view = super.getDropDownView(position, null, parent);
                        }
                    }
                } else {
                   /* if (!loadStatus.isEmpty()) {
                        if (statusList.get(position).equals(loadStatus)) {
                            TextView mTextView = new TextView(getContext());
                            mTextView.setVisibility(View.GONE);
                            mTextView.setHeight(0);
                            view = mTextView;
                            return view;
                        } else {
                            view = super.getDropDownView(position, null, parent);
                        }
                    } else {
                    }*/
                    view = super.getDropDownView(position, null, parent);
                }

                return view;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(spinnerAdapter);
        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                status = adapterView.getItemAtPosition(i).toString();

                Tools.setViewAndChildrenEnabled(find(R.id.vg_pembayaran, LinearLayout.class), status.equals("SELESAI"));
                Tools.setViewAndChildrenEnabled(find(R.id.ly_estimasi_selesai, LinearLayout.class), status.equals("ORDER"));
                Tools.setViewAndChildrenEnabled(find(R.id.vg_kontak, LinearLayout.class), status.equals("ORDER"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    private void viewStatusAvail() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "OUTSOURCE STATUS");
                args.put("outsourceID", String.valueOf(outsourceID));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    outsourceStatusList.asArray().clear();
                    outsourceStatusList.asArray().addAll(result.asArray());
                } else {
                    showError("STATUS OUTSOURCE GAGAL DI MUAT");
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
                    getLastBalanceKas(noRek);
                } else {
                    noRek = "";
                    namaBank = "";
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


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String estimasiSelesai = find(R.id.tv_tgl_estimasi, TextView.class).getText().toString() + " " +
                        find(R.id.tv_jam_estimasi, TextView.class).getText().toString();

                args.put("action", "add");
                args.put("group", "OUTSOURCE");
                args.put("outsourceID", String.valueOf(outsourceID));
                args.put("status", status);
                args.put("estimasiSelesai", estimasiSelesai);
                args.put("namaSupplier", NumberFormatUtils.formatOnlyCharacter(find(R.id.tv_nama_supplier, TextView.class).getText().toString()));
                args.put("noPonselSupplier", NumberFormatUtils.formatOnlyNumber(find(R.id.tv_nama_supplier, TextView.class).getText().toString()));
                args.put("biayaOutsource", NumberFormatUtils.formatOnlyNumber(etBiaya.getText().toString()));
                args.put("pembayaran", tipePembayaran.equals("--PILIH--") ? "" : tipePembayaran);
                args.put("noRekeningInternal", noRek);
                args.put("jatuhTempo", find(R.id.tv_tgl_jatuh_tempo, TextView.class).getText().toString());
                args.put("keterangan", etKeterangan.getText().toString());
                args.put("namaBankInternal", namaBank);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_TUGAS_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("SUKSES MENAMBAHKAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint({"NonConstantResourceId", "IntentReset"})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tgl_estimasi:
                getDatePickerEstimasiSelesai();
                break;
            case R.id.tv_jam_estimasi:
                getTimePickerEstimasiSelesai();
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
                getDatePickerDialogTextView(getActivity(), find(R.id.tv_tgl_jatuh_tempo, TextView.class));
                break;
        }
    }

    public void getDatePickerEstimasiSelesai() {
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

                find(R.id.tv_tgl_estimasi, TextView.class).setText(sdf.format(date));
            }
        }, year, month, day);

        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public void getTimePickerEstimasiSelesai() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        @SuppressLint("SimpleDateFormat") final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                String time = hourOfDay + ":" + minute;
                Date date = null;
                try {
                    date = sdf.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                find(R.id.tv_jam_estimasi, TextView.class).setText(sdf.format(date));
            }
        }, currentHour, currentMinute, true);

        //timePickerDialog.setMinTime();
        timePickerDialog.setTitle("Pilih Jam");
        timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                find(R.id.tv_nama_supplier, TextView.class).setText(contactName + "\n" + number);
            }
        } else if (requestCode == REQUEST_REKENING) {
            setSpRek("");
        }
    }
}
