package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.RekeningBank_MainTab_Activity;
import com.rkrzmail.srv.MultipartRequest;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ENTRY_EWALLET;
import static com.rkrzmail.utils.APIUrls.GET_BANK_BY_CID;
import static com.rkrzmail.utils.APIUrls.GET_EWALLET;
import static com.rkrzmail.utils.APIUrls.GET_QRIS_IMAGE;
import static com.rkrzmail.utils.APIUrls.MST_EWALLET;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;

public class EWallet_Rekening_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private EditText etNamaMerchant, etMerchantID, etNoRekening, etNamaRekening, etOnUs, etOffUs;
    private Spinner spNamaEwallet, spRekeningIn;


    private String noRek = "", namaBank = "", namaRekening = "";
    private String namaEwallet = "", kodeEwallet = "", mdrOnUs = "", mdrOffUs = "";
    private String bitmapQrisBase64 = "";
    private String eWalletID = "";

    private Bitmap bitmapQris;
    File tempQrisFile = null;
    String fileNameQrisFile = "qris.png";
    private AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.ewallet_rekening_fragment, container, false);
        activity = (RekeningBank_MainTab_Activity) getActivity();

        initHideToolbar();
        initComponent();
        getDataEwallet();

        return fragmentView;
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initComponent() {
        spNamaEwallet = fragmentView.findViewById(R.id.sp_nama_wallet);
        etNamaMerchant = fragmentView.findViewById(R.id.et_nama_merchant);
        etMerchantID = fragmentView.findViewById(R.id.et_merchant_id);
        spRekeningIn = fragmentView.findViewById(R.id.sp_rekening_internal);
        etNamaRekening = fragmentView.findViewById(R.id.et_nama_rekening);
        etNoRekening = fragmentView.findViewById(R.id.et_no_rekening);
        etOffUs = fragmentView.findViewById(R.id.et_mdr_off_us);
        etOnUs = fragmentView.findViewById(R.id.et_mdr_on_us);

        etOffUs.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etOffUs));
        etOnUs.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etOnUs));

        Button btnFotoQris = fragmentView.findViewById(R.id.btn_foto_qris);
        Button btnSimpan = fragmentView.findViewById(R.id.btn_simpan);

        btnFotoQris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmapQris == null) {
                    getImagePickOrCamera();
                } else {
                    showDialogPreviewFoto(true);
                }

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (namaEwallet.isEmpty()) {
                    activity.setErrorSpinner(spNamaEwallet, "NAMA E-WALLET HARUS DI PILIH");
                } else if (etNamaMerchant.getText().toString().isEmpty()) {
                    etNamaMerchant.setError("NAMA MERCHANT HARUS DI ISI");
                    activity.viewFocus(etNamaMerchant);
                } else if (etMerchantID.getText().toString().isEmpty()) {
                    etMerchantID.setError("MERCHANT ID HARUS DI ISI");
                    activity.viewFocus(etMerchantID);
                } else if (namaBank.isEmpty()) {
                    activity.setErrorSpinner(spRekeningIn, "NAMA BANK HARUS DI PILIH");
                } else if (bitmapQris == null) {
                    activity.showWarning("FOTO QRIS HARUS DI MASUKKAN");
                } else {
                    saveData();
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
            activity.getSupportActionBar().setTitle("Preview Qris");

        if (bitmapQris != null)
            img.setImageBitmap(bitmapQris);

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
                    if (bitmapQris != null) {
                        bitmapQris = null;
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
                            if (bitmapQris != null) {
                                bitmapQrisBase64 = activity.bitmapToBase64(bitmapQris);
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


    private void getImagePickOrCamera() {
        final List<Intent> intents = new ArrayList<>();
        intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

        Intent result = Intent.createChooser(intents.remove(0), null);
        result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        startActivityForResult(result, REQUEST_FOTO);
    }

    private void getDataEwallet() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(activity.getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_EWALLET), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                } else {
                    activity.showInfo(result.get("message").asString());
                }

                eWalletID = result.get("ID").asString();
                etOffUs.setText(result.get("MDR_OFF_US").asString());
                etOnUs.setText(result.get("MDR_ON_US").asString());
                etNoRekening.setText(result.get("NO_REKENING").asString());
                etNamaRekening.setText(result.get("NAMA_REKENING").asString());
                etNamaMerchant.setText(result.get("NAMA_MERCHANT").asString());
                etMerchantID.setText(result.get("MERCHANT_ID").asString());
                setSpNamaEwallet(result.get("NAMA_EWALLET").asString());
                setSpRekening(result.get("NAMA_BANK").asString() + " - " + result.get("NO_REKENING").asString());
                getQrisImage();
            }
        });
    }

    private void setSpNamaEwallet(final String selection) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(activity.getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(MST_EWALLET), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    ArrayList<String> str = new ArrayList<>();
                    final Nson dataEwallet = Nson.newArray();
                    result = result.get("data");
                    str.add("--PILIH--");
                    dataEwallet.add("");
                    dataEwallet.asArray().addAll(result.asArray());
                    for (int i = 0; i < result.size(); i++) {
                        str.add(result.get(i).get("NAMA_EWALLET").asString());
                    }

                    ArrayList<String> newStr = Tools.removeDuplicates(str);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spNamaEwallet.setAdapter(adapter);
                    spNamaEwallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (adapterView.getItemAtPosition(i).toString().equals(dataEwallet.get(i).get("NAMA_EWALLET").asString())) {
                                namaEwallet = dataEwallet.get(i).get("NAMA_EWALLET").asString();
                                kodeEwallet = dataEwallet.get(i).get("KODE_EWALLET").asString();
                            } else {
                                namaEwallet = "";
                                kodeEwallet = "";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spNamaEwallet.getCount(); i++) {
                            if (spNamaEwallet.getItemAtPosition(i).toString().equals(selection)) {
                                spNamaEwallet.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void setSpRekening(final String selection) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(activity.getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_BANK_BY_CID), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    final Nson dataBank = Nson.newArray();
                    ArrayList<String> str = new ArrayList<>();
                    result = result.get("data");
                    dataBank.add("");
                    str.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        dataBank.add(Nson.newObject()
                                .set("ID", result.get(i).get("ID"))
                                .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                                .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                                .set("EDC", result.get(i).get("EDC_ACTIVE"))
                                .set("OFF_US", result.get(i).get("OFF_US"))
                                .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));

                        str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, str);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRekeningIn.setAdapter(adapter);
                    spRekeningIn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (adapterView.getItemAtPosition(i).toString().equals(dataBank.get(i).get("COMPARISON").asString())) {
                                namaBank = dataBank.get(i).get("BANK_NAME").asString();
                                noRek = dataBank.get(i).get("NO_REKENING").asString();
                                namaRekening = dataBank.get(i).get("NAMA_REKENING").asString();
                            } else {
                                namaBank = "";
                                noRek = "";
                                namaRekening = "";
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spRekeningIn.getCount(); i++) {
                            if (spRekeningIn.getItemAtPosition(i).toString().equals(selection)) {
                                spRekeningIn.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    activity.showError("GAGAL MEMUAT DATA BANK INTERNAL");
                }
            }
        });
    }

    private void getQrisImage() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_QRIS_IMAGE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                    if (!result.get("QRIS_IMAGE").asString().isEmpty()) {
                        String base64String = result.get("QRIS_IMAGE").asString();
                        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                        bitmapQris = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    }
                }
            }
        });

    }

    private void saveData() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            String response = "";

            @Override
            public void run() {
                MultipartRequest formBody = new MultipartRequest(activity.getActivity());

                formBody.addString("CID", UtilityAndroid.getSetting(activity.getApplicationContext(), "CID", "").trim());
                formBody.addString("namaEwallet", namaEwallet);
                formBody.addString("kodeEwallet", kodeEwallet);
                formBody.addString("namaRekening", namaRekening);
                formBody.addString("noRekening", noRek);
                formBody.addString("merchantId", etMerchantID.getText().toString());
                formBody.addString("namaMerchant", etNamaMerchant.getText().toString());
                formBody.addString("namaBank", namaBank);
                formBody.addString("mdrOnUs", NumberFormatUtils.clearPercent(etOnUs.getText().toString()));
                formBody.addString("mdrOffUs", NumberFormatUtils.clearPercent(etOffUs.getText().toString()));
                //formBody.addImageFile("fotoQris", tempQrisFile.getAbsolutePath(), fileNameQrisFile);
                formBody.addString("eWalletID", eWalletID);
                formBody.addString("qrisBase64", activity.bitmapToBase64(bitmapQris));

                response = formBody.execute(AppApplication.getBaseUrlV4(ENTRY_EWALLET));
            }

            @Override
            public void runUI() {
                if (response != null && !response.isEmpty()) {
                    activity.showSuccess(eWalletID.isEmpty() ? "BERHASIL MENAMBAHKAN DATA" : "BERHASIL MEMPERBARUI DATA");
                }
            }
        });
    }

    private void getImageUri(final Uri imageUri, final Bundle imgBundle){
        activity.newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    try {
                        bitmapQris = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    bitmapQris = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                }

                if (bitmapQris != null) {
                    tempQrisFile = new File(Objects.requireNonNull(getContext()).getCacheDir(), fileNameQrisFile);
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        bitmapQris.compress(Bitmap.CompressFormat.PNG, 100, bos);
                        byte[] bitmapdata = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(tempQrisFile);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void runUI() {
                activity.showSuccess(tempQrisFile.getAbsolutePath(), Toast.LENGTH_LONG);
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
