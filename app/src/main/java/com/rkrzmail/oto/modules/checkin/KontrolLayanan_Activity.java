package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class KontrolLayanan_Activity extends AppActivity {

    private AlertDialog alertDialog;
    private RecyclerView rvKontrolLayanan;

    private LinearLayout lyContainerFilter;
    private BottomSheetBehavior filterBottomSheet;

    private String sortBy = "";
    private boolean isSwipe = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_with_filter);
        initToolbar();
        initComponent();
        initSortByStatus();
        initSortByAntrian();
        initSortByPelanggan();
        initFilterBottomSheet();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Kontrol Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        rvKontrolLayanan = findViewById(R.id.recyclerView);
        lyContainerFilter = findViewById(R.id.ly_container_filter_kontrol_layanan);
        find(R.id.ly_container_filter_layanan).setVisibility(View.GONE);

        if (getIntent().hasExtra("NOPOL")) {
            viewKontrolLayanan(getIntentStringExtra("NOPOL"), "");
        } else {
            viewKontrolLayanan("", "");
        }

        rvKontrolLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvKontrolLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_kontrol_layanan) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                        super.onBindViewHolder(viewHolder, position);
                        //String estimasi = Tools.setFormatDateTimeFromDb(nListArray.get(position).get("ESTIMASI_SELESAI").asString(), "yyyy-MM-dd hh:mm", "dd/MM-hh:mm", false);
                        String waktu = Tools.setFormatDateTimeFromDb(nListArray.get(position).get("CREATED_DATE").asString(), "", "dd/MM hh:mm", true);
                        String waktuStatus = "";
                        if (!nListArray.get(position).get("MODIFIED_DATE").asString().isEmpty()) {
                            waktuStatus = Tools.setFormatDateTimeFromDb(nListArray.get(position).get("MODIFIED_DATE").asString(), "", "dd/MM hh:mm", true);
                        }

                        if (nListArray.get(position).get("PELANGGAN_TIDAK_MENUNGGU").asString().equals("Y")) {
                            viewHolder.find(R.id.tv_tidak_menunggu, TextView.class).setText("TIDAK MENUNGGU");
                            viewHolder.find(R.id.tv_tidak_menunggu, TextView.class).setText("TIDAK MENUNGGU");
                            viewHolder.find(R.id.tv_waktu_ambil, TextView.class).setText(nListArray.get(position).get("WAKTU_AMBIL").asString());
                        } else {
                            viewHolder.find(R.id.tv_tidak_menunggu, TextView.class).setText("MENUNGGU");
                            viewHolder.find(R.id.tv_waktu_ambil, TextView.class).setText("");
                        }
                        viewHolder.find(R.id.tv_waktu_checkin, TextView.class).setText(waktu);
                        viewHolder.find(R.id.tv_no_antrian, TextView.class).setText(nListArray.get(position).get("NO_ANTRIAN").asString());
                        viewHolder.find(R.id.tv_nopol, TextView.class).setText(formatNopol(nListArray.get(position).get("NOPOL").asString()));
                        viewHolder.find(R.id.tv_jenis_kendaraan, TextView.class).setText(nListArray.get(position).get("JENIS_KENDARAAN").asString());
                        viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                        viewHolder.find(R.id.tv_layanan, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                        viewHolder.find(R.id.tv_estimasi_selesai, TextView.class).setText(nListArray.get(position).get("ESTIMASI_SELESAI").asString().equals("SELESAI") ? "" : nListArray.get(position).get("ESTIMASI_SELESAI").asString());
                        viewHolder.find(R.id.tv_no_kunci, TextView.class).setText(nListArray.get(position).get("NO_KUNCI").asString());
                        viewHolder.find(R.id.tv_waktu_status, TextView.class).setText(waktuStatus);
                        viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("STATUS_KONTROL").asString());

                        viewHolder.find(R.id.img_more_booking, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.img_more_booking, ImageButton.class));
                                popup.inflate(R.menu.menu_history);
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        if (menuItem.getItemId() == R.id.action_history) {
                                            Intent i = new Intent(getActivity(), History_Activity.class);
                                            i.putExtra(SET_CHECKIN, nListArray.get(position).toJson());
                                            startActivity(i);
                                        }
                                        return true;
                                    }
                                });
                                popup.show();
                            }
                        });
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), DetailKontrolLayanan_Activity.class);
                        i.putExtra(DATA, nListArray.get(position).toJson());
                        startActivityForResult(i, REQUEST_DETAIL);
                    }
                })
        );

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewKontrolLayanan("", "");
            }
        });

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

    private void initSortByStatus() {
        final Nson statusList = Nson.newArray();
        statusList.add(Nson.newObject().set("tittle", "ALL"));
        statusList.add(Nson.newObject().set("tittle", "CHECKIN ANTRIAN"));
        statusList.add(Nson.newObject().set("tittle", "DEBET DP"));
        statusList.add(Nson.newObject().set("tittle", "GANTI MEKANIK"));
        statusList.add(Nson.newObject().set("tittle", "INSPEKSI, PELAYANAN SELESAI"));
        statusList.add(Nson.newObject().set("tittle", "KREDIT DP"));
        statusList.add(Nson.newObject().set("tittle", "MEKANIK MULAI"));
        statusList.add(Nson.newObject().set("tittle", "MEKANIK PAUSE"));
        statusList.add(Nson.newObject().set("tittle", "PELAYANAN SELESAI"));
        statusList.add(Nson.newObject().set("tittle", "PENUGASAN INSPKESI"));
        statusList.add(Nson.newObject().set("tittle", "PENUGASAN MEKANIK"));
        statusList.add(Nson.newObject().set("tittle", "TAMBAH PART - JASA"));
        statusList.add(Nson.newObject().set("tittle", "TRANSFER DP"));
        statusList.add(Nson.newObject().set("tittle", "TUNGGU DP"));


        RecyclerView rvStatus = setRecylerViewSortBy(R.id.rv_status, 2);
        if (rvStatus != null) {
            rvStatus.setAdapter(new NikitaRecyclerAdapter(statusList, R.layout.item_sort_by) {
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    if(sortBy.isEmpty()){
                        viewHolder.find(R.id.img_check_selected).setVisibility(statusList.get(position).get("tittle").asString().equals("ALL") ? View.VISIBLE : View.GONE);
                    }else{
                        viewHolder.find(R.id.img_check_selected).setVisibility(sortBy.equals(statusList.get(position).get("tittle").asString()) ? View.VISIBLE : View.GONE);
                    }

                    viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(statusList.get(position).get("tittle").asString());
                    viewHolder.find(R.id.ly_container_status).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sortBy = statusList.get(position).get("tittle").asString();
                            if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                                filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            }
                            if (statusList.get(position).get("tittle").asString().equals("ALL")) {
                                viewKontrolLayanan("", "");
                            } else {
                                viewKontrolLayanan("", statusList.get(position).get("tittle").asString());
                            }
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    private void initSortByAntrian() {
        final Nson antrianList = Nson.newArray();
        antrianList.add(Nson.newObject().set("tittle", "ALL"));
        antrianList.add(Nson.newObject().set("tittle", "EXPRESS"));
        antrianList.add(Nson.newObject().set("tittle", "STANDART"));
        antrianList.add(Nson.newObject().set("tittle", "EXTRA"));
        antrianList.add(Nson.newObject().set("tittle", "H+"));

        RecyclerView rvAntrian = setRecylerViewSortBy(R.id.rv_antrian, 1);
        rvAntrian.setAdapter(new NikitaRecyclerAdapter(antrianList, R.layout.item_sort_by) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                if(sortBy.isEmpty()){
                    viewHolder.find(R.id.img_check_selected).setVisibility(antrianList.get(position).get("tittle").asString().equals("ALL") ? View.VISIBLE : View.GONE);
                }else{
                    viewHolder.find(R.id.img_check_selected).setVisibility(sortBy.equals(antrianList.get(position).get("tittle").asString()) ? View.VISIBLE : View.GONE);
                }
                viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(antrianList.get(position).get("tittle").asString());
                viewHolder.find(R.id.ly_container_status).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortBy = antrianList.get(position).get("tittle").asString();
                        if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        if (antrianList.get(position).get("tittle").asString().equals("ALL")) {
                            viewKontrolLayanan("", "");
                        } else {
                            viewKontrolLayanan("", antrianList.get(position).get("tittle").asString());
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initSortByPelanggan() {
        final Nson pelangganList = Nson.newArray();
        pelangganList.add(Nson.newObject().set("tittle", "ALL"));
        pelangganList.add(Nson.newObject().set("tittle", "MENUNGGU"));
        pelangganList.add(Nson.newObject().set("tittle", "TIDAK MENUNGGU"));

        RecyclerView rvPelanggan = setRecylerViewSortBy(R.id.rv_pelanggan, 1);
        rvPelanggan.setAdapter(new NikitaRecyclerAdapter(pelangganList, R.layout.item_sort_by) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                if(sortBy.isEmpty()){
                    viewHolder.find(R.id.img_check_selected).setVisibility(pelangganList.get(position).get("tittle").asString().equals("ALL") ? View.VISIBLE : View.GONE);
                }else{
                    viewHolder.find(R.id.img_check_selected).setVisibility(sortBy.equals(pelangganList.get(position).get("tittle").asString()) ? View.VISIBLE : View.GONE);
                }
                viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(pelangganList.get(position).get("tittle").asString());
                viewHolder.find(R.id.ly_container_status).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortBy = pelangganList.get(position).get("tittle").asString();
                        if (filterBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            filterBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        if (pelangganList.get(position).get("tittle").asString().equals("ALL")) {
                            viewKontrolLayanan("", "");
                        } else {
                            viewKontrolLayanan("", pelangganList.get(position).get("tittle").asString());
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });

    }

    private RecyclerView setRecylerViewSortBy(int viewId, int spanCount) {
        if (viewId == 0) return null;
        RecyclerView recyclerView = findViewById(viewId);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager
                (
                        spanCount,
                        StaggeredGridLayoutManager.HORIZONTAL
                );
        recyclerView.setLayoutManager(layoutManager);
        return recyclerView;
    }

    private void viewKontrolLayanan(final String cari, final String sortBy) {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                args.put("sortBy", sortBy);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    nListArray.asArray().clear();
                    for (int i = 0; i < result.size(); i++) {
                        if (!result.get(i).get("STATUS_KONTROL").asString().isEmpty()) {
                            nListArray.add(result.get(i));
                        }
                    }

                    rvKontrolLayanan.getAdapter().notifyDataSetChanged();
                    rvKontrolLayanan.scheduleLayoutAnimation();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }


    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari No. Polisi"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default

        adapterSearchView(mSearchView, "", VIEW_KONTROL_LAYANAN, "NOPOL", "");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                viewKontrolLayanan(query, "");

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewKontrolLayanan("", "");
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
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN)
            viewKontrolLayanan("", "");
        else if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL)
            viewKontrolLayanan("", "");
        else if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS)
            viewKontrolLayanan("", "");
    }
}
