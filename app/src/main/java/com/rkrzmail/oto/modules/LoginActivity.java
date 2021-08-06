package com.rkrzmail.oto.modules;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.BuildConfig;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Task.UpdateAppTask;
import com.rkrzmail.oto.modules.bengkel.VerifikasiOtp_Activity;
import com.rkrzmail.srv.DialogOto;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.rkrzmail.oto.MenuActivity.REQUEST_APP_UPDATE;
import static com.rkrzmail.utils.APIUrls.SET_LOGIN;

public class LoginActivity extends AppActivity {

    DialogOto dialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);

        checkAndRequestPermissions();
        if(reqStoragePermission()){
            UpdateAppTask updateAppTask = new UpdateAppTask(getActivity());
            updateAppTask.excuteVersionChecker(getActivity());
        }else{
            reqStoragePermission();
        }

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        initComponent();
        find(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.user, EditText.class).getText().toString().isEmpty()
                        || find(R.id.tl_user, TextInputLayout.class).isHelperTextEnabled()) {
                    showWarning("Nomor Handphone Harus Di isi");
                    find(R.id.user, EditText.class).requestFocus();
                } else {
                    String noPonsel = find(R.id.user, EditText.class).getText().toString().replace(" ", "");
                    String message = "Nomor Ponsel Sudah Benar ? " + "\n" + "\n" + Html.fromHtml(noPonsel);
                    dialog = new DialogOto(getActivity());
                    dialog.setMessage(message);
                    dialog.onClickOk(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestOtp();
                        }
                    });
                    if (dialog.getWindow() != null)
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.show();
                }
            }
        });

        if (getSetting("noponsel") != null) {
            find(R.id.user, EditText.class).setText(getSetting("noponsel"));
        }

        if (getIntent().hasExtra("NO_PONSEL")) {
            find(R.id.user, EditText.class).setText(getIntentStringExtra("NO_PONSEL"));
        }
    }

    private boolean reqStoragePermission(){
        List<String> listPermissionsNeeded = new ArrayList<>();
        int WritePermision = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalPermission = ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE);

        if(WritePermision != PackageManager.PERMISSION_GRANTED || readExternalPermission != PackageManager.PERMISSION_GRANTED){
            if (WritePermision != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (readExternalPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(READ_EXTERNAL_STORAGE);
            }
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int WritePermision = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalPermission = ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            StringBuilder mssgResult = new StringBuilder();
            for (String permission : permissions) {
                if ((permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && WritePermision != PackageManager.PERMISSION_GRANTED) ||
                        (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE) && readExternalPermission != PackageManager.PERMISSION_GRANTED)) {
                    mssgResult.append("Penyimpanan, ");
                }
            }
            if(!mssgResult.toString().isEmpty())
                showWarning("Ijinkan Aplikasi Untuk Mengakses " + mssgResult + ", di Pengaturan", Toast.LENGTH_LONG);
        }
    }

    private void initComponent() {
        String tittle = "<font color=#F44336><u>Registrasi</u></font>";
        find(R.id.registrasi, TextView.class).setText(Html.fromHtml(tittle));
        find(R.id.registrasi, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getActivity(), RegistrasiBengkel_Activity.class));

                startActivity(new Intent(getActivity(), VerifikasiOtp_Activity.class));
            }
        });

        find(R.id.user, EditText.class).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus && !find(R.id.user, EditText.class).getText().toString().startsWith("+62")) {
                    find(R.id.user, EditText.class).setText("+62 ");
                    find(R.id.user, EditText.class).setSelection(find(R.id.user, EditText.class).getText().toString().length());
                }
            }
        });

        find(R.id.user, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_user, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.user, EditText.class).removeTextChangedListener(this);
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) {
                    find(R.id.tl_user, TextInputLayout.class).setErrorEnabled(false);
                }else if(counting < 4){
                    find(R.id.user, EditText.class).setText("+62 ");
                    Selection.setSelection(find(R.id.user, EditText.class).getText(), find(R.id.user, EditText.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_user, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.user, EditText.class).requestFocus();
                } else {
                    find(R.id.tl_user, TextInputLayout.class).setErrorEnabled(false);
                }
                find(R.id.user, EditText.class).addTextChangedListener(this);
            }
        });

    }

    private void requestOtp() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                dialog.dismiss();
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Request");
                args.put("user", formatOnlyNumber(find(R.id.user, EditText.class).getText().toString()));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_LOGIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Request OTP, Silahkan Masukkan OTP Kembali");

                    Intent intent = new Intent(getActivity(), Otp_Activity.class);
                    intent.putExtra("user", formatPhone(find(R.id.user, EditText.class).getText().toString()));
                    startActivity(intent);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private String formatPhone(String phone) {
        if (phone.startsWith("+62")) {
            phone = phone.substring(1);
        } else if (phone.startsWith("0")) {
            phone = "62" + phone.substring(1);
        }
        phone = Utility.replace(phone, " ", "");
        return phone.trim();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 10) {
            showInfo("Akun telah di buat Silahkan Login");
            find(R.id.user, EditText.class).setText(data.getStringExtra("NO_PONSEL"));
            find(R.id.user, EditText.class).requestFocus();
        }
    }
}
