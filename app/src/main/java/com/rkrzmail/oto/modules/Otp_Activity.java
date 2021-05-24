package com.rkrzmail.oto.modules;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import com.rkrzmail.utils.APIUrls;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_LOGIN;
import static com.rkrzmail.utils.APIUrls.VIEW_DATA_BENGKEL;

public class Otp_Activity extends AppActivity {

    private char[] otp;
    private String one, two, three, four, five, six;
    private boolean isRegist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setContentView(R.layout.activity_otp);

        isRegist = getIntent().hasExtra("REGISTRASI");

        find(R.id.et1, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                one = s.toString();
                if (s.length() == 1) {
                    find(R.id.et2, EditText.class).requestFocus();
                } else if (s.length() == 0) {
                    find(R.id.et1, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et2, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                two = s.toString();
                if (s.length() == 1) {
                    find(R.id.et3, EditText.class).requestFocus();
                } else if (s.length() == 0) {
                    find(R.id.et1, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et3, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                three = s.toString();
                if (s.length() == 1) {
                    find(R.id.et4, EditText.class).requestFocus();
                } else if (s.length() == 0) {
                    find(R.id.et2, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et4, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                four = s.toString();
                if (s.length() == 1) {
                    find(R.id.et5, EditText.class).requestFocus();
                } else if (s.length() == 0) {
                    find(R.id.et3, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et5, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                five = s.toString();
                if (s.length() == 1) {
                    find(R.id.et6, EditText.class).requestFocus();
                } else if (s.length() == 0) {
                    find(R.id.et4, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et6, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    find(R.id.et6, EditText.class).clearFocus();
                } else if (s.length() == 0) {
                    find(R.id.et5, EditText.class).requestFocus();
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(((EditText)findViewById(R.id.et1)).getText().toString());
                stringBuilder.append(((EditText)findViewById(R.id.et2)).getText().toString());
                stringBuilder.append(((EditText)findViewById(R.id.et3)).getText().toString());
                stringBuilder.append(((EditText)findViewById(R.id.et4)).getText().toString());
                stringBuilder.append(((EditText)findViewById(R.id.et5)).getText().toString());
                stringBuilder.append(((EditText)findViewById(R.id.et6)).getText().toString());
                String dummy = stringBuilder.toString();

                if (dummy.length()==6){
                    if(isRegist){
                        checkOtpRegistasi(dummy);
                    }else{
                        login(dummy);
                    }
                }else{
                    showError("Lengkapi Request OTP");
                }


            }
        });

        String tittle = "<font color=#F44336><u>Kirim Ulang OTP</u></font>";
        find(R.id.tv_reqOtp, TextView.class).setText(Html.fromHtml(tittle));
        find(R.id.tv_reqOtp, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOtp();
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
                args.put("user",   getIntentStringExtra("user"));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("login"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Request OTP, Silahkan Login");

                } else {
                    showError("Gagal Request OTP");
                }
            }
        });
    }

    private void login(final String otp) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            String sResult;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Login");
                args.put("user",   getIntentStringExtra("user"));
                args.put("password", otp);

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
                    setSetting("STATUS_BENGKEL", nson.get("STATUS_BENGKEL").asString());
                    setSetting("PEMBAYARAN_ACTIVE", nson.get("PEMBAYARAN_ACTIVE").asString());
                    setSetting("NAMA_BENGKEL", nson.get("NAMA_BENGKEL").asString());
                    setSetting("JENIS_KENDARAAN", nson.get("JENIS_KENDARAAN").asString().trim());
                    setSetting("result", nson.toJson());
                    setSetting("CID", nson.get("CID").asString());
                    viewDataBengkel();
                    setSetting("NAMA_USER", nson.get("NAMA_USER").asString());
                    setSetting("TIPE_USER", nson.get("TIPE_USER").asString());
                    setSetting("ACCESS_MENU", nson.get("AKSES_APP").asString());
                    setSetting("MERK_KENDARAAN_BENGKEL", nson.get("MERK_KENDARAAN").asString());
                    setSetting("KATEGORI_BENGKEL", nson.get("KATEGORI_BENGKEL").asString());
                    setSetting("USER_ID", nson.get("USER_ID").asString());
                    setSetting("userId", nson.get("USER_ID").asString());
                    setSetting("session", nson.get("token").asString());
                    setSetting("user", formatOnlyNumber(  getIntentStringExtra("user") ));
                    setSetting("POSISI", nson.get("POSISI").asString());

                    Intent intent = new Intent(getActivity(), MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showError(nson.get("error").asString());
                }
            }
        });
    }

    private void checkOtpRegistasi(final String kodeOtp){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "checkOtp");
                args.put("nohp",   getIntentStringExtra("NO_PONSEL"));
                args.put("otp", kodeOtp);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(APIUrls.SET_REGISTRASI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Silahkan Lanjutkan Pendaftaran!");
                    Intent intent = new Intent(getActivity(), RegistrasiBengkel_Activity.class);
                    intent.putExtra("NO_PONSEL", getIntentStringExtra("NO_PONSEL"));
                    startActivity(intent);
                    finish();
                } else {
                    showError("Gagal Request OTP");
                }
            }
        });
    }

    private void clearAndSetFocus(final EditText editText, final EditText editText2){
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.KEYCODE_DEL){
                    editText.clearFocus();
                    editText2.requestFocus();
                }
                return false;
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
}