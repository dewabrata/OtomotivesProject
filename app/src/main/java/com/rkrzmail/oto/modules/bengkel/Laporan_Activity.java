package com.rkrzmail.oto.modules.bengkel;

import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ABSEN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;


public class Laporan_Activity extends AppActivity {

    private TextView tglMulai, tglSelesai;
    private Nson testList = Nson.newArray();

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
        tglMulai = findViewById(R.id.tv_tglMulai_lap);
        tglSelesai = findViewById(R.id.tv_tglSelesai_lap);
        Button btnUnduh = findViewById(R.id.btn_unduh);
        viewTest();

       find(R.id.ic_tglMulai_lap).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               getDatePickerDialogTextView(getActivity(), tglMulai);
           }
       });
       find(R.id.ic_tglSelesai_lap).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               getDatePickerDialogTextView(getActivity(), tglSelesai);
           }
       });

        btnUnduh.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //GenereteExcel();
           }
       });
    }

    private void viewTest(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("nama", "SEBAB KERUSAKAN");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    testList.asArray().clear();
                    testList.asArray().addAll(result.get("data").asArray());

                } else {
                    //showInfo(result.get("message").asString());
                }
            }
        });

    }

//    private void GenereteExcel (){
//        Workbook workbook = new HSSFWorkbook();
//        Sheet sheet = workbook.createSheet("Users"); //Creating a sheet
//
//        for(int i=0; i<testList.size(); i++){
//            Nson result = testList.get(i);
//            Row row = sheet.createRow((short)i);
//            for(int x=0; x<result.size(); x++){
//                Cell cell = row.createCell((short)x);
//                String data = result.get(x).get("data").toString();
//                cell.setCellValue("VALUE_1");
//
//            }
//
//
//        }
//        sheet.setColumnWidth(0,(10*200));
//        sheet.setColumnWidth(1,(10*200));
//
//        String fileName = "FileName.xlsx"; //Name of the file
//
//        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
//        File folder = new File(extStorageDirectory, "FolderName");// Name of the folder you want to keep your file in the local storage.
//        folder.mkdir(); //creating the folder
//        File file = new File(folder, fileName);
//        try {
//            file.createNewFile(); // creating the file inside the folder
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//
//        try {
//            FileOutputStream fileOut = new FileOutputStream(file); //Opening the file
//            workbook.write(fileOut); //Writing all your row column inside the file
//            fileOut.close(); //closing the file and done
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


}