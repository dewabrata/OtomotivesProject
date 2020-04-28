package com.rkrzmail.oto.gmod;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.penugasan.PenugasanActivity;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.Calendar;
import java.util.Map;

public class AturPenugasan_Activity extends AppActivity  {


    Nson nson = Nson.readNson("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_penugasan_);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_penugasan);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Atur Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        find(R.id.sp_lokasi, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("Tenda")) {
                    setViewAndChildrenEnabled(find(R.id.layout_external), false);
                } else if (item.equalsIgnoreCase("Bengkel")) {
                    setViewAndChildrenEnabled(find(R.id.layout_external), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.et_mulaiKerja, EditText.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateTime(find(R.id.et_mulaiKerja, EditText.class));
            }
        });

        find(R.id.et_selesaiKerja, EditText.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateTime(find(R.id.et_selesaiKerja, EditText.class));
            }
        });

        find(R.id.et_mulaistirahat, EditText.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateTime(find(R.id.et_mulaistirahat, EditText.class));
            }
        });

        find(R.id.et_selesaistirahat, EditText.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateTime(find(R.id.et_selesaistirahat, EditText.class));
            }
        });

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (find(R.id.tv_namaMekanik, NikitaAutoComplete.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Harus Di isi");
                } else if (find(R.id.et_mulaiKerja, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("Harus di isi");
                } else if ((find(R.id.et_selesaiKerja, EditText.class).getText().toString().equalsIgnoreCase(""))) {
                    showError("Harus di isi");
                } else if ((find(R.id.et_mulaistirahat, EditText.class).getText().toString().equalsIgnoreCase(""))) {
                    showError("Harus di isi");
                } else if ((find(R.id.et_selesaistirahat, EditText.class).getText().toString().equalsIgnoreCase(""))) {
                    showError("Harus di isi");
                }

                Messagebox.showDialog(getActivity(), "Konfirmasi", "Apakah yakin akan simpan ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        saveData();
                        Intent i = new Intent(AturPenugasan_Activity.this, PenugasanActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, null);

            }

        });

    }


    private void saveData() {

        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                args.put("tinggalkan_stnk", find(R.id.cb_home, CheckBox.class).isChecked()?"YES":"NO");
//                args.put("status", find(R.id.rgPenugasan, RadioGroup.class).isSelected()?"ON" : "OFF");
//                args.put("nama_mekanik", find(R.id.tv_namaMekanik, NikitaAutoComplete.class).getText().toString());
//                args.put("tipe_antrian", find(R.id.sp_antrian, Spinner.class).getSelectedItem().toString());
//                args.put("lokasi", find(R.id.sp_lokasi, Spinner.class).getSelectedItem().toString());
//                args.put("mulai_kerja", find(R.id.et_mulaiKerja, EditText.class).getText().toString());
//                args.put("selesai_kerja", find(R.id.et_selesaiKerja, EditText.class).getText().toString());
//                args.put("mulai_istirahat", find(R.id.et_mulaistirahat, EditText.class).getText().toString());
//                args.put("selesai_istirahat", find(R.id.et_selesaistirahat, EditText.class).getText().toString());

                String out = InternetX.postHttpConnection(AppApplication.getBaseUrl("save.php"), args);

                result = Nson.readJson(out);

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("success")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    private void getDateTime(final EditText editText) {

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AturPenugasan_Activity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                editText.setText(hourOfDay + ":" + minutes);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.show();
    }
}
