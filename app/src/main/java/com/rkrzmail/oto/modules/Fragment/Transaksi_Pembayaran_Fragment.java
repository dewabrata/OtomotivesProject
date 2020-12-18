package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.Rincian_Pembayaran_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_JUAL_PART;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_LAYANAN;


public class Transaksi_Pembayaran_Fragment extends Fragment {

    private RecyclerView rvPembayaranCheckin;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Nson pembayaranList = Nson.newArray();
    private String idCheckin = "";

    public Transaksi_Pembayaran_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        initHideToolbar(view);
        initRecylerviewPembayaran(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewPembayaran();
        }
    }


    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }


    private void initRecylerviewPembayaran(View view) {
        rvPembayaranCheckin = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvPembayaranCheckin.setHasFixedSize(true);
        rvPembayaranCheckin.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPembayaranCheckin.setAdapter(new NikitaMultipleViewAdapter(pembayaranList, R.layout.item_pembayaran, R.layout.item_permintaan_jual_part_tugas_part) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                int viewType = getItemViewType(position);

                if (viewType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_jenis_kendaraan, TextView.class).setText(pembayaranList.get(position).get("JENIS_KENDARAAN").asString());
                    viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(pembayaranList.get(position).get("NAMA_PELANGGAN").asString());
                    viewHolder.find(R.id.tv_nopol, TextView.class).setText(((Pembayaran_MainTab_Activity)getActivity()).formatNopol(pembayaranList.get(position).get("NOPOL").asString()));
                    viewHolder.find(R.id.tv_layanan, TextView.class).setText(pembayaranList.get(position).get("LAYANAN").asString());
                    viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(pembayaranList.get(position).get("NO_PONSEL").asString());
                    viewHolder.find(R.id.tv_no_kunci, TextView.class).setText(pembayaranList.get(position).get("NO_KUNCI").asString());
                    viewHolder.find(R.id.cv_pembayaran_checkin, CardView.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            idCheckin = pembayaranList.get(position).get(ID).asString();
                            Intent i = new Intent(getActivity(), Rincian_Pembayaran_Activity.class);
                            i.putExtra(RINCIAN_LAYANAN, "");
                            i.putExtra(DATA, pembayaranList.get(position).toJson());
                            startActivityForResult(i, REQUEST_DETAIL);
                        }
                    });
                } else if (viewType == ITEM_VIEW_2) {
                    String noHp = pembayaranList.get(position).get("NO_PONSEL").asString();
                    if (noHp.length() > 4) {
                        noHp = noHp.substring(noHp.length() - 4);
                    }
                    viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(pembayaranList.get(position).get("USER_JUAL").asString());
                    viewHolder.find(R.id.tv_nama_pelanggan_nama_usaha, TextView.class).setText(
                            pembayaranList.get(position).get("NAMA_PELANGGAN").asString() + " " + pembayaranList.get(position).get("NAMA_USAHA").asString());
                    viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText("XXXXXXXX" + noHp);
                    viewHolder.find(R.id.tv_tgl, TextView.class).setText(pembayaranList.get(position).get("TANGGAL").asString());
                    viewHolder.find(R.id.cv_pembayaran_jual_part, CardView.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getActivity(), Rincian_Pembayaran_Activity.class);
                            i.putExtra(RINCIAN_JUAL_PART, "");
                            i.putExtra(DATA, pembayaranList.get(position).toJson());
                            startActivityForResult(i, REQUEST_DETAIL);
                        }
                    });
                }
            }
        });

       swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewPembayaran();
            }
        });
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

    private void viewPembayaran() {
        ((Pembayaran_MainTab_Activity) getActivity()).newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "TRANSAKSI");
                args.put("jenisPembayaran", "CHECKIN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                pembayaranList.asArray().clear();
                pembayaranList.asArray().addAll(result.get("data").asArray());

                args.remove("jenisPembayaran");
                args.put("jenisPembayaran", "JUAL PART");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                pembayaranList.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    rvPembayaranCheckin.getAdapter().notifyDataSetChanged();
                    rvPembayaranCheckin.scheduleLayoutAnimation();
                } else {
                    ((Pembayaran_MainTab_Activity) getActivity()).showError(ERROR_INFO);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL)
            viewPembayaran();
    }
}