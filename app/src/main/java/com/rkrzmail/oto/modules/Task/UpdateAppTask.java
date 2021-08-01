package com.rkrzmail.oto.modules.Task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.BuildConfig;
import com.rkrzmail.oto.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.rkrzmail.utils.APIUrls.SET_LOGIN;
import static com.rkrzmail.utils.ConstUtils.EXTERNAL_DIR_OTO;

public class UpdateAppTask {

    private final Activity activity;
    public static final double SPACE_KB = 1024;
    public static final double SPACE_MB = 1024 * SPACE_KB;
    public static final double SPACE_GB = 1024 * SPACE_MB;
    public static final double SPACE_TB = 1024 * SPACE_GB;

    public UpdateAppTask(Activity activity) {
        this.activity = activity;
    }

    public void excuteVersionChecker(Activity activity) {
        VersionCheckerAsync versionCheckerAsync = new VersionCheckerAsync(activity);
        versionCheckerAsync.execute();
    }

    private static class VersionCheckerAsync extends AsyncTask<String, String, String> {
        WeakReference<Activity> weakReference;
        Activity[] activity = new Activity[1];
        private String newVersion, appSize;
        private String currentVersion = "";

        public VersionCheckerAsync(Activity activity) {
            this.activity[0] = activity;
            this.weakReference = new WeakReference<>(this.activity[0]);
            currentVersion = BuildConfig.VERSION_NAME;
        }

        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                if (Float.parseFloat(currentVersion) < Float.parseFloat(onlineVersion)) {
                    showDialogUpdate();
                }
            }

            Log.d("update", "Current version " + currentVersion + "playstore version " + onlineVersion);

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                Elements element = document.getElementsContainingOwnText("Current Version");
                for (Element ele : element) {
                    ele.siblingElements();
                    Elements sibElemets = ele.siblingElements();
                    for (Element sibElemet : sibElemets) {
                        newVersion = sibElemet.text();
                    }
                }
                element = document.getElementsContainingOwnText("Size");
                for (Element ele : element) {
                    ele.siblingElements();
                    Elements sibElemets = ele.siblingElements();
                    for (Element sibElemet : sibElemets) {
                        appSize = sibElemet.text();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;
        }

        AlertDialog updateDialog;
        private void showDialogUpdate() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity[0]);
            View dialogView = activity[0].getLayoutInflater().inflate(R.layout.dialog_update_app, null);
            builder.setView(dialogView);

            String appSizeInfo = "Size " + appSize;
            String updateInfo = "Versi terbaru Bengkel Pro telah tersedia " + newVersion;

            ((AppActivity) activity[0]).findView(dialogView, R.id.tv_app_size, TextView.class).setText(appSizeInfo);
            ((AppActivity) activity[0]).findView(dialogView, R.id.tv_update_info, TextView.class).setText(updateInfo);

            final GifImageView gifView = dialogView.findViewById(R.id.img_oto);
            try {
                GifDrawable gifDrawable = new GifDrawable(activity[0].getResources().openRawResource(R.raw.speed_progress_2));
                gifView.setImageDrawable(gifDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialogView.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String appPackageName = activity[0].getPackageName();
                    //must check storage permission

                    updateApp();
                    /*try {
                        activity[0].startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        activity[0].startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }*/
                }
            });

            updateDialog = builder.create();
            updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations_SmileWindow;
            updateDialog.setCancelable(false);
            updateDialog.setCanceledOnTouchOutside(false);
            updateDialog.show();
        }

        long downloadedsize, filesize;

        private void updateApp(){
            final ProgressDialog mProgressDialog = new ProgressDialog(activity[0]);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Downloading Update");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);

            MessageMsg.showProsesBar(activity[0], new Messagebox.DoubleRunnable() {
                @Override
                public void run() {
                    try {
                        @SuppressLint("SdCardPath") File file = new File("/sdcard/Otomotives/");
                        boolean isCreateDir = file.mkdirs();
                        if (!isCreateDir)
                            Log.d("isCreateDir", "doInBackground: " + "create directory fail");

                        File outputFile = new File(file, "otomotives.apk");
                        if (outputFile.exists()) {
                            boolean isDelete = outputFile.delete();
                            if (!isDelete)
                                Log.d("isDelete", "doInBackground: " + "delete fail.");
                        }

                        Map<String, String> args = AppApplication.getInstance().getArgsData();
                        args.put("action", "view");
                        Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("MobileVersion"), args));
                        result = result.get("data").get(0);

                        URL url = new URL(result.get("LINK_DOWNLOAD").asString());
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
                        OutputStream output = new FileOutputStream(outputFile);

                        byte[] data = new byte[1024];
                        filesize = connection.getContentLength();
                        int len1 = 0;
                        int progress = 0;

                        while ((len1 = input.read(data)) != -1) {
                            downloadedsize += len1;
                            int progress_temp = (int) (downloadedsize * 100 / filesize);
                            if (progress_temp % 10 == 0 && progress != progress_temp) {
                                progress = progress_temp;
                            }
                            activity[0].runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.setProgressNumberFormat((bytes2String(downloadedsize)) + "/" + (bytes2String(filesize)));
                                    mProgressDialog.show();
                                }
                            });

                            output.write(data, 0, len1);
                        }

                        output.flush();
                        output.close();
                        @SuppressLint("SetWorldReadable") boolean isReadable = outputFile.setReadable(true, false);
                        if(!isReadable){
                            Log.d("isReadable", "doInBackground: " + "cannot read.");
                        }
                        input.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void runUI() {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    if(updateDialog.isShowing())
                        updateDialog.dismiss();

                    Uri updateApkUri = FileProvider.getUriForFile(activity[0], BuildConfig.APPLICATION_ID + ".provider", new File(EXTERNAL_DIR_OTO + "/otomotives.apk"));
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    intent.setDataAndType(updateApkUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    activity[0].startActivity(intent);
                }
            });
        }

        private String bytes2String(long sizeInBytes) {
            NumberFormat nf = new DecimalFormat();
            nf.setMaximumFractionDigits(2);
            try {
                if (sizeInBytes < SPACE_KB) {
                    return nf.format(sizeInBytes) + " Byte(s)";
                } else if (sizeInBytes < SPACE_MB) {
                    return nf.format(sizeInBytes / SPACE_KB) + " KB";
                } else if (sizeInBytes < SPACE_GB) {
                    return nf.format(sizeInBytes / SPACE_MB) + " MB";
                } else if (sizeInBytes < SPACE_TB) {
                    return nf.format(sizeInBytes / SPACE_GB) + " GB";
                } else {
                    return nf.format(sizeInBytes / SPACE_TB) + " TB";
                }
            } catch (Exception e) {
                return sizeInBytes + " Byte(s)";
            }

        }
    }
}
