package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Otp_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.APIUrls;

import java.util.Map;

public class VerifikasiOtp_Activity extends AppActivity {

    private final long IDDLE_REFRESH = 60 * 1000;
    CountDownTimer refreshOtpTimer;

    private boolean isRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_otp);
        initComponent();
        setTimerRefresh();
    }


    private void initComponent() {
        find(R.id.tv_timer_refresh).setVisibility(View.GONE);
        find(R.id.cv_refresh_otp).setEnabled(false);
        find(R.id.btn_lanjut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.et_no_ponsel, EditText.class).getText().toString().isEmpty() ||
                        find(R.id.et_no_ponsel, EditText.class).getText().toString().length() <= 13) {
                    find(R.id.et_no_ponsel, EditText.class).setError("NO. PONSEL HARUS DI ISI");
                } else {
                    requestOtp();
                }
            }
        });

        find(R.id.cv_refresh_otp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOtp();
            }
        });

        find(R.id.et_no_ponsel, EditText.class).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !find(R.id.et_no_ponsel, EditText.class).getText().toString().contains("+62 ")) {
                    find(R.id.et_no_ponsel, EditText.class).setText("+62 ");
                }
            }
        });

        find(R.id.et_no_ponsel, EditText.class).addTextChangedListener(new TextWatcher() {
            int prevLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.et_no_ponsel, EditText.class).removeTextChangedListener(this);
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) return;
                if (counting < 4 && !find(R.id.et_no_ponsel, EditText.class).getText().toString().contains("+62 ")) {
                    find(R.id.et_no_ponsel, EditText.class).setTag(null);
                    find(R.id.et_no_ponsel, EditText.class).setText("+62 ");
                    Selection.setSelection(find(R.id.et_no_ponsel, EditText.class).getText(),
                            find(R.id.et_no_ponsel, EditText.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.et_no_ponsel, EditText.class).requestFocus();
                }

                find(R.id.et_no_ponsel, EditText.class).addTextChangedListener(this);
            }
        });
    }

    private void setTimerRefresh(){
        refreshOtpTimer =  new CountDownTimer(IDDLE_REFRESH, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long timer) {
                find(R.id.tv_timer_refresh).setVisibility(View.VISIBLE);
                int seconds = (int) (timer / 1000) % 60;
                String time = String.format("%02d", seconds);
                find(R.id.tv_timer_refresh, TextView.class).setText(time);
            }

            @Override
            public void onFinish() {
                find(R.id.cv_refresh_otp).setEnabled(true);
                refreshOtpTimer.cancel();
            }
        };
    }

    private void requestOtp() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "requestOtp");
                args.put("nohp", NumberFormatUtils.formatOnlyNumber(find(R.id.et_no_ponsel, EditText.class).getText().toString()));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(APIUrls.SET_REGISTRASI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Intent intent = new Intent(getActivity(), Otp_Activity.class);
                    intent.putExtra("REGISTRASI", "NEW");
                    intent.putExtra("NO_PONSEL", NumberFormatUtils.formatOnlyNumber(find(R.id.et_no_ponsel, EditText.class).getText().toString()));
                    startActivity(intent);
                    finish();
                } else {
                    if(result.get("message").asString().toLowerCase().contains("duplicate")){
                        showError("NO PONSEL YANG ANDA MASUKKAN TELAH MENDAFTAR");
                    }else{
                        showError(result.get("message").asString());
                    }
                }
            }
        });
    }

}