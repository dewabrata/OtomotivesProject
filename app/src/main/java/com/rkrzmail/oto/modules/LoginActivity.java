package com.rkrzmail.oto.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.registrasi_bengkel.RegistrasiBengkel_Activity;

import java.util.Map;

public class LoginActivity extends AppActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);

        initComponent();
        find(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(find(R.id.user, EditText.class).getText().toString().isEmpty()){
                   showWarning("Nomor Handphone Harus Di isi");
                }else if(find(R.id.password, EditText.class).getText().toString().isEmpty()){
                    showWarning("Otp Harus Di isi");
                }else{
                    login();
                }
            }
        });
    }

    private void initComponent(){
        String tittle = "<font color=#F44336><u>Registrasi</u></font>";
        String requestOtp = "<font color=#F44336><u>Request OTP ?</u></font>";
        find(R.id.registrasi, TextView.class).setText(Html.fromHtml(tittle));
        find(R.id.registrasi, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), RegistrasiBengkel_Activity.class), 10);
            }
        });
        find(R.id.otp, TextView.class).setText(Html.fromHtml(requestOtp));
        find(R.id.otp, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(find(R.id.user, EditText.class).getText().toString().isEmpty()){
                    showWarning("Masukkan User");
                    find(R.id.user, EditText.class).requestFocus();
                    return;
                }
                requestOtp();
            }
        });
    }

    private void requestOtp(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Request");
                args.put("user", formatPhone(find(R.id.user, EditText.class).getText().toString()));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("login"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Request OTP, Silahkan Login");
                    find(R.id.password, EditText.class).setText("123456");
                } else {
                    showError("Gagal Request OTP");
                }
            }
        });
    }
    private String formatPhone(String phone){
        if (phone.startsWith("+62")){
            phone = phone.substring(1);
        }else if (phone.startsWith("0")){
            phone = "62" + phone.substring(1);
        }
         
        return  Utility.getNumberOnly(phone.trim());
    }
    private void login() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            String sResult;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Login");
                args.put("user", formatPhone(find(R.id.user, EditText.class).getText().toString()));
                args.put("password", find(R.id.password, EditText.class).getText().toString());

                sResult = (InternetX.postHttpConnection(AppApplication.getBaseUrlV3("login"), args));
            }

            @Override
            public void runUI() {
                Nson nson = Nson.readNson(sResult);//test1
                if (nson.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Login");
                    nson = nson.get("data").get(0);
                    setSetting("L", "L");
                    setSetting("result", nson.toJson());
                    setSetting("CID", nson.get("CID").asString());
                    setSetting("NAMA_USER", nson.get("NAMA_USER").asString());
                    setSetting("NAMA_BENGKEL", nson.get("NAMA_BENGKEL").asString());
                    setSetting("TIPE_USER", nson.get("TIPE_USER").asString());
                    setSetting("ACCESS_MENU", nson.get("ACCESS_MENU").asString());
                    setSetting("session", nson.get("token").asString());
                    setSetting("user", find(R.id.user, EditText.class).getText().toString());
                    Log.d("Login__", "runUI: " + nson);
                    Log.d("Login__", "nama: " + getSetting("NAMA"));
                    Intent intent = new Intent(getActivity(), MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showError(nson.get("error").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 10){
            showInfo("Akun telah di buat Silahkan Login");
            find(R.id.user, EditText.class).requestFocus();
        }
    }
}
