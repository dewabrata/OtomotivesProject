package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.MapPicker_Dialog;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.rkrzmail.utils.APIUrls.GET_LOGO_BENGKEL;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;

public class Usaha_Profile_Fragment extends Fragment implements OnMapReadyCallback, MapPicker_Dialog.GetLocation {

    private static final int REQUEST_PHOTO = 80;
    private static final int REQUEST_LOGO = 81;

    private EditText etNamaBengkel, etAlamat, etBadanUsaha, etKotaKab, etNoTelp, etNib, etNpwp, etKodePos, etNoTelpMessage,
            etMaxAntrianExpress, etMaxAntrianStandart, etGoogleBisnis;
    private Spinner spAfiliasi, spMngKeuangan;
    private MultiSelectionSpinner spJenisKendaraan, spMerkKendaraan, spBidangUsaha, spPrincial;
    private CheckBox cbPkp;
    private AlertDialog alertDialog;
    private Button btnLogo, btnTampakDepan;

    private Bitmap bitmapLogo = null, bitmapTampakDepan = null;
    private String fotoLogoBase64 = "", fotoTampakDepanBase64 = "";
    private String latitude = "", longitude = "";
    private String logoName = "logo.png", tampakDepanName = "tampakDepan.png";
    List<String> loadPrincipalList = new ArrayList<>();

    private Nson dataPrincipalList = Nson.newArray();
    private final Nson principalSaveList = Nson.newArray();
    private final Nson saveDataMerk = Nson.newArray();
    private final Nson saveDataBidangUsaha = Nson.newArray();
    private final Nson dataMerkKendaraan = Nson.newArray();
    private Nson dataBidangUsaha = Nson.newArray();
    private Nson getData;

    private AppActivity activity;
    private File logoTempFile = null, tampakDepanTempFile = null;

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

