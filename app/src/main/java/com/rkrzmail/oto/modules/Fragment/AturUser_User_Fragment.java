package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.rkrzmail.oto.modules.Adapter.AturUser_MainTab_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KARYAWAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;

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
    private boolean isPosisi, isAksesApp;
    boolean isUpdate = false;
    private int idUser = 0;
    private String fotoBase64 = "";

    private Bitmap bitmapFoto;
    private AlertDialog alertDialog;

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
            fungsiMekanik = data.get("MEKANIK").asString().equals("Y") ? "YA" : "TIDAK";
            idUser = data.get("ID").asInteger();

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
    }


    private void initListener() {
        spPosisi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                find(R.id.sp_fungsiMekanik, Spinner.class).setEnabled(!item.contains("MEKANIK"));
                if (item.contains("MEKANIK") && !find(R.id.sp_fungsiMekanik, Spinner.class).isEnabled()) {
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
                getImagePickOrCamera();
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
                    activity.setErrorSpinner(find(R.id.spinnerStatus, Spinner.class), "STATUS HARUS DI PILIH");
                    return;
                }

                if (spAkses.getSelectedItemsAsString().isEmpty()) {
                    activity.showWarning("POSISI HARUS DI PILIH");
                    spAkses.performClick();
                    return;
                }

                if (spPosisi.getSelectedItem().toString().equals("--PILIH--")) {
                    activity.setErrorSpinner(spPosisi, "POSISI HARUS DI PILIH");
                    return;
                }

                if (isUpdate) {
                    updateData();
                } else {
                    if (bitmapFoto == null) {
                        activity.showWarning("Silahkan Masukkan Foto");
                        return;
                    }
                    if (activity.find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        activity.setErrorSpinner(find(R.id.spinnerKelamin, Spinner.class), "KELAMIN HARUS DI PILIH");
                        return;
                    }
                    addData();
                }
            }
        });

    }

    private void initTextWatcher() {
        activity.minEntryEditText(find(R.id.txtNamaKaryawan, EditText.class), 8, find(R.id.tl_nama_user, TextInputLayout.class), "Nama Min. 5 Karakter");
        activity.minEntryEditText(find(R.id.txtAlamat, EditText.class), 20, find(R.id.tl_alamat_user, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");

        find(R.id.et_no_ponsel, EditText.class).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        activity.showInfo("dome");
                        return true; // consume.
                    }
                }
                return false;
            }
        });

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
                } else {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (find(R.id.et_no_ponsel, EditText.class).getText().toString().length() < 13) {
                    find(R.id.tl_nohp_user, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.et_no_ponsel, EditText.class).requestFocus();
                }

                if (dataPonsel.size() > 0 && !isUpdate) {
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
        spAkses.setItems(aksesArr, loadFrom);
        spAkses.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });
    }

    private void getImagePickOrCamera() {
        final List<Intent> intents = new ArrayList<>();
        intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

        Intent result = Intent.createChooser(intents.remove(0), null);
        result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        startActivityForResult(result, REQUEST_FOTO);
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
                args.put("mekanik", find(R.id.sp_fungsiMekanik, Spinner.class).getSelectedItem().toString().equals("YA") ? "Y" : "N");
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
                args.put("akses", aksesApp);
                args.put("myCodeAbsen", spMycode.getSelectedItem().toString().equals("YA") ? "Y" : "N");
                args.put("jenisTab", "PERSONAL");
                args.put("mekanik", find(R.id.sp_fungsiMekanik, Spinner.class).getSelectedItem().toString().equals("YA") ? "Y" : "N");


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

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewFoto(final boolean isPreview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle("Preview Foto");

        if (bitmapFoto != null)
            img.setImageBitmap(bitmapFoto);

        if (isPreview) {
            btnCancel.setText("Tutup");
            btnSimpan.setText("Foto Ulang");
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    alertDialog.dismiss();
                } else {
                    if (bitmapFoto != null) {
                        bitmapFoto = null;
                    }
                    alertDialog.dismiss();
                }

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    getImagePickOrCamera();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmapFoto != null) {
                                fotoBase64 = activity.bitmapToBase64(bitmapFoto);
                            }
                        }
                    });

                    activity.showSuccess("BERHASIL MENYIMPAN FOTO");
                    alertDialog.dismiss();
                }
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }


    private void getImageUri(final Uri imageUri, final Bundle imgBundle) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    try {
                        bitmapFoto = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    bitmapFoto = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                }
            }

            @Override
            public void runUI() {
                showDialogPreviewFoto(false);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FOTO) {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            Bundle extras = null;
            Uri imageUri = null;
            if (data != null) {
                extras = data.getExtras();
                if (extras == null)
                    imageUri = data.getData();
            }
            getImageUri(imageUri, extras);
        }
    }
}
