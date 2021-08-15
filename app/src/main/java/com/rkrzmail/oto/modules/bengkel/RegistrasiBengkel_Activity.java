package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.LoginActivity;
import com.rkrzmail.oto.modules.MapPicker_Dialog;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.srv.SpinnerDialogOto;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.rkrzmail.utils.APIUrls.CHECK_REFFERAL;
import static com.rkrzmail.utils.APIUrls.SET_REGISTRASI;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_PONSEL;

public class RegistrasiBengkel_Activity extends AppActivity implements View.OnClickListener, MapPicker_Dialog.GetLocation {

    private static final int REQUEST_REFEREAL = 56;
    private static final int REQUEST_MAPS = 57;
    private static final String TAG = "REHIST___";
    private EditText etKontakPerson, etNoPonsel, etEmail, etNamaBengkel, etAlamat, etJabatan;
    private Spinner spKendaraan, spKodeRefferal;
    private NikitaAutoComplete etKotaKab;

    private String[] itemsMerk;
    private String typeKendaraan, bidangUsaha = "";
    private boolean isKategori, isRegist = false;

    private String latitude = "", longitude = "";
    private String noPonselReferee = "";
    private String kodeRefferal = "";

    private final List<String> bidangUsahaMotorList = new ArrayList<>();
    private final List<String> bidangUsahaMobilList = new ArrayList<>();
    private final List<String> noHpList = new ArrayList<>();
    private final List<String> merkMotorList = new ArrayList<>();
    private final List<String> merkMobilList = new ArrayList<>();

