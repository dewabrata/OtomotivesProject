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
import com.rkrzmail.oto.modules.bengkel.Dashboard_MainTab_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

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
    private TextView tvTglMulai, tvTglAkhir, tvHariKerja, tvRPLayanan, tvRPJualPart, tvRPBatal;
    private View fragmentView;

    private Nson pembayaranList = Nson.newArray();

    private String argsRefresh = "", idCheckin = "";
    private AppActivity activity;
    private String tanggalAkhir = "", tglAwal = "", totLayanan = "", totPart = "", totJasapart = "", totJasalain = "",
            totLainnya = "", totDiscount = "", totalPenjualan = "", totDownpayment = "", totIncome = "", totBiaya = "", totHpp = "",
            totMargin = "", totKas = "", totBank = "", totPiutang = "", totColeection = "", totStockpart = "", totHutang = "";
    private int totalHariKerja = 0, totalLayanan = 0, totalJualPart = 0,
            totalTidakMenunggu = 0, totalMenunggu = 0, totalProgress = 0,
            totalSelesai = 0, totalSaldoEpay = 0, totalPartOnline = 0, totalAsset = 0,
            totalPendapatanLain = 0, totalDonasi = 0;

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

        lyDasboard.setVisibility(GONE);

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
                    totalLayanan = result.get("LAYANAN").asInteger();
                    totalJualPart = result.get("JUAL_PART").asInteger();
                    totLayanan = result.get("TOTAL_LAYANAN").asString();
                    totPart = result.get("TOTAL_PART").asString();
                    totJasapart = result.get("TOTAL_JASA_PART").asString();
                    totJasalain = result.get("TOTAL_JASA_LAIN").asString();
                    totLainnya = result.get("TOTAL_LAINYA").asString();
                    totDiscount = result.get("TOTAL_DISCOUNT").asString();
                    totDownpayment = result.get("TOTAL_DP").asString();
                    totIncome = result.get("TOTAL_INCOME").asString();
                    totBiaya = "0";//result.get("TOTAL_BIAYA").asString()
                    totHpp = result.get("TOTAL_HPP").asString();
                    totMargin = "0";
                    totKas = result.get("TOTAL_KAS").asString();
                    totBank = result.get("TOTAL_KAS_BANK").asString();
                    totPiutang = result.get("TOTAL_PIUTANG").asString();
                    totColeection = result.get("TOTAL_COLLECTION").asString();
                    totStockpart = result.get("TOTAL_STOCK_PART").asString();
                    totHutang = result.get("TOTAL_HUTANG").asString();
                    setValuedashboard();
                    lyDasboard.setVisibility(VISIBLE);
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }

    private void setValuedashboard() {
        tvRPLayanan.setText(activity.setUnderline(String.valueOf(totalLayanan)));
        tvRPJualPart.setText(activity.setUnderline(String.valueOf(totalJualPart)));
        tvHariKerja.setText(activity.setUnderline(String.valueOf(totalHariKerja)));
        tvRPBatal.setText(activity.setUnderline("0"));

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
        activity.find(R.id.tv_dbMargin, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totMargin)));
        activity.find(R.id.tv_dbKas, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totKas)));
        activity.find(R.id.tv_dbBank, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totBank)));
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
                activity.find(R.id.ic_selesaitgl, TextView.class).setEnabled(true);
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