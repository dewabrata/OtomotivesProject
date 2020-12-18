package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;
import com.rkrzmail.srv.FragmentsAdapter;
import com.rkrzmail.utils.Tools;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class Absensi_MainTab_Activity extends AppActivity {

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
        ViewPager vpAbsensi = findViewById(R.id.vp);
        TabLayout tabAbsensi = findViewById(R.id.tablayout);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Absen_Absensi_Fragment());
        fragments.add(new Schedule_Absensi_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
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
                    showSuccess("Absen Berhasil");
//                    Absen_Absensi_Fragment absen_absensi_fragment = new Absen_Absensi_Fragment();
//                    absen_absensi_fragment.viewAbsensi(Absensi_MainTab_Activity.this);
                } else {
                    showError(result.get("message").asString());
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
                return "Selesa";
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
            MyCode.checkMyCode(this, barcodeResult, new MyCode.RunnableWD() {
                @Override
                public void runWD(Nson nson) {
                    if (nson.get("status").asString().equals("OK")) {
                        if (nson.get("data").asArray().isEmpty()) {
                            showError("Silahkan Refresh Barcode Anda!");
                            return;
                        }
                        nson = nson.get("data").get(0);
                        absenUser(nson.get("USERID").asString());
                        Log.d("Barcode__", "onActivityResult: " + nson);
                    } else {
                        showError(ERROR_INFO);
                    }
                }
            });
        }
    }
}