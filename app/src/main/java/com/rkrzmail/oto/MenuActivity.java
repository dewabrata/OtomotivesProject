package com.rkrzmail.oto;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.fragment.PageAdapter;
import com.rkrzmail.oto.fragment.SlideFragment;
import com.rkrzmail.oto.fragment.pageindicator.CirclePageIndicator;
import com.rkrzmail.oto.gmod.AturSparepartActivity;
import com.rkrzmail.oto.gmod.LayananActivity;
import com.rkrzmail.oto.gmod.MessageWA;
import com.rkrzmail.oto.gmod.Part_DiterimaActivity;
import com.rkrzmail.oto.gmod.Penerimaan_PartAcitivity;
import com.rkrzmail.oto.modules.biayamekanik.BiayaMekanik2Activity;
import com.rkrzmail.oto.modules.LoginActivity;
import com.rkrzmail.oto.modules.komisi.KomisiPart_Activity;
import com.rkrzmail.oto.modules.pembayaran.DaftarPembayaran_Activity;
import com.rkrzmail.oto.modules.perintah_kerja_mekanik.PerintahKerjaMekanik_Activity;
import com.rkrzmail.oto.modules.primary.KontrolLayanan_Activity;
import com.rkrzmail.oto.modules.primary.booking.Booking3_Activity;
import com.rkrzmail.oto.modules.primary.booking.KontrolBooking_Activity;
import com.rkrzmail.oto.modules.jasa.discount_jasa_lain.DiscountJasaLain_Activity;
import com.rkrzmail.oto.modules.layanan.discount_layanan.DiscountLayanan_Activity;
import com.rkrzmail.oto.modules.sparepart.AturParts_Activity;
import com.rkrzmail.oto.modules.sparepart.diskon_part.DiscountPart_Activity;
import com.rkrzmail.oto.modules.discount.FrekwensiDiscount_Activity;
import com.rkrzmail.oto.modules.discount.SpotDiscount_Activity;
import com.rkrzmail.oto.modules.jurnal.DaftarJurnal_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.profile.ProfileBengkel_Activity;
import com.rkrzmail.oto.modules.registrasi_bengkel.Referal_Activity;
import com.rkrzmail.oto.modules.registrasi_bengkel.RegistrasiBengkel_Activity;
import com.rkrzmail.oto.modules.sparepart.PenjualanPart_Activity;
import com.rkrzmail.oto.modules.sparepart.Spareparts_Activity;
import com.rkrzmail.oto.modules.sparepart.part_keluar.PartKeluar_Activity;
import com.rkrzmail.oto.modules.sparepart.tugas_part.TugasPart_Activity;
import com.rkrzmail.oto.modules.user.User_Activity;
import com.rkrzmail.oto.modules.komisi.KomisiJasaLain_Activity;
import com.rkrzmail.oto.modules.komisi.KomisiLayanan_Activity;
import com.rkrzmail.oto.modules.layanan.Layanan_Avtivity;
import com.rkrzmail.oto.modules.sparepart.lokasi_part.LokasiPart_Activity;
import com.rkrzmail.oto.modules.part.PartActivity;
import com.rkrzmail.oto.modules.part.PartSearchActivity;
import com.rkrzmail.oto.modules.penugasan.PenugasanActivity;
import com.rkrzmail.oto.modules.rekening_bank.RekeningBank_Activity;
import com.rkrzmail.oto.modules.tenda.Tenda_Activity;
import com.rkrzmail.oto.modules.sparepart.terima_part.TerimaPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MenuActivity extends AppActivity {

    Nson nPopulate = Nson.newArray();
    public final int MN_CHECKIN         =   3;
    public final int MN_PART            =   4;
    public final int MN_PART_SEARCH     =   5;
    public final int MN_MESSAGE_WA      =   7;
    public final int MN_SPAREPART =   8;
    public final int MN_REGISTRASI = 9;
    public final int MN_PROFILE = 10;
    public final int MN_MESSAGE_PenerimaanPart =   11;
    public final int MN_MESSAGE_PartDiterima =   12;
    public final int MN_CARI_PART = 13;
    public final int MN_TUGAS_PART =   14;
    public final int MN_KOMISI_KARYAWAN =   15;
    public final int MN_LOYALTI_PROGRAM =   16;
    public final int MN_LOKASI_PERSEDIAAN =17;
    public final int MN_STOCK_OPNAME =18;
    public static final int MN_PENUGASAN_MEKANIK = 19;
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


    public static final int MN_MEKANIK       = 130;
    public static final int MN_MYCODE       = 130;
    public static final int MN_INSPEKSI     = 130;
    public static final int MN_COLLECTION   = 130;
    public static final int MN_MENUNGGU_PART      = 130;
    public static final int MN_ANTAR_JEMPUT     = 130;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setTitle("Otomotives ("+getSetting("NAMA")+")");

        GridView gridView = findViewById(R.id.gridView);
        populate(gridView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (nPopulate.get(position).get("id").asInteger() == MN_CHECKIN){
                    Intent intent =  new Intent(MenuActivity.this, LayananActivity.class);
                    startActivity(intent);
                }else if (nPopulate.get(position).get("id").asInteger() == MN_PART){
                    Intent intent =  new Intent(MenuActivity.this, PartActivity.class);
                    startActivity(intent);
                }else if (nPopulate.get(position).get("id").asInteger() == MN_PART_SEARCH){
                    Intent intent =  new Intent(MenuActivity.this, PartSearchActivity.class);
                    startActivity(intent);
                }else if (nPopulate.get(position).get("id").asInteger() == MN_MESSAGE_WA){
                    Intent intent =  new Intent(MenuActivity.this, MessageWA.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("id").asInteger() == MN_SPAREPART) {
                    Intent intent = new Intent(MenuActivity.this, Spareparts_Activity.class);
                    intent.putExtra("flag", "atur_sparepart");
                    startActivityForResult(intent, MN_SPAREPART);

                } else if (nPopulate.get(position).get("id").asInteger() == MN_REGISTRASI) {
                    Intent intent = new Intent(MenuActivity.this, RegistrasiBengkel_Activity.class);
                    startActivity(intent);
                }
                else if (nPopulate.get(position).get("id").asInteger() == MN_CARI_PART) {
                    Intent intent = new Intent(MenuActivity.this, CariPart_Activity.class);
                    intent.putExtra("flag","ALL");
                    startActivityForResult(intent, 1);
                }
//                else if (nPopulate.get(position).get("id").asInteger() == MN_CARI_PART) {
//                    Intent intent = new Intent(MenuActivity.this, CariPart_Activity.class);
//                    startActivityForResult(intent, 2);
//                }
                else if (nPopulate.get(position).get("id").asInteger() == MN_PROFILE) {
                    Intent intent = new Intent(MenuActivity.this, ProfileBengkel_Activity.class);
                    startActivity(intent);

                }

//                else if (nPopulate.get(position).get("id").asInteger() == MN_BIAYA_MEKANIK) {
//                    Intent intent = new Intent(MenuActivity.this, BiayaMekanikActivity.class);
//                    startActivity(intent);
//                }
//                else if (nPopulate.get(position).get("id").asInteger() == MN_JUAL_PART) {
//                    Intent intent = new Intent(MenuActivity.this, DaftarPenjualanPartActivity.class);
//                    startActivity(intent);
//
//                }
                else if (nPopulate.get(position).get("id").asInteger() == MN_MESSAGE_PenerimaanPart) {
                    Intent intent = new Intent(MenuActivity.this, Penerimaan_PartAcitivity.class);
                    startActivity(intent);

                } else if (nPopulate.get(position).get("id").asInteger() == MN_MESSAGE_PartDiterima) {
                    Intent intent = new Intent(MenuActivity.this, Part_DiterimaActivity.class);
                    startActivity(intent);

                }
//                else if (nPopulate.get(position).get("id").asInteger() == MN_STOCK_OPNAME) {
//                    Intent intent = new Intent(MenuActivity.this, Stock_OpnameActivity.class);
//                    startActivity(intent);
//
//                }
                else if (nPopulate.get(position).get("id").asInteger() == MN_TUGAS_PART) {
                    Intent intent = new Intent(MenuActivity.this, TugasPart_Activity.class);
                    startActivity(intent);

                }
//                else if (nPopulate.get(position).get("id").asInteger() == MN_KOMISI_KARYAWAN) {
//                    Intent intent = new Intent(MenuActivity.this, Komisi_Karyawan_Activity.class);
//                    startActivity(intent);
//
//                }
//                else if (nPopulate.get(position).get("id").asInteger() == MN_LOYALTI_PROGRAM) {
//                    Intent intent = new Intent(MenuActivity.this, Loyalti_ProgramActivity.class);
//                    startActivity(intent);
//
//                }
//                else if (nPopulate.get(position).get("id").asInteger() == MN_LOKASI_PERSEDIAAN) {
//                    Intent intent = new Intent(MenuActivity.this, Penampung_ItemActivity.class);
//                    startActivity(intent);
//
//                }
                else if (nPopulate.get(position).get("id").asInteger() == MN_PENUGASAN_MEKANIK) {
                    Intent intent = new Intent(MenuActivity.this, PenugasanActivity.class);
                    startActivity(intent);

                }else if (nPopulate.get(position).get("id").asInteger() == MN_BIAYA_MEKANIK2) {
                    Intent intent = new Intent(MenuActivity.this, BiayaMekanik2Activity.class);
                    startActivity(intent);

                }else if(nPopulate.get(position).get("id").asInteger() == MN_LOKASI_PART){
                    Intent intent = new Intent(MenuActivity.this, LokasiPart_Activity.class);
                    startActivity(intent);

                }else if(nPopulate.get(position).get("id").asInteger() == MN_TERIMA_PART){
                    Intent intent = new Intent(MenuActivity.this, TerimaPart.class);
                    startActivity(intent);

                }else if((nPopulate.get(position).get("id").asInteger() == MN_LAYANAN)){
                    Intent intent = new Intent(MenuActivity.this, Layanan_Avtivity.class);
                    startActivity(intent);

                }else if((nPopulate.get(position).get("id").asInteger() == MN_TENDA)){
                    Intent intent = new Intent(MenuActivity.this, Tenda_Activity.class);
                    startActivity(intent);

                } else if ((nPopulate.get(position).get("id").asInteger() == MN_JURNAL)) {
                    Intent intent = new Intent(MenuActivity.this, DaftarJurnal_Activity.class);
                    startActivity(intent);

                } else if ((nPopulate.get(position).get("id").asInteger() == MN_SPOT_DISKON)) {
                    Intent intent = new Intent(MenuActivity.this, SpotDiscount_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_DISCOUNT_PART)) {
                    Intent intent = new Intent(MenuActivity.this, DiscountPart_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_REKENING)) {
                    Intent intent = new Intent(MenuActivity.this, RekeningBank_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_BOOKING)) {
                    Intent intent = new Intent(MenuActivity.this, KontrolBooking_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_KONTROL_LAYANAN)) {
                    Intent intent = new Intent(MenuActivity.this, KontrolLayanan_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_DISCOUNT_LAYANAN)) {
                    Intent intent = new Intent(MenuActivity.this, DiscountLayanan_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_DISCOUNT_JASALAIN)) {
                    Intent intent = new Intent(MenuActivity.this, DiscountJasaLain_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_KOMISI_LAYANAN)) {
                    Intent intent = new Intent(MenuActivity.this, KomisiLayanan_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_KOMISI_JASA_LAIN)) {
                    Intent intent = new Intent(MenuActivity.this, KomisiJasaLain_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_KARYAWAN)) {
                    Intent intent = new Intent(MenuActivity.this, User_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_REFERAL)) {
                    Intent intent = new Intent(MenuActivity.this, Referal_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_FREKWENSI_DISCOUNT)) {
                    Intent intent = new Intent(MenuActivity.this, FrekwensiDiscount_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_PART_KELUAR)) {
                    Intent intent = new Intent(MenuActivity.this, PartKeluar_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_JUAL_PART)) {
                    Intent intent = new Intent(MenuActivity.this, PenjualanPart_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_PEMBAYARAN)) {
                    Intent intent = new Intent(MenuActivity.this, DaftarPembayaran_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_KERJA_MEKANIK)) {
                    Intent intent = new Intent(MenuActivity.this, PerintahKerjaMekanik_Activity.class);
                    startActivity(intent);
                } else if ((nPopulate.get(position).get("id").asInteger() == MN_KOMISI_PART)) {
                    Intent intent = new Intent(MenuActivity.this, KomisiPart_Activity.class);
                    startActivity(intent);
                } else if (nPopulate.get(position).get("id").asInteger() == 130) {
                    Toast.makeText(getActivity(),"FORM BELUM SIAP", Toast.LENGTH_SHORT).show();

                }


            }
        });

        banner();
    }
    private int count ;
    private Handler handler;
    private void banner(){
        ViewPager page = (ViewPager) findViewById(R.id.pageframe);
        List<Fragment> fragments = new ArrayList<Fragment>();


        /*final String[] ban = new String[] {R.drawable.sample+"",
                "http://neyama.com/loyalty/images/layar1_2.jpg",
                "http://neyama.com/loyalty/images/layar2_2.gif",
                "http://neyama.com/loyalty/images/layar3_2.jpg",
                "http://neyama.com/loyalty/images/layar4_2.gif"};*/
        final String[] ban = new String[] {R.drawable.sample+"",
                R.drawable.sample+"",
                R.drawable.sample+""};

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
        ((FrameLayout)findViewById(R.id.indicator)).addView(mIndicator, new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, FrameLayout.LayoutParams.MATCH_PARENT));
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int arg0) {
                count = arg0;
                if (handler!=null) {
                    handler.removeMessages(1);
                    handler.sendEmptyMessageDelayed(1, delayms);
                }
            }
            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            public void onPageScrollStateChanged(int arg0) {}
        });
        handler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what==1) {
                    if (mIndicator!=null) {
                        count++;
                        count = count >= ban.length ? 0 : count;
                        try {
                            mIndicator.setCurrentItem(count);
                        }catch (Exception e){}
                    }
                }else if (msg.what == 2){
                    //check(MainActivity.this);
                }
            }
        };
        handler.sendEmptyMessageDelayed(1, delayms);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (handler!=null) {
            handler.removeMessages(1);
            handler.removeMessages(2);
        }
    }




    public void populate(GridView v){
        nPopulate.add(Nson.newObject().set("id", MN_CHECKIN).set("icon", R.drawable.mn_checkin).set("text", "CHECK IN"));
        nPopulate.add(Nson.newObject().set("id", MN_PEMBAYARAN).set("icon", R.drawable.m_pembayaran).set("text", "PEMBAYARAN"));
//        nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_WA).set("icon", R.drawable.wa).set("text", "MESSAGE"));
        //nPopulate.add(Nson.newObject().set("id", MN_SPAREPART).set("icon", R.drawable.mn_jualpart).set("text", "SPAREPART"));
        nPopulate.add(Nson.newObject().set("id", MN_TUGAS_PART).set("icon", R.drawable.m_tugaspart).set("text", "TUGAS PART"));
        nPopulate.add(Nson.newObject().set("id", MN_MEKANIK).set("icon", R.drawable.mn_alert).set("text", "MEKANIK"));

        nPopulate.add(Nson.newObject().set("id", MN_MYCODE).set("icon", R.drawable.m_mycode).set("text", "MY CODE"));
        nPopulate.add(Nson.newObject().set("id", MN_INSPEKSI).set("icon", R.drawable.mn_perawatan).set("text", "INSPEKSI"));

        nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_WA).set("icon", R.drawable.m_tugaspart).set("text", "MESSAGE"));
        nPopulate.add(Nson.newObject().set("id", MN_JUAL_PART).set("icon", R.drawable.mn_perawatan).set("text", "JUAL PART"));

        nPopulate.add(Nson.newObject().set("id", MN_TERIMA_PART).set("icon", R.drawable.m_tugaspart).set("text", "TERIMA PART"));
        nPopulate.add(Nson.newObject().set("id", MN_PART_KELUAR).set("icon", R.drawable.mn_perawatan).set("text", "PART KELUAR"));

        nPopulate.add(Nson.newObject().set("id", MN_PART).set("icon", R.drawable.m_tugaspart).set("text", "PART"));
        nPopulate.add(Nson.newObject().set("id", MN_BOOKING).set("icon", R.drawable.mn_perawatan).set("text", "BOOKING"));

        nPopulate.add(Nson.newObject().set("id", MN_MENUNGGU_PART).set("icon", R.drawable.m_tugaspart).set("text", "MENUNGGU PART"));
        nPopulate.add(Nson.newObject().set("id", MN_ANTAR_JEMPUT).set("icon", R.drawable.mn_perawatan).set("text", "ANTAR JEMPUT"));

        nPopulate.add(Nson.newObject().set("id", MN_PENUGASAN_MEKANIK).set("icon", R.drawable.m_tugaspart).set("text", "PENUGASAN"));
        nPopulate.add(Nson.newObject().set("id", MN_COLLECTION).set("icon", R.drawable.mn_perawatan).set("text", "KOLLECTION"));


    /*    nPopulate.add(Nson.newObject().set("id", MN_CHECKIN).set("icon", R.drawable.mn_checkin).set("text", "CHECK IN"));
//        nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_WA).set("icon", R.drawable.wa).set("text", "MESSAGE"));
        nPopulate.add(Nson.newObject().set("id", MN_SPAREPART).set("icon", R.drawable.mn_jualpart).set("text", "SPAREPART"));
        nPopulate.add(Nson.newObject().set("id", MN_CARI_PART).set("icon", R.drawable.mn_jualpart).set("text", "CARI PART"));
        nPopulate.add(Nson.newObject().set("id", MN_REGISTRASI).set("icon", R.drawable.mn_pelanggan).set("text", "REGISTRASI"));
        //nPopulate.add(Nson.newObject().set("id", MN_PART_SEARCH).set("icon", R.drawable.mn_jualpart).set("text", "CARI PART"));

        nPopulate.add(Nson.newObject().set("id", MN_PROFILE).set("icon", R.drawable.mn_pelanggan).set("text", "PROFILE"));
        //nPopulate.add(Nson.newObject().set("id", MN_LOKASI_PERSEDIAAN).set("icon", R.drawable.mn_booking).set("text", "LOKASI PERSEDIAAN"));
        //nPopulate.add(Nson.newObject().set("id", MN_JUAL_PART).set("icon", R.drawable.mn_belanja).set("text", "  PENJUALAN PART"));
        nPopulate.add(Nson.newObject().set("id", MN_TUGAS_PART).set("icon", R.drawable.mn_tugaspart).set("text", "TUGAS PART"));
        //nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_PenerimaanPart).set("icon", R.drawable.mn_tugaspart).set("text", "PENERIMAAN PART"));
        //nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_PartDiterima).set("icon", R.drawable.mn_tugaspart).set("text", "PART DI TERIMA"));
        //nPopulate.add(Nson.newObject().set("id", MN_STOCK_OPNAME).set("icon", R.drawable.mn_tugaspart).set("text", "STOCK OPNAME"));
        nPopulate.add(Nson.newObject().set("id", MN_PENUGASAN_MEKANIK).set("icon", R.drawable.mn_perawatan).set("text", "PENUGASAN MEKANIK"));
        nPopulate.add(Nson.newObject().set("id", MN_BIAYA_MEKANIK2).set("icon", R.drawable.mn_perawatan).set("text", "BIAYA MEKANIK"));
        nPopulate.add(Nson.newObject().set("id", MN_LOKASI_PART).set("icon", R.drawable.mn_belanja).set("text", "LOKASI PART"));
        nPopulate.add(Nson.newObject().set("id", MN_TERIMA_PART).set("icon", R.drawable.mn_tugaspart).set("text", "TERIMA PART"));
        nPopulate.add(Nson.newObject().set("id", MN_LAYANAN).set("icon", R.drawable.mn_pelanggan).set("text", "LAYANAN"));
        nPopulate.add(Nson.newObject().set("id", MN_TENDA).set("icon", R.drawable.mn_lainnya).set("text", "TENDA"));
        nPopulate.add(Nson.newObject().set("id", MN_JURNAL).set("icon", R.drawable.mn_perawatan).set("text", "JURNAL"));
        nPopulate.add(Nson.newObject().set("id", MN_SPOT_DISKON).set("icon", R.drawable.mn_belanja).set("text", "SPOT DISKON"));
        nPopulate.add(Nson.newObject().set("id", MN_DISCOUNT_PART).set("icon", R.drawable.mn_belanja).set("text", "DISKON PART"));
        nPopulate.add(Nson.newObject().set("id", MN_REKENING).set("icon", R.drawable.mn_pembayaran).set("text", "REKENING BANK"));
        nPopulate.add(Nson.newObject().set("id", MN_BOOKING).set("icon", R.drawable.mn_booking).set("text", "BOOKING"));
        nPopulate.add(Nson.newObject().set("id", MN_KONTROL_LAYANAN).set("icon", R.drawable.mn_booking).set("text", "CHECK-IN"));

        nPopulate.add(Nson.newObject().set("id", MN_DISCOUNT_LAYANAN).set("icon", R.drawable.mn_belanja).set("text", "DISCOUNT LAYANAN"));
        nPopulate.add(Nson.newObject().set("id", MN_DISCOUNT_JASALAIN).set("icon", R.drawable.mn_belanja).set("text", "DISCOUNT JASA LAIN"));
        nPopulate.add(Nson.newObject().set("id", MN_KOMISI_LAYANAN).set("icon", R.drawable.mn_inspeksi).set("text", "KOMISI LAYANAN"));
        nPopulate.add(Nson.newObject().set("id", MN_KOMISI_JASA_LAIN).set("icon", R.drawable.mn_inspeksi).set("text", "KOMISI JASA LAIN"));
        nPopulate.add(Nson.newObject().set("id", MN_KARYAWAN).set("icon", R.drawable.mn_pelanggan).set("text", "USER"));
        nPopulate.add(Nson.newObject().set("id", MN_REFERAL).set("icon", R.drawable.mn_lainnya).set("text", "REFERAL"));
        nPopulate.add(Nson.newObject().set("id", MN_FREKWENSI_DISCOUNT).set("icon", R.drawable.mn_lainnya).set("text", "FREKWENSI DISCOUNT"));
        nPopulate.add(Nson.newObject().set("id", MN_PART_KELUAR).set("icon", R.drawable.mn_tugaspart).set("text", "PART KELUAR"));
        nPopulate.add(Nson.newObject().set("id", MN_JUAL_PART).set("icon", R.drawable.mn_jualpart).set("text", "PENJUALAN PART"));
        nPopulate.add(Nson.newObject().set("id", MN_PEMBAYARAN).set("icon", R.drawable.mn_pembayaran).set("text", "PEMBAYARAN"));
        nPopulate.add(Nson.newObject().set("id", MN_KERJA_MEKANIK).set("icon", R.drawable.mn_homeservice).set("text", "PERINTAH KERJA MEKANIK"));
        nPopulate.add(Nson.newObject().set("id", MN_KOMISI_PART).set("icon", R.drawable.mn_inspeksi).set("text", "KOMISI PART"));
*/
//        nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_KomisiKaryawan).set("icon", R.drawable.sample).set("text", "MESSAGE"));
//        nPopulate.add(Nson.newObject().set("id", MN_MESSAGE_LoyaltiProgram).set("icon", R.drawable.sample).set("text", "MESSAGE"));
       // nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_perawatan).set("text", "MEKANIK"));
        /*nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_pembayaran).set("text", "KASIR"));
        nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_inspeksi).set("text", "INSPEKSI"));
        nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_tugaspart).set("text", "TUGAS PART"));
        nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_alert).set("text", "SERVICE ALERT"));
        nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_pelanggan).set("text", "DATA"));
        //nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_belanja).set("text", "BOOKING"));
        nPopulate.add(Nson.newObject().set("id", 1).set("icon", R.drawable.mn_lainnya).set("text", "LAINNYA"));
*/
        //nPopulate.add(Nson.newObject().set("id", MN_PART).set("icon", R.drawable.mn_lainnya).set("text", "PART"));
        //nPopulate.add(Nson.newObject().set("id", MN_PART_SEARCH).set("icon", R.drawable.mn_lainnya).set("text", "PART SEARCH"));

        ArrayAdapter<Vector<String>> arrayAdapter = new ArrayAdapter<Vector<String>>(MenuActivity.this, R.layout.activity_main_item, nPopulate.asArray()) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = getLayoutInflater().inflate(R.layout.activity_main_item, null);


                ((TextView)v.findViewById(R.id.txtIcon)).setText(  nPopulate.get(position).get("text").asString() );
                ((ImageView)v.findViewById(R.id.imgIcon)).setImageResource(  nPopulate.get(position).get("icon").asInteger());

                return v;
            }
        };
        GridView gridView = findViewById(R.id.gridView);
        gridView.setAdapter(arrayAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (item.getItemId() == R.id.action_logout){
            Messagebox.showDialog(getActivity(), "Logout", "Yakin Logout ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    UtilityAndroid.removeSettingAll(getActivity());

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == MN_SPAREPART){
                Intent i = new Intent(getActivity(), AturParts_Activity.class);
                i.putExtra("part", getIntentStringExtra(data, "part"));
                startActivityForResult(i, 112);
            }
        }
    }
}
