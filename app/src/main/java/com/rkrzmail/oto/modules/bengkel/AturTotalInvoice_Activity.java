package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NumberFormatUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.PIUTANG;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturTotalInvoice_Activity extends AppActivity {

    //NO INVOICE INV/DD-MM/12
    private Nson data;
    private Nson idPembayaranList = Nson.newArray();

    private StringBuilder idCheckin = new StringBuilder();
    private String namaPelanggan = "";
    private String noInv = "";
    private String namaPrincipal = "";
    private String tglBayar = "";
    private String namaBank = "", noRek = "", tipePemabayaran = "";
    private int totalInvoice = 0;
    private int noInvBerikutnya = 0;
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
        for (int i = 0; i < data.size(); i++) {
            totalFeeNonPaket += data.get(i).get("FEE_NON_PAKET").asInteger();
            totalPenggantianPart += data.get(i).get("PENGGANTIAN_PART").asInteger();
            totalGrandTotal += data.get(i).get("GRAND_TOTAL").asInteger();
            totalTotalDue += data.get(i).get("TOTAL_DUE").asInteger();
            totalPPN += data.get(i).get("PPN").asInteger();
            totalPembayaran += data.get(i).get("JUMLAH_PEMBAYARAN").asInteger();

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

            tipePemabayaran = data.get(i).get("TIPE_PEMBAYARAN").asString();
            namaBank = data.get(i).get("BANK_REK_INTERNAL").asString();
            noRek = data.get(i).get("NO_REK_INTERNAL").asString();
            tglBayar = data.get(i).get("TANGGA_PEMBAYARAN").asString();

            if (idCheckin.length() > 0) idCheckin.append("-");

            idCheckin.append(data.get(i).get("CHECKIN_ID").asInteger());
            namaPelanggan = data.get(i).get("NAMA_PELANGGAN").asString();
            namaPrincipal = data.get(i).get("PRINCIPAL").asString();
            idPembayaranList.add(Nson.newObject()
                    .set("PEMBAYARAN_ID", data.get(i).get("PEMBAYARAN_ID").asString())
            );
        }

        noInv = "INV" + "/" + currentDateTime("dd/MM") + "/" + idCheckin + "/" + 1;
        totalInvoice = getIntentIntegerExtra("TOTAL_INV");
        find(R.id.et_total_invoice, EditText.class).setText(RP + NumberFormatUtils.formatRp(String.valueOf(totalInvoice)));
        find(R.id.et_no_invoice, EditText.class).setText(noInv);
        find(R.id.tv_tgl_jatuh_tempo, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDatePicker();
            }
        });
        find(R.id.btn_simpan, Button.class).setText("XLS INVOICE");
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
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


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jenis", "TOTAL INVOICE");
                args.put("noInvoice", noInv);
                args.put("tglJatuhTempo", DateFormatUtils.formatDate(find(R.id.tv_tgl_jatuh_tempo, TextView.class).getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd"));
                args.put("namaPrincipal", namaPrincipal);
                args.put("biayaLayanan", String.valueOf(totalLayanan));
                args.put("feeNonPaket", String.valueOf(totalFeeNonPaket));
                args.put("discLayanan", String.valueOf(totalDiscLayanan));
                args.put("netLayanan", String.valueOf(totalLayanan));
                args.put("hargaPart", String.valueOf(totalHargaPart));
                args.put("penggantianPart", String.valueOf(totalPenggantianPart));
                args.put("discPart", String.valueOf(totalDiscPart));
                args.put("netPart", String.valueOf(netPart));
                args.put("jasaPart", String.valueOf(totalJasaPart));
                args.put("discJasaPart", String.valueOf(totalDiscJasaPart));
                args.put("netJasaPart", String.valueOf(netJasaPart));
                args.put("jasaLain", String.valueOf(totalJasaLain));
                args.put("discJasaLain", String.valueOf(totalDiscJasaLain));
                args.put("netJasaLain", String.valueOf(netJasaLain));
                args.put("totalDue", "");//String.valueOf(totalTotalDue)
                args.put("ppn", "");//String.valueOf(totalPPN)
                args.put("grandTotal", String.valueOf(totalGrandTotal));
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