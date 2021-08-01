package com.rkrzmail.oto.modules.checkin;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.CHECKOUT;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class CheckOut_Activity extends AppActivity {

    private RecyclerView rvCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        if(!Tools.isNetworkAvailable(getActivity())){
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }
        initToolbar();
        initComponent();
        viewCheckout();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check Out");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        rvCheckout = findViewById(R.id.recyclerView);
        rvCheckout.setLayoutManager(new LinearLayoutManager(this));
        rvCheckout.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_checkout) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_jenis_checkout, TextView.class).setText(nListArray.get(position).get("JENIS_KENDARAAN").asString());
                viewHolder.find(R.id.tv_nopol_checkout, TextView.class).setText(nListArray.get(position).get("NOPOL").asString());
                viewHolder.find(R.id.tv_namaP_checkout, TextView.class).setText(nListArray.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_noKunci_checkout, TextView.class).setText(nListArray.get(position).get("NO_KUNCI").asString());
            }
        });

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewCheckout();
            }
        });

        find(R.id.img_btn_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nListArray.asArray().isEmpty()) {
                    showWarning("TIDAK ADA TRANSAKSI");
                } else {
                    startActivityForResult(new Intent(getActivity(), BarcodeActivity.class), REQUEST_BARCODE);
                }
            }
        });
    }

    private void viewBarcode(final String noBuktiBayar) {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "BARCODE");
                args.put("noBuktiBayar", noBuktiBayar);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(CHECKOUT), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfoDialog("NO. BUKTI BAYAR VALID", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    showSuccess("SCAN BARCODE BERHASIL ");
                } else {
                    showInfoDialog("NO. BUKTI BAYAR INVALID", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                }
            }
        });
    }


    private void viewCheckout() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(CHECKOUT), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCheckout.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String barcodeResult = data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "";
            viewBarcode(barcodeResult);
            viewCheckout();
        }
    }
}
