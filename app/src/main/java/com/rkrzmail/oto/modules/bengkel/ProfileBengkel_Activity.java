package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Fragment.Schedule_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tambahan_Profile_Fragment;
import com.rkrzmail.oto.modules.Fragment.Usaha_Profile_Fragment;
import com.rkrzmail.oto.modules.Adapter.FragmentsAdapter;

import java.util.ArrayList;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;

public class ProfileBengkel_Activity extends AppActivity {

    private Nson data;

    private final Usaha_Profile_Fragment usahaProfileFragment = new Usaha_Profile_Fragment();
    private final Tambahan_Profile_Fragment tambahanProfileFragment = new Tambahan_Profile_Fragment();
    private final Schedule_Profile_Fragment scheduleProfileFragment = new Schedule_Profile_Fragment();

    private UsahaData usahaDataInterface;
    private TambahanData tambahanDataInterface;
    private ScheduleData scheduleDataInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initComponent();
        getData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {
        initToolbar();
        ViewPager vpProfile = findViewById(R.id.vp);
        TabLayout tabLayout = findViewById(R.id.tablayout);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(usahaProfileFragment);
        fragments.add(tambahanProfileFragment);
        fragments.add(scheduleProfileFragment);

        FragmentsAdapter fragmentsAdapter = new FragmentsAdapter(getSupportFragmentManager(), getActivity(), fragments);

        vpProfile.setAdapter(fragmentsAdapter);
        vpProfile.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                getData();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabLayout.setupWithViewPager(vpProfile);
    }

    public UsahaData getDataUsaha(UsahaData usahaData){
        this.usahaDataInterface = usahaData;
        return this.usahaDataInterface;
    }

    public TambahanData getDataTambahan(TambahanData tambahanData){
        this.tambahanDataInterface = tambahanData;
        return this.tambahanDataInterface;
    }


    public void getData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PROFILE), args));
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    data = data.get("data").get(0);

                    if(usahaDataInterface != null)
                        usahaDataInterface.getData(data);
                    if(tambahanDataInterface != null)
                        tambahanDataInterface.getData(data);
                    if(scheduleDataInterface != null)
                        scheduleDataInterface.getData(data);

                } else {
                    showError(data.get("message").asString());
                }
            }
        });
    }

    public interface UsahaData {
        void getData(Nson data);
    }

    public interface TambahanData {
        void getData(Nson data);
    }

    public interface ScheduleData{
        void getData(Nson data);
    }
}
