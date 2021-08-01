package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Dashboard_MainTab_Activity;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;
import java.util.Vector;

import static com.rkrzmail.utils.APIUrls.VIEW_DASHBOARD;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Dashboard_Statistik_Fragment extends Fragment {

    private View fragmentView;

    private final String JASA_PART = "JASA PART";
    private final String JASA_LAIN = "JASA LAIN";
    private final String LAIINYA = "LAINNYA";
    private final String DISCOUNT = "DISCOUNT";
    private final String PENJUALAN_LAIN = "PENJUALAN LAIN";
    private final String INCOME = "INCOME";
    private final String BIAYA = "BIAYA";
    private final String HPP_PART = "HPP PART";
    private final String MARGIN = "MARGIN";
    private final String MARGIN_PART = "MARGIN PART";
    private final String CHECKIN_AVERAGE = "RP. / CHECKIN";
    private final String JUAL_PART_AVERAGE = "RP. / JUAL PART";
    private final String KAS = "KAS";
    private final String BANK = "BANK";
    private final String PART_ONLINE = "PART ONLINE";

    private Nson transaksiList = Nson.newArray();
    private AppActivity activity;

    public Dashboard_Statistik_Fragment() {

    }

    public static Dashboard_Statistik_Fragment newInstance(String param1, String param2) {
        Dashboard_Statistik_Fragment fragment = new Dashboard_Statistik_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_dashboard_statistik, container, false);
        activity = (Dashboard_MainTab_Activity) getActivity();
        setTransaksiView();
        fragmentView.findViewById(R.id.btn_tampilkan).setEnabled(false);
        fragmentView.findViewById(R.id.btn_tampilkan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDashboard();
            }
        });
        return fragmentView;
    }

    private void addItem(int icon, String text) {
        transaksiList.add(Nson.newObject().set("icon", icon).set("text", text));
    }

    private void setTransaksiView() {
        addItem(R.drawable.ic_up_arrow_statistik_resize, JASA_PART);// percent, values
        addItem(R.drawable.ic_up_arrow_statistik_resize, JASA_LAIN);
        addItem(R.drawable.ic_up_arrow_statistik_resize, LAIINYA);
        addItem(R.drawable.ic_up_arrow_statistik_resize, DISCOUNT);
        addItem(R.drawable.ic_up_arrow_statistik_resize, PENJUALAN_LAIN);
        addItem(R.drawable.ic_up_arrow_statistik_resize, INCOME);
        addItem(R.drawable.ic_up_arrow_statistik_resize, BIAYA);
        addItem(R.drawable.ic_up_arrow_statistik_resize, HPP_PART);
        addItem(R.drawable.ic_up_arrow_statistik_resize, MARGIN);
        addItem(R.drawable.ic_up_arrow_statistik_resize, MARGIN_PART);
        addItem(R.drawable.ic_up_arrow_statistik_resize, CHECKIN_AVERAGE);
        addItem(R.drawable.ic_up_arrow_statistik_resize, JUAL_PART_AVERAGE);
        addItem(R.drawable.ic_up_arrow_statistik_resize, KAS);
        addItem(R.drawable.ic_up_arrow_statistik_resize, BANK);
        addItem(R.drawable.ic_up_arrow_statistik_resize, PART_ONLINE);

        ArrayAdapter<Vector<String>> arrayAdapter = new ArrayAdapter<Vector<String>>(getActivity(), R.layout.item_dashboard_statistik, transaksiList.asArray()) {
            @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = getLayoutInflater().inflate(R.layout.item_dashboard_statistik, null);

                ((TextView) v.findViewById(R.id.tv_tittle)).setText(transaksiList.get(position).get("text").asString());
                ((ImageView) v.findViewById(R.id.img_down_or_up)).setImageResource(transaksiList.get(position).get("icon").asInteger());
                ((TextView) v.findViewById(R.id.tv_rp_values)).setText(RP + NumberFormatUtils.formatRp("0"));
                return v;
            }
        };

        GridView gridView = fragmentView.findViewById(R.id.gridView);
        gridView.setAdapter(arrayAdapter);
    }

    @SuppressLint("NewApi")
    private void viewDashboard() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("kategori", "STATISTIK");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_DASHBOARD), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);

                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }


}