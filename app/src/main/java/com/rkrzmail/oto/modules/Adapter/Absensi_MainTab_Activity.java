package com.rkrzmail.oto.modules.Adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.KomisiUser_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.APIUrls.WEB_LOGIN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class Absensi_MainTab_Activity extends AppActivity {

    ViewPager vpAbsensi;
    TabLayout tabAbsensi;
    FragmentsAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Absen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        vpAbsensi = findViewById(R.id.vp);
        tabAbsensi = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Absen_Absensi_Fragment());
        fragments.add(new Schedule_Absensi_Fragment());
        fragments.add(new KomisiUser_Absensi_Fragment());

        pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpAbsensi.setAdapter(pagerAdapter);
        vpAbsensi.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabAbsensi));
        tabAbsensi.setupWithViewPager(vpAbsensi);

        setScanBarcode();
    }

    private void setScanBarcode() {
        find(R.id.img_scan_barcode, ImageButton.class).setVisibility(View.VISIBLE);
        find(R.id.img_scan_barcode, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });
    }

    private void absenUser(final String myCodeUser) {
        if(!Tools.isNetworkAvailable(getActivity())){
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "ABSEN");
                args.put("noPonsel", myCodeUser);
                args.put("hari", getDayOfWeek());
                //args.put("scheduleMulai", currentDateTime("HH:mm:ss"));
                args.put("absenMulai", currentDateTime("HH:mm:ss"));
                //args.put("lamaTerlambat", "");
                args.put("absenSelesai", currentDateTime("HH:mm:ss"));
                //args.put("izinTerlambat", "N");
                //args.put("userIzin", "");
                args.put("lokasi", "BENGKEL");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ABSEN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(vpAbsensi.getCurrentItem() == 0){
                        Absen_Absensi_Fragment absen = (Absen_Absensi_Fragment) Objects.requireNonNull(vpAbsensi.getAdapter()).instantiateItem(vpAbsensi, vpAbsensi.getCurrentItem());
                        absen.viewAbsensi(Absensi_MainTab_Activity.this);
                    }
                    showSuccess("Absen Berhasil");
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void webLogin(final String qrCode){
        if(!Tools.isNetworkAvailable(getActivity())){
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("qr", qrCode);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(WEB_LOGIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess(result.get("data").get("MESSAGE").asString());
                }
            }
        });
    }

    private String getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return "Minggu";
            case Calendar.MONDAY:
                return "Senin";
            case Calendar.TUESDAY:
                return "Selasa";
            case Calendar.WEDNESDAY:
                return "Rabu";
            case Calendar.THURSDAY:
                return "Kamis";
            case Calendar.FRIDAY:
                return "Jumat";
            case Calendar.SATURDAY:
                return "Sabtu";
            default:
                return "";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String barcodeResult = data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "";
            if(barcodeResult.contains("WEB")){
                webLogin(barcodeResult);
            }else {
                MyCode.checkMyCode(this, barcodeResult, new MyCode.RunnableWD() {
                    @Override
                    public void runWD(Nson nson) {
                        if (nson.get("status").asString().equals("OK")) {
                            if (nson.get("data").asArray().isEmpty()) {
                                showError("SILAHKAN REFRESH BARCODE ANDA!");
                            }else{
                                nson = nson.get("data").get(0);
                                absenUser(nson.get("USERID").asString());
                            }
                        } else {
                            showError(ERROR_INFO);
                        }
                    }
                });
            }
        }
    }
}