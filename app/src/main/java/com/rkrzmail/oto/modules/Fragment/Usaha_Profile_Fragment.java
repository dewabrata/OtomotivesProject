package com.rkrzmail.oto.modules.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.MapPicker_Dialog;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;
import static com.rkrzmail.utils.ConstUtils.PICK_IMAGE_CAMERA;
import static com.rkrzmail.utils.ConstUtils.PICK_IMAGE_GALLERY;

public class Usaha_Profile_Fragment extends Fragment implements OnMapReadyCallback, MapPicker_Dialog.GetLocation {

    private static final int REQUEST_PHOTO = 80;
    private static final int REQUEST_LOGO = 81;

    private EditText etNamaBengkel, etAlamat, etBadanUsaha, etKotaKab, etNoTelp, etNib, etNpwp, etKodePos, etNoTelpMessage,
            etMaxAntrianExpress, etMaxAntrianStandart;
    private Spinner spAfiliasi, spPrincial;
    private MultiSelectionSpinner spJenisKendaraan, spMerkKendaraan, spBidangUsaha;
    private CheckBox cbPkp;
    private TextView tvAntrianExpress, tvAntrianStandart;
    private AlertDialog alertDialog;
    private Button btnLogo, btnTampakDepan;

    private Bitmap bitmapLogo = null, bitmapTampakDepan = null;
    private String fotoLogoBase64 = "", fotoTampakDepanBase64 = "";
    private String latitude = "", longitude = "";
    private Nson principalList = Nson.newArray();
    private AppActivity activity;
    private Nson getData;

    private boolean isLoadBitmapLogo = false, isLoadBitmapDepan = false;
    private boolean isLogo = false, isTampakDepan = false;

    private final List<String> afiliasiList = Arrays.asList(
            "--PILIH--",
            "JARINGAN",
            "INDIVIDUAL"
    );

    private GoogleMap map;


    public Usaha_Profile_Fragment() {
    }