    private Nson dataMerkKendaraan = Nson.newArray();
    private Nson dataBidangUsaha = Nson.newArray();
    private final Nson saveDataMerk = Nson.newArray();
    private final Nson saveDataBidangUsaha = Nson.newArray();

    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi_bengkel);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Registrasi Bengkel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void initComponent() {
        spKodeRefferal = findViewById(R.id.sp_kode_refferal);
        etAlamat = findViewById(R.id.et_alamat_regist);
        etEmail = findViewById(R.id.et_email_regist);
        etKotaKab = findViewById(R.id.et_kotakab_regist);
        etNamaBengkel = findViewById(R.id.et_namaBengkel_regist);
        etKontakPerson = findViewById(R.id.et_cp_regist);
        etNoPonsel = findViewById(R.id.et_noPhone_regist);
        spKendaraan = findViewById(R.id.sp_jenisKendaraan_regist);
        etJabatan = findViewById(R.id.et_jabatan_regist);

        //etNoPonsel.setText("6281381768836");
        minEntryEditText(etNamaBengkel, 8, find(R.id.tl_namaBengkel_regist, TextInputLayout.class), "Nama Bengkel Min. 5 Karakter");
        minEntryEditText(etAlamat, 20, find(R.id.tl_alamat_regist, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");
        //getNoPonsel();
        setSpKendaraan("");

        if (getIntent().hasExtra("NO_PONSEL")) {
            etNoPonsel.setEnabled(false);
            etNoPonsel.setText("+" + getIntentStringExtra("NO_PONSEL"));
        }

        etKontakPerson.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_cp_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!isAlphabet(text)) {
                    find(R.id.tl_cp_regist, TextInputLayout.class).setError("Nama Tidak Valid");
                } else {
                    if (s.toString().length() == 0) {
                        find(R.id.tl_cp_regist, TextInputLayout.class).setErrorEnabled(false);
                    } else if (text.length() < 5) {
                        find(R.id.tl_cp_regist, TextInputLayout.class).setError("Panjang Nama Min. 5 Karakter");
                    } else {
                        find(R.id.tl_cp_regist, TextInputLayout.class).setErrorEnabled(false);
                    }
                }
            }
        });

        etNoPonsel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                } else if (counting < 4) {
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    etNoPonsel.requestFocus();
                } else {
                    find(R.id.tl_nohp_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_email_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains("@")) {
                    find(R.id.tl_email_regist, TextInputLayout.class).setError("Email Tidak Valid");
                } else {
                    find(R.id.tl_email_regist, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });

        String aggrement = "Setuju dengan <font color=#F44336><u> Syarat & kondisi </u></font> pemakain Bengkel Pro";
        find(R.id.tv_setuju_regist, TextView.class).setText(Html.fromHtml(aggrement));
        find(R.id.tv_setuju_regist, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo("Show Aggrement");
            }
        });

        mapPicker_dialog.getBengkelLocation(this);
        find(R.id.btn_lokasi_regist).setEnabled(false);
        find(R.id.btn_lokasi_regist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    mapPicker_dialog.show(getSupportFragmentManager(), null);
                } else {
                    requestLocationPermission();
                    showWarning("Anda Harus Mengijinkan Aplikasi Mengakses Lokasi!");
                }
            }
        });

        etKotaKab.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initDialogKotaKab();
                }
                return true;
            }
        });
        Log.d(TAG, "initComponent: " + bidangUsahaMotorList.size());
        find(R.id.btn_simpan_regist, Button.class).setOnClickListener(this);
        getRefferal();
    }

    int REQUEST_LOCATION = 8765;
    MapPicker_Dialog mapPicker_dialog = new MapPicker_Dialog();

    boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                mapPicker_dialog.show(getSupportFragmentManager(), null);
            } else {
                requestLocationPermission();
                showWarning("Anda Harus Mengijinkan Akses Lokasi!");
            }

        }
    }


    private boolean isAlphabet(String value) {
        return value.matches("^[a-zA-Z]*$");
    }

    private void setSpKendaraan(final String selection) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    List<String> kendaraanList = new ArrayList<>();
                    kendaraanList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        if (!kendaraanList.contains(result.get(i).get("TYPE").asString())) {
                            kendaraanList.add(result.get(i).get("TYPE").asString());
                        }
                    }
                    ArrayAdapter<String> kendaraanAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, kendaraanList);
                    kendaraanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spKendaraan.setAdapter(kendaraanAdapter);
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spKendaraan.getCount(); i++) {
                            if (spKendaraan.getItemAtPosition(i).toString().equals(selection)) {
                                spKendaraan.setSelection(i);
                                break;
                            }
                        }
                    }

                    spKendaraan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i != 0) {
                                setSpBidangUsaha(null);
                                setSpMerkKendaraan(spKendaraan.getItemAtPosition(i).toString());
                            }

                            if (spKendaraan.getItemAtPosition(i).toString().contains("MOTOR")) {
                                isKategori = true;
                            } else if (spKendaraan.getItemAtPosition(i).toString().contains("MOBIL")) {
                                isKategori = false;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else {
                    setSpKendaraan("");
                }
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                char[] noPonselChar = NumberFormatUtils.formatOnlyNumber(etNoPonsel.getText().toString()).toCharArray();
                if (noPonselChar[2] == '0') {
                    noPonselChar[2] = ' ';
                }

                args.put("action", "add");
                args.put("nohp", NumberFormatUtils.formatOnlyNumber(new String(noPonselChar)));
                args.put("nama", etKontakPerson.getText().toString());
                args.put("email", etEmail.getText().toString());
                args.put("nama_bengkel", etNamaBengkel.getText().toString());
                args.put("jenis", spKendaraan.getSelectedItem().toString().trim());
                args.put("persetujuan", find(R.id.cb_setuju_regist, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                args.put("alamat", etAlamat.getText().toString());
                args.put("daerah", etKotaKab.getText().toString());
                args.put("merk_kendaraan", find(R.id.btn_merk_kendaraan, Button.class).getText().toString());
                args.put("tanggal_regist", currentDateTime());
                args.put("tanggal_aktif", currentDateTime());
                args.put("kodeRefferal", kodeRefferal);
                args.put("noPonselReferee", noPonselReferee);
                args.put("latitudeLokasi", latitude);
                args.put("longitudeLokasi", longitude);
                args.put("merkList", saveDataMerk.toJson());
                args.put("bidangUsahaList", saveDataBidangUsaha.toJson());
                args.put("jabatan", etJabatan.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REGISTRASI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    AppApplication.getMessageTrigger();
                    showSuccess("Registrasi Berhasil");
                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.putExtra("NO_PONSEL", etNoPonsel.getText().toString().replaceAll("[^0-9]+", ""));
                    startActivity(i);
                    finish();
                } else {
                    if (result.get("message").asString().toLowerCase().contains("duplicate")) {
                        showError("No Ponsel Sudah Terdaftar");
                        etNoPonsel.setText("");
                        etNoPonsel.requestFocus();
                        etNoPonsel.setEnabled(true);
                    } else {
                        showError(result.get("message").asString());
                    }
                }
            }
        });
    }

    private void getNoPonsel() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "ALL");
                args.put("CID", "KOSONG");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_NOMOR_PONSEL), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        noHpList.add(result.get("data").get(i).get("NO_PONSEL").asString());
                    }
                    Log.d(TAG, "getNoPonsel: " + noHpList);
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        String info = "Silahkan lengkapi ";
        switch (view.getId()) {
            case R.id.btn_simpan_regist:
                if (etKontakPerson.getText().toString().isEmpty() || etKontakPerson.getText().toString().length() < 5
                        || find(R.id.tl_cp_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etKontakPerson.setError(info + "Nama Pemilik");
                    etKontakPerson.requestFocus();
                    return;
                }
                if (etNoPonsel.getText().toString().isEmpty() || etNoPonsel.getText().toString().length() < 6
                        || find(R.id.tl_nohp_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etNoPonsel.setError(info + "No. Ponsel");
                    etNoPonsel.requestFocus();
                    return;
                }
                if (etEmail.getText().toString().isEmpty() || !etEmail.getText().toString().contains("@") ||
                        find(R.id.tl_email_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etEmail.setError(info + "Email");
                    etEmail.requestFocus();
                    return;
                }
                if (etNamaBengkel.getText().toString().isEmpty() || etNamaBengkel.getText().toString().length() < 8 ||
                        find(R.id.tl_namaBengkel_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etNamaBengkel.setError(info + "Nama Bengkel");
                    etNamaBengkel.requestFocus();
                    return;
                }
                if (spKendaraan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showInfo(info + "Kendaraan");
                    spKendaraan.requestFocus();
                    return;
                }
                if (etKotaKab.getText().toString().isEmpty()) {
                    etKotaKab.setError(info + "Kota / Kab");
                    etKotaKab.requestFocus();
                    return;
                }
                if (etAlamat.getText().toString().isEmpty() || etAlamat.getText().toString().length() < 20 ||
                        find(R.id.tl_alamat_regist, TextInputLayout.class).isHelperTextEnabled()) {
                    etAlamat.setError(info + "Alamat");
                    etAlamat.requestFocus();
                    return;
                }
                if (!find(R.id.cb_setuju_regist, CheckBox.class).isChecked()) {
                    showWarning("Silahkan Setujui Syarat Dan Ketentuan Aplikasi");
                    return;
                }

               /* if (latitude.isEmpty() && longitude.isEmpty()) {
                    showWarning("PETA LOKASI HARUS DI SET");
                    return;
                }*/

                saveData();

                break;
        }
    }

    private final Nson merkSelectedList = Nson.newArray();
    boolean[] isSelectedMerkArr = null;
    boolean selectAllMerk = true;

    private void setSpMerkKendaraan(final String jenisKendaraan) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("CID", "CID");
                args.put("flag", "Merk");
                args.put("jenisKendaraan", jenisKendaraan);
                if (dataMerkKendaraan.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
                    dataMerkKendaraan = result.get("data");
                }

                if (merkMotorList.size() == 0 && merkMobilList.size() == 0) {
                    merkMotorList.add("ALL");
                    merkMobilList.add("ALL");
                    for (int i = 0; i < dataMerkKendaraan.size(); i++) {
                        if (dataMerkKendaraan.get(i).get("TYPE").asString().equalsIgnoreCase("MOTOR")) {
                            merkMotorList.add(dataMerkKendaraan.get(i).get("MERK").asString());
                        } else if (dataMerkKendaraan.get(i).get("TYPE").asString().equalsIgnoreCase("MOBIL")) {
                            merkMobilList.add(dataMerkKendaraan.get(i).get("MERK").asString());
                        }
                    }
                }
            }

            @Override
            public void runUI() {
                find(R.id.btn_merk_kendaraan).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogMerkKendaraan();
                    }
                });
            }
        });
    }

    private void showDialogMerkKendaraan() {
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(merkSelectedList.asArray());

        final String[] itemArray = isKategori ? merkMotorList.toArray(new String[]{}) : merkMobilList.toArray(new String[]{});
        isSelectedMerkArr = new boolean[itemArray.length];
        if(selectedList.size() > 0){
            for (int j = 0; j < itemArray.length; j++) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if(itemArray[j].equals(selectedList.get(i))){
                        isSelectedMerkArr[j] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Merk Kendaraan")
                .setMultiChoiceItems(itemArray, isSelectedMerkArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        merkSelectedList.asArray().clear();
                        merkSelectedList.asArray().addAll(selectedList);
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                for (int j = 0; j < dataMerkKendaraan.size(); j++) {
                                    if (selectedList.get(i).equals(dataMerkKendaraan.get(j).get("MERK").asString())) {
                                        saveDataMerk.add(Nson.newObject()
                                                .set("MERK_ID", dataMerkKendaraan.get(j).get("ID").asString())
                                                .set("MERK", dataMerkKendaraan.get(j).get("MERK").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                        initRvMerkKendaraan();
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
                    if (selectAllMerk) {
                        for (int i = 1; i < itemArray.length; i++) { // we start with first element after "Select all" choice
                            if (isChecked && !listView.isItemChecked(i)
                                    || !isChecked && listView.isItemChecked(i)) {
                                listView.performItemClick(listView, i, 0);
                            }
                        }
                    }
                } else {
                    if (!isChecked && listView.isItemChecked(0)) {
                        selectAllMerk = false;
                        listView.performItemClick(listView, 0, 0);
                        selectAllMerk = true;
                    }
                }

                try {
                    if (isChecked) {
                        selectedList.add(itemSelected);
                    } else {
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                if (selectedList.get(i).equals(itemSelected)) {
                                    selectedList.remove(i);
                                }
                            }
                        }
                    }
                    isSelectedMerkArr[position] = isChecked;
                } catch (Exception e) {
                    showError(selectedList.toString());
                }

            }
        });
    }

    private void initRvMerkKendaraan(){
        RecyclerView rvMerk = setRecylerViewHorizontal(R.id.rv_merk_kendaraan, 2);
        if (rvMerk != null) {
            rvMerk.setAdapter(new NikitaRecyclerAdapter(merkSelectedList, R.layout.item_sort_by) {
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.img_check_selected).setVisibility(View.GONE);
                    if(!merkSelectedList.get(position).asString().equals("ALL")){
                        viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(merkSelectedList.get(position).asString());
                    }
                }
            });
            Objects.requireNonNull(rvMerk.getAdapter()).notifyDataSetChanged();
        }

    }

    private void initRvBidangUsaha(){
        RecyclerView rvBidangUsaha = setRecylerViewHorizontal(R.id.rv_bidang_usaha, 2);
        if (rvBidangUsaha != null) {
            rvBidangUsaha.setAdapter(new NikitaRecyclerAdapter(bidangUsahaSelectedList, R.layout.item_sort_by) {
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.img_check_selected).setVisibility(View.GONE);
                    viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(bidangUsahaSelectedList.get(position).asString());
                }
            });
            Objects.requireNonNull(rvBidangUsaha.getAdapter()).notifyDataSetChanged();
        }

    }

    private final Nson bidangUsahaSelectedList = Nson.newArray();
    boolean[] isSelectedBidangUsahaArr = null;
    boolean selectAllBidangUsaha = true;

    private void setSpBidangUsaha(final List<String> selectionList) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "bidangUsaha");
                if (dataBidangUsaha.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
                    dataBidangUsaha = result.get("data");
                }

                if (bidangUsahaMotorList.size() == 0 && bidangUsahaMobilList.size() == 0) {
                    for (int i = 0; i < dataBidangUsaha.size(); i++) {
                        if (dataBidangUsaha.get(i).get("JENIS_KENDARAAN").asString().equalsIgnoreCase("MOTOR")) {
                            bidangUsahaMotorList.add(dataBidangUsaha.get(i).get("BIDANG_USAHA").asString());
                        } else if (dataBidangUsaha.get(i).get("JENIS_KENDARAAN").asString().equalsIgnoreCase("MOBIL")) {
                            bidangUsahaMobilList.add(dataBidangUsaha.get(i).get("BIDANG_USAHA").asString());
                        }
                    }
                }

                if(selectionList.size() > 0){
                    bidangUsahaSelectedList.asArray().addAll(selectionList);
                }
            }

            @Override
            public void runUI() {
                find(R.id.btn_bidang_usaha).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogBidangUsaha();
                    }
                });
                initRvBidangUsaha();
            }
        });
    }

    private void showDialogBidangUsaha(){
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(bidangUsahaSelectedList.asArray());

        final String[] itemArray = isKategori ? bidangUsahaMotorList.toArray(new String[]{}) : bidangUsahaMobilList.toArray(new String[]{});
        isSelectedBidangUsahaArr = new boolean[itemArray.length];
        if(selectedList.size() > 0){
            for (int j = 0; j < itemArray.length; j++) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if(itemArray[j].equals(selectedList.get(i))){
                        isSelectedBidangUsahaArr[j] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bidang Usaha")
                .setMultiChoiceItems(itemArray, isSelectedBidangUsahaArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        bidangUsahaSelectedList.asArray().clear();
                        bidangUsahaSelectedList.asArray().addAll(selectedList);
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                for (int j = 0; j < dataBidangUsaha.size(); j++) {
                                    if (selectedList.get(i).equals(dataBidangUsaha.get(j).get("BIDANG_USAHA").asString())) {
                                        saveDataBidangUsaha.add(Nson.newObject()
                                                .set("BIDANG_USAHA_ID", dataBidangUsaha.get(j).get("ID").asString())
                                                .set("BIDANG_USAHA", dataBidangUsaha.get(j).get("BIDANG_USAHA").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                        initRvBidangUsaha();
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

                if (!isChecked && listView.isItemChecked(0)) {
                    listView.performItemClick(listView, 0, 0);
                }

                try {
                    if (isChecked) {
                        selectedList.add(itemSelected);
                    } else {
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                if (selectedList.get(i).equals(itemSelected)) {
                                    selectedList.remove(i);
                                }
                            }
                        }
                    }
                    isSelectedBidangUsahaArr[position] = isChecked;
                } catch (Exception e) {
                    showError(e.getMessage());
                }

            }
        });
    }

    private RecyclerView setRecylerViewHorizontal(int viewId, int spanCount) {
        if (viewId == 0) return null;
        RecyclerView recyclerView = findViewById(viewId);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager
                (
                        spanCount,
                        StaggeredGridLayoutManager.HORIZONTAL
                );
        recyclerView.setLayoutManager(layoutManager);
        return recyclerView;
    }

    private void getRefferal() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + NumberFormatUtils.formatOnlyNumber(etNoPonsel.getText().toString());
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(CHECK_REFFERAL), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                    final Nson dataRefferalList = Nson.newArray();
                    List<String> kodeList = new ArrayList<>();
                    kodeList.add("--PILIH--");
                    dataRefferalList.add("");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if (kodeList.size() > 0) {
                                for (int j = 0; j < kodeList.size(); j++) {
                                    String referee = result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString();
                                    if (!kodeList.get(i).equals(referee)) {
                                        kodeList.add(result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString());
                                        dataRefferalList.add(Nson.newObject()
                                                .set("COMPARISON", result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString())
                                                .set("NO_REFERRAL", result.get(i).get("NO_REFERRAL").asString())
                                                .set("NO_PONSEL_REFEREE", result.get(i).get("NO_PONSEL_REFEREE").asString())
                                                .set("KONTAK_PERSON", result.get(i).get("KONTAK_PERSON").asString())
                                                .set("JABATAN", result.get(i).get("JABATAN").asString())
                                                .set("NAMA_BENGKEL", result.get(i).get("NAMA_BENGKEL").asString())
                                                .set("ALAMAT", result.get(i).get("ALAMAT").asString())
                                                .set("KOTA_KAB", result.get(i).get("KOTA_KAB").asString())
                                                .set("BIDANG_USAHA", result.get(i).get("BIDANG_USAHA").asString())
                                                .set("JENIS_KENDARAAN", result.get(i).get("JENIS_KENDARAAN").asString())
                                        );
                                        break;
                                    }
                                }
                            } else {
                                dataRefferalList.add(Nson.newObject()
                                        .set("COMPARISON", result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString())
                                        .set("NO_REFERRAL", result.get(i).get("NO_REFERRAL").asString())
                                        .set("NO_PONSEL_REFEREE", result.get(i).get("NO_PONSEL_REFEREE").asString())
                                        .set("KONTAK_PERSON", result.get(i).get("KONTAK_PERSON").asString())
                                        .set("JABATAN", result.get(i).get("JABATAN").asString())
                                        .set("NAMA_BENGKEL", result.get(i).get("NAMA_BENGKEL").asString())
                                        .set("ALAMAT", result.get(i).get("ALAMAT").asString())
                                        .set("KOTA_KAB", result.get(i).get("KOTA_KAB").asString())
                                        .set("BIDANG_USAHA", result.get(i).get("BIDANG_USAHA").asString())
                                        .set("JENIS_KENDARAAN", result.get(i).get("JENIS_KENDARAAN").asString())
                                );
                                kodeList.add(result.get(i).get("NAMA_REFEREE").asString() + " - " + result.get(i).get("NO_PONSEL_REFEREE").asString());
                            }
                        }
                    } else {
                        Tools.setViewAndChildrenEnabled(find(R.id.rl_referral, RelativeLayout.class), false);
                    }

                    setSpinnerOffline(kodeList, spKodeRefferal, "");
                    spKodeRefferal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String reff = parent.getItemAtPosition(position).toString();
                            if (position != 0 && reff.equals(dataRefferalList.get(position).get("COMPARISON").asString())) {
                                kodeRefferal = dataRefferalList.get(position).get("NO_REFERRAL").asString();
                                noPonselReferee = dataRefferalList.get(position).get("NO_PONSEL_REFEREE").asString();
                                etKontakPerson.setText(dataRefferalList.get(position).get("KONTAK_PERSON").asString());
                                etJabatan.setText(dataRefferalList.get(position).get("JABATAN").asString());
                                etNamaBengkel.setText(dataRefferalList.get(position).get("NAMA_BENGKEL").asString());
                                etAlamat.setText(dataRefferalList.get(position).get("ALAMAT").asString());
                                etKotaKab.setText(dataRefferalList.get(position).get("KOTA_KAB").asString());

                                String bidangUsaha = dataRefferalList.get(position).get("BIDANG_USAHA").asString();
                                String[] biidangUsahaSplit = bidangUsaha.split(",");
                                List<String> selectionBidangUsaha = new ArrayList<>(Arrays.asList(biidangUsahaSplit));
                                setSpBidangUsaha(selectionBidangUsaha);
                                setSpKendaraan(dataRefferalList.get(position).get("JENIS_KENDARAAN").asString());
                            } else {
                                noPonselReferee = "";
                                etKontakPerson.setText("");
                                etJabatan.setText("");
                                etNamaBengkel.setText("");
                                etAlamat.setText("");
                                etKotaKab.setText("");
                                setSpKendaraan("");
                                setSpBidangUsaha(null);
                            }


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            }
        });
    }

    private void initDialogKotaKab() {
        SpinnerDialogOto spinnerDialog = new SpinnerDialogOto(
                getActivity(),
                new ArrayList<String>(),
                "Pilih Kota / Kab"
        );

        Map<String, String> args = AppApplication.getInstance().getArgsData();
        args.put("nama", "DAERAH");

        spinnerDialog.setParamsSearch(args, "search");
        spinnerDialog.setApiUrl(AppApplication.getBaseUrlV3(VIEW_MST), "KOTA_KAB");
        spinnerDialog.bindOnSpinerListener(new SpinnerDialogOto.OnItemClick() {
            @Override
            public void onClick(String item) {
                etKotaKab.setText(item);
            }
        });

        spinnerDialog.showSpinerDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void getLatLong(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        find(R.id.tv_longlat, TextView.class).setText(latitude + ", " + longitude);
    }
}
