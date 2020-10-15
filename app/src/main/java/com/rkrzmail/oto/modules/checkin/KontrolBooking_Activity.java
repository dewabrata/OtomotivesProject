package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.Map;

public class KontrolBooking_Activity extends AppActivity {

    public static final int REQUEST_BOOKING_LAYANAN = 69;
    private static final int REQUEST_HISTORY = 70;
    private static final int REQUEST_DETAIL = 71;
    private static final String TAG = "Kontrol_bookking__";

    private RecyclerView rvKontrolBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kontrol Booking");
        setTitleColor(getResources().getColor(R.color.white_transparency));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Booking1A_Activity.class);
                startActivityForResult(intent, REQUEST_BOOKING_LAYANAN);
            }
        });
        rvKontrolBooking = findViewById(R.id.recyclerView);
        rvKontrolBooking.setLayoutManager(new LinearLayoutManager(this));
        rvKontrolBooking.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_kontrol_booking) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);

               // String tglSet = Tools.setFormatDayAndMonthFromDb(nListArray.get(position).get("CREATED_DATE").asString());

                viewHolder.find(R.id.tv_status_kontrolBooking, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_waktu_kontrolBooking, TextView.class).setText(nListArray.get(position).get("WAKTU_PENGERJAAN").asString());
                viewHolder.find(R.id.tv_nopol_kontrolBooking, TextView.class).setText(nListArray.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.tv_noPhone_kontrolBooking, TextView.class).setText(nListArray.get(position).get("PHONE").asString());
                viewHolder.find(R.id.tv_kondisi_kontrolBooking, TextView.class).setText(nListArray.get(position).get("KONDISI_KENDARAAN").asString());
                viewHolder.find(R.id.tv_jenisBooking_kontrolBooking, TextView.class).setText(nListArray.get(position).get("STATUS").asString());
                viewHolder.find(R.id.tv_ketPart_kontrolBooking, TextView.class).setText(nListArray.get(position).get("PART").asString());

                viewHolder.find(R.id.img_more_booking, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.img_more_booking, ImageButton.class));
                        popup.inflate(R.menu.menu_history);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.action_history:
                                        Intent i = new Intent(getActivity(), HistoryBookingCheckin_Activity.class);
                                        i.putExtra("booking", nListArray.get(position).toJson());
                                        startActivityForResult(i, REQUEST_HISTORY);
                                        break;
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
                Intent i = new Intent(getActivity(), DetailKontrolBooking_Activity.class);
                i.putExtra("data", parent.get(position).toJson());
                startActivityForResult(i, REQUEST_DETAIL);
            }
        }));

        catchData("");
    }

    private void catchData(final String cari) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkontrolbooking"), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(result.get("data").asArray().contains("DATA")){
                        for (int i = 0; i < result.size(); i++) {
                            Log.d(TAG, "runUI: " + result.get("data"));
                        }
                    }
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvKontrolBooking.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
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
        if(resultCode == RESULT_OK && requestCode == REQUEST_BOOKING_LAYANAN){
            catchData("");
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_HISTORY){
            catchData("");
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_DETAIL){
            catchData("");
        }
    }
}
