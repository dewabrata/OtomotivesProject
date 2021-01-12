package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Pembayaran_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NumberFormatUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.EXTERNAL_DIR_OTO;
import static com.rkrzmail.utils.ConstUtils.PRINT_BUKTI_BAYAR;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;


public class Print_Pembayaran_Fragment extends Fragment {

    private RecyclerView rvPrint;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;

    private Nson pembayaranList = Nson.newArray();
    private String idCheckin = "";
    private String noBuktiBayar = "";
    private String noPonsel = "";

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private CountDownTimer cdt;
    int id = 1;
    private Uri pdfUri;

    private Context context;
    private AppActivity activity;
    private File file;
    Future<File> downloading;

    public Print_Pembayaran_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_list_basic, container, false);
        activity = getActivity() == null ? null : ((Pembayaran_MainTab_Activity) getActivity());
        initProgressDialog();
        initHideToolbar(view);
        initRecylerviewPembayaran(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            viewPrintPembayaran();
        }
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Mendownload File Bukti Bayar ...");
    }

    private void initRecylerviewPembayaran(View view) {
        rvPrint = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        rvPrint.setHasFixedSize(true);
        rvPrint.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPrint.setAdapter(new NikitaRecyclerAdapter(pembayaranList, R.layout.item_print_pembayaran) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                String noHp = pembayaranList.get(position).get("NO_PONSEL").asString();
                if (noHp.length() > 4) {
                    noHp = "XXXXXXXX" + noHp.substring(noHp.length() - 4);
                }
                viewHolder.find(R.id.tv_nama_pelanggan, TextView.class).setText(pembayaranList.get(position).get("NAMA_PELANGGAN").asString());
                viewHolder.find(R.id.tv_no_ponsel, TextView.class).setText(noHp);
                viewHolder.find(R.id.tv_no_bukti_bayar, TextView.class).setText(pembayaranList.get(position).get("NO_BUKTI_BAYAR").asString());
                viewHolder.find(R.id.tv_total_bayar, TextView.class).setText(RP + new NumberFormatUtils().formatRp(pembayaranList.get(position).get("TOTAL_BAYAR").asString()));
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Nson parent, View view, final int position) {
                noBuktiBayar = parent.get(position).get("NO_BUKTI_BAYAR").asString();
                noPonsel = parent.get(position).get("NO_PONSEL").asString();
                new DownloadBuktiBayar().execute(PRINT_BUKTI_BAYAR(parent.get(position).get("NO_BUKTI_BAYAR").asString()));
            }
        }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewPrintPembayaran();
            }
        });
    }

    private void showDialogConfirmation() {
        Messagebox.showDialog(getActivity(), "KONFIRMASI", "PILIH BUKTI BAYAR", "PRINT", "MESSAGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setIntentOpenPDF();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(noPonsel.length() > 13){
                    activity.showWarning("MESSAGE TIDAK AKTIVE, PRINT BUKTI BAYAR MANDIRI");
                    showDialogConfirmation();
                    return;
                }
                setIntentWA(file);
            }
        });
    }

    private void setIntentOpenPDF() {
        pdfUri = Uri.parse(file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void setIntentWA(File file) {
        Intent sendIntent = new Intent("android.intent.action.SEND");
        pdfUri = Uri.parse(file.getAbsolutePath());
        sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.ContactPicker"));
        sendIntent.setType("application/pdf");
        sendIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        sendIntent.putExtra("jid", noPonsel + "@s.whatsapp.net");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "No Bukti Bayar");
        startActivity(sendIntent);
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    private void viewPrintPembayaran() {
        activity.newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "PRINT");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
                pembayaranList.asArray().clear();
                pembayaranList.asArray().addAll(result.get("data").asArray());
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    rvPrint.getAdapter().notifyDataSetChanged();
                    rvPrint.scheduleLayoutAnimation();
                } else {
                    activity.showError(ERROR_INFO);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL)
            viewPrintPembayaran();
    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadBuktiBayar extends AsyncTask<String, Integer, String> {

        int length = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            /*if(!file.exists()){
                activity.runOnActionThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.showError("Gagal Mendownload File");
                    }
                });
                return;
            }*/
            showDialogConfirmation();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d("prog__", "onProgressUpdate: " + values[0]);
            //progressDialog.setMessage("Mendownload File " + values[0] + " %");
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... urls) {
            int count;
            try {
                String fileName = "/No Bukti Bayar - " + noBuktiBayar + ".pdf";
                file = new File(EXTERNAL_DIR_OTO + fileName);
                if (!file.exists()) {
                    URL url = new URL(urls[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    int fileLengths = connection.getContentLength();

                    OutputStream output = new FileOutputStream(file);
                    byte[] data = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        length = (int) ((total * 100) / fileLengths);
                        Log.d("prog__", "file: " + length);
                        publishProgress(length);
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                activity.showError(e.getMessage());
            }
            return null;
        }
    }
}