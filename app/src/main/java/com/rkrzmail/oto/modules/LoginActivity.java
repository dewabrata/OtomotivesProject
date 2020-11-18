package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.RegistrasiBengkel_Activity;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_LOGIN;
import static com.rkrzmail.utils.APIUrls.VIEW_DATA_BENGKEL;

public class LoginActivity extends AppActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        initComponent();
        find(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.user, EditText.class).getText().toString().isEmpty() || find(R.id.tl_user, TextInputLayout.class).isHelperTextEnabled()) {
                    showWarning("Nomor Handphone Harus Di isi");
                    find(R.id.user, EditText.class).requestFocus();
               /* } else if (find(R.id.password, EditText.class).getText().toString().isEmpty()) {
                    showWarning("Otp Harus Di isi");
                    find(R.id.password, EditText.class).requestFocus();*/
                } else {
                    //login();//lanjut
                   requestOtp();


                }
            }
        });
        if(getSetting("noponsel") != null){
            find(R.id.user, EditText.class).setText(getSetting("noponsel"));
        }
    }

    private void initComponent() {
        String tittle = "<font color=#F44336><u>Registrasi</u></font>";
        find(R.id.registrasi, TextView.class).setText(Html.fromHtml(tittle));
        find(R.id.registrasi, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), RegistrasiBengkel_Activity.class), 10);
            }
        });

        find(R.id.user, EditText.class).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus && !find(R.id.user, EditText.class).getText().toString().startsWith("+62")){
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
                }else if (counting < 12) {
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
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Request");
                args.put("user", formatOnlyNumber(find(R.id.user, EditText.class).getText().toString()));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_LOGIN), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Request OTP, Silahkan Login");
                    // find(R.id.password, EditText.class).setText("123456");

                    Intent intent = new Intent(getActivity(), Otp_Activity.class);
                    intent.putExtra("user",  formatPhone(  find(R.id.user, EditText.class).getText().toString().replaceAll("[^0-9]+", "")));
                    startActivity(intent);
                } else {
                    if(result.get("message").asString().contains("Gagal")){
                        showError(result.get("message").asString());
                        return;
                    }
                    showError("Gagal Request OTP");
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
        phone = Utility.replace(phone," ","");
        return phone.trim();
    }

    private void login() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            String sResult;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Login");
                args.put("user", formatPhone(find(R.id.user, EditText.class).getText().toString()));
                sResult = (InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_LOGIN), args));
            }

            @Override
            public void runUI() {
                Nson nson = Nson.readNson(sResult);//test1
                if (nson.get("status").asString().equalsIgnoreCase("OK")) {
                    if (nson.get("data").get(0).get("status").asString().equalsIgnoreCase("error")) {
                        showError("User tidak di temukan / password salah");
                        return;
                    }

                    nson = nson.get("data").get(0);
                    if(nson.get("TIPE_USER").asString().equals("MEKANIK") || nson.get("MEKANIK").asString().equals("YA")){
                        setSetting("MEKANIK", "TRUE");
                    }else{
                        setSetting("MEKANIK", "FALSE");
                    }
                    setSetting("L", "L");
                    setSetting("NAMA_BENGKEL", nson.get("NAMA_BENGKEL").asString());
                    setSetting("JENIS_KENDARAAN", nson.get("JENIS_KENDARAAN").asString().trim());
                    setSetting("result", nson.toJson());
                    setSetting("CID", nson.get("CID").asString());
                    setSetting("USER_ID", nson.get("USER_ID").asString());
                    viewDataBengkel();
                    setSetting("NAMA_USER", nson.get("NAMA_USER").asString());
                    setSetting("TIPE_USER", nson.get("TIPE_USER").asString());
                    setSetting("ACCESS_MENU", nson.get("AKSES_APP").asString());
                    setSetting("JENIS_KENDARAAN_BENGKEL", nson.get("JENIS_KENDARAAN_BENGKEL").asString());
                    setSetting("MERK_KENDARAAN_BENGKEL", nson.get("MERK_KENDARAAN").asString());
                    setSetting("KATEGORI_BENGKEL", nson.get("KATEGORI_BENGKEL").asString());
                    setSetting("userId", nson.get("USER_ID").asString());
                    setSetting("session", nson.get("token").asString());
                    setSetting("user", formatOnlyNumber(find(R.id.user, EditText.class).getText().toString()));
                    Intent intent = new Intent(getActivity(), Otp_Activity.class);
                    intent.putExtra("user",  formatPhone(  find(R.id.user, EditText.class).getText().toString().replaceAll("[^0-9]+", "")));
                    startActivity(intent);
                } else {
                    showError(nson.get("error").asString());
                }
            }
        });
    }

    private void viewDataBengkel(){
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Data Bengkel");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_DATA_BENGKEL), args));
                if(result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    setSetting("MAX_ANTRIAN_EXPRESS_MENIT", result.get("MAX_ANTRIAN_EXPRESS_MENIT").asString());
                    setSetting("MAX_ANTRIAN_STANDART_MENIT", result.get("MAX_ANTRIAN_STANDART_MENIT").asString());
                    setSetting("DP_PERSEN", result.get("DP_PERSEN").asString());
                }
            }
            @Override
            public void runUI() {

            }
        });
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
