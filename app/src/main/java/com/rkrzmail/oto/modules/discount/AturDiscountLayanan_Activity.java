package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.rkrzmail.utils.APIUrls.DISCOUNT_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;

public class AturDiscountLayanan_Activity extends AppActivity {

    private AlertDialog alertDialog;
    private Button btnPekerjaan, btnLayanan;
    private SpinnerDialog spDialogLayanan;

    private final Nson pekerjaanSelectedList = Nson.newArray();
    private final Nson pekerjaanList = Nson.newArray();
    private final Nson dataLayananList = Nson.newArray();
    private final ArrayList<String> layananList = new ArrayList<>();
    boolean[] isSelectedPekerjaanArr = null;

    private int idLayanan = 0;
    boolean selectAllPekerjaan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_layanan);
        initToolbar();
        initComponent();
        loadData();
        setSpPekerjaan();
        setSpLayanan();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        btnPekerjaan = findViewById(R.id.btn_pekerjaan);
        btnLayanan = findViewById(R.id.btn_layanan);
        find(R.id.et_disc_layanan, EditText.class).addTextChangedListener(new NumberFormatUtils().percentTextWatcher(find(R.id.et_disc_layanan, EditText.class)));
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        boolean isUpdate = false;
        if (!data.asString().isEmpty()) {
            isUpdate = true;
            btnLayanan.setEnabled(false);
            btnPekerjaan.setEnabled(false);
            btnLayanan.setText(data.get("NAMA_LAYANAN").asString());
            btnPekerjaan.setText(data.get("PEKERJAAN").asString());
            find(R.id.et_disc_layanan, EditText.class).setText(data.get("DISCOUNT_LAYANAN").asString());

            String[] splitPekerjaan = data.get("PEKERJAAN").asString().trim().split(", ");
            for (String s : splitPekerjaan) {
                pekerjaanSelectedList.add(s.trim());
            }
        }

        setSpStatus(data.get("STATUS").asString());
        final boolean finalIsUpdate = isUpdate;
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalIsUpdate){
                    if(find(R.id.et_disc_layanan, EditText.class).getText().toString().equals("0,0%")){
                        find(R.id.et_disc_layanan, EditText.class).setError("DISCOUNT HARUS DI ISI");
                        viewFocus(find(R.id.et_disc_layanan, EditText.class));
                    }else{
                        saveData(finalIsUpdate, data.get("ID").asInteger());
                    }
                }else{
                    if(btnPekerjaan.getText().toString().equals("--PILIH--") || btnPekerjaan.getText().toString().isEmpty()){
                        btnPekerjaan.setError("PEKERJAAN HARUS DI PILIH");
                    }else if(btnLayanan.getText().toString().equals("--PILIH--")){
                        btnLayanan.setError("LAYANAN HARUS DI PILIH");
                    }else if(find(R.id.et_disc_layanan, EditText.class).getText().toString().equals("0,0%")){
                        find(R.id.et_disc_layanan, EditText.class).setError("DISCOUNT HARUS DI ISI");
                        viewFocus(find(R.id.et_disc_layanan, EditText.class));
                    }else{
                        saveData(finalIsUpdate, 0);
                    }
                }

            }
        });

    }

    private void setSpPekerjaan() {
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
                    pekerjaanSelectedList.add("");
                    pekerjaanList.add("ALL");
                    for (int i = 0; i < result.size(); i++) {
                        pekerjaanList.add(result.get(i).get("PEKERJAAN").asString());
                    }

                    final List<String> itemsPekerjaan = new ArrayList<>();
                    itemsPekerjaan.addAll(pekerjaanList.asArray());
                    isSelectedPekerjaanArr = new boolean[itemsPekerjaan.size()];

                    btnPekerjaan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //showDialogPekerjaan();
                            showAlertDialogPekerjaan(itemsPekerjaan.toArray(new String[]{}));
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialogPekerjaan(final String[] itemArray) {
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(pekerjaanSelectedList.asArray());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("PILIH PEKERJAAN")
                .setMultiChoiceItems(itemArray, isSelectedPekerjaanArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        pekerjaanSelectedList.asArray().clear();
                        pekerjaanSelectedList.asArray().addAll(selectedList);

                        StringBuilder pekerjaan = new StringBuilder();
                        for (int i = 0; i < pekerjaanSelectedList.size(); i++) {
                            pekerjaan.append(pekerjaanSelectedList.get(i).asString()).append(", ");
                        }

                        for (int i = 0; i < pekerjaanList.size(); i++) {
                            if(pekerjaanSelectedList.asArray().contains(pekerjaanList.get(i).asString())){
                                isSelectedPekerjaanArr[i] = true;
                            }else{
                                isSelectedPekerjaanArr[i] = false;
                            }
                        }

                        btnPekerjaan.setText(pekerjaan.toString());
                    }
                })
                .setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedList.clear();
                    }
                });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = listView.isItemChecked(position);
                String itemSelected = parent.getItemAtPosition(position).toString();

                if (itemSelected.equals("ALL")) {
                    if (selectAllPekerjaan) {
                        for (int i = 1; i < itemArray.length; i++) { // we start with first element after "Select all" choice
                            if (isChecked && !listView.isItemChecked(i)
                                    || !isChecked && listView.isItemChecked(i)) {
                                listView.performItemClick(listView, i, 0);
                            }
                        }
                    }
                } else {
                    if (!isChecked && listView.isItemChecked(0)) {
                        selectAllPekerjaan = false;
                        listView.performItemClick(listView, 0, 0);
                        selectAllPekerjaan = true;
                    }
                }

                try{
                    if (isChecked) {
                        selectedList.add(itemSelected);
                    } else {
                        if(selectedList.size() > 0){
                            for (int i = 0; i < selectedList.size(); i++) {
                                if(!isChecked && selectedList.get(i).equals(itemSelected)){
                                    selectedList.remove(i);
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    showError(selectedList.toString());
                }

            }
        });
    }


    private void initLvPekerjaan(View dialogView) {
        final ListView listView = dialogView.findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_multiple_select, pekerjaanList.asArray()) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (convertView == null) {
                    view = getLayoutInflater().inflate(R.layout.item_multiple_select, parent, false);
                }

                TextView tvTittle = view.findViewById(R.id.tv_tittle_selection);
                final CheckBox cbSelect = view.findViewById(R.id.cb_selection);
                tvTittle.setText(pekerjaanList.get(position).asString());
                cbSelect.setTag(pekerjaanList.get(position).asString());

                for (int i = 0; i < pekerjaanSelectedList.size(); i++) {
                    if (pekerjaanSelectedList.get(i).asString().equals(pekerjaanList.get(position).asString())) {
                        cbSelect.setChecked(true);
                    }
                }

                cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            pekerjaanSelectedList.add(pekerjaanList.get(position).asString());
                            //  onCheckedSelectedListener.getSelectedItem(pekerjaanSelectedList.asArray(), position);
                        } else {
                            for (int i = 0; i < pekerjaanSelectedList.size(); i++) {
                                if (pekerjaanSelectedList.get(i).asString().equals(pekerjaanList.get(position).asString())) {
                                    pekerjaanSelectedList.remove(i);
                                }
                            }
                            //  onCheckedSelectedListener.removeSelectedItem(pekerjaanSelectedList.asArray(), position);
                        }
                    }
                });
                return view;
            }
        };

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            }
        });
    }

    private void saveData(final boolean isUpdate, final int discountID) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                if(isUpdate){
                    args.put("action", "update");
                    args.put("discountID", String.valueOf(discountID));
                }else{
                    args.put("action", "add");
                    args.put("namaLayanan", btnLayanan.getText().toString());
                }

                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("pekerjaan", btnPekerjaan.getText().toString());
                args.put("diskon", NumberFormatUtils.clearPercent(find(R.id.et_disc_layanan, EditText.class).getText().toString()));
                args.put("layananID", String.valueOf(idLayanan));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
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

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(DISCOUNT_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }

    private void setSpStatus(String status) {
        List<String> statusList = new ArrayList<>();
        statusList.add("TIDAK AKTIF");
        statusList.add("AKTIF");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        find(R.id.sp_status, Spinner.class).setAdapter(statusAdapter);
        if (!status.isEmpty()) {
            for (int i = 0; i < find(R.id.sp_status, Spinner.class).getCount(); i++) {
                if (find(R.id.sp_status, Spinner.class).getItemAtPosition(i).equals(status)) {
                    find(R.id.sp_status, Spinner.class).setSelection(i);
                    break;
                }
            }
        }
    }

    private void setSpLayanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("spec", "BENGKEL");
                args.put("layanan", "DISCOUNT LAYANAN");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LAYANAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        dataLayananList.add(Nson.newObject()
                                .set("ID", result.get(i).get("LAYANAN_ID").asString())
                                .set("NAMA_LAYANAN", result.get(i).get("NAMA_LAYANAN").asString()));
                        layananList.add(result.get(i).get("NAMA_LAYANAN").asString());
                    }

                    spDialogLayanan = new SpinnerDialog(getActivity(), layananList, "PILIH JENIS LAYANAN");
                    spDialogLayanan.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int position) {
                            if (item.equals(dataLayananList.get(position).get("NAMA_LAYANAN").asString())) {
                                idLayanan = dataLayananList.get(position).get("ID").asInteger();
                                btnLayanan.setText(item);
                            }
                        }
                    });

                    btnLayanan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spDialogLayanan.showSpinerDialog();
                        }
                    });

                } else {
                    Messagebox.showDialog(getActivity(), "Konfirmasi", "Layanan Gagal di Muat, Muat Ulang?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpLayanan();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    showError(ERROR_INFO);
                }
            }
        });
    }
}
