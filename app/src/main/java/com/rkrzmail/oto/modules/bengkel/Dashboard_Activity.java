package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_DASHBOARD;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class Dashboard_Activity extends AppActivity {

    private String tanggalAkhir="", tglAwal="", totLayanan="", totPart="", totJasapart="", totJasalain="",
    totLainnya="", totDiscount="", totPendapatan="", totDownpayment="", totIncome="", totBiaya="", totHpp="",
    totMargin="", totKas="", totBank="", totPiutang="", totColeection="", totStockpart="", totHutang="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dashboard_angka);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DASHBOARD");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        find(R.id.ly_dashboard, LinearLayout.class).setVisibility(View.GONE);
        find(R.id.ic_mulaitgl, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerAwal();
            }
        });

        find(R.id.ic_selesaitgl, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerAkhir();
            }
        });
    }

    @SuppressLint("NewApi")
    private void viewDashboard() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("kategori", "ANGKA");
                args.put("periodeAwal", setFormatDayAndMonthToDb(tglAwal));
                args.put("periodeAkhir", setFormatDayAndMonthToDb(tanggalAkhir));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_DASHBOARD), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        totLayanan = result.get(i).get("TOTAL_LAYANAN").asString();
                        totPart = result.get(i).get("TOTAL_PART").asString();
                        totJasapart = result.get(i).get("TOTAL_JASA_PART").asString();
                        totJasalain = result.get(i).get("TOTAL_JASA_LAIN").asString();
                        totLainnya = result.get(i).get("TOTAL_LAINYA").asString();
                        totDiscount = result.get(i).get("TOTAL_DISCOUNT").asString();
                        totPendapatan = result.get(i).get("TOTAL_PENDAPATAN").asString();
                        totDownpayment = result.get(i).get("TOTAL_DP").asString();
                        totIncome = result.get(i).get("TOTAL_INCOME").asString();
                        totBiaya = result.get(i).get("TOTAL_BIAYA").asString();
                        totHpp = result.get(i).get("TOTAL_HPP").asString();
                        totMargin = result.get(i).get("TOTAL_MARGIN").asString();
                        totKas = result.get(i).get("TOTAL_KAS").asString();
                        totBank = result.get(i).get("TOTAL_KAS_BANK").asString();
                        totPiutang = result.get(i).get("TOTAL_PIUTANG").asString();
                        totColeection = result.get(i).get("TOTAL_COLLECTION").asString();
                        totStockpart = result.get(i).get("TOTAL_STOCK_PART").asString();
                        totHutang = result.get(i).get("TOTAL_HUTANG").asString();
                    }
                    setValuedashboard();
                    find(R.id.ly_dashboard, LinearLayout.class).setVisibility(View.VISIBLE);
                } else {
                    showInfo(result.get("message").asString());
                }
            }
        });
    }
    private void setValuedashboard(){
        find(R.id.tv_dbLayanan2, TextView.class).setText(setUnderline(RP + formatRp(totLayanan)));
        find(R.id.tv_dbPart, TextView.class).setText(setUnderline(RP + formatRp(totPart)));
        find(R.id.tv_dbJasapart, TextView.class).setText(setUnderline(RP + formatRp(totJasapart)));
        find(R.id.tv_dbJasalainnya, TextView.class).setText(setUnderline(RP + formatRp(totJasalain)));
        find(R.id.tv_dblainnya, TextView.class).setText(setUnderline(RP + formatRp(totLainnya)));
        find(R.id.tv_dbDiscount, TextView.class).setText(setUnderline(RP + formatRp(totDiscount)));
        //find(R.id.tv_dbPendapatan, TextView.class).setText(setUnderline(RP + formatRp(totPendapatan)));
        //find(R.id.tv_d, TextView.class).setText(setUnderline(RP + formatRp(totDownpayment)));
        find(R.id.tv_dbIncome, TextView.class).setText(setUnderline(RP + formatRp(totIncome)));
        find(R.id.tv_dbBiaya, TextView.class).setText(setUnderline(RP + formatRp(totBiaya)));
        find(R.id.tv_dbHpppart, TextView.class).setText(setUnderline(RP + formatRp(totHpp)));
        find(R.id.tv_dbMargin, TextView.class).setText(setUnderline(RP + formatRp(totMargin)));
        find(R.id.tv_dbKas, TextView.class).setText(setUnderline(RP + formatRp(totKas)));
        find(R.id.tv_dbBank, TextView.class).setText(setUnderline(RP + formatRp(totBank)));
        find(R.id.tv_dbPiutang, TextView.class).setText(setUnderline(RP + formatRp(totPiutang)));
        find(R.id.tv_dbColeection, TextView.class).setText(setUnderline(RP + formatRp(totColeection)));
        find(R.id.tv_dbStockpart, TextView.class).setText(setUnderline(RP + formatRp(totStockpart)));
        find(R.id.tv_dbHutang, TextView.class).setText(setUnderline(RP + formatRp(totHutang)));
    }

    public void getDatePickerAwal() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_WEEK);
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
                //tanggalString += formattedTime;
                find(R.id.tv_mulaitgl, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                find(R.id.ic_selesaitgl, TextView.class).setEnabled(true);
                tglAwal =  find(R.id.tv_mulaitgl, TextView.class).getText().toString();
            }
        });

        datePickerDialog.setMaxDate(parseWaktuNow());
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public void getDatePickerAkhir() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_WEEK);
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
                //tanggalAkhir += formattedTime;
                find(R.id.tv_selesaitgl, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    tanggalAkhir = find(R.id.tv_selesaitgl, TextView.class).getText().toString();
                    if (validateDate(tglAwal,tanggalAkhir)) {
                        showWarning("TANGGAL AKHIR TIDAK BOLEH LEBIH KECIL DARI TANGGAL AWAL");
                        find(R.id.tv_selesaitgl, TextView.class).setText("");
                        find(R.id.tv_selesaitgl, TextView.class).performClick();
                    } else {
                        viewDashboard();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, "WAKTU AMBIL: " + waktuLayananHplusExtra);
            }
        });

        datePickerDialog.setMaxDate(parseWaktuNow());
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private boolean validateDate(String tglAwal, String tglAkhir) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date tgl_Awal = sdf.parse(tglAwal);
        Date tgl_Akhir = sdf.parse(tglAkhir);

        if (tgl_Awal.after(tgl_Akhir))
            return true;
        else
            return false;
    }

    private Calendar parseWaktuNow() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long totalDate = current;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

}