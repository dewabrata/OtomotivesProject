package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KARYAWAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

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

    private List<String> aksesArr = new ArrayList<>();
    private String noPonsel, posisi = "", status = "", penggajian = "", fungsiMekanik = "";
    private String fotoUser = "";
    private boolean isClickDrawable = false, isPosisi, isAksesApp;
    boolean isUpdate = false;
    private int idUser = 0;

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
        initListener();
    }

    private void initListener() {
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

                if (isUpdate) {
                    updateData();
                } else {
                    if (validateFields(find(R.id.ly_user, LinearLayout.class))) {
                        return;
                    }
                    if (!isClickDrawable) {
                        showWarning("Silahkan Masukkan Foto");
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
                    addData();
                }
            }
        });

    }

    private void initTextWatcher() {
        find(R.id.txtGaji, TextView.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.txtGaji, EditText.class)));
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

    private void setSpAkses(List<String> loadFrom) {
        spAkses.setItems(aksesArr, loadFrom);
        //spAkses.setSelectionMatch(aksesArr, loadFrom);
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
        aksesArr = Arrays.asList(getResources().getStringArray(R.array.akses_app_karyawan));
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        if (data != null) {
            isUpdate = true;
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
            idUser = data.get("ID").asInteger();

            find(R.id.txtGaji, TextView.class).setText(RP + NumberFormatUtils.formatRp(data.get("GAJI").asString()));
            find(R.id.tblSimpan, Button.class).setText("Update");

            String[] splitAkses = data.get("AKSES_APP").asString().trim().split(", ");
            List<String> dummy = new ArrayList<>();
            for (String s : splitAkses) {
                dummy.add(s.trim());
            }
            if(posisi.equals("MANAGEMENT")){
                dummy.addAll(aksesArr);
            }
            setSpAkses(dummy);
        } else {
            setSpAkses(null);
        }


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
                args.put("noPonsel", find(R.id.txtNoPonsel, TextView.class).getText().toString().replaceAll("[^0-9]+", ""));
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tanggalmasuk", parseTglMasuk);
                args.put("posisi", spPosisi.getSelectedItem().toString());
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", formatOnlyNumber(find(R.id.txtGaji, EditText.class).getText().toString()));
                args.put("mekanik", find(R.id.sp_fungsiMekanik, Spinner.class).getSelectedItem().toString());
                args.put("akses", aksesApp);
                if (args.containsValue("--PILIH--")) {
                    args.values().remove("--PILIH--");
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KARYAWAN), args));
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

    private void updateData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            String parseTglMasuk = Tools.setFormatDayAndMonthToDb(find(R.id.txtTglMasuk, TextView.class).getText().toString());
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("idUser", String.valueOf(idUser));
                args.put("nama", find(R.id.txtNamaKaryawan, EditText.class).getText().toString());
                args.put("noPonsel", formatOnlyNumber(find(R.id.txtNoPonsel, EditText.class).getText().toString()));
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tangalmasuk", parseTglMasuk);
                args.put("posisi", spPosisi.getSelectedItem().toString());
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", formatOnlyNumber(find(R.id.txtGaji, EditText.class).getText().toString()));
                args.put("akses", aksesApp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KARYAWAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("SUKSES MEMPERHARUI DATA");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("GAGAL MEMPERBARUI DATA");
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
                fotoUser = getRealPathFromURI(imageUri);
                find(R.id.imgBtn_upload, ImageView.class).setImageBitmap(selectedImage);
            } catch (FileNotFoundException f) {
                showError("Fail" + f.getMessage());
            }
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }
}

