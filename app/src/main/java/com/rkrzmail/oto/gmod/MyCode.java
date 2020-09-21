package com.rkrzmail.oto.gmod;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.naa.data.ImageUtil;
import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.naa.data.ImageUtil.*;
import static com.naa.data.ImageUtil.rkrzmaiImageA;

public class MyCode extends AppActivity {
    View view;
    SwipeRefreshLayout swipeRefreshLayout;
    Bitmap bitBitmap = null;
    String string;
    int ix = 0;
    private long ob = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycode);

        ((TextView)findViewById(R.id.txtName)).setText(getSetting( "NAMA_USER"));
        ((TextView)findViewById(R.id.txtEmail)).setText(  getSetting("session" )  );
        findViewById(R.id.icon).setVisibility(View.GONE);
        rkrzmaiImageA(((ImageView) view.findViewById(R.id.icon)), getSetting("XLOGO"), new ImageLoadingListener() {
            public void onLoadingStarted(String var1, View var2) { }
            public void onLoadingFailed(String var1, View var2, String var3) {  }
            public void onLoadingComplete(String var1, View var2, Bitmap var3) {
                view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
            }
            public void onLoadingCancelled(String var1, View var2) {}
        });
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                ob = 0;ix=0;
                swipeRefreshLayout.setRefreshing(false);
             }
        });
        swipeRefreshLayout.setRefreshing(true);


        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                Nson sds = Nson.newObject();
                sds.set("X", Utility.Now());
                sds.set("S", UtilityAndroid.getSetting(getApplicationContext(), "session", ""));
                sds.set("U", UtilityAndroid.getSetting(getApplicationContext(), "NAMA_USER", ""));

                String barcode = sds.toJson();
                QRCodeWriter writer = new QRCodeWriter();
                try {
                    BitMatrix bitMatrix = writer.encode(barcode, BarcodeFormat.QR_CODE, 240, 240);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    bitBitmap = bmp;
                    string = "";
                } catch (WriterException e) {
                    string = String.valueOf(e.getMessage());
                }
            }
            @Override
            public void runUI() {
                if (bitBitmap!=null){
                    ((ImageView) view.findViewById(R.id.imgBarcode)).setImageBitmap(bitBitmap);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


}
