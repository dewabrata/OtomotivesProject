package com.rkrzmail.oto.modules.user;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AturUser_Activity extends AppActivity {

    private static final String TAG = "AturUser_____";
    private String ket = " Tidak Valid";
    private static final int REQUEST_PHOTO = 80;
    private MultiSelectionSpinner spAkses;
    private Spinner spStatus, spPosisi;
    private Nson layanan = Nson.newArray(), posisiList = Nson.newArray();
    private List<String>
            dataPonsel = new ArrayList<>(),
            listAkses = new ArrayList<>(),
            listStatus = new ArrayList<String>(Arrays.asList("--PILIH--", "AKTIF", "NON-AKTIF")),
            listPenggajian = new ArrayList<String>(Arrays.asList("--PILIH--", "HARIAN", "MINGGUAN", "BULANAN")),
            listFungsiMekanik = new ArrayList<>(Arrays.asList("TIDAK", "YA"));
    private String[] aksesArr;
    private String noPonsel, posisi = "", status = "", penggajian = "", fungsiMekanik = "";
    private boolean isClickDrawable = false, isAtur, isPosisi, isAksesApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_user);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spAkses = findViewById(R.id.spinnerAksesApp);
        spPosisi = findViewById(R.id.spinnerPosisi);
        spStatus = (Spinner) findViewById(R.id.spinnerStatus);

        loadData();
        getNoPonsel();
        initTextWatcher();
        initTextButton();
    }

    private void initTextButton() {
        find(R.id.txtTglMasuk, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateSpinnerDialog(find(R.id.txtTglMasuk, TextView.class), "Tanggal Masuk");
            }
        });
        find(R.id.txtTglLahir, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateSpinnerDialog(find(R.id.txtTglLahir, TextView.class), "Tanggal Lahir");
            }
        });

        find(R.id.imgBtn_upload, ImageView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClickDrawable = true;
                getImageFromAlbum();
            }
        });
    }

    private void initTextWatcher() {
        find(R.id.txtGaji, TextView.class).addTextChangedListener(new RupiahFormat(find(R.id.txtGaji, EditText.class)));
        minEntryEditText(find(R.id.txtNamaKaryawan, EditText.class), 8, find(R.id.tl_nama_user, TextInputLayout.class), "Nama Min. 5 Karakter");
        minEntryEditText(find(R.id.txtAlamat, EditText.class), 20, find(R.id.tl_alamat_user, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");

        find(R.id.txtNoPonsel, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setErrorEnabled(false);
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setErrorEnabled(false);
                } else if (counting < 4) {
                    find(R.id.txtNoPonsel, EditText.class).setText("+62 ");
                    Selection.setSelection(find(R.id.txtNoPonsel, EditText.class).getText(), find(R.id.txtNoPonsel, EditText.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.txtNoPonsel, EditText.class).requestFocus();
                } else {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dataPonsel.size() > 0) {
                    String noHp = find(R.id.txtNoPonsel, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
                    for (String no : dataPonsel) {
                        if (noHp.equalsIgnoreCase(no)) {
                            find(R.id.tl_nohp_user, TextInputLayout.class).setError("No. Hp Sudah Terdaftar");
                        }
                    }
                }
            }
        });

        find(R.id.txtEmail, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_email_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().contains("@") || !s.toString().contains(".com")) {
                    find(R.id.tl_email_user, TextInputLayout.class).setError("Email Tidak Valid");
                } else {
                    find(R.id.tl_email_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });
    }

    private void setSpAkses() {
        aksesArr = getResources().getStringArray(R.array.akses_app_karyawan);
        spAkses.setItems(aksesArr);
        spAkses.setSelection(aksesArr, false);
        spAkses.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });
    }

    private void getImageFromAlbum() {
        try {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, REQUEST_PHOTO);
        } catch (Exception exp) {
            Log.i("Error", exp.toString());
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        if (getIntent().hasExtra("data")) {
            isAtur = true;
            find(R.id.txtNamaKaryawan, EditText.class).setEnabled(false);
            find(R.id.txtNik, EditText.class).setEnabled(false);
            find(R.id.txtTglLahir, TextView.class).setEnabled(false);
            find(R.id.spinnerKelamin, Spinner.class).setEnabled(false);
            find(R.id.txtNamaKaryawan, EditText.class).setText(data.get("NAMA").asString());
            find(R.id.txtNik, EditText.class).setText(data.get("NIK").asString());
            find(R.id.txtTglLahir, TextView.class).setText(data.get("TANGGAL_LAHIR").asString());
            find(R.id.spinnerKelamin, Spinner.class).setSelection(Tools.getIndexSpinner
                    (find(R.id.spinnerKelamin, Spinner.class), data.get("KELAMIN").asString()));
            find(R.id.txtNoPonsel, EditText.class).setText(data.get("NO_PONSEL").asString());
            find(R.id.tl_nohp_user, TextInputLayout.class).setHelperTextEnabled(false);
            find(R.id.txtEmail, EditText.class).setText(data.get("EMAIL").asString());
            find(R.id.txtAlamat, EditText.class).setText(data.get("ALAMAT").asString());
            find(R.id.txtTglMasuk, TextView.class).setText(data.get("TANGGAL_MASUK").asString());

            listAkses.add(data.get("AKSES_APP").asString());
            status = data.get("STATUS").asString();
            penggajian = data.get("PENGGAJIAN").asString();
            posisi = data.get("POSISI").asString();
            fungsiMekanik = data.get("FUNGSI_MEKANIK").asString();

            find(R.id.txtGaji, TextView.class).setText(data.get("GAJI").asString());
            find(R.id.tblSimpan, Button.class).setText("Update");
            find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (validateFields(find(R.id.ly_user, LinearLayout.class))) {
                        return;
                    }
                    if (find(R.id.txtNoPonsel, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtNoPonsel, TextView.class).setError("No. Ponsel" + ket);
                        return;
                    }

                    if (find(R.id.txtTglMasuk, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtTglMasuk, TextView.class).setError("Tanggal Masuk" + ket);
                        return;
                    }
                    if (find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        showWarning("Silahkan Pilih Status");
                        find(R.id.spinnerStatus, Spinner.class).requestFocus();
                        find(R.id.spinnerStatus, Spinner.class).performClick();
                        return;
                    }
                    if (find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH")) {
                        showWarning("Silahkan Pilih Penggajian");
                        find(R.id.spinnerPenggajian, Spinner.class).requestFocus();
                        find(R.id.spinnerPenggajian, Spinner.class).performClick();
                        return;
                    }

                    if (spAkses.getSelectedItemsAsString().isEmpty()) {
                        showWarning("Silahkan Pilih Posisi");
                        spAkses.performClick();
                        return;
                    }
                    updateData(data);
                }
            });
        } else {
            isAtur = false;
            find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateFields(find(R.id.ly_user, LinearLayout.class))) {
                        return;
                    }

                    if (find(R.id.txtTglLahir, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtTglLahir, TextView.class).requestFocus();
                        find(R.id.txtTglLahir, TextView.class).setError("Tanggal Lahir" + ket);
                        return;
                    }
                    if (find(R.id.txtNoPonsel, TextView.class).getText().toString().isEmpty() || find(R.id.tl_nohp_user, TextInputLayout.class).isHelperTextEnabled()) {
                        find(R.id.txtNoPonsel, TextView.class).requestFocus();
                        find(R.id.txtNoPonsel, TextView.class).setError("No. Ponsel" + ket);
                        return;
                    }

                    if (find(R.id.txtTglMasuk, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtTglMasuk, TextView.class).requestFocus();
                        find(R.id.txtTglMasuk, TextView.class).setError("Tanggal Masuk" + ket);
                        return;
                    }
                    if (spAkses.getSelectedItemsAsString().isEmpty()) {
                        showWarning("Silahkan Pilih Posisi");
                        spAkses.performClick();
                        return;
                    }
                    if (find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        showWarning("Silahkan Pilih Posisi");
                        find(R.id.spinnerKelamin, Spinner.class).performClick();
                        return;
                    }
                    if (find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        showWarning("Silahkan Pilih Status");
                        find(R.id.spinnerStatus, Spinner.class).performClick();
                        return;
                    }
                    if (find(R.id.tl_nohp_user, TextInputLayout.class).isHelperTextEnabled()) {
                        showWarning("Silahkan Lengkapi Validasi Form");
                        return;
                    }

                    if (!isClickDrawable) {
                        showWarning("Silahkan Masukkan Foto");
                        return;
                    }
                    addData();
                }
            });
        }

        setSpAkses();
        setSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA", posisi);
        setSpinnerOffline(listStatus, spStatus, status);
        setSpinnerOffline(listFungsiMekanik, find(R.id.sp_fungsiMekanik, Spinner.class), fungsiMekanik);
        setSpinnerOffline(listPenggajian, find(R.id.spinnerPenggajian, Spinner.class), penggajian);
    }

    private void addData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        if (aksesApp.contains("--PILIH--")) {
            aksesApp.replace("--PILIH--", "");
        }
        final String parseTglLahir = Tools.setFormatDayAndMonthToDb(find(R.id.txtTglLahir, TextView.class).getText().toString());
        final String parseTglMasuk = Tools.setFormatDayAndMonthToDb(find(R.id.txtTglMasuk, TextView.class).getText().toString());
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("nama", find(R.id.txtNamaKaryawan, EditText.class).getText().toString());
                args.put("nik", find(R.id.txtNik, EditText.class).getText().toString());
                args.put("tanggallahir", parseTglLahir);
                args.put("kelamin", find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString());
                args.put("nopol", find(R.id.txtNoPonsel, TextView.class).getText().toString().replaceAll("[^0-9]+", ""));
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tanggalmasuk", parseTglMasuk);
                args.put("posisi", spPosisi.getSelectedItem().toString());
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", find(R.id.txtGaji, EditText.class).getText().toString());
                args.put("mekanik", find(R.id.sp_fungsiMekanik, Spinner.class).getSelectedItem().toString());
                args.put("akses", aksesApp);
                if (args.containsValue("--PILIH--")) {
                    args.values().remove("--PILIH--");
                }
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Mendaftarkan User");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    if (result.get("message").asString().contains("Duplicate entry")) {
                        showWarning("Nomor Ponsel Sudah Terdaftar di Database", Toast.LENGTH_LONG);
                        find(R.id.txtNoPonsel, TextView.class).setText("");
                        find(R.id.txtNoPonsel, TextView.class).requestFocus();
                    } else {
                        showError("Gagal Menambahkan Aktifitas");
                    }
                }
            }
        });
    }

    private void updateData(final Nson id) {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("nama", find(R.id.txtNamaKaryawan, EditText.class).getText().toString());
                args.put("nopol", find(R.id.txtNoPonsel, EditText.class).getText().toString());
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tangalmasuk", find(R.id.txtTglMasuk, TextView.class).getText().toString());
                args.put("posisi", spPosisi.getSelectedItem().toString());
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", find(R.id.txtGaji, EditText.class).getText().toString());
                args.put("akses", aksesApp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Memperharui Data");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Memperbarui Data");
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("nomorponsel"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        dataPonsel.add(result.get("data").get(i).get("NO_PONSEL").asString());
                    }
                    Log.d(TAG, "getNoPonsel: " + dataPonsel);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_PHOTO) {
            final Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = Tools.getResizedBitmap(selectedImage, 400);
                find(R.id.imgBtn_upload, ImageView.class).setImageBitmap(selectedImage);
            } catch (FileNotFoundException f) {
                showError("Fail" + f.getMessage());
            }
        }
    }
}

