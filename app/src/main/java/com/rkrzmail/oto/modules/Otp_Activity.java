package com.rkrzmail.oto.modules;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

public class Otp_Activity extends AppActivity {

    private char[] otp;
    private String one, two, three, four, five;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
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
                    find(R.id.et1, EditText.class).clearFocus();
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
                if(!one.equals("") && !two.equals("") && !three.equals("") && !four.equals("") && !five.equals("")){
                    find(R.id.tv_reqOtp, TextView.class).performClick();
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
                args.put("user", find(R.id.user, EditText.class).getText().toString().replaceAll("[^0-9]+", ""));
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
}