    public Usaha_Profile_Fragment newIntasnce(Nson data){
        this.getData = data;
        Bundle args = new Bundle();
        args.putString("DATA", data.toJson());
        Usaha_Profile_Fragment usahaProfileFragment = new Usaha_Profile_Fragment();
        usahaProfileFragment.setArguments(args);
        return usahaProfileFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_usaha_bengkel, container, false);
        activity = ((ProfileBengkel_Activity) getActivity());
        initComponent(v);
        initData();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVisible()){
        }
    }

    private void initComponent(View v) {
        etNamaBengkel = v.findViewById(R.id.et_namaBengkel_usaha);
        etAlamat = v.findViewById(R.id.et_alamat_usaha);
        etKodePos = v.findViewById(R.id.et_kodepos_usaha);
        etBadanUsaha = v.findViewById(R.id.et_namaUsaha_usaha);
        etKotaKab = v.findViewById(R.id.et_kotaKab_usaha);
        etNoTelp = v.findViewById(R.id.et_no_telp);
        etNoTelpMessage = v.findViewById(R.id.et_no_telp_message);
        etNib = v.findViewById(R.id.et_nib_usaha);
        etNpwp = v.findViewById(R.id.et_npwp_usaha);
        spAfiliasi = v.findViewById(R.id.sp_afiliasi_usaha);
        spPrincial = v.findViewById(R.id.sp_namaPrincial_usaha);
        spJenisKendaraan = v.findViewById(R.id.sp_jenisKendaraan_usaha);
        spBidangUsaha = v.findViewById(R.id.sp_bidangUsaha_usaha);
        spMerkKendaraan = v.findViewById(R.id.sp_merkKendaraan_usaha);
        cbPkp = v.findViewById(R.id.cb_pkp_usaha);
        etMaxAntrianExpress = v.findViewById(R.id.et_maxAntrianExpress);
        etMaxAntrianStandart = v.findViewById(R.id.et_maxAntrianStandart);
        tvAntrianExpress = v.findViewById(R.id.ic_AntrianExpress_usaha);
        tvAntrianStandart = v.findViewById(R.id.ic_AntrianStandart_usaha);
        btnLogo = v.findViewById(R.id.btn_logo_depan);
        btnTampakDepan = v.findViewById(R.id.btn_tampak_depan);
        Button btnSimpan = v.findViewById(R.id.btn_simpan_usaha);
        Button btnLokasi = v.findViewById(R.id.btn_lokasi_tambahan);

        final MapPicker_Dialog mapPicker_dialog = new MapPicker_Dialog();
        mapPicker_dialog.getBengkelLocation(this);
        btnLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPicker_dialog.show(getFragmentManager(), null);
            }
        });

        tvAntrianExpress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimeHourDialog(etMaxAntrianExpress);
            }
        });

        tvAntrianStandart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimeHourDialog(etMaxAntrianStandart);
            }
        });

        btnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLogo.getText().toString().equals("PREVIEW LOGO")) {
                    showDialogPreviewFoto(bitmapLogo, "Logo Bengkel", new String[]{}, true);
                } else {
                    isLogo = true;
                    isTampakDepan = false;
                    getImage();
                }
            }
        });

        btnTampakDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnTampakDepan.getText().toString().equals("PREVIEW TAMPAK DEPAN")) {
                    showDialogPreviewFoto(bitmapTampakDepan, "Tampak Depan Bengkel", new String[]{}, true);
                } else {
                    isLogo = false;
                    isTampakDepan = true;
                    getImage();
                }
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private Bitmap decodeBase64ToBitmap(String base64){
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void initData() {
        if (activity != null) {
            ((ProfileBengkel_Activity) getActivity()).getDataUsaha(new ProfileBengkel_Activity.UsahaData() {
                @SuppressLint("SetTextI18n")
                @Override
                public void getData(Nson nson) {
                    String logoBase64, depanBase64;
                    try{
                        logoBase64 = nson.get("LOGO_IMG").asString()
                                .replace("data:image/;base64,", "")
                                .replace("data:image/png;base64,", "");
                    }catch (Exception e){
                        logoBase64 = "";
                    }
                    try{
                        depanBase64 = nson.get("DEPAN_IMG").asString()
                                .replace("data:image/;base64,", "")
                                .replace("data:image/png;base64,", "");
                    }catch (Exception e){
                        depanBase64 = "";
                    }
                    if (!logoBase64.isEmpty()) {
                        isLoadBitmapLogo = true;
                        bitmapLogo = decodeBase64ToBitmap(logoBase64);
                        btnLogo.setText("PREVIEW LOGO");
                    }
                    if (!depanBase64.isEmpty()) {
                        isLoadBitmapDepan = true;
                        bitmapTampakDepan = decodeBase64ToBitmap(depanBase64);
                        btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
                    }

                    etKodePos.setText(nson.get("KODE_POS").asString());
                    cbPkp.setChecked(nson.get("PKP").asString().equals("Y"));
                    etNpwp.setText(nson.get("NPWP").asString());
                    etNib.setText(nson.get("NIB").asString());
                    etBadanUsaha.setText(nson.get("KATEGORI_BENGKEL").asString());
                    etNamaBengkel.setText(nson.get("NAMA_BENGKEL").asString());
                    etAlamat.setText(nson.get("ALAMAT").asString());
                    etKotaKab.setText(nson.get("KOTA_KABUPATEN").asString());
                    etNoTelp.setText(nson.get("NO_TELP").asString());
                    etNoTelpMessage.setText(nson.get("HP_MESSAGE").asString());
                    etMaxAntrianExpress.setText(nson.get("MAX_ANTRIAN_EXPRESS_MENIT").asString());
                    etMaxAntrianStandart.setText(nson.get("MAX_ANTRIAN_STANDART_MENIT").asString());

                    List<String> jenisKendaraanList = new ArrayList<>(), merkKendaraanList = new ArrayList<>(),
                            bidangUsahaList = new ArrayList<>();
                    jenisKendaraanList.add(nson.get("JENIS_KENDARAAN").asString());
                    merkKendaraanList.add(nson.get("MERK_KENDARAAN").asString());
                    bidangUsahaList.add(nson.get("KATEGORI_BENGKEL").asString());

                    activity.setSpinnerOffline(afiliasiList, spAfiliasi, nson.get("AFLIASI").asString());
                    setSpNamaPrincipal(nson.get("NAMA_PRINCIPAL").asString());
                    setJenisKendaraan(jenisKendaraanList);
                    setMerkKendaraan(merkKendaraanList);
                    setSpBidangUsaha(bidangUsahaList);
                }
            });
        }
    }

    private void saveData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String latLong = latitude + ", " + longitude;
                String afliasi = spAfiliasi.getSelectedItem().toString();
                if(afliasi.contains("--PILIH--")){
                    afliasi = afliasi
                            .replace("--PILIH--", "")
                            .replace("--PILIH--,", "");
                }
                String principal = spPrincial.getSelectedItem().toString();
                if(principal.contains("--PILIH--")){
                    principal = principal.replace("--PILIH--", "");
                }
                args.put("action", "update");
                args.put("kategori", "USAHA");
                args.put("kodePos", etKodePos.getText().toString().toUpperCase());
                args.put("namaUsaha", etBadanUsaha.getText().toString().toUpperCase());
                args.put("nib", etNib.getText().toString().toUpperCase());
                args.put("npwp", etNpwp.getText().toString().toUpperCase());
                args.put("pkp", cbPkp.isChecked() ? "Y" : "N");
                args.put("afliasi", afliasi);
                args.put("namaPrincipial", principal);
                args.put("noTelp", etNoTelp.getText().toString().toUpperCase());
                args.put("hpMessage", etNoTelpMessage.getText().toString().toUpperCase());
                args.put("fotoLogo", fotoLogoBase64);
                args.put("fotoTampakDepan", fotoTampakDepanBase64);
                args.put("antrianExpres", etMaxAntrianExpress.getText().toString());
                args.put("antrianStandart", etMaxAntrianStandart.getText().toString());
                args.put("petaLokasi", latLong);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PROFILE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.showSuccess("Sukses Menyimpan Data");
                    activity.setResult(RESULT_OK);
                } else {
                    activity.showError("Gagagl Menyimpan Data");
                }
            }
        });
    }

    private void setJenisKendaraan(List<String> string) {
        spJenisKendaraan.setEnabled(false);
        spJenisKendaraan.setItems(string);
    }

    private void setMerkKendaraan(List<String> string) {
        spMerkKendaraan.setEnabled(false);
        spMerkKendaraan.setItems(string);
    }

    private void setSpBidangUsaha(List<String> string) {
        spBidangUsaha.setEnabled(false);
        spBidangUsaha.setItems(string);
    }

    private void setSpNamaPrincipal(final String principal) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Principal");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("databengkel"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    principalList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        principalList.add(result.get(i).get("NAMA"));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item, principalList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPrincial.setAdapter(spinnerAdapter);
                    if (!principal.isEmpty()) {
                        for (int in = 0; in < spPrincial.getCount(); in++) {
                            if (spPrincial.getItemAtPosition(in).toString().contains(principal)) {
                                spPrincial.setSelection(in);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void getImageFromAlbum(final int REQUEST) {
        try {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, REQUEST);
        } catch (Exception exp) {
            Log.i("Error", exp.toString());
        }
    }

    public void getImage() {
        try {
            PackageManager pm = getContext().getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getContext().getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Kamera", "Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Kamera")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                activity.showError("IZINKAN AKSES CAMERA DI PENGATURAN");
        } catch (Exception e) {
            activity.showError("IZINKAN AKSES CAMERA DI PENGATURAN");
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CAMERA) {
            activity.showSuccess("");
            try {
                Uri selectedImage = data.getData();
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (isLogo) {
                    bitmapLogo = bitmap;
                    fotoLogoBase64 = activity.bitmapToBase64(bitmapLogo);
                    btnLogo.setText("PREVIEW LOGO");
                } else {
                    if (isTampakDepan) {
                        bitmapTampakDepan = bitmap;
                        fotoTampakDepanBase64 = activity.bitmapToBase64(bitmapTampakDepan);
                        btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
                    }
                }

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File destination = new File(Environment.getExternalStorageDirectory() + "/" +
                        getString(R.string.app_name), "IMG_" + timeStamp + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImage);
                if (isLogo) {
                    bitmapLogo = bitmap;
                    fotoLogoBase64 = activity.bitmapToBase64(bitmapLogo);
                    btnLogo.setText("PREVIEW LOGO");
                } else {
                    if (isTampakDepan) {
                        bitmapTampakDepan = bitmap;
                        fotoTampakDepanBase64 = activity.bitmapToBase64(bitmapTampakDepan);
                        btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
                    }
                }

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewFoto(final Bitmap bitmap, String toolbarTittle, final String[] base64, final boolean isPreview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(toolbarTittle);

        if (bitmap != null)
            img.setImageBitmap(bitmap);

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
                    if (bitmap == bitmapLogo) {
                        if (!isLoadBitmapLogo) {
                            bitmapLogo = null;
                        }
                    } else if (bitmap == bitmapTampakDepan) {
                        if (!isLoadBitmapDepan) {
                            bitmapTampakDepan = null;
                        }
                    }
                    alertDialog.dismiss();
                }

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    if (bitmap == bitmapLogo) {
                        isLogo = true;
                        isTampakDepan = false;
                        getImage();
                    } else if (bitmap == bitmapTampakDepan) {
                        isLogo = false;
                        isTampakDepan = true;
                        getImage();
                    }
                } else {
                    if (bitmap == bitmapLogo) {
                        btnLogo.setText("PREVIEW LOGO");
                    } else if (bitmap == bitmapTampakDepan) {
                        btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
                    }
                    if (bitmap != null) {
                        base64[0] = activity.bitmapToBase64(bitmap);
                    }
                    activity.showInfo("SUKSES MENYIMPAN FOTO");
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void getLatLong(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
