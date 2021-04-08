package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_DISKON_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_BENGKEL;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;

public class AturDiscountPart_Activity extends AppActivity {

    private EditText etDiscPart, etNoPart, etNamaPart, etMerk;
    private Spinner spPekerjaan;
    private RadioButton rbAll;

    private String pekerjaan = "";
    private final Nson pekerjaanList = Nson.newArray();
    private int partID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_part);
        initToolbar();
        initComponent();
        loadData();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDiscPart = findViewById(R.id.et_discPart_discPart);
        etNoPart = findViewById(R.id.et_noPart_discPart);
        etNamaPart = findViewById(R.id.et_namaPart_discPart);
        etMerk = findViewById(R.id.et_merk_part);
        rbAll = findViewById(R.id.rb_all);
        spPekerjaan = findViewById(R.id.sp_pekerjaan);

        etDiscPart.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDiscPart));
        find(R.id.btn_search, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_BENGKEL, "");
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });

        rbAll.setOnClickListener(new View.OnClickListener() {
            int count = 0;
            @Override
            public void onClick(View v) {
                count++;
                if(count == 2){
                    if(rbAll.isChecked()){
                        rbAll.setChecked(false);
                        count = 0;
                    }
                }

                Tools.setViewAndChildrenEnabled(find(R.id.vg_pekerjaan, LinearLayout.class), !rbAll.isChecked());
                setSelectionSpinner(rbAll.isChecked() ? "ALL" : "--PILIH--", spPekerjaan);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        boolean isUpdate = false;
        List<String> statusList = Arrays.asList(getResources().getStringArray(R.array.aktif_tidak_aktif));
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (!data.asString().isEmpty()) {
            isUpdate = true;
            rbAll.setChecked(data.get("PEKERJAAN").asString().equals("ALL"));
            Tools.setViewAndChildrenEnabled(find(R.id.vg_pekerjaan, LinearLayout.class), !rbAll.isChecked());

            etDiscPart.setText(data.get("DISCOUNT_PART").asString());
            etNamaPart.setText(data.get("NAMA_PART").asString());
            etNoPart.setText(data.get("NO_PART").asString());
            etMerk.setText(data.get("MERK").asString());
        }

        setSpPekerjaan(data.get("PEKERJAAN").asString());
        setSpinnerOffline(statusList, find(R.id.sp_status, Spinner.class), data.get("STATUS").asString());
        final boolean finalIsUpdate = isUpdate;

        find(R.id.btn_simpan_discPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalIsUpdate) {
                    if (etDiscPart.getText().toString().isEmpty() || etDiscPart.getText().toString().equals("0,0%")) {
                        etDiscPart.setError("DISCOUNT PART HARUS DI ISI");
                        viewFocus(etDiscPart);
                    }else{
                        saveData(true, data.get("ID").asInteger());
                    }
                } else {
                    if (etNoPart.getText().toString().isEmpty() ||
                            etNamaPart.getText().toString().isEmpty() ||
                            etMerk.getText().toString().isEmpty()) {
                       showWarning("KLIK PENCARIAN PART UNTUK MENAMBAHKAN DISCOUNT");
                    } else if (etDiscPart.getText().toString().isEmpty() || etDiscPart.getText().toString().equals("0,0%")) {
                       etDiscPart.setError("DISCOUNT PART HARUS DI ISI");
                       viewFocus(etDiscPart);
                    }else{
                        saveData(false, 0);
                    }
                }
            }
        });

    }

    private void setSpPekerjaan(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "PEKERJAAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equals("OK")) {
                    result = result.get("data");
                    pekerjaanList.add("--PILIH--");
                    pekerjaanList.add("ALL");
                    for (int i = 0; i < result.size(); i++) {
                        pekerjaanList.add(result.get(i).get("PEKERJAAN").asString());
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, pekerjaanList.asArray()){
                        @Override
                        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view;
                            if (pekerjaanList.get(position).asString().equals("ALL") || pekerjaanList.get(position).asString().equals("--PILIH--")) {
                                TextView mTextView = new TextView(getContext());
                                mTextView.setVisibility(View.GONE);
                                mTextView.setHeight(0);
                                view = mTextView;
                                return view;
                            } else {
                                return view = super.getDropDownView(position, null, parent);
                            }
                        }
                    };
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPekerjaan.setAdapter(spinnerAdapter);
                    setSelectionSpinner(selection, spPekerjaan);
                    spPekerjaan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            pekerjaan = parent.getItemAtPosition(position).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    /*  final List<String> itemsPekerjaan = new ArrayList<>();
                    itemsPekerjaan.addAll(pekerjaanList.asArray());
                    isSelectedPekerjaanArr = new boolean[itemsPekerjaan.size()];

                    btnPekerjaan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //showDialogPekerjaan();
                            showAlertDialogPekerjaan(itemsPekerjaan.toArray(new String[]{}));
                        }
                    });*/
                }
            }
        });
    }



    private void saveData(final boolean isUpdate, final int discPartID) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                if (isUpdate) {
                    args.put("action", "update");
                    args.put("discPartID", String.valueOf(discPartID));
                } else {
                    args.put("action", "add");
                    args.put("namapart", etNamaPart.getText().toString());
                    args.put("nopart", etNoPart.getText().toString());
                    args.put("partid", String.valueOf(partID));
                }

                args.put("pekerjaan", pekerjaan);
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("diskonpart", etDiscPart.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISKON_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menambahkan Aktivitas Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void deleteData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", id.get("ID").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISKON_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menghapus Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson getData = Nson.readJson(getIntentStringExtra(data, "part"));
            etNoPart.setText(getData.get("NO_PART").asString());
            etNamaPart.setText(getData.get("NAMA_PART").asString());
            etMerk.setText(getData.get("MERK").asString());
            partID = getData.get("PART_ID").asInteger();
        }
    }
}
