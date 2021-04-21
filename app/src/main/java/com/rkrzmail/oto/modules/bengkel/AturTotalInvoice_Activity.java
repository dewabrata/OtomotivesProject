package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static com.rkrzmail.utils.APIUrls.PIUTANG;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturTotalInvoice_Activity extends AppActivity {

    //NO INVOICE INV/DD-MM/12
    private Nson data;
    private Nson idPembayaranList = Nson.newArray();
    private final Nson kasPiutangIDList = Nson.newArray();

    private StringBuilder idCheckin = new StringBuilder();
    private String namaPelanggan = "";
    private String noInv = "";
    private String namaPrincipal = "";
    private String tglBayar = "";
    private String namaBank = "", noRek = "", tipePemabayaran = "";
    private String jenisTransaksi = "";
    private String jenisPiutang = "";

    private int totalInvPart = 0, totalInvLayanan = 0;
    private int totalInvoice = 0;
    private int noInvBerikutnya = 0;
    private int totalBiaya = 0;
    private int
            totalFeeNonPaket = 0, totalPenggantianPart = 0, totalGrandTotal = 0,
            totalTotalDue = 0, totalJasaLain = 0, totalJasaPart = 0,
            totalHargaPart = 0, totalDiscPart = 0, totalDiscJasaLain = 0,
            totalDiscJasaPart = 0, totalDiscLayanan = 0, totalLayanan = 0,
            netPart = 0, netJasaLain = 0, netLayanan = 0, netJasaPart = 0, totalPPN = 0, totalPembayaran = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_total_invoice);
        initToolbar();
        initData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Total Invoice");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        data = Nson.readJson(getIntentStringExtra(DATA));
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            count++;
            jenisPiutang = data.get(i).get("JENIS_PIUTANG").asString();
            if(!data.get(i).get("PIHUTANG_ID").asString().isEmpty()){
                kasPiutangIDList.add(Nson.newObject().set("KAS_PIUTANG_ID", data.get(i).get("PIHUTANG_ID").asInteger()));
            }
            if(data.get(i).get("JENIS_PIUTANG").asString().equals("INVOICE")){
                totalGrandTotal += data.get(i).get("GRAND_TOTAL").asInteger();
                totalTotalDue += data.get(i).get("TOTAL_DUE").asInteger();
                totalPPN += data.get(i).get("PPN").asInteger();
                totalPembayaran += data.get(i).get("JUMLAH_PEMBAYARAN").asInteger();
                totalBiaya += data.get(i).get("TOTAL_BIAYA").asInteger();

                totalJasaLain += data.get(i).get("BIAYA_JASA_LAIN").asInteger();
                totalDiscJasaLain += data.get(i).get("DISCOUNT_JASA_LAIN").asInteger();
                netJasaLain += data.get(i).get("BIAYA_JASA_LAIN_NET").asInteger();

                totalHargaPart += data.get(i).get("HARGA_PART").asInteger();
                totalDiscPart += data.get(i).get("DISCOUNT_PART").asInteger();
                netPart += data.get(i).get("HARGA_PART_NET").asInteger();
                totalJasaPart += data.get(i).get("BIAYA_JASA_PART").asInteger();
                totalDiscJasaPart += data.get(i).get("DISCOUNT_JASA_PART").asInteger();
                netJasaPart += data.get(i).get("BIAYA_JASA_PART_NET").asInteger();

                totalDiscLayanan += data.get(i).get("DISCOUNT_LAYANAN").asInteger();
                totalLayanan += data.get(i).get("BIAYA_LAYANAN").asInteger();
                netLayanan += data.get(i).get("BIAYA_LAYANAN_NET").asInteger();
            }else{
                totalFeeNonPaket += data.get(i).get("FEE_NON_PAKET").asInteger();
                totalPenggantianPart += data.get(i).get("PENGGANTIAN_PART").asInteger();
            }

            tipePemabayaran = data.get(i).get("TIPE_PEMBAYARAN").asString();
            namaBank = data.get(i).get("BANK_REK_INTERNAL").asString();
            noRek = data.get(i).get("NO_REK_INTERNAL").asString();
            tglBayar = data.get(i).get("TANGGAL_PEMBAYARAN").asString();

            if (idCheckin.length() > 0) idCheckin.append("-");

            jenisTransaksi = data.get(i).get("JENIS_TRANSAKSI").asString();
            idCheckin.append(data.get(i).get("CHECKIN_ID").asInteger());
            namaPelanggan = data.get(i).get("NAMA_PELANGGAN").asString();
            namaPrincipal = data.get(i).get("PRINCIPAL").asString();
            idPembayaranList.add(Nson.newObject()
                    .set("PEMBAYARAN_ID", data.get(i).get("PEMBAYARAN_ID").asString())
            );
        }
        namaPelanggan = count > 1 ? "" : namaPelanggan;
        totalInvoice = getIntentIntegerExtra("TOTAL_INV");
        final String cid = UtilityAndroid.getSetting(getApplicationContext(), "CID", "").substring(UtilityAndroid.getSetting(getApplicationContext(), "CID", "").length() - 4);
        final String kodeTransaksi;

        find(R.id.et_total_invoice, EditText.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(totalInvoice)));
        find(R.id.tv_tgl_jatuh_tempo, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDatePicker();
            }
        });

        if(jenisPiutang.equals("AFTER SALES SERVIS")){
            if(jenisTransaksi.equals("JUAL PART")){
                kodeTransaksi = "J";
                noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/P";
                find(R.id.cb_part_inv, CheckBox.class).setEnabled(true);
            }else{
                kodeTransaksi = jenisTransaksi.equals("CHECKIN") ? "C" : "P";
                if(totalFeeNonPaket > 0){
                    noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/L";
                    find(R.id.cb_layanan_inv, CheckBox.class).setChecked(true);
                    find(R.id.cb_layanan_inv, CheckBox.class).setEnabled(true);
                }else{
                    find(R.id.cb_layanan_inv, CheckBox.class).setChecked(false);
                    find(R.id.cb_layanan_inv, CheckBox.class).setEnabled(false);
                }

                if(totalPenggantianPart > 0){
                    if(noInv.isEmpty()){
                        noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/P";
                    }else{
                        noInv += "-P";
                    }
                    find(R.id.cb_part_inv, CheckBox.class).setChecked(true);
                    find(R.id.cb_part_inv, CheckBox.class).setEnabled(true);
                }else{
                    find(R.id.cb_part_inv, CheckBox.class).setChecked(false);
                    find(R.id.cb_part_inv, CheckBox.class).setEnabled(false);
                }
            }


            find(R.id.cb_layanan_inv, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        totalInvoice += totalFeeNonPaket;
                        noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/L";
                        if(find(R.id.cb_part_inv, CheckBox.class).isChecked()){
                            noInv += "-P";
                        }
                    }else{
                        totalInvoice -= totalFeeNonPaket;
                        noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/L";
                        if(find(R.id.cb_part_inv, CheckBox.class).isChecked()){
                            noInv += "-P";
                        }
                        if(noInv.contains("-P")){
                            noInv = noInv.replace("/L-", "/");
                        }else{
                            noInv = noInv.replace("/L", "");
                        }
                    }
                    find(R.id.et_total_invoice, EditText.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(totalInvoice)));
                    find(R.id.et_no_invoice, EditText.class).setText(noInv);
                }
            });

            find(R.id.cb_part_inv, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        totalInvoice += totalPenggantianPart;
                        if(noInv.isEmpty()){
                            noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/P";
                        }else{
                            if(!noInv.contains("/L")){
                                noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice + "/P";
                            }else{
                                noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice +  "/L-P";
                            }
                        }
                    }else{
                        totalInvoice -= totalPenggantianPart;
                        noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid + "/" + kodeTransaksi + "-" + totalInvoice;
                        if(find(R.id.cb_layanan_inv, CheckBox.class).isChecked()){
                            noInv += "/L";
                        }
                    }
                    find(R.id.et_total_invoice, EditText.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(totalInvoice)));
                    find(R.id.et_no_invoice, EditText.class).setText(noInv);
                }
            });
        }else{
            noInv = "INV" + "/" + currentDateTime("ddMMyyyy") + "/" + cid  + "-" + totalInvoice ;
        }

        find(R.id.et_no_invoice, EditText.class).setText(noInv);
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(find(R.id.et_total_invoice, EditText.class).getText().toString().equals("Rp. 0")){
                    viewFocus(find(R.id.et_total_invoice, EditText.class));
                    find(R.id.et_total_invoice, EditText.class).setError("PILIH SALAH SATU INVOICE");
                }else{
                    saveData();
                }
            }
        });
    }

    private void getDatePicker() {
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
                find(R.id.tv_tgl_jatuh_tempo, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    @SuppressLint("NewApi")
    private int generateRandomInt() {
        Random rn = new Random();
        int range = 20 - 1 + 1;
        return rn.nextInt(range) + 1;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("kasPiutangID", kasPiutangIDList.toJson());
                args.put("jenis", "TOTAL INVOICE");
                args.put("noInvoiceLayanan", find(R.id.cb_layanan_inv, CheckBox.class).isChecked() ? noInv : "");
                args.put("noInvoicePart", find(R.id.cb_part_inv, CheckBox.class).isChecked() ? noInv : "");
                args.put("tglJatuhTempo", DateFormatUtils.formatDate(find(R.id.tv_tgl_jatuh_tempo, TextView.class).getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd"));
                args.put("namaCustomer", namaPelanggan);
                args.put("namaPrincipal", namaPrincipal);
                args.put("biayaLayanan", String.valueOf(totalLayanan));
                args.put("feeNonPaket", find(R.id.cb_layanan_inv, CheckBox.class).isChecked() ? String.valueOf(totalFeeNonPaket) :  String.valueOf(0));
                args.put("discLayanan", String.valueOf(totalDiscLayanan));
                args.put("netLayanan", String.valueOf(totalLayanan));
                args.put("hargaPart", String.valueOf(totalHargaPart));
                args.put("penggantianPart", find(R.id.cb_layanan_inv, CheckBox.class).isChecked() ? String.valueOf(totalPenggantianPart) : String.valueOf(0));
                args.put("discPart", String.valueOf(totalDiscPart));
                args.put("netPart", String.valueOf(netPart));
                args.put("jasaPart", String.valueOf(totalJasaPart));
                args.put("discJasaPart", String.valueOf(totalDiscJasaPart));
                args.put("netJasaPart", String.valueOf(netJasaPart));
                args.put("jasaLain", String.valueOf(totalJasaLain));
                args.put("discJasaLain", String.valueOf(totalDiscJasaLain));
                args.put("netJasaLain", String.valueOf(netJasaLain));
                args.put("totalDue", jenisPiutang.equals("AFTER SALES SERVIS") ? String.valueOf(totalInvoice) : String.valueOf(totalTotalDue));
                args.put("ppn", String.valueOf(totalPPN));
                args.put("grandTotal",  jenisPiutang.equals("AFTER SALES SERVIS") ? String.valueOf(totalInvoice) : String.valueOf(totalGrandTotal));
                args.put("idPembayaranList", idPembayaranList.toJson());

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