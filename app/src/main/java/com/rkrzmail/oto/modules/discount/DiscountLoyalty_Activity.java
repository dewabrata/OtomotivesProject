package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_DISKON_PART;
import static com.rkrzmail.utils.APIUrls.GET_DISCOUNT_LOYALTY;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DISCOUNT;

public class DiscountLoyalty_Activity extends AppActivity {

    private RecyclerView rvDisc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_fab);
        initToolbar();
        initComponent();
    }


    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Voucher Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.fab_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AturDiscountLoyalty_Activity.class), REQUEST_DISCOUNT);
            }
        });

        rvDisc = findViewById(R.id.recyclerView);
        rvDisc.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDisc.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_discount_loyalty) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(nListArray.get(position).get("NO_PONSEL").asString());
                        viewHolder.find(R.id.tv_expired_date, TextView.class).setText(nListArray.get(position).get("EXPIRED").asString());
                        viewHolder.find(R.id.tv_nopol, TextView.class).setText(nListArray.get(position).get("NO_POLISI").asString());
                        viewHolder.find(R.id.tv_paket_layanan, TextView.class).setText(nListArray.get(position).get("NAMA_LAYANAN").asString());
                        viewHolder.find(R.id.tv_disc_layanan, TextView.class).setText(nListArray.get(position).get("DISCOUNT_LAYANAN").asString() + " %");
                        viewHolder.find(R.id.tv_disc_part, TextView.class).setText(nListArray.get(position).get("DISCOUNT_PART").asString()  + " %");

                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        showDialogBarcode(nListArray.get(position));
                    }
                })
        );

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewData();
            }
        });

        viewData();
    }

    private void viewData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                String[] args = new String[1];
                args[0] = "CID=" + getSetting("CID");
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_DISCOUNT_LOYALTY), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asBoolean()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvDisc.getAdapter()).notifyDataSetChanged();
                } else {
                    showError("Gagal Meload Aktifitas");
                }
            }
        });
    }

    View dialogView = null;

    @SuppressLint("InflateParams")
    private void showDialogBarcode(final Nson data) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        dialogView = getLayoutInflater().inflate(R.layout.dialog_barcode_disc_loyalty, null);
        builder.setView(dialogView);

        newProses(new Messagebox.DoubleRunnable() {
            Bitmap bitmapBarcode;

            @Override
            public void run() {
                bitmapBarcode = generateBarcode(data.get("QR_CODE").asString());
            }

            @Override
            public void runUI() {
                findView(dialogView, R.id.tv_nopol, TextView.class).setText(formatNopol(data.get("NO_POLISI").asString()));
                findView(dialogView, R.id.tv_expired_date, TextView.class).setText(formatNopol(data.get("EXPIRED").asString()));
                findView(dialogView, R.id.tv_no_ponsel, TextView.class).setText(formatNopol(data.get("NO_PONSEL").asString()));
                findView(dialogView, R.id.tv_disc, TextView.class).setText(formatNopol(data.get("DISCOUNT_PERCENT").asString()));
                findView(dialogView, R.id.img_barcode, ImageView.class).setImageBitmap(bitmapBarcode);
                findView(dialogView, R.id.toolbar, Toolbar.class).setTitle("Voucher Discount");

                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                alertDialog.show();

                Window window = alertDialog.getWindow();
                if (window == null) return;
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = 1000;
                params.height = 1200;
                window.setAttributes(params);
            }
        });
    }

    private Bitmap generateBarcode(String value) {
        if (value == null) return null;
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, 250, 250);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DISCOUNT) {
            viewData();
        }
    }
}
