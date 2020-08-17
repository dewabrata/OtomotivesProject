package com.rkrzmail.oto.modules.user;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AturUser_Activity extends AppActivity {

    private static final String TAG = "AturUser_____";
    private String ket = " Tidak Boleh Kosong";
    private static final int REQUEST_PHOTO = 80;
    private MultiSelectionSpinner spAkses, spPosisi;
    private Spinner spStatus;
    private Nson layanan = Nson.newArray();
    List<String> data = new ArrayList<>();
    private String[] aksesArr;
    private String noPonsel;
    private int day, month, year;

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
        loadData();
        getNoPonsel();
        spAkses = findViewById(R.id.spinnerAksesApp);
        spPosisi = findViewById(R.id.spinnerPosisi);
        spStatus = (Spinner) findViewById(R.id.spinnerStatus);

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

        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "NAMA", "");
        find(R.id.spinnerStatus, Spinner.class).setSelection(-1);
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
                } else if (counting < 6) {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.txtNoPonsel, EditText.class).requestFocus();
                } else {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (data.size() > 0) {
                    for (String no : data) {
                        if (s.toString().equalsIgnoreCase(no)) {
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
                if (!s.toString().contains("@")) {
                    find(R.id.tl_email_user, TextInputLayout.class).setError("Email Tidak Valid");
                } else {
                    find(R.id.tl_email_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });

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
                getImageFromAlbum();
            }
        });
    }

    private void getDateSpinnerDialog(final TextView dateTime, String tittle) {
        Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                dateTime.setText(formattedTime);
            }
        };
        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateListener,
                year, month, day
        );
        dialog.setTitle(tittle);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        if (getIntent().hasExtra("data")) {
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
            find(R.id.txtEmail, EditText.class).setText(data.get("EMAIL").asString());
            find(R.id.txtAlamat, EditText.class).setText(data.get("ALAMAT").asString());
            find(R.id.txtTglMasuk, TextView.class).setText(data.get("TANGGAL_MASUK").asString());
            //find(R.id.spinnerPosisi, MultiSelectionSpinner.class).setSelection(data.get("POSISI").asArray(), true);
            find(R.id.spinnerStatus, Spinner.class).setSelection(Tools.getIndexSpinner
                    (find(R.id.spinnerStatus, Spinner.class), data.get("STATUS").asString()));
            find(R.id.spinnerPenggajian, Spinner.class).setSelection(Tools.getIndexSpinner
                    (find(R.id.spinnerStatus, Spinner.class), data.get("PENGGAJIAN").asString()));
            find(R.id.txtGaji, TextView.class).setText(data.get("GAJI").asString());
            //find(R.id.spinnerAksesApp, MultiSelectionSpinner.class).setSelection(data.get("AKSES_APP").asStringArray(), true);
            find(R.id.tblSimpan, Button.class).setText("Update");
            find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (find(R.id.txtNoPonsel, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtNoPonsel, TextView.class).setError("No. Ponsel" + ket);
                        return;
                    }
                    if (find(R.id.txtEmail, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtEmail, EditText.class).setError("Email" + ket);
                        return;
                    }
                    if (find(R.id.txtAlamat, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtAlamat, EditText.class).setError("Alamat" + ket);
                        return;
                    }
                    if (find(R.id.txtTglMasuk, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtTglMasuk, TextView.class).setError("Tanggal Masuk" + ket);
                        return;
                    }
                    if (spPosisi.getSelectedItemsAsString().isEmpty()) {
                        showWarning("Silahkan Pilih Posisi");
                        spPosisi.requestFocus();
                        spPosisi.performClick();
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
                    if (find(R.id.txtGaji, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtGaji, EditText.class).setError("Gaji" + ket);
                        return;
                    }
                    if (spAkses.getSelectedItemsAsString().isEmpty()) {
                        showWarning("Silahkan Pilih Posisi");
                        spAkses.requestFocus();
                        spAkses.performClick();
                        return;
                    }
                    updateData(data);
                }
            });
        } else {
            find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (find(R.id.txtNamaKaryawan, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtNamaKaryawan, EditText.class).setError("Nama" + ket);
                        return;
                    }
                    if (find(R.id.txtNik, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtNik, EditText.class).setError("NIK" + ket);
                        return;
                    }
                    if (find(R.id.txtTglLahir, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtTglLahir, TextView.class).setError("Tanggal Lahir" + ket);
                        return;
                    }
                    if (find(R.id.txtNoPonsel, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtNoPonsel, TextView.class).setError("No. Ponsel" + ket);
                        return;
                    }
                    if (find(R.id.txtEmail, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtEmail, EditText.class).setError("Email" + ket);
                        return;
                    }
                    if (find(R.id.txtAlamat, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.txtAlamat, EditText.class).setError("Alamat" + ket);
                        return;
                    }
                    if (find(R.id.txtTglMasuk, TextView.class).getText().toString().isEmpty()) {
                        find(R.id.txtTglMasuk, TextView.class).setError("Tanggal Masuk" + ket);
                        return;
                    }
                    if (spPosisi.getSelectedItemsAsString().isEmpty()) {
                        showWarning("Silahkan Pilih Posisi");
                        spPosisi.requestFocus();
                        spPosisi.performClick();
                        return;
                    }
                    if (spAkses.getSelectedItemsAsString().isEmpty()) {
                        showWarning("Silahkan Pilih Posisi");
                        spAkses.requestFocus();
                        spAkses.performClick();
                        return;
                    }
                    if (find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        showWarning("Silahkan Pilih Posisi");
                        find(R.id.spinnerKelamin, Spinner.class).requestFocus();
                        find(R.id.spinnerKelamin, Spinner.class).performClick();
                        return;
                    }
                    if (find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        showWarning("Silahkan Pilih Status");
                        find(R.id.spinnerStatus, Spinner.class).requestFocus();
                        find(R.id.spinnerStatus, Spinner.class).performClick();
                        return;
                    }
                    addData();
                }
            });
        }
    }

    private void addData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        final String posisi = spPosisi.getSelectedItemsAsString();
        final String parseTglLahir = Tools.setFormatDayAndMonthToDb(find(R.id.txtTglLahir, TextView.class).getText().toString());
        final String parseTglMasuk = Tools.setFormatDayAndMonthToDb(find(R.id.txtTglMasuk, TextView.class).getText().toString());
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                /*add :  CID, action(add), nama, nik, tanggallahir, kelamin, nopol, email, alamat, tanggalmasuk,
                posisi, status, penggajian, gaji, akses*/
                args.put("action", "add");
                args.put("nama", find(R.id.txtNamaKaryawan, EditText.class).getText().toString());
                args.put("nik", find(R.id.txtNik, EditText.class).getText().toString());
                args.put("tanggallahir", parseTglLahir);
                args.put("kelamin", find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString());
                args.put("nopol", find(R.id.txtNoPonsel, TextView.class).getText().toString());
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tanggalmasuk", parseTglMasuk);
                args.put("posisi", posisi);
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", find(R.id.txtGaji, EditText.class).getText().toString());
                args.put("akses", aksesApp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menambahkan Aktifitas");
                }
            }
        });
    }

    private void updateData(final Nson id) {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        final String posisi = spPosisi.getSelectedItemsAsString();
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
                args.put("posisi", posisi);
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
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("NO_PONSEL").asString());
                }
            }
        });
        Log.d(TAG, "getNoPonsel: " + data);
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

