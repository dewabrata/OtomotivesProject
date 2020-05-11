package com.rkrzmail.oto.modules.lokasi_part;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.lokasi_part.stock_opname.StockOpname_Activity;
import com.rkrzmail.oto.modules.part.AdapterSuggestionSearch;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LokasiPart_Activity extends AppActivity {

    private static final String TAG = "LokasiPart_Activity";
    public static final int REQUEST_STOCK_OPNAME = 1212;
    private static final int REQUEST_PENYESUAIAN = 567;

    private View parent_view;
    private RecyclerView rvLokasi_part;
    private AdapterSuggestionSearch adapterSuggestionSearch;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lokasi_part);
        initToolbar();
        parent_view = findViewById(android.R.id.content);
        initComponent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_part);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AturLokasiPart_Activity.class);
                startActivity(intent);
            }
        });

    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_notifikasi, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_lokasi_part);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Lokasi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {

        rvLokasi_part = (RecyclerView) findViewById(R.id.recyclerView_lokasiPart);
        rvLokasi_part.setLayoutManager(new LinearLayoutManager(this));
        rvLokasi_part.setHasFixedSize(true);

        rvLokasi_part.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_lokasi_part) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, final int position) {

                viewHolder.find(R.id.tv_noFolder, TextView.class).setText("NO. FOLDER : " + nListArray.get(position).get("NO_FOLDER").asString());
                viewHolder.find(R.id.tv_lokasiPart, TextView.class).setText("LOKASI : " + (nListArray.get(position).get("LOKASI").asString()));
                viewHolder.find(R.id.tv_namaPart, TextView.class).setText("NAMA PART : " + nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_nomor_part, TextView.class).setText("NO. PART : " + nListArray.get(position).get("NO_PART_ID").asString());
                viewHolder.find(R.id.tv_tglOpname, TextView.class).setText("TGL. OPNAME : " + nListArray.get(position).get("TANGGAL_OPNAME").asString());
                viewHolder.find(R.id.tv_penempatan, TextView.class).setText("PENEMPATAN : " + nListArray.get(position).get("PENEMPATAN").asString());
                viewHolder.find(R.id.tv_stock, TextView.class).setText("STOCK : " + nListArray.get(position).get("STOCK").asString());
                viewHolder.find(R.id.tv_user, TextView.class).setText("USER : " + nListArray.get(position).get("USER").asString());

                viewHolder.find(R.id.tv_optionMenu, TextView.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.tv_optionMenu, TextView.class));
                        popup.inflate(R.menu.menu_lokasi_part);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {

                                switch (menuItem.getItemId()) {
                                    case R.id.action_stockOpname:
//                                        Stock opname : membuka form stock opname
                                        Intent i = new Intent(getActivity(), StockOpname_Activity.class);
                                        i.putExtra("NO_FOLDER", nListArray.get(position).get("NO_FOLDER"));
                                        i.putExtra("NO_PART_ID", nListArray.get(position).get("NO_PART_ID"));
                                        i.putExtra("NAMA", nListArray.get(position).get("NAMA"));
                                        i.putExtra("STOCK", nListArray.get(position).get("STOCK"));
                                        i.putExtra("LOKASI", nListArray.get(position).get("LOKASI"));
                                        i.putExtra("USER", nListArray.get(position).get("USER"));
                                        i.putExtra("PENEMPATAN", nListArray.get(position).get("PENEMPATAN"));
                                        i.putExtra("PALET", nListArray.get(position).get("PALET"));
                                        i.putExtra("RAK", nListArray.get(position).get("LOKASI"));
                                        i.putExtra("NO_PART_ID", nListArray.get(position).toJson());
                                        startActivityForResult(i, REQUEST_STOCK_OPNAME);
                                        break;
                                    case R.id.action_qrCode:
//                                        Print QR code lokasi : mengirimkan image label QR code kode lokasi ke WA user
                                        break;
                                    case R.id.action_hapusDaftar:
//                                        Hapus daftar : menghapus part dari lokasi penempatan
                                        break;
                                }
                                return true;
                            }
                        });
                        popup.show();
                    }
                });
                Log.d("NAMA", nListArray.get(position).get("search").asString());
            }
        });
        catchData("");
    }

    private void catchData(final String nama) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvLokasi_part.getAdapter().notifyDataSetChanged();
                    Log.d(TAG, result.get("status").asString());
                    Log.d("NAMA", result.get("search").get("data").asString());

                } else {
                    Log.d(TAG, result.get("status").asString());
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }

                if(nListArray.get("data").get("PENEMPATAN").asString().equalsIgnoreCase("")){
                    find(R.id.frame_notifikasi, FrameLayout.class).setVisibility(View.VISIBLE);
                    if (find(R.id.frame_notifikasi, FrameLayout.class).getVisibility() == View.VISIBLE) {
                        loadFragment(new Notifikasi_Alokasi_Fragment());
                    }
                } else {
                    find(R.id.frame_notifikasi, FrameLayout.class).setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Cari Nama Part"); /// YOUR HINT MESSAGE
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            public boolean onQueryTextChange(String newText) {
                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                catchData(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_STOCK_OPNAME && requestCode == RESULT_OK) {
            setResult(RESULT_OK);
            catchData("");
            finish();
        }else if(requestCode == REQUEST_PENYESUAIAN && requestCode == RESULT_OK){
            catchData("");
        }
    }
}