    public Usaha_Profile_Fragment newIntasnce(Nson data) {
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
        if (isVisible()) {
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
        ImageView imgAntrianExpress = v.findViewById(R.id.ic_AntrianExpress_usaha);
        ImageView imgAntrianStandart = v.findViewById(R.id.ic_AntrianStandart_usaha);
        btnLogo = v.findViewById(R.id.btn_logo_depan);
        btnTampakDepan = v.findViewById(R.id.btn_tampak_depan);
        Button btnSimpan = v.findViewById(R.id.btn_simpan_usaha);
        Button btnLokasi = v.findViewById(R.id.btn_lokasi_tambahan);
        etGoogleBisnis = v.findViewById(R.id.et_google_bisnis);
        spMngKeuangan  = v.findViewById(R.id.sp_management_keuangan);

        final MapPicker_Dialog mapPicker_dialog = new MapPicker_Dialog();
        mapPicker_dialog.getBengkelLocation(this);
        btnLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPicker_dialog.show(getFragmentManager(), null);
            }
        });

        imgAntrianExpress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimeHourDialog(etMaxAntrianExpress);
            }
        });

        imgAntrianStandart.setOnClickListener(new View.OnClickListener() {
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
                    getImagePickerByGalerryOrCamera();
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
                    getImagePickerByGalerryOrCamera();
                }
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etKodePos.getText().toString().isEmpty()) {
                    activity.errorFocus(etKodePos, "KODE POS HARUS DI PILIH");
                } else if (spAfiliasi.getSelectedItem().toString().equals("--PILIH--")) {
                    activity.setErrorSpinner(spAfiliasi, "AFILIASI HARUS DI PILIH");
                }else if(etMaxAntrianExpress.getText().toString().isEmpty()){
                    activity.errorFocus(etMaxAntrianExpress, "WAKTU MAX ANTRIAN HARUS DI ISI");
                }else if(etMaxAntrianStandart.getText().toString().isEmpty()){
                    activity.errorFocus(etMaxAntrianStandart, "WAKTU MAX ANTRIAN HARUS DI ISI");
                } else {
                    saveData();
                }
            }
        });
    }

    private Bitmap decodeBase64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void initData() {
        if (activity != null) {
            ((ProfileBengkel_Activity) Objects.requireNonNull(getActivity())).getDataUsaha(new ProfileBengkel_Activity.UsahaData() {
                @SuppressLint("SetTextI18n")
                @Override
                public void getData(Nson nson) {

                    etKodePos.setText(nson.get("KODE_POS").asString());
                    cbPkp.setChecked(nson.get("PKP").asString().equals("Y"));
                    etNpwp.setText(nson.get("NPWP").asString());
                    etNib.setText(nson.get("NIB").asString());
                    etBadanUsaha.setText(nson.get("NAMA_USAHA").asString());
                    etNamaBengkel.setText(nson.get("NAMA_BENGKEL").asString());
                    etAlamat.setText(nson.get("ALAMAT").asString());
                    etKotaKab.setText(nson.get("KOTA_KABUPATEN").asString());
                    etNoTelp.setText(nson.get("NO_TELP").asString());
                    etNoTelpMessage.setText(nson.get("HP_MESSAGE").asString());
                    etMaxAntrianExpress.setText(nson.get("MAX_ANTRIAN_EXPRESS_MENIT").asString());
                    etMaxAntrianStandart.setText(nson.get("MAX_ANTRIAN_STANDART_MENIT").asString());
                    etGoogleBisnis.setText(nson.get("GOOGLE_BISNIS").asString());

                    String isPembayaran = nson.get("PEMBAYARAN_ACTIVE").asString().equals("Y") ? "YA" : "TIDAK";

                    activity.setSpinnerOffline(Arrays.asList(activity.getResources().getStringArray(R.array.ya_tidak)), spMngKeuangan, isPembayaran);
                    setJenisKendaraan(Collections.singletonList(UtilityAndroid.getSetting(getContext(), "JENIS_KENDARAAN", "")));
                    setMerkKendaraan(nson.get("MERK_BENGKEL"));
                    setSpBidangUsaha(nson.get("BIDANG_USAHA_BENGKEL"));
                    activity.setSpinnerOffline(afiliasiList, spAfiliasi, nson.get("AFILIASI").asString());

                    if (nson.get("PRINCIPAL_BENGKEL").asArray().size() > 0) {
                        Nson loadPrincipal = nson.get("PRINCIPAL_BENGKEL");
                        if (loadPrincipal.size() > 0) {
                            for (int i = 0; i < loadPrincipal.size(); i++) {
                                loadPrincipalList.add(loadPrincipal.get(i).get("NAMA_PRINCIPAL").asString());
                            }
                        }
                    }
                    setSpNamaPrincipal(loadPrincipalList);
                }
            });
        }

        getImageBase64();
    }

    private void saveData() {
        final int jamExpress = Integer.parseInt(etMaxAntrianExpress.getText().toString().substring(0, 2));
        final int menitExpress = Integer.parseInt(etMaxAntrianExpress.getText().toString().substring(3, 5));
        final int jamStandart = Integer.parseInt(etMaxAntrianStandart.getText().toString().substring(0, 2));
        final int menitStandart = Integer.parseInt(etMaxAntrianStandart.getText().toString().substring(3, 5));

        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String latLong = latitude + ", " + longitude;
                String afliasi = spAfiliasi.getSelectedItem().toString();
                if (afliasi.contains("--PILIH--")) {
                    afliasi = afliasi
                            .replace("--PILIH--", "")
                            .replace("--PILIH--,", "");
                }
               
                args.put("action", "update");
                args.put("kategori", "USAHA");
                args.put("kodePos", etKodePos.getText().toString());
                args.put("namaUsaha", etBadanUsaha.getText().toString());
                args.put("nib", etNib.getText().toString());
                args.put("npwp", etNpwp.getText().toString());
                args.put("pkp", cbPkp.isChecked() ? "Y" : "N");
                args.put("afliasi", afliasi);
                args.put("namaPrincipial", "");
                args.put("noTelp", etNoTelp.getText().toString());
                args.put("hpMessage", etNoTelpMessage.getText().toString());
                args.put("fotoLogo", fotoLogoBase64);
                args.put("fotoTampakDepan", fotoTampakDepanBase64);
                args.put("antrianExpres", formatAntrianTime(jamExpress, menitExpress));
                args.put("antrianStandart", formatAntrianTime(jamStandart, menitStandart));
                args.put("petaLokasi", latLong);
                args.put("principalList", principalSaveList.toJson());
                args.put("googleBisnis", etGoogleBisnis.getText().toString());
                args.put("merkList", saveDataMerk.toJson());
                args.put("bidangUsahaList", saveDataBidangUsaha.toJson());
                args.put("pembayaranAktif", spMngKeuangan.getSelectedItem().toString().equals("YA") ? "Y" : "N");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PROFILE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.setSetting("MAX_ANTRIAN_EXPRESS_MENIT", formatAntrianTime(jamExpress, menitExpress));
                    activity.setSetting("MAX_ANTRIAN_STANDART_MENIT", formatAntrianTime(jamStandart, menitStandart));
                    if (saveDataMerk.size() > 0)
                        activity.setSetting("MERK_KENDARAAN_ARRAY", saveDataMerk.toJson());
                    if (saveDataBidangUsaha.size() > 0)
                        activity.setSetting("BIDANG_USAHA_ARRAY", saveDataBidangUsaha.toJson());

                    activity.showSuccess("BEHASIL MENYIMPAN DATA");
                    ((ProfileBengkel_Activity) activity).getData();
                    initData();
                } else {
                    activity.showError(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String formatAntrianTime(int jam, int menit) {
        try {
            return String.format("%02d:%02d:%02d", 0, jam, menit);
        } catch (Exception e) {
            return "";
        }
    }

    private void setJenisKendaraan(List<String> string) {
        spJenisKendaraan.setEnabled(false);
        spJenisKendaraan.setItems(string, true);
    }

    private void setMerkKendaraan(final Nson merkArray) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "Merk");
                if (dataMerkKendaraan.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("TYPE").asString().equalsIgnoreCase(activity.getSetting("JENIS_KENDARAAN"))) {
                            dataMerkKendaraan.add(Nson.newObject()
                                    .set("ID", result.get(i).get("ID").asString())
                                    .set("MERK", result.get(i).get("MERK").asString())
                            );
                        }
                    }
                }
            }

            @Override
            public void runUI() {
                List<String> loadMerk = new ArrayList<>();
                List<String> merkList = new ArrayList<>();

                for (int i = 0; i < merkArray.size(); i++) {
                    loadMerk.add(merkArray.get(i).get("MERK").asString());
                }
                if (dataMerkKendaraan.size() > 0) {
                    for (int j = 0; j < dataMerkKendaraan.size(); j++) {
                        merkList.add(dataMerkKendaraan.get(j).get("MERK").asString());
                    }
                }

                spMerkKendaraan.setTittle("Pilih Merk Kendaraan");
                spMerkKendaraan.setItems(merkList, loadMerk);
                spMerkKendaraan.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {
                        if (strings.size() > 0) {
                            saveDataMerk.asArray().clear();
                            for (int i = 0; i < strings.size(); i++) {
                                for (int j = 0; j < dataMerkKendaraan.size(); j++) {
                                    if (strings.get(i).equals(dataMerkKendaraan.get(j).get("MERK").asString())) {
                                        saveDataMerk.add(Nson.newObject()
                                                .set("MERK_ID", dataMerkKendaraan.get(j).get("ID").asString())
                                                .set("MERK", dataMerkKendaraan.get(j).get("MERK").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                    }
                });
            }
        });
    }

    private void setSpBidangUsaha(final Nson bidangUsahaArray) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "bidangUsaha");
                if (dataBidangUsaha.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("JENIS_KENDARAAN").asString().equalsIgnoreCase(activity.getSetting("JENIS_KENDARAAN"))) {
                            dataBidangUsaha.add(Nson.newObject()
                                    .set("ID", result.get(i).get("ID").asString())
                                    .set("BIDANG_USAHA", result.get(i).get("BIDANG_USAHA").asString())
                            );
                        }
                    }

                }
            }

            @Override
            public void runUI() {
                List<String> loadBidangUsahaList = new ArrayList<>();
                List<String> bidangUsahaList = new ArrayList<>();

                if (dataBidangUsaha.size() > 0) {
                    for (int j = 0; j < dataBidangUsaha.size(); j++) {
                        bidangUsahaList.add(dataBidangUsaha.get(j).get("BIDANG_USAHA").asString());
                    }
                }
                for (int i = 0; i < bidangUsahaArray.size(); i++) {
                    loadBidangUsahaList.add(bidangUsahaArray.get(i).get("KATEGORI").asString());
                }

                spBidangUsaha.setItems(bidangUsahaList, loadBidangUsahaList);
                spBidangUsaha.setTittle("Pilih Bidang Usaha");
                spBidangUsaha.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {
                        if (strings.size() > 0) {
                            saveDataBidangUsaha.asArray().clear();
                            for (int i = 0; i < strings.size(); i++) {
                                for (int j = 0; j < dataBidangUsaha.size(); j++) {
                                    if (strings.get(i).equals(dataBidangUsaha.get(j).get("BIDANG_USAHA").asString())) {
                                        saveDataBidangUsaha.add(Nson.newObject()
                                                .set("BIDANG_USAHA_ID", dataBidangUsaha.get(j).get("ID").asString())
                                                .set("BIDANG_USAHA", dataBidangUsaha.get(j).get("BIDANG_USAHA").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                    }
                });
            }
        });
    }

    private void setSpNamaPrincipal(final List<String> selectionList) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Principal");
                if (dataPrincipalList.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("databengkel"), args));
                    result = result.get("data");
                    dataPrincipalList = result;
                }

            }

            @Override
            public void runUI() {
                final List<String> principalNameList = new ArrayList<>();
                principalNameList.add("--PILIH--");
                dataPrincipalList.add("");
                if (dataPrincipalList.size() > 0) {
                    for (int i = 0; i < dataPrincipalList.size(); i++) {
                        if (!dataPrincipalList.get(i).asString().isEmpty()) {
                            principalNameList.add(dataPrincipalList.get(i).get("NAMA").asString());
                        }
                    }
                }

                spPrincial.setTittle("Pilih Principal");
                spPrincial.setItems(principalNameList, selectionList);
                spPrincial.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(final List<String> strings) {
                        if (strings.size() > 0) {
                            principalSaveList.asArray().clear();
                            for (int i = 0; i < dataPrincipalList.size(); i++) {
                                for (int j = 0; j < strings.size(); j++) {
                                    if (dataPrincipalList.get(i).get("NAMA").asString().equals(strings.get(j))) {
                                        if (!loadPrincipalList.contains(dataPrincipalList.get(i).get("NAMA").asString())) {
                                            principalSaveList.add(Nson.newObject()
                                                    .set("PRINCIPAL_ID", dataPrincipalList.get(i).get("ID"))
                                                    .set("NAMA_PRINCIPAL", dataPrincipalList.get(i).get("NAMA"))
                                            );
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void getImageBase64() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(activity.getApplicationContext(), "CID", "").trim();
                //logo
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_LOGO_BENGKEL), args));
                result = result.get("data");

                String base64String = result.get("LOGO_IMAGE").asString();
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                bitmapLogo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                base64String = result.get("TAMPAK_DEPAN_IMAGE").asString();
                decodedString = Base64.decode(base64String, Base64.DEFAULT);
                bitmapTampakDepan = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            @Override
            public void runUI() {
                if (bitmapLogo != null)
                    btnLogo.setText("PREVIEW LOGO");
                if (bitmapTampakDepan != null)
                    btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
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

    public void getImagePickerByGalerryOrCamera() {
        final List<Intent> intents = new ArrayList<>();
        intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

        Intent result = Intent.createChooser(intents.remove(0), null);
        result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        startActivityForResult(result, REQUEST_FOTO);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOTO) {
            Bundle extras = null;
            Uri imageUri = null;
            if (data != null) {
                extras = data.getExtras();
                if (extras == null)
                    imageUri = data.getData();
            }
            if (imageUri != null) {
                try {
                    if (isLogo) {
                        bitmapLogo = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                    } else {
                        if (isTampakDepan) {
                            bitmapTampakDepan = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (isLogo) {
                    bitmapLogo = (Bitmap) (extras != null ? extras.get("data") : null);
                } else {
                    if (isTampakDepan) {
                        bitmapTampakDepan = (Bitmap) (extras != null ? extras.get("data") : null);
                    }
                }
            }

            fotoLogoBase64 = activity.bitmapToBase64(bitmapLogo);
            fotoTampakDepanBase64 = activity.bitmapToBase64(bitmapTampakDepan);

            if (bitmapLogo != null)
                btnLogo.setText("PREVIEW LOGO");
            if (bitmapTampakDepan != null)
                btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
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
                        getImagePickerByGalerryOrCamera();
                    } else if (bitmap == bitmapTampakDepan) {
                        isLogo = false;
                        isTampakDepan = true;
                        getImagePickerByGalerryOrCamera();
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
        cursor.close();
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
