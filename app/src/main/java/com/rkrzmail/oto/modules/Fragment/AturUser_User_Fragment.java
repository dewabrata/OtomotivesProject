package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.rkrzmail.oto.modules.bengkel.AturUser_MainTab_Activity;
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
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturUser_User_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private MultiSelectionSpinner spAkses;

    private static final int REQUEST_PHOTO = 80;
    private static final String TAG = "AturUser_____";
    private String ket = " Tidak Valid";
    private Spinner spStatus, spPosisi, spMycode;
    private Nson layanan = Nson.newArray(), posisiList = Nson.newArray();
    private List<String>
            dataPonsel = new ArrayList<>(),
            listAkses = new ArrayList<>(),
            listStatus = new ArrayList<String>(Arrays.asList("--PILIH--", "AKTIF", "NON-AKTIF")),
            listPenggajian = new ArrayList<String>(Arrays.asList("--PILIH--", "HARIAN", "MINGGUAN", "BULANAN")),
            listFungsiMekanik = new ArrayList<>(Arrays.asList("TIDAK", "YA"));

    private List<String> aksesArr = new ArrayList<>();
    private String noPonsel, posisi = "", status = "", penggajian = "", fungsiMekanik = "", mycode = "";
    private String fotoUser = "";
    private boolean isClickDrawable = false, isPosisi, isAksesApp;
    boolean isUpdate = false;
    private int idUser = 0;

    public AturUser_User_Fragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_atur_user, container, false);
        activity = ((AturUser_MainTab_Activity) getActivity());
        initHideToolbar();
        initComponent();
        loadData();
        return fragmentView;
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
        }
    }

    public <T extends View> T to(View v, Class<? super T> s) {
        return (T) (v);
    }

    public <T extends View> T find(int id) {
        return (T) fragmentView.findViewById(id);
    }

    public <T extends View> T find(int id, Class<? super T> s) {
        return (T) fragmentView.findViewById(id);
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }


    private void initComponent() {
        spAkses = fragmentView.findViewById(R.id.spinnerAksesApp);
        spPosisi = fragmentView.findViewById(R.id.sp_posisi);
        spStatus = fragmentView.findViewById(R.id.spinnerStatus);
        spMycode = fragmentView.findViewById(R.id.sp_my_code_absen);

        getNoPonsel();
        initTextWatcher();
        initListener();
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        aksesArr = Arrays.asList(getResources().getStringArray(R.array.akses_app_karyawan));
        final Nson data = Nson.readJson(activity.getIntentStringExtra(DATA));
        if (!data.asString().isEmpty()) {
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
            find(R.id.et_no_ponsel, EditText.class).setText(data.get("NO_PONSEL").asString());
            find(R.id.tl_nohp_user, TextInputLayout.class).setHelperTextEnabled(false);
            find(R.id.txtEmail, EditText.class).setText(data.get("EMAIL").asString());
            find(R.id.txtAlamat, EditText.class).setText(data.get("ALAMAT").asString());
            find(R.id.txtTglMasuk, TextView.class).setText(data.get("TANGGAL_MASUK").asString());

            mycode = data.get("MY_CODE_ABSEN").asString();
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
            if (posisi.equals("MANAGEMENT")) {
                dummy.addAll(aksesArr);
            }
            setSpAkses(dummy);
        } else {
            setSpAkses(null);
        }


        activity.setSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA", posisi);
        activity.setSpinnerOffline(listStatus, spStatus, status);
        activity.setSpinnerOffline(Arrays.asList("--PILIH--", "YA", "TIDAK"), spMycode, mycode);
        activity.setSpinnerOffline(listFungsiMekanik, find(R.id.sp_fungsiMekanik, Spinner.class), fungsiMekanik);
        activity.setSpinnerOffline(listPenggajian, find(R.id.spinnerPenggajian, Spinner.class), penggajian);
    }


    private void initListener() {
        spPosisi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                find(R.id.sp_fungsiMekanik, Spinner.class).setEnabled(item.equals("MEKANIK"));
                if(item.equals("MEKANIK")){
                    find(R.id.sp_fungsiMekanik, Spinner.class).setSelection(1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        find(R.id.txtTglMasuk, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getDateSpinnerDialog(activity.find(R.id.txtTglMasuk, TextView.class), "Tanggal Masuk");
            }
        });
        find(R.id.txtTglLahir, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getDateSpinnerDialog(activity.find(R.id.txtTglLahir, TextView.class), "Tanggal Lahir");
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
                if (activity.validateFields(activity.find(R.id.ly_user, LinearLayout.class))) {
                    return;
                }
                if (activity.find(R.id.et_no_ponsel, TextView.class).getText().toString().isEmpty()) {
                    find(R.id.et_no_ponsel, TextView.class).setError("No. Ponsel" + ket);
                    return;
                }

                if (activity.find(R.id.txtTglMasuk, TextView.class).getText().toString().isEmpty()) {
                    find(R.id.txtTglMasuk, TextView.class).setError("Tanggal Masuk" + ket);
                    return;
                }
                if (activity.find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    activity.showWarning("Silahkan Pilih Status");
                    find(R.id.spinnerStatus, Spinner.class).requestFocus();
                    find(R.id.spinnerStatus, Spinner.class).performClick();
                    return;
                }
                if (activity.find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH")) {
                    activity.showWarning("Silahkan Pilih Penggajian");
                    find(R.id.spinnerPenggajian, Spinner.class).requestFocus();
                    find(R.id.spinnerPenggajian, Spinner.class).performClick();
                    return;
                }

                if (spAkses.getSelectedItemsAsString().isEmpty()) {
                    activity.showWarning("Silahkan Pilih Posisi");
                    spAkses.performClick();
                    return;
                }

                if (isUpdate) {
                    updateData();
                } else {
                    if (activity.validateFields(activity.find(R.id.ly_user, LinearLayout.class))) {
                        return;
                    }
                    if (!isClickDrawable) {
                        activity.showWarning("Silahkan Masukkan Foto");
                        return;
                    }
                    if (activity.find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        activity.showWarning("Silahkan Pilih Posisi");
                        find(R.id.spinnerKelamin, Spinner.class).performClick();
                        return;
                    }
                    if (activity.find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        activity.showWarning("Silahkan Pilih Status");
                        find(R.id.spinnerStatus, Spinner.class).performClick();
                        return;
                    }
                    if (activity.find(R.id.tl_nohp_user, TextInputLayout.class).isHelperTextEnabled()) {
                        activity.showWarning("Silahkan Lengkapi Validasi Form");
                        return;
                    }
                    addData();
                }
            }
        });

    }

    private void initTextWatcher() {
        find(R.id.txtGaji, TextView.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.txtGaji, EditText.class)));
        activity.minEntryEditText(find(R.id.txtNamaKaryawan, EditText.class), 8, find(R.id.tl_nama_user, TextInputLayout.class), "Nama Min. 5 Karakter");
        activity.minEntryEditText(find(R.id.txtAlamat, EditText.class), 20, find(R.id.tl_alamat_user, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");

        find(R.id.et_no_ponsel, EditText.class).addTextChangedListener(new TextWatcher() {
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
                    find(R.id.et_no_ponsel, EditText.class).setText("+62 ");
                    Selection.setSelection(activity.find(R.id.et_no_ponsel, EditText.class).getText(), find(R.id.et_no_ponsel, EditText.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.et_no_ponsel, EditText.class).requestFocus();
                } else {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dataPonsel.size() > 0) {
                    String noHp = find(R.id.et_no_ponsel, EditText.class).getText().toString().replaceAll("[^0-9]+", "");
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
        if(loadFrom == null){
            loadFrom = new ArrayList<>();
            loadFrom.add("PARTS");
            loadFrom.add("ABSENSI");
            loadFrom.add("REFFERAL");
            loadFrom.add("SARAN");
        };
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

    private void addData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        if (aksesApp.contains("--PILIH--")) {
            aksesApp.replace("--PILIH--", "");
        }
        final String parseTglLahir = Tools.setFormatDayAndMonthToDb(activity.find(R.id.txtTglLahir, TextView.class).getText().toString());
        final String parseTglMasuk = Tools.setFormatDayAndMonthToDb(activity.find(R.id.txtTglMasuk, TextView.class).getText().toString());
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("nama", find(R.id.txtNamaKaryawan, EditText.class).getText().toString());
                args.put("nik", find(R.id.txtNik, EditText.class).getText().toString());
                args.put("tanggallahir", parseTglLahir);
                args.put("kelamin", find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString());
                args.put("noPonsel", find(R.id.et_no_ponsel, TextView.class).getText().toString().replaceAll("[^0-9]+", ""));
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tanggalmasuk", parseTglMasuk);
                args.put("posisi", spPosisi.getSelectedItem().toString());
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", NumberFormatUtils.formatOnlyNumber(activity.find(R.id.txtGaji, EditText.class).getText().toString()));
                args.put("mekanik", find(R.id.sp_fungsiMekanik, Spinner.class).getSelectedItem().toString());
                args.put("akses", aksesApp);
                args.put("myCodeAbsen", spMycode.getSelectedItem().toString().equals("YA") ? "Y" : "N");
                if (args.containsValue("--PILIH--")) {
                    args.values().remove("--PILIH--");
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KARYAWAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.showSuccess("Sukses Mendaftarkan User");
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                } else {
                    if (result.get("message").asString().contains("Duplicate entry")) {
                        activity.showWarning("Nomor Ponsel Sudah Terdaftar di Database", Toast.LENGTH_LONG);
                        find(R.id.et_no_ponsel, TextView.class).setText("");
                        find(R.id.et_no_ponsel, TextView.class).requestFocus();
                    } else {
                        activity.showError("Gagal Menambahkan Aktifitas");
                    }
                }
            }
        });
    }

    private void updateData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            String parseTglMasuk = Tools.setFormatDayAndMonthToDb(activity.find(R.id.txtTglMasuk, TextView.class).getText().toString());

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("idUser", String.valueOf(idUser));
                args.put("nama", find(R.id.txtNamaKaryawan, EditText.class).getText().toString());
                args.put("noPonsel", activity.formatOnlyNumber(activity.find(R.id.et_no_ponsel, EditText.class).getText().toString()));
                args.put("email", find(R.id.txtEmail, EditText.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, EditText.class).getText().toString());
                args.put("tangalmasuk", parseTglMasuk);
                args.put("posisi", spPosisi.getSelectedItem().toString());
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", NumberFormatUtils.formatOnlyNumber(activity.find(R.id.txtGaji, EditText.class).getText().toString()));
                args.put("akses", aksesApp);
                args.put("myCodeAbsen", spMycode.getSelectedItem().toString().equals("YA") ? "Y" : "N");
                args.put("jenisTab", "PERSONAL");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KARYAWAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.showSuccess("SUKSES MEMPERHARUI DATA");
                    activity.setResult(activity.RESULT_OK);
                    activity.finish();
                } else {
                    activity.showError("GAGAL MEMPERBARUI DATA");
                }
            }
        });
    }

    private void getNoPonsel() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "NO PONSEL USER");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.get("data").size(); i++) {
                        dataPonsel.add(result.get("data").get(i).get("NO_PONSEL").asString());
                    }
                }
            }
        });
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHOTO) {
            final Uri imageUri = data.getData();
            try {
                InputStream imageStream = activity.getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = Tools.getResizedBitmap(selectedImage, 400);
                fotoUser = getRealPathFromURI(imageUri);
                find(R.id.imgBtn_upload, ImageView.class).setImageBitmap(selectedImage);
            } catch (FileNotFoundException f) {
                activity.showError("Fail" + f.getMessage());
            }
        }
    }
}
