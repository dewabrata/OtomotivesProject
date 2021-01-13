package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.CETAK_EXCEL;
import static com.rkrzmail.utils.ConstUtils.EXTERNAL_DIR_OTO;
import static com.rkrzmail.utils.ConstUtils.ONEDAY;


public class Laporan_Activity extends AppActivity {

    private TextView tglMulai, tglSelesai, txtProgress;
    private String jenisLaporan;
    private Spinner spJenisLap;
    private Nson testList = Nson.newArray();
    private ProgressBar progressBar;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LAPORAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        initProgressbar();
        tglMulai = findViewById(R.id.tv_tglMulai_lap);
        tglSelesai = findViewById(R.id.tv_tglSelesai_lap);
        spJenisLap = findViewById(R.id.sp_nama_laporan);
        Button btnUnduh = findViewById(R.id.btn_unduh);


       find(R.id.ic_tglMulai_lap).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               getDatePickerDari();

           }
       });
       find(R.id.ic_tglSelesai_lap).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               getDatePickerSampai();
           }
       });

        btnUnduh.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               new DownloadExcel().execute(CETAK_EXCEL("628123456789", jenisLaporan));
               progressBar.setVisibility(View.VISIBLE);
           }
       });

        spJenisLap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if(item.equalsIgnoreCase("UNIT ENTRY")){
                    jenisLaporan = "entry";
                }else if (item.equalsIgnoreCase("UNIT DETAIL")){
                    jenisLaporan = "detail";
                }else {
                    jenisLaporan = "lkk";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initProgressbar(){
        progressBar = (ProgressBar) findViewById(R.id.progressBarHorizontal);
        txtProgress = findViewById(R.id.txtProgress);
    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadExcel extends AsyncTask<String, Integer, String> {
        int length = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            txtProgress.setText(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress((values[0]));
        }

        @Override
        protected String doInBackground(String... urls) {
            int count = 0;
            try {
                String fileName = "/report - " +jenisLaporan+ ".xls";

                file = new File(EXTERNAL_DIR_OTO + fileName);
                if (!file.exists()) {

                    URL url = new URL(urls[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    int fileLength = connection.getContentLength();

                    OutputStream output = new FileOutputStream(file);
                    byte[] data = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        length = (int) ((total * 100) / fileLength);
                        publishProgress(length);
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
            return null;
        }
    }



    public void getDatePickerDari() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_WEEK);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                //tanggalString += formattedTime;
                find(R.id.tv_tglMulai_lap, TextView.class).setText(formattedTime);

            }
        }, year, month, day);

        datePickerDialog.setMinDate(parseWaktu1blnlalu());
        datePickerDialog.setMaxDate(parseWaktuNow());
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    public void getDatePickerSampai() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_WEEK);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                //tanggalString += formattedTime;
                find(R.id.tv_tglSelesai_lap, TextView.class).setText(formattedTime);

            }
        }, year, month, day);

        datePickerDialog.setMinDate(parseWaktu1blnlalu());
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private Calendar parseWaktuNow() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long totalDate = current;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

    private Calendar parseWaktu1blnlalu() {
        long current = 0;
        try {
            @SuppressLint("SimpleDateFormat") Date now = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
            current = now.getTime();
        } catch (ParseException e) {
            //Log.d(TAG, "Exception waktu pesan : " + e.getMessage());
        }
        long hari = 30 * ONEDAY;
        long totalDate = current - hari;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(totalDate);

        return calendar;
    }

}