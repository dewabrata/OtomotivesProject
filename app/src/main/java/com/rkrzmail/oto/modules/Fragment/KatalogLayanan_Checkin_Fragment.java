package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Checkin_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.GET_KATALOG_PAKET_LAYANAN;
import static com.rkrzmail.utils.APIUrls.GET_KENDARAAN_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.RP;

public class KatalogLayanan_Checkin_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private NikitaAutoComplete etSearch;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgSubmit;
    private RecyclerView rvData;

    private final Nson layananList = Nson.newArray();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.layout_recylerview_with_search_box, container, false);
        activity = (Checkin_MainTab_Activity) getActivity();
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);
        etSearch = fragmentView.findViewById(R.id.et_search);
        imgSubmit = fragmentView.findViewById(R.id.img_submit_search);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData("");
            }
        });

        initRv(fragmentView);
        initAutoCompleteSearch();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getData("");
    }

    private void initRv(View view) {
        rvData = view.findViewById(R.id.recyclerView);
        rvData.setHasFixedSize(true);
        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(new NikitaRecyclerAdapter(layananList, R.layout.item_katalog_layanan) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                @SuppressLint("DefaultLocale") String waktuLayanan = String.format("%02d:%02d:%02d",
                        layananList.get(position).get("WAKTU_LAYANAN_HARI").asInteger(),
                        layananList.get(position).get("WAKTU_LAYANAN_JAM").asInteger(),
                        layananList.get(position).get("WAKTU_LAYANAN_MENIT").asInteger());

                viewHolder.find(R.id.tv_nama_layanan, TextView.class).setText(layananList.get(position).get("NAMA_LAYANAN").asString());
                viewHolder.find(R.id.tv_biaya_layanan, TextView.class).setText(RP + NumberFormatUtils.formatRp(layananList.get(position).get("BIAYA_PAKET").asString()));
                viewHolder.find(R.id.tv_waktu_layanan, TextView.class).setText(waktuLayanan);
                viewHolder.find(R.id.tv_jenis_antrian, TextView.class).setText(layananList.get(position).get("JENIS_ANTRIAN").asString());
                viewHolder.find(R.id.tv_part_wajib, TextView.class).setText(layananList.get(position).get("PART_WAJIB").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

            }
        }));
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    public void getData(final String cari) {
        if (!Tools.isNetworkAvailable(getActivity())) {
            activity.showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }else{
            activity.newProses(new Messagebox.DoubleRunnable() {
                Nson result;

                @Override
                public void run() {
                    swipeProgress(true);
                    String[] args = new String[3];
                    args[0] = "CID=" + UtilityAndroid.getSetting(getContext(), "CID", "").trim();
                    args[1] = "search="  + cari;
                    result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_KATALOG_PAKET_LAYANAN), args));
                }

                @Override
                public void runUI() {
                    swipeProgress(false);
                    if (result.get("status").asBoolean()) {
                        result = result.get("data");
                        layananList.asArray().clear();
                        layananList.asArray().addAll(result.asArray());
                        rvData.getAdapter().notifyDataSetChanged();
                    } else {
                        activity.showError("Terjadi Kesalahan");
                    }
                }
            });

        }
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }

    private void initAutoCompleteSearch() {
        etSearch.setThreshold(0);
        etSearch.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getContext(), "CID", "").trim();
                args[1] = "search="  + bookTitle;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_KENDARAAN_LAYANAN), args));
                result = result.get("data");
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }

                findView(convertView, R.id.title, TextView.class).setText(getItem(position).get("JENIS_KENDARAAN").asString());
                return convertView;
            }
        });

        etSearch.setLoadingIndicator((android.widget.ProgressBar) fragmentView.findViewById(R.id.pb_search));
        etSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Nson data = Nson.readJson(String.valueOf(parent.getItemAtPosition(position)));
                etSearch.setText(data.get("JENIS_KENDARAAN").asString());
                getData(data.get("JENIS_KENDARAAN").asString());
            }
        });

        imgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etSearch.getText().toString().isEmpty()) {
                    getData(etSearch.getText().toString());
                } else {
                    imgSubmit.setVisibility(View.GONE);
                    etSearch.setError("Pencarian Harus di Isi");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            etSearch.setError(null);
                            imgSubmit.setVisibility(View.VISIBLE);
                        }
                    }, 2000);
                }
            }
        });
    }

}
