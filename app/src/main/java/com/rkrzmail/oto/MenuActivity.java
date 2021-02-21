package com.rkrzmail.oto;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.fragment.PageAdapter;
import com.rkrzmail.oto.fragment.SlideFragment;
import com.rkrzmail.oto.fragment.pageindicator.CirclePageIndicator;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.oto.modules.antar_jemput.AntarJemput_Activity;
import com.rkrzmail.oto.modules.bengkel.Absensi_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Asset_Activity;
import com.rkrzmail.oto.modules.bengkel.Collection_Activity;
import com.rkrzmail.oto.modules.bengkel.Dashboard_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Laporan_Activity;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.oto.modules.bengkel.SaranActivity;
import com.rkrzmail.oto.modules.bengkel.Schedule_MainTab_Activity;
import com.rkrzmail.oto.modules.checkin.Checkin1_Activity;
import com.rkrzmail.oto.modules.hutang.Hutang_MainTab_Activity;
import com.rkrzmail.oto.modules.hutang.Piutang_MainTab_Activity;
import com.rkrzmail.oto.modules.komisi.KomisiTerbayar_Activity;
import com.rkrzmail.oto.modules.mekanik.AturSchedule_Activity;
import com.rkrzmail.oto.modules.mekanik.BiayaMekanik2Activity;
import com.rkrzmail.oto.modules.LoginActivity;
import com.rkrzmail.oto.modules.komisi.KomisiPart_Activity;
import com.rkrzmail.oto.modules.mekanik.InspeksiMekanik_Activity;
import com.rkrzmail.oto.modules.mekanik.PerintahKerjaMekanik_Activity;
import com.rkrzmail.oto.modules.sparepart.MenungguPart_Activity;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.oto.modules.checkin.KontrolLayanan_Activity;
import com.rkrzmail.oto.modules.checkin.KontrolBooking_Activity;
import com.rkrzmail.oto.modules.discount.DiscountJasaLain_Activity;
import com.rkrzmail.oto.modules.discount.DiscountLayanan_Activity;
import com.rkrzmail.oto.modules.checkin.CheckOut_Activity;
import com.rkrzmail.oto.modules.sparepart.AturParts_Activity;
import com.rkrzmail.oto.modules.sparepart.DetailCariPart_Activity;
import com.rkrzmail.oto.modules.discount.DiscountPart_Activity;
import com.rkrzmail.oto.modules.discount.FrekwensiDiscount_Activity;
import com.rkrzmail.oto.modules.discount.DiscountSpot_Activity;
import com.rkrzmail.oto.modules.bengkel.Referal_Activity;
import com.rkrzmail.oto.modules.sparepart.PartHome_MainTab_Activity;
import com.rkrzmail.oto.modules.sparepart.PenjualanPart_Activity;
import com.rkrzmail.oto.modules.sparepart.Spareparts_Activity;
import com.rkrzmail.oto.modules.sparepart.PartKeluar_Activity;
import com.rkrzmail.oto.modules.sparepart.HistoryStockOpname_Activity;
import com.rkrzmail.oto.modules.sparepart.TugasPart_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.User_Activity;
import com.rkrzmail.oto.modules.komisi.KomisiJasaLain_Activity;
import com.rkrzmail.oto.modules.komisi.KomisiLayanan_Activity;
import com.rkrzmail.oto.modules.bengkel.Layanan_Avtivity;
import com.rkrzmail.oto.modules.sparepart.LokasiPart_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.RekeningBank_Activity;
import com.rkrzmail.oto.modules.bengkel.Tenda_Activity;
import com.rkrzmail.oto.modules.sparepart.TerimaPart_Activity;
import com.rkrzmail.srv.OtoReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class MenuActivity extends AppActivity {

    Nson nPopulate = Nson.newArray();
    Nson dataBengkel = Nson.newObject();
    Nson mekanikMenuArray = Nson.newArray();
    public final int MN_CHECKIN = 3;
    public final int MN_PART = 4;
    public final int MN_PART_SEARCH = 5;
    public final int MN_MESSAGE_WA = 7;
    public final int MN_SPAREPART = 8;
    public final int MN_REGISTRASI = 9;
    public final int MN_PROFILE = 10;
    public final int MN_MESSAGE_PenerimaanPart = 11;
    public final int MN_MESSAGE_PartDiterima = 12;
    public final int MN_CARI_PART = 13;
    public final int MN_TUGAS_PART = 14;
    public final int MN_KOMISI_KARYAWAN = 15;
    public final int MN_LOYALTI_PROGRAM = 16;
    public final int MN_LOKASI_PERSEDIAAN = 17;
    public final int MN_STOCK_OPNAME = 18;
    public static final int MN_BIAYA_MEKANIK2 = 20;
    public static final int MN_LOKASI_PART = 21;
    public static final int MN_TERIMA_PART = 22;
    public static final int MN_LAYANAN = 23;
    public static final int MN_TENDA = 24;
    public static final int MN_JURNAL = 25;
    public static final int MN_SPOT_DISKON = 26;
    public static final int MN_DISCOUNT_PART = 27;
    public static final int MN_REKENING = 28;
    public static final int MN_BOOKING = 29;
    public static final int MN_KONTROL_LAYANAN = 30;
    public static final int MN_DISCOUNT_JASALAIN = 31;
    public static final int MN_DISCOUNT_LAYANAN = 32;
    public static final int MN_KOMISI_LAYANAN = 33;
    public static final int MN_KOMISI_JASA_LAIN = 34;
    public static final int MN_KARYAWAN = 35;
    public static final int MN_REFERAL = 36;
    public static final int MN_FREKWENSI_DISCOUNT = 37;
    public static final int MN_PART_KELUAR = 38;
    public static final int MN_JUAL_PART = 39;
    public static final int MN_PEMBAYARAN = 40;
    public static final int MN_KERJA_MEKANIK = 41;
    public static final int MN_PENGEMBALIAN_PART = 42;
    public static final int MN_KOMISI_PART = 43;

    private final String SARAN = "SARAN";
    private final String REFERAL = "REFERAL";
    private final String CHECK_OUT = "CHECK OUT";
    private final String COLLECTION = "COLLECTION";
    private final String PART_KELUAR = "PART KELUAR";
    private final String OUTSOURCE = "OUTSOURCE";
    private final String ANTAR_JEMPUT = "ANTAR JEMPUT";
    public final String LAPORAN = "LAPORAN";
    public final String SCHEDULE = "SCHEDULE";

    private final String PENGATURAN_USER = "USER";
    private final String PENGATURAN_USER_LAYANAN = "LAYANAN";
    private final String PENGATURAN_USER_BIAYA_MEKANIK = "BIAYA MEKANIK";
    private final String PENGATURAN_USER_SPAREPARTS = "SPAREPARTS";
    private final String PENGATURAN_USER_REKENING_BANK = "REKENING BANK";
    private final String PENGATURAN_USER_TENDA = "TENDA";
    private final String PENGATURAN_PROFILE = "PROFILE BENGKEL";

    private final String KOMISI_JASA_LAIN = "KOMISI JASA LAIN";
    private final String KOMISI_LAYANAN = "KOMISI LAYANAN";
    private final String KOMISI_PART = "KOMISI PART";
    private final String KOMISI_PEMBAYARAN = "PEMBAYARAN KOMISI";
    private final String DISCOUNT_JASA_LAIN = "DISCOUNT JASA LAIN";
    private final String DISCOUNT_LAYANAN = "DISCOUNT LAYANAN";
    private final String DISCOUNT_PART = "DISCOUNT PART";
    private final String DISCOUNT_SPOT = "DISCOUNT SPOT";
    private final String DISCOUNT_FREKWENSI = "DISCOUNT FREKWENSI";

    private final String MY_BUSINESS_BILLING = "BILLING";
    private final String MY_BUSINESS_HUTANG = "HUTANG";
    private final String MY_BUSINESS_PIUTANG = "PIUTANG";
    private final String MY_BUSINESS_ASET = "ASET";
    private final String MY_BUSINESS_CUSTOMER = "CUSTOMER";//tambahan
    private final String MY_BUSINESS_PAYROLL = "PAYROLL";//tambahan
    private final String MY_BUSINESS_LOKASI_PART = "LOKASI PARTS";

    private final String LOKASI_PART = "LOKASI PART";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);

        initBrodcastReceiver();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Drawable iconOto = getResources().getDrawable(R.drawable.icon_oto);
        iconOto.setTint(getResources().getColor(R.color.colorWhite));
        toolbar.setOverflowIcon(iconOto);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getSetting("NAMA_BENGKEL"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        GridView gridView = findViewById(R.id.gridView);
        populate(gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(ANTAR_JEMPUT) && getAccess(ANTAR_JEMPUT)) {
                    Intent intent = new Intent(MenuActivity.this, AntarJemput_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_BOOKING) && getAccess(M_BOOKING)) {
                    Intent intent = new Intent(MenuActivity.this, KontrolBooking_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_CHECK_IN) && getAccess(M_CHECK_IN)) {
                    Intent intent = new Intent(MenuActivity.this, Checkin1_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_KONTROL_LAYANAN) && getAccess(M_KONTROL_LAYANAN)) {
                    Intent intent = new Intent(MenuActivity.this, KontrolLayanan_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_DASHBOARD) && getAccess(M_DASHBOARD)) {
                    Intent intent = new Intent(MenuActivity.this, Dashboard_MainTab_Activity.class);
                    intent.putExtra("title", "Dashboard");
                    intent.putExtra("url", "https://m.otomotives.com/#/?" + getWebUrl());
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_INSPEKSI) && getAccess(M_INSPEKSI)) {
                    Intent intent = new Intent(MenuActivity.this, InspeksiMekanik_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_JUAL_PARTS) && getAccess(M_JUAL_PARTS)) {
                    Intent intent = new Intent(MenuActivity.this, PenjualanPart_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_JURNAL) && getAccess(M_JURNAL)) {
                    Intent intent = new Intent(MenuActivity.this, WebActivity.class);
                    intent.putExtra("title", "Dashboard");
                    intent.putExtra("url", "https://m.otomotives.com/#/jurnal?" + getWebUrl());
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_MEKANIK) && getAccess(M_MEKANIK)) {
                    Intent intent = new Intent(MenuActivity.this, PerintahKerjaMekanik_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_MENUNGGU) && getAccess(M_MENUNGGU)) {
                    Intent intent = new Intent(MenuActivity.this, MenungguPart_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_MY_CODE) && getAccess(M_MY_CODE)) {
                    Intent intent = new Intent(MenuActivity.this, MyCode.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_PART) && getAccess(M_PART)) {
                    Intent intent = new Intent(MenuActivity.this, PartHome_MainTab_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_PEMBAYARAN) && getAccess(M_PEMBAYARAN)) {
                    Intent intent = new Intent(MenuActivity.this, Pembayaran_MainTab_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_STOCK_OPNAME) && getAccess(M_STOCK_OPNAME)) {
                    Intent intent = new Intent(MenuActivity.this, HistoryStockOpname_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_TERIMA_PARTS) && getAccess(M_TERIMA_PARTS)) {
                    Intent intent = new Intent(MenuActivity.this, TerimaPart_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_TUGAS_PARTS) && getAccess(M_TUGAS_PARTS)) {
                    Intent intent = new Intent(MenuActivity.this, TugasPart_MainTab_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_ABSENSI) && getAccess(M_ABSENSI)) {
                    Intent intent = new Intent(MenuActivity.this, Absensi_MainTab_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("text").asString().equalsIgnoreCase(M_COLLECTION) && getAccess(M_COLLECTION)) {
                    Intent intent = new Intent(MenuActivity.this, Collection_Activity.class);
                    startActivity(intent);
                } else {
                    showWarning("ANDA TIDAK MEMILIKI AKSES MENU " + nPopulate.get(position).get("text").asString(), Toast.LENGTH_LONG);
                }
            }
        });
        // banner();
    }

    private void initBrodcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())) {
                    case "notifyMekanik":
                        showInfo("Mekanik");
                        Log.e("otoReceiver", "onReceive: " + "mekanik");
                        break;
                    case "notifyPart":
                        showInfo("Mekanik");
                        Log.e("otoReceiver", "onReceive: " + "part");
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("notifyMekanik");
        intentFilter.addAction("notifyPart");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (item.getTitle().toString().equalsIgnoreCase(REFERAL) && getAccess(REFERAL)) {
            Intent intent = new Intent(MenuActivity.this, Referal_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(CHECK_OUT) && getAccess(REFERAL)) {
            Intent intent = new Intent(MenuActivity.this, CheckOut_Activity.class);
            startActivity(intent);
        }
        //Business
        else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_HUTANG) && getAccess(MY_BUSINESS)) {
            Intent intent = new Intent(MenuActivity.this, Hutang_MainTab_Activity.class);
            startActivity(intent);
            /*Intent intent = new Intent(MenuActivity.this, WebActivity.class);
            intent.putExtra("title", "Dashboard");
            intent.putExtra("url", "https://m.otomotives.com/#/hutang?" + getWebUrl());
            startActivity(intent);*/
        } else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_PIUTANG) && getAccess(MY_BUSINESS)) {
            Intent intent = new Intent(MenuActivity.this, Piutang_MainTab_Activity.class);
            startActivity(intent);
           /* Intent intent = new Intent(MenuActivity.this, WebActivity.class);
            intent.putExtra("title", "Dashboard");
            intent.putExtra("url", "https://m.otomotives.com/#/piutang?" + getWebUrl());
            startActivity(intent);*/
        } else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_BILLING) && getAccess(MY_BUSINESS)) {

        } else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_PROFILE) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, ProfileBengkel_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_ASET) && getAccess(MY_BUSINESS)) {
            Intent intent = new Intent(MenuActivity.this, Asset_Activity.class);
            startActivity(intent);
           /* Intent intent = new Intent(MenuActivity.this, WebActivity.class);
            intent.putExtra("title", "Dashboard");
            intent.putExtra("url", "https://m.otomotives.com/#/aset?" + getWebUrl());
            startActivity(intent);*/
        } else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_CUSTOMER) && getAccess(MY_BUSINESS)) {

        } else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_PAYROLL) && getAccess(MY_BUSINESS)) {

        } else if (item.getTitle().toString().equalsIgnoreCase(LAPORAN) && getAccess(LAPORAN)) {
            Intent intent = new Intent(MenuActivity.this, Laporan_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(SCHEDULE) && getAccess(SCHEDULE)) {
            Intent intent = new Intent(MenuActivity.this, Schedule_MainTab_Activity.class);
            startActivity(intent);
        }
        //Pengaturan
        else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_USER) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, User_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_USER_LAYANAN) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, Layanan_Avtivity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_USER_BIAYA_MEKANIK) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, BiayaMekanik2Activity.class);
            intent.putExtra("data", dataBengkel.toJson());
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_USER_SPAREPARTS) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, Spareparts_Activity.class);
            intent.putExtra("flag", "atur_sparepart");
            startActivityForResult(intent, MN_SPAREPART);
        } else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_USER_REKENING_BANK) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, RekeningBank_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(MY_BUSINESS_LOKASI_PART) && getAccess(MY_BUSINESS)) {
            Intent intent = new Intent(MenuActivity.this, LokasiPart_MainTab_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(PENGATURAN_USER_TENDA) && getAccess(PENGATURAN)) {
            Intent intent = new Intent(MenuActivity.this, Tenda_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(PART_KELUAR) && getAccess(PART_KELUAR)) {
            Intent intent = new Intent(MenuActivity.this, PartKeluar_Activity.class);
            startActivity(intent);
        }
        //Discount
        else if (item.getTitle().toString().equalsIgnoreCase(DISCOUNT_FREKWENSI) && getAccess(DISCOUNT)) {
            Intent intent = new Intent(MenuActivity.this, FrekwensiDiscount_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(DISCOUNT_JASA_LAIN) && getAccess(DISCOUNT)) {
            Intent intent = new Intent(MenuActivity.this, DiscountJasaLain_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(DISCOUNT_LAYANAN) && getAccess(DISCOUNT)) {
            Intent intent = new Intent(MenuActivity.this, DiscountLayanan_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(DISCOUNT_PART) && getAccess(DISCOUNT)) {
            Intent intent = new Intent(MenuActivity.this, DiscountPart_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(DISCOUNT_SPOT) && getAccess(DISCOUNT)) {
            Intent intent = new Intent(MenuActivity.this, DiscountSpot_Activity.class);
            startActivity(intent);
        }
        //Komisi
        else if (item.getTitle().toString().equalsIgnoreCase(KOMISI_JASA_LAIN) && getAccess(KOMISI)) {
            Intent intent = new Intent(MenuActivity.this, KomisiJasaLain_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(KOMISI_LAYANAN) && getAccess(KOMISI)) {
            Intent intent = new Intent(MenuActivity.this, KomisiLayanan_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(KOMISI_PART) && getAccess(KOMISI)) {
            Intent intent = new Intent(MenuActivity.this, KomisiPart_Activity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equalsIgnoreCase(KOMISI_PEMBAYARAN) && getAccess(KOMISI)) {
            Intent intent = new Intent(MenuActivity.this, KomisiTerbayar_Activity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_logout) {
            Messagebox.showDialog(getActivity(), "Logout", "Yakin Logout ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    UtilityAndroid.removeSettingAll(getActivity());
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
        } else if (item.getTitle().toString().equalsIgnoreCase(SARAN)) {
            Intent intent = new Intent(MenuActivity.this, SaranActivity.class);
            startActivity(intent);
        } else if (item.getTitle().toString().equals(PENGATURAN) && !getAccess(PENGATURAN)) {
            showWarning("ANDA TIDAK MEMILIK AKSES " + PENGATURAN);
        }else if (item.getTitle().toString().equals(PENGATURAN) && !getAccess(MY_BUSINESS)) {
            showWarning("ANDA TIDAK MEMILIK AKSES " + MY_BUSINESS);
        }else if (item.getTitle().toString().equals(PENGATURAN) && !getAccess(DISCOUNT)) {
            showWarning("ANDA TIDAK MEMILIK AKSES " + DISCOUNT);
        }else if (item.getTitle().toString().equals(PENGATURAN) && !getAccess(KOMISI)) {
            showWarning("ANDA TIDAK MEMILIK AKSES " + KOMISI);
        }

        return super.onOptionsItemSelected(item);
    }

    private String getWebUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("user=").append(UtilityAndroid.getSetting(getApplicationContext(), "user", ""));
        stringBuilder.append("&token=").append(UtilityAndroid.getSetting(getApplicationContext(), "session", ""));
        return stringBuilder.toString();
    }

    private int count;
    private Handler handler;

    private void banner() {
        ViewPager page = (ViewPager) findViewById(R.id.pageframe);
        List<Fragment> fragments = new ArrayList<Fragment>();


        /*final String[] ban = new String[] {R.drawable.sample+"",
                "http://neyama.com/loyalty/images/layar1_2.jpg",
                "http://neyama.com/loyalty/images/layar2_2.gif",
                "http://neyama.com/loyalty/images/layar3_2.jpg",
                "http://neyama.com/loyalty/images/layar4_2.gif"};*/
        final String[] ban = new String[]{R.drawable.sample + "",
                R.drawable.sample + "",
                R.drawable.sample + ""};

        for (int i = 0; i < ban.length; i++) {
            Bundle bundle = new Bundle();
            bundle.putString("image", ban[i]);

            Fragment fr = Fragment.instantiate(getApplicationContext(), SlideFragment.class.getName());
            fr.setArguments(bundle);
            fragments.add(fr);
        }

        final int delayms = 5000;
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), fragments);
        page.setAdapter(adapter);
        final CirclePageIndicator mIndicator = new CirclePageIndicator(getApplicationContext(), null);
        mIndicator.setViewPager(page);
        mIndicator.setCurrentItem(1);
        ((FrameLayout) findViewById(R.id.indicator)).addView(mIndicator, new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, FrameLayout.LayoutParams.MATCH_PARENT));
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int arg0) {
                count = arg0;
                if (handler != null) {
                    handler.removeMessages(1);
                    handler.sendEmptyMessageDelayed(1, delayms);
                }
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageScrollStateChanged(int arg0) {
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (mIndicator != null) {
                        count++;
                        count = count >= ban.length ? 0 : count;
                        try {
                            mIndicator.setCurrentItem(count);
                        } catch (Exception e) {
                        }
                    }
                } else if (msg.what == 2) {
                    //check(MainActivity.this);
                }
            }
        };
        handler.sendEmptyMessageDelayed(1, delayms);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (handler != null) {
            handler.removeMessages(1);
            handler.removeMessages(2);
        }
    }

    public final String M_BOOKING = "BOOKING";
    public final String M_CHECK_IN = "CHECK IN";
    public final String M_KONTROL_LAYANAN = "KONTROL";
    public final String M_COLLECTION = "COLLECTION";
    public final String M_INSPEKSI = "INSPEKSI";
    public final String M_JUAL_PARTS = "JUAL PARTS";
    public final String M_JURNAL = "JURNAL";
    public final String M_DASHBOARD = "DASHBOARD";
    public final String M_MEKANIK = "MEKANIK";
    public final String M_ABSENSI = "ABSENSI";
    public final String M_MENUNGGU = "MENUNGGU";
    public final String M_MY_CODE = "MY CODE";
    public final String M_PART = "PART";
    public final String M_PEMBAYARAN = "PEMBAYARAN";
    public final String M_STOCK_OPNAME = "STOCK OPNAME";
    public final String M_TERIMA_PARTS = "TERIMA PARTS";
    public final String M_TUGAS_PARTS = "TUGAS PARTS";
    //parent sub
    public final String KENDARAAN_CUSTOMER = "KENDARAAN CUSTOMER";
    public final String PEMBELIAN_PART = "PEMBELIAN PART";
    public final String MY_BUSINESS = "MY BUSINESS";
    public final String PENGATURAN = "PENGATURAN";
    public final String DISCOUNT = "DISCOUNT";
    public final String KOMISI = "KOMISI";

    private void addHome(int id, int icon, String text) {
        nPopulate.add(Nson.newObject().set("id", id).set("icon", icon).set("text", text));
    }

    public void populate(GridView v) {
        addHome(1, R.drawable.x_checkin, M_CHECK_IN);
        addHome(13, R.drawable.x_pembayaran, M_PEMBAYARAN);
        addHome(3, R.drawable.x_dashboard, M_KONTROL_LAYANAN);
        addHome(9, R.drawable.x_mekanik, M_MEKANIK);
        addHome(17, R.drawable.ic_tugas_part, M_TUGAS_PARTS);
        addHome(6, R.drawable.x_inpeksi, M_INSPEKSI);
        nPopulate.add(Nson.newObject().set("id", 11).set("icon", R.drawable.m_mycode).set("text", M_MY_CODE));
        addHome(7, R.drawable.ic_jual_part, M_JUAL_PARTS);
        addHome(16, R.drawable.x_terimapart, M_TERIMA_PARTS);
        addHome(14, R.drawable.x_stock, M_STOCK_OPNAME);
        nPopulate.add(Nson.newObject().set("id", 12).set("icon", R.drawable.x_part).set("text", M_PART));
        addHome(2, R.drawable.x_booking, M_BOOKING);
        addHome(4, R.drawable.x_collection, M_COLLECTION);
        nPopulate.add(Nson.newObject().set("id", 12).set("icon", R.drawable.x_penuasan).set("text", M_ABSENSI));
        addHome(5, R.drawable.ic_dashboard, M_DASHBOARD);
        addHome(8, R.drawable.x_jurnal, M_JURNAL);

        ArrayAdapter<Vector<String>> arrayAdapter = new ArrayAdapter<Vector<String>>(MenuActivity.this, R.layout.activity_main_item, nPopulate.asArray()) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = getLayoutInflater().inflate(R.layout.activity_main_item, null);

                ((TextView) v.findViewById(R.id.txtIcon)).setText(nPopulate.get(position).get("text").asString());
                ((ImageView) v.findViewById(R.id.imgIcon)).setImageResource(nPopulate.get(position).get("icon").asInteger());

                return v;
            }
        };
        GridView gridView = findViewById(R.id.gridView);
        gridView.setAdapter(arrayAdapter);
    }

    private boolean getAccess(String access) {
        if (getSetting("TIPE_USER").equalsIgnoreCase("ADMIN")) {
            return true;
        } else if (getSetting("ACCESS_MENU").contains(access)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        SubMenu subMenu;

        menu.add(ANTAR_JEMPUT);//FREE AKSES
        menu.add(CHECK_OUT);
        menu.add(KENDARAAN_CUSTOMER);
        menu.add(LAPORAN);
        menu.add(PART_KELUAR);
        menu.add(PEMBELIAN_PART);
        menu.add(SCHEDULE);
        menu.add(REFERAL);
        menu.add(SARAN);

        subMenu = menu.addSubMenu(MY_BUSINESS);
        subMenu.add(MY_BUSINESS_BILLING);
        subMenu.add(MY_BUSINESS_HUTANG);
        subMenu.add(MY_BUSINESS_PIUTANG);
        subMenu.add(MY_BUSINESS_ASET);
        subMenu.add(MY_BUSINESS_CUSTOMER);
        subMenu.add(MY_BUSINESS_PAYROLL);
        subMenu.add(MY_BUSINESS_LOKASI_PART);

        subMenu = menu.addSubMenu(PENGATURAN);
        subMenu.add(PENGATURAN_USER);
        subMenu.add(PENGATURAN_USER_LAYANAN);
        subMenu.add(PENGATURAN_USER_BIAYA_MEKANIK);
        subMenu.add(PENGATURAN_USER_SPAREPARTS);
        subMenu.add(PENGATURAN_USER_REKENING_BANK);
        subMenu.add(PENGATURAN_USER_TENDA);
        subMenu.add(PENGATURAN_PROFILE);

        subMenu = menu.addSubMenu(DISCOUNT);
        subMenu.add(DISCOUNT_JASA_LAIN);
        subMenu.add(DISCOUNT_LAYANAN);
        subMenu.add(DISCOUNT_PART);
        subMenu.add(DISCOUNT_SPOT);
        subMenu.add(DISCOUNT_FREKWENSI);

        subMenu = menu.addSubMenu(KOMISI);
        subMenu.add(KOMISI_JASA_LAIN);
        subMenu.add(KOMISI_LAYANAN);
        subMenu.add(KOMISI_PART);
        subMenu.add(KOMISI_PEMBAYARAN);

        return true;
    }

    private final Nson permintaanPart = Nson.newArray();
    private final Nson kerjaMekanik = Nson.newArray();
    int permintaanSize = 0;
    int kerjaMekanikSize = 0;

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //viewPartPermintaan();
                //viewPerintahMekanik("");
            }
        }, 2000);
    }

    @SuppressLint("NewApi")
    public void viewPartPermintaan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "NOTIFICATION");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if (permintaanPart.size() > 0) {
                                for (int j = 0; j < permintaanPart.size(); j++) {
                                    if (permintaanPart.get(j).get("PART_ID").asInteger() != result.get(i).get("PART_ID").asInteger()) {
                                        permintaanPart.add(Nson.newObject()
                                                .set("PART_ID", result.get(i).get("PART_ID").asInteger())
                                                .set("NAMA_PART", result.get(i).get("NAMA_PART").asString())
                                        );
                                    }
                                }
                            } else {
                                permintaanPart.add(Nson.newObject()
                                        .set("PART_ID", result.get(i).get("PART_ID").asInteger())
                                        .set("NAMA_PART", result.get(i).get("NAMA_PART").asString())
                                );
                            }
                        }

                        permintaanSize = permintaanPart.size();
                        if (permintaanSize > result.size()) {
                            Intent intent = new Intent(getActivity(), TugasPart_MainTab_Activity.class);
                            intent.putExtra("NOPOL", result.get(0).get("NOPOL").asString());
                            showNotification(
                                    getActivity(),
                                    "Tugas Part",
                                    "Tugas Part Baru",
                                    "TUGAS PART",
                                    intent);
                        }

                    }
                }
            }
        });
    }

    private void viewPerintahMekanik(final String cari) {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.asString().equals("PENUGASAN MEKANIK")) {
                        showInfo(result.asString());
                        Intent intent = new Intent(getActivity(), PerintahKerjaMekanik_Activity.class);
                        intent.putExtra("NOPOL", result.get(0).get("NOPOL").asString());
                        showNotification(
                                getActivity(),
                                "Perintah Kerja Mekanik",
                                "Mekanik",
                                "MEKANIK",
                                intent);
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MN_SPAREPART) {
                Intent i = new Intent(getActivity(), AturParts_Activity.class);
                i.putExtra(PART, getIntentStringExtra(data, PART));
                startActivityForResult(i, 112);
            }
            if (requestCode == REQUEST_DETAIL) {
                Intent i = new Intent(getActivity(), DetailCariPart_Activity.class);
                i.putExtra(PART, Nson.readJson(getIntentStringExtra(data, PART)).toJson());
                startActivityForResult(i, 109);
            }
        }
    }
}
