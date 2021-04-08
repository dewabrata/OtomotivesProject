package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.PIUTANG;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturPembayaranInvoice_Activity extends AppActivity {

    private Spinner spNoRek, spTipePembayaran;

    private boolean isTglJatuhTempo = false;
    private String noRek = "", namaBank = "";
    private String tipePembayaran = "";
    private Nson rekeningList = Nson.newArray();

    private String jenisTransaksi = "";
    private String namaPelanggan = "";
    private String noInv = "";
    private String namaPrincipal = "";
    private String tglBayar = "";
    private String tipePemabayaran = "";
    private int idPihutang = 0;
    private int totalInvoice = 0;
    private int noInvBerikutnya = 0;
    private int frekwensi = 0;
    private int
            totalFeeNonPaket = 0, totalPenggantianPart = 0, totalGrandTotal = 0,
            totalTotalDue = 0, totalJasaLain = 0, totalJasaPart = 0,
            totalHargaPart = 0, totalDiscPart = 0, totalDiscJasaLain = 0,
            totalDiscJasaPart = 0, totalDiscLayanan = 0, totalLayanan = 0,
            netPart = 0, netJasaLain = 0, netLayanan = 0, netJasaPart = 0, totalPPN = 0, totalPembayaran = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_pembayaran_invoice);
        initToolbar();
        initComponent();
        initData();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pembayaran Invoice");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spNoRek = findViewById(R.id.sp_norek);
        spTipePembayaran = findViewById(R.id.sp_tipe_pembayaran);
        find(R.id.tv_tgl_bayar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDatePicker(true);
            }
        });

        find(R.id.tv_tgl_jatuh_tempo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTglJatuhTempo) {
                    getDatePicker(false);
                } else {
                    showWarning("TANGGAL JATUH TEMPO TIDAK TERSEDIA", Toast.LENGTH_LONG);
                }
            }
        });

        find(R.id.et_total_bayar, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_total_bayar, EditText.class)));
        find(R.id.et_total_bayar, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) return;

                String totalInv = find(R.id.et_total_invoice, EditText.class).getText().toString();
                totalInv = NumberFormatUtils.formatOnlyNumber(totalInv);
                text = NumberFormatUtils.formatOnlyNumber(text);

                if(!text.isEmpty()){
                    int totalBayar = Integer.parseInt(text);
                    int totalTagihan = Integer.parseInt(totalInv);
                   /* if (totalBayar < totalTagihan) {
                        find(R.id.tl_total_bayar, TextInputLayout.class).setError("TOTAL BAYAR TIDAK VALID");
                    } else {
                        find(R.id.tl_total_bayar, TextInputLayout.class).setErrorEnabled(false);
                    }*/

                    int selisih = totalBayar - Integer.parseInt(totalInv);
                    //selisih = selisih > totalBayar ? 0 : selisih;
                    isTglJatuhTempo = selisih > 5000;
                    find(R.id.et_selisih, EditText.class).setText(RP + formatRp(String.valueOf(selisih)));
                }
            }
        });

        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tipePembayaran = adapterView.getItemAtPosition(i).toString();
                if (tipePembayaran.equals("CASH")) {
                    spNoRek.setSelection(0);
                    spNoRek.setEnabled(false);
                } else if (tipePembayaran.equals("TRANSFER")) {
                    spNoRek.setEnabled(true);
                    setSpRek();
                } else {
                    spNoRek.setSelection(0);
                    spNoRek.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tipePembayaran.equals("--PILIH--")) {
                    showWarning("TIPE PEMBAYARAN HARUS DI PILIH", Toast.LENGTH_LONG);
                } else {
                    if (tipePembayaran.equals("TRANSFER") && spNoRek.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("Rekening Internal Belum di Pilih");
                        spNoRek.performClick();
                    } else {
                        if (find(R.id.et_total_bayar, EditText.class).getText().toString().isEmpty() ||
                                formatOnlyNumber(find(R.id.et_total_bayar, EditText.class).getText().toString()).equals("0") ||
                                find(R.id.tl_total_bayar, TextInputLayout.class).isErrorEnabled()) {
                            find(R.id.et_total_bayar, EditText.class).setError("TOTAL  BAYAR HARUS DI ISI");
                        } else {
                            saveData();
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));

        idPihutang =  data.get("PIHUTANG_ID").asInteger();
        frekwensi = data.get("FREKWENSI").asInteger();
        jenisTransaksi = data.get("JENIS_TRANSAKSI").asString();
        totalFeeNonPaket = data.get("FEE_NON_PAKET").asInteger();
        totalPenggantianPart = data.get("PENGGANTIAN_PART").asInteger();
        totalGrandTotal = data.get("GRAND_TOTAL").asInteger();
        totalTotalDue = data.get("TOTAL_DUE").asInteger();
        totalPPN = data.get("PPN").asInteger();
        totalPembayaran = data.get("JUMLAH_PEMBAYARAN").asInteger();
        totalJasaLain = data.get("BIAYA_JASA_LAIN").asInteger();
        totalDiscJasaLain = data.get("DISCOUNT_JASA_LAIN").asInteger();
        netJasaLain = data.get("BIAYA_JASA_LAIN_NET").asInteger();
        totalHargaPart = data.get("HARGA_PART").asInteger();
        totalDiscPart = data.get("DISCOUNT_PART").asInteger();
        netPart = data.get("HARGA_PART_NET").asInteger();
        totalJasaPart = data.get("BIAYA_JASA_PART").asInteger();
        totalDiscJasaPart = data.get("DISCOUNT_JASA_PART").asInteger();
        netJasaPart = data.get("BIAYA_JASA_PART_NET").asInteger();
        totalDiscLayanan = data.get("DISCOUNT_LAYANAN").asInteger();
        totalLayanan = data.get("BIAYA_LAYANAN").asInteger();
        netLayanan = data.get("BIAYA_LAYANAN_NET").asInteger();
        tipePemabayaran = data.get("TIPE_PEMBAYARAN").asString();
        namaBank = data.get("BANK_REK_INTERNAL").asString();
        noRek = data.get("NO_REK_INTERNAL").asString();
        tglBayar = data.get("TANGGA_PEMBAYARAN").asString();
        namaPelanggan = data.get("NAMA_PELANGGAN").asString();
        namaPrincipal = data.get("PRINCIPAL").asString();

        if(data.get("JUMLAH_INVOICE").asString().isEmpty()){
            totalInvoice = data.get("GRAND_TOTAL").asInteger();
        }else{
            totalInvoice = data.get("JUMLAH_INVOICE").asInteger();
        }
        find(R.id.et_total_invoice, EditText.class).setText(RP + formatRp(String.valueOf(totalInvoice)));
        setSpinnerOffline(Arrays.asList("--PILIH--", "CASH", "TRANSFER"), spTipePembayaran, "");
    }

    private void getDatePicker(final boolean isTglBayar) {
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
                if (isTglBayar)
                    find(R.id.tv_tgl_bayar, TextView.class).setText(formattedTime);
                else
                    find(R.id.tv_tgl_jatuh_tempo, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        if (isTglBayar)
            datePickerDialog.setMaxDate(cldr);
        else
            datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
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
                ArrayList<String> str = new ArrayList<>();
                result = result.get("data");
                str.add("--PILIH--");
                rekeningList.add("");
                for (int i = 0; i < result.size(); i++) {
                    rekeningList.add(Nson.newObject()
                            .set("ID", result.get(i).get("ID"))
                            .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                            .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                            .set("EDC", result.get(i).get("EDC_ACTIVE"))
                            .set("OFF_US", result.get(i).get("OFF_US"))
                            .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                    str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                }

                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spNoRek.setAdapter(adapter);
            }
        });

        spNoRek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(rekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = rekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = rekeningList.get(i).get("BANK_NAME").asString();
                } else {
                    noRek = "";
                    namaBank = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String noPiutangBerikutnya = "";
                int totalPiutangBerikutnya = 0;
                if(isTglJatuhTempo){
                    frekwensi++;
                    noPiutangBerikutnya = String.valueOf(frekwensi);
                    totalPiutangBerikutnya = Integer.parseInt(NumberFormatUtils.formatOnlyNumber(find(R.id.et_selisih, EditText.class).getText().toString()));
                }

                args.put("action", "add");
                args.put("jenisTransaksi", jenisTransaksi);
                args.put("jenis", "PEMBAYARAN INVOICE");
                args.put("idPihutang", String.valueOf(idPihutang));
                args.put("tglJatuhTempo",
                        DateFormatUtils
                                .formatDate(find(R.id.tv_tgl_jatuh_tempo, TextView.class)
                                        .getText()
                                        .toString(), "dd/MM/yyyy", "yyyy-MM-dd"));
                args.put("tanggalBayar",
                        DateFormatUtils.formatDate(find(R.id.tv_tgl_bayar, TextView.class)
                                .getText()
                                .toString(), "dd/MM/yyyy", "yyyy-MM-dd"));
                args.put("tipePembayaran", tipePembayaran);
                args.put("jumlahPembayaran", NumberFormatUtils.formatOnlyNumber(find(R.id.et_total_bayar, EditText.class).getText().toString()));
                args.put("namaBankRekeningInternal", namaBank);
                args.put("nomorRekeningInternal", noRek);
                args.put("total", String.valueOf(totalInvoice));
                args.put("frekwensi", String.valueOf(frekwensi));
                args.put("balance", String.valueOf(totalPiutangBerikutnya));
                args.put("noPiutangBerikutnya", noPiutangBerikutnya);
                args.put("jumlahPiutangBerikutya", String.valueOf(totalPiutangBerikutnya));
                args.put("typePiutang", jenisTransaksi);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(PIUTANG), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("SUKSES MENAMBAHKAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }


}