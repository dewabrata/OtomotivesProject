package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Dashboard_MainTab_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.rkrzmail.utils.APIUrls.VIEW_DASHBOARD;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class Dashboard_Angka_Fragment extends Fragment {

    private RecyclerView rvPembayaranCheckin;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout lyDasboard;
    private TextView
            tvTglMulai, tvTglAkhir, tvHariKerja, tvRPLayanan, tvRPJualPart,
            tvRPBatal, tvRpPenjualanHarian, tvUnitHarianMargin, tvPercentMarginPart,
            tvRataRataCheckin, tvRataRataJualPart, tvPending, tvPartKosong, tvClaim, tvOutsource,
            tvBelanjaPart, tvHutangKomisi, tvHutangPPN;
    private View fragmentView;

    private Nson pembayaranList = Nson.newArray();

    private String argsRefresh = "", idCheckin = "";
    private AppActivity activity;
    private String tanggalAkhir = "", tglAwal = "", totLayanan = "", totPart = "", totJasapart = "", totJasalain = "",
            totLainnya = "", totDiscount = "", totalPenjualan = "", totDownpayment = "", totIncome = "", totBiaya = "", totHpp = "",
            totMargin = "", totKas = "", totPiutang = "", totColeection = "", totStockpart = "", totHutang = "";
    private int totalHariKerja = 0, jumlahLayanan = 0, totalJualPart = 0,
            totalTidakMenunggu = 0, totalMenunggu = 0, totalProgress = 0,
            totalSelesai = 0, totalSaldoEpay = 0, totalPartOnline = 0, totalAsset = 0,
            totalPendapatanLain = 0, totalDonasi = 0, totalPenjualanHarian = 0,
            totalRataRataCheckin = 0, totalRataRataJualPart = 0, totalOutsource = 0,
            totalClaim = 0, totalPartKosong = 0, totalBelanjaPart = 0,
            totalHutangKomisi = 0, totalHutangPPN = 0, totalBatal = 0, totalPenyusutan = 0,  totalRugiLaba = 0;

    private double totalPercentMarginPartHarian = 0, totalUnitHarian = 0,
            totalPending = 0,  totalKasBank = 0, totalProfitMargin = 0;

    public Dashboard_Angka_Fragment() {

    }

    public static Dashboard_Angka_Fragment newInstance(String param1, String param2) {
        Dashboard_Angka_Fragment fragment = new Dashboard_Angka_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            argsRefresh = getArguments().toString();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_dashboard_angka, container, false);
        activity = ((Dashboard_MainTab_Activity) getActivity());
        //expandLayout();
        initHideToolbar();
        initComponent();
        return fragmentView;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (isVisible()) {
//            viewDashboard((Dashboard_MainTab_Activity) Objects.requireNonNull(getActivity()));
//        }
//    }

    private void expandLayout(){
        final ExpandableLayout expandTransaksiHarian = fragmentView.findViewById(R.id.expand_transaksi_harian);
        final ExpandableLayout expandProses = fragmentView.findViewById(R.id.expand_proses);

        final TextView tvTransaksiHarian = fragmentView.findViewById(R.id.tv_expand_transaksi_harian);
        final TextView tvProses = fragmentView.findViewById(R.id.tv_expand_proses);

        tvProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(expandProses.isExpanded()){
                    expandProses.collapse();
                }else{
                    expandProses.expand();
                }
            }
        });

        tvTransaksiHarian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(expandTransaksiHarian.isExpanded()){
                    expandTransaksiHarian.collapse();
                }else{
                    expandTransaksiHarian.expand();
                }
            }
        });
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        tvTglAkhir = fragmentView.findViewById(R.id.tv_selesaitgl);
        tvTglMulai = fragmentView.findViewById(R.id.tv_mulaitgl);
        lyDasboard = fragmentView.findViewById(R.id.ly_dashboard);
        tvRPBatal = fragmentView.findViewById(R.id.tv_dbBatal);
        tvHariKerja = fragmentView.findViewById(R.id.tv_total_hari_kerja);
        tvRPJualPart = fragmentView.findViewById(R.id.tv_dbJualpart);
        tvRPLayanan = fragmentView.findViewById(R.id.tv_dbLayanan1);
        tvRpPenjualanHarian = fragmentView.findViewById(R.id.tv_total_penjualan_harian);
        tvUnitHarianMargin = fragmentView.findViewById(R.id.tv_total_unit_harian);
        tvPercentMarginPart = fragmentView.findViewById(R.id.tv_total_margin_part);
        tvRataRataJualPart = fragmentView.findViewById(R.id.tv_rata_rata_jual_part);
        tvRataRataCheckin = fragmentView.findViewById(R.id.tv_rata_rata_checkin);
        tvPending = fragmentView.findViewById(R.id.tv_total_pending);
        tvClaim = fragmentView.findViewById(R.id.tv_total_claim);
        tvPartKosong = fragmentView.findViewById(R.id.tv_total_part_kosong);
        tvOutsource = fragmentView.findViewById(R.id.tv_total_outsource);
        tvBelanjaPart = fragmentView.findViewById(R.id.tv_total_belanja_part);
        tvHutangKomisi = fragmentView.findViewById(R.id.tv_total_hutang_komisi);
        tvHutangPPN = fragmentView.findViewById(R.id.tv_total_hutang_ppn);

        tvTglMulai.setText(firstDate());
        tvTglAkhir.setText(activity.currentDateTime("dd/MM/yyyy"));

        fragmentView.findViewById(R.id.ic_mulaitgl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerAwal();
            }
        });

        fragmentView.findViewById(R.id.ic_selesaitgl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerAkhir();
            }
        });

        fragmentView.findViewById(R.id.btn_tampilkan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.find(R.id.tv_mulaitgl, TextView.class).getText().toString().isEmpty()) {
                    activity.showWarning("TANGGAL MULAI HARUS DI MASUKKAN");
                } else if (activity.find(R.id.tv_selesaitgl, TextView.class).getText().toString().isEmpty()) {
                    activity.showWarning("TANGGAL AKHIR HARUS DI MASUKKAN");
                } else {
                    viewDashboard(activity);
                }
            }
        });
    }

    public String firstDate() {
        Calendar calendar = Calendar.getInstance();
        final int month = calendar.get(Calendar.MONTH);
        final int year = calendar.get(Calendar.YEAR);
        calendar.set(year, month, 1);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return simpleDateFormat.format(calendar.getTime());
    }


    @SuppressLint("NewApi")
    private void viewDashboard(final AppActivity activity) {
        if(!Tools.isNetworkAvailable(getActivity())){
            activity.showWarning("TIDAK ADA KONEKSI INTERNET");
            return;
        }
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("kategori", "ANGKA");
                args.put("periodeAwal", setFormatDayAndMonthToDb(tvTglMulai.getText().toString()));
                args.put("periodeAkhir", setFormatDayAndMonthToDb(tvTglAkhir.getText().toString()));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_DASHBOARD), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);

                    totalRugiLaba = result.get("TOTAL_RUGI_LABA").asInteger();
                    totalProfitMargin = result.get("TOTAL_PROFIT_MARGIN").asDouble();
                    totalPenyusutan = result.get("TOTAL_PENYUSUTAN").asInteger();
                    totalBatal = result.get("TOTAL_BATAL").asInteger();
                    totalHutangPPN = result.get("TOTAL_HUTANG_PPN").asInteger();
                    totalHutangKomisi = result.get("TOTAL_HUTANG_KOMISI").asInteger();
                    totalBelanjaPart = result.get("TOTAL_BELANJA_PART").asInteger();
                    totalOutsource = result.get("TOTAL_OUTSOURCE").asInteger();
                    totalClaim = result.get("TOTAL_CLAIM").asInteger();
                    totalPartKosong = result.get("TOTAL_PART_KOSONG").asInteger();
                    totalPending =  NumberFormatUtils.format2NumberDecimal(result.get("TOTAL_PENDING").asInteger());
                    totalRataRataCheckin = result.get("RATA_RATA_CHECKIN").asInteger();
                    totalRataRataJualPart = result.get("RATA_RATA_JUAL_PART").asInteger();
                    totalPenjualanHarian = result.get("TOTAL_PENJUALAN_HARIAN").asInteger();
                    totalUnitHarian = result.get("TOTAL_UNIT_HARIAN").asDouble();
                    totalPercentMarginPartHarian = result.get("TOTAL_MARGIN_PART").asDouble();
                    totalTidakMenunggu = result.get("TOTAL_TIDAK_MENUNGGU").asInteger();
                    totalMenunggu = result.get("TOTAL_MENUNGGU").asInteger();
                    totalProgress = result.get("TOTAL_PROGRESS").asInteger();
                    totalSelesai = result.get("TOTAL_SELESAI").asInteger();
                    totalSaldoEpay = result.get("TOTAL_SALDO_EPAY").asInteger();
                    totalPartOnline = result.get("TOTAL_PART_ONLINE").asInteger();
                    totalAsset = result.get("TOTAL_ASSET").asInteger();
                    totalPendapatanLain = result.get("TOTAL_PENDAPATAN_LAIN").asInteger();
                    totalPenjualan = result.get("TOTAL_PENDAPATAN").asString();
                    totalDonasi = result.get("TOTAL_DONASI").asInteger();
                    totalHariKerja = result.get("HARI").asInteger();
                    jumlahLayanan = result.get("LAYANAN").asInteger();
                    totalJualPart = result.get("JUAL_PART").asInteger();
                    totLayanan = result.get("TOTAL_LAYANAN").asString();
                    totPart = result.get("TOTAL_PART").asString();
                    totJasapart = result.get("TOTAL_JASA_PART").asString();
                    totJasalain = result.get("TOTAL_JASA_LAIN").asString();
                    totLainnya = result.get("TOTAL_LAINYA").asString();
                    totDiscount = result.get("TOTAL_DISCOUNT").asString();
                    totDownpayment = result.get("TOTAL_DP").asString();
                    totIncome = result.get("TOTAL_INCOME").asString();
                    totBiaya = result.get("TOTAL_BIAYA").asString();
                    totHpp = result.get("TOTAL_HPP").asString();
                    totMargin = result.get("TOTAL_MARGIN").asString();
                    totKas = result.get("TOTAL_KAS").asString();
                    totalKasBank = result.get("TOTAL_KAS_BANK").asDouble();
                    totPiutang = result.get("TOTAL_PIUTANG").asString();
                    totColeection = result.get("TOTAL_COLLECTION").asString();
                    totStockpart = result.get("TOTAL_STOCK_PART").asString();
                    totHutang = result.get("TOTAL_HUTANG").asString();

                    lyDasboard.setVisibility(VISIBLE);
                    setValuedashboard();
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setValuedashboard() {
        activity.find(R.id.tv_total_rugi_laba, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalRugiLaba))));
        activity.find(R.id.tv_total_profit_margin_percent, TextView.class).setText(activity.setUnderline(totalProfitMargin + " %"));
        activity.find(R.id.tv_total_penyusutan, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalPenyusutan))));

        tvBelanjaPart.setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalBelanjaPart))));
        tvHutangPPN.setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalHutangPPN))));
        tvHutangKomisi.setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalHutangKomisi))));
        tvClaim.setText(activity.setUnderline(String.valueOf(totalClaim)));
        tvPartKosong.setText(activity.setUnderline(String.valueOf(totalPartKosong)));
        tvOutsource.setText(activity.setUnderline(String.valueOf(totalOutsource)));
        tvPending.setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalPending))));
        tvRataRataCheckin.setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalRataRataCheckin))));
        tvRataRataJualPart.setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalRataRataJualPart))));
        tvRPLayanan.setText(activity.setUnderline(String.valueOf(jumlahLayanan)));
        tvRPJualPart.setText(activity.setUnderline(String.valueOf(totalJualPart)));
        tvHariKerja.setText(activity.setUnderline(String.valueOf(totalHariKerja)));
        tvRPBatal.setText(activity.setUnderline(String.valueOf(totalBatal)));
        tvPercentMarginPart.setText(activity.setUnderline((totalPercentMarginPartHarian + " %")));
        tvRpPenjualanHarian.setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalPenjualanHarian))));
        tvUnitHarianMargin.setText(activity.setUnderline(totalUnitHarian  + " %"));

        activity.find(R.id.tv_total_tidak_menunggu, TextView.class).setText(activity.setUnderline(String.valueOf(totalTidakMenunggu)));
        activity.find(R.id.tv_total_menunggu, TextView.class).setText(activity.setUnderline(String.valueOf(totalMenunggu)));
        activity.find(R.id.tv_total_progress, TextView.class).setText(activity.setUnderline(String.valueOf(totalProgress)));
        activity.find(R.id.tv_total_selesai, TextView.class).setText(activity.setUnderline(String.valueOf(totalSelesai)));
        activity.find(R.id.tv_total_saldo_epay, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalSaldoEpay))));
        activity.find(R.id.tv_total_part_online, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalPartOnline))));
        activity.find(R.id.tv_total_asset, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalAsset))));
        activity.find(R.id.tv_total_pendapatan_lain, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalPendapatanLain))));
        activity.find(R.id.tv_total_donasi, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRp(String.valueOf(totalDonasi))));
        activity.find(R.id.tv_total_penjualan, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totalPenjualan)));

        activity.find(R.id.tv_dbLayanan2, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totLayanan)));
        activity.find(R.id.tv_dbPart, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totPart)));
        activity.find(R.id.tv_dbJasapart, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totJasapart)));
        activity.find(R.id.tv_dbJasalainnya, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totJasalain)));
        activity.find(R.id.tv_dblainnya, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totLainnya)));
        activity.find(R.id.tv_dbDiscount, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totDiscount)));
        activity.find(R.id.tv_total_dp_part, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totDownpayment)));
        activity.find(R.id.tv_dbIncome, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totIncome)));
        activity.find(R.id.tv_dbBiaya, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totBiaya)));
        activity.find(R.id.tv_dbHpppart, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totHpp)));
       // activity.find(R.id.tv_dbMargin, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totMargin)));
        activity.find(R.id.tv_dbKas, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totKas)));
        activity.find(R.id.tv_dbBank, TextView.class).setText(activity.setUnderline(RP + NumberFormatUtils.formatRpDecimal(String.valueOf(totalKasBank))));
        activity.find(R.id.tv_dbPiutang, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totPiutang)));
        activity.find(R.id.tv_dbColeection, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totColeection)));
        activity.find(R.id.tv_dbStockpart, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totStockpart)));
        activity.find(R.id.tv_dbHutang, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totHutang)));
    }

    public void getDatePickerAwal() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.getActualMinimum(Calendar.DAY_OF_MONTH);
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
                activity.find(R.id.tv_mulaitgl, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //activity.find(R.id.ic_selesaitgl, TextView.class).setEnabled(true);
                tglAwal = activity.find(R.id.tv_mulaitgl, TextView.class).getText().toString();
            }
        });

        datePickerDialog.setMaxDate(parseWaktuNow());
        datePickerDialog.show(activity.getFragmentManager(), "Datepickerdialog");
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
                activity.find(R.id.tv_selesaitgl, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    tanggalAkhir = activity.find(R.id.tv_selesaitgl, TextView.class).getText().toString();
                    if (validateDate(tglAwal, tanggalAkhir)) {
                        activity.showWarning("TANGGAL AKHIR TIDAK BOLEH LEBIH KECIL DARI TANGGAL AWAL");
                        activity.find(R.id.tv_selesaitgl, TextView.class).setText("");
                        activity.find(R.id.tv_selesaitgl, TextView.class).performClick();
                    } else {
                        fragmentView.findViewById(R.id.btn_tampilkan).post(new Runnable() {
                            @Override
                            public void run() {
                                fragmentView.findViewById(R.id.btn_tampilkan).performClick();
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, "WAKTU AMBIL: " + waktuLayananHplusExtra);
            }
        });

        datePickerDialog.setMaxDate(parseWaktuNow());
        datePickerDialog.show(activity.getFragmentManager(), "Datepickerdialog");
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
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(activity.currentDateTime("dd/MM/yyyy"));
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