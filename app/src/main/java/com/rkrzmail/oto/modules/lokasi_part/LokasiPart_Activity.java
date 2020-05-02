package com.rkrzmail.oto.modules.lokasi_part;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.AturPenugasan_Activity;
import com.rkrzmail.oto.modules.lokasi_part.stock_opname.StockOpname_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class LokasiPart_Activity extends AppActivity {

    private static final String TAG = "LokasiPart_Activity";
    private RecyclerView rvLokasi_part;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lokasi_part);
        initToolbar();
        initComponent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_tambah_part);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getActivity(), AturLokasiPart_Activity.class);
                startActivity(intent);
            }
        });
    }

    private void initToolbar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_lokasi_part);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Lokasi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

        rvLokasi_part = (RecyclerView) findViewById(R.id.recyclerView_lokasiPart);
        rvLokasi_part.setLayoutManager(new LinearLayoutManager(this));
        rvLokasi_part.setHasFixedSize(true);

        rvLokasi_part.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_lokasi_part){
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_noFolder, TextView.class).setText( nListArray.get(position).get("NAMA_MEKANIK").asString());
                viewHolder.find(R.id.tv_lokasiPart, TextView.class).setText(nListArray.get(position).get("TIPE_ANTRIAN").asString());
                viewHolder.find(R.id.tv_namaPart, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_tglOpname, TextView.class).setText(nListArray.get(position).get("JAM_MASUK").asString());
                viewHolder.find(R.id.tv_penempatan, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_stock, TextView.class).setText( nListArray.get(position).get("JAM_PULANG").asString());
                viewHolder.find(R.id.tv_user, TextView.class).setText(nListArray.get(position).get("JAM_PULANG").asString());

                viewHolder.find(R.id.tv_optionMenu, TextView.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.tv_optionMenu, TextView.class));
                        popup.inflate(R.menu.menu_lokasi_part);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {

                                switch (menuItem.getItemId()){
                                    case R.id.action_stockOpname:
//                                        Stock opname : membuka form stock opname
                                        startActivity(new Intent(LokasiPart_Activity.this, StockOpname_Activity.class));
                                        break;
                                    case R.id.action_qrCode:
//                                        Print QR code lokasi : mengirimkan image label QR code kode lokasi ke WA user
                                        break;
                                    case R.id.action_hapusDaftar:
//                                        Hapus daftar : menghapus part dari lokasi penempatan
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                });

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
//                Intent intent =  new Intent(getActivity(), AturPenugasan_Activity.class);
//                intent.putExtra("ID", nListArray.get(position).get("ID").asString());
//                intent.putExtra("TIPE_ANTRIAN", nListArray.get(position).get("TIPE_ANTRIAN").asString());
//                intent.putExtra("LOKASI", nListArray.get(position).get("LOKASI").asString());
//                intent.putExtra("ID", nListArray.get(position).toJson());
//                //intent.putExtra("id", nListArray.get(position).get("id").asString());
//                startActivityForResult(intent, REQUEST_PENUGASAN);
            }
        }));
        catchData();
    }

    private void catchData(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("daftarpenugasan"), args)) ;
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvLokasi_part.getAdapter().notifyDataSetChanged();
                    Log.d(TAG, "reload data");
                }else {
                    Log.d(TAG, "error");
                    showError("Mohon Di Coba Kembali" + result.get("message").asString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Search"); /// YOUR HINT MESSAGE
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
                //filter(newText);
                return true;
            }
            public boolean onQueryTextSubmit(String query) {
                //searchMenu.collapseActionView();
                //filter(null);
//                reload(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

}
