package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.discount.AturDiscountJasaLain_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_KONFIRMASI;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_JUAL_PART;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_LAYANAN;

public class Pembayaran_Activity extends AppActivity {

    private RecyclerView rvPembayaranCheckin;
    private Nson jualPartList = Nson.newArray();
    private String idCheckin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        initToolbar();
        initRecylerviewPembayaran();
        viewPembayaran();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecylerviewPembayaran() {
        rvPembayaranCheckin = findViewById(R.id.recyclerView);
        rvPembayaranCheckin.setHasFixedSize(true);
        rvPembayaranCheckin.setLayoutManager(new LinearLayoutManager(this));
        rvPembayaranCheckin.setAdapter(new NikitaMultipleViewAdapter(nListArray, R.layout.item_pembayaran, R.layout.item_permintaan_jual_part_tugas_part) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                int viewType = getItemViewType(position);

                if (viewType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_jenis_kendaraan, TextView.class).setText(nListArray.get(position).get("JENIS_KENDARAAN").asString());
                    viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                    viewHolder.find(R.id.tv_nopol, TextView.class).setText(nListArray.get(position).get("NOPOL").asString());
                    viewHolder.find(R.id.tv_layanan, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());
                    viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(nListArray.get(position).get("NO_PONSEL").asString());
                    viewHolder.find(R.id.cv_pembayaran_checkin, CardView.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            idCheckin = nListArray.get(position).get(ID).asString();
                            checkDataConfirmation(position);
                        }
                    });
                } else if (viewType == ITEM_VIEW_2) {
                    String noHp = nListArray.get(position).get("NO_PONSEL").asString();
                    if (noHp.length() > 4) {
                        noHp = noHp.substring(noHp.length() - 4);
                    }
                    viewHolder.find(R.id.tv_nama_mekanik, TextView.class).setText(nListArray.get(position).get("USER_JUAL").asString());
                    viewHolder.find(R.id.tv_nama_pelanggan_nama_usaha, TextView.class).setText(
                            nListArray.get(position).get("NAMA_PELANGGAN").asString() + " " + nListArray.get(position).get("NAMA_USAHA").asString());
                    viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText("XXXXXXXX" + noHp);
                    viewHolder.find(R.id.tv_tgl, TextView.class).setText(nListArray.get(position).get("TANGGAL").asString());
                    viewHolder.find(R.id.cv_pembayaran_jual_part, CardView.class).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getActivity(), Rincian_Pembayaran_Activity.class);
                            i.putExtra(RINCIAN_JUAL_PART, "");
                            i.putExtra(DATA, nListArray.get(position).toJson());
                            startActivityForResult(i, REQUEST_DETAIL);
                        }
                    });
                }
            }
        });
    }

    private void checkDataConfirmation(final int position) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "HISTORY CHECKIN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                Intent i;
                if (result.get("data").asArray().size() > 1) {
                    i = new Intent(getActivity(), Rincian_Pembayaran_Activity.class);
                    i.putExtra(RINCIAN_LAYANAN, "");
                    i.putExtra(DATA, nListArray.get(position).toJson());
                    startActivityForResult(i, REQUEST_DETAIL);
                } else {
                    i = new Intent(getActivity(), KonfirmasiData_Pembayaran_Activity.class);
                    i.putExtra(DATA, nListArray.get(position).toJson());
                    startActivityForResult(i, REQUEST_KONFIRMASI);
                    Log.d("u__", "runUI: " + nListArray.get(position).toJson());
                }
            }
        });
    }

    private void viewPembayaran() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("jenisPembayaran", "CHECKIN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                nListArray.asArray().clear();
                nListArray.asArray().addAll(result.get("data").asArray());

                args.remove("jenisPembayaran");
                args.put("jenisPembayaran", "JUAL PART");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                nListArray.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    rvPembayaranCheckin.getAdapter().notifyDataSetChanged();
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }


    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);
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

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {
            viewPembayaran();
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_KONFIRMASI){
            Intent i = new Intent(getActivity(), Rincian_Pembayaran_Activity.class);
            i.putExtra(RINCIAN_LAYANAN, "");
            i.putExtra(DATA, getIntentStringExtra(data, DATA));
            startActivityForResult(i, REQUEST_DETAIL);
            Log.d("u__", "accept: " + getIntentStringExtra(data, DATA));

        }
    }
}

