package com.rkrzmail.oto.gmod;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.EditText;
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
import com.naa.utils.RunnableX;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.naa.data.ImageUtil.*;
import static com.naa.data.ImageUtil.rkrzmaiImageA;
import static com.rkrzmail.utils.APIUrls.SET_LOGIN;

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

        view = findViewById(R.id.view_mycode);

        ((TextView)findViewById(R.id.txtName)).setText(getSetting( "NAMA_USER"));
        ((TextView)findViewById(R.id.txtEmail)).setText(getSetting("session" ));
        findViewById(R.id.icon).setVisibility(View.GONE);
        rkrzmaiImageA((find(R.id.icon, ImageView.class)), getSetting("XLOGO"), new ImageLoadingListener() {
            public void onLoadingStarted(String var1, View var2) { }
            public void onLoadingFailed(String var1, View var2, String var3) {  }
            public void onLoadingComplete(String var1, View var2, Bitmap var3) {
                find(R.id.icon, ImageView.class).setVisibility(View.VISIBLE);
            }
            public void onLoadingCancelled(String var1, View var2) {}
        });

        find(R.id.swiperefresh, SwipeRefreshLayout.class).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                ob = 0;ix=0;
                swipeRefreshLayout.setRefreshing(false);
             }
        });
        find(R.id.swiperefresh, SwipeRefreshLayout.class).setRefreshing(true);


        newProses(new Messagebox.DoubleRunnable() {
            Nson nson;
            public void run() {
                bitBitmap = null;
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("user", UtilityAndroid.getSetting(getApplicationContext(), "user", ""));
                args.put("action","get");
                nson = Nson.readJson (InternetX.postHttpConnection(AppApplication.getBaseUrlV3("mycode"), args));
                if (nson.get("status").asString().equalsIgnoreCase("OK")) {

                    String barcode = nson.get("data").get("barcode").asString();
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
                }else{
                    string = nson.toString();
                }
            }

            public void runUI() {
                if (bitBitmap!=null){
                    (find(R.id.imgBarcode, ImageView.class)).setImageBitmap(bitBitmap);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    public interface RunnableWD {
        public void runWD(Nson nson);
    }
    public static void checkMyCode(final AppActivity appActivity, final String barcode, final RunnableWD runnable){
        Nson nson = Nson.newObject();
        appActivity.newProses(new Messagebox.DoubleRunnable() {
            Nson nson;
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("user", UtilityAndroid.getSetting(appActivity, "user", ""));
                args.put("action","check");
                args.put("barcode",barcode);
                nson = Nson.readJson (InternetX.postHttpConnection(AppApplication.getBaseUrlV3("mycode"), args));

            }

            public void runUI() {
                runnable.runWD(nson);//{"status":"OK","message":"Success","data":[{"USERID":"1"}]}
            }
        });
     }

}
