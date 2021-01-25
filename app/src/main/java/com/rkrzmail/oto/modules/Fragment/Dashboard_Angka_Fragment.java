package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
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
import com.rkrzmail.oto.modules.bengkel.Absensi_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Dashboard_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Rincian_Pembayaran_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.rkrzmail.utils.APIUrls.VIEW_DASHBOARD;
import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_JUAL_PART;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.RP;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class Dashboard_Angka_Fragment extends Fragment {

    private RecyclerView rvPembayaranCheckin;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout lyDasboard;
    private Nson pembayaranList = Nson.newArray();

    private String argsRefresh = "",idCheckin="";
    private AppActivity activity;
    private String tanggalAkhir="", tglAwal="", totLayanan="", totPart="", totJasapart="", totJasalain="",
            totLainnya="", totDiscount="", totPendapatan="", totDownpayment="", totIncome="", totBiaya="", totHpp="",
            totMargin="", totKas="", totBank="", totPiutang="", totColeection="", totStockpart="", totHutang="";

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
        View view = inflater.inflate(R.layout.activity_dashboard_, container, false);
        activity = ((Dashboard_MainTab_Activity) getActivity());
        initHideToolbar(view);
        initComponent(view);
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (isVisible()) {
//            viewDashboard((Dashboard_MainTab_Activity) Objects.requireNonNull(getActivity()));
//        }
//    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(GONE);
    }

    private void initComponent(View view){
        lyDasboard= view.findViewById(R.id.ly_dashboard);
        lyDasboard.setVisibility(GONE);

        view.findViewById(R.id.ic_mulaitgl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerAwal();
            }
        });

        view.findViewById(R.id.ic_selesaitgl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerAkhir();
            }
        });
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
                    lyDasboard.setVisibility(VISIBLE);
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }
    private void setValuedashboard(){
        activity.find(R.id.tv_dbLayanan2, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totLayanan)));
        activity.find(R.id.tv_dbPart, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totPart)));
        activity.find(R.id.tv_dbJasapart, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totJasapart)));
        activity.find(R.id.tv_dbJasalainnya, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totJasalain)));
        activity.find(R.id.tv_dblainnya, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totLainnya)));
        activity.find(R.id.tv_dbDiscount, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totDiscount)));
        activity.find(R.id.tv_dbPendapatan, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totPendapatan)));
        activity.find(R.id.tv_dbDownpayment, TextView.class).setText(activity.setUnderline(RP + activity.formatRp(totDownpayment)));
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
                activity.find(R.id.tv_mulaitgl, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.find(R.id.ic_selesaitgl, TextView.class).setEnabled(true);
                tglAwal =  activity.find(R.id.tv_mulaitgl, TextView.class).getText().toString();
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
                    if (validateDate(tglAwal,tanggalAkhir)) {
                        activity.showWarning("TANGGAL AKHIR TIDAK BOLEH LEBIH KECIL DARI TANGGAL AWAL");
                        activity.find(R.id.tv_selesaitgl, TextView.class).setText("");
                        activity.find(R.id.tv_selesaitgl, TextView.class).performClick();
                    } else {
                        viewDashboard((Dashboard_MainTab_Activity) Objects.requireNonNull(getActivity()));
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