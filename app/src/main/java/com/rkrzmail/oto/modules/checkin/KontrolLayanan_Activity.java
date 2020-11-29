package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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

import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class KontrolLayanan_Activity extends AppActivity {

    private RecyclerView rvKontrolLayanan;
    private boolean isSwipe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kontrol Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvKontrolLayanan = findViewById(R.id.recyclerView);
        catchData("");
        rvKontrolLayanan.setLayoutManager(new LinearLayoutManager(this));
        rvKontrolLayanan.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_kontrol_layanan) {
                    @Override
                    public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                        super.onBindViewHolder(viewHolder, position);
                        String estimasi = Tools.setFormatDateTimeFromDb(nListArray.get(position).get("ESTIMASI_SELESAI").asString(), "yyyy-MM-dd hh:mm", "dd/MM-hh:mm", false);
                        String waktu =  Tools.setFormatDateTimeFromDb(nListArray.get(position).get("CREATED_DATE").asString(), "", "dd/MM hh:mm", true);

                        viewHolder.find(R.id.tv_waktu_checkin, TextView.class).setText(waktu);
                        viewHolder.find(R.id.tv_no_antrian, TextView.class).setText(nListArray.get(position).get("NO_ANTRIAN").asString());
                        viewHolder.find(R.id.tv_nopol, TextView.class).setText(formatNopol(nListArray.get(position).get("NOPOL").asString()));
                        viewHolder.find(R.id.tv_jenis_kendaraan, TextView.class).setText(nListArray.get(position).get("JENIS_KENDARAAN").asString());
                        viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                        viewHolder.find(R.id.tv_layanan, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                        viewHolder.find(R.id.tv_estimasi_selesai, TextView.class).setText("");
                        viewHolder.find(R.id.tv_estimasi, TextView.class).setText(estimasi);
                        viewHolder.find(R.id.tv_status, TextView.class).setText(nListArray.get(position).get("STATUS").asString());

                        viewHolder.find(R.id.img_more_booking, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.img_more_booking, ImageButton.class));
                                popup.inflate(R.menu.menu_history);
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        if (menuItem.getItemId() == R.id.action_history) {
                                            Intent i = new Intent(getActivity(), HistoryBookingCheckin_Activity.class);
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
                //ob = 0;
                //ix = 0;
                catchData("");
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void catchData(final String cari) {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_KONTROL_LAYANAN), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvKontrolLayanan.getAdapter().notifyDataSetChanged();
                    rvKontrolLayanan.scheduleLayoutAnimation();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            find(R.id.swiperefresh, SwipeRefreshLayout.class).setRefreshing(show);
            return;
        }
        find(R.id.swiperefresh, SwipeRefreshLayout.class).post(new Runnable() {
            @Override
            public void run() {
                find(R.id.swiperefresh, SwipeRefreshLayout.class).setRefreshing(show);
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

        //adapterSearchView(mSearchView, "nopol", "viewnopol", "NOPOL");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchMenu.collapseActionView();
                //filter(null);
                catchData(query);

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN)
            catchData("");
        else if(resultCode == RESULT_OK && requestCode == REQUEST_DETAIL)
            catchData("");
        else if(resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS)
            catchData("");
    }
}
