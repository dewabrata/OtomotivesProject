package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SALDO;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Saldo_Activity extends AppActivity {

    private RecyclerView recyclerView;
    private LinearLayout lyContainerFilter;
    private BottomSheetBehavior filterBottomSheet;

    private String sortBy = "";
    private boolean isKasir = false;
    private final Nson saldoList = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_filter);
        isKasir = getIntent().hasExtra("KASIR");
        initToolbar();
        initRv();
        viewSaldo("");
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(isKasir ? "Saldo Kasir" : "Saldo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        lyContainerFilter = findViewById(R.id.ly_container_filter_saldo);

        if (isKasir) {
            find(R.id.container_filter).setVisibility(View.GONE);
            FrameLayout.LayoutParams ps = (FrameLayout.LayoutParams) find(R.id.swiperefresh, SwipeRefreshLayout.class).getLayoutParams();
            int marginInDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 60, getResources()
                            .getDisplayMetrics());
            ps.topMargin = marginInDp;

            find(R.id.swiperefresh, SwipeRefreshLayout.class).setLayoutParams(ps);
            find(R.id.swiperefresh, SwipeRefreshLayout.class).requestLayout();
        }
        find(R.id.fab_tambah).setVisibility(View.VISIBLE);
        find(R.id.ly_container_filter_layanan).setVisibility(View.GONE);
        find(R.id.ly_container_filter_kontrol_layanan).setVisibility(View.GONE);

        find(R.id.btn_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filterBottomSheet.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    filterBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        find(R.id.img_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewSaldo("");
            }
        });

        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getActivity(), Atur_Saldo_Activity.class).putExtra("KASIR", isKasir),
                        REQUEST_DETAIL
                );
            }
        });

        initFilterBottomSheet();
        initSortByJenisAkun();
    }

    private void initFilterBottomSheet() {
        filterBottomSheet = BottomSheetBehavior.from(lyContainerFilter);
        filterBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;

                    case BottomSheetBehavior.STATE_EXPANDED: {

                    }
                    break;

                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        find(R.id.view_blur).setVisibility(View.GONE);
                    }
                    break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float slideOffset) {
                find(R.id.view_blur).setVisibility(View.VISIBLE);
                find(R.id.view_blur).setAlpha(slideOffset);
            }
        });
    }

    private void initSortByJenisAkun() {
        final Nson jenisLayananList = Nson.newArray();
        jenisLayananList.add(Nson.newObject().set("tittle", "ALL"));
        jenisLayananList.add(Nson.newObject().set("tittle", "KAS"));
        jenisLayananList.add(Nson.newObject().set("tittle", "BANK"));
        jenisLayananList.add(Nson.newObject().set("tittle", "EPAY"));


        RecyclerView recyclerView = findViewById(R.id.rv_jenis_akun);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager
                (
                        1,
                        StaggeredGridLayoutManager.HORIZONTAL
                );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(jenisLayananList, R.layout.item_sort_by) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                if (sortBy.isEmpty()) {
                    viewHolder.find(R.id.img_check_selected).setVisibility(jenisLayananList.get(position).get("tittle").asString().equals("ALL") ? View.VISIBLE : View.GONE);
                } else {
                    viewHolder.find(R.id.img_check_selected).setVisibility(sortBy.equals(jenisLayananList.get(position).get("tittle").asString()) ? View.VISIBLE : View.GONE);
                }
                viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(jenisLayananList.get(position).get("tittle").asString());
                viewHolder.find(R.id.ly_container_status).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortBy = jenisLayananList.get(position).get("tittle").asString();
                        if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        if (jenisLayananList.get(position).get("tittle").asString().equals("ALL")) {
                            viewSaldo("");
                        } else {
                            viewSaldo(jenisLayananList.get(position).get("tittle").asString());
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initRv() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        if (isKasir) {
            recyclerView.setAdapter(new NikitaRecyclerAdapter(saldoList, R.layout.item_saldo) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.tv_no_rek, TextView.class).setVisibility(View.GONE);
                    viewHolder.find(R.id.tv_nama_bank, TextView.class).setVisibility(View.GONE);

                    viewHolder.find(R.id.tv_akun, TextView.class).setText(saldoList.get(position).get("AKUN").asString());
                    viewHolder.find(R.id.tv_tanggal, TextView.class).setText(saldoList.get(position).get("CREATED_DATE").asString());
                    viewHolder.find(R.id.tv_saldo_akhir, TextView.class).setText(RP + NumberFormatUtils.formatRp(saldoList.get(position).get("SALDO_AKHIR").asString()));
                    viewHolder.find(R.id.tv_saldo_disesuaikan, TextView.class).setText(RP + NumberFormatUtils.formatRp(saldoList.get(position).get("SALDO_PENYESUAIAN").asString()));

                }
            }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Nson parent, View view, int position) {
                    Intent intent = new Intent(getActivity(), Atur_Saldo_Activity.class);
                    intent.putExtra(DATA, parent.get(position).toJson());
                    startActivityForResult(intent, REQUEST_DETAIL);
                }
            }));

        } else {
            recyclerView.setAdapter(new NikitaRecyclerAdapter(saldoList, R.layout.item_saldo) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    if (saldoList.get(position).get("AKUN").asString().equals("BANK")) {
                        viewHolder.find(R.id.tv_no_rek, TextView.class).setText((saldoList.get(position).get("NO_REKENING").asString()));
                        viewHolder.find(R.id.tv_nama_bank, TextView.class).setText(saldoList.get(position).get("KETERANGAN").asString());
                    } else {
                        viewHolder.find(R.id.tv_no_rek, TextView.class).setVisibility(View.GONE);
                        viewHolder.find(R.id.tv_nama_bank, TextView.class).setVisibility(View.GONE);
                    }
                    viewHolder.find(R.id.tv_akun, TextView.class).setText(saldoList.get(position).get("AKUN").asString());
                    viewHolder.find(R.id.tv_tanggal, TextView.class).setText(saldoList.get(position).get("CREATED_DATE").asString());
                    viewHolder.find(R.id.tv_saldo_akhir, TextView.class).setText(RP + NumberFormatUtils.formatRp(saldoList.get(position).get("SALDO_AKHIR").asString()));
                    viewHolder.find(R.id.tv_saldo_disesuaikan, TextView.class).setText(RP + NumberFormatUtils.formatRp(saldoList.get(position).get("SALDO_PENYESUAIAN").asString()));

                }
            }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Nson parent, View view, int position) {
                    Intent intent = new Intent(getActivity(), Atur_Saldo_Activity.class);
                    intent.putExtra(DATA, parent.get(position).toJson());
                    startActivityForResult(intent, REQUEST_DETAIL);
                }
            }));

        }
    }

    private void viewSaldo(final String sortBy) {
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET");
            return;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                args[1] = "sortBy=" + sortBy;
                args[2] = "isKasir=" + isKasir;
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(SALDO), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                    saldoList.asArray().clear();
                    saldoList.asArray().addAll(result.asArray());
                    Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) viewSaldo("");
    }
}